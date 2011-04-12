/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class OpenThumbnail implements ActionListener {

    private final OpenThumbCookie openCookie;

    public OpenThumbnail(OpenThumbCookie context) {
        this.openCookie = context;
    }

    public void actionPerformed(ActionEvent ev) {
        openCookie.open();
    }
}
