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

package de.cebitec.readxplorer.transcriptionanalyses.wizard;


import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;

import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_ANALYSIS_DIRECTION;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_AUTO_TSS_PARAMS;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MAX_FEATURE_DISTANCE;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MAX_LEADERLESS_DISTANCE;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MAX_LOW_COV_INIT_COUNT;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_LOW_COV_INC;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_PERCENT_INCREASE;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_TOTAL_INCREASE;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_MIN_TRANSCRIPT_EXTENSION_COV;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_UNANNOTATED_TRANSCRIPT_DET;
import static de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator.PROP_WIZARD_NAME;
import static de.cebitec.readxplorer.ui.dialogmenus.SelectReadClassWizardPanel.PROP_STRAND_OPTION;


/**
 * Panel for showing and handling all available options for the transcription
 * start site detection.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesTSSWizardPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesTSSVisualPanel component;


    /**
     * Panel for showing and handling all available options for the
     * transcription start site detection.
     */
    public TransAnalysesTSSWizardPanel() {
        super( "Please enter valid parameters (only positive numbers are allowed)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesTSSVisualPanel getComponent() {
        if( component == null ) {
            component = new TransAnalysesTSSVisualPanel();
        }
        return component;
    }


    @Override
    public void readSettings( final WizardDescriptor wiz ) {
        super.readSettings( wiz );
        byte strandOption = Byte.valueOf( NbPreferences.forModule( Object.class ).get(
                PROP_WIZARD_NAME + PROP_STRAND_OPTION, "1" ) );
        boolean isBothStrandOption = strandOption == Properties.STRAND_BOTH;
        this.getComponent().setDirectionOptionsVisible( isBothStrandOption );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( this.isValid() ) {
            wiz.putProperty( PROP_AUTO_TSS_PARAMS, this.component.isTssAutomatic() );
            wiz.putProperty( PROP_MIN_TOTAL_INCREASE, this.component.getMinTotalIncrease() );
            wiz.putProperty( PROP_MIN_PERCENT_INCREASE, this.component.getMinTotalPercentIncrease() );
            wiz.putProperty( PROP_MAX_LOW_COV_INIT_COUNT, this.component.getMaxLowCovInitialCount() );
            wiz.putProperty( PROP_MIN_LOW_COV_INC, this.component.getMinLowCovIncrease() );
            wiz.putProperty( PROP_UNANNOTATED_TRANSCRIPT_DET, this.component.getDetectUnannotatedTranscripts() );
            wiz.putProperty( PROP_MIN_TRANSCRIPT_EXTENSION_COV, this.component.getMinTranscriptExtensionCov() );
            wiz.putProperty( PROP_MAX_LEADERLESS_DISTANCE, this.component.getMaxLeaderlessDistance() );
            wiz.putProperty( PROP_MAX_FEATURE_DISTANCE, this.component.getMaxFeatureDistance() );
            wiz.putProperty( PROP_ANALYSIS_DIRECTION, this.component.isFwdDirectionSelected() );
            this.storePrefs();
        }
    }


    /**
     * Stores the chosen TSS parameters for this wizard for later use, also
     * after restarting the software.
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( PROP_WIZARD_NAME + PROP_AUTO_TSS_PARAMS, component.isTssAutomatic() ? "1" : "0" );
        pref.put( PROP_WIZARD_NAME + PROP_MIN_TOTAL_INCREASE, String.valueOf( component.getMinTotalIncrease() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MIN_PERCENT_INCREASE, String.valueOf( component.getMinTotalPercentIncrease() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MAX_LOW_COV_INIT_COUNT, String.valueOf( component.getMaxLowCovInitialCount() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MIN_LOW_COV_INC, String.valueOf( component.getMinLowCovIncrease() ) );
        pref.put( PROP_WIZARD_NAME + PROP_UNANNOTATED_TRANSCRIPT_DET, component.getDetectUnannotatedTranscripts() ? "1" : "0" );
        pref.put( PROP_WIZARD_NAME + PROP_MIN_TRANSCRIPT_EXTENSION_COV, String.valueOf( component.getMinTranscriptExtensionCov() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MAX_LEADERLESS_DISTANCE, String.valueOf( component.getMaxLeaderlessDistance() ) );
        pref.put( PROP_WIZARD_NAME + PROP_MAX_FEATURE_DISTANCE, String.valueOf( component.getMaxFeatureDistance() ) );
        pref.put( PROP_WIZARD_NAME + PROP_ANALYSIS_DIRECTION, component.isFwdDirectionSelected() ? "1" : "0" );
    }


}
