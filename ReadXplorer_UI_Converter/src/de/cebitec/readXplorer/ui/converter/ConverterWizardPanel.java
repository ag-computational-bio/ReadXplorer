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

package de.cebitec.readXplorer.ui.converter;


import de.cebitec.readXplorer.parser.output.ConverterI;
import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningFinishWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;


/**
 * The wizard panel representing the options for file conversion in ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ConverterWizardPanel extends ChangeListeningFinishWizardPanel {

    private ConverterSetupCard converterPanel;


    public ConverterWizardPanel() {
        super( "" );
    }


    @Override
    public Component getComponent() {
        if( this.converterPanel == null ) {
            this.converterPanel = new ConverterSetupCard();
        }
        return this.converterPanel;
    }


    @Override
    public void storeSettings( WizardDescriptor settings ) {
        // store converter parameters
        if( this.isFinishPanel() ) {
            ConverterI converter = converterPanel.getSelectedConverter();
            converter.setDataToConvert( converterPanel.getMappingFiles(), converterPanel.getRefChromosomeName(), converterPanel.getChromosomeLength() );
            settings.putProperty( ConverterAction.PROP_CONVERTER_TYPE, converter );
        }
    }


}