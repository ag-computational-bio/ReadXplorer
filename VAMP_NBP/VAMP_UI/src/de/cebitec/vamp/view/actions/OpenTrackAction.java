package de.cebitec.vamp.view.actions;

import de.cebitec.vamp.cookies.OpenTrackCookie;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

public final class OpenTrackAction implements ActionListener {

    private final List<OpenTrackCookie> context;

    public OpenTrackAction(List<OpenTrackCookie> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (OpenTrackCookie openCookie : context) {
            openCookie.open();
        }
    }
}
