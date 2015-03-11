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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSlider;


/**
 *
 * @author ddoppmeier
 */
public class ExtendedSlider extends JSlider implements SynchronousNavigator {

    private static final long serialVersionUID = 2347624;

    private int current;

    private final List<AdjustmentPanelListenerI> listeners;


    public ExtendedSlider( int min, int max, int init ) {
        super( JSlider.HORIZONTAL, min, max, init );
        this.current = init;
        listeners = new ArrayList<>();
        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
            }


            @Override
            public void mousePressed( MouseEvent e ) {
            }


            @Override
            public void mouseReleased( MouseEvent e ) {
                int newValue = ExtendedSlider.this.getValue();
                if( newValue != current ) {
                    current = newValue;
                    updateListeners();
                }
            }


            @Override
            public void mouseEntered( MouseEvent e ) {
            }


            @Override
            public void mouseExited( MouseEvent e ) {
            }


        } );

//        this.addChangeListener(new ChangeListener() {
//
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                int newValue = ExtendedSlider.this.getValue();
//                if(newValue != current){
//                    current = newValue;
//                    updateListeners();
//                }
//            }
//        });
    }


    public void addAdjustmentPanelListener( AdjustmentPanelListenerI listener ) {
        listeners.add( listener );
        listener.zoomLevelUpdated( current );
    }


    public void removeAdjustmentPanelListener( AdjustmentPanelListenerI listener ) {
        listeners.remove( listener );
    }


    @Override
    public void setCurrentScrollValue( int value ) {
    }


    @Override
    public void setCurrentZoomValue( int value ) {
        current = value;
        this.setValue( current );
    }


    private void updateListeners() {
        for( AdjustmentPanelListenerI l : listeners ) {
            l.zoomLevelUpdated( current );
        }
    }


}
