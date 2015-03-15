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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class ReadInput {

	/* ReadInput reads in the one input file, which contains a list of data file names and types.
	 *  Input file should be of one of two formats:
	 *  file ESP lmin lmax (if data is ESP)
	 *  file CGH/PRED (if data is CGH or prediction)
	 */
	FileReader f;
	BufferedReader b;
	ArrayList<String> CGHfiles;
	private String filename_;
	boolean setFiles;
	private ReadFile singleReadFile_;
	private ArrayList<ReadFile> readFiles_;
	private ReadFile[] arrayOfReadFiles_ = null;
	private int[] curPosArray_ = null;
	private int[] endWindowPosArray_ = null;
	
	private int windowSize_;
	private int curPos_;
	private int endWindowPos_;
	private int curLeftChr_;
	private int curRightChr_;

	private long[] chrFilePosStart_ = null;
	//private int[] readCGHFileIdx_ = null;
	ArrayList<long[]> chrFilePosStartList_ = null; 

	public ReadInput(String filename) throws IOException{
		filename_ = filename;
		setFiles = false;
		CGHfiles = new ArrayList<String>();
		singleReadFile_ = null;
		readFiles_ = null;
		curPos_ = 0;
		endWindowPos_ = 0; 
		curLeftChr_ = 0;
		curRightChr_ = 0;

		//if overflow is likely, then just use LMAX as the window size
		if (GASVMain.MAX_LMAX > (Integer.MAX_VALUE / Constants.WIN_SIZE_FACTOR)) {
			Out.print("Warning: lmax value of " + GASVMain.MAX_LMAX + " is extremely large. " 
					+ "Using this value as the window size to avoid overflow");
			windowSize_ = GASVMain.MAX_LMAX;
		} else {
			windowSize_ = (int) (GASVMain.MAX_LMAX * Constants.WIN_SIZE_FACTOR);	
			Out.print1("Using window size of " + windowSize_); 
		}

		chrFilePosStartList_ = new ArrayList<long[]>();
		chrFilePosStart_ = new long[GASVMain.NUM_CHROM];
		//readCGHFileIdx_ = new int[GASVMain.NUM_CHROM];
		
		for (int i=0; i<GASVMain.NUM_CHROM; ++i) {
			chrFilePosStart_[i] = 0;
			//readCGHFileIdx_[i] =  -1;
		}
	}

	/**
	 *  This reads a single file by creating the appropriate ReadFile
	 *  object to store results in the provided 2D array.
	 */
	public void readSingleFile(int lmin, int lmax,
			ArrayList<BreakRegion>[][] breakRegionsArray) throws IOException {

		readSingleFile(-1, -1, lmin, lmax, breakRegionsArray);
	}


	/**
	 *  This reads a single file by creating the appropriate ReadFile
	 *  object to store results in the ArrayList that is returned.
	 *  Only those results matching the specified chr numbers are returned.
	 */
	public ArrayList<BreakRegion> readSingleFile(int leftChr, int rightChr, int lmin, 
			int lmax) throws IOException {
		
		return readSingleFile(leftChr, rightChr, lmin, lmax, null);
	}

	/**
	 *  This reads a single file by creating the appropriate ReadFile
	 *  object to store results either in the provided 2D array, or if
	 *  the 2D array is null, then in the returned ArrayList.
	 *  Returns null if error occurs.
	 */
	private ArrayList<BreakRegion> readSingleFile(int leftChr, int rightChr, int lmin, int lmax,
			ArrayList<BreakRegion>[][] breakRegionsArray) 
			throws IOException {

		ArrayList<BreakRegion> breakRegions = null;

		//if 2D array is null, need to provide an ArrayList for ReadFile constructors
		// in which BreakRegion's can be placed
	       	if (breakRegionsArray == null) {
			breakRegions = new ArrayList<BreakRegion>();
		}
		ReadFile r = this.createReadFile(lmin, lmax, breakRegions);
	
		// if no 2D array provided, will only be storing regions from the specified chromosomes
		if (breakRegionsArray == null) {
			r.readBreakRegions(leftChr, rightChr);
			Out.print2("ReadInput: Loaded " + breakRegions.size() 
					+ " Break Regions for chr" 
					+ leftChr + ", chr" + rightChr); 
		} else {
			//otherwise store  all regions in the provided array
			r.readBreakRegions(breakRegionsArray);
		}
		return breakRegions;
	}	


	private ReadFile createReadFile(int lmin, int lmax, 
			ArrayList<BreakRegion> breakRegions) throws java.io.IOException{
		f = new FileReader(filename_);
		b = new BufferedReader(f);
		

		String nextLine = b.readLine();

		//Skip comment lines (those beginning with "#")
		while ((nextLine != null) && (nextLine.startsWith("#"))) {
			nextLine = b.readLine();
		}

		if (nextLine == null) {
			Out.print("ReadInput: ERROR! File " + filename_ + " is empty!!");
			b.close();
			f.close();
			return null;
		} else {

			String[] line = nextLine.split("\\s+");
			ReadFile r = null;
			
			// Careful.. if two file types have same number of columns, won't be able to use
			// the number of columns to distinguish file types any more
			if (line == null) {
				Out.print("ReadInput.java: Error parsing line:\n" 
						+ nextLine + "\n\t Couldn't split by whitespace!");
				b.close();
				f.close();
				return null;
			} else if (line.length == Constants.NUM_COLS_IN_ESP_FILE) {
				r = new ReadESP(filename_, lmin, lmax, breakRegions, windowSize_);
			} else if ((line.length == Constants.NUM_COLS_IN_SING_CGH_FILE) 
					|| (line.length == Constants.NUM_COLS_IN_PAIR_CGH_FILE)) {
				r = new ReadCGH(filename_, breakRegions);
			}
			/*else if(line[1].equals("PRED")) 
				r = new ReadPred(line[0], breakRegions);
			else if(line[1].equals("SINGLEPRED"))
				r = new ReadSinglePred(line[0], breakRegions);
			else if(line[1].equals("CGH")){
				r = new ReadCGH(line[0], breakRegions);
			}*/
			/*if(line[3].equals("p")){
				ReadFile_CGH_shorter r = new ReadFile_CGH_shorter(line[0],Integer.parseInt(line[1]),Integer.parseInt(line[2]),sep);
			}*/

			else {
				Out.print("ReadInput.java: Error parsing line:\n" 
						+ nextLine + "\n\t...unrecognized file format: Can't "
						+ "handle file with " + line.length + " columns!");
				b.close();
				f.close();
				return null;
			}
			return r;
		}


	}

	/** 
	  * This version is for CGH data since no lmin/lmax will be required.
	  * Also, since CGH is always for a single chromosome, pass in same value
	  * for both left and right chr.
	  */
	public boolean readWindowFromSingleFile(int targetChr, 
			ArrayList<BreakRegion> br) throws IOException {
		return readWindowFromSingleFile(targetChr, targetChr, -1, -1, br);
	}

	/**
	 *  This reads break regions from a single file by creating the appropriate 
	 *  ReadFile (if not already created),
	 *  and then getting the next "window" of break regions from it and returning them in the ArrayList.
	 *  Call this method multiple times until either no new BreakRegions are put in the "br" 
	 *  ArrayList or the end of file is reached.
	 *  @return true if end of file reached, false otherwise
	 */
	public boolean readWindowFromSingleFile(int leftChr, int rightChr, int lmin, 
			int lmax, ArrayList<BreakRegion> br) throws IOException {

		if (singleReadFile_ == null) {
			singleReadFile_ = this.createReadFile(lmin, lmax, br);
		}
		//if starting on a new left, right chr pair, then need to reset to the starting values
		boolean startNewChr = false;
		if ((leftChr != curLeftChr_) || (rightChr != curRightChr_)) {
			startNewChr = true;
			curPos_ = 0;
			endWindowPos_ = 0;
			curLeftChr_ = leftChr;
			curRightChr_ = rightChr;
		}
		curPos_ = endWindowPos_;
		endWindowPos_ = curPos_ + windowSize_;

		Out.print2("TMP: readWindowFromSingleFile() endWindowPos_: " + endWindowPos_);

		// In this call, the file is assumed to be sorted by chromosomes and 
		// genomic coordinates.
		//this will only read the break regions in the next "window" 
		//delimited by Constants.WIN_SIZE_FACTOR *lmax. If there are no more break regions for the 
		//specified  chromosomes, no more break regions will be added to the ArrayList, 
		// and a flag will be set to make getDiffChrPairReached() return true
		// Note that even though CGH data doesn't use LMIN or LMAX, the maximum LMAX value
		// specified for all ESP data being clustered is used to determine window size.
		/** 
		 * IT IS UP TO THE USER TO DETECT THIS CONDITION BY CALLING getDiffChrPairReached()!!!
		 */
		boolean endFileReached = singleReadFile_.readNextBreakRegions(leftChr, rightChr, br,
							endWindowPos_, startNewChr);

		return endFileReached;
	}

	/**
	 * This method does the work of reading the batch file and parsing each line within it into a
	 * ReadFile object and returning these objects in an ArrayList.
	 */
	private ArrayList<ReadFile> createReadFiles(ArrayList<BreakRegion> breakRegions) throws IOException{

		ArrayList<ReadFile> readFiles = new ArrayList<ReadFile>();
		//singleReadFile_ = this.createReadFile(lmin, lmax, br);
		f = new FileReader(filename_);
		b = new BufferedReader(f);

		String nextLine = b.readLine();

		while(nextLine != null){

			//Skip comment lines (those beginning with "#")
			if (nextLine.startsWith("#")) {
				nextLine = b.readLine();
				continue;
			}
			String[] line = nextLine.split("\\s+");
			//ReadFile r = new ReadFile(line[0]);
			ReadFile r;

			//also ignore blank lines
			if (line != null && line.length ==1 && line[0].equals("")) {
				nextLine = b.readLine();
				continue;
			}
			if (line == null || line.length < 2 || line.length > 4) {
				System.err.println("ReadInput.java: Error parsing line " 
						+ nextLine + " so skipping it.");
				nextLine = b.readLine();
				continue;
			}
			//skip comments (beginning with "#")
			if (line[0].startsWith("#")) {
				nextLine = b.readLine();
				continue;
			}
			//Out.print2("ReadInput: in readFiles(): Reading " + line[1] + " file.");
			if(line[1].equals("ESP") || line[1].equals("PR") ) {
				r = new ReadESP(line[0],Integer.parseInt(line[2]),Integer.parseInt(line[3]), breakRegions,
						windowSize_);
			} else if(line[1].equals("CGH")) {
				r = new ReadCGH(line[0], breakRegions);
			}
			/*else if(line[1].equals("PRED")) 
			  r = new ReadPred(line[0], breakRegions);
			  else if(line[1].equals("SINGLEPRED"))
			  r = new ReadSinglePred(line[0], breakRegions);
			  else if(line[1].equals("CGH")){
			  r = new ReadCGH(line[0], breakRegions);
			  }*/
			/*if(line[3].equals("p")){
			  ReadFile_CGH_shorter r = new ReadFile_CGH_shorter(line[0],Integer.parseInt(line[1]),Integer.parseInt(line[2]),sep);
			  }*/
			else {
				System.err.println("ReadInput.java: Encountered line of unknown type: "
						+ line[1] + " so skipping line."); 
				nextLine = b.readLine();
				continue;
			}
			readFiles.add(r);


			nextLine = b.readLine();
		}//end while

		b.close();
		f.close();
		return readFiles;
	}

	/**
	 *  This reads break regions from a batch of files by creating the appropriate 
	 *  ReadFile object for each file in the batch 
	 *  and then getting the next "window" of break regions from it and returning them in the ArrayList.
	 *  Call this method multiple times until the end of the last file is reached.
	 *  @return true if end of all files reached, false otherwise
	 */
	public boolean readWindowFromFiles(int leftChr, int rightChr,
			ArrayList<BreakRegion> br) throws IOException {

		int numFiles = 0;
		//only need to initialize readFiles_ variable first time through
		if (readFiles_ == null) {
			// the constructor for the ReadFile objects takes an ArrayList<BreakRegion>
			// but it will never be used in this case, so just pass null
			readFiles_ = this.createReadFiles(null);
			if (readFiles_.isEmpty()) {
				Out.print("WARNING: " + filename_ + " listed no parsable files!!");
				return true;
			}
			numFiles = readFiles_.size();
			arrayOfReadFiles_ = new ReadFile[numFiles];
			curPosArray_ = new int[numFiles];
			endWindowPosArray_ = new int[numFiles];
			for (int i=0; i<numFiles; ++i) {
				arrayOfReadFiles_[i] = readFiles_.get(i);
			}
		} else {
			numFiles = readFiles_.size();
		}

		//if starting on a new left, right chr pair, then need to reset to the starting values
		boolean startNewChr = false;
		if ((leftChr != curLeftChr_) || (rightChr != curRightChr_)) {
			startNewChr = true;
			curLeftChr_ = leftChr;
			curRightChr_ = rightChr;
		}
	
		boolean allFilesProcessed = true;
		for (int i=0; i<numFiles; ++i) {
			//when all data has been read from a file, it will be set to null
			if (arrayOfReadFiles_[i] == null) {
				continue;
			}
			if (startNewChr) {
				curPosArray_[i] = 0;	
				endWindowPosArray_[i] = 0;
				endWindowPos_ = 0;
			}
			curPosArray_[i] = endWindowPosArray_[i];
			endWindowPosArray_[i] = curPosArray_[i] + windowSize_;
			endWindowPos_ = endWindowPosArray_[i];

			// In this call, the file is assumed to be sorted by chromosomes and 
			// genomic coordinates.
			//this will only read the break regions in the next "window" 
			//delimited by Constants.WIN_SIZE_FACTOR *lmax. If there are no more break regions for 
			//the specified  chromosomes, no more break regions will be added to the ArrayList
			// and a flag will be set to make getDiffChrPairReached() return true
			/** 
			 * IT IS UP TO THE USER TO DETECT THIS CONDITION BY CALLING getDiffChrPairReached()!!!
			 */
			boolean endOfFileReached = arrayOfReadFiles_[i].readNextBreakRegions(leftChr, rightChr, br, 
					endWindowPosArray_[i], startNewChr);

			//set flag if not yet done with this file
			if (!endOfFileReached) {
				allFilesProcessed = false;
			} else {
				arrayOfReadFiles_[i] = null;
			}
		}
		Out.print2("DEBUG: readWindowFromFiles() endWindowPos_: " + endWindowPos_);
	
		return allFilesProcessed;
	}

	/**
	  * Parses the files for break regions and returns them in the provided breakRegionsArray list 
	  */
	public void readFiles(ArrayList<BreakRegion>[][] breakRegionsArray) 
			throws IOException {
		readFiles(-1, -1, breakRegionsArray);
	}


	/**
	  * Parses the files for break regions with left chromosome in "leftChr" and right chromosome
	  * in "rightChr" and returns these regions in an ArrayList.  
	  */
	public ArrayList<BreakRegion> readFiles(int leftChr, int rightChr) throws IOException{
		return readFiles(leftChr, rightChr, null);
	}

	/**
	  * If the breakRegions argument is null, then this method
	  * parses the files for break regions with left chromosome in "leftChr" and right chromosome
	  * in "rightChr" and returns these regions in an ArrayList.  
	  * Else if the breakRegions argument is not null, leftChr and rightChr are ignored and
	  * all break regions will be read into the breakRegions 2D array.
	  */
	private ArrayList<BreakRegion> readFiles(int leftChr, int rightChr, 
			ArrayList<BreakRegion>[][] breakRegionsArray) throws IOException {
		//ArrayList<BreakRegion>[][] breakRegions 
		////	= new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];

		ArrayList<BreakRegion> breakRegions = null;

		//if 2D array is null, need to provide an ArrayList for ReadFile constructors
		// in which BreakRegion's can be placed
	       	if (breakRegionsArray == null) {
			breakRegions = new ArrayList<BreakRegion>();
		}

		ArrayList<ReadFile> readFiles = this.createReadFiles(breakRegions);	

		for (int i=0; i<readFiles.size(); ++i) {
			ReadFile r = readFiles.get(i);
			// if no 2D array provided, will only be storing regions from the specified chromosomes
			if (breakRegionsArray == null) {
				r.readBreakRegions(leftChr, rightChr);
				//Out.print2("ReadInput: Loaded " + breakRegions.size() + " Break Regions for chr" 
						//+ leftChr + ", chr" + rightChr); 
			} else {
				//otherwise store  all regions in the provided array
				r.readBreakRegions(breakRegionsArray);
			}
			
		}
			//r.makeSingletons();
			
			//ArrayList<Cluster>[][] clust = r.getShapesArray();
			//clusters.add(clust);
			//ArrayList<BreakRegion>[][] clon = r.getCloneArray();
			//clones.add(clon);

		return breakRegions;
		//ArrayList[] datasets = new ArrayList[] {clusters, clones};
		//return datasets;
	}

	/**
	  * Returns true if a read with a chromosome pair other than the target chromosome pair is encountered
	  * in the "window" reading mode.
	  */
	public boolean getDiffChrPairReached() {
		//actually should never get called unless singleReadFile_ was initialized, so just let a 
		// NullPointerException happen if it ever does for easier debugging.
		//if (singleReadFile_ == null) {
		//	return false;
		//}

		//Out.print2("callin getDiffChrPairReached()");
		//for most modes, deal with one file at a time, so just query the current file
		if (arrayOfReadFiles_ == null) {
			return singleReadFile_.getDiffChrPairReached();
		}
		//Otherwise in --batch and window reading mode, so need to query all files
		// to make sure that all of them have reached a line w/non-matching chromosome pair
		// i.e. if even one has not yet reached a diff chr pair, return false
		boolean diffChrPairReached = true;
		boolean filesDone = true;
		for (int i=0; i<(readFiles_.size()); ++i) {

			//ignore files that are done for now
			if (arrayOfReadFiles_[i] != null) {
				filesDone = false;

				//if encounter any where a diffChrPair has not yet been reached, 
				// will need to keep reading from the current chr pair
				if (!arrayOfReadFiles_[i].getDiffChrPairReached()) {
					diffChrPairReached = false;
					break;
				}
			}
		}
		//in special case where all files are done, we actually have NOT reached a diff chr pair
		if (filesDone) {
			diffChrPairReached = false;
		}
		//Out.print2("getDiffChrPairReached() returning "+diffChrPairReached);
		return diffChrPairReached;
	}
	
	/**
	 * Reads a batch file and finds the maximum lmax value listed in it.
	 */
	public static int findMaxLmaxInBatchFile(String filename) throws IOException{
		Out.print2("findMaxLmaxInBatchFile for file " + filename);
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		int maxLmax = 0;
		int curLmax = 0;
		String nextLine = br.readLine();

		while(nextLine != null){

			//Skip comment lines (those beginning with "#")
			if (nextLine.startsWith("#")) {
				nextLine = br.readLine();
				continue;
			}
			String[] line = nextLine.split("\\s+");

			if (line == null || line.length < 2 || line.length > 4) {
				System.err.println("ReadInput.findMaxLmaxInBatchFile(): Error parsing line " 
						+ line + " so skipping it.");
				nextLine = br.readLine();
				continue;
			}
			//skip comments (beginning with "#")
			if (line[0].startsWith("#")) {
				nextLine = br.readLine();
				continue;
			}
			//Out.print2("ReadInput: in readFiles(): Reading " + line[1] + " file.");
			if(line[1].equals("ESP") || line[1].equals("PR"))
				curLmax = Integer.parseInt(line[3]);
			/*else if(line[1].equals("PRED")) 
			  r = new ReadPred(line[0], breakRegions);
			  else if(line[1].equals("SINGLEPRED"))
			  r = new ReadSinglePred(line[0], breakRegions);
			  else if(line[1].equals("CGH")){
			  r = new ReadCGH(line[0], breakRegions);
			  }*/
			/*if(line[3].equals("p")){
			  ReadFile_CGH_shorter r = new ReadFile_CGH_shorter(line[0],Integer.parseInt(line[1]),Integer.parseInt(line[2]),sep);
			  }*/
			else {
				System.err.println("ReadInput.findMaxLmaxInBatchFile(): Encountered line of unknown type: "
						+ line[1] + " so skipping line."); 
				nextLine = br.readLine();
				continue;
			}
			if (curLmax > maxLmax) {
				maxLmax = curLmax;
			}


			nextLine = br.readLine();
		}//end while

		br.close();
		fr.close();
		Out.print2("findMaxLmaxInBatchFile returning " + maxLmax);
		return maxLmax;
	}

	/**
	 * Reads in all CGH data from the single file that match a given chromosome.
	 * Assumes file is sorted so can always seek to the proper position after
	 * at least one pass through
	 */
	public void readSingleCGHFileByChr(int chr, ArrayList<BreakRegion> cgh) throws IOException {

		//lmin and lmax don't matter for CGH, just pass -1
		if (singleReadFile_ == null) {
			singleReadFile_ = this.createReadFile(-1, -1, null);
		}
	
		int chrIdx = chr -1;

		//only look at CGH files
		if ((singleReadFile_ instanceof ReadCGH) && (chrFilePosStart_[chrIdx] > -1)) {
			ReadCGH readCGH = (ReadCGH) singleReadFile_;
			long filePos = 0;
			if (chrFilePosStart_[chrIdx] > 0) {
				filePos = chrFilePosStart_[chrIdx];
			}
			long[] ret = readCGH.readBreakRegions(chr, cgh, filePos);

			// initialize some stuff if necessary
			if (ret[0] > 0) {
				if (chrFilePosStart_[chrIdx] == 0) {
					chrFilePosStart_[chrIdx] = ret[0];
				}
			}

			//if we reached a new chr before end of file, we know then the
			//starting position for the next chromosome.
			if (ret[1] > -1) {
				if (chrIdx+1 >= GASVMain.NUM_CHROM) {
					Out.print("ERROR file is not in sorted order by "
						 + "chromosome!! Sort file first before running GASV!!");
				}
				chrFilePosStart_[chrIdx+1] = ret[1];
			}
			//otherwise we reached the end of file 
			else {
				//set the rest of the file start positions to -1 to signal that file is done
				for (int j=chrIdx+1; j<GASVMain.NUM_CHROM; ++j) {
					chrFilePosStart_[j] = -1;
				}
			}
		} else if (chrFilePosStart_[chrIdx] > -1) { 
			Out.print("ERROR: CGH file does not appear to be a valid CGH file");
		}
		//need to handle end condition when reach end of file before hitting end of all chr?
		// Say last data in the files was for chr22. Then obviously no more data
	        // for chr23 or 24 and their array values won't be initialized.  Subsequent
		// attempts to read chr23 or 24	CGH data will result in starting from the 
		// very beginning of the very first file, and nothing will be found.
		// So we're OK.
	}
	/**
	 * Reads in all CGH data from the files that match a given chromosome.
	 * Assumes files are sorted so can always seek to the proper position after
	 * at least one pass through
	 */
	public void readCGHFilesByChr(int chr, ArrayList<BreakRegion> cgh) throws IOException {

		//only need to initialize readFiles_ variable first time through
		if (readFiles_ == null) {
			//will want to dynamically fill in potentialy different cgh arrays, so 
			// don't bother giving a single cgh reference
			readFiles_ = this.createReadFiles(null);
			if (readFiles_.isEmpty()) {
				Out.print("WARNING: " + filename_ + " listed no parsable files!!");
			}
			for (int i = 0; i<readFiles_.size(); ++i) {
				//java automatically initializes values to 0
				long[] curChrFilePosStart = new long[GASVMain.NUM_CHROM];
				chrFilePosStartList_.add(curChrFilePosStart);
			}
		}

		int chrIdx = chr -1;

		//int start = 0;
		//if (readCGHFileIdx_[chrIdx] != -1) {
			//start = readCGHFileIdx_[chrIdx]; 
		//}

		for (int i=0; i<readFiles_.size(); ++i) {
			ReadFile r = readFiles_.get(i);
			long[] curChrFilePosStart = chrFilePosStartList_.get(i);
			//only look at CGH files
			//if (r instanceof ReadCGH) {
			if ((r instanceof ReadCGH) && (curChrFilePosStart[chrIdx] > -1)) {
				ReadCGH readCGH = (ReadCGH) r;
				//long filePos = 0;
				//if (i == readCGHFileIdx_[chrIdx]) {
					//filePos = chrFilePosStart_[chrIdx];
				//}
				long filePos =  curChrFilePosStart[chrIdx];
				long[] ret = readCGH.readBreakRegions(chr, cgh, filePos);
				
				// initialize some stuff if necessary
				if (ret[0] > -1) {
					//if (readCGHFileIdx_[chrIdx] == -1) {
						//readCGHFileIdx_[chrIdx] = i; 
						//chrFilePosStart_[chrIdx] = ret[0];
					//}
					curChrFilePosStart[chrIdx] = ret[0];
				}

				//if we reached a new chr before end of file, we know then the
				//starting position for the next chromosome.
				if (ret[1] > -1) {
					if (chrIdx+1 >= GASVMain.NUM_CHROM) {
						Out.print("ERROR file is not in sorted order by "
								+ "chromosome!! Sort file first before running GASV!!");
					}
					//readCGHFileIdx_[chrIdx+1] = i;
					//chrFilePosStart_[chrIdx+1] = ret[1];
					curChrFilePosStart[chrIdx+1] = ret[1];
					//break;
				}
				//otherwise we reached the end of file 
				else {
					//set the rest of the file start positions to -1 to signal that file is done
					for (int j=chrIdx+1; j<GASVMain.NUM_CHROM; ++j) {
						curChrFilePosStart[j] = -1;
					}
				}
				//otherwise we reached the end of file and the last line of the file
				// was still a matching CGH data.
				// So should keep looking in the next file for more data.
				///if (ret[0] == -1 && ret[1] == -1) {
					//if (chrIdx+1 < GASVMain.NUM_CHROM) {
						//readCGHFileIdx_[chrIdx+1] = i;
						//chrFilePosStart_[chrIdx+1] = 0;
					//}
					//break;
				//}
				
			} else if (curChrFilePosStart[chrIdx] > -1) { //if (r != null) { //instanceof operator outputs false if reference was null
				Out.print("In --batch mode, a CGH input file was specified that does not appear to "
						+ "be a valid CGH input file: " + r.getFileName());
			}
		}
		//need to handle end condition when reach end of ALL files before hitting end of all chr?
		// Say last data in the files was for chr22. Then obviously no more data
	        // for chr23 or 24 and their array values won't be initialized.  Subsequent
		// attempts to read chr23 or 24	CGH data will result in starting from the 
		// very beginning of the very first file, and nothing will be found.
		// So we're OK.
	}

	public int getEndWindowPos() {
		return endWindowPos_;
	}
}
