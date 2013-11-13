/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.wizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
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
        id = "de.cebitec.vamp.transcriptomeAnalyses.StartTranscriptomeAnalysesAction")
@ActionRegistration(
        displayName = "#CTL_StartTranscriptomeAnalysesAction")
@ActionReference(path = "Menu/Tools", position = 112)
@Messages("CTL_StartTranscriptomeAnalysesAction=Start Transcriptome Analyses")
public final class StartTranscriptomeAnalysesAction implements ActionListener {

    private final ReferenceViewer refViewer;
    private int finishedAnalyses = 0;
    private List<PersistantTrack> tracks;
    private HashMap<Integer, PersistantTrack> trackMap;
    private String readClassPropString;
    private String selFeatureTypesPropString;
    private GenomeFeatureParser featureParser;
    private int referenceId;
    private FiveEnrichedDataAnalysesHandler fifePrimeAnalysesHandler;
    private WholeTranscriptDataAnalysisHandler wholeTranscriptAnalysesHandler;
    private ParameterSetFiveEnrichedAnalyses parameterSetFiveprime;
    private ParameterSetWholeTranscriptAnalyses parameterSetWholeTranscripts;
    private boolean performFivePrimeAnalyses, performTSSAnalysis, performLeaderless, performAntisense, performPutativeUnAnno, performRBSDetection, performPromotorDetection;
    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, performNovelRegionDetection, rPKMs;
    private double fraction;
    private boolean cdsShift, excludeInternalTss;
    private int ratio, upstream, downstream, leaderlessDistance, excludeTSSDistance, keepingInternalTssDistance;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private boolean putativeUnAnnotated;

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
        this.readClassPropString = transWizardIterator.getReadClassPropForWiz();
        this.selFeatureTypesPropString = transWizardIterator.getPropSelectedFeatTypes();


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
        System.out.println("Start transcriptome analyses now!");

        for (PersistantTrack track : this.tracks) {
            // Here we parse the genome
            // 1. getting region2Exclude 
            // forwardCDSs and reverseCDSs 
            // => Feature IDs on Position i in genome 3 strands in each direction are maximum.
            // allRegionsInHash => Key Feature id, Value is the featuer 

            this.performFivePrimeAnalyses = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_FIVEPRIME_DATASET);
            this.performWholeTrascriptomeAnalyses = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_WHOLEGENOME_DATASET);

            if (performFivePrimeAnalyses) {
                this.ratio = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RATIO);
                this.fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                this.upstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_UPSTREAM);
                this.downstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_DOWNSTREAM);
                this.performTSSAnalysis = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_TSS_ANALYSIS);
                this.performAntisense = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_ANTISENSE_ANALYSIS);
                this.performPutativeUnAnno = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_PUTATIVE_UNANNOTATED);
                this.excludeInternalTss = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_INTERNAL_TSS);
                this.excludeTSSDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_EXCLUDE_TSS_DISTANCE);
                this.leaderlessDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_LIMIT);
                this.keepingInternalTssDistance = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_KEEPINTERNAL_DISTANCE);
                this.cdsShift = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_CDSSHIFT);

                this.parameterSetFiveprime = new ParameterSetFiveEnrichedAnalyses(this.performTSSAnalysis, this.performAntisense, this.performPutativeUnAnno, this.fraction, this.ratio, this.upstream,
                        this.downstream, this.excludeInternalTss, this.excludeTSSDistance, this.leaderlessDistance, this.cdsShift, this.keepingInternalTssDistance);

                // start five prime transcripts analyses handler
                this.fifePrimeAnalysesHandler = new FiveEnrichedDataAnalysesHandler(
                        track, this.referenceId, this.parameterSetFiveprime, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);
                this.fifePrimeAnalysesHandler.start();
            }

            if (this.performWholeTrascriptomeAnalyses) {
                // get needed params
                this.performOperonDetection = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_OPERON_ANALYSIS);
                this.performNovelRegionDetection = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_NOVEL_ANALYSIS);
                this.fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                this.rPKMs = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_NORMAL_RPKM_ANALYSIS);
                this.parameterSetWholeTranscripts = new ParameterSetWholeTranscriptAnalyses(this.performWholeTrascriptomeAnalyses,
                        this.performOperonDetection, this.performNovelRegionDetection,
                        this.rPKMs, this.fraction);
                // start whole transcript analyses handler
                this.wholeTranscriptAnalysesHandler = new WholeTranscriptDataAnalysisHandler(track, this.referenceId,
                        this.parameterSetWholeTranscripts, this.refViewer,
                        this.transcAnalysesTopComp, this.trackMap);

                this.wholeTranscriptAnalysesHandler.start();

            }



        }
    }
}
