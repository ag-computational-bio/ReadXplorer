package de.cebitec.vamp.ui.visualisation.actions;

import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.windows.WindowManager;

public final class OpenRefGenViewerAction implements ActionListener {

    private final LoginCookie context;

    public OpenRefGenViewerAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WindowManager.getDefault().findTopComponent("ReferenceNavigatorTopComponent").open();
        
        AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
        appPanelTopComponent.open();
        appPanelTopComponent.requestActive();
    }
}
