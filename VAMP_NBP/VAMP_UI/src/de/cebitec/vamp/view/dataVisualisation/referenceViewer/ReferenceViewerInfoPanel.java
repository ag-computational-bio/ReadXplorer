package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.objects.FeatureType;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.basePanel.AbstractInfoPanel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceViewerInfoPanel extends AbstractInfoPanel implements MousePositionListener {

    private static final long serialVersionUID = 246312994;
    private PersistantReference reference;
    private boolean showCurrentPosition;

    /** Creates new form GenomeViewerInfoPanel */
    public ReferenceViewerInfoPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
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
        jPanel2 = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        typeText = new javax.swing.JTextField();
        productLabel = new javax.swing.JLabel();
        strandText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        productText = new javax.swing.JTextField();
        stopLabel = new javax.swing.JLabel();
        ecNumField = new javax.swing.JTextField();
        stopField = new javax.swing.JTextField();
        locusField = new javax.swing.JTextField();
        locusLabel = new javax.swing.JLabel();
        startField = new javax.swing.JTextField();
        startLabel = new javax.swing.JLabel();
        toolTabbedPane = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(238, 181));
        setPreferredSize(new java.awt.Dimension(238, 181));

        jTabbedPane1.setToolTipText("");

        jPanel1.setToolTipText("");

        jLabel1.setText("left:");
        jLabel1.setToolTipText("");

        intervalFromField.setEditable(false);
        intervalFromField.setToolTipText("");

        jLabel2.setText("right:");
        jLabel2.setToolTipText("");

        intervalToField.setEditable(false);
        intervalToField.setToolTipText("");

        statisticsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statisticsList.setToolTipText("");
        jScrollPane1.setViewportView(statisticsList);

        jLabel5.setText("Mouse pos.:");
        jLabel5.setToolTipText("");

        currentPosField.setEditable(false);
        currentPosField.setToolTipText("");

        jLabel6.setText("Visible features:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(intervalToField, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                            .addComponent(intervalFromField, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                            .addComponent(currentPosField, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalFromField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalToField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentPosField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Interval", jPanel1);

        typeLabel.setText("Type:");
        typeLabel.setToolTipText("");

        jLabel3.setText("Strand:");
        jLabel3.setToolTipText("");

        typeText.setEditable(false);
        typeText.setToolTipText("");

        productLabel.setText("Product:");
        productLabel.setToolTipText("");

        strandText.setEditable(false);
        strandText.setToolTipText("");

        jLabel4.setText("EC number:");
        jLabel4.setToolTipText("");

        productText.setEditable(false);
        productText.setToolTipText("");

        stopLabel.setText("Stop:");
        stopLabel.setToolTipText("");

        ecNumField.setEditable(false);
        ecNumField.setToolTipText("");

        stopField.setEditable(false);
        stopField.setToolTipText("");

        locusField.setEditable(false);
        locusField.setToolTipText("");

        locusLabel.setText("Locus:");
        locusLabel.setToolTipText("");

        startField.setEditable(false);
        startField.setToolTipText("");

        startLabel.setText("Start:");
        startLabel.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stopLabel)
                    .addComponent(jLabel4)
                    .addComponent(startLabel)
                    .addComponent(locusLabel)
                    .addComponent(productLabel)
                    .addComponent(jLabel3)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(locusField, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(startField, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(stopField, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(ecNumField, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(productText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(strandText, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ecNumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(strandText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Feature", jPanel2);

        jButton1.setText("Reference Editor");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolTabbedPaneLayout = new javax.swing.GroupLayout(toolTabbedPane);
        toolTabbedPane.setLayout(toolTabbedPaneLayout);
        toolTabbedPaneLayout.setHorizontalGroup(
            toolTabbedPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolTabbedPaneLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jButton1)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        toolTabbedPaneLayout.setVerticalGroup(
            toolTabbedPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolTabbedPaneLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton1)
                .addContainerGap(192, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Tools", toolTabbedPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//      ReferenceEditor refEditor =  new ReferenceEditor(getReference());
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentPosField;
    private javax.swing.JTextField ecNumField;
    private javax.swing.JTextField intervalFromField;
    private javax.swing.JTextField intervalToField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField locusField;
    private javax.swing.JLabel locusLabel;
    private javax.swing.JLabel productLabel;
    private javax.swing.JTextField productText;
    private javax.swing.JTextField startField;
    private javax.swing.JLabel startLabel;
    private javax.swing.JList statisticsList;
    private javax.swing.JTextField stopField;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JTextField strandText;
    private javax.swing.JPanel toolTabbedPane;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeText;
    // End of variables declaration//GEN-END:variables

    public void setIntervall(int from, int to){
        intervalFromField.setText(String.valueOf(from));
        intervalToField.setText(String.valueOf(to));
    }

    public void showFeatureDetails(PersistantFeature f) {
        ecNumField.setText(f.getEcNumber());
        startField.setText(String.valueOf(f.getStart()));
        stopField.setText(String.valueOf(f.getStop()));
        productText.setText(f.getProduct());
        productText.setToolTipText(f.getProduct());
        locusField.setText(f.getLocus());
        String strand = (f.getStrand() == 1? "forward" : "reverse");
        strandText.setText(strand);
        typeText.setText(FeatureType.getTypeString(f.getType()));
    }

    public void showFeatureStatisticsForIntervall(Map<Integer, Integer> featureStats) {
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

    @Override
    public void close() {

    }

    public PersistantReference getReference() {
        return reference;
    }

    public void setReference(PersistantReference reference) {
        this.reference = reference;
    }

    

}
