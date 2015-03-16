
package gasv.bamtogasv;


/**
 * Copyright 2010,2012 Benjamin Raphael, Suzanne Sindi, Hsin-Ta Wu, Anna Ritz, Luke Peng, Layla Oesper
 *
 *  This file is part of GASV.
 *
 *  gasv is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GASV is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with gasv.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import bio.comp.jlu.readxplorer.tools.gasv.GASVUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.picard.sam.FixMateInformation;
import net.sf.samtools.FileTruncatedException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.util.RuntimeEOFException;
import net.sf.samtools.util.RuntimeIOException;

/**
 * Bam to GASV Processor
 * TODO: LICENSE INFORMATION? PICARD LICENSE INFORMATION?
 * TODO: write a cleanup function to close things
 * TODO: Compress concordant file.
 *
 * @author Anna Ritz, Suzanne Sindi, Hsin-Ta Wu, and Layla Oesper
 * @version May 2012
 *
 * This class uses the Picard Java SDK to manipulate BAM files.  The program
 * designed similar to Hsin-Ta's BamPreprocessor.pl script that was
 * originally released with GASV.
 *
 */
public class BAMToGASV {

	/* Options and Default Values */
	public String BAMFILE;
	public String LIBRARY_SEPARATED="sep"; // changed from 'library-separated'
	public String OUTPUT_PREFIX;
	public int MAPPING_QUALITY = 10;
	public String CUTOFF_LMINLMAX="PCT=99%";
	public int USE_NUMBER_READS = 500000;
	public String CHROMOSOME_NAMING_FILE = null;
	public int PROPER_LENGTH = 10000;
	public String PLATFORM="illumina"; // note lower case
	public boolean WRITE_CONCORDANT = false;
	public boolean WRITE_LOWQ = false;
	public ValidationStringency STRINGENCY = ValidationStringency.SILENT;
	public boolean GASVPRO_OUTPUT = false; // GASVPro
	public boolean BATCH = false;
	public boolean NOSORT = false;

	/* Additional Variables */
	public boolean someLibPassed = false; // true if at least one library is paired, false otherwise.
	public boolean IS_URL; // true if the BAM file is a URL, false otherwise.
	public final int NUM_LINES_FOR_EXTERNAL_SORT = 500000; // number of lines for external sort.
	public int BAD_RECORD_COUNTER; // counts the # of poorly-formatted records.
	public long GL = 3000000000L; // default genome size

	/* Patterns for CUTOFF_LMINLMAX flag */
	public Pattern pct = Pattern.compile("PCT=(\\d+)%");
	public Pattern std = Pattern.compile("SD=(\\d+)");
	public Pattern exact = Pattern.compile("EXACT=(\\d+),(\\d+)");
	public Pattern file = Pattern.compile("FILE=(.*)");

	/* Data Structures */
	public HashMap<String,String> LIBRARY_IDS; // <library_id,full_library_name>
	public HashMap<String,Library> LIBRARY_INFO; // <full_library_name,Library Object>
	public ArrayList<String> LIBRARY_NAMES; // list of <full_library_name>
	public HashMap<String,String> HIGHQ_INDICATOR; // <full_library_name,#of reads>
	public HashMap<String,String> LOWQ_INDICATOR; // <full_library_name,#of reads>
	public HashMap<String,BufferedWriter> CONCORDANT_FILES; // <filename, filewriter>

	/* (Done by Anna on 5/5)
	 * (1) Make HIGHQ and LOWQ indicator data structures HashMaps (key; fragmentname), values are SAMRecord strings.
	 *     We can do this with getSAMString()
	 * (2) As parsing, write the the pairs out as a sam file.
	 */

	/* Sorters for ESPs and for Concordants */
	public ExternalSort discordantSorter,concordantSorter;

	/* Static Variables */
	public static HashMap<String,Boolean> NON_DEFAULT_REFS; // <refname,true>
	public static HashMap<String,Integer> CHR_NAMES; // <non-default chr naming, #>
	public static VariantType[] VARIANTS = VariantType.values(); // list of variant types

	/* Output BAM File Names */
	public BufferedWriter LOWQ_BAM_FILE;

	/**
	 * Main class.
	 * @param args - see usage information.
	 */
	public static void main(String[] args) {

		// Get instance of BAMToGASV
		BAMToGASV b2g = null;
		try {
			b2g = new BAMToGASV(args);
		} catch (IOException e) {
			System.out.println("ERROR: cannot open error log for writing. Make sure you can write to the BAM file's directory (or the OUTPUT_PREFIX's directory).");
			System.exit(-1);
		}

		// Get library information from header
		b2g.getHeaderInformation();

		// Open and read BAM file.
		b2g.readBAMfile();

		if(!b2g.someLibPassed)  // BAM file read exited prematurely.  Run fixmates and die.
			b2g.fixMates();

		// If BAM file read exited prematurely again, die with an error.
		if(!b2g.someLibPassed)
			System.out.println("After Running fixMates, reading the BAM file resulted in errors.  Consult the FAQ for troubleshooting solutions.");

		// MERGE CONCORDANT FILES (Anna - new June 30,2012)
		// Only when GASVPRO_FLAG is set AND LIBRARY_SEPARATED is not 'all'
		if(b2g.GASVPRO_OUTPUT)
			b2g.mergeConcordantFiles();
		// (1) Get list of all concordant files.
		// (2) Merge all concordant files using Sorter.merge() function. Write to OUTPUT_PREFIX+"_all.concordant
		// (3) Delete all other concordant files

		// Write .info and .gasv.in files.
		b2g.writeInfoFile();
		b2g.writeGASVInputFile();
		b2g.writeGASVPROInputFile();

		// Report output files and any skipped records.
		System.out.println("BAMToGASV complete.\n");
		b2g.printSkipped();
		b2g.printOutputFiles();
	}

	/**
	 * Constructor.
	 * @param args - see usage information for arguments.
	 * @throws IOException
	 */
	public BAMToGASV(String[] args) throws IOException {

		// Parse & print arguments.  If arguments didn't parse correctly,
		// then print the usage and exit.
		boolean success = parseArguments(args);
		if (!success) {
			printUsage();
			System.exit(-1);
		}
		printArguments();

		// initialize variables.
		LIBRARY_IDS = new HashMap<String,String>();
		LIBRARY_NAMES = new ArrayList<String>();
		LIBRARY_INFO = new HashMap<String,Library>();
		HIGHQ_INDICATOR = new HashMap<String,String>();
		LOWQ_INDICATOR = new HashMap<String,String>();
		NON_DEFAULT_REFS = new HashMap<String,Boolean>();
		if(NOSORT) {
			CONCORDANT_FILES = new HashMap<String,BufferedWriter>();
		}
		BAD_RECORD_COUNTER = 0;

		// Try opening sorters and catch errors.
		try {
			discordantSorter = new ExternalSort("ESP");
			int[] concordantSortOrder = {1,2,3};
			concordantSorter = new ExternalSort(concordantSortOrder);
		} catch (Exception e) {
			System.out.println("ERROR while instantiating the ExternalSort object.");
			System.exit(-1);
		}

		// If CHROMOSOME_NAMING_FILE was passed, parse non-default chromsome names.
		if(CHROMOSOME_NAMING_FILE != null) {
			try {
				getChromosomeNames();
			} catch (FileNotFoundException e) {
				System.out.println("ERROR: " + CHROMOSOME_NAMING_FILE + " does not exist.");
				System.exit(-1);
			}
		} else { // to be safe, set CHR_NAMES to null.
			CHR_NAMES = null;
		}

	}

	/**
	 * Parses arguments
	 * @param args - see usage instructions.
	 * @return true if there are no errors, false otherwise.
	 */
	public boolean parseArguments(String[] args) {

		// Arguments come in pairs AND 1 bam file, so there should be an odd number of parameters.
		if(args.length % 2 == 0) {
			System.out.println("Error! Incorrect number of arguments (perhaps an option is missing a value).");
			return false;
		}

		// BAM file will always be the first argument.
		BAMFILE=args[0];

		// default for OUTPUT_PREFIX is BAM file name.
		OUTPUT_PREFIX = BAMFILE;

		// Determine whether the BAM file is a URL.
		IS_URL = false;
		if(!(new File(BAMFILE).exists())) {
			try {
				URL url = new URL(BAMFILE);
				IS_URL = true;
			} catch (Exception e) {
				System.out.println("Error! "+BAMFILE+" does not exist.");
				return false;
			}
		}

		// For each pair of arguments, set the appropriate flags.
		for(int i=1;i<args.length;i+=2) {
			if(args[i].equalsIgnoreCase("-LIBRARY_SEPARATED")) {
				if(args[i+1].equalsIgnoreCase("sep") || args[i+1].equalsIgnoreCase("all"))
					LIBRARY_SEPARATED = args[i+1].toLowerCase();
				else {
					System.out.println("Error! LIBRARY_SEPARATED option can only be 'sep' or 'all'.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-OUTPUT_PREFIX")) {
				OUTPUT_PREFIX = args[i+1];
			} else if(args[i].equalsIgnoreCase("-MAPPING_QUALITY")) {
				try {
					MAPPING_QUALITY = Integer.parseInt(args[i+1]);
					if(MAPPING_QUALITY < 0)
						throw new Exception();
				} catch (Exception e) {
					System.out.println("Error! MAPPING_QUALITY must be a non-negative integer.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-CUTOFF_LMINLMAX")) {
				CUTOFF_LMINLMAX = args[i+1];
				if(!pct.matcher(CUTOFF_LMINLMAX).matches() &&
						!std.matcher(CUTOFF_LMINLMAX).matches() &&
						!exact.matcher(CUTOFF_LMINLMAX).matches() &&
						!file.matcher(CUTOFF_LMINLMAX).matches()) {
					System.out.println("Error! "+CUTOFF_LMINLMAX+" is not a valid CUTOFF_LMINLMAX option.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-USE_NUMBER_READS")) {
				try {
					USE_NUMBER_READS = Integer.parseInt(args[i+1]);
					if(USE_NUMBER_READS < 0)
						throw new Exception();
				} catch (Exception e) {
					System.out.println("Error! USE_NUMBER_READS must be an non-negative integer.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-CHROMOSOME_NAMING")) { // 5/22 Update flag as CHROMOSOME_NAMING
				CHROMOSOME_NAMING_FILE = args[i+1];
				if(!(new File(CHROMOSOME_NAMING_FILE).exists())) {
					System.out.println("Error! " + CHROMOSOME_NAMING_FILE + " does not exist.");
				}
			} else if(args[i].equalsIgnoreCase("-PROPER_LENGTH")) {
				try {
					PROPER_LENGTH = Integer.parseInt(args[i+1]);
					if(PROPER_LENGTH < 0)
						throw new Exception();
				} catch (Exception e) {
					System.out.println("Error! PROPER_LENGTH must be a non-negative integer.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-PLATFORM")) {
				if(args[i+1].equalsIgnoreCase("illumina") || args[i+1].equalsIgnoreCase("solid") || args[i+1].equalsIgnoreCase("matepair"))
					PLATFORM = args[i+1].toLowerCase();
				else {
					System.out.println("Error! PLATFORM option can only be 'Illumina', 'SOLiD' or 'MatePair'.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-WRITE_CONCORDANT")) {
				if(args[i+1].equalsIgnoreCase("true"))
					WRITE_CONCORDANT = true;
				else if (args[i+1].equalsIgnoreCase("false"))
					WRITE_CONCORDANT = false;
				else {
					System.out.println("Error! WRITE_CONCORDANT option can only be 'True' or 'False'.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-WRITE_LOWQ")) {
				if(args[i+1].equalsIgnoreCase("true"))
					WRITE_LOWQ = true;
				else if (args[i+1].equalsIgnoreCase("false"))
					WRITE_LOWQ = false;
				else {
					System.out.println("Error! WRITE_LOWQ option can only be 'True' or 'False'.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-VALIDATION_STRINGENCY")) {
				if(args[i+1].equalsIgnoreCase("silent"))
					STRINGENCY = ValidationStringency.SILENT;
				else if(args[i+1].equalsIgnoreCase("lenient"))
					STRINGENCY = ValidationStringency.LENIENT;
				else if(args[i+1].equalsIgnoreCase("strict"))
					STRINGENCY = ValidationStringency.STRICT;
				else {
					System.out.println("ERROR: VALIDATION_STRINGENCY option can only be 'Silent','Lenient', or 'Strict'.");
					return false;
				}
			} else if(args[i].equalsIgnoreCase("-GASVPRO")) { // GASVPro
				if(args[i+1].equalsIgnoreCase("true")){
					WRITE_CONCORDANT = true;
					LIBRARY_SEPARATED = "sep";
					GASVPRO_OUTPUT = true;
				}
				else if (args[i+1].equalsIgnoreCase("false")){
					GASVPRO_OUTPUT = false;
				}
			} else if(args[i].equalsIgnoreCase("-NOSORT")) { // Don't Sort Concordants
				if(args[i+1].equalsIgnoreCase("true")){
					NOSORT = true;
				}
				else if (args[i+1].equalsIgnoreCase("false")){
					NOSORT = false;
				} else {
					System.out.println("Error! Option " + args[i] + " does not exist.");
					return false;
				}
			} else {
				System.out.println("Error! Option " + args[i] + " does not exist.");
				return false;
			}
		}

		// if it's a  URL and the OUTPUT_PREFIX is the BAMFILE, die.
		if(IS_URL && OUTPUT_PREFIX.equals(BAMFILE)) {
			System.out.println("ERROR: OUTPUT_PREFIX must be specified if BAMFILE is a URL.");
			return false;
		}

		if(!WRITE_CONCORDANT && NOSORT) {
			System.out.println("ERROR: NOSORT flag cannot be set if Concordant Pairs are not written.");
		}

		return true;
	}

	/**
	 * Prints the usage information.
	 **/
	public void printUsage() {
		System.out.println("\nProgram: BAMToGASV\nVersion: 2.0.1\n");
		System.out.println("USAGE (src): java -Xms512m -Xmx2048m BAMToGASV <bam file or hyperlink> [Options]\n" +
				"USAGE (jar): java -Xms512m -Xmx2048m -jar BAMToGASV.jar <bam file or hyperlink> [Options]\n\n" +
				"Options are:\n" +
				"-LIBRARY_SEPARATED [String] (Default: sep)\n" +
				"\tsep\tProduce a set of output files for each library.\n" +
				"\tall\tProduce a single set of output files for all libraries.\n" +
				"-OUTPUT_PREFIX [String] (Default: BAM filename)\n" +
				"\tThe prefix for your output files.\n" +
				"-MAPPING_QUALITY [Integer] (Default: 10)\n" +
				"\tMapping quality threshold for reads.\n" +
				"-CUTOFF_LMINLMAX [String] (Default: PCT=99%)\n" +
				"\tSpecifies lower and upper bounds on the fragment distribution.\n" +
				"\tPCT=X%\tTake the quantile on the top/bottom X percent.\n" +
				"\tSD=X\tTake the standard deviation above/below the mean.\n"+
				"\tEXACT=X,Y\tSet Lmin to X and Lmax to Y.\n" +
				"\tFILE=fname\tFile of the form '<libname>\\t<CUTOFF_LMINLMAX>' for using different cutoffs on different libraries.\n" +
				"-USE_NUMBER_READS [Integer] (Default: 500000)\n" +
				"\tThe number of fragments in the BAM file to use in computing Lmax and Lmin.  To get a good estimate of the distribution, up to 1 million fragments is sufficient for most applicaitons.\n" +
				"-CHROMOSOME_NAMING [String] (Default: none)\n" +
				"\tFile of the form '<ChrName>\\t<IntegerID>' for specifying non-default chromosome namings.  Default chromosome namings are Integers,X, and Y, either alone or with a 'chr' prefix.\n" +
				"-PROPER_LENGTH [Integer] (Default: 10000)\n" +
				"\tIgnore ESPs with separation larger than PROPER_LENGTH when calculating Lmin and Lmax.\n" +
				"-PLATFORM [String] (Default: Illumina)\n" +
				"\tIllumina\tReads are sequenced with the Illumina platform (paired-read orientation --> <--).\n" +
				"\tSOLiD\tReads are sequenced with the SOLiD platform.\n" +
				"\tMatePair\tReads are sequenced with the mate pair orientation (outward orientation <-- -->).\n" +
				"-WRITE_CONCORDANT [Boolean] (Default: False)\n" +
				"\tTrue\tWrites concordant file. Warning - this will be large!\n"+
				"-WRITE_LOWQ [Boolean] (Default: False)\n" +
				"\tTrue\tWrites low quality read file. Warning - this will be large!\n"+
				"-VALIDATION_STRINGENCY [String] (Default: silent)\n"+
				"\tsilent\tRead SAM records without any validation.\n"+
				"\tlenient\tRead SAM records and emit a warning when a record is not formatted properly.\n"+
				"\tstrict\tRead SAM records and die when a record is not formatted properly.\n"+
				"-GASVPRO [Boolean] (Default: False)\n"+
				"\tTrue\tGenerate GASVPro parameters file and concordant file. Warning - this will be large!\n"+
				"-NOSORT [Boolean] (Default: False)\n"+
				"\tTrue\tDo not sort the concordant file. Improves running time.\n"+
		"Refer to the Manual for more details.");
	}

	/**
	 * Prints the arguments that are set.  Currently, if LIBRARY_SEPARATED is
	 * not set to 'all' but there is no library information, then LIBRARY_SEPARATED
	 * flag is changed.
	 **/
	public void printArguments() {
		System.out.println("\n===================================");
		System.out.println("Arguments are:");
		if(IS_URL)
			System.out.println("  BAM File URL = " + BAMFILE);
		else
			System.out.println("  BAM File = " + BAMFILE);
		System.out.println("  Output Prefix = " + OUTPUT_PREFIX);
		System.out.println("  Minimum Mapping Quality = " + MAPPING_QUALITY);
		System.out.println("  Lmin/Lmax Cutoff = \"" + CUTOFF_LMINLMAX+"\"");
		System.out.println("  # of Reads for Calculating Lmin/Lmax, Checking Pairs, and Checking Variant Types = " + USE_NUMBER_READS);
		System.out.println("  Chromosome Naming File (null if none specified) = " + CHROMOSOME_NAMING_FILE);
		System.out.println("  Maximum Length for a Proper Pair = " + PROPER_LENGTH);
		System.out.println("  Platform = \"" + PLATFORM+"\"");
		System.out.println("  Write Concordant File? " + WRITE_CONCORDANT);
		System.out.print("  Separate Libraries? ");
		if(LIBRARY_SEPARATED.equals("all"))
			System.out.println("false");
		else
			System.out.println("true");
		System.out.println("  Validation Stringency: " + STRINGENCY);
		System.out.print("  Prepare GASVPro Output? ");
		if(GASVPRO_OUTPUT)
			System.out.println("true");
		else
			System.out.println("false");
		System.out.println("===================================\n");
	}

	/**
	 * If there is a non-default chromosome naming file, read the
	 * file and fill the CHR_NAMES variable.
	 **/
	public void getChromosomeNames() throws FileNotFoundException {
		CHR_NAMES = new HashMap<String,Integer>();
		Scanner scan = new Scanner(new File(CHROMOSOME_NAMING_FILE));
		String name;
		int num;
		while(scan.hasNext()) { // lines are of the form <name> <num>
			try {
				name = scan.next();
				num = scan.nextInt();
				CHR_NAMES.put(name,num);
			} catch (NumberFormatException e) {
				System.out.println("ERROR: some line in " + CHROMOSOME_NAMING_FILE + " is not of the form <name>\t<integer.");
				System.exit(-1);
			}
		}
	}

	/**
	 * Gets header information from BAM file.
	 * Does not require the file to be read.
	 **/
	public void getHeaderInformation() {
		System.out.println("Processing Header Information...");
		String platform,id,libname;
		SAMReadGroupRecord record;

		// keep track of the platforms we've seen in this BAM file.
		HashMap<String,String> viewedPlatforms = new HashMap<String,String>();

		// open the file and read it.
		SAMFileReader reader = null;
		if(!IS_URL)
			reader = new SAMFileReader( new File(BAMFILE));
		else {
			try {
				URL url = new URL(BAMFILE);
				reader = new SAMFileReader( url,null,false);
			} catch (Exception e) {
				System.out.println("ERROR: Error reading URL " + BAMFILE + ".");
				System.exit(-1);
			}
		}

		// get SAMFileHeader object
		SAMFileHeader header = reader.getFileHeader();

		// GASGPro using - getSequenceInfo in the header
		if (GASVPRO_OUTPUT){
			SAMSequenceDictionary SeqInfo = header.getSequenceDictionary();
			if (SeqInfo.getReferenceLength() == 0)
				System.out.println("  WARNING: Can't find genome length in Header, using default setting 3,000,000,000.");
			else {
				System.out.println("  Genome length: " + SeqInfo.getReferenceLength());
				GL = SeqInfo.getReferenceLength();
			}
		}
		// getReadGroups() returns a list of SAMReadGroupRecords.
		Iterator<SAMReadGroupRecord> groups = header.getReadGroups().iterator();

		while(groups.hasNext()) {

			// get platform, library ID, and library name.
			record = groups.next();
			platform = record.getPlatform();
			id = record.getId();
			libname = record.getLibrary();

			if(platform == null || platform.equals(""))
				System.out.println("  WARNING: Platform not specified for library "+libname+". Proceeding with "+PLATFORM+". You can specify the platform as Illumina, SOLiD or MatePair using -PLATFORM.");

			else if(!platform.toLowerCase().equals(PLATFORM))
				System.out.println("  WARNING: Platform in library " + libname + " is " + platform+". Proceeding with "+PLATFORM+". You can specify the platform as Illumina, SOLiD or MatePair using -PLATFORM.");

			else if(!platform.equals("")) // if platform is non an empty string, we've seen it.
				viewedPlatforms.put(platform.toLowerCase(),platform);

			if(LIBRARY_SEPARATED.equals("sep")) {
				//System.out.println("  ID " + id+" associated with Library " + libname+".");
				LIBRARY_IDS.put(id,libname);
			} else { // "all"
				//System.out.println("  ID " + id + " associated with Library " + libname + " with ID " + id + " considered in \"all\"");
				LIBRARY_IDS.put(id,LIBRARY_SEPARATED);
			}
		}

		// if there is more than one viewed platform, die with an error.
		if(viewedPlatforms.size() > 1) {
			System.out.println("  ERROR: Different libraries have different platforms.  The platforms found are:");
			Iterator<String> iter = viewedPlatforms.values().iterator();
			while(iter.hasNext())
				System.out.println("\t"+iter.next());
			System.out.println("Check your BAM/SAM file.");
			reader.close();
			System.exit(-1);
		}

		if(LIBRARY_IDS.size() == 0) {
			// 5/22 Show WARNING rather than ERROR
			System.out.println("  WARNING: No library information found in the BAM.  Proceeding with -LIBRARY_SEPARATED \"all\" flag.");
			LIBRARY_SEPARATED = "all";
			LIBRARY_IDS.put("all",LIBRARY_SEPARATED);
		}

		reader.close();

		// Finally, make list of library information
		Iterator<String> iter = LIBRARY_IDS.values().iterator();
		while(iter.hasNext()) {
			libname = iter.next();
			if(!LIBRARY_NAMES.contains(libname))
				LIBRARY_NAMES.add(libname);
		}

		System.out.println("  Proceeding with the following libraries:");
		for(int i=0;i<LIBRARY_NAMES.size();i++)
			System.out.println("    \""+LIBRARY_NAMES.get(i)+"\"");
		System.out.println();
	}

	/**
	 * Check if pairing info exists for this library.
	 * @param lib - Library object.
	 **/
	public void checkPairingInfo(Library lib) {
		System.out.println("\nChecking pairing info for library \""+lib.name+"\"...");
		String libname = lib.name;

		if(!lib.mateFound && lib.counter > 0){
			System.out.println("WARNING: Library " + libname +
					" has no pairing info in the first " + USE_NUMBER_READS + " fragments.");
		} else if(!lib.mateFound) {
			System.out.println("WARNING: Library " + libname +
					" has no pairing info in the first " + USE_NUMBER_READS + " fragments.");
		}else {
			System.out.println("  Library \""+libname+"\" has mated pairs.");
			someLibPassed = true;
		}
	}

	/**
	 * Calls the fixMates() function from Picard.
	 * Picard's function currently exits.
	 **/
	public void fixMates() {
		System.out.println("Running Fixmate() to pair reads.  When this is done, re-run BAMToGASV.jar with the new BAM file.");

		// NEWBAMFILE is the name of the file that will be fixed if the program runs
		// without error.
		String NEWBAMFILE = OUTPUT_PREFIX+".fixmate.bam";

		System.out.println("Writing fixed BAM/SAM file to "+NEWBAMFILE);
		String[] fixmateargs = {"INPUT="+BAMFILE,"OUTPUT="+NEWBAMFILE};
		try {
			FixMateInformation.main(fixmateargs);
		} catch (Exception e) {
			System.out.println("Error while fixing mate information.  If the read names do not have a \"/1\" or a \"/2\" suffix, then fixmate won't work.");
			System.exit(-1);
		}

		BAMFILE = NEWBAMFILE;
		System.out.println("BAMFILE to be accessed is now "+BAMFILE);
	}

	/**
	 *
	 * Computes the Lmin and Lmax
	 * @param lib - Library object.
	 **/
	public void getLminLmax(Library lib) {
		System.out.println("Getting Lmin and Lmax for library \"" + lib.name+"\"...");

		// Step 0: Calculate Mean and STD
		getMeanAndSTDForNovoalign(lib);

		// Step 1: Determine which metric to use.

		/// EXACT CUTOFF_LMINLMAX ///
		Matcher match = exact.matcher(CUTOFF_LMINLMAX);
		if(match.matches()) {
			int tmpLmin = Integer.parseInt(match.group(1));
			int tmpLmax = Integer.parseInt(match.group(2));
			// If it matches exact Lmin/Lmax, then set this for each library.
			lib.Lmin = tmpLmin;
			lib.Lmax = tmpLmax;
			System.out.println("  Setting Exact Lmin="+tmpLmin + " and Lmax="+tmpLmax+".");
			return; // we're done.
		}

		/// STD CUTOFF_LMINLMAX ///
		match = std.matcher(CUTOFF_LMINLMAX);
		if(match.matches()) {
			int standarddev = Integer.parseInt(match.group(1));
			getLminLmaxSTD(lib,standarddev);
			checkLmin(lib);
			System.out.println("  Library \""+lib.name+"\" has Lmin="+lib.Lmin +
					" and Lmax="+lib.Lmax + " using standard deviation method in first " +
					lib.counter + " reads.");
			return; // we're done
		}

		/// PCT CUTOFF_LMINLMAX ///
		match = pct.matcher(CUTOFF_LMINLMAX);
		if(match.matches()) {
			int percent = Integer.parseInt(match.group(1));
			getLminLmaxPCT(lib,percent);
			checkLmin(lib);
			System.out.println("  Library \""+lib.name+"\" has Lmin="+lib.Lmin +
					" and Lmax="+lib.Lmax + " using percentile method in first " + lib.counter + " reads.");
			return; // we're done
		}

		/// FILE CUTOFF_LMINLMAX ///
		match = file.matcher(CUTOFF_LMINLMAX);
		if(match.matches()) {
			String file = match.group(1);
			String pattern;
			Scanner scan = null;
			try {
				scan = new Scanner(new File(file));
			} catch (FileNotFoundException e) {
				System.out.println("ERROR: "+file+" does not exist.");
				System.exit(-1);
			}
			while(scan.hasNext()) {
				String libname = scan.next();
				pattern = scan.next();

				// we're only looking for the name of this library.
				if(!libname.equals(lib.name))
					continue;

				// check if pattern is ok.
				if(!pct.matcher(pattern).matches() &&
						!std.matcher(pattern).matches() &&
						!exact.matcher(pattern).matches()) {
					System.out.println("ERROR: library " + lib.name + " does not have a valid CUTOFF_LMINLMAX option.");
					System.exit(-1);
				}

				/// EXACT CUTOFF_LMINLMAX
				match = exact.matcher(pattern);
				if(match.matches()) {
					// If it matches exact Lmin/Lmax, then set this for the library.
					lib.Lmin = Integer.parseInt(match.group(1));
					lib.Lmax = Integer.parseInt(match.group(2));
					System.out.println("  Library \""+lib.name+"\" sets Lmin="+lib.Lmin +
							" and Lmax="+lib.Lmax + ".");
					continue;
				}

				/// STD CUTOFF_LMINLMAX ///
				match = std.matcher(pattern);
				if(match.matches()) {
					int standarddev = Integer.parseInt(match.group(1));
					getLminLmaxSTD(lib,standarddev);
					checkLmin(lib);
					System.out.println("  Library \""+lib.name+"\" has Lmin="+lib.Lmin +
							" and Lmax="+lib.Lmax + " using standard deviation method in first " + lib.counter + " reads.");
					continue;
				}

				/// PCT CUTOFF_LMINLMAX ///
				match = pct.matcher(pattern);
				if(match.matches()) {
					int percent = Integer.parseInt(match.group(1));
					getLminLmaxPCT(lib,percent);
					checkLmin(lib);
					System.out.println("  Library \""+lib.name+"\" has Lmin="+lib.Lmin +
							" and Lmax="+lib.Lmax + " using percentile method in first " + lib.counter + " reads.");
					continue;
				}
			}
			scan.close();
		}
	}

	/**
	 * Lmin cannot be smaller than twice the read length.  Check this.
	 * @param lib - library to check.
	 */
	public void checkLmin(Library lib){

		// If Lmin/Lmax were never set, then no paired reads were found.
		if(lib.Lmin == Integer.MIN_VALUE ||
				lib.Lmax == Integer.MIN_VALUE ||
				lib.minRead_L == Integer.MAX_VALUE) {
			if(LIBRARY_SEPARATED.equals("sep"))
				System.out.println("  WARNING: No paired reads found for library \"" + lib.name +"\"");
			else {
				System.out.println("  WARNING: No paired reads found.");
			}
			return;
		}

		if(lib.Lmin < 0 ) {
			System.out.println("  WARNING: Lmin of " + lib.Lmin +
					" is negative for library \"" + lib.name + "\".  Resetting to 0.");
			lib.Lmin = 0;
		}

		// If Lmin > Lmax, then die with an error.
		if(lib.Lmin >= lib.Lmax) {
			System.out.println("ERROR: Lmin >= Lmax for library \"" + lib.name + "\" after ensuring that Lmin >= 2*minreadlen.");
			System.exit(-1);
		}
	}

	/**
	 * Check Lmin/Lmax by standard deviation.
	 * @param lib - library to use
	 * @param stddev - standard deviation parameter passed by the user.
	 */
	public void getLminLmaxSTD(Library lib,int stddev) {
		//double mean = (double) (lib.total_L)/lib.counter;
		lib.Lmin = (int) (lib.mean-(lib.std*stddev));
		lib.Lmax = (int) (lib.mean+(lib.std*stddev));
	}

	/**
	 * Check Lmin/Lmax by percent
	 * @param lib - Library to use
	 * @param percent - percent parameters passed by the user.
	 */
	public void getLminLmaxPCT(Library lib, int percent) {
		if(percent < 50){
			System.out.println("WARNING: PCT=" + percent + "% would result in an Lmin LARGER than Lmax!!  Proceeding under assumption user meant to input PCT="+ (100-percent) + "% instead!");
			percent=100-percent;
		}
		long Lmin_q = Math.round(((lib.total_C) * ((float)(100-percent)/100)) + 0.5);
		long Lmax_q = Math.round(((lib.total_C) * ((float)percent/100)) + 0.5 );
		//System.out.println("Lmin_q = " + Lmin_q + " and Lmax_q = " + Lmax_q);

		int counting_q = 0;
		int last_value = 0;
		Iterator<Map.Entry<Integer, Integer>> libIter = lib.lengthHist.entrySet().iterator();
		while(libIter.hasNext()){
			Map.Entry<Integer,Integer> libtmp = libIter.next();
			if(counting_q <= Lmin_q && counting_q+libtmp.getValue().intValue() >= Lmin_q){
				lib.Lmin=libtmp.getKey().intValue();
				//System.out.println("lib.Lmin = " + lib.Lmin + " and lib.Lmax = " + lib.Lmax);
			}
			if(counting_q <= Lmax_q && counting_q+libtmp.getValue().intValue() >= Lmax_q){
				lib.Lmax=libtmp.getKey().intValue();
				//System.out.println("lib.Lmin = " + lib.Lmin + " and lib.Lmax = " + lib.Lmax);
			}
			counting_q += libtmp.getValue().intValue();
			//System.out.println(libtmp.getKey() + " --> " + libtmp.getValue() + " ---" + counting_q);
			last_value = libtmp.getKey().intValue();
		}

		if(lib.Lmax == Integer.MIN_VALUE){
			lib.Lmax = last_value;
		}
	}

	public void getMeanAndSTDForNovoalign(Library lib) {
		lib.mean = (int)((double)lib.total_L/lib.total_C+0.5);
		double sd_up = 0;

		//System.out.println(mean);
		Iterator<Map.Entry<Integer, Integer>> libIter = lib.lengthHist.entrySet().iterator();
		while(libIter.hasNext()){
			Map.Entry<Integer,Integer> libtmp = libIter.next();
			sd_up += libtmp.getValue().intValue()*Math.pow(libtmp.getKey().intValue()-lib.mean, 2);
		}
		//double sd = Math.sqrt(sd_up/lib.counter);
		lib.std = (int)(Math.sqrt(sd_up/lib.total_C)+0.5);
	}

	/**
	 * Check which variant types are available in the first USE_NUMBER_READS reads.
	 * @param lib - Library to use.
	 */
	public void checkVariantTypes(Library lib) {
		System.out.println("Checking discordant types for library " + lib.name+"...");
		VariantType type;
		String libname = lib.name;
		boolean inv = false;
		boolean del = false;
		boolean div = false;
		boolean tr = false;
		boolean ins = false;

		for(GASVPair pobj : lib.firstNreads){
			type = getVariantType(pobj,lib);
			if(type == VariantType.INV)
				inv = true;
			else if(type == VariantType.DEL)
				del = true;
			else if(type == VariantType.INS)
				ins = true;
			else if(type == VariantType.DIV)
				div = true;
			else if(type == VariantType.TRANS)
				tr = true;
			else if(type != VariantType.CONC) {
				System.out.println("ERROR! Variant Type " + type + " not recognized.");
				System.exit(-1);
			}

			if(inv && del && div && tr && ins)
				break;
		}

		if(!inv)
			System.out.println("  WARNING: Library \""+libname+"\" has no inversions in first " + lib.counter + " reads.");
		if(!del)
			System.out.println("  WARNING: Library \""+libname+"\" has no deletions in first " + lib.counter + " reads.");
		if(!div)
			System.out.println("  WARNING: Library \""+libname+"\" has no divergents in first " + lib.counter + " reads.");
		if(!tr)
			System.out.println("  WARNING: Library \""+libname+"\" has no translocations in first " + lib.counter + " reads.");
		if(!ins)
			System.out.println("  WARNING: Library \""+libname+"\" has no insertions in first " + lib.counter + " reads.");
		System.out.println();

	}

	/**
	 * Reads the BAM file.  For each library listed, calculate stats along the way.
	 * @throws IOException
	 */
	public void readBAMfile() {
		System.out.println("Reading BAM file.  Once "+ USE_NUMBER_READS + " lines have been acquired for each library, stats will be calculated.");
		String id,libname = null;
		Library lib;
		VariantType type;
		int recordCounter = 0; // for printing lines
		ArrayList<String> tmpFilenames = new ArrayList<String>();;

		// set validation stringency for SAMFileReader.
		SAMFileReader.setDefaultValidationStringency(STRINGENCY);

		// Open SAM file, either from the file or from the URL.
		SAMFileReader inputSam = null;
		if(!IS_URL)
			inputSam = new SAMFileReader(new File(BAMFILE));
		else {
			try {
				inputSam = new SAMFileReader(new URL(BAMFILE), null,false);
			} catch (MalformedURLException e) {
				System.out.println("ERROR: URL " + BAMFILE + " is malformed.");
				System.exit(-1);
			}
		}

		// initialize BAM file for lowquality alignments.
		if(WRITE_LOWQ)
			try {
				LOWQ_BAM_FILE = new BufferedWriter(new FileWriter(new File(OUTPUT_PREFIX+"_lowqual.sam")));
				String header = inputSam.getFileHeader().getTextHeader();
				LOWQ_BAM_FILE.write(header);
			} catch (IOException e) {
				System.out.println("ERROR creating low quality SAM file.");
				System.exit(-1);
			}
			else
				LOWQ_BAM_FILE = null;


		// Initialize Library objects
		for(int i=0; i<LIBRARY_NAMES.size();i++) {
			libname = LIBRARY_NAMES.get(i);
			LIBRARY_INFO.put(libname,new Library(libname));
		}

		// Iterate through each Record and store relevant information.
		// Surround with try/catch to catch any IO Exception (Anna & Layla 8/21/2012)
		try {
			for (SAMRecord samRecord : inputSam) {

				// print information every 500,000 lines.
				recordCounter++;
				if(recordCounter % 500000 == 0 || recordCounter == 1) {
					System.out.println("  processing BAM line " + recordCounter + ": ");
					for(int i=0;i<LIBRARY_NAMES.size();i++) {
						int numinmem = 0;

						String tmp = "";
						for(int v=0;v<VARIANTS.length;v++) {
							numinmem+=LIBRARY_INFO.get(LIBRARY_NAMES.get(i)).rowsForVariant.get(VARIANTS[v]).size();
							tmp+=VARIANTS[v]+":"+LIBRARY_INFO.get(LIBRARY_NAMES.get(i)).rowsForVariant.get(VARIANTS[v]).size()+" ";
						}

						if(LIBRARY_INFO.get(LIBRARY_NAMES.get(i)).firstNreads != null)
							System.out.println("\t\""+LIBRARY_NAMES.get(i)+"\" has " + numinmem + " lines in memory and " +
									LIBRARY_INFO.get(LIBRARY_NAMES.get(i)).firstNreads.size() + " records in the first N reads.");
						else
							System.out.println("\t\""+LIBRARY_NAMES.get(i)+"\" has " + numinmem + " lines in memory ("+tmp+")");
					}
				}

				// get Record's ID
				if(samRecord.getReadGroup() != null)
					id = samRecord.getReadGroup().getId();
				else // no reading group information
					id = "all";

				// From ID, determine library name.
				if (LIBRARY_IDS.containsKey(id)){
					libname = LIBRARY_IDS.get(id);
				} else{ // Reading group id does not exist in the header.
					System.out.println("ERROR: Reading group id at read " + samRecord.getReadName()+ "does not exist in the header!!");
					inputSam.close();
					System.exit(-1);
				}

				// get Library object.
				lib = LIBRARY_INFO.get(libname);

				// If we've already computed stats, just parse the record.
				// Otherwise, keep track of insert length and store in memory.
				parseSAMRecord(samRecord,lib);

				// If we haven't computed stats yet AND we have enough reads, compute stats!
				if(!lib.computedStats && lib.counter >= USE_NUMBER_READS) {

					// check pairInfo
					checkPairingInfo(lib);

					// get LminLmax
					getLminLmax(lib);

					// check variantTypes
					checkVariantTypes(lib);

					// First N reads are in memory. We must go through them and store them
					// in their respective types.  Afterwards, sets pairs to null for memory
					// efficiency.
					for(GASVPair p : lib.firstNreads){
						parseESPfromGASVPair(p, lib);
						lib.firstNreads = null;
					}
					lib.computedStats = true;

				} // END haven't computed stats yet && lib.counter >= USE_NUMBER_READS
			} // end for (SAMRecord samRecord : inputSam)
		} catch (RuntimeIOException e) {
			System.out.println("WARNING: RuntimeIOException caught when iterating through records - closing BAM file and processing output. This might be due to a truncated file.");
			System.err.println("WARNING: RuntimeIOException caught when iterating through records - closing BAM file and processing output. This might be due to a truncated file.");
		}
		// Layla 8/22/2012 - Add additional catches for truncated files.
		catch (FileTruncatedException e) {
			System.out.println("WARNING: FileTruncatedException caught when iterating through records - closing BAM file and processing output.");
			System.err.println("WARNING: FileTruncatedException caught when iterating through records - closing BAM file and processing output.");
		}

		catch (RuntimeEOFException e) {
			System.out.println("WARNING: RuntimeEOFException caught when iterating through records - closing BAM file and processing output.");
			System.err.println("WARNING: RuntimeEOFException caught when iterating through records - closing BAM file and processing output.");

		}
		inputSam.close();
		if(WRITE_LOWQ) {
			try {
				LOWQ_BAM_FILE.close();
			} catch (IOException e) {
				System.out.println("WARNING: Cannot close low-quality sam file.");
			}
		}

		System.out.println("Done reading BAM file.\n");

		// finish analysis with remaining records in libraries.
		// FOR EACH LIBRARY:
		// (1) If stats haven't been computed, compute.
		//   FOR EACH VARIANT:
		// (2) Write last temporary file
		// (3) merge temporary files into one sorted file.
		for(int i=0;i<LIBRARY_NAMES.size();i++) {
			libname = LIBRARY_NAMES.get(i);
			lib = LIBRARY_INFO.get(libname);

			// (1) If stats haven't been computed, compute.
			// After reading the entire BAM file, some libraries
			// might have fewer than USE_NUMBER_READS pairs.
			// Calculate stats now if this is the case.
			if (!lib.computedStats){
				System.out.println("  WARNING: There are fewer than " +
						USE_NUMBER_READS + " for library \""+libname+"\". Computing statistics now...");

				// check pairInfo
				checkPairingInfo(lib);

				// get LminLmax
				getLminLmax(lib);

				// check variantTypes
				checkVariantTypes(lib);

				// First N reads are in memory. We must go through them and store them
				// in their respective types.  Afterwards, sets pairs to null for memory
				// efficiency.
				for(GASVPair p : lib.firstNreads) {
					parseESPfromGASVPair(p,lib);
					lib.firstNreads = null;
				}
				lib.computedStats = true;

			} // END haven't computed stats yet

			// IF Lmin and Lmax are the initial parameters, then there are NO reads for this
			// library. We don't need to write variant files here.
			if(lib.Lmin == Integer.MIN_VALUE || lib.Lmax == Integer.MIN_VALUE)
				continue;

			// Go through each Variant Type.
			for(int j=0;j<VARIANTS.length;j++) {
				type = VARIANTS[j];

				// skip variant types that aren't flagged as "to-write"
				if(type == VariantType.CONC && !WRITE_CONCORDANT)
					continue;

				// (2) Write last temporary files for variants
				if(lib.rowsForVariant.get(type).size() > 0)
					sortAndWriteTempFile(lib,type);

				// If it's concordant and we aren't sorting, then no need to merge.
				if(type == VariantType.CONC && NOSORT)
					continue;

				// (3) For each variant, merge temporary files into one sorted file.
				// Files are deleted in the merge() function.

				// First, get a list of the temporary file names.
				tmpFilenames.clear();
				for(int k=1;k<=lib.numTmpFilesForVariant.get(type);k++)
					tmpFilenames.add(getTmpFileName(libname,type,k));

				System.out.println("  Library \""+libname+"\" type "+type+": merging "+
						tmpFilenames.size() + " temporary files");

				// Now, merge files. Concordants are written differently than all
				// other ESP files.
				if(type == VariantType.CONC) {
					concordantSorter.merge(tmpFilenames,getFinalFileName(libname,type));
				} else
					discordantSorter.merge(tmpFilenames,getFinalFileName(libname,type));

			} // END for each variant

		} // END for each library

		// If we're not sorting concordants, then close all concordant files.
		if(NOSORT) {
			Iterator<String> iter = CONCORDANT_FILES.keySet().iterator();
			try {
				while(iter.hasNext()) {
					CONCORDANT_FILES.get(iter.next()).close();
				}
			} catch (IOException e) {
				System.out.println("WARNING: Cannot close unsorted concordant files.");
			}
		}

		System.out.println();
	}

	/**
	 * Calculates the insert length if the pair is high qual and convergent.
	 * Also, finds min read length for library.
	 * @param lib - Library
	 * @param pobj - GASVPair object of this record
	 * @param s - SAMRecord
	 */
	public void calculateInsertLength(Library lib,GASVPair pobj,SAMRecord s) {
		if(lib.counter < USE_NUMBER_READS){
			lib.counter+=2;
			if(pobj.equalChromosome()) { // equal chromosome: inversion, concordant, deletion, divergent
				if(pobj.equalConvPair()) { // only consider convergent pairs
					int insertL = pobj.getInsertSize();
					if (s.getReadLength() < lib.minRead_L){
						lib.minRead_L = s.getReadLength();
					}
					if(PROPER_LENGTH == 0){ // TODO: Should this be insertL == 0?
						lib.total_L += insertL;
						lib.total_RL += s.getReadLength();
						lib.total_C += 1;
						if (lib.lengthHist.containsKey(insertL)){
							int tmp = lib.lengthHist.get(insertL).intValue();
							lib.lengthHist.put(insertL, new Integer(tmp+1));
						} else{
							lib.lengthHist.put(insertL, 1);
						}
					} else if(insertL <= PROPER_LENGTH){
						lib.total_L += insertL;
						lib.total_C += 1;
						lib.total_RL += s.getReadLength();
						if (lib.lengthHist.containsKey(insertL)){
							int tmp = lib.lengthHist.get(insertL).intValue();
							lib.lengthHist.put(insertL, new Integer(tmp+1));
						} else{
							lib.lengthHist.put(insertL, 1);
						}
					}
				}
			}
		}
	}

	/**
	 * Add GASVPair object to the appropriate place in Library's data structure.
	 * @param pobj - GASVPair object
	 * @param lib - library to add ESP to.
	 */
	private void parseESPfromGASVPair(GASVPair pobj, Library lib){
		VariantType type = getVariantType(pobj,lib);

		if(type == VariantType.CONC && !WRITE_CONCORDANT)
			return;

		if(type == VariantType.CONC && GASVPRO_OUTPUT){ // GASVPro calculating avg length (insert, read) for GASVPro parameters file
			int conc_chrom = pobj.getChromosome().intValue();
			if (lib.numConcord.containsKey(conc_chrom)){
				int tmp = lib.numConcord.get(conc_chrom).intValue();
				lib.numConcord.put(conc_chrom, new Integer(tmp+1));
			} else{
				lib.numConcord.put(conc_chrom, 1);
			}
		}

		// Add line to variant list for this library.
		lib.addLine(type,pobj.createOutput(type));

		// Check to see if we should sort and write tmp file here.
		// Now, sort and write ALL tmp files (all libraries, all types)
		if(lib.rowsForVariant.get(type).size() >= NUM_LINES_FOR_EXTERNAL_SORT) {
			String libname;
			for(int i = 0;i<LIBRARY_NAMES.size();i++) {
				libname = LIBRARY_NAMES.get(i);
				lib = LIBRARY_INFO.get(libname);

				for(int j=0;j<VARIANTS.length;j++) {
					if(VARIANTS[j] == VariantType.CONC && !WRITE_CONCORDANT)
						continue;

					// if there are fewer than 1/10th of the number of reads, don't do this yet.
					if(lib.rowsForVariant.get(VARIANTS[j]).size() < USE_NUMBER_READS/10)
						continue;

					sortAndWriteTempFile(lib,VARIANTS[j]);

				} // END for each variant
			} // END for each Library
		} // END if(lib.rowsForVariant.get(type).size() >= NUM_LINES_FOR_EXTERNAL_SORT)

	}


	/**
	 * Add GASVPair object to the LOWQUAL list in Library's data structure.
	 * @param pobj - GASVPair object
	 * @param lib - library to add ESP to.
	 */
	/*
	private void parseLowQualESPfromGASVPair(GASVPair pobj, Library lib){
		VariantType type = VariantType.LOW;

		// Add line to variant list for this library.
		lib.addLine(type,pobj.createOutput(type));

		// Check to see if we should sort and write tmp file here.
		// Now, sort and write ALL tmp files (all libraries, all types)
		if(lib.rowsForVariant.get(type).size() >= NUM_LINES_FOR_EXTERNAL_SORT) {
			String libname;
			for(int i = 0;i<LIBRARY_NAMES.size();i++) {
				libname = LIBRARY_NAMES.get(i);
				lib = LIBRARY_INFO.get(libname);

				for(int j=0;j<VARIANTS.length;j++) {
					if(VARIANTS[j] == VariantType.CONC && !WRITE_CONCORDANT)
						continue;
					if(VARIANTS[j] == VariantType.LOW && !WRITE_LOWQ)
						continue;

					// if there are fewer than 1/10th of the number of reads, don't do this yet.
					if(lib.rowsForVariant.get(VARIANTS[j]).size() < USE_NUMBER_READS/10)
						continue;

					sortAndWriteTempFile(lib,VARIANTS[j]);

				} // END for each variant
			} // END for each Library
		} // END if(lib.rowsForVariant.get(type).size() >= NUM_LINES_FOR_EXTERNAL_SORT)

	}
	 */

	/**
	 * Parse read and put the SAM record in the appropriate place.
	 * --> if it's the first of the pair, keep track of it.
	 * --> if it's the second ofthe pair, make the ESP and store it in
	 * the library's list of ESPs in memory.
	 * Finally, write list of ESPs to temp file if they exceed a buffer size.
	 *
	 * @param s - SAM record to parse
	 * @param lib - Library to use.
	 */
	private void parseSAMRecord(SAMRecord s, Library lib){

                // If this record is duplicated (according to flag) OR is NOT paired, then
                // return immediately.
                // Rolf Hilker added first condition (!isForbiddenMapping(s)) to generate
                // useful results in conjunction with ReadXplorer
		if(GASVUtils.isForbiddenMapping(s) || s.getDuplicateReadFlag() || !s.getReadPairedFlag())
			return;

		String readname = s.getReadName();

		// If this record has high mapping quality, then store it.
		// If this record has low mapping quality AND the write-lowq flag is set, store it too.
		if(s.getMappingQuality() >= MAPPING_QUALITY){

			// Have we seen it's mate? First check HIGHQ, then check LOWQ
			if(HIGHQ_INDICATOR.containsKey(readname)) {
				// remove PAIR counting of this read
				HIGHQ_INDICATOR.remove(readname);

				// make a GASVPair object
				GASVPair pobj = null;
				try {
					pobj = new GASVPair(s, PLATFORM);
					if(pobj.badChrParse) // chromosome not recognized. skip.
						return;
				} catch (SAMFormatException e) {
					BAD_RECORD_COUNTER++;
					System.err.println("**SAMFormatException** "+e.getMessage());
					return;
				}

				// If we've already computed stats, then we just need to
				// parse the ESP.  If not, then keep in mem.
				if(lib.computedStats) {
					// parse ESP
					parseESPfromGASVPair(pobj,lib);
				} else { // keep this read in memory
					// ADD this GASVPair object to the list of first N reads
					lib.firstNreads.add(pobj);

					// calculate insert length
					calculateInsertLength(lib,pobj,s);

					// if we haven't found a mate, check pair information.
					if (!lib.mateFound)
						lib.isRecordPaired(s);
				}

			} else if (WRITE_LOWQ && LOWQ_INDICATOR.containsKey(readname)) {
				//Current read has high quality, but the other read was: (1) already seen; (2) has low-quality.
				// NOTE: if write_lowq is set, then LOWQ_ind is populated.

				// remove PAIR counting of this read
				String other = LOWQ_INDICATOR.remove(readname);

				writeLowQualityRecordPair(other,s.getSAMString());

			} else { // haven't seen it, put it in HIGHQ
				HIGHQ_INDICATOR.put(readname,s.getSAMString());
			} // END high quality read conditional

		} else { // (s.getMappingQuality() < MAPPING_QUALITY)

			// Have we seen it's mate? First check HIGHQ, then check LOWQ
			if(HIGHQ_INDICATOR.containsKey(readname)) {
				// remove PAIR counting of this read
				String other = HIGHQ_INDICATOR.remove(readname);

				// IF write_lowq, then write low qual.
				if(WRITE_LOWQ) {
					writeLowQualityRecordPair(other,s.getSAMString());
				}

			} else if (WRITE_LOWQ && LOWQ_INDICATOR.containsKey(readname)) {
				// NOTE: if write_lowq is set, then LOWQ_ind is populated.

				// remove PAIR counting of this read
				String other = LOWQ_INDICATOR.remove(readname);
				writeLowQualityRecordPair(other,s.getSAMString());

			} else if (WRITE_LOWQ) { // we haven't seen it - add to LOWQ
				LOWQ_INDICATOR.put(readname,s.getSAMString());
			} // END low quality read conditional
		} // END mapping quality check
	}

	public void writeLowQualityRecordPair(String record1,String record2)  {
		try {
			LOWQ_BAM_FILE.write(record1+record2);
		} catch (IOException e) {
			System.out.println("ERROR: Cannot write low-quality sam file.");
			System.exit(-1);
		}
	}

	/**
	 * Gets the variant type of a GASVPair depending on chr, orientation, and distance.
	 * Note that LOW Variant Type is special and never returned here.
	 *
	 * @param pobj - GASVPair object
	 * @param lib - this Library
	 * @return - Variant Type (INV,CONC,INS,DEL,DIV,TRANS)
	 */
	public VariantType getVariantType(GASVPair pobj,Library lib) {
		// equal chromosome: inversion, concordant, deletion, divergent, insertion
		if(pobj.equalChromosome()){

			if(pobj.equalStrand())  // inversion
				return VariantType.INV;

			if(pobj.equalConvPair()){ // concordant or deletion or insertion
				int L = pobj.getInsertSize();

				if(L <= lib.Lmax && L >= lib.Lmin)  // concordant
					return VariantType.CONC;
				if (L > lib.Lmax)  // deletion
					return VariantType.DEL;

				// if neither concordant or deletion, then it's an insertion
				return VariantType.INS;
			}

			// if not convergent, then it's divergent
			return VariantType.DIV;

		} // END equalChromosome

		// If on different chromosome, then translocation
		return VariantType.TRANS;
	}

	/**
	 * Sorts the ESPs available for a library/type pair and writes the result
	 * to a temporary file.
	 * @param lib - library
	 * @param type - Variant Type to write
	 */
	public void sortAndWriteTempFile(Library lib, VariantType type) {

		System.out.println("  Library \""+lib.name+"\" type "+type+": writing "+lib.rowsForVariant.get(type).size()+" lines in temp file");

		// Increment temporary file number.
		int curnum = lib.numTmpFilesForVariant.get(type);
		curnum++;
		lib.numTmpFilesForVariant.put(type,curnum);

		// Sort and write this set of reads to a temporary file.
		if(type == VariantType.CONC){ // Handle Concordants.
			if(NOSORT) { // ADDED by Anna 8/20
				String filename = getFinalFileName(lib.name,type);
				try {
					if(!CONCORDANT_FILES.containsKey(filename))
						CONCORDANT_FILES.put(filename,new BufferedWriter(new FileWriter(new File(filename))));
					BufferedWriter writer = CONCORDANT_FILES.get(filename);
					ArrayList<String> towrite = lib.rowsForVariant.get(type);
					for(int i=0;i<towrite.size();i++)
						writer.write(towrite.get(i)+"\n");
				} catch (IOException e) {
					System.out.println("ERROR WHILE OPENING AND WRITING TO " + filename);
					System.exit(-1);
				}
			} else {
				try {
					concordantSorter.sort(
							lib.rowsForVariant.get(type),
							getTmpFileName(lib.name,type,curnum));
				} catch (IOException e) {
					System.out.println("ERROR WHILE WRITING CONCORDANT TMP FILE " + getTmpFileName(lib.name,type,curnum) + ". Most likely, the tmp file cannot be created - check the output directory location.");
					System.exit(-1);
				}
			}
		} else { // Handle Discordants
			try {
				discordantSorter.sort(
						lib.rowsForVariant.get(type),
						getTmpFileName(lib.name,type,curnum));
			} catch (IOException e) {
				System.out.println("ERROR WHILE WRITING DISCORDANT TMP FILE " + getTmpFileName(lib.name,type,curnum) + ". Most likely, the tmp file cannot be created - check the output directory location.");
				System.exit(-1);
			}
		}

		// finally, clear buffer
		lib.clearVariantBuffer(type);
		System.gc();
	}

	/**
	 * Gets the temporary file name.
	 * @param lib - library
	 * @param type - variant type
	 * @param curnum - current temporary file number
	 * @return String of the file name.
	 */
	public String getTmpFileName(String lib,VariantType type, int curnum) {
		return OUTPUT_PREFIX+"_"+lib+"."+type+"."+curnum+".tmpFile";
	}

	/**
	 * Gets the final file name.
	 * @param lib - library
	 * @param type = variant type
	 * @return String of the file name.
	 */
	public String getFinalFileName(String lib,VariantType type) {
		if(type == VariantType.CONC)
			return OUTPUT_PREFIX+"_"+lib+".concordant";
		if(type == VariantType.DEL)
			return OUTPUT_PREFIX+"_"+lib+".deletion";
		if(type == VariantType.INS)
			return OUTPUT_PREFIX+"_"+lib+".insertion";
		if(type == VariantType.INV)
			return OUTPUT_PREFIX+"_"+lib+".inversion";
		if(type == VariantType.DIV)
			return OUTPUT_PREFIX+"_"+lib+".divergent";
		if(type == VariantType.TRANS)
			return OUTPUT_PREFIX+"_"+lib+".translocation";

		System.out.println("ERROR: type "+type+" is not one of the recognized options.");
		System.exit(-1);
		return "";
	}

	public void mergeConcordantFiles() {
		String concordantFile = OUTPUT_PREFIX+"_all.concordant"; // from writeGASVPROInputFile()

		// MERGE CONCORDANT FILES (Anna - new June 30,2012)
		// Only when GASVPRO_FLAG is set AND LIBRARY_SEPARATED is not 'all'
		// (1) Get list of all concordant files.
		ArrayList<String> concordantfiles = new ArrayList<String>();
		String libname;
		for(int i=0;i<LIBRARY_NAMES.size();i++) {
			libname = LIBRARY_NAMES.get(i);
			String fname = getFinalFileName(libname,VariantType.CONC);
			if(new File(fname).exists())
				concordantfiles.add(fname);
		}
		// if there are no concordant files, print a warning.
		if(concordantfiles.size()==0)
			System.out.println("\nWARNING: " + concordantFile + " was not created because there are no concordant files for the libraries.\n");

		// if this list has only one file, we're done.
		if(concordantfiles.size()==1) {
			System.out.println("\nMoving single concordant file to one with the correct name.\n");
			File old = new File(concordantfiles.get(0));
			boolean success = old.renameTo(new File(concordantFile));
			if(!success)
				System.out.println("WARNING: " + concordantFile + " was not created by moving " + old.getName());
		}

		// Otherwise, merge the multiple concordant files.
		if(concordantfiles.size() > 1) {
			// (2) Merge all concordant files using Sorter.merge() function. Write to OUTPUT_PREFIX+"_all.concordant
			System.out.println("\nMerging " +concordantfiles.size()+" concordant files for all libraries into a single one.  This might take a while...\n");
			concordantSorter.merge(concordantfiles,concordantFile);
		}
	}

	/**
	 * Writes the .info file, which lists all the Lmin/Lmax values for each library.
	 */
	public void writeInfoFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_PREFIX+".info"));
			writer.write("LibraryName\tLmin\tLmax\tMean\tStdDev\n");
			String libname;
			Library lib;
			for(int i=0;i<LIBRARY_NAMES.size();i++) {
				libname = LIBRARY_NAMES.get(i);
				lib = LIBRARY_INFO.get(libname);

				// skip if there are no reads.
				if(lib.Lmin == Integer.MIN_VALUE || lib.Lmax == Integer.MIN_VALUE)
					continue;

				writer.write(libname + "\t" + LIBRARY_INFO.get(libname).Lmin+"\t"+
						LIBRARY_INFO.get(libname).Lmax+"\t"+
						LIBRARY_INFO.get(libname).mean+"\t"+
						LIBRARY_INFO.get(libname).std+"\n");
				//writerpro.write(libname+"\t"+LIBRARY_INFO.get(libname).getAverageInsertLength()+"\t"+LIBRARY_INFO.get(libname).getAverageReadLength()+"\t"+LIBRARY_INFO.get(libname).getConcordDist()+"\n");
			}

			writer.close();
		} catch (Exception e) {
			System.out.println("Error writing .info file. Continuing.");
		}
	}

	/**
	 * Writes the .gasv.in file, which includes all discordant ESP files except for insertions.
	 */
	public void writeGASVInputFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_PREFIX+".gasv.in"));
			String libname;
			Library lib;
			VariantType type;

			for(int i=0;i<LIBRARY_NAMES.size();i++) {
				libname = LIBRARY_NAMES.get(i);
				lib = LIBRARY_INFO.get(libname);
				// skip if there are no reads.
				if(lib.Lmin == Integer.MIN_VALUE || lib.Lmax == Integer.MIN_VALUE)
					continue;
				for(int j=0;j<VARIANTS.length;j++) {
					type = VARIANTS[j];
					if(type == VariantType.CONC ||
							type == VariantType.INS)
						continue;

					writer.write(getFinalFileName(libname,type)+"\tPR\t"+
							lib.Lmin+"\t"+lib.Lmax+"\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error writing .gasvInput file. Continuing.");
		}
	}

	/**
	 * Writes the .gasvpro.in file. // GASVPro - Output GASVPro parameters file
	 */
	public void writeGASVPROInputFile() {
		try {
			if (!GASVPRO_OUTPUT)
				return;
			BufferedWriter writerpro = new BufferedWriter(new FileWriter(OUTPUT_PREFIX+".gasvpro.in"));
			String libname;
			Library lib;

			int totalNC = 0;
			float totalGAvgRead = 0;
			float totalGAvgInsert = 0;
			for(int i=0;i<LIBRARY_NAMES.size();i++) {
				libname = LIBRARY_NAMES.get(i);
				lib = LIBRARY_INFO.get(libname);
				if(lib.Lmin == Integer.MIN_VALUE || lib.Lmax == Integer.MIN_VALUE)
					continue;
				totalGAvgRead += lib.getGlobalAvgReadLength();
				totalGAvgInsert += lib.getGlobalAvgInsertLength();
				totalNC += lib.getnumConcordGenome();
			}
			writerpro.write("# Generated by BamToGASV\n");
			writerpro.write("ConcordantFile: "+OUTPUT_PREFIX+"_all.concordant\n");
			writerpro.write("Lavg: "+totalGAvgInsert/totalNC+"\n");
			writerpro.write("ReadLen: "+totalGAvgRead/totalNC+"\n");
			writerpro.write("Lambda: "+totalGAvgInsert/GL+"\n");
			writerpro.close();
		} catch (Exception e) {
			System.out.println("Error writing .gasvproInput file. Continuing.");
		}
	}

	/**
	 * Prints the non-default chromosome names that were skipped.
	 * Also prints libraries that were skipped.
	 */
	public void printSkipped() {
		if(CHROMOSOME_NAMING_FILE == null && BAMToGASV.NON_DEFAULT_REFS.size() > 0) {
			System.out.println("The following non-default chromosome names were skipped:");
			Iterator<String> iter = BAMToGASV.NON_DEFAULT_REFS.keySet().iterator();
			while(iter.hasNext())
				System.out.println("  "+iter.next());
		} else if (CHROMOSOME_NAMING_FILE != null && BAMToGASV.NON_DEFAULT_REFS.size() > 0) {
			System.out.println("The following non-default chromosome names were NOT in "+CHROMOSOME_NAMING_FILE+":");
			Iterator<String> iter = BAMToGASV.NON_DEFAULT_REFS.keySet().iterator();
			while(iter.hasNext())
				System.out.println("  "+iter.next());
		}
		System.out.println();

		String libname;
		Library lib;
		for(int i=0;i<LIBRARY_NAMES.size();i++) {
			libname = LIBRARY_NAMES.get(i);
			lib = LIBRARY_INFO.get(libname);

			// skip if there are no reads.
			if(lib.Lmin == Integer.MIN_VALUE || lib.Lmax == Integer.MIN_VALUE)
				System.out.println("\nWARNING: Library \""+libname+
				"\" had zero reads present. Not including this library in output files.\n");
		}
	}

	/**
	 * Prints the output files that were written.
	 */
	public void printOutputFiles() {
		System.out.println("Output files are:");
		System.out.println("  "+OUTPUT_PREFIX+".info"); // always written
		System.out.println("  "+OUTPUT_PREFIX+".gasv.in"); // always written
		if (GASVPRO_OUTPUT){
			System.out.println("  "+OUTPUT_PREFIX+".gasvpro.in"); // GASVPro
			System.out.println("  "+OUTPUT_PREFIX+"_all.concordant");// GASVPro
		}
		if(WRITE_LOWQ) {
			System.out.println("  "+OUTPUT_PREFIX+"_lowqual.sam");
		}


		for(int i=0;i<LIBRARY_NAMES.size();i++) {
			for(int j=0;j<VARIANTS.length;j++) {

				if(VARIANTS[j] == VariantType.CONC && !WRITE_CONCORDANT)
					continue;
				if(VARIANTS[j] == VariantType.CONC && GASVPRO_OUTPUT) // GASVPro
					continue;

				System.out.println("  "+getFinalFileName(LIBRARY_NAMES.get(i),VARIANTS[j]));

			}
		}
	}

}
