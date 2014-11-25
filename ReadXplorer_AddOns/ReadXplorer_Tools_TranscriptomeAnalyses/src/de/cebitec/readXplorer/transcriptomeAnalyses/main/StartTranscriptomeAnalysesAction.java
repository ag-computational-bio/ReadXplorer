package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
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
    private HashMap<Integer, PersistantTrack> trackMap;
//    private String readClassPropString;
//    private String selFeatureTypesPropString;
    private int referenceId;
    private FiveEnrichedDataAnalysesHandler fifePrimeAnalysesHandler;
    private WholeTranscriptDataAnalysisHandler wholeTranscriptAnalysesHandler;
    private boolean performFivePrimeAnalyses;
    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, performNovelRegionDetection, performRpkmAnalysis, ratioInclusion;
    private double fraction = 0.05;
    private int minBoundaryLength;
    private int increaseRatioValue;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;

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
//        boolean containsDBTrack = PersistantTrack.checkForDBTrack(this.tracks);
//        transWizardIterator.setUsingDBTrack(containsDBTrack);
//        this.readClassPropString = transWizardIterator.getReadClassPropForWiz();
//        this.selFeatureTypesPropString = transWizardIterator.getPropSelectedFeatTypes();


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
            this.trackMap = new HashMap<>();
            for (PersistantTrack track : this.tracks) {
                this.trackMap.put(track.getId(), track);
            }

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
            this.performFivePrimeAnalyses = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_FIVEPRIME_DATASET);
            this.performWholeTrascriptomeAnalyses = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_WHOLEGENOME_DATASET);

            if (performFivePrimeAnalyses) {
                int ratio = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RATIO);
                this.fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                boolean excludeInternalTss = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_INTERNAL_TSS);
                int excludeTSSDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE);
                int leaderlessDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT);
                int keepingInternalTssDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_KEEPINTERNAL_DISTANCE);
                int cdsShiftPercentage = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_PERCENTAGE_FOR_CDS_ANALYSIS);

                ParameterSetFiveEnrichedAnalyses parameterSetFiveprime = new ParameterSetFiveEnrichedAnalyses(this.fraction, ratio,
                        excludeInternalTss, excludeTSSDistance, leaderlessDistance, keepingInternalTssDistance, cdsShiftPercentage);

                // start five prime transcripts analyses handler
                this.fifePrimeAnalysesHandler = new FiveEnrichedDataAnalysesHandler(
                        track, parameterSetFiveprime, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);
                this.fifePrimeAnalysesHandler.start();
            }

            if (this.performWholeTrascriptomeAnalyses) {
                // get needed params
                this.performOperonDetection = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_OPERON_ANALYSIS);
                this.performNovelRegionDetection = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_NOVEL_ANALYSIS);

                this.performRpkmAnalysis = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RPKM_ANALYSIS);
                if (this.performOperonDetection) {
                    this.fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                }

                if (this.performNovelRegionDetection) {
                    this.fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_FRACTION_NOVELREGION_DETECTION);
                    this.minBoundaryLength = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_MIN_BOUNDRY_LENGTH);
                    this.ratioInclusion = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_INCLUDE_RATIOVALUE_IN_NOVEL_REGION_DETECTION);
                    if (this.ratioInclusion) {
                        this.increaseRatioValue = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RAIO_NOVELREGION_DETECTION);
                    } else {
                        this.increaseRatioValue = 0;
                    }

                }

                ParameterSetWholeTranscriptAnalyses parameterSetWholeTranscripts = new ParameterSetWholeTranscriptAnalyses(this.performWholeTrascriptomeAnalyses,
                        this.performOperonDetection, this.performNovelRegionDetection,
                        this.performRpkmAnalysis, this.fraction, this.minBoundaryLength, this.ratioInclusion, this.increaseRatioValue);
                // start whole transcript analyses handler
                this.wholeTranscriptAnalysesHandler = new WholeTranscriptDataAnalysisHandler(track, parameterSetWholeTranscripts, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);

                this.wholeTranscriptAnalysesHandler.start();

            }
        }
    }
}
