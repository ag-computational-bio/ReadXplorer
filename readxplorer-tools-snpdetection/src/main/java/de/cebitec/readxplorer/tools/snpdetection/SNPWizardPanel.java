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

package de.cebitec.readxplorer.tools.snpdetection;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * The SNP detection wizard main panel.
 */
public class SNPWizardPanel extends ChangeListeningWizardPanel {

    static final String PROP_MIN_PERCENT = "minPercent";
    static final String PROP_MIN_VARYING_BASES = "minNoBases";
    static final String PROP_USE_MAIN_BASE = "useMainBase";
    static final String PROP_SEL_QUAL_FILTER = "includeQualFilter";
    static final String PROP_MIN_BASE_QUAL = "minBaseQual";
    static final String PROP_MIN_AVERAGE_BASE_QUAL = "minAvrgBaseQual";
    static final String PROP_MIN_AVERAGE_MAP_QUAL = "minAvrgMapQual";


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SNPVisualPanel component;


    /**
     * The SNP detection wizard main panel.
     */
    public SNPWizardPanel() {
        super( "Please enter valid parameters (Only number & percent values. Quality values need to be between 0 & 127)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SNPVisualPanel getComponent() {
        if( component == null ) {
            component = new SNPVisualPanel();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( this.isValid() ) {
            wiz.putProperty( PROP_MIN_PERCENT, this.component.getMinPercentage() );
            wiz.putProperty( PROP_MIN_VARYING_BASES, this.component.getMinMismatchingBases() );
            wiz.putProperty( PROP_USE_MAIN_BASE, this.component.isUseMainBase() );
            wiz.putProperty( PROP_MIN_BASE_QUAL, this.component.isUseQualFilter() ? this.component.getMinBaseQuality() : 0 );
            wiz.putProperty( PROP_MIN_AVERAGE_BASE_QUAL, this.component.isUseQualFilter() ? this.component.getMinAverageBaseQual() : 0 );
            wiz.putProperty( PROP_MIN_AVERAGE_MAP_QUAL, this.component.isUseQualFilter() ? this.component.getMinAverageMappingQual() : 0 );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().put( PROP_MIN_PERCENT, String.valueOf( this.component.getMinPercentage() ) );
        getPref().put( PROP_MIN_VARYING_BASES, String.valueOf( this.component.getMinMismatchingBases() ) );
        getPref().put( PROP_USE_MAIN_BASE, this.component.isUseMainBase() ? "1" : "0" );
        getPref().put( PROP_SEL_QUAL_FILTER, this.component.isUseQualFilter() ? "1" : "0" );
        getPref().put( PROP_MIN_BASE_QUAL, String.valueOf( this.component.getMinBaseQuality() ) );
        getPref().put( PROP_MIN_AVERAGE_BASE_QUAL, String.valueOf( this.component.getMinAverageBaseQual() ) );
        getPref().put( PROP_MIN_AVERAGE_MAP_QUAL, String.valueOf( this.component.getMinAverageMappingQual() ) );
    }


}
