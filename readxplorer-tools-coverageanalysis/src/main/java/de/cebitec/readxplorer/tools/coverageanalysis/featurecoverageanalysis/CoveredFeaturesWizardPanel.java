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

package de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * Panel for showing and handling all available options for the covered
 * feature detection.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeaturesWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_GET_COVERED_FEATURES = "getCoveredFeatures";
    public static final String PROP_MIN_COVERED_PERCENT = "minCoveredPercent";
    public static final String PROP_MIN_COVERAGE_COUNT = "minCoverageCount";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CoveredFeaturesVisualPanel component;


    /**
     * Panel for showing and handling all available options for the covered
     * feature detection.
     */
    public CoveredFeaturesWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public CoveredFeaturesVisualPanel getComponent() {
        if( component == null ) {
            component = new CoveredFeaturesVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( PROP_GET_COVERED_FEATURES, this.component.isGetCoveredFeatures() );
            wiz.putProperty( PROP_MIN_COVERED_PERCENT, this.component.getMinCoveredPercent() );
            wiz.putProperty( PROP_MIN_COVERAGE_COUNT, this.component.getMinCoverageCount() );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().put( PROP_GET_COVERED_FEATURES, this.component.isGetCoveredFeatures() ? "1" : "0" );
        getPref().put( PROP_MIN_COVERED_PERCENT, String.valueOf( this.component.getMinCoveredPercent() ) );
        getPref().put( PROP_MIN_COVERAGE_COUNT, String.valueOf( this.component.getMinCoverageCount() ) );
    }


}
