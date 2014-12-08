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

package de.cebitec.readXplorer.differentialExpression.wizard;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.ui.visualisation.reference.FeatureTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


public final class ExpressTestVisualPanelNormalization extends JPanel {

    private static final long serialVersionUID = 1L;

    private ButtonGroup bg = new ButtonGroup();
    private FeatureTableModel tm;
    private DefaultTableModel emptyTm = new DefaultTableModel();
    private List<PersistentFeature> features;
    private TableRowSorter trs;


    /**
     * Creates new form ExpressTestVisualPanelNormalization
     */
    public ExpressTestVisualPanelNormalization() {
        initComponents();
        bg.add( useHKGButton );
        bg.add( calculateButton );
        featureTable.setModel( emptyTm );
        searchField.getDocument().addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate( DocumentEvent e ) {
                updateFilter();
            }


            @Override
            public void removeUpdate( DocumentEvent e ) {
                updateFilter();
            }


            @Override
            public void changedUpdate( DocumentEvent e ) {
                updateFilter();
            }


        } );
    }


    @Override
    public String getName() {
        return "Select Normalization";
    }


    public void setFeatureList( List<PersistentFeature> features ) {
        this.features = features;
        tm = new FeatureTableModel( features.toArray( new PersistentFeature[features.size()] ) );
        trs = new TableRowSorter<>( tm );
        featureTable.setRowSorter( trs );
        updateFilter();
    }


    private void updateFilter() {
        RowFilter<TableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter( searchField.getText(), 0 );
        }
        catch( java.util.regex.PatternSyntaxException e ) {
            return;
        }
        trs.setRowFilter( rf );
    }


    public boolean useHouseKeepingGenesToNormalize() {
        return useHKGButton.isSelected();
    }


    public List<Integer> getSelectedFeatures() {
        List<Integer> ret = new ArrayList<>();
        int[] selected = featureTable.getSelectedRows();
        for( int i = 0; i < selected.length; i++ ) {
            int rowToModel = featureTable.convertRowIndexToModel( selected[i] );
            ret.add( features.get( rowToModel ).getId() );
        }
        return ret;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        calculateButton = new javax.swing.JRadioButton();
        useHKGButton = new javax.swing.JRadioButton();
        searchField = new javax.swing.JTextField();
        searchLable = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        featureTable = new javax.swing.JTable();

        calculateButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(calculateButton, org.openide.util.NbBundle.getMessage(ExpressTestVisualPanelNormalization.class, "ExpressTestVisualPanelNormalization.calculateButton.text_1")); // NOI18N
        calculateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useHKGButton, org.openide.util.NbBundle.getMessage(ExpressTestVisualPanelNormalization.class, "ExpressTestVisualPanelNormalization.useHKGButton.text_1")); // NOI18N
        useHKGButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useHKGButtonActionPerformed(evt);
            }
        });

        searchField.setText(org.openide.util.NbBundle.getMessage(ExpressTestVisualPanelNormalization.class, "ExpressTestVisualPanelNormalization.searchField.text_1")); // NOI18N
        searchField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(searchLable, org.openide.util.NbBundle.getMessage(ExpressTestVisualPanelNormalization.class, "ExpressTestVisualPanelNormalization.searchLable.text_1")); // NOI18N
        searchLable.setEnabled(false);

        jScrollPane2.setEnabled(false);

        featureTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        featureTable.setEnabled(false);
        jScrollPane2.setViewportView(featureTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchField)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchLable)
                            .addComponent(calculateButton)
                            .addComponent(useHKGButton))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(calculateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useHKGButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchLable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useHKGButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useHKGButtonActionPerformed
        featureTable.setModel( tm );
        featureTable.setRowSorter( trs );
        searchField.setEnabled( true );
        searchLable.setEnabled( true );
        featureTable.setEnabled( true );
        jScrollPane2.setEnabled( true );
    }//GEN-LAST:event_useHKGButtonActionPerformed

    private void calculateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculateButtonActionPerformed
        searchField.setEnabled( false );
        searchLable.setEnabled( false );
        featureTable.setEnabled( false );
        jScrollPane2.setEnabled( false );
        featureTable.setModel( emptyTm );
    }//GEN-LAST:event_calculateButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton calculateButton;
    private javax.swing.JTable featureTable;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLable;
    private javax.swing.JRadioButton useHKGButton;
    // End of variables declaration//GEN-END:variables
}
