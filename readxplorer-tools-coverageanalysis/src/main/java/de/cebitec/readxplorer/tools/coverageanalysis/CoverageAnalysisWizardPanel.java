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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * Panel for showing and handling all available options for the coverage
 * analysis.
 * <p>
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageAnalysisWizardPanel extends ChangeListeningWizardPanel {

    public static final String MIN_COVERAGE_COUNT = "minCoverageCount";
    public static final String SUM_COVERAGE = "sumCoverage";
    public static final String COVERED_INTERVALS = "coveredIntervals";


    /**
     * Panel for showing and handling all available options for the coverage
     * analysis.
     */
    public CoverageAnalysisWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CoverageAnalysisVisualPanel component;


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public CoverageAnalysisVisualPanel getComponent() {
        if( component == null ) {
            component = new CoverageAnalysisVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( isValid() ) {
            wiz.putProperty( MIN_COVERAGE_COUNT, this.component.getMinCoverageCount() );
            wiz.putProperty( SUM_COVERAGE, this.component.isSumCoverageOfBothStrands() );
            wiz.putProperty( COVERED_INTERVALS, this.component.isDetectCoveredIntervals() );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().put( MIN_COVERAGE_COUNT, String.valueOf( this.component.getMinCoverageCount() ) );
        getPref().put( SUM_COVERAGE, this.component.isSumCoverageOfBothStrands() ? "1" : "0" );
        getPref().put( COVERED_INTERVALS, this.component.isDetectCoveredIntervals() ? "1" : "0" );
    }


}
