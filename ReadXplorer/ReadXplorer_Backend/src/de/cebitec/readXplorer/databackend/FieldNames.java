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
package de.cebitec.readXplorer.databackend;

/**
 * Contains all field names for data base requests.
 *
 * @author ddoppmeier, rhilker
 */
public class FieldNames {
    
    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private FieldNames() {
    }

    // names for various database tables
    public static final String TABLE_PROJECT_FOLDER = "PROJECT_FOLDER";
    public static final String TABLE_REFERENCE = "REFERENCE";
    public static final String TABLE_CHROMOSOME = "CHROMOSOME";
    public static final String TABLE_FEATURES = "FEATURE";
    public static final String TABLE_TRACK = "TRACK";
    public static final String TABLE_STATISTICS = "STATISTICS";  
    public static final String TABLE_COUNT_DISTRIBUTION = "COUNT_DISTRIBUTION";
    public static final String TABLE_DB_VERSION = "DB_VERSION"; 
    
    
    ////////////////////////  tables fields  //////////////////////////////// 
        
    // project folder field
//    public static final String PROJECT_FOLDER_PATH = "PATH";
    
    // reference genome table fields
    public static final String REF_GEN_ID ="ID";
    public static final String REF_GEN_NAME = "NAME";
    public static final String REF_GEN_DESCRIPTION = "DESCRIPTION";
    public static final String REF_GEN_SEQUENCE = "SEQUENCE"; //still needed for old DBs...
    public static final String REF_GEN_TIMESTAMP = "CREATIONTIME";
    public static final String REF_GEN_FASTA_FILE = "FASTAFILE";

    // chromosome table fields
    public static final String CHROM_ID = "ID";
    public static final String CHROM_NUMBER = "NUMBER";
    public static final String CHROM_REFERENCE_ID = "REFERENCE_ID";
    public static final String CHROM_NAME = "NAME";
    public static final String CHROM_LENGTH = "LENGTH";
    
    //all feature table fields
    public static final String FEATURE_ID = "ID";
    /** Dont use this in new DBs, it is not available anmymore and was replaced by FEATURE_CHROMOSOME_ID!*/
    public static final String FEATURE_REFGEN_ID = "REFERENCE_ID"; 
    public static final String FEATURE_CHROMOSOME_ID = "CHROMOSOME_ID";
    public static final String FEATURE_PARENT_IDS = "PARENT_IDS"; //should be 0, if no parent exists
    public static final String FEATURE_TYPE = "TYPE";
    public static final String FEATURE_START = "START";
    public static final String FEATURE_STOP = "STOP";
    public static final String FEATURE_LOCUS_TAG = "LOCUS_TAG";
    public static final String FEATURE_PRODUCT = "PRODUCT";
    public static final String FEATURE_EC_NUM = "EC_NUM";
    public static final String FEATURE_STRAND = "STRAND";
    public static final String FEATURE_GENE = "GENE";

    // track table fields
    public static final String TRACK_ID = "ID";
    public static final String TRACK_REFERENCE_ID = "REFERENCE_ID";
    public static final String TRACK_READ_PAIR_ID = "SEQUENCE_PAIR_ID";
    public static final String TRACK_DESCRIPTION = "DESCRIPTION";
    public static final String TRACK_TIMESTAMP = "CREATIONTIME";
    public static final String TRACK_PATH = "PATH";
    
    // statistics table fields
    public static final String STATISTICS_ID = "ID";
    public static final String STATISTICS_TRACK_ID = "TRACK_ID";
    public static final String STATISTICS_KEY = "KEY";
    public static final String STATISTICS_VALUE = "VALUE";
    
    //old stats table fields
    public static final String STATISTICS_NUMBER_UNIQUE_MAPPINGS = "NUMBER_UNIQUE_MAPPINGS";
    public static final String STATISTICS_NUMBER_OF_UNIQUE_SEQ = "NUMBER_OF_UNIQUE_SEQ";
    public static final String STATISTICS_NUMBER_OF_REPEATED_SEQ = "NUMBER_OF_REPEATED_SEQ";
    public static final String STATISTICS_NUMBER_OF_MAPPINGS = "NUMBER_OF_MAPPINGS";
    public static final String STATISTICS_NUMBER_PERFECT_MAPPINGS = "NUMBER_OF_PERFECT_MAPPINGS";
    public static final String STATISTICS_NUMBER_BM_MAPPINGS = "NUMBER_OF_BM_MAPPINGS";
    public static final String STATISTICS_PERFECT_COVERAGE_OF_GENOME = "PERFECT_COVERAGE_OF_GENOME";
    public static final String STATISTICS_BM_COVERAGE_OF_GENOME = "BM_COVERAGE_OF_GENOME";
    public static final String STATISTICS_COMPLETE_COVERAGE_OF_GENOME = "COVERAGE_OF_GENOME";
    public static final String STATISTICS_NUMBER_READS = "NUMBER_OF_READS";
    public static final String STATISTICS_NUM_SEQUENCE_PAIRS = "NUM_SEQPAIRS";
    public static final String STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS = "NUM_UNIQUE_SEQPAIRS";
    public static final String STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS = "NUM_PERFECT_SEQPAIRS";
    public static final String STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS = "NUM_UNIQUE_PERFECT_SEQPAIRS";
    public static final String STATISTICS_NUM_SINGLE_MAPPINGS = "NUM_SINGLE_MAPPINGS";
    public static final String STATISTICS_NUM_SMALL_DIST_PAIRS = "NUM_SMALL_DIST_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_SMALL_PAIRS = "NUM_UNIQ_SMALL_PAIRS";
    public static final String STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS = "NUM_SMALL_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS = "NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_NUM_ORIENT_WRONG_PAIRS = "NUM_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS = "NUM_UNIQ_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_NUM_LARGE_DIST_PAIRS = "NUM_LARGE_DIST_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_LARGE_PAIRS = "NUM_UNIQ_LARGE_PAIRS";
    public static final String STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS = "NUM_LARGE_ORIENT_WRONG_PAIRS";
    public static final String STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS = "NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS";
    public static final String STATISTICS_AVERAGE_READ_LENGTH = "AVERAGE_READ_LENGTH";
    public static final String STATISTICS_AVERAGE_SEQ_PAIR_LENGTH= "AVERAGE_SEQPAIR_LENGTH";
    
    
    // unique mappings = count all distinct mapping ids
    // unique sequences = num mapped seq = count all distinct seq ids
    // num mappings = count ALL mapping ids
    // num reads = extra calculation: count all reads during import process, also possible later
    
    public static final String COUNT_DISTRIBUTION_TRACK_ID = "TRACK_ID";
    public static final String COUNT_DISTRIBUTION_DISTRIBUTION_TYPE = "DISTRIBUTION_TYPE";
    public static final String COUNT_DISTRIBUTION_COV_INTERVAL_ID = "BIN_INDEX";
    public static final String COUNT_DISTRIBUTION_BIN_COUNT = "BIN_COUNT";
    
    public static final String DB_VERSION_DB_VERSION_NO = "DB_VERSION_NO";
}
