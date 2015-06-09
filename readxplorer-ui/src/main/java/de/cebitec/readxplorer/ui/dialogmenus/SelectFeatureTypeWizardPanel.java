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


import de.cebitec.readxplorer.api.enums.FeatureType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.openide.WizardDescriptor;


/**
 * Wizard panel for showing and handling the selection of feature types.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectFeatureTypeWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_SELECTED_FEAT_TYPES = "PropSelectedFeatTypes";
    public static final String PROP_START_OFFSET = "PropStartOffset";
    public static final String PROP_STOP_OFFSET = "PropStopOffset";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectFeatureTypeVisualPanel component;
    private final String analysisName;
    private final boolean hasOffsetOption;


    /**
     * Wizard panel for showing and handling the selection of feature types.
     * <p>
     * @param analysisName the name of the analysis using this wizard panel. It
     *                     will be used to store the selected settings for this
     *                     wizard panel under a unique identifier.
     */
    public SelectFeatureTypeWizardPanel( String analysisName, boolean hasOffsetOption ) {
        super( "At least one feature type has to be selected & offsets have to be integers >= 0!" );
        this.analysisName = analysisName;
        this.hasOffsetOption = hasOffsetOption;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectFeatureTypeVisualPanel getComponent() {
        if( component == null ) {
            component = new SelectFeatureTypeVisualPanel( analysisName, hasOffsetOption );
        }
        return component;
    }


    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen. Further invoces the super.readSettings() method.
     */
    @Override
    public void readSettings( final WizardDescriptor wiz ) {
        super.readSettings( wiz );

        String featuresString = getPref().get( analysisName + PROP_SELECTED_FEAT_TYPES, "Gene,CDS" );
        String[] featuresArray = featuresString.split( "," );

        List<FeatureType> selectedFeatTypes = new ArrayList<>();
        for( String featureString : featuresArray ) {
            selectedFeatTypes.add( FeatureType.getFeatureType( featureString ) );
        }

        List<FeatureType> featTypeList = Arrays.asList( FeatureType.SELECTABLE_FEATURE_TYPES );
        List<Integer> selectedInices = new ArrayList<>();
        for( FeatureType selFeatureType : selectedFeatTypes ) {
            selectedInices.add( featTypeList.indexOf( selFeatureType ) );
        }

        int[] selIndicesArray = new int[selectedInices.size()];
        for( int i = 0; i < selectedInices.size(); ++i ) {
            selIndicesArray[i] = selectedInices.get( i );
        }

        String startOffsetString = getPref().get( getPropFeatureStartOffset(), "0" );
        String stopOffsetString = getPref().get( getPropFeatureStopOffset(), "0" );

        component.setSelectedFeatureTypes( selIndicesArray );
        component.setFeatureOffsets( startOffsetString, stopOffsetString );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( getPropSelectedFeatTypes(), new HashSet<>( component.getSelectedFeatureTypes() ) );
            wiz.putProperty( getPropFeatureStartOffset(), Integer.parseInt( component.getStartOffsetField().getText() ) );
            wiz.putProperty( getPropFeatureStopOffset(), Integer.parseInt( component.getStopOffsetField().getText() ) );
            storePrefs();
        }
    }


    /**
     * Stores the selected feature types for this specific wizard for later use,
     * also after restarting the software.
     */
    private void storePrefs() {
        List<FeatureType> featureTypeList = component.getSelectedFeatureTypes();
        StringBuilder featTypeString = new StringBuilder( 30 );
        for( FeatureType type : featureTypeList ) {
            featTypeString.append( type ).append( ',' );
        }
        getPref().put( getPropSelectedFeatTypes(), featTypeString.toString() );
        getPref().put( getPropFeatureStartOffset(), component.getStartOffsetField().getText() );
        getPref().put( getPropFeatureStopOffset(), component.getStopOffsetField().getText() );
    }


    /**
     * @return The property string for the selected feature type list for the
     *         corresponding wizard.
     */
    public String getPropSelectedFeatTypes() {
        return analysisName + PROP_SELECTED_FEAT_TYPES;
    }


    /**
     * @return The property string for the start offset to use for features in
     *         the corresponding wizard.
     */
    public String getPropFeatureStartOffset() {
        return analysisName + PROP_START_OFFSET;
    }


    /**
     * @return The property string for the stop offset to use for features in
     *         the corresponding wizard.
     */
    public String getPropFeatureStopOffset() {
        return analysisName + PROP_STOP_OFFSET;
    }


}
