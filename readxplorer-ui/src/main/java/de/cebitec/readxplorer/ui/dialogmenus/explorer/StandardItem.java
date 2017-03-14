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

package de.cebitec.readxplorer.ui.dialogmenus.explorer;


import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;


/**
 * A standard item to use for explorers. It knows, whether this item is
 * selected.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class StandardItem implements ItemI {

    private Boolean selected;


    /**
     * A standard item to use for explorers. It knows, whether this item is
     * selected.
     */
    public StandardItem() {
        this.selected = false;
    }


    /**
     * @return <code>true</code>, if this item is selected, <code>false</code>
     *         otherwise
     */
    public Boolean getSelected() {
        return selected;
    }


    /**
     * @param selected <code>true</code>, if this item is selected,
     *                 <code>false</code>
     *                 otherwise
     */
    public void setSelected( Boolean selected ) {
        this.selected = selected;
    }


    /**
     * Selects or deselects all nodes in the explorer, depending on the given
     * parameter.
     * <p>
     * @param outlineView The outline view in which the
     *                    <code>StandardItem</code>s are used
     * @param nodes       the array of nodes whose selection shall be updated
     * @param selected    true, if all nodes shall be selected, false otherwise
     */
    public static void setSelectionOfAllItems( OutlineView outlineView, Node[] nodes, boolean selected ) {
        for( int i = 0; i < nodes.length; ++i ) {
            StandardItem item = StandardNode.getItemForNode( nodes[i] );
            if( item != null ) {
                item.setSelected( selected );
            }
            StandardItem.setSelectionOfAllItems( outlineView, nodes[i].getChildren().getNodes(), selected );
        }
        outlineView.repaint();
    }


}
