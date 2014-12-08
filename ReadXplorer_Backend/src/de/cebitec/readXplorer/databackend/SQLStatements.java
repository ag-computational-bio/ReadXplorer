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
 * Contains all general SQL statements for communicating with the ReadXplorer
 * DB.
 *
 * @author ddoppmeier, rhilker
 */
public class SQLStatements {

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private SQLStatements() {
    }


    /**
     * For retrieving "NUM" result from result set of a db request.
     */
    public static final String GET_NUM = "NUM";

    //////////////////  statements for table creation  /////////////////////////

//    public static final String SETUP_PROJECT_FOLDER =
//            "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_PROJECT_FOLDER
//            + " ("
//            + FieldNames.PROJECT_FOLDER_PATH + " VARCHAR(400) NOT NULL "
//            + ") ";
    public final static String SETUP_STATISTICS
            = "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_STATISTICS + " "
              + "( "
              + FieldNames.STATISTICS_ID + " BIGINT UNSIGNED PRIMARY KEY, "
              + FieldNames.STATISTICS_TRACK_ID + " BIGINT UNSIGNED NOT NULL, "
              + FieldNames.STATISTICS_KEY + " VARCHAR(100) NOT NULL, "
              + FieldNames.STATISTICS_VALUE + " BIGINT UNSIGNED NOT NULL "
              + ") ";

    public static final String SETUP_DB_VERSION_TABLE
            = "CREATE TABLE IF NOT EXISTS " + FieldNames.TABLE_DB_VERSION
              + " ( "
              + FieldNames.DB_VERSION_DB_VERSION_NO + " INT UNSIGNED "
              + " ) ";


    /**
     * Only needed for backward compatibility as long as older databases are
     * floating around with unnecessary or changed tables.
     */
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    /**
     * Only needed as long as older databases are floating around and did not
     * already drop this index which is not necessary anymore.
     */
    public static String DROP_INDEX = "DROP INDEX IF EXISTS ";

    //////////////////  statements for data insertion  ////////////////////////

//    public static final String INSERT_PROJECT_FOLDER =
//            "INSERT INTO " + FieldNames.TABLE_PROJECT_FOLDER
//            + "("
//            + FieldNames.PROJECT_FOLDER_PATH
//            + ") "
//            + " VALUES (?); ";
    public final static String INSERT_REFGENOME
            = "INSERT INTO " + FieldNames.TABLE_REFERENCE + " "
              + "("
              + FieldNames.REF_GEN_ID + ", "
              + FieldNames.REF_GEN_NAME + ","
              + FieldNames.REF_GEN_DESCRIPTION + ","
              + FieldNames.REF_GEN_TIMESTAMP + ","
              + FieldNames.REF_GEN_FASTA_FILE + " "
              + ") "
              + "VALUES (?,?,?,?,?)";


    public final static String INSERT_CHROMOSOME
            = "INSERT INTO " + FieldNames.TABLE_CHROMOSOME + " "
              + "("
              + FieldNames.CHROM_ID + ", "
              + FieldNames.CHROM_NUMBER + ", "
              + FieldNames.CHROM_REFERENCE_ID + ", "
              + FieldNames.CHROM_NAME + ", "
              + FieldNames.CHROM_LENGTH + " "
              + ") "
              + "VALUES (?,?,?,?,?)";


    public final static String INSERT_FEATURE
            = "INSERT INTO " + FieldNames.TABLE_FEATURES + " "
              + "("
              + FieldNames.FEATURE_ID + ", "
              + FieldNames.FEATURE_CHROMOSOME_ID + ", "
              + FieldNames.FEATURE_PARENT_IDS + ", "
              + FieldNames.FEATURE_TYPE + ", "
              + FieldNames.FEATURE_START + ", "
              + FieldNames.FEATURE_STOP + ", "
              + FieldNames.FEATURE_LOCUS_TAG + ", "
              + FieldNames.FEATURE_PRODUCT + ", "
              + FieldNames.FEATURE_EC_NUM + ", "
              + FieldNames.FEATURE_STRAND + ", "
              + FieldNames.FEATURE_GENE
              + " ) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?,?) ";


    public final static String INSERT_TRACK
            = "INSERT INTO " + FieldNames.TABLE_TRACK
              + " ("
              + FieldNames.TRACK_ID + ", "
              + FieldNames.TRACK_REFERENCE_ID + ", "
              + FieldNames.TRACK_DESCRIPTION + ", "
              + FieldNames.TRACK_TIMESTAMP + ", "
              + FieldNames.TRACK_PATH
              + ") "
              + "VALUES (?,?,?,?,?)";


    public static final String RESET_TRACK_PATH
            = "UPDATE " + FieldNames.TABLE_TRACK
              + " SET " + FieldNames.TRACK_PATH + " = ? "
              + "WHERE " + FieldNames.TRACK_ID + " = ?;";


    public static final String RESET_REF_PATH
            = "UPDATE " + FieldNames.TABLE_REFERENCE
              + " SET " + FieldNames.REF_GEN_FASTA_FILE + " = ? "
              + "WHERE " + FieldNames.REF_GEN_ID + " = ?;";


    /**
     * @param readPairId read pair id to set for current track
     * @param trackId    track id to set the read pair id for
     */
    public static final String INSERT_TRACK_READ_PAIR_ID
            = "UPDATE " + FieldNames.TABLE_TRACK
              + " SET " + FieldNames.TRACK_READ_PAIR_ID + " = ? "
              + " WHERE " + FieldNames.TRACK_ID + " = ? ";

    /**
     * Insert statistics data of one track into statistics table.
     */
    public final static String INSERT_STATISTICS
            = "INSERT INTO " + FieldNames.TABLE_STATISTICS + " "
              + "("
              + FieldNames.STATISTICS_ID + ", "
              + FieldNames.STATISTICS_TRACK_ID + ", "
              + FieldNames.STATISTICS_KEY + ", "
              + FieldNames.STATISTICS_VALUE + " ) "
              + "VALUES (?,?,?,?)";

    /**
     * Update exisiting row of track statistics with sequence pair statistics
     */
    public static final String INSERT_READPAIR_STATISTICS
            = "UPDATE " + FieldNames.TABLE_STATISTICS
              + " SET "
              + FieldNames.STATISTICS_NUM_SEQUENCE_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQUE_SEQUENCE_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_PERFECT_SEQUENCE_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQUE_PERFECT_SEQUENCE_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_SMALL_DIST_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQ_SMALL_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_LARGE_DIST_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQ_LARGE_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_ORIENT_WRONG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQ_ORIENT_WRNG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_SMALL_ORIENT_WRONG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQ_SMALL_ORIENT_WRNG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_LARGE_ORIENT_WRONG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_UNIQ_LARGE_ORIENT_WRNG_PAIRS + " = ?, "
              + FieldNames.STATISTICS_NUM_SINGLE_MAPPINGS + " = ?, "
              + FieldNames.STATISTICS_AVERAGE_SEQ_PAIR_LENGTH + " = ? "
              + " WHERE "
              + FieldNames.STATISTICS_TRACK_ID + " = ? ";

    /**
     * Insert a new coverage distribution for a track with all 35 distribution
     * fields.
     */
    public static final String INSERT_COUNT_DISTRIBUTION
            = "INSERT INTO " + FieldNames.TABLE_COUNT_DISTRIBUTION
              + " ("
              + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + ", "
              + FieldNames.COUNT_DISTRIBUTION_DISTRIBUTION_TYPE + ", "
              + FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID + ", "
              + FieldNames.COUNT_DISTRIBUTION_BIN_COUNT
              + " ) "
              + "VALUES (?,?,?,?)";

    public static final String INSERT_DB_VERSION_NO
            = "INSERT INTO " + FieldNames.TABLE_DB_VERSION
              + " ("
              + FieldNames.DB_VERSION_DB_VERSION_NO
              + " )"
              + " VALUES (?)";

    public static final String UPDATE_DB_VERSION_NO
            = "UPDATE " + FieldNames.TABLE_DB_VERSION
              + " SET " + FieldNames.DB_VERSION_DB_VERSION_NO + " = ? ";


    public final static String DELETE_TRACK
            = "DELETE FROM "
              + FieldNames.TABLE_TRACK
              + " WHERE "
              + FieldNames.TRACK_ID + " = ? ";


    public final static String DELETE_STATISTIC_FROM_TRACK
            = "DELETE FROM "
              + FieldNames.TABLE_STATISTICS
              + " WHERE "
              + FieldNames.STATISTICS_TRACK_ID + " = ? ";

    public final static String DELETE_SPECIFIC_TRACK_STATISTIC
            = "DELETE FROM "
              + FieldNames.TABLE_STATISTICS
              + " WHERE "
              + FieldNames.STATISTICS_TRACK_ID + " = ? and "
              + FieldNames.STATISTICS_KEY + " = ?";

    public static final String DELETE_COUNT_DISTRIBUTIONS_FROM_TRACK
            = "DELETE FROM "
              + FieldNames.TABLE_COUNT_DISTRIBUTION
              + " WHERE "
              + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " = ? ";

    /**
     * Delete all genomic features of a chromosome from the feature table.
     */
    public final static String DELETE_FEATURES_FROM_CHROMOSOME
            = "DELETE FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ?";

    /**
     * Delete a chromosome from the chromosome table.
     */
    public static String DELETE_CHROMOSOME
            = "DELETE FROM "
              + FieldNames.TABLE_CHROMOSOME
              + " WHERE "
              + FieldNames.CHROM_ID + " = ?";

    /**
     * Delete a reference genome from the reference table.
     */
    public final static String DELETE_GENOME
            = "DELETE FROM "
              + FieldNames.TABLE_REFERENCE
              + " WHERE "
              + FieldNames.REF_GEN_ID + " = ?";


    // statements to fetch data from database
//    public static final String FETCH_PROJECT_FOLDER =
//            "SELECT "
//                + FieldNames.PROJECT_FOLDER_PATH
//            + " FROM "
//                + FieldNames.TABLE_PROJECT_FOLDER;
    public static final String FETCH_GENOMES
            = "SELECT "
              + "R." + FieldNames.REF_GEN_ID + ", "
              + "R." + FieldNames.REF_GEN_NAME + ", "
              + "R." + FieldNames.REF_GEN_DESCRIPTION + ", "
              + "R." + FieldNames.REF_GEN_TIMESTAMP + ", "
              + "R." + FieldNames.REF_GEN_FASTA_FILE + " "
              + " FROM "
              + FieldNames.TABLE_REFERENCE + " AS R ";


    public static final String FETCH_DB_VERSION
            = "SELECT "
              + FieldNames.DB_VERSION_DB_VERSION_NO
              + " FROM "
              + FieldNames.TABLE_DB_VERSION;

    public static final String FETCH_CHROMOSOMES
            = "SELECT "
              + FieldNames.CHROM_ID + ", "
              + FieldNames.CHROM_NUMBER + ", "
              + FieldNames.CHROM_NAME + ", "
              + FieldNames.CHROM_LENGTH
              + " FROM "
              + FieldNames.TABLE_CHROMOSOME
              + " WHERE "
              + FieldNames.CHROM_REFERENCE_ID + " = ? ";

    public static final String FETCH_CHROMOSOME
            = "SELECT "
              + FieldNames.CHROM_NAME + ", "
              + FieldNames.CHROM_NUMBER + ", "
              + FieldNames.CHROM_LENGTH
              + " FROM "
              + FieldNames.TABLE_CHROMOSOME
              + " WHERE "
              + FieldNames.CHROM_ID + " = ? ";

    /**
     * Fetch the reference sequence file for a given reference id.
     */
    public static final String FETCH_REF_FILE
            = "SELECT "
              + FieldNames.REF_GEN_FASTA_FILE
              + " FROM "
              + FieldNames.TABLE_REFERENCE
              + " WHERE "
              + FieldNames.REF_GEN_ID + " = ?";

    /**
     * Fetch the number of chromosomes for a reference.
     */
    public static String FETCH_NUMBER_CHROMS_FOR_REF
            = "SELECT "
              + "COUNT(" + FieldNames.TABLE_CHROMOSOME + "." + FieldNames.CHROM_ID + ") as NUM "
              + " FROM "
              + FieldNames.TABLE_CHROMOSOME
              + " WHERE "
              + FieldNames.CHROM_REFERENCE_ID + " = ? ";

    /**
     * Updates the reference genome sequence.
     */
    public static final String UPDATE_REF_FILE
            = "UPDATE " + FieldNames.TABLE_REFERENCE
              + " SET "
              + FieldNames.REF_GEN_FASTA_FILE + " = ? "
              + " WHERE "
              + FieldNames.REF_GEN_ID + " = ? ";


    public static final String UPDATE_FEATURE_TABLE
            = "UPDATE " + FieldNames.TABLE_FEATURES
              + " SET "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? "
              + " WHERE "
              + FieldNames.FEATURE_REFGEN_ID + " = ? ";

    /**
     * Statement for old DBs to fetch the reference sequence, still stored here.
     * <p>
     * @deprecated for newer DB Versions, since the sequences are stored in the
     * chromosome table!
     */
    @Deprecated
    public static final String FETCH_REF_SEQ
            = " SELECT "
              + FieldNames.REF_GEN_SEQUENCE + ", "
              + FieldNames.REF_GEN_NAME
              + " FROM "
              + FieldNames.TABLE_REFERENCE
              + " WHERE "
              + FieldNames.REF_GEN_ID + " = ? ";


    public final static String FETCH_TRACKS
            = "SELECT "
              + " * "
              + "FROM "
              + FieldNames.TABLE_TRACK;

    public static final String FETCH_TRACK
            = " SELECT * FROM "
              + FieldNames.TABLE_TRACK
              + " WHERE "
              + FieldNames.TRACK_ID + " = ? ";


    public final static String FETCH_SINGLE_GENOME
            = "SELECT "
              + "R." + FieldNames.REF_GEN_ID + ", "
              + "R." + FieldNames.REF_GEN_NAME + ", "
              + "R." + FieldNames.REF_GEN_DESCRIPTION + ", "
              + "R." + FieldNames.REF_GEN_TIMESTAMP + ", "
              + "R." + FieldNames.REF_GEN_FASTA_FILE + " "
              + " FROM "
              + FieldNames.TABLE_REFERENCE + " AS R"
              + " WHERE "
              + FieldNames.REF_GEN_ID + " = ?";

    //Select ID from first feature belonging to the referece genome
    public final static String CHECK_IF_FEATURES_EXIST
            = "SELECT "
              + FieldNames.FEATURE_ID
              + " FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? LIMIT 1";

    public final static String CHECK_IF_FEATURES_OF_TYPE_EXIST
            = "SELECT "
              + FieldNames.FEATURE_ID
              + " FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? and "
              + FieldNames.FEATURE_TYPE + " = ?";

    public final static String FETCH_FEATURES_FOR_CHROM_INTERVAL
            = "SELECT "
              + FieldNames.FEATURE_ID + ", "
              + FieldNames.FEATURE_PARENT_IDS + ", "
              + FieldNames.FEATURE_TYPE + ", "
              + FieldNames.FEATURE_START + ", "
              + FieldNames.FEATURE_STOP + ", "
              + FieldNames.FEATURE_EC_NUM + ", "
              + FieldNames.FEATURE_LOCUS_TAG + ", "
              + FieldNames.FEATURE_PRODUCT + ", "
              + FieldNames.FEATURE_STRAND + ", "
              + FieldNames.FEATURE_GENE
              + " FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? and "
              + FieldNames.FEATURE_STOP + " >= ? and "
              + FieldNames.FEATURE_START + " <= ? "
              + " ORDER BY " + FieldNames.FEATURE_START;

    public final static String FETCH_SPECIFIED_FEATURES_FOR_CHROM_INTERVAL
            = "SELECT "
              + FieldNames.FEATURE_ID + ", "
              + FieldNames.FEATURE_PARENT_IDS + ", "
              + FieldNames.FEATURE_TYPE + ", "
              + FieldNames.FEATURE_START + ", "
              + FieldNames.FEATURE_STOP + ", "
              + FieldNames.FEATURE_EC_NUM + ", "
              + FieldNames.FEATURE_LOCUS_TAG + ", "
              + FieldNames.FEATURE_PRODUCT + ", "
              + FieldNames.FEATURE_STRAND + ", "
              + FieldNames.FEATURE_GENE
              + " FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? and "
              + FieldNames.FEATURE_STOP + " >= ? and "
              + FieldNames.FEATURE_START + " <= ? and "
              + FieldNames.FEATURE_TYPE + " = ? "
              + " ORDER BY " + FieldNames.FEATURE_START;

    public final static String FETCH_FEATURES_FOR_CLOSED_GENOME_INTERVAL
            = "SELECT "
              + FieldNames.FEATURE_ID + ", "
              + FieldNames.FEATURE_PARENT_IDS + ", "
              + FieldNames.FEATURE_TYPE + ", "
              + FieldNames.FEATURE_START + ", "
              + FieldNames.FEATURE_STOP + ", "
              + FieldNames.FEATURE_EC_NUM + ", "
              + FieldNames.FEATURE_LOCUS_TAG + ", "
              + FieldNames.FEATURE_PRODUCT + ", "
              + FieldNames.FEATURE_STRAND + ", "
              + FieldNames.FEATURE_GENE
              + " FROM "
              + FieldNames.TABLE_FEATURES
              + " WHERE "
              + FieldNames.FEATURE_CHROMOSOME_ID + " = ? and "
              + FieldNames.FEATURE_START + " between ? and ? and "
              + FieldNames.FEATURE_STOP + " between ? and ? "
              + " ORDER BY " + FieldNames.FEATURE_START;


    public final static String FETCH_TRACKS_FOR_GENOME
            = "SELECT * "
              + " FROM "
              + FieldNames.TABLE_TRACK
              + " WHERE "
              + FieldNames.TRACK_REFERENCE_ID + " = ? ";


    public final static String GET_LATEST_STATISTICS_ID
            = "SELECT "
              + "MAX(" + FieldNames.STATISTICS_ID + ") AS LATEST_ID "
              + "FROM "
              + FieldNames.TABLE_STATISTICS;


    /////////////////// statistics calculations and queries //////////////////////////
    public final static String CHECK_FOR_TRACK_IN_STATS_CALCULATE
            = "SELECT "
              + "COUNT(S." + FieldNames.STATISTICS_TRACK_ID + ") as NUM "
              + " FROM "
              + FieldNames.TABLE_STATISTICS + " as S "
              + "WHERE "
              + "S." + FieldNames.STATISTICS_TRACK_ID + " = ?";


    public static final String FETCH_STATS_FOR_TRACK
            = "SELECT * FROM "
              + FieldNames.TABLE_STATISTICS
              + " WHERE "
              + FieldNames.STATISTICS_TRACK_ID + " = ?";


//    public final static String FETCH_GENOMEID_FOR_TRACK =
//            "SELECT "
//            + FieldNames.TRACK_REFERENCE_ID + " "
//            + "FROM "
//            + FieldNames.TABLE_TRACK + " "
//            + "WHERE "
//            + FieldNames.TRACK_ID + " = ?";
    /**
     * @param trackId track id of one track of a read pair
     */
    public static String FETCH_READ_PAIR_TO_TRACK_ID
            = "SELECT "
              + FieldNames.TRACK_READ_PAIR_ID + " AS NUM "
              + "FROM "
              + FieldNames.TABLE_TRACK + " "
              + "WHERE "
              + FieldNames.TRACK_ID + " = ? ";

    /**
     * Fetches second track id for read pair tracks.
     * <p>
     * @param readPairId read pair id
     * @param trackId    track id of one of the two tracks of the pair
     */
    public static String FETCH_TRACK_ID_TO_READ_PAIR_ID
            = "SELECT "
              + FieldNames.TRACK_ID + " "
              + "FROM "
              + FieldNames.TABLE_TRACK + " "
              + "WHERE "
              + FieldNames.TRACK_READ_PAIR_ID + " = ? AND "
              + FieldNames.TRACK_ID + " != ? ";


    public final static String GET_LATEST_REFERENCE_ID
            = "SELECT MAX(" + FieldNames.REF_GEN_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_REFERENCE;


    public final static String GET_LATEST_CHROMOSOME_ID
            = "SELECT MAX(" + FieldNames.CHROM_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_CHROMOSOME;


    public final static String GET_LATEST_FEATURE_ID
            = "SELECT MAX(" + FieldNames.FEATURE_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_FEATURES;


    public final static String GET_LATEST_TRACK_ID
            = "SELECT MAX(" + FieldNames.TRACK_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_TRACK;


    public static final String GET_LATEST_TRACK_READ_PAIR_ID
            = "SELECT MAX(" + FieldNames.TRACK_READ_PAIR_ID + ") AS LATEST_ID FROM " + FieldNames.TABLE_TRACK;


    public static final String FETCH_COUNT_DISTRIBUTION
            = "SELECT "
              + FieldNames.COUNT_DISTRIBUTION_COV_INTERVAL_ID + ", "
              + FieldNames.COUNT_DISTRIBUTION_BIN_COUNT
              + " FROM "
              + FieldNames.TABLE_COUNT_DISTRIBUTION
              + " WHERE "
              + FieldNames.COUNT_DISTRIBUTION_TRACK_ID + " = ? AND "
              + FieldNames.COUNT_DISTRIBUTION_DISTRIBUTION_TYPE + " = ? ";


    public static String INIT_FEATURE_PARENT_ID
            = "UPDATE "
              + FieldNames.TABLE_FEATURES
              + " SET "
              + FieldNames.FEATURE_PARENT_IDS
              + " = 0 "
              + " WHERE "
              + FieldNames.FEATURE_PARENT_IDS + " IS NULL ";


    public static final String NOT_NULL_FEATURE_PARENT_ID
            = "ALTER TABLE "
              + FieldNames.TABLE_FEATURES
              + " ALTER COLUMN "
              + FieldNames.FEATURE_PARENT_IDS
              + " SET NOT NULL";


    public static String INIT_FASTAFILE
            = "UPDATE "
              + FieldNames.TABLE_REFERENCE
              + " SET "
              + FieldNames.REF_GEN_FASTA_FILE
              + " = default "
              + " WHERE "
              + FieldNames.REF_GEN_FASTA_FILE + " IS NULL ";


    public static final String NOT_NULL_FASTAFILE
            = "ALTER TABLE "
              + FieldNames.TABLE_REFERENCE
              + " ALTER COLUMN "
              + FieldNames.REF_GEN_FASTA_FILE
              + " SET NOT NULL";


    public static final String NOT_NULL_CHROMOSOME_ID
            = "ALTER TABLE "
              + FieldNames.TABLE_FEATURES
              + " ALTER COLUMN "
              + FieldNames.FEATURE_CHROMOSOME_ID
              + " SET NOT NULL";

//             public static final String COPY_TO_FEATURE_DETAILS_TABLE =
//                " INSERT INTO " + FieldNames.TABLE_FEATURE_DETAILS + " ("
//                    + FieldNames.FEATURE_ID + ", "
//                    + FieldNames.FEATURE_EC_NUM + ", "
//                    + FieldNames.FEATURE_LOCUS_TAG + ", "
//                    + FieldNames.FEATURE_PRODUCT + ", "
//                    + FieldNames.FEATURE_STRAND + ", "
//                    + FieldNames.FEATURE_GENE + ") "
//                + " SELECT "
//                    + FieldNames.FEATURE_ID + ", "
//                    + FieldNames.FEATURE_EC_NUM + ", "
//                    + FieldNames.FEATURE_LOCUS_TAG + ", "
//                    + FieldNames.FEATURE_PRODUCT + ", "
//                    + FieldNames.FEATURE_STRAND + ", "
//                    + FieldNames.FEATURE_GENE
//                + " FROM "
//                    + FieldNames.TABLE_FEATURES +
//                " WHERE EXISTS ("
//                    + "SELECT * "
//                    + "FROM INFORMATION_SCHEMA.COLUMNS "
//                    + "WHERE TABLE_NAME = '" + FieldNames.TABLE_FEATURES + "' "
//                    + " AND COLUMN_NAME = '" + FieldNames.FEATURE_PRODUCT + "' ) ";
//
//
//
//
//             public static final String CHECK_FEATURE_TABLE =
//                "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
//                    + FieldNames.TABLE_FEATURES + "' AND COLUMN_NAME = '"
//                    + FieldNames.FEATURE_PRODUCT + "' ";
//
//
//
//
//             public static final String ALTER_FEATURE_TABLE =
//                "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_EC_NUM + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_GENE + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_LOCUS_TAG + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_PRODUCT + "; "
//                    + "ALTER TABLE " + FieldNames.TABLE_FEATURES + " DROP COLUMN " + FieldNames.FEATURE_STRAND + "; ";
}
