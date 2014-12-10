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

package de.cebitec.readxplorer.tools.coverageanalysis.featureCoverageAnalysis;


import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.ui.TopComponentHelper;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * TopComponent for displaying all gui elements belonging to the feature
 * coverage
 * analysis.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ConvertAsProperties(
         dtd = "-//de.cebitec.readxplorer.genomeAnalyses//CoveredFeaturesAnalysis//EN",
         autostore = false )
@TopComponent.Description(
         preferredID = "CoveredFeaturesAnalysisTopComponent",
         iconBase = "de/cebitec/readxplorer/genomeAnalyses/coveredFeatures.png",
         persistenceType = TopComponent.PERSISTENCE_ALWAYS )
@TopComponent.Registration( mode = "output", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readxplorer.genomeAnalyses.CoveredFeaturesAnalysisTopComponent" )
@ActionReference( path = "Menu/Window" /*, position = 333 */ )
@TopComponent.OpenActionRegistration(
         displayName = "#CTL_CoveredFeaturesAnalysisAction",
         preferredID = "FeatureCoverageAnalysisTopComponent" )
@Messages( {
    "CTL_CoveredFeaturesAnalysisAction=FeatureCoverageAnalysis",
    "CTL_CoveredFeaturesAnalysisTopComponent=Feature Coverage Analysis Window",
    "HINT_CoveredFeaturesAnalysisTopComponent=This is a Feature Coverage Analysis window"
} )
public final class CoveredFeaturesAnalysisTopComponent extends TopComponentExtended {

    private static final long serialVersionUID = 1L;


    /**
     * TopComponent for displaying all gui elements belonging to the feature
     * coverage analysis.
     */
    public CoveredFeaturesAnalysisTopComponent() {
        initComponents();
        setName( Bundle.CTL_CoveredFeaturesAnalysisTopComponent() );
        setToolTipText( Bundle.HINT_CoveredFeaturesAnalysisTopComponent() );
        putClientProperty( TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE );

        TopComponentHelper.setupContainerListener( coveredFeaturesTabbedPane, preferredID() );

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coveredFeaturesTabbedPane = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(coveredFeaturesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(coveredFeaturesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane coveredFeaturesTabbedPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // add custom code on component opening
    }


    @Override
    public void componentClosed() {
        // add custom code on component closing
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // store your settings
    }


    void readProperties( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // read your settings according to their version
    }


    /**
     * This method needs to be called in order to open a new tab for a
     * covered feature detection. Make sure to call {@link setAnalysisContext()}
     * first in order to display the correct context for the analysis result.
     * <p>
     * @param panelName   title of the new tab to create
     * @param resultPanel the panel to place in the new tab
     */
    public void openAnalysisTab( String panelName, JPanel resultPanel ) {
        TopComponentHelper.openTableTab( coveredFeaturesTabbedPane, panelName, resultPanel );
    }


    /**
     * @return true, if this component already contains other components, false
     *         otherwise.
     */
    public boolean hasComponents() {
        return this.coveredFeaturesTabbedPane.getComponentCount() > 0;
    }


}
