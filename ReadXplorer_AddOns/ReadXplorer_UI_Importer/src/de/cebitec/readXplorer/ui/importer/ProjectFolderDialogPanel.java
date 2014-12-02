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

import de.cebitec.readXplorer.api.objects.NewJobDialogI;
import de.cebitec.readXplorer.util.Properties;
import java.io.File;
import javax.swing.JFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ProjectFolderDialogPanel extends javax.swing.JPanel implements NewJobDialogI {
    
    private static final long serialVersionUID = 1L;
    
    private String projectFolder;
    
    /**
     * Creates new form ProjectFolderDialogPanel
     */
    public ProjectFolderDialogPanel() {
        initComponents();
        this.projectFolder = "";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectFolderPathLabel = new javax.swing.JLabel();
        projectFolderPathText = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();

        projectFolderPathLabel.setText(org.openide.util.NbBundle.getMessage(ProjectFolderDialogPanel.class, "ProjectFolderDialogPanel.projectFolderPathLabel.text")); // NOI18N

        projectFolderPathText.setText(org.openide.util.NbBundle.getMessage(ProjectFolderDialogPanel.class, "ProjectFolderDialogPanel.projectFolderPathText.text")); // NOI18N

        chooseButton.setText(org.openide.util.NbBundle.getMessage(ProjectFolderDialogPanel.class, "ProjectFolderDialogPanel.chooseButton.text")); // NOI18N
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
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chooseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(projectFolderPathText, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(projectFolderPathLabel))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(projectFolderPathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseButton)
                    .addComponent(projectFolderPathText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        JFileChooser chooser = new JFileChooser(NbPreferences.forModule(Object.class).get(Properties.ReadXplorer_DATABASE_DIRECTORY, null));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = chooser.showOpenDialog(this);
        
        File file = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            // file chosen
            file = chooser.getSelectedFile();
//            try { //store current directory
                this.projectFolder = chooser.getSelectedFile().getPath();
//            } catch (IOException ex) {
//                // do nothing, path is not stored in properties...
//            }
            
            if (!file.exists()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ProjectFolderDialogPanel.class, "MSG_ProjectFolderDialogPanel.warning.folder", file.getAbsolutePath()), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            projectFolderPathText.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_chooseButtonActionPerformed

    public String getProjectFolder() {
        return projectFolder;
    }
   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseButton;
    private javax.swing.JLabel projectFolderPathLabel;
    private javax.swing.JTextField projectFolderPathText;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean isRequiredInfoSet() {
        return !this.projectFolder.isEmpty();
    }
}
