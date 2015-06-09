
package de.cebitec.readxplorer.api.constants;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class Paths {


    private Paths() {}


    /**
     * Extension to use for bam index files (".bai").
     */
    public static final String BAM_INDEX_EXT = ".bai";

    /**
     * The CRAN Mirror used by Gnu R to load missing packages.
     */
    public static final String CRAN_MIRROR = "CRAN_MIRROR";

    // ReadXplorer file chooser properties
    public static final String READXPLORER_DATABASE_DIRECTORY    = "readXplorer.Database.Directory";
    public static final String READXPLORER_FILECHOOSER_DIRECTORY  = "readXplorerFileChooser.Directory";

    /**
     * Temporary directory used for import of data (SAM/BAM/JOK).
     */
    public static final String TMP_IMPORT_DIR = "TMP_IMPORT_DIR";

    public static final String MAPPER_PATH = "MAPPER_PATH";


    // protein DB properties
    public static final String DB_BRENDA = "http://www.brenda-enzymes.org/enzyme.php?ecno=";
    public static final String DB_EC2PDB = "http://www.ebi.ac.uk/thornton-srv/databases/cgi-bin/enzymes/GetPage.pl?ec_number=";
    public static final String DB_EXPASY = "http://enzyme.expasy.org/EC/";
    public static final String DB_INTENZ = "http://www.ebi.ac.uk/intenz/query?cmd=SearchEC&ec=";
    public static final String DB_KEGG = "http://www.genome.jp/dbget-bin/www_bget?ec:";
    public static final String DB_METACYC = "http://biocyc.org/META/substring-search?type=REACTION&object=";
    public static final String DB_PRIAM = "http://priam.prabi.fr/cgi-bin/PRIAM_profiles_CurrentRelease.pl?EC=";

}
