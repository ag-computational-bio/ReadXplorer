package de.cebitec.vamp.ui.dataAdministration.actions;

import de.cebitec.vamp.ui.importer.LoginCookie;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class OpenDataAdminDialog implements ActionListener {

    private final LoginCookie context;

    public OpenDataAdminDialog(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.openDataAdminDialog();
    }
}
