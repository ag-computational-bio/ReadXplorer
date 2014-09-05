package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.WizardPropertyStrings;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class NovelRegionResult extends ResultTrackAnalysis<ParameterSetWholeTranscriptAnalyses> {

    private List<NovelTranscript> results;
    private final StatisticsOnMappingData stats;
    private Map<String, Object> statsMap;
    private static final TableType TABLE_TYPE = TableType.NOVEL_TRANSCRIPTS_TABLE;

    public NovelRegionResult(PersistentReference reference, StatisticsOnMappingData stats, Map<Integer, PersistentTrack> trackMap, 
            List<NovelTranscript> novelRegions, boolean combineTracks) {
        super(reference, trackMap, combineTracks, 2, 0);
        this.results = novelRegions;
        this.stats = stats;
    }

    public List<NovelTranscript> getResults() {
        return this.results;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table - putative unannotated regions");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Putative Start Position");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("False Positive");
        dataColumnDescriptions.add("Selected For BLAST");
        dataColumnDescriptions.add("Finished");
        dataColumnDescriptions.add("Site");
        dataColumnDescriptions.add("Coverage Dropoff");
        dataColumnDescriptions.add("Length In Bp");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Chrom ID");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Track ID");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Novel Transcripts Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();

        for (int i = 0; i < results.size(); ++i) {
            NovelTranscript novelRegion = results.get(i);
            List<Object> novelRegionRow = new ArrayList<>();

            novelRegionRow.add(novelRegion.getStartPosition());
            novelRegionRow.add(novelRegion.isFwdDirection() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING);
            novelRegionRow.add(novelRegion.isFalsePositive());
            novelRegionRow.add(novelRegion.isSelected());
            novelRegionRow.add(novelRegion.isConsidered());
            novelRegionRow.add(novelRegion.getLocation());
            novelRegionRow.add(novelRegion.getDropOffPos());
            novelRegionRow.add(novelRegion.getLength());
            novelRegionRow.add(novelRegion.getSequence());
            novelRegionRow.add(this.getChromosomeMap().get(novelRegion.getChromId()));
            novelRegionRow.add(novelRegion.getChromId());
            novelRegionRow.add(this.getTrackMap().get(novelRegion.getTrackId()));
            novelRegionRow.add(novelRegion.getTrackId());

            tSSResults.add(novelRegionRow);
        }

        tSSExport.add(tSSResults);

        double mappingCount = (double) this.statsMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
        double meanMappingLength = (double) this.statsMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
        double mappingsPerMio = (double) this.statsMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
        double backgroundThreshold = (double) this.statsMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);

        //create statistics sheet
        ParameterSetWholeTranscriptAnalyses novelRegionParameters = (ParameterSetWholeTranscriptAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTableRow(
                "Novel Region detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Parameters:"));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow(WizardPropertyStrings.PROP_Fraction,
                novelRegionParameters.getFraction()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD,
                novelRegionParameters.isThresholdManuallySet() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE,
                String.valueOf(String.format("%2.2f", backgroundThreshold))));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH,
                novelRegionParameters.getMinLengthBoundary()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION,
                novelRegionParameters.isRatioInclusion() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION,
                novelRegionParameters.getIncreaseRatioValue()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR,
                novelRegionParameters.isIncludeBestMatchedReadsNr() ? "yes" : "no"));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC)));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Mapping statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                String.valueOf(String.format("%2.2f", mappingCount))));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH,
                String.valueOf(String.format("%2.2f", meanMappingLength))));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow(ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                String.valueOf(String.format("%2.2f", mappingsPerMio))));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow(""));

        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Table Type", TABLE_TYPE.toString(), ""));

        tSSExport.add(statisticsExportData);

        return tSSExport;
    }

    public void setResults(List<NovelTranscript> results) {
        this.results = results;
    }

    public StatisticsOnMappingData getStats() {
        return stats;
    }

    /**
     * @return The statistics map associated with this analysis
     */
    public Map<String, Object> getStatsAndParametersMap() {
        return this.statsMap;
    }

    /**
     * Sets the statistics map associated with this analysis.
     *
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsAndParametersMap(Map<String, Object> statsMap) {
        this.statsMap = statsMap;
    }
}
