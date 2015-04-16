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


import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.api.enums.Distribution;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.api.enums.SAMRecordTag;
import de.cebitec.readxplorer.api.enums.TotalCoverage;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.DiscreteCountingDistribution;
import de.cebitec.readxplorer.utils.ErrorLimit;
import de.cebitec.readxplorer.utils.MessageSenderI;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.PositionUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
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
 * <p>
 * @author -Rolf Hilker-
 */
public class SamBamStatsParser implements Observable, MessageSenderI {

    private final List<Observer> observers;
    private StatsContainer statsContainer;
    private final DiscreteCountingDistribution readLengthDistribution;
    private final ErrorLimit errorLimit;


    /**
     * Creates and stores the statistics for a track, which needs to be sorted
     * by position. The data to store is directly forwarded to the observer,
     * which should then further process it (store it in the db).
     */
    public SamBamStatsParser() {
        this.observers = new ArrayList<>();
        this.errorLimit = new ErrorLimit( 100 );
        this.readLengthDistribution = new DiscreteCountingDistribution( 400 );
        readLengthDistribution.setType( Distribution.ReadLength );
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
    public ParsedTrack createTrackStats( final TrackJob trackJob, final Map<String, Integer> chromLengthMap ) {

        final long startTime = System.currentTimeMillis();
        final String fileName = trackJob.getFile().getName();
        this.notifyObservers( Bundle.StatsParser_Start( fileName ) );
//        int noMappings = 0;
//        long starti = System.currentTimeMillis();

        String lastReadSeq = "";
        List<Integer> readsDifferentPos = new ArrayList<>();
        int seqCount = 0;
        //Map with one covered interval list for each mapping class
        Map<String, Map<Classification, List<Pair<Integer, Integer>>>> classToCoveredIntervalsMap = new HashMap<>();
        for( String chromName : chromLengthMap.keySet() ) {
            Map<Classification, List<Pair<Integer, Integer>>> mapClassMap = new HashMap<>();
            for( MappingClass mapClass : MappingClass.values() ) {
                mapClassMap.put( mapClass, new ArrayList<>() );
                mapClassMap.get( mapClass ).add( new Pair<>( 0, 0 ) );
            }
            mapClassMap.put( TotalCoverage.TOTAL_COVERAGE, new ArrayList<>() );
            mapClassMap.get( TotalCoverage.TOTAL_COVERAGE ).add( new Pair<>( 0, 0 ) );
            classToCoveredIntervalsMap.put( chromName, mapClassMap );
        }

        try( final SAMFileReader sam = new SAMFileReader( trackJob.getFile() ) ) {

            sam.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            final SAMRecordIterator samItor = sam.iterator();

            int lineNo = 0;
            while( samItor.hasNext() ) {
                try {
                    ++lineNo;

                    final SAMRecord record = samItor.next();
                    final String refName = record.getReferenceName();
                    if( !record.getReadUnmappedFlag() && chromLengthMap.containsKey( refName ) ) {

                        String readSeq = record.getReadString();
                        if( !CommonsMappingParser.checkReadSam( this, readSeq, fileName, lineNo ) ) {
                            continue; //continue, and ignore read, if it contains inconsistent information
                        }

                        //statistics calculations: count no mappings in classifications and distinct sequences ////////////

                        Integer mappingCount = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mapCount = mappingCount != null ? mappingCount : 0;
                        if( mapCount == 1 ) {
                            statsContainer.increaseValue( StatsContainer.NO_UNIQ_MAPPINGS, mapCount );
                        }
                        statsContainer.increaseValue( StatsContainer.NO_MAPPINGS, 1 );

                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = classification != null ? MappingClass.getFeatureType( classification ) : MappingClass.COMMON_MATCH;
                        readLengthDistribution.increaseDistribution( readSeq.length() );

                        if( !lastReadSeq.equals( readSeq ) ) { //same seq counted multiple times when multiple reads with same sequence
                            if( readsDifferentPos.size() == 1 ) { //1 means all reads since last clean started at same pos
                                if( seqCount == 1 ) { // only one sequence found at same position
                                    statsContainer.increaseValue( StatsContainer.NO_UNIQUE_SEQS, seqCount );
                                } else {
                                    statsContainer.increaseValue( StatsContainer.NO_REPEATED_SEQ, 1 );
                                    //counting the repeated seq and not in how many reads they are contained
                                }
                            }
                            readsDifferentPos.clear();
                        }

                        int start = record.getAlignmentStart();
                        int stop = record.getAlignmentEnd();
                        if( !readsDifferentPos.contains( start ) ) {
                            readsDifferentPos.add( start );
                            seqCount = 0;
                        }
                        ++seqCount;
                        lastReadSeq = readSeq;

                        statsContainer.increaseValue( mappingClass.toString(), 1 );
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
                } catch( NumberFormatException nfe ) {
                    //skip error messages, if too many occur to prevent bug in the output panel
                    if( nfe.getMessage() == null || !nfe.getMessage().contains( "MAPQ should be 0" ) ) {
                        //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                        this.sendMsgIfAllowed( NbBundle.getMessage( SamBamStatsParser.class,
                                                                    "Parser.Parsing.CorruptData", lineNo, nfe.toString() ) );
                        Exceptions.printStackTrace( nfe );
                    }
                }
                if( (lineNo % 500000) == 0 ) {//output process info only on every XX line
                    long finish = System.currentTimeMillis();
                    this.notifyObservers( Benchmark.calculateDuration( startTime, finish, lineNo + " mappings processed in " ) );
                }
                System.err.flush();
            }
            if( errorLimit.getSkippedCount() > 0 ) {
                this.notifyObservers( "... " + (errorLimit.getSkippedCount()) + " more errors occurred" );
            }
            samItor.close();

        } catch( RuntimeEOFException e ) {
            this.notifyObservers( "Last read in file is incomplete, ignoring it!" );
        } catch( Exception e ) {
            this.notifyObservers( e.getMessage() );
            Exceptions.printStackTrace( e ); //TODO correct error handling or remove
        }

        //finish statistics and return the track with the statistics data in the end
        //TODO claculate separately for all chromosomes for extended stats panel
        statsContainer.setCoveredPositionsImport( classToCoveredIntervalsMap );

        ParsedTrack track = new ParsedTrack( trackJob );
        statsContainer.setReadLengthDistribution( readLengthDistribution );
        track.setStatsContainer( statsContainer );

        long finish = System.currentTimeMillis();
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
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


    @Override
    public void sendMsgIfAllowed( final String msg ) {
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
