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
import java.awt.Component;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;

import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_WIZARD_NAME;


/**
 * Panel for showing and handling all available options for the normalization
 * (TPM and RPKM) and read count analysis.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesNormWizardPanel extends ChangeListeningWizardPanel {

    private TransAnalysesNormVisualPanel component;


    /**
     * Panel for showing and handling all available options for the
     * normalization (TPM and RPKM) and read count analysis.
     */
    public TransAnalysesNormWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new TransAnalysesNormVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( PROP_MIN_NUMBER_READS, this.component.getMinReadCount() );
            wiz.putProperty( PROP_MAX_NUMBER_READS, this.component.getMaxReadCount() );
            storePrefs();
        }
    }


    /**
     * Stores the chosen read count normalization analysis parameters for this
     * wizard for later use, also after restarting the software.
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( PROP_WIZARD_NAME + PROP_MIN_NUMBER_READS, String.valueOf( component.getMinReadCount() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MAX_NUMBER_READS, String.valueOf( component.getMaxReadCount() ) );
    }
}
