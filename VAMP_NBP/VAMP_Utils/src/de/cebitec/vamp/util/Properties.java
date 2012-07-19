package de.cebitec.vamp.util;

/**
 * Contains non language specific global constants.
 * 
 * @author Rolf Hilker
 */
public class Properties {

    // different adapter types for a project and or database
    public static final String ADAPTER_MYSQL = "mysql";
    public static final String ADAPTER_H2 = "h2";
    public static final String ADAPTER_DIRECT_ACCESS = "direct";
    
    // properties mainly for genetic codes
    public static final String SEL_GENETIC_CODE = "selectedGeneticCode";
    public static final String STANDARD = "Standard";
    public static final String STANDARD_CODE_INDEX = "1";
    public static final String GENETIC_CODE_INDEX = "geneticCodeIndex";
    public static final String CUSTOM_GENETIC_CODES = "customGeneticCode";

    // VAMP file chooser properties
    public static final String VAMP_FILECHOOSER_DIRECTORY = "VampFileChooser.Directory";
    public static final String VAMP_DATABASE_DIRECTORY = "Vamp.Database.Directory";
    
    // Sequence pair properties
    public static final byte TYPE_PERFECT_PAIR = 0;
    public static final byte TYPE_DIST_LARGE_PAIR = 1;
    public static final byte TYPE_DIST_SMALL_PAIR = 2;
    public static final byte TYPE_ORIENT_WRONG_PAIR = 3;
    public static final byte TYPE_OR_DIST_LARGE_PAIR = 4;
    public static final byte TYPE_OR_DIST_SMALL_PAIR = 5;
    public static final byte TYPE_UNPAIRED_PAIR = 6;
    
    //Properties to set different viewers active
    public static final String PROP_REF_AND_TRACK_VIEWERS = "RefAndTrackViewersActive";
    public static final String PROP_HISTOGRAM_VIEWER = "HistogramViewerActive";
    public static final String PROP_ALIGNMENT_VIEWER = "AlignmentViewerActive";
    public static final String PROP_SEQ_PAIR_VIEWER = "SeqPairViewerActive";
    
    public static final byte NONE = -1;
    public static final byte BOTH = 0;
    public static final byte SEQ_PAIRS = 1;
    public static final byte SINGLE_MAPPINGS = 2;
    
    /** Perfect coverage value (1).*/
    public static final byte PERFECT_COVERAGE = 1;
    /** Best match coverage value (2). */
    public static final byte BEST_MATCH_COVERAGE = 2;
    /** Common match coverage value (3). */
    public static final byte COMPLETE_COVERAGE = 3;
    public static final byte COUNT_DATA = 4;
    
    public static final byte COVERAGE_INCREASE_DISTRIBUTION = 1;
    public static final byte COVERAGE_INC_PERCENT_DISTRIBUTION = 2;
    
    
    /** Type value identifying an object as belonging to a "Start".*/
    public static final byte START = 1;
    /** Type value identifying an object as belonging to a "Stop".*/
    public static final byte STOP = 2;
    /** Type value identifying an object as belonging to a "pattern".*/
    public static final byte PATTERN = 3;
    /** Type value identifying an object as belonging to a "CDS" = coding sequence.*/
    public static final byte CDS = 4;
    /** Type value identifying an object as belonging to any of the other types.*/
    public static final byte ALL = 0;
    
    /** The CRAN Mirror used by Gnu R to load missing packages */
    public static final String CRAN_MIRROR = "CRAN_MIRROR";
    
}
