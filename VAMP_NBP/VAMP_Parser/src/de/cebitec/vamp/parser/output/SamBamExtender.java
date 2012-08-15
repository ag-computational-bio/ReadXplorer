package de.cebitec.vamp.parser.output;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.samtools.*;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Extends a SAM/BAM file !!sorted by read sequence!! with VAMP classification
 * information (perfect, best and common match classes), adds the number of for
 * occurences of each mapping and sorts the whole file by coordinate again.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamExtender implements ConverterI, ParserI, Observable, Observer {

    private Map<String, Pair<Integer, Integer>> classificationMap;
    private TrackJob trackJob;
    private static String name = "Sam/Bam Extender";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam", "sam", "SAM", "Sam"};
    private static String fileDescription = "SAM/BAM Input, extended SAM/BAM Output";
    private List<Observer> observers;
    private String refGenome;
    private int refSeqLength;

    /**
     * Extends a SAM/BAM file !!sorted by read sequence!! with VAMP
     * classification information (perfect, best and common match classes), adds
     * the number of for occurences of each mapping and sorts the whole file by
     * coordinate again.
     * @param classificationMap the classification map to store for the reads
     */
    public SamBamExtender(Map<String, Pair<Integer, Integer>> classificationMap) {
        this.classificationMap = classificationMap;
    }

    /**
     * The main method of the extender: Converting the old data !!sorted by read
     * sequence!! in the enriched and extended data. The extended sam/bam data 
     * is stored in a new file, which is then set as the trackJob's file.
     * @throws ParsingException
     */
    @Override
    public void convert() throws ParsingException {
        this.extendSamBamFile();
    }

    /**
     * @param trackJob Sets the track job including a sam or bam file for
     * extension with more data.
     * @param refGenome the reference genome belonging to the trackJob
     */
    public void setDataToConvert(TrackJob trackJob, String refGenome) {
        this.observers = new ArrayList<>();
        this.trackJob = trackJob;
        this.refGenome = refGenome;
        this.refSeqLength = this.refGenome.length();
    }

    /**
     * Carries out the extension of the bam file from the track job and stores
     * it in a new file.
     * @throws ParsingException
     */
    private void extendSamBamFile() throws ParsingException {

        File fileToExtend = trackJob.getFile();
        String fileName = fileToExtend.getName();
        String lastReadSeq = "";
        int lastStartPos = -1;
        int noReads = 0;
        int noSequences = 0;
        int noUniqueMappings = 0;
        int noBestMatch = 0;
        int noPerfect = 0;

        this.notifyObservers(NbBundle.getMessage(SamBamExtender.class, "Converter.Convert.Start", fileName));
        File outputFile;
        SAMFileWriter samBamFileWriter;

        try (SAMFileReader samBamReader = new SAMFileReader(fileToExtend)) {

            SAMRecordIterator samBamItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);

            
//commented out because: we currently don't allow to write sam files, only bam! (more efficient)
            
            //determine writer type (sam or bam):
//            String[] nameParts = fileName.split(".");
//            String extension;
//            try {
//                extension = nameParts[nameParts.length - 1];
//            } catch (ArrayIndexOutOfBoundsException e) {
//                extension = "bam";
//            }

            SAMFileWriterFactory factory = new SAMFileWriterFactory();
//            if (extension.toLowerCase().contains("sam")) {
//                outputFile = new File(fileToExtend.getAbsolutePath() + "_extended.sam");
//                samBamFileWriter = factory.makeSAMWriter(header, false, outputFile);
//            } else {
                outputFile = new File(fileToExtend.getAbsolutePath() + "_extended.bam");
                samBamFileWriter = factory.makeBAMWriter(header, false, outputFile);
//            }

            trackJob.setFile(outputFile);

            int lineno = 0;
            SAMRecord record;
            String readSeq;
            String refSeq;
            int seqMatches;
            int lowestDiffRate;
            String cigar;
            int start;
            int stop;
            int differences;
            String readName;
            Pair<Integer, Integer> data;
            List<String> readNamesSameSeq = new ArrayList<>();
            List<Integer> readsDifferentPos = new ArrayList<>();

            while (samBamItor.hasNext()) {
                ++lineno;

                try {
                    record = samBamItor.next();
                    if (!record.getReadUnmappedFlag()) {
                        cigar = record.getCigarString();
                        readSeq = record.getReadString();
                        readName = record.getReadName();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        refSeq = this.refGenome.substring(start - 1, stop);

                        if (!ParserCommonMethods.checkRead(this, readSeq, this.refSeqLength, cigar, start, stop, fileName, lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }
                        
                        //statistics claculations: count no reads and distinct sequences
                        if (!lastReadSeq.equals(readSeq)) {
                            ++noSequences;
                            noReads += readNamesSameSeq.size();
                            if (readsDifferentPos.size() == 1) {
                                ++noUniqueMappings;
                            }
                            readNamesSameSeq.clear();
                            readsDifferentPos.clear();
                        }
                        if (!readNamesSameSeq.contains(readName)) {
                            readNamesSameSeq.add(readName);
                        }
                        if (!readsDifferentPos.contains(start)) {
                            readsDifferentPos.add(start);
                        }
                        lastReadSeq = readSeq;
                        /////////////////////////////////////////////////////////////////

                        //count differences to reference
                        differences = ParserCommonMethods.countDiffsAndGaps(cigar, readSeq, refSeq, record.getReadNegativeStrandFlag(), start);

                        data = this.classificationMap.get(readName);
                        if (data != null) {
                            seqMatches = data.getFirst(); //number matches for read name
                            lowestDiffRate = data.getSecond(); //lowest error no for read name
                        } else {
                            seqMatches = 1;
                            lowestDiffRate = differences;
                        }

                        if (differences == 0) { //perfect mapping
                            record.setAttribute(Properties.TAG_READ_CLASS, Properties.PERFECT_COVERAGE);
                            ++noPerfect;
                            ++noBestMatch;

                        } else if (differences == lowestDiffRate) { //best match mapping
                            record.setAttribute(Properties.TAG_READ_CLASS, Properties.BEST_MATCH_COVERAGE);
                            ++noBestMatch;

                        } else if (differences > lowestDiffRate) { //common mapping
                            record.setAttribute(Properties.TAG_READ_CLASS, Properties.COMPLETE_COVERAGE);

                        } else { //meaning: differences < lowestDiffRate
                            this.notifyObservers("Cannot contain less than the lowest diff rate number of errors!");
                        }
                        record.setAttribute(Properties.TAG_MAP_COUNT, seqMatches);

                        //set sequence pair information in case this is a sequence pair data set
                        //                    if (seqPairData) {
                        //                        readNameLength = readName.length() - 1;
                        //                        lastChar = readName.charAt(readNameLength);
                        //                        readName = readName.substring(0, readNameLength); //keep in mind that name was changed here
                        //                        
                        //                        if (lastChar == SeqPairProcessorI.EXT_A1 || lastChar == SeqPairProcessorI.EXT_B1) {
                        //                            if (seqIdToReadNameMap1.containsKey(readName)) {
                        //                                
                        //                                record.setAttribute(SeqPairProcessorI.TAG_SEQ_PAIR_ID, seqIdToReadNameMap1.get(readName));
                        //                                record.setAttribute(SeqPairProcessorI.TAG_SEQ_PAIR_TYPE, name); //TODO: get type and correct id
                        //                            }
                        //                        } else if (lastChar == SeqPairProcessorI.EXT_A2 || lastChar == SeqPairProcessorI.EXT_B2) {
                        //                            if (seqIdToReadNameMap2.containsKey(readName)) {
                        //                                
                        //                                record.setAttribute(SeqPairProcessorI.TAG_SEQ_PAIR_ID, seqIdToReadNameMap2.get(readName));
                        //                                record.setAttribute(SeqPairProcessorI.TAG_SEQ_PAIR_TYPE, name); //TODO: get type and correct id
                        //                            }
                        //                        }
                        //                        
                        //                    }

                    }

                    samBamFileWriter.addAlignment(record);
                } catch (NumberFormatException | RuntimeEOFException e) {
                    this.notifyObservers("Last record in file is incomplete! Ignoring last record.");
                }
            }
            
            samBamItor.close();
            samBamFileWriter.close();
        }

        SAMFileReader samReaderNew = new SAMFileReader(outputFile);
        SamUtils utils = new SamUtils();
        utils.registerObserver(this);
        utils.createIndex(samReaderNew, new File(outputFile + ".bai"));
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }

    @Override
    public void update(Object args) {
        this.notifyObservers(args);
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }
}
