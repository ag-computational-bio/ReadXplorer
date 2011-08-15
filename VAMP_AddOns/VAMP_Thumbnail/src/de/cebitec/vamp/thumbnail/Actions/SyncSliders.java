package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class SyncSliders implements ActionListener {

    private final SyncSliderCookie context;

    public SyncSliders(SyncSliderCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.syncSliders();        
    }
}
