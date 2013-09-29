/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.wizard.TranscriptomeAnalysisWizardIterator;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "de.cebitec.vamp.transcriptomeAnalyses.StartTranscriptomeAnalysesAction")
@ActionRegistration(
        displayName = "#CTL_StartTranscriptomeAnalysesAction")
@ActionReference(path = "Menu/Tools", position = 112)
@Messages("CTL_StartTranscriptomeAnalysesAction=Start Transcriptome Analyses")
public final class StartTranscriptomeAnalysesAction implements ActionListener, DataVisualisationI {

    private final ReferenceViewer refViewer;
    private List<PersistantTrack> tracks;
    private Map<Integer, PersistantTrack> trackMap;
    private String readClassPropString;
    private String selFeatureTypesPropString;
    private GenomeFeatureParser featureParser;
    private Statistics stats;
    private int referenceId;
    private double backgroundCutoff;
    FifeEnrichedDataAnalysesHandler fifePrimeAnalysesHandler;
    ParameterSetFiveEnrichedAnalyses parameterSetFiveprime;
    
    private boolean performFivePrimeAnalyses, performTSSAnalysis, performLeaderless, performAntisense, performRBSDetection, performPromotorDetectin;
    private double fraction;
    private int upstream, downstream;
    public StartTranscriptomeAnalysesAction(ReferenceViewer reference) {
        this.refViewer = reference;
        this.referenceId = this.refViewer.getReference().getId();
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
                fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
                upstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_UPSTREAM);
                downstream = (int) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_DOWNSTREAM);
                    }
            parameterSetFiveprime = new ParameterSetFiveEnrichedAnalyses(performTSSAnalysis, false, false, fraction, upstream, downstream);
            // Here we parse the genome
            // 1. getting region2Exclude 
            // forwardCDSs and reverseCDSs 
            // => Feature IDs on Position i in genome 3 strands in each direction are maximum.
            // allRegionsInHash => Key Feature id, Value is the featuer 
            double fraction = (double) wiz.getProperty(TranscriptomeAnalysisWizardIterator.PROP_Fraction);
            featureParser = new GenomeFeatureParser(connector);
            
            fifePrimeAnalysesHandler = new FifeEnrichedDataAnalysesHandler(featureParser, track, referenceId, parameterSetFiveprime);
            fifePrimeAnalysesHandler.run();
        }
    }

    @Override
    public void showData(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
    }
}
