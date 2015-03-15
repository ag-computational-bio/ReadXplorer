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
import gasv.common.Out;
import java.util.*;
import gasv.geom.*;

public class Clone extends BreakRegion{
	String name;
	int number;
	int chrx;
	int chry;
	double x;
	double y; 
	int Lmin;
	int Lmax;
	double bmin;
	double bmax;
	int xLen;
	int yLen;
	boolean overlap;
	
	//A clone is a series of edges.
	Edge top;
	Edge bottom;

	protected Poly p = null;

	protected int clusterID_ = -1;
	
	//by convention, consider clone to overlap with itself
	//protected int numOverlaps_ = 1;

	public Clone(String NAME, int CHRX, int CHRY, double X, double Y,int LMIN, int LMAX, int XLEN, int YLEN,boolean OVERLAP){
		name = NAME;
		number = -1; //Default set to -1;		
		chrx = CHRX; chry = CHRY;
		x = X; y = Y;
		Lmin = LMIN; Lmax = LMAX;
		xLen = XLEN;
		yLen = YLEN;
		overlap = OVERLAP;
		top = new Edge(); bottom = new Edge(); 
		//create Poly first so setEdges doesn't need to recalculate points!
		getPoly();
		setEdges();
		setBounds(); 
	}

	public Clone(Clone orig){
		name = orig.getName();
		number = orig.getNum();
		chrx = orig.getChrX();
		chry = orig.getChrY();
		x = orig.getX();
		y = orig.getY(); 
		Lmin = orig.getLmin();
		Lmax = orig.getLmax();
		bmin = orig.getBmin();
		bmax = orig.getBmax();
		xLen = orig.getXLen();
		yLen = orig.getYLen();
		top = new Edge(orig.getTop());
		bottom = new Edge(orig.getBottom()); 
		p = orig.getPoly();
		overlap = orig.getOverlap();
	}

	public Clone(){
		name = "default";
		number = -1; //Default set to -1;		
		chrx = 0; chry = 0;
		x = -1; y = -1;
		Lmin = 0; Lmax = Integer.MAX_VALUE;
		xLen = 0;
		yLen = 0;
		overlap = false;
		top = new Edge();
		bottom = new Edge();
		p = null;
	}
	
	public Clone(int slope){
		name = "default";
		number = -1; //Default set to -1;		
		chrx = 0; chry = 0;
		if(slope == -1){
			x = -1; y = -1;
		}
		else{
			x = 1; y = -1;
		}
		Lmin = 0; Lmax = Integer.MAX_VALUE;
		xLen = 0;
		yLen = 0;
		top = new Edge();
		bottom = new Edge();
		p = null;
	}
	
	//this was formerly called makeTrapezoid()
	public Poly getPoly(){
		if (p == null) {
			p = new PolyDefault();
			//assume any read starting at coordinate 0 must be positive!
			int signumX = (int) Math.signum(x);
			if (signumX == 0) {
				signumX = 1;
			}
			int signumY = (int) Math.signum(y);
			if (signumY == 0) {
				signumY = 1;
			}
			p.add(Math.abs(x) + signumX * Lmin, Math.abs(y));
			p.add(Math.abs(x) + signumX * Lmax, Math.abs(y));
			p.add(Math.abs(x), Math.abs(y) + signumY * Lmax);
			p.add(Math.abs(x), Math.abs(y) + signumY * Lmin);

			//for now eliminate trimming of trapezoids, but still have to check for small lmin (and automatically bump up if too small!
			if (isTriangle()) {
				// set Lmin = Math.sqrt(2*xLen^2 + 2*yLen^2) + 1
				// this avoids triangles
				// However....
				// since we don't care about triangles, but only want to 
				// increase lmin enough to make sense (the reads shouldn't
				// overlap each other in the fragment), the Lmin must be 
				// at least as long as the two red lengths
				/*if (xLen == yLen) { 
					Lmin = (2 * xLen) + 1;
				} else {
					Lmin = (int)(Math.sqrt((2 * xLen * xLen) + (2 * yLen * yLen))
						+ 1.0d);

				}
				Out.print2("WARNING!!!  Encountered triangle condition for ESP: " 
						+ name 
						+ ", so increasing Lmin for this ESP to " 
						+ Lmin);
				*/
				Lmin = xLen + yLen;
				Out.print2("WARNING: Encountered ESP: " 
						+ name 
						+ " where 2*readlength was > Lmin, so increasing Lmin for this ESP to " 
						+ Lmin);
				if (Lmin >= Lmax) {
					Out.print("ERROR: Lmin has been increased beyond >= Lmax for ESP: " + name 
							+ " so halting program!");
					System.exit(-1);

				}
				//old code to be used if we go back to allowing triangles
				//p.add(newX, newY);
				//p.add(Math.abs(x) + signumX * Lmax - signumX * yLen, newY);
				//p.add(newX, Math.abs(y) + signumY * Lmax - signumY * xLen);
			}
			

			// Originally we were trimming trapezoids because we assumed breakpoints couldn't lie within the span of the read
			// However, after more detailed analysis, we decided to abandon trimming for now.
			// We had trimmed by readlength - 5bp in case ends of alignment were mismatches (split read), assuming at most 5 mismatches allowed.
			/*int xLenTrimmed = xLen - 5;
			int yLenTrimmed = yLen - 5;
			//assume any read starting at coordinate 0 must be positive!
			int signumX = (int) Math.signum(x);
			if (signumX == 0) {
				signumX = 1;
			}
			int signumY = (int) Math.signum(y);
			if (signumY == 0) {
				signumY = 1;
			}

			int signedXLen = signumX * xLenTrimmed;
			int signedYLen = signumY * yLenTrimmed;
			double newX = Math.abs(x) + signedXLen;
			double newY = Math.abs(y) + signedYLen;

			p.add(Math.abs(x) + signumX * Lmin - signumX * yLenTrimmed, newY);
			p.add(Math.abs(x) + signumX * Lmax - signumX * yLenTrimmed, newY);
			p.add(newX, Math.abs(y) + signumY * Lmax - signumY * xLenTrimmed);
			p.add(newX, Math.abs(y) + signumY * Lmin - signumY * xLenTrimmed);
			*/
		}
		return p;
	}

	//for now eliminate trimming of trapezoids (so don't have to worry about triangle condition!
	private boolean isTriangle() {
		boolean isTri = true;
		// trimmed trapezoids must satisfy this inequality
		// xLen^2 + yLen^2 < (Lmin^2) / 2
		// or simply:
		// 2*xLen^2 + 2*yLen^2 < (Lmin^2)
		// if xLen = yLen, then this simplifies to 
		// 2*len < Lmin
		// otherwise they are triangles after trimming by readlengths!
		// However....
		// since we don't care about triangles, but only want to 
		// increase lmin enough to make sense (the reads shouldn't
		// overlap each other in the fragment), the Lmin must be 
		// at least Lmin, make the inequality <= rather than <
		int leftSide = (2*xLen*xLen) + (2*yLen*yLen);
		int rightSide = Lmin * Lmin;
		if (leftSide <= rightSide) {
			isTri = false;
		}
		return isTri;
	}
	

	//Get Functions:
	public String getName(){ return name; }
	public int getNum(){ return number; }
	public int getChrX(){ return chrx; }
	public int getChrY(){ return chry; }
	public double getBmin(){ return bmin; }
	public double getBmax(){ return bmax; }
	public int getLmin(){ return Lmin; }
	public int getLmax(){ return Lmax; }
	public double getX(){ return x; }
	public double getY(){ return y; } 
	public int getXLen(){ return xLen; } 
	public int getYLen(){ return yLen; } 
	public boolean getOverlap(){ return overlap; }
	
	public int getClusterID() {
		return clusterID_;
	}
	public void setClusterID(int clusterID) {
		clusterID_ = clusterID;
	}

	//Set Function for Bottom & Top Edges:
	public int setNumber(int NUM){
		number = NUM;
		return 0;
	}

	public void setEdges(){
		//Assumes the poly has already been built with the appropriate boundary points!
			
		//The Endpoints of the Breakpoint Region;
		double ycoord = p.getY(0);
		double ycoordMin = p.getY(3);
		double ycoordMax = p.getY(2);

		double xcoord = p.getX(3);
		double xcoordMin = p.getX(0);
		double xcoordMax = p.getX(1);
		/*
		double ycoord = Math.abs(y);
		double ycoordMin = Math.abs(y) + Math.signum(y)*Lmin;
		double ycoordMax = Math.abs(y) + Math.signum(y)*Lmax;

		double xcoord = Math.abs(x);
		double xcoordMin = Math.abs(x) + Math.signum(x)*Lmin;
		double xcoordMax = Math.abs(x) + Math.signum(x)*Lmax;
		*/
		
		//Four points in total:
		// (xcoord,ycoordMin), (xcoord,ycoordMax); (xcoordMin, ycoord) (xcoordMax,ycoord)
		EndPoint p1 = new EndPoint(xcoord,ycoordMin);
		EndPoint p2 = new EndPoint(xcoord,ycoordMax);
		EndPoint p3 = new EndPoint(xcoordMin,ycoord);
		EndPoint p4 = new EndPoint(xcoordMax,ycoord);
		
		//Set-up top & bottom;
		if(getType().equals("same")){
			if(ycoord >= ycoordMin && ycoord >= ycoordMax){
				if(xcoordMin < xcoordMax){ top = new Edge(p3,p4); }
				else{ top = new Edge(p4,p3); }
				if(ycoordMin < ycoordMax){ bottom = new Edge(p1,p2);}
				else{ bottom = new Edge(p2,p1); }
			}
			else{
				if(xcoordMin < xcoordMax){ bottom = new Edge(p3,p4); }
				else{ bottom = new Edge(p4,p3); }
				if(ycoordMin < ycoordMax){ top = new Edge(p1,p2); }
				else{ top = new Edge(p2,p1); }
			}
	
		}
				
		else{//if(getType().equals("different")){
			//MAKE THE CONDITIONS PARALLEL!
			if(Math.signum(x) < 0 && Math.signum(y) >= 0){
					//bottom = new Edge(p4,p3);
					bottom = new Edge(p3,p4);
					top = new Edge(p1, p2);
			}
			else{ //Math.signum(x) > 0 && Math.signum(y) < 0){
					bottom = new Edge(p1, p2);
					top = new Edge(p3,p4);
					//top = new Edge(p4,p3);
			}
			
		}
		
	}

	//Get Functions for edges:
	public Edge getTop(){ return top;}
	public Edge getBottom(){ return bottom;}

	//Task: Computes the upper and lower bounding values
	//      for the intersection of a cluster.
	//Uses: Stopping points in the line sweep 
	//Can also set directly:
	public void setBmin(int min){ bmin = min;}
	public void setBmax(int max){ bmax = max;}
	

	//SAME      -- means the orientation is either +/+ or -/-
	//DIFFERENT -- means we are either +/- or -/+
	public String getType(){
		int signumX = (int) Math.signum(getX());
		if (signumX == 0) {
			signumX = 1;
		}
		int signumY = (int) Math.signum(getY());
		if (signumY == 0) {
			signumY = 1;
		}

		if(signumX == signumY){ return "same"; }
		else{ return "different"; }
	}

	public int setBounds(){
		
		//Inversion Clone: slope -1; b = y + x;
		if(getType().equals("same")){
			bmin = (top.getLeft()).getY()  + (top.getLeft()).getX();
			bmax = (top.getRight()).getY() + (top.getRight()).getX();
		}
		//Deletion/Other clone: slope +1; y = x + b; b = y-x;
		else{
			
			bmin = (top.getLeft()).getY() - (top.getLeft()).getX();
			bmax = (top.getRight()).getY() - (top.getRight()).getX();
			//DO NOT UNDERSTAND WHY WE HAVE THIS PROBLEM!!!!!
			// Luke: this problem is due to the order of the coordinates in 
			// the top and bottom edges being incompatible for divergent vs convergent
			// trapezoids.  Assuming that bmin should be less than bmax,
			// then top and bottom for the divergent (-/+) case is correct,
			// but for convergent, top is currently (p3, p4)
			// and bottom is currently (p1, p2).
			// These should be reversed to (p4, p3) and (p2, p1), respectively.
			// Then the below if-statement can be deleted.  But I believe
			// the GASVMain.overlap() method depends on the current ordering
			// right now, so don't have the time to mess with anything since
			// it all seems to work.
			if(bmin>bmax){
				double temp = bmax;
				bmax = bmin;
				bmin = temp;
			}
		}
			
			//bmax = (top.getLeft()).getY() - (top.getLeft()).getX();
			//bmin = (top.getRight()).getY() - (top.getRight()).getX();
		return 0;
	}
	
	public String toOutput(){
		String orientX; String orientY;
		double xBegin = x;
		double yBegin = y;
		double xEnd = x + xLen;
		double yEnd = y + yLen;
		
		if(Math.signum(x) > 0 || Math.signum(x) == 0){ 
			orientX = "+"; 
		}
		else{ 
			orientX = "-";
			xBegin = xEnd; 	
			xEnd = x; 
		} 
		
		if(Math.signum(y) > 0 || Math.signum(y) == 0){ 
			orientY = "+"; 
		}
		else{ 
			orientY = "-";
			yBegin = yEnd; 	
			yEnd = y; 
		}

		
		return name + "\t" + chrx + "\t" + (int) Math.abs(xBegin) + "\t" + (int) Math.abs(xEnd) + "\t" + orientX + "\t" + chry + "\t" + (int) Math.abs(yBegin) + "\t" + (int) Math.abs(yEnd) + "\t" + orientY; 
	}
	
	//Task: Outputs the clone type to a string.
	public String toString(){
		return name + " " + number + " " + chrx + " " + x + " " + chry + " " + y + " " + bmin + " " + bmax + "\n" + top.toString() + " " + bottom.toString();
	}
	
	/** 
	  * shifts the x and y coordinates each by a random value between 0.1 and 0.4
	  * The point of this is to avoid degenerate cases in the algorithm.
	  */
	/*public void shift() {
		//nextDouble() returns rand double between 0.0 and 1.0
		// multiply by MAX_SHIFT to get random value between 0.0 and MAX_SHIFT 
		double xShift = rand_.nextDouble() * Constants.MAX_SHIFT;
		double yShift = rand_.nextDouble() * Constants.MAX_SHIFT;

		//ensure shifts are at least 0.1... This will skew the 
		//distribution somewhat, but shouldn't matter
		double threshold = Constants.ZERO_THRESHOLD;
		if (xShift < threshold) {
			xShift += threshold;
		}
		if (yShift < threshold) {
			yShift += threshold;
		}

		x += xShift;
		y += yShift;
	}
	*/
	
}

