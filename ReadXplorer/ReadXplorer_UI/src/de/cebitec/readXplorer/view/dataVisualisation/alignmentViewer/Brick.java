package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A brick represents one base or gap of a DNA sequence and stores its type.
 * 
 * @author ddoppmeier, rhilker
 */
public class Brick {

    public final static int READGAP = 1;
    public final static int FOREIGN_GENOMEGAP = 3;
    public final static int BASE_N = 4;
    public final static int BASE_A = 5;
    public final static int BASE_G = 6;
    public final static int BASE_C = 7;
    public final static int BASE_T = 8;
    public final static int MATCH = 9;
    public final static int GENOME_GAP_N = 10;
    public final static int GENOME_GAP_A = 11;
    public final static int GENOME_GAP_G = 12;
    public final static int GENOME_GAP_C = 13;
    public final static int GENOME_GAP_T = 14;
    public final static int UNDEF = 15;
    public final static int SKIPPED = 16;
    public final static int TRIMMED = 17;

    private int type;

    /**
     * A brick represents one base or gap of a dna sequence and stores its type.
     * @param type type of the base or gap
     */
    public Brick(int type){
        this.type = type;
    }

    /**
     * @return the integer type of this brick, which represents a certain base.
     */
    public int getType(){
        return type;
    }

    @Override
    public String toString(){
        switch (type) {
            case Brick.MATCH : return "#";
            case Brick.BASE_A : return "A";
            case Brick.BASE_C : return "C";    
            case Brick.BASE_G : return "G";
            case Brick.BASE_T : return "T";
            case Brick.BASE_N : return "N";
            case Brick.FOREIGN_GENOMEGAP : return "-";
            case Brick.GENOME_GAP_A : return "A";
            case Brick.GENOME_GAP_C : return "C";
            case Brick.GENOME_GAP_G : return "G";
            case Brick.GENOME_GAP_T : return "T";
            case Brick.GENOME_GAP_N : return "N";  
            case Brick.READGAP : return "-";
            case Brick.UNDEF : return "@";
            case Brick.SKIPPED : return ".";
            case Brick.TRIMMED : return "|";
            default:
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
                return "@";
        }
    }

}
