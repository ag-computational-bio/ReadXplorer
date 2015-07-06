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


import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.MousePositionListener;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.datavisualisation.trackviewer.TrackViewer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import org.openide.util.NbPreferences;


/**
 * A BasePanel serves as basis for other visual components.
 *
 * @author ddoppmei, rhilker
 */
public class BasePanel extends JPanel implements MousePositionListener {

    private static final long serialVersionUID = 246153482;

    private AbstractViewer viewer;
    private AbstractInfoPanel rightPanel;
    private AbstractInfoPanel leftPanel;
    private final BoundsInfoManager boundsManager;
    private MousePositionListener viewController;
    private final List<MousePositionListener> currentMousePosListeners;
    private JPanel centerPanel;
    private AdjustmentPanel adjustmentPanelHorizontal;
    private Component topPanel;
    private JScrollPane centerScrollpane;
    private final Preferences pref = NbPreferences.forModule( Object.class );
    private final List<PreferenceChangeListener> listeners = new ArrayList<>();


    /**
     * A BasePanel serves as basis for other visual components.
     * <p>
     * @param boundsManager  The reference bounds manager for this panel
     * @param viewController The view controller for this panel
     */
    public BasePanel( BoundsInfoManager boundsManager, MousePositionListener viewController ) {
        super();
        this.setLayout( new BorderLayout() );
        this.centerPanel = new JPanel( new BorderLayout() );
        this.add( centerPanel, BorderLayout.CENTER );
        this.boundsManager = boundsManager;
        this.viewController = viewController;
        this.currentMousePosListeners = new ArrayList<>();
    }


    /**
     * Method to call when this base panel is closed.
     */
    public void close() {
        this.shutdownViewer();
        this.shutdownInfoPanelAndAdjustmentPanel();
        for( PreferenceChangeListener listener : listeners ) {
            this.pref.removePreferenceChangeListener( listener );
        }
        this.remove( centerPanel );
        this.centerPanel = null;
        this.viewController = null;
        this.updateUI();
    }


    /**
     * Method shutting down the viewer contained in this base panel.
     */
    private void shutdownViewer() {
        if( this.viewer != null ) {
            this.boundsManager.removeBoundListener( viewer );
            this.currentMousePosListeners.remove( viewer );
            this.centerPanel.remove( viewer );
            this.viewer.close();
            this.viewer = null;
        }
    }


    /**
     * Method shutting down the info and adjustment panels of this base panel.
     */
    private void shutdownInfoPanelAndAdjustmentPanel() {
        if( adjustmentPanelHorizontal != null ) {
            centerPanel.remove( adjustmentPanelHorizontal );
            adjustmentPanelHorizontal = null;
        }

        if( rightPanel != null ) {
            rightPanel.close();
            this.remove( rightPanel );
            currentMousePosListeners.remove( rightPanel );
            rightPanel = null;
        }

        if( leftPanel != null ) {
            leftPanel.close();
            this.remove( leftPanel );
            currentMousePosListeners.remove( leftPanel );
            leftPanel = null;
        }
    }


    /**
     * Set an AbstractViewer with a vertical zoom slider into this base panel.
     * <p>
     * @param viewer       The viewer to display in this base panel
     * @param verticalZoom the vertical zoom slider for the viewer
     */
    public void setViewer( AbstractViewer viewer, JSlider verticalZoom ) {
        this.viewer = viewer;
        verticalZoom.setOrientation( JSlider.VERTICAL );
        this.boundsManager.addBoundsListener( viewer );
        currentMousePosListeners.add( viewer );
        if( viewer instanceof TrackViewer ) {
            TrackViewer tv = (TrackViewer) viewer;
            tv.setVerticalZoomSlider( verticalZoom );
        }
        centerPanel.add( viewer, BorderLayout.CENTER );
        centerPanel.add( verticalZoom, BorderLayout.WEST );

        this.updateSize();
    }


    /**
     * Set an AbstractViewer without a vertical zoom slider into this base
     * panel.
     * <p>
     * @param viewer The viewer to display in this base panel
     */
    public void setViewer( AbstractViewer viewer ) {
        this.viewer = viewer;
        this.boundsManager.addBoundsListener( viewer );
        currentMousePosListeners.add( viewer );
        centerPanel.add( viewer, BorderLayout.CENTER );

        this.addPlaceholder();
        this.updateSize();
    }


    /**
     * Sets the horizontal adjustment panel for this base panel.
     * <p>
     * @param adjustmentPanel The horizontal scroll panel
     */
    public void setHorizontalAdjustmentPanel( AdjustmentPanel adjustmentPanel ) {
        this.adjustmentPanelHorizontal = adjustmentPanel;
        centerPanel.add( adjustmentPanel, BorderLayout.NORTH );
        this.updateSize();
    }


    /**
     * Adds a viewer in a scrollpane allowing for vertical scrolling. Horizontal
     * scrolling is only available by "setHorizontalAdjustmentPanel".
     * <p>
     * @param viewer viewer to set
     */
    public void setViewerInScrollpane( AbstractViewer viewer ) {
        this.prepareViewerInScrollpane( viewer );
        this.centerScrollpane.setPreferredSize( new Dimension( 490, 200 ) );
        this.centerScrollpane.setVisible( true );
        this.viewer.setVisible( true );

        this.updateSize();

    }


    /**
     * Adds a viewer in a scrollpane allowing for vertical scrolling and
     * vertical zooming.
     * Horizontal scrolling is only available by "setHorizontalAdjustmentPanel".
     * <p>
     * @param viewer       viewer to set
     * @param verticalZoom vertical zoom slider
     */
    public void setViewerInScrollpane( AbstractViewer viewer, JSlider verticalZoom ) {
        this.prepareViewerInScrollpane( viewer );
        verticalZoom.setOrientation( JSlider.VERTICAL );
        this.centerPanel.add( verticalZoom, BorderLayout.WEST );
        this.updateSize();
    }


    private void prepareViewerInScrollpane( AbstractViewer viewer ) {
        this.viewer = viewer;
        this.boundsManager.addBoundsListener( viewer );
        this.currentMousePosListeners.add( viewer );
        this.centerScrollpane = new JScrollPane( this.viewer );
        this.centerScrollpane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        this.centerPanel.add( this.centerScrollpane, BorderLayout.CENTER );
        this.viewer.setScrollPane( this.centerScrollpane );

        this.addPlaceholder();
    }


    /**
     * Adds a placeholder in case this viewer is a ReferenceViewer
     */
    private void addPlaceholder() {
        if( viewer instanceof ReferenceViewer ) {
            JPanel p = new JPanel();
            p.add( new JLabel( " " ) );
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            centerPanel.add( p, BorderLayout.WEST );
        }
    }


    public void setTopInfoPanel( MousePositionListener infoPanel ) {
        this.topPanel = (Component) infoPanel;
        centerPanel.add( topPanel, BorderLayout.NORTH );
        currentMousePosListeners.add( infoPanel );
        this.updateSize();
    }


    public void setRightInfoPanel( AbstractInfoPanel infoPanel ) {
        this.rightPanel = infoPanel;
        this.add( infoPanel, BorderLayout.EAST );
        currentMousePosListeners.add( infoPanel );
        this.updateSize();
    }


    public void setLeftInfoPanel( AbstractInfoPanel infoPanel ) {
        this.leftPanel = infoPanel;
        this.add( leftPanel, BorderLayout.WEST );
        this.updateSize();
    }


    public void setTitlePanel( JPanel title ) {
        this.add( title, BorderLayout.NORTH );
        this.updateSize();
    }


    public void reportCurrentMousePos( int currentLogMousePos ) {
        viewController.setCurrentMousePosition( currentLogMousePos );
    }


    @Override
    public void setCurrentMousePosition( int logPos ) {
        for( MousePositionListener c : currentMousePosListeners ) {
            c.setCurrentMousePosition( logPos );
        }
    }


    public void reportMouseOverPaintingStatus( boolean b ) {
        viewController.setMouseOverPaintingRequested( b );
    }


    @Override
    public void setMouseOverPaintingRequested( boolean requested ) {
        for( MousePositionListener c : currentMousePosListeners ) {
            c.setMouseOverPaintingRequested( requested );
        }
    }


    /**
     * @return The AbstractViewer displayed by this base panel.
     */
    public AbstractViewer getViewer() {
        return viewer;
    }


    private void updateSize() {
        this.setMaximumSize( new Dimension( Integer.MAX_VALUE, this.viewer.getPreferredSize().height ) );
    }


    /**
     * Add a <code>PreferenceChangeListener</code> to this
     * <code>BasePanel</code>'s preferences.
     * <p>
     * @param listener The listener to add
     */
    public void addPrefListener( PreferenceChangeListener listener ) {
        this.listeners.add( listener );
        this.pref.addPreferenceChangeListener( listener );
    }


    /**
     * Remove a <code>PreferenceChangeListener</code> from this
     * <code>BasePanel</code>'s preferences.
     * <p>
     * @param listener The listener to remove
     */
    public void removePrefListener( PreferenceChangeListener listener ) {
        this.pref.removePreferenceChangeListener( listener );
    }


}
