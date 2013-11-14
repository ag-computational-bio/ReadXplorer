package de.cebitec.readXplorer.ui.visualisation.actions;

import de.cebitec.readXplorer.api.cookies.LoginCookie;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action, which opens a new AppPanelTopComponent for displaying a reference
 * sequence and the tracks belonging to that reference.
 */
public final class OpenRefGenViewerAction implements ActionListener {

    private final LoginCookie context;

    /**
     * Action, which opens a new AppPanelTopComponent for displaying a reference
     * sequence and the tracks belonging to that reference.
     */
    public OpenRefGenViewerAction(LoginCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
        appPanelTopComponent.open();
        ViewController vc = appPanelTopComponent.getLookup().lookup(ViewController.class);
        boolean canOpenRefViewer = vc.openRefGen();
        if (canOpenRefViewer) {
            appPanelTopComponent.setName(vc.getDisplayName());
            appPanelTopComponent.requestActive();
        } else {
            appPanelTopComponent.close();
        }
    }
}
