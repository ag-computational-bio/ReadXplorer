/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.TranscriptionStart;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class TSSDetectionResults extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses> {

    private List<TranscriptionStart> results;
    private Statistics stats;

    public TSSDetectionResults(Statistics stats, List<TranscriptionStart> results, Map<Integer, PersistantTrack> trackMap) {
        super(trackMap,false);
        this.results = results;
        this.stats = stats;
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
        dataColumnDescriptions.add("Count");
        dataColumnDescriptions.add("Rel. count");
        dataColumnDescriptions.add("-10");
        dataColumnDescriptions.add("-9");
        dataColumnDescriptions.add("-8");
        dataColumnDescriptions.add("-7");
        dataColumnDescriptions.add("-6");
        dataColumnDescriptions.add("-5");
        dataColumnDescriptions.add("-4");
        dataColumnDescriptions.add("-3");
        dataColumnDescriptions.add("-2");
        dataColumnDescriptions.add("-1");
        dataColumnDescriptions.add("Feature");
        dataColumnDescriptions.add("Offset");
        dataColumnDescriptions.add("dist. to start");
        dataColumnDescriptions.add("dist. to stop");
        dataColumnDescriptions.add("Next gene");
        dataColumnDescriptions.add("Next offset");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Leaderless");
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

            tssRow.add(tss.getPos());
            tssRow.add(tss.getReadStarts());
            tssRow.add(tss.getRelCount());
            for (int c : tss.getBeforeCounts()) {
                tssRow.add((Integer) c);
            }

            tssRow.add(tss.getDetectedGene());
            tssRow.add(tss.getOffset());
            tssRow.add(tss.getDist2start());
            tssRow.add(tss.getDist2stop());
            tssRow.add(tss.getNextGene());
            tssRow.add(tss.getNextOffset());
            tssRow.add(tss.getSequence());
            tssRow.add(tss.isFwdStrand() ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING);
            tssRow.add(tss.isLeaderless());

            // additionally informations about detected gene
            PersistantFeature detectedGene = tss.getDetectedGene();
            PersistantFeature nextGene = tss.getNextGene();
                    
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
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum number of read starts:", 
//                tssParameters.getMinNoReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum percent of coverage increase:", 
//                tssParameters.getMinPercentIncrease()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Maximum low coverage read start count:", 
//                tssParameters.getMaxLowCovReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum low coverage read starts:", 
//                tssParameters.getMinLowCovReadStarts()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Detect novel transcripts?", 
//                tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no"));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum transcript extension coverage:", 
//                tssParameters.getMinTranscriptExtensionCov()));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Transcription start site statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_TOTAL,
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_CORRECT,
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_CORRECT)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FWD,
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_FWD)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_REV,
                getStatsMap().get(ResultPanelTranscriptionStart.TSS_REV)));

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
}
