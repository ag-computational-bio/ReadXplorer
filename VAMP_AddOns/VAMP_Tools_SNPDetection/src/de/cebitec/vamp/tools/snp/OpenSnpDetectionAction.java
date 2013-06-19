package de.cebitec.vamp.tools.snp;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.dataObjects.SnpI;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import de.cebitec.vamp.view.dialogMenus.SelectFeatureTypeWizardPanel;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
 * Action for opening a new snp detection. It opens a track list containing all
 * tracks of the selected reference and creates a new snp detection setup top component
 * when tracks were selected.
 * 
 * @author rhilker
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
public final class OpenSnpDetectionAction implements ActionListener, Observer {

    private final ReferenceViewer context;
    private static final long serialVersionUID = 1L;
    
    private ProjectConnector proCon;
    private SnpDetectionResult snpData;
    private int referenceId;
    private List<Integer> trackIds;
    private Map<Integer, PersistantTrack> trackList;
    private SNP_DetectionTopComponent snpDetectionTopComp;
    private SelectFeatureTypeWizardPanel featureTypePanel;
    private Set<FeatureType> selFeatureTypes;

    /**
     * Action for opening a new snp detection. It opens a track list containing
     * all tracks of the selected reference and creates a new snp detection
     * setup top component when tracks were selected.
     */
    public OpenSnpDetectionAction(ReferenceViewer context) {
        this.context = context;
        this.proCon = ProjectConnector.getInstance();
        this.referenceId = this.context.getReference().getId();
    }

    /**
     * Carries out the calculations for a complete SNP detection + opening the
     * corresponding TopComponent.
     * @param ev the event itself, which is not used currently
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        
        //show track list
        OpenTrackPanelList otp = new OpenTrackPanelList(referenceId);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, NbBundle.getMessage(OpenSnpDetectionAction.class, "CTL_OpenTrackList"));
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            this.trackIds = new ArrayList<>();
            this.trackList = new HashMap<>();
            for (PersistantTrack track : otp.getSelectedTracks()) {
                this.trackIds.add(track.getId());
                this.trackList.put(track.getId(), track);
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
        featureTypePanel = new SelectFeatureTypeWizardPanel("SNP_Wizard");
        panels.add(new SNPWizardPanel());
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
            this.snpDetectionTopComp.close();
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
        this.selFeatureTypes = (Set<FeatureType>) wiz.getProperty(featureTypePanel.getPropSelectedFeatTypes());

        SnpThread snpThread = new SnpThread(minPercentage, minVaryingBases);
        snpThread.registerObserver(this);
        snpThread.start();
    }

    @Override
    public void update(Object args) {
        snpDetectionTopComp.openDetectionTab(context, snpData);
    }
    
    /**
     * The thread carrying out the lengthy SNP detection itself.
     */
    private class SnpThread extends Thread implements Observable {

        private int percent;
        private int num;
        private ProgressHandle ph;
        private List<Observer> observers;

        /**
         * The thread carrying out the lengthy SNP detection itself.
         * @param percent 
         * @param num
         */
        SnpThread(int percent, int num) {
            this.observers = new ArrayList<>();
            this.percent = percent;
            this.num = num;
            this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(OpenSnpDetectionAction.SnpThread.class, "MSG_SNPWorker.progress.name"));
        }

        @Override
        public void run() {
            CentralLookup.getDefault().add(this);

            ph.start();
            List<SnpI> snps = proCon.findSNPs(percent, num, trackIds);
            snpData = new SnpDetectionResult(snps, trackList, selFeatureTypes);
            snpData.setSearchParameters(percent, num);
            CentralLookup.getDefault().remove(this);
            this.notifyObservers(snpData);
            ph.finish();
        }

        @Override
        public void registerObserver(Observer observer) {
            this.observers.add(observer);
        }

        @Override
        public void removeObserver(Observer observer) {
            this.observers.remove(observer);
        }

        @Override
        public void notifyObservers(Object data) {
            for (Observer observer : this.observers) {
                observer.update(data);
            }
        }

    }
    
}
