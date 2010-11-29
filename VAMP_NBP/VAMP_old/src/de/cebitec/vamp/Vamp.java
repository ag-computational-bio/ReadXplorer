package de.cebitec.vamp;

import de.cebitec.vamp.controller.ApplicationController;
import javax.swing.SwingUtilities;

/**
 *
 * @author ddoppmeier
 */
public class Vamp {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationController.getInstance();
            }
        });
    }

}
