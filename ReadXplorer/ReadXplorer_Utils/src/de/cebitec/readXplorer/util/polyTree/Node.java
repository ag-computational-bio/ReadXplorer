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


import de.cebitec.readXplorer.util.classification.FeatureType;
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
 * Defines a <tt>node</tt> constructor to create a new <tt>node</tt> and has
 * some methods to manipulate a created <tt>node</tt>.
 * <p>
 * @author Pina Krell, Rolf Hilker
 */
public class Node implements Traversable {//, Cloneable {

    /**
     * Vector with the children of a node.
     */
    private List<Node> nodeChildren;
    /**
     * Parents of the Node.
     */
    private List<Node> parents;
    private FeatureType nodeType;
    private boolean visited = false;


    /**
     * Constructs a <tt>node</tt> with a given name, parent, an incoming edge
     * length and a key-object to be stored in the <tt>node</tt> .
     * <p>
     * @param nodeType The node type to store within the current <tt>node</tt>
     * @param parents  A list of <tt>nodes</tt> which are the parents of the
     * <tt>node</tt> to be
     * created.
     */
    public Node( final FeatureType nodeType, final List<Node> parents ) {
        this.nodeType = nodeType;
        if( parents != null ) {
            for( Node parent : parents ) {
                parent.addChild( this );
            }
        }
        else {
            this.parents = new ArrayList<>();
        }
        this.nodeChildren = new ArrayList<>();
    }

//    /**
//     * Returns the depth of the Node.
//     * @return depth the depth of the Node
//     */
//    public Integer getDepth() {
//        Node node = this;
//        Integer depth = 0;
//        while (!node.isRoot()) {
//            node = node.getParents();
//            depth++;
//        }
//        return depth;
//    }

//    /**
//     * Clones the node.
//     * @return newNode the copy of the original node.
//     */
//    @Override
//    public Node clone() {
//
//        Node newNode;
//        if (this.nodeType == null) {
//            newNode = new Node(null, null);
//        } else {
//            newNode = new Node(this.nodeType, null);
//        }
//        if (!this.isLeaf()) {
//            for (Node n : this.getNodeChildren()) {
//                newNode.getNodeChildren().add(n.clone());
//                newNode.getNodeChildren().get(newNode.getNodeChildren().size() - 1).parents.add(newNode);
//            }
//        }
//        return newNode;
//    }
    /**
     * Bottom up through the tree. Does not set the visited flag for any nodes!
     * The node visitor has to take care of this behaviour. But nodes and their
     * corresponding subtree, whose visited flag is set are not visited anymore.
     * <p>
     * @param nodeVisitor the nodeVisitor
     */
    public void bottomUp( final NodeVisitor nodeVisitor ) {
        if( !visited ) { //since the whole subtree has already been visited in this case
            for( Node nodeChild : this.nodeChildren ) {
                nodeChild.bottomUp( nodeVisitor );
            }
            nodeVisitor.visit( this );
        }
    }


    /**
     * Top down through the tree. Does not set the visited flag for any nodes!
     * The node visitor has to take care of this behaviour. But nodes and their
     * corresponding subtree, whose visited flag is set are not visited anymore.
     * <p>
     * @param nodeVisitor the nodeVisitor
     */
    public void topDown( final NodeVisitor nodeVisitor ) {
        this.traverse( nodeVisitor );
    }


    /**
     * Traverses a <tt>node</tt> with different visitors in top down fashion.
     * <p>
     * @param nodeVisitor Visitor-type with which the <tt>node</tt> should be
     *                    traversed. Does not set the visited flag for any nodes! The node visitor
     *                    has to take care of this behaviour. But nodes and their corresponding
     *                    subtree,
     *                    whose visited flag is set are not visited anymore.
     */
    @Override
    public void traverse( final NodeVisitor nodeVisitor ) {
        if( !visited ) {
            // visits the actual node
            nodeVisitor.visit( this );
            // calls the method traverse on all children of the current node
            for( Node nodeChild : this.nodeChildren ) {
                nodeChild.traverse( nodeVisitor );
            }
        }
    }


    /**
     * Adds a new child (also a <tt>node</tt>) to a <tt>nodes</tt> vector of
     * children and also sets adds this node to the parents list of the
     * <tt>newChild</tt>.
     * <p>
     * @param newChild The <tt>node</tt> to be set as a new child
     */
    public void addChild( final Node newChild ) {
        this.nodeChildren.add( newChild );
        newChild.getParents().add( this );
    }


    /**
     * Deletes all children (<tt>nodes</tt>) of a <tt>node</tt>.
     */
    public void clearChildren() {
        this.nodeChildren.clear();
    }


    /**
     * Parses the object stored in a <tt>node</tt> to String.
     * <p>
     * @return key The key object parsed to a string
     */
    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        if( !this.isLeaf() ) {
            out.append( '(' );
            for( Node nodeChild : this.nodeChildren ) {
                out.append( nodeChild.toString() ).append( "," );
            }
            out.deleteCharAt( out.length() - 1 );
            out.append( ")" );
        }
        return out.toString();
    }


    /**
     * Getter for the parents of a <tt>node</tt>.
     * <p>
     * @return The parents <tt>node</tt> of a <tt>node</tt>
     */
    public List<Node> getParents() {
        return this.parents;
    }


    /**
     * Boolean whether a <tt>node</tt> is a <tt>leaf</tt> or not. A
     * <tt>leaf</tt> does not contain children (<tt>nodes</tt>).
     * <p>
     * @return true if the <tt>node</tt> is a leaf
     */
    public boolean isLeaf() {
        return (this.nodeChildren.isEmpty());
    }


    /**
     * Boolean whether a <tt>node</tt> is a <tt>root</tt> of a tree or not.
     * Means that the <tt>node</tt> has no parents.
     * <p>
     * @return true if the <tt>node</tt> is the <tt>root</tt>
     */
    public boolean isRoot() {
        return this.parents.isEmpty();
    }


    /**
     * Sets a specific key-object to store within the actual <tt>node</tt>.
     * <p>
     * @param nodeType Node type to store within the <tt>node</tt>
     */
    public void setNodeType( final FeatureType nodeType ) {
        this.nodeType = nodeType;
    }


    /**
     * Getter for the key-object which is stored in the <tt>node</tt>.
     * <p>
     * @return key A key object which is stored in the <tt>node</tt>
     */
    public FeatureType getNodeType() {
        return this.nodeType;
    }


    /**
     * Getter for the vector of children-nodes of the actual <tt>node</tt>.
     * <p>
     * @return nodeChildren The vector of children of a <tt>node</tt>
     */
    public List<Node> getNodeChildren() {
        return this.nodeChildren;
    }


    /**
     * Sets a vector of children (<tt>nodes</tt>) to a <tt>node</tt>.
     * <p>
     * @param children A vector of children-nodes
     */
    public void setNodeChildren( final List<Node> children ) {
        this.nodeChildren = children;
    }


    /**
     * @return <code>true</code>, if this node was already visited in the
     *         current traversal.
     */
    public boolean isVisited() {
        return visited;
    }


    /**
     * Sets whether this node was already visited in the current traversal.
     * <p>
     * @param visited <code>true</code>, if this node was already visited in the
     *                current traversal
     */
    public void setVisited( boolean visited ) {
        this.visited = visited;
    }


}
