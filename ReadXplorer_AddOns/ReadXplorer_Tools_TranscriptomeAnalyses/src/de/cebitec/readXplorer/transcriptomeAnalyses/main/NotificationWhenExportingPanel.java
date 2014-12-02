package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import javax.swing.BorderFactory;

/**
 *
 * @author jritter
 */
public class NotificationWhenExportingPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final String notificationText = "<html><p align='justify'>Attention! If you change the "
            + "<b>COLUMNS</b> in the exported Excel-file (.xls), "
            + "you will not be able to reimport "
            + "your data back to ReadXplorer. "
            + "We advise you to keep the original "
            + "file and work with a copy of it. "
            + "Please understand that this feature "
            + "is still in preparation.</p></html>";

    /**
     * Creates new form NotificationWhenExportingPanel
     */
    public NotificationWhenExportingPanel() {
        initComponents();
        this.notificationTextLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        this.notificationTextLabel.setText(notificationText);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        notificationTextLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(notificationTextLabel, org.openide.util.NbBundle.getMessage(NotificationWhenExportingPanel.class, "NotificationWhenExportingPanel.notificationTextLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(notificationTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(notificationTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel notificationTextLabel;
    // End of variables declaration//GEN-END:variables
}
