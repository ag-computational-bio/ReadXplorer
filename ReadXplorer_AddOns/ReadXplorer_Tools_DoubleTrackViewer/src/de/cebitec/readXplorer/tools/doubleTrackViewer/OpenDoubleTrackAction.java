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
package de.cebitec.readXplorer.tools.doubleTrackViewer;

import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readXplorer.view.TopComponentHelper;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
        id = "de.cebitec.readXplorer.tools.doubleTrackViewer.OpenDoubleTrackAction")
@ActionRegistration(iconBase = "de/cebitec/readXplorer/tools/doubleTrackViewer/doubleTrack.png",
        displayName = "#CTL_OpenDoubleTrackAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 162),
    @ActionReference(path = "Toolbars/Tools", position = 287)
})
@NbBundle.Messages("CTL_OpenDoubleTrackAction=Double Track Viewer")
public final class OpenDoubleTrackAction implements ActionListener {

    private final ReferenceViewer context;

    public OpenDoubleTrackAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        AppPanelTopComponent appComp = TopComponentHelper.getActiveTopComp(AppPanelTopComponent.class);
        if (appComp != null) {
            //Get ViewController from AppPanelTopComponent-Lookup
            ViewController viewCon = appComp.getLookup().lookup(ViewController.class);
            viewCon.openDoubleTrack();
        }
    }
}
