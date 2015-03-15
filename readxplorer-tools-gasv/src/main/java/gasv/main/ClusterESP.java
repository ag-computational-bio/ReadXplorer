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
import java.text.*;
import gasv.geom.*;
/**
Class: ClusterESP

*/
public class ClusterESP{

	/** note that none of these have a trailing newline, so need to add it */
	private static final String ESP_HEAD_STD = "#Cluster_ID:\tLeftChr:\tLeftBreakPoint:\tRightChr:\tRightBreakPoint:\tNum PRS:\tLocalization:\tType:";
	private static final String ESP_HEAD_READS = "#Cluster_ID:\tLeftChr:\tLeftBreakPoint:\tRightChr:\tRightBreakPoint:\tNum PRS:\tLocalization:\tType:\tList of PRS:";
	private static final String ESP_HEAD_REGIONS = "#Cluster_ID:\tNum PRS:\tLocalization:\tType:\tList of PRS:\t LeftChr:\tRightChr:\tBoundary Points:";
	private static RandomAccessFile tmpFile = null;
	private static ArrayList<BreakRegion> cLocal = new ArrayList<BreakRegion>();

	//Printing at most one decimal point.
	private static DecimalFormat df_ = new DecimalFormat("#.#");

	private static ArrayList<Clone>                  cloneList_     = new ArrayList<Clone>();
	private static int CLUSTER_NUMBER = 1;
	private static int CGH_NUMBER = 1;
	private static BufferedWriter clusterWriter_ = null;
	private static double sameRightMostTrapEnd_ = 0;
	private static double samePrevRightMostTrapEnd_ = 0;
	private static double diffRightMostTrapEnd_ = 0;
	private static double diffPrevRightMostTrapEnd_ = 0;
	private static int LABEL = -1;
	private static int chrx = -1;
	private static int chry = -1;
	private static final String cghHeaderLine = "#CGH_ID:\t            Chr1: \tChr2:\t "
				+ "Num Clusters:\t Cluster List (ClusterID"
				+ Constants.SEP+ "NumPES" + Constants.SEP + "Localization" + Constants.SEP 
				+ "List of PES" + Constants.SEP + "ChrM" + Constants.SEP + "ChrN" 
				+ Constants.SEP + "Boundary Points)\n";
	private static final String SAME = "same";
	private static final String DIFFERENT = "different";
	private static final String TMPFILENAME = Constants.GASV_TMP_NAME + "." + System.currentTimeMillis() 
				+ java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	private static final CloneComparator COMPARATOR = new CloneComparator();

	// values used for saving previous window state
	private static ArrayList<Cluster> sameClusterList = null;
	private static ArrayList<Cluster> diffClusterList = null;


	public static LinkedList polyToList(Poly p){
		LinkedList poly = new LinkedList();
		for(int i = 0; i < p.getNumPoints(); i++){
			int coord[] = {(int) p.getX(i), (int) p.getY(i)};
			poly.add(coord);
		}
		return poly;
	}
	
	public static String printPoly(Poly p){
		LinkedList coords = polyToList(p);
		String toReturn = "";
		Iterator iter = coords.iterator();
		while(iter.hasNext()){
			int[] pair = (int[]) iter.next();
			toReturn = toReturn + pair[0] + ", " + pair[1];
			if(iter.hasNext()){ toReturn = toReturn + ", ";}
		}
		return toReturn;
	}

	private static String printType(Cluster cluster) {
		boolean diffChrom = false;
		boolean pluMin = false;
		boolean minPlu = false;
		boolean pluPlu = false;
		boolean minMin = false;

		ArrayList<Clone> clones = cluster.getClones();
		if (clones.get(0).getChrX() != clones.get(0).getChrY()) {
			diffChrom = true;
		}
		for (int i=0; i<clones.size(); ++i) {
			Clone clone = clones.get(i);
			if (clone.getX() < 0) {
				if (clone.getY() < 0) {
					minMin = true;
				} else {
					minPlu = true;
				}
			} else {
				if (clone.getY() < 0) {
					pluMin = true;
				} else {
					pluPlu = true;
				}
			}
		}
		//shouldn't be able to mix SAME and DIFFERENT orientations
		if ((pluMin || minPlu) && (pluPlu || minMin)) {
			Out.print("ERROR: Encountered Mix of orientations in cluster!");
			System.exit(-1);
		}

		if (pluMin && minPlu) {
			if (diffChrom) {
				return "TR+";
			} else {
				return "DV";
			}
		} else if (pluPlu && minMin) {
			if (diffChrom) {
				return "TR-";
			} else {
				return "IR";
			}
		} else {
			if (diffChrom) {
				if (pluPlu) {
					return "TN-1";
				} else if (minMin) {
					return "TN-2";
				} else if (pluMin) {
					return "TN+1";
				} else if (minPlu) {
					return "TN+2";
				} else {
					Out.print("ERROR: parsing type, encountered impossible situation!");
					System.exit(-1);
				}
			} else {
				if (pluPlu) {
					return "I+";
				} else if (minMin) {
					return "I-";
				} else if (pluMin) {
					return "D";
				} else if (minPlu) {
					return "V";
				} else {
					Out.print("ERROR: UNABLE TO COMPUTE TYPE! Cluster has 0 clones??");
					System.exit(-1);
				}
			}

		}
		return null;
	}

	private static String printIntervalMaxMinNonCluster(Cluster cluster, int chrX, int chrY){
		double maxX = 0;
		double minX = Double.MAX_VALUE;
		double maxY = 0;
		double minY = Double.MAX_VALUE;

		ArrayList<Clone> clones = cluster.getClones();
		for (int i=0; i<clones.size(); ++i) {
			Clone clone = clones.get(i);
			double x = clone.getX();
			double y = clone.getY();
			if (x < 0) {
				x -= clone.getLmax();
			} else {
				x += clone.getLmax();
			}
			if (y < 0) {
				y -= clone.getLmax();
			} else {
				y += clone.getLmax();
			}


			if (Math.abs(x) < minX) {
				minX = Math.abs(x);
			} 
			if (Math.abs(x) > maxX) {
				maxX = Math.abs(x);
			}

			if (Math.abs(y) < minY) {
				minY = Math.abs(y);
			} 
			if (Math.abs(y) > maxY) {
				maxY = Math.abs(y);
			}
		}

		String retVal = (chrX + "\t" + (int)minX) + "," + ((int)maxX) + "\t" + chrY + "\t" + ((int)minY) + "," + ((int)maxY);
		return retVal;
	}

	//this mode will print the smallest x and largest y
	private static String printIntervalMaxMin(Cluster cluster, int chrX, int chrY){
		double minX = Double.MAX_VALUE;
		double maxX = -1;
		double minY = Double.MAX_VALUE;
		double maxY = -1;
		Poly p = cluster.getPoly();
		for(int i = 0; i < p.getNumPoints(); i++){
			double x = p.getX(i);
			double y = p.getY(i);
			if (x < minX) {
				minX = x;
			}
			if (x > maxX) { maxX = x;
			}
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}
		}
		String retVal = (chrX + "\t" + (int)minX) + "," + ((int)maxX) + "\t" + chrY + "\t" + ((int)minY) + "," + ((int)maxY);
		return retVal;
	}

	private static String printInterval(Cluster cluster){
		double maxPosX = 0;
		double minNegX = Double.MAX_VALUE;
		double maxPosY = 0;
		double minNegY = Double.MAX_VALUE;
		double outX, outY;

		ArrayList<Clone> clones = cluster.getClones();
		for (int i=0; i<clones.size(); ++i) {
			Clone clone = clones.get(i);
			double x = clone.getX();
			double y = clone.getY();
			if (x <0) {
				if (Math.abs(x) < minNegX) {
					minNegX = Math.abs(x);
				}
			} else { 
				if (Math.abs(x) > maxPosX) {
					maxPosX = Math.abs(x);
				}
			}
			if (y <0) {
				if (Math.abs(y) < minNegY) {
					minNegY = Math.abs(y);
				}
			} else {
				if (Math.abs(y) > maxPosY) {
					maxPosY = Math.abs(y);
				}
			}
		}

		if (maxPosX < minNegX && (maxPosX > 0)) {
			outX = maxPosX;
		} else {
			outX = minNegX;
		}

		if (maxPosY < minNegY && (maxPosY > 0)) {
			outY = maxPosY;
		} else {
			outY = minNegY;
		}

		return ((int)outX) + ", " + ((int)outY);
	}

	private static String printPolyNoSpaces(Poly p){
		LinkedList coords = polyToList(p);
		String toReturn = "";
		Iterator iter = coords.iterator();
		while(iter.hasNext()){
			int[] pair = (int[]) iter.next();
			toReturn = toReturn + pair[0] + "," + pair[1];
			if(iter.hasNext()){ toReturn = toReturn + ",";}
		}
		return toReturn;
	}
	public static void printQueue(PriorityQueue<Integer> Q){
		PriorityQueue<Integer> Temp = new PriorityQueue<Integer>();
		Temp.addAll(Q);
		int size = Temp.size();
		System.out.print("[");
		int i = 0;
		while(i < size){
			System.out.print(Temp.poll());
			if(i<(size-1)){ System.out.print(", ");}
			i++;
		}
		System.out.print("] ");
	}

	public static boolean clique(Cluster cluster) {
		ArrayList<Clone> clusterClones = cluster.getClones();
		int size = clusterClones.size();
		//java initializes boolean arrays to all false
		//boolean[][] visited = new boolean[size][size];
		//first need to set num overlaps for each clone
		int[] overlaps = new int[size];
		for (int i=0; i< size; ++i) {
			Clone clone = clusterClones.get(i);
			//by convention consider clone to overlap with itself
			//int numOverlaps = 1;
			overlaps[i]++;
			for (int j=i+1; j<size; ++j) {
				Clone clone2 = clusterClones.get(j);
				if (GASVMain.overlap(clone, clone2) > 0) {
					//clone.setNumOverlaps(clone.getNumOverlaps() +1);
					//clone2.setNumOverlaps(clone2.getNumOverlaps() +1);
					overlaps[i]++;
					overlaps[j]++;
				}
			}
		}
		
		boolean value = false;
		int marked = 0;
		for(int i = 0; i<size; i++){
			//if (clusterClones.get(i).getNumOverlaps() == size) {
			if (overlaps[i] == size) {
				marked++;
			}
		}
		if(marked == size){
			value = true;
		}
		return value;
	}

	public static void clusterESP(String fileName) throws IOException, CloneNotSupportedException, NullPointerException{
		
		String fileNameMinusPath = fileName;
		int idxOfSlash = fileName.lastIndexOf("/");
		if (idxOfSlash != -1) {
			fileNameMinusPath = fileName.substring(idxOfSlash + 1);
		}

		String cluster = GASVMain.OUTPUT_DIR + fileNameMinusPath + ".clusters";

		File clusterFile = new File(cluster);	
		if (clusterFile.exists()) {
			clusterFile.delete();
		}
		clusterWriter_ = new BufferedWriter(new FileWriter(clusterFile));
		
		if (GASVMain.USE_HEADER) {
			String headerLine = ESP_HEAD_STD;
			if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.STANDARD) {
				headerLine = ESP_HEAD_STD;
			} else if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.READS) {
				headerLine = ESP_HEAD_READS;
			} else if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.REGIONS) {
				headerLine = ESP_HEAD_REGIONS;
			}
			headerLine += "\n";
			clusterWriter_.write(headerLine);
		}

		boolean useFast = GASVMain.USE_FAST;
		boolean useBatch = GASVMain.USE_BATCH;
		
		ReadInput readInput = new ReadInput(fileName);
		
		ArrayList<BreakRegion>[][] breakRegions = null; 
		int lmin = GASVMain.LMIN;
		int lmax = GASVMain.LMAX;

		boolean saveMemory = GASVMain.SAVE_MEMORY;
		boolean fileDone = false;

		//in fast mode, read PES from all chromosomes into memory at once
		if (useFast) {
			breakRegions = new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];
			if (useBatch) {
				readInput.readFiles(breakRegions);
			} else {
				readInput.readSingleFile(lmin, lmax, breakRegions);
			}
			fileDone = true;
		}

				
		for(int i = 0; i<GASVMain.NUM_CHROM; i++){
			for(int j = i; j<GASVMain.NUM_CHROM; j++){
				chrx = i+1; 
				chry = j+1;

				ArrayList<BreakRegion> c = null;
				Out.print1("ClusterESP: processing chr " + chrx + ", chr" + chry);
				//if (fileDone) {
				//	Out.print1("ClusterESP: fileDone, shouldn't keep going...");
				//}
				do {
					if (useFast) {
						c = breakRegions[i][j];
					} else {
						if (saveMemory) {
							if (c == null) { 
								c = new ArrayList<BreakRegion>();
							}
						}
						if (useBatch) {
							if (saveMemory) {
								fileDone = readInput.readWindowFromFiles(chrx, 
										chry, c);
							} else {
								c = readInput.readFiles(chrx, chry);
							}
						} else {
							if (saveMemory) {
								fileDone = readInput.readWindowFromSingleFile(chrx, 
										chry, lmin, lmax, c);
							} else {
								c = readInput.readSingleFile(chrx, chry, lmin, lmax);
							}
						}
					}

					//check each cluster in (i) to see if it is within Lmax of 
					//the right-most cluster in i-1.  If so, check for actual overlap 
					//with clusters in (i-1) and remove any (i-1) clusters that overlap it.  
					//At the end of iteration i, output any remaining clusters from (i-1).

					if(c!=null){
						if (!c.isEmpty()) {
							Out.print2("ClusterESP: Loaded " + c.size() 
									+ " Break Regions for chr" 
									+ chrx + ", chr" + chry); 
						}

						//first go in forward direction 
						// pass false to indicate we're not finding split reads
						clusterHelper(c, SAME, false);

						//NOW GOING THROUGH THE REVERSE DIRECTION!
						clusterHelper(c, DIFFERENT, false);

						c.clear();
					}//End c!= Null
					samePrevRightMostTrapEnd_ = sameRightMostTrapEnd_;
					diffPrevRightMostTrapEnd_ = diffRightMostTrapEnd_;
					sameRightMostTrapEnd_ = 0;
					diffRightMostTrapEnd_ = 0;
				} while (saveMemory && !fileDone 
						&& !readInput.getDiffChrPairReached() );

				// reached the end of the input or the end of a chromosome,
				// so can now safely output all clusters in the current window
				// (usually there is a "delayed" write until the next window to
				// ensure that no duplicates are found
				// In the --fast case, only one giant window, so this final 
				// write will actually do all the work of writing everying.
				writeClusters(SAME);
				writeClusters(DIFFERENT);
				if (fileDone) {
					clusterWriter_.flush();
					clusterWriter_.close();
					return;
				}

				//reset prev window variables to initial values
				sameClusterList = null;
				diffClusterList = null;

			}//End j;
		}//End i;
		if (!fileDone) {
			Out.print("Warning: Finished looping through all possible Chr combinations, but did not finish the file."
					  + " It's likely input files were not sorted correctly.");
		}
		clusterWriter_.flush();
		clusterWriter_.close();
	
	}

	/**
	  * This is the version of clusterHelper to use if just want to do clustering of ESP's (no CGH data)
	  * This version will write out the clusters to file
	  */
	private static void clusterHelper(ArrayList<BreakRegion> c, String directionString, boolean findSplitReads)
			throws IOException {
		clusterHelper(c, directionString, null, null, null, findSplitReads);
	}
	
	/**
	  * This is the version of clusterHelper to use if just want to do clustering of ESP's along
	  * with CGH data. This version won't write anything to file, just store the cgh and cluster
	  * overlaps in the appropriate HashMap's.
	  */
	private static void clusterHelper(ArrayList<BreakRegion> c, String directionString, 
			HashMap<BreakRegion, ArrayList<Cluster>> cghxMap, 
			HashMap<BreakRegion, ArrayList<Cluster>> cghyMap,
			HashMap<BreakRegion, ArrayList<Cluster>> cghPairMap,
			boolean findSplitReads)
			throws IOException {

		/*if (c.size() > 1000) {
			Out.print2("clusterHelper() benchmarking for " + c.size() 
					+ " clones in direction " + directionString 
					+ ": at start of clusterHelper, time: " + System.currentTimeMillis());
		}*/
		int numClones = 0;
		cloneList_.clear();

		//if any of the current Clones in c overlap with previous clusters, remove those previous 
		//clusters (before they get written out), and add the associated clones instead to the 
		//current list, c.
		checkForOverlapsWithPrevClusters(c, directionString);

		/*if (c.size() > 1000) {
			Out.print2("clusterHelper() benchmarking for " + c.size() 
					+ " clones in direction " + directionString 
					+ ": after checking for overlaps with prev, time: " + System.currentTimeMillis());
		}
		*/
		
		//will be removing items from c within this loop, so c.size() will decrease, but we're adjusting
		// the counter variable 'k' within the loop to account for this 
		for(int k = 0; k< c.size(); k++){
			Clone c1 = (Clone) c.get(k);
			if(c1.getType().equals(directionString)){
				//Will process it and forget it, so to save some memroy,
				// remove from c and decrement the counter
				c.remove(k);
				k--;

				//keep track of the right most trapezoid boundary for this window
				// If read start was negative (leftward oriented), then just change to positive
	        		// But if read was positive the end of its trap would actually be the value + LMAX
				// In case there are multiple lmax values specified for different files, 
				//	use the max lmax to be safe
				double rightMostX = c1.getX();
				if (rightMostX < 0) {
					rightMostX = -rightMostX;
				} else {
					rightMostX = rightMostX + GASVMain.MAX_LMAX;
				}
				if (directionString.equals(SAME) 
							&& (rightMostX > sameRightMostTrapEnd_)) {
					sameRightMostTrapEnd_ =  rightMostX;
				} else if (directionString.equals(DIFFERENT) 
							&& (rightMostX > diffRightMostTrapEnd_)) {
					diffRightMostTrapEnd_ = rightMostX; 
				}

				//need to make sure that any old clustering information (if carried over from a previous window)
				// is cleared!!!
				c1.setClusterID(-1);


				//cloneList_.add(numClones,c1);
				cloneList_.add(c1);

			}
		}
		numClones = cloneList_.size();

		/*if (c.size() > 1000) {
			Out.print2("clusterHelper() benchmarking for " + numClones
					+ " clones in direction " + directionString 
					+ ": after pruning " + (c.size() - numClones) 
					+ "clones that didn't match direction, time: " + System.currentTimeMillis());
		}
		*/


		//this will be the list of clones completely unlabeled 
		//this will first be large, then shrink in size as more clones labeled
		ArrayList<Clone> unlabeledClones = new ArrayList<Clone>(cloneList_);

		//sort cloneList_ by bmin

		java.util.Collections.sort(unlabeledClones, COMPARATOR);
		/*Out.print2("clusterHelper() benchmarking for " + numClones
				+ " clones in direction " + directionString 
				+ ": after sorting unlabeledClones, time: " + System.currentTimeMillis());
		*/
		//Explore Clusters;
		ArrayList<Cluster> curClusterList = new ArrayList<Cluster>();
		LABEL = -1;
		while (unlabeledClones.size() > 0) {
			Clone cloneN = unlabeledClones.remove(0);

			// Each clusterLabel_ entry corresponds to a Clone and value is the LABEL pointer to the 
			// cluster to which it belongs in clusters 
			// Each entry in clusters is a PriorityQueue identifying clones in the cluster
			// If initializing a clone's cluster label, set up clusters structure and add the 
			// pointer to the clone as the first member of the cluster.
			LABEL++;
			cloneN.setClusterID(LABEL);
			Cluster tmpCluster = new Cluster(chrx, chry);
			tmpCluster.addClone(cloneN);
			curClusterList.add(tmpCluster);



			//for every Clone, if it belongs to the current LABEL cluster (at first, cluster
			// will contain only the initial Clone) and it hasn't yet been visited...
			ArrayList<Clone> tmpClusterClones = tmpCluster.getClones();
			for(int m = 0; m<tmpClusterClones.size(); m++) {
				Clone cloneM = tmpClusterClones.get(m);
				//count up the number of overlapping clones
				//by convention, consider each clone to be overlapping with itself
				// so start off at 1
				//move numOverlaps functionality into clique() calculation instead
				//this saves time for huge clusters (with --maxClusterSize mode)
				//at the expense of some redundant overlap computations for smaller clusters
				//int numOverlaps = 1;

				//THIS IS THE "MEAT" OF THE CODE, WHERE THE GRAPH OF OVERLAPS IS COMPUTED
				// ...then add all of its overlapping Clones to the current LABEL 
				// cluster and mark it (but not the overlapping clones) as visited
				// ...eventually those overlapping Clones will also be visited by
				// this loop and the Overlaps of those Overlaps will also be added
				// to the same cluster, if they are not already in it
				for(int b = 0; b<unlabeledClones.size(); b++){
					Clone cloneB = unlabeledClones.get(b);
					//if already in the cluster, don't need to compute overlap again!
					//unless we're counting numOverlaps (only necessary if not a huge cluster)
					if (GASVMain.overlap(cloneM, cloneB) > 0){
						cloneB.setClusterID(LABEL);
						// no need to add to huge clusters that will be ignored
						if (tmpCluster.getSize() <= GASVMain.MAX_CLUSTER_SIZE) {
							tmpCluster.addClone(cloneB);
						} 
						//has been labeled, so remove from unlabeled
						unlabeledClones.remove(b);
						//need to decrement to look at same index next iteration
						--b;
					} else if (cloneB.getBmin() > cloneM.getBmax()) {	
						//since unlabeledClones sorted by bmin, if current cloneB has 
						//bmin beyond the bounds of cloneM, then none of the subsequent
						//cloneB's will be near cloneM either. Can stop looking for this
						//cloneM.
						// QUESTION: should this be >= ?? From suzanne's overlap() method,
						// looks like colinear sides ARE considered same cluster!
						if (GASVMain.NORECIPROCAL_MODE) {
							//if non-reciprocal mode, can only take this shortcut
							// if they are the exact same orientations
							if (GASVMain.orientationsMatch(cloneB, cloneM)) {
								break;
							}

						} else { 
							break;
						}
					}
				}
			}//End For

		}//End Went through all clones;

		if (cghxMap == null && cghyMap == null && cghPairMap == null) {
			//for non-CGH (regular ESP clustering),
			//write the clusters out from the previous window in the same direction
			if (findSplitReads) {
				writeClustersAndSplitReads(directionString);
			} else {
				writeClusters(directionString);
			}
		} else {
			// For clustering w/CGH, first find overlaps between CGH regions and ESP clusters
			// and then write them to file
			findCGHAndESPOverlaps(directionString, cghxMap, cghyMap, cghPairMap);
			if (cghPairMap != null) {
				printCGHClusterEntries(cghPairMap);
				clearCGHClusterMap(cghPairMap);
			} else {
				printCGHClusterEntries(cghxMap);
				clearCGHClusterMap(cghxMap);
				if (cghyMap != null) {
					printCGHClusterEntries(cghyMap);
					clearCGHClusterMap(cghyMap);
				}
			}
		}

		// Go through clusters and determine whether each is a clique	
		for(int b = 0; b<=LABEL; b++){
			Cluster curCluster = curClusterList.get(b);
			//can ignore clusters larger than max cluster size
			if (curCluster.getSize() <= GASVMain.MAX_CLUSTER_SIZE) {
				if (curCluster.getSize() <= GASVMain.MAX_CLIQUE_SIZE) {
					boolean isClique = clique(curCluster);
					//this sets the boolean member variable and also initializes the cluster's 
					//intersection polygon if it's a clique
					curCluster.setIsCliqueAndMakePoly(isClique);
				} else {
					Out.print1("Huge cluster of size " + curCluster.getSize() + " encountered, automatically assuming that it's NOT a clique!!");
					curCluster.setIsCliqueAndMakePoly(false);
				}
			} else {
				Out.print1("Ignoring cluster exceeding max cluster size of " + GASVMain.MAX_CLUSTER_SIZE + " containing read: " + curCluster.getClones().get(0));
				//delete clusters larger than the max cluster size
				curClusterList.set(b, null);
			}
		}

		// save the current clusters so that we can write them out after next window
		if (directionString.equals(SAME)) {
			sameClusterList = curClusterList;
		} else if (directionString.equals(DIFFERENT)) {
			diffClusterList = curClusterList;
		} 
	}

	//if any of the current Clones in c overlap with previous clusters, remove those previous 
	//clusters (before they get written out), and add the associated clones instead to the current list, c.
	private static void checkForOverlapsWithPrevClusters(ArrayList<BreakRegion> c, String direction) {

		ArrayList<Cluster> oldClusterList      = null;
		double bound = -1;
		if (direction.equals(SAME)) {
			//if the prev structures are null, we must've started a new chromosome pair, so no need to 
			//check for overlaps
			if (sameClusterList == null) {
			       return;	
			}	
			bound = samePrevRightMostTrapEnd_;
			oldClusterList = sameClusterList;
		} else if (direction.equals(DIFFERENT)) {
			//if the prev structures are null, we must've started a new chromosome pair, so no need to 
			//check for overlaps
			if (diffClusterList == null) {
			       return;	
			}	
			bound = diffPrevRightMostTrapEnd_;
			oldClusterList = diffClusterList;
		} else {
			Out.print("ClusterESP.checkForOverlapsWithPrevClusters: Should never happen!!!!");
		}

		//use c_size rather than calling c.size() since will be adding to c within the loop
		int c_size = c.size();
		for(int k = 0; k< c_size; k++){
			Clone c1 = (Clone) c.get(k);
			if(c1.getType().equals(direction)) {

				//find the leftmost coordinate of current clone
				double c1LeftMostX = c1.getX();
				if (c1LeftMostX < 0) {
					c1LeftMostX = Math.abs(c1LeftMostX) - GASVMain.MAX_LMAX;
				}
				
				// only check those Clones where overlap is even possible
				if (c1LeftMostX <= bound) {
					//for any overlap, remove the associated cluster from the 
					//old data structure and add the cluster's Clones onto the end of c
					//Note that the Clones added to c won't be processed in this loop
					//since they'll be beyond c_size
					for (int i=0; i < oldClusterList.size(); ++i) {
						Cluster curCluster = oldClusterList.get(i);

						// only consider those clusters within overlap distance of c1
						if (curCluster != null 
								&& curCluster.getRightMostX() >= c1LeftMostX) {
							ArrayList<Clone> curClones = curCluster.getClones();
							for (int j=0; j<curClones.size(); ++j) {
								Clone oldClone = curClones.get(j);
								if (oldClone != null && GASVMain.overlap(c1, oldClone) > 0) {
									c.addAll(curClones);

									//can't just remove otherwise indexes get
									//screwed up, so delete by setting to null 
									oldClusterList.set(i, null);
									//break out of this inner for loop
									j = curClones.size();
								}
							}
						}
					}
				}
			}
		}
		if (c.size() - c_size > 100) {
			Out.print2("ClusterESP.checkForOverlapsWithPrevClusters() Added " + (c.size() - c_size) 
					+ " Break Regions from the previous window into the current window"); 
		}

	}

	//Find overlaps between CGH and ESP data, and fill out the appropriate (non-null) HashMap(s)
	private static void findCGHAndESPOverlaps(String direction, 
			HashMap<BreakRegion, ArrayList<Cluster>> cghxMap, 
			HashMap<BreakRegion, ArrayList<Cluster>> cghyMap,
			HashMap<BreakRegion, ArrayList<Cluster>> cghPairMap) throws IOException {

		ArrayList<Cluster> curClusterList = null;
		if (direction.equals(SAME)) {
			curClusterList = sameClusterList;
		} else if (direction.equals(DIFFERENT)) {
			curClusterList = diffClusterList;
		} else  {
			Out.print("ClusterESP.findCGHAndESPOverlaps(): Should never happen!!!!");
		}
		if (curClusterList == null) {
			return;
		}
		
		Set<BreakRegion> cghPairs = null;
		Set<BreakRegion> cghx = null;
		Set<BreakRegion> cghy = null;

		//if usePairedCGH mode, then cghxMap and cghyMap are both null, and only cghPairMap is valid 
		if (cghxMap == null) {
			cghPairs = cghPairMap.keySet();
		} else {
			//otherwise at least cghxMap is valid (cghyMap will be valid only for translocations)
			cghx = cghxMap.keySet();
		}

		//iterate through clusters list as well as CGH maps and record in hash map any intersections

		//For single CGH translocations, each CGH from cghx represents a vertical region and each 
		// CGH from cghy is a horizontal region.
		//For single non-translocations, each CGH from cghx represents both a vertical 
		// and a horizontal region
		//For paired CGH, translocations and non-translocations are both represented as PairedCGH objects
		// and are represented as finite rectangles, so don't need to handle translocations any differently.
		boolean isSingleTranslocation = false;
		if (cghyMap != null) {
			cghy = cghyMap.keySet();
			isSingleTranslocation = true;
		}

		for (int i=0; i<curClusterList.size(); ++i) {
			Cluster curCluster = curClusterList.get(i);
			if (curCluster == null) {
				continue;
			}
			//skip any clusters that don't have the min number of clones
			if (curCluster.getSize() < GASVMain.MIN_CLUSTER_SIZE) {
				continue;
			}
			if (cghPairMap != null) {
				Iterator<BreakRegion> piter = cghPairs.iterator();
				while (piter.hasNext()) {
					CGH curCGH = (CGH) piter.next();
					if (intersect(curCGH, curCluster)) {
						cghPairMap.get(curCGH).add(curCluster);
						Out.print2("ClusterESP.findCGHAndESPOverlaps() Found match, pairing " + curCGH.getName() + " to " + curCluster.clonesToStringNoSpaces());
					}
				}
			}
			if (cghxMap != null) {
				Iterator<BreakRegion> xiter = cghx.iterator();
				while (xiter.hasNext()) {
					CGH curCGH = (CGH) xiter.next();
					if (isSingleTranslocation) { 
						if (intersectX(curCGH, curCluster)) {
							cghxMap.get(curCGH).add(curCluster);
							Out.print2("ClusterESP.findCGHAndESPOverlaps() Found match, pairing " + curCGH.getName() + " to " + curCluster.clonesToStringNoSpaces());
						}
					} else {
						if (intersect(curCGH, curCluster)) {
							cghxMap.get(curCGH).add(curCluster);
							Out.print2("ClusterESP.findCGHAndESPOverlaps() Found match, pairing " + curCGH.getName() + " to " + curCluster.clonesToStringNoSpaces());
						}
					}
				}
			}
			if (cghyMap != null) {
				Iterator<BreakRegion> yiter = cghy.iterator();
				while (yiter.hasNext()) {
					CGH curCGH = (CGH) yiter.next();
					if (intersectY(curCGH, curCluster)) {
						cghyMap.get(curCGH).add(curCluster);
						Out.print2("ClusterESP.findCGHAndESPOverlaps() Found match, pairing " + curCGH.getName() + " to " + curCluster.clonesToStringNoSpaces());
					}
				}
			}
		}
		
	}

	private static void clearCGHClusterMap(
			HashMap<BreakRegion, ArrayList<Cluster>> map)
			throws IOException
	{
		Iterator<Map.Entry<BreakRegion, ArrayList<Cluster>>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<BreakRegion, ArrayList<Cluster>> entry = iter.next();
			entry.getValue().clear();
		}
	}

	private static void printCGHClusterEntries(
			HashMap<BreakRegion, ArrayList<Cluster>> map)
			throws IOException
	{
		//for (int k=0; k<maps.length;++k) { 
		//Set<Map.Entry< BreakRegion, ArrayList<Cluster> > > entries = maps[i].entrySet();
		//Iterator<Map.Entry<BreakRegion, ArrayList<Cluster>>> iter = entries.iterator();
		Iterator<Map.Entry<BreakRegion, ArrayList<Cluster>>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<BreakRegion, ArrayList<Cluster>> entry = iter.next();
			ArrayList<Cluster> curClusterList = entry.getValue();
			CGH cgh = (CGH) entry.getKey();

			//ignore entries where no overlap occurred
			if (curClusterList.isEmpty()) {
				continue;
			}
			//write out the CGH_ID
			clusterWriter_.write(cgh.getName() + "\t");
			//write out chromosome1
			clusterWriter_.write(cgh.getChr() + "\t");

			//write out chromosome2 (always the same as chr1 if in single CGH mode)
			if (cgh instanceof PairedCGH) {
				PairedCGH pcgh = (PairedCGH) cgh;
				clusterWriter_.write(pcgh.getChr2() + "\t");
			} else { 
				clusterWriter_.write(cgh.getChr() + "\t");
			}
			//write out number of associated clusters
			clusterWriter_.write(curClusterList.size() + "\t");


			for (int i=0; i<curClusterList.size(); ++i) {
				Cluster curCluster = curClusterList.get(i); 
				if (curCluster == null) {
					continue;
				}
				//start off cluster 
				clusterWriter_.write("(");
			
				//"#Cluster_ID:\tNum PES:\tLocalization:\tList of PES:\t ChrM:\tChrN:\tBoundary Points:\n";
				//Write out the cluster number and increment:
				clusterWriter_.write("c" + CLUSTER_NUMBER + Constants.SEP);
				CLUSTER_NUMBER++;

				//write num PES
				clusterWriter_.write(curCluster.getSize() + Constants.SEP);

				boolean isCliqueWithNonZeroArea = false; 
				if (curCluster.isClique() && curCluster.getPoly().getArea() > 0) {
					isCliqueWithNonZeroArea = true;
				}

				//write localization
				if (isCliqueWithNonZeroArea){
					clusterWriter_.write(df_.format(Math.sqrt(curCluster.getPoly().getArea())) + Constants.SEP);
				} else {
					clusterWriter_.write("-1" + Constants.SEP); 
				}
				//write out list of PES
				clusterWriter_.write(curCluster.clonesToStringNoSpaces() + Constants.SEP);
				//write out chromosomes
				clusterWriter_.write(curCluster.getChrX() + Constants.SEP + curCluster.getChrY());

				//write out boundary positions if it's a clique w/non zero intersection
				if (isCliqueWithNonZeroArea){
					clusterWriter_.write(Constants.SEP + printPolyNoSpaces(curCluster.getPoly()));
				} 
				//else {
				//	clusterWriter_.write("\n");
				//}

				if (i == (curClusterList.size()-1)) {
					clusterWriter_.write(")");
				} else {
					clusterWriter_.write("),");
				}
			}
			clusterWriter_.write("\n");
		}	
		//}
	}

	private static boolean intersect(CGH cgh, Cluster cluster) {
		boolean theyIntersect = false;
		if (cgh instanceof PairedCGH) {
			PairedCGH pcgh = (PairedCGH) cgh;
			PolyDefault cghPoly = new PolyDefault();
			cghPoly.add(pcgh.getCoord1(), pcgh.getCoord3());
			cghPoly.add(pcgh.getCoord2(), pcgh.getCoord3());
			cghPoly.add(pcgh.getCoord2(), pcgh.getCoord4());
			cghPoly.add(pcgh.getCoord1(), pcgh.getCoord4());

			if (cluster.isClique() && cluster.getPoly() != null) {
				PolyDefault res = (PolyDefault) cghPoly.intersection(cluster.getPoly());
				if (res.getArea() > 0) {
					return true;
				}
			} else {
				ArrayList<Clone> list = cluster.getClones();
				for (int i=0; i<list.size(); ++i) {
					PolyDefault clonePoly = (PolyDefault) list.get(i).getPoly();
					PolyDefault res = (PolyDefault) cghPoly.intersection(clonePoly);
					if (res.getArea() > 0) {
						return true;
					}
					/*if (cghPoly2 != null) {
					  res = (PolyDefault) cghPoly2.intersection(clonePoly);
					  if (res.getArea() > 0) {
					  return true;
					  }
					  }*/
				}
			}

		} else {
			double min = cgh.getX();
			double max = cgh.getY();
			if (cluster.getLeftMostX() < max && cluster.getRightMostX() > min) {
				theyIntersect = true;
			}
			if (cluster.getTopMostY() > min && cluster.getBottomMostY() < max) {
				theyIntersect = true;
			}
		}
		return theyIntersect;
	}

	private static boolean intersectX(CGH cgh, Cluster cluster) {
		// for now assume 2D paired CGH incompatible with intersections with translocation ESP's.
		//TODO: are all cgh instances of PairedCGH?????
		if (cgh instanceof PairedCGH) {
			Out.print("Error: intersectX should not be called for PairedCGH!");
			return false;
		}
		boolean theyIntersect = false;
		double min = cgh.getX();
		double max = cgh.getY();
		if (cluster.getLeftMostX() < max && cluster.getRightMostX() > min) {
			theyIntersect = true;
		}
		return theyIntersect;
	}

	private static boolean intersectY(CGH cgh, Cluster cluster) {
		// for now assume 2D paired CGH incompatible with intersections with translocation ESP's.
		if (cgh instanceof PairedCGH) {
			Out.print("Error: intersectY should not be called for PairedCGH!");
			return false;
		}
		boolean theyIntersect = false;
		double min = cgh.getX();
		double max = cgh.getY();
		if (cluster.getTopMostY() > min && cluster.getBottomMostY() < max) {
			theyIntersect = true;
		}
		return theyIntersect;
	}

	// save state of these parameters
	// LABEL, chrx, chry
	// clusters, cloneOverlaps_, cloneList_
	//private static void writeClusters(int LABEL, int chrx, int chry, String direction) throws IOException {
	private static void writeClusters(String direction) //, ArrayList<Cluster> curClusterList) 
			throws IOException {
		//OUTPUT THE DIFFERENT CLUSTERS
		ArrayList<Cluster> curClusterList = null;
		if (direction.equals(SAME)) {
			curClusterList = sameClusterList;
		} else if (direction.equals(DIFFERENT)) {
			curClusterList = diffClusterList;
		} else  {
			Out.print("ClusterESP.writeClusters(): Should never happen!!!!");
		}
		if (curClusterList == null) {
			return;
		}

		//OUTPUT THE DIFFERENT CLUSTERS
		for (int i=0; i<curClusterList.size(); ++i) {
			Cluster myCluster = curClusterList.get(i); 
			if (myCluster == null) {
				continue;
			}
			ArrayList<Cluster> clustersToWrite = new ArrayList<Cluster>();
			//if (myCluster.isClique() && myCluster.getPoly().getArea() > 0) {
			clustersToWrite.add(myCluster);
			//else if
			if ((GASVMain.USE_MAXIMAL || GASVMain.USE_ALL) && (!myCluster.isClique() || !(myCluster.getPoly().getArea() > 0))) {
				Out.print2("ClusterESP: c" + CLUSTER_NUMBER + " before findMaximalClusters() call, " 
						+ "myCluster size is " + myCluster.getSize()
						+ " and myCluster poly is " + myCluster.getPoly()); 
				myCluster.findMaximalClusters(clustersToWrite);	
				Out.print2("ClusterESP: after findMaximalClusters() call, " 
						+ "myCluster size is " + myCluster.getSize()
						+ " and myCluster poly is " + myCluster.getPoly()
						+ " with clustersToWrite size " + clustersToWrite.size());
			} 

			int subClusterNum = 0;
			for (int j=0; j<clustersToWrite.size(); ++j) {
				Cluster curCluster = clustersToWrite.get(j);
				if (curCluster.getSize() > 1000) {
					Out.print2("writeClusters() benchmarking for cluster of size " 
							+ curCluster.getSize()
							+ ": about to start writing, time: " + System.currentTimeMillis());
				}
				//skip any clusters that don't have the min number of clones
				if (curCluster.getSize() < GASVMain.MIN_CLUSTER_SIZE) {
					continue;
				}
				
				//Write out the cluster number and increment:
				String clusterName = null;
				if (j == 0) {
					//clusterWriter_.write("c" + CLUSTER_NUMBER + "\t");
					clusterName = "c" + CLUSTER_NUMBER;
				} else {
					//clusterWriter_.write("c" + CLUSTER_NUMBER 
					//		+ "." + subClusterNum + "\t");
					clusterName = "c" + CLUSTER_NUMBER + "." + subClusterNum;
					subClusterNum++;
				}
				writeSingleCluster(clusterName, curCluster, false, clusterWriter_);

			}
			CLUSTER_NUMBER++;
		}
	}

	private static void writeClustersAndSplitReads(String direction)
			throws IOException {

		ArrayList<Cluster> curClusterList = null;
		if (direction.equals(SAME)) {
			curClusterList = sameClusterList;
		} else if (direction.equals(DIFFERENT)) {
			curClusterList = diffClusterList;
		} else  {
			Out.print("ClusterESP.writeClustersAndSplitReads(): Should never happen!!!!");
		}
		if (curClusterList == null) {
			return;
		}

		//OUTPUT THE DIFFERENT CLUSTERS along with associated split reads
		for (int i=0; i<curClusterList.size(); ++i) {
			Cluster myCluster = curClusterList.get(i); 
			if (myCluster == null) {
				continue;
			}
			ArrayList<Cluster> clustersToWrite = new ArrayList<Cluster>();
			clustersToWrite.add(myCluster);

			if ((GASVMain.USE_MAXIMAL || GASVMain.USE_ALL) && (!myCluster.isClique() || !(myCluster.getPoly().getArea() > 0))) {
				myCluster.findMaximalClusters(clustersToWrite);	
			}

			int subClusterNum = 0;
			for (int j=0; j<clustersToWrite.size(); ++j) {
				Cluster curCluster = clustersToWrite.get(j);

				//skip any clusters that don't have the min number of clones
				if (curCluster.getSize() < GASVMain.MIN_CLUSTER_SIZE) {
					continue;
				}
				//Write out the cluster number and increment:
				String clusterName = null;
				if (j == 0) {
					//clusterWriter_.write("c" + CLUSTER_NUMBER + "\t");
					clusterName = "c" + CLUSTER_NUMBER;
				} else {
					//clusterWriter_.write("c" + CLUSTER_NUMBER 
					//		+ "." + subClusterNum + "\t");
					clusterName = "c" + CLUSTER_NUMBER + "." + subClusterNum;
					subClusterNum++;
				}
				//CLUSTER_NUMBER++;
				writeSingleCluster(clusterName, curCluster, true, clusterWriter_);
			}
			CLUSTER_NUMBER++;
		}
	}

	//creates PairedCGH objects for all possible pairings of the two lists of CGH data and inserts them 
	// as keys to the map
	private static void genPairsAndPutInMap(ArrayList<BreakRegion> list1, ArrayList<BreakRegion> list2,
			HashMap<BreakRegion, ArrayList<Cluster>> map) {
		for (int i=0; i<list1.size(); ++i) {
			for (int j=0; j<list2.size(); ++j) {
				CGH cghi = (CGH) list1.get(i);
				CGH cghj = (CGH) list2.get(j);
				PairedCGH cghPair = new PairedCGH(CGH_NUMBER, cghi, cghj);
				CGH_NUMBER++;
				map.put(cghPair, new ArrayList<Cluster>());
			}
		}

	}
	private static void convertArrayListToMap(ArrayList<BreakRegion> list, 
			HashMap<BreakRegion, ArrayList<Cluster>> map) {
		for (int i=0; i<list.size(); ++i) {
			map.put(list.get(i), new ArrayList<Cluster>());
		}
	}

	public static void clusterESPAndCGH(String espFilename, String cghFilename) 
			throws IOException {
		String fileNameMinusPath = cghFilename;
		int idxOfSlash = cghFilename.lastIndexOf("/");
		if (idxOfSlash != -1) {
			fileNameMinusPath = cghFilename.substring(idxOfSlash + 1);
		}

		boolean usePairedCGH = GASVMain.USE_PAIRED_CGH;
		//will need to write CGH and clusters to a temp file, sort the 
		//temp file and then finally write the sorted contents out to the final file
		String finalFileName = GASVMain.OUTPUT_DIR + fileNameMinusPath + ".cgh";
		File finalFile = new File(finalFileName);
		if (finalFile.exists()) {
			finalFile.delete();
		}
		BufferedWriter finalWriter = new BufferedWriter(new FileWriter(finalFile));

		String cluster = TMPFILENAME;

		File clusterFile = new File(cluster);	
		if (clusterFile.exists()) {
			clusterFile.delete();
		}
		clusterWriter_ = new BufferedWriter(new FileWriter(clusterFile));
		

		//if using paired, don't need to sort a temp file, so clusterWriter_ will do the actual final
		// writing so give it a header if necessary
		//if (GASVMain.USE_HEADER && usePairedCGH) {
			//clusterWriter_.write(cghHeaderLine);
		//}
		

		boolean useFast = GASVMain.USE_FAST;
		boolean useBatch = GASVMain.USE_BATCH;
		
		ReadInput readInput = new ReadInput(espFilename);
		ReadInput cghReadInput = new ReadInput(cghFilename);
		
		ArrayList<BreakRegion>[][] breakRegions = null; 
		int lmin = GASVMain.LMIN;
		int lmax = GASVMain.LMAX;

		boolean saveMemory = GASVMain.SAVE_MEMORY;
		boolean fileDone = false;

		//in fast mode, read PES from all chromosomes into memory at once
		if (useFast) {
			breakRegions = new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];
			if (useBatch) {
				readInput.readFiles(breakRegions);
			} else {
				readInput.readSingleFile(lmin, lmax, breakRegions);
			}
			fileDone = true;
		}

		//trading off memory for performance here.
		//keep all CGH data in memory for every chromosome rather than having to re-read data for a particular
		//chromosome whenever we encounter it
		/*ArrayList<BreakRegion> cghDataByChr[] = new ArrayList<BreakRegion>[GASVMain.NUM_CHROM];
		for(int i = 0; i<GASVMain.NUM_CHROM; i++){
			cghDataByChr[i] = null; 
		}
		*/
		

		//int k = 0;
		//int numClones = 0;
		
		//int CLUSTER_NUMBER = 1;

		for(int i = 0; i<GASVMain.NUM_CHROM; i++){
			chrx = i+1; 
			ArrayList<BreakRegion> cghx = new ArrayList<BreakRegion>();
			if (useBatch) {
				cghReadInput.readCGHFilesByChr(chrx, cghx); 
			} else {
				cghReadInput.readSingleCGHFileByChr(chrx, cghx); 
			}

			for(int j = i; j<GASVMain.NUM_CHROM; j++){
				boolean skipClustering = false;
				//ArrayList<BreakRegion> c = cs[i][j];
				//int chrx = i+1; int chry = j+1;
				chry = j+1;

				Out.print1("ClusterESP: processing chr " + chrx + ", chr" + chry);

				Out.print1("ClusterESP: considering " + cghx.size() + " cgh regions for chr " + chrx);

				// if in paired CGH mode, will only have a single cghPairMap keyed by PairedCGH data,
				// with either chrx crossed with x or x crossed with y (in the case of translocations).
				// if in single CGH mode, will have a single cghxMap for non-translocation ESPs, or
			        // both cghxMap and cghyMap for translocation ESP's.	
				HashMap<BreakRegion, ArrayList<Cluster>> cghPairMap = null; 
				HashMap<BreakRegion, ArrayList<Cluster>> cghxMap = null; 
				HashMap<BreakRegion, ArrayList<Cluster>> cghyMap = null;

				if (usePairedCGH) {
					if (cghx.size() == 0) {
						Out.print1("ClusterESP: no CGH data for chr" + chrx 
								+ " so no pairs can be generated. Will skip clustering.");
						skipClustering = true;
					}
					cghPairMap = new HashMap<BreakRegion, ArrayList<Cluster>>(
							Constants.EXPECTED_NUM_CGH_PER_CHR ); 
					//if same chr, only need to generate the paired data based on one chr's CGH data
					//if diffChr, will populate the map after reading in chry CGH data too 
					if (chrx == chry) {
						// generate all pairings of CGH data and insert each pair as a key
						// in a map (with the value to be all associated clusters)	
						genPairsAndPutInMap(cghx, cghx, cghPairMap);
					} 
				} else {
					//for non-paired, we know we have to at least make the chrxMap
					cghxMap = new HashMap<BreakRegion, ArrayList<Cluster>>(
							Constants.EXPECTED_NUM_CGH_PER_CHR ); 
					//convertArrayListToMap(cghDataByChr[i], cghxMap); 
					convertArrayListToMap(cghx, cghxMap); 
				}

				//for translocations will also have to read the CGH data for chry
				if (chrx != chry) {
					ArrayList<BreakRegion> cghy = new ArrayList<BreakRegion>();
					if (useBatch) {
						cghReadInput.readCGHFilesByChr(chry, cghy);
					} else {
						cghReadInput.readSingleCGHFileByChr(chry, cghy); 
					}
					Out.print1("ClusterESP: considering " + cghy.size() + " cgh regions for chr " + chry);
					// for paired data, generate the map based on both x and y 
					if (usePairedCGH) {
						if (cghy.size() == 0) {
							Out.print1("ClusterESP: no CGH data for chr" + chry 
									+ " so no pairs can be generated. "
									+ "Will skip clustering.");
							skipClustering = true;
						}
						genPairsAndPutInMap(cghx, cghy, cghPairMap);
					} else {
						//for non-paired data, make CGH y data into a separate map
						//if both cghx and cghy are empty, no need to do clustering
						if ((cghx.size() == 0) && (cghy.size() == 0)) {
							Out.print1("ClusterESP: no CGH data for chr" + chrx 
									+ " or for chr" + chry 
									+ " so will skip clustering.");
							skipClustering = true;
						}
						cghyMap = new HashMap<BreakRegion, 
							ArrayList<Cluster>>(Constants.EXPECTED_NUM_CGH_PER_CHR);
						convertArrayListToMap(cghy, cghyMap); 
					}
				} else if (!usePairedCGH) {
					//if in single CGH mode and it is NOT a translocation, can skip clustering
					//for chromsoome if no CGH data exists for this chromosome
					if (cghx.size() == 0) {
						Out.print1("ClusterESP: no CGH data for chr" + chrx 
								+ " so will skip clustering.");
						skipClustering = true;
					}
				}

				ArrayList<BreakRegion> c = null;
				do {
					// will be either fast mode or window (saveMemory mode)
					if (useFast) {
						c = breakRegions[i][j];
					} else {
						if (c == null) { 
							c = new ArrayList<BreakRegion>();
						}
						if (useBatch) {
							fileDone = readInput.readWindowFromFiles(chrx, 
									chry, c);
						} else {
							fileDone = readInput.readWindowFromSingleFile(chrx, 
									chry, lmin, lmax, c);
						}
					}

					//check each cluster in (i) to see if it is within Lmax of 
					//the right-most cluster in i-1.  If so, check for actual overlap 
					//with clusters in (i-1) and remove any (i-1) clusters that overlap it.  
					//At the end of iteration i, output any remaining clusters from (i-1).

					if(c!=null && !skipClustering){


						if (c.size() > 100) {
							Out.print2("ClusterESP: Loaded " + c.size() 
									+ " Break Regions for chr" 
									+ chrx + ", chr" + chry); 
						} 

						//first go in forward direction
						// pass false to indicate we don't want to find split reads
						clusterHelper(c, SAME, cghxMap, cghyMap, cghPairMap, false);

						//NOW GOING THROUGH THE REVERSE DIRECTION!
						clusterHelper(c, DIFFERENT, cghxMap, cghyMap, cghPairMap, false);

						c.clear();
					}//End c!= Null
					samePrevRightMostTrapEnd_ = sameRightMostTrapEnd_;
					diffPrevRightMostTrapEnd_ = diffRightMostTrapEnd_;
					sameRightMostTrapEnd_ = 0;
					diffRightMostTrapEnd_ = 0;

				} while (saveMemory && !fileDone 
						&& !readInput.getDiffChrPairReached() );

				if (skipClustering) {
					continue;
				}

				// Can now safely assume all clusters in the current window are valid,
				// so find overlaps between current window's ESP clusters and the CGH data.
				// (Usually there is a "delayed" save until the next window to
				// ensure that no clusters overlaps across the window boundary)

				// In the --fast case, only one giant window, so this final 
				// call will actually do all the work of finding CGH overlaps for ALL clusters.
				findCGHAndESPOverlaps(SAME, cghxMap, cghyMap, cghPairMap);
				findCGHAndESPOverlaps(DIFFERENT, cghxMap, cghyMap, cghPairMap);

				//DESIGN CHOICE: 
				// Could have printed everything out at every window to save memory
				// But for performance, ease of coding, and assuming cgh data is small, decided to 
				// print out at the end of every chromosome pairing instead.

				//do one last time (don't bother clearing the maps since they'll be garbage collected
				// anyway)
				if (usePairedCGH) {
					printCGHClusterEntries(cghPairMap);
				} else {
					printCGHClusterEntries(cghxMap);
					if (cghyMap != null) {
						printCGHClusterEntries(cghyMap);
					}
				}
				

				// if no more ESP's, stop looking
				if (fileDone) {
					i = GASVMain.NUM_CHROM;
					j = GASVMain.NUM_CHROM;
				}

				//reset prev window variables to initial values
				sameClusterList = null;
				diffClusterList = null;
				//}
			}//End j;
		}//End i;

		clusterWriter_.flush();
		clusterWriter_.close();
		//sort (and aggregate duplicates for) the file, only necessary in single CGH mode?
		// will also take care of deleting the temp file
		sortAndPrintFinalFile(finalWriter);

		if (!fileDone) {
			Out.print("Warning: Finished looping through all possible Chr combinations, but did not finish the file."
				       + " It's likely input files were not sorted correctly.");
		}
	}

	//takes the data in the Constants.GASV_TMP_NAME file and handles
	// processing/sorting it into a final file
	// removes the temp file as a final step.
	private static void sortAndPrintFinalFile(BufferedWriter finalWriter) throws IOException {

		if (GASVMain.USE_HEADER) {
			finalWriter.write(cghHeaderLine);
		}
		
		//open tmp file read-only
		RandomAccessFile f = new RandomAccessFile(TMPFILENAME, "r");

		//pos always represents the position at the start of the current line
		//long pos = 0;

		HashMap<String, Object> visitedCGH 
			= new HashMap<String, Object>(Constants.EXPECTED_NUM_CGH_PER_CHR);
		String cghName;
		String nextLine = f.readLine();
		while (nextLine!=null) {
			if (nextLine.startsWith("#")) {
				nextLine = f.readLine();
				continue;
			}
			
			String[] line = nextLine.split("\\s+");
			cghName = line[0];
			//if already visited this CGH data before, skip it
			if (visitedCGH.containsKey(cghName)) {
				//pos = f.getFilePointer();
				nextLine = f.readLine();
				continue;
			} else {
				visitedCGH.put(cghName, null);
			}
			String chr1 = line[1];
			String chr2 = line[2];
			String clusters = line[4];
			ArrayList<String> list = new ArrayList<String>();
			addClustersToList(clusters, list);

			//now save place so can seek() back to it after searching thru the rest
			//of the file for more cghName instances
			long returnPos = f.getFilePointer();
			nextLine = f.readLine();
			while (nextLine!=null) {
				String[] tmpline = nextLine.split("\\s+");
				if (tmpline[0].equals(cghName)) {
					addClustersToList(tmpline[4], list);
				}
				nextLine = f.readLine();
			}

			finalWriter.write(cghName + "\t" + chr1 + "\t" + chr2 + "\t" + list.size() + "\t");
			for (int i=0; i<list.size(); ++i) {
				finalWriter.write(list.get(i));
				if (i < (list.size()-1)) {
					finalWriter.write(",");
				}
			}
			finalWriter.write("\n");

			//return back to previous position
			f.seek(returnPos);

			nextLine = f.readLine();
		}

		f.close();
		Out.print1("sortAndPrintFinalFile() finishing up, deleting tmpfile: " + TMPFILENAME);

		//delete temp file
		File tmpFileToDelete = new File(TMPFILENAME);
		if (!tmpFileToDelete.delete()) {
			Out.print1("Unable to delete tmp file for GASV!");
		}

		finalWriter.flush();
		finalWriter.close();
	}


	//adds a string representing clusters to an ArrayList
	private static void addClustersToList(String str, ArrayList<String> list) {
		String[] parts = str.split("\\(");
		for (int i=0; i<parts.length; ++i) {
			int end = parts[i].indexOf(")");
			if (end != -1) {
				list.add("(" + parts[i].substring(0, end+1));
			}
		}
	}


	/**
	  * Find all clusters as usual. Then for each cluster, output any candidate split
	  * reads (i.e. those reads of the cluster that contain the entire max intersection 
	  * region
	  */
	public static void clusterESPAndFindSplitReads(String espFilename) 
			throws IOException {
		String fileNameMinusPath = espFilename;
		int idxOfSlash = espFilename.lastIndexOf("/");
		if (idxOfSlash != -1) {
			fileNameMinusPath = espFilename.substring(idxOfSlash + 1);
		}

		//will need to write CGH and clusters to a temp file, sort the 
		//temp file and then finally write the sorted contents out to the final file?
		//String finalFileName = GASVMain.OUTPUT_DIR + fileNameMinusPath + ".cgh";
		//File finalFile = new File(finalFileName);
		//BufferedWriter finalWriter = new BufferedWriter(new FileWriter(finalFile));

		String cluster = GASVMain.OUTPUT_DIR + fileNameMinusPath + ".clusters";

		File clusterFile = new File(cluster);	
		if (clusterFile.exists()) {
			clusterFile.delete();
		}
		clusterWriter_ = new BufferedWriter(new FileWriter(clusterFile));
		
		if (GASVMain.USE_HEADER) {
			String headerLine = ESP_HEAD_STD + "\tCandidate Split Reads:"; 
			if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.STANDARD) {
				headerLine = ESP_HEAD_STD + "\tCandidate Split Reads:"; 
			} else if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.READS) {
				headerLine = ESP_HEAD_READS + "\tCandidate Split Reads:"; 
			} else if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.REGIONS) {
				headerLine = ESP_HEAD_REGIONS + "\tCandidate Split Reads:"; 
			}
			headerLine += "\n";
			clusterWriter_.write(headerLine);
		}

		boolean useFast = GASVMain.USE_FAST;
		boolean useBatch = GASVMain.USE_BATCH;
		
		ReadInput readInput = new ReadInput(espFilename);
		
		ArrayList<BreakRegion>[][] breakRegions = null; 
		int lmin = GASVMain.LMIN;
		int lmax = GASVMain.LMAX;

		boolean saveMemory = GASVMain.SAVE_MEMORY;
		boolean fileDone = false;

		//in fast mode, read PES from all chromosomes into memory at once
		if (useFast) {
			breakRegions = new ArrayList[GASVMain.NUM_CHROM][GASVMain.NUM_CHROM];
			if (useBatch) {
				readInput.readFiles(breakRegions);
			} else {
				readInput.readSingleFile(lmin, lmax, breakRegions);
			}
			fileDone = true;
		}

		for(int i = 0; i<GASVMain.NUM_CHROM; i++){
			chrx = i+1; 

			for(int j = i; j<GASVMain.NUM_CHROM; j++){
				//ArrayList<BreakRegion> c = cs[i][j];
				//int chrx = i+1; int chry = j+1;
				chry = j+1;

				Out.print1("ClusterESP: processing chr " + chrx + ", chr" + chry);

				ArrayList<BreakRegion> c = null;
				do {
					// will be either fast mode or window (saveMemory mode)
					if (useFast) {
						c = breakRegions[i][j];
					} else {
						if (c == null) { 
							c = new ArrayList<BreakRegion>();
						}
						if (useBatch) {
							fileDone = readInput.readWindowFromFiles(chrx, 
									chry, c);
						} else {
							fileDone = readInput.readWindowFromSingleFile(chrx, 
									chry, lmin, lmax, c);
						}
					}

					//check each cluster in (i) to see if it is within Lmax of 
					//the right-most cluster in i-1.  If so, check for actual overlap 
					//with clusters in (i-1) and remove any (i-1) clusters that overlap it.  
					//At the end of iteration i, output any remaining clusters from (i-1).

					if(c!=null){
						if (!c.isEmpty()) {
							Out.print2("ClusterESP: Loaded " + c.size() 
									+ " Break Regions for chr" 
									+ chrx + ", chr" + chry); 
						} else {
							Out.print2("ClusterESP: Loaded 0" 
									+ " Break Regions for chr" 
									+ chrx + ", chr" + chry); 
						}

						//first go in forward direction. Pass true to indicate that
						// we are also computing candidate split reads
						clusterHelper(c, SAME, true);

						//NOW GOING THROUGH THE REVERSE DIRECTION!
						clusterHelper(c, DIFFERENT, true);

						c.clear();
					}//End c!= Null
					samePrevRightMostTrapEnd_ = sameRightMostTrapEnd_;
					diffPrevRightMostTrapEnd_ = diffRightMostTrapEnd_;
					sameRightMostTrapEnd_ = 0;
					diffRightMostTrapEnd_ = 0;

				} while (saveMemory && !fileDone 
						&& !readInput.getDiffChrPairReached() );

				// reached the end of the input or the end of a chromosome,
				// so can now safely output all clusters in the current window
				// (usually there is a "delayed" write until the next window to
				// ensure that no duplicates are found
				// In the --fast case, only one giant window, so this final 
				// write will actually do all the work of writing everying.
				writeClustersAndSplitReads(SAME);
				writeClustersAndSplitReads(DIFFERENT);

				// if no more ESP's, stop looking
				if (fileDone) {
					i = GASVMain.NUM_CHROM;
					j = GASVMain.NUM_CHROM;
					//printAllCGHClusterEntries(maps);
					//clusterWriter_.flush();
					//clusterWriter_.close();
					//return;
				}

				//reset prev window variables to initial values
				sameClusterList = null;
				diffClusterList = null;
				//}
			}//End j;
		}//End i;

		clusterWriter_.flush();
		clusterWriter_.close();
		//sort (and aggregate duplicates for) the file
		// will also take care of deleting the temp file
		//sortAndPrintFinalFile(finalWriter);

		if (!fileDone) {
			Out.print("Warning: Finished looping through all possible Chr combinations, but did not finish the file."
					  + " It's likely input files were not sorted correctly.");

		}
	}

	/**
	  * The --noclusters mode will use this method as well, so make it package private rather than strictly private.
	  */
	static void writeSingleCluster(String clusterName, Cluster curCluster, boolean isSplitReadOut, BufferedWriter writer) 
			throws java.io.IOException{
		ArrayList<String> splitReads = null;
		writer.write(clusterName + "\t");
		if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.REGIONS) {
			writer.write(curCluster.getSize() + "\t");

			boolean isCliqueWithNonZeroArea = false; 
			if (curCluster.isClique() && curCluster.getPoly().getArea() > 0) {
				isCliqueWithNonZeroArea = true;
				if (isSplitReadOut) {
					splitReads = curCluster.getSplitReads(); 
				}
				writer.write(df_.format(Math.sqrt(curCluster.getPoly().getArea())) + "\t");
			} else {
				writer.write("-1\t"); 
			}
			writer.write(printType(curCluster) + "\t");
			
			writer.write(curCluster.clonesToString() + "\t");
			if (curCluster.getSize() > 1000) {
				Out.print2("writeClusters() benchmarking for cluster of size " 
						+ curCluster.getSize()
						+ ": finished writing all clones in cluster, time: " + System.currentTimeMillis());
			}
			writer.write(curCluster.getChrX() + "\t" + curCluster.getChrY() + "\t");
			if (isCliqueWithNonZeroArea){
				writer.write(printPoly(curCluster.getPoly()));
			} else {
				writer.write(printInterval(curCluster));
			}

			if (splitReads == null || splitReads.isEmpty()) {
				writer.write("\n");
			} else {
				writer.write("\t");
				for (int k=0; k< splitReads.size(); ++k) {
					writer.write(splitReads.get(k));
					if (k == (splitReads.size()-1)) {
						writer.write("\n");
					} else {
						writer.write(", ");
					}
				}
			}
			if (curCluster.getSize() > 1000) {
				Out.print2("writeClusters() benchmarking for cluster of size " 
						+ curCluster.getSize()
						+ ": finished writing the interval and the cluster in general, time: " + System.currentTimeMillis());
			}
		} else {
			boolean isCliqueWithNonZeroArea = false; 
			if (curCluster.isClique() && curCluster.getPoly().getArea() > 0) {
				isCliqueWithNonZeroArea = true;
				if (isSplitReadOut) {
					splitReads = curCluster.getSplitReads(); 
				}
			}
			//writer.write(curCluster.getChrX() + "\t" + curCluster.getChrY() + "\t");
			if (isCliqueWithNonZeroArea) {
				writer.write(printIntervalMaxMin(curCluster, curCluster.getChrX(), curCluster.getChrY()) + "\t");
			} else {
				writer.write(printIntervalMaxMinNonCluster(curCluster, curCluster.getChrX(), curCluster.getChrY()) + "\t");
			}

			writer.write(curCluster.getSize() + "\t");

			if (isCliqueWithNonZeroArea) {
				writer.write(df_.format(Math.sqrt(curCluster.getPoly().getArea())) + "\t");
			} else {
				writer.write("-1\t"); 
			}
			writer.write(printType(curCluster));
			if (GASVMain.OUT_MODE == GASVMain.GASV_OUTPUT_MODE.READS) {
				writer.write("\t" + curCluster.clonesToStringNoSpaces());
			}

			if (splitReads == null || splitReads.isEmpty()) {
				writer.write("\n");
			} else {
				writer.write("\t");
				for (int k=0; k< splitReads.size(); ++k) {
					writer.write(splitReads.get(k));
					if (k == (splitReads.size()-1)) {
						writer.write("\n");
					} else {
						writer.write(", ");
					}
				}
			}
		}
	}
}

class CloneComparator implements Comparator{
	CloneComparator() {}
	public int compare(Object o1, Object o2) {
		Clone c1 = (Clone) o1;
		Clone c2 = (Clone) o2;
		if (c1.getBmin() < c2.getBmin()) {
			return -1;
		} else if (c1.getBmin() > c2.getBmin()) {
			return 1;
		} else {
			return 0;
		}

	}
}
