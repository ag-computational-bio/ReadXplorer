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
package de.cebitec.vamp.plotting.overlay.nodes;

import de.cebitec.vamp.plotting.api.overlay.SelectionOverlay;
import de.cebitec.vamp.plotting.api.selection.ISelection;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Nils Hoffmann
 */
public class SelectionOverlayChildFactory extends ChildFactory<ISelection> implements PropertyChangeListener {

    private final SelectionOverlay so;
    private final Set<ISelection> selection;
    private final Object source;

    public SelectionOverlayChildFactory(Object source, Set<ISelection> selection, SelectionOverlay so) {
        this.so = so;
        this.source = source;
        this.selection = selection;
        so.addPropertyChangeListener(WeakListeners.propertyChange(this, so));
    }

    @Override
    protected boolean createKeys(List<ISelection> list) {
        list.addAll(selection);
        return true;
    }

    @Override
    protected Node createNodeForKey(ISelection key) {
        try {
            SelectionNode selectionNode = new SelectionNode(key);
            FilterNode fn = new FilterNode(selectionNode, Children.LEAF, new ProxyLookup(selectionNode.getLookup(), Lookups.fixed(key)));
            return fn;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);

        }
        return Node.EMPTY;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(SelectionOverlay.PROP_SELECTION)) {
            refresh(true);
        }
    }
}
