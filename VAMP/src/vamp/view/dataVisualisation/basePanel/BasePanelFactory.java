package vamp.view.dataVisualisation.basePanel;

import vamp.view.dataVisualisation.abstractViewer.LegendLabel;
import vamp.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import vamp.view.dataVisualisation.*;
import vamp.view.dataVisualisation.referenceViewer.ReferenceViewerInfoPanel;
import vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import vamp.view.dataVisualisation.referenceViewer.ReferenceNavigator;
import vamp.view.dataVisualisation.trackViewer.TrackViewer;
import vamp.view.dataVisualisation.trackViewer.TrackNavigatorPanel;
import vamp.view.dataVisualisation.trackViewer.TrackInfoPanel;
import java.awt.Color;
import java.awt.Component;
import vamp.view.dataVisualisation.alignmentViewer.AlignmentViewer;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vamp.ColorProperties;
import vamp.databackend.dataObjects.PersistantReference;
import vamp.databackend.dataObjects.PersistantTrack;
import vamp.databackend.connector.ProjectConnector;
import vamp.databackend.connector.TrackConnector;
import vamp.view.dataVisualisation.BoundsInfoManager;
import vamp.view.ViewController;
import vamp.view.dataVisualisation.histogramViewer.HistogramViewer;

/**
 *
 * @author ddoppmeier
 */
public class BasePanelFactory {

    private BoundsInfoManager boundsManager;
    private PersistantReference refGen;
    private ViewController viewController;

    public BasePanelFactory(BoundsInfoManager boundsManager, ViewController viewController){
        this.boundsManager = boundsManager;
        this.viewController = viewController;
    }


    public BasePanel getGenomeViewerBasePanel(PersistantReference refGen){

        this.refGen = refGen;
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener((MousePositionListener) b);

        // create info panel
        ReferenceViewerInfoPanel info = new ReferenceViewerInfoPanel();

        // create viewer
        ReferenceViewer genomeViewer = new ReferenceViewer(boundsManager, b, refGen);
        genomeViewer.setGenomeViewerInfoPanel(info);

        // show a color legend
        genomeViewer.setupLegend(new LegendLabel(genomeViewer), this.getGenomeViewerLegend());

        // create navigator
        AbstractInfoPanel navigator = new ReferenceNavigator(refGen, boundsManager, genomeViewer);

        // add panels to basepanel
        b.setRightInfoPanel(info);
        b.setViewer(genomeViewer);
        b.setLeftInfoPanel(navigator);
        b.setAdjustmentPanel(this.createAdjustmentPanel(true, true));
        b.setTitlePanel(this.getTitlePanel(refGen.getName()));

        return b;
    }

    public BasePanel getTrackBasePanel(PersistantTrack track){
        
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);
        
        // create track viewer
        TrackConnector tc = ProjectConnector.getInstance().getTrackConnector(track.getId());
        TrackViewer trackV = new TrackViewer(boundsManager, b, refGen, tc);

        // create and set up legend
        trackV.setupLegend(new LegendLabel(trackV), this.getTrackPanelLegend());
        
        // create info panel
        TrackInfoPanel info = new TrackInfoPanel();
        trackV.setTrackInfoPanel(info);

        // create navi panel
        TrackNavigatorPanel navi = new TrackNavigatorPanel(tc, this, track, boundsManager);

        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        // add panels to basepanel
        b.setRightInfoPanel(info);
        b.setLeftInfoPanel(navi);
        b.setViewer(trackV, slider);
        b.setTitlePanel(this.getTitlePanel(track.getDescription()));

        return b;
    }

    public BasePanel getDetailTrackBasePanel(PersistantTrack track){
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a trackviewer
        TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track.getId());
        AlignmentViewer viewer = new AlignmentViewer(boundsManager, b, refGen, connector);

        // create a legend
        viewer.setupLegend(new LegendLabel(viewer), this.getDetailViewLegend());

        b.setViewer(viewer);
        b.setAdjustmentPanel(this.createAdjustmentPanel(true, false));
        b.setTitlePanel(this.getTitlePanel(track.getDescription()));

        return b;
    }

    public BasePanel getSequenceLogoBasePanel(PersistantTrack track){
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a trackviewer
        TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(track.getId());
        HistogramViewer viewer = new HistogramViewer(boundsManager, b, refGen, connector);

        // create a legend
        viewer.setupLegend(new LegendLabel(viewer), getSequenceLogoLegend());

        // add panels to basepanel
        b.setViewer(viewer);
        b.setAdjustmentPanel(this.createAdjustmentPanel(true, false));
        b.setTitlePanel(this.getTitlePanel(track.getDescription()));

        return b;

    }

    private AdjustmentPanel createAdjustmentPanel(boolean hasScrollbar, boolean hasSlider){
        // create control panel
        BoundsInfo bounds = boundsManager.getUpdatedBoundsInfo(new Dimension(10, 10));
        AdjustmentPanel controll = new AdjustmentPanel(1, refGen.getSequence().length(), bounds.getCurrentLogPos(), bounds.getZoomValue(),  hasScrollbar, hasSlider);
        controll.addAdjustmentListener(boundsManager);
        boundsManager.addSynchronousNavigator(controll);
        return controll;
    }

    private JPanel getTitlePanel(String title){
        JPanel p = new JPanel();
        p.add(new JLabel(title));
        p.setBackground(ColorProperties.TITLE_BACKGROUND);
        return p;
    }

    private JPanel getLegendEntry(Color c, String description){
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEADING));
        entry.setBackground(ColorProperties.LEGEND_BACKGROUND);

        ColorPanel color = new ColorPanel();
        color.setSize(new Dimension(10, 10));
        color.setBackground(c);

        entry.add(color);

        entry.add(new JLabel(description));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        return entry;
    }

    private class ColorPanel extends JPanel{
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, this.getSize().width-1, this.getSize().height-1);
        }
    }

    private JPanel getGradientEntry(String description){
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEADING));
        entry.setBackground(ColorProperties.LEGEND_BACKGROUND);

        JPanel color = new JPanel(){
            private static final long serialVersionUID = 1234537;
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint blacktowhite = new GradientPaint(0,0,Color.BLACK, this.getSize().width-1, 0,Color.WHITE);
                g2.setPaint(blacktowhite);
                g2.fill(new Rectangle2D.Double(0, 0, this.getSize().width, this.getSize().height));
                g2.setPaint(null);
                g2.setColor(Color.black);
                g2.drawRect(0, 0, this.getSize().width-1, this.getSize().height-1);
            }
        };
        color.setSize(new Dimension(10, 10));
        entry.add(color);

        entry.add(new JLabel(description));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        return entry;
    }

    private JPanel getGenomeViewerLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.CDS, "CDS"));
        legend.add(getLegendEntry(ColorProperties.GENE, "gene"));
        legend.add(getLegendEntry(ColorProperties.REPEAT_UNIT, "Repeat Unit"));
        legend.add(getLegendEntry(ColorProperties.MRNA, "mRNA"));
        legend.add(getLegendEntry(ColorProperties.MI_RNA, "microRNA"));
        legend.add(getLegendEntry(ColorProperties.RRNA, "rRNA"));
        legend.add(getLegendEntry(ColorProperties.TRNA, "tRNA"));
        legend.add(getLegendEntry(ColorProperties.MISC_RNA, "misc. RNA"));

        return legend;
    }

    private JPanel getTrackPanelLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.PERFECT_MATCH, "Perfect match cov."));
        legend.add(getLegendEntry(ColorProperties.BEST_MATCH, "Best match cov."));
        legend.add(getLegendEntry(ColorProperties.N_ERROR_COLOR, "Complete cov."));

        return legend;
    }

    private JPanel getSequenceLogoLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.LOGO_A, "A"));
        legend.add(getLegendEntry(ColorProperties.LOGO_C, "C"));
        legend.add(getLegendEntry(ColorProperties.LOGO_G, "G"));
        legend.add(getLegendEntry(ColorProperties.LOGO_T, "T"));
        legend.add(getLegendEntry(ColorProperties.LOGO_N, "N"));
        legend.add(getLegendEntry(ColorProperties.LOGO_MATCH, "Match"));
        legend.add(getLegendEntry(ColorProperties.LOGO_READGAP, "Gap in read"));

        return legend;
    }

    private JPanel getDetailViewLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.PERFECT_MATCH, "Perfect Match"));
        legend.add(getLegendEntry(ColorProperties.BEST_MATCH, "Best Match"));
        legend.add(getLegendEntry(ColorProperties.N_ERROR_COLOR, "Ordinary Match"));
        legend.add(getGradientEntry("Coverage"));
        legend.add(getLegendEntry(ColorProperties.ALIGNMENT_A, "Diff."));

        return legend;
    }
}
