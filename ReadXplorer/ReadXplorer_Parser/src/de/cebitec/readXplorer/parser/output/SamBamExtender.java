package de.cebitec.readXplorer.parser.output;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParserI;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.common.RefSeqFetcher;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SamUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Extends a SAM/BAM file !!sorted by read sequence!! with ReadXplorer classification
 * information (perfect, best and common match classes), adds the number of for
 * occurrences of each mapping and sorts the whole file by coordinate again.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamExtender implements ConverterI, ParserI, Observable, Observer, MessageSenderI {

    private Map<String, ParsedClassification> classificationMap;
    private TrackJob trackJob;
    private static String name = "Sam/Bam Extender";
    private static String[] fileExtension = new String[]{"bam", "BAM", "Bam", "sam", "SAM", "Sam"};
    private static String fileDescription = "SAM/BAM Input, extended SAM/BAM Output";
    private List<Observer> observers;
    private Map<String, Integer> chromLengthMap;
    private ErrorLimit errorLimit;
    private RefSeqFetcher refSeqFetcher;

    /**
     * Extends a SAM/BAM file !!sorted by read sequence!! with ReadXplorer
     * classification information (perfect, best and common match classes), adds
     * the number of for occurences of each mapping and sorts the whole file by
     * coordinate again.
     * @param classificationMap the classification map to store for the reads
     */
    public SamBamExtender(Map<String, ParsedClassification> classificationMap) {
        this.classificationMap = classificationMap;
        this.errorLimit = new ErrorLimit(100);
    }

    /**
     * The main method of the extender: Converting the old data !!sorted by read
     * sequence!! in the enriched and extended data. The extended sam/bam data 
     * is stored in a new file, which is then set as the trackJob's file.
     * @throws ParsingException
     */
    @Override
    public boolean convert() throws ParsingException {
        return this.extendSamBamFile();
    }

    /**
     * @param data A SamBamExtender needs exactly two arguments:
     * - trackJob the track job including a sam or bam file for
     * extension with more data.
     * - chromLengthMap mapping of chromosome names to their length
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setDataToConvert(Object... data) {
        this.observers = new ArrayList<>();
        this.chromLengthMap = new HashMap<>();
        boolean works = true;
        if (data.length >= 2) {
            if (data[0] instanceof TrackJob) {
                this.trackJob = (TrackJob) data[0];
            } else {
                works = false;
            }
            if (this.chromLengthMap.getClass().equals(data[1].getClass())) {
                this.chromLengthMap = (Map<String, Integer>) data[1];
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
    private boolean extendSamBamFile() throws ParsingException {
        File fileToExtend = trackJob.getFile();

        File outputFile;
        SAMFileWriter samBamFileWriter;

        try (SAMFileReader samBamReader = new SAMFileReader(fileToExtend)) {
            this.refSeqFetcher = new RefSeqFetcher(trackJob.getRefGen().getFile(), this);

            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samBamItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);

            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(
                    fileToExtend, header, false, SamUtils.EXTENDED_STRING);
            
            samBamFileWriter = writerAndFile.getFirst();
            outputFile = writerAndFile.getSecond();
            trackJob.setFile(outputFile);

            int lineno = 0;
            SAMRecord record;
            String readSeq;
            String refSeq;
            String cigar;
            int start;
            int stop;
            int differences;
   
            while (samBamItor.hasNext()) {
                ++lineno;

                try {
                    record = samBamItor.next();
                    if (!record.getReadUnmappedFlag() && chromLengthMap.containsKey(record.getReferenceName())) {
                        cigar = record.getCigarString();
                        readSeq = record.getReadString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();

                        if (!CommonsMappingParser.checkReadSam(this, readSeq, chromLengthMap.get(record.getReferenceName()), 
                                cigar, start, stop, fileToExtend.getName(), lineno)) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }
                        
                        refSeq = refSeqFetcher.getSubSequence(record.getReferenceName(), start, stop);
                        
                        //count differences to reference
                        differences = CommonsMappingParser.countDiffsAndGaps(cigar, readSeq, refSeq, record.getReadNegativeStrandFlag());

                        CommonsMappingParser.addClassificationData(record, differences, classificationMap);
                    }

                    samBamFileWriter.addAlignment(record);
                } catch (NumberFormatException e) {
                    this.notifyObservers("Or last incomplete record is ignored.");
                    Exceptions.printStackTrace(e);
                } catch (RuntimeEOFException e) {
                    this.notifyObservers("Last record in file is incomplete! Ignoring last record."); 
                } catch (SAMFormatException e) {
                    if (!e.getMessage().contains("MAPQ should be 0")) {
                        this.notifyObservers(e.getMessage());
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
                } catch (AssertionError e) {
                    this.notifyObservers(e.getMessage());
                } catch (Exception e) {
                    this.notifyObservers(e.getMessage());
                    Exceptions.printStackTrace(e);
                }
            }
            
            samBamItor.close();
            samBamFileWriter.close();
        }
        
        boolean success = false;
        try (SAMFileReader samReaderNew = new SAMFileReader(outputFile)) {
            samReaderNew.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SamUtils utils = new SamUtils();
            utils.registerObserver(this);
            success = utils.createIndex(samReaderNew, new File(outputFile + Properties.BAM_INDEX_EXT));
        }

        return success;
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
    public void sendMsgIfAllowed(String msg) {
        if (this.errorLimit.allowOutput()) {
            this.notifyObservers(msg);
        }
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
