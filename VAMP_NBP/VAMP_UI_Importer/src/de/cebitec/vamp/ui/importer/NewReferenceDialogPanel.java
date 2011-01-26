/*
 * NewReferenceDialogPanel.java
 *
 * Created on 12.01.2011, 12:14:37
 */

package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.reference.ReferenceParserI;
import de.cebitec.vamp.parser.reference.embl.biojava.BioJavaEmblParser;
import de.cebitec.vamp.parser.reference.fasta.FastaReferenceParser;
import de.cebitec.vamp.parser.reference.genbank.biojava.BioJavaGenBankParser;
import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author jwinneba
 */
public class NewReferenceDialogPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 8362375;
    private File refGenFile = null;
    private ReferenceParserI[] availableParsers = new ReferenceParserI[]{new BioJavaEmblParser(), new BioJavaGenBankParser(), new FastaReferenceParser()};
    private ReferenceParserI currentParser;

    /** Creates new form NewReferenceDialogPanel */
    public NewReferenceDialogPanel() {
        initComponents();
        currentParser = availableParsers[0];
    }

    public boolean isRequiredInfoSet(){
        if (refGenFile == null || nameField.getText().isEmpty() || descriptionField.getText().isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    public File getReferenceFile(){
        return refGenFile;
    }

    public ReferenceParserI getParser(){
        return currentParser;
    }

    public String getDescription(){
        return descriptionField.getText();
    }

    public String getReferenceName(){
        return nameField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filetypeBox = new javax.swing.JComboBox(availableParsers);
        filetypeLabel = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        fileField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();
        descriptionLabel = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        filetypeBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                if(value instanceof ParserI){
                    return super.getListCellRendererComponent(list, ((ParserI) value).getParserName(), index, isSelected, cellHasFocus);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filetypeLabel)
                    .addComponent(fileLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(nameLabel))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileField, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                        .addGap(22, 22, 22)
                        .addComponent(fileChooserButton))
                    .addComponent(filetypeBox, 0, 274, Short.MAX_VALUE)
                    .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                    .addComponent(descriptionField, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                .addContainerGap())
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptionLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filetypeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filetypeBoxActionPerformed
        ReferenceParserI newparser = (ReferenceParserI) filetypeBox.getSelectedItem();
        if (currentParser != newparser) {
            currentParser = newparser;
            refGenFile = null;
            nameField.setText("");
            fileField.setText("");
            descriptionField.setText("");
        }
}//GEN-LAST:event_filetypeBoxActionPerformed

    private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(currentParser.getInputFileDescription(), currentParser.getFileExtensions()));
        Preferences prefs2 = Preferences.userNodeForPackage(NewReferenceDialogPanel.class);
        String path = prefs2.get("RefGenome.Filepath", null);
        if(path!=null){
            fc.setCurrentDirectory(new File(path));
        }
        int result = fc.showOpenDialog(this);

        File file = null;

        if (result == 0) {
            // file chosen
            file = fc.getSelectedFile();

            if (file.canRead()) {
                refGenFile = file;
                Preferences prefs = Preferences.userNodeForPackage(NewReferenceDialogPanel.class);
                prefs.put("RefGenome.Filepath", refGenFile.getAbsolutePath());
                fileField.setText(refGenFile.getAbsolutePath());
                try {
                    prefs.flush();
                } catch (BackingStoreException ex) {
                    Logger.getLogger(NewReferenceDialogPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.err.print("NewReferenceDialog couldnt read file"); // TODO get rid of System.err.print
            }
        }
}//GEN-LAST:event_fileChooserButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JTextField fileField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JComboBox filetypeBox;
    private javax.swing.JLabel filetypeLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    // End of variables declaration//GEN-END:variables

}
