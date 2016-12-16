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

package de.cebitec.readxplorer.ui.datavisualisation.basepanel;


import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.api.FileException;
import de.cebitec.readxplorer.api.constants.Colors;
import de.cebitec.readxplorer.api.constants.GUI;
import de.cebitec.readxplorer.api.enums.ComparisonClass;
import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfo;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.MenuLabel;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.AlignmentOptionsPanel;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.AlignmentViewer;
import de.cebitec.readxplorer.ui.datavisualisation.histogramviewer.HistogramViewer;
import de.cebitec.readxplorer.ui.datavisualisation.readpairviewer.ReadPairViewer;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.CoverageZoomSlider;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.DoubleTrackViewer;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.MultipleTrackViewer;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.TrackOptionsPanel;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.TrackViewer;
import de.cebitec.readxplorer.ui.dialogmenus.ChromosomeVisualizationHelper;
import de.cebitec.readxplorer.ui.dialogmenus.ChromosomeVisualizationHelper.ChromosomeListener;
import de.cebitec.readxplorer.utils.ColorUtils;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


/**
 * Factory used to initialize all different kinds of base panels.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class BasePanelFactory {

    private final Preferences pref = NbPreferences.forModule( Object.class );
    private final BoundsInfoManager boundsManager;
    private PersistentReference refGenome;
    private final ViewController viewController;


    /**
     * Factory used to initialize all different kinds of base panels.
     * <p>
     * @param boundsManager  the bounds info manager keeping track of the bounds
     *                       of all viewers associated with it.
     * @param viewController the view controller for all viewers generated with
     *                       this BasePanelFactory
     */
    public BasePanelFactory( BoundsInfoManager boundsManager, ViewController viewController ) {
        this.boundsManager = boundsManager;
        this.viewController = viewController;
    }


    public BasePanel getGenericBasePanel( boolean hasScrollbar, boolean hasSlider, boolean hasTitle,
                                          String title ) {
        BasePanel b = new BasePanel( boundsManager, viewController );
        viewController.addMousePositionListener( b );

        // add panels to basepanel
        int maxSliderValue = 500;
        b.setHorizontalAdjustmentPanel( this.createAdjustmentPanel( hasScrollbar, hasSlider, maxSliderValue ) );
        if( hasTitle ) {
            b.setTitlePanel( this.getTitlePanel( title ) );
        }
        return b;
    }


    /**
     * Creates a base panel for reference sequences (genomes).
     * <p>
     * @param refGenome the reference genome to visualize
     * <p>
     * @return a base panel for reference sequences (genomes).
     */
    public BasePanel getRefViewerBasePanel( PersistentReference refGenome ) {

        this.refGenome = refGenome;
        BasePanel b = new BasePanel( boundsManager, viewController );
        viewController.addMousePositionListener( b );
        int maxSliderValue = 500;
        AdjustmentPanel adjustmentPanel = this.createAdjustmentPanel( true, true, maxSliderValue );

        // create viewer
        ReferenceViewer refViewer = new ReferenceViewer( boundsManager, b, refGenome );

        // show a legend
        JPanel genomePanelLegend = this.getRefViewerLegend( refViewer );
        refViewer.setupLegend( new MenuLabel( genomePanelLegend, MenuLabel.TITLE_LEGEND ), genomePanelLegend );

        //show chromosome selection panel
        if( refGenome.getNoChromosomes() > 1 ) {
            JPanel chromSelectionPanel = this.getRefChromSelectionPanel( refViewer, adjustmentPanel );
            refViewer.setupChromSelectionPanel( chromSelectionPanel );
        }

        // add panels to basepanel
        b.setViewer( refViewer );
        b.setHorizontalAdjustmentPanel( adjustmentPanel );

        return b;
    }


    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track).
     * <p>
     * @param track  the track to visualize on this <code>BasePanel</code>.
     * @param refGen the reference genome of this track
     * <p>
     * @return a base panel for a mapping data set (track).
     */
    public BasePanel getTrackBasePanel( PersistentTrack track, PersistentReference refGen ) {

        final BasePanel basePanel = new BasePanel( boundsManager, viewController );
        basePanel.setName( track.getDescription() );
        viewController.addMousePositionListener( basePanel );

        // create track viewer
        TrackConnector tc = null;
        try {
            tc = (new SaveFileFetcherForGUI()).getTrackConnector( track );
        } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
            String msg = "You did not complete the track path selection. The track panel cannot be opened.";
            ErrorHelper.getHandler().handle( new FileException( msg, ex ), "Error resolving path to track" );
            return null;
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }
        if( tc != null ) {
            final TrackViewer trackV = new TrackViewer( boundsManager, basePanel, refGen, tc, false );
            trackV.setName( track.getDescription() );

            this.initializeLegendAndOptions( basePanel, trackV, false );
            basePanel.setTitlePanel( this.getTitlePanel( track.getDescription() ) );

            return basePanel;
        } else {
            return null;
        }
    }


    /**
     * Method to get one <code>BasePanel</code> for multiple tracks. Only 2
     * tracks at once are currently supported for the double track viewer.
     * <p>
     * @param tracks        to visualize on this <code>BasePanel</code>.
     * @param refGen        reference the tracks belong to.
     * @param combineTracks true, if the coverage of two or more tracks should
     *                      be combined
     * <p>
     * @return A <code>BasePanel</code> for multiple tracks.
     */
    public BasePanel getMultipleTracksBasePanel( List<PersistentTrack> tracks, PersistentReference refGen, boolean combineTracks ) {
        if( tracks.size() > 2 && !combineTracks ) {
            throw new UnsupportedOperationException( "More than two tracks not supported in non-combined mode." );
        } else if( tracks.size() == 2 && !combineTracks || combineTracks ) {
            BasePanel basePanel = new BasePanel( boundsManager, viewController );
            viewController.addMousePositionListener( basePanel );

            // get double track connector
            TrackConnector trackCon;
            try {
                trackCon = (new SaveFileFetcherForGUI()).getTrackConnector( tracks, combineTracks );
            } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                String msg = "You did not complete the track path selection. The track panel cannot be opened.";
                ErrorHelper.getHandler().handle( new FileException( msg, ex ), "Error resolving path to track" );
                return null;
            } catch( DatabaseException ex ) {
                ErrorHelper.getHandler().handle( ex );
                return null;
            }

            TrackViewer trackV;
            if( combineTracks ) {
                trackV = new MultipleTrackViewer( boundsManager, basePanel, refGen, trackCon, combineTracks );
                String viewerName = "Combined: " + tracks.get( 0 ).getDescription() + tracks.get( 1 ).getDescription();
                if( tracks.size() > 2 ) {
                    viewerName += ",...";
                }
                trackV.setName( viewerName );
            } else {
                trackV = new DoubleTrackViewer( boundsManager, basePanel, refGen, trackCon );
                trackV.setName( "Double Track: " + tracks.get( 0 ).getDescription() + tracks.get( 1 ).getDescription() );
            }

            this.initializeLegendAndOptions( basePanel, trackV, combineTracks );

            String title = GeneralUtils.generateConcatenatedString( trackCon.getAssociatedTrackNames(), 80 );
            basePanel.setTitlePanel( getTitlePanel( title ) );

            viewController.openTrack2( basePanel );
            return basePanel;
        } else if( tracks.size() == 1 ) {
            return this.getTrackBasePanel( tracks.get( 0 ), refGen );
        } else {
            throw new UnknownError();
        }
    }


    /**
     * Initializes the legend and options panel of the track/multiple track
     * viewer.
     * <p>
     * @param basePanel     the base panel with the viewer
     * @param trackV        the track viewer
     * @param combineTracks <code>true</code>, if tracks shall be combined
     */
    private void initializeLegendAndOptions( BasePanel basePanel, TrackViewer trackV, boolean combineTracks ) {
        // create and set up legend
        JPanel trackPanelLegend;
        if( combineTracks || !trackV.isTwoTracks() ) {
            trackPanelLegend = this.getTrackPanelLegend( trackV );
        } else {
            trackPanelLegend = this.getDoubleTrackPanelLegend( trackV );
        }
        MenuLabel legendLabel = new MenuLabel( trackPanelLegend, MenuLabel.TITLE_LEGEND );
        trackV.setupLegend( legendLabel, trackPanelLegend );

        // create and set up options (currently normalization)
        JPanel trackPanelOptions = this.getTrackPanelOptions( trackV );
        MenuLabel optionsLabel = new MenuLabel( trackPanelOptions, MenuLabel.TITLE_OPTIONS );
        trackV.setupOptions( optionsLabel, trackPanelOptions );

        //assign observers to handle visualization correctly
        legendLabel.registerObserver( optionsLabel );
        optionsLabel.registerObserver( legendLabel );

        // create zoom slider
        CoverageZoomSlider slider = new CoverageZoomSlider( trackV );

        // add panels to basepanel
        int maxSliderValue = 500;
        basePanel.setViewer( trackV, slider );
        basePanel.setHorizontalAdjustmentPanel( this.createAdjustmentPanel( true, true, maxSliderValue ) );
    }


    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track),
     * visualized in an alignment viewer.
     * <p>
     * @param connector the track connector of the track to visualize
     * <p>
     * @return A <code>BasePanel</code> containing an
     *         <code>AlignmentViewer</code>
     */
    public BasePanel getAlignmentViewBasePanel( TrackConnector connector ) {
        BasePanel b = new BasePanel( boundsManager, viewController );
        viewController.addMousePositionListener( b );

        // create an alignmentviewer
        AlignmentViewer viewer = new AlignmentViewer( boundsManager, b, refGenome, connector );

        // create a legend
        JPanel alignmentPanelLegend = this.getAlignmentViewLegend( viewer );
        MenuLabel legendLabel = new MenuLabel( alignmentPanelLegend, MenuLabel.TITLE_LEGEND );
        viewer.setupLegend( legendLabel, alignmentPanelLegend );

        // create and set up options (currently normalization)
        JPanel alignmentViewerOptions = this.getAlignmentViewerOptions( viewer );
        MenuLabel optionsLabel = new MenuLabel( alignmentViewerOptions, MenuLabel.TITLE_OPTIONS );
        viewer.setupOptions( optionsLabel, alignmentViewerOptions );

        //assign observers to handle visualization correctly
        legendLabel.registerObserver( optionsLabel );
        optionsLabel.registerObserver( legendLabel );

        // add panels to basepanel and add scrollbars
        int maxSliderValue = pref.getInt( GUI.MAX_ZOOM, GUI.DEFAULT_ZOOM );
        viewer.setMaxZoomValue( maxSliderValue );
        b.setViewerInScrollpane( viewer );
        viewer.createListenerForScrollBar();
        b.setHorizontalAdjustmentPanel( this.createAdjustmentPanel( true, true, maxSliderValue ) );
        String title = GeneralUtils.generateConcatenatedString( connector.getAssociatedTrackNames(), 80 );
        b.setTitlePanel( getTitlePanel( title ) );

        return b;
    }


    /**
     * Creates a <code>BasePanel</code> for a mapping data set (track), whose
     * coverage is visualized in a <code>HistogramViewer</code>.
     * <p>
     * @param connector the track connector of the track to visualize
     * <p>
     * @return A <code>BasePanel</code> containing a
     *         <code>HistogramViewer</code>
     */
    public BasePanel getHistogrammViewerBasePanel( TrackConnector connector ) {
        BasePanel b = new BasePanel( boundsManager, viewController );
        viewController.addMousePositionListener( b );

        // create a histogram viewer
        HistogramViewer viewer = new HistogramViewer( boundsManager, b, refGenome, connector );

        // create a legend
        JPanel historgramPanelLegend = this.getHistogramViewerLegend( viewer );
        viewer.setupLegend( new MenuLabel( historgramPanelLegend, MenuLabel.TITLE_LEGEND ), historgramPanelLegend );

        // add panels to basepanel
        int maxSliderValue = 500;
        b.setViewer( viewer );
        b.setHorizontalAdjustmentPanel( this.createAdjustmentPanel( true, false, maxSliderValue ) );
        b.setTitlePanel( this.getTitlePanel( connector.getAssociatedTrackName() ) );

        return b;

    }


    /**
     * @param connector track connector of first track of two sequence pair
     *                  tracks
     * <p>
     * @return A viewer for sequence pair data
     */
    public BasePanel getReadPairBasePanel( TrackConnector connector ) {
        BasePanel b = new BasePanel( boundsManager, viewController );
        viewController.addMousePositionListener( b );

        // create a sequence pair viewer
        ReadPairViewer viewer = new ReadPairViewer( boundsManager, b, refGenome, connector );

        // create a legend
        JPanel seqPairPanelLegend = this.getReadPairViewerLegend( viewer );
        viewer.setupLegend( new MenuLabel( seqPairPanelLegend, MenuLabel.TITLE_LEGEND ), seqPairPanelLegend );

        // add panels to basepanel and add scrollbars
        int maxSliderValue = pref.getInt( GUI.MAX_ZOOM, GUI.DEFAULT_ZOOM ); //smaller than usual
        viewer.setMaxZoomValue( maxSliderValue );
        b.setViewerInScrollpane( viewer );
        b.setHorizontalAdjustmentPanel( this.createAdjustmentPanel( true, true, maxSliderValue ) );
        b.setTitlePanel( this.getTitlePanel( connector.getAssociatedTrackName() ) );

        return b;
    }


    /**
     * Create an <code>AdjustmentPanel</code> for the given parameters. This
     * panel may contain a scrollbar for scrolling along a reference and a
     * slider for zooming in and out.
     * <p>
     * @param hasScrollbar true, if a scrollbar for the reference sequence is
     *                     needed, false otherwise
     * @param hasSlider    true, if a zoom slider is needed, false otherwise
     * @param sliderMax    maximum slider value
     * <p>
     * @return <code>AdjustmentPanel</code> for the given parameters.
     */
    private AdjustmentPanel createAdjustmentPanel( boolean hasScrollbar, boolean hasSlider, int sliderMax ) {
        // create control panel
        BoundsInfo bounds = boundsManager.getUpdatedBoundsInfo( new Dimension( 10, 10 ) );
        AdjustmentPanel control = new AdjustmentPanel( 1, refGenome.getActiveChromLength(),
                                                       bounds.getCurrentLogPos(), bounds.getZoomValue(), sliderMax, hasScrollbar, hasSlider );
        control.addAdjustmentListener( boundsManager );
        boundsManager.addSynchronousNavigator( control );
        return control;
    }


    /**
     * @param title a title to display on a panel
     * <p>
     * @return The panel displaying the title on a gray background
     */
    private JPanel getTitlePanel( String title ) {
        JPanel p = new JPanel( new GridBagLayout() );
        p.add( new JLabel( title ) );
        p.setBackground( Colors.TITLE_BACKGROUND );
        p.setPreferredSize( new Dimension( p.getPreferredSize().width, 18 ) );
        return p;
    }


    /**
     * @param typeColor color of the feature type
     * @param type      the feature type whose legend entry is created
     * @param viewer    the viewer to which the legend entry belongs. If no
     *                  function is assigend to the legend entry, viewer can be
     *                  set to null. In this case a simple label is returned
     *                  instead of the checkbox.
     * <p>
     * @return A legend entry for a feature type.
     */
    private JPanel getLegendEntry( Color typeColor, final Classification type, AbstractViewer viewer ) {
        JPanel entry = new JPanel( new FlowLayout( FlowLayout.LEADING ) );
        entry.setBackground( Colors.LEGEND_BACKGROUND );

        final ColorPanel colorPanel = new ColorPanel();
        colorPanel.setSize( new Dimension( 10, 10 ) );
        colorPanel.setBackground( typeColor );
        pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                Map<Classification, Color> classificationColors = ColorUtils.updateClassificationColors();
                if( classificationColors.containsKey( type ) ) {
                    colorPanel.setBackground( classificationColors.get( type ) );
                    colorPanel.repaint();
                }
            }


        } );

        entry.add( colorPanel );
        if( viewer != null ) {
            entry.add( this.getFeatureTypeBox( type, viewer ) );
        } else {
            entry.add( new JLabel( type.toString() ) );
        }
        entry.setAlignmentX( Component.LEFT_ALIGNMENT );
        return entry;
    }


    /**
     * @param type   the feature type whose legend entry is created
     * @param viewer the viewer to which the legend entry belongs. If no
     *               function is assigend to the legend entry, viewer can be set
     *               to null. In this case a simple label is returned instead of
     *               the checkbox.
     * <p>
     * @return A legend entry for a feature type.
     */
    private JPanel getLegendEntry( Classification type, AbstractViewer viewer ) {
        Map<Classification, Color> classToColorMap = ColorUtils.updateClassificationColors();
        return this.getLegendEntry( classToColorMap.get( type ), type, viewer );
    }


    /**
     * @param type   the FeatureType for which the checkbox should be created
     * @param viewer the viewer to which the checkbox belongs
     * <p>
     * @return a check box for the given feature type, connected to the given
     *         viewer.
     */
    private JCheckBox getFeatureTypeBox( Classification type, AbstractViewer viewer ) {
        JCheckBox checker = new JCheckBox( type.toString() );
        //special cases are handled here
        if( type != FeatureType.UNDEFINED ) {
            checker.setSelected( true );
        } else {
            checker.setSelected( false );
        }
        checker.setBackground( Colors.LEGEND_BACKGROUND );
        //strangely next line is needed to ensure correct size of whole legend panel
        checker.setBorder( BorderFactory.createLineBorder( Colors.LEGEND_BACKGROUND ) );
        checker.addActionListener( new FeatureTypeListener( type, viewer ) );
        return checker;
    }


    private class ColorPanel extends JPanel {

        private static final long serialVersionUID = 1L;


        @Override
        public void paintComponent( Graphics g ) {
            super.paintComponent( g );
            g.setColor( Color.BLACK );
            g.drawRect( 0, 0, this.getSize().width - 1, this.getSize().height - 1 );
        }


    }


    private JPanel getGradientEntry( String description ) {
        JPanel entry = new JPanel( new FlowLayout( FlowLayout.LEADING ) );
        entry.setBackground( Colors.LEGEND_BACKGROUND );

        JPanel color = new JPanel() {
            private static final long serialVersionUID = 1234537;


            @Override
            protected void paintComponent( Graphics g ) {
                super.paintComponent( g );
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint whiteToBlack = new GradientPaint( 0, 0, Color.WHITE, this.getSize().width - 1, 0, Color.BLACK );
                g2.setPaint( whiteToBlack );
                g2.fill( new Rectangle2D.Double( 0, 0, this.getSize().width, this.getSize().height ) );
                g2.setPaint( null );
                g2.setColor( Color.black );
                g2.drawRect( 0, 0, this.getSize().width - 1, this.getSize().height - 1 );
            }


        };
        color.setSize( new Dimension( 10, 10 ) );
        entry.add( color );

        entry.add( new JLabel( description ) );
        entry.setAlignmentX( Component.LEFT_ALIGNMENT );
        return entry;
    }


    /**
     * Creates a legend JPanel for the reference viewer.
     * <p>
     * @param viewer the viewer to which the legend shall be added.
     * <p>
     * @return the new legend Panel for the reference viewer.
     */
    private JPanel getRefViewerLegend( AbstractViewer viewer ) {
        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        JPanel legend3 = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.X_AXIS ) );
        legend1.setLayout( new BoxLayout( legend1, BoxLayout.PAGE_AXIS ) );
        legend2.setLayout( new BoxLayout( legend2, BoxLayout.PAGE_AXIS ) );
        legend3.setLayout( new BoxLayout( legend3, BoxLayout.Y_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend1.add( this.getLegendEntry( Colors.CDS, FeatureType.CDS, viewer ) );
        legend1.add( this.getLegendEntry( Colors.GENE, FeatureType.GENE, viewer ) );
        legend1.add( this.getLegendEntry( Colors.EXON, FeatureType.EXON, viewer ) );
        legend1.add( this.getLegendEntry( Colors.REPEAT_UNIT, FeatureType.REPEAT_UNIT, viewer ) );
        legend1.add( this.getLegendEntry( Colors.MRNA, FeatureType.MRNA, viewer ) );
        legend1.add( this.getLegendEntry( Colors.MI_RNA, FeatureType.MIRNA, viewer ) );
        legend2.add( this.getLegendEntry( Colors.RRNA, FeatureType.RRNA, viewer ) );
        legend2.add( this.getLegendEntry( Colors.TRNA, FeatureType.TRNA, viewer ) );
        legend2.add( this.getLegendEntry( Colors.MISC_RNA, FeatureType.MISC_RNA, viewer ) );
        legend2.add( this.getLegendEntry( Colors.NC_RNA, FeatureType.NC_RNA, viewer ) );
        legend2.add( this.getLegendEntry( Colors.FIVE_UTR, FeatureType.FIVE_UTR, viewer ) );
        legend2.add( this.getLegendEntry( Colors.THREE_UTR, FeatureType.THREE_UTR, viewer ) );
        legend3.add( this.getLegendEntry( Colors.RBS, FeatureType.RBS, viewer ) );
        legend3.add( this.getLegendEntry( Colors.MINUS_THIRTYFIVE, FeatureType.MINUS_THIRTYFIVE, viewer ) );
        legend3.add( this.getLegendEntry( Colors.MINUS_TEN, FeatureType.MINUS_TEN, viewer ) );
        legend3.add( this.getLegendEntry( Colors.TRANSCRIPT, FeatureType.TRANSCRIPT, viewer ) );
        legend3.add( this.getLegendEntry( Colors.UNDEF_FEATURE, FeatureType.UNDEFINED, viewer ) );

        legend.add( legend1 );
        legend.add( legend2 );
        legend.add( legend3 );

        return legend;
    }


    /**
     * Creates a legend for a track, which contains entries for the different
     * mapping classes.
     * <p>
     * @param viewer the viewer to which the legend shall be added
     * <p>
     * @return the new track legend panel
     */
    private JPanel getTrackPanelLegend( AbstractViewer viewer ) {
        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.X_AXIS ) );
        legend1.setLayout( new BoxLayout( legend1, BoxLayout.PAGE_AXIS ) );
        legend2.setLayout( new BoxLayout( legend2, BoxLayout.PAGE_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend1.add( this.getLegendEntry( MappingClass.PERFECT_MATCH, viewer ) );
        legend1.add( this.getLegendEntry( MappingClass.BEST_MATCH, viewer ) );
        legend1.add( this.getLegendEntry( MappingClass.COMMON_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_PERFECT_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_BEST_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( Color.white, FeatureType.MULTIPLE_MAPPED_READ, viewer ) );

        legend.add( legend1 );
        legend.add( legend2 );

        return legend;
    }


    /**
     * Creates a legend for a double track viewer, which contains entries for
     * the different track coverages to visualize.
     * <p>
     * @param viewer the viewer to which the legend shall be added
     * <p>
     * @return the new double track legend panel
     */
    private JPanel getDoubleTrackPanelLegend( AbstractViewer viewer ) {
        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        JPanel legend3 = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.X_AXIS ) );
        legend1.setLayout( new BoxLayout( legend1, BoxLayout.PAGE_AXIS ) );
        legend2.setLayout( new BoxLayout( legend2, BoxLayout.PAGE_AXIS ) );
        legend3.setLayout( new BoxLayout( legend3, BoxLayout.Y_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend1.add( this.getLegendEntry( ComparisonClass.DIFF_COVERAGE, null ) );
        legend1.add( this.getLegendEntry( ComparisonClass.TRACK1_COVERAGE, null ) );
        legend1.add( this.getLegendEntry( ComparisonClass.TRACK2_COVERAGE, null ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_PERFECT_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.PERFECT_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_BEST_MATCH, viewer ) );
        legend3.add( this.getLegendEntry( MappingClass.BEST_MATCH, viewer ) );
        legend3.add( this.getLegendEntry( MappingClass.COMMON_MATCH, viewer ) );
        legend3.add( this.getLegendEntry( Color.white, FeatureType.MULTIPLE_MAPPED_READ, viewer ) );

        legend.add( legend1 );
        legend.add( legend2 );
        legend.add( legend3 );

        return legend;
    }


    /**
     * Creates a JPanel containing a JComboBox for the selection of the
     * currently active chromosome.
     * <p>
     * @param viewer the viewer, for which the active chromosome shall be
     *               controlled.
     * <p>
     * @return A JPanel containing a JComboBox for the selection of the
     *         currently active chromosome.
     */
    private JPanel getRefChromSelectionPanel( final AbstractViewer viewer, final AdjustmentPanel adjustmentPanel ) {
        JPanel selectionPanel = new JPanel( new BorderLayout() );
        selectionPanel.setBackground( Colors.LEGEND_BACKGROUND );

        ChromosomeVisualizationHelper chromHelper = new ChromosomeVisualizationHelper();
        final JComboBox<PersistentChromosome> chromSelectionBox = new JComboBox<>();
        chromHelper.createChromBoxWithObserver( chromSelectionBox, refGenome );
        ChromosomeListener chromListener = chromHelper.new ChromosomeListener( chromSelectionBox, viewer ) {

            @Override
            public void actionPerformed( ActionEvent e ) {
                super.actionPerformed( e );
                PersistentChromosome activeChrom = (PersistentChromosome) chromSelectionBox.getSelectedItem();
                adjustmentPanel.setNavigatorMax( activeChrom.getLength() );
            }


        };
        chromSelectionBox.addActionListener( chromListener );
        chromSelectionBox.setSize( chromSelectionBox.getPreferredSize() );
        chromSelectionBox.setVisible( true );
        selectionPanel.add( chromSelectionBox, BorderLayout.CENTER );
        selectionPanel.setSize( chromSelectionBox.getPreferredSize() );

        Observer chromChangeObserver = new Observer() { //observer for chromosome changes from elsewhere

            @Override
            public void update( Object args ) {
                chromSelectionBox.setSelectedItem( refGenome.getActiveChromosome() );
                chromSelectionBox.repaint();
            }


        };
        refGenome.registerObserver( chromChangeObserver );

        return selectionPanel;
    }


    /**
     * @param viewer the track viewer for which the options panel should be
     *               created.
     * <p>
     * @return A new options panel for a track viewer.
     */
    private JPanel getTrackPanelOptions( TrackViewer viewer ) {
        TrackOptionsPanel options = new TrackOptionsPanel( viewer );
        options.setLayout( new BoxLayout( options, BoxLayout.PAGE_AXIS ) );
        options.setBackground( Colors.LEGEND_BACKGROUND );

        return options;
    }


    /**
     * @param viewer the alignment viewer for which the options panel should be
     *               created.
     * <p>
     * @return A new options panel for an alignment viewer.
     */
    private JPanel getAlignmentViewerOptions( AlignmentViewer viewer ) {
        AlignmentOptionsPanel options = new AlignmentOptionsPanel( viewer );
        options.setLayout( new BoxLayout( options, BoxLayout.PAGE_AXIS ) );
        options.setBackground( Colors.LEGEND_BACKGROUND );

        return options;
    }


    /**
     * @param viewer the histogram viewer for which the legend panel should be
     *               created.
     * <p>
     * @return A new legend panel for a histogram viewer.
     */
    private JPanel getHistogramViewerLegend( AbstractViewer viewer ) {

        JPanel legend = new JPanel();
        JPanel legend1 = new JPanel();
        JPanel legend2 = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.X_AXIS ) );
        legend1.setLayout( new BoxLayout( legend1, BoxLayout.PAGE_AXIS ) );
        legend2.setLayout( new BoxLayout( legend2, BoxLayout.PAGE_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend1.add( this.getLegendEntry( Colors.LOGO_A, FeatureType.BASE_A, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_C, FeatureType.BASE_C, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_G, FeatureType.BASE_G, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_T, FeatureType.BASE_T, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_N, FeatureType.BASE_N, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_MATCH, FeatureType.MATCH, null ) );
        legend1.add( this.getLegendEntry( Colors.LOGO_READGAP, FeatureType.GAP, null ) );
        legend2.add( this.getLegendEntry( Color.white, FeatureType.MULTIPLE_MAPPED_READ, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_PERFECT_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.PERFECT_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.SINGLE_BEST_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.BEST_MATCH, viewer ) );
        legend2.add( this.getLegendEntry( MappingClass.COMMON_MATCH, viewer ) );

        legend.add( legend1 );
        legend.add( legend2 );

        return legend;
    }


    /**
     * @param viewer The alignment viewer for which the legend panel should be
     *               created.
     * <p>
     * @return A new legend panel for an alignment viewer.
     */
    private JPanel getAlignmentViewLegend( AbstractViewer viewer ) {
        JPanel legend = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.PAGE_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend.add( this.getLegendEntry( MappingClass.SINGLE_PERFECT_MATCH, viewer ) );
        legend.add( this.getLegendEntry( MappingClass.PERFECT_MATCH, viewer ) );
        legend.add( this.getLegendEntry( MappingClass.SINGLE_BEST_MATCH, viewer ) );
        legend.add( this.getLegendEntry( MappingClass.BEST_MATCH, viewer ) );
        legend.add( this.getLegendEntry( MappingClass.COMMON_MATCH, viewer ) );
        legend.add( this.getLegendEntry( Color.white, FeatureType.MULTIPLE_MAPPED_READ, viewer ) );
        legend.add( this.getLegendEntry( Colors.MISMATCH_BACKGROUND, FeatureType.DIFF, null ) );
        legend.add( this.getGradientEntry( "Replicates: High to low" ) );
        legend.add( this.getGradientEntry( "Base Quality: High to low" ) );
        return legend;
    }


    /**
     * @param viewer the read pair viewer for which the legend panel should be
     *               created.
     * <p>
     * @return A new legend panel for a read pair viewer.
     */
    private JPanel getReadPairViewerLegend( AbstractViewer viewer ) {
        JPanel legend = new JPanel();
        legend.setLayout( new BoxLayout( legend, BoxLayout.PAGE_AXIS ) );
        legend.setBackground( Colors.LEGEND_BACKGROUND );

        legend.add( this.getLegendEntry( Colors.BLOCK_PERFECT, FeatureType.PERFECT_PAIR, viewer ) );
        legend.add( this.getLegendEntry( Colors.BLOCK_DIST_LARGE, FeatureType.DISTORTED_PAIR, viewer ) );
        legend.add( this.getLegendEntry( Colors.BLOCK_UNPAIRED, FeatureType.SINGLE_MAPPING, viewer ) );
        legend.add( this.getGradientEntry( "Perfect to best to common mappings" ) );
        legend.add( this.getLegendEntry( Color.white, FeatureType.MULTIPLE_MAPPED_READ, viewer ) );

        return legend;
    }


    /**
     * A feature type listener adds or removes the feature type associated with
     * it to to/from the excluded feature list of its associated viewer. Needs
     * an AbstractButton as source, in order to determine if the button was
     * selected or not.
     */
    private class FeatureTypeListener implements ActionListener {

        private Classification type;
        private AbstractViewer viewer;


        /**
         * A feature type listener adds or removes the feature type associated
         * with it to to/from the excluded feature list of its associated
         * viewer. Needs an AbstractButton as source, in order to determine if
         * the button was selected or not.
         * <p>
         * @param type   the type handled by this listener
         * @param viewer the viewer whose excluded feature list should be
         *               updated
         */
        FeatureTypeListener( Classification type, AbstractViewer viewer ) {
            this.type = type;
            this.viewer = viewer;
        }


        @Override
        public void actionPerformed( ActionEvent e ) {
            if( ((AbstractButton) e.getSource()).isSelected() ) {
                viewer.removeExcludedClassifications( type );
            } else {
                viewer.addExcludedClassifications( type );
            }
            this.viewer.setNewDataRequestNeeded( true );
            this.viewer.boundsChangedHook();
            this.viewer.repaint();
        }


    }

}
