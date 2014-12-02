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
package de.cebitec.readXplorer.ui.importer;

import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author ddoppmeier
 */
public class ImportOverviewCard extends javax.swing.JPanel {

    private static final long serialVersionUID = 663057846;

    /** Creates new form ImportOverviewCard */
    public ImportOverviewCard() {
        initComponents();
    }

    public void showOverview(List<ReferenceJob> refGenJobList, List<TrackJob> trackJobList, 
            List<ReadPairJobContainer> seqPairJobList) {
        overviewTextArea.setText("");

        if(!refGenJobList.isEmpty()){
            overviewTextArea.append(NbBundle.getMessage(ImportOverviewCard.class, "MSG_ImportOverviewCard.text.references") + ":\n");
            for(Iterator<ReferenceJob> it = refGenJobList.iterator(); it.hasNext(); ){
                ReferenceJob r = it.next();
                overviewTextArea.append(r.getFile().getAbsolutePath()+"\n");
                overviewTextArea.append("\t"+r.getName()+"\n");
                overviewTextArea.append("\t"+r.getDescription()+"\n");
            }
            overviewTextArea.append("\n");
        }

        if(!trackJobList.isEmpty()){
            overviewTextArea.append(NbBundle.getMessage(ImportOverviewCard.class, "MSG_ImportOverviewCard.text.tracks") + ":\n");
            for(Iterator<TrackJob> it = trackJobList.iterator(); it.hasNext(); ){
                TrackJob r = it.next();
                overviewTextArea.append(r.getFile().getAbsolutePath()+"\n");
                overviewTextArea.append("\t"+r.getDescription()+"\n");
            }
        }
        
        if(!seqPairJobList.isEmpty()){
            overviewTextArea.append(NbBundle.getMessage(ImportOverviewCard.class, "MSG_ImportOverviewCard.text.readPairs") + ":\n");
            for(Iterator<ReadPairJobContainer> it = seqPairJobList.iterator(); it.hasNext(); ){
                ReadPairJobContainer seqPairCont = it.next();
                String file2Name = seqPairCont.getTrackJob2() != null ? seqPairCont.getTrackJob2().getFile().getAbsolutePath() : "-";
                overviewTextArea.append(seqPairCont.getTrackJob1().getFile().getAbsolutePath()+"\n");
                overviewTextArea.append("\t"+seqPairCont.getTrackJob1().getDescription()+"\n");
                overviewTextArea.append(file2Name + "\n");
                String description2 = seqPairCont.getTrackJob2() != null ? seqPairCont.getTrackJob2().getDescription() : "-";
                overviewTextArea.append("\t"+description2+"\n");
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportOverviewCard.class, "CTL_ImportOverviewCard.name");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea overviewTextArea;
    // End of variables declaration//GEN-END:variables

}
