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

package de.cebitec.readxplorer.transcriptionanalyses;

import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * Wizard panel allowing the configuration of the length of exported promoter
 * sequences.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class PromoterSeqLengthWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROMOTER_LENGTH = "PROMOTER_LENGTH";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PromoterSeqLengthVisualPanel component;


    /**
     * Wizard panel allowing the configuration of the length of exported
     * promoter sequences.
     */
    public PromoterSeqLengthWizardPanel() {
        super( "Please only input positive integer values!" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public PromoterSeqLengthVisualPanel getComponent() {
        if( component == null ) {
            component = new PromoterSeqLengthVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( PROMOTER_LENGTH, this.component.getPromoterLength() );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().putInt( PROMOTER_LENGTH, this.component.getPromoterLength() );
    }


}
