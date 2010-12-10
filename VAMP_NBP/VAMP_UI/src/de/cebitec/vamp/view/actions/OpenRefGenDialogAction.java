package de.cebitec.vamp.view.actions;

import de.cebitec.vamp.cookies.OpenRefGenCookie;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class OpenRefGenDialogAction implements ActionListener {

    private final OpenRefGenCookie context;

    public OpenRefGenDialogAction(OpenRefGenCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.open();
    }
}
