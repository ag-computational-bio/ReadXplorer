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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollBar;
import javax.swing.JSlider;


/**
 *
 * @author ddoppmeier
 */
public class ExtendedScroller extends JScrollBar implements SynchronousNavigator {

    private static final long serialVersionUID = 7416234;

    private static final int BLOCK_INCREMENT = 1000;
    private static final int UNIT_INCREMENT = 10;
    private int currentValue;
    private int currentZoomValue;
    private final List<AdjustmentPanelListenerI> listeners;


    public ExtendedScroller( int min, int max, int init ) {
        super( JSlider.HORIZONTAL, init, 0, min, max );
        setBlockIncrement( BLOCK_INCREMENT );
        setUnitIncrement( UNIT_INCREMENT );

        currentValue = init;
        currentZoomValue = 1;
        listeners = new ArrayList<>();
        this.addAdjustmentListener( new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged( AdjustmentEvent e ) {
                int newValue = ExtendedScroller.this.getValue();
                if( newValue != currentValue ) {
                    currentValue = newValue;
                    updateListeners();
                }
            }


        } );
    }


    /**
     *
     * @param listener register this listener to be notified of changes
     */
    public void addAdjustmentListener( AdjustmentPanelListenerI listener ) {
        listeners.add( listener );
        listener.navigatorBarUpdated( currentValue );
    }


    /**
     *
     * @param listener remove the listener, so it is not updated anymore on
     *                 occurring changes
     */
    public void removeAdjustmentListener( AdjustmentPanelListenerI listener ) {
        if( listeners.contains( listener ) ) {
            listeners.remove( listener );
        }
    }


    private void updateListeners() {
        for( AdjustmentPanelListenerI l : listeners ) {
            l.navigatorBarUpdated( currentValue );
        }
    }


    @Override
    public void setCurrentScrollValue( int value ) {
        this.currentValue = value;
        this.setValue( currentValue );
    }


    @Override
    public void setCurrentZoomValue( int value ) {
        currentZoomValue = value;
        setBlockIncrement( BLOCK_INCREMENT * currentZoomValue / 10 );
        setUnitIncrement( UNIT_INCREMENT * currentZoomValue / 10 );
    }


}
