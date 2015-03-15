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
package gasv.common;
/**
 * Constants.java is just an interface with a list of constants used by GASV
 *
 */

public interface Constants {

	/**
	 * The Version number of GASV.
	 */
	public static final String GASV_VERSION = "2.0";
	
	/**
	 * Any difference between two numbers less than this threshold shall be 
	 * deemed insignificant and the two numbers should be regarded as being equal
	 */
	public static final double ZERO_THRESHOLD = 0.1;

	/**
	 * Max amount by which to shift a clone's coordinates when encountering a 
	 * degenerate case.  Shift amount will be a random value between 
	 * ZERO_THRESHOLD and MAX_SHIFT
	 */
	public static final double MAX_SHIFT = 1;

	/**
	 * Number of items (columns) expected in each row of an ESP file.
	 * Currently, this is
	 * clone_name chr1 x1 y1 orient1 chr2 x2 y2 orient2
	 */
	public static final int NUM_COLS_IN_ESP_FILE = 9;

	public static final int NUM_COLS_IN_SING_CGH_FILE = 5;

	public static final int NUM_COLS_IN_PAIR_CGH_FILE = 10;
	/**
	 * Default value to use for LMin if user doesn't specify and it's not 
	 * specified in a file.
	 */
	public static final int DEFAULT_LMIN = 100;

	/**
	 * Default value to use for LMax if user doesn't specify and it's not 
	 * specified in a file.
	 */
	public static final int DEFAULT_LMAX = 400;

	/**
	 * Default value to use for MinClusterSize if the user doesn't specify  
	 */
	public static final int DEFAULT_MIN_CLUSTER_SIZE = 4;
	
	/**
	 * Lmax is multiplied by this factor to come up with the window size to 
	 * use in the "saveMemory" mode of operation
	 */
	public static final float WIN_SIZE_FACTOR = 1.5f;

	/**
	  * The expected number of CGH datapoints for any one chromosome.
	  * This is used for deterimining the initial capacity of HashMap's.
	  */
	public static final int EXPECTED_NUM_CGH_PER_CHR = 100;

	/**
	  * A filename to use for GASV temp file
	  */
	public static final String GASV_TMP_NAME = ".gasv.tmp";

	/**
	  * A string value used for separating fields describing a cluster
	  */
	public static final String SEP = "**";

	/**
	 * Minimum side length of a bounding rectangle.  
	 * Recursion in Cluster.findMaximalClustersRecursive() will terminate if
	 * we try to create a rectangle with sides less than this length.
	 */
	public static final double MIN_BOUND_RECT_SIDE_LEN = 1;

	/**
	 * Maximum number of rectangles to search for any one connected component.
	 * Theoretically there should not be a limit, but in practice searching thousands of
	 * rectangles for each connected component takes quite a bit of time.
	 * So set a limit for the interests of time. Or replace the number with the Integer.MAX_VALUE
	 * to remove the limit (well still have a limit but only limite by the number of integers a
	 * Java int can hold).
	 *
	 * Generally, on each iteration, 4 sub-rectangles are formed, and the length of each rectangle is cut
	 * in half.  So for n iterations, 1 + 4 + 4^2 + 4^3 + ... + 4^n rectangles  formed
	 * And at the n-th iteration, each smallest rectangle has dimensions W(1/2)^n  by L(1/2)^n 
	 * 	where W = orig rect's width, L = orig rect's length
	 * So if we assume original connected component has a bounding rectangle of 100,000 by 100,000 bp,
	 *	then will need n = 17 to resolve down to smallest rectangles with side lengths of 1 bp.
	 *      2^17 = 131072
	 *      And the number of rectangles searched (at most) will be: 
	 */
	public static final int MAX_RECTANGLES_TO_SEARCH = 1000; 

}
