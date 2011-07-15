package de.cebitec.vamp.thumbnail.Actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class AsyncSliders implements ActionListener {

    private final ASyncSliderCookie context;

    public AsyncSliders(ASyncSliderCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.async();
    }
}
