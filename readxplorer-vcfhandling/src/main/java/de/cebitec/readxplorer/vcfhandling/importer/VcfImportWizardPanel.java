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
package de.cebitec.readxplorer.vcfhandling.importer;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningFinishWizardPanel;
import org.openide.WizardDescriptor;


/**
 * The wizard panel for importing VCF files.
 *
 * @author marend, vetz, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
class VcfImportWizardPanel extends ChangeListeningFinishWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private VcfImportVisualPanel component;
    public static final String PROP_SELECTED_FILE = "selected File";
    public static final String PROP_SELECTED_REF = "selected Reference";


    /**
     * The wizard panel for importing VCF files.
     */
    VcfImportWizardPanel() {
        super( "Select a file to continue." );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public VcfImportVisualPanel getComponent() {
        if( component == null ) {
            component = new VcfImportVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        if( isValid() ) {
            wiz.putProperty( VcfImportWizardPanel.PROP_SELECTED_FILE, this.component.getVcfFile() );
            wiz.putProperty( VcfImportWizardPanel.PROP_SELECTED_REF, this.component.getReference() );
        }
    }


}
