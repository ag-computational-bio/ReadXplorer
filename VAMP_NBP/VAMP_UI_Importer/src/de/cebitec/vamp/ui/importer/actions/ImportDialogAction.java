package de.cebitec.vamp.ui.importer.actions;

import de.cebitec.vamp.ui.importer.LoginCookie;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class ImportDialogAction implements ActionListener {

    private final LoginCookie context;

    public ImportDialogAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.openImporterDialog();
    }
}
