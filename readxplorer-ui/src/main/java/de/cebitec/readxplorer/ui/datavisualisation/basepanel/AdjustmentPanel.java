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


import de.cebitec.readxplorer.ui.datavisualisation.SynchronousNavigator;
import javax.swing.BoxLayout;
import javax.swing.JPanel;


/**
 * A Panel to control zoom-level and the currently shown positions of listeners
 * <p>
 * @author ddoppmeier
 */
public class AdjustmentPanel extends JPanel implements SynchronousNavigator {

    public static final long serialVersionUID = 623482568;

    private ExtendedSlider slider;
    private ExtendedScroller scrollbar;


    /**
     * Create an AdjustmentPanel used for managing the displayed area of
     * listeners.
     * <p>
     * @param navigatorMin  mostly 1
     * @param navigatorMax  maximum value of the navigator, normally the
     *                      chromosome length
     * @param positionInit
     * @param zoomInit
     * @param sliderMax     maximal value of the zoom slider
     * @param hasZoomslider
     * @param hasScrollbar
     */
    public AdjustmentPanel( int navigatorMin, int navigatorMax, int positionInit, int zoomInit,
                            int sliderMax, boolean hasScrollbar, boolean hasZoomslider ) {
        super();
        this.setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );

        if( hasScrollbar ) {
            scrollbar = new ExtendedScroller( navigatorMin, navigatorMax, positionInit );
            this.add( scrollbar );
        }
        if( hasZoomslider ) {
            zoomInit = sliderMax < zoomInit ? sliderMax : zoomInit;
            slider = new ExtendedSlider( 1, sliderMax, zoomInit );
            this.add( slider );
        }

    }


    /**
     * Adjusts the navigator max value. Needed, when e.g. chromosomes are
     * switched.
     * <p>
     * @param navigatorMax Sets the navigator max to this value.
     */
    public void setNavigatorMax( int navigatorMax ) {
        scrollbar.setMaximum( navigatorMax );
    }


    /**
     *
     * @param listener register this listener to be notified of changes
     */
    public void addAdjustmentListener( AdjustmentPanelListenerI listener ) {
        if( scrollbar != null ) {
            scrollbar.addAdjustmentListener( listener );
        }
        if( slider != null ) {
            slider.addAdjustmentPanelListener( listener );
        }
    }


    /**
     *
     * @param listener remove the listener, so it is not updated anymore on
     *                 occurring changes
     */
    public void removeAdjustmentListener( AdjustmentPanelListenerI listener ) {
        if( scrollbar != null ) {
            scrollbar.removeAdjustmentListener( listener );
        }
        if( slider != null ) {
            slider.removeAdjustmentPanelListener( listener );
        }
    }


    @Override
    public void setCurrentScrollValue( int value ) {
        if( scrollbar != null ) {
            scrollbar.setCurrentScrollValue( value );
        }
    }


    @Override
    public void setCurrentZoomValue( int value ) {
        if( slider != null ) {
            slider.setCurrentZoomValue( value );
            scrollbar.setCurrentZoomValue( value );
        }
    }


}
