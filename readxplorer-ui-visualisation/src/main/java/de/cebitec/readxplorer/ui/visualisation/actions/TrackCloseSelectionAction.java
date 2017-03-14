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

package de.cebitec.readxplorer.ui.visualisation.actions;


import de.cebitec.readxplorer.ui.visualisation.AppPanelTopComponent;
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
public class TrackCloseSelectionAction extends AbstractAction implements
        DynamicMenuContent {

    private static final long serialVersionUID = 1L;


    @Override
    public void actionPerformed( ActionEvent e ) {
        throw new AssertionError( "Should never be called" );
    }


    @Override
    public JComponent[] getMenuPresenters() {
        AppPanelTopComponent context = null;
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if( tc instanceof AppPanelTopComponent ) {
            context = (AppPanelTopComponent) tc;
        }

        List<Action> actions = context != null ? context.allTrackCloseActions() : new ArrayList<Action>();
        JMenu menu = new JMenu( "Close specific tracks" );
        for( Action a : actions ) {
            menu.add( new JMenuItem( a ) );
        }
        return new JComponent[]{ menu };
    }


    @Override
    public JComponent[] synchMenuPresenters( JComponent[] items ) {
        return getMenuPresenters();
    }


}
