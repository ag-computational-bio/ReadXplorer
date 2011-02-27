package de.cebitec.vamp.ui.visualisation.actions;

import de.cebitec.vamp.api.cookies.CloseTrackCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public final class CloseTrackAction implements ActionListener {

    private final List<CloseTrackCookie> context;

    public CloseTrackAction(List<CloseTrackCookie> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (CloseTrackCookie closeCookie : context) {
            closeCookie.close();
        }
    }
    
}
