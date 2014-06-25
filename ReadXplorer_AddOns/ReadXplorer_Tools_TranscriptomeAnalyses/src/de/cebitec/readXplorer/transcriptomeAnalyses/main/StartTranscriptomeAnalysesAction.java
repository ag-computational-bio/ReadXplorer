package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.FivePrimeEnrichedTracksVisualPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.WizardPropertyStrings;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Tools",
        id = "de.cebitec.readXplorer.transcriptomeAnalyses.main.StartTranscriptomeAnalysesAction")
@ActionRegistration(
        displayName = "#CTL_StartTranscriptomeAnalysesAction")
@ActionReference(path = "Menu/Tools", position = 112)
@Messages("CTL_StartTranscriptomeAnalysesAction=Start Transcriptome Analyses")
public final class StartTranscriptomeAnalysesAction implements ActionListener {

    private final ReferenceViewer refViewer;
    private List<PersistantTrack> tracks;
    private Map<Integer, PersistantTrack> trackMap;
    private final int referenceId;
    private FiveEnrichedDataAnalysesHandler fifePrimeAnalysesHandler;
    private WholeTranscriptDataAnalysisHandler wholeTranscriptAnalysesHandler;
    private boolean performFivePrimeAnalyses;
    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, performNovelRegionDetection, performRpkmAnalysis, ratioInclusion;
    private double fraction = 0.05;
    private int minBoundaryLength;
    private int increaseRatioValue;
    private boolean isBgThresholdSetManually;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private File referenceFile;

    public StartTranscriptomeAnalysesAction(ReferenceViewer reference) {
        this.refViewer = reference;
        this.referenceId = this.refViewer.getReference().getId();
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.runWizardAndTranscriptionAnalysis();
    }

    /**
     * Initializes the setup wizard for the transcription analyses.
     */
    private void runWizardAndTranscriptionAnalysis() {
        @SuppressWarnings("unchecked")
        TranscriptomeAnalysisWizardIterator transWizardIterator = new TranscriptomeAnalysisWizardIterator(this.referenceId);
        WizardDescriptor wiz = new WizardDescriptor(transWizardIterator);
        transWizardIterator.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "TTL_TransAnalysesWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        List<PersistantTrack> selectedTracks = transWizardIterator.getSelectedTracks();
        if (!cancelled && !selectedTracks.isEmpty()) {
            this.tracks = selectedTracks;
            this.trackMap = ProjectConnector.getTrackMap(tracks);

            this.transcAnalysesTopComp.open();
            this.startTranscriptomeAnalyses(wiz);

        } else {
            String msg = NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfo",
                    "No track selected. To start a transcription analysis at least one track has to be selected.");
            String title = NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.refViewer, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Starts the transcription analyses.
     *
     * @param wiz the wizard containing the transcription analyses parameters
     */
    @SuppressWarnings("unchecked")
    private void startTranscriptomeAnalyses(WizardDescriptor wiz) {
        for (PersistantTrack track : this.tracks) {
            this.performFivePrimeAnalyses = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_FIVEPRIME_DATASET);
            this.performWholeTrascriptomeAnalyses = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_WHOLEGENOME_DATASET);

            if (performFivePrimeAnalyses) {
                int ratio = (int) wiz.getProperty(WizardPropertyStrings.PROP_RATIO);
                this.fraction = (double) wiz.getProperty(WizardPropertyStrings.PROP_Fraction);
                boolean excludeAllInternalTss = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_EXCLUDE_INTERNAL_TSS);
                int excludeTSSDistance = (int) wiz.getProperty(WizardPropertyStrings.PROP_UTR_LIMIT);
                int leaderlessDistance = (int) wiz.getProperty(WizardPropertyStrings.PROP_LEADERLESS_LIMIT);
                int keepingInternalTssDistance = (int) wiz.getProperty(WizardPropertyStrings.PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT);
                int cdsShiftPercentage = (int) wiz.getProperty(WizardPropertyStrings.PROP_PERCENTAGE_FOR_CDS_ANALYSIS);
                boolean keepAllIntragenicTss = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_KEEP_ALL_INTRAGENIC_TSS);
                boolean keepOnlyAssignedIntragenicTss = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_KEEP_ONLY_ITRAGENIC_TSS_ASSIGNED_TO_FEATURE);
                boolean includeBestMatchedReads = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS);
                boolean isThresholdManuallySetted = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE);
                int minStackSizeManuallySetted = (int) wiz.getProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE);
                int maxDistantaseFor3UtrAntisenseDetection = (int) wiz.getProperty(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION);
                HashMap<String, StartCodon> validStartCodons = (HashMap<String, StartCodon>) wiz.getProperty(WizardPropertyStrings.PROP_VALID_START_CODONS);

                HashSet<FeatureType> excludeFeatureTypes = (HashSet<FeatureType>) wiz.getProperty(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT);

                ParameterSetFiveEnrichedAnalyses parameterSetFiveprime
                        = new ParameterSetFiveEnrichedAnalyses(this.fraction, ratio,
                                excludeAllInternalTss, excludeTSSDistance, leaderlessDistance,
                                keepingInternalTssDistance, keepAllIntragenicTss, keepOnlyAssignedIntragenicTss,
                                cdsShiftPercentage, includeBestMatchedReads,
                                maxDistantaseFor3UtrAntisenseDetection, validStartCodons,
                                excludeFeatureTypes);

                parameterSetFiveprime.setThresholdManuallySet(isThresholdManuallySetted);
                parameterSetFiveprime.setManuallySetThreshold(minStackSizeManuallySetted);

                // start five prime transcripts analyses handler
                this.fifePrimeAnalysesHandler = new FiveEnrichedDataAnalysesHandler(
                        track, parameterSetFiveprime, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);
                this.fifePrimeAnalysesHandler.start();
            }

            if (this.performWholeTrascriptomeAnalyses) {
                // get needed params
                this.performOperonDetection = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_OPERON_ANALYSIS);
                this.performNovelRegionDetection = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_NOVEL_ANALYSIS);
                boolean includeBestMatchedReadsOp = false;
                boolean includeBestMatchedReadsRpkm = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_RPKM);
                boolean includeBestMatchedReadsNr = false;
                this.referenceFile = (File) wiz.getProperty(WizardPropertyStrings.PROP_REFERENCE_FILE_RPKM_DETERMINATION);
                boolean isThresholdManuallySetted = false;
                int minStackSizeManuallySetted = 1;

                this.performRpkmAnalysis = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_RPKM_ANALYSIS);
                if (this.performOperonDetection) {
                    includeBestMatchedReadsOp = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP);
                    this.fraction = (double) wiz.getProperty(WizardPropertyStrings.PROP_Fraction);
                    isThresholdManuallySetted = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE);
                    minStackSizeManuallySetted = (int) wiz.getProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE);
                }

                if (this.performNovelRegionDetection) {
                    includeBestMatchedReadsNr = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR);
                    this.fraction = (double) wiz.getProperty(WizardPropertyStrings.PROP_Fraction);
                    this.minBoundaryLength = (int) wiz.getProperty(WizardPropertyStrings.PROP_MIN_LENGTH_OF_NOVEL_TRANSCRIPT);
                    this.ratioInclusion = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION);
                    if (this.ratioInclusion) {
                        this.increaseRatioValue = (int) wiz.getProperty(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION);
                    } else {
                        this.increaseRatioValue = 0;
                    }
                    isThresholdManuallySetted = (boolean) wiz.getProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE);
                    minStackSizeManuallySetted = (int) wiz.getProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE);
                }

                ParameterSetWholeTranscriptAnalyses parameterSetWholeTranscripts = new ParameterSetWholeTranscriptAnalyses(this.performWholeTrascriptomeAnalyses,
                        this.performOperonDetection, this.performNovelRegionDetection,
                        this.performRpkmAnalysis, this.referenceFile, this.fraction, this.minBoundaryLength, this.ratioInclusion, this.increaseRatioValue, includeBestMatchedReadsOp, includeBestMatchedReadsRpkm, includeBestMatchedReadsNr);
                parameterSetWholeTranscripts.setThresholdManuallySet(isThresholdManuallySetted);
                parameterSetWholeTranscripts.setManuallySetThreshold(minStackSizeManuallySetted);
                // start whole transcript analyses handler
                this.wholeTranscriptAnalysesHandler = new WholeTranscriptDataAnalysisHandler(track, parameterSetWholeTranscripts, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);

                this.wholeTranscriptAnalysesHandler.start();

            }
        }
    }
}
