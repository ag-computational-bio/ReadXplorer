package de.cebitec.readXplorer.view.dialogMenus;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

/**
 * Class handling the mouse event for opening the basic copy, paste, cut, 
 * select all right-click menu for JTextComponents.
 *
 * @author -Rolf Hilker-
 */
public class StandardMenuEvent extends MouseAdapter {

    /**
     * Handling the mouse event for opening the basic copy, paste, cut, 
     * select all right-click menu for JTextComponents.
     */
    public StandardMenuEvent() {
        //nothing to do
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            this.openMenu(event);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            this.openMenu(event);
        }
    }

    /**
     * Opens the menu.
     * @param event The Mouse event which triggered this method
     */
    private void openMenu(final MouseEvent event) {
        JTextComponent parentText = (JTextComponent) event.getComponent();
        JPopupMenu menu = new JPopupMenu();
        MenuItemFactory menuItemFactory = new MenuItemFactory();
        
        menu.add(menuItemFactory.getCopyTextfieldItem(parentText));
        menu.add(menuItemFactory.getPasteItem(parentText));
        menu.add(menuItemFactory.getCutItem(parentText));
        menu.add(new JSeparator(SwingConstants.HORIZONTAL));
        menu.add(menuItemFactory.getSelectAllItem(parentText));
        
        menu.show(parentText, event.getX(), event.getY());
    }
}
