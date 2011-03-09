/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.ui.visualisation.reference.ReferenceFeatureTopComponent;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.IThumbnailView;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageInfoLabel;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.Dimension;
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
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup.Result;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Implements IThumbnailView. Is responsible for showing a thumbnail-view of all Tracks for given features.
 * @author denis
 */
@ServiceProvider(service = IThumbnailView.class)
public class ThumbnailController extends MouseAdapter implements IThumbnailView, ActionListener {

    private ThumbNailViewTopComponent topComp;
    private List<PersistantFeature> selectedFeatures;
    private HashMap<PersistantFeature, List<BasePanel>> featureToTrackpanelList;
    private ReferenceViewer viewer;
    private PersistantFeature currentFeature;
    private ViewController controller;
    final private String SYNCHCB = "SYNCH-CB";

    public ThumbnailController() {
        this.selectedFeatures = new ArrayList<PersistantFeature>();
        this.featureToTrackpanelList = new HashMap<PersistantFeature, List<BasePanel>>();
    }

    @Override
    public void showThumbnailView(ReferenceViewer refViewer) {
        viewer = refViewer;
        topComp = ThumbNailViewTopComponent.findInstance();
        topComp.open();
        Scene scene = topComp.getScene();
        scene.removeChildren();
        scene.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
        scene.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        //ViewController holen
        Result<ViewController> viewControlResult = Utilities.actionsGlobalContext().lookupResult(ViewController.class);
        controller = viewControlResult.allInstances().iterator().next();
        topComp.requestActive();
        drawScene();
        ThumbnailOptionsTopComponent.findInstance().getjCheckBox1().setSelected(false);

    }

    private void createToolTipText(Widget compWidg, TrackViewer track) {

        //TODO: Sinnvoller ToolTipText
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<b>TrackID:</b>: ").append(track.getTrackCon().getTrackID());
        sb.append("</html>");

        compWidg.setToolTipText(sb.toString());
    }

    @Override
    public void addToList(PersistantFeature feature) {
        this.selectedFeatures.add(feature);
    }

    /*
     * Creates actual BasePanel with TrackViewer and ZoomSlider for wrapping into ComponentWidget.
     */
    private BasePanel createTrackPanel(PersistantTrack track, ViewController controller) {
        //return controller.getBasePanelFac().getTrackBasePanel(track, controller.getCurrentRefGen());
        BoundsInfoManager boundsManager = new BoundsInfoManager(controller.getCurrentRefGen());
        BasePanel b = new BasePanel(boundsManager, controller);
        b.setName(track.getDescription());
        controller.addMousePositionListener(b);

        // create track viewer
        MultiTrackConnector tc = new MultiTrackConnector(track);
        final TrackViewer trackV = new TrackViewer(boundsManager, (BasePanel) b, controller.getCurrentRefGen(), tc);
        int featureWidth = (currentFeature.getStop() - currentFeature.getStart()) / 2;
        trackV.getTrackCon().getThread().setCoveredWidth(featureWidth);

        //trackV.updatePhysicalBounds();
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

    private JPanel getTitlePanel(String title) {
        JPanel p = new JPanel();
        p.add(new JLabel(title));
        p.add(new JCheckBox("Compare Track"));
        p.setBackground(ColorProperties.TITLE_BACKGROUND);
        return p;
    }

    @Override
    public void removeAllFeatures() {
        this.selectedFeatures.clear();
    }

    //ActionListener fuer ThumbnailOptionsTopComponent
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(SYNCHCB)) {
            /////Umstaendlich,alles neu zeichen!
            //topComp.getScene().removeChildren();
            //drawScene(ThumbnailOptionsTopComponent.findInstance().getjCheckBox1().isSelected());

            for (PersistantFeature feature : this.selectedFeatures) {
                ZoomChangeListener zoomChangeListener = new ZoomChangeListener();
                for (BasePanel bp : this.featureToTrackpanelList.get(feature)) {
                    JPanel panel = (JPanel) bp.getComponent(0);
                    if (panel != null) {
                        CoverageZoomSlider slider = (CoverageZoomSlider) panel.getComponent(1);
                        slider.setValue(5);
                        if (ThumbnailOptionsTopComponent.findInstance().getjCheckBox1().isSelected()) {
                            slider.addChangeListener(zoomChangeListener);
                            zoomChangeListener.addMapValue((TrackViewer) panel.getComponent(0), slider);
                        } else {
                            slider.removeChangeListener(slider.getChangeListeners()[0]);
                            System.out.println(slider.getChangeListeners().length);
                        }
                    }
                }
            }
        }
    }

    /*
     * Draws all thumbnail-Widgets for all features
     */
    private void drawScene() {
        //Alle associatedTracks für Reference holen
        ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector(controller.getCurrentRefGen().getId());
        for (PersistantFeature feature : this.selectedFeatures) {
            this.currentFeature = feature;
            Widget layoutWidg = new Widget(topComp.getScene());
            layoutWidg.setLayout(new ThumbGridLayout((refCon.getAssociatedTracks().size())));
            List<BasePanel> bps = new ArrayList<BasePanel>();
            for (PersistantTrack track : refCon.getAssociatedTracks()) {
                BasePanel trackPanel = createTrackPanel(track, controller);
                bps.add(trackPanel);
                trackPanel.addMouseListener(this);
                ComponentWidget compWidg = new ComponentWidget(topComp.getScene(), trackPanel);
                compWidg.setBorder(BorderFactory.createRaisedBevelBorder());
                compWidg.getActions().addAction(ActionFactory.createResizeAction(ActionFactory.createFreeResizeStategy(), ActionFactory.createDefaultResizeProvider()));
                compWidg.getActions().addAction(ActionFactory.createMoveAction());

                layoutWidg.addChild(compWidg);
                layoutWidg.setBorder(BorderFactory.createTitledBorder("Tracks for feature:" + currentFeature.toString()));
            }
            this.featureToTrackpanelList.put(currentFeature, bps);
            topComp.getScene().addChild(layoutWidg);
            topComp.getScene().validate();
        }
    }

     /**
     * MouseAdapter
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        ReferenceFeatureTopComponent comp = (ReferenceFeatureTopComponent) WindowManager.getDefault().findTopComponent("ReferenceFeatureTopComponent");
        BasePanel p = (BasePanel) e.getSource();
        for (PersistantFeature feature : this.featureToTrackpanelList.keySet()) {
            if (this.featureToTrackpanelList.get(feature).contains(p)) {
                comp.showFeatureDetails(feature);
                break;
            }
        }

    }
  
}
