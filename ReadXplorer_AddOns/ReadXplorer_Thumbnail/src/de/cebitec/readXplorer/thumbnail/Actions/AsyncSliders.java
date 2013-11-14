package de.cebitec.readXplorer.thumbnail.Actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
