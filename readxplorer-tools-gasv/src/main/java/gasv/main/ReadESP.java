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
import java.io.IOException;
import java.util.*;


public class ReadESP extends ReadFile {

	/* ReadESP reads in a file of ESP data and creates trapezoids in the plane from the two genomic locations given and
	 *		provided lmin and lmax.
	 * Input should be of format:
	 * clone_index chr1 pos1 orient1 chr2 pos2 orient2
	 */
	
	private int lmin_, lmax_;

	private String nextLine_;

	private java.io.FileReader f_;
	private java.io.BufferedReader b_;
	private boolean diffChrPairReached_;
	private int curLeftChr_;
	private int curRightChr_;
	private int endWindowPos_ = 0;
	private int windowSize_;
	private boolean concordantESPWarningRaised_ = false;
	private boolean overlapESPWarningRaised_ = false;
	private int numConcordant_ = 0;
	private int numOverlapping_ = 0;
	private int intervalID_ = 1;
	private HashMap<String, Integer> transcriptToIntMap = null;
	
	public ReadESP(String file, int min, int max, 
			ArrayList<BreakRegion> breakRegions, int windowSize) throws IOException{
		super(file, breakRegions);
		lmin_ = min;
		lmax_ = max;
		f_ = new java.io.FileReader(file_);
		b_ = new java.io.BufferedReader(f_);
		curLeftChr_ = 0;
		curRightChr_ = 0;
		diffChrPairReached_ = false; 
		nextLine_ = null;
		windowSize_ = windowSize;
	}
	
	public int getNumConcordant() {
		return numConcordant_;
	}

	//FORMAT:
	// 0     1     2       3      4       5      6      7      8
	//NAME CHR_X X_START X_END X_ORIENT CHR_Y Y_START Y_END Y_ORIENT

	/**
  	 *  In this version of readBreakRegions(), all clones are read into memory in one pass
	 *  and stored in the provided 2D Array of ArrayList<breakRegions> 
	 */
	public void readBreakRegions(ArrayList<BreakRegion>[][] breakRegionsArray) throws IOException{
		readBreakRegions(-1, -1, breakRegionsArray);
	}

	/**
	 *  Read in clones from the file into an ArrayList. Only read in those clones that match the 
	 * specified targetLeftChr and targetRightChr
	 */
	public void readBreakRegions(int targetLeftChr, int targetRightChr) throws IOException{
		readBreakRegions(targetLeftChr, targetRightChr, null);
	}

	/**
	 *  Returns true if c represents a concordant ESP.
	 */
	private boolean isConcordant(Clone c) {
		double diff = Math.abs(c.getY()) - Math.abs(c.getX());
		if ((diff < lmax_) && (diff > lmin_) && (c.getChrX() == c.getChrY()) && (c.getX() >= 0) && (c.getY() < 0)) {
		       return true;
		} else {
			return false;
		}	 
	}
	
	/**
	 *  Returns true if we correspond to a valid mapping. (i.e., the reads alignments do NOT overlap.)
	 */
	private boolean isOverlapping(Clone c) {
		return c.getOverlap();
	}
	
	
	private Clone parseIntervalCloneFromString(String nextLine, int targetLeftChr, int targetRightChr, 
			boolean useWindowSize, boolean matchChromosomes, String[] line) 
			throws NumberFormatException, Exception {

		if (line.length != 3 && line.length != 5) {
			System.out.println("Found a row with incorrect"
					+ " number of items. Expect 3 or 5"
					+ " in an PR (or interval) file, found: " 
					+ line.length + ", so skipping row.");
			throw new Exception();
		}
		int leftChr = 0;
		int rightChr = 0;

		//if these operations fail, will throw NumberFormatException and skip row
		leftChr = chrFormatToNumber(line[0]);
		rightChr = leftChr;

		// if breakRegionsArray is null, meaning that only want matching chr #'s,
		// ignore unless the current line's chromosomes matches the target chromosomes
		if (matchChromosomes) {
			boolean chrMatch = false;
			if (leftChr <= rightChr) {
				if ((leftChr == targetLeftChr) && (rightChr == targetRightChr)) {
					chrMatch = true;
				}	
			} else {
				if ((rightChr == targetLeftChr) && (leftChr == targetRightChr)) {
					chrMatch = true;
				}	
			}
			if (!chrMatch) { 
				Out.print3("ReadESP: leftChr"+leftChr+ ",rightChr"+rightChr+" doesn't match target chr"+targetLeftChr + ",chr"+ targetRightChr + " so returning null");
				diffChrPairReached_ = true;
				//Out.print2("ReadESP: different chromosome encountered!");
				return null;
			} 
		}

		IntervalClone c = null;

		boolean isInversion = false;
		if (file_.contains("inv") || file_.contains("inv"))  {
			isInversion = true;
		}
		double start = Double.parseDouble(line[1]);
		double end = Double.parseDouble(line[2]);
		if(start < end){
			int slashIdx = file_.lastIndexOf("/");
			String fnStripped = file_;
			if (slashIdx > 0) {
				fnStripped = file_.substring(slashIdx + 1, file_.length());
			}
			c = new IntervalClone(fnStripped + "." + intervalID_, leftChr, start, 
					end, lmin_, lmax_, isInversion);
		} else {
			Out.print("ERROR: x is not < y!! for line : " + nextLine);
		}

		//make sure that the file is sorted correctly!
		if (useWindowSize && (Math.abs(c.getX()) < (endWindowPos_ - windowSize_))) {
			Out.print("Encountered line " + nextLine + "\n with matching chromosomes but has coordinate " 
					+ Math.abs(c.getX()) + " that is smaller than "
					+ "the start of the current window, which would be " 
					+ (endWindowPos_ - windowSize_) + "! File probably isn't sorted correctly!");
		 	System.exit(-1);
		}

		if (useWindowSize && (Math.abs(c.getX()) > endWindowPos_)) {
			//Out.print3("TMP!!! ReadESP: cur read pair outside current window, with read.x= " + Math.abs(c.getX()) 
					//+ " and endWindowPos_=" + endWindowPos_);
			return null;
		} else {
			intervalID_++;
			return c;
		}


	}
	/**
	  * Throws exceptions if have problems parsing the line.
	  * If no problems parsing line but encounter clone that is either on a different chromosome
	  * or useWindowSize is true and has x coordinate outside of the current window, return null
	  */
	private Clone parseCloneFromString(String nextLine, int targetLeftChr, int targetRightChr, 
			boolean useWindowSize, boolean matchChromosomes) 
			throws NumberFormatException, Exception {
		String[] line = nextLine.split("\\s+");
		//if (targetLeftChr == 24) {
			//Out.print1("TMP**** ReadESP.parseCloneFromString() called for line: " + nextLine);
		//}

		if (line.length != Constants.NUM_COLS_IN_ESP_FILE) {
			return parseIntervalCloneFromString(nextLine, targetLeftChr, targetRightChr, 
					useWindowSize, matchChromosomes, line);
			//TMP change!!
			/*System.out.println("Found a row with incorrect"
					+ " number of items. Expect "
					+ Constants.NUM_COLS_IN_ESP_FILE
					+ " in an ESP file, found: " 
					+ line.length + ", so skipping row.");
			throw new Exception();
			*/
		}
		int leftChr = 0;
		int rightChr = 0; 
		//if these operations fail, will throw NumberFormatException and skip row
		try {
			leftChr = chrFormatToNumber(line[1]);
			rightChr = chrFormatToNumber(line[5]);
		} catch (NumberFormatException nex) {
				throw nex;
		}

		// if breakRegionsArray is null, meaning that only want matching chr #'s,
		// ignore unless the current line's chromosomes matches the target chromosomes
		if (matchChromosomes) {
			boolean chrMatch = false;
			if (leftChr <= rightChr) {
				if ((leftChr == targetLeftChr) && (rightChr == targetRightChr)) {
					chrMatch = true;
				}	
			} else {
				if ((rightChr == targetLeftChr) && (leftChr == targetRightChr)) {
					chrMatch = true;
				}	
			}
			if (!chrMatch) { 
				//if in single chr mode, just want to skip any earlier chromosomes
				//if ((GASVMain.CHR > 0) && ((leftChr < targetLeftChr) || (rightChr < targetRightChr))) {
					//throw new Exception("In --chr (single chromosome) mode, so skipping an early chromosome");
				//}
				Out.print3("ReadPR: leftChr"+leftChr+ ",rightChr"+rightChr+" doesn't match target chr"+targetLeftChr + ",chr"+ targetRightChr + " so returning null");
				diffChrPairReached_ = true;
				//Out.print2("ReadESP: different chromosome encountered!");
				return null;
			} 
		}

		double x_start = makeSigned(line[2],line[4]);
		double x_end   = makeSigned(line[3],line[4]);
		double y_start = makeSigned(line[6],line[8]);
		double y_end   = makeSigned(line[7],line[8]);
		double x = 0;
		double y = 0;

		if(x_start < 0){ 
			x = x_end; 
		}
		else{ 
			x = x_start;
		}
		if(y_start < 0){ 
			y = y_end; 
		}
		else{ 
			y = y_start;
		}
		
		//Begin Consistency Check: Check to make sure the two read alignments do NOT overlap.
		boolean overlap = false;
		double x_start_real = Double.parseDouble(line[2]);
		double x_end_real = Double.parseDouble(line[3]);
		double y_start_real = Double.parseDouble(line[6]);
		double y_end_real  = Double.parseDouble(line[7]);
		if( leftChr == rightChr ){
			if( (x_end_real < y_start_real)  || (y_end_real < x_start_real) ){
				//The read alignments do NOT overlap, and we have a valid discordant fragment.
			}
			else{
				//The read alignments DO overlap (by >= 1 bp) and so this is not a valid discordant fragment.
				overlap = true;
			}
		}
		//End Consistency Check
				
		int xLen = (int) Math.round(Math.abs(x_start - x_end));
		int yLen = (int) Math.round(Math.abs(y_start - y_end));
		Clone c = null;

		//= new Clone(line[0],leftChr,rightChr,x,y,lmin_,lmax_);
		if(leftChr != rightChr){
			if(leftChr < rightChr){ 
				//Clone Command 1 of 4
				c = new Clone(line[0],leftChr,rightChr,x,y,lmin_,lmax_,xLen,yLen,overlap); 
			} else { 
				//Clone Command 2 of 4
				c = new Clone(line[0],rightChr,leftChr,y,x,lmin_,lmax_,yLen,xLen,overlap);
				int temp = leftChr;
				leftChr = rightChr;
				rightChr = temp;
			}
		} else{
			if(Math.abs(x) < Math.abs(y)){
				//Clone Command 3 of 4
				c = new Clone(line[0],leftChr,rightChr,x,y,lmin_,lmax_,xLen,yLen,overlap);
				//System.err.println("line[0]\n" + "LEFT:\t" + "chr" + leftChr + "\tx: " + x + "\nRIGHT:\t " + "chr" + rightChr + "\ty:\t" + y + "\n" + c.toString());
			} else {
				//Clone Command 4 of 4
				c = new Clone(line[0],rightChr,leftChr,y,x,lmin_,lmax_,yLen,xLen,overlap);
				//System.err.println("line[0]\n" + "LEFT:\t" + "chr" + rightChr + "\tx: " + y + "\nRIGHT:\t " + "chr" + leftChr + "\ty:\t" + x + "\n" + c.toString());

			}

		}
	
				if(isOverlapping(c)){
					if(!overlapESPWarningRaised_){
						Out.print("Warning: File: " +  file_ + " contains a PR with overlapping reads "
								  + " GASV ignores such cases. The first such PR case encountered: "
								  + nextLine);
						overlapESPWarningRaised_ = true;
					}
					numOverlapping_++;
					throw new Exception("Overlapping PR encountered: " + nextLine);
				}
				
				if (isConcordant(c)) {
					
					if (!concordantESPWarningRaised_) {
						Out.print("Warning: File: " + file_ + " contains concordant PR data! "
								  + "Concordant data is ignored by GASV. The first concordant PR encountered: " 
								  + nextLine);  
						concordantESPWarningRaised_ = true;
					}
					numConcordant_++;
					throw new Exception("Concordant PR encountered: " + nextLine);
				}
				

				
				//make sure that the file is sorted correctly!
		if (useWindowSize && (Math.abs(c.getX()) < (endWindowPos_ - windowSize_))) {
			Out.print("Encountered line " + nextLine + "\n with matching chromosomes but has coordinate " 
					+ Math.abs(c.getX()) + " that is smaller than "
					+ "the start of the current window, which would be " 
					+ (endWindowPos_ - windowSize_) + ". Check file for correct sorting.");
		 	System.exit(-1);
		}



		if (useWindowSize && (Math.abs(c.getX()) > endWindowPos_)) {
			//Out.print3("TMP!!! ReadESP: cur read pair outside current window, with read.x= " + Math.abs(c.getX()) 
					//+ " and endWindowPos_=" + endWindowPos_);
			return null;
		} else {
			return c;
		}


	}	
	
	/**
	 *  Read in clones from the file's current file position into an ArrayList. 
	 *  Only read in those clones that match the 
	 *  specified targetLeftChr and targetRightChr and are within the current window as specified by
	 *  the endWindowPos.
	 *  Upon encountering a clone that doesn't meet the criteria, method returns immediately.
	 *
	 *  Return true if end of file reached, otherwise returns false.
	 *  
	 */
	public boolean readNextBreakRegions(int targetLeftChr, int targetRightChr, 
			ArrayList<BreakRegion> br, int endWindowPos, boolean startNewChr) throws IOException{

		//if starting on a new left, right chr pair, then need to clear the following flag
		if (startNewChr) {
			diffChrPairReached_ = false;
		}
		endWindowPos_ = endWindowPos;
		// only need to do this the first time through. After that, the previously read
		// nextLine_ will need to be parsed again to see if it matches this next chr pair or it will
		// need to be added to this new window
		if (nextLine_ == null) {
			nextLine_ = b_.readLine();
		}
		int numClonesInWin = 0;
		//will break out of this loop when first ESP outside of the current window is encountered
		while(nextLine_ != null){

			//Skip comment lines (those beginning with "#")
			if (nextLine_.startsWith("#")) {
				nextLine_ = b_.readLine();
				continue;
			}

			Clone c = null;

			try {
				c = this.parseCloneFromString(nextLine_, targetLeftChr, targetRightChr, true, true);
			} catch (NumberFormatException ex) {
				System.out.println("Couldn't parse chromosome identifiers: " 
						+ " for line: " + nextLine_ 
						+ " so skipping current line");
				nextLine_ = b_.readLine();
				continue;
			} catch (Exception ex) {
				//encountered a row with incorrect number of columns
				//Already printed error message so just skip to next line
				nextLine_ = b_.readLine();
				continue;
			}

			// If c is null we either reached a read on different chromosomes or a read that is beyond
			// the current window, but we haven't reached the end of the file yet, so return false 
			if (c == null) {
				return false;
			}


			if (numClonesInWin < GASVMain.MAX_READS_PER_WIN){ 
				// if no 2D array provided, put it in the ArrayList 
				br.add(c);
				++numClonesInWin;
			} else {
				br.clear();
			}

			nextLine_ = b_.readLine();
		}

		// if we reach the end of the file, close streams and return true
		b_.close();
		f_.close();
		if (numConcordant_ > 0) { 
			Out.print("Ignored " + numConcordant_ + " concordant PR's in file " + file_);
		}
		if (numOverlapping_ > 0) { 
			Out.print("Ignored " + numOverlapping_ + " overlapping-read PR's in file " + file_);
		}
		return true;
		
	}

	/**
	  * Returns true if a read with a chromosome pair other than the target chromosome pair is encountered
	  * in the "window" reading mode.
	  */
	public boolean getDiffChrPairReached() {
		return diffChrPairReached_;
	}
	
	/**
	 *  Read in clones from the file into an ArrayList. 
	 *  If breakRegionsArray is null, only read in those clones that match the 
	 *  specified targetLeftChr and targetRightChr.
	 *  Else if breakRegionsArray is not null, ignore the chromosome numbers and
	 *  just put all break regions into the provided 2D array.
	 */
	private void readBreakRegions(int targetLeftChr, int targetRightChr,
			ArrayList<BreakRegion>[][] breakRegionsArray) throws IOException{
		
		//f = new java.io.FileReader(file_);
		//b = new java.io.BufferedReader(f);
		int counter = 0;
		String nextLine = b_.readLine();
		while(nextLine != null){

			//Skip comment lines (those beginning with "#")
			if (nextLine.startsWith("#")) {
				nextLine = b_.readLine();
				continue;
			}

			Clone c = null;

			try {
				boolean matchChromosomes = false;
				if (breakRegionsArray == null) {
					matchChromosomes = true;
				}
				// pass false because we don't want to use the Window Size as a limiter
				// and also 
				c = this.parseCloneFromString(nextLine, targetLeftChr, targetRightChr, false,
						matchChromosomes);
			} catch (NumberFormatException ex) {
				System.out.println("Couldn't parse chromosome identifiers: " 
						+ " for line: " + nextLine 
						+ " skipping current line");
				nextLine = b_.readLine();
				continue;
			} catch (Exception ex) {
				//encountered a row with incorrect number of columns
				//Already printed error message so just skip to next line
				nextLine = b_.readLine();
				continue;
			}

			// If c is null we reached a read on different chromosomes, so skip it and keep going 
			if (c == null) {
				nextLine = b_.readLine();
				continue;
			}
		
			// if no 2D array provided, put it in the member ArrayList 
			if (breakRegionsArray == null) {
				clones.add(c);
			} else {
				//otherwise, put it in the 2D array
				int x = c.getChrX() - 1;
				int y = c.getChrY() - 1;

				if(breakRegionsArray[x][y]==null) {
					breakRegionsArray[x][y] = new ArrayList<BreakRegion>();
				}

				breakRegionsArray[x][y].add(c);

			}

			nextLine = b_.readLine();
		}
		b_.close();
		f_.close();
	}
}


