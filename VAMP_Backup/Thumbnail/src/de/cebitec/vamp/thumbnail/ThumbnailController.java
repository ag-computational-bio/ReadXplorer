/*
 * This Module can display all Tracks for a given List of Features in a Thumbnail-like View.
 */
package de.cebitec.vamp.thumbnail;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.thumbnail.Actions.ASynchSliderCookie;
import de.cebitec.vamp.thumbnail.Actions.CompareTrackCookie;
import de.cebitec.vamp.thumbnail.Actions.RemoveCookie;
import de.cebitec.vamp.thumbnail.Actions.SynchSliderCookie;
import de.cebitec.vamp.ui.visualisation.reference.ReferenceFeatureTopComponent;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Utilities;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * ServiceProvider for IThumbnailView
 * @author denis
 */
@ServiceProvider(service = IThumbnailView.class)
public class ThumbnailController extends MouseAdapter implements IThumbnailView, Lookup.Provider {

    private ThumbNailViewTopComponent topComp;
    private HashMap<ReferenceViewer, List<PersistantFeature>> selectedFeatures;
    //Gives access to all BasePanels for feature
    private HashMap<PersistantFeature, List<BasePanel>> featureToTrackpanelList;
    //Gives access to PersistantTrack from BasePanel
    private HashMap<BasePanel, PersistantTrack> trackPanelToTrack;
    //Gives access to LayoutWidget for currentFeature
    private HashMap<PersistantFeature, Widget> featureToLayoutWidget;
    private ReferenceViewer viewer;
    private PersistantFeature currentFeature;
    private ViewController controller;
    private int countTracks = 0;
    private BasePanel firstTrackPanelToCompare;
    private InstanceContent content;
    //Controller of ThumbnailController
    private ThumbControllerLookup controllerLookup;

    public ThumbnailController() {
        this.selectedFeatures = new HashMap<ReferenceViewer, List<PersistantFeature>>();
        this.featureToTrackpanelList = new HashMap<PersistantFeature, List<BasePanel>>();
        this.trackPanelToTrack = new HashMap<BasePanel, PersistantTrack>();
        this.featureToLayoutWidget = new HashMap<PersistantFeature, Widget>();
        content = new InstanceContent();
        controllerLookup = new ThumbControllerLookup(content);
    }

    @Override
    public void showThumbnailView(ReferenceViewer refViewer) {
        countTracks = 0;
        viewer = refViewer;
        topComp = ThumbNailViewTopComponent.findInstance();
        topComp.setName("ThumbnailReference: " + refViewer.getReference().getName());
        topComp.open();
        Scene scene = topComp.getScene();
        scene.removeChildren();
        scene.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        //Get ViewController
        Result<ViewController> viewControlResult = Utilities.actionsGlobalContext().lookupResult(ViewController.class);
        controller = viewControlResult.allInstances().iterator().next();
        //After Lookup-stuff is done requestActive for ThumbnailTopComponent
        topComp.requestActive();
        //Build scene
        drawScene();
        removeCookies();
        //Activate Synchronize-Action for ZoomSliders
        addSynchCookieToLookup();
    }

    /**
     * Creates ThumbnailView with given ViewController.
     * @param refViewer
     * @param con
     */
    @Override
    public void showThumbnailView(ReferenceViewer refViewer, ViewController con) {
        countTracks = 0;
        viewer = refViewer;
        topComp = ThumbNailViewTopComponent.findInstance();
        topComp.setName("ThumbnailReference: " + refViewer.getReference().getName());
        topComp.open();
        Scene scene = topComp.getScene();
        scene.removeChildren();
        scene.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        controller = con;
        topComp.requestActive();
        //Build scene
        drawScene();
        removeCookies();
        //Activate Synchronize-Action for ZoomSliders
        addSynchCookieToLookup();
    }

    /**
     * Activates synchronize-Sliders Action in Menu.
     */
    private void addSynchCookieToLookup() {
        getLookup().add(new SynchSliderCookie() {

            @Override
            public void synchSliders() {
                sliderSynchronisation(true);
                //Sliders a synchronized now so remove old cookie and add a new one so that they can be asynchronized again.
                getLookup().removeAll(SynchSliderCookie.class);
                addASynchCookieToLookup();
            }
        });
    }

    /**
     * Activates don't-synchronize-Sliders Action in Menu.
     */
    private void addASynchCookieToLookup() {
        getLookup().add(new ASynchSliderCookie() {

            @Override
            public void asynch() {
                sliderSynchronisation(false);
                getLookup().removeAll(ASynchSliderCookie.class);
                addSynchCookieToLookup();
            }
        });
    }

    /**
     * Sets all Sliders based on synch-Value
     * @param synch Is Set through Cookie-Actions to specify if VerticalSliders should be synchronized.
     */
    private void sliderSynchronisation(boolean synch) {
        for (PersistantFeature feature : selectedFeatures.get(viewer)) {
            ZoomChangeListener zoomChangeListener = new ZoomChangeListener();
            for (BasePanel bp : featureToTrackpanelList.get(feature)) {
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
            }
        }
    }


    /*
     * Draws all Thumbnail-Widgets for all features
     */
    private void drawScene() {
        //Get all associated Tracks for Reference
        ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
        for (PersistantFeature feature : selectedFeatures.get(viewer)) {
            this.currentFeature = feature;
            //Create LayoutWidget to layout all Tracks for a feature in GridLayout
            Widget layoutWidg = new Widget(topComp.getScene());
            layoutWidg.setLayout(new ThumbGridLayout((refCon.getAssociatedTracks().size())));
            featureToLayoutWidget.put(feature, layoutWidg);
            //Save all BasePanels for feature in List to put into HashMap
            List<BasePanel> bps = new ArrayList<BasePanel>();
            for (PersistantTrack track : refCon.getAssociatedTracks()) {
                BasePanel trackPanel = createTrackPanel(track, controller);
                bps.add(trackPanel);
                this.trackPanelToTrack.put(trackPanel, track);
                trackPanel.addMouseListener(this);
                trackPanel.getViewer().addMouseMotionListener(this);
                //Put TrackPanel into ComponentWidget for Scene
                ComponentWidget compWidg = new ComponentWidget(topComp.getScene(), trackPanel);
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
                layoutWidg.setBorder(BorderFactory.createTitledBorder("Tracks for feature:" + currentFeature.toString()));
            }
            this.featureToTrackpanelList.put(currentFeature, bps);
            topComp.getScene().addChild(layoutWidg);
            topComp.getScene().validate();
        }
    }

    /*
     * Creates BasePanel for one Track with TrackViewer and ZoomSlider for wrapping into ComponentWidget.
     */
    private BasePanel createTrackPanel(PersistantTrack track, ViewController controller) {
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel b = new BasePanel(boundsManager, controller);
        b.setName(track.getDescription());
        controller.addMousePositionListener(b);

        // create track viewer
        MultiTrackConnector tc = new MultiTrackConnector(track);
        final TrackViewer trackV = new TrackViewer(boundsManager, (BasePanel) b, controller.getCurrentRefGen(), tc);
        int featureWidth = (currentFeature.getStop() - currentFeature.getStart()) / 2;
        trackV.getTrackCon().getThread().setCoveredWidth(featureWidth);

        trackV.setName(track.getDescription());

        CoverageInfoLabel cil = new CoverageInfoLabel();
        trackV.setTrackInfoPanel(cil);

        //eigener ComponentListener für TrackV
        trackV.addComponentListener(new TrackViewerCompListener(currentFeature, trackV));


        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        b.setViewer(trackV, slider);
        b.setTitlePanel(this.getTitlePanel(track.getDescription()));

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
    private BasePanel createMultipleTrackPanel(List<PersistantTrack> tracks, PersistantFeature feature) {
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel b = new BasePanel(boundsManager, controller);
        controller.addMousePositionListener(b);

        // get double track connector
        MultiTrackConnector trackCon = new MultiTrackConnector(tracks);
        MultipleTrackViewer trackV = new MultipleTrackViewer(boundsManager, b, controller.getCurrentRefGen(), trackCon);

        int featureWidth = (feature.getStop() - feature.getStart()) / 2;
        trackV.getTrackCon().getThread().setCoveredWidth(featureWidth);

        //eigener ComponentListener für TrackV
        trackV.addComponentListener(new TrackViewerCompListener(feature, trackV));


        // create info panel
        CoverageInfoLabel cil = new CoverageInfoLabel();
        cil.renameFields();
        trackV.setTrackInfoPanel(cil);

        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        // add panels to basepanel
        b.setViewer(trackV, slider);

        //TitlePanel
        String title = tracks.get(0).getDescription() + " - " + tracks.get(1).getDescription();
        JPanel tp = new JPanel();
        tp.add(new JLabel(title));
        tp.setBackground(ColorProperties.TITLE_BACKGROUND);
        b.setTitlePanel(tp);
        //estimate current size of other BPs based on first BP
        BasePanel refBP = featureToTrackpanelList.get(feature).get(0);
        b.setMinimumSize(new Dimension(200, 150));
        b.setPreferredSize(new Dimension(refBP.getBounds().width, refBP.getBounds().height));
        return b;
    }

    /**
     * TitlePanel for TrackPanel with Label and Checkbox.
     * @param title
     * @return
     */
    private JPanel getTitlePanel(String title) {
        JPanel p = new JPanel();
        p.add(new JLabel(title));
        final JCheckBox compare = new JCheckBox("Compare");
        compare.addActionListener(new CheckBoxActionListener(compare));
        p.add(compare);
        p.setBackground(ColorProperties.TITLE_BACKGROUND);
        return p;
    }

    @Override
    public void removeAllFeatures(ReferenceViewer refViewer) {
        selectedFeatures.get(refViewer).clear();
        getLookup().removeAll(RemoveCookie.class);
    }

    @Override
    public void addToList(PersistantFeature feature, final ReferenceViewer refViewer) {
        if (!selectedFeatures.containsKey(refViewer)) {
            ArrayList<PersistantFeature> list = new ArrayList<PersistantFeature>();
            list.add(feature);
            selectedFeatures.put(refViewer, list);
        } else {
            selectedFeatures.get(refViewer).add(feature);
        }
        /*CentralLookup.getDefault().add(new RemoveCookie() {

        @Override
        public void removeTracks() {
        RemoveTrackListPanel orgp = new RemoveTrackListPanel(selectedFeatures.get(viewer));
        DialogDescriptor dialogDescriptor = new DialogDescriptor(orgp, "Open Reference");
        Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        openRefGenDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {

        }
        }
        });*/
        if (getLookup().lookup(RemoveCookie.class) == null) {
            getLookup().add(new RemoveCookie() {

                @Override
                public void removeTracks() {
                    RemoveTrackListPanel orgp = new RemoveTrackListPanel(selectedFeatures.get(viewer));
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(orgp, "Open Reference");
                    Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                    openRefGenDialog.setVisible(true);

                    if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                    }
                }
            });
        }

    }

    private void compareTwoTracks(List<PersistantTrack> tracks, PersistantFeature feature) {
        BasePanel bp = createMultipleTrackPanel(tracks, feature);
        bp.addMouseListener(this);
        featureToTrackpanelList.get(feature).add(bp);
        //If Sliders are currently synchronized, synchronize again for new MultipleTrackViewer
        if (getLookup().lookup(ASynchSliderCookie.class) != null) {
            sliderSynchronisation(true);
        }
        ComponentWidget compWidg = new ComponentWidget(topComp.getScene(), bp);
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

        //Add MultipleTrackPanel to Layout for currentFeature
        featureToLayoutWidget.get(currentFeature).addChild(compWidg);
        topComp.getScene().validate();
    }

    /**
     * MouseAdapter, for updating feature information
     */
    @Override
    public void mousePressed(MouseEvent e) {
        BasePanel p = (BasePanel) e.getSource();
        if (p != null) {
            updateCurrentFeature(p);
        }
    }

    @Override
    public ThumbControllerLookup getLookup() {
        return controllerLookup;
    }

    void removeCookies() {
        getLookup().removeAll(SynchSliderCookie.class);
        getLookup().removeAll(ASynchSliderCookie.class);
        getLookup().removeAll(CompareTrackCookie.class);
    }

    /**
     * Updates the ReferenceFeatureComponent to the currently selected feature and sets currentFeature value.
     * @param bp BasePanel where user has clicked
     */
    private void updateCurrentFeature(BasePanel bp) {
        ReferenceFeatureTopComponent comp = (ReferenceFeatureTopComponent) WindowManager.getDefault().findTopComponent("ReferenceFeatureTopComponent");
        if (comp != null) {
            for (PersistantFeature feature : featureToTrackpanelList.keySet()) {
                if (featureToTrackpanelList.get(feature).contains(bp)) {
                    currentFeature = feature;
                    comp.showFeatureDetails(feature);
                    break;
                }
            }
        }
    }

    /**
     * ActionListener for CompareCheckBox's.
     */
    private class CheckBoxActionListener implements ActionListener {

        private JCheckBox textBox;

        public CheckBoxActionListener(JCheckBox tb) {
            textBox = tb;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            //Get Source of Event i.e. BasePanel
            BasePanel bp = (BasePanel) ((JPanel) ((JCheckBox) e.getSource()).getParent()).getParent();
            if (bp != null) {
                updateCurrentFeature(bp);
                if (textBox.isSelected()) {
                    countTracks++;
                    switch (countTracks) {
                        case 1:
                            firstTrackPanelToCompare = bp;
                            break;
                        case 2: {
                            //if selected panel belongs to the same feature as the previous selected panel activate compareAction
                            if (featureToTrackpanelList.get(currentFeature).contains(firstTrackPanelToCompare)) {
                                getLookup().add(new CompareTrackCookie() {

                                    @Override
                                    public void compare() {
                                        BasePanel secondTrackBP = (BasePanel) ((JPanel) ((JCheckBox) e.getSource()).getParent()).getParent();
                                        ArrayList<PersistantTrack> trackList = new ArrayList();
                                        trackList.add(trackPanelToTrack.get(firstTrackPanelToCompare));
                                        trackList.add(trackPanelToTrack.get(secondTrackBP));
                                        compareTwoTracks(trackList, currentFeature);
                                    }
                                });
                            } else {
                                countTracks--;
                                textBox.setSelected(false);
                            }
                            break;
                        }
                        default:
                            countTracks--;
                            textBox.setSelected(false);
                            break;
                    }
                } else {
                    countTracks--;
                    if (countTracks == 1) {
                        getLookup().removeAll(CompareTrackCookie.class);
                    }
                }
            }
        }
    }
}
