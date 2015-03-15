/**
 * Copyright 2010 Benjamin Raphael, Suzanne Sindi, Hsin-Ta Wu, Anna Ritz, Luke Peng
 *
 *  This file is part of gasv.
 * 
 *  gasv is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  gasv is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with gasv.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package gasv.main;
import gasv.geom.*;

public class Rectangle extends PolyDefault {
	
	public Rectangle(int x1, int x2, int y1, int y2){
		this.add(x1, y1);
		this.add(x2, y1);
		this.add(x2, y2);
		this.add(x1, y2);
	}
	public Poly intersection(Poly p) {
	      return Clip.intersection( p, this, this.getClass().getSuperclass() );
	}
	
	public static void main(String args[]){
		PolyDefault t = new Rectangle(10,200,10,200);
		PolyDefault t2 = new Rectangle(10,300,10,300);
		System.out.println(t.getArea());
		System.out.println(t2.getArea());
		System.out.println(t.intersection(t2));
	}
}
