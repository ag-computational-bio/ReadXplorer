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


import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.visualisation.AppPanelTopComponent;
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
    public OpenRefGenViewerAction( LoginCookie context ) {
        this.context = context;
    }


    @Override
    public void actionPerformed( ActionEvent ev ) {
        AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
        appPanelTopComponent.open();
        ViewController vc = appPanelTopComponent.getLookup().lookup( ViewController.class );
        boolean canOpenRefViewer = vc.openRefGen();
        if( canOpenRefViewer ) {
            appPanelTopComponent.setName( vc.getDisplayName() );
            appPanelTopComponent.requestActive();
        } else {
            appPanelTopComponent.close();
        }
    }


}
