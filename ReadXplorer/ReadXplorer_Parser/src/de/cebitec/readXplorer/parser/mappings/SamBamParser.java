/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.common.RefSeqFetcher;
import de.cebitec.readXplorer.parser.output.SamBamSorter;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SamUtils;
import de.cebitec.readXplorer.util.StatsContainer;
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
 * Sam/Bam parser for the data needed for a sam/bam track. This means the
 * classification of the reads is carried out and an extended bam file
 * containing the classification information in the SAMRecords is stored on the
 * disk next to the original file. Original files are not changed!
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamParser implements MappingParserI, Observer, MessageSenderI {

    private static String name = "SAM/BAM Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM/BAM Read Mappings";
    
    private List<Observer> observers;
    private StatsContainer statsContainer;
    private ErrorLimit errorLimit;
    private RefSeqFetcher refSeqFetcher;
    private boolean deleteSortedFile;

    /**
     * Sam/Bam parser for the data needed for a sam/bam track. This means the
     * classification of the reads is carried out and an extended bam file
     * containing the classification information in the SAMRecords is stored on
     * the disk next to the original file. Original files are not changed!
     */
    public SamBamParser() {
        this.observers = new ArrayList<>();
        this.statsContainer = new StatsContainer();
        this.statsContainer.prepareForTrack();
        this.errorLimit = new ErrorLimit(100);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    /**
     * Does nothing, as the sam bam parser currently does not need any conversions.
     * @param trackJob
     * @param chromLengthMap the mapping of chromosome name to chromosome length
     * for this track
     * @return true
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean convert(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError {
        return true;
    }

    /**
     * Sorts the input sam/bam file contained in the track job by read name
     * and stores the sorted data in a new file next to the input file.
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        SamBamSorter sorter = new SamBamSorter();
        sorter.registerObserver(this);
        boolean success = sorter.sortSamBam(trackJob, SAMFileHeader.SortOrder.queryname, SamUtils.SORT_READNAME_STRING);
        this.deleteSortedFile = success;
        sorter.removeObserver(this);
        return success;
    }

    /**
     * First calls the preprocessing method, which sorts the input sam/bam file
     * contained in the track job by read name and then classifies the read
     * mapping classes of the input determined by the track job. After the 
     * classification is done, a new extended bam file is created next to the
     * original one, containing the SAMRecords including the classification 
     * data.
     * @param trackJob the track job to parse
     * @param chromLengthMap the map of chromosome names to chromosome sequence
     * @return a direct access data container constisting of:
     * a classification map: The key is the readname and each name
     * links to a pair consisting of the number of occurrences of the read name
     * in the dataset (no mappings) and the lowest diff rate among all hits.
     * Remember that replicates are not needed, they can be deduced from the 
     * reads querried from an interval!
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean parseInput(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError {
        
        //new algorithm:
       /* 1. sort by read name
        * 2. iterate all mappings, store record data including diffs for all with same read name
        * 3. when read name finished: add mappings to bam writer with classification
        * 4. CommonsMappingParser.addClassificationData(record, differences, classificationMap);
        * 5. clear data structures and contine with next read name...
        */
                
        this.refSeqFetcher = new RefSeqFetcher(trackJob.getRefGen().getFile(), this);
        Boolean success = this.preprocessData(trackJob);
        if (!success) {
            throw new ParsingException("Sorting of the input file by read name was not successful, please try again and make sure to have enough "
                    + "free space in your systems temp directory to store intermediate files for sorting (e.g. on Windows 7 the hard disk containing: "
                    + "C:\\Users\\UserName\\AppData\\Local\\Temp needs to have enough free space).");
        }
        
        File fileSortedByReadName = trackJob.getFile(); //sorted by read name bam file
        File outputFile;
        long startTime = System.currentTimeMillis();
        long finish;
        this.notifyObservers(NbBundle.getMessage(SamBamParser.class, "Parser.Parsing.Start", fileSortedByReadName.getName()));

        int lineno = 0;
        int noReads = 0;
        int noSkippedReads = 0;

        try (SAMFileReader samReader = new SAMFileReader(trackJob.getFile())) {
            samReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMFileHeader.SortOrder sortOrder = samReader.getFileHeader().getSortOrder();
            SAMRecordIterator samItor = samReader.iterator();
            
            SAMFileHeader header = samReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(
                    fileSortedByReadName, header, false, SamUtils.EXTENDED_STRING);
            SAMFileWriter bamWriter = writerAndFile.getFirst();
            outputFile = writerAndFile.getSecond();
            trackJob.setFile(outputFile);

            //record and read name specific variables
            SAMRecord record; 
            String readName;
            String lastReadName = "";
            Map<SAMRecord, Integer> diffMap = new HashMap<>(); //mapping of record to number of differences
            ParsedClassification classificationData = new ParsedClassification(sortOrder); //classification data for all reads with same read name

            while (samItor.hasNext()) {
                try {
                    ++lineno;
                    record = samItor.next();
                    
                    if (!record.getReadUnmappedFlag() && chromLengthMap.containsKey(record.getReferenceName())) {
                        
                        readName = record.getReadName();
                        //store data and clear data structure, if new read name is reached - file needs to be sorted by read name
                        if (!lastReadName.equals(readName)) {
                            CommonsMappingParser.writeSamRecord(diffMap, classificationData, bamWriter);
                            classificationData = new ParsedClassification(sortOrder);
                            ++noReads;
                        }
                        
                        boolean classified = CommonsMappingParser.classifyRead(record, this, chromLengthMap, 
                                fileSortedByReadName, lineno, refSeqFetcher, diffMap, classificationData);
                        if (!classified) { 
                            ++noSkippedReads;
                            continue; //continue, and ignore read, if it contains inconsistent information
                        } 
                        lastReadName = readName;

                        
                    } else { // else read is unmapped or belongs to another reference
                        if (record.getReadUnmappedFlag()) {
                            this.sendMsgIfAllowed(NbBundle.getMessage(SamBamParser.class,
                                    "Parser.Parsing.Unmapped", lineno, record.getSAMString()));
                        } else {
                            this.sendMsgIfAllowed(NbBundle.getMessage(SamBamParser.class,
                                    "Parser.Parsing.WrongReference", lineno, record.getSAMString()));
                        }
                    }
                } catch (SAMFormatException e) {
                    if (!e.getMessage().contains("MAPQ should be 0")) {
                        this.sendMsgIfAllowed(NbBundle.getMessage(SamBamParser.class,
                                "Parser.Parsing.CorruptData", lineno, e.toString()));
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                }
                
                if (lineno % 500000 == 0) {
                    finish = System.currentTimeMillis();
                    this.notifyObservers(Benchmark.calculateDuration(startTime, finish, lineno + " mappings processed in "));
                }
            }
            
            CommonsMappingParser.writeSamRecord(diffMap, classificationData, bamWriter);
            ++noReads;
            
            if (errorLimit.getSkippedCount() > 0) {
                this.notifyObservers("... " + errorLimit.getSkippedCount() + " more errors occurred");
            }

            this.notifyObservers("Writing extended bam file...");
            samItor.close();
            bamWriter.close();
            samReader.close();
            
            //delete the sorted/preprocessed file
            if (deleteSortedFile) {
                GeneralUtils.deleteOldWorkFile(fileSortedByReadName);
            }
            
            try (SAMFileReader samReaderNew = new SAMFileReader(outputFile)) {
                samReaderNew.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
                SamUtils utils = new SamUtils();
                utils.registerObserver(this);
                success = utils.createIndex(samReaderNew, new File(outputFile + Properties.BAM_INDEX_EXT));
            }
        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in the file is incomplete, ignoring it.");
        } catch (Exception e) {
            this.notifyObservers(e.getMessage() != null ? e.getMessage() : e);
            Exceptions.printStackTrace(e);
        }

        this.notifyObservers("Reads skipped during parsing due to inconsistent data: " + noSkippedReads);
        finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamParser.class, "Parser.Parsing.Successfully", fileSortedByReadName.getName());
        this.notifyObservers(Benchmark.calculateDuration(startTime, finish, msg));
        statsContainer.increaseValue(StatsContainer.NO_READS, noReads);

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
    
    /**
     * Sends the given msg to all observers, if the error limit is not already
 reached for this instance of SamBamParser.
     * @param msg The message to send
     */
    @Override
    public void sendMsgIfAllowed(String msg) {
        if (this.errorLimit.allowOutput()) {
            this.notifyObservers(msg);
        }
    }
    
    /**
     * Adds a statistics container for handling statistics for the extended track.
     * @param statsContainer the container
     */
    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }
}
