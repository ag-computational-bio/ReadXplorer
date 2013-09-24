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

    // ReadXplorer file chooser properties
    public static final String VAMP_FILECHOOSER_DIRECTORY = "VampFileChooser.Directory";
    public static final String VAMP_DATABASE_DIRECTORY = "Vamp.Database.Directory";
    
    //Properties to set different viewers active
    public static final String PROP_REF_AND_TRACK_VIEWERS = "RefAndTrackViewersActive";
    public static final String PROP_HISTOGRAM_VIEWER = "HistogramViewerActive";
    public static final String PROP_ALIGNMENT_VIEWER = "AlignmentViewerActive";
    public static final String PROP_SEQ_PAIR_VIEWER = "SeqPairViewerActive";
    
    public static final byte NONE = -1;
    public static final byte BOTH = 0;
    public static final byte SEQ_PAIRS = 1;
    public static final byte SINGLE_MAPPINGS = 2;
    
    /** Standard value, if all data is needed (0).*/
    public static final byte NORMAL = 0;
    /** Perfect coverage value (1).*/
    public static final byte PERFECT_COVERAGE = 1;
    /** Best match coverage value (2). */
    public static final byte BEST_MATCH_COVERAGE = 2;
    /** Common match coverage value (3). */
    public static final byte COMPLETE_COVERAGE = 3;
    /** Value for diffs (5). */
    public static final byte DIFFS = 5;
    /** Value for mappings with diffs (6). */
    public static final byte MAPPINGS_W_DIFFS = 6;
    /** Value for mappings without diffs (7). */
    public static final byte MAPPINGS_WO_DIFFS = 7;
    /** kasterm: Value for all reduced mappings*/
    public static final byte REDUCED_MAPPINGS  = 8;
    /** Value for obtaining read starts instead of coverage. */
    public static byte READ_STARTS = 9;
    
    /** Value for read start distribution = 5. */
    public static final byte READ_START_DISTRIBUTION = 5;
    /** Value for coverage increase in percent distribution = 6. */
    public static final byte COVERAGE_INC_PERCENT_DISTRIBUTION = 6;
    /** Value for read length distribution = 3. */
    public static final byte READ_LENGTH_DISTRIBUTION = 3;
    /** Value for seq pair size distribution = 4. */
    public static final byte READ_PAIR_SIZE_DISTRIBUTION = 4;    
    
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
    
    /** 'Yc' = Tag for read classification in one of the three vamp classes. */
    public static final String TAG_READ_CLASS = "Yc";
    /** 'Yt' = Tag for number of positions a sequence maps to in a reference. */
    public static final String TAG_MAP_COUNT = "Yt";
    /** 'Yi' = Tag for the sequence pair id. */
    public static final String TAG_SEQ_PAIR_ID = "Yi";
    /** 'Ys' = Tag for the sequence pair type. */
    public static final String TAG_SEQ_PAIR_TYPE = "Ys";
    
    //Supported sequence pair extensions.
    /** 1 = Supported extension of read 1. */
    public static final char EXT_A1 = '1';
    /** 2 = Supported extension of read 2. */
    public static final char EXT_A2 = '2';
    /** f = Supported extension of read 1. */
    public static final char EXT_B1 = 'f';
    /** r = Supported extension of read 2. */
    public static final char EXT_B2 = 'r';
    
    /** The CRAN Mirror used by Gnu R to load missing packages */
    public static final String CRAN_MIRROR = "CRAN_MIRROR";
    
    /* cache every track automatically when opened? */
    public static final String OBJECTCACHE_AUTOSTART = "OBJECTCACHE_AUTOSTART";
    public static final String OBJECTCACHE_ACTIVE = "OBJECTCACHE_ACTIVE";
    
    public static final String MAPPER_PATH = "MAPPER_PATH";
    
    /** Extension to use for bam index files (".bai"). */
    public static final String BAM_INDEX_EXT = ".bai";
    
    /** '-1' For reference features, which do not have a parent. */
    public static final String NO_PARENT_STRING = "-1";
    public static String MAPPER_PARAMS = "MAPPER_PARAMS";
}
