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

    private Map<Integer, Pair<Integer, Integer>> classificationMap;
    private TrackJob trackJob;
    private static String name = "Sam/Bam Extender";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam", "sam", "SAM", "Sam"};
    private static String fileDescription = "SAM/BAM Input, extended SAM/BAM Output";
    private List<Observer> observers;
    private String refGenome;
    private int refSeqLength;

    /**
     * Extends a SAM/BAM file !!sorted by read sequence!! with VAMP classification
     * information (perfect, best and common match classes), adds the number 
     * of for occurences of each mapping and sorts the whole file by coordinate again.
     */
    public SamBamExtender(Map<Integer, Pair<Integer, Integer>> classificationMap) {
        this.classificationMap = classificationMap;
    }

    /**
     * The main method of the extender: Converting the old data in the enriched
     * and extended data. The extended sam/bam data is stored in a new file, which
     * is then set as the trackJob's file.
     * @throws ParsingException 
     */
    @Override
    public void convert() throws ParsingException {
        this.extendSamBamFile();
    }

    /**
     * @param samBamFile Sets the track job including a sam or bam file for
     *      extension with more data.
     */
    public void setDataToConvert(TrackJob trackJob, String refGenome) {
        this.observers = new ArrayList<Observer>();
        this.trackJob = trackJob;
        this.refGenome = refGenome;
        this.refSeqLength = this.refGenome.length();
    }

    /**
     * Carries out the extension of the bam file from the track job and stores it
     * in a new file.
     * @throws ParsingException 
     */
    private void extendSamBamFile() throws ParsingException {
        File fileToExtend = trackJob.getFile();
        String fileName = fileToExtend.getName();
        String lastReadSeq = "";
        int noReads = 0;
        int seqId = -1;

        this.notifyObservers(NbBundle.getMessage(SamBamExtender.class, "Converter.Convert.Start", fileName));

        SAMFileReader samBamReader = new SAMFileReader(fileToExtend);
        SAMRecordIterator samBamItor = samBamReader.iterator();
        SAMFileHeader header = samBamReader.getFileHeader();
        header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
        File outputFile;
        SAMFileWriter samBamFileWriter;
        
        //determine writer type (sam or bam):
        String[] nameParts = fileName.split(".");
        String extension = nameParts[nameParts.length - 1];
        
        SAMFileWriterFactory factory = new SAMFileWriterFactory();
        if (extension.toLowerCase().contains("sam")) {
            outputFile = new File(fileToExtend.getAbsolutePath() + "_extended.sam");
            samBamFileWriter = factory.makeSAMWriter(header, false, outputFile);
        } else {
            outputFile = new File(fileToExtend.getAbsolutePath() + "_extended.bam");
            samBamFileWriter = factory.makeBAMWriter(header, false, outputFile);
        }
        
        trackJob.setFile(outputFile);

        int lineno = 0;
        SAMRecord record;
        String readSeq = null;
        String refSeq = null;
        int seqMatches = 0;
        int lowestDiffRate = Integer.MAX_VALUE;
        String cigar = null;
        int start;
        int stop;
        int differences = 0;
        while (samBamItor.hasNext()) {
            ++lineno;

            try {
                record = samBamItor.next();
                if (!record.getReadUnmappedFlag()) {
                    cigar = record.getCigarString();
                    readSeq = record.getReadString().toLowerCase();
                    if (!lastReadSeq.equals(readSeq)) {
                        ++seqId;
                    }
                    lastReadSeq = readSeq;

                    //count differences to reference
                    if (cigar.contains("M")) {
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();

                        if (refSeqLength < start || refSeqLength < stop) {
                            this.notifyObservers(NbBundle.getMessage(SamBamExtender.class,
                                    "Parser.checkMapping.ErrorReadPosition",
                                    fileName, lineno, start, stop, refSeqLength));
                            continue;
                        }
                        if (start >= stop) {
                            this.notifyObservers(NbBundle.getMessage(SamBamExtender.class,
                                    "Parser.checkMapping.ErrorStartStop", fileName, lineno, start, stop));
                            continue;
                        }

                        refSeq = this.refGenome.substring(start - 1, stop).toLowerCase(); //TODO: test if -1 is correct

                        if (refSeq == null || refSeq.isEmpty()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamExtender.class,
                                    "Parser.checkMapping.ErrorRef", fileName, lineno, refSeq));
                            continue;
                        }
                        if (readSeq.length() != refSeq.length()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamExtender.class,
                                    "Parser.checkMapping.ErrorReadLength", fileName, lineno, readSeq, refSeq));
                            continue;
                        }

                        differences = ParserCommonMethods.countDifferencesToRef(cigar, readSeq, refSeq);


                    } else //the convenient case, that no "M"'s are present in the cigar
                    if (cigar.contains("X") || cigar.contains("D") || cigar.contains("I")
                            || cigar.contains("S") || cigar.contains("N") || cigar.contains("P")) {

                        differences = ParserCommonMethods.countDifferencesToRef(cigar);

                    } else {
                        differences = 0;
                    }

                    Pair<Integer, Integer> data = this.classificationMap.get(seqId);
                    if (data != null) {
                        seqMatches = data.getFirst(); //no matches for read seq
                        lowestDiffRate = data.getSecond(); //lowest error no for read seq
                    } else {
                        seqMatches = 1;
                        lowestDiffRate = differences;
                    }

                    if (differences == 0) { //perfect mapping
                        record.setAttribute("Yc", Properties.PERFECT_COVERAGE);

                    } else if (differences == lowestDiffRate) { //best match mapping
                        record.setAttribute("Yc", Properties.BEST_MATCH_COVERAGE);

                    } else if (differences > lowestDiffRate) { //common mapping
                        record.setAttribute("Yc", Properties.COMPLETE_COVERAGE);

                    } else { //meaning: differences < lowestDiffRate
                        this.notifyObservers("Cannot contain less than the lowest diff rate number of errors!");
                    }
                    record.setAttribute("Yt", seqMatches);
                    if (record.getReferenceIndex() != 0) {
                        System.out.println("Ref index: " + record.getReferenceIndex());
                    }

                }

                samBamFileWriter.addAlignment(record);
            } catch (RuntimeEOFException e) {
                //do nothing and ignore read, send error msg later
            }
        }
        
        samBamItor.close();
        samBamReader.close();
        samBamFileWriter.close();

        SAMFileReader samReaderNew = new SAMFileReader(outputFile);
        SamUtils utils = new SamUtils();
        utils.registerObserver(this);
        utils.createIndex(samReaderNew, new File(outputFile + ".bai"), this);

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
    public String getParserName() {
        return name;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }
}
