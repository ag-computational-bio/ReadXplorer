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

import java.util.*;
import java.awt.Polygon;
import java.util.*;
import gasv.geom.*;
import gasv.common.Out;
import gasv.common.Constants;

public class Cluster {

	// note that clones previously just held clone names - this is what was used in ClusterAlgorithm.java
	private ArrayList<Clone> clones_;
	private PolyDefault intersect_;
	private boolean mixed_;
	private boolean isClique_ = false;
	private int chrx_ = -1;
	private int chry_ = -1;
	private double rightMostX_ = -1;
	private double leftMostX_ = Double.MAX_VALUE;
	private double topMostY_ = -1;
	private double bottomMostY_ = Double.MAX_VALUE;
	
	public Cluster(int chrx, int chry) {
		clones_ = new ArrayList<Clone>();
		//intersect = inter;
		intersect_ = null;
		mixed_ = false;
		isClique_ = false;
		chrx_ = chrx;
		chry_ = chry;
	}
	public PolyDefault getPoly(){
		return intersect_;
	}
	public ArrayList<Clone> getClones(){
		return clones_;
	}
	public int getSize(){
		return clones_.size();
	}
	public void setMixed(){
		mixed_ = true;
	}
	public boolean isMixed(){
		return mixed_;
	}
	public double getRightMostX() {
		return rightMostX_;
	}
	public double getLeftMostX() {
		return leftMostX_;
	}
	public double getTopMostY() {
		return topMostY_;
	}
	public double getBottomMostY() {
		return bottomMostY_;
	}
	public boolean partOf(Cluster c){
		boolean toReturn = false;
		ArrayList clonesOfC = c.getClones();
		for(int i = 0 ; i < clones_.size(); i++){
			for(int j = 0; j < clonesOfC.size(); j++){
				if(clones_.get(i).equals(clonesOfC.get(j)))
					toReturn = true;
			}
		}
		return toReturn;
	}
	public int getChrX() {
		return chrx_;
	}
	public int getChrY() {
		return chry_;
	}
	public String toString(){
		
		//return clones + " " + intersect.print();
		/*Iterator iter = clones.iterator();
		String toReturn = "{" + iter.next();
		while(iter.hasNext()){
			toReturn = toReturn + ", " + iter.next();
		}
		return toReturn + "}\t" + intersect.print();*/
		return clones_.toString();
	}

	/**
	  * Updates the boundary limits (leftMostX, rightMostX, topY, bottomY) for the Cluster
	  * based on this clone which is to be added.
	  */
	private void updateBoundaries(Clone clone) {
		//update rightMostX_ and leftMostX_ if necessary
		double rightx, leftx, topy, bottomy;
		double x = clone.getX();
		double y = clone.getY();
		double lmax = clone.getLmax();
		Poly p = clone.getPoly();
		int numPts = p.getNumPoints();
		for (int i=0; i<numPts; ++i) {
			if (p.getX(i) > rightMostX_) {
				rightMostX_ = p.getX(i);
			}
			if (p.getX(i) < leftMostX_) {
				leftMostX_ = p.getX(i);
			}
			if (p.getY(i) > topMostY_) {
				topMostY_ = p.getY(i);
			}
			if (p.getY(i) < bottomMostY_) {
				bottomMostY_ = p.getY(i);
			}
		}
	}

	public void addClone(Clone clone) {
		
		this.updateBoundaries(clone);
		
		clones_.add(clone);
	}

	public String clonesToString() {
		String toReturn = "";
		int size = clones_.size();
		//if more than MAX_CLIQUE_SIZE 
		if (size > GASVMain.MAX_CLIQUE_SIZE) {
			Out.print2("clonesToString(): only returning MAX_CLIQUE_SIZE clones in string.");
			size = GASVMain.MAX_CLIQUE_SIZE;
		}
		for (int i=0; i<size; ++i) {
			toReturn += clones_.get(i).getName();
			if (i < (size-1)) {
				toReturn = toReturn + ", ";
			}	
		}
		return toReturn;
	}
	
	public String clonesToStringNoSpaces() {
		String toReturn = "";
		int size = clones_.size();
		//if more than MAX_CLIQUE_SIZE 
		if (size > GASVMain.MAX_CLIQUE_SIZE) {
			Out.print2("clonesToString(): only returning MAX_CLIQUE_SIZE clones in string.");
			size = GASVMain.MAX_CLIQUE_SIZE;
		}
		for (int i=0; i<size; ++i) {
			toReturn += clones_.get(i).getName();
			if (i < (size-1)) {
				toReturn = toReturn + ",";
			}	
		}
		return toReturn;
	}

	public boolean isClique() {
		return isClique_;
	}

	public void setIsCliqueAndMakePoly(boolean isClique) {
		isClique_ = isClique;

		
		// only need to worry about the Poly functionality if it's a clique
		// update the intersection PolyDefault
		if (isClique_) {
			for (int i=0; i< clones_.size(); ++i) {
				PolyDefault newPoly = (PolyDefault) clones_.get(i).getPoly();
				//PolyDefault newPoly = (PolyDefault) clone.getPoly();
				if (intersect_ == null) {
					intersect_ = newPoly;
				} else {
					PolyDefault res = (PolyDefault) intersect_.intersection(newPoly);
					intersect_ = res;
				}
			}
		}
	}
	public void setIsCliqueAndSetPoly(boolean isClique, PolyDefault intersect) {
		isClique_ = isClique;
		intersect_ = intersect;
	}

	public ArrayList<String> getSplitReads() {
		//if no common intersection area (not a clique), won't be any split reads!
		if (intersect_ == null) {
			return null;
		}

		//iterate through intersection's boundary points and find extreme x and y values
		double maxX = -1;
		double minX = Double.MAX_VALUE;
		double maxY = -1;
		double minY = Double.MAX_VALUE;
		for(int i = 0; i < intersect_.getNumPoints(); i++){
			double x = intersect_.getX(i);
			double y = intersect_.getY(i);
			if (x > maxX) {
				maxX = x;
			}
			if (x < minX) {
				minX = x;
			}
			if (y > maxY) {
				maxY = y;
			}
			if (y < minY) {
				minY = y;
			}
		}

		ArrayList<String> splitReads = new ArrayList<String>();
		for (int i=0; i<clones_.size(); ++i) {
			Clone curClone = clones_.get(i);
			//two options: 
			// (1) Compare and find if read has _any_ overlap with intersect_ area
			// (2) Compare and find if read completely contains the intersect_ area 

			double cloneX = curClone.getX();
			double cloneY = curClone.getY();
			//int xLen = curClone.getXLen();
			//int yLen = curClone.getYLen();
			int xLen = GASVMain.READ_LENGTH;
			int yLen = GASVMain.READ_LENGTH;
			double xStart, yStart, xEnd, yEnd;
			if (cloneX < 0) {
				xEnd = Math.abs(cloneX);
				xStart = Math.abs(cloneX - xLen);	
			} else {
				xStart = cloneX;
				xEnd = cloneX + xLen;
			}	
			if (cloneY < 0) {
				yEnd = Math.abs(cloneY);
				yStart = Math.abs(cloneY - yLen);	
			} else {
				yStart = cloneY;
				yEnd = cloneY + yLen;
			}	
			boolean overlapX = false;
			boolean overlapY = false;

			String xYOrBoth = null;
			//Option 1:
			if ((xStart < maxX && xEnd > minX) || (minX < xEnd && maxX > xStart)) {
				overlapX = true;
			}	
			if ((yStart < maxY && yEnd > minY) || (minY < yEnd && maxY > yStart)) {
				overlapY = true;
			}
			//Option 2:
			/*if (xStart < minX && xEnd > maxX) { 
				overlapX = true;
			}	
			if (yStart < minY && yEnd > maxY) { 
				overlapY = true;
			}*/	
			if (overlapX && overlapY) {
				xYOrBoth = "B";
			} else if (overlapX) {
				xYOrBoth = "X";
			} else if (overlapY) {
				xYOrBoth = "Y";
			}

			if (xYOrBoth != null) {
				splitReads.add(xYOrBoth + Constants.SEP + curClone.getName()); 
			}
			//for reads that don't intersect, the intersection region, don't add to the array 
		}
		return splitReads;
	}

	public void findMaximalClusters(ArrayList<Cluster> maximalList) {

		if (clones_.size() > GASVMain.MAX_CLIQUE_SIZE) {
			Out.print2("Cluster of size " + clones_.size() + " exceeds --maxCliqueSize " 
					+ GASVMain.MAX_CLIQUE_SIZE + " so skip finding maximal clusters in it");
			return;

		}
		if (isClique_ && intersect_.getArea() > 0) {
			Out.print("Cluster.findMaximalClusters() ERROR this should never happen!!");
			maximalList.add(this);
		} else {
			//int[] maxClusterSize = new int[1];
			//maxClusterSize[0] = 0;
			// keep track of clusters so don't repeat work (i.e. if two clusters contain the same
			// exact clones, only have to process one of them)
			HashMap<String, Object> visitedMaximalClusters = new HashMap<String, Object>();
			
			Out.print2("findMaximalClusters() called, for cluster containing " + clones_.get(0).getName());

			//pass true to indicate this is the first top level call, and can split into rectangles 
			//immediately without checking the termination condition
			//try { 
			ArrayList<Cluster> tmpMaximalList = new ArrayList<Cluster>();
			LinkedList<ClusterNode> bfsQueue = new LinkedList<ClusterNode>();
			ClusterNode firstNode = new ClusterNode(this, true, //maxClusterSize, 
					tmpMaximalList,
					leftMostX_, rightMostX_, topMostY_, bottomMostY_, visitedMaximalClusters, 0);
			bfsQueue.addLast(firstNode);
			try {
				while (bfsQueue.size() > 0) {
					findMaximalClustersRecursive(bfsQueue.removeFirst(), bfsQueue);
				}
			} catch (java.io.IOException ex) {
				Out.print("WARNING: \"TIMED OUT\" while searching cluster "
						+ "(containing read " + clones_.get(0).getName() + ") for maximal clusters! "
						+"Probably due to too many rectangles to be searched.  Caught "
						+ "exception " + ex.getMessage());
			}
			maximalList.addAll(tmpMaximalList);
			/*} catch (Exception ex) {
				Out.print("Exception: " + ex + " in findMaximalClustersRecursive()"); 
				Out.print("...for connected component of size: " + clones_.size() + " with reads: ");
				String msg = "";
				for (int i=0; i<clones_.size(); i++) {
					msg += clones_.get(i) + ", ";
				}
				Out.print(msg);
			}*/
		}

	}

	
	// maxClusterSize is a 1 membered array indicating the current largest maximal cluster size encountered.
	// only clusters that meet or exceed this size should be added to the maximal cluster list
	// MODEL THIS AS A BFS RATHER THAN A DFS???
	static void findMaximalClustersRecursive(ClusterNode node, LinkedList<ClusterNode> bfsQueue) throws java.io.IOException{
	//void findMaximalClustersRecursive(boolean isTopLevel, int[] maxClusterSize, ArrayList<Cluster> maximalList,
			//double leftMostX, double rightMostX, double topMostY, double bottomMostY,
			//HashMap<String, Object> visitedMaximalClusters, int recursionLvl) {
		
		//don't need to check termination condition if this is a "top-level" Cluster that has already been
		// checked by ClusterESP.java for whether it's a clique.
		// ClusterESP.java will only call findMaximalClusters() on top-level Clusters that are NOT cliques
		//TODO could fix the efficiency of this (avoid GPCJ intersection() calls) eventually
		//TODO TO optimize performance, can keep track of known clones and make sure we dont' duplicate effort
		// .......but how to do this??  Can depend on fact that clones are added to the 4 quadrant clusters in 
	        //  				the same order. Don't depend on clones having unique names (may be named
		//				same if ambiguous reads), but "name + x + y" string should be unique,
		//				and just concatenate all clones together to get unique id of cluster.	
		//				This will allow us to skip the step of evaluating whether a clique is 
		//				large enough and avoid adding duplicate maximal clusters.
		ArrayList<Clone> clones = node.cluster.getClones();
		boolean isTopLevel = node.isTopLevel;
		//int[] maxClusterSize = node.maxClusterSize;
		ArrayList<Cluster> maximalList = node.maximalList;
		double leftMostX = node.leftMostX;
		double rightMostX = node.rightMostX;
		double topMostY = node.topMostY;
		double bottomMostY = node.bottomMostY;
		HashMap<String, Object> visitedMaximalClusters = node.visitedMaximalClusters;
		int recursionLvl = node.recursionLvl;

		if (!isTopLevel) {
			String clusterID = "";
			//if fewer clones in this cluster than the current maximal cluster size, then there's no way we can 
			//discover any maximal clusters here.  Just return
			// BUT, realized that I was misinterpreting maximal mode, so we need to return ALL non-dominated subclusters,
			// not just the ones of largest size
			//if (clones.size() < maxClusterSize[0]) {
				//Out.print2("Reached small cluster of size " + clones.size() 
						//+ ", ignoring it since max size is " + maxClusterSize[0]);
				//return;
			//}

			for (int i=0; i< clones.size(); ++i) {
				Clone curClone = clones.get(i);
				clusterID += curClone.getName(); 
				clusterID += curClone.getX(); 
				clusterID += curClone.getY();
				//lengths shouldn't add to uniqueness since almost always the same length
				//clusterID += curClone.getXLen(); 
				//clusterID += curClone.getYLen();
				clusterID += " ";
			}
			Out.print2("Recursion level: " + recursionLvl + "... Considering cluster: " + clusterID 
					+ " with BFS QUEUE size " + bfsQueue.size());
			if (recursionLvl > 1000) {
				Out.print("WARNING, max recursion level reached! unable to finish finding maximal clusters!!");
				//throw new Exception("recursion level reached");
				return;
			}
			if (bfsQueue.size() > Constants.MAX_RECTANGLES_TO_SEARCH) {
				throw new java.io.IOException("TIMED OUT - too many rectangles to search: " 
						+ Constants.MAX_RECTANGLES_TO_SEARCH);
			}
			//if it has been visited, we already know it's a clique, and it has already either been
			// ignored or added to the list of clusters
			if (visitedMaximalClusters.containsKey(clusterID)) {
				Out.print2("already visited this cluster: " + clusterID);
				return;
			}
			
			
			PolyDefault intersect = null;
			for (int i=0; i< clones.size(); ++i) {
				PolyDefault newPoly = (PolyDefault) clones.get(i).getPoly();
				//PolyDefault newPoly = (PolyDefault) clone.getPoly();
				if (intersect == null) {
					intersect = newPoly;
				} else {
					PolyDefault res = (PolyDefault) intersect.intersection(newPoly);
					intersect = res;
				}
			}
			//RECURSION termination CONDITION!
			if (intersect.getArea() > 0) {
				node.cluster.setIsCliqueAndSetPoly(true, intersect);
				Out.print2("Reached termination condition for clique: " + clusterID + " with size " + clones.size());
						//+ " and current maxClusterSize " + maxClusterSize[0]);
				//if (clones.size() > maxClusterSize[0]) {
					//maxClusterSize[0] = clones.size();
					//maximalList.clear();	
					//maximalList.add(node.cluster);
				//} else if (clones.size() == maxClusterSize[0]) {
					//maximalList.add(node.cluster);
				//}

				// Have to prune away from the final list any subclusters strictly dominated by other subclusters!
				// Can do this by - each time we add a cluster to the list, loop through and remove any
				// which it dominates! Also, if it is dominated by any existing cluster, don't add it!
				boolean addToList = true;
				for (int i=0; i<maximalList.size(); ++i) {
					Cluster listCluster = maximalList.get(i);
					if (listCluster.getSize() >= node.cluster.getSize()) {
						//check whether the list's cluster dominates this cluster
						if (dominates(listCluster, node.cluster)) {
							addToList = false;
							break;
						}
					} else {
						//check whether this cluster dominates the list's cluster 
						if (dominates(node.cluster, listCluster)) {
							maximalList.remove(i);
							//decrement so that we don't skip the next cluster in list
							--i;
						}
					}
				}
				if (addToList) {
					maximalList.add(node.cluster);
				}

				//TODO as a later optimization, might want to add all subclusters to the visited list as well 
				//addAllSubclusters(visitedMaximalClusters, clusterID)
				visitedMaximalClusters.put(clusterID, null);
				
				return;
			} else {
				node.cluster.setIsCliqueAndSetPoly(false, null);
				Out.print2("Cluster " + clusterID + " is not a clique, so divide into 4 quadrants and recurse");
			}

		}
		//if didn't terminate, need to divide into 4 and recurse
		//if we use the _cluster_'s own boundaries, as below, then should just remove these parameters
		//from method signature as unnecessary
		/*leftMostX = leftMostX_;
		rightMostX = rightMostX_;
		topMostY = topMostY_;
		bottomMostY = bottomMostY_;
		*/
		Out.print2("Divide this rect into 4: "
				+ " leftX: " + leftMostX
				+ " rightX: " + rightMostX
				+ " topY: " + topMostY
				+ " bottomY: " + bottomMostY);
		//bounding rectangle:
		double midX = (rightMostX + leftMostX) / 2; 
		double midY = (topMostY + bottomMostY) / 2;
		if (((midX - leftMostX) < Constants.MIN_BOUND_RECT_SIDE_LEN) 
			&& ((midY - bottomMostY) < Constants.MIN_BOUND_RECT_SIDE_LEN)) {

			/**
			 *  Handle the case of "infinite recursion" - happens when the edges of two polygons overlap
			 *  exactly, ensuring that, as we zoom in on this shared edge, that multiple polygons are always
			 *  in the cluster and that they never form a clique.
			 * 
			 *  Suzanne's suggestions for dealing with this:
			 *  (1) For now, just ignore this type of event. Have a maximum recursion. 
			 *  It seems that we would still  be guaranteed to get any maximal cluster with size > LxL.
			 *
			 *  (2) When we see which trapezoids overlap any current region check if there are 
			 *  any that share edges. If there are then perhaps we can split differently somehow?
			 *
			 *  For simplicity, choosing approach (1) for now.
			 *
			 */
			Out.print2("Reached a termination condition at recursionLvl " + recursionLvl 
					+ " where the bounding rectangle was too small"
					+ ".  I.e. both of its side lengths were < " 
					+ Constants.MIN_BOUND_RECT_SIDE_LEN); 
			return;
		}

		//if the new rectangles will be too small, just return (probably
		Cluster upperLeft = null;
		Cluster upperRight = null;
		Cluster lowerLeft = null;
		Cluster lowerRight = null;
		Poly upperLeftRect = makeRect(leftMostX, midX, topMostY, midY);
		Poly upperRightRect = makeRect(midX, rightMostX, topMostY, midY);
		Poly lowerLeftRect = makeRect(leftMostX, midX, midY, bottomMostY);
		Poly lowerRightRect = makeRect(midX, rightMostX, midY, bottomMostY);

		int chrx = node.cluster.getChrX();
		int chry = node.cluster.getChrY();
		for (int i=0; i<clones.size(); ++i) {
			Clone curClone = clones.get(i);
			if (cloneIsInRect(curClone, upperLeftRect)) {
				if (upperLeft == null) {
					upperLeft = new Cluster(chrx, chry);
				}
				upperLeft.addClone(curClone);
			} 
			if (cloneIsInRect(curClone, upperRightRect)) {
				if (upperRight == null) {
					upperRight = new Cluster(chrx, chry);
				}
				upperRight.addClone(curClone);
			} 
			if (cloneIsInRect(curClone, lowerLeftRect)) {
				if (lowerLeft == null) {
					lowerLeft = new Cluster(chrx, chry);
				}
				lowerLeft.addClone(curClone);
			} 
			if (cloneIsInRect(curClone, lowerRightRect)) {
				if (lowerRight == null) {
					lowerRight = new Cluster(chrx, chry);
				}
				lowerRight.addClone(curClone);
			}
		}
		++recursionLvl;
		if (upperLeft != null) {
			//upperLeft.findMaximalClustersRecursive(
			bfsQueue.addLast(new ClusterNode(upperLeft, false, maximalList,
					leftMostX, midX, topMostY, midY, visitedMaximalClusters,
					recursionLvl));
		}
		if (upperRight != null) {
			//upperRight.findMaximalClustersRecursive(
			bfsQueue.addLast(new ClusterNode(upperRight, false, maximalList,
					midX, rightMostX, topMostY, midY, visitedMaximalClusters,
					recursionLvl));
		}
		if (lowerLeft != null) {
			//lowerLeft.findMaximalClustersRecursive(
			bfsQueue.addLast(new ClusterNode(lowerLeft, false, maximalList,
					leftMostX, midX, midY, bottomMostY, visitedMaximalClusters,
					recursionLvl));
		}
		if (lowerRight != null) {
			//lowerRight.findMaximalClustersRecursive(
			
			bfsQueue.addLast(new ClusterNode(lowerRight, false, maximalList,
					midX, rightMostX, midY, bottomMostY, visitedMaximalClusters,
					recursionLvl));
		}

			//before we only added cliques (termination condition) to this visited list, but now that
			//the bounding rectangle is re-framed each time around the cluster, we can safely only 
			//visit each cluster exactly once (after all the maximal clusters it contains have been found)
			//visitedMaximalClusters.put(clusterID, null);
	}
	
	/**
	  * This adds all contained subclusters to the visited list.  Never finished writing this method as it's
	  * an optimization and isn't required for full functionality.
	  */
	//private static void addAllSubclusters(HashTable<String, Object> visitedMaximalClusters, String clusterID) {
		//String[] ids = clusterID.split("\\s+");
		//for (int i=0; i<ids.length; ++i) {
			////add the singles
			//visitedMaximalClusters.put(ids[i]+" ", null);
			//for (int j=i+1; j<ids.length; ++j) {
				////add the pairs
				//visitedMaximalClusters.put(ids[i]+" "+ids[j], null);
			//}
		//}
		////as final step add in the original full cluster
		////visitedMaximalClusters.put(clusterID, null);
	//}
	/**
	  * Returns true if cluster1 dominates cluster2.
	  * Otherwise, false.
	  * NOTE: this comparison is by name only. If doing ambiguous mapping, the names might be the same for 
	  * two different mappings of same read, and so would have to modify this code to take x, y into account as well...
	  */
	private static boolean dominates(Cluster cluster1, Cluster cluster2) {
		boolean c1Dominates = true;
		ArrayList<Clone> c1 = cluster1.getClones();
		ArrayList<Clone> c2 = cluster2.getClones();
		for (int i=0; i<c2.size(); ++i) {
			if (!c1.contains(c2.get(i))) {
				c1Dominates = false;
				break;
			}
		}
		return c1Dominates;
	}

	private static boolean cloneIsInRect(Clone clone, Poly rect) {
		//TODO make this more efficient - can just check bounds rather than use the GPCJ intersect call
		boolean isInRect = false;
		PolyDefault res = (PolyDefault) clone.getPoly().intersection(rect);
		if (res.getArea() > 0) {
			isInRect = true;
		}
		return isInRect;
	}
	private static Poly makeRect(double leftX, double rightX, double topY, double bottomY) {
		Out.print2("Making a rectangle with boundaries: "
				+ " leftX: " + leftX
				+ " rightX: " + rightX
				+ " topY: " + topY
				+ " bottomY: " + bottomY);
		Poly rect = new PolyDefault();
		rect.add(leftX, topY);
		rect.add(rightX, topY);
		rect.add(rightX, bottomY);
		rect.add(leftX, bottomY);
		return rect;
	}
}
class ClusterNode {
	Cluster cluster;
	boolean isTopLevel;
	ArrayList<Cluster> maximalList;
	double leftMostX;
	double rightMostX;
	double topMostY; 
	double bottomMostY;
	HashMap<String, Object> visitedMaximalClusters;
	int recursionLvl;
	public ClusterNode(Cluster clusterP, boolean isTopLevelP, ArrayList<Cluster> maximalListP,
			double leftMostXP, double rightMostXP, double topMostYP, double bottomMostYP,
			HashMap<String, Object> visitedMaximalClustersP, int recursionLvlP){

		cluster = clusterP;
		isTopLevel = isTopLevelP;
		maximalList = maximalListP;
		leftMostX = leftMostXP;
		rightMostX = rightMostXP;
		topMostY = topMostYP; 
		bottomMostY = bottomMostYP;
		visitedMaximalClusters = visitedMaximalClustersP;
		recursionLvl = recursionLvlP;
	}
}
