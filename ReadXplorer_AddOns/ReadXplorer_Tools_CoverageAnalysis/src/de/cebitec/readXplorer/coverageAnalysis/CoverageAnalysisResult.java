package de.cebitec.readXplorer.coverageAnalysis;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.exporter.excel.ExcelExportDataI;
import de.cebitec.readXplorer.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to a coverage analysis result. Also converts
 * a all data into the format readable for the ExcelExporter. Generates all
 * three, the sheet names, headers and data to write.
 *
 * @author Tobias Zimmermann, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageAnalysisResult extends ResultTrackAnalysis<ParameterSetCoverageAnalysis> implements ExcelExportDataI {

    private CoverageIntervalContainer results;

    public CoverageAnalysisResult(CoverageIntervalContainer results, HashMap<Integer, PersistantTrack> trackMap, 
            int referenceId, boolean combineTracks) {
        super(trackMap, referenceId, combineTracks, 0, 3);
        this.results = results;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();

        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
        String tableHeader;
        if (parameters.isDetectCoveredIntervals()) {
            tableHeader = "Covered Intervals Table";
        } else {
            tableHeader = "Uncovered Intervals Table";
        }
        sheetNames.add(tableHeader);
        sheetNames.add("Parameters and Statistics");
        return sheetNames;

    }

    @Override
    public List<List<String>> dataColumnDescriptions() {

        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
        String coveredString = parameters.isDetectCoveredIntervals() ? "Covered" : "Uncovered";

        List<List<String>> dataColumnDescriptions = new ArrayList<>();
        List<String> resultDescriptions = new ArrayList<>();

        resultDescriptions.add("Start");
        resultDescriptions.add("Stop");
        resultDescriptions.add("Track");
        resultDescriptions.add("Chromosome");
        resultDescriptions.add("Strand");
        resultDescriptions.add("Length");
        resultDescriptions.add("Mean Coverage");


        dataColumnDescriptions.add(resultDescriptions);

        //add covered interval detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add(coveredString + " Interval Analysis Parameter and Statistics Table");

        dataColumnDescriptions.add(statisticColumnDescriptions);

        return dataColumnDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> coveredIntervalsExport = new ArrayList<>();
        List<List<Object>> coveredIntervalsResultList = new ArrayList<>();

        fillTableRow(this.results.getCoverageIntervals(), coveredIntervalsResultList);
        fillTableRow(this.results.getCoverageIntervalsRev(), coveredIntervalsResultList);

        coveredIntervalsExport.add(coveredIntervalsResultList);

        //create statistics sheet
        ParameterSetCoverageAnalysis parameters = (ParameterSetCoverageAnalysis) this.getParameters();
         String coveredString = parameters.isDetectCoveredIntervals() ? "Covered" : "Uncovered";

        List<List<Object>> statisticsExportData = new ArrayList<>();
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(coveredString + " interval analysis statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(coveredString + " interval analysis detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum counted coverage:", parameters.getMinCoverageCount()));

        String coverageCount = parameters.isSumCoverageOfBothStrands() ? "sum coverage of both strands" : "treat each strand separately";
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Count coverage for:", coverageCount));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(coveredString + " interval analysis statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                ResultPanelCoverageAnalysis.NUMBER_INTERVALS, getStatsMap().get(ResultPanelCoverageAnalysis.NUMBER_INTERVALS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                ResultPanelCoverageAnalysis.MEAN_INTERVAL_LENGTH, getStatsMap().get(ResultPanelCoverageAnalysis.MEAN_INTERVAL_LENGTH)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                ResultPanelCoverageAnalysis.MEAN_INTERVAL_COVERAGE, getStatsMap().get(ResultPanelCoverageAnalysis.MEAN_INTERVAL_COVERAGE)));


        coveredIntervalsExport.add(statisticsExportData);


        return coveredIntervalsExport;
    }

    public CoverageIntervalContainer getResults() {
        return results;
    }

    private void fillTableRow(List<CoverageInterval> coverageList, List<List<Object>> coveredFeaturesResultList) {
        for (CoverageInterval interval : coverageList) {
            List<Object> coveredIntervalRow = new ArrayList<>();
            coveredIntervalRow.add(interval.isFwdStrand() ? interval.getStart() : interval.getStop());
            coveredIntervalRow.add(interval.isFwdStrand() ? interval.getStop() : interval.getStart());
            coveredIntervalRow.add(this.getTrackEntry(interval.getTrackId(), true));
            coveredIntervalRow.add(this.getChromosomeMap().get(interval.getChromId()));
            coveredIntervalRow.add(interval.getStrandString());
            coveredIntervalRow.add(interval.getLength());
            coveredIntervalRow.add(interval.getMeanCoverage());

            coveredFeaturesResultList.add(coveredIntervalRow);
        }
    }
}
