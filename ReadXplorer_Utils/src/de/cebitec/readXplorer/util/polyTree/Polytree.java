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
package de.cebitec.readXplorer.util.polyTree;


import java.util.ArrayList;
import java.util.List;


/**
 * *************************************************************************
 * Copyright (C) 2010 by Rolf Hilker *
 * rhilker a t cebitec.uni-bielefeld.de *
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation; either version 2 of the License, or *
 * (at your option) any later version. *
 *                                                                         *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 *                                                                         *
 * You should have received a copy of the GNU General Public License *
 * along with this program; if not, write to the *
 * Free Software Foundation, Inc., *
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. *
 **************************************************************************
 */
/**
 * Defines a <tt>traversal</tt> on a <tt>polytree</tt>. Therefore it
 * defines a constructor to create a list of <tt>roots</tt>(which is a list of
 * <tt>nodes</tt>) to
 * create a <tt>tree</tt>-object. Keep in mind, that in a polytree nodes can be
 * visited multiple times during traversal. If a visitor wants to guarantee
 * visiting
 * each node only once, then the <tt>visited</tt> flag of a <tt>node</tt> can be
 * used.
 * <p>
 * @author Rolf Hilker
 */
public class Polytree implements Traversable { //, Cloneable {

    private List<Node> roots;


    /**
     * Defines a <tt>traversal</tt> on a <tt>polytree</tt>. Therefore it
     * defines a constructor to create a list of <tt>roots</tt>(which is a list
     * of <tt>nodes</tt>) to create a <tt>tree</tt>-object. Keep in mind, that
     * in a polytree nodes can be visited multiple times during traversal. If a
     * visitor wants to guarantee visiting each node only once, then the
     * <tt>visited</tt> flag of a <tt>node</tt> can be used.
     * <p>
     * @param roots the roots
     */
    public Polytree( final List<Node> roots ) {
        if( roots == null ) {
            this.roots = new ArrayList<>();
        }
        else {
            this.roots = roots;
        }
    }


    /**
     * Constructor to create a <tt>polytree</tt>.
     */
    public Polytree() {
        this.roots = new ArrayList<>();
    }


    /**
     * Checks if the tree is empty.
     * <p>
     * @return <code>true</code> if parentTree is empty, <code>false</code>
     *         otherwise.
     */
    public boolean isTreeEmpty() {
        return this.roots.isEmpty();
    }


    /**
     * Returns the roots.
     * <p>
     * @return the roots
     */
    public List<Node> getRoots() {
        return this.roots;
    }


    /**
     * Sets the roots.
     * <p>
     * @param roots the roots
     */
    public void setRoots( final List<Node> roots ) {
        this.roots = roots;
    }


    /**
     * Inherited method of the interface <tt>Traversable</tt>. Implements the
     * <tt>traversal</tt> of a Tree (means the traversal of a <tt>node</tt>,
     * e.g. the <tt>roots</tt> in top down fashion.
     * <p>
     * @param nodeVisitor The visitor visiting all nodes
     */
    @Override
    public void traverse( final NodeVisitor nodeVisitor ) {
        for( Node root : this.getRoots() ) {
            root.topDown( nodeVisitor );
        }
    }


    /**
     * Goes through the tree bottom-up.
     * <p>
     * @param nodeVisitor the visitor
     */
    public void bottomUp( final NodeVisitor nodeVisitor ) {
        for( Node root : this.getRoots() ) {
            root.bottomUp( nodeVisitor );
        }
    }


    /**
     * Goes through the tree topDown.
     * <p>
     * @param nodeVisitor The visitor
     */
    public void topDown( final NodeVisitor nodeVisitor ) {
        this.traverse( nodeVisitor );
    }

//    @Override
//    public Polytree clone() {
//        return new Polytree(this.roots.clone());
//    }

}
