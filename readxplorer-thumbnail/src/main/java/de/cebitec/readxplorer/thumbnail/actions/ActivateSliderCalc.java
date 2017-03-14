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

package de.cebitec.readxplorer.thumbnail.actions;


import de.cebitec.readxplorer.thumbnail.ThumbnailController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;


/**
 * This ActionClass presents a CheckBoxMenuItem to enable or disable the
 * automatic calibration of the CoverageZoomSlider on a TrackPanel.
 * For better perfomance this option can be disabled.
 * <p>
 * @author denis
 */
public final class ActivateSliderCalc implements ActionListener, Presenter.Menu {

    private static final String ITEM_TEXT = "Auto Slider Calculation";


    @Override
    public void actionPerformed( ActionEvent e ) {
    }


    @Override
    public JMenuItem getMenuPresenter() {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem( ITEM_TEXT, true );
        item.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                ThumbnailController thumb = Lookup.getDefault().lookup( ThumbnailController.class );
                if( thumb != null ) {
                    thumb.setAutoSlider( item.isSelected() );
                }
            }


        } );
        return item;
    }


}
