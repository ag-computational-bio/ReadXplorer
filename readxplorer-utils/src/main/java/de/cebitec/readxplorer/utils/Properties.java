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

package de.cebitec.readxplorer.utils;


/**
 * Contains non language specific global constants.
 * <p>
 * @author Rolf Hilker
 */
public final class Properties {


    private Properties() {
    }


    // different adapter types for a project and or database
    public static final String ADAPTER_MYSQL = "mysql";
    public static final String ADAPTER_H2 = "h2";

    // protein DB properties
    public static final String DB_BRENDA = "http://www.brenda-enzymes.org/enzyme.php?ecno=";
    public static final String DB_EC2PDB = "http://www.ebi.ac.uk/thornton-srv/databases/cgi-bin/enzymes/GetPage.pl?ec_number=";
    public static final String DB_EXPASY = "http://enzyme.expasy.org/EC/";
    public static final String DB_INTENZ = "http://www.ebi.ac.uk/intenz/query?cmd=SearchEC&ec=";
    public static final String DB_KEGG = "http://www.genome.jp/dbget-bin/www_bget?ec:";
    public static final String DB_METACYC = "http://biocyc.org/META/substring-search?type=REACTION&object=";
    public static final String DB_PRIAM = "http://priam.prabi.fr/cgi-bin/PRIAM_profiles_CurrentRelease.pl?EC=";

    // properties mainly for genetic codes
    public static final String SEL_GENETIC_CODE = "selectedGeneticCode";
    public static final String STANDARD = "Standard";
    /**
     * 1 = Index of the standard genetic code.
     */
    public static final String STANDARD_CODE_INDEX = "1";
    public static final String GENETIC_CODE_INDEX = "geneticCodeIndex";
    public static final String CUSTOM_GENETIC_CODES = "customGeneticCode";

    // ReadXplorer file chooser properties
    public static final String READXPLORER_FILECHOOSER_DIRECTORY = "readXplorerFileChooser.Directory";
    public static final String READXPLORER_DATABASE_DIRECTORY = "readXplorer.Database.Directory";
    

    /**
     * Type value identifying an object as belonging to a "Start".
     */
    public static final byte START = 1;
    /**
     * Type value identifying an object as belonging to a "Stop".
     */
    public static final byte STOP = 2;
    /**
     * Type value identifying an object as belonging to a "pattern".
     */
    public static final byte PATTERN = 3;
    /**
     * Type value identifying an object as belonging to a "CDS" = coding
     * sequence.
     */
    public static final byte CDS = 4;
    /**
     * Type value identifying an object as belonging to any of the other types.
     */
    public static final byte ALL = 0;

    /**
     * 'Yc' = Tag for read classification in one of the three readxplorer
     * classes.
     */
    public static final String TAG_READ_CLASS = "Yc";
    /**
     * 'Yt' = Tag for number of positions a sequence maps to in a reference.
     */
    public static final String TAG_MAP_COUNT = "Yt";
    /**
     * 'Yi' = Tag for the read pair id.
     */
    public static final String TAG_READ_PAIR_ID = "Yi";
    /**
     * 'Ys' = Tag for the read pair type.
     */
    public static final String TAG_READ_PAIR_TYPE = "Ys";

    //Supported read pair extensions.
    /**
     * / = separator used for read pair tags before Casava 1.8 format.
     */
    public static final char EXT_SEPARATOR = '/';
    /**
     * 0 = For reads not having a pair tag.
     */
    public static final char EXT_UNDEFINED = '0';
    /**
     * 1 = Supported extension of read 1.
     */
    public static final char EXT_A1 = '1';
    /**
     * 2 = Supported extension of read 2.
     */
    public static final char EXT_A2 = '2';
    /**
     * f = Supported extension of read 1.
     */
    public static final char EXT_B1 = 'f';
    /**
     * r = Supported extension of read 2.
     */
    public static final char EXT_B2 = 'r';
    /**
     * 1 = Supported extension of read 1 as String.
     */
    public static final String EXT_A1_STRING = String.valueOf( EXT_A1 );
    /**
     * 2 = Supported extension of read 2 as String.
     */
    public static final String EXT_A2_STRING = String.valueOf( EXT_A2 );

    /**
     * Temporary directory used for import of data (SAM/BAM/JOK).
     */
    public static final String TMP_IMPORT_DIR = "TMP_IMPORT_DIR";

    /**
     * Protein database prefix used for creating EC-number links.
     */
    public static final String ENZYME_DB_LINK = "PROTEIN_DB_LINK";

    /**
     * The CRAN Mirror used by Gnu R to load missing packages.
     */
    public static final String CRAN_MIRROR = "CRAN_MIRROR";


    public static final String MAPPER_PATH = "MAPPER_PATH";

    /**
     * Extension to use for bam index files (".bai").
     */
    public static final String BAM_INDEX_EXT = ".bai";

    /**
     * '-1' For reference features, which do not have a parent.
     */
    public static final String NO_PARENT_STRING = "-1";
    public static final String MAPPER_PARAMS = "MAPPER_PARAMS";



    /**
     * Option for showing base qualities.
     */
    public static final String BASE_QUALITY_OPTION = "BASE_QUALITY_OPTION";

    /**
     * Option for adjusting the alignment block height.
     */
    public static final String BLOCK_HEIGHT_OPTION = "BLOCK_HEIGHT_OPTION";


}
