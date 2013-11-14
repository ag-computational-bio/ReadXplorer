package de.cebitec.readXplorer.thumbnail.Actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
