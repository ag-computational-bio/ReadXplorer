package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class RemoveFeatureAction implements ActionListener {

    private final RemoveCookie context;

    public RemoveFeatureAction(RemoveCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.removeTracks();
    }
}
