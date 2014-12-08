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
import de.cebitec.readXplorer.parser.common.ParsedTrack;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.PositionUtils;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.StatsContainer;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.util.classification.TotalCoverage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Creates and stores the statistics for a track, which needs to be sorted by
 * position. The data to store is directly forwarded to the observer, which
 * should then further process it (store it in the db).
 *
 * @author -Rolf Hilker-
 */
public class SamBamStatsParser implements Observable, MessageSenderI {

    private List<Observer> observers;
    private StatsContainer statsContainer;
    private DiscreteCountingDistribution readLengthDistribution;
    private ErrorLimit errorLimit;


    /**
     * Creates and stores the statistics for a track, which needs
     * to be sorted by position. The data to store is directly forwarded to the
     * observer, which should then further process it (store it in the db).
     */
    public SamBamStatsParser() {
        this.observers = new ArrayList<>();
        this.errorLimit = new ErrorLimit( 100 );
        this.readLengthDistribution = new DiscreteCountingDistribution( 400 );
        readLengthDistribution.setType( Properties.READ_LENGTH_DISTRIBUTION );
    }


    /**
     * Creates the global track statistics for the given track job, which needs
     * to be sorted by position. The data to store is directly forwarded to the
     * observer, which should then further process it (store it in the db).
     * <p>
     * @param trackJob       track job whose position table needs to be created
     * @param chromLengthMap mapping of chromosome name
     * <p>
     * @return
     */
    @SuppressWarnings( "fallthrough" )
    @NbBundle.Messages( {
        "# {0} - track file path",
        "StatsParser.Finished=Finished creating track statistics for {0}. ",
        "# {0} - track file path",
        "StatsParser.Start=Start creating track statistics for {0}" } )
    public ParsedTrack createTrackStats( TrackJob trackJob, Map<String, Integer> chromLengthMap ) {

        long startTime = System.currentTimeMillis();
        long finish;
        String fileName = trackJob.getFile().getName();
        this.notifyObservers( Bundle.StatsParser_Start( fileName ) );

//        int noMappings = 0;
//        long starti = System.currentTimeMillis();
        int lineno = 0;

        String lastReadSeq = "";
        List<Integer> readsDifferentPos = new ArrayList<>();
        int seqCount = 0;
        int start;
        int stop;
        String refName;
        String readName;
        String readSeq;
        String cigar;
        MappingClass mappingClass;
        int mapCount;
        //Map with one covered interval list for each mapping class
        Map<String, Map<Classification, List<Pair<Integer, Integer>>>> classToCoveredIntervalsMap = new HashMap<>();
        for( String chromName : chromLengthMap.keySet() ) {
            Map<Classification, List<Pair<Integer, Integer>>> mapClassMap = new HashMap<>();
            for( MappingClass mapClass : MappingClass.values() ) {
                mapClassMap.put( mapClass, new ArrayList<Pair<Integer, Integer>>() );
                mapClassMap.get( mapClass ).add( new Pair<>( 0, 0 ) );
            }
            mapClassMap.put( TotalCoverage.TOTAL_COVERAGE, new ArrayList<Pair<Integer, Integer>>() );
            mapClassMap.get( TotalCoverage.TOTAL_COVERAGE ).add( new Pair<>( 0, 0 ) );
            classToCoveredIntervalsMap.put( chromName, mapClassMap );
        }
        Byte classification;
        Integer mappingCount;
//        HashMap<String, Object> readNameSet = new HashMap<>();

//        String[] nameArray;
//        String shortReadName;

        try( SAMFileReader sam = new SAMFileReader( trackJob.getFile() ) ) {
            sam.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            while( samItor.hasNext() ) {
                try {
                    ++lineno;

                    record = samItor.next();
                    readName = record.getReadName();
                    refName = record.getReferenceName();
                    if( !record.getReadUnmappedFlag() && chromLengthMap.containsKey( refName ) ) {

                        cigar = record.getCigarString();
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        readSeq = record.getReadString();

                        if( !CommonsMappingParser.checkReadSam( this, readSeq, chromLengthMap.get( refName ), cigar, start, stop, fileName, lineno ) ) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        //statistics calculations: count no mappings in classifications and distinct sequences ////////////
                        mappingCount = (Integer) record.getAttribute( Properties.TAG_MAP_COUNT );
                        if( mappingCount != null ) {
                            mapCount = mappingCount;
                        }
                        else {
                            mapCount = 0;
                        }

                        if( mapCount == 1 ) {
                            statsContainer.increaseValue( StatsContainer.NO_UNIQ_MAPPINGS, mapCount );
                        }
                        statsContainer.increaseValue( StatsContainer.NO_MAPPINGS, 1 );

                        classification = Byte.valueOf( record.getAttribute( Properties.TAG_READ_CLASS ).toString() );
                        if( classification != null ) {
                            mappingClass = MappingClass.getFeatureType( classification );
                        }
                        else {
                            mappingClass = MappingClass.COMMON_MATCH;
                        }
                        readLengthDistribution.increaseDistribution( readSeq.length() );

                        if( !lastReadSeq.equals( readSeq ) ) { //same seq counted multiple times when multiple reads with same sequence
                            if( readsDifferentPos.size() == 1 ) { //1 means all reads since last clean started at same pos
                                if( seqCount == 1 ) { // only one sequence found at same position
                                    statsContainer.increaseValue( StatsContainer.NO_UNIQUE_SEQS, seqCount );
                                }
                                else {
                                    statsContainer.increaseValue( StatsContainer.NO_REPEATED_SEQ, 1 );
                                    //counting the repeated seq and not in how many reads they are contained
                                }
                            }
                            readsDifferentPos.clear();
                        }
                        if( !readsDifferentPos.contains( start ) ) {
                            readsDifferentPos.add( start );
                            seqCount = 0;
                        }
                        ++seqCount;
                        lastReadSeq = readSeq;

                        statsContainer.increaseValue( mappingClass.getTypeString(), mapCount );
                        PositionUtils.updateIntervals( classToCoveredIntervalsMap.get( refName ).get( mappingClass ), start, stop );
                        PositionUtils.updateIntervals( classToCoveredIntervalsMap.get( refName ).get( TotalCoverage.TOTAL_COVERAGE ), start, stop );
                        //saruman starts genome at 0 other algorithms like bwa start genome at 1

//                        //can be used for debugging performance
//                        if (++noMappings % 10000 == 0) {
//                            long finish = System.currentTimeMillis();
//                            this.notifyObservers(Benchmark.calculateDuration(starti, finish, noMappings + " mappings processed. "));
//                            starti = System.currentTimeMillis();
//                        }
                    }
                }
                catch( Exception e ) {
                    //skip error messages, if too many occur to prevent bug in the output panel
                    if( e.getMessage() == null || !e.getMessage().contains( "MAPQ should be 0" ) ) {
                        //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                        this.sendMsgIfAllowed( NbBundle.getMessage( SamBamStatsParser.class,
                                                                    "Parser.Parsing.CorruptData", lineno, e.toString() ) );
                        Exceptions.printStackTrace( e );
                    }
                }
                if( (lineno % 500000) == 0 ) {//output process info only on every XX line
                    finish = System.currentTimeMillis();
                    this.notifyObservers( Benchmark.calculateDuration( startTime, finish, lineno + " mappings processed in " ) );
                }
                System.err.flush();
            }
            if( errorLimit.getSkippedCount() > 0 ) {
                this.notifyObservers( "... " + (errorLimit.getSkippedCount()) + " more errors occurred" );
            }
            samItor.close();

        }
        catch( RuntimeEOFException e ) {
            this.notifyObservers( "Last read in file is incomplete, ignoring it!" );
        }
        catch( Exception e ) {
            Exceptions.printStackTrace( e ); //TODO: correct error handling or remove
        }

        //finish statistics and return the track with the statistics data in the end
        //TODO: claculate separately for all chromosomes for extended stats panel
        statsContainer.setCoveredPositionsImport( classToCoveredIntervalsMap );

        ParsedTrack track = new ParsedTrack( trackJob );
        statsContainer.setReadLengthDistribution( readLengthDistribution );
        track.setStatsContainer( statsContainer );

        finish = System.currentTimeMillis();
        String msg = Bundle.StatsParser_Finished( fileName );
        this.notifyObservers( Benchmark.calculateDuration( startTime, finish, msg ) );

        return track;
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


    @Override
    public void sendMsgIfAllowed( String msg ) {
        if( this.errorLimit.allowOutput() ) {
            this.notifyObservers( msg );
        }
    }


    /**
     * Sets the statistics container for handling statistics for the extended
     * track.
     * <p>
     * @param statsContainer the stats container
     */
    public void setStatsContainer( StatsContainer statsContainer ) {
        this.statsContainer = statsContainer;
    }


    /**
     * @return The statistics parser for handling statistics for the extended
     *         track.
     */
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }


}
