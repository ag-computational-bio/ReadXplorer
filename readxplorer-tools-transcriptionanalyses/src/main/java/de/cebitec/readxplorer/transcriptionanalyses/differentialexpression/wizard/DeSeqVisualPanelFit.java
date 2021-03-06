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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;


public final class DeSeqVisualPanelFit extends JPanel {

    private final DefaultListModel<String> allConditionGroupsModel = new DefaultListModel<>();
    private final DefaultListModel<String> fittingOneModel = new DefaultListModel<>();
    private final DefaultListModel<String> fittingTwoModel = new DefaultListModel<>();
    private final List<String> fittingGroupOne = new ArrayList<>();
    private final List<String> fittingGroupTwo = new ArrayList<>();
    private final Set<String> assignedGroups = new HashSet<>();


    /**
     * Creates new form DeSeqVisualPanelFit
     */
    public DeSeqVisualPanelFit() {
        initComponents();
        infoLabel.setText( "" );
    }


    public void updateConditionGroupsList( Set<String> conditionGroups ) {
        boolean newDataSet = false;
        for( String currentGroup : conditionGroups ) {
            if( !allConditionGroupsModel.contains( currentGroup ) &&
                     !fittingOneModel.contains( currentGroup ) &&
                     !fittingTwoModel.contains( currentGroup ) ) {
                newDataSet = true;
                break;
            }
        }
        if( newDataSet ) {
            allConditionGroupsModel.clear();
            fittingOneModel.clear();
            fittingTwoModel.clear();
            for( String currentGroup : conditionGroups ) {
                allConditionGroupsModel.addElement( currentGroup );
            }
        }
    }


    public List<String> getFittingGroupOne() {
        return Collections.unmodifiableList( fittingGroupOne );
    }


    public List<String> getFittingGroupTwo() {
        return Collections.unmodifiableList( fittingGroupTwo );
    }


    public boolean allConditionGroupsAssigned() {
        return (allConditionGroupsModel.getSize() == assignedGroups.size());
    }


    @Override
    public String getName() {
        return "Fitting model";
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        conditionGroupsList = new javax.swing.JList<String>();
        addToFittingOne = new javax.swing.JButton();
        removeFromFittingOne = new javax.swing.JButton();
        addToFittingTwo = new javax.swing.JButton();
        removeFromFittingTwo = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        fittingTwoList = new javax.swing.JList<String>();
        jScrollPane2 = new javax.swing.JScrollPane();
        fittingOneList = new javax.swing.JList<String>();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        infoLabel = new javax.swing.JLabel();

        conditionGroupsList.setModel(allConditionGroupsModel);
        jScrollPane1.setViewportView(conditionGroupsList);

        org.openide.awt.Mnemonics.setLocalizedText(addToFittingOne, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.addToFittingOne.text_1")); // NOI18N
        addToFittingOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToFittingOneActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeFromFittingOne, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.removeFromFittingOne.text_1")); // NOI18N
        removeFromFittingOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFromFittingOneActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addToFittingTwo, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.addToFittingTwo.text_1")); // NOI18N
        addToFittingTwo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToFittingTwoActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeFromFittingTwo, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.removeFromFittingTwo.text_1")); // NOI18N
        removeFromFittingTwo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFromFittingTwoActionPerformed(evt);
            }
        });

        fittingTwoList.setModel(fittingTwoModel);
        jScrollPane3.setViewportView(fittingTwoList);

        fittingOneList.setModel(fittingOneModel);
        jScrollPane2.setViewportView(fittingOneList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.jLabel3.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.jLabel2.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.jLabel1.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(DeSeqVisualPanelFit.class, "DeSeqVisualPanelFit.infoLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(infoLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeFromFittingOne)
                            .addComponent(addToFittingOne)
                            .addComponent(addToFittingTwo)
                            .addComponent(removeFromFittingTwo))
                        .addGap(18, 18, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(infoLabel)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(addToFittingOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeFromFittingOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addToFittingTwo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeFromFittingTwo)
                .addGap(91, 91, 91))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addToFittingOneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToFittingOneActionPerformed
        List<String> tracks = conditionGroupsList.getSelectedValuesList();
        for( String currentGroup : tracks ) {
            if( !fittingOneModel.contains( currentGroup ) ) {
                fittingOneModel.addElement( currentGroup );
                fittingGroupOne.add( currentGroup );
                assignedGroups.add( currentGroup );
                infoLabel.setText( "" );
            } else {
                infoLabel.setText( "Each group can just be added once to a fitting group." );
            }
        }
    }//GEN-LAST:event_addToFittingOneActionPerformed

    private void removeFromFittingOneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFromFittingOneActionPerformed
        List<String> tracks = fittingOneList.getSelectedValuesList();
        for( String currentGroup : tracks ) {
            fittingOneModel.removeElement( currentGroup );
            fittingGroupOne.remove( currentGroup );
            assignedGroups.remove( currentGroup );
            infoLabel.setText( "" );
        }
    }//GEN-LAST:event_removeFromFittingOneActionPerformed

    private void addToFittingTwoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToFittingTwoActionPerformed
        List<String> tracks = conditionGroupsList.getSelectedValuesList();
        for( String currentGroup : tracks ) {
            if( !fittingTwoModel.contains( currentGroup ) ) {
                fittingTwoModel.addElement( currentGroup );
                fittingGroupTwo.add( currentGroup );
                assignedGroups.add( currentGroup );
                infoLabel.setText( "" );
            } else {
                infoLabel.setText( "Each group can just be added once to a fitting group." );
            }
        }
    }//GEN-LAST:event_addToFittingTwoActionPerformed

    private void removeFromFittingTwoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFromFittingTwoActionPerformed
        List<String> tracks = fittingTwoList.getSelectedValuesList();
        for( String currentGroup : tracks ) {
            fittingTwoModel.removeElement( currentGroup );
            fittingGroupTwo.remove( currentGroup );
            assignedGroups.remove( currentGroup );
            infoLabel.setText( "" );
        }
    }//GEN-LAST:event_removeFromFittingTwoActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addToFittingOne;
    private javax.swing.JButton addToFittingTwo;
    private javax.swing.JList<String> conditionGroupsList;
    private javax.swing.JList<String> fittingOneList;
    private javax.swing.JList<String> fittingTwoList;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton removeFromFittingOne;
    private javax.swing.JButton removeFromFittingTwo;
    // End of variables declaration//GEN-END:variables
}
