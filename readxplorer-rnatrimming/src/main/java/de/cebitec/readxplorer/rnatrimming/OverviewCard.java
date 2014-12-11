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

package de.cebitec.readxplorer.rnatrimming;


import de.cebitec.readxplorer.mapping.api.MappingApi;
import org.openide.util.NbBundle;


/**
 * Displayes an overview of selected trimming parameters.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class OverviewCard extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;


    /**
     * Creates new form OverviewCard
     */
    public OverviewCard() {
        initComponents();
    }


    @Override
    public String getName() {
        return NbBundle.getMessage( OverviewCard.class, "CTL_OverviewCard.name" );
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
        overviewTextArea = new javax.swing.JTextArea();

        overviewTextArea.setColumns(20);
        overviewTextArea.setRows(5);
        jScrollPane1.setViewportView(overviewTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea overviewTextArea;
    // End of variables declaration//GEN-END:variables


    void showGenereateOverview( String reference, String source, String method, String maximum ) {
        overviewTextArea.setText( "Mapping script:\n" );
        overviewTextArea.append( MappingApi.getMapperPath() + "\n" + "\n" );

        overviewTextArea.append( "Reference file:\n" );
        overviewTextArea.append( reference + "\n" + "\n" );

        overviewTextArea.append( "Source file:\n" );
        overviewTextArea.append( source + "\n" + "\n" );

        overviewTextArea.append( "Trim maximum:\n" );
        overviewTextArea.append( maximum + "\n" + "\n" );

        overviewTextArea.append( "Trim method:\n" );
        overviewTextArea.append( method + "\n" + "\n" );
    }


}
