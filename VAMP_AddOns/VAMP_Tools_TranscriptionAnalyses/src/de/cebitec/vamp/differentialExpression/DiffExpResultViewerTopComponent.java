package de.cebitec.vamp.differentialExpression;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.differentialExpression.DeAnalysisHandler.AnalysisStatus;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.vamp.differentialExpression.DeAnalysisHandler.Tool.SimpleTest;
import de.cebitec.vamp.differentialExpression.plotting.PlotTopComponent;
import de.cebitec.vamp.exporter.excel.ExcelExportFileChooser;
import de.cebitec.vamp.exporter.excel.TableToExcel;
import de.cebitec.vamp.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.TableRightClickFilter;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
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
    private ArrayList<TableRowSorter> tableRowSorter = new ArrayList<>();
    private TopComponent GraficsTopComponent;
    private TopComponent LogTopComponent;
    private DeAnalysisHandler analysisHandler;
    private DeAnalysisHandler.Tool usedTool;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Differential Expression Analysis");
    private TableRightClickFilter<UnchangeableDefaultTableModel> rktm = new TableRightClickFilter<>(UnchangeableDefaultTableModel.class);
    private ReferenceFeatureTopComp refComp;

    public DiffExpResultViewerTopComponent() {
    }

    public DiffExpResultViewerTopComponent(DeAnalysisHandler handler, DeAnalysisHandler.Tool usedTool) {
        refComp = ReferenceFeatureTopComp.findInstance();
        this.analysisHandler = handler;
        this.usedTool = usedTool;

        tm = new UnchangeableDefaultTableModel();
        cbm = new DefaultComboBoxModel<>();

        initComponents();
        setName(Bundle.CTL_DiffExpResultViewerTopComponent());
        setToolTipText(Bundle.HINT_DiffExpResultViewerTopComponent());
        topCountsTable.getTableHeader().addMouseListener(rktm);
        topCountsTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    showPosition();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    private void showPosition() {
        PersistantFeature feature = getFeatureFromTable();
        if (feature != null) {
            int pos = feature.getStart();
            Collection<ViewController> viewControllers;
            viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll(ViewController.class);
            for (Iterator<ViewController> it = viewControllers.iterator(); it.hasNext();) {
                ViewController tmpVCon = it.next();
                BoundsInfoManager bm = tmpVCon.getBoundsManager();
                if (bm != null) {
                    bm.navigatorBarUpdated(pos);
                }
            }
            refComp.showFeatureDetails(feature);
        }
    }

    private PersistantFeature getFeatureFromTable() {
        PersistantFeature feature = null;
        DefaultListSelectionModel model = (DefaultListSelectionModel) topCountsTable.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();
        int selectedModel = topCountsTable.convertRowIndexToModel(selectedView);
        switch (usedTool) {
            case DeSeq:
                feature = (PersistantFeature) topCountsTable.getModel().getValueAt(selectedModel, 1);
                break;
            case BaySeq:
                feature = (PersistantFeature) topCountsTable.getModel().getValueAt(selectedModel, 1);
                break;
            case SimpleTest:
                feature = (PersistantFeature) topCountsTable.getModel().getValueAt(selectedModel, 0);
                break;
        }
        return feature;
    }

    private void addResults() {
        List<ResultDeAnalysis> results = analysisHandler.getResults();
        List<String> descriptions = new ArrayList<>();
        for (Iterator<ResultDeAnalysis> it = results.iterator(); it.hasNext();) {
            ResultDeAnalysis currentResult = it.next();
            Vector colNames = new Vector(currentResult.getColnames());
            colNames.add(0, " ");
            Vector<Vector> tableContents = currentResult.getTableContentsContainingRowNames();
            TableModel tmpTableModel = new UnchangeableDefaultTableModel(tableContents, colNames);
            descriptions.add(currentResult.getDescription());
            tableModels.add(tmpTableModel);
            TableRowSorter<TableModel> tmpRowSorter = new TableRowSorter<>(tmpTableModel);
            Vector firstRow = tableContents.get(0);
            int columnCounter = 0;
            for (Iterator it1 = firstRow.iterator(); it1.hasNext(); columnCounter++) {
                Object object = it1.next();
                if (object instanceof Double) {
                    tmpRowSorter.setComparator(columnCounter, new Comparator<Double>() {
                        @Override
                        public int compare(Double o1, Double o2) {
                            if (o1.doubleValue() == o2.doubleValue()) {
                                return 0;
                            }
                            if (o1.doubleValue() > o2.doubleValue()) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                }
                if (object instanceof Integer) {
                    tmpRowSorter.setComparator(columnCounter, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            if (o1.intValue()== o2.intValue()) {
                                return 0;
                            }
                            if (o1.intValue() > o2.intValue()) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                }

            }
            tableRowSorter.add(tmpRowSorter);
        }

        resultComboBox.setModel(new DefaultComboBoxModel<>(descriptions.toArray()));
        topCountsTable.setModel(tableModels.get(0));
        topCountsTable.setRowSorter(tableRowSorter.get(0));

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
        topCountsTable.setRowSorter(null);
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createGraphicsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
//        PlotTopComponent plotTop = new PlotTopComponent(analysisHandler);
//        analysisHandler.registerObserver(plotTop);
//        plotTop.open();
//        plotTop.requestActive();
    }//GEN-LAST:event_createGraphicsButtonActionPerformed

    private void saveTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTableButtonActionPerformed
        ExcelExportFileChooser fc = new ExcelExportFileChooser(new String[]{"xls"},
                "xls", new TableToExcel(resultComboBox.getSelectedItem().toString(), (UnchangeableDefaultTableModel) topCountsTable.getModel()));
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
                            LogTopComponent = new DiffExpLogTopComponent();
                            LogTopComponent.open();
                            LogTopComponent.requestActive();
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
            rktm.resetOriginalTableModel();
            topCountsTable.setModel(tableModels.get(resultComboBox.getSelectedIndex()));
            topCountsTable.setRowSorter(tableRowSorter.get(resultComboBox.getSelectedIndex()));
        }
    }

    public static class UnchangeableDefaultTableModel extends DefaultTableModel {

        public UnchangeableDefaultTableModel() {
            super();
        }

        public UnchangeableDefaultTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        public UnchangeableDefaultTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public UnchangeableDefaultTableModel(Vector data, Vector columnNames) {
            super(data, columnNames);
        }

        public UnchangeableDefaultTableModel(Vector columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public UnchangeableDefaultTableModel(int rowCount, int columnCount) {
            super(rowCount, columnCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
