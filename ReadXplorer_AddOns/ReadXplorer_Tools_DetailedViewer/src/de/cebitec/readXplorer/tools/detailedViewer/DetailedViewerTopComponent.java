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
package de.cebitec.readXplorer.tools.detailedViewer;

import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.view.TopComponentExtended;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.readXplorer.view.dataVisualisation.histogramViewer.HistogramViewer;
import java.awt.CardLayout;
import java.util.logging.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//de.cebitec.readXplorer.tools.detailedViewer//DetailedViewer//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DetailedViewerTopComponent",
        iconBase = "de/cebitec/readXplorer/tools/detailedViewer/detailedViewer.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.readXplorer.tools.detailedViewer.DetailedViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DetailedViewerAction",
        preferredID = "DetailedViewerTopComponent"
)
@Messages({
    "CTL_DetailedViewerAction=DetailedViewer",
    "CTL_DetailedViewerTopComponent=DetailedViewer Window",
    "HINT_DetailedViewerTopComponent=This is a DetailedViewer window"
})
public final class DetailedViewerTopComponent extends TopComponentExtended {
    private static final long serialVersionUID = 1L;
    private static final String PREFERRED_ID = "DetailedViewerTopComponent";
    
    private static DetailedViewerTopComponent instance;
    
    private TrackConnector trackConnector;
    private BasePanel alignmentBasePanel;
    private BasePanel histogramBasePanel;
    private BasePanel readPairBasePanel;
    private CardLayout cards;

    private static String HISTOGRAMCARD = "histo";
    private static String ALIGNMENTCARD = "alignment";
    private static String READPAIRCARD = "readPair";

    private String selectedViewer;
    private ViewController viewCon;
    
    public DetailedViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_DetailedViewerTopComponent());
        setToolTipText(Bundle.HINT_DetailedViewerTopComponent());
        this.viewCon = Utilities.actionsGlobalContext().lookup(ViewController.class);
    }
    
    public DetailedViewerTopComponent(ViewController viewCon) {
        this();
        this.viewCon = viewCon;

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        switchPanel = new javax.swing.JPanel();
        histogramButton = new javax.swing.JButton();
        alignmentButton = new javax.swing.JButton();
        colorHistogramBox = new javax.swing.JCheckBox();
        readPairButton = new javax.swing.JButton();
        viewerPanel = new javax.swing.JPanel();
        cardPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        switchPanel.setPreferredSize(new java.awt.Dimension(120, 100));

        org.openide.awt.Mnemonics.setLocalizedText(histogramButton, org.openide.util.NbBundle.getMessage(DetailedViewerTopComponent.class, "DetailedViewerTopComponent.histogramButton.text_1")); // NOI18N
        histogramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histogramButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(alignmentButton, org.openide.util.NbBundle.getMessage(DetailedViewerTopComponent.class, "DetailedViewerTopComponent.alignmentButton.text_1")); // NOI18N
        alignmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignmentButtonActionPerformed(evt);
            }
        });

        colorHistogramBox.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(colorHistogramBox, org.openide.util.NbBundle.getMessage(DetailedViewerTopComponent.class, "DetailedViewerTopComponent.colorHistogramBox.text_1")); // NOI18N
        colorHistogramBox.setMinimumSize(new java.awt.Dimension(50, 22));
        colorHistogramBox.setPreferredSize(new java.awt.Dimension(100, 22));
        colorHistogramBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorHistogramBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(readPairButton, org.openide.util.NbBundle.getMessage(DetailedViewerTopComponent.class, "DetailedViewerTopComponent.readPairButton.text_1")); // NOI18N
        readPairButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readPairButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout switchPanelLayout = new javax.swing.GroupLayout(switchPanel);
        switchPanel.setLayout(switchPanelLayout);
        switchPanelLayout.setHorizontalGroup(
            switchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(switchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(switchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(histogramButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alignmentButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(readPairButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(switchPanelLayout.createSequentialGroup()
                .addComponent(colorHistogramBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        switchPanelLayout.setVerticalGroup(
            switchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(switchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(histogramButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alignmentButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(readPairButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(colorHistogramBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(171, Short.MAX_VALUE))
        );

        add(switchPanel, java.awt.BorderLayout.WEST);

        viewerPanel.setPreferredSize(new java.awt.Dimension(490, 300));
        viewerPanel.setLayout(new java.awt.BorderLayout());

        cardPanel.setPreferredSize(new java.awt.Dimension(470, 300));
        cardPanel.setLayout(new java.awt.CardLayout());
        viewerPanel.add(cardPanel, java.awt.BorderLayout.CENTER);

        add(viewerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void histogramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histogramButtonActionPerformed
        this.selectedViewer = HISTOGRAMCARD;
        this.changeViewerStatus(this.selectedViewer, true);
        this.changeViewerStatus(ALIGNMENTCARD, false);
        this.changeViewerStatus(READPAIRCARD, false);
        cards.show(cardPanel, HISTOGRAMCARD);
    }//GEN-LAST:event_histogramButtonActionPerformed

    private void alignmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignmentButtonActionPerformed
        this.selectedViewer = ALIGNMENTCARD;
        this.changeViewerStatus(HISTOGRAMCARD, false);
        this.changeViewerStatus(this.selectedViewer, true);
        this.changeViewerStatus(READPAIRCARD, false);
        //alignmentBasePanel.getViewer().setActive(true); //to ensure size calculation is performed correctly
        cards.show(cardPanel, ALIGNMENTCARD);
    }//GEN-LAST:event_alignmentButtonActionPerformed

    private void colorHistogramBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorHistogramBoxActionPerformed
        HistogramViewer histViewer = (HistogramViewer) histogramBasePanel.getViewer();
        histViewer.setIsColored(colorHistogramBox.isSelected());
        histViewer.boundsChangedHook();
        histViewer.repaint();
    }//GEN-LAST:event_colorHistogramBoxActionPerformed

    private void readPairButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readPairButtonActionPerformed
        this.selectedViewer = READPAIRCARD;
        this.changeViewerStatus(HISTOGRAMCARD, false);
        this.changeViewerStatus(ALIGNMENTCARD, false);
        this.changeViewerStatus(this.selectedViewer, true);
        this.cards.show(cardPanel, READPAIRCARD);
    }//GEN-LAST:event_readPairButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton alignmentButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JCheckBox colorHistogramBox;
    private javax.swing.JButton histogramButton;
    private javax.swing.JButton readPairButton;
    private javax.swing.JPanel switchPanel;
    private javax.swing.JPanel viewerPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     * @return default instance
     */
    public static synchronized DetailedViewerTopComponent getDefault() {
        if (instance == null) {
            instance = new DetailedViewerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DetailedViewerTopComponent instance. Never call
     * {@link #getDefault} directly!
     * @return the DetailedViewerTopComponent instance
     */
    public static synchronized DetailedViewerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DetailedViewerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DetailedViewerTopComponent) {
            return (DetailedViewerTopComponent) win;
        }
        Logger.getLogger(DetailedViewerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        BasePanelFactory factory = viewCon.getBasePanelFac();

        if (this.trackConnector.isReadPairTrack()) {
            this.readPairBasePanel = factory.getReadPairBasePanel(this.trackConnector);
            this.changeViewerStatus(READPAIRCARD, false);
            this.cardPanel.add(this.readPairBasePanel, READPAIRCARD);
        } else {
            this.readPairButton.setEnabled(false);
        }

        this.histogramBasePanel = factory.getHistogrammViewerBasePanel(this.trackConnector);
        this.changeViewerStatus(HISTOGRAMCARD, true);
        this.selectedViewer = HISTOGRAMCARD;
        this.alignmentBasePanel = factory.getAlignmentViewBasePanel(this.trackConnector);
//        this.alignmentBasePanel.setPreferredSize(new Dimension(490, 300));
        this.changeViewerStatus(ALIGNMENTCARD, false);
        this.cards = (CardLayout) this.cardPanel.getLayout();

        this.cardPanel.add(this.alignmentBasePanel, ALIGNMENTCARD);
        this.cardPanel.add(this.histogramBasePanel, HISTOGRAMCARD);
        this.cards.show(this.cardPanel, HISTOGRAMCARD);
//        this.histogramButton.setEnabled(false);

    }

    @Override
    public void componentClosed() {
        this.alignmentBasePanel.close();
        this.histogramBasePanel.close();
        this.alignmentBasePanel = null;
        this.histogramBasePanel = null;
        if (this.readPairBasePanel != null) {
            this.readPairBasePanel.close();
            this.readPairBasePanel = null;
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Sets the corresponding track connector for this detailed viewer.
     * @param trackConnector the corresponding track connector for this detailed
     * viewer
     */
    public void setTrackConnector(TrackConnector trackConnector) {
        this.trackConnector = trackConnector;
        setName(NbBundle.getMessage(DetailedViewerTopComponent.class, "CTL_DetailedViewerTopComponent") + trackConnector.getAssociatedTrackName());
    }

    /*
     * Overriding these two methods ensures that only displayed components are updated
     * and thus increases performance of the viewers.
     */
    @Override
    public void componentShowing() {
        changeViewerStatus(getActiveViewer(), true);
    }

    @Override
    public void componentHidden() {
        changeViewerStatus(getActiveViewer(), false);
    }

    /**
     * @return the property representing the currently active viewer
     */
    public String getActiveViewer() {
        return this.selectedViewer;
    }

    /**
     * Update the viewer status of the selectedViewer
     * @param selectedViewer the viewer whose status is to be changed
     * @param activated true, if the viewer should be activated, false, if not
     */
    public void changeViewerStatus(String selectedViewer, boolean activated) {
        if (selectedViewer.equals(HISTOGRAMCARD)) {
            this.histogramBasePanel.getViewer().setActive(activated);
//            this.histogramButton.setEnabled(!activated);
        }
        if (selectedViewer.equals(ALIGNMENTCARD)) {
            this.alignmentBasePanel.getViewer().setActive(activated);
//            this.alignmentButton.setEnabled(!activated);
        }
        if (this.readPairBasePanel != null && selectedViewer.equals(READPAIRCARD)) {
//            this.readPairButton.setEnabled(!activated);
            this.readPairBasePanel.getViewer().setActive(activated);
        }
    }
}
