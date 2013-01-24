package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a List of Covered Features into the format readable for the
 * ExcelExporter. Generates all three, the sheet names, headers and data to
 * write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeaturesColumns implements ExcelExportDataI {
    
    private CoveredFeatureResult coveredFeaturesResult;

    /**
     * Converts a List of Covered Features into the format readable for the
     * ExcelExporter. Generates all three, the sheet names, headers and data to
     * write.
     * @param coveredFeaturesResult the list of covered features
     */
    public CoveredFeaturesColumns(CoveredFeatureResult coveredFeaturesResult) {
        this.coveredFeaturesResult = coveredFeaturesResult;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Covered Feature");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Length");
        resultDescriptions.add("Covered Percent");
        resultDescriptions.add("Covered Bases Count");

        dataColumnDescriptions.add(resultDescriptions);
        
        //add covered features detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Covered features detection parameter and statistics table");

        dataColumnDescriptions.add(statisticColumnDescriptions);

        return dataColumnDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredFeaturesExport = new ArrayList<>();
        List<List<Object>> coveredFeaturesResultList = new ArrayList<>();

        PersistantFeature anno;
        for (CoveredFeature coveredAnno : this.coveredFeaturesResult.getResults()) {
            List<Object> coveredAnnoRow = new ArrayList<>();
            anno = coveredAnno.getCoveredFeature();
            coveredAnnoRow.add(PersistantFeature.getFeatureName(anno));
            coveredAnnoRow.add(anno.isFwdStrand() ? "Fwd" : "Rev");
            coveredAnnoRow.add(anno.getStart());
            coveredAnnoRow.add(anno.getStop());
            coveredAnnoRow.add(anno.getStop() - anno.getStart());
            coveredAnnoRow.add(coveredAnno.getPercentCovered());
            coveredAnnoRow.add(coveredAnno.getNoCoveredBases());

            coveredFeaturesResultList.add(coveredAnnoRow);
        }
        
        coveredFeaturesExport.add(coveredFeaturesResultList);
        
        

        //create statistics sheet
        ParameterSetCoveredFeatures parameters = (ParameterSetCoveredFeatures) this.coveredFeaturesResult.getParameters();
        
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();

        statisticsExport.add("Covered Feature Detection Statistics for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(coveredFeaturesResult.getTrackList()));
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Covered feature detection parameters:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum covered percent:");
        statisticsExport.add(parameters.getMinCoveredPercent());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum counted coverage:");
        statisticsExport.add(parameters.getMinCoverageCount());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Covered feature statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Total number of covered features");
        statisticsExport.add(coveredFeaturesResultList.size());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Total number of reference features");
        statisticsExport.add(coveredFeaturesResult.getFeatureListSize());
        statisticsExportData.add(statisticsExport);
        
        
        coveredFeaturesExport.add(statisticsExportData);


        return coveredFeaturesExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Covered Features Table");
        sheetNames.add("Covered Feature Parameters & Stats");
        return sheetNames;
    }
}
