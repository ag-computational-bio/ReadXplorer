package de.cebitec.vamp.controller;

import de.cebitec.vamp.ui.dataAdministration.model.DataAdminController;
//import de.cebitec.vamp.ui.dataAdministration.model.GestureListenerI;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.ui.importer.ImporterController;
import de.cebitec.vamp.ui.importer.LoginCookie;
import de.cebitec.vamp.view.ApplicationFrameI;
import de.cebitec.vamp.view.OpenRefGenDialog;
import de.cebitec.vamp.view.OpenTrackDialog;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.windows.WindowManager;

/**
 *
 * @author ddoppmeier
 */
public class ViewController implements LoginCookie, de.cebitec.vamp.view.dataVisualisation.MousePositionListener {

//    private ApplicationFrameI appFrame;
//    private List<GestureListenerI> gestureListeners;
    private List<MousePositionListener> mousePosListener;
    private BoundsInfoManager boundsManager;
    private BasePanelFactory basePanelFac;

    private PersistantReference currentRefGen;
    private BasePanel genomeViewer;
    private Map<PersistantTrack, BasePanel> trackToPanel;
    private Map<PersistantTrack, TrackItem> trackToItem;

    public ViewController(){
//        appFrame = new ApplicationFrame();
//        appFrame.setViewController(this);
        mousePosListener = new ArrayList<MousePositionListener>();

//        gestureListeners = new ArrayList<GestureListenerI>();
        trackToPanel = new HashMap<PersistantTrack, BasePanel>();
        trackToItem = new HashMap<PersistantTrack, TrackItem>();
    }

//    public void addGestureListener(GestureListenerI gestureListener){
//        gestureListeners.add(gestureListener);
//    }

    public void showApplicationFrame(boolean show){
//        appFrame.setVisible(show);
    }

    @Override
    public void openImporterDialog(){
        new ImporterController().setupNewImport();
    }

    @Override
    public void openDataAdminDialog(){
        new DataAdminController().showDataAdministration();
    }

//    public void blockControlsByRunningTasks(List<RunningTaskI> tasks) {
//        appFrame.releaseButtons();
//        for(RunningTaskI r : tasks){
//            appFrame.blockControlsByRunningTask(r);
//        }
//    }

//    public void logoff() {
//        for(GestureListenerI l : gestureListeners){
//            l.logOff();
//        }
//    }

//    public void shutDownApplication() {
//        for(GestureListenerI l : gestureListeners){
//            l.shutDownApplication();
//        }
//    }

    @Override
    public void openRefGen(){
        OpenRefGenDialog d = new OpenRefGenDialog(WindowManager.getDefault().getMainWindow(), true);
        d.setVisible(true);

        if(d.refgenWasSelected()){
            currentRefGen = d.getSelectedRefGen();
            boundsManager = new BoundsInfoManager(currentRefGen);
            basePanelFac = new BasePanelFactory(boundsManager, this);
            genomeViewer = basePanelFac.getGenomeViewerBasePanel(currentRefGen);
            getApp().showRefGenPanel(genomeViewer);
        }
    }

    @Override
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

    @Override
    public void openTrack() {
        OpenTrackDialog d = new OpenTrackDialog(WindowManager.getDefault().getMainWindow(), true, currentRefGen);
        d.setVisible(true);

        if(d.wasTrackSelected()){
            PersistantTrack t = d.getSelectedTrack();

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

        getApp().closeTrackPanel(trackPanel, trackItem);
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
