package de.cebitec.vamp.ui.visualisation;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.api.ApplicationFrameI;
import de.cebitec.vamp.api.cookies.CloseRefGenCookie;
import de.cebitec.vamp.api.cookies.CloseTrackCookie;
import de.cebitec.vamp.api.cookies.OpenTrackCookie;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.MultipleTrackViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.view//AppPanel//EN", autostore = false)
public final class AppPanelTopComponent extends TopComponent implements ApplicationFrameI {

    private static AppPanelTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AppPanelTopComponent";
    private static final long serialVersionUID = 1L;
    private InstanceContent content = new InstanceContent();
    private Lookup localLookup;
    private ReferenceViewer referenceViewer;
    private ArrayList<TrackViewer> trackViewerList;

    public AppPanelTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(AppPanelTopComponent.class, "CTL_AppPanelTopComponent"));
        setToolTipText(NbBundle.getMessage(AppPanelTopComponent.class, "HINT_AppPanelTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        localLookup = new AbstractLookup(content);
        associateLookup(localLookup);
        this.trackViewerList = new ArrayList<TrackViewer>();
    }

    private void clearLookup() {
        Collection<? extends Object> allCookies = getLookup().lookupAll(Object.class);

        for (Object cookie : allCookies) {
            content.remove(cookie);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        visualPanel = new javax.swing.JPanel();

        visualPanel.setLayout(new javax.swing.BoxLayout(visualPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(visualPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel visualPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * @return default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized AppPanelTopComponent getDefault() {
        if (instance == null) {
            instance = new AppPanelTopComponent();
        }
        return instance;
    }

    /**
     * @return Obtain the AppPanelTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized AppPanelTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(AppPanelTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof AppPanelTopComponent) {
            return (AppPanelTopComponent) win;
        }
        Logger.getLogger(AppPanelTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        ViewController vc = new ViewController(this);
        vc.openRefGen();
        if (vc.hasRefGen()) {
            setName(vc.getDisplayName());
            content.add(vc);
        } else {
            this.close();
        }
    }

    @Override
    public void componentClosed() {
        // remove all viewers
        Component[] comps = visualPanel.getComponents();
        for (Component comp : comps){
            if (comp instanceof BasePanel) {
                try {
                    ((BasePanel) comp).getViewer().close();
                } catch (NullPointerException e) {
                    //do nothing, we want to close something, which is already destroyed
                }
            }
        }
        visualPanel.removeAll();

        // remove all cookies
        clearLookup();

        // if last Viewer close Navigator
        boolean lastViewer = true;
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof ApplicationFrameI && !tc.equals(this)) {
                lastViewer = false;
                break;
            }
        }
        if (lastViewer) {
            WindowManager.getDefault().findTopComponent("ReferenceNavigatorTopComponent").close();
            WindowManager.getDefault().findTopComponent("ReferenceIntervalTopComponent").close();
            WindowManager.getDefault().findTopComponent("ReferenceFeatureTopComponent").close();
            WindowManager.getDefault().findTopComponent("TrackStatisticsTopComponent").close();
             WindowManager.getDefault().findTopComponent("TrackNormalizationTopComponent").close();
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public Lookup getLookup() {
        return localLookup;
    }

    // ===================== AppFrameI stuff ============================== //
    @Override
    public void showRefGenPanel(JPanel refGenPanel) {
        visualPanel.add(refGenPanel);
        visualPanel.updateUI();

        WindowManager.getDefault().findTopComponent("ReferenceNavigatorTopComponent").open();
        WindowManager.getDefault().findTopComponent("ReferenceIntervalTopComponent").open();
        WindowManager.getDefault().findTopComponent("ReferenceFeatureTopComponent").open();

        // put the panels ReferenceViewer in lookup so it can be accessed
        BasePanel bp = (BasePanel) refGenPanel;
        ReferenceViewer rv = (ReferenceViewer) bp.getViewer();
        content.add(rv);
        this.referenceViewer = rv;

        content.add(new CloseRefGenCookie() {

            @Override
            public boolean close() {
                getLookup().lookup(ViewController.class).closeRefGen();
                content.remove(this);
                return true;
            }
        });
        content.add(new OpenTrackCookie() {

            @Override
            public void open() {
                getLookup().lookup(ViewController.class).openTrack();
            }
        });
    }

    @Override
    public void removeRefGenPanel(JPanel genomeViewer) {
        this.close();
    }

    @Override
    public void showTrackPanel(final JPanel trackPanel) {
        // add the trackPanel
        visualPanel.add(trackPanel);
        visualPanel.updateUI();
        this.referenceViewer.increaseTrackCount();

        // search for opened tracks, if there are none open the track statistics window
        if (getLookup().lookupAll(TrackViewer.class).isEmpty()) {
            WindowManager.getDefault().findTopComponent("TrackStatisticsTopComponent").open();
             WindowManager.getDefault().findTopComponent("TrackNormalizationTopComponent").open();
        }

        // put the panel's TrackViewer in lookup so it can be accessed
        BasePanel basePanel = (BasePanel) trackPanel;
        // make sure the multiple track viewers do not cause a mess in the lookup
        if (!(basePanel.getViewer() instanceof MultipleTrackViewer)) {
            TrackViewer trackViewer = (TrackViewer) basePanel.getViewer();
            this.content.add(trackViewer);
            this.trackViewerList.add(trackViewer);
        }

        CloseTrackCookie closeTrackCookie = new CloseTrackCookie() {

            @Override
            public boolean close() {
                getLookup().lookup(ViewController.class).closeTrackPanel(trackPanel);
                content.remove(this);
                referenceViewer.decreaseTrackCount();
                return true;
            }

            @Override
            public String getName() {
                return trackPanel.getName();
            }
        };

        content.add(closeTrackCookie);
//        all.add(new WeakReference<JPanel>(trackPanel));
    }

    @Override
    public void closeTrackPanel(JPanel trackPanel) {
        visualPanel.remove(trackPanel);
        visualPanel.updateUI();

        // remove the panel's TrackViewer from lookup
        BasePanel bp = (BasePanel) trackPanel;
        TrackViewer tv = (TrackViewer) bp.getViewer();
        content.remove(tv);

        // if this was the last trackPanel close the track statistics window
        if (getLookup().lookupAll(TrackViewer.class).isEmpty()) {
            WindowManager.getDefault().findTopComponent("TrackStatisticsTopComponent").close();
            WindowManager.getDefault().findTopComponent("TrackNormalizationTopComponent").close();
        }
    }
    
    /*
     * Overriding these two methods ensures that only displayed components are updated
     * and thus increases performance of the viewers.
     */
    @Override
    public void componentShowing() {
        if (referenceViewer != null) {
            this.referenceViewer.setActive(true);
            this.changeActiveTrackStatus(true);
        }
    }

    @Override
    public void componentHidden() {
        if (referenceViewer != null) {
            this.referenceViewer.setActive(false);
            this.changeActiveTrackStatus(false);
        }
    }

    /**
     * Updates the status of all track viewers belonging to this top component.
     * @param isActive true, if track viewers should be active, false, if not.
     */
    private void changeActiveTrackStatus(boolean isActive) {
        for (TrackViewer viewer : this.trackViewerList) {
            viewer.setActive(isActive);
        }
    }
    
    
    // ==================== Experimental track closing stuff ==================== //
    private List<Reference<JPanel>> all = Collections.synchronizedList(new ArrayList<Reference<JPanel>>());

    public List<Action> allTrackCloseActions() {
        List<Action> result = new ArrayList<Action>();
        for (Iterator<Reference<JPanel>> it = all.iterator(); it.hasNext();) {
            Reference<JPanel> cookieRef = it.next();
            JPanel cookie = cookieRef.get();
            if (cookie == null) {
                it.remove();
            } else {
                result.add(new ShowAction(cookie.getName(), cookieRef, new WeakReference<ViewController>(getLookup().lookup(ViewController.class))));
            }
        }
        return result;
    }

    private static final class ShowAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        //Our action should not hold a strong reference to the TopComponent -
        //if it is closed, it should get garbage collected.  If a menu
        //item holds a reference to the component, then it won't be
        private final Reference<JPanel> tc;
        private final Reference<ViewController> vc;

        ShowAction(String name, Reference<JPanel> tc, Reference<ViewController> vc) {
            this.tc = tc;
            this.vc = vc;
            putValue(NAME, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel comp = tc.get();
            if (comp != null) { //Could have been garbage collected
                vc.get().closeTrackPanel(comp);
            } else {
                //will almost never happen
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public boolean isEnabled() {
            JPanel comp = tc.get();
            return comp != null;
        }
    }
}
