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
 * Defines an <tt>interface</tt> with methods to traverse a <tt>tree struktur</tt> i.e. the
 * specific <tt>nodes</tt> of a tree. 
 * Implementation via <tt>Visitor Pattern</tt>, this could be seen as the "Visitable" interface which 
 * defines the method "traverse" to determine which objects call back the visit method from 
 * the visitor class.
 * Also see the interface "NodeVisitor" which is implemented as the "Visitor" of the <tt>Visitor Pattern</tt>
 * @author Pina Krell
 */
public interface Traversable {

	/**
	 * Determines which objects call back the visit method of the visitor classes.
	 * @param nodeVisitor The visitor class which is used at the moment in inherited classes
	 * to traverse the traversable object with. 
	 */
	void traverse(NodeVisitor nodeVisitor);
	
}
