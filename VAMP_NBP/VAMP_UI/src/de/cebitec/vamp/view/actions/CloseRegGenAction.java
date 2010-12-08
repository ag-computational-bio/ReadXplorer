package de.cebitec.vamp.view.actions;

import de.cebitec.vamp.cookies.CloseRefGenCookie;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class CloseRegGenAction implements ActionListener {

    private final CloseRefGenCookie context;

    public CloseRegGenAction(CloseRefGenCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.close();
    }
}
