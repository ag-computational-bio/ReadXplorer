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

package de.cebitec.readxplorer.rnatrimming.correlationanalysis;


import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;

import static de.cebitec.readxplorer.rnatrimming.correlationanalysis.CorrelationAnalysisAction.PROP_CORRELATIONCOEFFICIENT;
import static de.cebitec.readxplorer.rnatrimming.correlationanalysis.CorrelationAnalysisAction.PROP_INTERVALLENGTH;
import static de.cebitec.readxplorer.rnatrimming.correlationanalysis.CorrelationAnalysisAction.PROP_MINCORRELATION;
import static de.cebitec.readxplorer.rnatrimming.correlationanalysis.CorrelationAnalysisAction.PROP_MINPEAKCOVERAGE;


/**
 * Panel for the parameter selection as part of the correlation analysis wizard.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>, rhilker
 */
class ParameterSelectionPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ParameterSelectionCard component;


    /**
     * Panel for the parameter selection as part of the correlation analysis
     * wizard.
     */
    ParameterSelectionPanel() {
        super( "Interval length needs to be > 2 and minimum correlcation > 0" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new ParameterSelectionCard();
        }
        return component;
    }


    @Override
    public void storeSettings( WizardDescriptor settings ) {
        settings.putProperty( PROP_INTERVALLENGTH, component.getIntervalLength() );
        settings.putProperty( PROP_MINCORRELATION, component.getMinimumCorrelation() );
        settings.putProperty( PROP_MINPEAKCOVERAGE, component.getMinimumPeakCoverage() );
        settings.putProperty( PROP_CORRELATIONCOEFFICIENT, component.getCorrelationMethod() );
        storePrefs();
    }


    /**
     * Stores the chosen correlation analysis parameters for this wizard for
     * later use, also after restarting the software.
     */
    private void storePrefs() {
        getPref().putInt( PROP_INTERVALLENGTH, component.getIntervalLength() );
        getPref().putInt( PROP_MINCORRELATION, component.getMinimumCorrelation() );
        getPref().putInt( PROP_MINPEAKCOVERAGE, component.getMinimumPeakCoverage() );
        getPref().put( PROP_CORRELATIONCOEFFICIENT, component.getCorrelationMethod().toString() );
    }


}
