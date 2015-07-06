/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.ui.dialogmenus;


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
    public void mousePressed( final MouseEvent event ) {
        if( event.isPopupTrigger() ) {
            this.openMenu( event );
        }
    }


    @Override
    public void mouseReleased( final MouseEvent event ) {
        if( event.isPopupTrigger() ) {
            this.openMenu( event );
        }
    }


    /**
     * Opens the menu.
     * <p>
     * @param event The Mouse event which triggered this method
     */
    private void openMenu( final MouseEvent event ) {
        JTextComponent parentText = (JTextComponent) event.getComponent();
        JPopupMenu menu = new JPopupMenu();
        MenuItemFactory menuItemFactory = new MenuItemFactory();

        menu.add( menuItemFactory.getCopyTextfieldItem( parentText ) );
        menu.add( menuItemFactory.getPasteItem( parentText ) );
        menu.add( menuItemFactory.getCutItem( parentText ) );
        menu.add( new JSeparator( SwingConstants.HORIZONTAL ) );
        menu.add( menuItemFactory.getSelectAllItem( parentText ) );

        menu.show( parentText, event.getX(), event.getY() );
    }


}
