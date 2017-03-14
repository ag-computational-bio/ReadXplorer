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

package de.cebitec.readxplorer.ui.importer.datatable;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningFinishWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;


/**
 * The wizard panel for choosing the parser to import a table file.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ImportTableWizardPanel extends ChangeListeningFinishWizardPanel {

    public static final String PROP_TABLE_TYPE = "selTableType";
    public static final String PROP_SELECTED_FILE = "selFile";
    public static final String PROP_SELECTED_STATS_FILE = "selStatsFile";
    public static final String PROP_SELECTED_REF = "selRef";
    public static final String PROP_AUTO_DELEMITER = "autoDelimiter";
    public static final String PROP_SEL_PREF = "selPref";
    public static final String PROP_SEL_PARSER = "selParser";

    private ImportTableVisualPanel component;


    /**
     * The wizard panel for choosing the parser to import a table file.
     */
    @NbBundle.Messages( "ErrorMsg=Please select a parser and a valid file to import." )
    public ImportTableWizardPanel() {
        super( Bundle.ErrorMsg() );
    }


    @Override
    public ImportTableVisualPanel getComponent() {
        if( component == null ) {
            component = new ImportTableVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( this.isValid() ) {
            wiz.putProperty( PROP_TABLE_TYPE, this.component.getSelectedTableType() );
            wiz.putProperty( PROP_SELECTED_FILE, this.component.getFileLocation() );
            wiz.putProperty( PROP_SELECTED_STATS_FILE, this.component.getStatsFileLocation() );
            wiz.putProperty( PROP_SELECTED_REF, this.component.getReference() );
            wiz.putProperty( PROP_AUTO_DELEMITER, this.component.isAutodetectDelimiter() );
            wiz.putProperty( PROP_SEL_PREF, this.component.getCsvPref() );
            wiz.putProperty( PROP_SEL_PARSER, this.component.getParser() );
        }
    }


}
