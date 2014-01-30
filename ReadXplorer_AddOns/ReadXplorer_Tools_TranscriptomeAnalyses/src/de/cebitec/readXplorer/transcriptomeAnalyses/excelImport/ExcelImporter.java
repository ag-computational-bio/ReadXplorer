package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.RPKMvalue;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.NovelRegionResult;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.NovelRegionResultPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.OperonDetectionResult;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ParameterSetFiveEnrichedAnalyses;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ParameterSetWholeTranscriptAnalyses;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.RPKMAnalysisResult;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelOperonDetection;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelRPKM;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelTranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.StatisticsOnMappingData;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.TSSDetectionResults;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.TranscriptomeAnalysesTopComponentTopComponent;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private HashMap<String, String> secondSheetMap;
    private static final String TABLE_TYPE = "Table Type";

    public ExcelImporter(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.model = new DefaultTableModel();
    }

    /**
     * Starts excel to data converter.
     *
     * @param importFile Excel file, contains only specific Datastructures.
     */
    public void startExcelToTableConverter(File importFile, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ExcelToTable exlToTable = null;
        try {
            exlToTable = new ExcelToTable(importFile, progressHandle);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.model = exlToTable.dataToDataTableImport();
        this.secondSheetMap = exlToTable.getSecondSheetData();

        progressHandle.progress(10);
        if (secondSheetMap.get(TABLE_TYPE).equals(TableType.TSS_TABLE.toString())) {
            setUpTSSDataStructuresAndTable(refViewer, transcAnalysesTopComp);
        } else if (secondSheetMap.get(TABLE_TYPE).equals(TableType.NOVEL_REGION_TABLE.toString())) {
            setUpNewRegionStructuresAndTable(refViewer, transcAnalysesTopComp);
        } else if (secondSheetMap.get(TABLE_TYPE).equals(TableType.RPKM_TABLE.toString())) {
            setUpRpkmStructuresAndTable(transcAnalysesTopComp);
        } else if (secondSheetMap.get(TABLE_TYPE).equals(TableType.OPERON_TABLE.toString())) {
            setUpOperonStructuresAndTable(refViewer, transcAnalysesTopComp);
        }
    }

    /**
     *
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent
     */
    public void setUpTSSDataStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        tssResultsPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int refID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveTrackConnectorFetcherForGUI.showPathSelectionErrorMsg();
        }

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(connector.getRefGenome().getId());
        List<PersistantFeature> genomeFeatures = new ArrayList<>();
        int genomeId = refConnector.getRefGenome().getId();
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
        }

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

        tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS);
        replaced = tmp.replaceAll(",", ".");
        int cdsPercentageValue = Integer.valueOf(replaced);

        ParameterSetFiveEnrichedAnalyses params = new ParameterSetFiveEnrichedAnalyses(
                fraction, ratio, isInternalExclusion,
                rangeForKeepingTSS, rangeForLeaderlessDetection, keepingInternalRange, cdsPercentageValue);
        StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);


        TSSDetectionResults tssResult = new TSSDetectionResults(stats, null, trackMap, genomeId);
        tssResult.setParameters(params);
        List<TranscriptionStart> tss = new ArrayList<>();
        TranscriptionStart ts = null;
        progressHandle.progress("Initialize table ... ", 20);

        for (int row = 1; row < model.getRowCount(); row++) {

            String internalTSS = (String) model.getValueAt(row, 14);
            boolean isInternalTSS;

            if (internalTSS.equals("false")) {
                isInternalTSS = false;
            } else {
                isInternalTSS = true;
            }

            PersistantFeature detectedGene = null;
            PersistantFeature downstreamNextGene = null;
            String locus = (String) model.getValueAt(row, 7);

            if (featureMap.containsKey(locus)) {
                if (isInternalTSS) {
                    downstreamNextGene = featureMap.get(locus);
                } else {
                    detectedGene = featureMap.get(locus);
                }
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


            String comment = (String) model.getValueAt(row, 3);

            int readStarts;
            String readStartsString = (String) model.getValueAt(row, 4);
            if (readStartsString.equals("-") || readStartsString.isEmpty()) {
                readStarts = 0;
            } else {
                readStarts = Integer.valueOf(readStartsString);
            }

            double relCount;
            String relCountsString = (String) model.getValueAt(row, 5);
            if (relCountsString.equals("-")|| readStartsString.isEmpty()) {
                relCount = 0.0;
            } else {
                replaced = relCountsString.replaceAll(",", ".");
                relCount = Double.valueOf(replaced);
            }


            String offsetString = (String) model.getValueAt(row, 8);
            int offset;
            if (offsetString.equals("-")|| readStartsString.isEmpty()) {
                offset = 0;
            } else {
                offset = Integer.valueOf(offsetString);
            }

            int dist2Start;
            String dist2StartString = (String) model.getValueAt(row, 9);
            if (dist2StartString.equals("-")|| readStartsString.isEmpty()) {
                dist2Start = 0;
            } else {
                dist2Start = Integer.valueOf(dist2StartString);
            }

            int dist2Stop;
            String dist2StopString = (String) model.getValueAt(row, 10);
            if (dist2StopString.equals("-")|| readStartsString.isEmpty()) {
                dist2Stop = 0;
            } else {
                dist2Stop = Integer.valueOf(dist2StopString);
            }

            String leaderlessBool = (String) model.getValueAt(row, 12);
            boolean isLeaderless;
            if (leaderlessBool.equals("false")) {
                isLeaderless = false;
            } else {
                isLeaderless = true;
            }

            String cdsShiftBool = (String) model.getValueAt(row, 13);
            boolean isCdsShift;
            if (cdsShiftBool.equals("false")) {
                isCdsShift = false;
            } else {
                isCdsShift = true;
            }



            String antisenseBool = (String) model.getValueAt(row, 15);
            boolean isPutAntisense;
            if (antisenseBool.equals("false")) {
                isPutAntisense = false;
            } else {
                isPutAntisense = true;
            }
            String selectedForUpstreamAnalysis = (String) model.getValueAt(row, 16);
            boolean isSelected;
            if (selectedForUpstreamAnalysis.equals("false")) {
                isSelected = false;
            } else {
                isSelected = true;
            }

            String isConsideredString = (String) model.getValueAt(row, 17);
            boolean isConsidered;
            if (isConsideredString.equals("false")) {
                isConsidered = false;
            } else {
                isConsidered = true;
            }

            ts = new TranscriptionStart(tssPosition,
                    isFwd, readStarts, relCount,
                    detectedGene, offset,
                    dist2Start, dist2Stop,
                    downstreamNextGene, offset, isLeaderless, isCdsShift,
                    (String) model.getValueAt(row, 21), (String) model.getValueAt(row, 22),
                    isInternalTSS, isPutAntisense, chromId, refID);
            ts.setComment(comment);
            ts.setSelected(isSelected);
            ts.setIsconsideredTSS(isConsidered);
            tss.add(ts);
        }
        progressHandle.progress(27);
        tssResult.setResults(tss);
        tssResultsPanel.addResult(tssResult);
        transcAnalysesTopComp.openAnalysisTab("TSS-Detection for: " + refConnector.getAssociatedTrackNames().get(track.getId()) + " (Hits: " + tss.size() + ")", tssResultsPanel);
    }

    public void setUpRpkmStructuresAndTable(TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelRPKM resultPanel = new ResultPanelRPKM();

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(trackID);
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);
        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveTrackConnectorFetcherForGUI.showPathSelectionErrorMsg();
        }

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(connector.getRefGenome().getId());
        List<PersistantFeature> genomeFeatures = new ArrayList<>();
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
        }
        HashMap<String, PersistantFeature> featureMap = new HashMap();
        for (PersistantFeature persistantFeature : genomeFeatures) {
            featureMap.put(persistantFeature.getLocus(), persistantFeature);
        }

        progressHandle.progress("Load Statistics from file ... ", 15);


        List<RPKMvalue> rpkms = new ArrayList<>();
        RPKMvalue rpkm = null;
        progressHandle.progress("Initialize table ... ", 20);
        for (int row = 1; row < model.getRowCount(); row++) {

            String featureLocus = (String) model.getValueAt(row, 0);
            String rpkmString = (String) model.getValueAt(row, 8);
            String replaced = rpkmString.replaceAll(",", ".");
            double rpkmValue = Double.valueOf(replaced);
            String logRpkmString = (String) model.getValueAt(row, 9);
            replaced = logRpkmString.replaceAll(",", ".");
            double logRpkm = Double.valueOf(replaced);

            trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
            trackID = Integer.valueOf(trackId);
            chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
            chromId = Integer.valueOf(chromID);

            rpkm = new RPKMvalue(featureMap.get(featureLocus), rpkmValue, logRpkm, 0, 0, 0, 0, trackID, chromId);
            rpkms.add(rpkm);
        }
        progressHandle.progress(27);

        RPKMAnalysisResult rpkmResult = new RPKMAnalysisResult(trackMap, rpkms, refConnector.getRefGenome().getId());
        resultPanel.addResult(rpkmResult);
        transcAnalysesTopComp.openAnalysisTab("Operon detection for track: " + refConnector.getAssociatedTrackNames().get(track.getId()), resultPanel);
    }

    public void setUpOperonStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelOperonDetection resultPanel = new ResultPanelOperonDetection();
//        resultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
        resultPanel.setReferenceViewer(refViewer);


        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(trackID);
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);
        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
        }

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(connector.getRefGenome().getId());
        List<PersistantFeature> genomeFeatures = new ArrayList<>();
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
        }
        HashMap<String, PersistantFeature> featureMap = new HashMap();
        for (PersistantFeature persistantFeature : genomeFeatures) {
            featureMap.put(persistantFeature.getLocus(), persistantFeature);
        }


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

        tmp = (String) secondSheetMap.get("Fraction for Background threshold calculation:");
        replaced = tmp.replaceAll(",", ".");
        double fraction = Double.valueOf(replaced);



        ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, fraction, 0, false, 0);
        StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);



        List<Operon> operons = new ArrayList<>();
        List<OperonAdjacency> adjacencies;
        Operon operon = null;
        progressHandle.progress("Initialize table ... ", 20);
        for (int row = 1; row < model.getRowCount(); row++) {
            adjacencies = new ArrayList<>();
            operon = new Operon(trackID);
            // getAll Adjacencies, put them in operon.
            int transcriptStart = Integer.parseInt((String) model.getValueAt(row, 0));
            operon.setStartPositionOfTranscript(transcriptStart);

            String firstFeatures = (String) model.getValueAt(row, 1);
            String[] splitedFeatures = firstFeatures.split("\n");
            String secondFeatures = (String) model.getValueAt(row, 1);
            String[] splitedSecFeatures = secondFeatures.split("\n");
            String spanningReadCount = (String) model.getValueAt(row, 10);
            String[] splitedSpanningReadCounts = spanningReadCount.split("\n");

            for (int i = 0; i < splitedFeatures.length; i++) {
                String firstFeature = splitedFeatures[i];
                String secondFeature = splitedSecFeatures[i];
                int spanningReads = Integer.valueOf(splitedSpanningReadCounts[i]);
                OperonAdjacency adj = new OperonAdjacency(featureMap.get(firstFeature), featureMap.get(secondFeature));
                adj.setSpanningReads(spanningReads);
                adjacencies.add(adj);
            }

            boolean isFwd;
            String direction = (String) model.getValueAt(row, 5);
            String withoutNewLine = direction.substring(0, direction.length() - 1);
            if (withoutNewLine.equals("Fwd")) {
                isFwd = true;
            } else {
                isFwd = false;
            }

            boolean isUpstreamAnalysisMarked;
            String upstreamAnalysis = (String) model.getValueAt(row, 8);
            if (upstreamAnalysis.equals("false")) {
                isUpstreamAnalysisMarked = false;
            } else {
                isUpstreamAnalysisMarked = true;
            }

            boolean isConsidered;
            String consideration = (String) model.getValueAt(row, 9);
            if (consideration.equals("false")) {
                isConsidered = false;
            } else {
                isConsidered = true;
            }

            operon.addAllOperonAdjacencies(adjacencies);
            operon.setIsConsidered(isConsidered);
            operon.setFwd(isFwd);
            operon.setForUpstreamAnalysisMarked(isUpstreamAnalysisMarked);
            operons.add(operon);
        }
        progressHandle.progress(27);
        OperonDetectionResult operonResults = new OperonDetectionResult(stats, trackMap, operons, refConnector.getRefGenome().getId());
        operonResults.setParameters(params);
        resultPanel.addResult(operonResults);
        transcAnalysesTopComp.openAnalysisTab("Operon detection for track: " + refConnector.getAssociatedTrackNames().get(track.getId()), resultPanel);
    }

    public void setUpNewRegionStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        NovelRegionResultPanel novelRegionsResultsPanel = new NovelRegionResultPanel();
        novelRegionsResultsPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int refID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);
        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveTrackConnectorFetcherForGUI.showPathSelectionErrorMsg();
        }

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(connector.getRefGenome().getId());
        List<PersistantFeature> genomeFeatures = new ArrayList<>();
        Map<Integer, PersistantChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistantChromosome chrom : chroms.values()) {
            genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
        }
        HashMap<String, PersistantFeature> featureMap = new HashMap();
        for (PersistantFeature persistantFeature : genomeFeatures) {
            featureMap.put(persistantFeature.getLocus(), persistantFeature);
        }


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

        tmp = (String) secondSheetMap.get(TranscriptomeAnalysisWizardIterator.PROP_FRACTION_NOVELREGION_DETECTION);
        replaced = tmp.replaceAll(",", ".");
        double fraction = Double.valueOf(replaced);

        tmp = (String) secondSheetMap.get(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH);
        int minBoundary = Integer.valueOf(tmp);

        tmp = (String) secondSheetMap.get(TranscriptomeAnalysisWizardIterator.PROP_INCLUDE_RATIOVALUE_IN_NOVEL_REGION_DETECTION);
        boolean includeRatioValue = false;
        if (tmp.equals("true")) {
            includeRatioValue = true;
        }

        tmp = (String) secondSheetMap.get(TranscriptomeAnalysisWizardIterator.PROP_RAIO_NOVELREGION_DETECTION);
        int ratio = Integer.valueOf(tmp);


        ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, fraction, minBoundary, includeRatioValue, ratio);
        StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);


        NovelRegionResult novelRegionResults = new NovelRegionResult(stats, trackMap, null, false);
        novelRegionResults.setParameters(params);
        List<NovelTranscript> novelRegions = new ArrayList<>();
        NovelTranscript novelRegion = null;
        progressHandle.progress("Initialize table ... ", 20);
        for (int row = 1; row < model.getRowCount(); row++) {

            String position = (String) model.getValueAt(row, 0);
            int novelRegStartPos = Integer.valueOf(position);

            boolean isFwd;
            String strand = (String) model.getValueAt(row, 1);
            if (strand.equals("Fwd")) {
                isFwd = true;
            } else {
                isFwd = false;
            }

            boolean isFP;
            String falsePositive = (String) model.getValueAt(row, 4);
            if (falsePositive.equals("false")) {
                isFP = false;
            } else {
                isFP = true;
            }

            boolean isSelectedForBlast;
            String selected = (String) model.getValueAt(row, 5);
            if (falsePositive.equals("false")) {
                isSelectedForBlast = false;
            } else {
                isSelectedForBlast = true;
            }

            boolean isFinished;
            String finishedSring = (String) model.getValueAt(row, 6);
            if (finishedSring.equals("false")) {
                isFinished = false;
            } else {
                isFinished = true;
            }

            int dropOff;
            String dropOffString = (String) model.getValueAt(row, 8);
            dropOff = Integer.valueOf(dropOffString);

            int length;
            String lengthString = (String) model.getValueAt(row, 9);
            length = Integer.valueOf(lengthString);

            novelRegion = new NovelTranscript(isFwd, novelRegStartPos, dropOff, (String) model.getValueAt(row, 7),
                    length, (String) model.getValueAt(row, 10), isFP, isSelectedForBlast, refID, chromId);
            novelRegion.setIsConsidered(isFinished);
            novelRegions.add(novelRegion);
        }
        progressHandle.progress(27);
        novelRegionResults.setResults(novelRegions);
        novelRegionsResultsPanel.addResult(novelRegionResults);
        transcAnalysesTopComp.openAnalysisTab("Novel Region detection for track: " + refConnector.getAssociatedTrackNames().get(track.getId()), novelRegionsResultsPanel);
    }
}
