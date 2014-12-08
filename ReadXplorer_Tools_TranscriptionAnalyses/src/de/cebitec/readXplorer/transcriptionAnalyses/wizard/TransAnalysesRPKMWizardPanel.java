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

package de.cebitec.readXplorer.transcriptionAnalyses.wizard;


import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;


/**
 * Panel for showing and handling all available options for the operon
 * detection.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesRPKMWizardPanel extends ChangeListeningWizardPanel {

    private TransAnalysesRPKMVisualPanel component;


    /**
     * Panel for showing and handling all available options for the operon
     * detection.
     */
    public TransAnalysesRPKMWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new TransAnalysesRPKMVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS, this.component.getMinReadCount() );
            wiz.putProperty( TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS, this.component.getMaxReadCount() );
        }
    }


}
