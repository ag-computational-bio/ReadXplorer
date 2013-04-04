package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.util.TabWithCloseX;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * TopComponent for displaying all gui elements belonging to the covered feature
 * detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ConvertAsProperties(
    dtd = "-//de.cebitec.vamp.genomeAnalyses//CoveredFeaturesAnalysis//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "CoveredFeaturesAnalysisTopComponent",
iconBase = "de/cebitec/vamp/genomeAnalyses/coveredFeatures.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.vamp.genomeAnalyses.CoveredFeaturesAnalysisTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_CoveredFeaturesAnalysisAction",
preferredID = "CoveredFeaturesAnalysisTopComponent")
@Messages({
    "CTL_CoveredFeaturesAnalysisAction=CoveredFeaturesAnalysis",
    "CTL_CoveredFeaturesAnalysisTopComponent=Covered Features Analysis Window",
    "HINT_CoveredFeaturesAnalysisTopComponent=This is a Covered Features Analysis window"
})
public final class CoveredFeaturesAnalysisTopComponent extends TopComponent {
    
    public static final String PREFERRED_ID = "CoveredFeaturesAnalysisTopComponent";
    private static final long serialVersionUID = 1L;

    /**
     * TopComponent for displaying all gui elements belonging to the covered
     * feature detection.
     */
    public CoveredFeaturesAnalysisTopComponent() {
        initComponents();
        setName(Bundle.CTL_CoveredFeaturesAnalysisTopComponent());
        setToolTipText(Bundle.HINT_CoveredFeaturesAnalysisTopComponent());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        
        // add listener to close TopComponent when no tabs are shown
        this.coveredFeaturesTabbedPane.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (coveredFeaturesTabbedPane.getTabCount() == 0) {
                    WindowManager.getDefault().findTopComponent(PREFERRED_ID).close();
                }
            }
        });

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
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    /**
     * This method needs to be called in order to open a new tab for a
     * covered feature detection. Make sure to call {@link setAnalysisContext()}
     * first in order to display the correct context for the analysis result.
     * @param panelName title of the new tab to create
     * @param resultPanel the panel to place in the new tab
     */
    public void openAnalysisTab(String panelName, JPanel resultPanel) {
        this.coveredFeaturesTabbedPane.add(panelName, resultPanel);
        this.coveredFeaturesTabbedPane.setTabComponentAt(this.coveredFeaturesTabbedPane.getTabCount() - 1, new TabWithCloseX(this.coveredFeaturesTabbedPane));
        this.coveredFeaturesTabbedPane.setSelectedIndex(this.coveredFeaturesTabbedPane.getTabCount() - 1);
    }
}
