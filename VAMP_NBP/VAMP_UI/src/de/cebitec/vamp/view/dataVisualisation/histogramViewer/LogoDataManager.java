package de.cebitec.vamp.view.dataVisualisation.histogramViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for all diffs in a specific interval defined by absStart and width.
 * After setting the coverage for each position and adding all diffs and gaps,
 * it can return the count of each DNA base or gap for each position in the
 * interval.
 *
 * @author ddoppmeier, rhilker
 */
public class LogoDataManager {

    private static final int NO_BASE_TYPES = 7;
    private static final int NO_BASE_FIELDS = NO_BASE_TYPES * 2;
    private static final int MATCH = 0;
    private static final int A = 1;
    private static final int C = 2;
    private static final int G = 3;
    private static final int T = 4;
    private static final int N = 5;
    private static final int READGAP = 6;
    
    private Integer[][] counts; //array for the positions in the interval and all 7 base types (fwd and rev = 14)
    private int absStart;
    private int stop;
    private int width;
    private int upperCutoff;
    private int maxFoundCoverage;

    /**
     * Manager for all diffs in a specific interval defined by absStart and
     * width. After setting the coverage for each position and adding all diffs
     * and gaps, it can return the count of each DNA base or gap for each
     * position in the interval.
     * @param absStart start position of the currently viewed interval
     * @param width length of the currently viewed interval
     */
    public LogoDataManager(int absStart, int width) {
        this.absStart = absStart;
        this.stop = absStart + width - 1;
        this.width = width;

        if (this.width < 1) {
            this.width = 1;
            this.stop = absStart;
        }
        this.upperCutoff = this.stop;
        this.maxFoundCoverage = 0;

        counts = new Integer[this.width][NO_BASE_FIELDS];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < NO_BASE_FIELDS; j++) {
                counts[i][j] = 0;
            }
        }
    }

    
    public int getNumOfMatchesAt(int position, boolean forwardStrand) {
        int row = MATCH;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfAAt(int position, boolean forwardStrand) {
        int row = A;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfCAt(int position, boolean forwardStrand) {
        int row = C;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfGAt(int position, boolean forwardStrand) {
        int row = G;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfTAt(int position, boolean forwardStrand) {
        int row = T;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfNAt(int position, boolean forwardStrand) {
        int row = N;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    public int getNumOfReadGapsAt(int position, boolean forwardStrand) {
        int row = READGAP;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        return counts[position][row];
    }

    /**
     * Sets the coverage value for a matching base to the reference genome.
     * @param position position whose coverage will be stored now
     * @param value match coverage value to store
     * @param forwardStrand true, if this is a coverage value for the fwd strand
     */
    public void setCoverageAt(int position, int value, boolean forwardStrand) {
        int row = MATCH;
        if (!forwardStrand) {
            row += NO_BASE_TYPES;
        }
        position -= absStart;
        maxFoundCoverage = (value > maxFoundCoverage ? value : maxFoundCoverage);
        counts[position][row] = value;
    }

    /**
     * If a base differs from the reference genome, add it to this manager.
     * @param diff diff to add
     * @param position position at which the diff occurs
     */
    public void addExtendedPersistantDiff(PersistantDiff diff, int position) {
        char base = diff.getBase();
        int row = 0;
        switch (base) {
            case 'A': row = A; break;
            case 'C': row = C; break;
            case 'G': row = G; break;
            case 'T': row = T; break;
            case 'N': row = N; break;
            case '_': row = READGAP; break; 
            default:
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown base {0}", base);
        }

        int column = MATCH;
        if (!diff.isForwardStrand()) {
            row += NO_BASE_TYPES;
            column += NO_BASE_TYPES;
        }

        int count = diff.getCount();
        position -= absStart;

        // increase counts for current base
        counts[position][row] += count;

        // decrease match coverage
        counts[position][column] -= count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("width: ").append(width).append(", height: ").append(NO_BASE_FIELDS).append("\n");

        for (int i = 0; i < width; i++) {
            int pos = absStart + i;
            sb.append("pos: ").append(pos).append("\t");
            for (int j = 0; j < NO_BASE_FIELDS; j++) {
                sb.append(counts[i][j]).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void addGaps(Collection<PersistantReferenceGap> gaps, GenomeGapManager gapManager) {
        for (Iterator<PersistantReferenceGap> it = gaps.iterator(); it.hasNext();) {
            PersistantReferenceGap gap = it.next();
            int origPos = gap.getPosition();
            
            if (origPos > this.absStart && origPos < this.stop) {

                char base = gap.getBase();
                int count = gap.getCount();
                int shiftedPos = origPos + gapManager.getNumOfGapsSmaller(origPos);
                shiftedPos += gap.getOrder();

                // gaps have been loaded with original bounds
                // so, some of them may be outside of the interval, this LogoDataManager
                // manages. Skip those gaps
                if (shiftedPos > upperCutoff) {
                    continue;
                }
                shiftedPos -= absStart;

                int row = 0;
                switch (base) {
                    case 'A': row = A; break;
                    case 'C': row = C; break;
                    case 'G': row = G; break;
                    case 'T': row = T; break;
                    case 'N': row = N; break;
                    default:
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown base {0}", base);
                }

                if (!gap.isForwardStrand()) {
                    row += NO_BASE_TYPES;
                }

                counts[shiftedPos][row] += count;
            
            } else if (origPos > this.stop) {
                break;
            }
        }
    }

    public int getMaxFoundCoverage() {
        return maxFoundCoverage;
    }
}
