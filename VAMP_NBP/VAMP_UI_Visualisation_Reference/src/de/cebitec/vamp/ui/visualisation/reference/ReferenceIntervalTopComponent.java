package de.cebitec.vamp.ui.visualisation.reference;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
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
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.ui.visualisation.reference//ReferenceInterval//EN", autostore = false)
public final class ReferenceIntervalTopComponent extends TopComponent implements LookupListener, MousePositionListener {

    private static final long serialVersionUID = 1L;
    private static ReferenceIntervalTopComponent instance;
    private Result<ReferenceViewer> result;
    private boolean showCurrentPosition = true;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ReferenceIntervalTopComponent";

    public ReferenceIntervalTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ReferenceIntervalTopComponent.class, "CTL_ReferenceIntervalTopComponent"));
        setToolTipText(NbBundle.getMessage(ReferenceIntervalTopComponent.class, "HINT_ReferenceIntervalTopComponent"));
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

        jLabel1 = new javax.swing.JLabel();
        intervalFromField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        intervalToField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        statisticsList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        currentPosField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel1.toolTipText")); // NOI18N

        intervalFromField.setEditable(false);
        intervalFromField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.intervalFromField.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel2.toolTipText")); // NOI18N

        intervalToField.setEditable(false);
        intervalToField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.intervalToField.toolTipText")); // NOI18N

        statisticsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statisticsList.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.statisticsList.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(statisticsList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel5.toolTipText")); // NOI18N

        currentPosField.setEditable(false);
        currentPosField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.currentPosField.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ReferenceIntervalTopComponent.class, "ReferenceIntervalTopComponent.jLabel6.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(intervalToField, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .addComponent(intervalFromField, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .addComponent(currentPosField, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalFromField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalToField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentPosField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentPosField;
    private javax.swing.JTextField intervalFromField;
    private javax.swing.JTextField intervalToField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList statisticsList;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ReferenceIntervalTopComponent getDefault() {
        if (instance == null) {
            instance = new ReferenceIntervalTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ReferenceIntervalTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ReferenceIntervalTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ReferenceIntervalTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ReferenceIntervalTopComponent) {
            return (ReferenceIntervalTopComponent) win;
        }
        Logger.getLogger(ReferenceIntervalTopComponent.class.getName()).warning(
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
        for (ReferenceViewer referenceViewer : result.allInstances()) {
            // update visible feature list
            Map<Integer, Integer> featureStats = referenceViewer.getFeatureStats();
            showFeatureStatisticsForIntervall(featureStats);

            // update intervall
            BoundsInfo boundsInfo = referenceViewer.getBoundsInfo();
            setIntervall(boundsInfo.getLogLeft(), boundsInfo.getLogRight());

            // register listeners so every change occurs
            referenceViewer.addPropertyChangeListener(ReferenceViewer.PROP_FEATURE_STATISTICS_CHANGED, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // update visible feature list
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> featureStats = (Map<Integer, Integer>) evt.getNewValue();
                    showFeatureStatisticsForIntervall(featureStats);

                    // update intervall
                    BoundsInfo boundsInfo = ((ReferenceViewer) evt.getSource()).getBoundsInfo();
                    setIntervall(boundsInfo.getLogLeft(), boundsInfo.getLogRight());
                }
            });
            referenceViewer.addPropertyChangeListener(ReferenceViewer.PROP_MOUSEPOSITION_CHANGED, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setCurrentMousePosition((Integer) evt.getNewValue());
                }
            });
            referenceViewer.addPropertyChangeListener(ReferenceViewer.PROP_MOUSEOVER_REQUESTED, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setMouseOverPaintingRequested((Boolean) evt.getNewValue());
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

    private void setIntervall(int from, int to){
        intervalFromField.setText(String.valueOf(from));
        intervalToField.setText(String.valueOf(to));
    }

    private void showFeatureStatisticsForIntervall(Map<Integer, Integer> featureStats) {
        statisticsList.removeAll();
        DefaultListModel model = new DefaultListModel();

        Set<Integer> keys = featureStats.keySet();
        for(Iterator<Integer> it = keys.iterator(); it.hasNext(); ){
            int type = it.next();
            String typeS = FeatureType.getTypeString(type);
            model.addElement(typeS+": "+featureStats.get(type));
        }
        statisticsList.setModel(model);
    }

    @Override
    public void setCurrentMousePosition(int logPos) {
        if(showCurrentPosition){
            currentPosField.setText(String.valueOf(logPos));
        } else {
            currentPosField.setText("");
        }
    }

    @Override
    public void setMouseOverPaintingRequested(boolean requested) {
        showCurrentPosition = requested;
        if(showCurrentPosition == false){
            currentPosField.setText("");
        }
    }

}
