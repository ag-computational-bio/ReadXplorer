/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_MULTIPLE_CONDS;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_WIZARD_NAME;


public class DeSeqWizardPanel1 extends ChangeListeningWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DeSeqVisualPanel1 component;


    public DeSeqWizardPanel1() {
        super( "Please select one of the options above." );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DeSeqVisualPanel1 getComponent() {
        if( component == null ) {
            component = new DeSeqVisualPanel1();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( getComponent().buttonChecked() ) {
            boolean moreThanTwoConditions = getComponent().moreThanTwoConditions();
            wiz.putProperty( PROP_DGE_MULTIPLE_CONDS, moreThanTwoConditions );
            storePrefs( moreThanTwoConditions );
        }
    }


    /**
     * Stores the selected parameters for this wizard panel for later use, also
     * after restarting the software.
     */
    private void storePrefs( boolean moreThanTwoConditions ) {
        getPref().putBoolean( PROP_DGE_WIZARD_NAME + PROP_DGE_MULTIPLE_CONDS, moreThanTwoConditions );
    }


    @Override
    public void validate() throws WizardValidationException {
        if( !getComponent().buttonChecked() ) {
            throw new WizardValidationException( null, "Please select one of the options above.", null );
        }
    }


}
