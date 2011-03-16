/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class AsynchSliders implements ActionListener {

    private final ASynchSliderCookie context;

    public AsynchSliders(ASynchSliderCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.asynch();
    }
}
