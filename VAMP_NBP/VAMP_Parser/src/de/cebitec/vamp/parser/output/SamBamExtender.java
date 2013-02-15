package de.cebitec.vamp.parser.output;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.*;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Extends a SAM/BAM file !!sorted by read sequence!! with VAMP classification
 * information (perfect, best and common match classes), adds the number of for
 * occurrences of each mapping and sorts the whole file by coordinate again.
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
    private Map<Integer, Integer> mappingInfos;

    /**
     * Extends a SAM/BAM file !!sorted by read sequence!! with VAMP
     * classification information (perfect, best and common match classes), adds
     * the number of for occurences of each mapping and sorts the whole file by
     * coordinate again.
     * @param classificationMap the classification map to store for the reads
     */
    public SamBamExtender(Map<String, Pair<Integer, Integer>> classificationMap) {
        this.classificationMap = classificationMap;
        this.mappingInfos = new HashMap<>();
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
     * A SamBamExtender needs exactly two arguments:
     * - trackJob the track job including a sam or bam file for
     * extension with more data.
     * - refGenome the reference genome belonging to the trackJob
     */
    @Override
    public void setDataToConvert(Object... data) {
        this.observers = new ArrayList<>();
        boolean works = true;
        if (data.length >= 2) {
            if (data[0] instanceof TrackJob) {
                this.trackJob = (TrackJob) data[0];
            } else {
                works = false;
            }
            if (data[1] instanceof String) {
                this.refGenome = (String) data[1];
                this.refSeqLength = this.refGenome.length();
            } else {
                works = false;
            }
        } else {
            works = false;
        }
        if (!works) {
            throw new IllegalArgumentException(NbBundle.getMessage(JokToBamConverter.class, 
                    "Converter.SetDataError", "Inappropriate arguments passed to the converter!"));
        }
    }

    /**
     * Carries out the extension of the bam file from the track job and stores
     * it in a new file.
     * @throws ParsingException
     */
    private void extendSamBamFile() throws ParsingException {

        File fileToExtend = trackJob.getFile();

        File outputFile;
        SAMFileWriter samBamFileWriter;

        try (SAMFileReader samBamReader = new SAMFileReader(fileToExtend)) {

            SAMRecordIterator samBamItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);

            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(fileToExtend, header, false, "_extended.bam");
            samBamFileWriter = writerAndFile.getFirst();
            outputFile = writerAndFile.getSecond();
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

                        if (!ParserCommonMethods.checkRead(this, readSeq, this.refSeqLength, cigar, start, stop, fileToExtend.getName(), lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }
                        
                        //count differences to reference
                        differences = ParserCommonMethods.countDiffsAndGaps(cigar, readSeq, refSeq, record.getReadNegativeStrandFlag());

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

                        } else if (differences == lowestDiffRate) { //best match mapping
                            record.setAttribute(Properties.TAG_READ_CLASS, Properties.BEST_MATCH_COVERAGE);

                        } else if (differences > lowestDiffRate) { //common mapping
                            record.setAttribute(Properties.TAG_READ_CLASS, Properties.COMPLETE_COVERAGE);

                        } else { //meaning: differences < lowestDiffRate
                            this.notifyObservers("Cannot contain less than the lowest diff rate number of errors!");
                        }
                        record.setAttribute(Properties.TAG_MAP_COUNT, seqMatches);
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
        utils.createIndex(samReaderNew, new File(outputFile + Properties.BAM_INDEX_EXT));
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
