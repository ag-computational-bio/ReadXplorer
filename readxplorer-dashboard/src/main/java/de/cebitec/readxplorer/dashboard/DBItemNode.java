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


import de.cebitec.readxplorer.ui.dialogmenus.explorer.StandardNode;
import java.beans.IntrospectionException;
import javax.swing.Action;


/**
 * A DBItemNode represents a BeanNode of a DBItem, which is designed for a track
 * or a genome.
 *
 * @author jeff
 */
public class DBItemNode extends StandardNode {

    /**
     * A DBItemNode represents a BeanNode of a DBItem, which is designed for a
     * track or a genome.
     * <p>
     * @param bean     the DBItem to associate with this node
     * @param children the children of the given <cc>bean</cc>
     * <p>
     * @throws IntrospectionException
     */
    public DBItemNode( DBItem bean, DBItemChildren children ) throws IntrospectionException {
        super( bean, children );
        setDisplayName( bean.getTitle() );
        setShortDescription( bean.getDescription() );
        setIconBaseWithExtension( "de/cebitec/readxplorer/ui/visualisation/refOpen.png" );
    }


    /**
     * A DBItemNode represents a BeanNode of a DBItem, which is designed for a
     * track or a genome.
     * <p>
     * @param bean the DBItem to associate with this node
     * <p>
     * @throws IntrospectionException
     */
    public DBItemNode( DBItem bean ) throws IntrospectionException {
        super( bean );
        setDisplayName( bean.getTitle() );
        setShortDescription( bean.getDescription() );
        setIconBaseWithExtension( "de/cebitec/readxplorer/ui/visualisation/trackOpen.png" );


    //public DBItemNode(DBItem bean, Children children, Lookup lkp) throws IntrospectionException {
        /*if (children==null) {
         super(bean);
         }
         else {
         super(bean, children);
         }*/


        //super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        //this.movie = key;
        //setDisplayName(key.getTitle());
    }


    @Override
    public boolean canCut() {
        return true;
    }


    @Override
    public boolean canDestroy() {
        return true;
    }


    @Override
    public Action[] getActions( boolean popup ) {
        return new Action[]{ /*SystemAction.get( CopyAction.class ),
         SystemAction.get( CutAction.class ),
         null,
         SystemAction.get( DeleteAction.class ) */};
    }


}
