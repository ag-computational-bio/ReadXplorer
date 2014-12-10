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

package de.cebitec.readxplorer.view.tablevisualization;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.parser.tables.TableType;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import de.cebitec.readxplorer.view.tablevisualization.tablefilter.TableRightClickFilter;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Creates a new position table panel. A position table starts with a column
 * containing the position.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class PosTablePanel extends TablePanel {

    private static final long serialVersionUID = 1L;
    private final UneditableTableModel tableData;
    private PersistentReference reference;
    private final TableRightClickFilter<UneditableTableModel> filterListener;


    /**
     * Creates a new position table panel. A position table starts with a column
     * containing the position.
     * <p>
     * @param tableData The data to display in this panel's table.
     * @param tableType The type of data table.
     */
    public PosTablePanel( UneditableTableModel tableData, TableType tableType ) {
        this.tableData = tableData;
        final int posColumn = 0;
        final int trackColumn;
        final int chromColumn;
        switch( tableType ) {
            case COVERAGE_ANALYSIS: //fallthrough
            case RPKM_ANALYSIS: //fallthrough
            case SNP_DETECTION: //fallthrough
            case OPERON_DETECTION:
                trackColumn = 2;
                chromColumn = 3;
                break;
            case DIFF_GENE_EXPRESSION:
                chromColumn = 1;
                trackColumn = 2;
                break;
            case FEATURE_COVERAGE_ANALYSIS: //fallthrough
            case TSS_DETECTION: //fallthrough
            default:
                trackColumn = 1;
                chromColumn = 2;
                break; //for all other tables
        }
        this.initComponents();
        this.initAdditionalComponents( posColumn, chromColumn );
        filterListener = new TableRightClickFilter<>( UneditableTableModel.class, posColumn, trackColumn );
        this.dataTable.getTableHeader().addMouseListener( filterListener );
    }


    /**
     * Initializes additionals stuff for this panel.
     */
    private void initAdditionalComponents( final int posColumn, final int chromColumn ) {
        this.dataTable.setModel( this.tableData );

        DefaultListSelectionModel model = (DefaultListSelectionModel) dataTable.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                //TODO: feature position - map mit features im ram halten
                //TODO: after closing of ref and reopening, it does not react anymore
                TableUtils.showPosition( dataTable, posColumn, chromColumn, getBoundsInfoManager(), reference );
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

        dataScrollPane = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        dataScrollPane.setViewportView(dataTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JTable dataTable;
    // End of variables declaration//GEN-END:variables


    /**
     * @return Number of rows in the complete data model.
     */
    @Override
    public int getDataSize() {
        if( tableData != null ) {
            return tableData.getRowCount();
        }
        else {
            return 0;
        }
    }


    /**
     * @param reference The reference genome, for which this table was imported.
     */
    public void setReferenceGenome( PersistentReference reference ) {
        this.reference = reference;
    }


    /**
     * @return The reference genome, for which this table was imported.
     */
    public PersistentReference getReferenceGenome() {
        return this.reference;
    }


}
