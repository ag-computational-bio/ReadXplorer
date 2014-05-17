/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Container for all data belonging to a transcription start site detection
 * result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TssDetectionResult extends ResultTrackAnalysis<ParameterSetTSS> {
    
    private List<TranscriptionStart> results;
    private List<String> promotorRegions;
    
    /**
     * Container for all data belonging to a transcription start site detection
     * result.
     * @param results the results of the TSS detection
     * @param trackList the list of tracks, for which the TSS detection was carried out
     * @param referenceId id of the reference genome, for which this result was
     * generated
     * @param combineTracks <cc>true</cc>, if the tracks in the list are
     * combined, <cc>false</cc> otherwise
     */
    public TssDetectionResult(List<TranscriptionStart> results, Map<Integer, PersistantTrack> trackList, 
            int referenceId, boolean combineTracks, int trackColumn, int filterColumn) {
        super(trackList, referenceId, combineTracks, trackColumn, filterColumn);
        this.results = results;
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
     * @param promotorRegions Promotor regions of the detected TSS 
     */
    public void setPromotorRegions(List<String> promotorRegions) {
        this.promotorRegions = promotorRegions;
    }
    
    /**
     * @return creates and returns the list of transcription start site descriptions 
     * for the columns of the table.
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();
        
        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("No Read Starts");
        dataColumnDescriptions.add("Coverage Increase");
        dataColumnDescriptions.add("Coverage Increase %");
        dataColumnDescriptions.add("Correct Start Feature");
        dataColumnDescriptions.add("Correct Start Feature Start");
        dataColumnDescriptions.add("Correct Start Feature Stop");
        dataColumnDescriptions.add("Next Upstream Feature");
        dataColumnDescriptions.add("Next Upstream Feature Start");
        dataColumnDescriptions.add("Next Upstream Feature Stop");
        dataColumnDescriptions.add("Distance Upstream Feature");
        dataColumnDescriptions.add("Next Downstream Feature");
        dataColumnDescriptions.add("Next Downstream Feature Start");
        dataColumnDescriptions.add("Next Downstream Feature Stop");
        dataColumnDescriptions.add("Distance Downstream Feature");
        dataColumnDescriptions.add("Novel Transcript");
        dataColumnDescriptions.add("Transcript Stop");
        dataColumnDescriptions.add("70bp Upstream of Start");
        dataColumnDescriptions.add("Correct Start Locus");
        dataColumnDescriptions.add("Correct Start EC-Number");
        dataColumnDescriptions.add("Correct Start Product");
        dataColumnDescriptions.add("Next Upstream Locus");
        dataColumnDescriptions.add("Next Upstream EC-Number");
        dataColumnDescriptions.add("Next Upstream Product");
        dataColumnDescriptions.add("Next Downstream Locus");
        dataColumnDescriptions.add("Next Downstream EC-Number");
        dataColumnDescriptions.add("Next Downstream Product");
        
        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Transcription Start Site Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);
        
        return allSheetDescriptions;
    }

    /**
     * @return creates and returns the list of transcription start rows belonging 
     * to the transcription start site table.
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();
        
        PersistantFeature feature;
        
        for (int i = 0; i < results.size(); ++i) {      
            TranscriptionStart tss = results.get(i);
            List<Object> tssRow = new ArrayList<>();
            
            tssRow.add(tss.getPos());
            tssRow.add(this.getTrackEntry(tss.getTrackId(), true));
            tssRow.add(this.getChromosomeMap().get(tss.getChromId()));
            tssRow.add(tss.isFwdStrand() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING);
            tssRow.add(tss.getReadStartsAtPos());
            tssRow.add(tss.getCoverageIncrease());
            tssRow.add(tss.getPercentIncrease());
            
            DetectedFeatures detFeatures = tss.getDetFeatures();
            this.addFeatureRows(detFeatures.getCorrectStartFeature(), tssRow, tss, false);
            this.addFeatureRows(detFeatures.getUpstreamFeature(), tssRow, tss, true);
            this.addFeatureRows(detFeatures.getDownstreamFeature(), tssRow, tss, true);
            
            if (tss instanceof TransStartUnannotated) {
                TransStartUnannotated unannoStart = (TransStartUnannotated) tss;
                tssRow.add("yes");
                tssRow.add(unannoStart.getDetectedStop());
            } else {
                tssRow.add("-");
                tssRow.add("-");
            }
           
            tssRow.add(promotorRegions.get(i));
            
            tSSResults.add(tssRow);
        }
        
        tSSExport.add(tSSResults);
        
        
        //create statistics sheet
        ParameterSetTSS tssParameters = (ParameterSetTSS) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTableRow(
                "Transcription start site detection statistics for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Transcription start site detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum number of read starts:", 
                tssParameters.getMinNoReadStarts()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum percent of coverage increase:", 
                tssParameters.getMinPercentIncrease()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Maximum low coverage read start count:", 
                tssParameters.getMaxLowCovReadStarts()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum low coverage read starts:", 
                tssParameters.getMinLowCovReadStarts()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Detect novel transcripts?", 
                tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum transcript extension coverage:", 
                tssParameters.getMinTranscriptExtensionCov()));
        tssParameters.getReadClassParams().addReadClassParamsToStats(statisticsExportData);
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Transcription start site statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_TOTAL, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_CORRECT, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_CORRECT)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_UPSTREAM, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_UPSTREAM)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_DOWNSTREAM, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_DOWNSTREAM)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_LEADERLESS, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_LEADERLESS)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_FWD, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_FWD)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_REV, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_REV)));
        
        int noUnannotatedTrans = this.getStatsMap().get(ResultPanelTranscriptionStart.TSS_NOVEL);
        String unannotatedTransValue = noUnannotatedTrans
                == ResultPanelTranscriptionStart.UNUSED_STATISTICS_VALUE ? "-" : String.valueOf(noUnannotatedTrans);
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_NOVEL, 
                unannotatedTransValue));

        tSSExport.add(statisticsExportData);
        
        return tSSExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
    
    /**
     * Adds the rows corresponding to a feature to the given tssRow (name, 
     * start, stop).
     * @param feature the feature to add. In case it is null the row receives 
     * "-" entries.
     * @param tssRow the row to which the data should be added
     * @param tss The TSS fo which the data shall be added
     * @param addDistance true, if the distance from the feature start to the
     * current TSS shall be printed, too
     */
    private void addFeatureRows(PersistantFeature feature, List<Object> tssRow, TranscriptionStart tss, boolean addDistance) {
        if (feature != null) {
            tssRow.add(feature.toString());
            tssRow.add(feature.getLocus());
            tssRow.add(feature.getEcNumber());
            tssRow.add(feature.getProduct());
            tssRow.add(feature.isFwdStrand() ? feature.getStart() : feature.getStop());
            tssRow.add(feature.isFwdStrand() ? feature.getStop() : feature.getStart());
            if (addDistance) {
                tssRow.add(Math.abs(tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop())));
            }
        } else {
            tssRow.add("-");
            tssRow.add("-");
            tssRow.add("-");
            if (addDistance) { tssRow.add(""); }
        }
    }
    
}
