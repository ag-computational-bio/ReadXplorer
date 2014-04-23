package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.exporter.tables.ExportDataI;
import de.cebitec.readXplorer.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to a covered feature detection result. Also
 * converts a all data into the format readable for the ExcelExporter. Generates
 * all three, the sheet names, headers and data to write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeatureResult extends ResultTrackAnalysis<ParameterSetCoveredFeatures> implements ExportDataI {
    
    private List<CoveredFeature> results;

    /**
     * Container for all data belonging to a covered feature detection result.
     * Also converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * @param results the results of the covered feature detection
     * @param trackMap the map of track ids to the tracks, for which the covered
     * feature detection was carried out
     * @param currentTrack the track on which this analysis result was generated
     */
    public CoveredFeatureResult(List<CoveredFeature> results, HashMap<Integer, PersistantTrack> trackMap, int referenceId, 
            boolean combineTracks, int trackColumn, int filterColumn) {
        super(trackMap, referenceId, combineTracks, trackColumn, filterColumn);
        this.results = results;
        
    }

    /**
     * @return The current content of the result list.
     */
    public List<CoveredFeature> getResults() {
        return results;
    }
    
    /**
     * Use this method when adding new results to the current results. It
     * synchronizes the list and prevents making changes during the adding
     * process.
     * @param coveredFeatures 
     */
    public void addAllToResult(List<CoveredFeature> coveredFeatures) {
        this.results.addAll(coveredFeatures);
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        
        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String coveredString = parameters.isGetCoveredFeatures() ? "Covered" : "Uncovered";
        
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();
        
        resultDescriptions.add(coveredString + " Feature");
        resultDescriptions.add("Track");
        resultDescriptions.add("Chromosome");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Length");
        resultDescriptions.add("Covered Percent");
        resultDescriptions.add("Covered Bases Count");

        dataColumnDescriptions.add(resultDescriptions);

        //add covered features detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add(coveredString + " Features Detection Parameter and Statistics Table");

        dataColumnDescriptions.add(statisticColumnDescriptions);

        return dataColumnDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredFeaturesExport = new ArrayList<>();
        List<List<Object>> coveredFeaturesResultList = new ArrayList<>();

        PersistantFeature feature;
        for (CoveredFeature coveredFeature : this.results) {
            List<Object> coveredFeatureRow = new ArrayList<>();
            feature = coveredFeature.getCoveredFeature();
            coveredFeatureRow.add(feature.toString());
            coveredFeatureRow.add(this.getTrackEntry(coveredFeature.getTrackId(), true));
            coveredFeatureRow.add(this.getChromosomeMap().get(feature.getChromId()));
            coveredFeatureRow.add(feature.isFwdStrandString());
            coveredFeatureRow.add(feature.isFwdStrand() ? feature.getStart() : feature.getStop());
            coveredFeatureRow.add(feature.isFwdStrand() ? feature.getStop() : feature.getStart());
            coveredFeatureRow.add(feature.getStop() - feature.getStart());
            coveredFeatureRow.add(coveredFeature.getPercentCovered());
            coveredFeatureRow.add(coveredFeature.getNoCoveredBases());

            coveredFeaturesResultList.add(coveredFeatureRow);
        }

        coveredFeaturesExport.add(coveredFeaturesResultList);



        //create statistics sheet
        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String coveredString = parameters.isGetCoveredFeatures() ? "Covered" : "Uncovered";
        
        List<List<Object>> statisticsExportData = new ArrayList<>();
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(coveredString + " feature detection statistics for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(coveredString + " feature detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum covered percent:", parameters.getMinCoveredPercent()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum counted coverage:", parameters.getMinCoverageCount()));
        String whateverStrand = parameters.isWhateverStrand() ? "no" : "yes";
        String uncoveredFeatures = parameters.isGetCoveredFeatures() ? "no" : "yes";
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Only count read on feature strand:", whateverStrand));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Detect uncovered instead of covered features:", uncoveredFeatures));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(coveredString + " feature statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelCoveredFeatures.FEATURES_COVERED, coveredFeaturesResultList.size()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                ResultPanelCoveredFeatures.FEATURES_TOTAL, this.getStatsMap().get(ResultPanelCoveredFeatures.FEATURES_TOTAL)));

        coveredFeaturesExport.add(statisticsExportData);

        return coveredFeaturesExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        
        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.getParameters();
        String tableHeader;
        if (parameters.isGetCoveredFeatures()) {
            tableHeader = "Covered Features Table";
        } else {
            tableHeader = "Uncovered Features Table";
        }
        sheetNames.add(tableHeader);
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
}
