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

package de.cebitec.readXplorer.transcriptionAnalyses;


import de.cebitec.readXplorer.view.TopComponentExtended;
import de.cebitec.readXplorer.view.TopComponentHelper;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * TopComponent for displaying all gui elements belonging to the transcription
 * analyses.
 *
 * @author -Rolf Hilker-
 */
@ConvertAsProperties(
         dtd = "-//de.cebitec.readXplorer.transcriptionAnalyses//TranscriptionAnalyses//EN",
         autostore = false )
@TopComponent.Description(
         preferredID = "TranscriptionAnalysesTopComponent",
         iconBase = "de/cebitec/readXplorer/transcriptionAnalyses/transcriptionAnalyses.png",
         persistenceType = TopComponent.PERSISTENCE_ALWAYS )
@TopComponent.Registration( mode = "output", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readXplorer.transcriptionAnalyses.TranscriptionAnalysesTopComponent" )
@ActionReference( path = "Menu/Window" /*, position = 333 */ )
@TopComponent.OpenActionRegistration(
         displayName = "#CTL_TranscriptionAnalysesAction",
         preferredID = "TranscriptionAnalysesTopComponent" )
@Messages( {
    "CTL_TranscriptionAnalysesAction=TranscriptionAnalyses",
    "CTL_TranscriptionAnalysesTopComponent=TranscriptionAnalyses Window",
    "HINT_TranscriptionAnalysesTopComponent=This is a TranscriptionAnalyses window"
} )
public final class TranscriptionAnalysesTopComponent extends TopComponentExtended {

    private static final long serialVersionUID = 1L;


    /**
     * TopComponent for displaying all gui elements belonging to the
     * transcription analyses.
     */
    public TranscriptionAnalysesTopComponent() {
        initComponents();
        setName( Bundle.CTL_TranscriptionAnalysesTopComponent() );
        setToolTipText( Bundle.HINT_TranscriptionAnalysesTopComponent() );
        putClientProperty( TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE );
        TopComponentHelper.setupContainerListener( analysesTabbedPane, preferredID() );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        analysesTabbedPane = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(analysesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(analysesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane analysesTabbedPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }


    @Override
    public void componentClosed() {
        this.analysesTabbedPane.removeAll();
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // TODO store your settings
    }


    void readProperties( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // TODO read your settings according to their version
    }


    /**
     * This method needs to be called in order to open a new tab for
     * transcription analyses. Make sure to call {@link setAnalysisContext()}
     * first in order to display the correct context for the analysis result.
     * <p>
     * @param panelName   title of the new tab to create
     * @param resultPanel the panel to place in the new tab
     */
    public void openAnalysisTab( final String panelName, final JPanel resultPanel ) {
        TopComponentHelper.openTableTab( analysesTabbedPane, panelName, resultPanel );
    }


    /**
     * @return true, if this component already contains other components, false
     *         otherwise.
     */
    public boolean hasComponents() {
        return this.analysesTabbedPane.getComponentCount() > 0;
    }


}