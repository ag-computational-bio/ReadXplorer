package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;

/**
 *
 * @author ddoppmei
 */
public class Block implements BlockI{

    private int absStart;
    private int absStop;
    private PersistantMapping mapping;
    private GenomeGapManager gapManager;
    private ArrayList<Brick> bricks;

    public Block(int absStart, int absStop, PersistantMapping mapping, GenomeGapManager gapManager){
        this.absStart = absStart;
        this.absStop = absStop;
        this.mapping = mapping;
        this.gapManager = gapManager;
        bricks = new ArrayList<Brick>();
        this.createBricks();
    }

    private void createBricks(){
        for(int i = absStart; i<= absStop; i++){
            if(gapManager.hasGapAt(i)){
                    if(mapping.hasGenomeGapAtPosition(i)){
                        this.fillWithOwnGenomeGaps(mapping, i);
                        this.addDiffOrMatchBrick(mapping, i);
                    } else {
                        this.fillWithForeignGaps(gapManager.getNumOfGapsAt(i));
                        this.addDiffOrMatchBrick(mapping, i);
                    }
            } else {
                this.addDiffOrMatchBrick(mapping, i);
            }
        }
    }

    private void fillWithOwnGenomeGaps(PersistantMapping mapping, int position){
        // do not only paint one gap, but ALL of them
        for(Iterator<PersistantReferenceGap> it = mapping.getGenomeGapsAtPosition(position).iterator(); it.hasNext(); ){
            PersistantReferenceGap gap = it.next();
            int type = this.determineGapType(gap.getBase());
            bricks.add(new Brick(type));
        }
    }
    
    private void fillWithForeignGaps(int numberOfForeignGaps){
        int type;
        for(int x = 0; x < numberOfForeignGaps; x++){
            type = Brick.FOREIGN_GENOMEGAP;
            bricks.add(new Brick(type));
        }
    }

    private void addDiffOrMatchBrick(PersistantMapping mapping, int position){
            int type;
            if(mapping.hasDiffAtPosition(position)){
                type = determineDiffType(mapping.getDiffAtPosition(position));
            } else {
                type = Brick.MATCH;
            }
            bricks.add(new Brick(type));
    }



    private int determineDiffType(char c){
        int type;
        if       (c == 'A' ){
            type = Brick.BASE_A;
        } else if(c == 'C'){
            type = Brick.BASE_C;
        } else if(c == 'G'){
            type = Brick.BASE_G;
        } else if(c == 'T'){
            type = Brick.BASE_T;
        } else if(c == 'N'){
            type = Brick.BASE_N;
        } else if(c == '_'){
            type = Brick.READGAP;
        } else {
            type = Brick.UNDEF;
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type "+c);
        }
        return type;
    }

    private int determineGapType(char c){
        int type;
        if(c == 'A'){
            type = Brick.GENOME_GAP_A;
        } else if(c == 'C'){
            type = Brick.GENOME_GAP_C;
        } else if(c == 'G'){
            type = Brick.GENOME_GAP_G;
        } else if(c == 'T'){
            type = Brick.GENOME_GAP_T;
        } else if(c == 'N'){
            type = Brick.GENOME_GAP_N;
        } else {
            type = Brick.UNDEF;
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type "+c);
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
    public PersistantMapping getMapping(){
        return mapping;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Brick b : bricks){
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
