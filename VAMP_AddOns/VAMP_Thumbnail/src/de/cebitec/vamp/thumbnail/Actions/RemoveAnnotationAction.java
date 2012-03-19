package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class RemoveAnnotationAction implements ActionListener {

    private final RemoveCookie context;

    public RemoveAnnotationAction(RemoveCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.removeTracks();
    }
}
