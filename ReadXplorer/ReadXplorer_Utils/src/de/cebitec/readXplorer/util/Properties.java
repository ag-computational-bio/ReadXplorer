/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.util;

/**
 * Contains non language specific global constants.
 * 
 * @author Rolf Hilker
 */
public class Properties {

    private Properties() {
    }
    
    // different adapter types for a project and or database
    public static final String ADAPTER_MYSQL = "mysql";
    public static final String ADAPTER_H2 = "h2";
    public static final String ADAPTER_DIRECT_ACCESS = "direct";
    
    // properties mainly for genetic codes
    public static final String SEL_GENETIC_CODE = "selectedGeneticCode";
    public static final String STANDARD = "Standard";
    /** 1 = Index of the standard genetic code. */
    public static final String STANDARD_CODE_INDEX = "1";
    public static final String GENETIC_CODE_INDEX = "geneticCodeIndex";
    public static final String CUSTOM_GENETIC_CODES = "customGeneticCode";

    // ReadXplorer file chooser properties
    public static final String READXPLORER_FILECHOOSER_DIRECTORY = "readXplorerFileChooser.Directory";
    public static final String ReadXplorer_DATABASE_DIRECTORY = "readXplorer.Database.Directory";
    
    //Properties to set different viewers active
    public static final String PROP_REF_AND_TRACK_VIEWERS = "RefAndTrackViewersActive";
    public static final String PROP_HISTOGRAM_VIEWER = "HistogramViewerActive";
    public static final String PROP_ALIGNMENT_VIEWER = "AlignmentViewerActive";
    public static final String PROP_SEQ_PAIR_VIEWER = "SeqPairViewerActive";
    
    public static final byte NONE = -1;
    public static final byte BOTH = 0;
    public static final byte READ_PAIRS = 1;
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
    /** Value for mappings sorted by id (6). */
    public static final byte MAPPINGS_DB_BY_ID = 6;
    /** Value for mappings without diffs (7). */
    public static final byte MAPPINGS_WO_DIFFS = 7;
    /** kasterm: Value for all reduced mappings (8). */
    public static final byte REDUCED_MAPPINGS  = 8;
    /** Value for obtaining read starts instead of coverage (9). */
    public static byte READ_STARTS = 9;
    /** Value for viewing all mappings as if they came from the fwd. strand 
     * (10). This should only be used in combination with {@link STRAND_BOTH}
     to infer, if all mappings shall be treated as if the came from the fwd. or
     the rev. strand ()*/
    public static byte STRAND_FWD_ANALYSIS = 10;
    
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
    
    /** 'Yc' = Tag for read classification in one of the three readXplorer classes. */
    public static final String TAG_READ_CLASS = "Yc";
    /** 'Yt' = Tag for number of positions a sequence maps to in a reference. */
    public static final String TAG_MAP_COUNT = "Yt";
    /** 'Yi' = Tag for the read pair id. */
    public static final String TAG_READ_PAIR_ID = "Yi";
    /** 'Ys' = Tag for the read pair type. */
    public static final String TAG_READ_PAIR_TYPE = "Ys";
    
    //Supported read pair extensions.
    /** / = separator used for read pair tags before Casava 1.8 format. */
    public static final char EXT_SEPARATOR = '/';
    /** 0 = For reads not having a pair tag. */
    public static final char EXT_UNDEFINED = '0';
    /** 1 = Supported extension of read 1. */
    public static final char EXT_A1 = '1';
    /** 2 = Supported extension of read 2. */
    public static final char EXT_A2 = '2';
    /** f = Supported extension of read 1. */
    public static final char EXT_B1 = 'f';
    /** r = Supported extension of read 2. */
    public static final char EXT_B2 = 'r';
    /** 1 = Supported extension of read 1 as String. */
    public static final String EXT_A1_STRING = String.valueOf(EXT_A1);
    /** 2 = Supported extension of read 2 as String. */
    public static final String EXT_A2_STRING = String.valueOf(EXT_A2);
    
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
    public static final String MAPPER_PARAMS = "MAPPER_PARAMS";
    
    /** Property for the height of data viewers. */
    public static final String VIEWER_HEIGHT = "VIEWER_HEIGHT";
    /** Small viewer height = "120" pixels. */
    public static final int SMALL_HEIGHT = 120;
    /** Medium viewer height = "200" pixels. */
    public static final int DEFAULT_HEIGHT = 200;
    /** Large viewer height = "250" pixels. */
    public static final int MAX_HEIGHT = 250;
    /** Property for auto scaling of viewers. */
    public static final String VIEWER_AUTO_SCALING = "AUTO_SCALING";
    
    /** 0 = Combine data of both strands option.*/
    public static final byte STRAND_BOTH = 0;
    /** 3 = Combine data of both strands option and treat them as if they were originating from fwd strand. */
    public static final byte STRAND_BOTH_FWD = 3;
    /** 4 = Combine data of both strands option and treat them as if they were originating from rev strand. */
    public static final byte STRAND_BOTH_REV = 4;
    /** 1 = Feature/analysis strand option.*/
    public static final byte STRAND_FEATURE = 1;
    /** 2 = Opposite strand option.*/
    public static final byte STRAND_OPPOSITE = 2;
    /** 0 = Combine data of both strands option string.*/
    public static final String STRAND_BOTH_STRING = String.valueOf(STRAND_BOTH);
    /** 1 = Feature/analysis strand option string.*/
    public static final String STRAND_FEATURE_STRING = String.valueOf(STRAND_FEATURE);
    /** 2 = Opposite strand option string.*/
    public static final String STRAND_OPPOSITE_STRING = String.valueOf(STRAND_OPPOSITE);
    
    /** 2 = Opposite strand option string.*/
    public static final String BASE_QUALITY_OPTION = "BASE_QUALITY_OPTION";
}
