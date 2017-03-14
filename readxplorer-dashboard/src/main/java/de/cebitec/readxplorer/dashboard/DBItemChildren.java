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

package de.cebitec.readxplorer.dashboard;


import de.cebitec.readxplorer.ui.dialogmenus.explorer.StandardItemChildren;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;


/**
 * Children container for DBItems.
 * <p>
 * @author jeff
 */
public class DBItemChildren extends StandardItemChildren {

    private final List<DBItem> items;


    /**
     * Children container for DBItems.
     * <p>
     * @param items the list of items to store as children
     */
    public DBItemChildren( List<DBItem> items ) {
        this.items = items;
    }


    @Override
    protected List<Node> initCollection() {
        List<Node> childrenNodes = new ArrayList<>();
        for( DBItem item : this.items ) {
            try {
                childrenNodes.add( new DBItemNode( item ) );
            } catch( IntrospectionException ex ) {
                Exceptions.printStackTrace( ex );
            }

        }
        return childrenNodes;
    }


}
