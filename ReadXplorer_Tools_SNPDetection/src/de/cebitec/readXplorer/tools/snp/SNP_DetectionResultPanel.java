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

package de.cebitec.readXplorer.tools.snp;


import de.cebitec.common.sequencetools.geneticcode.AminoAcidProperties;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CodonSnp;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.databackend.dataObjects.SnpI;
import de.cebitec.readXplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readXplorer.util.LineWrapCellRenderer;
import de.cebitec.readXplorer.util.SequenceComparison;
import de.cebitec.readXplorer.util.UneditableTableModel;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import de.cebitec.readXplorer.view.analysis.ResultTablePanel;
import de.cebitec.readXplorer.view.tableVisualization.TableComparatorProvider;
import de.cebitec.readXplorer.view.tableVisualization.TableUtils;
import de.cebitec.readXplorer.view.tableVisualization.tableFilter.TableRightClickFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.NbPreferences;


/**
 * Panel showing a SNP detection result.
 * <p>
 * @author joern, rhilker
 */
public class SNP_DetectionResultPanel extends ResultTablePanel {

    public static final String SNPS_TOTAL = "Total number of SNPs";
    public static final String SNPS_INTERGENEIC = "Intergenic SNPs";
    public static final String SNPS_SYNONYMOUS = "Synonymous SNPs";
    public static final String SNPS_CHEMIC_NEUTRAL = "Chemically neutral SNPs";
    public static final String SNPS_CHEMIC_DIFF = "Chemically different SNPs";
    public static final String SNPS_STOPS = "Stop Mutations";
    public static final String SNPS_AA_INSERTIONS = "AA Insertions";
    public static final String SNPS_AA_DELETIONS = "AA Deletions";
    public static final String SNPS_SUBSTITUTIONS = "Substitutions";
    public static final String SNPS_INSERTIONS = "Insertions";
    public static final String SNPS_DELETIONS = "Deletions";

    private static final long serialVersionUID = 1L;
    private SnpDetectionResult completeSnpData;
    private Map<String, Integer> snpStatsMap;
    private PersistentReference reference;
    private TableRightClickFilter<UneditableTableModel> tableFilter;


    /**
     * Creates new form SNP_DetectionResultPanel
     */
    public SNP_DetectionResultPanel() {
        initComponents();
        final int posColumn = 0;
        final int trackColumn = 2;
        final int chromColumn = 3;
        tableFilter = new TableRightClickFilter<>( UneditableTableModel.class, posColumn, trackColumn );
        this.snpTable.getTableHeader().addMouseListener( tableFilter );

        //ensures number of lines will adapt to number of translations (features) for each snp
        LineWrapCellRenderer lineWrapRenderer = new LineWrapCellRenderer();
        for( int i = 15; i <= 20; i++ ) { //these columns might contain multiple entries
            this.snpTable.getColumnModel().getColumn( i ).setCellRenderer( lineWrapRenderer );
        }

        DefaultListSelectionModel model = (DefaultListSelectionModel) snpTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( snpTable, posColumn, chromColumn, getBoundsInfoManager() );
            }


        } );
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        snpTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        alignmentButton = new javax.swing.JButton();
        alignmentButton1 = new javax.swing.JButton();
        parametersLabel = new javax.swing.JLabel();
        statisticsButton = new javax.swing.JButton();

        snpTable.setAutoCreateRowSorter(true);
        snpTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pos", "Gap", "Track", "Chromosome", "Base", "Ref", "A", "C", "G", "T", "N", "_", "Ref Cov", "Freq", "Type", "AA Ref", "AA SNP", "Codon Ref", "Codon SNP", "Effect on AA", "Feature", "Av Base Qual", "Av Mapping Qual"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        snpTable.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(snpTable);
        snpTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (snpTable.getColumnModel().getColumnCount() > 0) {
            snpTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title0_1")); // NOI18N
            snpTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title17_1")); // NOI18N
            snpTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title2")); // NOI18N
            snpTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title20")); // NOI18N
            snpTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title1_1")); // NOI18N
            snpTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title5_1")); // NOI18N
            snpTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title6_1")); // NOI18N
            snpTable.getColumnModel().getColumn(7).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title7_1")); // NOI18N
            snpTable.getColumnModel().getColumn(8).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title8_1")); // NOI18N
            snpTable.getColumnModel().getColumn(9).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title9_1")); // NOI18N
            snpTable.getColumnModel().getColumn(10).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title10_1")); // NOI18N
            snpTable.getColumnModel().getColumn(11).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title11_1")); // NOI18N
            snpTable.getColumnModel().getColumn(12).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title3_1")); // NOI18N
            snpTable.getColumnModel().getColumn(13).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.jTable1.columnModel.title4_1")); // NOI18N
            snpTable.getColumnModel().getColumn(14).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title12_1")); // NOI18N
            snpTable.getColumnModel().getColumn(15).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title14_1")); // NOI18N
            snpTable.getColumnModel().getColumn(16).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title13_1")); // NOI18N
            snpTable.getColumnModel().getColumn(17).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title19_1")); // NOI18N
            snpTable.getColumnModel().getColumn(18).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title18_1")); // NOI18N
            snpTable.getColumnModel().getColumn(19).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title15_1")); // NOI18N
            snpTable.getColumnModel().getColumn(20).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title16_1")); // NOI18N
            snpTable.getColumnModel().getColumn(21).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title21")); // NOI18N
            snpTable.getColumnModel().getColumn(22).setHeaderValue(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.snpTable.columnModel.title22")); // NOI18N
        }

        exportButton.setText(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.exportButton.text")); // NOI18N
        exportButton.setActionCommand(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.exportButton.actionCommand")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        alignmentButton.setText(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.alignmentButton.text")); // NOI18N
        alignmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignmentButtonActionPerformed(evt);
            }
        });

        alignmentButton1.setText(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.alignmentButton1.text")); // NOI18N
        alignmentButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignmentButton1ActionPerformed(evt);
            }
        });

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.parametersLabel.text")); // NOI18N

        statisticsButton.setText(org.openide.util.NbBundle.getMessage(SNP_DetectionResultPanel.class, "SNP_DetectionResultPanel.statisticsButton.text")); // NOI18N
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
                .addComponent(parametersLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statisticsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alignmentButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alignmentButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportButton))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(alignmentButton)
                    .addComponent(alignmentButton1)
                    .addComponent(parametersLabel)
                    .addComponent(statisticsButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        TableExportFileChooser fileChooser = new TableExportFileChooser( TableExportFileChooser.getTableFileExtensions(), completeSnpData );
}//GEN-LAST:event_exportButtonActionPerformed

    private void alignmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignmentButtonActionPerformed
        SNP_Phylogeny sp = new SNP_Phylogeny( completeSnpData );
    }//GEN-LAST:event_alignmentButtonActionPerformed

    private void alignmentButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignmentButton1ActionPerformed
        this.setFdnamlPath();
    }//GEN-LAST:event_alignmentButton1ActionPerformed

    private void statisticsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsButtonActionPerformed
        JOptionPane.showMessageDialog( this, new SnpStatisticsPanel( this.completeSnpData.getStatsMap() ), "SNP Statistics", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_statisticsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton alignmentButton;
    private javax.swing.JButton alignmentButton1;
    private javax.swing.JButton exportButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JTable snpTable;
    private javax.swing.JButton statisticsButton;
    // End of variables declaration//GEN-END:variables


    /**
     * Adds the SNPs to show to this panel. Amino acids are calculated and the
     * SNP result table is generated
     * <p>
     * @param newResult the snps to show
     */
    @Override
    public void addResult( ResultTrackAnalysis newResult ) {

        tableFilter.setTrackMap( newResult.getTrackMap() );

        if( newResult instanceof SnpDetectionResult ) {
            SnpDetectionResult snpData = (SnpDetectionResult) newResult;

            // if first result: initialize data structures and stats
            if( this.completeSnpData == null ) {
                this.completeSnpData = snpData;
                this.snpStatsMap = snpData.getStatsMap();
                snpStatsMap.put( SNPS_TOTAL, 0 );
                snpStatsMap.put( SNPS_INTERGENEIC, 0 );
                snpStatsMap.put( SNPS_SYNONYMOUS, 0 );
                snpStatsMap.put( SNPS_CHEMIC_NEUTRAL, 0 );
                snpStatsMap.put( SNPS_CHEMIC_DIFF, 0 );
                snpStatsMap.put( SNPS_STOPS, 0 );
                snpStatsMap.put( SNPS_AA_INSERTIONS, 0 );
                snpStatsMap.put( SNPS_AA_DELETIONS, 0 );
                snpStatsMap.put( SNPS_SUBSTITUTIONS, 0 );
                snpStatsMap.put( SNPS_INSERTIONS, 0 );
                snpStatsMap.put( SNPS_DELETIONS, 0 );
            }
            else {
                this.completeSnpData.getSnpList().addAll( snpData.getSnpList() );
            }

            //snp effect statistics
            int noIntergenicSnps = snpStatsMap.get( SNPS_INTERGENEIC );
            int noSynonymousSnps = snpStatsMap.get( SNPS_SYNONYMOUS );
            int noChemicallyDiffSnps = snpStatsMap.get( SNPS_CHEMIC_DIFF );
            int noChemicallyNeutralSnps = snpStatsMap.get( SNPS_CHEMIC_NEUTRAL );
            int noStopMutations = snpStatsMap.get( SNPS_STOPS );
            int noAAInsertions = snpStatsMap.get( SNPS_AA_INSERTIONS );
            int noAADeletions = snpStatsMap.get( SNPS_AA_DELETIONS );

            //snp type statistics
            int noSubstitutions = snpStatsMap.get( SNPS_SUBSTITUTIONS );
            int noInsertions = snpStatsMap.get( SNPS_INSERTIONS );
            int noDeletions = snpStatsMap.get( SNPS_DELETIONS );

            final String intergenic = "Intergenic";
            final int snpDataSize = 23;
            List<SnpI> snps = snpData.getSnpList();
            Collections.sort( snps );
            DefaultTableModel model = (DefaultTableModel) snpTable.getModel();

            //get all features from the reference to determine amino acid
            ReferenceConnector refGenCon = ProjectConnector.getInstance().getRefGenomeConnector( this.reference.getId() );

            ParameterSetSNPs snpAnalysisParams = (ParameterSetSNPs) snpData.getParameters();
            for( PersistentChromosome chrom : reference.getChromosomes().values() ) {
                List<PersistentFeature> featuresSorted = refGenCon.getFeaturesForRegionInclParents( 0, chrom.getLength(),
                                                                                                    snpAnalysisParams.getSelFeatureTypes(), chrom.getId() );

                SnpTranslator snpTranslator = new SnpTranslator( featuresSorted, chrom, reference );

                Snp snp;
                Object[] rowData;
                String aminosSnp;
                String aminosRef;
                String codonsSnp;
                String codonsRef;
                String effect;
                String ids;
                char aminoAcid;
                List<PersistentFeature> featuresFound;
                SequenceComparison type;

                for( SnpI snpi : snps ) {

                    snp = (Snp) snpi;
                    if( snp.getChromId() == chrom.getId() ) { //only treat SNPs of current chromosome in each iteration
                        type = snp.getType();

                        int i = 0; //column index
                        rowData = new Object[snpDataSize];
                        rowData[i++] = snp.getPosition();
                        rowData[i++] = snp.getGapOrderIndex();
                        rowData[i++] = snpData.getTrackEntry( snp.getTrackId(), false );
                        rowData[i++] = snpData.getChromosomeMap().get( snp.getChromId() );
                        rowData[i++] = snp.getBase().toUpperCase();
                        rowData[i++] = snp.getRefBase();
                        rowData[i++] = snp.getARate();
                        rowData[i++] = snp.getCRate();
                        rowData[i++] = snp.getGRate();
                        rowData[i++] = snp.getTRate();
                        rowData[i++] = snp.getNRate();
                        rowData[i++] = snp.getGapRate();
                        rowData[i++] = snp.getCoverage();
                        rowData[i++] = snp.getFrequency();
                        rowData[i++] = type.toString();

                        //determine amino acid substitutions among snp substitutions
                        if( type.equals( SequenceComparison.SUBSTITUTION ) ) {
                            ++noSubstitutions;

                            aminosRef = "";
                            aminosSnp = "";
                            codonsRef = "";
                            codonsSnp = "";
                            effect = "";
                            ids = "";

                            snpTranslator.checkForFeature( snp );
                            List<CodonSnp> codons = snp.getCodons();

                            for( CodonSnp codon : codons ) {

                                aminoAcid = codon.getAminoRef();
                                if( aminoAcid != '-' ) {
                                    aminosRef += aminoAcid + " (" + AminoAcidProperties.getPropertyForAA( aminoAcid ) + ")\n";
                                }
                                else {
                                    aminosRef += aminoAcid + "\n";
                                }
                                aminoAcid = codon.getAminoSnp();
                                if( aminoAcid != '-' ) {
                                    aminosSnp += aminoAcid + " (" + AminoAcidProperties.getPropertyForAA( aminoAcid ) + ")\n";
                                }
                                else {
                                    aminosSnp += aminoAcid + "\n";
                                }
                                codonsRef += codon.getTripletRef() + "\n";
                                codonsSnp += codon.getTripletSnp() + "\n";
                                effect += codon.getEffect().getType() + "\n";
                                ids += codon.getFeature() + "\n";
                            }

                            if( codons.isEmpty() ) {
                                aminosRef = intergenic;
                                aminosSnp = intergenic;
                                codonsRef = "-";
                                codonsSnp = "-";
                                effect = "-";
                                ids = "-";

                                ++noIntergenicSnps;

                            }
                            else if( effect.contains( "E" ) ) {
                                ++noChemicallyDiffSnps;
                                if( aminosSnp.contains( "*" ) ) {
                                    ++noStopMutations;
                                }
                            }
                            else if( effect.contains( "N" ) ) {
                                ++noChemicallyNeutralSnps;
                            }
                            else if( effect.contains( "M" ) ) {
                                ++noSynonymousSnps;
                            }

                            rowData[i++] = aminosRef;
                            rowData[i++] = aminosSnp;
                            rowData[i++] = codonsRef;
                            rowData[i++] = codonsSnp;
                            rowData[i++] = effect;
                            rowData[i++] = ids;

                        }
                        else {
                            featuresFound = snpTranslator.checkCoveredByFeature( snp.getPosition() );
                            ids = "";
                            if( !featuresFound.isEmpty() ) { // insertion or deletion
                                if( type.equals( SequenceComparison.INSERTION ) ) {
                                    ++noAAInsertions;
                                    ++noInsertions;

                                }
                                else if( type.equals( SequenceComparison.DELETION ) ) {
                                    ++noAADeletions;
                                    ++noDeletions;

                                }
                                else {
                                    type = SequenceComparison.UNKNOWN;
                                }

                                for( PersistentFeature feature : featuresFound ) {
                                    ids += feature + "\n";
                                    snp.addCodon( new CodonSnp( "", "", ' ', ' ', type, feature ) );
                                }
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                rowData[i++] = String.valueOf( type.getType() );
                                rowData[i++] = ids;

                            }
                            else { //intergenic
                                rowData[i++] = intergenic;
                                rowData[i++] = intergenic;
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                rowData[i++] = "-";
                                ++noIntergenicSnps;
                                if( type.equals( SequenceComparison.INSERTION ) ) {
                                    ++noInsertions;
                                }
                                else if( type == SequenceComparison.DELETION ) {
                                    ++noDeletions;
                                }
                            }
                        }
                        rowData[i++] = snp.getAverageBaseQual();
                        rowData[i++] = snp.getAverageMappingQual();

                        model.addRow( rowData );
                    }
                }

                TableRowSorter<TableModel> sorter = new TableRowSorter<>();
                this.snpTable.setRowSorter( sorter );
                sorter.setModel( model );
                TableComparatorProvider.setPersistentTrackComparator( sorter, 2 );

                snpStatsMap.put( SNPS_TOTAL, this.completeSnpData.getSnpList().size() );
                snpStatsMap.put( SNPS_INTERGENEIC, noIntergenicSnps );
                snpStatsMap.put( SNPS_SYNONYMOUS, noSynonymousSnps );
                snpStatsMap.put( SNPS_CHEMIC_NEUTRAL, noChemicallyNeutralSnps );
                snpStatsMap.put( SNPS_CHEMIC_DIFF, noChemicallyDiffSnps );
                snpStatsMap.put( SNPS_STOPS, noStopMutations );
                snpStatsMap.put( SNPS_AA_INSERTIONS, noAAInsertions );
                snpStatsMap.put( SNPS_AA_DELETIONS, noAADeletions );
                snpStatsMap.put( SNPS_SUBSTITUTIONS, noSubstitutions );
                snpStatsMap.put( SNPS_INSERTIONS, noInsertions );
                snpStatsMap.put( SNPS_DELETIONS, noDeletions );

                this.completeSnpData.setStatsMap( snpStatsMap );

                ParameterSetSNPs params = (ParameterSetSNPs) snpData.getParameters();
                String useMainBaseString = params.isUseMainBase() ? "yes" : "no";
                this.parametersLabel.setText( org.openide.util.NbBundle.getMessage( SNP_DetectionResultPanel.class,
                                                                                    "SNP_DetectionResultPanel.parametersLabel.text", params.getMinPercentage(), params.getMinMismatchingBases(), useMainBaseString ) );

            }
        }
    }


    public void setReferenceGenome( PersistentReference reference ) {
        this.reference = reference;
    }


    /**
     * Allows to set the path to the fdnaml executable to be used for tree
     * reconstructions.
     */
    private void setFdnamlPath() {
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser( new String[1], "" ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Operation not supported!" );
            }


            @Override
            public void open( String fileLocation ) {
                NbPreferences.forModule( SNP_DetectionResultPanel.class ).put( SNP_Phylogeny.FDNAML_PATH, fileLocation );
            }


        };
        fc.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
    }


    /**
     * @return The size of the SNP data.
     */
    @Override
    public int getDataSize() {
        return this.completeSnpData.getStatsMap().get( SNPS_TOTAL );
    }


}
