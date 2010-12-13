package de.cebitec.vamp.ui.visualisation.actions;

import de.cebitec.vamp.ui.visualisation.cookies.OpenRefGenCookie;
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
