package de.cebitec.vamp.ui.visualisation.actions;

import de.cebitec.vamp.ui.visualisation.cookies.CloseTrackCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public final class CloseTrackAction implements ActionListener {

    private final List<CloseTrackCookie> context;

    public CloseTrackAction(List<CloseTrackCookie> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (CloseTrackCookie closeCookie : context) {
            closeCookie.close();
        }
    }
/* methods from CallableSystemAction, but since a variable name did not work it is commented out until this works...
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
    }*/
}
