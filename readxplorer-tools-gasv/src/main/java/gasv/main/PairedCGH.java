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


public class PairedCGH extends CGH {
	// probe1_ and probe2_ of the super class are one pair of probes (on one chromosome)
	// probe2_ and probe3_ are a second pair of probes (perhaps on a different chromosome)
	protected String probe3_;
	protected String probe4_;

	//chromosome of these second two probes
	protected int chr2_;

	//coordx_ is the coordinate of the corresponding probex_
	protected double coord1_, coord2_, coord3_, coord4_;

	protected String id_;

	private static final String ID_NAME = "CGHPair";
	//protected boolean isTranslocation_;
	
	public PairedCGH(String ID, String PROBE1, String PROBE2, String PROBE3, String PROBE4,
			int CHR, int CHR2, double COORD1, double COORD2, double COORD3, double COORD4,
			boolean isTranslocation){
		super(PROBE1, PROBE2, CHR, COORD1, COORD2);
		id_ = ID;
		probe3_ = PROBE3;
		probe4_ = PROBE4;
		coord1_ = COORD1;
		coord2_ = COORD2;
		coord3_ = COORD3;
		coord4_ = COORD4;
		chr2_ = CHR2;
		//isTranslocation_ = isTranslocation;
	}
	public PairedCGH(int uid, CGH cgh1, CGH cgh2) {
		super(cgh1.getProbe1(), cgh1.getProbe2(), cgh1.getChr(), cgh1.getX(), cgh1.getY());
		probe3_ = cgh2.getProbe1();
		probe4_ = cgh2.getProbe2();
		coord1_ = cgh1.getX();
		coord2_ = cgh1.getY();
		coord3_ = cgh2.getX();
		coord4_ = cgh2.getY();
		chr2_ = cgh2.getChr();
		id_ = ID_NAME + uid; 
		//isTranslocation_ = isTranslocation;
	}

	public String getProbe3() { return probe3_; }
	public String getProbe4() { return probe4_; }
	public int getChr2() { return chr2_; }
	public double getCoord1() { return coord1_; }
	public double getCoord2() { return coord2_; }
	public double getCoord3() { return coord3_; }
	public double getCoord4() { return coord4_; }
	public String getID() { return id_; } 

	//TODO: Put in README.txt: while for the same chr (non-translocation) it will usually be the case that
	// coord1_, coord2_ have to both be less than coord3_, coord4_...
	// if they are all close in value, there
	// can be some cases where coord3_ and coord4_ are actually greater. In this case there will probably be 
	// a "reversed duplicate" in that one PairedCGH will be created for one ordering and another created for the other
	// ordering. And both will overlap with (not-necessarily the same) clusters of ESP's.
	// Particularly, this can be the case whenever coord2_ (the greater x value) is greater than coord3_ (the
	// smaller y value) - then when x's and y's are switched, coord2_ becomes the upper bound of the rectangle
	// (the greater y value) and it is higher than the y=x dividing line at x=coord3_.
	public String getName() { return probe1_+"_"+probe2_+"_"+probe3_+"_"+probe4_;}

}
