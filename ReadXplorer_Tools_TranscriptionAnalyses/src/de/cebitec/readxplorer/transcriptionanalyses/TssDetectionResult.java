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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.DetectedFeatures;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.TransStartUnannotated;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.TranscriptionStart;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for all data belonging to a transcription start site detection
 * result.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TssDetectionResult extends ResultTrackAnalysis<ParameterSetTSS> {

    public static final String TSS_TOTAL = "Total number of detected TSSs";
    public static final String TSS_CORRECT = "Correct TSS";
    public static final String TSS_FWD = "TSS on fwd strand";
    public static final String TSS_REV = "TSS on rev strand";
    public static final String TSS_LEADERLESS = "Leaderless TSS";
    public static final String TSS_NOVEL = "Novel Transcripts";
    public static final String TSS_UPSTREAM = "TSS with upstream feature";
    public static final String TSS_UPSTREAM1 = "Distance to upstream feature = 1 bp";
    public static final String TSS_UPSTREAM5 = "Distance to upstream feature 2-5 bp";
    public static final String TSS_UPSTREAM10 = "Distance to upstream feature 6-10 bp";
    public static final String TSS_UPSTREAM20 = "Distance to upstream feature 11-20 bp";
    public static final String TSS_UPSTREAM50 = "Distance to upstream feature 21-50 bp";
    public static final String TSS_UPSTREAM100 = "Distance to upstream feature 51-100 bp";
    public static final String TSS_UPSTREAM250 = "Distance to upstream feature 101-250 bp";
    public static final String TSS_DOWNSTREAM = "TSS with downstream feature";
    public static final String TSS_DOWNSTREAM1 = "Distance to downstream feature = 1 bp";
    public static final String TSS_DOWNSTREAM5 = "Distance to downstream feature 2-5 bp";
    public static final String TSS_DOWNSTREAM10 = "Distance to downstream feature 6-10 bp";
    public static final String TSS_DOWNSTREAM20 = "Distance to downstream feature 11-20 bp";
    public static final String TSS_DOWNSTREAM50 = "Distance to downstream feature 21-50 bp";
    public static final String TSS_DOWNSTREAM100 = "Distance to downstream feature 51-100 bp";
    public static final String TSS_DOWNSTREAM250 = "Distance to downstream feature 101-250 bp";
    public static final int UNUSED_STATISTICS_VALUE = -1;

    private final List<TranscriptionStart> results;
    private List<String> promotorRegions;


    /**
     * Container for all data belonging to a transcription start site detection
     * result.
     * <p>
     * @param results       the results of the TSS detection
     * @param trackList     the list of tracks, for which the TSS detection was
     *                      carried out
     * @param reference     reference genome, for which this result was
     *                      generated
     * @param combineTracks <code>true</code>, if the tracks in the list are
     *                      combined, <code>false</code> otherwise
     */
    public TssDetectionResult( List<TranscriptionStart> results, ParameterSetTSS tssParams, Map<Integer, PersistentTrack> trackList,
                               PersistentReference reference, boolean combineTracks, int trackColumn, int filterColumn ) {
        super( reference, trackList, combineTracks, trackColumn, filterColumn );
        this.setParameters( tssParams );
        this.results = results;
        this.calcStats( results );
    }


    /**
     * @return The results of the TSS detection
     */
    public List<TranscriptionStart> getResults() {
        return results;
    }


    /**
     * @return Promotor regions of the detected TSS
     */
    public List<String> getPromotorRegions() {
        return promotorRegions;
    }


    /**
     * Sets the promotor regions of the detected TSS
     * <p>
     * @param promotorRegions Promotor regions of the detected TSS
     */
    public void setPromotorRegions( List<String> promotorRegions ) {
        this.promotorRegions = promotorRegions;
    }


    /**
     * @return creates and returns the list of transcription start site
     *         descriptions
     *         for the columns of the table.
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add( "Position" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Strand" );
        dataColumnDescriptions.add( "No Read Starts" );
        dataColumnDescriptions.add( "Coverage Increase" );
        dataColumnDescriptions.add( "Coverage Increase %" );
        dataColumnDescriptions.add( "Leaderless" );
        dataColumnDescriptions.add( "Correct Start Feature" );
        dataColumnDescriptions.add( "Correct Start Locus" );
        dataColumnDescriptions.add( "Correct Start EC-Number" );
        dataColumnDescriptions.add( "Correct Start Product" );
        dataColumnDescriptions.add( "Correct Start Feature Start" );
        dataColumnDescriptions.add( "Correct Start Feature Stop" );
        dataColumnDescriptions.add( "Next Upstream Feature" );
        dataColumnDescriptions.add( "Next Upstream Locus" );
        dataColumnDescriptions.add( "Next Upstream EC-Number" );
        dataColumnDescriptions.add( "Next Upstream Product" );
        dataColumnDescriptions.add( "Next Upstream Feature Start" );
        dataColumnDescriptions.add( "Next Upstream Feature Stop" );
        dataColumnDescriptions.add( "Distance Upstream Feature" );
        dataColumnDescriptions.add( "Next Downstream Feature" );
        dataColumnDescriptions.add( "Next Downstream Locus" );
        dataColumnDescriptions.add( "Next Downstream EC-Number" );
        dataColumnDescriptions.add( "Next Downstream Product" );
        dataColumnDescriptions.add( "Next Downstream Feature Start" );
        dataColumnDescriptions.add( "Next Downstream Feature Stop" );
        dataColumnDescriptions.add( "Distance Downstream Feature" );
        dataColumnDescriptions.add( "Novel Transcript" );
        dataColumnDescriptions.add( "Transcript Stop" );
        dataColumnDescriptions.add( "70bp Upstream of Start" );



        allSheetDescriptions.add( dataColumnDescriptions );

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "Transcription Start Site Detection Parameter and Statistics Table" );

        allSheetDescriptions.add( statisticColumnDescriptions );

        return allSheetDescriptions;
    }


    /**
     * @return creates and returns the list of transcription start rows
     *         belonging
     *         to the transcription start site table.
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();

        for( int i = 0; i < results.size(); ++i ) {
            TranscriptionStart tss = results.get( i );
            List<Object> tssRow = new ArrayList<>();

            tssRow.add( tss.getPos() );
            tssRow.add( this.getTrackEntry( tss.getTrackId(), true ) );
            tssRow.add( this.getChromosomeMap().get( tss.getChromId() ) );
            tssRow.add( tss.isFwdStrand() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING );
            tssRow.add( tss.getReadStartsAtPos() );
            tssRow.add( tss.getCoverageIncrease() );
            tssRow.add( tss.getPercentIncrease() );

            DetectedFeatures detFeatures = tss.getDetFeatures();
            tssRow.add( detFeatures.isLeaderless() ? "yes" : "" );
            this.addFeatureRows( detFeatures.getCorrectStartFeature(), tssRow, tss, false );
            this.addFeatureRows( detFeatures.getUpstreamFeature(), tssRow, tss, true );
            this.addFeatureRows( detFeatures.getDownstreamFeature(), tssRow, tss, true );

            if( tss instanceof TransStartUnannotated ) {
                TransStartUnannotated unannoStart = (TransStartUnannotated) tss;
                tssRow.add( "yes" );
                tssRow.add( unannoStart.getDetectedStop() );
            }
            else {
                tssRow.add( "-" );
                tssRow.add( "-" );
            }

            tssRow.add( promotorRegions.get( i ) );

            tSSResults.add( tssRow );
        }

        tSSExport.add( tSSResults );


        //create statistics sheet
        ParameterSetTSS tssParameters = (ParameterSetTSS) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow(
                "Transcription start site detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Transcription start site detection parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum number of read starts:",
                                                                      tssParameters.getMinNoReadStarts() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum percent of coverage increase:",
                                                                      tssParameters.getMinPercentIncrease() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Maximum low coverage read start count:",
                                                                      tssParameters.getMaxLowCovReadStarts() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum low coverage read starts:",
                                                                      tssParameters.getMinLowCovReadStarts() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Detect novel transcripts?",
                                                                      tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum transcript extension coverage:",
                                                                      tssParameters.getMinTranscriptExtensionCov() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Maximum distance to feature of leaderless transcripts:",
                                                                      tssParameters.getMaxFeatureDistance() ) );
        tssParameters.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics
        Map<String, Integer> statsMap = this.getStatsMap();
        int totalTSS = statsMap.get( TSS_TOTAL );
        Map<String, String> percentMap = new HashMap<>(); //Calculate the percentage of each statistics value in relation to total TSS count
        for( Map.Entry<String, Integer> statsSet : statsMap.entrySet() ) {
            percentMap.put( statsSet.getKey(), GeneralUtils.formatNumberAsPercent( ((double) statsSet.getValue()) / totalTSS ) );
        }

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Transcription start site statistics:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_TOTAL, statsMap.get( TSS_TOTAL ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_CORRECT, statsMap.get( TSS_CORRECT ), percentMap.get( TSS_CORRECT ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_FWD, statsMap.get( TSS_FWD ), percentMap.get( TSS_FWD ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_REV, statsMap.get( TSS_REV ), percentMap.get( TSS_REV ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_LEADERLESS, statsMap.get( TSS_LEADERLESS ), percentMap.get( TSS_LEADERLESS ) ) );

        int noUnannotatedTrans = statsMap.get( TSS_NOVEL );
        String unannotatedTransValue = noUnannotatedTrans == UNUSED_STATISTICS_VALUE ? "-" : String.valueOf( noUnannotatedTrans );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_NOVEL, unannotatedTransValue, percentMap.get( TSS_NOVEL ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM, statsMap.get( TSS_UPSTREAM ), percentMap.get( TSS_UPSTREAM ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM1, statsMap.get( TSS_UPSTREAM1 ), percentMap.get( TSS_UPSTREAM1 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM5, statsMap.get( TSS_UPSTREAM5 ), percentMap.get( TSS_UPSTREAM5 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM10, statsMap.get( TSS_UPSTREAM10 ), percentMap.get( TSS_UPSTREAM10 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM20, statsMap.get( TSS_UPSTREAM20 ), percentMap.get( TSS_UPSTREAM20 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM50, statsMap.get( TSS_UPSTREAM50 ), percentMap.get( TSS_UPSTREAM50 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM100, statsMap.get( TSS_UPSTREAM100 ), percentMap.get( TSS_UPSTREAM100 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_UPSTREAM250, statsMap.get( TSS_UPSTREAM250 ), percentMap.get( TSS_UPSTREAM250 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM, statsMap.get( TSS_DOWNSTREAM ), percentMap.get( TSS_DOWNSTREAM ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM1, statsMap.get( TSS_DOWNSTREAM1 ), percentMap.get( TSS_DOWNSTREAM1 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM5, statsMap.get( TSS_DOWNSTREAM5 ), percentMap.get( TSS_DOWNSTREAM5 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM10, statsMap.get( TSS_DOWNSTREAM10 ), percentMap.get( TSS_DOWNSTREAM10 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM20, statsMap.get( TSS_DOWNSTREAM20 ), percentMap.get( TSS_DOWNSTREAM20 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM50, statsMap.get( TSS_DOWNSTREAM50 ), percentMap.get( TSS_DOWNSTREAM50 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM100, statsMap.get( TSS_DOWNSTREAM100 ), percentMap.get( TSS_DOWNSTREAM100 ) ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( TSS_DOWNSTREAM250, statsMap.get( TSS_DOWNSTREAM250 ), percentMap.get( TSS_DOWNSTREAM250 ) ) );

        tSSExport.add( statisticsExportData );

        return tSSExport;
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "Transcription Analysis Table" );
        sheetNames.add( "Parameters and Statistics" );
        return sheetNames;
    }


    /**
     * Adds the rows corresponding to a feature to the given tssRow (name,
     * start, stop).
     * <p>
     * @param feature     the feature to add. In case it is null the row
     *                    receives
     *                    "-" entries.
     * @param tssRow      the row to which the data should be added
     * @param tss         The TSS fo which the data shall be added
     * @param addDistance true, if the distance from the feature start to the
     *                    current TSS shall be printed, too
     */
    private void addFeatureRows( PersistentFeature feature, List<Object> tssRow, TranscriptionStart tss, boolean addDistance ) {
        if( feature != null ) {
            tssRow.add( feature.toString() );
            tssRow.add( feature.getLocus() );
            tssRow.add( feature.getEcNumber() );
            tssRow.add( feature.getProduct() );
            tssRow.add( feature.getStartOnStrand() );
            tssRow.add( feature.getStopOnStrand() );
            if( addDistance ) {
                tssRow.add( Math.abs( tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()) ) );
            }
        }
        else {
            tssRow.add( "-" );
            tssRow.add( "-" );
            tssRow.add( "-" );
            tssRow.add( "-" );
            tssRow.add( "" );
            tssRow.add( "" );
            if( addDistance ) {
                tssRow.add( "" );
            }
        }
    }


    /**
     * Calculates all TSS related statistics.
     * <p>
     * @param tssResult The result to add to the statistics.
     */
    private void calcStats( List<TranscriptionStart> tssResult ) {
        this.initStatsMap();
        ParameterSetTSS params = (ParameterSetTSS) this.getParameters();

        int noCorrectStarts = 0;
        int noFwdFeatures = 0;
        int noRevFeatures = 0;
        int noUnannotatedTranscripts = 0;
        int noLeaderlessTranscripts = 0;
        int noUpstreamFeature = 0;
        int noUpstreamFeature1 = 0;
        int noUpstreamFeature5 = 0;
        int noUpstreamFeature10 = 0;
        int noUpstreamFeature20 = 0;
        int noUpstreamFeature50 = 0;
        int noUpstreamFeature100 = 0;
        int noUpstreamFeature250 = 0;
        int noDownstreamFeature = 0;
        int noDownstreamFeature1 = 0;
        int noDownstreamFeature5 = 0;
        int noDownstreamFeature10 = 0;
        int noDownstreamFeature20 = 0;
        int noDownstreamFeature50 = 0;
        int noDownstreamFeature100 = 0;
        int noDownstreamFeature250 = 0;

        int distance;
        DetectedFeatures detFeatures;
        PersistentFeature feature;

        for( TranscriptionStart tss : tssResult ) {

            if( tss.isFwdStrand() ) {
                ++noFwdFeatures;
            }
            else {
                ++noRevFeatures;
            }


            detFeatures = tss.getDetFeatures();
            feature = detFeatures.getCorrectStartFeature();
            if( feature != null ) {
                ++noCorrectStarts;
            }
            feature = detFeatures.getUpstreamFeature();
            if( feature != null ) {
                ++noUpstreamFeature;

                distance = Math.abs( tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()) );
                if( distance == 1 ) {
                    ++noUpstreamFeature1;
                }
                else if( distance > 1 && distance < 6 ) {
                    ++noUpstreamFeature5;
                }
                else if( distance > 5 && distance < 11 ) {
                    ++noUpstreamFeature10;
                }
                else if( distance > 10 && distance < 21 ) {
                    ++noUpstreamFeature20;
                }
                else if( distance > 20 && distance < 51 ) {
                    ++noUpstreamFeature50;
                }
                else if( distance > 50 && distance < 101 ) {
                    ++noUpstreamFeature100;
                }
                else if( distance > 100 && distance < 251 ) {
                    ++noUpstreamFeature250;
                }
            }
            feature = detFeatures.getDownstreamFeature();
            if( feature != null ) {
                ++noDownstreamFeature;

                distance = Math.abs( tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()) );
                if( distance <= params.getMaxLeaderlessDistance() ) {
                    ++noLeaderlessTranscripts;
                }

                if( distance == 1 ) {
                    ++noDownstreamFeature1;
                }
                else if( distance > 1 && distance < 6 ) {
                    ++noDownstreamFeature5;
                }
                else if( distance > 5 && distance < 11 ) {
                    ++noDownstreamFeature10;
                }
                else if( distance > 10 && distance < 21 ) {
                    ++noDownstreamFeature20;
                }
                else if( distance > 20 && distance < 51 ) {
                    ++noDownstreamFeature50;
                }
                else if( distance > 50 && distance < 101 ) {
                    ++noDownstreamFeature100;
                }
                else if( distance > 100 && distance < 251 ) {
                    ++noDownstreamFeature250;
                }
            }

            if( tss instanceof TransStartUnannotated ) {
                ++noUnannotatedTranscripts;
            }
        }

        //create statistics
        this.getStatsMap().put( TSS_TOTAL, this.getStatsMap().get( TSS_TOTAL ) + tssResult.size() );
        this.getStatsMap().put( TSS_CORRECT, this.getStatsMap().get( TSS_CORRECT ) + noCorrectStarts );
        this.getStatsMap().put( TSS_LEADERLESS, this.getStatsMap().get( TSS_LEADERLESS ) + noLeaderlessTranscripts + noCorrectStarts );
        this.getStatsMap().put( TSS_FWD, this.getStatsMap().get( TSS_FWD ) + noFwdFeatures );
        this.getStatsMap().put( TSS_REV, this.getStatsMap().get( TSS_REV ) + noRevFeatures );
        if( params.isPerformUnannotatedTranscriptDet() ) {
            this.getStatsMap().put( TSS_NOVEL, this.getStatsMap().get( TSS_NOVEL ) + noUnannotatedTranscripts );
        }
        else {
            this.getStatsMap().put( TSS_NOVEL, UNUSED_STATISTICS_VALUE );
        }
        this.getStatsMap().put( TSS_UPSTREAM, this.getStatsMap().get( TSS_UPSTREAM ) + noUpstreamFeature );
        this.getStatsMap().put( TSS_UPSTREAM1, this.getStatsMap().get( TSS_UPSTREAM1 ) + noUpstreamFeature1 );
        this.getStatsMap().put( TSS_UPSTREAM5, this.getStatsMap().get( TSS_UPSTREAM5 ) + noUpstreamFeature5 );
        this.getStatsMap().put( TSS_UPSTREAM10, this.getStatsMap().get( TSS_UPSTREAM10 ) + noUpstreamFeature10 );
        this.getStatsMap().put( TSS_UPSTREAM20, this.getStatsMap().get( TSS_UPSTREAM20 ) + noUpstreamFeature20 );
        this.getStatsMap().put( TSS_UPSTREAM50, this.getStatsMap().get( TSS_UPSTREAM50 ) + noUpstreamFeature50 );
        this.getStatsMap().put( TSS_UPSTREAM100, this.getStatsMap().get( TSS_UPSTREAM100 ) + noUpstreamFeature100 );
        this.getStatsMap().put( TSS_UPSTREAM250, this.getStatsMap().get( TSS_UPSTREAM250 ) + noUpstreamFeature250 );
        this.getStatsMap().put( TSS_DOWNSTREAM, this.getStatsMap().get( TSS_DOWNSTREAM ) + noDownstreamFeature );
        this.getStatsMap().put( TSS_DOWNSTREAM1, this.getStatsMap().get( TSS_DOWNSTREAM1 ) + noDownstreamFeature1 );
        this.getStatsMap().put( TSS_DOWNSTREAM5, this.getStatsMap().get( TSS_DOWNSTREAM5 ) + noDownstreamFeature5 );
        this.getStatsMap().put( TSS_DOWNSTREAM10, this.getStatsMap().get( TSS_DOWNSTREAM10 ) + noDownstreamFeature10 );
        this.getStatsMap().put( TSS_DOWNSTREAM20, this.getStatsMap().get( TSS_DOWNSTREAM20 ) + noDownstreamFeature20 );
        this.getStatsMap().put( TSS_DOWNSTREAM50, this.getStatsMap().get( TSS_DOWNSTREAM50 ) + noDownstreamFeature50 );
        this.getStatsMap().put( TSS_DOWNSTREAM100, this.getStatsMap().get( TSS_DOWNSTREAM100 ) + noDownstreamFeature100 );
        this.getStatsMap().put( TSS_DOWNSTREAM250, this.getStatsMap().get( TSS_DOWNSTREAM250 ) + noDownstreamFeature250 );
    }


    /**
     * Initializes the statistics map.
     */
    private void initStatsMap() {
        this.getStatsMap().put( TSS_TOTAL, 0 );
        this.getStatsMap().put( TSS_CORRECT, 0 );
        this.getStatsMap().put( TSS_LEADERLESS, 0 );
        this.getStatsMap().put( TSS_FWD, 0 );
        this.getStatsMap().put( TSS_REV, 0 );
        this.getStatsMap().put( TSS_NOVEL, 0 );
        this.getStatsMap().put( TSS_UPSTREAM, 0 );
        this.getStatsMap().put( TSS_UPSTREAM1, 0 );
        this.getStatsMap().put( TSS_UPSTREAM5, 0 );
        this.getStatsMap().put( TSS_UPSTREAM10, 0 );
        this.getStatsMap().put( TSS_UPSTREAM20, 0 );
        this.getStatsMap().put( TSS_UPSTREAM50, 0 );
        this.getStatsMap().put( TSS_UPSTREAM100, 0 );
        this.getStatsMap().put( TSS_UPSTREAM250, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM1, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM5, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM10, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM20, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM50, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM100, 0 );
        this.getStatsMap().put( TSS_DOWNSTREAM250, 0 );
    }


}