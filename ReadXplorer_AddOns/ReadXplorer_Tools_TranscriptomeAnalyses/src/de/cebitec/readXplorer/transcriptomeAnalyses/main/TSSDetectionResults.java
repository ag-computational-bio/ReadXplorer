package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class TSSDetectionResults extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses> {

    private List<TranscriptionStart> results;
    private Statistics stats;
    private Map<String, Object> statsMap;

    public TSSDetectionResults(Statistics stats, List<TranscriptionStart> results, Map<Integer, PersistantTrack> trackMap, int refId) {
        super(trackMap, refId, false);
        this.results = results;
        this.stats = stats;
        this.statsMap = new HashMap<>();
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Read starts");
        dataColumnDescriptions.add("Rel. count");
        dataColumnDescriptions.add("Feature name");
        dataColumnDescriptions.add("Feature locus");
        dataColumnDescriptions.add("Offset");
        dataColumnDescriptions.add("dist. to start");
        dataColumnDescriptions.add("dist. to stop");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Leaderless");
        dataColumnDescriptions.add("Putative CDS-Shift");
        dataColumnDescriptions.add("Internal TSS");
        dataColumnDescriptions.add("Putative antisense");
        dataColumnDescriptions.add("Gene start");
        dataColumnDescriptions.add("Gene stop");
        dataColumnDescriptions.add("Gene length in bp");
        dataColumnDescriptions.add("Frame");
        dataColumnDescriptions.add("Gene product");
        dataColumnDescriptions.add("Start codon");
        dataColumnDescriptions.add("Stop codon");
        dataColumnDescriptions.add("Track ID");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Transcription Start Site Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();

        for (int i = 0; i < results.size(); ++i) {
            TranscriptionStart tss = results.get(i);
            List<Object> tssRow = new ArrayList<>();

            tssRow.add(tss.getStartPosition());
            tssRow.add(this.getChromosomeMap().get(tss.getChromId()));
            tssRow.add(tss.getReadStarts());
            tssRow.add(tss.getRelCount());

            PersistantFeature detectedGene = tss.getDetectedGene();
            PersistantFeature nextGene = tss.getNextGene();
            
            if (detectedGene != null) {
                tssRow.add(tss.getDetectedGene().toString());
                tssRow.add(tss.getDetectedGene().getLocus());
                tssRow.add(tss.getOffset());
            } else {
                tssRow.add(tss.getNextGene().toString());
                tssRow.add(tss.getNextGene().getLocus());
                tssRow.add(tss.getNextOffset());
            }
            tssRow.add(tss.getDist2start());
            tssRow.add(tss.getDist2stop());
            tssRow.add(tss.getSequence());
            tssRow.add(tss.isLeaderless());
            tssRow.add(tss.isCdsShift());
            tssRow.add(tss.isInternalTSS());
            tssRow.add(tss.isPutativeAntisense());

            // additionally informations about detected gene
            if (tss.getDetectedGene() != null) {
                tssRow.add(detectedGene.isFwdStrand() ? detectedGene.getStart() : detectedGene.getStop());
                tssRow.add(detectedGene.isFwdStrand() ? detectedGene.getStop() : detectedGene.getStart());
                tssRow.add(detectedGene.getStop() - detectedGene.getStart());
                tssRow.add(detectedGene.getFrame());
                tssRow.add(detectedGene.getProduct());
            } else {
                tssRow.add(nextGene.isFwdStrand() ? nextGene.getStart() : nextGene.getStop());
                tssRow.add(nextGene.isFwdStrand() ? nextGene.getStop() : nextGene.getStart());
                tssRow.add(nextGene.getStop() - nextGene.getStart());
                tssRow.add(nextGene.getFrame());
                tssRow.add(nextGene.getProduct());
            }

            tssRow.add(tss.getDetectedFeatStart());
            tssRow.add(tss.getDetectedFeatStop());
            tssRow.add(tss.getTrackId());
            tSSResults.add(tssRow);
        }

        tSSExport.add(tSSResults);


        //create statistics sheet
        ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                "Transcription start site detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Transcription start site detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_RATIO, 
                tssParameters.getRatio()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FRACTION, 
                tssParameters.getFraction()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_CHOOSEN_UPSTREAM_REGION, 
                tssParameters.getUpstreamRegion()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_CHOOSEN_DOWNSTREAM_REGION, 
                tssParameters.getDownstreamRegion()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS, 
                tssParameters.isExclusionOfInternalTSS() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION, 
                tssParameters.getLeaderlessLimit()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION, 
                tssParameters.getExclusionOfTSSDistance()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS, 
                tssParameters.getKeepingInternalTssDistance()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Transcription start site statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_TOTAL,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_CORRECT,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_CORRECT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FWD,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_FWD)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_REV,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_REV)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LEADERLESS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_LEADERLESS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_INTERNAL,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_INTERNAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_NO_PUTATIVE_CDS_SHIFTS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_NO_PUTATIVE_CDS_SHIFTS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_COUNT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.MAPPINGS_MILLION)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD)));
        
        tSSExport.add(statisticsExportData);

        return tSSExport;
    }

    public List<TranscriptionStart> getResults() {
        return results;
    }

    public void setResults(List<TranscriptionStart> results) {
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
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsAndParametersMap(Map<String, Object> statsMap) {
        this.statsMap = statsMap;
    }
}
