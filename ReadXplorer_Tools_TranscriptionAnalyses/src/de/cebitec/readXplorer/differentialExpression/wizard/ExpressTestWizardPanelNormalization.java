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

package de.cebitec.readXplorer.differentialExpression.wizard;


import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;


public class ExpressTestWizardPanelNormalization implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ExpressTestVisualPanelNormalization component;


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ExpressTestVisualPanelNormalization getComponent() {
        if( component == null ) {
            component = new ExpressTestVisualPanelNormalization();
        }
        return component;
    }


    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }


    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }


    @Override
    public void addChangeListener( ChangeListener l ) {
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        int id = (int) wiz.getProperty( "genomeID" );
        Set<FeatureType> usedFeatures = (Set<FeatureType>) wiz.getProperty( "featureType" );
        ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( id );
        List<PersistentFeature> allRefFeatures = new ArrayList<>();
        for( PersistentChromosome chrom : referenceConnector.getChromosomesForGenome().values() ) {
            int chromLength = chrom.getLength();
            allRefFeatures.addAll( referenceConnector.getFeaturesForRegion( 1, chromLength, usedFeatures, chrom.getId() ) );
        }
        getComponent().setFeatureList( allRefFeatures );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        boolean useHouseKeepingGenesToNormalize = getComponent().useHouseKeepingGenesToNormalize();
        wiz.putProperty( "useHouseKeepingGenesToNormalize", useHouseKeepingGenesToNormalize );
        if( useHouseKeepingGenesToNormalize ) {
            List<Integer> normalizationFeatures = getComponent().getSelectedFeatures();
            wiz.putProperty( "normalizationFeatures", normalizationFeatures );
        }
    }


    @Override
    public void validate() throws WizardValidationException {
    }


}
