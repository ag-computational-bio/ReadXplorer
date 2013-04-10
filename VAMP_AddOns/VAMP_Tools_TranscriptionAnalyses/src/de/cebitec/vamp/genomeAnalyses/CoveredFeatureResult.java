package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.GeneralUtils;
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
public class CoveredFeatureResult extends ResultTrackAnalysis<ParameterSetCoveredFeatures> implements ExcelExportDataI {
    
    private List<CoveredFeature> results;
    private int featureListSize;

    /**
     * Container for all data belonging to a covered feature detection result.
     * Also converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * @param results the results of the covered feature detection
     * @param trackMap the map of track ids to the tracks, for which the covered
     * feature detection was carried out
     * @param currentTrack the track on which this analysis result was generated
     */
    public CoveredFeatureResult(List<CoveredFeature> results, HashMap<Integer, PersistantTrack> trackMap) {
        super(trackMap);
        this.results = results;
        
    }
    
    public void setFeatureListSize(int size) {
        this.featureListSize = size;
    }
    
    public int getFeatureListSize() {
        return featureListSize;
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
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Covered Feature");
        resultDescriptions.add("Track");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Length");
        resultDescriptions.add("Covered Percent");
        resultDescriptions.add("Covered Bases Count");

        dataColumnDescriptions.add(resultDescriptions);

        //add covered features detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Covered Features Detection Parameter and Statistics Table");

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
            coveredFeatureRow.add(PersistantFeature.Utils.getFeatureName(feature));
            coveredFeatureRow.add(this.getTrackMap().get(coveredFeature.getTrackId()));
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

        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Covered feature detection statistics for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList())));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Covered feature detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum covered percent:", parameters.getMinCoveredPercent()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum counted coverage:", parameters.getMinCoverageCount()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Covered feature statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Total number of covered features", coveredFeaturesResultList.size()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Total number of reference features", featureListSize));

        coveredFeaturesExport.add(statisticsExportData);


        return coveredFeaturesExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Covered Features Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }
}
