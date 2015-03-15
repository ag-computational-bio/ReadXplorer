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
import gasv.common.Constants;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract class ReadFile {

	/* ReadFile is a generic class that contains all basic functions of reading a data file and creating geometric structures.
	 * ReadESP, ReadCGH, and ReadPred extend from this class.
	 */
	protected FileReader f;
	protected BufferedReader b;
	protected int numChrom;
	//protected ArrayList<BreakRegion> breakRegions_;
	//TODO: change name from clones to breakRegions_
	protected ArrayList<BreakRegion> clones;
	//protected ArrayList<Cluster>[][] shapes;
	protected GenomicPos genomicPosComparator_;
	protected String file_;
	
	public ReadFile(String file, ArrayList<BreakRegion> breakRegions) throws IOException{
		//TODO: change name from clones to breakRegions_
		//breakRegions_ = breakRegions;
		clones = breakRegions;
		file_ = file;
		numChrom = GASVMain.NUM_CHROM;
		//clones = new ArrayList[numChrom][numChrom];
		//shapes = new ArrayList[numChrom][numChrom];
		genomicPosComparator_ = new GenomicPos();
	}
	public ArrayList<BreakRegion> getBreakRegions() {
		return clones;
	}	

	public String getFileName() {
		return file_;
	}

	protected int makeSigned(String coord, String orient){
		if(orient.equals("+") || orient.equals("PLUS") || orient.equals("Plus"))
			return Math.abs(Integer.parseInt(coord));
		if(orient.equals("-") || orient.equals("MINUS")|| orient.equals("Minus"))
			return -Math.abs(Integer.parseInt(coord));
		return 0;
	}
	
	protected int chrFormatToNumber(String ch) throws NumberFormatException{
		//String[] chr = ch.split("");
		int toReturn = 0;
		if(ch.length() >= 3 && ch.substring(0,3).equals("chr")){
			if(ch.substring(3,4).equals("X"))
				toReturn =  23;
			else if (ch.substring(3,4).equals("Y"))
				toReturn = 24;
			else toReturn = Integer.parseInt(ch.substring(3,ch.length()));
		} else if (ch.equalsIgnoreCase("x")) {
			toReturn = 23;
		} else if (ch.equalsIgnoreCase("y")) {
			toReturn = 24;
		} else {
			toReturn = Integer.parseInt(ch);
		}
		return toReturn; 
	}
	
	protected void sortByFirstPos(ArrayList[][] toSort){
		for(int i = 0; i < numChrom; i++){
			for(int j = 0; j < numChrom; j++){
				if(toSort[i][j]!=null)
					Collections.sort(toSort[i][j], genomicPosComparator_);
			}
		}
	}
	
	/**
	  * Subclasses must implement this abstract method.
	  * Does the work of reading in the data from the file and converting each 
	  * line of data into a type of BreakRegion.  The BreakRegion is then 
	  * placed in the ArrayList<BreakRegion> data structure that was passed
	  * into the constructor as long as it matches the specified leftChr and rightChr.
	  */
	public abstract void readBreakRegions(int leftChr, int rightChr) throws IOException;

	/**
	  * Subclasses must implement this abstract method.
	  * Does the work of reading in the data from the file and converting each 
	  * line of data into a type of BreakRegion.  The BreakRegion is then 
	  * placed in the provided 2D array of BreakRegion ArrayList's. 
	  */
	public abstract void readBreakRegions(ArrayList<BreakRegion>[][] breakRegionsArray) throws IOException;
	
	/**
	 * Subclasses must implement this abstract method.
	 * Does the work of reading in the data from the file and converting each 
	 * line of data into a type of BreakRegion.  The BreakRegion is then 
	 * placed in the ArrayList<BreakRegion> data structure that was passed
	 * into this method call as long as it matches the specified leftChr and rightChr.
	 *
	 *  Only read in those clones that match the 
	 *  specified targetLeftChr and targetRightChr and are within the current window as specified
	 *  by @endWindowPos.
	 *  Upon encountering a clone that doesn't meet the criteria, method returns immediately.
	 *
	 *  Return true if end of file reached, otherwise returns false.
	 */
	public abstract boolean readNextBreakRegions(int leftChr, int rightChr, 
			ArrayList<BreakRegion> br, int endWindowPos, boolean startNewChr) throws IOException;

	public abstract boolean getDiffChrPairReached();

}
