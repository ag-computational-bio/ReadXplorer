package de.cebitec.vamp.controller;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.api.ApplicationFrameI;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.vamp.view.dialogPanels.OpenRefGenPanel;
import de.cebitec.vamp.view.dialogPanels.OpenTrackPanel;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Controls the view for one <code>ApplicationFrameI</code>
 *
 * @author ddoppmeier
 */
public class ViewController implements de.cebitec.vamp.view.dataVisualisation.MousePositionListener {

    private List<MousePositionListener> mousePosListener;
    private BoundsInfoManager boundsManager;
    private BasePanelFactory basePanelFac;

    private PersistantReference currentRefGen;
    private BasePanel genomeViewer;
    private Map<PersistantTrack, BasePanel> trackToPanel;

    private ApplicationFrameI app;

    public ViewController(ApplicationFrameI app){
        this.app = app;

        mousePosListener = new ArrayList<MousePositionListener>();

        trackToPanel = new HashMap<PersistantTrack, BasePanel>();
    }

    public void openRefGen(){
        OpenRefGenPanel orgp = new OpenRefGenPanel();
        DialogDescriptor dialogDescriptor = new DialogDescriptor(orgp, "Open Reference");
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if(dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && orgp.getSelectedReference() != null){
            currentRefGen = orgp.getSelectedReference();
            boundsManager = new BoundsInfoManager(currentRefGen);
            basePanelFac = new BasePanelFactory(boundsManager, this);
            genomeViewer = basePanelFac.getGenomeViewerBasePanel(currentRefGen);

            getApp().showRefGenPanel(genomeViewer);
        }
    }

    public void closeRefGen() {
        // remove all tracks that are still open
        List<PersistantTrack> tracks = new ArrayList<PersistantTrack>();
        for(PersistantTrack t : trackToPanel.keySet()){
            tracks.add(t);
        }
        for(Iterator<PersistantTrack> it = tracks.iterator(); it.hasNext(); ){
            PersistantTrack t = it.next();
            closeTrack(t);
        }

        // unregister from listeners and remove
        genomeViewer.close();
        mousePosListener.remove(genomeViewer);
        getApp().removeRefGenPanel(genomeViewer);
        genomeViewer = null;
        currentRefGen = null;
        basePanelFac = null;
        boundsManager = null;

    }

    public void openTrack() {
        OpenTrackPanel otp = new OpenTrackPanel(currentRefGen.getId());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, "Open Track");
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if(dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && !otp.getSelectedTracks().isEmpty()){
            for (PersistantTrack track : otp.getSelectedTracks()) {
                // create basepanel
                BasePanel trackPanel = basePanelFac.getTrackBasePanel(track, currentRefGen);
                trackToPanel.put(track, trackPanel);

                // show the panel and the track
                getApp().showTrackPanel(trackPanel);
            }
        }
    }

    /**
     * Replacement for <code>closeTrack</code> so the application does not need
     * to know <code>PersistantTrack</code> or <code>BasePanel</code>
     *
     * @param track
     */
    public void closeTrackPanel(JPanel track) {
        BasePanel trackPanel = (BasePanel) track;

        getApp().closeTrackPanel(trackPanel);
        trackPanel.close();
        mousePosListener.remove(trackPanel);

        trackToPanel.values().remove((BasePanel) track);
    }

    public void openTrack2(BasePanel tp) {
        getApp().showTrackPanel(tp);
    }

    @Deprecated
    public void closeTrack(PersistantTrack track) {
        BasePanel trackPanel = trackToPanel.get(track);

        getApp().closeTrackPanel(trackPanel);
        trackPanel.close();
        mousePosListener.remove(trackPanel);

        trackToPanel.remove(track);
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
        return currentRefGen != null ? Boolean.TRUE : Boolean.FALSE;
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
