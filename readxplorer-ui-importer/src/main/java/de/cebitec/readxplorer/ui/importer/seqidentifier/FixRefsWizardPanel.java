/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningFinishWizardPanel;
import htsjdk.samtools.SAMSequenceDictionary;
import java.util.List;
import org.openide.WizardDescriptor;


public class FixRefsWizardPanel extends ChangeListeningFinishWizardPanel {

    public static final String PROP_FIXED_DICTIONARY = "PROP_FIXED_DICTIONARY";
    private final List<String> chromNames;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FixRefsVisualPanel component;
    private final SAMSequenceDictionary sequenceDictionary;


    /**
     * Wizard panel to manually fix the reference sequence ids in the mapping
     * file dictionary.
     *
     * @param chromNames         Reference chromosome names
     * @param sequenceDictionary Mapping file sequence dictionary
     */
    FixRefsWizardPanel( List<String> chromNames, SAMSequenceDictionary sequenceDictionary ) {
        super( "" );
        this.chromNames = chromNames;
        this.sequenceDictionary = sequenceDictionary;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FixRefsVisualPanel getComponent() {
        if( component == null ) {
            component = new FixRefsVisualPanel( chromNames, sequenceDictionary );
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( PROP_FIXED_DICTIONARY, this.component.getDictionary() );
        }
    }


}
