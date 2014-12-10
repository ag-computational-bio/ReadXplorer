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

package de.cebitec.readxplorer.ui.importer;


import de.cebitec.readxplorer.utils.fileChooser.ReadXplorerFileChooser;
import de.cebitec.readxplorer.api.objects.NewJobDialogI;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParserI;
import de.cebitec.readxplorer.parser.reference.BioJavaGff2Parser;
import de.cebitec.readxplorer.parser.reference.BioJavaGff3Parser;
import de.cebitec.readxplorer.parser.reference.BioJavaParser;
import de.cebitec.readxplorer.parser.reference.FastaReferenceParser;
import de.cebitec.readxplorer.parser.reference.ReferenceParserI;
import java.awt.Component;
import java.io.File;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.NbBundle;


/**
 * Panel displaying the options for importing new references into ReadXplorer.
 *
 * @author jwinneba, rhilker
 */
public class NewReferenceDialogPanel extends JPanel implements NewJobDialogI {

    private static final long serialVersionUID = 8362375;
    private File refSeqFile = null;
    private File refFeatureFile = null;
    private String referenceName = null;
    private final ReferenceParserI[] availableParsers = new ReferenceParserI[]{ new BioJavaParser( BioJavaParser.EMBL ),
                                                                          new BioJavaParser( BioJavaParser.GENBANK ), new BioJavaGff3Parser(), new BioJavaGff2Parser(), new FastaReferenceParser() };
    private ReferenceParserI currentParser;


    /**
     * Panel displaying the options for importing new references into
     * ReadXplorer.
     */
    public NewReferenceDialogPanel() {
        this.currentParser = this.availableParsers[0];
        this.initComponents();
        this.updateExtraComponents();
    }


    @Override
    public boolean isRequiredInfoSet() {
        return !(refSeqFile == null
                 || nameField.getText().isEmpty()
                 || descriptionField.getText().isEmpty()
                 || currentParser instanceof BioJavaGff3Parser && refFeatureFile == null);
    }


    /**
     * @return Creates and returns the reference job containing alle reference
     *         data.
     */
    public ReferenceJob getReferenceJob() {
        return new ReferenceJob( null, refSeqFile, refFeatureFile, currentParser,
                                 descriptionField.getText(), referenceName,
                                 new Timestamp( System.currentTimeMillis() ) );
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

        filetypeBox = new javax.swing.JComboBox<>(availableParsers);
        filetypeLabel = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        fileField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();
        descriptionLabel = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        descriptionLabel1 = new javax.swing.JLabel();
        fileGffLabel = new javax.swing.JLabel();
        fileGffField = new javax.swing.JTextField();
        fileGffChooserButton = new javax.swing.JButton();

        filetypeBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                if(value instanceof ParserI){
                    return super.getListCellRendererComponent(list, ((ParserI) value).getName(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }

        });
        filetypeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filetypeBoxActionPerformed(evt);
            }
        });

        filetypeLabel.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.filetypeLabel.text")); // NOI18N

        fileLabel.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileLabel.text")); // NOI18N

        fileField.setEditable(false);
        fileField.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileField.text")); // NOI18N

        fileChooserButton.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileChooserButton.text")); // NOI18N
        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserButtonActionPerformed(evt);
            }
        });

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.descriptionLabel.text")); // NOI18N

        nameLabel.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.nameLabel.text")); // NOI18N

        nameField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                nameFieldPropertyChange(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setPreferredSize(new java.awt.Dimension(450, 74));
        jScrollPane1.setViewportView(jTextArea1);

        descriptionLabel1.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.descriptionLabel1.text")); // NOI18N

        fileGffLabel.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffLabel.text")); // NOI18N

        fileGffField.setEditable(false);
        fileGffField.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffField.text")); // NOI18N
        fileGffField.setToolTipText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffField.toolTipText")); // NOI18N

        fileGffChooserButton.setText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffChooserButton.text")); // NOI18N
        fileGffChooserButton.setToolTipText(org.openide.util.NbBundle.getMessage(NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffChooserButton.toolTipTextGff3")); // NOI18N
        fileGffChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileGffChooserButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileGffLabel)
                    .addComponent(fileLabel)
                    .addComponent(filetypeLabel)
                    .addComponent(nameLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(descriptionLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fileGffChooserButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filetypeBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fileField, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                            .addComponent(fileGffField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileChooserButton))
                    .addComponent(nameField)
                    .addComponent(descriptionField)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filetypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filetypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileLabel)
                    .addComponent(fileChooserButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fileGffLabel)
                            .addComponent(fileGffField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(descriptionLabel)))
                    .addComponent(fileGffChooserButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filetypeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filetypeBoxActionPerformed
        ReferenceParserI newparser = (ReferenceParserI) filetypeBox.getSelectedItem();
        if( currentParser != newparser ) {
            currentParser = newparser;
            refSeqFile = null;
            refFeatureFile = null;
            referenceName = "";
            nameField.setText( "" );
            fileField.setText( NbBundle.getMessage( NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileField.text" ) );
            fileGffField.setText( NbBundle.getMessage( NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGffField.text" ) );
            descriptionField.setText( "" );
            this.updateExtraComponents();
        }
}//GEN-LAST:event_filetypeBoxActionPerformed


    @NbBundle.Messages( { "ErrorTitle=Open File Error",
                          "ErrorMsg=Could not open the given file! (Are the permissions set correctly?)" } )
    private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
        ReferenceParserI usedParser = currentParser instanceof BioJavaGff3Parser
                                      || currentParser instanceof BioJavaGff2Parser ? new FastaReferenceParser() : currentParser;
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( usedParser.getFileExtensions(), usedParser.getInputFileDescription() ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Saving not supported here." );
            }


            @Override
            public void open( String fileLocation ) {
                File file = new File( fileLocation );

                if( file.canRead() ) {
                    refSeqFile = file;
                    Preferences prefs = Preferences.userNodeForPackage( NewReferenceDialogPanel.class );
                    prefs.put( "RefGenome.Filepath", refSeqFile.getAbsolutePath() );
                    fileField.setText( refSeqFile.getAbsolutePath() );
                    nameField.setText( refSeqFile.getName() );
                    referenceName = refSeqFile.getName();
                    descriptionField.setText( refSeqFile.getName() );
                    try {
                        prefs.flush();
                    }
                    catch( BackingStoreException ex ) {
                        Logger.getLogger( NewReferenceDialogPanel.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                }
                else {
                    JOptionPane.showMessageDialog( this, Bundle.ErrorMsg(), Bundle.ErrorTitle(), JOptionPane.ERROR_MESSAGE );
                }
            }


        };
        fileChooser.setDirectoryProperty( "RefGenome.Filepath" );
        fileChooser.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
}//GEN-LAST:event_fileChooserButtonActionPerformed

    private void fileGffChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileGffChooserButtonActionPerformed

        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser( currentParser.getFileExtensions(), currentParser.getInputFileDescription() ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Saving not supported here." );
            }


            @Override
            public void open( String fileLocation ) {
                File file = new File( fileLocation );
                if( file.canRead() ) {
                    refFeatureFile = file;
                    Preferences prefs = Preferences.userNodeForPackage( NewReferenceDialogPanel.class );
                    prefs.put( "RefGenome.Filepath", refFeatureFile.getAbsolutePath() );
                    fileGffField.setText( refFeatureFile.getAbsolutePath() );
                    try {
                        prefs.flush();
                    }
                    catch( BackingStoreException ex ) {
                        Logger.getLogger( NewReferenceDialogPanel.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                }
                else {
                    Logger.getLogger( NewReferenceDialogPanel.class.getName() ).log( Level.WARNING, "Could not read file" );
                }
            }


        };

        fileChooser.setDirectoryProperty( "RefGenome.Filepath" );
        fileChooser.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
    }//GEN-LAST:event_fileGffChooserButtonActionPerformed

    private void nameFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_nameFieldPropertyChange
        this.referenceName = this.nameField.getText();
    }//GEN-LAST:event_nameFieldPropertyChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel descriptionLabel1;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JTextField fileField;
    private javax.swing.JButton fileGffChooserButton;
    private javax.swing.JTextField fileGffField;
    private javax.swing.JLabel fileGffLabel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JComboBox<ReferenceParserI> filetypeBox;
    private javax.swing.JLabel filetypeLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    // End of variables declaration//GEN-END:variables


    /**
     * Updates all components dependent on the chosen input file type.
     */
    private void updateExtraComponents() {
        if( currentParser instanceof BioJavaGff3Parser || currentParser instanceof BioJavaGff2Parser ) {
            this.fileGffChooserButton.setVisible( true );
            this.fileGffField.setVisible( true );
            this.fileGffLabel.setVisible( true );
            this.nameField.setVisible( false );
            this.nameLabel.setVisible( false );
            this.fileLabel.setText( "Fasta file:" );

            if( currentParser instanceof BioJavaGff2Parser ) {
                this.fileGffLabel.setText( NbBundle.getMessage( NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGff2Label.text" ) );
                this.fileGffChooserButton.setToolTipText( NbBundle.getMessage( NewReferenceDialogPanel.class,
                                                                               "NewReferenceDialogPanel.fileGffChooserButton.toolTipTextGFF2" ) );
            }
            else if( currentParser instanceof BioJavaGff3Parser ) {
                this.fileGffLabel.setText( NbBundle.getMessage( NewReferenceDialogPanel.class, "NewReferenceDialogPanel.fileGff3Label.text" ) );
                this.fileGffChooserButton.setToolTipText( NbBundle.getMessage( NewReferenceDialogPanel.class,
                                                                               "NewReferenceDialogPanel.fileGffChooserButton.toolTipTextGff3" ) );
            }
        }
        else {
            this.fileGffChooserButton.setVisible( false );
            this.fileGffField.setVisible( false );
            this.fileGffLabel.setVisible( false );
            this.nameField.setVisible( true );
            this.nameLabel.setVisible( true );
            this.fileLabel.setText( "File:" );
        }
    }


}
