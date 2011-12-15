package de.cebitec.vamp.util;

/**
 * @author Rolf Hilker
 *
 * Contains non language specific global constants.
 */
public class Properties {

    public static final String SEL_GENETIC_CODE = "selectedGeneticCode";
    public static final String STANDARD = "Standard";
    public static final String STANDARD_CODE_INDEX = "1";
    public static final String GENETIC_CODE_INDEX = "geneticCodeIndex";
    public static final String CUSTOM_GENETIC_CODES = "customGeneticCode";

    public static final String VAMP_FILECHOOSER_DIRECTORY = "VampFileChooser.Directory";
    public static final String VAMP_DATABASE_DIRECTORY = "Vamp.Database.Directory";
    
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
    
}
