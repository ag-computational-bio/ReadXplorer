/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_SAVE_R_CMD;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_SAVE_R_CMD_FILE;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_SELECTED_FEAT_TYPES;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_START_OFFSET;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_STOP_OFFSET;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_TOOL;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard.DiffExpressionWizardIterator.PROP_DGE_WIZARD_NAME;


public class GeneralSettingsWizardPanel extends ChangeListeningWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GeneralSettingsVisualPanel component;
    private Integer genomeID;
    private Tool tool;


    public GeneralSettingsWizardPanel() {
        super( "Error" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GeneralSettingsVisualPanel getComponent() {
        if( component == null ) {
            component = new GeneralSettingsVisualPanel();
        }
        return component;
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        super.readSettings( wiz );
        genomeID = (Integer) wiz.getProperty( "genomeID" );
        tool = (Tool) wiz.getProperty( PROP_DGE_TOOL );
        getComponent().enableSaveRCmd( tool != Tool.ExpressTest && tool != Tool.ExportCountTable );

        boolean storeRCmd = getPref().getBoolean( PROP_DGE_WIZARD_NAME + PROP_DGE_SAVE_R_CMD, false );
        String startOffset = getPref().get( PROP_DGE_WIZARD_NAME + PROP_DGE_START_OFFSET, "0" );
        String stopOffset = getPref().get( PROP_DGE_WIZARD_NAME + PROP_DGE_STOP_OFFSET, "0" );
        String featuresString = getPref().get( PROP_DGE_WIZARD_NAME + PROP_DGE_SELECTED_FEAT_TYPES, "Gene,CDS" );
        int[] selIndicesArray = FeatureType.calcSelectedIndices( featuresString );
        getComponent().setFeatureOffsets( startOffset, stopOffset );
        getComponent().setSelectedFeatureTypes( selIndicesArray );
        getComponent().setStoreRCmd( storeRCmd );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        if( getComponent().verifyInput() ) {
            wiz.putProperty( PROP_DGE_START_OFFSET, getComponent().getStartOffset() );
            wiz.putProperty( PROP_DGE_STOP_OFFSET, getComponent().getStopOffset() );
            getPref().putInt( PROP_DGE_WIZARD_NAME + PROP_DGE_START_OFFSET, getComponent().getStartOffset() );
            getPref().putInt( PROP_DGE_WIZARD_NAME + PROP_DGE_STOP_OFFSET, getComponent().getStopOffset() );
        }
        if( getComponent().isSaveBoxChecked() ) {
            //TODO: Input validation
            String path = getComponent().getSavePath();
            File file = new File( path );
            wiz.putProperty( PROP_DGE_SAVE_R_CMD_FILE, file );
            getPref().put( PROP_DGE_WIZARD_NAME + PROP_DGE_SAVE_R_CMD_FILE, path );
        }
        getPref().putBoolean( PROP_DGE_WIZARD_NAME + PROP_DGE_SAVE_R_CMD, getComponent().isSaveBoxChecked() );

        List<FeatureType> usedFeatures = getComponent().getSelectedFeatureTypes();
        //If all possible features are selected, we use the ANY feature type
        if( usedFeatures.size() == FeatureType.SELECTABLE_FEATURE_TYPES.length ) {
            usedFeatures = new ArrayList<>();
            usedFeatures.add( FeatureType.ANY );
        }
        wiz.putProperty( PROP_DGE_SELECTED_FEAT_TYPES, new HashSet<>( usedFeatures ) );

        String featureTypeString = FeatureType.createFeatureTypeString( usedFeatures );
        getPref().put( PROP_DGE_WIZARD_NAME + PROP_DGE_SELECTED_FEAT_TYPES, featureTypeString );
    }


    @Override
    public void validate() throws WizardValidationException {
        if( !getComponent().verifyInput() ) {
            throw new WizardValidationException( null, "Please enter a number greater or equal to zero as start/stop offset.", null );
        }
        List<FeatureType> usedFeatures = getComponent().getSelectedFeatureTypes();
        if( usedFeatures.isEmpty() ) {
            throw new WizardValidationException( null, "Please select at least one type of annotation.", null );
        } else if( usedFeatures.size() < FeatureType.SELECTABLE_FEATURE_TYPES.length ) {
            ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( genomeID );
            if( !referenceConnector.hasFeatures( usedFeatures ) ) {
                throw new WizardValidationException( null, "The selected reference genome does not contain annotations of the selected type(s).", null );
            }
        }
    }


}
