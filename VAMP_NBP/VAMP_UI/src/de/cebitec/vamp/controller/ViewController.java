package de.cebitec.vamp.controller;

import de.cebitec.vamp.view.dialogPanels.OpenRefGenPanel;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.api.ApplicationFrameI;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackItem;
import de.cebitec.vamp.view.dialogPanels.OpenTrackPanel;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;

/**
 *
 * @author ddoppmeier
 */
public class ViewController implements de.cebitec.vamp.view.dataVisualisation.MousePositionListener {

    private static ViewController instance;
    private List<MousePositionListener> mousePosListener;
    private BoundsInfoManager boundsManager;
    private BasePanelFactory basePanelFac;

    private PersistantReference currentRefGen;
    private BasePanel genomeViewer;
    private Map<PersistantTrack, BasePanel> trackToPanel;
    private Map<PersistantTrack, TrackItem> trackToItem;

    private ViewController(){
        mousePosListener = new ArrayList<MousePositionListener>();

        trackToPanel = new HashMap<PersistantTrack, BasePanel>();
        trackToItem = new HashMap<PersistantTrack, TrackItem>();
    }

    public ViewController(PersistantReference reference){
        mousePosListener = new ArrayList<MousePositionListener>();

        trackToPanel = new HashMap<PersistantTrack, BasePanel>();
        trackToItem = new HashMap<PersistantTrack, TrackItem>();

        currentRefGen = reference;
        boundsManager = new BoundsInfoManager(currentRefGen);
        basePanelFac = new BasePanelFactory(boundsManager, this);
        genomeViewer = basePanelFac.getGenomeViewerBasePanel(currentRefGen);

        // TODO go on here, need a non sinlgeton topcomponent
        WindowManager.getDefault().findTopComponent("AppPanelTopComponent").open();
        getApp().showRefGenPanel(genomeViewer);
    }

    public static ViewController getInstance(){
        if (instance == null){
            instance = new ViewController();
        }
        return instance;
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
            
            WindowManager.getDefault().findTopComponent("AppPanelTopComponent").open();
            getApp().showRefGenPanel(genomeViewer);
        }
    }

    public void closeRefGen() {
        // remove all tracks that are still open
        List<PersistantTrack> tracks = new ArrayList<PersistantTrack>();
        for(PersistantTrack t : trackToItem.keySet()){
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
        DialogDescriptor dialogDescriptor = new DialogDescriptor(otp, "Open Reference");
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if(dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION) && otp.getSelectedTrack() != null){
            PersistantTrack t = otp.getSelectedTrack();

            // create basepanel 
            BasePanel tp = basePanelFac.getTrackBasePanel(t);

            // create a menuItem to close this track
            TrackItem trackItem = new TrackItem(t);
            trackToItem.put(t, trackItem);
            trackToPanel.put(t, tp);

            // show the panel and the track
            getApp().showTrackPanel(tp, trackItem);
        }
    }

    public void closeTrack(PersistantTrack track) {
        BasePanel trackPanel = trackToPanel.get(track);
        TrackItem trackItem = trackToItem.get(track);

        getApp().closeTrackPanel(trackPanel);
        trackPanel.close();
        mousePosListener.remove(trackPanel);

        trackToItem.remove(track);
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

    public void addMousePositionListener(MousePositionListener listener){
        mousePosListener.add(listener);
    }

    public void removeMousePositionListener(MousePositionListener listener){
        if(mousePosListener.contains(listener)){
            mousePosListener.remove(listener);
        }
    }

    private ApplicationFrameI getApp(){
        return (ApplicationFrameI) WindowManager.getDefault().findTopComponent("AppPanelTopComponent");
    }

}
