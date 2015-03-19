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

import bio.comp.jlu.readxplorer.tools.gasv.ParametersGASVMain;
import de.cebitec.readxplorer.ui.dialogmenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;


/**
 * The GASVMain configuration wizard panel.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class GASVMainWizardPanel extends ChangeListeningWizardPanel {

    public static final String PROP_GASV_MAIN_PARAMS = "gasvMainParams";

    private static final String PROP_MIN_CLUSTER_SIZE = "minClusterSize";
    private static final String PROP_MAX_CLUSTER_SIZE = "maxClusterSize";
    private static final String PROP_MAX_CLIQUE_SIZE = "maxCliqueSize";
    private static final String PROP_MAX_READ_PAIRS = "maxReadPairs";
    private static final String PROP_OUTPUT_TYPE = "outputType";
    private static final String PROP_IS_MAX_SUB_CLUSTERS = "isMaxSubCluster";
    private static final String PROP_IS_NONRECIPROCAL = "isNonreciprocal";
    private static final String PROP_IS_HEADERLESS = "isHeaderless";
    private static final String PROP_IS_FASTER = "isFaster";
    private static final String PROP_IS_VERBOSE = "isVerbose";
    private static final String PROP_IS_DEBUG = "isDebug";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GASVMainVisualPanel component;


    /**
     * The GASVMain configuration wizard panel.
     */
    public GASVMainWizardPanel() {
        super( "Please enter valid parameters (Only number & percent values. Quality values need to be between 0 & 127)" );
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GASVMainVisualPanel getComponent() {
        if( component == null ) {
            component = new GASVMainVisualPanel( new ParametersGASVMain() );
        }
        return component;
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        super.readSettings( wiz );
//        if( wiz.getProperty( PROP_GASV_MAIN_PARAMS ) != null ) {
//            component.setLastParameterSelection( (ParametersGASVMain) wiz.getProperty( PROP_GASV_MAIN_PARAMS ) );
//        } else {
        int minClusterSize = getPref().getInt( PROP_MIN_CLUSTER_SIZE, 30 );
        int maxClusterSize = getPref().getInt( PROP_MAX_CLUSTER_SIZE, 0 );
        int maxCliqueSize = getPref().getInt( PROP_MAX_CLIQUE_SIZE, 0 );
        int maxReadPairs = getPref().getInt( PROP_MAX_READ_PAIRS, 0 );
        String outputType = getPref().get( PROP_OUTPUT_TYPE, ParametersGASVMain.OUT_STANDARD );
        boolean isMaxSubClusters = getPref().getBoolean( PROP_IS_MAX_SUB_CLUSTERS, false );
        boolean isNonreciprocal = getPref().getBoolean( PROP_IS_NONRECIPROCAL, false );
        boolean isHeaderless = getPref().getBoolean( PROP_IS_HEADERLESS, false );
        boolean isFaster = getPref().getBoolean( PROP_IS_FASTER, false );
        boolean isVerbose = getPref().getBoolean( PROP_IS_VERBOSE, false );
        boolean isDebug = getPref().getBoolean( PROP_IS_DEBUG, false );

        ParametersGASVMain parametersGASVMain = new ParametersGASVMain( minClusterSize,
                                                                        maxClusterSize,
                                                                        maxCliqueSize,
                                                                        maxReadPairs,
                                                                        outputType,
                                                                        isMaxSubClusters,
                                                                        isNonreciprocal,
                                                                        isHeaderless,
                                                                        isFaster,
                                                                        isVerbose,
                                                                        isDebug );

        component.setLastParameterSelection( parametersGASVMain );
//        }
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        if( this.isValid() ) {
            wiz.putProperty( PROP_GASV_MAIN_PARAMS, component.getGASVMainParams() );
            this.storePrefs();
        }
    }


    /**
     * Stores the selected parameters for this specific wizard page for later
     * use, also after restarting the software.
     */
    private void storePrefs() {
        ParametersGASVMain gasvMainParams = component.getGASVMainParams();
        getPref().putInt( PROP_MIN_CLUSTER_SIZE, gasvMainParams.getMinClusterSize() );
        getPref().putInt( PROP_MAX_CLUSTER_SIZE, gasvMainParams.getMaxClusterSize() );
        getPref().putInt( PROP_MAX_CLIQUE_SIZE, gasvMainParams.getMaxCliqueSize() );
        getPref().putInt( PROP_MAX_READ_PAIRS, gasvMainParams.getMaxReadPairs() );
        getPref().put( PROP_OUTPUT_TYPE, gasvMainParams.getOutputType() );
        getPref().putBoolean( PROP_IS_MAX_SUB_CLUSTERS, gasvMainParams.isMaxSubClusters() );
        getPref().putBoolean( PROP_IS_NONRECIPROCAL, gasvMainParams.isNonreciprocal() );
        getPref().putBoolean( PROP_IS_HEADERLESS, gasvMainParams.isHeaderless() );
        getPref().putBoolean( PROP_IS_FASTER, gasvMainParams.isFast() );
        getPref().putBoolean( PROP_IS_VERBOSE, gasvMainParams.isVerbose() );
        getPref().putBoolean( PROP_IS_DEBUG, gasvMainParams.isDebug() );
    }


}
