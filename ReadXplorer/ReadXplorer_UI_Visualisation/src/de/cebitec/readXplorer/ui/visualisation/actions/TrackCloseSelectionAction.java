package de.cebitec.readXplorer.ui.visualisation.actions;

import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author jwinneba
 */
public class TrackCloseSelectionAction extends AbstractAction implements DynamicMenuContent{

    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new AssertionError("Should never be called");
    }

    @Override
    public JComponent[] getMenuPresenters() {
        AppPanelTopComponent context = null;
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc instanceof AppPanelTopComponent) {
            context = (AppPanelTopComponent) tc;
        }

        List<Action> actions = context != null ? context.allTrackCloseActions() : new ArrayList<Action>();
        JMenu menu = new JMenu("Close specific tracks");
        for (Action a : actions) {
            menu.add(new JMenuItem(a));
        }
        return new JComponent[] { menu };
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return getMenuPresenters();
    }

}
