package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.RPKMvalue;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
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
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.FivePrimeEnrichedTracksVisualPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.WizardPropertyStrings;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 * ExcelImporter provides methods for the import of the table from novel
 * transcrtipts, tss, operon detection as well as the table from rpkm and read
 * count calculation.
 *
 * @author jritter
 */
public class ExcelImporter {

    private DefaultTableModel model;
    private final ProgressHandle progressHandle;
    private HashMap<String, String> secondSheetMap;
    private HashMap<String, String> secondSheetMapThirdCol;
    private static final String TABLE_TYPE = "Table Type";

    public ExcelImporter(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.model = new DefaultTableModel();
    }

//    public ExcelImporter() {
//        this.progressHandle = ProgressHandleFactory.createHandle("Import progress ...");
//        progressHandle.start(30);
//        this.model = new DefaultTableModel();
//    }
    public ExcelImporter() {
        this.model = new DefaultTableModel();
        this.progressHandle = ProgressHandleFactory.createHandle("Import progress ...");
        progressHandle.start(30);
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
        this.secondSheetMapThirdCol = exlToTable.getSecondSheetDataThirdColumn();

        progressHandle.progress(10);
        if (secondSheetMap.containsKey(TABLE_TYPE)) {
            if (secondSheetMap.get(TABLE_TYPE).equals(TableType.TSS_TABLE.toString())) {
                setUpTSSDataStructuresAndTable(refViewer, transcAnalysesTopComp);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(TableType.NOVEL_TRANSCRIPTS_TABLE.toString())) {
                setUpNovelTranscriptsStructuresAndTable(refViewer, transcAnalysesTopComp);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(TableType.RPKM_TABLE.toString())) {
                setUpRpkmStructuresAndTable(transcAnalysesTopComp);
            }
            if (secondSheetMap.get(TABLE_TYPE).equals(TableType.OPERON_TABLE.toString())) {
                setUpOperonStructuresAndTable(refViewer, transcAnalysesTopComp);
            }
        } else {
            JOptionPane.showMessageDialog(refViewer, this, "Import of table is canceled, because no table tag was found! Please check the Parameters and Statistics sheet.", JOptionPane.CANCEL_OPTION);
        }
    }

    /**
     * Method for importing all important excel cells to create all TSS
     * instances.
     *
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent
     * TranscriptomeAnalysesTopComponentTopComponent
     */
    public void setUpTSSDataStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        tssResultsPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int refID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
        if (refConnector != null) {
            PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
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

                progressHandle.progress("Load statistics and parameters from file ... ", 15);

                String tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RATIO);
                int ratio = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS);
                boolean isInternalExclusion;
                isInternalExclusion = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                boolean isKeepingAllIntragenicTSS;
                isKeepingAllIntragenicTSS = !tmp.equals("no");

                Integer isKeepingAllIntragenicTss_Limit = 0;
                tmp = (String) secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                try {
                    isKeepingAllIntragenicTss_Limit = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                boolean isKeepingOnlyAssignedIntragenicTSS;
                isKeepingOnlyAssignedIntragenicTSS = !tmp.equals("no");

                Integer isKeepingOnlyAssignedIntragenicTssLimitDistance = 0;
                tmp = (String) secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                try {
                    isKeepingOnlyAssignedIntragenicTssLimitDistance = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION);
                int rangeForKeepingTSS = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION);
                int rangeForLeaderlessDetection = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS);
                replaced = tmp.replaceAll(",", ".");
                int cdsPercentageValue = Integer.valueOf(replaced);

                boolean includeBestMatchedReads;
                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS);
                includeBestMatchedReads = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION);
                int maxDistantaseFor3UtrAntisenseDetection = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_VALID_START_CODONS);
                HashMap<String, StartCodon> validStartCodons = new HashMap<>();
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
                tmp = (String) secondSheetMap.get(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT);
                List<FeatureType> types = new ArrayList<>();
                HashSet<FeatureType> featTypes = null;
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
                StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                TSSDetectionResults tssResult = new TSSDetectionResults(stats, null, trackMap, refConnector.getRefGenome());
                tssResult.setParameters(params);
                List<TranscriptionStart> tss = new ArrayList<>();
                TranscriptionStart ts = null;
                progressHandle.progress("Initialize table ... ", 20);

                for (int row = 1; row < model.getRowCount(); row++) {

                    tmp = (String) model.getValueAt(row, 13);
                    boolean isInternalTSS;
                    isInternalTSS = !tmp.equals("false");

                    PersistantFeature detectedGene = null;
                    PersistantFeature downstreamNextGene = null;
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
                            isInternalTSS, isPutAntisense, chromId, refID);
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
                progressHandle.progress(27);
                tssResult.setResults(tss);
                tssResultsPanel.addResult(tssResult);
                transcAnalysesTopComp.openAnalysisTab("TSS detection results for: " + refConnector.getAssociatedTrackNames().get(refID) + " Hits: " + tss.size(), tssResultsPanel);

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Import was successfull!",
                        "Import was successfull!", JOptionPane.INFORMATION_MESSAGE);
                progressHandle.finish();
            } catch (Exception e) {
                progressHandle.finish();
                JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            progressHandle.finish();
            JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the chrosome id. Check the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }

    public void setUpRpkmStructuresAndTable(TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelRPKM resultPanel = new ResultPanelRPKM();

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();

        PersistantTrack track = ProjectConnector.getInstance().getTrack(trackID);

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
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

                boolean includeBestMatchedReads_RPKM;
                String tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP);
                includeBestMatchedReads_RPKM = !tmp.equals("no");

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, false, true, null, 0, 0, false, 0, false, includeBestMatchedReads_RPKM, false);
                List<RPKMvalue> rpkms = new ArrayList<>();
                RPKMvalue rpkm = null;
                progressHandle.progress("Initialize table ... ", 20);
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
                progressHandle.progress(27);

                RPKMAnalysisResult rpkmResult = new RPKMAnalysisResult(trackMap, rpkms, refConnector.getRefGenome());
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

    public void setUpOperonStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelOperonDetection resultPanel = new ResultPanelOperonDetection();
        resultPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int trackID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(trackID);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
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
                String tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_OVERSPANNING_READS);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                boolean includeBestMatchedReads_OP;
                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP);
                includeBestMatchedReads_OP = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, 0, false, 0, includeBestMatchedReads_OP, false, false);
                params.setThresholdManuallySet(isThresholdSettedManually);
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

                    boolean isFwd;
                    String direction = (String) model.getValueAt(row, 3);
                    String withoutNewLine = direction.substring(0, direction.length() - 1);
                    if (withoutNewLine.equals("Fwd")) {
                        isFwd = true;
                    } else {
                        isFwd = false;
                    }

                    boolean isFalsPositive;
                    String falsePositiveString = (String) model.getValueAt(row, 6);
                    if (falsePositiveString.equals("false")) {
                        isFalsPositive = false;
                    } else {
                        isFalsPositive = true;
                    }

                    boolean isConsidered;
                    String consideration = (String) model.getValueAt(row, 7);
                    if (consideration.equals("false")) {
                        isConsidered = false;
                    } else {
                        isConsidered = true;
                    }

                    operon.addAllOperonAdjacencies(adjacencies);
                    operon.setIsConsidered(isConsidered);
                    operon.setFwdDirection(isFwd);
                    operon.setFalsPositive(isFalsPositive);
                    operons.add(operon);
                }
                progressHandle.progress(27);
                OperonDetectionResult operonResults = new OperonDetectionResult(stats, trackMap, operons, refConnector.getRefGenome());
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

    public void setUpNovelTranscriptsStructuresAndTable(ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        NovelRegionResultPanel novelRegionsResultsPanel = new NovelRegionResultPanel();
        novelRegionsResultsPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int refID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 3);
        int chromId = Integer.valueOf(chromID);
        PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
        Map<Integer, PersistantTrack> trackMap = new HashMap<>();
        trackMap.put(track.getId(), track);

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
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
                String tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_Fraction);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(NovelRegionResultPanel.NOVELREGION_DETECTION_MIN_LENGTH);
                int minBoundary = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION);
                boolean includeRatioValue = false;
                if (tmp.equals("yes")) {
                    includeRatioValue = true;
                }

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION);
                int ratio = Integer.valueOf(tmp);

                boolean includeBestMatchedReads;
                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR);
                includeBestMatchedReads = !tmp.equals("no");

                ParameterSetWholeTranscriptAnalyses params = new ParameterSetWholeTranscriptAnalyses(true, false, true, false, null, fraction, minBoundary, includeRatioValue, ratio, false, false, includeBestMatchedReads);
                params.setThresholdManuallySet(isThresholdSettedManually);
                StatisticsOnMappingData stats = new StatisticsOnMappingData(refConnector.getRefGenome(), mappingMeanLength, mappingsPerMillion, mappingCount, backgroundThreshold);

                NovelRegionResult novelRegionResults = new NovelRegionResult(refConnector.getRefGenome(), stats, trackMap, null, false);
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
                    String falsePositive = (String) model.getValueAt(row, 2);
                    if (falsePositive.equals("false")) {
                        isFP = false;
                    } else {
                        isFP = true;
                    }

                    boolean isSelectedForBlast;
                    String selected = (String) model.getValueAt(row, 3);
                    if (falsePositive.equals("false")) {
                        isSelectedForBlast = false;
                    } else {
                        isSelectedForBlast = true;
                    }

                    boolean isFinished;
                    String finishedSring = (String) model.getValueAt(row, 4);
                    if (finishedSring.equals("false")) {
                        isFinished = false;
                    } else {
                        isFinished = true;
                    }

                    int dropOff;
                    String dropOffString = (String) model.getValueAt(row, 6);
                    dropOff = Integer.valueOf(dropOffString);

                    int length;
                    String lengthString = (String) model.getValueAt(row, 7);
                    length = Integer.valueOf(lengthString);

                    novelRegion = new NovelTranscript(isFwd, novelRegStartPos, dropOff, (String) model.getValueAt(row, 5),
                            length, (String) model.getValueAt(row, 8), isFP, isSelectedForBlast, refID, chromId);
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
     * TranscriptomeAnalysesTopComponentTopComponent
     */
    public void setUpTSSTable(List<List<?>> fstSheet, List<List<?>> sndSheet, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp) {
        ResultPanelTranscriptionStart tssResultsPanel = new ResultPanelTranscriptionStart();
        tssResultsPanel.setReferenceViewer(refViewer);

        String trackId = (String) model.getValueAt(1, model.getColumnCount() - 1);
        int refID = Integer.valueOf(trackId);
        String chromID = (String) model.getValueAt(1, model.getColumnCount() - 2);
        int chromId = Integer.valueOf(chromID);
        HashMap<Integer, PersistantTrack> trackMap = new HashMap<>();

        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(chromId);
        if (refConnector != null) {
            PersistantTrack track = ProjectConnector.getInstance().getTrack(refID);
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

                String tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
                String replaced = tmp.replaceAll(",", ".");
                double mappingCount = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
                replaced = tmp.replaceAll(",", ".");
                double mappingMeanLength = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
                replaced = tmp.replaceAll(",", ".");
                double mappingsPerMillion = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
                replaced = tmp.replaceAll(",", ".");
                double backgroundThreshold = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_FRACTION);
                replaced = tmp.replaceAll(",", ".");
                double fraction = Double.valueOf(replaced);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RATIO);
                int ratio = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS);
                boolean isInternalExclusion;
                isInternalExclusion = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD);
                boolean isThresholdSettedManually;
                isThresholdSettedManually = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                boolean isKeepingAllIntragenicTSS;
                isKeepingAllIntragenicTSS = !tmp.equals("no");

                Integer isKeepingAllIntragenicTss_Limit = 0;
                tmp = (String) secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS);
                try {
                    isKeepingAllIntragenicTss_Limit = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                boolean isKeepingOnlyAssignedIntragenicTSS;
                isKeepingOnlyAssignedIntragenicTSS = !tmp.equals("no");

                Integer isKeepingOnlyAssignedIntragenicTssLimitDistance = 0;
                tmp = (String) secondSheetMapThirdCol.get(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS);
                try {
                    isKeepingOnlyAssignedIntragenicTssLimitDistance = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(refViewer, "Problem in second sheet of excel import file. No integer value for limit distance in field for keeping all intragenic tss.", "Import went wrong!", JOptionPane.CANCEL_OPTION);
                }

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION);
                int rangeForKeepingTSS = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION);
                int rangeForLeaderlessDetection = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS);
                replaced = tmp.replaceAll(",", ".");
                int cdsPercentageValue = Integer.valueOf(replaced);

                boolean includeBestMatchedReads;
                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS);
                includeBestMatchedReads = !tmp.equals("no");

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION);
                int maxDistantaseFor3UtrAntisenseDetection = Integer.valueOf(tmp);

                tmp = (String) secondSheetMap.get(WizardPropertyStrings.PROP_VALID_START_CODONS);
                HashMap<String, StartCodon> validStartCodons = new HashMap<>();
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
                tmp = (String) secondSheetMap.get(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT);
                List<FeatureType> types = new ArrayList<>();
                HashSet<FeatureType> featTypes = null;
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
                            isInternalTSS, isPutAntisense, chromId, refID);
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
            } catch (Exception e) {
                JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the track id. The database should contain the track id.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
            }
        } else {
            JOptionPane.showMessageDialog(refViewer, "Something went wrong, please check the chrosome id. Check the chromosome id. Check also the database.", "Something went wrong!", JOptionPane.CANCEL_OPTION);
        }
    }
}
