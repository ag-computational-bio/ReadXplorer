/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ParameterSetFiveEnrichedAnalyses;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelTranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.Statistics;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.TSSDetectionResults;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.TranscriptomeAnalysesTopComponentTopComponent;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class ExcelImporter {

    private DefaultTableModel model;
    private ProgressHandle progressHandle;
    HashMap<String, String> secondSheetMap;

    public ExcelImporter(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.model = new DefaultTableModel();
    }

    /**
     * Starts excel to data converter.
     *
     * @param exportFile Excel file, contains only specific Datastructures.
     */
    public void startExcelToTableConverter(File exportFile) {
        ExcelToTable exlToTable = null;
        try {
            exlToTable = new ExcelToTable(exportFile, progressHandle);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.model = exlToTable.dataToDataTableImport();
        this.secondSheetMap = exlToTable.getSecondSheetData();
        progressHandle.progress(10);
    }

    /**
     * 
     * @param refViewer
     * @param transcAnalysesTopComp 
     */
    public void setUpTSSDataStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        tssResultsPanel.setReferenceViewer(refViewer);

        String referenceID = (String) model.getValueAt(1, 20);
        int refID = Integer.valueOf(referenceID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
        }

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(connector.getRefGenome().getId());
        int refSeqLength = refConnector.getRefSequence().length();
        List<PersistantFeature> genomeFeatures = refConnector.getFeaturesForClosedInterval(0, refSeqLength);
        HashMap<String, PersistantFeature> featureMap = new HashMap();
        for (PersistantFeature persistantFeature : genomeFeatures) {
            featureMap.put(persistantFeature.getLocus(), persistantFeature);
        }

        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);
        progressHandle.progress("Load Statistics from file ... ", 15);
        String tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
        String replaced = tmp.replaceAll(",", ".");
        double mappingCount = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MEAN_LENGTH);
        replaced = tmp.replaceAll(",", ".");
        double mappingMeanLength = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
        replaced = tmp.replaceAll(",", ".");
        double mappingsPerMillion = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD);
        replaced = tmp.replaceAll(",", ".");
        double backgroundThreshold = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
        replaced = tmp.replaceAll(",", ".");
        double fraction = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RATIO);
        int ratio = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_CHOOSEN_DOWNSTREAM_REGION);
        int downstream = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_CHOOSEN_UPSTREAM_REGION);
        int upstream = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS);
        boolean isInternalExclusion;
        if (tmp.equals("no")) {
            isInternalExclusion = false;
        } else {
            isInternalExclusion = true;
        }

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS);
        int keepingInternalRange = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION);
        int rangeForKeepingTSS = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION);
        int rangeForLeaderlessDetection = Integer.valueOf(tmp);

        ParameterSetFiveEnrichedAnalyses params = new ParameterSetFiveEnrichedAnalyses(
                fraction, ratio, upstream, downstream, isInternalExclusion,
                rangeForKeepingTSS, rangeForLeaderlessDetection, keepingInternalRange);
        Statistics stats = new Statistics(mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);


        TSSDetectionResults tssResult = new TSSDetectionResults(stats, null, trackMap);
        tssResult.setParameters(params);
        List<TranscriptionStart> tss = new ArrayList<>();
        TranscriptionStart ts = null;
        progressHandle.progress("Initialize table ... ", 20);
        for (int row = 1; row < model.getRowCount(); row++) {

            String internalTSS = (String) model.getValueAt(row, 11);
            boolean isInternalTSS;

            if (internalTSS.equals("false")) {
                isInternalTSS = false;
            } else {
                isInternalTSS = true;
            }

            PersistantFeature detectedGene = null;
            PersistantFeature downstreamNextGene = null;
            String locus = (String) model.getValueAt(row, 5);

            if (isInternalTSS) {
                downstreamNextGene = featureMap.get(locus);
            } else {
                detectedGene = featureMap.get(locus);
            }
            boolean isFwd;
            String strand = (String) model.getValueAt(row, 1);
            if (strand.equals("Fwd")) {
                isFwd = true;
            } else {
                isFwd = false;
            }

            String position = (String) model.getValueAt(row, 0);
            int tssPosition = Integer.valueOf(position);

            String readStartsString = (String) model.getValueAt(row, 2);
            int readStarts = Integer.valueOf(readStartsString);

            String relCountsString = (String) model.getValueAt(row, 3);
            replaced = relCountsString.replaceAll(",", ".");
            double relCount = Double.valueOf(replaced);

            String offsetString = (String) model.getValueAt(row, 6);
            int offset = Integer.valueOf(offsetString);

            String dist2StartString = (String) model.getValueAt(row, 7);
            int dist2Start = Integer.valueOf(dist2StartString);

            String dist2StopString = (String) model.getValueAt(row, 8);
            int dist2Stop = Integer.valueOf(dist2StopString);

            String leaderlessBool = (String) model.getValueAt(row, 10);
            boolean isLeaderless;
            if (leaderlessBool.equals("false")) {
                isLeaderless = false;
            } else {
                isLeaderless = true;
            }

            String antisenseBool = (String) model.getValueAt(row, 12);
            boolean isPutAntisense;
            if (antisenseBool.equals("false")) {
                isPutAntisense = false;
            } else {
                isPutAntisense = true;
            }

            ts = new TranscriptionStart(tssPosition,
                    isFwd, readStarts, relCount,
                    detectedGene, offset,
                    dist2Start, dist2Stop,
                    downstreamNextGene, offset,
                    (String) model.getValueAt(row, 9), isLeaderless, false,
                    (String) model.getValueAt(row, 18), (String) model.getValueAt(row, 19),
                    isInternalTSS, isPutAntisense, refID);
            tss.add(ts);
        }
        progressHandle.progress(27);
        tssResult.setResults(tss);
        tssResultsPanel.addResult(tssResult);
        transcAnalysesTopComp.openAnalysisTab(refConnector.getAssociatedTrackNames().get(track.getId()), tssResultsPanel);
    }
}
