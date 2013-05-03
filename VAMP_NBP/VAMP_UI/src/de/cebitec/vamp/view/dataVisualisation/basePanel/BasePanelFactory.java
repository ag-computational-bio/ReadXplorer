package de.cebitec.vamp.view.dataVisualisation.basePanel;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.MenuLabel;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.AlignmentOptionsPanel;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.AlignmentViewer;
import de.cebitec.vamp.view.dataVisualisation.histogramViewer.HistogramViewer;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dataVisualisation.seqPairViewer.SequencePairViewer;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.*;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.*;

/**
 * Factory used to initialize all different kinds of base panels.
 *
 * @author ddoppmeier
 */
public class BasePanelFactory {

    private BoundsInfoManager boundsManager;
    private PersistantReference refGenome;
    private ViewController viewController;

    public BasePanelFactory(BoundsInfoManager boundsManager, ViewController viewController) {
        this.boundsManager = boundsManager;
        this.viewController = viewController;
    }

    public BasePanel getGenomeViewerBasePanel(PersistantReference refGenome) {

        this.refGenome = refGenome;
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create viewer
        ReferenceViewer genomeViewer = new ReferenceViewer(boundsManager, b, refGenome);

        // show a color legend
        JPanel genomePanelLegend = this.getGenomeViewerLegend(genomeViewer);
        genomeViewer.setupLegend(new MenuLabel(genomePanelLegend, MenuLabel.TITLE_LEGEND), genomePanelLegend);

        // add panels to basepanel
        int maxSliderValue = 500;
        b.setViewer(genomeViewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));

        return b;
    }

    public BasePanel getTrackBasePanel(PersistantTrack track, PersistantReference refGen) {

        BasePanel basePanel = new BasePanel(boundsManager, viewController);
        basePanel.setName(track.getDescription());
        viewController.addMousePositionListener(basePanel);

        // create track viewer
        TrackConnector tc = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(track);
        if (tc != null) {
            TrackViewer trackV = new TrackViewer(boundsManager, basePanel, refGen, tc, false);
            trackV.setName(track.getDescription());

            // create and set up legend
            JPanel trackPanelLegend = this.getTrackPanelLegend(trackV);
            MenuLabel legendLabel = new MenuLabel(trackPanelLegend, MenuLabel.TITLE_LEGEND);
            trackV.setupLegend(legendLabel, trackPanelLegend);

            // create and set up options (currently normalization)
            JPanel trackPanelOptions = this.getTrackPanelOptions(trackV);
            MenuLabel optionsLabel = new MenuLabel(trackPanelOptions, MenuLabel.TITLE_OPTIONS);
            trackV.setupOptions(optionsLabel, trackPanelOptions);

            //assign observers to handle visualization correctly
            legendLabel.registerObserver(optionsLabel);
            optionsLabel.registerObserver(legendLabel);

            // create info label
            CoverageInfoLabel cil = new CoverageInfoLabel();
            trackV.setTrackInfoPanel(cil);

            // create zoom slider
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

            // add panels to basepanel
            int maxSliderValue = 500;
            basePanel.setTopInfoPanel(cil);
            basePanel.setViewer(trackV, slider);
            basePanel.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));
            basePanel.setTitlePanel(this.getTitlePanel(track.getDescription()));

            return basePanel;
        } else {
            return null;
        }
    }

    /**
     * Method to get one
     * <code>BasePanel</code> for multiple tracks. Only 2 tracks at once are
     * currently supported.
     *
     * @param tracks to visualize on this <code>BasePanel</code>.
     * @param refGen reference the tracks belong to.
     * @param combineTracks true, if the coverage of two or more tracks should
     * be combined
     * @return
     */
    public BasePanel getMultipleTracksBasePanel(List<PersistantTrack> tracks, PersistantReference refGen, boolean combineTracks) {
        if (tracks.size() > 2 && !combineTracks) {
            throw new UnsupportedOperationException("More than two tracks not supported in non-combined mode.");
        } else if (tracks.size() == 2 && !combineTracks || combineTracks) {
            BasePanel basePanel = new BasePanel(boundsManager, viewController);
            viewController.addMousePositionListener(basePanel);

            // get double track connector
            TrackConnector trackCon = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(tracks, combineTracks);

            MultipleTrackViewer trackV = new MultipleTrackViewer(boundsManager, basePanel, refGen, trackCon, combineTracks);

            // create and set up legend
            JPanel trackPanelLegend;
            if (combineTracks) {
                trackPanelLegend = this.getTrackPanelLegend(trackV);
            } else {
                trackPanelLegend = this.getDoubleTrackPanelLegend();
            }
            MenuLabel legendLabel = new MenuLabel(trackPanelLegend, MenuLabel.TITLE_LEGEND);
            trackV.setupLegend(legendLabel, trackPanelLegend);

            // create and set up options (currently normalization)
            JPanel trackPanelOptions = this.getTrackPanelOptions(trackV);
            MenuLabel optionsLabel = new MenuLabel(trackPanelOptions, MenuLabel.TITLE_OPTIONS);
            trackV.setupOptions(optionsLabel, trackPanelOptions);

            //assign observers to handle visualization correctly
            legendLabel.registerObserver(optionsLabel);
            optionsLabel.registerObserver(legendLabel);

            // create info panel
            CoverageInfoLabel cil = new CoverageInfoLabel();
            cil.setCombineTracks(combineTracks);
            cil.renameFields();
            trackV.setTrackInfoPanel(cil);

            // create zoom slider
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

            // add panels to basepanel
            int maxSliderValue = 500;
            basePanel.setTopInfoPanel(cil);
            basePanel.setViewer(trackV, slider);
            basePanel.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));

            String title = tracks.get(0).getDescription() + " - " + tracks.get(1).getDescription();
            basePanel.setTitlePanel(this.getTitlePanel(title));

            viewController.openTrack2(basePanel);
            return basePanel;
        } else if (tracks.size() == 1) {
            return this.getTrackBasePanel(tracks.get(0), refGen);
        } else {
            throw new UnknownError();
        }
    }

    public BasePanel getAlignmentViewBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create an alignmentviewer
        AlignmentViewer viewer = new AlignmentViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel alignmentPanelLegend = this.getAlignmentViewLegend(viewer);
        viewer.setupLegend(new MenuLabel(alignmentPanelLegend, MenuLabel.TITLE_LEGEND), alignmentPanelLegend);

        // create and set up options (currently normalization)
        JPanel alignmentViewerOptions = this.getAlignmentViewerOptions(viewer);
        MenuLabel optionsLabel = new MenuLabel(alignmentViewerOptions, MenuLabel.TITLE_OPTIONS);
        viewer.setupOptions(optionsLabel, alignmentViewerOptions);

        // add panels to basepanel and add scrollbars
        int maxSliderValue = 500;
        b.setViewerInScrollpane(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, false, maxSliderValue));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    public BasePanel getHistogrammViewerBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a histogram viewer
        HistogramViewer viewer = new HistogramViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel historgramPanelLegend = this.getHistogramViewerLegend();
        viewer.setupLegend(new MenuLabel(historgramPanelLegend, MenuLabel.TITLE_LEGEND), historgramPanelLegend);

        // add panels to basepanel
        int maxSliderValue = 500;
        b.setViewer(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, false, maxSliderValue));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;

    }

    /**
     * @param connector track connector of first track of two sequence pair
     * tracks
     * @return A viewer for sequence pair data
     */
    public BasePanel getSeqPairBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a sequence pair viewer
        SequencePairViewer viewer = new SequencePairViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel seqPairPanelLegend = this.getSeqPairViewerLegend(viewer);
        viewer.setupLegend(new MenuLabel(seqPairPanelLegend, MenuLabel.TITLE_LEGEND), seqPairPanelLegend);

        // add panels to basepanel and add scrollbars
        int maxSliderValue = 50; //smaller than usual
        b.setViewerInScrollpane(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    private AdjustmentPanel createAdjustmentPanel(boolean hasScrollbar, boolean hasSlider, int sliderMax) {
        // create control panel
        BoundsInfo bounds = boundsManager.getUpdatedBoundsInfo(new Dimension(10, 10));
        AdjustmentPanel control = new AdjustmentPanel(1, refGenome.getRefLength(),
                bounds.getCurrentLogPos(), bounds.getZoomValue(), sliderMax, hasScrollbar, hasSlider);
        control.addAdjustmentListener(boundsManager);
        boundsManager.addSynchronousNavigator(control);
        return control;
    }

    private JPanel getTitlePanel(String title) {
        JPanel p = new JPanel();
        p.add(new JLabel(title));
        p.setBackground(ColorProperties.TITLE_BACKGROUND);
        return p;
    }

    /**
     * @param typeColor color of the feature type
     * @param type the feature type whose legend entry is created
     * @param viewer the viewer to which the legend entry belongs. If no
     * function is assigend to the legend entry, viewer can be set to null. In
     * this case a simple label is returned instead of the checkbox.
     * @return A legend entry for a feature type.
     */
    private JPanel getLegendEntry(Color typeColor, FeatureType type, AbstractViewer viewer) {
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEADING));
        entry.setBackground(ColorProperties.LEGEND_BACKGROUND);

        ColorPanel color = new ColorPanel();
        color.setSize(new Dimension(10, 10));
        color.setBackground(typeColor);

        entry.add(color);
        if (viewer != null) {
            entry.add(this.getCheckBox(type, viewer));
        } else {
            entry.add(new JLabel(type.getTypeString()));
        }
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        return entry;
    }

    /**
     * @param type the FeatureType for which the checkbox should be created
     * @param viewer the viewer to which the checkbox belongs
     * @return a check box for the given feature type, connected to the given
     * viewer.
     */
    private JCheckBox getCheckBox(FeatureType type, AbstractViewer viewer) {
        JCheckBox checker = new JCheckBox(type.getTypeString());
        //special cases are handled here
        if (type != FeatureType.UNDEFINED) {
            checker.setSelected(true);
        } else {
            checker.setSelected(false);
        }
        checker.setBackground(ColorProperties.LEGEND_BACKGROUND);
        //strangely next line is needed to ensure correct size of whole legend panel
        checker.setBorder(BorderFactory.createLineBorder(ColorProperties.LEGEND_BACKGROUND));
        checker.addActionListener(new FeatureTypeListener(type, viewer));
        return checker;
    }

    private class ColorPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, this.getSize().width - 1, this.getSize().height - 1);
        }
    }

    private JPanel getGradientEntry(String description) {
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEADING));
        entry.setBackground(ColorProperties.LEGEND_BACKGROUND);

        JPanel color = new JPanel() {
            private static final long serialVersionUID = 1234537;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint whiteToBlack = new GradientPaint(0, 0, Color.WHITE, this.getSize().width - 1, 0, Color.BLACK);
                g2.setPaint(whiteToBlack);
                g2.fill(new Rectangle2D.Double(0, 0, this.getSize().width, this.getSize().height));
                g2.setPaint(null);
                g2.setColor(Color.black);
                g2.drawRect(0, 0, this.getSize().width - 1, this.getSize().height - 1);
            }
        };
        color.setSize(new Dimension(10, 10));
        entry.add(color);

        entry.add(new JLabel(description));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        return entry;
    }

    private JPanel getGenomeViewerLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        legend.setLayout(new BorderLayout());
        legend1.setLayout(new BoxLayout(legend1, BoxLayout.PAGE_AXIS));
        legend2.setLayout(new BoxLayout(legend2, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend1.add(this.getLegendEntry(ColorProperties.CDS, FeatureType.CDS, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.GENE, FeatureType.GENE, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.EXON, FeatureType.EXON, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.REPEAT_UNIT, FeatureType.REPEAT_UNIT, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.MRNA, FeatureType.MRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.MI_RNA, FeatureType.MIRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.RRNA, FeatureType.RRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.TRNA, FeatureType.TRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.MISC_RNA, FeatureType.MISC_RNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.UNDEF_FEATURE, FeatureType.UNDEFINED, viewer));

        legend.add(legend1, BorderLayout.WEST);
        legend.add(legend2, BorderLayout.EAST);

        return legend;
    }

    private JPanel getTrackPanelLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.PERFECT_MATCH, FeatureType.PERFECT_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.BEST_MATCH_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.COMMON_MATCH, FeatureType.COMPLETE_COV, viewer));

        return legend;
    }

    private JPanel getDoubleTrackPanelLegend() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.COV_DIFF_COLOR, FeatureType.COMPLETE_COVERAGE, null));
        legend.add(this.getLegendEntry(ColorProperties.TRACK1_COLOR, FeatureType.TRACK1_COVERAGE, null));
        legend.add(this.getLegendEntry(ColorProperties.TRACK2_COLOR, FeatureType.TRACK2_COVERAGE, null));

        return legend;
    }

    /**
     * @param viewer the track viewer for which the options panel should be
     * created.
     * @return A new options panel for a track viewer.
     */
    private JPanel getTrackPanelOptions(TrackViewer viewer) {
        TrackOptionsPanel options = new TrackOptionsPanel(viewer);
        options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
        options.setBackground(ColorProperties.LEGEND_BACKGROUND);

        return options;
    }

    /**
     * @param viewer the alignment viewer for which the options panel should be
     * created.
     * @return A new options panel for a track viewer.
     */
    private JPanel getAlignmentViewerOptions(AlignmentViewer viewer) {
        AlignmentOptionsPanel options = new AlignmentOptionsPanel(viewer);
        options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
        options.setBackground(ColorProperties.LEGEND_BACKGROUND);

        return options;
    }

    private JPanel getHistogramViewerLegend() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.LOGO_A, FeatureType.BASE_A, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_C, FeatureType.BASE_C, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_G, FeatureType.BASE_G, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_T, FeatureType.BASE_T, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_N, FeatureType.BASE_N, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_MATCH, FeatureType.MATCH, null));
        legend.add(this.getLegendEntry(ColorProperties.LOGO_READGAP, FeatureType.GAP, null));

        return legend;
    }

    private JPanel getAlignmentViewLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.PERFECT_MATCH, FeatureType.PERFECT_MATCH, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.BEST_MATCH, viewer));
        legend.add(this.getLegendEntry(ColorProperties.COMMON_MATCH, FeatureType.ORDINARY_MATCH, viewer));
        legend.add(this.getLegendEntry(ColorProperties.ALIGNMENT_A, FeatureType.DIFF, null));
        legend.add(this.getGradientEntry("Replicates: High to low"));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.NONUNIQUE, viewer));
        return legend;
    }

    private JPanel getSeqPairViewerLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.BLOCK_PERFECT, FeatureType.PERFECT_PAIR, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BLOCK_DIST_LARGE, FeatureType.DISTORTED_PAIR, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BLOCK_UNPAIRED, FeatureType.SINGLE_MAPPING, viewer));
        legend.add(this.getGradientEntry("Perfect to best to common mappings"));

        return legend;
    }

    /**
     * A feature type listener adds or removes the feature type associated with
     * it to to/from the excluded feature list of its associated viewer. Needs
     * an AbstractButton as source, in order to determine if the button was
     * selected or not.
     */
    private class FeatureTypeListener implements ActionListener {

        FeatureType featureType;
        AbstractViewer viewer;

        /**
         * A feature type listener adds or removes the feature type associated
         * with it to to/from the excluded feature list of its associated
         * viewer. Needs an AbstractButton as source, in order to determine if
         * the button was selected or not.
         *
         * @param featureType the feature type handled by this listener
         * @param viewer the viewer whose excluded feature list should be
         * updated
         */
        public FeatureTypeListener(FeatureType featureType, AbstractViewer viewer) {
            this.featureType = featureType;
            this.viewer = viewer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (((AbstractButton) e.getSource()).isSelected()) {
                this.viewer.getExcludedFeatureTypes().remove(this.featureType);
            } else {
                this.viewer.getExcludedFeatureTypes().add(this.featureType);
            }
            this.viewer.boundsChangedHook();
            this.viewer.repaint();
        }
    }
}
