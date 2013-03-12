package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.differentialExpression.DeAnalysisHandler.AnalysisStatus;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.differentialExpression//DiffExpResultViewer//EN",
autostore = false)
@TopComponent.Description(preferredID = "DiffExpResultViewerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "bottomSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.vamp.differentialExpression.DiffExpResultViewerTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_DiffExpResultViewerAction",
preferredID = "DiffExpResultViewerTopComponent")
@Messages({
    "CTL_DiffExpResultViewerAction=DiffExpResultViewer",
    "CTL_DiffExpResultViewerTopComponent=Differential expression analysis - results",
    "HINT_DiffExpResultViewerTopComponent=This is a DiffExpResultViewer window"
})
public final class DiffExpResultViewerTopComponent extends TopComponent implements Observer, ItemListener {

    private static final long serialVersionUID = 1L;
    private TableModel tm;
    private ComboBoxModel<Object> cbm;
    private ArrayList<TableModel> tableModels = new ArrayList<>();
    private TopComponent GraficsTopComponent;
    private TopComponent LogTopComponent;
    private DeAnalysisHandler analysisHandler;
    private DeAnalysisHandler.Tool usedTool;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Differential Expression Analysis");

    public DiffExpResultViewerTopComponent() {
    }

    public DiffExpResultViewerTopComponent(DeAnalysisHandler handler, DeAnalysisHandler.Tool usedTool) {
//        BoundsInfoManager man = getLookup().lookupAll(ViewController.class).iterator().next().getBoundsManager();
        Lookup look = getLookup();
        this.analysisHandler = handler;
        this.usedTool = usedTool;

        tm = new DefaultTableModel();
        cbm = new DefaultComboBoxModel<>();

        initComponents();
        setName(Bundle.CTL_DiffExpResultViewerTopComponent());
        setToolTipText(Bundle.HINT_DiffExpResultViewerTopComponent());
        DefaultListSelectionModel model = (DefaultListSelectionModel) topCountsTable.getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showPosition();
            }
        });
    }

    private void showPosition() {
//        DefaultListSelectionModel model = (DefaultListSelectionModel) topCountsTable.getSelectionModel();
//        int selectedView = model.getLeadSelectionIndex();
//        int selectedModel = topCountsTable.convertRowIndexToModel(selectedView);
//        int pos = 0;
//        //TODO
//        switch (usedTool) {
//            case DeSeq:
////                pos = (int) topCountsTable.getModel().getValueAt(selectedModel, 2);
//                break;
//            case BaySeq:
//                pos = (int) topCountsTable.getModel().getValueAt(selectedModel, 2);
//                break;
//            case SimpleTest:
////                pos = (int) topCountsTable.getModel().getValueAt(selectedModel, 2);
//                String locus = (String) topCountsTable.getModel().getValueAt(selectedModel, 0);
//                break;
//        }
//
//        Collection<ViewController> viewControllers;
//        viewControllers = (Collection<ViewController>) getLookup().lookupAll(ViewController.class);
//        for (Iterator<ViewController> it = viewControllers.iterator(); it.hasNext();) {
//            ViewController tmpVCon = it.next();
//            tmpVCon.getBoundsManager().navigatorBarUpdated(pos);
//
//        }
    }

    private void addResults() {
        List<DeAnalysisHandler.Result> results = analysisHandler.getResults();
        List<String> descriptions = new ArrayList<>();
        for (Iterator<DeAnalysisHandler.Result> it = results.iterator(); it.hasNext();) {
            DeAnalysisHandler.Result currentResult = it.next();
            Vector colNames = new Vector(currentResult.getColnames());
            colNames.add(0, " ");
            TableModel tmpTableModel = new DefaultTableModel(currentResult.getTableContentsContainingRowNames(), colNames);
            descriptions.add(currentResult.getDescription());
            tableModels.add(tmpTableModel);
        }

        resultComboBox.setModel(new DefaultComboBoxModel<>(descriptions.toArray()));
        topCountsTable.setModel(tableModels.get(0));

        createGraphicsButton.setEnabled(true);
        saveTableButton.setEnabled(true);
        showLogButton.setEnabled(true);
        resultComboBox.setEnabled(true);
        topCountsTable.setEnabled(true);
        jLabel1.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        resultComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        topCountsTable = new javax.swing.JTable();
        createGraphicsButton = new javax.swing.JButton();
        saveTableButton = new javax.swing.JButton();
        showLogButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DiffExpResultViewerTopComponent.class, "DiffExpResultViewerTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setEnabled(false);

        resultComboBox.setModel(cbm);
        resultComboBox.setEnabled(false);
        resultComboBox.addItemListener(this);

        topCountsTable.setAutoCreateRowSorter(true);
        topCountsTable.setModel(tm);
        topCountsTable.setEnabled(false);
        jScrollPane1.setViewportView(topCountsTable);

        org.openide.awt.Mnemonics.setLocalizedText(createGraphicsButton, org.openide.util.NbBundle.getMessage(DiffExpResultViewerTopComponent.class, "DiffExpResultViewerTopComponent.createGraphicsButton.text")); // NOI18N
        createGraphicsButton.setEnabled(false);
        createGraphicsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createGraphicsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveTableButton, org.openide.util.NbBundle.getMessage(DiffExpResultViewerTopComponent.class, "DiffExpResultViewerTopComponent.saveTableButton.text")); // NOI18N
        saveTableButton.setEnabled(false);
        saveTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTableButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(showLogButton, org.openide.util.NbBundle.getMessage(DiffExpResultViewerTopComponent.class, "DiffExpResultViewerTopComponent.showLogButton.text")); // NOI18N
        showLogButton.setEnabled(false);
        showLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLogButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resultComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(saveTableButton)
                        .addGap(18, 18, 18)
                        .addComponent(createGraphicsButton)
                        .addGap(18, 18, 18)
                        .addComponent(showLogButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(resultComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createGraphicsButton)
                    .addComponent(saveTableButton)
                    .addComponent(showLogButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createGraphicsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createGraphicsButtonActionPerformed
        switch (usedTool) {
            case DeSeq:
                GraficsTopComponent = new DeSeqGraficsTopComponent(analysisHandler,
                        ((DeSeqAnalysisHandler) analysisHandler).moreThanTwoCondsForDeSeq(), usedTool);
                analysisHandler.registerObserver((DeSeqGraficsTopComponent) GraficsTopComponent);
                GraficsTopComponent.open();
                GraficsTopComponent.requestActive();
                break;
            case BaySeq:
                GraficsTopComponent = new DiffExpGraficsTopComponent(analysisHandler);
                analysisHandler.registerObserver((DiffExpGraficsTopComponent) GraficsTopComponent);
                GraficsTopComponent.open();
                GraficsTopComponent.requestActive();
                break;
            case SimpleTest:
                GraficsTopComponent = new DeSeqGraficsTopComponent(analysisHandler, usedTool);
                analysisHandler.registerObserver((DeSeqGraficsTopComponent) GraficsTopComponent);
                GraficsTopComponent.open();
                GraficsTopComponent.requestActive();
                break;
        }
    }//GEN-LAST:event_createGraphicsButtonActionPerformed

    private void saveTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTableButtonActionPerformed
        VampFileChooser fc = new VampFileChooser(new String[]{"csv"}, "csv") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                analysisHandler.saveResultsAsCSV(resultComboBox.getSelectedIndex(), fileLocation);
            }

            @Override
            public void open(String fileLocation) {
            }
        };
        fc.openFileChooser(VampFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_saveTableButtonActionPerformed

    private void showLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLogButtonActionPerformed
        LogTopComponent = new DiffExpLogTopComponent(analysisHandler);
        analysisHandler.registerObserver((DiffExpLogTopComponent) LogTopComponent);
        LogTopComponent.open();
        LogTopComponent.requestActive();
    }//GEN-LAST:event_showLogButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createGraphicsButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<Object> resultComboBox;
    private javax.swing.JButton saveTableButton;
    private javax.swing.JButton showLogButton;
    private javax.swing.JTable topCountsTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        analysisHandler.removeObserver(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void update(Object args) {
        try {
            final AnalysisStatus status = (AnalysisStatus) args;
            final DiffExpResultViewerTopComponent cmp = this;
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    switch (status) {
                        case RUNNING:
                            progressHandle.start();
                            progressHandle.switchToIndeterminate();
                            break;
                        case FINISHED:
                            addResults();
                            progressHandle.switchToDeterminate(100);
                            progressHandle.finish();
                            break;
                        case ERROR:
                            progressHandle.switchToDeterminate(0);
                            progressHandle.finish();
                            cmp.close();
                            break;
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            //Exception will occure when the TopComponent Window is closed but
            //this is not a problem.
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED) {
            topCountsTable.setModel(tableModels.get(resultComboBox.getSelectedIndex()));
        }
    }
}
