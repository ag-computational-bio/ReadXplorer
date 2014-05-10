package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.readXplorer.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.readXplorer.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Container for all data belonging to an operon detection result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class OperonDetectionResult extends ResultTrackAnalysis<ParameterSetOperonDet> {
    //sdfsdf
    private final List<Operon> detectedOperons;

    public OperonDetectionResult(Map<Integer, PersistantTrack> trackList, List<Operon> detectedOperons, 
            int referenceId, boolean combineTracks, int trackColumn, int filterColumn) {
        super(trackList, referenceId, combineTracks, trackColumn, filterColumn);
        this.detectedOperons = detectedOperons;
    }

    public List<Operon> getResults() {
        return detectedOperons;
    }
    
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature 1");
        dataColumnDescriptions.add("Feature 2");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Anno 1");
        dataColumnDescriptions.add("Start Anno 2");
        dataColumnDescriptions.add("Reads Overlap Stop 1");
        dataColumnDescriptions.add("Reads Overlap Start 2");
        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Operon Detection Parameters and Statistics");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> operonResults = new ArrayList<>();

        for (Operon operon : this.detectedOperons) {
            PersistantFeature feat1 = operon.getOperonAdjacencies().get(0).getFeature1();
            String annoName1 = "";
            String annoName2 = "";
            String strand = feat1.isFwdStrandString() + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
            String readsAnno1 = "";
            String readsAnno2 = "";
            String internalReads = "";
            String spanningReads = "";

            for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
                readsAnno1 += opAdj.getReadsFeature1() + "\n";
                readsAnno2 += opAdj.getReadsFeature2() + "\n";
                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add(annoName1);
            operonsRow.add(annoName2);
            operonsRow.add(this.getTrackEntry(operon.getTrackId(), true));
            operonsRow.add(this.getChromosomeMap().get(feat1.getChromId()));
            operonsRow.add(strand);
            operonsRow.add(startAnno1);
            operonsRow.add(startAnno2);
            operonsRow.add(readsAnno1);
            operonsRow.add(readsAnno2);
            operonsRow.add(internalReads);
            operonsRow.add(spanningReads);

            operonResults.add(operonsRow);
        }

        exportData.add(operonResults);

        //create statistics sheet
        ParameterSetOperonDet operonDetectionParameters = (ParameterSetOperonDet) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Operon detection statistics for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Operon detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum number of spanning reads:", 
                operonDetectionParameters.getMinSpanningReads()));
        operonDetectionParameters.getReadClassParams().addReadClassParamsToStats(statisticsExportData);

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Operon detection statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelOperonDetection.OPERONS_TOTAL, 
                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS, 
                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_WITH_OVERLAPPING_READS)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS, 
                this.getStatsMap().get(ResultPanelOperonDetection.OPERONS_WITH_INTERNAL_READS)));

        exportData.add(statisticsExportData);

        return exportData;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Operon Detection Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
}
