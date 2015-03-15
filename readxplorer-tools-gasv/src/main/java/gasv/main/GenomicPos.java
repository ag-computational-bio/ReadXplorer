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
import java.util.Comparator;

public class GenomicPos implements Comparator {
	public int compare(Object c1, Object c2){
		BreakRegion clone1 = (BreakRegion) c1;
		BreakRegion clone2 = (BreakRegion) c2;
		if(Math.abs(clone1.getX()) > Math.abs(clone2.getX()))
			return 1;
		else if(Math.abs(clone1.getX()) == Math.abs(clone2.getX()))
			return 0;
		else return -1;
	}

}
