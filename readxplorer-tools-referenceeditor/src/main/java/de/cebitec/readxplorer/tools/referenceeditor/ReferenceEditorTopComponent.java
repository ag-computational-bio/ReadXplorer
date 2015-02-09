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

package de.cebitec.readxplorer.tools.referenceeditor;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.utils.SequenceUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * Top component which displays the reference editor.
 */
@ConvertAsProperties(
         dtd = "-//de.cebitec.readxplorer.tools.referenceeditor//ReferenceEditor//EN",
         autostore = false
)
@TopComponent.Description(
         preferredID = "ReferenceEditorTopComponent",
         iconBase = "de/cebitec/readxplorer/tools/referenceeditor/openRefEditor.png",
         persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration( mode = "editor", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readxplorer.tools.referenceeditor.ReferenceEditorTopComponent" )
@ActionReference( path = "Menu/Window" /*, position = 333 */ )
@TopComponent.OpenActionRegistration(
         displayName = "#CTL_ReferenceEditorAction",
         preferredID = "ReferenceEditorTopComponent"
)
@Messages( {
    "CTL_ReferenceEditorAction=ReferenceEditor",
    "CTL_ReferenceEditorTopComponent=ReferenceEditor Window",
    "HINT_ReferenceEditorTopComponent=This is a ReferenceEditor window"
} )
public final class ReferenceEditorTopComponent extends TopComponent {

    private static final long serialVersionUID = 1L;

    private PersistentReference reference;
    private String activeChromSubSeq;


    /**
     * Top component which displays the reference editor.
     * <br>Make sure to call <code>setReference()</code> before opening the
     * editor.
     */
    public ReferenceEditorTopComponent() {
        initComponents();
        setName( Bundle.CTL_ReferenceEditorTopComponent() );
        setToolTipText( Bundle.HINT_ReferenceEditorTopComponent() );

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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

        org.openide.awt.Mnemonics.setLocalizedText(refSeqLabel, org.openide.util.NbBundle.getMessage(ReferenceEditorTopComponent.class, "ReferenceEditorTopComponent.refSeqLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fromLabel, org.openide.util.NbBundle.getMessage(ReferenceEditorTopComponent.class, "ReferenceEditorTopComponent.fromLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(toLabel, org.openide.util.NbBundle.getMessage(ReferenceEditorTopComponent.class, "ReferenceEditorTopComponent.toLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(revComlementCheckBox, org.openide.util.NbBundle.getMessage(ReferenceEditorTopComponent.class, "ReferenceEditorTopComponent.revComlementCheckBox.text")); // NOI18N

        genomeTextArea.setColumns(20);
        genomeTextArea.setRows(5);
        sequenceScrollPane.setViewportView(genomeTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(getSequenceButton, org.openide.util.NbBundle.getMessage(ReferenceEditorTopComponent.class, "ReferenceEditorTopComponent.getSequenceButton.text")); // NOI18N
        getSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSequenceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
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
                        .addGap(0, 201, Short.MAX_VALUE))
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
                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
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
    }// </editor-fold>//GEN-END:initComponents

    private void getSequenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getSequenceButtonActionPerformed
        int from = (Integer) fromSpinner.getValue();
        int to = (Integer) toSpinner.getValue();

        if( from > 0 && to > 0 && from <= reference.getActiveChromLength() && to < reference.getActiveChromLength() && from < to ) {
            activeChromSubSeq = reference.getActiveChromSequence( from - 1, to );

            if( revComlementCheckBox.isSelected() ) {
                activeChromSubSeq = SequenceUtils.getReverseComplement( activeChromSubSeq );
            }
            genomeTextArea.setLineWrap( true );
            genomeTextArea.setText( activeChromSubSeq );
        }
        else {
            NotifyDescriptor nd = new NotifyDescriptor.Message( "The values don't fit in the genome range", NotifyDescriptor.INFORMATION_MESSAGE );
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


    @Override
    public void componentOpened() {
        // add custom code on component opening
    }


    @Override
    public void componentClosed() {
        // add custom code on component closing
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // store your settings
    }


    void readProperties( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // read your settings according to their version
    }


    /**
     * @param reference Set the reference genome used in this editor.
     * <br>Must be called before anything else can be done!
     */
    public void setReference( PersistentReference reference ) {
        this.reference = reference;
    }


}
