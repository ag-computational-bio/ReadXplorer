package de.cebitec.common.sequencetools.geneticcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The genetic code factory is responsible to provide genetic codes to the application.
 * By default the getDefault() method retrieves the NCBI genetic codes.
 *
 * @author rhilker
 * @author ljelonek
 */
public abstract class GeneticCodeFactory {

    private static GeneticCodeFactory defaultFactory;

    public static GeneticCodeFactory getDefault() {
        synchronized (GeneticCodeFactory.class) {
            if (defaultFactory == null) {
                try {
                    defaultFactory = new NCBIGeneticCodeParser().initGeneticCodes();
                } catch (IOException ex) {
                    throw new IllegalStateException("Genetic codes could not be loaded.", ex);
                }
            }
        }
        return defaultFactory;
    }

    /**
     * @return all available genetic codes for this factory.
     */
    public abstract List<GeneticCode> getGeneticCodes();

    /**
     * @param id id to scan for
     * @return genetic code with the given id or null, if it is not existent
     */
    public abstract GeneticCode getGeneticCodeById(int id);

    public static class DefaultGeneticCodeFactory extends GeneticCodeFactory {

        private final List<GeneticCode> geneticCodes;

        public DefaultGeneticCodeFactory(List<GeneticCode> geneticCodes) {
            this.geneticCodes = Collections.unmodifiableList(geneticCodes);
        }

        @Override
        public List<GeneticCode> getGeneticCodes() {
            return geneticCodes;
        }

        @Override
        public GeneticCode getGeneticCodeById(int id) {
            for (GeneticCode geneticCode : geneticCodes) {
                if (geneticCode.getId() == id) {
                    return geneticCode;
                }
            }
            throw new IllegalGeneticCodeException(id);
        }
    }

    /**
     * Generates a list of genetic codes from a hard coded local ASN1 file which should contain NCBI
     * standard genetic codes.
     */
    static class NCBIGeneticCodeParser {

        public static final String NAME_FIELD = "name";
        public static final String ID_FIELD = "id";
        public static final String AACID_FIELD = "ncbieaa";
        public static final String START_FIELD = "sncbieaa";
        public static final String BASE1_FIELD = "Base1";
        public static final String BASE2_FIELD = "Base2";
        public static final String BASE3_FIELD = "Base3";
        //file path with content according to ftp://ftp.ncbi.nih.gov/entrez/misc/data/gc.prt
        static final String GEN_CODE_LOCATION = "/de/cebitec/common/sequencetools/resources/geneticCodesNcbi.asn.prt";

        /**
         * Initializes and stores the genetic codes from a hard coded local ASN1 file. The genetic
         * codes can afterwards be obtained by calling
         * <code>getGeneticCodes()</code>
         */
        public GeneticCodeFactory initGeneticCodes() throws IOException {
            List<GeneticCode> geneticCodes = null;

            ASN1Parser parser = new ASN1Parser();
            ParsedASN1Table table = parser.parseData(GeneticCodeFactory.class.getResourceAsStream(GEN_CODE_LOCATION));

            geneticCodes = extractGeneticCodes(table.getParsedEntries());

            if (geneticCodes == null) {
                throw new IllegalStateException("Couldn't parse genetic codes");
            }

            return new DefaultGeneticCodeFactory(geneticCodes);
        }

        /**
         * Extracts the list of genetic codes obtained from a list of parsedANS1Entries sorted by
         * their id.
         *
         * @param entries parsed data from an ASN1 table containing genetic codes
         * @return sorted list of genetic codes obtained from the input
         */
        private List<GeneticCode> extractGeneticCodes(List<ParsedASN1Entry> entries) throws IOException {

            List<GeneticCode> codes = new ArrayList<GeneticCode>();

            for (ParsedASN1Entry entry : entries) {
                HashMap<String, List<String>> map = entry.getEntry();
                String name = null;
                String altName = null;
                int id = -1;
                int altId = 10000;
                String aminoAcids = null;
                String startList = null;
                String base1List = null;
                String base2List = null;
                String base3List = null;


                String[] names = getStringFromHash(map, NAME_FIELD, 2);
                //we only support two names!
                if (names != null) {
                    name = names[0];
                }
                if (names.length >= 2) {
                    altName = names[1];
                }
                String[] idList = getStringFromHash(map, ID_FIELD, 1);
                if (idList != null) {
                    try {
                        id = Integer.valueOf(idList[0]);
                    } catch (ClassCastException e) {
                        Logger.getLogger(ASN1Parser.class.getName()).log(Level.SEVERE, null,
                                "The id of at least one entry is not a number in your data at " + GEN_CODE_LOCATION
                                + ", thus replaced by 10000+");
                        id = altId++; //to prevent replacing existing ids
                    }
                }
                String[] aacids = getStringFromHash(map, AACID_FIELD, 1);
                if (aacids != null) {
                    aminoAcids = aacids[0];
                }
                String[] starts = getStringFromHash(map, START_FIELD, 1);
                if (starts != null) {
                    startList = starts[0];
                }
                String[] base1 = getStringFromHash(map, BASE1_FIELD, 1);
                if (base1 != null) {
                    base1List = base1[0];
                }
                String[] base2 = getStringFromHash(map, BASE2_FIELD, 1);
                if (base2 != null) {
                    base2List = base2[0];
                }
                String[] base3 = getStringFromHash(map, BASE3_FIELD, 1);
                if (base3 != null) {
                    base3List = base3[0];
                }


                try { //parse codons from strings here
                    GeneticCode code = parseCodeFromStrings(name, altName, id, aminoAcids, startList, base1List, base2List, base3List);
                    codes.add(code);
                } catch (NullPointerException ex) {
                    throw new IOException(ResourceBundle.getBundle("de/cebitec/common/sequencetools/Bundle").getString("GeneticCodeFactory.Parsing-Error") + " " + GEN_CODE_LOCATION);
                }
            }


            return codes;
        }

        /**
         * Tests the map for a given key and returns the first element of the value.
         *
         * @param map map containing identifiers and one or more strings belonging to the id
         * @param key id to check the map for
         * @return first element of the list belonging to the key (id),
         * <code>null</code> otherwise
         */
        private String[] getStringFromHash(HashMap<String, List<String>> map, String key, int maxEntries) {
            if (map.containsKey(key)) {
                List<String> list = map.get(key);
                maxEntries = list.size() < maxEntries ? list.size() : maxEntries;
                String[] entries = new String[maxEntries];
                for (int i = 0; i < maxEntries; ++i) {
                    if (list.get(i) != null) {
                        entries[i] = list.get(i);
                    } else {
                        return null; //everytime something is wrong just return null
                    }
                }
                return entries;

            }
            return null;
        }

        /**
         * Parses the assignment of an amino acid (single char code) to its DNA-codon and if it is a
         * start codon. The whole data for one genetic code generated from the input is then
         * returned. All strings are required to contain their corresponding data at the same index
         * of the string, e.g. Amino acid at 3 consists of codons at base1List pos 3, base2List pos
         * 3 and base3List pos 3
         *
         * @param aminoAcids list of amino acids in one character code
         * @param startList an 'M' means the codon at the current position is a start codon
         * @param base1List list of fst bases of each codon
         * @param base2List list of scnd bases of each codon
         * @param base3List list of thrd bases of each codon
         * @return whole data for one genetic code generated from the input
         */
        private GeneticCode parseCodeFromStrings(String name, String altName, int id, String aminoAcids,
                String startList, String base1List, String base2List, String base3List) {

            List<Codon> codonList = new ArrayList<Codon>();
            int length = aminoAcids.length(); //check length to prevent parsing wrong data
            if (length != startList.length() || length != base1List.length() || length != base2List.length()
                    || length != base3List.length()) {
                return null;
            }
            for (int i = 0; i < aminoAcids.length(); ++i) {

                boolean isStart = startList.charAt(i) == 'M';
                char[] codonArray = {base1List.charAt(i), base2List.charAt(i), base3List.charAt(i)};
                Codon codon = new Codon(String.copyValueOf(codonArray), aminoAcids.charAt(i), isStart);
                codonList.add(codon);
            }

            GeneticCode code = null;
            try {
                if (altName == null) {
                    code = new GeneticCode(id, name, codonList);
                } else {
                    code = new GeneticCode(id, name, altName, codonList);
                }
            } catch (MalformedGeneticCodeException ex) {
                Logger.getLogger(GeneticCodeFactory.class.getName()).log(Level.SEVERE, null, ex.getMessage());
            }

            return code;
        }
    }
}
