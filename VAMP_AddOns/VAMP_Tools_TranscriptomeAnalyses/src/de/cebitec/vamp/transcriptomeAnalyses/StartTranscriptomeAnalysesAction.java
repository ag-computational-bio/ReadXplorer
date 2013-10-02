/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.wizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
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
public final class StartTranscriptomeAnalysesAction implements ActionListener, DataVisualisationI {

    private final ReferenceViewer refViewer;
    private int finishedAnalyses = 0;
    private List<PersistantTrack> tracks;
    private HashMap<Integer, PersistantTrack> trackMap;
    private String readClassPropString;
    private String selFeatureTypesPropString;
    private GenomeFeatureParser featureParser;
    private int referenceId;
   
    FifeEnrichedDataAnalysesHandler fifePrimeAnalysesHandler;
    ParameterSetFiveEnrichedAnalyses parameterSetFiveprime;
    private boolean performFivePrimeAnalyses, performTSSAnalysis, performLeaderless, performAntisense, performRBSDetection, performPromotorDetectin;
    private double fraction;
    private int ratio, upstream, downstream;
    
    private TranscriptomeAnalysesTopComponent transcAnalysesTopComp;
    private HashMap<Integer, AnalysesContainer> trackToAnalysisMap;

    public StartTranscriptomeAnalysesAction(ReferenceViewer reference) {
        this.refViewer = reference;
        this.referenceId = this.refViewer.getReference().getId();
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponent.PREFERRED_ID);
        this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponent) findTopComponent;
        this.trackToAnalysisMap = new HashMap<>();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenTrackPanelList otp = new OpenTrackPanelList(referenceId);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "CTL_OpenTrackList"));
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            this.tracks = otp.getSelectedTracks();
            this.trackMap = new HashMap<>();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                this.trackMap.put(track.getId(), track);
            }
            
            this.transcAnalysesTopComp.open();
            this.runWizardAndTranscriptionAnalysis();

        } else {
            String msg = NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfo",
                    "No track selected. To start a transcription analysis at least one track has to be selected.");
            String title = NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.refViewer, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Initializes the setup wizard for the transcription analyses.
     */
    private void runWizardAndTranscriptionAnalysis() {
        @SuppressWarnings("unchecked")
        TranscriptomeAnalysisWizardIterator transWizardIterator = new TranscriptomeAnalysisWizardIterator();
        boolean containsDBTrack = PersistantTrack.checkForDBTrack(this.tracks);
        transWizardIterator.setUsingDBTrack(containsDBTrack);
        this.readClassPropString = transWizardIterator.getReadClassPropForWiz();
        this.selFeatureTypesPropString = transWizardIterator.getPropSelectedFeatTypes();
        WizardDescriptor wiz = new WizardDescriptor(transWizardIterator);
        transWizardIterator.setWiz(wiz);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(StartTranscriptomeAnalysesAction.class, "TTL_TransAnalysesWizardTitle"));

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            this.startTranscriptomeAnalyses(wiz);
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

        TrackConnector connector;
        for (PersistantTrack track : this.tracks) {
            try {
                connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }

            performFivePrimeAnalyses = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_FIVEPRIME_DATASET);
            if (performFivePrimeAnalyses) {
                this.ratio = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RATIO);
                fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                upstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_UPSTREAM);
                downstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_DOWNSTREAM);
                performLeaderless = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_LEADERLESS_ANALYSIS);
                performTSSAnalysis = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_TSS_ANALYSIS);
                performAntisense = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_ANTISENSE_ANALYSIS);
                performPromotorDetectin = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS);
                performRBSDetection = (boolean) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_RBS_ANALYSIS);
                System.out.println(ratio);
                System.out.println(fraction);
                System.out.println(upstream);
                System.out.println(downstream);
                System.out.println(performLeaderless);
                System.out.println(performTSSAnalysis);
                System.out.println(performAntisense);
                System.out.println(performPromotorDetectin);
                System.out.println(performRBSDetection);
            }
            parameterSetFiveprime = new ParameterSetFiveEnrichedAnalyses(performTSSAnalysis, performLeaderless, performAntisense, fraction, ratio, upstream, downstream);
            // Here we parse the genome
            // 1. getting region2Exclude 
            // forwardCDSs and reverseCDSs 
            // => Feature IDs on Position i in genome 3 strands in each direction are maximum.
            // allRegionsInHash => Key Feature id, Value is the featuer 
            double fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
            featureParser = new GenomeFeatureParser(connector);

            fifePrimeAnalysesHandler = new FifeEnrichedDataAnalysesHandler(featureParser, track, referenceId, parameterSetFiveprime, this.refViewer, this.transcAnalysesTopComp, this.trackMap);
            fifePrimeAnalysesHandler.start();
            
//            trackToAnalysisMap.put(track.getId(), new AnalysesContainer(fifePrimeAnalysesHandler.getTssDetection(), fifePrimeAnalysesHandler.getOperonDetection()));
        }
    }

    @Override
    public void showData(Object data) {
        
//        System.out.println("We are in Show data of StartTranscriptomeAnalysesAction!");
//        try {
//            @SuppressWarnings("unchecked")
//            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) data;
//            final int trackId = dataTypePair.getFirst();
//            final String dataType = dataTypePair.getSecond();
//
//            SwingUtilities.invokeLater(new Runnable() { //because it is not called from the swing dispatch thread
//                @Override
//                public void run() {
//
//                    //get track name(s) for tab descriptions
//                    String trackNames;
//                    
//                    if (parameterSetFiveprime.isPerformTSSAnalysis()) {
//
//                        ++finishedAnalyses;
//                        TssDetection analysisTSS = trackToAnalysisMap.get(trackId).getAnalysisTSS();
//                        if (transcriptionStartResultPanel == null) {
//                            transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
//                            transcriptionStartResultPanel.setReferenceViewer(refViewer);
//                        }
//
//                        TSSDetectionResults tssResult = new TSSDetectionResults(analysisTSS.getResults(), trackMap);
//                        transcriptionStartResultPanel.addResult(tssResult);
//
//                        if (finishedAnalyses >= tracks.size()) {
//                            trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
//                            String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getResultSize() + " hits)";
//                            transcAnalysesTopComp.openAnalysisTab(panelName, transcriptionStartResultPanel);
//                        }
//                    }
//                    if (dataType.equals(AnalysesHandler.DATA_TYPE_MAPPINGS)) {
//                        ++finishedMappingAnalyses;
//
//                        if (parametersOperonDet.isPerformOperonAnalysis()) {
//
//                            if (operonResultPanel == null) {
//                                operonResultPanel = new ResultPanelOperonDetection(parametersOperonDet);
//                                operonResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
//                            }
//                            OperonDetectionResult operonDetectionResult = new OperonDetectionResult(trackMap,
//                                    trackToAnalysisMap.get(trackId).getAnalysisOperon().getResults());
//                            operonDetectionResult.setParameters(parametersOperonDet);
//                            operonResultPanel.addResult(operonDetectionResult);
//
//                            if (finishedMappingAnalyses >= tracks.size()) {
//                                trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
//                                String panelName = "Detected operons for " + trackNames + " (" + operonResultPanel.getResultSize() + " hits)";
//                                transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
//                            }
//                        }
//
//                        if (parametersRPKM.isPerformRPKMAnalysis()) {
//                            AnalysisRPKM rpkmAnalysis = trackToAnalysisMap.get(trackId).getAnalysisRPKM();
//                            if (rpkmResultPanel == null) {
//                                rpkmResultPanel = new ResultPanelRPKM();
//                                rpkmResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
//                            }
//                            RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult(trackMap,
//                                    trackToAnalysisMap.get(trackId).getAnalysisRPKM().getResults());
//                            rpkmAnalysisResult.setParameters(parametersRPKM);
//                            rpkmAnalysisResult.setNoGenomeFeatures(rpkmAnalysis.getNoGenomeFeatures());
//                            rpkmResultPanel.addResult(rpkmAnalysisResult);
//
//                            if (finishedMappingAnalyses >= tracks.size()) {
//                                trackNames = GeneralUtils.generateConcatenatedString(rpkmAnalysisResult.getTrackNameList(), 120);
//                                String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getResultSize() + " hits)";
//                                transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
//                            }
//                        }
//                    }
//                }
//            });
//        } catch (ClassCastException e) {
//            //do nothing, we dont handle other data in this class
//        }
    }

    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
    }
}
