package de.cebitec.vamp.view.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.cookies.ViewCookie;

public final class OpenRefGenDialogAction implements ActionListener {

    private final ViewCookie context;

    public OpenRefGenDialogAction(ViewCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.view();
    }
}
