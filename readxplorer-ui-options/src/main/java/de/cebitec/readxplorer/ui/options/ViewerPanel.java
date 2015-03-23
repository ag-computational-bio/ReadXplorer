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

package de.cebitec.readxplorer.ui.options;


import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Properties;
import java.util.prefs.Preferences;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.util.NbPreferences;


/**
 * Panel for configuring the options of viewers.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class ViewerPanel extends OptionsPanel {

    private static final long serialVersionUID = 1L;

    private final ViewerOptionsPanelController controller;
    private final Preferences pref;

    private int maxZoom;


    /**
     * Panel for configuring the options of viewers.
     * <p>
     * @param controller The controller of the panel
     */
    public ViewerPanel( ViewerOptionsPanelController controller ) {
        this.controller = controller;
        this.pref = NbPreferences.forModule( Object.class );
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JLabel();
        autoScalingBox = new javax.swing.JCheckBox();
        viewerSizeSlider = new javax.swing.JSlider();
        jSeparator1 = new javax.swing.JSeparator();
        maxZoomLevelField = new javax.swing.JTextField();
        maxZoomLevelLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.descriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoScalingBox, org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.autoScalingBox.text")); // NOI18N

        viewerSizeSlider.setMajorTickSpacing(30);
        viewerSizeSlider.setMaximum(Properties.MAX_HEIGHT);
        viewerSizeSlider.setMinimum(Properties.MIN_HEIGHT);
        viewerSizeSlider.setMinorTickSpacing(5);
        viewerSizeSlider.setPaintLabels(true);
        viewerSizeSlider.setPaintTicks(true);
        viewerSizeSlider.setSnapToTicks(true);
        viewerSizeSlider.setToolTipText(org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.viewerSizeSlider.toolTipText")); // NOI18N
        viewerSizeSlider.setValue(200);

        maxZoomLevelField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxZoomLevelField.setText(org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.maxZoomLevelField.text")); // NOI18N
        maxZoomLevelField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                maxZoomLevelFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(maxZoomLevelLabel, org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.maxZoomLevelLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ViewerPanel.class, "ViewerPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(2, 2, 2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewerSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(autoScalingBox)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maxZoomLevelLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(maxZoomLevelField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewerSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autoScalingBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxZoomLevelField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxZoomLevelLabel))
                .addContainerGap(42, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void maxZoomLevelFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxZoomLevelFieldKeyTyped
        valid();
    }//GEN-LAST:event_maxZoomLevelFieldKeyTyped


    @Override
    void load() {
        this.viewerSizeSlider.setValue( pref.getInt( Properties.VIEWER_HEIGHT, Properties.DEFAULT_HEIGHT ) );
        this.autoScalingBox.setSelected( pref.getBoolean( Properties.VIEWER_AUTO_SCALING, false ) );
        maxZoom = pref.getInt( Properties.MAX_ZOOM, Properties.DEFAULT_ZOOM );
        this.maxZoomLevelField.setText( String.valueOf( maxZoom ) );
    }


    @Override
    void store() {
        pref.putInt( Properties.VIEWER_HEIGHT, this.viewerSizeSlider.getValue() );
        pref.putBoolean( Properties.VIEWER_AUTO_SCALING, this.autoScalingBox.isSelected() );
        pref.putInt( Properties.MAX_ZOOM, maxZoom );
    }


    @Override
    public boolean valid() {
        boolean isValid = false;
        if( GeneralUtils.isValidRangeInput( maxZoomLevelField.getText(), 1, 500 ) ) {
            maxZoom = Integer.parseInt( maxZoomLevelField.getText() );
            isValid = true;
        }
        return isValid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoScalingBox;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField maxZoomLevelField;
    private javax.swing.JLabel maxZoomLevelLabel;
    private javax.swing.JSlider viewerSizeSlider;
    // End of variables declaration//GEN-END:variables


    private HyperlinkListener getHyperlinkListener() {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate( HyperlinkEvent e ) {
//                if( HyperlinkEvent.EventType.ACTIVATED.equals( e.getEventType() ) ) {
//                    HelpCtx.setHelpIDString(jEditorPane1, e.getURL().toString());
//                    HelpCtx help = new HelpCtx(e.getURL().toString());
//                    help.display();
//                }
            }


        };
    }


}
