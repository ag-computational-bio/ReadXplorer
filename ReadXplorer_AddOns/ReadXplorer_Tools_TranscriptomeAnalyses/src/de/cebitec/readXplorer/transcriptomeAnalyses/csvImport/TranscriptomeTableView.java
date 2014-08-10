/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.csvImport;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.ui.importer.TranscriptomeTableViewI;
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
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author jritter
 */
@ServiceProvider(service = TranscriptomeTableViewI.class)
public class TranscriptomeTableView implements TranscriptomeTableViewI {

    private final ReferenceViewer refViewer;
    private ProgressHandle progressHandle;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;

    public TranscriptomeTableView() {

        refViewer = null;
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
            this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponentTopComponent();
        }
    }

    @Override
    public void process(List<List<?>> tableData, List<List<?>> tableData2, TableType type) {
        if (type == TableType.TSS_DETECTION_JR) {
            // TSS TAble
            setUpTSSTable(tableData, tableData2, refViewer, transcAnalysesTopComp);
        } else if (type == TableType.NOVEL_TRANSCRIPT_DETECTION_JR) {
            // NR Table
            setUpNovelTranscriptsTable(tableData, tableData2, refViewer, transcAnalysesTopComp);
        } else if (type == TableType.OPERON_DETECTION_JR) {
            // Operon Table
            setUpOperonTable(tableData, tableData2, refViewer, transcAnalysesTopComp);
        } else if (type == TableType.RPKM_ANALYSIS_JR) {
            // RPKM Table
            setUpRPKMTable(tableData, tableData2, refViewer, transcAnalysesTopComp);
        }
    }

    public void setUpOperonTable(List<List<?>> fstSheet, List<List<?>> sndSheet, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelOperonDetection resultPanel = new ResultPanelOperonDetection();
        resultPanel.setReferenceViewer(refViewer);

        Object trackID = fstSheet.get(1).get(fstSheet.get(0).size() - 1);
        Object chromId = fstSheet.get(1).get(fstSheet.get(0).size() - 3);

        PersistantTrack track = ProjectConnector.getInstance().getTrack((int) trackID);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector((int) chromId);
        if (refConnector != null) {
            try {
                trackMap.put(track.getId(), track);
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

                String tmp = "";
                String replaced = "";
                double mappingCount = 0;
                double mappingMeanLength = 0;
                double mappingsPerMillion = 0;
                double backgroundThreshold = 0;
                double fraction = 0;
                boolean includeBestMatchedReads_OP = false;
                boolean isThresholdSettedManually = false;
                if (sndSheet != null) {
                    for (List<?> columns : sndSheet) {
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

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, 0, false, 0, includeBestMatchedReads_OP, false, false);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                List<Operon> operons = new ArrayList<>();
                List<OperonAdjacency> adjacencies;
                Operon operon = null;
                progressHandle.progress("Initialize table ... ", 20);

                for (int row = 1; row < fstSheet.size(); row++) {
                    adjacencies = new ArrayList<>();
                    operon = new Operon((int) trackID);
                    // getAll Adjacencies, put them in operon.
                    int transcriptStart = Integer.parseInt((String) fstSheet.get(row).get(0));
                    operon.setStartPositionOfTranscript(transcriptStart);

                    String firstFeatures = (String) fstSheet.get(row).get(1);
                    String[] splitedFeatures = firstFeatures.split("\n");
                    String secondFeatures = (String) fstSheet.get(row).get(2);
                    String[] splitedSecFeatures = secondFeatures.split("\n");
                    String spanningReadCount = (String) fstSheet.get(row).get(8);
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
                    String direction = (String) fstSheet.get(row).get(3);
                    String withoutNewLine = direction.substring(0, direction.length() - 1);
                    isFwd = withoutNewLine.equals("Fwd");

                    boolean isFalsPositive;
                    String falsePositiveString = (String) fstSheet.get(row).get(6);
                    isFalsPositive = !falsePositiveString.equals("false");

                    boolean isConsidered;
                    String consideration = (String) fstSheet.get(row).get(7);
                    isConsidered = !consideration.equals("false");

                    operon.addAllOperonAdjacencies(adjacencies);
                    operon.setIsConsidered(isConsidered);
                    operon.setFwdDirection(isFwd);
                    operon.setFalsPositive(isFalsPositive);
                    operons.add(operon);
                }
                progressHandle.progress(27);
                OperonDetectionResult operonResults = new OperonDetectionResult(stats, trackMap, operons, refConnector.getRefGenome().getId());
                operonResults.setParameters(params);
                resultPanel.addResult(operonResults);
                transcAnalysesTopComp.openAnalysisTab("Operon detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + operonResults.getResults().size(), resultPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successfull!",
                        "Import was successfull!", JOptionPane.INFORMATION_MESSAGE);
                progressHandle.finish();
            } catch (Exception e) {
                progressHandle.finish();
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            progressHandle.finish();
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpRPKMTable(List<List<?>> fstSheet, List<List<?>> sndSheet, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelRPKM resultPanel = new ResultPanelRPKM();

        Object trackID = (String) fstSheet.get(1).get(fstSheet.size() - 1);
        Object chromId = (String) fstSheet.get(1).get(fstSheet.size() - 3);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();

        PersistantTrack track = ProjectConnector.getInstance().getTrack((int) trackID);

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector((int) chromId);
        List<PersistantFeature> genomeFeatures = new ArrayList<>();

        if (refConnector != null) {

            try {
                trackMap.put(track.getId(), track);

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

                String tmp = "";
                boolean includeBestMatchedReads_RPKM = false;

                if (sndSheet != null) {
                    for (List<?> columns : sndSheet) {

                        if (columns.get(0).equals(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP)) {
                            tmp = (String) columns.get(1);
                            includeBestMatchedReads_RPKM = !tmp.equals("no");
                        }
                    }
                }
                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, false, true, null, 0, 0, false, 0, false, includeBestMatchedReads_RPKM, false);
                List<RPKMvalue> rpkms = new ArrayList<>();
                RPKMvalue rpkm = null;
                progressHandle.progress("Initialize table ... ", 20);
                for (int row = 1; row < fstSheet.size(); row++) {

                    String featureLocus = (String) fstSheet.get(row).get(0);
                    String knownFiveUtr = (String) fstSheet.get(row).get(6);
                    int known5Utr = Integer.valueOf(knownFiveUtr);
                    String rpkmString = (String) fstSheet.get(row).get(7);
                    String replaced = rpkmString.replaceAll(",", ".");
                    double rpkmValue = Double.valueOf(replaced);
                    String logRpkmString = (String) fstSheet.get(row).get(8);
                    replaced = logRpkmString.replaceAll(",", ".");
                    double logRpkm = Double.valueOf(replaced);
                    String readCountString = (String) fstSheet.get(row).get(9);
                    int readCount = Integer.valueOf(readCountString);

                    rpkm = new RPKMvalue(featureMap.get(featureLocus), rpkmValue, logRpkm, readCount, (int) trackID, (int) chromId);
                    rpkm.setLongestKnownUtrLength(known5Utr);
                    rpkms.add(rpkm);
                }
                progressHandle.progress(27);

                RPKMAnalysisResult rpkmResult = new RPKMAnalysisResult(trackMap, rpkms, refConnector.getRefGenome().getId());
                resultPanel.addResult(rpkmResult);
                rpkmResult.setParameters(params);
                transcAnalysesTopComp.openAnalysisTab("RPKM values for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits:" + rpkmResult.getResults().size(), resultPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successfull!",
                        "Import was successfull!", JOptionPane.INFORMATION_MESSAGE);
                progressHandle.finish();
            } catch (Exception e) {
                progressHandle.finish();
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            progressHandle.finish();
            JOptionPane.showMessageDialog(null, "Something went wrong, please check the chrosome id. The reference should contain the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpNovelTranscriptsTable(List<List<?>> fstSheet, List<List<?>> sndSheet, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        NovelRegionResultPanel novelRegionsResultsPanel = new NovelRegionResultPanel();
        novelRegionsResultsPanel.setReferenceViewer(refViewer);

        Object refID = (String) fstSheet.get(1).get(fstSheet.size() - 1);
        Object chromId = (String) fstSheet.get(1).get(fstSheet.size() - 3);

        PersistantTrack track = ProjectConnector.getInstance().getTrack((int) refID);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector((int) chromId);
        if (refConnector != null) {

            try {
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

                String tmp = "";
                String replaced = "";
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
                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, minBoundary, includeRatioValue, ratio, false, false, includeBestMatchedReads);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                NovelRegionResult novelRegionResults = new NovelRegionResult(stats, trackMap, null, false);
                novelRegionResults.setParameters(params);
                List<NovelTranscript> novelRegions = new ArrayList<>();
                NovelTranscript novelRegion = null;
                progressHandle.progress("Initialize table ... ", 20);
                for (int row = 1; row < fstSheet.size(); row++) {

                    String position = (String) fstSheet.get(row).get(0);
                    int novelRegStartPos = Integer.valueOf(position);

                    String strand = (String) fstSheet.get(row).get(1);
                    boolean isFwd = strand.equals("Fwd");

                    String falsePositive = (String) fstSheet.get(row).get(2);
                    boolean isFP = !falsePositive.equals("false");

                    String selected = (String) fstSheet.get(row).get(3);
                    boolean isSelectedForBlast = !selected.equals("false");

                    String finishedSring = (String) fstSheet.get(row).get(4);
                    boolean isFinished = !finishedSring.equals("false");

                    String dropOffString = (String) fstSheet.get(row).get(6);
                    int dropOff = Integer.valueOf(dropOffString);

                    String lengthString = (String) fstSheet.get(row).get(7);
                    int length = Integer.valueOf(lengthString);

                    novelRegion = new NovelTranscript(isFwd, novelRegStartPos, dropOff, (String) fstSheet.get(row).get(5),
                            length, (String) fstSheet.get(row).get(8), isFP, isSelectedForBlast, (int) refID, (int) chromId);
                    novelRegion.setIsConsidered(isFinished);
                    novelRegions.add(novelRegion);
                }
                progressHandle.progress(27);
                novelRegionResults.setResults(novelRegions);
                novelRegionsResultsPanel.addResult(novelRegionResults);
                transcAnalysesTopComp.openAnalysisTab("Novel Region detection results for " + refConnector.getAssociatedTrackNames().get(track.getId()) + " Hits: " + novelRegions.size(), novelRegionsResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successfull!",
                        "Import was successfull!", JOptionPane.INFORMATION_MESSAGE);
                progressHandle.finish();
            } catch (Exception e) {
                progressHandle.finish();
                JOptionPane.showMessageDialog(null, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            progressHandle.finish();
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
    public void setUpTSSTable(List<List<?>> fstSheet, List<List<?>> sndSheet, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        tssResultsPanel.setReferenceViewer(refViewer);

        Object refID = fstSheet.get(1).get(fstSheet.get(0).size() - 1);
        Object chromId = fstSheet.get(1).get(fstSheet.get(0).size() - 2);
        // needed once!
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector((int) chromId);
        if (refConnector != null) {
            PersistantTrack track = ProjectConnector.getInstance().getTrack((int) refID);
            try {
                trackMap.put(track.getId(), track);

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

                progressHandle.progress("Load statistics and parameters from file ... ", 15);

                String replaced = "";
                String tmp = "";
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
                                JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                            }
                        } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS)) {
                            tmp = (String) columns.get(1);
                            isKeepingOnlyAssignedIntragenicTSS = !tmp.equals("no");
                        } else if (columns.get(0).equals(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS)) {
                            tmp = (String) columns.get(2);
                            try {
                                isKeepingOnlyAssignedIntragenicTssLimitDistance = Integer.parseInt(tmp);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
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

                            if (!tmp.equals("")) {
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

                            if (!tmp.equals("")) {
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
                if (isKeepingAllIntragenicTSS) {
                    keepingInternalRange = isKeepingAllIntragenicTss_Limit;
                } else if (isKeepingOnlyAssignedIntragenicTSS) {
                    keepingInternalRange = isKeepingOnlyAssignedIntragenicTssLimitDistance;
                }
                ParameterSetFiveEnrichedAnalyses params = new ParameterSetFiveEnrichedAnalyses(
                        fraction, ratio, isInternalExclusion,
                        rangeForKeepingTSS, rangeForLeaderlessDetection, keepingInternalRange, isKeepingAllIntragenicTSS, isKeepingOnlyAssignedIntragenicTSS, cdsPercentageValue, includeBestMatchedReads, maxDistantaseFor3UtrAntisenseDetection, validStartCodons, featTypes);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                TSSDetectionResults tssResult = new TSSDetectionResults(stats, null, trackMap, genomeId);
                tssResult.setParameters(params);
                List<TranscriptionStart> tss = new ArrayList<>();
                TranscriptionStart ts = null;

                for (List<?> list : fstSheet) {
                    int tssPosition = (Integer) list.get(0);
                    boolean isFwd = (Boolean) list.get(1);
                    String comment = (String) list.get(2);
                    int readStarts = (Integer) list.get(3);
                    double relCount = (Double) list.get(4);

                    boolean isInternalTSS = (Boolean) list.get(13);
                    PersistantFeature detectedGene = null;
                    PersistantFeature downstreamNextGene = null;
                    String locus = (String) list.get(6);
                    if (featureMap.containsKey(locus)) {
                        if (isInternalTSS) {
                            downstreamNextGene = featureMap.get(locus);
                        } else {
                            detectedGene = featureMap.get(locus);
                        }
                    }

                    int offset = (Integer) list.get(7);
                    int dist2Start = (Integer) list.get(8);
                    int dist2Stop = (Integer) list.get(9);

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
                            isInternalTSS, isPutAntisense, (int) chromId, (int) refID);
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
                transcAnalysesTopComp.openAnalysisTab("TSS detection results for: " + refConnector.getAssociatedTrackNames().get(refID) + " Hits: " + tss.size(), tssResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successfull!",
                        "Import was successfull!", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException | HeadlessException e) {
                JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the chrosome id. Check the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }
}
