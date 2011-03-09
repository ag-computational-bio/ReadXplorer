package de.cebitec.vamp.ui.visualisation.reference;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.Feature;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.ui.visualisation.reference//ReferenceFeature//EN", autostore = false)
public final class ReferenceFeatureTopComponent extends TopComponent implements LookupListener{

    private static ReferenceFeatureTopComponent instance;
    private static final long serialVersionUID = 1L;
    private Result<ReferenceViewer> result;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ReferenceFeatureTopComponent";

    public ReferenceFeatureTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ReferenceFeatureTopComponent.class, "CTL_ReferenceFeatureTopComponent"));
        setToolTipText(NbBundle.getMessage(ReferenceFeatureTopComponent.class, "HINT_ReferenceFeatureTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        typeText = new javax.swing.JTextField();
        productLabel = new javax.swing.JLabel();
        strandText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        stopLabel = new javax.swing.JLabel();
        ecNumField = new javax.swing.JTextField();
        stopField = new javax.swing.JTextField();
        locusField = new javax.swing.JTextField();
        locusLabel = new javax.swing.JLabel();
        startField = new javax.swing.JTextField();
        startLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        productText = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.typeLabel.text")); // NOI18N
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.typeLabel.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.jLabel3.toolTipText")); // NOI18N

        typeText.setEditable(false);
        typeText.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.typeText.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.productLabel.text")); // NOI18N
        productLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.productLabel.toolTipText")); // NOI18N

        strandText.setEditable(false);
        strandText.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.strandText.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.jLabel4.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stopLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.stopLabel.text")); // NOI18N
        stopLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.stopLabel.toolTipText")); // NOI18N

        ecNumField.setEditable(false);
        ecNumField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.ecNumField.toolTipText")); // NOI18N

        stopField.setEditable(false);
        stopField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.stopField.toolTipText")); // NOI18N

        locusField.setEditable(false);
        locusField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.locusField.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(locusLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.locusLabel.text")); // NOI18N
        locusLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.locusLabel.toolTipText")); // NOI18N

        startField.setEditable(false);
        startField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.startField.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(startLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.startLabel.text")); // NOI18N
        startLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComponent.class, "ReferenceFeatureTopComponent.startLabel.toolTipText")); // NOI18N

        productText.setColumns(20);
        productText.setRows(5);
        jScrollPane1.setViewportView(productText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(stopLabel)
                            .addComponent(jLabel4)
                            .addComponent(startLabel)
                            .addComponent(locusLabel)
                            .addComponent(productLabel)
                            .addComponent(typeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(typeText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(locusField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(startField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(stopField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(ecNumField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(strandText, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ecNumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(strandText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(37, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ecNumField;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField locusField;
    private javax.swing.JLabel locusLabel;
    private javax.swing.JLabel productLabel;
    private javax.swing.JTextArea productText;
    private javax.swing.JTextField startField;
    private javax.swing.JLabel startLabel;
    private javax.swing.JTextField stopField;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JTextField strandText;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeText;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ReferenceFeatureTopComponent getDefault() {
        if (instance == null) {
            instance = new ReferenceFeatureTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ReferenceFeatureTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ReferenceFeatureTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ReferenceFeatureTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ReferenceFeatureTopComponent) {
            return (ReferenceFeatureTopComponent) win;
        }
        Logger.getLogger(ReferenceFeatureTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult(ReferenceViewer.class);
        result.addLookupListener(this);
        resultChanged(new LookupEvent(result));
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        for (ReferenceViewer refViewer : result.allInstances()) {
            Feature feature = refViewer.getCurrentlySelectedFeature();
            showFeatureDetails(feature != null ? feature.getPersistantFeature() : null);

            refViewer.addPropertyChangeListener(ReferenceViewer.PROP_FEATURE_SELECTED, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    Feature feature = (Feature) evt.getNewValue();
                    showFeatureDetails(feature.getPersistantFeature());
                }
            });
        }
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
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

    public void showFeatureDetails(PersistantFeature f) {
        ecNumField.setText(f!= null ? f.getEcNumber() : "");
        startField.setText(f!= null ? String.valueOf(f.getStart()) : "");
        stopField.setText(f!= null ? String.valueOf(f.getStop()) : "");
        productText.setText(f!= null ? f.getProduct() : "");
        productText.setToolTipText(f!= null ? f.getProduct() : "");
        locusField.setText(f!= null ? f.getLocus() : "");
        typeText.setText(f!= null ? FeatureType.getTypeString(f.getType()) : "");

        String strand = "";
        if (f != null){
            strand = f.getStrand() == 1 ? "forward" : "reverse";
        }
        strandText.setText(strand);
    }

}
