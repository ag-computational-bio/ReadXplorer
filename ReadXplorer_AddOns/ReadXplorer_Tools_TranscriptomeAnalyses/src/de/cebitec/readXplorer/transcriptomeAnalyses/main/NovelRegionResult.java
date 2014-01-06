package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelRegion;
import de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.TranscriptomeAnalysisWizardIterator;
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

    private List<NovelRegion> results;
    private ParameterSetFiveEnrichedAnalyses parameters;
    private Statistics stats;
    private Map<String, Object> statsMap;
    private static final TableType TABLE_TYPE = TableType.NOVEL_REGION_TABLE;

    public NovelRegionResult(Statistics stats, Map<Integer, PersistantTrack> trackMap, List<NovelRegion> novelRegions, boolean combineTracks) {
        super(trackMap, 1, combineTracks, 2, 0);
        this.results = novelRegions;
        this.stats = stats;
    }

    public List<NovelRegion> getResults() {
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
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("FALSE POSITIVE");
        dataColumnDescriptions.add("Selected for BLAST");
        dataColumnDescriptions.add("Site");
        dataColumnDescriptions.add("Coverage dropoff");
        dataColumnDescriptions.add("Length in bp");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Chrom ID");
        dataColumnDescriptions.add("Track ID");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Novel Region Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();

        for (int i = 0; i < results.size(); ++i) {
            NovelRegion novelRegion = results.get(i);
            List<Object> novelRegionRow = new ArrayList<>();

            novelRegionRow.add(novelRegion.getPos());
            novelRegionRow.add(novelRegion.isFwdStrand() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING);

            novelRegionRow.add(this.getTrackMap().get(novelRegion.getTrackId()));
            novelRegionRow.add(this.getChromosomeMap().get(novelRegion.getChromId()));

            novelRegionRow.add(novelRegion.isFalsePositive());
            novelRegionRow.add(novelRegion.isSelected());
            novelRegionRow.add(novelRegion.getSite());
            novelRegionRow.add(novelRegion.getDropOffPos());
            novelRegionRow.add(novelRegion.getLength());
            novelRegionRow.add(novelRegion.getSequence());
            novelRegionRow.add(novelRegion.getChromId());
            novelRegionRow.add(novelRegion.getTrackId());

            tSSResults.add(novelRegionRow);
        }

        tSSExport.add(tSSResults);


        //create statistics sheet
        ParameterSetWholeTranscriptAnalyses novelRegionParameters = (ParameterSetWholeTranscriptAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                "Novel Region detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Novel Region detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH,
                novelRegionParameters.getMinLengthBoundary()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(TranscriptomeAnalysisWizardIterator.PROP_FRACTION_NOVELREGION_DETECTION,
                novelRegionParameters.getFraction()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Novel Region statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_REV_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_FWD_FEATURES)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_CISANTISENSE)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC,
                getStatsAndParametersMap().get(NovelRegionResultPanel.NOVELREGION_DETECTION_NO_OF_TRANSGENIC)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_COUNT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_MILLION)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD)));

        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(""));
        
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Table Type", TABLE_TYPE.toString()));
        
        tSSExport.add(statisticsExportData);

        return tSSExport;
    }

    public void setResults(List<NovelRegion> results) {
        this.results = results;
    }

    public Statistics getStats() {
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
