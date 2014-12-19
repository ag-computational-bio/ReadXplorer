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

package de.cebitec.readxplorer.readpairclassifier;


import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedClassification;
import de.cebitec.readxplorer.parser.common.ParsedReadPairContainer;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.common.RefSeqFetcher;
import de.cebitec.readxplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readxplorer.parser.mappings.ReadPairClassifierI;
import de.cebitec.readxplorer.parser.mappings.SamBamParser;
import de.cebitec.readxplorer.parser.output.SamBamSorter;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.DiscreteCountingDistribution;
import de.cebitec.readxplorer.utils.ErrorLimit;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.MessageSenderI;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.ReadPairType;
import de.cebitec.readxplorer.utils.SamUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * Sam/Bam read pair classifier for a direct file access track. This means
 * the classification of the read pairs has to be carried out. Besides the
 * classification this class also acts as extender for the given sam/bam file
 * and thus creates an extended copy of the original file after the
 * classification. The reads are be sorted by read name for efficient
 * classification. Note for multichromosomal mappings: The classification works,
 * no matter on which chromosome the reads were mapped!
 *
 * @author Rolf Hilker
 */
@ServiceProvider( service = ReadPairClassifierI.class )
public class SamBamReadPairClassifier implements ReadPairClassifierI, Observer,
                                                 Observable, MessageSenderI {

    private final ErrorLimit errorLimit;
    private final List<Observer> observers;
    private TrackJob trackJob;
    private int dist;
    private int minDist;
    private int maxDist;
    private short orienation; //orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
    private SAMFileWriter samBamWriter;
    private final Map<String, Integer> chromLengthMap;
    private boolean deleteSortedFile;
    private ParsedClassification class1;
    private ParsedClassification class2;

    StatsContainer statsContainer;
    private DiscreteCountingDistribution readPairSizeDistribution;
    private RefSeqFetcher refSeqFetcher;


    /**
     * Empty constructor, because nothing to do yet. But don't forget to set
     * data before calling classifyReadPairs().
     */
    public SamBamReadPairClassifier() {
        //set data later
        this.observers = new ArrayList<>();
        this.chromLengthMap = new HashMap<>();
        this.errorLimit = new ErrorLimit( 100 );
    }


    /**
     * Sam/Bam read pair classifier for a direct file access track. This
     * means the classification of the read pairs has to be carried out. Besides
     * the classification this class also acts as extender for the given sam/bam
     * file and thus creates an extended copy of the original file after the
     * classification. The reads are sorted by read name for efficient
     * classification. Note for multichromosomal mappings: The classification
     * works, no matter on which chromosome the reads were mapped!
     * <p>
     * @param readPairJobContainer the read pair job container to classify
     * @param chromLengthMap       the mapping of chromosome name to chromosome
     *                             sequence
     */
    public SamBamReadPairClassifier( ReadPairJobContainer readPairJobContainer, Map<String, Integer> chromLengthMap ) {
        this.observers = new ArrayList<>();
        this.trackJob = readPairJobContainer.getTrackJob1();
        this.dist = readPairJobContainer.getDistance();
        this.calculateMinAndMaxDist( dist, readPairJobContainer.getDeviation() );
        this.orienation = readPairJobContainer.getOrientation();
        this.chromLengthMap = chromLengthMap;
        this.errorLimit = new ErrorLimit( 100 );
        this.readPairSizeDistribution = new DiscreteCountingDistribution( maxDist * 3 );
        readPairSizeDistribution.setType( Properties.READ_PAIR_SIZE_DISTRIBUTION );
    }


    /**
     * Sorts the file by read name.
     * <p>
     * @param trackJob the trackjob to preprocess
     * <p>
     * @return true, if the method succeeded, false otherwise
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean preprocessData( TrackJob trackJob ) throws ParsingException, OutOfMemoryError {
        SamBamSorter sorter = new SamBamSorter();
        sorter.registerObserver( this );
        boolean success = sorter.sortSamBam( trackJob, SAMFileHeader.SortOrder.queryname, SamUtils.SORT_READNAME_STRING );
        this.deleteSortedFile = success;
        return success;
    }


    /**
     * First preprocesses the track job stored in this classifier by sorting it
     * by read name in this implementation and then classifies the seuqence
     * pairs. The reads have to be sorted by read name for efficient
     * classification.
     * <p>
     * @return an empty read pair container, because no data needs to be stored
     * <p>
     * @throws ParsingException
     */
    @Override
    @NbBundle.Messages( { "Classifier.Classification.Start=Starting read pair classification...",
                          "Classifier.Classification.Finish=Finished read pair classification. ",
                          "Classifier.Error=An error occured during the read pair classification: {0}" } )
    public ParsedReadPairContainer classifyReadPairs() throws ParsingException, OutOfMemoryError {

        this.refSeqFetcher = new RefSeqFetcher( trackJob.getRefGen().getFile(), this );
        boolean success = this.preprocessData( trackJob );
        if( !success ) {
            throw new ParsingException( "Sorting of the input file by read name was not successful, please try again and make sure to have enough "
                                        + "free space in your systems temp directory to store intermediate files for sorting (e.g. on Windows 7 the hard disk containing: "
                                        + "C:\\Users\\UserName\\AppData\\Local\\Temp needs to have enough free space)." );
        }
        final File oldWorkFile = trackJob.getFile();

        try {
            final long startTime = System.currentTimeMillis();
            long finish;
            this.notifyObservers( Bundle.Classifier_Classification_Start() );

            int lineno = 0;
            int noSkippedReads = 0;
            SAMFileReader samBamReader = new SAMFileReader( trackJob.getFile() );
            samBamReader.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            SAMRecordIterator samItor = samBamReader.iterator();

            SAMFileHeader header = samBamReader.getFileHeader();
            SAMFileHeader.SortOrder sortOrder = samBamReader.getFileHeader().getSortOrder();
            header.setSortOrder( SAMFileHeader.SortOrder.coordinate );

            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(
                    trackJob.getFile(), header, false, SamUtils.EXTENDED_STRING );

            this.samBamWriter = writerAndFile.getFirst();

            final File outputFile = writerAndFile.getSecond();

            String lastReadName = ""; //read name without pair tag
            final Map<SAMRecord, Integer> diffMap1 = new HashMap<>( 1024 ); //mapping of record to number of differences
            final Map<SAMRecord, Integer> diffMap2 = new HashMap<>( 1024 ); //mapping of record to number of differences
            class1 = new ParsedClassification( sortOrder ); //classification data for all reads with same read name
            class2 = new ParsedClassification( sortOrder );
            int readPairId = 1;
            while( samItor.hasNext() ) {
                ++lineno;
                try {
                    //separate all mappings of same pair by read pair tag and hand it over to classification then
                    final SAMRecord record = samItor.next();
                    if( !record.getReadUnmappedFlag() && chromLengthMap.containsKey( record.getReferenceName() ) ) {
                        char pairTag = CommonsMappingParser.getReadPairTag( record );
                        String readName = CommonsMappingParser.getReadNameWithoutPairTag( record.getReadName() );//read name without pair tag

                        // classify read pair, because all mappings for this pair are currently stored in the lists
                        if( !readName.equals( lastReadName ) && !lastReadName.isEmpty() ) { //meaning: next pair, because sorted by read name
                            this.performClassification( diffMap1, diffMap2, readPairId );
                            CommonsMappingParser.writeSamRecord( diffMap1, class1, samBamWriter );
                            CommonsMappingParser.writeSamRecord( diffMap2, class2, samBamWriter );
                            class1 = new ParsedClassification( sortOrder );
                            class2 = new ParsedClassification( sortOrder );
                            ++readPairId;

                        }

                        final boolean classified;
                        if( pairTag == Properties.EXT_A1 ) {
                            record.setReadPairedFlag( true );
                            record.setFirstOfPairFlag( true );
                            classified = CommonsMappingParser.classifyRead( record, this, chromLengthMap, outputFile.getName(), lineno, refSeqFetcher, diffMap1, class1 );
                        }
                        else if( pairTag == Properties.EXT_A2 ) {
                            record.setReadPairedFlag( true );
                            record.setSecondOfPairFlag( true );
                            classified = CommonsMappingParser.classifyRead( record, this, chromLengthMap, outputFile.getName(), lineno, refSeqFetcher, diffMap2, class2 );
                        }
                        else { //since only reads without pair tag can have the same read name as the current one without pair tag its okay to add them to 1's data
                            classified = CommonsMappingParser.classifyRead( record, this, chromLengthMap, outputFile.getName(), lineno, refSeqFetcher, diffMap1, class1 );
                        }

                        if( !classified ) {
                            ++noSkippedReads;
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        lastReadName = readName;
                    }
                    else { // else read is unmapped or belongs to another reference
                        this.sendMsgIfAllowed( NbBundle.getMessage( SamBamParser.class,
                                                                    "Parser.Parsing.CorruptData", lineno, record.getReadName() ) );
                    }
                }
                catch( SAMFormatException e ) {
                    if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                        this.sendMsgIfAllowed( NbBundle.getMessage( SamBamParser.class,
                                                                    "Parser.Parsing.CorruptData", lineno, e.toString() ) );
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                }

                if( lineno % 500000 == 0 ) {
                    finish = System.currentTimeMillis();
                    this.notifyObservers( Benchmark.calculateDuration( startTime, finish, lineno + " mappings processed in " ) );
                }
                System.err.flush();
            }

            if( !diffMap1.isEmpty() || !diffMap2.isEmpty() ) {
                this.performClassification( diffMap1, diffMap2, readPairId );
                CommonsMappingParser.writeSamRecord( diffMap1, class1, samBamWriter );
                CommonsMappingParser.writeSamRecord( diffMap2, class2, samBamWriter );
            }

            if( errorLimit.getSkippedCount() > 0 ) {
                this.notifyObservers( "... " + errorLimit.getSkippedCount() + " more errors occured" );
            }

            this.notifyObservers( "Writing extended read pair bam file..." );
            samItor.close();
            this.samBamWriter.close();
            samBamReader.close();

            samBamReader = new SAMFileReader( outputFile );
            samBamReader.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            File indexFile = new File( outputFile.getAbsolutePath() + Properties.BAM_INDEX_EXT );
            SamUtils utils = new SamUtils();
            utils.createIndex( samBamReader, indexFile );
            samBamReader.close();

            this.notifyObservers( "Reads skipped during parsing due to inconsistent data: " + noSkippedReads );
            finish = System.currentTimeMillis();
            String msg = Bundle.Classifier_Classification_Finish();
            this.notifyObservers( Benchmark.calculateDuration( startTime, finish, msg ) );

            if( deleteSortedFile ) { //delete the sorted/preprocessed file
                GeneralUtils.deleteOldWorkFile( oldWorkFile );
            }

            trackJob.setFile( outputFile );

            this.statsContainer.setReadPairDistribution( this.readPairSizeDistribution );

        }
        catch( RuntimeEOFException e ) {
            this.notifyObservers( "Last read in the file is incomplete, ignoring it." );
        }
        catch( MissingResourceException | IOException e ) {
            this.notifyObservers( Bundle.Classifier_Error( e.getMessage() ) );
            Logger.getLogger( this.getClass().getName() ).log( Level.INFO, e.getMessage() );
        }

        return new ParsedReadPairContainer();
    }


    /**
     * Actually performs the classification of the read pairs.
     * <p>
     * @param currentRecords1 all records of the same read pair
     * <p>
     * @return
     */
    @NbBundle.Messages( "Classifier.UnclassifiedRead=Found unclassified read. Also no read pair classification for this read: {0}" )
    private void performClassification( Map<SAMRecord, Integer> diffMap1, Map<SAMRecord, Integer> diffMap2, int readPairId ) {

        int largestSmallerDist = Integer.MIN_VALUE;
        int largestPotSmallerDist = Integer.MIN_VALUE;
        int largestUnorSmallerDist = Integer.MIN_VALUE;
        int largestPotUnorSmallerDist = Integer.MIN_VALUE;

        final int orient1 = this.orienation == 1 ? -1 : 1;
        final int dir = this.orienation == 2 ? 1 : -1;

        List<ReadPair> potPairList = new ArrayList<>(); //also perfect
        List<ReadPair> potSmallPairList = new ArrayList<>();
        List<ReadPair> potPotSmallPairList = new ArrayList<>();
        List<ReadPair> unorPairList = new ArrayList<>();
        List<ReadPair> potUnorPairList = new ArrayList<>();
        List<ReadPair> unorSmallPairList = new ArrayList<>();
        List<ReadPair> potUnorSmallPairList = new ArrayList<>();

        List<SAMRecord> omitList = new ArrayList<>(); //(enthält alle und werden step by step rausgelöscht)

        /*
         * 0 = fr -r1(1)-> <-r2(-1)- (stop1<start2) oder -r2(1)-> <-r1(-1)-
         * (stop2 < start1) 1 = rf <-r1(-1)- -r2(1)-> (stop1<start2) oder
         * <-r2(-1)- -r1(1)-> (stop2 < start1) 2 = ff -r1(1)-> -r2(1)->
         * (stop1<start2) oder <-r2(-1)- <-r1(-1)- (stop2 < start1)
         */

        if( !diffMap2.isEmpty() ) {
            //both sides of the read pair have been mapped

            if( diffMap1.size() == 1  &&  diffMap2.size() == 1 ) { //only one mapping per readname = we can always store a pair object

                final SAMRecord record1 = diffMap1.keySet().iterator().next();
                final SAMRecord record2 = diffMap2.keySet().iterator().next();
                final byte direction  = record1.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                final byte direction2 = record2.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                final int start1 = record1.getAlignmentStart();
                final int stop1  = record1.getAlignmentEnd();
                final int start2 = record2.getAlignmentStart();
                final int stop2  = record2.getAlignmentEnd();
                //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr

                final boolean case1 = direction == orient1 && start1 <= start2;
                if( (case1 || direction == -orient1 && start2 <= start1)
                    && direction == dir * direction2 ) {

                    //determine insert size between both reads
                    int currDist;
                    if( case1 ) {
                        currDist = Math.abs( start1 - stop2 ) + 1; //distance if on different chromosomes??? read 1 + rest chr1 + start chr2 bis read2?
                    }
                    else {
                        currDist = Math.abs( start2 - stop1 ) + 1;
                    }

                    if( currDist <= this.maxDist && currDist >= this.minDist ) {
                        // found a perfect pair!
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.PERFECT_PAIR, currDist ) );
                    }
                    else if( currDist < this.maxDist ) { //both reads of pair mapped, but distance in reference is different
                        // imperfect pair, distance too small
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.DIST_SMALL_PAIR, currDist ) );
                    }
                    else { // imperfect pair, distance too large
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.DIST_LARGE_PAIR, currDist ) );
                    }
                }
                else { // inversion of one read
                    int currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                    ++currDist;

                    if( currDist <= this.maxDist && currDist >= this.minDist ) { // distance fits, orientation not
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.ORIENT_WRONG_PAIR, currDist ) );
                    }
                    else if( currDist < this.maxDist ) { // orientation wrong & distance too small
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.OR_DIST_SMALL_PAIR, currDist ) );
                    }
                    else { // orientation wrong & distance too large
                        this.addPairedRecord( new ReadPair( record1, record2, readPairId, ReadPairType.OR_DIST_LARGE_PAIR, currDist ) );
                    }
                }
            }
            else if( !diffMap1.isEmpty() ) {

                ReadPair readPair;
                for( Map.Entry<SAMRecord, Integer> entry : diffMap1.entrySet() ) { //block for one readname, pos and direction can deviate
                    SAMRecord recordA = entry.getKey();
                    int diffs1 = entry.getValue();

                    try {
                        final byte direction = recordA.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                        final int start1 = recordA.getAlignmentStart();
                        final int stop1 = recordA.getAlignmentEnd();

                        for( Map.Entry<SAMRecord, Integer> entry2 : diffMap2.entrySet() ) {
                            try {

                                SAMRecord recordB = entry2.getKey();
                                int diffs2 = entry2.getValue();
                                if( !(omitList.contains( recordA ) && omitList.contains( recordB )) ) {
                                    final int start2 = recordB.getAlignmentStart();
                                    final int stop2 = recordB.getAlignmentEnd();
                                    final byte direction2 = recordB.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;


                                    //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                                    final boolean case1 = direction == orient1 && start1 < start2;
                                    if( (case1 || direction == -orient1 && start2 < start1)
                                        && direction == dir * direction2 ) { //direction fits

                                        //determine insert size between both reads
                                        final int currDist = case1 ? Math.abs( start1 - stop2 ) + 1 : Math.abs( start2 - stop1 ) + 1;
                                        if( currDist <= this.maxDist && currDist >= this.minDist ) { //distance fits, found a perfect pair!
                                            readPair = new ReadPair( recordA, recordB, readPairId, ReadPairType.PERFECT_PAIR, currDist );
                                            if( diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches() ) { //only perfect and best match mappings pass here
                                                this.addPairedRecord( readPair );
                                                omitList.add( recordA );
                                                omitList.add( recordB );
                                            }
                                            else {// store potential perfect pair for common mappings
                                                potPairList.add( readPair );
                                            }
                                        }
                                        else if( currDist < this.minDist ) { // distance too small, potential pair
                                            readPair = new ReadPair( recordA, recordB, readPairId, ReadPairType.DIST_SMALL_PAIR, currDist );
                                            if( largestSmallerDist < currDist && diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches() ) { //best mappings
                                                largestSmallerDist = currDist;
                                                potSmallPairList.add( readPair );
                                            }
                                            else if( largestPotSmallerDist < currDist ) { //at least one common mapping in potential pair
                                                largestPotSmallerDist = currDist; //replace even smaller pair with this one (more likely)
                                                potPotSmallPairList.add( readPair );
                                            }
                                        }
                                        else { // distance too large, currently nothing to do if dist too large
                                        }
                                    }
                                    else { // inversion of one read
                                        int currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                                        ++currDist;

                                        if( currDist <= this.maxDist && currDist >= this.minDist ) { //distance fits, orientation not
                                            readPair = new ReadPair( recordA, recordB, readPairId, ReadPairType.ORIENT_WRONG_PAIR, currDist );
                                            if( diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches() ) { //best mappings
                                                unorPairList.add( readPair );
                                            }
                                            else {
                                                potUnorPairList.add( readPair );
                                            }
                                        }
                                        else if( currDist < this.maxDist && largestSmallerDist < currDist ) { // orientation wrong & distance too small
                                            readPair = new ReadPair( recordA, recordB, readPairId, ReadPairType.OR_DIST_SMALL_PAIR, currDist );
                                            if( largestUnorSmallerDist < currDist && diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches() ) { //best mappings
                                                largestUnorSmallerDist = currDist;
                                                unorSmallPairList.add( readPair );
                                            }
                                            else if( largestPotUnorSmallerDist < currDist ) {
                                                largestPotUnorSmallerDist = currDist;
                                                potUnorSmallPairList.add( readPair );
                                            }
                                        }
                                        else {
                                            // orientation wrong & distance too large
                                            // currently nothing to do
                                        }
                                    }
                                }
                            }
                            catch( NullPointerException e ) {
                                this.sendMsgIfAllowed( Bundle.Classifier_UnclassifiedRead( recordA.getReadName() ) );
                                Exceptions.printStackTrace( e );
                            }
                        }
                        largestSmallerDist = Integer.MIN_VALUE;
                        largestPotSmallerDist = Integer.MIN_VALUE;
                        largestUnorSmallerDist = Integer.MIN_VALUE;
                        largestPotUnorSmallerDist = Integer.MIN_VALUE;

                    }
                    catch( NullPointerException e ) {
                        this.sendMsgIfAllowed( Bundle.Classifier_UnclassifiedRead( recordA.getReadName() ) );
                        Exceptions.printStackTrace( e );
                    }
                }

                /*
                 * Determines order of insertion of pairs. If one id is
                 * contained in an earlier list, then it is ignored in all other
                 * lists!
                 */
                for( ReadPair pairMapping : potSmallPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( ReadPair pairMapping : unorPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( ReadPair pairMapping : unorSmallPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( ReadPair pairMapping : potPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( ReadPair pairMapping : potPotSmallPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( ReadPair pairMapping : potUnorSmallPairList ) {
                    this.addPairedRecord( pairMapping, omitList );
                }

                for( SAMRecord record : diffMap1.keySet() ) {
                    if( !omitList.contains( record ) ) { //so single mappings link to the first mapping of their partner read
                        SAMRecord mateRecord = diffMap2.keySet().iterator().next();
                        this.classifySingleRecord( record, readPairId, mateRecord.getAlignmentStart(), mateRecord.getReferenceName() );
                    }
                }

                for( SAMRecord record : diffMap2.keySet() ) {
                    if( !omitList.contains( record ) ) { //so single mappings link to the first mapping of their partner read
                        SAMRecord mateRecord = diffMap1.keySet().iterator().next();
                        this.classifySingleRecord( record, readPairId, mateRecord.getAlignmentStart(), mateRecord.getReferenceName() );
                    }
                }

                //reset data structures
                potPairList.clear();
                potSmallPairList.clear();
                potPotSmallPairList.clear();
                unorPairList.clear();
                potUnorPairList.clear();
                unorSmallPairList.clear();
                potUnorSmallPairList.clear();
                omitList.clear();
            }
            else {
                for( SAMRecord record : diffMap2.keySet() ) { //pos and direction can deviate
                    this.classifySingleRecord( record, readPairId, 0, "*" );
                }
            }

        }
        else { //only one side of the read pair could be mapped
            for( SAMRecord record : diffMap1.keySet() ) { //pos and direction can deviate
                this.classifySingleRecord( record, readPairId, 0, "*" );
            }
        }
    }


    /**
     * Adds a new read pair mapping object to the list and sets necessary
     * sam flags for both records.
     * <p>
     * @param readPair the read pair to add
     */
    private void addPairedRecord( final ReadPair readPair ) {
        final SAMRecord mapping1 = readPair.getRecord1();
        final SAMRecord mapping2 = readPair.getRecord2();
        this.setReadPairForType( readPair );
        mapping1.setAttribute( Properties.TAG_READ_PAIR_TYPE, readPair.getType().getTypeInt() );
        mapping1.setAttribute( Properties.TAG_READ_PAIR_ID, readPair.getReadPairId() );
        mapping1.setMateReferenceName( mapping2.getReferenceName() );
        mapping1.setMateAlignmentStart( mapping2.getAlignmentStart() );
        mapping1.setMateNegativeStrandFlag( mapping2.getReadNegativeStrandFlag() );
        mapping1.setProperPairFlag( readPair.getType() == ReadPairType.PERFECT_PAIR || readPair.getType() == ReadPairType.PERFECT_UNQ_PAIR );

        mapping2.setAttribute( Properties.TAG_READ_PAIR_TYPE, readPair.getType().getTypeInt() );
        mapping2.setAttribute( Properties.TAG_READ_PAIR_ID, readPair.getReadPairId() );
        mapping2.setMateReferenceName( mapping1.getReferenceName() );
        mapping2.setMateAlignmentStart( mapping1.getAlignmentStart() );
        mapping2.setMateNegativeStrandFlag( mapping1.getReadNegativeStrandFlag() );
        mapping2.setProperPairFlag( readPair.getType() == ReadPairType.PERFECT_PAIR || readPair.getType() == ReadPairType.PERFECT_UNQ_PAIR );

        if( mapping1.getAlignmentStart() < mapping2.getAlignmentStart() ) { //different chromosomes means there is no distance here according to sam spec. Still think about a solution for stats
            mapping1.setInferredInsertSize( readPair.getDistance() );
            mapping2.setInferredInsertSize( -readPair.getDistance() );
        }
        else {
            mapping1.setInferredInsertSize( -readPair.getDistance() );
            mapping2.setInferredInsertSize( readPair.getDistance() );
        }

        this.readPairSizeDistribution.increaseDistribution( readPair.getDistance() );
        this.statsContainer.incReadPairStats( readPair.getType(), 1 );
    }


    /**
     * Adds a new read pair mapping to the writer, if one of the records
     * is not already contained in the omit list. Also takes care that the
     * classification and read pair tags are set into the contained sam
     * records. Note that the ordinary classification data is not set here!
     * <p>
     * @param potPair  the potential pair to add to the sam/bam writer
     * @param omitList the omit list containing records, which where already
     *                 added
     */
    private void addPairedRecord( final ReadPair potPair, final List<SAMRecord> omitList ) {
        final SAMRecord record1 = potPair.getRecord1();
        final SAMRecord record2 = potPair.getRecord2();
        if( !(omitList.contains( record1 ) || omitList.contains( record2 )) ) {
            this.addPairedRecord( potPair );
            omitList.add( record1 );
            omitList.add( record2 );
        }
    }


    /**
     * Adds a single mapping (sam record) to the file writer and sets its read
     * pair and classification attributes.
     * <p>
     * @param record       the unpaired record to write
     * @param readPairId   the read pair id of this single record
     * @param mateUnmapped true, if the mate of this mapping is unmapped, false
     *                     if it is mapped, but does not form a pair with this record
     */
    private void classifySingleRecord( final SAMRecord record, final int readPairId, final int mateStart, final String mateRef ) {
        record.setMateReferenceName( mateRef );
        record.setMateAlignmentStart( mateStart );
        record.setMateUnmappedFlag( mateStart == 0 );
        record.setNotPrimaryAlignmentFlag( mateStart != 0 );
        record.setAttribute( Properties.TAG_READ_PAIR_TYPE, ReadPairType.UNPAIRED_PAIR.getTypeInt() );
        record.setAttribute( Properties.TAG_READ_PAIR_ID, readPairId );
        this.statsContainer.incReadPairStats( ReadPairType.UNPAIRED_PAIR, 1 );
    }


    /**
     * Updated the read pair for a given read pair. If both reads are
     * only mapped once, the corresponding unique read pair type is chosen.
     * <p>
     * @param readPair the read pair to update in case it is unique
     */
    private void setReadPairForType( final ReadPair readPair ) {
        final int mapCount1 = class1.getNumberOccurrences();
        final int mapCount2 = class2.getNumberOccurrences();
        if( mapCount1 == 1  &&  mapCount2 == 1 ) {
            switch( readPair.getType() ) {
                case PERFECT_PAIR:
                    readPair.setType( ReadPairType.PERFECT_UNQ_PAIR );
                    break;
                case DIST_SMALL_PAIR:
                    readPair.setType( ReadPairType.DIST_SMALL_UNQ_PAIR );
                    break;
                case DIST_LARGE_PAIR:
                    readPair.setType( ReadPairType.DIST_LARGE_UNQ_PAIR );
                    break;
                case ORIENT_WRONG_PAIR:
                    readPair.setType( ReadPairType.ORIENT_WRONG_UNQ_PAIR );
                    break;
                case OR_DIST_SMALL_PAIR:
                    readPair.setType( ReadPairType.OR_DIST_SMALL_UNQ_PAIR );
                    break;
                case OR_DIST_LARGE_PAIR:
                    readPair.setType( ReadPairType.OR_DIST_LARGE_UNQ_PAIR );
                    break;
                default: //change nothing
            }
        }
    }


    @Override
    public void update( Object args ) {
        if( args instanceof String ) {
            this.notifyObservers( args );
        }
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
    public void notifyObservers( Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


    /**
     * Depending on deviation the min and max values of the distance between a
     * read pair is set.
     * <p>
     * @param dist      distance in bases
     * @param deviation deviation in % (1-100)
     * <p>
     * @return the maximum distance of a mapping pair, which is accepted as
     *         valid
     */
    protected int calculateMinAndMaxDist( final int dist, final int deviation ) {
        int devInBP = dist / 100 * deviation;
        this.minDist = dist - devInBP;
        this.maxDist = dist + devInBP;
        return maxDist;
    }


    /**
     * Sets the stats container to keep track of statistics for this track.
     * <p>
     * @param statsContainer The stats container to add
     */
    public void setStatsContainer( StatsContainer statsContainer ) {
        this.statsContainer = statsContainer;
    }


    /**
     * Sends the given msg to all observers, if the error limit is not already
     * reached for this instance of SamBamParser.
     * <p>
     * @param msg The message to send
     */
    @Override
    public void sendMsgIfAllowed( String msg ) {
        if( this.errorLimit.allowOutput() ) {
            this.notifyObservers( msg );
        }
    }


}
