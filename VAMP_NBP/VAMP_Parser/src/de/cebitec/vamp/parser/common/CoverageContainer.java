package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.SequenceUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for the coverage data. Computes the coverage, on creation.
 *
 * @author ddoppmeier
 */
public class CoverageContainer {

    private HashMap<Integer, Integer[]> coverage;
    private static final int BEST_MAPPING_CASE = 0;
    private static final int ZERO_ERROR_CASE = 1;
    private static final int N_ERROR_CASE = 2;
    private static final int NUM_OF_CASES = 3;
    private static final int FIELDS_PER_CASE = 4; //2 for fwd (all & without duplicates), and 2 rev
    // snp table
    private HashMap<String, Integer[]> positionTable;
    private static final int BASE_A = 0;
    private static final int BASE_C = 1;
    private static final int BASE_G = 2;
    private static final int BASE_T = 3;
    private static final int BASE_N = 4;
    private static final int BASE_GAP = 5;
    private static final int GAP_A = 6;
    private static final int GAP_C = 7;
    private static final int GAP_G = 8;
    private static final int GAP_T = 9;
    private static final int GAP_N = 10;
    private static final int DIFFS = 11;
    private int coverageArrayLength;

    /**
     * Creates a new CoverageContainer and immediately computes the coverage.
     * @param mappings The mappings whose coverage has to be computed
     */
    public CoverageContainer(ParsedMappingContainer mappings) {
        coverage = new HashMap<Integer, Integer[]>();
        positionTable = new HashMap<String, Integer[]>();
        coverageArrayLength = NUM_OF_CASES * FIELDS_PER_CASE;
        this.computeCoverage(mappings);
    }

    private void computeCoverage(ParsedMappingContainer mappings) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start computing the coverage");
        Iterator<Integer> sequenceIDIt = mappings.getMappedSequenceIDs().iterator();

        // add all mappings to their mapping groups
        while (sequenceIDIt.hasNext()) {
            ParsedMappingGroup g = mappings.getParsedMappingGroupBySeqID(sequenceIDIt.next());
            Iterator<ParsedMapping> mappingIt = g.getMappings().iterator();
            while (mappingIt.hasNext()) {
                ParsedMapping s = mappingIt.next();
                this.addMapping(s);
                this.savePositions(s);
                
                //mappingIt.remove();
            }
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished computing the coverage");
    }

    private void addMapping(ParsedMapping s) {
        
        // store best mapping coverage
        if (s.isBestMapping()) {
            this.increaseCoverage(s, BEST_MAPPING_CASE);
        }
        // store zero error coverage
        if (!s.hasDiffs()) {
            this.increaseCoverage(s, ZERO_ERROR_CASE);
        }
        // store n error coverage
        this.increaseCoverage(s, N_ERROR_CASE);

    }

    private void increaseCoverage(ParsedMapping mapping, int coverageCase) {
        int coverageIdx = coverageCase * FIELDS_PER_CASE;
        // if this mapping is on reverse strand, we need an offset of 2
        int offset = (mapping.getDirection() == 1 ? 0 : 2);
        coverageIdx += offset;
        int numIdx = coverageIdx + 1;

        for (int i = mapping.getStart(); i <= mapping.getStop(); i++) {
            // init coverage array if not done yet
            if (!coverage.containsKey(i)) {
                Integer[] newCov = new Integer[coverageArrayLength];
                for (int j = 0; j < newCov.length; j++) {
                    newCov[j] = 0;
                }
                coverage.put(i, newCov);
            }
            // increase the values in coverage array
            Integer[] cov = coverage.get(i);
            cov[coverageIdx] += mapping.getCount();
            cov[numIdx] += 1;
        }
    }

    public Collection<Integer> getCoveredPositions() {
        return coverage.keySet();
    }

    //real coverage
    public int getBestMappingForwardCoverage(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, true, true);
    }
    //number of unique mappings

    public int getNumberOfBestMapppingsForward(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, true, false);
    }

    public int getBestMappingReverseCoverage(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, false, true);
    }

    public int getNumberOfBestMapppingsReverse(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, false, false);
    }

    public int getZeroErrorMappingsForwardCoverage(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, true, true);
    }

    public int getNumberOfZeroErrorMappingsForward(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, true, false);
    }

    public int getZeroErrorMappingsReverseCoverage(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, false, true);
    }

    public int getNumberOfZeroErrorMappingsReverse(int position) {
        return this.getCoverageOfType(position, ZERO_ERROR_CASE, false, false);
    }

    public int getNErrorMappingsForwardCoverage(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, true, true);
    }

    public int getNumberOfNErrorMappingsForward(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, true, false);
    }

    public int getNErrorMappingsReverseCoverage(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, false, true);
    }

    public int getNumberOfNErrorMappingsReverse(int position) {
        return this.getCoverageOfType(position, N_ERROR_CASE, false, false);
    }

    /**
     *
     * @param position Position for which the coverage is requestet
     * @param type Type of coverage that is queried. For example: bestMatchCoverage
     * @param forward Coverage on forward strand requested, yes or no,
     * @param mult If true, get the "real" coverage, if false get the number of unique mappings (no redundancy)
     * @return coverage
     */
    private int getCoverageOfType(int position, int type, boolean forward, boolean mult) {

        Integer[] cov = coverage.get(position);
        int baseIDX = type * FIELDS_PER_CASE; // basic index defined by type of coverage
        baseIDX += (forward ? 0 : 2); // if reverse strand requested, increase column by two
        baseIDX += (mult ? 0 : 1); // if multiplied coverage (instead of unique_mapping_coverage) requested do nothing, else increase by one

        return cov[baseIDX];
    }

    public void clear() {
        coverage.clear();
    }
    
    public void savePositions(ParsedMapping s) {
        
        if (s.isBestMapping()) {
            List<ParsedDiff> diffs = s.getDiffs();
            List<ParsedReferenceGap> gaps = s.getGenomeGaps();
            // Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.valueOf(s.getNumOfDiffs()));
            // Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.valueOf(s.getGenomeGaps().size()));
            // saves diffs
            for (int i = 0; i < s.getNumOfDiffs(); i++) {

                ParsedDiff diff = diffs.get(i);
                long positionInt = diff.getPosition();
                String position = String.valueOf(positionInt);
                char base = diff.getBase();
                if (s.getDirection() == -1) {
                    base = SequenceUtils.complementDNA(base);
                }
                
                // init positionTable if not done yet
                if (!positionTable.containsKey(position)) {
                    Integer[] bases = new Integer[12];
                    for (int j = 0; j < bases.length; j++) {
                        bases[j] = 0;
                    }
                    positionTable.put(position, bases);
                }

                // increase occurence of bases at position
                Integer[] bases = positionTable.get(position);
                switch (base) {
                    case 'A':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_A] = bases[BASE_A] + 1;
                        break;
                    case 'C':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_C] = bases[BASE_C] + 1;
                        break;
                    case 'G':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_G] = bases[BASE_G] + 1;
                        break;
                    case 'T':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_T] = bases[BASE_T] + 1;
                        break;
                    case 'N':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_N] = bases[BASE_N] + 1;
                        break;
                    case '_':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[BASE_GAP] = bases[BASE_GAP] + 1;
                        break;
                }

            }
            for (int i = 0; i < gaps.size(); i++) {
                ParsedReferenceGap gap = gaps.get(i);
                long positionInt = gap.getAbsPos();
                String position = String.valueOf(positionInt);
                char base = gap.getBase();
                int order = gap.getOrder();
                if (s.getDirection() == -1) {
                    base = SequenceUtils.complementDNA(base);
                }
                char value = 'a';
                for (int j = 0; j <= order; j++) {
                    if (value > 'a'){
                        position = position.substring(0,position.length()-2);
                    }
                    position = position + "_" +  value;
                    value ++;
                }

                // init positionTable if not done yet
                if (!positionTable.containsKey(position)) {
                    Integer[] bases = new Integer[12];
                    for (int j = 0; j < bases.length; j++) {
                        bases[j] = 0;
                    }
                    positionTable.put(position, bases);
                }

                // increase occurence of bases at position
                Integer[] bases = positionTable.get(position);
                switch (base) {
                    case 'A':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[GAP_A] = bases[GAP_A] + 1;
                        break;
                    case 'C':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[GAP_C] = bases[GAP_C] + 1;
                        break;
                    case 'G':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[GAP_G] = bases[GAP_G] + 1;
                        break;
                    case 'T':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[GAP_T] = bases[GAP_T] + 1;
                        break;
                    case 'N':
                        bases[DIFFS] = bases[DIFFS] + 1;
                        bases[GAP_N] = bases[GAP_N] + 1;
                        break;
                }

            }
        }
    }

    public HashMap<String, Integer[]> getPositionTable() {
        return this.positionTable;
    }
    
    public boolean positionCovered(int position) {
        boolean covered = false;
        if(coverage.containsKey(position)) {
            covered = true;
        }
        return covered;
    }
}
