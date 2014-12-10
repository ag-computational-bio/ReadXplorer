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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import de.cebitec.readxplorer.utils.fileChooser.StoreStringFileChooser;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.ui.tablevisualization.tablefilter.TableRightClickFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * CoverageIntervalContainer panel for the coverage analysis. It displays the
 * table with
 * all covered or uncovered intervals of the reference.
 * <p>
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ResultPanelCoverageAnalysis extends javax.swing.JPanel {

    public static final String NUMBER_INTERVALS = "Total number of detected intervals";
    public static final String MEAN_INTERVAL_LENGTH = "Mean interval length";
    public static final String MEAN_INTERVAL_COVERAGE = "Global mean interval coverage";
    private static final long serialVersionUID = 1L;
    private BoundsInfoManager bim;
    private CoverageAnalysisResult coverageAnalysisResult;
    private final Map<String, Integer> coverageStatisticsMap;
    private final TableRightClickFilter<UneditableTableModel> tableFilter;


    /**
     * CoverageIntervalContainer panel for the coverage analysis. It displays
     * the table
     * with all covered or uncovered intervals of the reference.
     */
    public ResultPanelCoverageAnalysis() {
        initComponents();
        final int posColumnIdx = 0;
        final int trackColumnIdx = 2;
        final int chromColumnIdx = 3;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumnIdx, trackColumnIdx );
        this.coverageAnalysisTable.getTableHeader().addMouseListener( tableFilter );
        this.coverageStatisticsMap = new HashMap<>();
        this.coverageStatisticsMap.put( NUMBER_INTERVALS, 0 );
        this.coverageStatisticsMap.put( MEAN_INTERVAL_LENGTH, 0 );
        this.coverageStatisticsMap.put( MEAN_INTERVAL_COVERAGE, 0 );

        DefaultListSelectionModel model = (DefaultListSelectionModel) this.coverageAnalysisTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( coverageAnalysisTable, posColumnIdx, chromColumnIdx, bim );
            }


        } );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coverageAnalysisPane = new javax.swing.JScrollPane();
        coverageAnalysisTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        statisticsButton = new javax.swing.JButton();
        parametersLabel = new javax.swing.JLabel();
        exportSeqButton = new javax.swing.JButton();

        coverageAnalysisTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Start", "Stop", "Track", "Chromosome", "Strand", "Length", "Mean Coverage"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        coverageAnalysisPane.setViewportView(coverageAnalysisTable);
        if (coverageAnalysisTable.getColumnModel().getColumnCount() > 0) {
            coverageAnalysisTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title2")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title3")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title0")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title6")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title1")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title4")); // NOI18N
            coverageAnalysisTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.coverageAnalysisTable.columnModel.title5")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.exportButton.text_1")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.statisticsButton.text_1")); // NOI18N
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(parametersLabel, org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.parametersLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exportSeqButton, org.openide.util.NbBundle.getMessage(ResultPanelCoverageAnalysis.class, "ResultPanelCoverageAnalysis.exportSeqButton.text_1")); // NOI18N
        exportSeqButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSeqButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(coverageAnalysisPane, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parametersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(exportSeqButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(coverageAnalysisPane, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(statisticsButton)
                    .addComponent(parametersLabel)
                    .addComponent(exportSeqButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), this.coverageAnalysisResult );
    }//GEN-LAST:event_exportButtonActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new CoverageAnalysisStatsPanel( coverageStatisticsMap ), "Coverage Analysis Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed

    private void exportSeqButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSeqButtonActionPerformed
        this.exportSeqAsMultipleFasta();
    }//GEN-LAST:event_exportSeqButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane coverageAnalysisPane;
    private javax.swing.JTable coverageAnalysisTable;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton exportSeqButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    /**
     * @param boundsInformationManager The bounds info manager to update, when
     *                                 a result is clicked.
     */
    public void setBoundsInfoManager( BoundsInfoManager boundsInformationManager ) {
        this.bim = boundsInformationManager;
    }


    /**
     * Adds a list of covered or uncovered intervals to this panel.
     * <p>
     * @param coverageAnalysisResultNew the new result of intervals to add
     */
    public void addCoverageAnalysis( final CoverageAnalysisResult coverageAnalysisResultNew ) {

        tableFilter.setTrackMap( coverageAnalysisResultNew.getTrackMap() );

        if( this.coverageAnalysisResult == null ) {
            this.coverageAnalysisResult = coverageAnalysisResultNew;
        }
        else {
            this.coverageAnalysisResult.getResults().getCoverageIntervals().addAll( coverageAnalysisResultNew.getResults().getCoverageIntervals() );
            this.coverageAnalysisResult.getResults().getCoverageIntervalsRev().addAll( coverageAnalysisResultNew.getResults().getCoverageIntervalsRev() );
        }

        this.createTableEntries( coverageAnalysisResult.getResults().getCoverageIntervals() );
        this.createTableEntries( coverageAnalysisResult.getResults().getCoverageIntervalsRev() );
    }


    /**
     * Prepares the results stored in this panel for output in the gui.
     * <p>
     * @param intervalList list to create the entries for
     */
    private void createTableEntries( List<CoverageInterval> intervalList ) {
        final int nbColumns = 7;

        DefaultTableModel model = (DefaultTableModel) coverageAnalysisTable.getModel();
        int meanIntervalLength = 0;
        int meanIntervalCoverage = 0;

        for( CoverageInterval interval : intervalList ) {
            Object[] rowData = new Object[nbColumns];
            int i = 0;
            rowData[i++] = interval.isFwdStrand() ? interval.getStart() : interval.getStop();
            rowData[i++] = interval.isFwdStrand() ? interval.getStop() : interval.getStart();
            rowData[i++] = coverageAnalysisResult.getTrackEntry( interval.getTrackId(), false );
            rowData[i++] = coverageAnalysisResult.getChromosomeMap().get( interval.getChromId() );
            rowData[i++] = interval.getStrandString();
            rowData[i++] = interval.getLength();
            rowData[i++] = interval.getMeanCoverage();
            meanIntervalLength += interval.getLength();
            meanIntervalCoverage += interval.getMeanCoverage();

            model.addRow( rowData );
        }

        if( intervalList.size() > 0 ) {
            meanIntervalLength /= intervalList.size();
            meanIntervalLength = coverageStatisticsMap.get( MEAN_INTERVAL_LENGTH ) > 0 ? meanIntervalLength / 2 : meanIntervalLength;
            meanIntervalCoverage /= intervalList.size();
            meanIntervalCoverage = coverageStatisticsMap.get( MEAN_INTERVAL_COVERAGE ) > 0 ? meanIntervalCoverage / 2 : meanIntervalCoverage;

            coverageStatisticsMap.put( NUMBER_INTERVALS, coverageStatisticsMap.get( NUMBER_INTERVALS ) + intervalList.size() );
            coverageStatisticsMap.put( MEAN_INTERVAL_LENGTH, meanIntervalLength );
            coverageStatisticsMap.put( MEAN_INTERVAL_COVERAGE, meanIntervalCoverage );
        }
        coverageAnalysisResult.setStatsMap( coverageStatisticsMap );

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(); //set a sorter for the table
        coverageAnalysisTable.setRowSorter( sorter );
        sorter.setModel( model );

        ParameterSetCoverageAnalysis parameters = ((ParameterSetCoverageAnalysis) coverageAnalysisResult.getParameters());
        String coverageCount = parameters.isSumCoverageOfBothStrands() ? "both strands" : "each strand separately";
        String uncoveredIntervals = parameters.isDetectCoveredIntervals() ? "no" : "yes";
        parametersLabel.setText( org.openide.util.NbBundle.getMessage( ResultPanelCoverageAnalysis.class,
                                                                       "ResultPanelCoverageAnalysis.parametersLabel.text",
                                                                       parameters.getMinCoverageCount(), coverageCount, uncoveredIntervals ) );
    }


    /**
     * @return the number of covered or uncovered intervals during the
     *         associated analysis.
     */
    public int getResultSize() {
        return coverageStatisticsMap.get( NUMBER_INTERVALS );
    }


    /**
     * Retrieves the reference sequence of each interval in the result shown
     * in this panel. A header describing the reference, chromosome and
     * position is created and the reference sequence is appended in fasta
     * format. Eventually, a StringFileChooser enables storing a file containing
     * all interval sequences in mutliple fasta format.
     */
    private void exportSeqAsMultipleFasta() {
        StringBuilder results = new StringBuilder( 100 );
        List<CoverageInterval> coverageIntervals = coverageAnalysisResult.getResults().getCoverageIntervals();
        coverageIntervals.addAll( coverageAnalysisResult.getResults().getCoverageIntervalsRev() );
        PersistentReference reference = coverageAnalysisResult.getReference();
        int start;
        int stop;
        String seq;
        for( CoverageInterval coverageInterval : coverageIntervals ) {
            int chromId = coverageInterval.getChromId();
            if( coverageInterval.isFwdStrand() ) {
                start = coverageInterval.getStart();
                stop = coverageInterval.getStop();
                seq = reference.getChromSequence( chromId, start, stop );
            }
            else {
                start = coverageInterval.getStop();
                stop = coverageInterval.getStart();
                seq = SequenceUtils.getReverseComplement( reference.getChromSequence( chromId, stop, start ) );
            }
            results.append( ">" ).
                    append( reference.getName().replaceAll( " ", "_" ) ).
                    append( "-" ).
                    append( reference.getChromosome( chromId ).getName() ).
                    append( "-" ).
                    append( coverageInterval.getStrandString() ).
                    append( "-bases_" ).
                    append( start ).
                    append( "-" ).
                    append( stop ).
                    append( "\n" ).
                    append( seq ).
                    append( "\n" );
        }
        new StoreStringFileChooser( new String[]{ "fasta" }, "fasta", results.toString() );
    }


}
