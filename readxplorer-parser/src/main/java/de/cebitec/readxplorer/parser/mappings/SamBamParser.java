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

package de.cebitec.readxplorer.parser.mappings;


import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedClassification;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.common.RefSeqFetcher;
import de.cebitec.readxplorer.parser.output.SamBamSorter;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.ErrorLimit;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.MessageSenderI;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.SamUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
import de.cebitec.readxplorer.utils.sequence.RefDictionary;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.RuntimeEOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * Sam/Bam parser for the data needed for a sam/bam track. This means the
 * classification of the reads is carried out and an extended bam file
 * containing the classification information in the SAMRecords is stored on the
 * disk next to the original file. Original files are not changed!
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamParser implements MappingParserI, Observer, MessageSenderI {

    private static final String NAME = "SAM/BAM Parser";
    private static final String[] FILE_EXTENSIONS = new String[]{ "sam", "SAM", "Sam", "bam", "BAM", "Bam" };
    private static final String FILE_DESCRIPTION = "SAM/BAM Read Mappings";

    private final List<Observer> observers;
    private StatsContainer statsContainer;
    private final ErrorLimit errorLimit;
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
        this.errorLimit = new ErrorLimit( 100 );
    }


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public String getInputFileDescription() {
        return FILE_DESCRIPTION;
    }


    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSIONS;
    }


    /**
     * Does nothing, as the sam bam parser currently does not need any
     * conversions.
     * <p>
     * @param trackJob       The track job to parse
     * @param chromLengthMap the mapping of chromosome NAME to chromosome length
     *                       for this track
     * <p>
     * @return true
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean convert( TrackJob trackJob, Map<String, Integer> chromLengthMap ) throws ParsingException, OutOfMemoryError {
        return true;
    }


    /**
     * Sorts the input sam/bam file contained in the track job by read NAME and
     * stores the sorted data in a new file next to the input file.
     * <p>
     * @param trackJob the trackjob to preprocess
     * <p>
     * @return true, if the method succeeded, false otherwise
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean preprocessData( final TrackJob trackJob ) throws ParsingException, OutOfMemoryError {
        SamBamSorter sorter = new SamBamSorter();
        sorter.registerObserver( this );
        boolean success = sorter.sortSamBam( trackJob, SAMFileHeader.SortOrder.queryname, SamUtils.SORT_READNAME_STRING );
        this.deleteSortedFile = success;
        sorter.removeObserver( this );
        return success;
    }


    /**
     * First calls the preprocessing method, which sorts the input sam/bam file
     * contained in the track job by read NAME and then classifies the read
     * mapping classes of the input determined by the track job. After the
     * classification is done, a new extended bam file is created next to the
     * original one, containing the SAMRecords including the classification
     * data.
     * <p>
     * @param trackJob       the track job to parse
     * @param chromLengthMap the map of chromosome names to chromosome sequence
     * <p>
     * @return a direct access data container constisting of: a classification
     *         map: The key is the readname and each NAME links to a pair
     *         consisting of the number of occurrences of the read NAME in the
     *         dataset (no mappings) and the lowest diff rate among all hits.
     *         Remember that replicates are not needed, they can be deduced from
     *         the reads querried from an interval!
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    @NbBundle.Messages( { "# {0} - lineNo",
                          "Parser.Parsing.Start=Start parsing mappings from file {0}",
                          "# {0} - fileName",
                          "Parser.Parsing.Successfully=Mapping data successfully parsed from {0}.",
                          "# {0} - lineNo",
                          "# {1} - sam string of the record",
                          "Parser.Parsing.Unmapped=The current read in line {0} is unmapped {1}",
                          "# {0} - lineNo",
                          "# {1} - sam string of the record",
                          "Parser.Parsing.WrongReference=The current read in line {0} is mapped to a different reference (not present in the selected one) {1}" } )
    public Boolean parseInput( final TrackJob trackJob, final Map<String, Integer> chromLengthMap ) throws ParsingException, OutOfMemoryError {

        //new algorithm:
        // 1.
        // sort by read NAME 
        // 2. iterate all mappings, store record data including diffs for all
        // with same read NAME 
        // 3. when read NAME finished: add mappings to bam writer with
        // classification 
        // 4. CommonsMappingParser.addClassificationData(record, differences,
        // classificationMap); 
        // 5. clear data structures and continue with next read NAME... 

        this.refSeqFetcher = new RefSeqFetcher( trackJob.getRefGen().getFile(), this );
        Boolean success = this.preprocessData( trackJob );
        if( !success ) {
            throw new ParsingException( "Sorting of the input file by read name was not successful. Please either switch your RX temp directory " +
                                        "(Tools->Options->Miscellaneous->Directories) to a disk with sufficient space or make sure to have " +
                                        "enough free space in your systems temp directory to store intermediate files for sorting (e.g. on " +
                                        "Windows 7 the standard disk and folder: " +
                                        "C:\\Users\\UserName\\AppData\\Local\\Temp needs to have enough free space)." );
        }

        final File fileSortedByReadName = trackJob.getFile(); //sorted by read NAME bam file
        final long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        this.notifyObservers( Bundle.Parser_Parsing_Start( fileSortedByReadName.getName() ) );


        int noReads = 0;
        int noSkippedReads = 0;
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        try( final SamReader samReader = samReaderFactory.open( fileSortedByReadName ); ) {

            SAMFileHeader.SortOrder sortOrder = samReader.getFileHeader().getSortOrder();
            SAMFileHeader header = samReader.getFileHeader();
            header.setSortOrder( SAMFileHeader.SortOrder.coordinate );
            RefDictionary refDictionary = trackJob.getSequenceDictionary();
            if( refDictionary != null && refDictionary instanceof SamSeqDictionary ) {
                header.setSequenceDictionary( ((SamSeqDictionary) refDictionary).getSamDictionary() );
            }
            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(
                    fileSortedByReadName, header, false, SamUtils.EXTENDED_STRING );
            SAMFileWriter bamWriter = writerAndFile.getFirst();
            final File outputFile = writerAndFile.getSecond();
            trackJob.setFile( outputFile );

            //record and read NAME specific variables
            String lastReadName = "";
            Map<SAMRecord, Integer> diffMap1 = new HashMap<>( 1024 ); //mapping of record to number of differences
            Map<SAMRecord, Integer> diffMap2 = new HashMap<>( 1024 ); //mapping of record to number of differences
            ParsedClassification class1 = new ParsedClassification( sortOrder ); //classification data for all reads with same read NAME
            ParsedClassification class2 = new ParsedClassification( sortOrder );

            int lineno = 0;
            
            SAMRecordIterator samItor = samReader.iterator();
            while( samItor.hasNext() ) {
                try {
                    ++lineno;
                    final SAMRecord record = samItor.next();

                    if( !record.getReadUnmappedFlag() && chromLengthMap.containsKey( record.getReferenceName() ) ) {

                        String readName = record.getReadName();
                        //store data and clear data structure, if new read NAME is reached - file needs to be sorted by read NAME
                        if( !lastReadName.equals( readName ) ) {
                            CommonsMappingParser.writeSamRecord( diffMap1, class1, bamWriter );
                            CommonsMappingParser.writeSamRecord( diffMap2, class2, bamWriter );
                            class1 = new ParsedClassification( sortOrder );
                            class2 = new ParsedClassification( sortOrder );
                            ++noReads;
                        }

                        boolean classified;
                        if( record.getReadPairedFlag() && record.getSecondOfPairFlag() ) {
                            classified = CommonsMappingParser.classifyRead( record, this, chromLengthMap, fileSortedByReadName.getName(),
                                                                            lineno, refSeqFetcher, diffMap2, class2 );
                        } else {
                            classified = CommonsMappingParser.classifyRead( record, this, chromLengthMap, fileSortedByReadName.getName(),
                                                                            lineno, refSeqFetcher, diffMap1, class1 );
                        }

                        if( !classified ) {
                            ++noSkippedReads;
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }
                        lastReadName = readName;


                    } else { // else read is unmapped or belongs to another reference
                        if( record.getReadUnmappedFlag() ) {
                            this.sendMsgIfAllowed( Bundle.Parser_Parsing_Unmapped( lineno, record.getSAMString() ) );
                        } else {
                            this.sendMsgIfAllowed( Bundle.Parser_Parsing_WrongReference( lineno, record.getSAMString() ) );
                        }
                    }
                } catch( SAMFormatException | StringIndexOutOfBoundsException e ) {
                    if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                        this.sendMsgIfAllowed( NbBundle.getMessage( SamBamParser.class,
                                                                    "Parser.Parsing.CorruptData", lineno, e.toString() ) );
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                }

                if( lineno % 10000 == 0 ) {
                    long finish = System.currentTimeMillis();
                    if( finish - lastTime > 60000 || lineno % 500000 == 0 ) {
                        notifyObservers( Benchmark.calculateDuration( startTime, finish, lineno + " mappings processed in " ) );
                        lastTime = finish;
                    }
                }
                System.err.flush();
            }

            CommonsMappingParser.writeSamRecord( diffMap1, class1, bamWriter );
            CommonsMappingParser.writeSamRecord( diffMap2, class2, bamWriter );
            ++noReads;

            if( errorLimit.getSkippedCount() > 0 ) {
                this.notifyObservers( "... " + errorLimit.getSkippedCount() + " more errors occurred" );
            }

            this.notifyObservers( "Writing extended bam file..." );
            samItor.close();
            bamWriter.close();

            success = SamUtils.createBamIndex( outputFile, this );

        } catch( RuntimeEOFException e ) {
            this.notifyObservers( "Last read in the file is incomplete, ignoring it." );
        } catch( Exception e ) {
            this.notifyObservers( e.getMessage() != null ? e.getMessage() : e );
            Exceptions.printStackTrace( e );
        }

        //delete the sorted/preprocessed file
        if( deleteSortedFile ) {
            try {
                GeneralUtils.deleteOldWorkFile( fileSortedByReadName );
            } catch( IOException e ) {
                this.notifyObservers( e.getMessage() != null ? e.getMessage() : e );
                Exceptions.printStackTrace( e );
            }
        }

        this.notifyObservers( "Reads skipped during parsing due to inconsistent data: " + noSkippedReads );
        long finish = System.currentTimeMillis();
        String msg = Bundle.Parser_Parsing_Successfully( fileSortedByReadName.getName() );
        this.notifyObservers( Benchmark.calculateDuration( startTime, finish, msg ) );
        statsContainer.increaseValue( StatsContainer.NO_READS, noReads );

        return success;
    }


    @Override
    public void registerObserver( Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


    @Override
    public void update( Object args ) {
        this.notifyObservers( args );
    }


    /**
     * Sends the given msg to all observers, if the error limit is not already
     * reached for this instance of SamBamParser.
     * <p>
     * @param msg The message to send
     */
    @Override
    public void sendMsgIfAllowed( final String msg ) {
        if( this.errorLimit.allowOutput() ) {
            this.notifyObservers( msg );
        }
    }


    /**
     * Adds a statistics container for handling statistics for the extended
     * track.
     * <p>
     * @param statsContainer the container
     */
    @Override
    public void setStatsContainer( StatsContainer statsContainer ) {
        this.statsContainer = statsContainer;
    }


}
