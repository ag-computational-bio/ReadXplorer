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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action to disable synchronization of sliders in the thumbnail viewer.
 * 
 * @author denis, Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@ActionID(
         category = "Tools",
         id = "de.cebitec.readxplorer.thumbnail.actions.AsyncSliders"
)
@ActionRegistration(displayName = "#CTL_AsyncSliders", iconInMenu = false)
@ActionReference( path = "Menu/Tools/Thumbnail", position = 350 )
@NbBundle.Messages( "CTL_AsyncSliders=Don't sync sliders" )
public final class AsyncSliders implements ActionListener {

    private final AsyncSliderCookie context;

    /**
     * Action to disable synchronization of sliders in the thumbnail viewer.
     *
     * @param context
     */
    public AsyncSliders( AsyncSliderCookie context ) {
        this.context = context;
    }


    @Override
    public void actionPerformed( ActionEvent ev ) {
        context.async();
    }


}
