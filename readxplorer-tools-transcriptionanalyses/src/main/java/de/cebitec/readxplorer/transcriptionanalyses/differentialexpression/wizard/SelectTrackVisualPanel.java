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


import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Panel to first select a reference and then some tracks associated with this
 * reference for an analysis.
 * <p>
 * @author kstadermann
 */
public final class SelectTrackVisualPanel extends JPanel implements
        ListSelectionListener {

    private static final long serialVersionUID = 1L;

    private final PersistentReference[] references;
    private int selectedIndex = -1;
    private PersistentReference selectedRef;
    private final DefaultListModel<PersistentTrack> trackListModel = new DefaultListModel<>();


    /**
     * Panel to first select a reference and then some tracks associated with
     * this reference for an analysis.
     */
    public SelectTrackVisualPanel() {
        ProjectConnector con = ProjectConnector.getInstance();
        references = con.getGenomesAsArray();
        initComponents();
    }


    /**
     * @return The name is: "Select tracks".
     */
    @Override
    public String getName() {
        return "Select tracks";
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        referenceList = new javax.swing.JList<>(references);
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        trackList = new javax.swing.JList<>(trackListModel);

        referenceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        referenceList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        referenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        referenceList.addListSelectionListener(this);
        jScrollPane1.setViewportView(referenceList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectTrackVisualPanel.class, "SelectTrackVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SelectTrackVisualPanel.class, "SelectTrackVisualPanel.jLabel2.text")); // NOI18N

        jScrollPane2.setViewportView(trackList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(0, 215, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<de.cebitec.readxplorer.databackend.dataobjects.PersistentReference> referenceList;
    private javax.swing.JList<de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack> trackList;
    // End of variables declaration//GEN-END:variables


    @Override
    public void valueChanged( ListSelectionEvent e ) {
        if( selectedIndex != referenceList.getSelectedIndex() && !e.getValueIsAdjusting() ) {
            selectedIndex = referenceList.getSelectedIndex();
            selectedRef = references[selectedIndex];
            ReferenceConnector refCon = ProjectConnector.getInstance().getRefGenomeConnector( selectedRef.getId() );
            List<PersistentTrack> tracks = refCon.getAssociatedTracks();
            trackListModel.clear();
            for( PersistentTrack persistentTrack : tracks ) {
                trackListModel.addElement( persistentTrack );
            }
        }
    }


    public int getSelectedReferenceGenomeID() {
        return selectedRef.getId();
    }


    /**
     * @return The list of selected tracks for the single selected reference.
     */
    public List<PersistentTrack> getSelectedTracks() {
        return trackList.getSelectedValuesList();
    }


    /**
     * @return Checks whether the selection of tracks is valid. true, if it is
     *         valid, false otherwise.
     */
    public boolean selectionFinished() {
        if( trackList.isSelectionEmpty() ) {
            return false;
        } else {
            List<PersistentTrack> selectedTracks = trackList.getSelectedValuesList();
            return selectedTracks.size() >= 2;
        }
    }


}
