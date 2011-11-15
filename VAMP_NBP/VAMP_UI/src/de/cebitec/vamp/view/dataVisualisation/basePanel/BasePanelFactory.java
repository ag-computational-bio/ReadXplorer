package de.cebitec.vamp.view.dataVisualisation.basePanel;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.LegendLabel;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.AlignmentViewer;
import de.cebitec.vamp.view.dataVisualisation.histogramViewer.HistogramViewer;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dataVisualisation.seqPairViewer.SequencePairViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageInfoLabel;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.MultipleTrackViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Factory used to initialize all different kinds of base panels.
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
        viewController.addMousePositionListener(b);

        // create viewer
        ReferenceViewer genomeViewer = new ReferenceViewer(boundsManager, b, refGen);

        // show a color legend
        genomeViewer.setupLegend(new LegendLabel(genomeViewer), this.getGenomeViewerLegend());

        // add panels to basepanel
        b.setViewer(genomeViewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true));

        return b;
    }

    public BasePanel getTrackBasePanel(PersistantTrack track,PersistantReference refGen){
        
        BasePanel b = new BasePanel(boundsManager, viewController);
        b.setName(track.getDescription());
        viewController.addMousePositionListener(b);
        
        // create track viewer
        TrackConnector tc = ProjectConnector.getInstance().getTrackConnector(track);
        TrackViewer trackV = new TrackViewer(boundsManager, b, refGen, tc);
        trackV.setName(track.getDescription());

        // create and set up legend
        trackV.setupLegend(new LegendLabel(trackV), this.getTrackPanelLegend());
        
        // create info label
        CoverageInfoLabel cil = new CoverageInfoLabel();
        trackV.setTrackInfoPanel(cil);

        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

        // add panels to basepanel
        b.setTopInfoPanel(cil);
        b.setViewer(trackV, slider);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true));
        b.setTitlePanel(this.getTitlePanel(track.getDescription()));

        return b;
    }

    /**
     * Method to get one <code>BasePanel</code> for multiple tracks.
     * Only 2 tracks at once are currently supported.
     *
     * @param tracks to visualize on this <code>BasePanel</code>.
     * @param refGen reference the tracks belong to.
     * @return
     */
    public BasePanel getMultipleTracksBasePanel(List<PersistantTrack> tracks,PersistantReference refGen){
        if (tracks.size() > 2){
            throw new UnsupportedOperationException("More than two tracks not supported yet.");
        }
        else if (tracks.size() == 2) {
            BasePanel b = new BasePanel(boundsManager, viewController);
            viewController.addMousePositionListener(b);

            // get double track connector
            TrackConnector trackCon = ProjectConnector.getInstance().getTrackConnector(tracks);
            MultipleTrackViewer trackV = new MultipleTrackViewer(boundsManager, b, refGen, trackCon);

            // create and set up legend
            trackV.setupLegend(new LegendLabel(trackV), this.getTrackPanelLegend());

            // create info panel
            CoverageInfoLabel cil = new CoverageInfoLabel();
            cil.renameFields();
            trackV.setTrackInfoPanel(cil);

            // create zoom slider
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

            // add panels to basepanel
            b.setTopInfoPanel(cil);
            b.setViewer(trackV, slider);
            String title = tracks.get(0).getDescription() + " - " + tracks.get(1).getDescription();

            b.setTitlePanel(this.getTitlePanel(title));
            viewController.openTrack2(b);
            return b;
        }
        else if (tracks.size() == 1) {
            return getTrackBasePanel(tracks.get(0), refGen);
        }
        else {
            throw new UnknownError();
        }
    }


    public BasePanel getAlignmentViewBasePanel(TrackConnector connector){
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create an alignmentviewer
        AlignmentViewer viewer = new AlignmentViewer(boundsManager, b, refGen, connector);

        // create a legend
        viewer.setupLegend(new LegendLabel(viewer), this.getAlignmentViewLegend());

        // add panels to basepanel and add scrollbars
        b.setViewerInScrollpane(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, false));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    public BasePanel getHistogrammViewerBasePanel(TrackConnector connector){
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a trackviewer
        HistogramViewer viewer = new HistogramViewer(boundsManager, b, refGen, connector);

        // create a legend
        viewer.setupLegend(new LegendLabel(viewer), getHistogramViewerLegend());

        // add panels to basepanel
        b.setViewer(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, false));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;

    }
    
    /**
     * @param connector track connector of first track of two sequence pair tracks
     * @return A viewer for sequence pair data
     */
    public BasePanel getSeqPairBasePanel(TrackConnector connector){
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a sequence pair viewer
        SequencePairViewer viewer = new SequencePairViewer(boundsManager, b, refGen, connector);

        // create a legend
        viewer.setupLegend(new LegendLabel(viewer), this.getSeqPairViewerLegend());

        // add panels to basepanel and add scrollbars
        b.setViewerInScrollpane(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    
    private AdjustmentPanel createAdjustmentPanel(boolean hasScrollbar, boolean hasSlider){
        // create control panel
        BoundsInfo bounds = boundsManager.getUpdatedBoundsInfo(new Dimension(10, 10));
        AdjustmentPanel control = new AdjustmentPanel(1, refGen.getSequence().length(),
                bounds.getCurrentLogPos(), bounds.getZoomValue(),  hasScrollbar, hasSlider);
        control.addAdjustmentListener(boundsManager);
        boundsManager.addSynchronousNavigator(control);
        return control;
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
        private static final long serialVersionUID = 1L;
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
                GradientPaint whiteToBlack = new GradientPaint(0,0,Color.WHITE, this.getSize().width-1, 0,Color.BLACK);
                g2.setPaint(whiteToBlack);
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
        legend.add(getLegendEntry(ColorProperties.COMMON_MATCH, "Complete cov."));

        return legend;
    }

    private JPanel getHistogramViewerLegend(){
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

    private JPanel getAlignmentViewLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.PERFECT_MATCH, "Perfect Match"));
        legend.add(getLegendEntry(ColorProperties.BEST_MATCH, "Best Match"));
        legend.add(getLegendEntry(ColorProperties.COMMON_MATCH, "Ordinary Match"));
        legend.add(getGradientEntry("Low to high coverage"));
        legend.add(getLegendEntry(ColorProperties.ALIGNMENT_A, "Diff."));

        return legend;
    }
    
        private JPanel getSeqPairViewerLegend(){
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(getLegendEntry(ColorProperties.BLOCK_PERFECT, "Perfect seq. pair"));
        legend.add(getLegendEntry(ColorProperties.BLOCK_DIST_LARGE, "Distorted seq. pair"));
        legend.add(getLegendEntry(ColorProperties.BLOCK_UNPAIRED, "Single mapping"));
        legend.add(getGradientEntry("Perfect to best to common mappings"));

        return legend;
    }
}
