package de.cebitec.vamp.parser.output;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SamUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.*;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Combines two mapping files (e.g. read 1 and read2 of the same pairs) in one
 * file. The first trackjob contains the new file name afterwards, while the
 * second trackjob contains an empty file name to prevent reuse of it after
 * the combination.
 *
 * @author -Rolf Hilker-
 */
public class SamBamCombiner implements CombinerI {
    
    private final TrackJob trackJob1;
    private final TrackJob trackJob2;
    private boolean sortCoordinate;
    private List<Observer> observers;
    private String referenceSeq;
    
    /**
     * Allows to combine two mapping files (e.g. read 1 and read2 of the same
     * pairs) in one file. The first trackjob contains the new file name
     * afterwards, while the second trackjob contains an empty file name to
     * prevent reuse of it after the combination. The merge process is started
     * by calling "combineData".
     * @param trackJob1 containing the first file before the merge and the new
     *      file name after the merge process
     * @param trackJob2 containing the second file, which is merged with the first
     *      an its file path is reset to an empty string afterwards
     * @param sortCoordinate true, if the combined file should be sorted by 
     * coordinate and false otherwise
     * @param referenceSeq the complete reference sequence 
     */
    public SamBamCombiner(TrackJob trackJob1, TrackJob trackJob2, boolean sortCoordinate, String referenceSeq) {
        this.trackJob1 = trackJob1;
        this.trackJob2 = trackJob2;
        this.sortCoordinate = sortCoordinate;
        this.observers = new ArrayList<>();
        this.referenceSeq = referenceSeq;
    }
    
    /**
     * Allows to combine two mapping files (e.g. read 1 and read2 of the same
     * pairs) in one file. The first trackjob contains the new file name
     * afterwards, while the second trackjob contains an empty file name to
     * prevent reuse of it after the combination.
     * @throws OutOfMemoryError 
     */
    @Override
    public boolean combineData() throws ParsingException, OutOfMemoryError {
        
        boolean success = true;
        
        long startTime = System.currentTimeMillis();
        //only proceed if the second track job contains a file
        File fileToExtend = trackJob1.getFile();
        File file2 = trackJob2.getFile();
        if (file2.exists()) { //if all reads already in same file this file is null and no combination needed
            String fileName = fileToExtend.getName();

            this.notifyObservers(NbBundle.getMessage(SamBamCombiner.class, "Combiner.Combine.Start", fileName + " and " + file2.getName()));

            SAMFileReader samBamReader = new SAMFileReader(fileToExtend);
            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samBamItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            if (sortCoordinate) {
                header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
            } else {
                header.setSortOrder(SAMFileHeader.SortOrder.unsorted);
            }

            //determine writer type (sam or bam):
            Pair<SAMFileWriter, File> writerAndFilePair = SamUtils.createSamBamWriter(
                    fileToExtend, header, !sortCoordinate, SamUtils.COMBINED_STRING);
            
            SAMFileWriter samBamFileWriter = writerAndFilePair.getFirst();
            File outputFile = writerAndFilePair.getSecond();
            trackJob1.setFile(outputFile);
            trackJob2.setFile(new File("")); //clean file to make sure, it is not used anymore

            this.readAndWrite(samBamItor, samBamFileWriter);
            samBamItor.close();
            samBamReader.close();

            samBamReader = new SAMFileReader(file2);
            samBamItor = samBamReader.iterator();
            this.readAndWrite(samBamItor, samBamFileWriter);
            samBamItor.close();
            samBamReader.close();
            samBamFileWriter.close();

            if (sortCoordinate) { 
                try (SAMFileReader samReaderNew = new SAMFileReader(outputFile)) { //close is performed by try statement
                    samReaderNew.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
                    SamUtils utils = new SamUtils();
                    utils.registerObserver(this);
                    success = utils.createIndex(samReaderNew, new File(outputFile + Properties.BAM_INDEX_EXT));
                    utils.removeObserver(this);
                }
            }
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamCombiner.class, "Combiner.Combine.Finished", fileName + " and " + file2.getName());
            this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));
        }
        
        return success;
    }
    
    /**
     * Carries out the actual I/O stuff. Observers are noticed in case a read 
     * cannot be processed.
     * @param samBamItor the iterator to read sam records from
     * @param samBamFileWriter the writer to write to
     */
    private void readAndWrite(SAMRecordIterator samBamItor, SAMFileWriter samBamFileWriter) {
        SAMRecord record = new SAMRecord(null);
        while (samBamItor.hasNext()) {
            try {
                record = samBamItor.next();
                samBamFileWriter.addAlignment(record);
            } catch (RuntimeEOFException e) {
                this.notifyObservers("Read could not be added to new file: " + record.getReadName());
            } catch (SAMFormatException e) {
                if (!e.getMessage().contains("MAPQ should be 0")) {
                    this.notifyObservers(e.getMessage());
                } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
            }
        }
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
    
}
