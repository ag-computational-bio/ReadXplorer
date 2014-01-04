/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import javax.swing.BorderFactory;

/**
 *
 * @author jritter
 */
public class NotificationWhenExportingPanel extends javax.swing.JPanel {

    private final String text = "Attention! If you change the COLUMNS in the exported Excel-file, you will\n"
            + "not be able to reimport your data back to VAMP. We advise you to keep the\n"
            + "original file and work with a copy of it. Please understand that this\n"
            + "feature is still in preparation.";

    /**
     * Creates new form NotificationWhenExportingPanel
     */
    public NotificationWhenExportingPanel() {
        initComponents();
        this.notificationTP.setBorder(BorderFactory.createRaisedBevelBorder());
        this.notificationTP.setText(this.text);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        notificationTP = new javax.swing.JTextPane();

        jScrollPane1.setViewportView(notificationTP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane notificationTP;
    // End of variables declaration//GEN-END:variables
}
