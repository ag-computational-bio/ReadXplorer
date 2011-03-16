/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class SynchSliders implements ActionListener {

    private final SynchSliderCookie context;

    public SynchSliders(SynchSliderCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.synchSliders();        
    }
}
