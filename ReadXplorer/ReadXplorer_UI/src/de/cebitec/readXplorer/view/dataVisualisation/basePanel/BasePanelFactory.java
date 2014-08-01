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
package de.cebitec.readXplorer.view.dataVisualisation.basePanel;

import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfo;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.MenuLabel;
import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.AlignmentOptionsPanel;
import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.AlignmentViewer;
import de.cebitec.readXplorer.view.dataVisualisation.histogramViewer.HistogramViewer;
import de.cebitec.readXplorer.view.dataVisualisation.readPairViewer.ReadPairViewer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.CoverageZoomSlider;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.MultipleTrackViewer;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackOptionsPanel;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;
import de.cebitec.readXplorer.view.dialogMenus.ChromosomeVisualizationHelper;
import de.cebitec.readXplorer.view.dialogMenus.ChromosomeVisualizationHelper.ChromosomeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Factory used to initialize all different kinds of base panels.
 *
 * @author ddoppmeier, rhilker
 */
public class BasePanelFactory {

    private BoundsInfoManager boundsManager;
    private PersistantReference refGenome;
    private ViewController viewController;

    /**
     * Factory used to initialize all different kinds of base panels.
     * @param boundsManager the bounds info manager keeping track of the bounds
     * of all viewers associated with it.
     * @param viewController the view controller for all viewers generated with
     * this BasePanelFactory
     */
    public BasePanelFactory(BoundsInfoManager boundsManager, ViewController viewController) {
        this.boundsManager = boundsManager;
        this.viewController = viewController;
    }
    
    public BasePanel getGenericBasePanel(boolean hasScrollbar, boolean hasSlider, boolean hasTitle,
            String title) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // add panels to basepanel
        int maxSliderValue = 500;
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(hasScrollbar, hasSlider, maxSliderValue));
        if (hasTitle) {
            b.setTitlePanel(this.getTitlePanel(title));
        }
        return b;
    }

    /**
     * Creates a base panel for reference sequences (genomes).
     * @param refGenome the reference genome to visualize
     * @return a base panel for reference sequences (genomes).
     */
    public BasePanel getRefViewerBasePanel(PersistantReference refGenome) {

        this.refGenome = refGenome;
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);
        int maxSliderValue = 500;
        AdjustmentPanel adjustmentPanel = this.createAdjustmentPanel(true, true, maxSliderValue);

        // create viewer
        ReferenceViewer refViewer = new ReferenceViewer(boundsManager, b, refGenome);

        // show a legend
        JPanel genomePanelLegend = this.getRefViewerLegend(refViewer);
        refViewer.setupLegend(new MenuLabel(genomePanelLegend, MenuLabel.TITLE_LEGEND), genomePanelLegend);
        
        //show chromosome selection panel
        if (refGenome.getNoChromosomes() > 1) {
            JPanel chromSelectionPanel = this.getRefChromSelectionPanel(refViewer, adjustmentPanel);
            refViewer.setupChromSelectionPanel(chromSelectionPanel);
        }

        // add panels to basepanel
        b.setViewer(refViewer);
        b.setHorizontalAdjustmentPanel(adjustmentPanel);

        return b;
    }

    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track).
     * @param track the track to visualize on this <code>BasePanel</code>.
     * @param refGen the reference genome of this track
     * @return a base panel for a mapping data set (track).
     */
    public BasePanel getTrackBasePanel(PersistantTrack track, PersistantReference refGen) {

        final BasePanel basePanel = new BasePanel(boundsManager, viewController);
        basePanel.setName(track.getDescription());
        viewController.addMousePositionListener(basePanel);

        // create track viewer
        TrackConnector tc;
        try {
            tc = (new SaveFileFetcherForGUI()).getTrackConnector(track);
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        if (tc != null) {
            final TrackViewer trackV = new TrackViewer(boundsManager, basePanel, refGen, tc, false);
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
//            CoverageInfoLabel cil = new CoverageInfoLabel();
//            trackV.setTrackInfoPanel(cil);

            // create zoom slider
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

            // add panels to basepanel
            int maxSliderValue = 500;
//            basePanel.setTopInfoPanel(cil); //coverage info panel, which we don't show anymore
            basePanel.setViewer(trackV, slider);
            basePanel.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));
            basePanel.setTitlePanel(this.getTitlePanel(track.getDescription()));

            return basePanel;
        } else {
            return null;
        }
    }

    /**
     * Method to get one <code>BasePanel</code> for multiple tracks. Only 2 
     * tracks at once are currently supported for the double track viewer.
     * @param tracks to visualize on this <code>BasePanel</code>.
     * @param refGen reference the tracks belong to.
     * @param combineTracks true, if the coverage of two or more tracks should
     * be combined
     * @return A <code>BasePanel</code> for multiple tracks.
     */
    public BasePanel getMultipleTracksBasePanel(List<PersistantTrack> tracks, PersistantReference refGen, boolean combineTracks) {
        if (tracks.size() > 2 && !combineTracks) {
            throw new UnsupportedOperationException("More than two tracks not supported in non-combined mode.");
        } else if (tracks.size() == 2 && !combineTracks || combineTracks) {
            BasePanel basePanel = new BasePanel(boundsManager, viewController);
            viewController.addMousePositionListener(basePanel);

            // get double track connector           
            TrackConnector trackCon;
            try {
                trackCon = (new SaveFileFetcherForGUI()).getTrackConnector(tracks, combineTracks);
            } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                return null;
            }

            MultipleTrackViewer trackV = new MultipleTrackViewer(boundsManager, basePanel, refGen, trackCon, combineTracks);

            // create and set up legend
            JPanel trackPanelLegend;
            if (combineTracks) {
                trackPanelLegend = this.getTrackPanelLegend(trackV);
            } else {
                trackPanelLegend = this.getDoubleTrackPanelLegend(trackV);
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

//            // create info panel
//            CoverageInfoLabel cil = new CoverageInfoLabel();
//            cil.setCombineTracks(combineTracks);
//            cil.renameFields();
//            trackV.setTrackInfoPanel(cil);

            // create zoom slider
            CoverageZoomSlider slider = new CoverageZoomSlider(trackV);

            // add panels to basepanel
            int maxSliderValue = 500;
//            basePanel.setTopInfoPanel(cil);
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

    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track),
     * visualized in an alignment viewer.
     * @param connector the track connector of the track to visualize
     * @return A <code>BasePanel</code> containing an <code>AlignmentViewer</code>
     */
    public BasePanel getAlignmentViewBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create an alignmentviewer
        AlignmentViewer viewer = new AlignmentViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel alignmentPanelLegend = this.getAlignmentViewLegend(viewer);
        MenuLabel legendLabel = new MenuLabel(alignmentPanelLegend, MenuLabel.TITLE_LEGEND);
        viewer.setupLegend(legendLabel, alignmentPanelLegend);

        // create and set up options (currently normalization)
        JPanel alignmentViewerOptions = this.getAlignmentViewerOptions(viewer);
        MenuLabel optionsLabel = new MenuLabel(alignmentViewerOptions, MenuLabel.TITLE_OPTIONS);
        viewer.setupOptions(optionsLabel, alignmentViewerOptions);
        
        //assign observers to handle visualization correctly
        legendLabel.registerObserver(optionsLabel);
        optionsLabel.registerObserver(legendLabel);

        // add panels to basepanel and add scrollbars
        int maxSliderValue = 500;
        b.setViewerInScrollpane(viewer);
        viewer.createListenerForScrollBar();
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, false, maxSliderValue));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track), whose
     * coverage is visualized in a <code>HistogramViewer</code>.
     * @param connector the track connector of the track to visualize
     * @return A <code>BasePanel</code> containing a <code>HistogramViewer</code>
     */
    public BasePanel getHistogrammViewerBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a histogram viewer
        HistogramViewer viewer = new HistogramViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel historgramPanelLegend = this.getHistogramViewerLegend(viewer);
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
    public BasePanel getReadPairBasePanel(TrackConnector connector) {
        BasePanel b = new BasePanel(boundsManager, viewController);
        viewController.addMousePositionListener(b);

        // create a sequence pair viewer
        ReadPairViewer viewer = new ReadPairViewer(boundsManager, b, refGenome, connector);

        // create a legend
        JPanel seqPairPanelLegend = this.getReadPairViewerLegend(viewer);
        viewer.setupLegend(new MenuLabel(seqPairPanelLegend, MenuLabel.TITLE_LEGEND), seqPairPanelLegend);

        // add panels to basepanel and add scrollbars
        int maxSliderValue = 50; //smaller than usual
        b.setViewerInScrollpane(viewer);
        b.setHorizontalAdjustmentPanel(this.createAdjustmentPanel(true, true, maxSliderValue));
        b.setTitlePanel(this.getTitlePanel(connector.getAssociatedTrackName()));

        return b;
    }

    /**
     * Create an <code>AdjustmentPanel</code> for the given parameters. This
     * panel may contain a scrollbar for scrolling along a reference and 
     * a slider for zooming in and out.
     * @param hasScrollbar true, if a scrollbar for the reference sequence is 
     * needed, false otherwise
     * @param hasSlider true, if a zoom slider is needed, false otherwise
     * @param sliderMax maximum slider value
     * @return <code>AdjustmentPanel</code> for the given parameters.
     */
    private AdjustmentPanel createAdjustmentPanel(boolean hasScrollbar, boolean hasSlider, int sliderMax) {
        // create control panel
        BoundsInfo bounds = boundsManager.getUpdatedBoundsInfo(new Dimension(10, 10));
        AdjustmentPanel control = new AdjustmentPanel(1, refGenome.getActiveChromLength(),
                bounds.getCurrentLogPos(), bounds.getZoomValue(), sliderMax, hasScrollbar, hasSlider);
        control.addAdjustmentListener(boundsManager);
        boundsManager.addSynchronousNavigator(control);
        return control;
    }

    /**
     * @param title a title to display  on a panel
     * @return The panel displaying the title on a gray background
     */
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
            entry.add(this.getFeatureTypeBox(type, viewer));
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
    private JCheckBox getFeatureTypeBox(FeatureType type, AbstractViewer viewer) {
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

    /**
     * Creates a legend JPanel for the reference viewer.
     * @param viewer the viewer to which the legend shall be added.
     * @return the new legend Panel for the reference viewer.
     */
    private JPanel getRefViewerLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        JPanel legend3 = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.X_AXIS));
        legend1.setLayout(new BoxLayout(legend1, BoxLayout.PAGE_AXIS));
        legend2.setLayout(new BoxLayout(legend2, BoxLayout.PAGE_AXIS));
        legend3.setLayout(new BoxLayout(legend3, BoxLayout.Y_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend1.add(this.getLegendEntry(ColorProperties.CDS, FeatureType.CDS, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.GENE, FeatureType.GENE, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.EXON, FeatureType.EXON, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.REPEAT_UNIT, FeatureType.REPEAT_UNIT, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.MRNA, FeatureType.MRNA, viewer));
        legend1.add(this.getLegendEntry(ColorProperties.MI_RNA, FeatureType.MIRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.RRNA, FeatureType.RRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.TRNA, FeatureType.TRNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.MISC_RNA, FeatureType.MISC_RNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.NC_RNA, FeatureType.NC_RNA, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.FIVE_UTR, FeatureType.FIVE_UTR, viewer));
        legend2.add(this.getLegendEntry(ColorProperties.THREE_UTR, FeatureType.THREE_UTR, viewer));
        legend3.add(this.getLegendEntry(ColorProperties.RBS, FeatureType.RBS, viewer));
        legend3.add(this.getLegendEntry(ColorProperties.MINUS_THIRTYFIVE, FeatureType.MINUS_THIRTYFIVE, viewer));
        legend3.add(this.getLegendEntry(ColorProperties.MINUS_TEN, FeatureType.MINUS_TEN, viewer));
        legend3.add(this.getLegendEntry(ColorProperties.UNDEF_FEATURE, FeatureType.UNDEFINED, viewer));

        legend.add(legend1);
        legend.add(legend2);
        legend.add(legend3);

        return legend;
    }

    /**
     * Creates a legend for a track, which contains entries for the different
     * mapping classes.
     * @param viewer the viewer to which the legend shall be added
     * @return the new track legend panel
     */
    private JPanel getTrackPanelLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.PERFECT_MATCH, FeatureType.PERFECT_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.BEST_MATCH_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.COMMON_MATCH, FeatureType.COMMON_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.MULTIPLE_MAPPED_READ, viewer));

        return legend;
    }

    /**
     * Creates a legend for a double track viewer, which contains entries for 
     * the different track coverages to visualize.
     * @param viewer the viewer to which the legend shall be added
     * @return the new double track legend panel
     */
    private JPanel getDoubleTrackPanelLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.COV_DIFF_COLOR, FeatureType.COMPLETE_COVERAGE, null));
        legend.add(this.getLegendEntry(ColorProperties.TRACK1_COLOR, FeatureType.TRACK1_COVERAGE, null));
        legend.add(this.getLegendEntry(ColorProperties.TRACK2_COLOR, FeatureType.TRACK2_COVERAGE, null));
//        legend.add(this.getLegendEntry(ColorProperties.PERFECT_MATCH, FeatureType.PERFECT_COVERAGE, viewer));
//        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.BEST_MATCH_COVERAGE, viewer));
//        legend.add(this.getLegendEntry(ColorProperties.COMMON_MATCH, FeatureType.COMMON_COVERAGE, viewer));
//        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.MULTIPLE_MAPPED_READ, viewer));

        return legend;
    }
    
    /**
     * Creates a JPanel containing a JComboBox for the selection of the
     * currently active chromosome.
     * @param viewer the viewer, for which the active chromosome shall be
     * controlled.
     * @return A JPanel containing a JComboBox for the selection of the
     * currently active chromosome.
     */
    private JPanel getRefChromSelectionPanel(final AbstractViewer viewer, final AdjustmentPanel adjustmentPanel) {
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(ColorProperties.LEGEND_BACKGROUND);
        
        ChromosomeVisualizationHelper chromHelper = new ChromosomeVisualizationHelper();
        final JComboBox<PersistantChromosome> chromSelectionBox = new JComboBox<>();
        chromHelper.createChromBoxWithObserver(chromSelectionBox, refGenome);
        ChromosomeListener chromListener = chromHelper.new ChromosomeListener(chromSelectionBox, viewer) {
        
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        super.actionPerformed(e);
                        PersistantChromosome activeChrom = (PersistantChromosome) chromSelectionBox.getSelectedItem();
                        adjustmentPanel.setNavigatorMax(activeChrom.getLength());
                    }
        };
        chromSelectionBox.addActionListener(chromListener);
        chromSelectionBox.setSize(chromSelectionBox.getPreferredSize());
        chromSelectionBox.setVisible(true);
        selectionPanel.add(chromSelectionBox, BorderLayout.CENTER);
        selectionPanel.setSize(chromSelectionBox.getPreferredSize());
        
        Observer chromChangeObserver = new Observer() { //observer for chromosome changes from elsewhere

            @Override
            public void update(Object args) {
                chromSelectionBox.setSelectedItem(refGenome.getActiveChromosome());
                chromSelectionBox.repaint();
            }
        };
        refGenome.registerObserver(chromChangeObserver);
        
        return selectionPanel;
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
     * @return A new options panel for an alignment viewer.
     */
    private JPanel getAlignmentViewerOptions(AlignmentViewer viewer) {
        AlignmentOptionsPanel options = new AlignmentOptionsPanel(viewer);
        options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
        options.setBackground(ColorProperties.LEGEND_BACKGROUND);

        return options;
    }

    /**
     * @param viewer the histogram viewer for which the legend panel should be
     * created.
     * @return A new legend panel for a histogram viewer.
     */
    private JPanel getHistogramViewerLegend(AbstractViewer viewer) {
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
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.MULTIPLE_MAPPED_READ, viewer));

        return legend;
    }

    /**
     * @param viewer the alignment viewer for which the legend panel should be
     * created.
     * @return A new legend panel for an alignment viewer.
     */
    private JPanel getAlignmentViewLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.PERFECT_MATCH, FeatureType.PERFECT_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.BEST_MATCH_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.COMMON_MATCH, FeatureType.COMMON_COVERAGE, viewer));
        legend.add(this.getLegendEntry(ColorProperties.MISMATCH_BACKGROUND, FeatureType.DIFF, null));
        legend.add(this.getGradientEntry("Replicates: High to low"));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.MULTIPLE_MAPPED_READ, viewer));
        return legend;
    }

    /**
     * @param viewer the read pair viewer for which the legend panel should be
     * created.
     * @return A new legend panel for a read pair viewer.
     */
    private JPanel getReadPairViewerLegend(AbstractViewer viewer) {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.PAGE_AXIS));
        legend.setBackground(ColorProperties.LEGEND_BACKGROUND);

        legend.add(this.getLegendEntry(ColorProperties.BLOCK_PERFECT, FeatureType.PERFECT_PAIR, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BLOCK_DIST_LARGE, FeatureType.DISTORTED_PAIR, viewer));
        legend.add(this.getLegendEntry(ColorProperties.BLOCK_UNPAIRED, FeatureType.SINGLE_MAPPING, viewer));
        legend.add(this.getGradientEntry("Perfect to best to common mappings"));
        legend.add(this.getLegendEntry(ColorProperties.BEST_MATCH, FeatureType.MULTIPLE_MAPPED_READ, viewer));

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
            this.viewer.setNewDataRequestNeeded(true);
            this.viewer.boundsChangedHook();
            this.viewer.repaint();
        }
    }
}
