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

package de.cebitec.readxplorer.transcriptionanalyses.wizard;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_AUTO_OPERON_PARAMS;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_SPANNING_READS;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_WIZARD_NAME;


/**
 * Panel for showing and handling all available options for the operon
 * detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesOperonWizardPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesOperonVisualPanel component;


    /**
     * Panel for showing and handling all available options for the operon
     * detection.
     */
    public TransAnalysesOperonWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesOperonVisualPanel getComponent() {
        if( component == null ) {
            component = new TransAnalysesOperonVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( PROP_AUTO_OPERON_PARAMS, this.component.isOperonAutomatic() );
            wiz.putProperty( PROP_MIN_SPANNING_READS, this.component.getMinSpanningReads() );
            storePrefs();
        }
    }


    /**
     * Stores the chosen operon detection parameters for this wizard for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().putBoolean( PROP_WIZARD_NAME + PROP_AUTO_OPERON_PARAMS, component.isOperonAutomatic() );
        getPref().put( PROP_WIZARD_NAME + PROP_MIN_SPANNING_READS, String.valueOf( component.getMinSpanningReads() ) );
    }
}
