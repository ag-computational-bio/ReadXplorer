/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * Action for opening a genome rearrangement detection using the integrated
 * version of GASV. It opens a track list containing all tracks of the selected
 * reference and creates a new GASV configuration setup wizard, runs the
 * analysis and opens the result TopComponent.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(
         category = "Tools",
         id = "bio.comp.jlu.readxplorer.tools.gasv.OpenGASVAction"
)
@ActionRegistration(
         iconBase = "bio/comp/jlu/readxplorer/tools/gasv/gasv.png",
         displayName = "#CTL_OpenGASVAction"
)
@ActionReferences( {
    @ActionReference( path = "Menu/Tools", position = 225 ),
    @ActionReference( path = "Toolbars/Tools", position = 150 )
} )
@Messages( "CTL_OpenGASVAction=Run GASV" )
public final class OpenGASVAction implements ActionListener {

    private final ReferenceViewer context;


    /**
     * Action for opening a genome rearrangement detection using the integrated
     * version of GASV. It opens a track list containing all tracks of the
     * selected reference and creates a new GASV configuration setup wizard,
     * runs the analysis and opens the result TopComponent.
     * <p>
     * @param context The ReferenceViewer for which the analysis is carried out.
     */
    public OpenGASVAction( ReferenceViewer context ) {
        this.context = context;
    }


    /**
     * {@inheritdoc}
     */
    @Override
    public void actionPerformed( ActionEvent ev ) {
        //Open track selection & option wizard.
        //Afterwards run GASV with:
        GASVCaller gasvCaller = new GASVCaller();
        gasvCaller.callGASV( context.getReference() );
    }


}
