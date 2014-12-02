/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.verifier;


import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;


/**
 *
 * @author jritter
 */
public class IntegerKeyVerifier extends KeyAdapter {

    private JTextField tf;
    private Border border;


    public IntegerKeyVerifier( JTextField tf ) {
        this.tf = tf;
        this.border = tf.getBorder();
    }


    @Override
    public void keyTyped( KeyEvent e ) {
        char c = e.getKeyChar();
        if( !((c >= '0') && (c <= '9')
              || (c == KeyEvent.VK_BACK_SPACE)
              || (c == KeyEvent.VK_DELETE)) ) {
            e.consume();
            tf.setBorder( BorderFactory.createLineBorder( Color.red ) );
        }
        else {
            tf.setBorder( this.border );
        }
    }


}
