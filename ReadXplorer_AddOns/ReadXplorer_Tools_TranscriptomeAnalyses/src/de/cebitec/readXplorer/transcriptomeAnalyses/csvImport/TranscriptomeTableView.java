/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.csvImport;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.parser.tables.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.RPKMvalue;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
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
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.FivePrimeEnrichedTracksVisualPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.WizardPropertyStrings;
import de.cebitec.readXplorer.ui.importer.TranscriptomeTableViewI;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author jritter, rhilker
 */
@ServiceProvider(service = TranscriptomeTableViewI.class)
public class TranscriptomeTableView implements TranscriptomeTableViewI {

    private static final String TABLE_TYPE = "Table Type";
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    
    private PersistentTrack track;
    private ReferenceConnector refConnector;
    private TrackConnector trackConnector;
    private Map<Integer, PersistentTrack> trackMap;
    private Map<String, PersistentFeature> featureMap;

    public TranscriptomeTableView() {
    }
    
    private void initConnectors(int chromId, int trackID) {
        track = ProjectConnector.getInstance().getTrack(trackID);
        trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);

        trackConnector = null;
        SaveFileFetcherForGUI saveFileFetcherForGUI = new SaveFileFetcherForGUI();
        try {
            trackConnector = saveFileFetcherForGUI.getTrackConnector(track);
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
        }

        refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
    }

    private void initFeatureData() {
        List<PersistentFeature> genomeFeatures = new ArrayList<>();
        Map<Integer, PersistentChromosome> chroms = refConnector.getChromosomesForGenome();
        for (PersistentChromosome chrom : chroms.values()) {
            genomeFeatures.addAll(refConnector.getFeaturesForClosedInterval(
                    0, chrom.getLength(), chrom.getId()));
        }
        featureMap = new HashMap<>();
        for (PersistentFeature persistantFeature : genomeFeatures) {
            featureMap.put(persistantFeature.getLocus(), persistantFeature);
        }
    }

    @Override
    public void processCsvInput(List<List<?>> tableData, List<List<?>> tableData2, TableType type, PersistentReference ref) {
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
            this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponentTopComponent();
        }
        transcAnalysesTopComp.open();

        if (type == TableType.TSS_DETECTION_JR) {
            // TSS TAble
            setUpTSSTable(tableData, tableData2, ref, transcAnalysesTopComp);
        } else if (type == TableType.NOVEL_TRANSCRIPT_DETECTION_JR) {
            // NR Table
            setUpNovelTranscriptsTable(tableData, tableData2, ref, transcAnalysesTopComp);
        } else if (type == TableType.OPERON_DETECTION_JR) {
            // Operon Table
            setUpOperonTable(tableData, tableData2, ref, transcAnalysesTopComp);
        } else if (type == TableType.RPKM_ANALYSIS_JR) {
            // RPKM Table
            setUpRPKMTable(tableData, tableData2, transcAnalysesTopComp);
        }
    }

    public void setUpOperonTable(List<List<?>> fstSheet, List<List<?>> sndSheet, PersistentReference reference, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelOperonDetection resultPanel = new ResultPanelOperonDetection();
        resultPanel.setPersistentReference(reference);
//        resultPanel.setReferenceViewer(reference);
        checkAndOpenRefViewer(reference, resultPanel);

        int trackID = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 1);
        int chromId = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 3);

        this.initConnectors(chromId, trackID);
        if (refConnector != null) {
            try {
                this.initFeatureData();

                String tmp;
                String replaced;
                double mappingCount = 0;
                double mappingMeanLength = 0;
                double mappingsPerMillion = 0;
                double backgroundThreshold = 0;
                double fraction = 0;
                boolean includeBestMatchedReads_OP = false;
                boolean isThresholdSettedManually = false;
                if (sndSheet != null) {
                    for (List<?> columns : sndSheet) {
                        if (columns.get(0) != null) {
                            if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_COUNT)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingCount = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingMeanLength = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_MILLION)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingsPerMillion = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                backgroundThreshold = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_FRACTION)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                fraction = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP)) {
                                tmp = (String) columns.get(1);
                                includeBestMatchedReads_OP = !tmp.equals("no");
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD)) {
                                tmp = (String) columns.get(1);
                                isThresholdSettedManually = !tmp.equals("no");
                            }
                        }
                    }
                }

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, 0, false, 0, includeBestMatchedReads_OP, false, false);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                List<Operon> operons = new ArrayList<>();
                List<OperonAdjacency> adjacencies;
                Operon operon;

                for (int row = 1; row < fstSheet.size(); row++) {
                    adjacencies = new ArrayList<>();
                    operon = new Operon(trackID);
                    // getAll Adjacencies, put them in operon.
                    int transcriptStart = (Integer) fstSheet.get(row).get(0);
                    operon.setStartPositionOfTranscript(transcriptStart);

                    String firstFeatures = (String) fstSheet.get(row).get(1);
                    String[] splittedFeatures = firstFeatures.split("\n");
                    String secondFeatures = (String) fstSheet.get(row).get(2);
                    String[] splitedSecFeatures = secondFeatures.split("\n");
                    String spanningReadCount = (String) fstSheet.get(row).get(8);
                    String[] splitedSpanningReadCounts = spanningReadCount.split("\n");

                    for (int i = 0; i < splittedFeatures.length; i++) {
                        String firstFeature = splittedFeatures[i];
                        String secondFeature = splitedSecFeatures[i];
                        int spanningReads = Integer.valueOf(splitedSpanningReadCounts[i]);
                        OperonAdjacency adj = new OperonAdjacency(featureMap.get(firstFeature), featureMap.get(secondFeature)); //TODO: when import on wrong genome: nullpointerexception
                        adj.setSpanningReads(spanningReads);
                        adjacencies.add(adj);
                    }

                    boolean isFwd;
                    String direction = (String) fstSheet.get(row).get(3);
                    String withoutNewLine = direction.substring(0, direction.length() - 1);
                    isFwd = withoutNewLine.equals("Fwd");

                    boolean isFalsPositive = (Boolean) fstSheet.get(row).get(6);

                    boolean isConsidered = (Boolean) fstSheet.get(row).get(7);

                    operon.addAllOperonAdjacencies(adjacencies);
                    operon.setIsConsidered(isConsidered);
                    operon.setFwdDirection(isFwd);
                    operon.setFalsPositive(isFalsPositive);
                    operons.add(operon);
                }
                OperonDetectionResult operonResults = new OperonDetectionResult(stats, trackMap, operons, refConnector.getRefGenome());
                operonResults.setParameters(params);
                resultPanel.addResult(operonResults);
                transcAnalysesTopComp.openAnalysisTab("Operon detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + operonResults.getResults().size(), resultPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpRPKMTable(List<List<?>> fstSheet, List<List<?>> sndSheet, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelRPKM resultPanel = new ResultPanelRPKM();

        int trackID = (Integer) fstSheet.get(1).get(fstSheet.get(1).size() - 1);
        int chromId = (Integer) fstSheet.get(1).get(fstSheet.get(1).size() - 3);
        this.initConnectors(chromId, trackID);

        if (refConnector != null) {
            try {
                this.initFeatureData();

                String tmp;
                boolean includeBestMatchedReads_RPKM = false;

                if (sndSheet != null) {
                    for (List<?> columns : sndSheet) {
                        if (columns.get(0) != null) {
                            if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_RPKM)) {
                                tmp = (String) columns.get(1);
                                includeBestMatchedReads_RPKM = !tmp.equals("no");
                            }
                        }
                    }
                }
                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, false, true, null, 0, 0, false, 0, false, includeBestMatchedReads_RPKM, false);
                List<RPKMvalue> rpkms = new ArrayList<>();
                RPKMvalue rpkm = null;
                for (int row = 1; row < fstSheet.size(); row++) {

                    String featureLocus = (String) fstSheet.get(row).get(0);
                    int known5Utr = (Integer) fstSheet.get(row).get(6);

                    String rpkmString = (String) fstSheet.get(row).get(7);
                    String replaced = rpkmString.replaceAll(",", ".");
                    double rpkmValue = Double.valueOf(replaced);

                    String logRpkmString = (String) fstSheet.get(row).get(8);
                    replaced = logRpkmString.replaceAll(",", ".");
                    double logRpkm = Double.valueOf(replaced);

                    int readCount = (Integer) fstSheet.get(row).get(9);

                    rpkm = new RPKMvalue(featureMap.get(featureLocus), rpkmValue, logRpkm, readCount, trackID, chromId);
                    rpkm.setLongestKnownUtrLength(known5Utr);
                    rpkms.add(rpkm);
                }

                RPKMAnalysisResult rpkmResult = new RPKMAnalysisResult(trackMap, rpkms, refConnector.getRefGenome());
                resultPanel.addResult(rpkmResult);
                rpkmResult.setParameters(params);
                transcAnalysesTopComp.openAnalysisTab("RPKM values for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits:" + rpkmResult.getResults().size(), resultPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpNovelTranscriptsTable(List<List<?>> fstSheet, List<List<?>> sndSheet, PersistentReference reference, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        NovelRegionResultPanel novelRegionsResultsPanel = new NovelRegionResultPanel();
//        novelRegionsResultsPanel.setReferenceViewer(reference);
        novelRegionsResultsPanel.setPersistentReference(reference);
        checkAndOpenRefViewer(reference, novelRegionsResultsPanel);

        int trackID = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 1);
        int chromId = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 3);
        this.initConnectors(chromId, trackID);

        if (refConnector != null) {
            try {
                this.initFeatureData();

                String tmp;
                String replaced;
                double mappingCount = 0;
                boolean includeBestMatchedReads = false;
                boolean includeRatioValue = false;
                boolean isThresholdSettedManually = false;
                double mappingMeanLength = 0;
                double mappingsPerMillion = 0;
                double backgroundThreshold = 0;
                double fraction = 0;
                int minBoundary = 0;
                int ratio = 0;

                if (sndSheet != null && !sndSheet.isEmpty()) {
                    for (List<?> columns : sndSheet) {
                        if (columns.get(0) != null) {
                            if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_COUNT)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingCount = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingMeanLength = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_MILLION)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingsPerMillion = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                backgroundThreshold = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_Fraction)) {

                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                fraction = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH)) {
                                tmp = (String) columns.get(1);
                                minBoundary = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION)) {
                                tmp = (String) columns.get(1);
                                if (tmp.equals("yes")) {
                                    includeRatioValue = true;
                                }
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD)) {
                                tmp = (String) columns.get(1);
                                isThresholdSettedManually = !tmp.equals("no");
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION)) {
                                tmp = (String) columns.get(1);
                                ratio = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR)) {
                                tmp = (String) columns.get(1);
                                includeBestMatchedReads = !tmp.equals("no");
                            }
                        }
                    }
                }
                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, minBoundary, includeRatioValue, ratio, false, false, includeBestMatchedReads);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                List<NovelTranscript> novelRegions = new ArrayList<>();
                NovelRegionResult novelRegionResults = new NovelRegionResult(refConnector.getRefGenome(), stats, trackMap, novelRegions, false);
                novelRegionResults.setParameters(params);
                NovelTranscript novelRegion;
                for (int row = 1; row < fstSheet.size(); row++) {

                     int novelRegStartPos = (Integer) fstSheet.get(row).get(0);

                    String strand = (String) fstSheet.get(row).get(1);
                    boolean isFwd = strand.equals("Fwd");
                    boolean isFP = (Boolean) fstSheet.get(row).get(2);
                    boolean isSelectedForBlast = (Boolean) fstSheet.get(row).get(3);
                    boolean isFinished = (Boolean) fstSheet.get(row).get(4);
                    int dropOff = (Integer) fstSheet.get(row).get(6);
                    int length = (Integer) fstSheet.get(row).get(7);

                    novelRegion = new NovelTranscript(isFwd, novelRegStartPos, dropOff, (String) fstSheet.get(row).get(5),
                            length, (String) fstSheet.get(row).get(8), isFP, isSelectedForBlast, trackID, chromId);
                    novelRegion.setIsConsidered(isFinished);
                    novelRegions.add(novelRegion);
                }
                novelRegionResults.setResults(novelRegions);
                novelRegionsResultsPanel.addResult(novelRegionResults);
                transcAnalysesTopComp.openAnalysisTab("Novel Region detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + novelRegions.size(), novelRegionsResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    /**
     * Method for importing all important excel cells to create all TSS
     * instances.
     *
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent
     *
     */
    public void setUpTSSTable(List<List<?>> fstSheet, List<List<?>> sndSheet, PersistentReference ref, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        checkAndOpenRefViewer(ref, tssResultsPanel);
        tssResultsPanel.setPersistentReference(ref);

        int trackID = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 1);
        int chromId = (Integer) fstSheet.get(1).get(fstSheet.get(0).size() - 2);
        this.initConnectors(chromId, trackID);
        if (refConnector != null) {
            try {
                this.initFeatureData();

                String replaced;
                String tmp;
                double mappingCount = 0;
                double mappingMeanLength = 0;
                double mappingsPerMillion = 0;
                double backgroundThreshold = 0;
                double fraction = 0;
                int ratio = 0;
                Integer isKeepingAllIntragenicTss_Limit = 0;
                Integer isKeepingOnlyAssignedIntragenicTssLimitDistance = 0;
                boolean isInternalExclusion = false;
                boolean includeBestMatchedReads = false;
                boolean isThresholdSettedManually = false;
                boolean isKeepingAllIntragenicTSS = false;
                boolean isKeepingOnlyAssignedIntragenicTSS = false;
                int rangeForKeepingTSS = 0;
                int rangeForLeaderlessDetection = 0;
                int cdsPercentageValue = 0;
                int maxDistantaseFor3UtrAntisenseDetection = 0;
                int keepingInternalRange = 0;
                HashSet<FeatureType> featTypes = null;
                HashMap<String, StartCodon> validStartCodons = new HashMap<>();
                if (sndSheet != null) {
                    for (List<?> columns : sndSheet) {

                        if (columns.get(0) != null) {
                            if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_COUNT)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingCount = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingMeanLength = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.MAPPINGS_MILLION)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                mappingsPerMillion = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                backgroundThreshold = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_FRACTION)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                fraction = Double.valueOf(replaced);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_RATIO)) {
                                tmp = (String) columns.get(1);
                                ratio = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS)) {
                                tmp = (String) columns.get(1);
                                isInternalExclusion = !tmp.equals("no");
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD)) {
                                tmp = (String) columns.get(1);
                                isThresholdSettedManually = !tmp.equals("no");
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS)) {
                                tmp = (String) columns.get(1);
                                isKeepingAllIntragenicTSS = !tmp.equals("no");
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS)) {
                                tmp = (String) columns.get(2);
                                try {
                                    isKeepingAllIntragenicTss_Limit = Integer.parseInt(tmp);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                                }
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS)) {
                                tmp = (String) columns.get(1);
                                isKeepingOnlyAssignedIntragenicTSS = !tmp.equals("no");
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS)) {
                                tmp = (String) columns.get(2);
                                try {
                                    isKeepingOnlyAssignedIntragenicTssLimitDistance = Integer.parseInt(tmp);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                                }
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION)) {
                                tmp = (String) columns.get(1);
                                rangeForKeepingTSS = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION)) {
                                tmp = (String) columns.get(1);
                                rangeForLeaderlessDetection = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS)) {
                                tmp = (String) columns.get(1);
                                replaced = tmp.replaceAll(",", ".");
                                cdsPercentageValue = Integer.valueOf(replaced);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS)) {
                                tmp = (String) columns.get(1);
                                includeBestMatchedReads = !tmp.equals("no");
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION)) {
                                tmp = (String) columns.get(1);
                                maxDistantaseFor3UtrAntisenseDetection = Integer.valueOf(tmp);
                            } else if (columns.get(0).equals(WizardPropertyStrings.PROP_VALID_START_CODONS)) {
                                tmp = (String) columns.get(1);

                                if (!tmp.isEmpty()) {
                                    String[] startCodons = tmp.split(";");

                                    for (String string : startCodons) {
                                        switch (string) {
                                            case "ATG":
                                                validStartCodons.put("ATG", StartCodon.ATG);
                                                break;
                                            case "CTG":
                                                validStartCodons.put("CTG", StartCodon.CTG);
                                                break;
                                            case "GTG":
                                                validStartCodons.put("GTG", StartCodon.GTG);
                                                break;
                                            case "TTG":
                                                validStartCodons.put("TTG", StartCodon.TTG);
                                                break;
                                        }
                                    }
                                }
                            } else if (columns.get(0).equals(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT)) {
                                tmp = (String) columns.get(1);
                                List<FeatureType> types = new ArrayList<>();

                                if (!tmp.isEmpty()) {
                                    String[] typeStings = tmp.split(";");

                                    for (String type : typeStings) {
                                        if (type.equals(FeatureType.MISC_RNA.toString())) {
                                            types.add(FeatureType.MISC_RNA);
                                        } else if (type.equals(FeatureType.SOURCE.toString())) {
                                            types.add(FeatureType.SOURCE);
                                        }
                                    }
                                    featTypes = new HashSet<>(types);
                                }
                            }
                        }
                    }
                }
                if (isKeepingAllIntragenicTSS) {
                    keepingInternalRange = isKeepingAllIntragenicTss_Limit;
                } else if (isKeepingOnlyAssignedIntragenicTSS) {
                    keepingInternalRange = isKeepingOnlyAssignedIntragenicTssLimitDistance;
                }
                ParameterSetFiveEnrichedAnalyses params = new ParameterSetFiveEnrichedAnalyses(
                        fraction, ratio, isInternalExclusion,
                        rangeForKeepingTSS, rangeForLeaderlessDetection, keepingInternalRange, isKeepingAllIntragenicTSS, isKeepingOnlyAssignedIntragenicTSS, cdsPercentageValue, includeBestMatchedReads, maxDistantaseFor3UtrAntisenseDetection, validStartCodons, featTypes);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                List<TranscriptionStart> tss = new ArrayList<>();
                TSSDetectionResults tssResult = new TSSDetectionResults(stats, tss, trackMap, refConnector.getRefGenome());
                tssResult.setParameters(params);
                TranscriptionStart ts;

                fstSheet.remove(0);
                for (List<?> list : fstSheet) {
                    int tssPosition = (Integer) list.get(0);
                    boolean isFwd = false;
                    if (list.get(1).equals("Fwd")) {
                        isFwd = true;
                    }
                    String comment = (String) list.get(2);
                    int readStarts = (Integer) list.get(3);
                    String relCountStr = (String) list.get(4);
                    relCountStr = relCountStr.replaceAll(",", ".");
                    double relCount = Double.parseDouble(relCountStr);

                    boolean isInternalTSS = (Boolean) list.get(13);
                    PersistentFeature detectedGene = null;
                    PersistentFeature downstreamNextGene = null;
                    String locus = (String) list.get(6);
                    if (featureMap.containsKey(locus)) {
                        if (isInternalTSS) {
                            downstreamNextGene = featureMap.get(locus);
                        } else {
                            detectedGene = featureMap.get(locus);
                        }
                    }
                    
                    int offset = GeneralUtils.isValidNumberInput((String) list.get(7)) ? Integer.valueOf((String) list.get(7)) : 0;
                    int dist2Start = GeneralUtils.isValidNumberInput((String) list.get(8)) ? Integer.valueOf((String) list.get(8)) : 0;
                    int dist2Stop = GeneralUtils.isValidNumberInput((String) list.get(9)) ? Integer.valueOf((String) list.get(9)) : 0;

                    boolean isLeaderless = (Boolean) list.get(11);
                    boolean isCdsShift = (Boolean) list.get(12);
                    boolean isIntergenic = (Boolean) list.get(14);
                    boolean isPutAntisense = (Boolean) list.get(15);
                    boolean is5PrimeAntisense = (Boolean) list.get(16);
                    boolean is3PrimeAntisense = (Boolean) list.get(17);
                    boolean isIntragenicAntisense = (Boolean) list.get(18);
                    boolean isAssignedToStableRna = (Boolean) list.get(19);
                    boolean isFalsePositive = (Boolean) list.get(20);
                    boolean isSelected = (Boolean) list.get(21);
                    boolean isConsidered = (Boolean) list.get(22);

                    ts = new TranscriptionStart(tssPosition,
                            isFwd, readStarts, relCount,
                            detectedGene, offset,
                            dist2Start, dist2Stop,
                            downstreamNextGene, offset, isLeaderless, isCdsShift,
                            isInternalTSS, isPutAntisense, chromId, trackID);
                    ts.setComment(comment);
                    ts.setAssignedToStableRNA(isAssignedToStableRna);
                    ts.setIs5PrimeUtrAntisense(is5PrimeAntisense);
                    ts.setIs3PrimeUtrAntisense(is3PrimeAntisense);
                    ts.setIntragenicAntisense(isIntragenicAntisense);
                    ts.setFalsePositive(isFalsePositive);
                    ts.setSelected(isSelected);
                    ts.setIntergenicTSS(isIntergenic);
                    ts.setIsconsideredTSS(isConsidered);
                    tss.add(ts);
                }
                tssResult.setResults(tss);
                tssResultsPanel.addResult(tssResult);
                transcAnalysesTopComp.openAnalysisTab("TSS detection results for: " + refConnector.getAssociatedTrackNames().get(trackID) + " Hits: " + tss.size(), tssResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException | HeadlessException e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. Check the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    private void checkAndOpenRefViewer(PersistentReference ref, ResultTablePanel tablePanel) {
        @SuppressWarnings("unchecked")
        Collection<ViewController> viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll(ViewController.class);
        for (ViewController tmpVCon : viewControllers) {
            if (tmpVCon.getCurrentRefGen().equals(ref)) {
                tablePanel.setBoundsInfoManager(tmpVCon.getBoundsManager());
                break;
            }
        }
    }

    @Override
    public void processXlsInput(PersistentReference reference, DefaultTableModel model, Map<String, String> secondSheetMap, Map<String, String> secondSheetMapThirdCol) {

        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
            this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponentTopComponent();
        }

        if (secondSheetMap.containsKey(TABLE_TYPE)) {
            if (secondSheetMap.get(TABLE_TYPE).equals(de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType.TSS_TABLE.toString())) {
                setUpTSSDataStructuresAndTable(reference, transcAnalysesTopComp, model, secondSheetMap, secondSheetMapThirdCol);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType.NOVEL_TRANSCRIPTS_TABLE.toString())) {
                setUpNovelTranscriptsStructuresAndTable(reference, transcAnalysesTopComp, model, secondSheetMap, secondSheetMapThirdCol);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType.RPKM_TABLE.toString())) {
                setUpRpkmStructuresAndTable(transcAnalysesTopComp, model, secondSheetMap, secondSheetMapThirdCol);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType.OPERON_TABLE.toString())) {
                setUpOperonStructuresAndTable(reference, transcAnalysesTopComp, model, secondSheetMap, secondSheetMapThirdCol);
            }
        } else {
            JOptionPane.showMessageDialog(null, this, "Import of table is canceled, because no table tag was found! Please check the Parameters and Statistics sheet.", JOptionPane.CANCEL_OPTION);
        }
    }

    /**
     * Method for importing all important excel cells to create all TSS
     * instances.
     *
     * @param refeference ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent
     * TranscriptomeAnalysesTopComponentTopComponent
     */
    public void setUpTSSDataStructuresAndTable(PersistentReference reference,
            TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp,
            DefaultTableModel model, Map<String, String> secondSheetMap,
            Map<String, String> secondSheetMapThirdCol) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
//        tssResultsPanel.setReferenceViewer(reference);
        checkAndOpenRefViewer(reference, tssResultsPanel);
        tssResultsPanel.setPersistentReference(reference);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        this.initConnectors(chromId, trackID);
        if (refConnector != null) {
            try {
                this.initFeatureData();

                String tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RATIO);
                int ratio = Integer.valueOf(tmp);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS);
                boolean isInternalExclusion;
                isInternalExclusion = !tmp.equals("no");

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                boolean isKeepingAllIntragenicTSS;
                isKeepingAllIntragenicTSS = !tmp.equals("no");

                Integer isKeepingAllIntragenicTss_Limit = 0;
                tmp = secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                try {
                    isKeepingAllIntragenicTss_Limit = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                boolean isKeepingOnlyAssignedIntragenicTSS;
                isKeepingOnlyAssignedIntragenicTSS = !tmp.equals("no");

                Integer isKeepingOnlyAssignedIntragenicTssLimitDistance = 0;
                tmp = secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                try {
                    isKeepingOnlyAssignedIntragenicTssLimitDistance = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION);
                int rangeForKeepingTSS = Integer.valueOf(tmp);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION);
                int rangeForLeaderlessDetection = Integer.valueOf(tmp);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS);
                replaced = tmp.replaceAll(",", ".");
                int cdsPercentageValue = Integer.valueOf(replaced);

                boolean includeBestMatchedReads;
                tmp = secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS);
                includeBestMatchedReads = !tmp.equals("no");

                tmp = secondSheetMap.get(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION);
                int maxDistantaseFor3UtrAntisenseDetection = Integer.valueOf(tmp);

                tmp = secondSheetMap.get(WizardPropertyStrings.PROP_VALID_START_CODONS);
                Map<String, StartCodon> validStartCodons = new HashMap<>();
                if (!tmp.isEmpty()) {
                    String[] startCodons = tmp.split(";");

                    for (String string : startCodons) {
                        switch (string) {
                            case "ATG":
                                validStartCodons.put("ATG", StartCodon.ATG);
                                break;
                            case "CTG":
                                validStartCodons.put("CTG", StartCodon.CTG);
                                break;
                            case "GTG":
                                validStartCodons.put("GTG", StartCodon.GTG);
                                break;
                            case "TTG":
                                validStartCodons.put("TTG", StartCodon.TTG);
                                break;
                        }
                    }
                }
                tmp = secondSheetMap.get(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT);
                List<FeatureType> types = new ArrayList<>();
                Set<FeatureType> featTypes = null;
                if (!tmp.isEmpty()) {
                    String[] typeStings = tmp.split(";");

                    for (String type : typeStings) {
                        if (type.equals(FeatureType.MISC_RNA.toString())) {
                            types.add(FeatureType.MISC_RNA);
                        } else if (type.equals(FeatureType.SOURCE.toString())) {
                            types.add(FeatureType.SOURCE);
                        }
                    }
                    featTypes = new HashSet<>(types);
                }

                int keepingInternalRange = 0;
                if (isKeepingAllIntragenicTSS) {
                    keepingInternalRange = isKeepingAllIntragenicTss_Limit;
                } else if (isKeepingOnlyAssignedIntragenicTSS) {
                    keepingInternalRange = isKeepingOnlyAssignedIntragenicTssLimitDistance;
                }
                ParameterSetFiveEnrichedAnalyses params = new ParameterSetFiveEnrichedAnalyses(
                        fraction, ratio, isInternalExclusion,
                        rangeForKeepingTSS, rangeForLeaderlessDetection, keepingInternalRange, isKeepingAllIntragenicTSS, isKeepingOnlyAssignedIntragenicTSS, cdsPercentageValue, includeBestMatchedReads, maxDistantaseFor3UtrAntisenseDetection, validStartCodons, featTypes);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                TSSDetectionResults tssResult = new TSSDetectionResults(stats, null, trackMap, refConnector.getRefGenome());
                tssResult.setParameters(params);
                List<TranscriptionStart> tss = new ArrayList<>();
                TranscriptionStart ts;
//                progressHandle.progress("Initialize table ... ", 20);

                for (int row = 1; row < model.getRowCount(); row++) {

                    tmp = (String) model.getValueAt(row, 13);
                    boolean isInternalTSS;
                    isInternalTSS = !tmp.equals("false");

                    PersistentFeature detectedGene = null;
                    PersistentFeature downstreamNextGene = null;
                    String locus = (String) model.getValueAt(row, 6);

                    if (featureMap.containsKey(locus)) {
                        if (isInternalTSS) {
                            downstreamNextGene = featureMap.get(locus);
                        } else {
                            detectedGene = featureMap.get(locus);
                        }
                    }

                    boolean isFwd;
                    tmp = (String) model.getValueAt(row, 1);
                    isFwd = tmp.equals("Fwd");

                    tmp = (String) model.getValueAt(row, 0);
                    int tssPosition = Integer.valueOf(tmp);

                    String comment = (String) model.getValueAt(row, 2);

                    int readStarts;
                    String readStartsString = (String) model.getValueAt(row, 3);
                    if (readStartsString.equals("-") || readStartsString.isEmpty()) {
                        readStarts = 0;
                    } else {
                        readStarts = Integer.valueOf(readStartsString);
                    }

                    double relCount;
                    tmp = (String) model.getValueAt(row, 4);
                    if (tmp.equals("-") || readStartsString.isEmpty()) {
                        relCount = 0.0;
                    } else {
                        replaced = tmp.replaceAll(",", ".");
                        relCount = Double.valueOf(replaced);
                    }

                    tmp = (String) model.getValueAt(row, 7);
                    int offset;
                    if (tmp.equals("-") || readStartsString.isEmpty()) {
                        offset = 0;
                    } else {
                        offset = Integer.valueOf(tmp);
                    }

                    int dist2Start;
                    tmp = (String) model.getValueAt(row, 8);
                    if (tmp.equals("-") || readStartsString.isEmpty()) {
                        dist2Start = 0;
                    } else {
                        dist2Start = Integer.valueOf(tmp);
                    }

                    int dist2Stop;
                    tmp = (String) model.getValueAt(row, 9);
                    if (tmp.equals("-") || readStartsString.isEmpty()) {
                        dist2Stop = 0;
                    } else {
                        dist2Stop = Integer.valueOf(tmp);
                    }

                    tmp = (String) model.getValueAt(row, 11);
                    boolean isLeaderless;
                    isLeaderless = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 12);
                    boolean isCdsShift;
                    isCdsShift = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 14);
                    boolean isIntergenic;
                    isIntergenic = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 15);
                    boolean isPutAntisense;
                    isPutAntisense = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 16);
                    boolean is5PrimeAntisense;
                    is5PrimeAntisense = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 17);
                    boolean is3PrimeAntisense;
                    is3PrimeAntisense = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 18);
                    boolean isIntragenicAntisense;
                    isIntragenicAntisense = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 19);
                    boolean isAssignedToStableRna;
                    isAssignedToStableRna = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 20);
                    boolean isFalsePositive;
                    isFalsePositive = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 21);
                    boolean isSelected;
                    isSelected = !tmp.equals("false");

                    tmp = (String) model.getValueAt(row, 22);
                    boolean isConsidered;
                    isConsidered = !tmp.equals("false");

                    ts = new TranscriptionStart(tssPosition,
                            isFwd, readStarts, relCount,
                            detectedGene, offset,
                            dist2Start, dist2Stop,
                            downstreamNextGene, offset, isLeaderless, isCdsShift,
                            isInternalTSS, isPutAntisense, chromId, trackID);
                    ts.setComment(comment);
                    ts.setAssignedToStableRNA(isAssignedToStableRna);
                    ts.setIs5PrimeUtrAntisense(is5PrimeAntisense);
                    ts.setIs3PrimeUtrAntisense(is3PrimeAntisense);
                    ts.setIntragenicAntisense(isIntragenicAntisense);
                    ts.setFalsePositive(isFalsePositive);
                    ts.setSelected(isSelected);
                    ts.setIntergenicTSS(isIntergenic);
                    ts.setIsconsideredTSS(isConsidered);
                    tss.add(ts);
                }
                tssResult.setResults(tss);
                tssResultsPanel.addResult(tssResult);
                transcAnalysesTopComp.openAnalysisTab("TSS detection results for: " + refConnector.getAssociatedTrackNames().get(trackID) + " Hits: " + tss.size(), tssResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. Check the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpRpkmStructuresAndTable(TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, DefaultTableModel model, Map<String, String> secondSheetMap,
            Map<String, String> secondSheetMapThirdCol) {
        ResultPanelRPKM resultPanel = new ResultPanelRPKM();

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        this.initConnectors(chromId, trackID);

        if (refConnector != null) {
            this.initFeatureData();

            boolean includeBestMatchedReads_RPKM;
            String tmp = secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_RPKM);
            includeBestMatchedReads_RPKM = !tmp.equals("no");

            ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, false, true, null, 0, 0, false, 0, false, includeBestMatchedReads_RPKM, false);
            List<RPKMvalue> rpkms = new ArrayList<>();
            RPKMvalue rpkm;
            for (int row = 1; row < model.getRowCount(); row++) {

                String featureLocus = (String) model.getValueAt(row, 0);
                String knownFiveUtr = (String) model.getValueAt(row, 6);
                int known5Utr = Integer.valueOf(knownFiveUtr);
                String rpkmString = (String) model.getValueAt(row, 7);
                String replaced = rpkmString.replaceAll(",", ".");
                double rpkmValue = Double.valueOf(replaced);
                String logRpkmString = (String) model.getValueAt(row, 8);
                replaced = logRpkmString.replaceAll(",", ".");
                double logRpkm = Double.valueOf(replaced);
                String readCountString = (String) model.getValueAt(row, 9);
                int readCount = Integer.valueOf(readCountString);

                rpkm = new RPKMvalue(featureMap.get(featureLocus), rpkmValue, logRpkm, readCount, trackID, chromId);
                rpkm.setLongestKnownUtrLength(known5Utr);
                rpkms.add(rpkm);
            }

            RPKMAnalysisResult rpkmResult = new RPKMAnalysisResult(trackMap, rpkms, refConnector.getRefGenome());
            resultPanel.addResult(rpkmResult);
            rpkmResult.setParameters(params);
            transcAnalysesTopComp.openAnalysisTab("RPKM values for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits:" + rpkmResult.getResults().size(), resultPanel);

            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                    "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpOperonStructuresAndTable(PersistentReference reference, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, DefaultTableModel model, Map<String, String> secondSheetMap,
            Map<String, String> secondSheetMapThirdCol) {
        ResultPanelOperonDetection resultPanel = new ResultPanelOperonDetection();
        resultPanel.setPersistentReference(reference);
        checkAndOpenRefViewer(reference, resultPanel);
//        resultPanel.setReferenceViewer(reference);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        this.initConnectors(chromId, trackID);
        if (refConnector != null) {
            try {
                this.initFeatureData();

                String tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                boolean includeBestMatchedReads_OP;
                tmp = secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP);
                includeBestMatchedReads_OP = !tmp.equals("no");

                tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, 0, false, 0, includeBestMatchedReads_OP, false, false);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                List<Operon> operons = new ArrayList<>();
                List<OperonAdjacency> adjacencies;
                Operon operon;
                for (int row = 1; row < model.getRowCount(); row++) {
                    adjacencies = new ArrayList<>();
                    operon = new Operon(trackID);
                    // getAll Adjacencies, put them in operon.
                    int transcriptStart = Integer.parseInt((String) model.getValueAt(row, 0));
                    operon.setStartPositionOfTranscript(transcriptStart);

                    String firstFeatures = (String) model.getValueAt(row, 1);
                    String[] splitedFeatures = firstFeatures.split("\n");
                    String secondFeatures = (String) model.getValueAt(row, 2);
                    String[] splitedSecFeatures = secondFeatures.split("\n");
                    String spanningReadCount = (String) model.getValueAt(row, 8);
                    String[] splitedSpanningReadCounts = spanningReadCount.split("\n");

                    for (int i = 0; i < splitedFeatures.length; i++) {
                        String firstFeature = splitedFeatures[i];
                        String secondFeature = splitedSecFeatures[i];
                        int spanningReads = Integer.valueOf(splitedSpanningReadCounts[i]);
                        OperonAdjacency adj = new OperonAdjacency(featureMap.get(firstFeature), featureMap.get(secondFeature));
                        adj.setSpanningReads(spanningReads);
                        adjacencies.add(adj);
                    }

                    String direction = (String) model.getValueAt(row, 3);
                    String withoutNewLine = direction.substring(0, direction.length() - 1);
                    boolean isFwd = withoutNewLine.equals("Fwd");

                    String falsePositiveString = (String) model.getValueAt(row, 6);
                    boolean isFalsPositive = !falsePositiveString.equals("false");

                    String consideration = (String) model.getValueAt(row, 7);
                    boolean isConsidered = !consideration.equals("false");

                    operon.addAllOperonAdjacencies(adjacencies);
                    operon.setIsConsidered(isConsidered);
                    operon.setFwdDirection(isFwd);
                    operon.setFalsPositive(isFalsPositive);
                    operons.add(operon);
                }
                OperonDetectionResult operonResults = new OperonDetectionResult(stats, trackMap, operons, refConnector.getRefGenome());
                operonResults.setParameters(params);
                resultPanel.addResult(operonResults);
                transcAnalysesTopComp.openAnalysisTab("Operon detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + operonResults.getResults().size(), resultPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                        "Import was successful!", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpNovelTranscriptsStructuresAndTable(PersistentReference reference, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, DefaultTableModel model, Map<String, String> secondSheetMap,
            Map<String, String> secondSheetMapThirdCol) {
        NovelRegionResultPanel novelRegionsResultsPanel = new NovelRegionResultPanel();
        novelRegionsResultsPanel.setPersistentReference(reference);
        checkAndOpenRefViewer(reference, novelRegionsResultsPanel);
//        novelRegionsResultsPanel.setReferenceViewer(reference);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        this.initConnectors(chromId, trackID);

        if (refConnector != null) {
           this.initFeatureData();

            String tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
            String replaced = tmp.replaceAll(",", ".");
            double mappingCount = Double.valueOf(replaced);

            tmp = secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
            replaced = tmp.replaceAll(",", ".");
            double mappingMeanLength = Double.valueOf(replaced);

            tmp = secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
            replaced = tmp.replaceAll(",", ".");
            double mappingsPerMillion = Double.valueOf(replaced);

            tmp = secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
            replaced = tmp.replaceAll(",", ".");
            double backgroundThreshold = Double.valueOf(replaced);

            tmp = secondSheetMap.get(WizardPropertyStrings.PROP_Fraction);
            replaced = tmp.replaceAll(",", ".");
            double fraction = Double.valueOf(replaced);

            tmp = secondSheetMap.get(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH);
            int minBoundary = Integer.valueOf(tmp);

            tmp = secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION);
            boolean includeRatioValue = false;
            if (tmp.equals("yes")) {
                includeRatioValue = true;
            }

            tmp = secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
            boolean isThresholdSettedManually;
            isThresholdSettedManually = !tmp.equals("no");

            tmp = secondSheetMap.get(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION);
            int ratio = Integer.valueOf(tmp);

            boolean includeBestMatchedReads;
            tmp = secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR);
            includeBestMatchedReads = !tmp.equals("no");

            ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, minBoundary, includeRatioValue, ratio, false, false, includeBestMatchedReads);
            params.setThresholdManuallySet(isThresholdSettedManually);
            StatisticsOnMappingData stats = new StatisticsOnMappingData(trackConnector, mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

            NovelRegionResult novelRegionResults = new NovelRegionResult(refConnector.getRefGenome(), stats, trackMap, null, false);
            novelRegionResults.setParameters(params);
            List<NovelTranscript> novelRegions = new ArrayList<>();
            NovelTranscript novelRegion;
            for (int row = 1; row < model.getRowCount(); row++) {

                String position = (String) model.getValueAt(row, 0);
                int novelRegStartPos = Integer.valueOf(position);

                String strand = (String) model.getValueAt(row, 1);
                boolean isFwd = strand.equals("Fwd");

                String falsePositive = (String) model.getValueAt(row, 2);
                boolean isFP = !falsePositive.equals("false");

                String selected = (String) model.getValueAt(row, 3);
                boolean isSelectedForBlast = !falsePositive.equals("false");

                String finishedSring = (String) model.getValueAt(row, 4);
                boolean isFinished = !finishedSring.equals("false");

                String dropOffString = (String) model.getValueAt(row, 6);
                int dropOff = Integer.valueOf(dropOffString);

                String lengthString = (String) model.getValueAt(row, 7);
                int length = Integer.valueOf(lengthString);

                novelRegion = new NovelTranscript(isFwd, novelRegStartPos, dropOff, (String) model.getValueAt(row, 5),
                        length, (String) model.getValueAt(row, 8), isFP, isSelectedForBlast, trackID, chromId);
                novelRegion.setIsConsidered(isFinished);
                novelRegions.add(novelRegion);
            }
            novelRegionResults.setResults(novelRegions);
            novelRegionsResultsPanel.addResult(novelRegionResults);
            transcAnalysesTopComp.openAnalysisTab("Novel Region detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + novelRegions.size(), novelRegionsResultsPanel);

            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successful!",
                    "Import was successful!", JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chromosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

}
