package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import de.cebitec.vamp.view.dialogMenus.SelectFeatureTypeWizardPanel;
import de.cebitec.vamp.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Action for opening a new SNP and DIP detection. It opens a track list
 * containing all tracks of the selected reference and creates a new snp
 * detection setup top component when tracks were selected.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(category = "Tools",
id = "de.cebitec.vamp.tools.snp.OpenSnpDetectionAction")
@ActionRegistration(iconBase = "de/cebitec/vamp/tools/snp/snpDetection.png",
displayName = "#CTL_OpenSNPDetection")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 125),
    @ActionReference(path = "Toolbars/Tools", position = 100)
})
@Messages("CTL_OpenSnpDetectionAction=OpenSnpDetectionAction")
public final class OpenSnpDetectionAction implements ActionListener, DataVisualisationI {

    private static final long serialVersionUID = 1L;
    private static final String PROP_WIZARD_NAME = "SNP_Wizard";
    
    private final ReferenceViewer context;
    
    private int referenceId;
    private List<PersistantTrack> tracks;
    private Map<Integer, PersistantTrack> trackMap;
    private SNP_DetectionTopComponent snpDetectionTopComp;
    private SelectReadClassWizardPanel readClassWizPanel;
    private SelectFeatureTypeWizardPanel featureTypePanel;
    private Set<FeatureType> selFeatureTypes;
    private ParameterSetSNPs parametersSNPs;
    private Map<Integer, AnalysisSNPs> trackToAnalysisMap;
    
    private int finishedCovAnalyses = 0;
    private SNP_DetectionResultPanel snpDetectionResultPanel;

    /**
     * Action for opening a new snp detection. It opens a track list containing
     * all tracks of the selected reference and creates a new snp detection
     * setup top component when tracks were selected.
     */
    public OpenSnpDetectionAction(ReferenceViewer context) {
        this.context = context;
        this.referenceId = this.context.getReference().getId();
    }

    /**
     * Carries out the calculations for a complete SNP detection + opening the
     * corresponding TopComponent.
     * @param ev the event itself, which is not used currently
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        
        this.finishedCovAnalyses = 0;
        this.trackToAnalysisMap = new HashMap<>();
        
        //show track list
        OpenTrackPanelList otp = new OpenTrackPanelList(referenceId);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenTrackList"));
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            this.tracks = new ArrayList<>();
            this.trackMap = new HashMap<>();
            this.tracks = otp.getSelectedTracks();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                this.trackMap.put(track.getId(), track);
            }
            
            this.snpDetectionTopComp = (SNP_DetectionTopComponent) WindowManager.getDefault().findTopComponent("SNP_DetectionTopComponent");
            this.snpDetectionTopComp.open();
            this.runWizardAndSnpDetection();

        } else if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().isEmpty()) {
            String msg = NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenSNPDetectionInfo", 
                    "No track selected. To start a SNP detection at least one track has to be selected.");
            String title = NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenSNPDetectionInfoTitle", "Info");
            JOptionPane.showMessageDialog(this.context, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

    }
    
    /**
     * Initializes the setup wizard for the snp detection
     * @param trackIds the list of track ids for which the snp detection has to
     * be carried out
     */
    private void runWizardAndSnpDetection() {
        
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        this.readClassWizPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
        this.featureTypePanel = new SelectFeatureTypeWizardPanel(PROP_WIZARD_NAME);
        boolean containsDBTrack = PersistantTrack.checkForDBTrack(this.tracks);
        this.readClassWizPanel.getComponent().setUsingDBTrack(containsDBTrack);
        panels.add(new SNPWizardPanel());
        panels.add(readClassWizPanel);
        panels.add(featureTypePanel);
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(OpenSnpDetectionAction.class, "TTL_SNPWizardTitle"));
        
        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            this.startSNPDetection(wiz);
        } else {
            if (!this.snpDetectionTopComp.hasComponents()) {
                this.snpDetectionTopComp.close();
            }
        }
    }
    
    /**
     * Starts the SNP detection.
     * @param wiz the wizard containing the SNP detection parameters
     */
    @SuppressWarnings("unchecked")
    private void startSNPDetection(final WizardDescriptor wiz) {
        int minVaryingBases = (int) wiz.getProperty(SNPWizardPanel.PROP_MIN_VARYING_BASES);
        int minPercentage = (int) wiz.getProperty(SNPWizardPanel.PROP_MIN_PERCENT);
        boolean useMainBase = (boolean) wiz.getProperty(SNPWizardPanel.PROP_USE_MAIN_BASE);
        this.selFeatureTypes = (Set<FeatureType>) wiz.getProperty(featureTypePanel.getPropSelectedFeatTypes());
        ParametersReadClasses readClassParams = (ParametersReadClasses) wiz.getProperty(readClassWizPanel.getPropReadClassParams());
        
        this.parametersSNPs = new ParameterSetSNPs(minVaryingBases, minPercentage, useMainBase, selFeatureTypes, readClassParams);
        TrackConnector connector;
        for (PersistantTrack track : tracks) {
            try {
                connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The SNP analysis will be cancelled now.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }
            
            AnalysesHandler snpAnalysisHandler = connector.createAnalysisHandler(this,
                    NbBundle.getMessage(OpenSnpDetectionAction.class, "MSG_AnalysesWorker.progress.name"), readClassParams); //every track has its own analysis handlers
            AnalysisSNPs analysisSNPs = new AnalysisSNPs(connector, parametersSNPs);
            snpAnalysisHandler.registerObserver(analysisSNPs);
            snpAnalysisHandler.setCoverageNeeded(true);
            snpAnalysisHandler.setDiffsAndGapsNeeded(true);
            trackToAnalysisMap.put(track.getId(), analysisSNPs);
            snpAnalysisHandler.startAnalysis();
        }
    }

    @Override
    public void showData(Object dataTypeObject) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            int trackId = dataTypePair.getFirst();
            String dataType = dataTypePair.getSecond();

            if (dataType.equals(AnalysesHandler.DATA_TYPE_COVERAGE)) {

                ++finishedCovAnalyses;

                AnalysisSNPs analysisSNPs = trackToAnalysisMap.get(trackId);
                SnpDetectionResult result = new SnpDetectionResult(analysisSNPs.getResults(), trackMap);
                result.setParameters(parametersSNPs);

                if (snpDetectionResultPanel == null) {
                    snpDetectionResultPanel = new SNP_DetectionResultPanel();
                    snpDetectionResultPanel.setBoundsInfoManager(this.context.getBoundsInformationManager());
                }
                snpDetectionResultPanel.setReferenceGenome(this.context.getReference());
                snpDetectionResultPanel.addSNPs(result);

                if (finishedCovAnalyses >= tracks.size()) {

                    //get track name(s) for tab descriptions
                    String trackNames = GeneralUtils.generateConcatenatedString(result.getTrackNameList(), 120);
                    String panelName = "SNP Detection for " + trackNames + " (" + snpDetectionResultPanel.getSnpDataSize() + " hits)";
                    this.snpDetectionTopComp.openDetectionTab(panelName, snpDetectionResultPanel);
                }
            }

        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }
    }

}
