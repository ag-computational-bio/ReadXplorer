/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.verifier;


import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;


/**
 *
 * @author jritter
 */
public class DoubleVerifier extends InputVerifier {

    private final JTextField tf;
    private final Border border;


    public DoubleVerifier( JComponent comp ) {
        this.tf = (JTextField) comp;
        this.border = tf.getBorder();
    }


    @Override
    public boolean verify( JComponent input ) {
        boolean returnValue = true;
        JTextField textField = (JTextField) input;
        String content = textField.getText();
        if( content.length() != 0 ) {
            try {
                Double.parseDouble( textField.getText() );
                this.tf.setBorder( border );
            }
            catch( NumberFormatException e ) {
                Toolkit.getDefaultToolkit().beep();
                this.tf.setBorder( BorderFactory.createLineBorder( Color.red ) );
                returnValue = false;
            }
        }
        return returnValue;
    }


}
