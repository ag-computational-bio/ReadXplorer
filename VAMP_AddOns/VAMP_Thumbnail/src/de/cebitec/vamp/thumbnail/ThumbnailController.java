package de.cebitec.vamp.thumbnail;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.thumbnail.Actions.ASyncSliderCookie;
import de.cebitec.vamp.thumbnail.Actions.OpenThumbCookie;
import de.cebitec.vamp.thumbnail.Actions.RemoveCookie;
import de.cebitec.vamp.thumbnail.Actions.SyncSliderCookie;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.ui.visualisation.reference.ReferenceAnnotationTopComp;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.IThumbnailView;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageInfoLabel;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.MultipleTrackViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * ServiceProvider for IThumbnailView.
 * This Module can display all Tracks for a given List of Annotations in a Thumbnail-like View.
 *
 * @author denis
 */
@ServiceProvider(service = IThumbnailView.class)
public class ThumbnailController extends MouseAdapter implements IThumbnailView, Lookup.Provider {

    private HashMap<ReferenceViewer, ThumbNailViewTopComponent> refThumbTopComponents;
    //Currently active ThumbnailTopComponent and ReferenceViewer
    private ThumbNailViewTopComponent activeTopComp;
    private ReferenceViewer activeViewer;
    //Gives access to all Annotations which are displayed for a referenceViewer
    private HashMap<ReferenceViewer, List<PersistantAnnotation>> selectedAnnotations;
    //Gives access to all BasePanels for Annotation
    private HashMap<PersistantAnnotation, List<BasePanel>> annotationToTrackpanelList;
    //Gives access to PersistantTrack from BasePanel
    private HashMap<BasePanel, PersistantTrack> trackPanelToTrack;
    //Gives access to LayoutWidget for currentAnnotation
    private HashMap<PersistantAnnotation, Widget> annotationToLayoutWidget;
    private PersistantAnnotation currentAnnotation;
    private ViewController controller;
    private InstanceContent content;
    //Controller of ThumbnailController
    private ThumbControllerLookup controllerLookup;
    //is true if SliderValues get calculated on creation of TrackPanel
    private boolean autoSlider = true;

    public ThumbnailController() {
        this.refThumbTopComponents = new HashMap<ReferenceViewer, ThumbNailViewTopComponent>();
        this.selectedAnnotations = new HashMap<ReferenceViewer, List<PersistantAnnotation>>();
        this.annotationToTrackpanelList = new HashMap<PersistantAnnotation, List<BasePanel>>();
        this.trackPanelToTrack = new HashMap<BasePanel, PersistantTrack>();
        this.annotationToLayoutWidget = new HashMap<PersistantAnnotation, Widget>();

        content = new InstanceContent();
        controllerLookup = new ThumbControllerLookup(content);
    }

    @Override
    public void showThumbnailView(ReferenceViewer refViewer) {
        activeViewer = refViewer;
        if (refThumbTopComponents.containsKey(activeViewer)) {
            activeTopComp = refThumbTopComponents.get(activeViewer);
        } else {
            activeTopComp = new ThumbNailViewTopComponent();
            refThumbTopComponents.put(activeViewer, activeTopComp);
        }
        activeTopComp.setName("ThumbnailReference: " + refViewer.getReference().getName());

        activeTopComp.open();
        Scene scene = activeTopComp.getScene();
        scene.removeChildren();
        scene.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        //Get ViewController
        Result<ViewController> viewControlResult = Utilities.actionsGlobalContext().lookupResult(ViewController.class);
        controller = viewControlResult.allInstances().iterator().next();

        //After Lookup-stuff is done requestActive for ThumbnailTopComponent
        activeTopComp.requestActive();
        //Build scene
        if (selectedAnnotations.containsKey(activeViewer) && selectedAnnotations.get(activeViewer).size() > 40) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_TooManyAnnotations"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            drawScene();
            //Cookie Stuff
            removeThumbSpecificCookies();
            addSynchCookieToLookup();
        }
    }

    /**
     * Creates ThumbnailView with given ViewController.Is called from JumpPanel.
     * @param refViewer
     * @param con
     */
    @Override
    public void showThumbnailView(ReferenceViewer refViewer, ViewController con) {
        activeViewer = refViewer;
        if (refThumbTopComponents.containsKey(refViewer)) {
            activeTopComp = refThumbTopComponents.get(refViewer);
        } else {
            activeTopComp = new ThumbNailViewTopComponent();
            refThumbTopComponents.put(activeViewer, activeTopComp);
        }
        activeTopComp.open();
        Scene scene = activeTopComp.getScene();
        scene.removeChildren();
        scene.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        controller = con;
        activeTopComp.requestActive();
        //Build scene
        if (selectedAnnotations.containsKey(activeViewer) && selectedAnnotations.get(activeViewer).size() > 40) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_TooManyAnnotations"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            drawScene();
            removeThumbSpecificCookies();
            //Activate Synchronize-Action for ZoomSliders
            addSynchCookieToLookup();
        }

    }

    /**
     * Activates synchronize-Sliders Action in Menu.
     */
    private void addSynchCookieToLookup() {
        getLookup().add(new SyncSliderCookie() {

            @Override
            public void syncSliders() {
                sliderSynchronisation(true);
                //Sliders a synchronized now so remove old cookie and add a new one so that they can be asynchronized again.
                getLookup().removeAll(SyncSliderCookie.class);
                addASynchCookieToLookup();
            }
        });
    }

    /**
     * Activates don't-synchronize-Sliders Action in Menu.
     */
    private void addASynchCookieToLookup() {
        getLookup().add(new ASyncSliderCookie() {

            @Override
            public void async() {
                sliderSynchronisation(false);
                getLookup().removeAll(ASyncSliderCookie.class);
                addSynchCookieToLookup();
            }
        });
    }

    /**
     * Sets all Sliders based on synch-Value
     * @param synch Is Set through Cookie-Actions to specify if VerticalSliders should be synchronized.
     */
    private void sliderSynchronisation(boolean synch) {
        //synchronize all Sliders for all RefrenceViewer's ThumbnailViewTopComponents
        for (ReferenceViewer oneViewer : refThumbTopComponents.keySet()) {
            if (selectedAnnotations.containsKey(oneViewer)) {
                for (PersistantAnnotation annotation : selectedAnnotations.get(oneViewer)) {
                    ZoomChangeListener zoomChangeListener = new ZoomChangeListener();
                    for (BasePanel bp : annotationToTrackpanelList.get(annotation)) {
                        try {
                            JPanel panel = (JPanel) bp.getComponent(0);
                            if (panel != null) {
                                CoverageZoomSlider slider = (CoverageZoomSlider) panel.getComponent(1);
                                if (synch) {
                                    slider.addChangeListener(zoomChangeListener);
                                    zoomChangeListener.addMapValue((TrackViewer) panel.getComponent(0), slider);
                                } else {
                                    while (slider.getChangeListeners().length > 1) {
                                        slider.removeChangeListener(slider.getChangeListeners()[0]);
                                    }
                                }
                            }
                        } catch (ClassCastException e) {
                            Logger.getLogger(ThumbnailController.class.getName()).log(
                                    Level.WARNING, e.getMessage());
                        }
                    }
                }
            }
        }
    }


    /*
     * Draws all Thumbnail-Widgets for all annotations
     */
    private void drawScene() {
        //Get all associated Tracks for Reference
        ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
        if (activeViewer != null && selectedAnnotations.containsKey(activeViewer)) {
            for (PersistantAnnotation annotation : selectedAnnotations.get(activeViewer)) {
                this.addAnnotationToView(annotation, refCon);
            }
            if (!(WindowManager.getDefault().getRegistry().getActivated() == activeTopComp)) {
                activeTopComp.requestAttention(true);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_NoAnnotations"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            activeTopComp.close();
            refThumbTopComponents.remove(activeViewer);
        }
    }

    /*
     * Creates BasePanel for one Track with TrackViewer and ZoomSlider for wrapping into ComponentWidget.
     */
    private BasePanel createTrackPanel(PersistantTrack track, ViewController controller, CheckBoxActionListener cbListener) {
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel b = new BasePanel(boundsManager, controller);
        b.setName(track.getDescription());
        controller.addMousePositionListener(b);

        // create track viewer
        MultiTrackConnector tc = new MultiTrackConnector(track);

        final TrackViewer trackV = new TrackViewer(boundsManager, b, controller.getCurrentRefGen(), tc, false);
        int annotationWidth = (currentAnnotation.getStop() - currentAnnotation.getStart()) / 2;
        trackV.getTrackCon().getCoverageThread().setCoveredWidth(annotationWidth);

        trackV.setName(track.getDescription());

        CoverageInfoLabel cil = new CoverageInfoLabel();
        trackV.setTrackInfoPanel(cil);

        //eigener ComponentListener für TrackV
        trackV.addComponentListener(new TrackViewerCompListener(currentAnnotation, trackV));


        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        //Set initial Slider-value based on Coverage if autoSlider is true
        if (autoSlider) {
            HashMap<Integer, Integer> cov = tc.getCoverageInfosOfTrack(currentAnnotation.getStart(), currentAnnotation.getStop());
            int max = 0;
            int cnt = 0;
            int avg = 0;
            for (Integer object : cov.values()) {
                cnt++;
                max += object;
            }
            if (cnt > 0) {
                avg = max / cnt;
            }
            if (avg > 1000) {
                slider.setValue(avg / 100);
            } else {
                slider.setValue(avg / 30);
            }
        }

        b.setViewer(trackV, slider);
        b.setTitlePanel(this.getTitlePanel(track.getDescription(), cbListener));

        //Größe ändern
        b.setMinimumSize(new Dimension(200, 150));
        b.setPreferredSize(new Dimension(200, 150));
        return b;
    }

    /**
     * Creates BasePanel of two Tracks which have been compared.
     * @param tracks Tracks to compare.
     * @return
     */
    private BasePanel createMultipleTrackPanel(List<PersistantTrack> tracks, PersistantAnnotation annotation) {
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel b = new BasePanel(boundsManager, controller);
        controller.addMousePositionListener(b);

        // get double track connector
        MultiTrackConnector trackCon = new MultiTrackConnector(tracks);
        MultipleTrackViewer trackV = new MultipleTrackViewer(boundsManager, b, controller.getCurrentRefGen(), trackCon, false);

        int annotationWidth = (annotation.getStop() - annotation.getStart()) / 2;
        trackV.getTrackCon().getCoverageThread().setCoveredWidth(annotationWidth);

        //eigener ComponentListener für TrackV
        trackV.addComponentListener(new TrackViewerCompListener(annotation, trackV));


        // create info panel
        CoverageInfoLabel cil = new CoverageInfoLabel();
        cil.renameFields();
        trackV.setTrackInfoPanel(cil);

        // create zoom slider and set its value based on other slider's values for this Annotation
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);
        BasePanel p = annotationToTrackpanelList.get(annotation).get(0);
        try {
            int sValue = ((CoverageZoomSlider) ((JPanel) p.getComponent(0)).getComponent(1)).getValue();
            slider.setValue(sValue);
        } catch (ClassCastException e) {
            Logger.getLogger(ThumbnailController.class.getName()).log(
                    Level.WARNING, "{0}: Can't set value MultipleTrackPanel-Slider", e.getMessage());
        }

        // add panels to basepanel
        b.setViewer(trackV, slider);

        //TitlePanel
        String title = tracks.get(0).getDescription() + " - " + tracks.get(1).getDescription();
        JPanel tp = new JPanel();
        tp.add(new JLabel(title));
        tp.setBackground(ColorProperties.TITLE_BACKGROUND);
        b.setTitlePanel(tp);
        //estimate current size of other BPs based on first BP
        BasePanel refBP = annotationToTrackpanelList.get(annotation).get(0);
        b.setMinimumSize(new Dimension(200, 150));
        b.setPreferredSize(new Dimension(refBP.getBounds().width, refBP.getBounds().height));
        return b;
    }

    /**
     * TitlePanel for TrackPanel with Label and Checkbox.
     * @param title
     * @return
     */
    private JPanel getTitlePanel(String title, CheckBoxActionListener cbListener) {
        JPanel p = new JPanel();
        p.add(new JLabel(title));
        final JCheckBox compare = new JCheckBox("Compare");
        compare.addActionListener(cbListener);
        p.add(compare);
        p.setBackground(ColorProperties.TITLE_BACKGROUND);
        return p;
    }

    @Override
    public void removeAllAnnotations(ReferenceViewer refViewer) {
        //could be used as a function to delete all Annotations
    }

    @Override
    public void removeCertainAnnotation(PersistantAnnotation f) {
        selectedAnnotations.get(activeViewer).remove(f);
        //If all Annotations for activeViewer have been removed it is also removed as key from the list
        if (selectedAnnotations.get(activeViewer).isEmpty()) {
            selectedAnnotations.remove(activeViewer);
        }
    }

    @Override
    public void addAnnotationToList(PersistantAnnotation annotation, final ReferenceViewer refViewer) {
        if (!selectedAnnotations.containsKey(refViewer)) {
            ArrayList<PersistantAnnotation> list = new ArrayList<PersistantAnnotation>();
            list.add(annotation);
            selectedAnnotations.put(refViewer, list);
        } else {
            selectedAnnotations.get(refViewer).add(annotation);
        }
        activeTopComp = refThumbTopComponents.get(refViewer);
        //adds Annotation directly to Scene if ThumbnailTopComponent for this RefViewer is open
        if (WindowManager.getDefault().getRegistry().getOpened().contains(activeTopComp)) {
            ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
            addAnnotationToView(annotation, refCon);
            if (getLookup().lookup(ASyncSliderCookie.class) != null) {
                sliderSynchronisation(true);
            }
            if (!(WindowManager.getDefault().getRegistry().getActivated() == activeTopComp)) {
                activeTopComp.requestAttention(true);
            }
            //activeTopComp.requestActive();
        }

        if (getLookup().lookup(RemoveCookie.class) == null) {
            getLookup().add(new RemoveCookie() {

                @Override
                public void removeTracks() {
                    RemoveAnnotationListPanel rfp = new RemoveAnnotationListPanel(selectedAnnotations.get(activeViewer));
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(rfp, "Remove Annotations");
                    Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                    openRefGenDialog.setVisible(true);
                    //Removes all Selected Annotations from Scene and ArrayLists
                    if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                        for (Object f : rfp.getSelectedValues()) {
                            PersistantAnnotation feat = (PersistantAnnotation) f;
                            removeCertainAnnotation(feat);
                            if (annotationToLayoutWidget.containsKey(feat) && annotationToTrackpanelList.containsKey(feat)) {
                                activeTopComp.getScene().removeChild(annotationToLayoutWidget.get(feat));

                                for (BasePanel p : annotationToTrackpanelList.get(feat)) {
                                    trackPanelToTrack.remove(p);
                                    //Stop CoverageThread
                                    ((MultiTrackConnector) ((TrackViewer) p.getViewer()).getTrackCon()).getCoverageThread().stop();
                                }
                                annotationToTrackpanelList.remove(feat);
                                annotationToLayoutWidget.remove(feat);
                                //If no Annotations are to display remove Cookies and close TopComp.
                                if (activeTopComp.getScene().getChildren().isEmpty()) {
                                    getLookup().removeAll(RemoveCookie.class);
                                    refThumbTopComponents.get(activeViewer).remove(activeTopComp);
                                    refThumbTopComponents.remove(activeViewer);
                                    activeTopComp.close();
                                }
                            }
                        }
                    }
                }
            });
        }
        addOpenCookie();
    }

    /**
     * Creates Widgets for CompareTrackBasePanel to display in TopComponent's Scene
     * @param tracks
     * @param annotation
     */
    private void compareTwoTracks(List<PersistantTrack> tracks, PersistantAnnotation annotation) {
        BasePanel bp = createMultipleTrackPanel(tracks, annotation);
        bp.addMouseListener(this);
        annotationToTrackpanelList.get(annotation).add(bp);
        //If Sliders are currently synchronized, synchronize again for new MultipleTrackViewer
        if (getLookup().lookup(ASyncSliderCookie.class) != null) {
            sliderSynchronisation(true);
        }
        ComponentWidget compWidg = new ComponentWidget(activeTopComp.getScene(), bp);
        compWidg.setBorder(BorderFactory.createRaisedBevelBorder());
        compWidg.getActions().addAction(ActionFactory.createResizeAction(new ResizeStrategy() {

            @Override
            public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
                Widget layout = widget.getParentWidget();
                for (Widget child : layout.getChildren()) {
                    child.setPreferredBounds(suggestedBounds);
                }
                return suggestedBounds;
            }
        }, ActionFactory.createDefaultResizeProvider()));

        //Add MultipleTrackPanel to Layout for currentAnnotation
        annotationToLayoutWidget.get(currentAnnotation).addChild(compWidg);
        activeTopComp.getScene().validate();
    }

    /**
     * MouseAdapter, for updating Annotation information
     */
    @Override
    public void mousePressed(MouseEvent e) {
        BasePanel p = (BasePanel) e.getSource();
        if (p != null) {
            this.updateCurrentAnnotation(p);
        }
    }

    @Override
    public ThumbControllerLookup getLookup() {
        return controllerLookup;
    }

    void removeThumbSpecificCookies() {
        getLookup().removeAll(SyncSliderCookie.class);
        getLookup().removeAll(ASyncSliderCookie.class);
    }

    /**
     * Updates the ReferenceAnnotationComponent to the currently selected Annotation and sets currentAnnotation value.
     * @param bp BasePanel where user has clicked
     */
    private void updateCurrentAnnotation(BasePanel bp) {
        ReferenceAnnotationTopComp comp = (ReferenceAnnotationTopComp) WindowManager.getDefault().findTopComponent("ReferenceAnnotationTopComp");
        if (comp != null) {
            for (PersistantAnnotation annotation : annotationToTrackpanelList.keySet()) {
                if (annotationToTrackpanelList.get(annotation).contains(bp)) {
                    currentAnnotation = annotation;
                    comp.showAnnotationDetails(annotation);
                    break;
                }
            }
        }
    }

    @Override
    public void showPopUp(final PersistantAnnotation f, final ReferenceViewer viewer, MouseEvent e, final JPopupMenu popUp) {
        JMenuItem addListItem = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Add"));
        addListItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addAnnotationToList(f, viewer);
            }
        });
        popUp.add(addListItem);

        JMenuItem showThumbnail = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Show"));
        showThumbnail.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showThumbnailView(viewer);
            }
        });
        popUp.add(showThumbnail);
        if (viewer.getTrackCount() < 1){
            addListItem.setEnabled(false);
            showThumbnail.setEnabled(false);
        }
    }

    @Override
    public void showTablePopUp(final JTable table, final ReferenceViewer refViewer, MouseEvent e) {
        JPopupMenu popUp = new JPopupMenu();
        JMenuItem addListItem = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Add"));
        addListItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Chooses all selected Rows and adds them to ThumbnailViewList
                int[] selectedRows = table.getSelectedRows();
                if (selectedRows.length > 0) {
                    for (int i : selectedRows) {
                        int correctedRow = table.convertRowIndexToModel(i);
                        PersistantAnnotation annotation = (PersistantAnnotation) table.getModel().getValueAt(correctedRow, 0);
                        addAnnotationToList(annotation, refViewer);
                    }
                }
            }
        });
        popUp.add(addListItem);
        JMenuItem showThumbnail = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Show"));
        showThumbnail.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Get all open Components and filter for AppPanelTopComponent
                Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
                AppPanelTopComponent appComp = null;
                for (Iterator<TopComponent> it = topComps.iterator(); it.hasNext();) {
                    TopComponent topComponent = it.next();
                    if (topComponent instanceof AppPanelTopComponent) {
                        appComp = (AppPanelTopComponent) topComponent;
                        break;
                    }
                }
                if (appComp != null) {
                    //Get ViewController from AppPanelTopComponent-Lookup
                    ViewController co = appComp.getLookup().lookup(ViewController.class);
                    showThumbnailView(refViewer, co);
                }
            }
        });
        popUp.add(showThumbnail);
        popUp.show(table, e.getX(), e.getY());
    }

    public void setAutoSlider(boolean set) {
        this.autoSlider = set;
    }

    /**
     * Set selected ThumbnailTopComponent as active and also the respective ReferenceViewer.
     * @param aThis
     */
    void setMeAsActive(ThumbNailViewTopComponent aThis) {
        this.activeTopComp = aThis;
        for (ReferenceViewer refV : refThumbTopComponents.keySet()) {
            if (refThumbTopComponents.get(refV) == activeTopComp) {
                this.activeViewer = refV;
                break;
            }
        }

    }

    void removeOpenCookie() {
        CentralLookup.getDefault().removeAll(OpenThumbCookie.class);
    }

    void addOpenCookie() {
        if (CentralLookup.getDefault().lookup(OpenThumbCookie.class) == null) {
            CentralLookup.getDefault().add(new OpenThumbCookie() {

                @Override
                public void open() {
                    ReferenceViewer refViewer = Utilities.actionsGlobalContext().lookup(ReferenceViewer.class);
                    if (refViewer != null) {
                        showThumbnailView(refViewer);
                    }
                }
            });
        }
    }

    /**
     * Creates a new LayoutWidget for given annotation and adds it to the scene.
     * @param annotation
     * @param refCon
     */
    private void addAnnotationToView(PersistantAnnotation annotation, ReferenceConnector refCon) {
        this.currentAnnotation = annotation;
        //Create LayoutWidget to layout all Tracks for a annotation in GridLayout
        Widget layoutWidg = new Widget(activeTopComp.getScene());
        layoutWidg.setLayout(new ThumbGridLayout((refCon.getAssociatedTracks().size())));
        annotationToLayoutWidget.put(annotation, layoutWidg);

        //Save all BasePanels for annotation in List to put into HashMap
        List<BasePanel> bps = new ArrayList<BasePanel>();
        CheckBoxActionListener cbListener = new CheckBoxActionListener();
        for (PersistantTrack track : refCon.getAssociatedTracks()) {
            BasePanel trackPanel = createTrackPanel(track, controller, cbListener);
            bps.add(trackPanel);
            this.trackPanelToTrack.put(trackPanel, track);
            trackPanel.addMouseListener(this);
            trackPanel.getViewer().addMouseMotionListener(this);
            //Put TrackPanel into ComponentWidget for Scene
            ComponentWidget compWidg = new ComponentWidget(activeTopComp.getScene(), trackPanel);
            compWidg.setBorder(BorderFactory.createRaisedBevelBorder());
            compWidg.getActions().addAction(ActionFactory.createResizeAction(new ResizeStrategy() {

                @Override
                public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
                    Widget layout = widget.getParentWidget();
                    for (Widget child : layout.getChildren()) {
                        child.setPreferredBounds(suggestedBounds);
                    }
                    return suggestedBounds;
                }
            }, ActionFactory.createDefaultResizeProvider()));

            layoutWidg.addChild(compWidg);
            layoutWidg.setBorder(BorderFactory.createTitledBorder("Tracks for annotation:" + currentAnnotation.toString()));
        }
        this.annotationToTrackpanelList.put(currentAnnotation, bps);
        activeTopComp.getScene().addChild(layoutWidg);
        activeTopComp.getScene().validate();
    }

    /**
     * ActionListener for CompareCheckBox's.
     */
    private class CheckBoxActionListener implements ActionListener {

        private int countTracks;
        private BasePanel firstTrackPanelToCompare;

        public CheckBoxActionListener() {
            countTracks = 0;
        }

        void startCompare(ActionEvent e) {
            try {
                BasePanel secondTrackBP = (BasePanel) ((JPanel) ((JCheckBox) e.getSource()).getParent()).getParent();
                ArrayList<PersistantTrack> trackList = new ArrayList<PersistantTrack>();
                trackList.add(trackPanelToTrack.get(firstTrackPanelToCompare));
                trackList.add(trackPanelToTrack.get(secondTrackBP));
                compareTwoTracks(trackList, currentAnnotation);
            } catch (ClassCastException ex) {
                Logger.getLogger(ThumbnailController.class.getName()).log(
                        Level.WARNING, ex.getMessage());
            }
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            //Get Source of Event i.e. BasePanel
            BasePanel bp = (BasePanel) ((JPanel) ((JCheckBox) e.getSource()).getParent()).getParent();
            if (bp != null) {
                updateCurrentAnnotation(bp);
                JCheckBox src = (JCheckBox) e.getSource();
                if (src.isSelected()) {
                    countTracks++;
                    switch (countTracks) {
                        case 1:
                            firstTrackPanelToCompare = bp;
                            break;
                        case 2: {
                            if (annotationToTrackpanelList.get(currentAnnotation).contains(firstTrackPanelToCompare)) {
                                startCompare(e);

                            } else {
                                countTracks--;
                                src.setSelected(false);
                            }
                            break;
                        }
                        default:
                            countTracks--;
                            src.setSelected(false);
                            break;
                    }
                } else {
                    countTracks--;
                }
            }
        }
    }
}
