/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.tools.referenceEditor;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.NotifyDescriptor;

/**
 * An editor showing a part of the currently viewed chromosome sequence. It 
 * allows to edit the sequence and copy or store it.
 *
 * @author jstraube, rhilker
 */
public class ReferenceEditor extends javax.swing.JFrame {
    private static final long serialVersionUID = 1L;

    private PersistantReference refGen;
    private String activeChromSubSeq;



    /** 
     * An editor showing a part of the currently viewed chromosome sequence. It
     * allows to edit the sequence and copy or store it.
     */
    public ReferenceEditor(PersistantReference reference) {
        this.setSize(300, 300);

        if (reference != null) {
            this.refGen = reference;
            this.setTitle(refGen.getName());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "PersistanceReference is null {0}!");
        }
//        myInit();
        this.initComponents();
        this.setLocation(100, 300);
        this.setVisible(true);
    }

//    private void myInit() {
//        refSeqLabel = new javax.swing.JLabel();
//        fromLabel = new javax.swing.JLabel();
//        toLabel = new javax.swing.JLabel();
//        fromSpinner = new javax.swing.JSpinner();
//        toSpinner = new javax.swing.JSpinner();
//        revComlementCheckBox = new javax.swing.JCheckBox();
//        sequenceScrollPane = new javax.swing.JScrollPane();
//        genomeTextArea = new javax.swing.JTextArea();
//        getSequenceButton = new javax.swing.JButton();
//
//        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
//
//    refSeqLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.refSeqLabel.text")); // NOI18N
//
//      fromLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.fromLabel.text")); // NOI18N
//
//        toLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.toLabel.text")); // NOI18N
//
//        revComlementCheckBox.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.revComlementCheckBox.text")); // NOI18N
//
//        genomeTextArea.setColumns(20);
//        genomeTextArea.setRows(5);
//  sequenceScrollPane.setViewportView(genomeTextArea);
//
//        getSequenceButton.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.getSequenceButton.text")); // NOI18N
//        getSequenceButton.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                getSequenceButtonActionPerformed(evt);
//            }
//        });
//
//        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addComponent(refSeqLabel)
//                .addGroup(layout.createSequentialGroup()
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(layout.createSequentialGroup()
//                .addComponent(fromLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(fromSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(layout.createSequentialGroup()
//                .addComponent(toLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(toSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
//                .addComponent(revComlementCheckBox)))
//                .addGap(110, 110, 110)
//                .addComponent(getSequenceButton))
//                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(15, Short.MAX_VALUE)));
//        layout.setVerticalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(refSeqLabel)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
//                .addGap(12, 12, 12)
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
//                .addComponent(getSequenceButton)
//                .addGroup(layout.createSequentialGroup()
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                .addComponent(fromSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addComponent(fromLabel))
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                .addComponent(toLabel)
//                .addComponent(toSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addGap(18, 18, 18)
//                .addComponent(revComlementCheckBox)))
//                .addContainerGap()));
//
//        pack();
//
//    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        refSeqLabel = new javax.swing.JLabel();
        fromLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        fromSpinner = new javax.swing.JSpinner();
        toSpinner = new javax.swing.JSpinner();
        revComlementCheckBox = new javax.swing.JCheckBox();
        sequenceScrollPane = new javax.swing.JScrollPane();
        genomeTextArea = new javax.swing.JTextArea();
        getSequenceButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        refSeqLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.refSeqLabel.text")); // NOI18N

        fromLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.fromLabel.text")); // NOI18N

        toLabel.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.toLabel.text")); // NOI18N

        revComlementCheckBox.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.revComlementCheckBox.text")); // NOI18N

        genomeTextArea.setColumns(20);
        genomeTextArea.setRows(5);
        sequenceScrollPane.setViewportView(genomeTextArea);

        getSequenceButton.setText(org.openide.util.NbBundle.getMessage(ReferenceEditor.class, "ReferenceEditor.getSequenceButton.text")); // NOI18N
        getSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSequenceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequenceScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fromLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(refSeqLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(toLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(revComlementCheckBox)
                                    .addComponent(toSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 163, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(getSequenceButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refSeqLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fromLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toLabel)
                    .addComponent(toSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revComlementCheckBox)
                    .addComponent(getSequenceButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void getSequenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getSequenceButtonActionPerformed
        int from = (Integer) fromSpinner.getValue();
        int to = (Integer) toSpinner.getValue();

        if (from > 0 && to > 0 && from <= refGen.getActiveChromLength() && to < refGen.getActiveChromLength() && from < to) {
            activeChromSubSeq = refGen.getActiveChromSequence(from -1, to);

            if (revComlementCheckBox.isSelected()) {
                activeChromSubSeq = SequenceUtils.getReverseComplement(activeChromSubSeq);
            }
            genomeTextArea.setLineWrap(true);
            genomeTextArea.setText(activeChromSubSeq);
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("The values don't fit in the genome range", NotifyDescriptor.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_getSequenceButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fromLabel;
    private javax.swing.JSpinner fromSpinner;
    private javax.swing.JTextArea genomeTextArea;
    private javax.swing.JButton getSequenceButton;
    private javax.swing.JLabel refSeqLabel;
    private javax.swing.JCheckBox revComlementCheckBox;
    private javax.swing.JScrollPane sequenceScrollPane;
    private javax.swing.JLabel toLabel;
    private javax.swing.JSpinner toSpinner;
    // End of variables declaration//GEN-END:variables

}
