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
import gasv.common.Constants;
import gasv.common.Out;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
/**
Class: FilterESP
Testing commit
*/
public class FilterESP{


	public static void filterESP(String refFile, String targetFile) throws IOException, CloneNotSupportedException, NullPointerException{
						
		ArrayList<BreakRegion> cLocal = new ArrayList<BreakRegion>();
	
		ReadInput Kidd = new ReadInput(refFile);

		System.out.println("Reference ESP File: " + refFile);		
	
		ReadInput WashU = new ReadInput(targetFile);

		ArrayList<BreakRegion>[][] breakRegionsK = null; 
		ArrayList<BreakRegion>[][] breakRegionsW = null; 
		int lminKidd = GASVMain.LMIN;
		int lmaxKidd = GASVMain.LMAX;
		int lminWashU = GASVMain.LMIN2;
		int lmaxWashU = GASVMain.LMAX2;

		boolean useFast = GASVMain.USE_FAST;
		boolean useBatch = GASVMain.USE_BATCH;
		boolean saveMemory = GASVMain.SAVE_MEMORY;
		boolean washuDone = false; 
		boolean kiddDone = false;

		//in fast mode, read PES from all chromosomes into memory at once
		if (useFast) {
			breakRegionsK = new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];
			breakRegionsW = new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];
			if (useBatch) {
				Kidd.readFiles(breakRegionsK);
				WashU.readFiles(breakRegionsW);
			} else {
				Kidd.readSingleFile(lminKidd, lmaxKidd, breakRegionsK);
				WashU.readSingleFile(lminWashU, lmaxWashU, breakRegionsW);
			}

			//indicate that all data from files has now been read
			washuDone = true;
			kiddDone = true;
		}

		String washUNameMinusPath = targetFile;
		int idxOfSlash = targetFile.lastIndexOf("/");
		if (idxOfSlash != -1) {
			washUNameMinusPath = targetFile.substring(idxOfSlash + 1);
		}
		//ArrayList<BreakRegion> [][] WashU_CS = WashU.readFiles();

		String retained = GASVMain.OUTPUT_DIR + washUNameMinusPath + ".retained";
		String removed = GASVMain.OUTPUT_DIR + washUNameMinusPath + ".removed";
		
		File retainedFile = new File(retained);
		File removedFile = new File(removed);
		if (retainedFile.exists()) {
			retainedFile.delete();
		}
		if (removedFile.exists()) {
			removedFile.delete();
		}
		BufferedWriter retainedWriter = new BufferedWriter(new FileWriter(retainedFile));
		BufferedWriter removedWriter  = new BufferedWriter(new FileWriter(removedFile));
		if (GASVMain.USE_HEADER) {
			String headerLine = "# Name:\t    Left Chr:  Left Start:    Left End:  Left Orient: Right Chr:  Right Start:  Right End:  Right Orient:\n";

			retainedWriter.write(headerLine);
			removedWriter.write(headerLine);
		}
		
		System.out.println("Target ESP File: " + targetFile);		
		System.out.println("Output Files:");
		System.out.println("\tRetained: " + retainedFile);
		System.out.println("\tRemoved: " + removedFile);


		ArrayList<Clone>                  cloneList     = new ArrayList<Clone>();
		ArrayList<Clone>                  borderClones = new ArrayList<Clone>();
		ArrayList<Clone>                  cloneListKidd = new ArrayList<Clone>();
		ArrayList<Clone>                  borderKiddClones = new ArrayList<Clone>();

		// cloneOverlaps should just hold a list of booleans.
		// It doesn't seem like the list of integers is ever used, just 
		// testing to see whether any integers were ever added to the list.
		//ArrayList<ArrayList<Integer>>     cloneOverlaps = new ArrayList<ArrayList<Integer>>();
		ArrayList<AtomicBoolean>	  cloneOverlaps = new ArrayList<AtomicBoolean>();
		ArrayList<PriorityQueue<Integer>> clusters      = new ArrayList<PriorityQueue<Integer>>();
		ArrayList<Integer>                clusterLabel  = new ArrayList<Integer>();
		ArrayList<Integer>                visited       = new ArrayList<Integer>();
		
		//Step 2: Add all the Add/Remove Events for all clones!
		//(***) Need a way to add more clones (different sizes/etc) to the clone array!(***)
		//NORMAL VERSION: No debugging Information:
		
		int k = 0;

		for(int i = 0; i<GASVMain.NUM_CHROM; i++){
			for(int j = i; j<GASVMain.NUM_CHROM; j++){
				int chrx = i+1; int chry = j+1;
				//ArrayList<BreakRegion> d = Kidd_CS[i][j];
				//ArrayList<BreakRegion> c = WashU_CS[i][j];
				Out.print1("FilterESP: processing chr " + chrx + ", chr" + chry);

				ArrayList<BreakRegion> c = null;
				ArrayList<BreakRegion> d = null;
				do {
					int endWindowPos = 0;
				if (useFast) {
					c = breakRegionsW[i][j];
					d = breakRegionsK[i][j];
				} else {
					//if (saveMemory) {
					if (c == null) { 
						c = new ArrayList<BreakRegion>();
					}
					if (d == null) { 
						d = new ArrayList<BreakRegion>();
					}
					Out.print2("BEFORE READING: Reference end window pos = " + endWindowPos + " chrs " + chrx + " and " + chry);
					if (useBatch) {
						if (!washuDone) {
							Out.print2("FilterESP: Reading Cancer window.");
							washuDone = WashU.readWindowFromFiles(chrx, 
									chry, c);
						}
						if (!kiddDone) {
							Out.print2("FilterESP: Reading Normal window.");
							kiddDone = Kidd.readWindowFromFiles(chrx, 
									chry, d);
						}
					} else {
						if (!washuDone) {
							//Out.print2("FilterESP: Reading Cancer window.");
							washuDone = WashU.readWindowFromSingleFile(chrx, 
									chry, lminWashU, lmaxWashU, c);
						}
						if (!kiddDone) {
							//Out.print2("FilterESP: Reading Normal window.");
							kiddDone = Kidd.readWindowFromSingleFile(chrx, 
									chry, lminKidd, lmaxKidd, d);
						}
					}
					endWindowPos = Kidd.getEndWindowPos();
					Out.print2("AFTER READING: Reference end window pos = " + endWindowPos + " chrs " + chrx + " and " + chry);
					//update window of reference clones with clones from previous window that 
					//bordered on this window
					c.addAll(borderClones);
					d.addAll(borderKiddClones);
					if (!borderClones.isEmpty() || !borderKiddClones.isEmpty()) {
						Out.print2("FilterESP: added " + borderClones.size()
								+ " borderClones.");
						Out.print2("FilterESP: added " + borderKiddClones.size()
								+ " borderKiddClones.");
					}
					borderClones.clear();
					borderKiddClones.clear();
					if (!d.isEmpty() || !c.isEmpty()) {
						Out.print2("FilterESP: Loaded " + d.size()
								+ " normal break regions.");
						Out.print2("FilterESP: Loaded " + c.size()
								+ " cancer break regions.");
					}
				}
					

				//If there are NO clones for filtering in Reference output all!
				if ((c!= null && d == null) || (!c.isEmpty() && d.isEmpty())){
					for(k = 0; k<c.size(); k++){
						Clone c1 = (Clone) c.get(k);							
						//But for border clones don't write them out until next window in case they overlap
						// with some Kidd clones in the next window
						if (isBorderClone(c1, endWindowPos)) {
							borderClones.add(c1);
						} else {
							retainedWriter.write(c1.toOutput()+"\n");
						}
					}
				}
				else if ((c!=null && d!= null) && (!c.isEmpty() && !d.isEmpty())){
					Out.print2("FilterESP: Going in the forward direction.");

					int numKiddClones = 0;
					int numClones = 0;
					for(k = 0; k< c.size(); k++){
						Clone c1 = (Clone) c.get(k);
						if(c1.getType().equals("same")){
							cloneList.add(numClones,c1);
							//cloneOverlaps.add(new ArrayList<Integer>());
							cloneOverlaps.add(new AtomicBoolean(false));
							//cloneOverlaps.get(numClones).add(new Integer(numClones));
							cloneList.get(numClones).setNumber(numClones);
							numClones++;
						}
					}//End for
										
					for(k = 0; k< d.size(); k++){
						Clone c1 = (Clone) d.get(k);
						if(c1.getType().equals("same")){
							//add all reference ESP's (+x orientation) within LMax of end of 
							//current window into next window. 
							if (isBorderClone(c1, endWindowPos)) {
								borderKiddClones.add(c1);
							}
							cloneListKidd.add(numKiddClones,c1);
							numKiddClones++;
						}
					}
					
					for(int n1 = 0; n1<numClones; n1++){
						for(int n2 = 0; n2<numKiddClones; n2++){
							if(GASVMain.overlap(cloneList.get(n1),cloneListKidd.get(n2))>0){
								//cloneOverlaps.get(n1).add(n2);
								cloneOverlaps.get(n1).set(true);
							}
						}
					}
				
					for(int n1 = 0; n1<numClones; n1++){
						//We start off with 1 overlap!
						//if(cloneOverlaps.get(n1).size() == 1){
						if(cloneOverlaps.get(n1).get() == false){
							//For border clones don't write them out until next window in case they overlap
							// with some Kidd clones in the next window
							if (isBorderClone(cloneList.get(n1), endWindowPos)) {
								borderClones.add(cloneList.get(n1));
							} else {
								//System.out.println(cloneList.get(n1).toOutput());
								retainedWriter.write(cloneList.get(n1).toOutput()+"\n");
							}
						}
						else{
							removedWriter.write(cloneList.get(n1).toOutput()+"\n");
						}
					}
					
					//GOING THROUGH THE REVERSE DIRECTION NOW!
					Out.print2("FilterESP: Now going through the reverse direction.");
					numClones = 0;
					numKiddClones = 0;
					cloneList.clear();
					cloneListKidd.clear();
					cloneOverlaps.clear();
					clusterLabel.clear();
					clusters.clear();
					visited.clear();

					//for(k = 0; k< c.size(); k++){
					while (c.size() > 0) {
						//Clone c1 = (Clone) c.get(k);
						Clone c1 = (Clone) c.remove(0);
						if(c1.getType().equals("different")){
							cloneList.add(numClones,c1);
							//cloneOverlaps.add(new ArrayList<Integer>());
							//cloneOverlaps.get(numClones).add(new Integer(numClones));
							cloneOverlaps.add(new AtomicBoolean(false));
							cloneList.get(numClones).setNumber(numClones);
							numClones++;						
						}
					}//End For
					
					//for(k = 0; k<d.size(); k++){
					while (d.size() > 0) {
						//Clone c1 = (Clone) d.get(k);
						Clone c1 = (Clone) d.remove(0);
						if(c1.getType().equals("different")){
							//add all reference ESP's (+x orientation) within LMax of end of 
							//current window into next window. 
							if (isBorderClone(c1, endWindowPos)) {
								borderKiddClones.add(c1);
							}
							cloneListKidd.add(numKiddClones,c1);
							numKiddClones++;
						}
					}

					for(int n1 = 0; n1<numClones; n1++){
						for(int n2 = 0; n2<numKiddClones; n2++){
							if(GASVMain.overlap(cloneList.get(n1),cloneListKidd.get(n2))>0){
								//cloneOverlaps.get(n1).add(n2);
								cloneOverlaps.get(n1).set(true);
							}
						}
					}
			

					for(int n1 = 0; n1<numClones; n1++) {
						//if(cloneOverlaps.get(n1).size() == 1){
						if(cloneOverlaps.get(n1).get() == false) {
							//For border clones don't write them out until next window in case they overlap
							// with some Kidd clones in the next window
							if (isBorderClone(cloneList.get(n1), endWindowPos)) {
								borderClones.add(cloneList.get(n1));
							} else {
								//System.out.println(cloneList.get(n1).toOutput());
								retainedWriter.write(cloneList.get(n1).toOutput()+"\n");
							}
						}
						else {
							removedWriter.write(cloneList.get(n1).toOutput()+"\n");
						}	
					}	
							
				}//End c!= Null

				if (c != null) {
					c.clear();
				}
				if (d != null) {
					d.clear();
				}

				//keep going as long as we're not using --fast mode 
				// and either file still has more data to be read
				// and either file has not yet reached the next chr pair
				} while (saveMemory && (!washuDone || !kiddDone) && (!WashU.getDiffChrPairReached() || !kiddDone)
						&& (!Kidd.getDiffChrPairReached() || !washuDone)
						&& (!WashU.getDiffChrPairReached() || !Kidd.getDiffChrPairReached()) );
				if (washuDone) {
					retainedWriter.close();
					removedWriter.close();
					Out.print2("End of Target File reached");
					return;
				}
		}//End j;
		
	}//End i;
	
	retainedWriter.close();
	removedWriter.close();

		// this if-conditional is actually redundant since washuDone must be false at this point
		// but leave conditional in for clarity
		if (!washuDone) {
			Out.print("WARNING: Finished looping through all possible Chr combinations, but didn't make it"
				       + " through all input files! The inputs files are either not sorted "
				       + "properly or have additional data with chromosome number > " 
				       + GASVMain.NUM_CHROM + "!");
		}
	}

	private static boolean isBorderClone(Clone c, int endWindowPos) {
		boolean ret = false;
		double x = c.getX();
		if (x > 0 && (x + c.getLmax()) > endWindowPos) {
			ret = true;
		}
		return ret;
	}
}
