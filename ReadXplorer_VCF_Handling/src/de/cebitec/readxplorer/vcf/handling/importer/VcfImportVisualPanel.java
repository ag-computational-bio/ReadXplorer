/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.vcf.handling.importer;


import de.cebitec.readxplorer.api.objects.JobPanel;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.utils.fileChooser.ReadXplorerFileChooser;
import java.io.File;


/**
 *
 * @author marend
 */
public final class VcfImportVisualPanel extends JobPanel {

    private File vcfFile = null;


    /**
     * Creates new form VcfImportVisualPanel1
     */
    public VcfImportVisualPanel() {
        initComponents();

    }


    public File getVcfFile() {
        return vcfFile;
    }


    @Override
    public String getName() {
        return "VCF Parser";
    }


    public PersistentReference getReference() {
        return (PersistentReference) jComboBox1.getSelectedItem();

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>(ProjectConnector.getInstance().getGenomesAsArray());
        fileTextField = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();

        fileTextField.setEditable(false);
        fileTextField.setText(org.openide.util.NbBundle.getMessage(VcfImportVisualPanel.class, "VcfImportVisualPanel.fileTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chooseButton, org.openide.util.NbBundle.getMessage(VcfImportVisualPanel.class, "VcfImportVisualPanel.chooseButton.text")); // NOI18N
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chooseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        ReadXplorerFileChooser fc;
        fc = new ReadXplorerFileChooser( new String[]{ "vcf", "VCF", "Vcf" }, "VCF" ) {

            private static final long serialVersionUID = 1L;
            private String fileLocation;


            @Override
            public void save( String fileLocation ) {
                throw new UnsupportedOperationException( "Operation not supported!" );
            }


            @Override
            public void open( String fileLocation ) {
                vcfFile = new File( fileLocation );
                fileTextField.setText( fileLocation );
                isRequiredInfoSet();
            }


        };
//        fc.setDirectoryProperty("Converter.Filepath");
        fc.setMultiSelectionEnabled( false );
        fc.openFileChooser( ReadXplorerFileChooser.OPEN_DIALOG );
    }//GEN-LAST:event_chooseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JComboBox<de.cebitec.readxplorer.databackend.dataObjects.PersistentReference> jComboBox1;
    // End of variables declaration//GEN-END:variables


    @Override
    public boolean isRequiredInfoSet() {
        boolean isValidated = vcfFile != null && vcfFile.exists();
        firePropertyChange( VcfImportWizardPanel.PROP_VALIDATE, null, isValidated );
        return isValidated;

    }


}