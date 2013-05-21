/*
 * Maui, Maltcms User Interface. 
 * Copyright (C) 2008-2012, The authors of Maui. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maui may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maui, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maui is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package de.cebitec.vamp.plotting.overlay.ui;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Nils Hoffmann
 */
//@NavigatorPanel.Registration(mimeType = "application/jfreechart+overlay", displayName = "Chart Overlay")
public class OverlayNavigatorPanel implements NavigatorPanel {

    /**
     * holds UI of this panel
     */
    private OverlayNavigatorPanelUI panelUI;
    private Lookup.Result<Node> selectedNodesResult;
    private Collection<? extends Node> selectedNodes;
    /**
     * template for finding data in given context. Object used as example,
     * replace with your own data source, for example JavaDataObject etc
     */
    private static final Lookup.Template<Node> MY_DATA = new Lookup.Template(Node.class);
    /**
     * current context to work on
     */
    private Lookup.Result<Node> curContext;
//    private Lookup lookup;
    /**
     * listener to context changes
     */
    private LookupListener contextL;

    /**
     * public no arg constructor needed for system to instantiate provider well
     */
    public OverlayNavigatorPanel() {

    }

    @Override
    public String getDisplayHint() {
        return "Displays overlay layers for chart";
    }

    @Override
    public String getDisplayName() {
        return "Chart Overlay Layers";
    }

    @Override
    public JComponent getComponent() {
        if (panelUI == null) {
            panelUI = new OverlayNavigatorPanelUI();
            // You can override requestFocusInWindow() on the component if desired.
        }
        return panelUI;
    }

    @Override
    public void panelActivated(Lookup context) {
        System.out.println("Received lookup: " + context);
        // lookup context and listen to result to get notified about context changes
        curContext = context.lookup(MY_DATA);
        curContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection<? extends Node> data = curContext.allInstances();
        setNewContent(data);
    }

    @Override
    public void panelDeactivated() {
        curContext.removeLookupListener(getContextListener());
        curContext = null;
    }

    @Override
    public Lookup getLookup() {
        // go with default activated Node strategy
//        return null;
//        return null;
        return ((OverlayNavigatorPanelUI)getComponent()).getLookup();
    }

    /**
     * *********** non - public part ***********
     */
    private void setNewContent(Collection<? extends Node> newData) {
        // put your code here that grabs information you need from given
        // collection of data, recompute UI of your panel and show it.
        // Note - be sure to compute the content OUTSIDE event dispatch thread,
        // just final repainting of UI should be done in event dispatch thread.
        // Please use RequestProcessor and Swing.invokeLater to achieve this.
        System.out.println("Received " + newData.size() + " overlays!");
        if (!newData.isEmpty()) {
            ((OverlayNavigatorPanelUI) getComponent()).setContent(newData);
        }
    }

    /**
     * Accessor for listener to context
     */
    private LookupListener getContextListener() {
        if (contextL == null) {
            contextL = new ContextListener();
        }
        return contextL;
    }

    /**
     * Listens to changes of context and triggers proper action
     */
    private class ContextListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            System.out.println("Received result changed for ChartOverlay from " + ev.getSource());
            Collection<? extends Node> data = curContext.allInstances();
            if (curContext != null && !curContext.allInstances().isEmpty()) {
                setNewContent(data);
            }
        }
    } // end of ContextListener
}
