package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
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

    private int type;
    private char causeForGenomeGap;

    public Brick(int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public void setCauseForGenomeGap(char c){
        causeForGenomeGap = c;
    }

    public char getCauseForGenomeGap(){
        return causeForGenomeGap;
    }

    @Override
    public String toString(){
        if(type == Brick.BASE_A){
            return "A";
        } else if(type == Brick.BASE_C){
            return "C";
        } else if(type == Brick.BASE_G){
            return "G";
        } else if(type == Brick.BASE_T){
            return "T";
        } else if(type == Brick.BASE_N){
            return "N";
        } else if(type == Brick.FOREIGN_GENOMEGAP){
            return "-";
        } else if(type == Brick.MATCH){
            return "#";
        } else if(type == Brick.UNDEF){
            return "@";
        } else if(type == Brick.GENOME_GAP_A){
            return "A";
        } else if(type == Brick.GENOME_GAP_C){
            return "C";
        } else if(type == Brick.GENOME_GAP_G){
            return "G";
        } else if(type == Brick.GENOME_GAP_T){
            return "T";
        } else if(type == Brick.GENOME_GAP_N){
            return "N";
        } else if(type == Brick.READGAP){
            return "-";
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
            return "@";
        }
    }

}
