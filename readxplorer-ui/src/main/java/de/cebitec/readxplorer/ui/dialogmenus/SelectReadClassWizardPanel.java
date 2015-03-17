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


import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import org.openide.WizardDescriptor;


/**
 * Panel for showing and handling all available options for the selection of
 * read classes.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SelectReadClassWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_PERFECT_SELECTED = "PerfectSelected";
    public static final String PROP_BEST_MATCH_SELECTED = "BestMatchSelected";
    public static final String PROP_COMMON_MATCH_SELECTED = "CommonMatchSelected";
    public static final String PROP_SINGLE_PERFECT_SELECTED = "SinglePerfectSelected";
    public static final String PROP_SINGLE_BEST_MATCH_SELECTED = "SingleBestMatchSelected";
    public static final String PROP_SINGLE_COMMON_MATCH_SELECTED = "SingleCommonMatchSelected";
    public static final String PROP_UNIQUE_SELECTED = "UniqueSelected";
    public static final String PROP_MIN_MAPPING_QUAL = "minMapQual";
    public static final String PROP_STRAND_OPTION = "strandOption";

    private static final String PROP_READ_CLASS_PARAMS = "ReadClassParams";
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectReadClassVisualPanel component;
    private final String wizardName;
    private final boolean isFeatureAnalysis;


    /**
     * Panel for showing and handling all available options for the selection of
     * read classes.
     * <p>
     * @param wizardName        the name of the wizard using this wizard panel.
     *                          It will be used to store the selected settings
     *                          for this wizard under a unique identifier.
     * @param isFeatureAnalysis <code>true</code> means the analysis runs on
     *                          genomic features and should show appropriate
     *                          texts. <code>false</code> means the analysis
     *                          generally runs on both strands and should show
     *                          appropriate texts for this case.
     */
    public SelectReadClassWizardPanel( String wizardName, boolean isFeatureAnalysis ) {
        super( "Please select at least one read class to continue and enter a value between 0 and 127 as mapping quality!" );
        this.wizardName = wizardName;
        this.isFeatureAnalysis = isFeatureAnalysis;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectReadClassVisualPanel getComponent() {
        if( component == null ) {
            component = new SelectReadClassVisualPanel( wizardName, isFeatureAnalysis );
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            ParametersReadClasses readClassParams = component.getReadClassParams();
            wiz.putProperty( getPropReadClassParams(), readClassParams );
            storePrefs( readClassParams );
        }
    }


    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     * <p>
     * @param readClassParams The parameters to store
     */
    private void storePrefs( ParametersReadClasses readClassParams ) {
        boolean isPerfectSelected = readClassParams.isClassificationAllowed( MappingClass.PERFECT_MATCH );
        boolean isBestMatchSelected = readClassParams.isClassificationAllowed( MappingClass.BEST_MATCH );
        boolean isCommonMatchSelected = readClassParams.isClassificationAllowed( MappingClass.COMMON_MATCH );
        boolean isSinglePerfectSelected = readClassParams.isClassificationAllowed( MappingClass.SINGLE_PERFECT_MATCH );
        boolean isSingleBestMatchSelected = readClassParams.isClassificationAllowed( MappingClass.SINGLE_BEST_MATCH );
        boolean isUniqueSelected = !readClassParams.isClassificationAllowed( FeatureType.MULTIPLE_MAPPED_READ );
        String minMappingQuality = String.valueOf( readClassParams.getMinMappingQual() );
        String strandOption = String.valueOf( readClassParams.getStrandOption() );
        getPref().putBoolean( wizardName + PROP_PERFECT_SELECTED, isPerfectSelected );
        getPref().putBoolean( wizardName + PROP_BEST_MATCH_SELECTED, isBestMatchSelected );
        getPref().putBoolean( wizardName + PROP_COMMON_MATCH_SELECTED, isCommonMatchSelected );
        getPref().putBoolean( wizardName + PROP_SINGLE_PERFECT_SELECTED, isSinglePerfectSelected );
        getPref().putBoolean( wizardName + PROP_SINGLE_BEST_MATCH_SELECTED, isSingleBestMatchSelected );
        getPref().putBoolean( wizardName + PROP_UNIQUE_SELECTED, isUniqueSelected );
        getPref().put( wizardName + PROP_MIN_MAPPING_QUAL, minMappingQuality );
        getPref().put( wizardName + PROP_STRAND_OPTION, strandOption );
    }


    /**
     * @return The property string for the read class parameter set for the
     *         corresponding wizard.
     */
    public String getPropReadClassParams() {
        return wizardName + PROP_READ_CLASS_PARAMS;
    }


}
