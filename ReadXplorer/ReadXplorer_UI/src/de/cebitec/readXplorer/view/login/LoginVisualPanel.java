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
package de.cebitec.readXplorer.view.login;

import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author ddopmeier?, jstraube?, rhilker
 */
public final class LoginVisualPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private String defaultDatabaseMySQL;
    private String defaultdatabaseh2;
    private String defaultuser;
    private String defaulthostname;

    /** Creates new form LoginVisualPanel */
    public LoginVisualPanel() {
        this.initComponents();
        this.setLoginData();
        this.updateUIForH2();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.name");
    }
    
    private void setLoginData() {
        Preferences prefs = Preferences.userNodeForPackage(LoginProperties.class);
        defaultuser = prefs.get(LoginProperties.LOGIN_USER , null);
        defaultDatabaseMySQL = prefs.get(LoginProperties.LOGIN_DATABASE_MYSQL, null);
        defaulthostname = prefs.get(LoginProperties.LOGIN_HOSTNAME, null);
        defaultdatabaseh2 = prefs.get(LoginProperties.LOGIN_DATABASE_H2, null);
        userField.setText(defaultuser);
        urlField.setText(defaulthostname);
        databaseField.setText(defaultDatabaseMySQL);
    }

    public Map<String, String> getLoginData(){
        Map<String, String> loginData = new HashMap<>();

        String adapter = dbTypeBox.getSelectedItem().toString();
        String hostname, database, user, password;

        if (adapter.equalsIgnoreCase("mysql")){
            hostname = urlField.getText();
            database = databaseField.getText();
            user = userField.getText();
            password = new String(passwordField.getPassword());
        }
        else if (adapter.equalsIgnoreCase("h2")){
            hostname = null;
            database = databaseField.getText();
            if (database.endsWith(".h2.db")) { database = database.replace(".h2.db", ""); }
            if (database.endsWith(".h2")) { database = database.replace(".h2", ""); }
            user = null;
            password = null;
        }
        else /* <editor-fold defaultstate="collapsed" desc="should not reach here">*/{
            hostname = null;
            database = null;
            user = null;
            password = null;
        }// </editor-fold>

        loginData.put(LoginWizardPanel.PROP_ADAPTER, adapter);
        loginData.put(LoginWizardPanel.PROP_HOST, hostname);
        loginData.put(LoginWizardPanel.PROP_DATABASE, database);
        loginData.put(LoginWizardPanel.PROP_USER, user);
        loginData.put(LoginWizardPanel.PROP_PASSWORD, password);

        // save login data if desired
        saveLoginData(loginData);

        return loginData;
    }

    private void saveLoginData(Map<String, String> loginData){
        Preferences prefs = Preferences.userNodeForPackage(LoginVisualPanel.class);
        String adapter = loginData.get(LoginWizardPanel.PROP_ADAPTER);

        if (saveDataCheckBox.isSelected()) {
            if (adapter.equalsIgnoreCase("mysql")){
                prefs.put(LoginProperties.LOGIN_HOSTNAME, loginData.get(LoginWizardPanel.PROP_HOST));
                prefs.put(LoginProperties.LOGIN_USER, loginData.get(LoginWizardPanel.PROP_USER));
                prefs.put(LoginProperties.LOGIN_DATABASE_MYSQL, loginData.get(LoginWizardPanel.PROP_DATABASE));
            }
            else if (adapter.equalsIgnoreCase("h2")){
                prefs.put(LoginProperties.LOGIN_DATABASE_H2, loginData.get(LoginWizardPanel.PROP_DATABASE));
            }
            else{
                // should not reach here
            }
        }
        else {
            if (adapter.equalsIgnoreCase("mysql")){
                prefs.put(LoginProperties.LOGIN_HOSTNAME, "");
                prefs.put(LoginProperties.LOGIN_DATABASE_MYSQL, "");
                prefs.put(LoginProperties.LOGIN_USER, "");
            }
            else if (adapter.equalsIgnoreCase("h2")){
                prefs.put(LoginProperties.LOGIN_DATABASE_H2, "");
            }
            else{
                // should not reach here
            }
        }
        
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(LoginVisualPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel = new javax.swing.JLabel();
        databaseLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        databaseField = new javax.swing.JTextField();
        userField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        urlField = new javax.swing.JTextField();
        dbTypeLabel = new javax.swing.JLabel();
        dbTypeBox = new javax.swing.JComboBox<>();
        saveDataCheckBox = new javax.swing.JCheckBox();
        dbChooseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.urlLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(databaseLabel, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.databaseLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.userLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.passwordLabel.text")); // NOI18N

        databaseField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(dbTypeLabel, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.dbTypeLabel.text")); // NOI18N

        dbTypeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "h2", "MySQL" }));
        dbTypeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbTypeBoxActionPerformed(evt);
            }
        });

        saveDataCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(saveDataCheckBox, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.saveDataCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dbChooseButton, org.openide.util.NbBundle.getMessage(LoginVisualPanel.class, "LoginVisualPanel.dbChooseButton.text")); // NOI18N
        dbChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbChooseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(saveDataCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(databaseLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(27, 27, 27)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(userLabel)
                                        .addComponent(passwordLabel)))
                                .addComponent(dbTypeLabel))
                            .addComponent(urlLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                            .addComponent(userField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                            .addComponent(dbTypeBox, 0, 418, Short.MAX_VALUE)
                            .addComponent(urlField, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(databaseField, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbChooseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(urlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbChooseButton)
                    .addComponent(databaseLabel))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveDataCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dbTypeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbTypeBoxActionPerformed
        String db = dbTypeBox.getSelectedItem().toString();
        if (db.equalsIgnoreCase("h2")) {
            this.updateUIForH2();
        } else {
            userField.setVisible(true);
            urlField.setVisible(true);
            passwordField.setVisible(true);
            passwordLabel.setVisible(true);
            urlLabel.setVisible(true);
            userLabel.setVisible(true);
            dbChooseButton.setVisible(false);
            databaseField.setText(defaultDatabaseMySQL);
        }
}//GEN-LAST:event_dbTypeBoxActionPerformed

    private void dbChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbChooseButtonActionPerformed
        ReadXplorerFileChooser fileChooser = new ReadXplorerFileChooser(new String[]{"db"}, "db files") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                //not supported here
            }

            @Override
            public void open(String fileLocation) {
                File file = new File(fileLocation);

                try { //store current directory
                    NbPreferences.forModule(Object.class).put(Properties.ReadXplorer_DATABASE_DIRECTORY, this.getCurrentDirectory().getCanonicalPath());
                } catch (IOException ex) {
                    // do nothing, path is not stored in properties...
                }

                if (!file.exists()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(LoginVisualPanel.class, "MSG_LoginVisualPanel.warning.database", fileLocation), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
                databaseField.setText(file.getAbsolutePath());
                
            }
        };
        fileChooser.setDirectory(NbPreferences.forModule(Object.class).get(Properties.ReadXplorer_DATABASE_DIRECTORY, null));
        
        Preferences prefs2 = Preferences.userNodeForPackage(LoginVisualPanel.class);
        String db = dbTypeBox.getSelectedItem().toString();
        if (db.equalsIgnoreCase("h2")) {
            String path = prefs2.get(LoginProperties.LOGIN_DATABASE_H2, null);
            if (path != null) {
                fileChooser.setCurrentDirectory(new File(path));
            }
        } else {
            String path = prefs2.get(LoginProperties.LOGIN_DATABASE_MYSQL, null);
            if (path != null) {
                fileChooser.setCurrentDirectory(new File(path));
            }
        }
        
        fileChooser.openFileChooser(ReadXplorerFileChooser.OPEN_DIALOG);
}//GEN-LAST:event_dbChooseButtonActionPerformed

    private void databaseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseFieldActionPerformed
        // add your handling code here:
    }//GEN-LAST:event_databaseFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField databaseField;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JButton dbChooseButton;
    private javax.swing.JComboBox<String> dbTypeBox;
    private javax.swing.JLabel dbTypeLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox saveDataCheckBox;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Updates the ui that only the database field and choose button are still active, since
     * no login information is needed for the local h2 database.
     */
    private void updateUIForH2() {
        userField.setVisible(false);
        urlField.setVisible(false);
        passwordField.setVisible(false);
        userLabel.setVisible(false);
        passwordLabel.setVisible(false);
        urlLabel.setVisible(false);
        dbChooseButton.setVisible(true);
        databaseField.setText(defaultdatabaseh2);
    }
    
    /**
     * Updates the choose button text.
     * @param chooseButtonText 
     */
    public void setChooseButtonText(String chooseButtonText) {
        this.dbChooseButton.setText(chooseButtonText);
    }
}
