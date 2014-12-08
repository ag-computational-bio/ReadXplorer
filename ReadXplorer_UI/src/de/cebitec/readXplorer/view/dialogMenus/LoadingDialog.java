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

package de.cebitec.readXplorer.view.dialogMenus;


import java.awt.Frame;
import java.awt.IllegalComponentStateException;


/**
 * Displays a large loading progress bar.
 *
 * @author kstaderm
 */
public class LoadingDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;

    private final Frame parent;


    /**
     * Displays a large loading progress bar.
     * <p>
     * @param parent the parent, which shall be blocked during the loading
     *               process.
     */
    public LoadingDialog( Frame parent ) {
        super( parent, false );
        this.parent = parent;
        this.showLoadingIcon();
    }


    /**
     * Displays a large loading progress bar.
     */
    private void showLoadingIcon() {
        parent.setEnabled( false );
        this.setUndecorated( true );
        initComponents();
        int x = ((parent.getWidth() - 400) / 2) + parent.getX();
        int y = ((parent.getHeight() - 42) / 2) + parent.getY();
        this.setLocation( x, y );
        this.requestFocus();
        this.getRootPane().setOpaque( false );
        this.getContentPane().setBackground( new java.awt.Color( 0, 0, 0, 0 ) );
        try {
            this.setBackground( new java.awt.Color( 0, 0, 0, 0 ) );
        }
        catch( UnsupportedOperationException | IllegalComponentStateException e ) {
            //do nothing, just ignore command
        }
        this.setVisible( true );
    }


    /**
     * Call this method, when the parent can be enabled again and the loading
     * is finished.
     */
    public void finished() {
        parent.setEnabled( true );
        this.dispose();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jProgressBar1.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
