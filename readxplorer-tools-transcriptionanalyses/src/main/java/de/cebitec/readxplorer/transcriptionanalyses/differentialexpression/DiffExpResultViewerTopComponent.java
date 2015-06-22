/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.exporter.tables.ListTableToExcel;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.AnalysisStatus;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot.BaySeqGraphicsTopComponent;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot.DeSeq2GraphicsTopComponent;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot.DeSeqGraphicsTopComponent;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot.ExpressTestGraphicsTopComponent;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.ui.tablevisualization.tablefilter.TableRightClickFilterList;
import de.cebitec.readxplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.readxplorer.utils.GenerateRowSorterList;
import de.cebitec.readxplorer.utils.ListTableModel;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.UneditableListTableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.AnalysisStatus.ERROR;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.AnalysisStatus.FINISHED;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.AnalysisStatus.RUNNING;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.BaySeq;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.DeSeq;
import static de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool.ExpressTest;


/**
 * Top component which displays the results of differential expression analysis.
 */
@ConvertAsProperties( dtd = "-//de.cebitec.readxplorer.transcriptionanalyses.differentialexpression//DiffExpResultViewer//EN",
                      autostore = false )
@TopComponent.Description( preferredID = "DiffExpResultViewerTopComponent",
                           //iconBase="SET/PATH/TO/ICON/HERE",
                           persistenceType = TopComponent.PERSISTENCE_NEVER )
@TopComponent.Registration( mode = "bottomSlidingSide", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DiffExpResultViewerTopComponent" )
@ActionReference( path = "Menu/Window" /*
 * , position = 333
 */ )
@TopComponent.OpenActionRegistration( displayName = "#CTL_DiffExpResultViewerAction",
                                      preferredID = "DiffExpResultViewerTopComponent" )
@Messages( {
    "CTL_DiffExpResultViewerAction=DiffExpResultViewer",
    "# {0} - tool",
    "CTL_DiffExpResultViewerTopComponent={0} Differential Gene Expression Results",
    "HINT_DiffExpResultViewerTopComponent=This is a Differential Gene Expression Result Window"
} )
public final class DiffExpResultViewerTopComponent extends TopComponentExtended
        implements Observer, ItemListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger( DiffExpResultViewerTopComponent.class.getName() );
    private static final int POS_IDX = 0;
    private static final int TRACK_IDX = 2;
    private static final int CHROM_IDX = 1;
    private TableModel tm;
    private ComboBoxModel<Object> cbm;
    private final List<ListTableModel> tableModels = new ArrayList<>();
    private TopComponent graphicsTopComponent;
    private ExpressTestGraphicsTopComponent ptc;
    private TopComponent logTopComponent;
    private DeAnalysisHandler analysisHandler;
    private DeAnalysisHandler.Tool usedTool;
    private final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Differential Gene Expression Analysis" );
    private final TableRightClickFilterList<UneditableListTableModel> rktm = new TableRightClickFilterList<>( UneditableListTableModel.class, POS_IDX, TRACK_IDX );
    private ReferenceFeatureTopComp refComp;


    public DiffExpResultViewerTopComponent() {
    }


    public DiffExpResultViewerTopComponent( DeAnalysisHandler handler, DeAnalysisHandler.Tool usedTool ) {
        refComp = ReferenceFeatureTopComp.findInstance();
        this.analysisHandler = handler;
        this.usedTool = usedTool;

        tm = new UneditableListTableModel();
        cbm = new DefaultComboBoxModel<>();

        initComponents();
        setName( Bundle.CTL_DiffExpResultViewerTopComponent( usedTool ) );
        setToolTipText( Bundle.HINT_DiffExpResultViewerTopComponent() );
        topCountsTable.getTableHeader().addMouseListener( rktm );
        topCountsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                showPosition();
            }


        } );
    }


    /**
     * Updates the position in all available bounds info managers to the
     * reference position of the currently selected genomic feature.
     */
    private void showPosition() {
        Collection<? extends ViewController> viewControllers = CentralLookup.getDefault().lookupAll( ViewController.class );
        for( ViewController tmpVCon : viewControllers ) {
            BoundsInfoManager bm = tmpVCon.getBoundsManager();
            if( bm != null && analysisHandler.getRefGenomeID() == tmpVCon.getCurrentRefGen().getId() ) {

                TableUtils.showPosition( topCountsTable, POS_IDX, CHROM_IDX, bm );
            }
        }
        refComp.showTableFeature( topCountsTable, 0 );
    }


    /**
     * Adds the results of a finished differential gene expression analysis to
     * the table of this top component.
     */
    private void addResults() {
        if( analysisHandler.getResults() != null ) {
            List<ResultDeAnalysis> results = analysisHandler.getResults();
            List<String> descriptions = new ArrayList<>( results.size() );
            for( final ResultDeAnalysis currentResult : results ) {
                List<Object> colNames = currentResult.getColnames();
                List<List<Object>> tableContents;
                switch( usedTool ) {
                    case ExportCountTable:
                    //fallthrough, since handling is same as for DESeq2
                    case DeSeq2:
                        colNames.add( 0, "Feature" );
                        tableContents = currentResult.getTableContentsContainingRowNames();
                        break;
                    default:
                        colNames.remove( 0 );
                        colNames.add( 0, "Feature" );
                        tableContents = currentResult.getTableContents();
                }

                ListTableModel tmpTableModel = new UneditableListTableModel( tableContents, colNames );
                descriptions.add( currentResult.getDescription() );
                tableModels.add( tmpTableModel );
            }

            resultComboBox.setModel( new DefaultComboBoxModel<>( descriptions.toArray() ) );
            ListTableModel dtm = tableModels.get( 0 );
            topCountsTable.setModel( dtm );
            TableRowSorter<ListTableModel> trs = GenerateRowSorterList.createRowSorter( dtm );
            topCountsTable.setRowSorter( trs );
            if( usedTool == ExpressTest ) {
                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                sortKeys.add( new RowSorter.SortKey( 7, SortOrder.DESCENDING ) );
                trs.setSortKeys( sortKeys );
                trs.sort();
            }
            if( usedTool != DeAnalysisHandler.Tool.ExportCountTable ) {
                createGraphicsButton.setEnabled( true );
                showLogButton.setEnabled( true );
            }
            saveTableButton.setEnabled( true );
            resultComboBox.setEnabled( true );
            topCountsTable.setEnabled( true );
            jLabel1.setEnabled( true );
        }
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
                        .addComponent(resultComboBox, 0, 253, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(saveTableButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createGraphicsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(showLogButton))
                    .addComponent(jScrollPane1))
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
        switch( usedTool ) {
            case DeSeq:
                graphicsTopComponent = new DeSeqGraphicsTopComponent( analysisHandler,
                                                                      ((DeSeqAnalysisHandler) analysisHandler).moreThanTwoCondsForDeSeq() );
                analysisHandler.registerObserver( (Observer) graphicsTopComponent );
                graphicsTopComponent.open();
                graphicsTopComponent.requestActive();
                break;
            case BaySeq:
                graphicsTopComponent = new BaySeqGraphicsTopComponent( analysisHandler );
                analysisHandler.registerObserver( (Observer) graphicsTopComponent );
                graphicsTopComponent.open();
                graphicsTopComponent.requestActive();
                break;
            case ExpressTest:
                ptc = new ExpressTestGraphicsTopComponent( analysisHandler, usedTool );
                analysisHandler.registerObserver( ptc );
                ptc.open();
                ptc.requestActive();
                break;
            case DeSeq2:
                graphicsTopComponent = new DeSeq2GraphicsTopComponent( analysisHandler );
                analysisHandler.registerObserver( (Observer) graphicsTopComponent );
                graphicsTopComponent.open();
                graphicsTopComponent.requestActive();
                break;
            default:
                LOG.severe( "Encountered unknown differential gene expression tool" );
        }
    }//GEN-LAST:event_createGraphicsButtonActionPerformed

    private void saveTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTableButtonActionPerformed
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(),
                                                                         new ListTableToExcel( resultComboBox.getSelectedItem().toString(), (ListTableModel) topCountsTable.getModel() ) );
    }//GEN-LAST:event_saveTableButtonActionPerformed

    private void showLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLogButtonActionPerformed
        logTopComponent = new DiffExpLogTopComponent( analysisHandler );
        analysisHandler.registerObserver( (Observer) logTopComponent );
        logTopComponent.open();
        logTopComponent.requestActive();
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
    }


    @Override
    public void componentClosed() {
        analysisHandler.removeObserver( this );
    }


    void writeProperties( java.util.Properties p ) {
        p.setProperty( "version", "1.0" );
    }


    void readProperties( java.util.Properties p ) {
    }


    @Override
    public void update( Object args ) {
        final AnalysisStatus status = (AnalysisStatus) args;
        final DiffExpResultViewerTopComponent cmp = this;
        //Might be called from outside of the EDT, so using swing utils
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                switch( status ) {
                    case RUNNING:
                        progressHandle.start();
                        progressHandle.switchToIndeterminate();
                        break;
                    case FINISHED:
                        addResults();
                        progressHandle.switchToDeterminate( 100 );
                        progressHandle.finish();
                        break;
                    case ERROR:
                        progressHandle.switchToDeterminate( 0 );
                        progressHandle.finish();
                        logTopComponent = new DiffExpLogTopComponent();
                        logTopComponent.open();
                        logTopComponent.requestActive();
                        cmp.close();
                        break;
                    default:
                        LOG.info( "Encountered unknown analysis status" );
                }
            }


        } );
    }


    @Override
    public void itemStateChanged( ItemEvent e ) {
        int state = e.getStateChange();
        if( state == ItemEvent.SELECTED ) {
            rktm.resetOriginalTableModel();
            ListTableModel dtm = tableModels.get( resultComboBox.getSelectedIndex() );
            topCountsTable.setModel( dtm );
            TableRowSorter<ListTableModel> trs = GenerateRowSorterList.createRowSorter( dtm );
            topCountsTable.setRowSorter( trs );
            if( usedTool == ExpressTest ) {
                trs.setSortKeys( Collections.singletonList( new RowSorter.SortKey( 8, SortOrder.DESCENDING ) ) );
                trs.sort();
            }
        }
    }


}
