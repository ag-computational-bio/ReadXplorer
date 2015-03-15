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

public class CGH extends BreakRegion{
	protected String probe1_;
	protected String probe2_;
	protected double x_,y_;
	protected int chr_;
	
	public CGH(String PROBE1, String PROBE2, int CHR, double X, double Y){
		probe1_ = PROBE1;
		probe2_ = PROBE2;
		chr_ = CHR;
		x_ = X;
		y_ = Y;
	}
	public String getProbe1() { return probe1_; }
	public String getProbe2() { return probe2_; }
	public double getX() { return x_; }
	public double getY() { return y_; }
	public int getChr() { return chr_; }
	public String getName() { return probe1_+"_"+probe2_;}
}
