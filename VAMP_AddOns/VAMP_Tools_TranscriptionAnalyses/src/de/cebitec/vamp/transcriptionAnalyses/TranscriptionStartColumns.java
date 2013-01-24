package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 * 
 * @author -Rolf Hilker-
 */
public class TranscriptionStartColumns implements ExcelExportDataI {
    
    private final TssDetectionResult tssDetectionResult;
    private final HashMap<String, Integer> tssStatisticMap;

    /** 
     * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
     * Generates both, the header and the data to write.
     * @param tssDetectionResult the tss detection result to display
     * @param tssStatisticMap statistics of the tss detection 
     */
    public TranscriptionStartColumns(TssDetectionResult tssDetectionResult, HashMap<String, Integer> tssStatisticMap) {
        this.tssDetectionResult = tssDetectionResult;
        this.tssStatisticMap = tssStatisticMap;
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
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Initial Coverage");
        dataColumnDescriptions.add("Gene Start Coverage");
        dataColumnDescriptions.add("Coverage Increase");
        dataColumnDescriptions.add("Coverage Increase %");
        dataColumnDescriptions.add("Correct Start Feature");
        dataColumnDescriptions.add("Correct Start Feature Start");
        dataColumnDescriptions.add("Correct Start Feature Stop");
        dataColumnDescriptions.add("Next Upstream Feature");
        dataColumnDescriptions.add("Next Upstream Feature Start");
        dataColumnDescriptions.add("Next Upstream Feature Stop");
        dataColumnDescriptions.add("Next Downstream Feature");
        dataColumnDescriptions.add("Next Downstream Feature Start");
        dataColumnDescriptions.add("Next Downstream Feature Stop");
        dataColumnDescriptions.add("Unannotated Transcript");
        dataColumnDescriptions.add("Transcript Stop");
        dataColumnDescriptions.add("70bp Upstream of Start");
        
        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Transcription start site detection parameter and statistics table");

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
        
        List<TranscriptionStart> results = this.tssDetectionResult.getResults();
        List<String> promotorRegions = this.tssDetectionResult.getPromotorRegions();
        
        for (int i = 0; i < results.size(); ++i) {      
            TranscriptionStart geneStart = results.get(i);
            List<Object> geneStartRow = new ArrayList<>();
            
            int percentageIncrease;
            if (geneStart.getInitialCoverage() > 0) {
                percentageIncrease = (int) (((double) geneStart.getStartCoverage() / (double) geneStart.getInitialCoverage()) * 100.0) - 100;
            } else {
                percentageIncrease = Integer.MAX_VALUE;
            }
            
            geneStartRow.add(geneStart.getPos());
            geneStartRow.add(geneStart.isFwdStrand() ? "Fwd" : "Rev");
            geneStartRow.add(geneStart.getInitialCoverage());
            geneStartRow.add(geneStart.getStartCoverage());
            geneStartRow.add(geneStart.getStartCoverage() - geneStart.getInitialCoverage());
            geneStartRow.add(percentageIncrease);
            
            DetectedFeatures detFeatures = geneStart.getDetFeatures();
            PersistantFeature feature = detFeatures.getCorrectStartFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            feature = detFeatures.getUpstreamFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            feature = detFeatures.getDownstreamFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            if (geneStart instanceof TransStartUnannotated) {
                TransStartUnannotated unannoStart = (TransStartUnannotated) geneStart;
                geneStartRow.add("yes");
                geneStartRow.add(unannoStart.getDetectedStop());
            } else {
                geneStartRow.add("-");
                geneStartRow.add("-");
            }
           
            geneStartRow.add(promotorRegions.get(i));
            
            tSSResults.add(geneStartRow);
        }
        
        tSSExport.add(tSSResults);
        
        
        //create statistics sheet
        ParameterSetTSS tssParameters = (ParameterSetTSS) this.tssDetectionResult.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();

        statisticsExport.add("Transcription Start Site Detection Statistics for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(tssDetectionResult.getTrackList()));
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Transcription start site detection parameters:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum total coverage increase:");
        statisticsExport.add(tssParameters.getMinTotalIncrease());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum percent of increase:");
        statisticsExport.add(tssParameters.getMinPercentIncrease());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Maximum initial low coverage count:");
        statisticsExport.add(tssParameters.getMaxLowCovInitCount());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum low coverage increase:");
        statisticsExport.add(tssParameters.getMinLowCovIncrease());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Detect unannotated transcripts?");
        statisticsExport.add(tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum transcript extension coverage:");
        statisticsExport.add(tssParameters.getMinTranscriptExtensionCov());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Transcription start site statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_TOTAL);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_TOTAL));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_CORRECT);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_CORRECT));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_UPSTREAM);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_UPSTREAM));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_DOWNSTREAM);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_DOWNSTREAM));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_FWD);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_FWD));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_REV);
        statisticsExport.add(tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_REV));
        statisticsExportData.add(statisticsExport);
        
        int noUnannotatedTrans = this.tssStatisticMap.get(ResultPanelTranscriptionStart.TSS_UNANNOTATED);
        String unannotatedTransValue = noUnannotatedTrans
                == ResultPanelTranscriptionStart.UNUSED_STATISTICS_VALUE ? "-" : String.valueOf(noUnannotatedTrans);
        statisticsExport = new ArrayList<>();
        statisticsExport.add(ResultPanelTranscriptionStart.TSS_UNANNOTATED);
        statisticsExport.add(unannotatedTransValue);
        statisticsExportData.add(statisticsExport);

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
}
