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

package bio.comp.jlu.readxplorer.tools.gasv;

import bio.comp.jlu.readxplorer.tools.gasv.ParametersBamToGASV.FragmentBoundsMethod;
import bio.comp.jlu.readxplorer.tools.gasv.ParametersBamToGASV.SamValidationStringency;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import java.util.prefs.Preferences;
import org.openide.WizardDescriptor;
import org.openide.util.NbPreferences;


/**
 * The BAMToGASV configuration wizard panel.
 *
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

    private final Preferences pref;


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
        pref = NbPreferences.forModule( Object.class );
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
            boolean platform = pref.getBoolean( PROP_PLATFORM, false );
            boolean isLibrarySeparated = pref.getBoolean( PROP_LIBRARY_SEPARATED, false );
            boolean isWriteConcordantPairs = pref.getBoolean( PROP_WRITE_CONCORDANT_PAIRS, false );
            boolean isWriteLowQualityPairs = pref.getBoolean( PROP_WRITE_LOW_QUALITY_PAIRS, false );
            byte minMappingQuality = (byte) pref.getInt( PROP_MIN_MAPPING_QUALITY, 20 );
            int maxPairLength = pref.getInt( PROP_MAX_PAIR_LENGTH, 10000 );
            String fragmentBoundsString = pref.get( PROP_FRAGMENT_BOUNDS_METHOD, FragmentBoundsMethod.PCT.getTypeString() );
            FragmentBoundsMethod fragmentBoundsMethod = FragmentBoundsMethod.getMethodType( fragmentBoundsString );
            int distPCTValue = pref.getInt( PROP_DIST_PCT_VALUE, 99 );
            int distSDValue = pref.getInt( PROP_DIST_SD_VALUE, -1 );
            String distExactValue = pref.get( PROP_DIST_EXACT_VALUE, "" );
            String distFile = pref.get( PROP_DIST_FILE, "" );
            String samValidationStringencyString = pref.get( PROP_SAM_VALI_STRINGENCY, SamValidationStringency.SILENT.getTypeString() );
            SamValidationStringency samValidationStringency = SamValidationStringency.getMethodType( samValidationStringencyString );
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
        pref.putBoolean( PROP_PLATFORM, bamToGASVParams.getPlatform() );
        pref.putBoolean( PROP_LIBRARY_SEPARATED, bamToGASVParams.isLibrarySeparated() );
        pref.putBoolean( PROP_WRITE_CONCORDANT_PAIRS, bamToGASVParams.isWriteConcordantPairs() );
        pref.putBoolean( PROP_WRITE_LOW_QUALITY_PAIRS, bamToGASVParams.isWriteLowQualityPairs() );
        pref.putInt( PROP_MIN_MAPPING_QUALITY, bamToGASVParams.getMinMappingQuality() );
        pref.putInt( PROP_MAX_PAIR_LENGTH, bamToGASVParams.getMaxPairLength() );
        pref.put( PROP_FRAGMENT_BOUNDS_METHOD, bamToGASVParams.getFragmentBoundsMethod().getTypeString() );
        pref.putInt( PROP_DIST_PCT_VALUE, bamToGASVParams.getDistPCTValue() );
        pref.putInt( PROP_DIST_SD_VALUE, bamToGASVParams.getDistSDValue() );
        pref.put( PROP_DIST_EXACT_VALUE, bamToGASVParams.getDistExactValue() );
        pref.put( PROP_DIST_FILE, bamToGASVParams.getDistFile() );
        pref.put( PROP_SAM_VALI_STRINGENCY, bamToGASVParams.getSamValidationStringency().getTypeString() );
    }


}
