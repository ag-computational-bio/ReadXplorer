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
 * Contains H2SQL statements needed for data base connection and fetching of data
 * especially for h2 data bases.
 *
 * @author jstraube, rhilker
 */
public class H2SQLStatements {
    
    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private H2SQLStatements() {
    }

    //////////////////  statements for table creation  /////////////////////////
        
    public final static String SETUP_REFERENCE_GENOME =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_REFERENCE
            + " ("
            + FieldNames.REF_GEN_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.REF_GEN_NAME + " VARCHAR(200) NOT NULL, "
            + FieldNames.REF_GEN_DESCRIPTION + " VARCHAR(200) NOT NULL,"
            + FieldNames.REF_GEN_TIMESTAMP + " DATETIME NOT NULL,"
            + FieldNames.REF_GEN_FASTA_FILE + " VARCHAR(600) NOT NULL"
            + ") ";
    
    
    public final static String SETUP_CHROMOSOME = 
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_CHROMOSOME
            + " (" 
            + FieldNames.CHROM_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.CHROM_NUMBER + " BIGINT UNSIGNED NOT NULL, "             
            + FieldNames.CHROM_REFERENCE_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.CHROM_NAME + " VARCHAR(200) NOT NULL, "
            + FieldNames.CHROM_LENGTH + " BIGINT UNSIGNED NOT NULL "
            + ") ";
    
    
    public final static String INDEX_CHROMOSOME =
            "CREATE INDEX IF NOT EXISTS INDEXCHROMOSOM ON " + FieldNames.TABLE_CHROMOSOME 
            + " (" + FieldNames.CHROM_REFERENCE_ID + ") ";
       
    
    public final static String SETUP_FEATURES =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_FEATURES
            + " ("
            + FieldNames.FEATURE_ID + " BIGINT PRIMARY KEY, "
            + FieldNames.FEATURE_CHROMOSOME_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_PARENT_IDS + " VARCHAR (1000) NOT NULL, "
            + FieldNames.FEATURE_TYPE + " TINYINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_START + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_STOP + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.FEATURE_LOCUS_TAG + " VARCHAR (1000), "
            + FieldNames.FEATURE_PRODUCT + " VARCHAR (2000), "
            + FieldNames.FEATURE_EC_NUM + " VARCHAR (20), " +
            FieldNames.FEATURE_STRAND+" TINYINT NOT NULL, " +
            FieldNames.FEATURE_GENE+" VARCHAR (20) " +
            ") ";
    
    
    public final static String INDEX_FEATURES = 
            "CREATE INDEX IF NOT EXISTS INDEXFEATURES ON " + FieldNames.TABLE_FEATURES 
            + " (" + FieldNames.FEATURE_CHROMOSOME_ID + ") ";
    
    
    public final static String SETUP_TRACKS =
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_TRACK
            + " ( "
            + FieldNames.TRACK_ID + " BIGINT UNSIGNED PRIMARY KEY, "
            + FieldNames.TRACK_REFERENCE_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.TRACK_READ_PAIR_ID + " BIGINT UNSIGNED, " //only for paired sequences
            + FieldNames.TRACK_DESCRIPTION + " VARCHAR (200) NOT NULL, "
            + FieldNames.TRACK_TIMESTAMP + " DATETIME NOT NULL,  "
            + FieldNames.TRACK_PATH + " VARCHAR(600) "
            + ") ";
    
    
    public final static String INDEX_TRACK_REFID =
            "CREATE INDEX IF NOT EXISTS INDEXTRACK ON " + FieldNames.TABLE_TRACK 
            + " (" + FieldNames.TRACK_REFERENCE_ID + ") ";
    
    public final static String INDEX_TRACK_READ_PAIR_ID =
            "CREATE INDEX IF NOT EXISTS INDEXTRACK ON " + FieldNames.TABLE_TRACK 
            + " (" + FieldNames.TRACK_READ_PAIR_ID + ") ";
    
    
    public static final String SETUP_COUNT_DISTRIBUTION = 
            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_COUNT_DISTRIBUTION + " ( "
            + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COUNT_DISTRIBUTION_DISTRIBUTION_TYPE + " TINYINT UNSIGNED NOT NULL, "
            + FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID + " BIGINT UNSIGNED NOT NULL, "
            + FieldNames.COUNT_DISTRIBUTION_BIN_COUNT + " BIGINT UNSIGNED NOT NULL ) ";
    
    
    public static final String INDEX_COUNT_DIST = 
            "CREATE INDEX IF NOT EXISTS INDEX_COUNT_DIST ON " + FieldNames.TABLE_COUNT_DISTRIBUTION
            + " (" + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " ) ";
    
}
