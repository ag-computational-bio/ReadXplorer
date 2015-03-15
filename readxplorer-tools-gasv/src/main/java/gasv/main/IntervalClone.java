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
public class IntervalClone extends Clone {

	private boolean isSame_;

	/**
	  * isSame should be true when representing an inversion, and false when representing a deletion
	  */
	IntervalClone(String NAME, int myChr, double myStart, double myEnd, int LMIN, int LMAX, boolean isSame){
		super(NAME, myChr, myChr, myStart, myEnd, LMIN, LMAX, 0, 0, false);
		
		isSame_ = isSame;

	}
	public Poly getPoly(){
		if (p == null) {
			p = new PolyDefault();
			if (isSame_) {
				p.add(x - Lmax, y + Lmax);
				p.add(x + Lmax, y + Lmax);
				p.add(x + Lmax, y - Lmax);
				p.add(x - Lmax, y - Lmax);
			} else {
				p.add(x, y);
				p.add(x + Lmax, y);
				p.add(x + Lmax, y - Lmax);
				p.add(x, y - Lmax);
			}
		}
		return p;
	}
	public String getType() {
		if (isSame_) {
			return "same";
		} else {
			return "different";
		}
	}
}
