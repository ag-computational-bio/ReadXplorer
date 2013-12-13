package de.cebitec.readXplorer.controller;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.api.ApplicationFrameI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManagerFactory;
import de.cebitec.readXplorer.view.dataVisualisation.MousePositionListener;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.readXplorer.view.dialogMenus.OpenRefGenPanel;
import de.cebitec.readXplorer.view.dialogMenus.OpenTracksVisualPanel;
import java.awt.Dialog;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Controls the view for one <code>ApplicationFrameI</code>.
 *
 * @author ddoppmeier
 */
public class ViewController implements de.cebitec.readXplorer.view.dataVisualisation.MousePositionListener {

    private List<MousePositionListener> mousePosListener;
    private BoundsInfoManager boundsManager;
    private BasePanelFactory basePanelFac;

    private PersistantReference currentRefGen;
    private BasePanel genomeViewer;
    private Map<PersistantTrack, BasePanel> trackToPanel;
    private List<BasePanel> currentTracks= new ArrayList<>();
    
    private BoundsInfoManagerFactory boundsInfoManagerFactory;
    
    private ApplicationFrameI app;

    /**
     * Controls the view for one <code>ApplicationFrameI</code>.
     * @param app the <code>ApplicationFrameI</code> interface implementation,
     * for which the view controller is created.
     */
    public ViewController(ApplicationFrameI app){
        this.app = app;

        mousePosListener = new ArrayList<>();

        trackToPanel = new HashMap<>();
        registerInLookup();
        this.boundsInfoManagerFactory = new BoundsInfoManagerFactory();
    }
    
    /**
     * Registers this <code>ViewController</code> in the 
     * <code>CentralLookup</code>.
     */
    private void registerInLookup(){
        CentralLookup.getDefault().add(this);
    }
    
    /**
     * Opens a reference viewer for the given reference genome.
     * @param genome the genome for which a viewer shall be opened.
     */
    public void openGenome(PersistantReference genome) {
        currentRefGen = genome;
        
        boundsManager = this.boundsInfoManagerFactory.get(currentRefGen);
        basePanelFac = new BasePanelFactory(boundsManager, this);
        genomeViewer = basePanelFac.getRefViewerBasePanel(currentRefGen);
        getApp().showRefGenPanel(genomeViewer);
    }
    
    /**
     * Handles the opening of a reference genome viewer. First the list of
     * reference sequences is shown, and after a selection was made, the
     * corresponding reference viewer is opened.
     * @return true, if a reference genome is selected and OK was clicked in the dialog, false otherwise
     */
    public boolean openRefGen() {
        OpenRefGenPanel orgp = new OpenRefGenPanel();
        DialogDescriptor dialogDescriptor = new DialogDescriptor(orgp, "Open Reference");
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        boolean canOpenRefViewer = orgp.getSelectedReference() != null && dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION);
        if (canOpenRefViewer) {
            this.openGenome(orgp.getSelectedReference());
        }
        
        return canOpenRefViewer;
    }

    /**
     * Closes all tracks, which are currently open for the reference viewer of
     * this top component and the reference viewer.
     */
    public void closeRefGen() {

        // unregister from listeners and remove
        genomeViewer.close();
        mousePosListener.remove(genomeViewer);
        getApp().removeRefGenPanel(genomeViewer);
        genomeViewer = null;
        currentRefGen = null;
        basePanelFac = null;
        boundsManager = null;

    }
    
    /**
     * opens the given track on the current genome
     * @param tracks the tracks belonging to the current reference genome
     */
    public void openTracksOnCurrentGenome(Collection<PersistantTrack> tracks) {
        for (PersistantTrack track : tracks) {
            // create basepanel
            BasePanel trackPanel = basePanelFac.getTrackBasePanel(track, currentRefGen);
            if (trackPanel != null) {
                currentTracks.add(trackPanel);
                trackToPanel.put(track, trackPanel);

                // show the panel and the track
                getApp().showTrackPanel(trackPanel);
            }
        }
    }
    
    /**
     * Opens a dialog with all available tracks for the current reference genome.
     * After selecting a track, the associated track viewer is opened.
     */
    public void openTrack() {
        OpenTracksVisualPanel otp = new OpenTracksVisualPanel(currentRefGen.getId());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, "Open Track");
        Dialog openTrackDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openTrackDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()) {
            if (otp.isCombineTracks()) {
                ViewController viewCon = Utilities.actionsGlobalContext().lookup(ViewController.class);
                BasePanelFactory factory = viewCon.getBasePanelFac();
                factory.getMultipleTracksBasePanel(otp.getSelectedTracks(), currentRefGen, otp.isCombineTracks());
            } else {
                this.openTracksOnCurrentGenome(otp.getSelectedTracks());
            }
        } else if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().isEmpty()) {
            String msg = NbBundle.getMessage(ViewController.class, "CTL_OpenTrackInfo",
                    "No track selected. To open a track, at least one track has to be selected.");
            String title = NbBundle.getMessage(ViewController.class, "CTL_OpenTrackInfoTitle", "Info");
            JOptionPane.showMessageDialog(genomeViewer, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Opens a dialog with all available tracks for the current reference
     * genome. After selecting exactly two tracks, the associated double track
     * viewer is opened.
     */
    @Messages({ "DT_OpenTitle=Open Double Track Viewer",
                "DT_ErrorMsg=Please select exactly TWO tracks for the Double Track Viewer!"})
    public void openDoubleTrack() {        
        OpenTracksVisualPanel otp = new OpenTracksVisualPanel(currentRefGen.getId());       
        otp.setCombineTracksEnabled(false);

        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, Bundle.DT_OpenTitle());
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        // check if two tracks were selected
        boolean okSelected = false;
        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTracks().size() == 2) {
            okSelected = true;
        } else if (!(dialogDescriptor.getValue().equals(DialogDescriptor.CANCEL_OPTION)
                || dialogDescriptor.getValue().equals(DialogDescriptor.CLOSED_OPTION))) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.DT_ErrorMsg(),
                    NotifyDescriptor.INFORMATION_MESSAGE));
            this.openDoubleTrack();
        }
        if (okSelected) {
            BasePanelFactory factory = this.getBasePanelFac();
            factory.getMultipleTracksBasePanel(otp.getSelectedTracks(), currentRefGen, false);
        }
    }

    /**
     * @return The list of currently opened track base panels.
     */
    public List<BasePanel> getOpenTracks(){
        return currentTracks ;
    }

    /**
     * Replacement for <code>closeTrack</code> so the application does not need
     * to know <code>PersistantTrack</code> or <code>BasePanel</code>
     *
     * @param track
     */
    public void closeTrackPanel(JPanel track) {
        BasePanel trackPanel = (BasePanel) track;
        currentTracks.clear();
        getApp().closeTrackPanel(trackPanel);
        trackPanel.close();
        mousePosListener.remove(trackPanel);

        trackToPanel.values().remove((BasePanel) track);
    }

    public void openTrack2(BasePanel tp) {
        getApp().showTrackPanel(tp);
        currentTracks.add(tp);
    }

    @Override
    public void setCurrentMousePosition(int logPos) {
        for(MousePositionListener c : mousePosListener){
            c.setCurrentMousePosition(logPos);
        }
    }
    
    @Override
    public void setMouseOverPaintingRequested(boolean requested) {
        for(MousePositionListener c : mousePosListener){
            c.setMouseOverPaintingRequested(requested);
        }
    }

    public BoundsInfoManager getBoundsManager() {
        return boundsManager;
    }

    public PersistantReference getCurrentRefGen() {
        return currentRefGen;
    }

    public boolean hasRefGen(){
        return currentRefGen != null;
    }

    public String getDisplayName(){
        return currentRefGen.getName() + ": " + currentRefGen.getDescription();
    }

    public void addMousePositionListener(MousePositionListener listener){
        mousePosListener.add(listener);
    }

    public void removeMousePositionListener(MousePositionListener listener){
        if(mousePosListener.contains(listener)){
            mousePosListener.remove(listener);
        }
    }

    private ApplicationFrameI getApp(){
        return app;
    }

    public BasePanelFactory getBasePanelFac() {
        return basePanelFac;
    }

}
