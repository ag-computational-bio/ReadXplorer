package de.cebitec.vamp.ui.visualisation.reference;

import de.cebitec.centrallookup.ProjectLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.IThumbnailView;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sun.applet.AppletPanel;

/**
 *
 * @author ddoppmeier
 */
public class JumpPanel extends javax.swing.JPanel implements LookupListener, PropertyChangeListener {

    private final static long serialVersionUID = 247246234;
    private int jumpPosition;
    private PersistantReference refGen;
    private ReferenceConnector refGenCon;
    private BoundsInfoManager boundsManager;
    private ReferenceViewer curRefViewer;
    private Lookup.Result<ReferenceViewer> res;

    public BoundsInfoManager getBoundsManager() {
        return boundsManager;
    }

    /** Creates new form JumpPanel */
    public JumpPanel() {
        initComponents();
        jumpPosition = 1;
        filterText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();

            }
        });

        //Listener for TableSelect-Events
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selectedRows = jTable1.getSelectedRows();
                if (selectedRows.length > 0) {
                    int correctedRow = jTable1.convertRowIndexToModel(selectedRows[0]);
                    PersistantFeature feature = (PersistantFeature) jTable1.getModel().getValueAt(correctedRow, 0);
                    boundsManager.navigatorBarUpdated(feature.getStart());
                }
            }
        });

        jTable1.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger())) {
                    showPopUp(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger())) {
                }
            }

            private void showPopUp(MouseEvent e) {
                curRefViewer.requestFocus();
                final IThumbnailView thumb = Lookup.getDefault().lookup(IThumbnailView.class);
                if (thumb != null) {
                    JPopupMenu popUp = new JPopupMenu();
                    JMenuItem showThumbnail = new JMenuItem("Show ThumbnailView");
                    showThumbnail.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int[] selectedRows = jTable1.getSelectedRows();
                            if (selectedRows.length > 0) {
                                for (int i : selectedRows) {
                                    int correctedRow = jTable1.convertRowIndexToModel(i);
                                    PersistantFeature feature = (PersistantFeature) jTable1.getModel().getValueAt(correctedRow, 0);
                                    thumb.addToList(feature, curRefViewer);
                                }
                            }
                            //Get all open Components and filter for AppPanelTopComponent
                            Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
                            AppPanelTopComponent appComp = null;
                            for (Iterator<TopComponent> it = topComps.iterator(); it.hasNext();) {
                                TopComponent topComponent = it.next();
                                if (topComponent instanceof AppPanelTopComponent) {
                                    appComp = (AppPanelTopComponent) topComponent;
                                    break;
                                }
                            }
                            if (appComp != null) {
                                Lookup lo = appComp.getLookup();
                                //Get ViewController from AppPanelTopComponent-Lookup
                                ViewController co = lo.lookup(ViewController.class);
                                thumb.showThumbnailView(curRefViewer,co);
                            }
                        }
                    });
                    popUp.add(showThumbnail);
                    popUp.show(jTable1, e.getX(), e.getY());
                }
            }
        });

        //PropertyChangeListener for RevViewer
        res = Utilities.actionsGlobalContext().lookupResult(ReferenceViewer.class);
        res.addLookupListener(this);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jumpPositionLabel = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jumpFilterLabel = new javax.swing.JLabel();
        jumpButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        filterText = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Navigation"));
        setPreferredSize(new java.awt.Dimension(190, 500));

        jumpPositionLabel.setText("Position:");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });

        jumpFilterLabel.setText("Filter:");

        jumpButton.setText("Go");
        jumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpButtonActionPerformed(evt);
            }
        });

        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jumpPositionLabel)
                .addContainerGap(126, Short.MAX_VALUE))
            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jumpFilterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterText, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jumpButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jumpPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jumpButton)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jumpFilterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
        //jumpList.setSelectedValue(null, false);
        //DefaultListSelectionModel model = (DefaultListSelectionModel) jumpList.getSelectionModel();
        //model.clearSelection();
}//GEN-LAST:event_jTextField1KeyTyped

    private void jumpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpButtonActionPerformed
        if (isValidInput(jTextField1.getText())) {
            jumpPosition = Integer.parseInt(jTextField1.getText());
            boundsManager.navigatorBarUpdated(jumpPosition);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a valid position!", "Invalid Position", JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_jumpButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        jumpButtonActionPerformed(evt);
    }//GEN-LAST:event_jTextField1ActionPerformed

    private boolean isValidInput(String s) {
        try {
            int tmp = Integer.parseInt(s);
            if (tmp >= 1 && tmp <= refGen.getSequence().length()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jumpButton;
    private javax.swing.JLabel jumpFilterLabel;
    private javax.swing.JLabel jumpPositionLabel;
    // End of variables declaration//GEN-END:variables

    public void setReferenceGenome(PersistantReference refGen) {
        this.refGen = refGen;
        refGenCon = ProjectConnector.getInstance().getRefGenomeConnector(refGen.getId());
        fillFeatureList();
    }

    private void fillFeatureList() {
        List<PersistantFeature> feat = refGenCon.getFeaturesForRegion(0, refGen.getSequence().length());
        Collections.sort(feat, new FeatureNameSorter());
        PersistantFeature[] featureData = feat.toArray(new PersistantFeature[0]);

        //Create new Model for Table
        jTable1.setModel(new FeatureTableModel(featureData));
        jTable1.setRowSorter(new TableRowSorter<TableModel>(jTable1.getModel()));
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);
    }

    /*
     * Uses regular expression to filter all matching entries in Product-Column.
     */
    private void updateFilter() {
        RowFilter<TableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter(filterText.getText(), 1);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        ((TableRowSorter<TableModel>) jTable1.getRowSorter()).setRowFilter(rf);
    }

    public void setBoundsInfoManager(BoundsInfoManager boundsManager) {
        this.boundsManager = boundsManager;

    }

    @Override
    public void resultChanged(LookupEvent le) {
        for (ReferenceViewer refViewer : res.allInstances()) {
            refViewer.addPropertyChangeListener(this);
            curRefViewer = refViewer;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //do something because jumpPanel gets updated every time referenceViewer triggers PropertyChangeEvent
    }

    void clearFilter() {
        filterText.setText("");
    }

    private class FeatureNameSorter implements Comparator<PersistantFeature> {

        @Override
        public int compare(PersistantFeature o1, PersistantFeature o2) {
            String name1 = o1.getLocus();
            String name2 = o2.getLocus();

            // null string is always "bigger" than anything else
            if (name1 == null && name1 != null) {
                return 1;
            } else if (name1 != null && name2 == null) {
                return -1;
            } else if (name1 == name2) {
                // both are null
                return 0;
            } else {
                return name1.compareTo(name2);
            }
        }
    }
}
