package de.cebitec.vamp.options;

import de.cebitec.vamp.util.Properties;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

final class ObjectCachePanel extends javax.swing.JPanel {
    
    private final ObjectCachePanelController controller;
    private Preferences pref;
    
    ObjectCachePanel(ObjectCachePanelController controller) {
        this.controller = controller;
        initComponents();
        this.pref = NbPreferences.forModule(Object.class);
        setUpListener();
    }
    
    private void setUpListener() {
        cacheCheckBox.addKeyListener(new KeyListener() {
            
            @Override
            public void keyTyped(KeyEvent e) {
                controller.changed();
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cacheCheckBox = new javax.swing.JCheckBox();
        useCacheCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ObjectCachePanel.class, "ObjectCachePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cacheCheckBox, org.openide.util.NbBundle.getMessage(ObjectCachePanel.class, "ObjectCachePanel.cacheCheckBox.text")); // NOI18N
        cacheCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cacheCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useCacheCheckBox, org.openide.util.NbBundle.getMessage(ObjectCachePanel.class, "ObjectCachePanel.useCacheCheckBox.text")); // NOI18N
        useCacheCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectCachePanel.class, "ObjectCachePanel.useCacheCheckBox.toolTipText")); // NOI18N
        useCacheCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCacheCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 843, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cacheCheckBox)
                            .addComponent(useCacheCheckBox))))
                .addContainerGap(435, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(useCacheCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cacheCheckBox)
                .addContainerGap(135, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cacheCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cacheCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cacheCheckBoxActionPerformed

    private void useCacheCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCacheCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useCacheCheckBoxActionPerformed

    void load() {
        cacheCheckBox.setSelected(pref.getBoolean(Properties.OBJECTCACHE_AUTOSTART, true));
        useCacheCheckBox.setSelected(pref.getBoolean(Properties.OBJECTCACHE_ACTIVE, true));
    }
    
    void store() {
        pref.putBoolean(Properties.OBJECTCACHE_AUTOSTART, cacheCheckBox.isSelected());
        pref.putBoolean(Properties.OBJECTCACHE_ACTIVE, useCacheCheckBox.isSelected());
    }
    
    boolean valid() {
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cacheCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox useCacheCheckBox;
    // End of variables declaration//GEN-END:variables
}
