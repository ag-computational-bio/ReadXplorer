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

import de.cebitec.vamp.plotting.api.overlay.ChartOverlay;
import java.awt.Image;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.openide.explorer.view.CheckableNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Nils Hoffmann
 */
public class OverlayNode<T extends ChartOverlay> extends BeanNode<T> implements CheckableNode {

    private boolean checkable = true;
    private boolean checkEnabled = true;
//    private boolean selected = false;

    public OverlayNode(T bean) throws IntrospectionException {
        super(bean);
    }

    public OverlayNode(T bean, Children children) throws IntrospectionException {
        super(bean, children);
    }

    public OverlayNode(T bean, Children children, Lookup lkp) throws IntrospectionException {
        super(bean, children, lkp);
    }

    @Override
    public String getName() {
        return getBean().getName();
    }

    @Override
    public String getDisplayName() {
        return getBean().getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return getBean().getShortDescription();
    }

    @Override
    public boolean isCheckable() {
        return checkable;
    }

    @Override
    public boolean isCheckEnabled() {
        return checkEnabled;
    }

    @Override
    public Boolean isSelected() {
        return getBean().isVisible();
    }

    @Override
    public void setSelected(Boolean selected) {
        getBean().setVisible(selected);
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions(context);
        List<? extends Action> selectionActions = Utilities.actionsForPath("Actions/OverlayNodeActions");
        List<Action> nodeActions = new ArrayList<Action>(selectionActions);
        nodeActions.add(null);
        nodeActions.addAll(Arrays.asList(actions));
        return nodeActions.toArray(new Action[nodeActions.size()]);
    }
    
    @Override
    public Image getIcon(int type) {
        if(isSelected()) {
            return ImageUtilities.loadImage(
                "net/sf/maltcms/common/charts/resources/SelectionVisible.png");
        }else{
            return ImageUtilities.loadImage(
                "net/sf/maltcms/common/charts/resources/SelectionHidden.png");
        }
    }
}
