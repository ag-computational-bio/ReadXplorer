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
import java.awt.*;
import java.util.*;
import java.io.*;
import java.text.*;
import gasv.geom.*;
public class GASVMain{

	public static boolean USE_HEADER = true;
	public static boolean USE_PAIRED_CGH = false;
	public static String OUTPUT_DIR = "";
	public static int LMIN = -1;
	public static int LMAX = -1;
	public static int LMIN2 = -1;
	public static int LMAX2 = -1;

	// If running in "window" mode, and filtering...
	// Since there are two LMAX values, one for cancer and one for normal,
	// 	need to pick one (the bigger one) on which to base WIN_SIZE
	//	since filtering needs to move in lock-step with the same windows
	public static int MAX_LMAX = -1;

	public static boolean USE_BATCH = false;
	public static boolean USE_FAST = false;
	public static boolean SAVE_MEMORY = true;
	public static boolean FIND_SPLIT_READS = false;
	public static int MIN_CLUSTER_SIZE = Constants.DEFAULT_MIN_CLUSTER_SIZE; //-1;
	public static int MAX_CLUSTER_SIZE = Integer.MAX_VALUE;
	public static int MAX_CLIQUE_SIZE = Integer.MAX_VALUE;
	public static int MAX_READS_PER_WIN = Integer.MAX_VALUE;
	public static boolean USE_MAXIMAL = false;
	public static int READ_LENGTH = -1;
	public static boolean USE_ALL = false;
	public static boolean NORECIPROCAL_MODE = false;

	public static int NUM_CHROM = 24;
	public enum GASV_OUTPUT_MODE {
		STANDARD, READS, REGIONS
	}
	public static GASV_OUTPUT_MODE OUT_MODE = GASV_OUTPUT_MODE.STANDARD;

	//public static int CHR = -1;

	private enum GASV_MODE {
		FILTER,
		CLUSTER,
		CLUSTER_CGH,
		NO_POLYGONS
	}

	static boolean orientationsMatch(Clone c1, Clone c2) {
		if (c1.getX() < 0) {
			if (c2.getX() >= 0) {
				return false;
			} 
		} else {
			if (c2.getX() < 0) {
				return false;
			}
		}
		if (c1.getY() < 0) {
			if (c2.getY() >= 0) {
				return false;
			} 
		} else {
			if (c2.getY() < 0) {
				return false;
			}
		}
		return true;
	}

	static int overlap(Clone Clone1,Clone Clone2){
		//Note: If in --noreciprocal mode, orientations much match in order for overlap to be true!
		if (GASVMain.NORECIPROCAL_MODE) {
			if (!GASVMain.orientationsMatch(Clone1, Clone2)) {
				return 0;
			}
		}
		
		//Note: Want to forbid creating combination deletion and divergent clusters.
		//      Orientations are gone through similarly in clusterESP.java printType function.

		boolean diffChrom = false;		
		if (Clone1.getChrX() != Clone2.getChrY()) {
			diffChrom = true;
		}
		
		if(diffChrom == false){
			int numDeletion = 0;
			int numDivergent = 0;
			
			//Classify Clone1:
			if(Clone1.getX() < 0 && Clone1.getY() > 0){ numDivergent++; }
			else if(Clone1.getX() > 0 && Clone1.getY() < 0){ numDeletion++; }
	
			//Classify Clone2:
			if(Clone2.getX() < 0 && Clone2.getY() > 0){  numDivergent++; }
			else if(Clone2.getX() > 0 && Clone2.getY() < 0){ numDeletion++;  }
			
			if(numDeletion > 0 && numDivergent > 0){
				return 0;
			}
		}	

	
		int bmin1 =(int)Clone1.getBmin(); int bmax1 =(int)Clone1.getBmax();
		int bmin2 =(int)Clone2.getBmin(); int bmax2 =(int)Clone2.getBmax();
	
		//System.out.println("Clone 1: " + Clone1.getName() + "\tClone 2:" + Clone2.getName());
	
		int slope = 0;
		if(Clone1.getType().equals("same")){ slope = -1; }
		else{ slope = 1; }
	
		//System.out.println("\tClone1: " + bmin1 + "\t" + bmax1);
		//System.out.println("\tClone2: " + bmin2 + "\t" + bmax2);
	
		//Do NOT oberlap on the sweep line!
		if( (bmin1 < bmin2 && bmax1 < bmin2) || (bmin2 < bmin1 && bmax2 < bmin1)){
			//System.out.println("NO: Do not overlap on Sweep Line");
			return 0;
		}
		else{
			//bEnter = minimum Enter; bExit = minimum Exit;
			double bEnter = bmin1; if(bEnter < bmin2){ bEnter = bmin2; }
			double bExit = bmax1; if(bExit > bmax2){ bExit = bmax2; }
			
			double c1EnterTop = Clone1.getTop().Value(bEnter, slope);
			double c2EnterTop = Clone2.getTop().Value(bEnter,slope);
			double c1EnterBottom = Clone1.getBottom().Value(bEnter,slope);
			double c2EnterBottom = Clone2.getBottom().Value(bEnter,slope);
			
			double c1ExitTop = Clone1.getTop().Value(bExit,slope);
			double c2ExitTop = Clone2.getTop().Value(bExit,slope);
			double c1ExitBottom = Clone1.getBottom().Value(bExit,slope);
			double c2ExitBottom = Clone2.getBottom().Value(bExit,slope);
			
			if(c2EnterBottom > c1EnterTop && c2ExitBottom > c1ExitTop){
				//System.out.println("\tNO: C2 ABOVE C1\n");
				return 0;
			}
			else if(c2EnterTop < c1EnterBottom && c2ExitTop < c1ExitBottom){
				//System.out.println("\tNO: C1 ABOVE C2\n");
				return 0;
			}
			else{
				//System.out.println("\tYES: Overlap\n");
				return 1; //They are interleaved!
			}
			
		}
		
		//return 0;
	}


	private static void printShortUsage(){
		System.out.println("");
		System.out.println("Program: GASV (Geometric Analysis for Structural Variation)");
		System.out.println("Version: " + Constants.GASV_VERSION + "\n");
		System.out.println("Usage:   java -jar GASV.jar [options] <InputFile>" + "\n");
		System.out.println("         InputFile: PR file");
		System.out.println("           java -jar GASV.jar [options] <InputFile>\n");
		System.out.println("         InputFile: Batch File");
		System.out.println("           java -jar GASV.jar [options] --batch <InputFile>\n");
		System.out.println("Output:    <InputFile>.clusters");
		System.out.println("Default:   Reports all SVs supported by at least 4 mappings.\n");
		return;
	}

	private static void printUsage() {
		
		System.out.println("Program: GASV (Geometric Analysis for Structural Variation)");
		System.out.println("Version: " + Constants.GASV_VERSION + "\n");
		System.out.println("Usage:   java -jar GASV.jar [options] <InputFile>" + "\n");
		System.out.println("         InputFile: PR file");
		System.out.println("           java -jar GASV.jar [options] <InputFile>\n");
		System.out.println("         InputFile: Batch File");
		System.out.println("           java -jar GASV.jar [options] --batch <InputFile>\n");
		System.out.println("Output:    <InputFile>.clusters");
		System.out.println("Default:   Reports all SVs supported by at least 4 mappings.\n");
		
		System.out.println("\n****************************");
		System.out.println("General GASV Options:");
		System.out.println("****************************");
		
		System.out.println("--outputdir      <dir>   Directory in which to place output files");
		System.out.println("--lmin           <val>   Lmin value (integer) (Default: 100)");
		System.out.println("--lmax           <val>   Lmax value (integer) (Default: 400)");
		System.out.println("--nohead                 Output files will not have a header line");
		System.out.println("--output         <mode>  Specify the GASV cluster output mode (Default: standard)\n" 
						   + "                           standard   Interval coordinates without read names\n"
						   + "                           reads      Interval coordinates *with* read names\n"
						   + "                           regions    Breakpoint Polygon with read names\n");
		System.out.println("--minClusterSize <val>   Predictions with less than <val> fragments are ignored (integer) (Default: 4)");
		System.out.println("--fast                   Increases the memory usage by GASV, for faster computing, but risks OutOfMemory errors");
		System.out.println("--numChrom       <val>   Specify the number of chromosomes in this genome (integer) (Default: 24)");
		System.out.println("--batch          <file>  Batch clustering of multiple files of PRs. Input file(s) are lists of PR file(s)");
		
		System.out.println("--nonreciprocal             Only PRs with the same orientations will be clustered together.");
		System.out.println("                               E.g. +/+ and -/- inversion PRs will be segregated into separate clusters.");
		System.out.println("--maxClusterSize <val>      Predictions with more than <val> fragments are ignored (integer)");
		System.out.println("--maxCliqueSize  <val>      For predictions with more than <val> paired end sequences, no clique calculation");
		System.out.println("                               or finding of maximal sub clusters will be done (integer).");
		System.out.println("                               At most <val> PR names are output in regions model (numESP column is still accurate, locaization = -1).");
		System.out.println("--maximal                   Maximal clusters will be output");
		System.out.println("--maxPairedEndsPerWin <val> If more than <val> PRs are in a window, extra PRs are discarded. Ignored if --fast is set. (integer)");
		System.out.println("--verbose                   Prints extra information to standard output");
		System.out.println("--debug                     Run program in debug mode");
		
		System.out.println("\n****************************");
		System.out.println("Deprecated GASV Options:");
		System.out.println("****************************");
		
		System.out.println("\nCGH Mode (<mode> = --cgh or -c):");
		System.out.println("\tUsage: java -jar GASV.jar --cgh [options] <PRFile> <CGHFile>");
		System.out.println("\tOutput: <CGHFile>.clusters");
		
		System.out.println("\nCGH Options:");
		System.out.println("--paired                    Each line in the CGH file is paired with every other line, to form two dimensional data which can be represented as finite rectangles.");
		System.out.println("--minClusterSize <val>      See Cluster Mode Options (integer)");
		System.out.println("--maxClusterSize <val>      See Cluster Mode Options (integer)");
		System.out.println("--maxCliqueSize  <val>      See Cluster Mode Options (integer)");
		System.out.println("--maximal                   Maximal clusters will be output");
		System.out.println("--maxPairedEndsPerWin <val> See Cluster Mode Options (integer)");
		
		System.out.println("\nFilter Mode (<mode> = --filter or -f):");
		System.out.println("\tUsage: java -jar GASV.jar --filter [options] <ReferenceInputFile> <Target_Input_File>");
		System.out.println("\tESPs listed in the Target File will be filtered against the Reference File.");
		System.out.println("\tOutput:");
		System.out.println("\t\t<Target_Input_File>.removed : PRs that overlap PRs in the Reference File.");
		System.out.println("\t\t<Target_Input_File>.retained : PRs that represent unique variants relative to the Reference File.");
		
		System.out.println("\nFilter Mode Options:");
		System.out.println("--lmin <val>       Lmin value for reference input file (integer)");
		System.out.println("--lmax <val>       Lmax value for reference input file (integer)");
		System.out.println("--lmin2 <val>      Lmin value for target input file (integer)");
		System.out.println("--lmax2 <val>      Lmax value for target input file (integer)");
		
		System.out.println("\nNocluster Mode (<mode> = --nocluster or -n):");
		System.out.println("\tUsage: java -jar GASV.jar --nocluster [options] <InputFile>");
		System.out.println("\tOutput: <InputFile>.noclusters");
		
		System.out.println("\nOther Options:");
		System.out.println("--split            Ignored unless in --cluster mode. This option specifies that candidate split reads are output for each cluster as an additional output column. The --readlength option must be specified.");
		System.out.println("--readlength <val> The length of each read. Required for --split mode.");
		
		return;
	}
	
	
	/** 
	 *  Copy the last (from.length - x) elements of the "from" array into 
	 *  the "to" array.  Assumes "to" array is of proper length.
	 */
	private static void copyArgsExceptFirstX(int x, String[] from, String[] to) {
		for (int i=0; i < to.length; ++i) {
			to[i] = from[i+x];
		}
	}
	
	/**
	  * Add an element to the head of a string array, use to add --cluster as the default option.
	  */
	static public String[] addStringElement(String[] arr, String name){
		String[] copy = new String[arr.length+1];
		System.arraycopy(arr,0,copy,1,arr.length);
		copy[0] = name;
		return copy;
	}

	/*
	 * Determines if the help information needs to be printed or not. (i.e., was the help flag set.)
	 */
	private static int parseForHelp(String [] args) throws IllegalArgumentException{
		if(args[0].equals("-h") || args[0].equals("--h") || args[0].equals("-H") || args[0].equals("--H") 
		   || args[0].equals("--help") ||  args[0].equals("-help") || args[0].equals("--Help") || args[0].equals("--HELP")){ 
			return 1; 
		}
		else{ 
			return 0; 
		}
	}
	
	/** 
	  * Handles parsing each of the option arguments and 
	  * returns the number of options found
	  *
	  * Special Handling of: 1) help option
	  *                      2) assumption of default cluster mode.
	  */
	private static int parseOptions(String[] args) throws IllegalArgumentException {
		int count = 0;
		
		//skip the first since that is the <mode> option of either -f or -c
		for (int i=1; i<args.length; ++i) {
			//System.out.println("Processing item " + i + " = " + args[i]);
			
			if (args[i].startsWith("--")) {
				if (args[i].equals("--nohead")) {
					USE_HEADER = false;
					++count;
				} else if (args[i].equals("--outputdir")) {
					//next token should be the output directory
					GASVMain.OUTPUT_DIR = args[i+1];
					java.io.File dir = new File(GASVMain.OUTPUT_DIR);
					if (!dir.exists()) {
						throw new IllegalArgumentException(
							"Output dir doesn't exist: " 
							+ args[i+1]);
					}
					if (!dir.isDirectory()) {
						throw new IllegalArgumentException(
							"Output dir isn't a directory: "
							+ args[i+1]);
					}

					//add trailing '/' if not already there
					if (!GASVMain.OUTPUT_DIR.endsWith("/")){
						GASVMain.OUTPUT_DIR += "/";
					}
					++i;
					count += 2;
				} else if (args[i].equals("--lmin")) {
					//next token should be the value of lmin 
					try {
						GASVMain.LMIN = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						//Out.print("Couldn't parse lmin value: " + args[i+1] 
						//		+ " so using default lmin=" + Constants.DEFAULT_LMIN);
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--lmax")) {
					//next token should be the value of lmax
					try {
						GASVMain.LMAX = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						//Out.print("Couldn't parse lmax value: " + args[i+1] 
						//		+ " so using default lmax=" + Constants.DEFAULT_LMAX);
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--lmin2")) {
					//next token should be the value of lmin2 
					try {
						GASVMain.LMIN2 = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						//Out.print("Couldn't parse lmin2 value: " + args[i+1] 
						//		+ " so using default lmin2=" + Constants.DEFAULT_LMIN);
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--lmax2")) {
					//next token should be the value of lmax2
					try {
						GASVMain.LMAX2 = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						//Out.print("Couldn't parse lmax2 value: " + args[i+1] 
						//		+ " so using default lmax2=" + Constants.DEFAULT_LMAX);
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--debug")) {
					Out.setDebugLevel(Out.MAX_LVL);
					++count;

				} else if (args[i].equals("--verbose")) {
					// if debug already set high, don't set any lower
					if (Out.getDebugLevel() < Out.VERBOSE_LVL) {
						Out.setDebugLevel(Out.VERBOSE_LVL);
					}
					++count;

				} else if (args[i].equals("--batch")) {
					GASVMain.USE_BATCH = true;
					++count;
				} else if (args[i].equals("--fast")) {
					GASVMain.USE_FAST = true;
					GASVMain.SAVE_MEMORY = false;
					++count;
				} else if (args[i].equals("--paired")) {
					USE_PAIRED_CGH = true;
					++count;
				} else if (args[i].equals("--split")) {
					FIND_SPLIT_READS = true;
					++count;
				} else if (args[i].equals("--readlength")) {
					//next token should be the value of lmax2
					try {
						GASVMain.READ_LENGTH = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --readlength value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--minClusterSize")) {
					//next token should be the value of min cluster size
					try {
						GASVMain.MIN_CLUSTER_SIZE = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --minClusterSize value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--maxClusterSize")) {
					//next token should be the value of max cluster size
					try {
						GASVMain.MAX_CLUSTER_SIZE = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --maxClusterSize value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;

				} else if (args[i].equals("--maxCliqueSize")) {
					//next token should be the value of max cluster size
					try {
						GASVMain.MAX_CLIQUE_SIZE = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --maxCliqueSize value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--maxPairedEndsPerWin")) {
					//next token should be the value of max cluster size
					try {
						GASVMain.MAX_READS_PER_WIN = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --maxPairedEndsPerWin value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
				} else if (args[i].equals("--numChrom")) {
					//next token should be the number of chromosomes in use
					try {
						NUM_CHROM = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex) { 
						Out.print("Couldn't parse --numChrom value: " + args[i+1] 
								+ " so ignoring this option.");
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;
	
				} else if (args[i].equals("--output")) {
					//next token should be the desired output mode 
					String mode = args[i+1];
					if (mode.equalsIgnoreCase("standard")) {
						OUT_MODE = GASV_OUTPUT_MODE.STANDARD;
					} else if (mode.equalsIgnoreCase("reads")) {
						OUT_MODE = GASV_OUTPUT_MODE.READS;
					} else if (mode.equalsIgnoreCase("regions")) {
						OUT_MODE = GASV_OUTPUT_MODE.REGIONS;
					} else {
						Out.print("Couldn't parse --output value: " + mode);
						throw new IllegalArgumentException(args[i+1]);
					}
					++i;
					count += 2;


				} else if (args[i].equals("--maximal")) {
					USE_MAXIMAL = true;
					++count;

				} else if (args[i].equals("--noreciprocal") || args[i].equals("--nonreciprocal") ) {
					GASVMain.NORECIPROCAL_MODE = true;
					++count;

				} else {
					throw new IllegalArgumentException(
							"Unrecognized option: " 
							+ args[i]);
				}
			} else {
				break;
			}
		}
		
		if (!GASVMain.USE_BATCH) {
			if (LMIN < 0) {
				Out.print("Warning: no --lmin specified so using "
					+ " default value of " + Constants.DEFAULT_LMIN);
				LMIN = Constants.DEFAULT_LMIN;
			}
			if (LMAX < 0) {
				Out.print("Warning: no --lmax specified so using" 
					+ " default value of " + Constants.DEFAULT_LMAX);
				LMAX = Constants.DEFAULT_LMAX;
			}

			if (args.length > 0 && (args[0].equals("--filter") || args[0].equals("-f"))) {
				if (LMIN2 < 0) {
					Out.print("Warning: no --lmin2 specified in filter mode so using "
							+ " default value of " + Constants.DEFAULT_LMIN);
					LMIN2 = Constants.DEFAULT_LMIN;
				}
				if (LMAX2 < 0) {
					Out.print("Warning: no --lmax2 specified in filter mode so using" 
							+ " default value of " + Constants.DEFAULT_LMAX);
					LMAX2 = Constants.DEFAULT_LMAX;
				}
			}

			// If running in "window" mode, and filtering...
			// Since there are two LMAX values, one for cancer and one for normal,
			// 	need to pick one (the bigger one) on which to base WIN_SIZE
			//	since filtering needs to move in lock-step with the same windows
			if (LMAX > LMAX2) {
				MAX_LMAX = LMAX;
			} else {
				MAX_LMAX = LMAX2;
			}
		} 
		if (FIND_SPLIT_READS) {
			if (READ_LENGTH < 1) {
				Out.print("ERROR: must specify a --readlength value if using --split option.");
				throw new IllegalArgumentException("--split");
			}	
		}
		return count;
	}

	public static void main(String[] args) throws IOException, CloneNotSupportedException, NullPointerException{

		GASV_MODE mode = null;
		
		/* 
		 * Determine if help information should be printed.
		 */
		//No options given: Print short usage statement;
		if(args.length == 0){ printShortUsage(); return; }
		//Help asked for, print the full usage information.
		int printFullHelpMessage = parseForHelp(args);
		if(printFullHelpMessage > 0){ printUsage(); return;}
		
		
		/*
		 * Set Default clustering option if not given.
		 */
		if (args[0].equals("--filter") || args[0].equals("-f") || args[0].equals("--cluster") || args[0].equals("-c") 
			|| args[0].equals("--cgh") || args[0].equals("-g") || args[0].equals("--nocluster") || args[0].equals("-n") )  {}
		else{
			args = addStringElement(args,"--cluster");
		}
		
		/*
		 * Process arguments as normal.
		 */
		int numOpts = parseOptions(args);
		int numArgsNoOpts = args.length - numOpts;
		
		//Error Checking: Too few or too many arguments.
		if(numArgsNoOpts < 2) {
			System.out.println("Error: Missing arguments, check usage or get full help with --help");
			return;
		} 
		if (numArgsNoOpts > 3) {
			System.out.println("Error: Too many arguments, check usage or get full help with --help");
			return;
		} 

		if (args[0].equals("--filter") || args[0].equals("-f")) {
			mode = GASV_MODE.FILTER;
			if (numArgsNoOpts == 3) {
				String[] newArgs = new String[2];
				copyArgsExceptFirstX(numOpts+1, args, newArgs);

				if (GASVMain.USE_BATCH && GASVMain.SAVE_MEMORY) {
					// If in window mode AND batch mode, will be multiple LMAX's so find the max
					int lmax1 = ReadInput.findMaxLmaxInBatchFile(newArgs[0]);
					int lmax2 = ReadInput.findMaxLmaxInBatchFile(newArgs[1]);
					if (lmax1 > lmax2) {
						GASVMain.MAX_LMAX = lmax1;
					} else {
						GASVMain.MAX_LMAX = lmax2;
					}
				}		
				FilterESP.filterESP(newArgs[0], newArgs[1]);
			} else {
				System.out.println("ERROR: Missing required arguments in --filter mode.");
				printUsage();
			}
			
		} else if ((args[0].equals("--cluster") || args[0].equals("-c"))) {			
			mode = GASV_MODE.CLUSTER;
			if (numArgsNoOpts == 2) {
				String[] newArgs = new String[1];
				copyArgsExceptFirstX(numOpts+1, args, newArgs);
				if (GASVMain.USE_BATCH && GASVMain.SAVE_MEMORY) {
					// If in window mode AND batch mode, will be multiple LMAX's so find the max
					GASVMain.MAX_LMAX  = ReadInput.findMaxLmaxInBatchFile(newArgs[0]);
				}
				if (FIND_SPLIT_READS) {
					ClusterESP.clusterESPAndFindSplitReads(newArgs[0]);
				} else { 
					ClusterESP.clusterESP(newArgs[0]);
				}
			} else {
				System.out.println("ERROR: Too many arguments provided in --cluster mode.");
				printUsage();
			}
		} else if ((args[0].equals("--cgh") || args[0].equals("-g"))) {
			mode = GASV_MODE.CLUSTER_CGH;
			if (numArgsNoOpts == 3) {
				String[] newArgs = new String[2];
				copyArgsExceptFirstX(numOpts+1, args, newArgs);
				if (GASVMain.USE_BATCH && GASVMain.SAVE_MEMORY) {
					// If in window mode AND batch mode, will be multiple LMAX's so find the max
					GASVMain.MAX_LMAX  = ReadInput.findMaxLmaxInBatchFile(newArgs[0]);
				}
				ClusterESP.clusterESPAndCGH(newArgs[0], newArgs[1]);
			} else {
				System.out.println("ERROR: Too many arguments provided in --cgh mode.");
				printUsage();
			}
		} else if ((args[0].equals("--nocluster") || args[0].equals("-n"))) {
			mode = GASV_MODE.NO_POLYGONS;
			if (numArgsNoOpts == 2) {
				String[] newArgs = new String[1];
				copyArgsExceptFirstX(numOpts+1, args, newArgs);

				if (GASVMain.USE_BATCH && GASVMain.SAVE_MEMORY) {
					GASVMain.MAX_LMAX = ReadInput.findMaxLmaxInBatchFile(newArgs[0]);
				}
				ConvertFileToPolygons.convertFileToPolys(newArgs[0], GASVMain.LMIN, GASVMain.LMAX, GASVMain.USE_BATCH, GASVMain.USE_FAST);
			} else {
				System.out.println("ERROR: Too many arguments provided in --nocluster mode.");
				printUsage();
			}
		} else {
			System.out.println("ERROR: Unrecognized <mode> argument: " + args[0]);
			printUsage();
		}

	
	
        System.out.println("GASV Program has Finished Successfully.");
    }
}
