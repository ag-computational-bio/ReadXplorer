package de.cebitec.vamp.util.polyTree;

/***************************************************************************
 *   Copyright (C) 2010 by Rolf Hilker                                     *
 *   rhilker   a t  cebitec.uni-bielefeld.de                               *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

/**
 * Defines an <tt>interface</tt> with methods to visit a <tt>node</tt>
 * Implementation via <tt>Visitor Pattern</tt>, this could be seen as the
 * "Visitor" interface which defines the method "visit" which is called back by
 * objects which inherit from the "Visitable" interface. Also see the interface
 * "Traversable" which is implemented as the "Visitable" interface of the
 * <tt>Visitor Pattern</tt>.
 * 
 * @author pkrell
 */
public interface NodeVisitor {

    /**
     * Method to visit a <tt>node</tt> and do something with it.
     * 
     * @param node
     *            The <tt>node</tt> to visit and do something with
     */
    void visit(Node node);

}