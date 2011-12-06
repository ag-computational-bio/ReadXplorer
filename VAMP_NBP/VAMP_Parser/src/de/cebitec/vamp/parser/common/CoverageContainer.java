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
    /** 
     * Snp table: Each position in the array stores how many occurences of a certain base where seen at that position.
     * The index in the array is defined by the constants below. 
     */
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

    private final int coverageArrayLength;

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
    

    public void computeCoverage(ParsedMappingContainer mappings){
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start computing the coverage");
        Iterator<Integer> sequenceIDIt = mappings.getMappedSequenceIDs().iterator();

        // add all mappings to their mapping groups
        ParsedMappingGroup group;
        Iterator<ParsedMapping> mappingIt;
        while (sequenceIDIt.hasNext()) {
            group = mappings.getParsedMappingGroupBySeqID(sequenceIDIt.next());
            mappingIt = group.getMappings().iterator();
            while (mappingIt.hasNext()) {
                ParsedMapping mapping = mappingIt.next();
                this.addMapping(mapping);
                this.savePositions(mapping);
            }
        }
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished computing the coverage");
    }

    private void addMapping(ParsedMapping mapping) {
        
        // store best mapping coverage
        if (mapping.isBestMapping()) {
            this.increaseCoverage(mapping, BEST_MAPPING_CASE);
        }
        // store zero error coverage
        if (!mapping.hasDiffs()) {
            this.increaseCoverage(mapping, ZERO_ERROR_CASE);
        }
        // store n error coverage
        this.increaseCoverage(mapping, N_ERROR_CASE);

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

    public int getNumberOfBestMappingsForward(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, true, false);
    }

    public int getBestMappingReverseCoverage(int position) {
        return this.getCoverageOfType(position, BEST_MAPPING_CASE, false, true);
    }

    public int getNumberOfBestMappingsReverse(int position) {
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

    /**
     * Clears the coverage container.
     */
    public void clearCoverageContainer() {
        coverage.clear();
        positionTable.clear();
    }
    
    /**
     * Updates the position information for a new mapping in this container.
     * @param mapping the mapping whose position count should be stored.
     */
    public void savePositions(ParsedMapping mapping) {

        if (mapping.isBestMapping()) {
            List<ParsedDiff> diffs = mapping.getDiffs();
            List<ParsedReferenceGap> gaps = mapping.getGenomeGaps();

            ParsedDiff diff;
            ParsedReferenceGap gap;
            long positionInt;
            String position;
            char base;

            // saves diffs
            for (int i = 0; i < mapping.getNumOfDiffs(); i++) {
                diff = diffs.get(i);
                positionInt = diff.getPosition();
                position = String.valueOf(positionInt);
                base = diff.getBase();
                if (mapping.getDirection() == -1) {
                    base = SequenceUtils.getDnaComplement(base, "");
                }
                // init positionTable if not done yet 
                Integer[] bases;
                if (!positionTable.containsKey(position)) {
                    bases = new Integer[12];
                    for (int j = 0; j < bases.length; ++j) {
                        bases[j] = 0;
                    }
                    positionTable.put(position, bases);
                }
                bases = positionTable.get(position);

                // increase occurence of bases at position
                switch (base) {
                    case 'A':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_A] += mapping.getCount();
                        break;
                    case 'C':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_C] += mapping.getCount();
                        break;
                    case 'G':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_G] += mapping.getCount();
                        break;
                    case 'T':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_T] += mapping.getCount();
                        break;
                    case 'N':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_N] += mapping.getCount();
                        break;
                    case '_':
                        bases[DIFFS] += mapping.getCount();
                        bases[BASE_GAP] += mapping.getCount();
                        break;
                }
            }

            //save gaps
            for (int i = 0; i < gaps.size(); i++) {
                gap = gaps.get(i);
                positionInt = gap.getAbsPos();
                position = String.valueOf(positionInt);
                base = gap.getBase();
                int order = gap.getOrder();
                if (mapping.getDirection() == -1) {
                    base = SequenceUtils.getDnaComplement(base, "");
                }
                char value = 'a';
                for (int j = 0; j <= order; j++) {
                    if (value > 'a') {
                        position = position.substring(0, position.length() - 2);
                    }
                    position = position + "_" + value;
                    value++;
                }

                // init positionTable if not done yet 
//                String posString = String.valueOf(positionInt);
                Integer[] bases;
                if (!positionTable.containsKey(position)) {
                    bases = new Integer[12];
                    for (int j = 0; j < bases.length; ++j) {
                        bases[j] = 0;
                    }
                    positionTable.put(position, bases);
                }

                // increase occurence of gap bases at position
                bases = positionTable.get(position);

                switch (base) { //724960
                    case 'A':
                        bases[DIFFS] += mapping.getCount();
                        bases[GAP_A] += mapping.getCount();
                        break;
                    case 'C':
                        bases[DIFFS] += mapping.getCount();
                        bases[GAP_C] += mapping.getCount();
                        break;
                    case 'G':
                        bases[DIFFS] += mapping.getCount();
                        bases[GAP_G] += mapping.getCount();
                        break;
                    case 'T':
                        bases[DIFFS] += mapping.getCount();
                        bases[GAP_T] += mapping.getCount();
                        break;
                    case 'N':
                        bases[DIFFS] += mapping.getCount();
                        bases[GAP_N] += mapping.getCount();
                        break;
                }
            }
        }
    }

    public HashMap<String, Integer[]> getPositionTable() {
        return this.positionTable;
    }
    
    /**
     * @param position the current position to check
     * @return true, if the current position is covered by mappings, false otherwise.
     */
    public boolean positionCovered(int position) {
        return coverage.containsKey(position);
    }

}
