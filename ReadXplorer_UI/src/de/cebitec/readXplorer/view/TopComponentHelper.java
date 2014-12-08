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

package de.cebitec.readXplorer.view;


import de.cebitec.readXplorer.util.TabWithCloseX;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Class containing helper methods around TopComponents.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TopComponentHelper {

    /**
     * Fetches the first <cc>TopComponent</cc>, which is currently visible on
     * the screen and of the given subclass instance handed over to the method.
     * <p>
     * @param <T>                Class type of the TopComponent
     * @param activeTopCompToGet the specific subclass of <cc>TopComponent</cc>
     * which is desired.
     * <p>
     * @return The first <cc>TopComponent</cc>, which is currently visible on
     *         the screen and of the given subclass instance handed over to the method.
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T getActiveTopComp( Class<? extends TopComponent> activeTopCompToGet ) {
        //Get all open Components and filter for AppPanelTopComponent
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        T desiredTopComp = null;
        for( TopComponent topComponent : topComps ) {
            if( topComponent.getClass().isAssignableFrom( activeTopCompToGet ) && topComponent.isShowing() ) {
                desiredTopComp = (T) topComponent;
                break;
            }
        }
        return desiredTopComp;
    }


    /**
     * Fetches all <cc>TopComponent</cc>s of the given subclass instance handed
     * over to the method, which are currently available.
     * <p>
     * @param <T>               Class type of the TopComponent
     * @param topCompClassToGet the specific subclass of <cc>TopComponent</cc>
     * which is desired.
     * <p>
     * @return All <cc>TopComponent</cc>s of the given subclass instance handed
     *         over to the method, which are currently available.
     */
    @SuppressWarnings( { "unchecked" } )
    public static <T> List<T> getTopComps( Class<? extends TopComponent> topCompClassToGet ) {
        List<T> topCompsOfClass = new ArrayList<>();
        //Get all open Components and filter for AppPanelTopComponent
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        for( TopComponent topComponent : topComps ) {
            if( topComponent.getClass().isAssignableFrom( topCompClassToGet ) ) {
                topCompsOfClass.add( (T) topComponent );
            }
        }
        return topCompsOfClass;
    }


    /**
     * @return The array of all currently opened TopComponents.
     */
    public static TopComponent[] getAllOpenedTopComponents() {
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        TopComponent[] topCompArray = new TopComponent[topComps.size()];
        return topComps.toArray( topCompArray );
    }


    /**
     * Sets up a container listener, which assures that the TopComponent with
     * the given preferredId is closed when no tabs are shown.
     * <p>
     * @param tabs        the JTabbedPane containing the tabs
     * @param preferredId the id of the corresponding TopCopmonent
     */
    public static void setupContainerListener( final JTabbedPane tabs, final String preferredId ) {

        // add listener to close TopComponent when no tabs are shown
        tabs.addContainerListener( new ContainerListener() {
            @Override
            public void componentAdded( ContainerEvent e ) {
            }


            @Override
            public void componentRemoved( ContainerEvent e ) {
                if( tabs.getTabCount() == 0 ) {
                    TopComponent topComp = WindowManager.getDefault().findTopComponent( preferredId );
                    if( topComp != null ) {
                        topComp.close();
                    }
                }
            }


        } );
    }


    /**
     * This method needs to be called in order to open a new tab for a table.
     * <p>
     * @param tabs       JTabbedPane containing the tabs
     * @param panelName  name of the panel to open
     * @param tablePanel the panel to display in the new tab
     */
    public static void openTableTab( JTabbedPane tabs, String panelName, JPanel tablePanel ) {
        tabs.addTab( panelName, tablePanel );
        tabs.setTabComponentAt( tabs.getTabCount() - 1, new TabWithCloseX( tabs ) );
        tabs.setSelectedIndex( tabs.getTabCount() - 1 );
    }


}
