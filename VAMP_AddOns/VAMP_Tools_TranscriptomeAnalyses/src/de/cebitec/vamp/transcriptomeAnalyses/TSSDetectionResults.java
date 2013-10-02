/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.TranscriptionStart;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class TSSDetectionResults extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses>{ 

    private List<TranscriptionStart> results;
    
    public TSSDetectionResults(List<TranscriptionStart> results, Map<Integer, PersistantTrack> trackMap) {
        super(trackMap);
        this.results = results;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();
        
        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Cunt");
        dataColumnDescriptions.add("Rel. count");
        dataColumnDescriptions.add("-10");
        dataColumnDescriptions.add("-9");
        dataColumnDescriptions.add("-8");
        dataColumnDescriptions.add("-7");
        dataColumnDescriptions.add("-6");
        dataColumnDescriptions.add("-5");
        dataColumnDescriptions.add("-4");
        dataColumnDescriptions.add("-3");
        dataColumnDescriptions.add("-2");
        dataColumnDescriptions.add("-1");
        dataColumnDescriptions.add("Feature");
        dataColumnDescriptions.add("Offset");
        dataColumnDescriptions.add("dist. to start");
        dataColumnDescriptions.add("dist. to stop");
        dataColumnDescriptions.add("Next gene");
        dataColumnDescriptions.add("Next offset");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Strand");
        
        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Transcription Start Site Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);
        
        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();
        
        for (int i = 0; i < results.size(); ++i) {      
            TranscriptionStart tss = results.get(i);
            List<Object> tssRow = new ArrayList<>();
            
//            tssRow.add(this.getTrackMap().get(tss.getTrackId()));
            
            tssRow.add(tss.getPos());
            tssRow.add(tss.getReadStarts());
            tssRow.add(tss.getRelCount());
                for (int c : tss.getBeforeCounts()) {
                    tssRow.add((Integer) c);
                }
            
            tssRow.add(tss.getDetectedGene());
            tssRow.add(tss.getOffset());
            tssRow.add(tss.getDist2start());
            tssRow.add(tss.getDist2stop());
            tssRow.add(tss.getNextGene());
            tssRow.add(tss.getNextOffset());
            tssRow.add(tss.getSequence());
            
//            if (tss instanceof TransStartUnannotated) {
//                TransStartUnannotated unannoStart = (TransStartUnannotated) tss;
//                tssRow.add("yes");
//                tssRow.add(unannoStart.getDetectedStop());
//            } else {
//                tssRow.add("-");
//                tssRow.add("-");
//            }
           
//            tssRow.add(promotorRegions.get(i));
            tssRow.add(tss.isFwdStrand() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING);
            tSSResults.add(tssRow);
        }
        
        tSSExport.add(tSSResults);
        
        
        //create statistics sheet
        ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                "Transcription start site detection statistics for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Transcription start site detection parameters:"));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum number of read starts:", 
//                tssParameters.getMinNoReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum percent of coverage increase:", 
//                tssParameters.getMinPercentIncrease()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Maximum low coverage read start count:", 
//                tssParameters.getMaxLowCovReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum low coverage read starts:", 
//                tssParameters.getMinLowCovReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Detect novel transcripts?", 
//                tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no"));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum transcript extension coverage:", 
//                tssParameters.getMinTranscriptExtensionCov()));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Transcription start site statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_TOTAL, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_CORRECT, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_CORRECT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_UPSTREAM, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_UPSTREAM)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_DOWNSTREAM, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_DOWNSTREAM)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FWD, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_FWD)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_REV, 
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_REV)));
        
        int noUnannotatedTrans = this.getStatsMap().get(ResultPanelTranscriptionStart.TSS_NOVEL);
        String unannotatedTransValue = noUnannotatedTrans
                == ResultPanelTranscriptionStart.UNUSED_STATISTICS_VALUE ? "-" : String.valueOf(noUnannotatedTrans);
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_NOVEL, 
                unannotatedTransValue));

        tSSExport.add(statisticsExportData);
        
        return tSSExport;
    }

    public List<TranscriptionStart> getResults() {
        return results;
    }

    public void setResults(List<TranscriptionStart> results) {
        this.results = results;
    }
    
    
    
}
