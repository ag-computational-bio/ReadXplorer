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


import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.differentialExpression.Group;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public final class BaySeqVisualPanel3 extends JPanel implements
        ListSelectionListener {

    private final DefaultListModel<PersistentTrack> trackListModel = new DefaultListModel<>();
    private final DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private final List<Group> createdGroups = new ArrayList<>();
    private List<PersistentTrack> selectedTracks = new ArrayList<>();
    private Integer[] currentGroupBeingCreated = null;
    private int currentGroupNumber = 1;
    private int selectedIndex = -1;


    /**
     * Creates new form BaySeqVisualPanel3
     */
    public BaySeqVisualPanel3() {
        initComponents();
    }


    public void updateTrackList( List<PersistentTrack> selectedTracks ) {
        if( !this.selectedTracks.equals( selectedTracks ) ) {
            this.selectedTracks = selectedTracks;
            currentGroupNumber = 1;
            selectedIndex = -1;
            currentGroupBeingCreated = null;
            createdGroups.clear();
            groupListModel.clear();
            Integer[] defaultGroup = new Integer[selectedTracks.size()];
            StringBuilder strBuilder = new StringBuilder( "{" );
            for( Iterator<PersistentTrack> it = selectedTracks.iterator(); it.hasNext(); ) {
                PersistentTrack persistentTrack = it.next();
                defaultGroup[selectedTracks.indexOf( persistentTrack )] = currentGroupNumber;
                strBuilder.append( persistentTrack.getDescription() );
                if( it.hasNext() ) {
                    strBuilder.append( "," );
                }
                else {
                    strBuilder.append( "}" );
                }
            }
            currentGroupNumber++;
            createdGroups.add( new Group( defaultGroup, strBuilder.toString() ) );
            infoText.setText( "The group " + strBuilder.toString() + " is created automatically." );
        }
        trackListModel.clear();
        for( PersistentTrack persistentTrack : selectedTracks ) {
            trackListModel.addElement( persistentTrack );
        }
    }


    @Override
    public String getName() {
        return "Create models";
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        trackList = new javax.swing.JList(trackListModel);
        groupCreationField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        addModelButton = new javax.swing.JButton();
        removeModelButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        infoText = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        createdGroupsList = new javax.swing.JList(groupListModel);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.jLabel1.text_1")); // NOI18N

        jScrollPane1.setViewportView(trackList);

        groupCreationField.setEditable(false);
        groupCreationField.setText(org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.groupCreationField.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.addButton.text_1")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addModelButton, org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.addModelButton.text_1")); // NOI18N
        addModelButton.setEnabled(false);
        addModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeModelButton, org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.removeModelButton.text_1")); // NOI18N
        removeModelButton.setEnabled(false);
        removeModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModelButtonActionPerformed(evt);
            }
        });

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        infoText.setEditable(false);
        infoText.setText(org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.infoText.text")); // NOI18N
        infoText.setBorder(null);
        infoText.setPreferredSize(new java.awt.Dimension(490, 58));
        jScrollPane4.setViewportView(infoText);
        infoText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BaySeqVisualPanel3.class, "BaySeqVisualPanel3.infoText.AccessibleContext.accessibleDescription")); // NOI18N

        createdGroupsList.addListSelectionListener(this);
        createdGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(createdGroupsList);

        jScrollPane5.setViewportView(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(58, 470, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(addButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(addModelButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(removeModelButton))
                                            .addComponent(groupCreationField)))
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(groupCreationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(addButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(removeModelButton))
                            .addComponent(addModelButton, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if( currentGroupBeingCreated == null ) {
            currentGroupBeingCreated = new Integer[selectedTracks.size()];
        }
        if( !trackList.isSelectionEmpty() ) {
            List<PersistentTrack> tracks = trackList.getSelectedValuesList();
            StringBuilder strBuilder = new StringBuilder( groupCreationField.getText() + "{" );
            for( Iterator<PersistentTrack> it = tracks.iterator(); it.hasNext(); ) {
                PersistentTrack persistentTrack = it.next();
                currentGroupBeingCreated[selectedTracks.indexOf( persistentTrack )] = currentGroupNumber;
                strBuilder.append( persistentTrack.getDescription() );
                trackListModel.removeElement( persistentTrack );
                if( it.hasNext() ) {
                    strBuilder.append( "," );
                }
                else {
                    strBuilder.append( "}" );
                }
            }
            groupCreationField.setText( strBuilder.toString() );
            currentGroupNumber++;
        }
        if( trackListModel.isEmpty() ) {
            addModelButton.setEnabled( true );
            addButton.setEnabled( false );
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void addModelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModelButtonActionPerformed

        createdGroups.add( new Group( currentGroupBeingCreated, groupCreationField.getText() ) );
        currentGroupBeingCreated = null;
        groupListModel.addElement( groupCreationField.getText() );
        groupCreationField.setText( "" );
        updateTrackList( selectedTracks );
        addButton.setEnabled( true );
        addModelButton.setEnabled( false );
        jScrollPane5.updateUI();
    }//GEN-LAST:event_addModelButtonActionPerformed

    private void removeModelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModelButtonActionPerformed
        createdGroups.remove( selectedIndex );
        groupListModel.remove( selectedIndex );
        selectedIndex = -1;
        removeModelButton.setEnabled( false );
    }//GEN-LAST:event_removeModelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addModelButton;
    private javax.swing.JList createdGroupsList;
    private javax.swing.JTextField groupCreationField;
    private javax.swing.JTextField infoText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton removeModelButton;
    private javax.swing.JList trackList;
    // End of variables declaration//GEN-END:variables


    @Override
    public void valueChanged( ListSelectionEvent e ) {
        if( selectedIndex != e.getFirstIndex() ) {
            selectedIndex = e.getFirstIndex();
            removeModelButton.setEnabled( true );
        }
    }


    public List<Group> getCreatedGroups() {
        return createdGroups;
    }


    public boolean noGroupCreated() {
        return createdGroups.isEmpty();
    }


}
