package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a List of Covered Annotations into the format readable for the
 * ExcelExporter. Generates all three, the sheet names, headers and data to
 * write.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredAnnosColumns implements ExcelExportDataI {
    
    private CoveredAnnotationResult coveredAnnosResult;

    /**
     * Converts a List of Covered Annotations into the format readable for the
     * ExcelExporter. Generates all three, the sheet names, headers and data to
     * write.
     * @param coveredAnnosResult the list of covered annotations
     */
    public CoveredAnnosColumns(CoveredAnnotationResult coveredAnnosResult) {
        this.coveredAnnosResult = coveredAnnosResult;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Covered Annotation");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Length");
        resultDescriptions.add("Covered Percent");
        resultDescriptions.add("Covered Bases Count");

        dataColumnDescriptions.add(resultDescriptions);
        
        //add covered annos detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Covered annotations detection parameter and statistics table");

        dataColumnDescriptions.add(statisticColumnDescriptions);

        return dataColumnDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredAnnosExport = new ArrayList<>();
        List<List<Object>> coveredAnnosResultList = new ArrayList<>();

        PersistantAnnotation anno;
        for (CoveredAnnotation coveredAnno : this.coveredAnnosResult.getResults()) {
            List<Object> coveredAnnoRow = new ArrayList<>();
            anno = coveredAnno.getCoveredAnnotation();
            coveredAnnoRow.add(PersistantAnnotation.getAnnotationName(anno));
            coveredAnnoRow.add(anno.isFwdStrand() ? "Fwd" : "Rev");
            coveredAnnoRow.add(anno.getStart());
            coveredAnnoRow.add(anno.getStop());
            coveredAnnoRow.add(anno.getStop() - anno.getStart());
            coveredAnnoRow.add(coveredAnno.getPercentCovered());
            coveredAnnoRow.add(coveredAnno.getNoCoveredBases());

            coveredAnnosResultList.add(coveredAnnoRow);
        }
        
        coveredAnnosExport.add(coveredAnnosResultList);
        
        

        //create statistics sheet
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();

        statisticsExport.add("Covered Annotation Detection Statistics for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(coveredAnnosResult.getTrackList()));
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Covered annotation detection parameters:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum covered percent:");
        statisticsExport.add(this.coveredAnnosResult.getParameters().getMinCoveredPercent());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum counted coverage:");
        statisticsExport.add(this.coveredAnnosResult.getParameters().getMinCoverageCount());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Covered annotation statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Total number of covered annotations");
        statisticsExport.add(coveredAnnosResultList.size());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Total number of reference annotations");
        statisticsExport.add(coveredAnnosResult.getAnnotationListSize());
        statisticsExportData.add(statisticsExport);
        
        
        coveredAnnosExport.add(statisticsExportData);


        return coveredAnnosExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Covered Annotations Table");
        sheetNames.add("Covered Annotation Parameters/Stats");
        return sheetNames;
    }
}
