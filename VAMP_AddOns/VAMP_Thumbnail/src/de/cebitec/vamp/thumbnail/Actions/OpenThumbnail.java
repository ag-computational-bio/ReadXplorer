package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class OpenThumbnail implements ActionListener {

    private final OpenThumbCookie openCookie;

    public OpenThumbnail(OpenThumbCookie context) {
        this.openCookie = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        openCookie.open();
    }
}
