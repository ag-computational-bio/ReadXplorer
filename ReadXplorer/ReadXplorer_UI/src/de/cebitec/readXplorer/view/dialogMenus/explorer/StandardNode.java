/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.dialogMenus.explorer;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;

/**
 * A StandardNode for use in the explorer. It allows to add any StandardItems
 * and StandardItemChildren to the node and can return its item.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class StandardNode extends BeanNode<StandardItem> {

    /**
     * A StandardNode for use in the explorer. It allows to add any
     * StandardItems and StandardItemChildren to the node and can return its
     * item.
     * @param bean the StandardItems to associate with this node
     * @param children the children of the given <cc>bean</cc>
     * @throws IntrospectionException
     */
    public StandardNode(StandardItem bean, StandardItemChildren children) throws IntrospectionException {
        super(bean, children);
    }

    /**
     * A StandardNode for use in the explorer. It allows to add any
     * StandardItems to the node and can return its item.
     * @param bean the StandardItems to associate with this node
     * @throws IntrospectionException
     */
    public StandardNode(StandardItem bean) throws IntrospectionException {
        super(bean);
    }
    
    /**
     * @return The item associated with this node.
     */
    public StandardItem getData() {
        return this.getBean();
    }
    
    /**
     * Iterates through all given nodes and their children and returns only
     * those with getSelected() == true.
     * Any nodes, which are not of type StandardNode are ignored.
     * @param nodes list of StandardNodes to check for selected StandardNodes
     * @return All selected nodes from the given node list
     */
    public static List<Node> getAllMarkedNodes(List<Node> nodes) {
        List<Node> selectedNodes = new ArrayList<>();
        for (Node n : nodes) {
            if (n instanceof StandardNode) {
                StandardNode node = (StandardNode) n;
                StandardItem item = node.getData();
                if (item.getSelected()) {
                    selectedNodes.add(n);
                }
                List<Node> markedChildren = Arrays.asList(n.getChildren().getNodes());
                selectedNodes.addAll(getAllMarkedNodes(markedChildren));
            }
        }
        return selectedNodes;
    }
    
}
