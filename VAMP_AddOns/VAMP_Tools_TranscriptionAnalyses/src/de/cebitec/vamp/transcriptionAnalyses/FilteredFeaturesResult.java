package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for all data belonging to a filtered features result. Also converts 
 * the list of FilteredFeatures into the format readable for the ExcelExporter.
 * Generates all three, the sheet names, headers and data to write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilteredFeaturesResult extends ResultTrackAnalysis<ParameterSetFilteredFeatures> implements ExcelExportDataI {
    
    private final List<FilteredFeature> filteredFeatures;
    private int noGenomeFeatures;

    /**
     * Container for all data belonging to a filtered features result. Also
     * converts the list of FilteredFeatures into the format readable for the
     * ExcelExporter. Generates all three, the sheet names, headers and data to
     * write.
     * @param trackMap map of trackids to tracks
     * @param filteredFeatures list of filtered features of this result
     */
    public FilteredFeaturesResult(Map<Integer, PersistantTrack> trackMap, List<FilteredFeature> filteredFeatures) {
        super(trackMap);
        this.filteredFeatures = filteredFeatures;
    }

    public List<FilteredFeature> getResults() {
        return filteredFeatures;
    }

    public void setNoGenomeFeatures(int noGenomeFeatures) {
        this.noGenomeFeatures = noGenomeFeatures;
    }

    public int getNoGenomeFeatures() {
        return noGenomeFeatures;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Filtered Feature");
        resultDescriptions.add("Track");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Total Read Count");

        dataColumnDescriptions.add(resultDescriptions);

        //add feature filering statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Feature Filtering Parameter and Statistics Table");

        dataColumnDescriptions.add(statisticColumnDescriptions);

        return dataColumnDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> filteredFeaturesExport = new ArrayList<>();
        List<List<Object>> filteredFeaturesResult = new ArrayList<>();

        for (FilteredFeature filteredFeature : this.getResults()) {
            List<Object> filteredGeneRow = new ArrayList<>();

            filteredGeneRow.add(PersistantFeature.Utils.getFeatureName(filteredFeature.getFilteredFeature()));
            filteredGeneRow.add(this.getTrackMap().get(filteredFeature.getTrackId()));
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStart());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().getStop());
            filteredGeneRow.add(filteredFeature.getFilteredFeature().isFwdStrandString());
            filteredGeneRow.add(filteredFeature.getReadCount());

            filteredFeaturesResult.add(filteredGeneRow);
        }

        filteredFeaturesExport.add(filteredFeaturesResult);

        //create statistics sheet
        ParameterSetFilteredFeatures filterParameters = (ParameterSetFilteredFeatures) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();
        
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Feature filtering statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList())));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Feature Filtering parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum number of reads in a feature:", filterParameters.getMinNumberReads()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Maximum number of reads in a feature:", filterParameters.getMaxNumberReads()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Feature filtering statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelFilteredFeatures.FEATURES_FILTERED, 
                this.getStatsMap().get(ResultPanelFilteredFeatures.FEATURES_FILTERED)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelFilteredFeatures.FEATURES_TOTAL, noGenomeFeatures));

        filteredFeaturesExport.add(statisticsExportData);

        return filteredFeaturesExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Filtered Features Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
    
}
