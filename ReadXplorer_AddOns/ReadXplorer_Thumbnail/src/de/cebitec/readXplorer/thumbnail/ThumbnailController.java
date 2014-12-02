/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.thumbnail;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException;
import de.cebitec.readXplorer.databackend.connector.MultiTrackConnector;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.thumbnail.Actions.ASyncSliderCookie;
import de.cebitec.readXplorer.thumbnail.Actions.OpenThumbCookie;
import de.cebitec.readXplorer.thumbnail.Actions.RemoveCookie;
import de.cebitec.readXplorer.thumbnail.Actions.SyncSliderCookie;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.view.TopComponentHelper;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.IThumbnailView;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.DoubleTrackViewer;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTable;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.border.BorderFactory;
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
import org.openide.windows.WindowManager;

/**
 * ServiceProvider for IThumbnailView.
 * This Module can display all Tracks for a given List of Features in a Thumbnail-like View.
 *
 * @author denis, rhilker
 */
@ServiceProvider(service = IThumbnailView.class)
public class ThumbnailController extends MouseAdapter implements IThumbnailView, Lookup.Provider {

    private HashMap<ReferenceViewer, ThumbNailViewTopComponent> refThumbTopComponents;
    //Currently active ThumbnailTopComponent and ReferenceViewer
    private ThumbNailViewTopComponent activeTopComp;
    private ReferenceViewer activeViewer;
    //Gives access to all Features which are displayed for a referenceViewer
    private HashMap<ReferenceViewer, List<PersistentFeature>> selectedFeatures;
    //Gives access to all BasePanels for Feature
    private HashMap<PersistentFeature, List<BasePanel>> featureToTrackpanelList;
    //Gives access to PersistentTrack from BasePanel
    private HashMap<BasePanel, PersistentTrack> trackPanelToTrack;
    //Gives access to LayoutWidget for currentFeature
    private HashMap<PersistentFeature, Widget> featureToLayoutWidget;
    private PersistentFeature currentFeature;
    private ViewController controller;
    private InstanceContent content;
    //Controller of ThumbnailController
    private ThumbControllerLookup controllerLookup;
    //is true if SliderValues get calculated on creation of TrackPanel
    private boolean autoSlider = true;

    public ThumbnailController() {
        this.refThumbTopComponents = new HashMap<>();
        this.selectedFeatures = new HashMap<>();
        this.featureToTrackpanelList = new HashMap<>();
        this.trackPanelToTrack = new HashMap<>();
        this.featureToLayoutWidget = new HashMap<>();

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
        if (selectedFeatures.containsKey(activeViewer) && selectedFeatures.get(activeViewer).size() > 40) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_TooManyFeatures"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            drawScene();
            //Cookie Stuff
            removeThumbSpecificCookies();
            addSyncCookieToLookup();
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
        if (selectedFeatures.containsKey(activeViewer) && selectedFeatures.get(activeViewer).size() > 40) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_TooManyFeatures"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            drawScene();
            removeThumbSpecificCookies();
            //Activate Synchronize-Action for ZoomSliders
            addSyncCookieToLookup();
        }

    }

    /**
     * Activates synchronize-Sliders Action in Menu.
     */
    private void addSyncCookieToLookup() {
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
                addSyncCookieToLookup();
            }
        });
    }

    /**
     * Sets all Sliders based on sync-Value
     * @param sync Is Set through Cookie-Actions to specify if VerticalSliders should be synchronized.
     */
    private void sliderSynchronisation(boolean sync) {
        //synchronize all Sliders for all RefrenceViewer's ThumbnailViewTopComponents
        for (ReferenceViewer oneViewer : refThumbTopComponents.keySet()) {
            if (selectedFeatures.containsKey(oneViewer)) {
                for (PersistentFeature feature : selectedFeatures.get(oneViewer)) {
                    ZoomChangeListener zoomChangeListener = new ZoomChangeListener();
                    for (BasePanel bp : featureToTrackpanelList.get(feature)) {
                        try {
                            JPanel panel = (JPanel) bp.getComponent(0);
                            if (panel != null) {
                                CoverageZoomSlider slider = (CoverageZoomSlider) panel.getComponent(1);
                                if (sync) {
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


    /**
     * Draws all Thumbnail-Widgets for all features
     */
    private void drawScene() {
        //Get all associated Tracks for Reference
        ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
        if (activeViewer != null && selectedFeatures.containsKey(activeViewer)) {
            for (PersistentFeature feature : selectedFeatures.get(activeViewer)) {
                this.addFeatureToView(feature, refCon);
            }
            if (!(WindowManager.getDefault().getRegistry().getActivated() == activeTopComp)) {
                activeTopComp.requestAttention(true);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ThumbnailController.class, "MSG_NoFeatures"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            activeTopComp.close();
            refThumbTopComponents.remove(activeViewer);
        }
    }

    /**
     * Creates BasePanel for one Track with TrackViewer and ZoomSlider for wrapping into ComponentWidget.
     */
    private BasePanel createTrackPanel(PersistentTrack track, ViewController controller, CheckBoxActionListener cbListener) {
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel basePanel = new BasePanel(boundsManager, controller);
        basePanel.setName(track.getDescription());
        controller.addMousePositionListener(basePanel);

        // create track viewer
        MultiTrackConnector tc;
        SaveFileFetcherForGUI fetcher = new SaveFileFetcherForGUI();
        try {
            tc = fetcher.getMultiTrackConnector(track);
        } catch (UserCanceledTrackPathUpdateException ex) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            return null;
        }

        final TrackViewer trackV = new TrackViewer(boundsManager, basePanel, controller.getCurrentRefGen(), tc, false);
        trackV.setName(track.getDescription());
        trackV.setUseMinimalIntervalLength(false);
        trackV.setIsPanModeOn(false);
        trackV.setCanZoom(false);

//        CoverageInfoLabel cil = new CoverageInfoLabel();
//        trackV.setTrackInfoPanel(cil);

        //own ComponentListener for TrackViewer
        trackV.addComponentListener(new TrackViewerCompListener(currentFeature, trackV));

        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        //Set initial Slider-value based on Coverage if autoSlider is true
        trackV.setAutomaticScaling(autoSlider);

        basePanel.setViewer(trackV, slider);
        basePanel.setTitlePanel(this.getTitlePanel(track.getDescription(), cbListener));

        //adapt size
        basePanel.setMinimumSize(new Dimension(200, 150));
        basePanel.setPreferredSize(new Dimension(200, 150));
        
        return basePanel;
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
    public void removeAllFeatures(ReferenceViewer refViewer) {
        //could be used as a function to delete all Features
    }

    @Override
    public void removeCertainFeature(PersistentFeature f) {
        selectedFeatures.get(activeViewer).remove(f);
        //If all Features for activeViewer have been removed it is also removed as key from the list
        if (selectedFeatures.get(activeViewer).isEmpty()) {
            selectedFeatures.remove(activeViewer);
        }
    }

    @Override
    public void addFeatureToList(PersistentFeature feature, final ReferenceViewer refViewer) {
        if (!selectedFeatures.containsKey(refViewer)) {
            ArrayList<PersistentFeature> list = new ArrayList<>();
            list.add(feature);
            selectedFeatures.put(refViewer, list);
        } else {
            selectedFeatures.get(refViewer).add(feature);
        }
        activeTopComp = refThumbTopComponents.get(refViewer);
        //adds Feature directly to Scene if ThumbnailTopComponent for this RefViewer is open
        if (WindowManager.getDefault().getRegistry().getOpened().contains(activeTopComp)) {
            ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
            addFeatureToView(feature, refCon);
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
                    RemoveFeatureListPanel rfp = new RemoveFeatureListPanel(selectedFeatures.get(activeViewer));
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(rfp, "Remove Features");
                    Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                    openRefGenDialog.setVisible(true);
                    //Removes all Selected Features from Scene and ArrayLists
                    if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                        for (Object f : rfp.getSelectedValues()) {
                            PersistentFeature feat = (PersistentFeature) f;
                            removeCertainFeature(feat);
                            if (featureToLayoutWidget.containsKey(feat) && featureToTrackpanelList.containsKey(feat)) {
                                activeTopComp.getScene().removeChild(featureToLayoutWidget.get(feat));

                                for (BasePanel p : featureToTrackpanelList.get(feat)) {
                                    trackPanelToTrack.remove(p);
                                    //Stop CoverageThread
                                    ((TrackViewer) p.getViewer()).getTrackCon().getCoverageThread().stop();
                                }
                                featureToTrackpanelList.remove(feat);
                                featureToLayoutWidget.remove(feat);
                                //If no Features are to display remove Cookies and close TopComp.
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
     * MouseAdapter, for updating Feature information
     */
    @Override
    public void mousePressed(MouseEvent e) {
        BasePanel p = (BasePanel) e.getSource();
        if (p != null) {
            this.updateCurrentFeature(p);
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
     * Updates the ReferenceFeatureComponent to the currently selected Feature and sets currentFeature value.
     * @param bp BasePanel where user has clicked
     */
    private void updateCurrentFeature(BasePanel bp) {
        ReferenceFeatureTopComp comp = ReferenceFeatureTopComp.findInstance();
        if (comp != null) {
            for (PersistentFeature feature : featureToTrackpanelList.keySet()) {
                if (featureToTrackpanelList.get(feature).contains(bp)) {
                    currentFeature = feature;
                    comp.showFeatureDetails(feature);
                    break;
                }
            }
        }
    }

    @Override
    public void showPopUp(final PersistentFeature f, final ReferenceViewer viewer, MouseEvent e, final JPopupMenu popUp) {
        JMenuItem addListItem = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Add"));
        addListItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addFeatureToList(f, viewer);
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
                        PersistentFeature feature = (PersistentFeature) table.getModel().getValueAt(correctedRow, 0);
                        addFeatureToList(feature, refViewer);
                    }
                }
            }
        });
        popUp.add(addListItem);
        JMenuItem showThumbnail = new JMenuItem(NbBundle.getMessage(ThumbnailController.class, "ThumbController.Show"));
        showThumbnail.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AppPanelTopComponent appComp = TopComponentHelper.getActiveTopComp(AppPanelTopComponent.class);
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
     * Creates a new LayoutWidget for given feature and adds it to the scene.
     * @param feature
     * @param refCon
     */
    private void addFeatureToView(PersistentFeature feature, ReferenceConnector refCon) {
        this.currentFeature = feature;
        //Create LayoutWidget to layout all Tracks for a feature in GridLayout
        Widget layoutWidg = new Widget(activeTopComp.getScene());
        layoutWidg.setLayout(new ThumbGridLayout((refCon.getAssociatedTracks().size())));
        featureToLayoutWidget.put(feature, layoutWidg);

        //Save all BasePanels for feature in List to put into HashMap
        List<BasePanel> bps = new ArrayList<>();
        CheckBoxActionListener cbListener = new CheckBoxActionListener();
        for (PersistentTrack track : refCon.getAssociatedTracks()) {
            BasePanel trackPanel = createTrackPanel(track, controller, cbListener);
            if (trackPanel != null) {
                bps.add(trackPanel);
                this.trackPanelToTrack.put(trackPanel, track);
                trackPanel.addMouseListener(this);
                trackPanel.getViewer().addMouseMotionListener(this);
                //Put TrackPanel into ComponentWidget for Scene
                ComponentWidget compWidg = new ComponentWidget(activeTopComp.getScene(), trackPanel);
                compWidg.setBorder(BorderFactory.createResizeBorder(6, Color.GRAY, false));
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
                layoutWidg.setBorder(javax.swing.BorderFactory.createTitledBorder("Tracks for feature:" + currentFeature.toString()));
            }
        }
        this.featureToTrackpanelList.put(currentFeature, bps);
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

        private void startCompare(ActionEvent e) {
            try {
                BasePanel secondTrackBP = (BasePanel) ((Component) e.getSource()).getParent().getParent();
                ArrayList<PersistentTrack> trackList = new ArrayList<>();
                trackList.add(trackPanelToTrack.get(firstTrackPanelToCompare));
                trackList.add(trackPanelToTrack.get(secondTrackBP));
                this.compareTwoTracks(trackList, currentFeature);
            } catch (ClassCastException ex) {
                Logger.getLogger(ThumbnailController.class.getName()).log(
                        Level.WARNING, ex.getMessage());
            }
        }
        
        /**
         * Creates Widgets for CompareTrackBasePanel to display in
         * TopComponent's Scene
         * @param tracks
         * @param feature
         */
        private void compareTwoTracks(List<PersistentTrack> tracks, PersistentFeature feature) {
            BasePanel bp = createDoubleTrackPanel(tracks, feature);
            bp.addMouseListener(ThumbnailController.this);
            featureToTrackpanelList.get(feature).add(bp);
            //If Sliders are currently synchronized, synchronize again for new MultipleTrackViewer
            if (getLookup().lookup(ASyncSliderCookie.class) != null) {
                sliderSynchronisation(true);
            }
            ComponentWidget compWidg = new ComponentWidget(activeTopComp.getScene(), bp);
            compWidg.setBorder(BorderFactory.createResizeBorder(6, Color.GRAY, false));
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
            activeTopComp.getScene().validate();
        }
        
        /**
         * Creates BasePanel of two Tracks which have been compared.
         * @param tracks Tracks to compare.
         * @return
         */
        private BasePanel createDoubleTrackPanel(List<PersistentTrack> tracks, PersistentFeature feature) {
            BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
            BasePanel b = new BasePanel(boundsManager, controller);
            controller.addMousePositionListener(b);

            // get double track connector
            MultiTrackConnector trackCon;
            SaveFileFetcherForGUI fetcher = new SaveFileFetcherForGUI();
            try {
                trackCon = fetcher.getMultiTrackConnector(tracks);
            } catch (UserCanceledTrackPathUpdateException ex) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                return null; //cannot occur, since both tracks are already open in the thumbnail viewer
            }
            DoubleTrackViewer trackV = new DoubleTrackViewer(boundsManager, b, controller.getCurrentRefGen(), trackCon);
            trackV.setUseMinimalIntervalLength(false);
            trackV.setIsPanModeOn(false);
            trackV.setCanZoom(false);

            //eigener ComponentListener für TrackV
            trackV.addComponentListener(new TrackViewerCompListener(feature, trackV));

//        // create info panel
//        CoverageInfoLabel cil = new CoverageInfoLabel();
//        cil.renameFields();
//        trackV.setTrackInfoPanel(cil);
            // create zoom slider and set its value based on other slider's values for this Feature
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);
            BasePanel p = featureToTrackpanelList.get(feature).get(0);
            try {
                int sValue = ((JSlider) ((Container) p.getComponent(0)).getComponent(1)).getValue();
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
            BasePanel refBP = featureToTrackpanelList.get(feature).get(0);
            b.setMinimumSize(new Dimension(200, 150));
            b.setPreferredSize(new Dimension(refBP.getBounds().width, refBP.getBounds().height));
            return b;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            //Get Source of Event i.e. BasePanel
            BasePanel bp = (BasePanel) ((Component) e.getSource()).getParent().getParent();
            if (bp != null) {
                updateCurrentFeature(bp);
                JCheckBox src = (JCheckBox) e.getSource();
                if (src.isSelected()) {
                    countTracks++;
                    switch (countTracks) {
                        case 1:
                            firstTrackPanelToCompare = bp;
                            break;
                        case 2: {
                            if (featureToTrackpanelList.get(currentFeature).contains(firstTrackPanelToCompare)) {
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
