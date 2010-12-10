package de.cebitec.vamp.view.actions;

import de.cebitec.vamp.cookies.CloseTrackCookie;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.JMenuItem;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public final class CloseTrackAction extends CallableSystemAction {

    private final List<CloseTrackCookie> context;

    public CloseTrackAction(List<CloseTrackCookie> context) {
        this.context = context;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem item = super.getMenuPresenter();

        item.setName(getName());
        item.setBackground(Color.red);

        return item;
    }

    @Override
    public Component getToolbarPresenter() {
        Component comp = super.getToolbarPresenter();

        comp.setName(getName());
        comp.setBackground(Color.red);

        return comp;
    }

    @Override
    public void performAction() {
        for (CloseTrackCookie closeCookie : context) {
            closeCookie.close();
        }
    }

    @Override
    public String getName() {
        return "Close all tracks";// + context.get(0).getTrackName();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
