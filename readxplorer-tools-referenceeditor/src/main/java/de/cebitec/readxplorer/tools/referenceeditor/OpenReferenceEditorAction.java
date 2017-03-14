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

package de.cebitec.readxplorer.tools.referenceeditor;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Action for opening a reference editor.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class OpenReferenceEditorAction implements ActionListener {

    private final ReferenceViewer context;


    /**
     * Action for opening a reference editor.
     * <p>
     * @param context The reference associated to the reference editor
     */
    public OpenReferenceEditorAction( ReferenceViewer context ) {
        this.context = context;
    }


    @Override
    public void actionPerformed( ActionEvent ev ) {
        PersistentReference reference = this.context.getReference();
        ReferenceEditorTopComponent referenceEditor = new ReferenceEditorTopComponent();
        referenceEditor.setName( "Reference editor of ".concat( reference.getName() ) );
        referenceEditor.setReference( reference );
        referenceEditor.open();
        referenceEditor.requestActive();
        referenceEditor.toFront();
    }


}
