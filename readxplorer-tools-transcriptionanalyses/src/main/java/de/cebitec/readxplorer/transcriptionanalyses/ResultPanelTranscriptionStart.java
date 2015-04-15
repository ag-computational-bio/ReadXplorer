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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.DetectedFeatures;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TransStartUnannotated;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.ui.analysis.ResultTablePanel;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.tablevisualization.TableComparatorProvider;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.ui.tablevisualization.tablefilter.TableRightClickFilter;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.NbBundle;


/**
 * This panel is capable of showing a table with transcription start sites and
 * contains an export button, which exports the data into an excel file.
 * <p>
 * @author -Rolf Hilker-
 */
public class ResultPanelTranscriptionStart extends ResultTablePanel {

    private static final long serialVersionUID = 1L;

    private List<String> promoterRegions;
    private ReferenceViewer referenceViewer;
    private TssDetectionResult tssResult;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;


    /**
     * This panel is capable of showing a table with transcription start sites
     * and contains an export button, which exports the data into an excel file.
     * <p>
     * @param referenceViewer The reference viewer belonging to this analysis
     *                        and needed for updating the currently shown
     *                        position and extracting the reference sequence.
     */
    public ResultPanelTranscriptionStart( ReferenceViewer referenceViewer ) {
        setBoundsInfoManager( referenceViewer.getBoundsInformationManager() );
        this.referenceViewer = referenceViewer;
        this.initComponents();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 1;
        final int chroColumnIdx = 2;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.tSSTable.getTableHeader().addMouseListener( tableFilter );

        TableUtils.addTableListSelectionListener( tSSTable, posColumnIdx, chroColumnIdx, getBoundsInfoManager() );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tssScrollPane = new javax.swing.JScrollPane();
        tSSTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        parametersLabel = new javax.swing.JLabel();
        statisticsButton = new javax.swing.JButton();

        tSSTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Track", "Chromosome", "Strand", "Initial Coverage", "Coverage Increase", "Coverage Increase %", "Correct Feature", "Next Upstream Feature", "Dist. Upstream Feature", "Next Downstream Feature", "Dist. Downstream Feature", "Novel Transcript", "Cov. Transcript Stop", "Start Codon Pos", "Leader Length", "Stop Codon Pos", "Codon CDS Length", "TSS Type", "Primary TSS", "Associated TSS"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tssScrollPane.setViewportView(tSSTable);
        if (tSSTable.getColumnModel().getColumnCount() > 0) {
            tSSTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title0_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_2")); // NOI18N
            tSSTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title11_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title1_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title2_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title4_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title5_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title6_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title7_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title12")); // NOI18N
            tSSTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title8_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title13")); // NOI18N
            tSSTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title9_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title10_1")); // NOI18N
            tSSTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title17")); // NOI18N
            tSSTable.getColumnModel().getColumn(15).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title18")); // NOI18N
            tSSTable.getColumnModel().getColumn(16).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title19")); // NOI18N
            tSSTable.getColumnModel().getColumn(17).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title20")); // NOI18N
            tSSTable.getColumnModel().getColumn(18).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title14")); // NOI18N
            tSSTable.getColumnModel().getColumn(19).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title15")); // NOI18N
            tSSTable.getColumnModel().getColumn(20).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.tSSTable.columnModel.title16")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.exportButton.text_1")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.parametersLabel.text_1")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelTranscriptionStart.class, "ResultPanelTranscriptionStart.statisticsButton.text_1")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(parametersLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
            .addComponent(tssScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tssScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(parametersLabel)
                    .addComponent(statisticsButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        this.processResultForExport();

        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), tssResult );
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new TssDetectionStatsPanel(
                                       tssResult.getStatsMap(),
                                       (ParameterSetTSS) tssResult.getParameters() ),
                                       "TSS Detection Statistics",
                                       JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    private javax.swing.JTable tSSTable;
    private javax.swing.JScrollPane tssScrollPane;
    // End of variables declaration//GEN-END:variables


    /**
     * Adds a list of transcription start site objects to this panel's table.
     * <p>
     * @param newResult transcription start sites detection result to add
     */
    @Override
    @NbBundle.Messages( { "# {0} - min no read starts",
                          "# {1} - min coverage increase percent",
                          "# {2} - max leaderless dist",
                          "# {3} - init. low cov. read start max",
                          "# {4} - min low cov. read starts",
                          "# {5} - detect novel transcripts",
                          "# {6} - transcript extension cov",
                          "ResultPanelTranscriptionStart.parametersLabel.text_1=Parameters: min no read starts:  {0},  min coverage increase percent: {1}%,  max leaderless dist.: {2},  init. low cov. read start max:  {3},  min low cov. read starts: {4},  detect novel transcripts: {5},  transcript extension cov.: {6}" } )
    public void addResult( final ResultTrackAnalysis newResult ) {

        tableFilter.setTrackMap( newResult.getTrackMap() );

        if( newResult instanceof TssDetectionResult ) {
            final TssDetectionResult tssResultNew = (TssDetectionResult) newResult;
            final List<TranscriptionStart> tsss = tssResultNew.getResults();

            if( tssResult == null ) {
                tssResult = tssResultNew;
            } else {
                tssResult.addTss( tsss );
                tssResult.updateStatsMap( tssResultNew.getStatsMap() );
            }
            final ParameterSetTSS tssParameters = (ParameterSetTSS) tssResult.getParameters();

            SwingUtilities.invokeLater( new Runnable() {
                //because it is not called from the swing dispatch thread
                @Override
                public void run() {

                    if( !tssParameters.isAssociateTss() ) {
                        tSSTable.removeColumn( tSSTable.getColumn( "Associated TSS" ) );
                    }

                    final int nbColumns = 21;

                    DefaultTableModel model = (DefaultTableModel) tSSTable.getModel();
                    for( TranscriptionStart tss : tsss ) {

                        final String strand;
                        if( tss.isFwdStrand() ) {
                            strand = Strand.Forward.toString();
                        } else {
                            strand = Strand.Reverse.toString();
                        }

                        final Object[] rowData = new Object[nbColumns];
                        int i = 0;
                        rowData[i++] = tss.getPos();
                        rowData[i++] = tssResult.getTrackEntry( tss.getTrackId(), false );
                        rowData[i++] = tssResult.getChromosomeMap().get( tss.getChromId() );
                        rowData[i++] = strand;
                        rowData[i++] = tss.getReadStartsAtPos();
                        rowData[i++] = tss.getCoverageIncrease();
                        rowData[i++] = tss.getPercentIncrease();

                        DetectedFeatures detFeatures = tss.getDetFeatures();
                        PersistentFeature feature = detFeatures.getCorrectStartFeature();
                        if( feature != null ) {
                            rowData[i++] = feature.toString();
                        } else {
                            rowData[i++] = "-";
                        }
                        feature = detFeatures.getUpstreamFeature();
                        if( feature != null ) {
                            rowData[i++] = feature.toString();
                            rowData[i++] = Math.abs( tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()) );
                        } else {
                            rowData[i++] = "-";
                            rowData[i++] = null;
                        }
                        feature = detFeatures.getDownstreamFeature();
                        if( feature != null ) {
                            rowData[i++] = feature.toString();
                            rowData[i++] = Math.abs( tss.getPos() - (tss.isFwdStrand() ? feature.getStart() : feature.getStop()) );
                        } else {
                            rowData[i++] = "-";
                            rowData[i++] = null;
                        }

                        if( tss instanceof TransStartUnannotated ) {
                            TransStartUnannotated tSSU = (TransStartUnannotated) tss;
                            rowData[i++] = true;
                            rowData[i++] = tSSU.getDetectedStop();
                            if( tSSU.hasStartCodon() ) {
                                rowData[i++] = tSSU.getStartCodon().getStartOnStrand();
                                rowData[i++] = tSSU.getStartPosDifference();
                                if( tSSU.hasStopCodon() ) {
                                    rowData[i++] = tSSU.getStopCodon().getStartOnStrand();
                                    rowData[i++] = tSSU.getCodonCDSLength();
                                } else {
                                    rowData[i++] = null;
                                }
                            } else {
                                i += 4;
                            }
                        } else {
                            i += 6;
                        }

                        rowData[i++] = tss.isPrimaryTss() ? "primary" : "secondary";
                        if( !tss.isPrimaryTss() ) {
                            rowData[i++] = tss.getPrimaryTss().getPos();
                        } else {
                            i++;
                        }

                        rowData[i++] = GeneralUtils.implode( ",", tss.getAssociatedTssList().toArray() );

                        model.addRow( rowData );
                    }

                    TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                    tSSTable.setRowSorter( sorter );
                    sorter.setModel( model );
                    TableComparatorProvider.setPersistentTrackComparator( sorter, 1 );

                    String unannotatedTranscriptDet = tssParameters.isPerformUnannotatedTranscriptDet() ? "yes" : "no";
                    parametersLabel.setText( Bundle.ResultPanelTranscriptionStart_parametersLabel_text_1( tssParameters.getMinNoReadStarts(), tssParameters.getMinPercentIncrease(),
                                                                                                          tssParameters.getMaxLeaderlessDistance(), tssParameters.getMaxLowCovReadStarts(), tssParameters.getMinLowCovReadStarts(),
                                                                                                          unannotatedTranscriptDet, tssParameters.getMinTranscriptExtensionCov() ) );
                }


            } );
        }
    }


    /**
     * @return The number of detected TSS
     */
    @Override
    public int getDataSize() {
        return this.tssResult.getResults().size();
    }


    /**
     * Prepares the result for output. Any special operations are carried out
     * here. In this case generating the promoter region for each TSS.
     */
    private void processResultForExport() {
        //Generating promoter regions for the TSS
        this.promoterRegions = new ArrayList<>();

        //get reference sequence for promoter regions
        PersistentReference ref = this.referenceViewer.getReference();

        //get the promoter region for each TSS
        int chromLength = ref.getActiveChromosome().getLength();
        for( TranscriptionStart tSS : this.tssResult.getResults() ) {
            final String promoter;
            if( tSS.isFwdStrand() ) {
                int promoterStart = tSS.getPos() - 70;
                promoterStart = promoterStart < 0 ? 0 : promoterStart;
                promoter = ref.getActiveChromSequence( promoterStart, tSS.getPos() );
            } else {
                int promoterStart = tSS.getPos() + 70;
                promoterStart = promoterStart > chromLength ? chromLength : promoterStart;
                promoter = SequenceUtils.getReverseComplement( ref.getActiveChromSequence( tSS.getPos(), promoterStart ) );
            }
            this.promoterRegions.add( promoter );
        }
        tssResult.setPromoterRegions( promoterRegions );
    }


}
