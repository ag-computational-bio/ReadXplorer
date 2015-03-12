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

package de.cebitec.readxplorer.ui.dialogmenus;


import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;


/**
 * Wizard panel for showing and handling the selection of feature types.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectFeatureTypeWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_SELECTED_FEAT_TYPES = "PropSelectedFeatTypes";
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectFeatureTypeVisualPanel component;
    private final String analysisName;


    /**
     * Wizard panel for showing and handling the selection of feature types.
     * <p>
     * @param analysisName the name of the analysis using this wizard panel. It
     *                     will be used to store the selected settings for this
     *                     wizard panel under a unique identifier.
     */
    public SelectFeatureTypeWizardPanel( String analysisName ) {
        super( "Please select at least one feature type to continue." );
        this.analysisName = analysisName;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectFeatureTypeVisualPanel getComponent() {
        if( component == null ) {
            component = new SelectFeatureTypeVisualPanel( analysisName );
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( getPropSelectedFeatTypes(), new HashSet<>( this.component.getSelectedFeatureTypes() ) );
            this.storeFeatureTypes( this.component.getSelectedFeatureTypes() );
        }
    }


    /**
     * Stores the selected feature types for this specific wizard for later use,
     * also after restarting the software.
     * <p>
     * @param readClassParams The parameters to store
     */
    private void storeFeatureTypes( List<FeatureType> featureTypeList ) {
        StringBuilder featTypeString = new StringBuilder( 30 );
        for( FeatureType type : featureTypeList ) {
            featTypeString.append( type.getTypeString() ).append( "," );
        }
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( getPropSelectedFeatTypes(), featTypeString.toString() );
    }


    /**
     * @return The property string for the selected feature type list for the
     *         corresponding wizard.
     */
    public String getPropSelectedFeatTypes() {
        return this.analysisName + PROP_SELECTED_FEAT_TYPES;
    }


}
