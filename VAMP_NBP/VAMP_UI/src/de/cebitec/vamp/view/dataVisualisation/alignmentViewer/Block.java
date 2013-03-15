package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A block contains detailed information about one PersistantMapping.
 * 
 * @author ddoppmei, Rolf Hilker
 */
public class Block implements BlockI {

    private int absStart;
    private int absStop;
    private PersistantMapping mapping;
    private GenomeGapManager gapManager;
    private ArrayList<Brick> bricks;

    /**
     * A block contains detailed information about one PersistantMapping.
     * @param absStart start of the block (might be larger than start of mapping)
     * @param absStop stop of the block (might be smaller than stop of mapping)
     * @param mapping mapping whose detailed information is needed
     * @param gapManager gap manager of the mapping
     */
    public Block(int absStart, int absStop, PersistantMapping mapping, GenomeGapManager gapManager) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.mapping = mapping;
        this.gapManager = gapManager;
        this.bricks = new ArrayList<Brick>();
        this.createBricks();
    }

    /**
     * Each position in the block gets one brick.
     */
    private void createBricks() {
        for (int i = absStart; i <= absStop; i++) {
            if ((mapping.getStart()>i) || (i>mapping.getStop())) {
                Brick trimmedBrick = new Brick(Brick.TRIMMED);
                bricks.add(trimmedBrick);
            }
            else {
                if (gapManager.hasGapAt(i)) {
                    if (mapping.hasGenomeGapAtPosition(i)) {
                        this.fillWithOwnGenomeGaps(mapping, i);
                    } else {
                        this.fillWithForeignGaps(gapManager.getNumOfGapsAt(i));
                    }
                }
                this.addDiffOrMatchBrick(mapping, i);
            }
        }
    }

    private void fillWithOwnGenomeGaps(PersistantMapping mapping, int position) {
        // do not only paint one gap, but ALL of them
        for (Iterator<PersistantReferenceGap> it = mapping.getGenomeGapsAtPosition(position).iterator(); it.hasNext();) {
            PersistantReferenceGap gap = it.next();
            int type = this.determineGapType(gap.getBase());
            bricks.add(new Brick(type));
        }
    }
    
    private void fillWithForeignGaps(int numberOfForeignGaps) {
        int type;
        for (int x = 0; x < numberOfForeignGaps; x++) {
            type = Brick.FOREIGN_GENOMEGAP;
            bricks.add(new Brick(type));
        }
    }

    private void addDiffOrMatchBrick(PersistantMapping mapping, int position) {
        int type;
        if (mapping.hasDiffAtPosition(position)) {
            type = determineDiffType(mapping.getDiffAtPosition(position));
        } else {
            type = Brick.MATCH;
        }
        bricks.add(new Brick(type));
    }

    private int determineDiffType(char c) {
        int type;
        switch (c) {
            case 'A' : type = Brick.BASE_A; break;
            case 'C' : type = Brick.BASE_C; break;
            case 'G' : type = Brick.BASE_G; break;
            case 'T' : type = Brick.BASE_T; break;
            case 'N' : type = Brick.BASE_N; break;
            case '_' : type = Brick.READGAP; break;
            case '.' : type = Brick.SKIPPED; break;
            default  : type = Brick.UNDEF;
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", c);
        }
        return type;
    }

    private int determineGapType(char c){
        int type;
        switch (c) {
            case 'A' : type = Brick.GENOME_GAP_A; break;
            case 'C' : type = Brick.GENOME_GAP_C; break;
            case 'G' : type = Brick.GENOME_GAP_G; break;
            case 'T' : type = Brick.GENOME_GAP_T; break;
            case 'N' : type = Brick.GENOME_GAP_N; break;
            default  : type = Brick.UNDEF;
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", c);
        }
        return type;
    }

    @Override
    public int getAbsStart() {
        return absStart;
    }

    @Override
    public int getAbsStop() {
        return absStop;
    }

    @Override
    public PersistantMapping getPersistantObject() {
        return mapping;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Brick b : bricks) {
            sb.append(b);
        }
        return sb.toString();
    }

    @Override
    public Iterator<Brick> getBrickIterator() {
        return bricks.iterator();
    }

    @Override
    public int getNumOfBricks() {
        return bricks.size();
    }

}
