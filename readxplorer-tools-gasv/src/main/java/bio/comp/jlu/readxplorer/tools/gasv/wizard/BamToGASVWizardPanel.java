/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv.wizard;

import bio.comp.jlu.readxplorer.tools.gasv.ParametersBamToGASV;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * The BAMToGASV configuration wizard panel.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class BamToGASVWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_BAM_TO_GASV_PARAMS = "bamToGasvParams";

    private static final String PROP_PLATFORM = "platform";
    private static final String PROP_LIBRARY_SEPARATED = "librarySeparated";
    private static final String PROP_WRITE_CONCORDANT_PAIRS = "writeConcPairs";
    private static final String PROP_WRITE_LOW_QUALITY_PAIRS = "writeLowQualiPairs";
    private static final String PROP_MIN_MAPPING_QUALITY = "minMappingQuali";
    private static final String PROP_MAX_PAIR_LENGTH = "maxPairLength";
    private static final String PROP_FRAGMENT_BOUNDS_METHOD = "fragmentBoundsMethod";
    private static final String PROP_DIST_PCT_VALUE = "distPct";
    private static final String PROP_DIST_SD_VALUE = "distSd";
    private static final String PROP_DIST_EXACT_VALUE = "distExact";
    private static final String PROP_DIST_FILE = "distFile";
    private static final String PROP_SAM_VALI_STRINGENCY = "samValiStringency";


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BamToGASVVisualPanel component;


    /**
     * The BAMToGASV configuration wizard panel.
     */
    public BamToGASVWizardPanel() {
        super( "Please enter valid parameters (Only number & percent values. Quality values need to be between 0 & 127)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BamToGASVVisualPanel getComponent() {
        if( component == null ) {
            component = new BamToGASVVisualPanel( new ParametersBamToGASV() );
        }
        return component;
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        super.readSettings( wiz );
        if( wiz.getProperty( PROP_BAM_TO_GASV_PARAMS ) != null ) {
            component.setLastParameterSelection( (ParametersBamToGASV) wiz.getProperty( PROP_BAM_TO_GASV_PARAMS ) );
        } else {
            boolean platform = getPref().getBoolean( PROP_PLATFORM, false );
            boolean isLibrarySeparated = getPref().getBoolean( PROP_LIBRARY_SEPARATED, false );
            boolean isWriteConcordantPairs = getPref().getBoolean( PROP_WRITE_CONCORDANT_PAIRS, false );
            boolean isWriteLowQualityPairs = getPref().getBoolean( PROP_WRITE_LOW_QUALITY_PAIRS, false );
            byte minMappingQuality = (byte) getPref().getInt( PROP_MIN_MAPPING_QUALITY, 20 );
            int maxPairLength = getPref().getInt( PROP_MAX_PAIR_LENGTH, 10000 );
            String fragmentBoundsMethod = getPref().get( PROP_FRAGMENT_BOUNDS_METHOD, ParametersBamToGASV.FB_METHOD_PCT );
            int distPCTValue = getPref().getInt( PROP_DIST_PCT_VALUE, 99 );
            int distSDValue = getPref().getInt( PROP_DIST_SD_VALUE, -1 );
            String distExactValue = getPref().get( PROP_DIST_EXACT_VALUE, "" );
            String distFile = getPref().get( PROP_DIST_FILE, "" );
            String samValidationStringency = getPref().get( PROP_SAM_VALI_STRINGENCY, ParametersBamToGASV.STRINGENCY_SILENT );

            ParametersBamToGASV parametersBamToGASV = new ParametersBamToGASV( platform,
                                                                               isLibrarySeparated,
                                                                               isWriteConcordantPairs,
                                                                               isWriteLowQualityPairs,
                                                                               minMappingQuality,
                                                                               maxPairLength,
                                                                               fragmentBoundsMethod,
                                                                               distPCTValue,
                                                                               distSDValue,
                                                                               distExactValue,
                                                                               distFile,
                                                                               samValidationStringency );
            component.setLastParameterSelection( parametersBamToGASV );
        }
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( this.isValid() ) {
            wiz.putProperty( PROP_BAM_TO_GASV_PARAMS, component.getBamToGASVParams() );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        ParametersBamToGASV bamToGASVParams = component.getBamToGASVParams();
        getPref().putBoolean( PROP_PLATFORM, bamToGASVParams.getPlatform() );
        getPref().putBoolean( PROP_LIBRARY_SEPARATED, bamToGASVParams.isLibrarySeparated() );
        getPref().putBoolean( PROP_WRITE_CONCORDANT_PAIRS, bamToGASVParams.isWriteConcordantPairs() );
        getPref().putBoolean( PROP_WRITE_LOW_QUALITY_PAIRS, bamToGASVParams.isWriteLowQualityPairs() );
        getPref().putInt( PROP_MIN_MAPPING_QUALITY, bamToGASVParams.getMinMappingQuality() );
        getPref().putInt( PROP_MAX_PAIR_LENGTH, bamToGASVParams.getMaxPairLength() );
        getPref().put( PROP_FRAGMENT_BOUNDS_METHOD, bamToGASVParams.getFragmentBoundsMethod() );
        getPref().putInt( PROP_DIST_PCT_VALUE, bamToGASVParams.getDistPCTValue() );
        getPref().putInt( PROP_DIST_SD_VALUE, bamToGASVParams.getDistSDValue() );
        getPref().put( PROP_DIST_EXACT_VALUE, bamToGASVParams.getDistExactValue() );
        getPref().put( PROP_DIST_FILE, bamToGASVParams.getDistFile() );
        getPref().put( PROP_SAM_VALI_STRINGENCY, bamToGASVParams.getSamValidationStringency() );
    }


}
