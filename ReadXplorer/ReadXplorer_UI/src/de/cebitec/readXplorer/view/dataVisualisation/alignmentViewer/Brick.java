/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A brick represents one base or gap of a DNA sequence and stores its typeInt.
 * 
 * @author rhilker
 */
public enum Brick {
    
    READGAP(Brick.READGAP_INT, Brick.READGAP_STRING),
    FOREIGN_GENOMEGAP(Brick.FOREIGN_GENOMEGAP_INT, Brick.FOREIGN_GENOMEGAP_STRING),
    BASE_N(Brick.BASE_N_INT, Brick.BASE_N_STRING),
    BASE_A(Brick.BASE_A_INT, Brick.BASE_A_STRING),
    BASE_G(Brick.BASE_G_INT, Brick.BASE_G_STRING),
    BASE_C(Brick.BASE_C_INT, Brick.BASE_C_STRING),
    BASE_T(Brick.BASE_T_INT, Brick.BASE_T_STRING),
    MATCH(Brick.MATCH_INT, Brick.MATCH_STRING),
    GENOMEGAP_N(Brick.GENOME_GAP_N_INT, Brick.GENOME_GAP_N_STRING),
    GENOMEGAP_A(Brick.GENOME_GAP_A_INT, Brick.GENOME_GAP_A_STRING),
    GENOMEGAP_G(Brick.GENOME_GAP_G_INT, Brick.GENOME_GAP_G_STRING),
    GENOMEGAP_C(Brick.GENOME_GAP_C_INT, Brick.GENOME_GAP_C_STRING),
    GENOMEGAP_T(Brick.GENOME_GAP_T_INT, Brick.GENOME_GAP_T_STRING),
    UNDEF(Brick.UNDEF_INT, Brick.UNDEF_STRING),
    SKIPPED(Brick.SKIPPED_INT, Brick.SKIPPED_STRING),
    TRIMMED(Brick.TRIMMED_INT, Brick.TRIMMED_STRING);

    private final static int READGAP_INT = 1;
    private final static int FOREIGN_GENOMEGAP_INT = 3;
    private final static int BASE_N_INT = 4;
    private final static int BASE_A_INT = 5;
    private final static int BASE_G_INT = 6;
    private final static int BASE_C_INT = 7;
    private final static int BASE_T_INT = 8;
    private final static int MATCH_INT = 9;
    private final static int GENOME_GAP_N_INT = 10;
    private final static int GENOME_GAP_A_INT = 11;
    private final static int GENOME_GAP_G_INT = 12;
    private final static int GENOME_GAP_C_INT = 13;
    private final static int GENOME_GAP_T_INT = 14;
    private final static int UNDEF_INT = 15;
    private final static int SKIPPED_INT = 16;
    private final static int TRIMMED_INT = 17;
    
    private final static String READGAP_STRING = "_";
    private final static String FOREIGN_GENOMEGAP_STRING = "";
    private final static String BASE_N_STRING = "N";
    private final static String BASE_A_STRING = "A";
    private final static String BASE_G_STRING = "G";
    private final static String BASE_C_STRING = "C";
    private final static String BASE_T_STRING = "T";
    private final static String MATCH_STRING = "";
    private final static String GENOME_GAP_N_STRING = "N";
    private final static String GENOME_GAP_A_STRING = "A";
    private final static String GENOME_GAP_G_STRING = "G";
    private final static String GENOME_GAP_C_STRING = "C";
    private final static String GENOME_GAP_T_STRING = "T";
    private final static String UNDEF_STRING = "@";
    private final static String SKIPPED_STRING = ".";
    private final static String TRIMMED_STRING = "⌿";

    private int typeInt;
    private String typeString;

    /**
     * A brick represents one base or gap of a dna sequence and stores its typeInt.
     * @param typeInt typeInt of the base or gap
     */
    private Brick(int typeInt, String typeString){
        this.typeInt = typeInt;
        this.typeString = typeString;
    }

    /**
     * @return the integer typeInt of this brick, which represents a certain base.
     */
    public int getType(){
        return typeInt;
    }

    /**
     * @return the string representation of the current feature type.
     */
    public String getTypeString() {
        return this.typeString;
    }

    /**
     * @return The type string of this brick.
     */
    @Override
    public String toString() {
        return this.typeString;
    }
    
    /**
     * Determines the type of a diff.
     * @param c character of the diff
     * @return The brick type of the diff
     */
    public static Brick determineDiffType(char c) {
        Brick type;
        switch (c) {
            case 'A' : type = Brick.BASE_A; break;
            case 'C' : type = Brick.BASE_C; break;
            case 'G' : type = Brick.BASE_G; break;
            case 'T' : type = Brick.BASE_T; break;
            case 'N' : type = Brick.BASE_N; break;
            case '_' : type = Brick.READGAP; break;
            case '.' : type = Brick.SKIPPED; break;
            default  : type = Brick.UNDEF;
                Logger.getLogger(Brick.class.getName()).log(Level.SEVERE, "found unknown brick type {0}", c);
        }
        return type;
    }
    
    /**
     * Determines the type of a gap.
     * @param c character of the gap
     * @return The brick type of the gap
     */
    public static Brick determineGapType(char c){
        Brick type;
        switch (c) {
            case 'A' : type = Brick.GENOMEGAP_A; break;
            case 'C' : type = Brick.GENOMEGAP_C; break;
            case 'G' : type = Brick.GENOMEGAP_G; break;
            case 'T' : type = Brick.GENOMEGAP_T; break;
            case 'N' : type = Brick.GENOMEGAP_N; break;
            default  : type = Brick.UNDEF;
                Logger.getLogger(Brick.class.getName()).log(Level.SEVERE, "found unknown brick type {0}", c);
        }
        return type;
    }
}
