package de.cebitec.vamp.ui.visualisation.actions;

import de.cebitec.vamp.api.cookies.CloseRefGenCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class CloseRefGenAction implements ActionListener {

    private final CloseRefGenCookie context;

    public CloseRefGenAction(CloseRefGenCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.close();
    }
}
