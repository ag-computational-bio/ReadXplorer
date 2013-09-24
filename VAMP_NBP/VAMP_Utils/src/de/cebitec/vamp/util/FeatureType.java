package de.cebitec.vamp.util;

/**
 * Enumeration for all different features types used in vamp. This does not only
 * include genetic features, but also features displayed in viewers.
 * Each type is created with an integer and a string representation and
 * it can return values for both. 
 * It also allows to return the feature type represented by a given integer
 * or string.
 * 
 *@author ddoppmeier, rhilker
 */
public enum FeatureType {
        
    /** getType() returns '-1' = To be used if feature type does not matter. */
    ANY(FeatureType.ANY_INT, FeatureType.ANY_STRING),
    /** getType() returns '0' = To be used for a feature with unknown type. */
    UNDEFINED(FeatureType.UNDEFINED_INT, FeatureType.UNDEFINED_STRING),
    /** getType() returns '1' = To be used for mRNAs. */
    MRNA(FeatureType.MRNA_INT, FeatureType.MRNA_STRING),
    /** getType() returns '2' = To be used for coding sequences. */
    CDS(FeatureType.CDS_INT, FeatureType.CDS_STRING),
    /** getType() returns '3' = To be used for misc rnas. */
    MISC_RNA(FeatureType.MISC_RNA_INT, FeatureType.MISC_RNA_STRING),
    /** getType() returns '4' = To be used for rRNAs. */
    RRNA(FeatureType.RRNA_INT, FeatureType.RRNA_STRING),
    /** getType() returns '5' = To be used for repeat units. */
    REPEAT_UNIT(FeatureType.REPEAT_UNIT_INT, FeatureType.REPEAT_UNIT_STRING),
    /** getType() returns '6' = To be used for sources. */
    SOURCE(FeatureType.SOURCE_INT, FeatureType.SOURCE_STRING),
    /** getType() returns '7' = To be used for tRNAs. */
    TRNA(FeatureType.TRNA_INT, FeatureType.TRNA_STRING),
    /** getType() returns '8' = To be used for genes. */
    GENE(FeatureType.GENE_INT, FeatureType.GENE_STRING),
    /** getType() returns '9' = To be used for micro RNAs. */
    MIRNA(FeatureType.MIRNA_INT, FeatureType.MIRNA_STRING), 
    /** getType() returns '10' = To be used for exons. */
    EXON(FeatureType.EXON_INT, FeatureType.EXON_STRING),

    //feature types for the track viewer
    PERFECT_COVERAGE(FeatureType.PERFECT_COVERAGE_INT, FeatureType.PERFECT_COVERAGE_STRING),
    BEST_MATCH_COVERAGE(FeatureType.BEST_MATCH_COVERAGE_INT, FeatureType.BEST_MATCH_COVERAGE_STRING),
    COMPLETE_COV(FeatureType.COMPLETE_COV_INT, FeatureType.COMPLETE_COV_STRING),
    
    //feature types for the double track viewer
    COMPLETE_COVERAGE(FeatureType.COMPLETE_COVERAGE_INT, FeatureType.COMPLETE_COVERAGE_STRING),
    TRACK1_COVERAGE(FeatureType.TRACK1_COVERAGE_INT, FeatureType.TRACK1_COVERAGE_STRING),
    TRACK2_COVERAGE(FeatureType.TRACK2_COVERAGE_INT, FeatureType.TRACK2_COVERAGE_STRING),
    
    //feature types for the histogram viewer
    BASE_A(FeatureType.BASE_A_INT, FeatureType.BASE_A_STRING),
    BASE_C(FeatureType.BASE_C_INT, FeatureType.BASE_C_STRING),
    BASE_G(FeatureType.BASE_G_INT, FeatureType.BASE_G_STRING),
    BASE_T(FeatureType.BASE_T_INT, FeatureType.BASE_T_STRING),
    BASE_N(FeatureType.BASE_N_INT, FeatureType.BASE_N_STRING),
    MATCH(FeatureType.MATCH_INT, FeatureType.MATCH_STRING),
    GAP(FeatureType.GAP_INT, FeatureType.GAP_STRING),
    
    //feature types for the alignment viewer
    PERFECT_MATCH(FeatureType.PERFECT_MATCH_INT, FeatureType.PERFECT_MATCH_STRING),
    BEST_MATCH(FeatureType.BEST_MATCH_INT, FeatureType.BEST_MATCH_STRING),
    ORDINARY_MATCH(FeatureType.ORDINARY_MATCH_INT, FeatureType.ORDINARY_MATCH_STRING),
    DIFF(FeatureType.DIFF_INT, FeatureType.DIFF_STRING),
    NONUNIQUE(FeatureType.NONUNIQUE_INT, FeatureType.NONUNIQUE_STRING),
    
    //feature types for the sequence pair viewer
    PERFECT_PAIR(FeatureType.PERFECT_PAIR_INT, FeatureType.PERFECT_PAIR_STRING),
    DISTORTED_PAIR(FeatureType.DISTORTED_PAIR_INT, FeatureType.DISTORTED_PAIR_STRING),
    SINGLE_MAPPING(FeatureType.SINGLE_MAPPING_INT, FeatureType.SINGLE_MAPPING_STRING);

    //feature types supported by the reference viewer
    private static final int ANY_INT = -1;
    private static final int UNDEFINED_INT = 0;
    private static final int MRNA_INT = 1;
    private static final int CDS_INT = 2;
    private static final int MISC_RNA_INT = 3;
    private static final int RRNA_INT = 4;
    private static final int REPEAT_UNIT_INT = 5;
    private static final int SOURCE_INT = 6;
    private static final int TRNA_INT = 7;
    private static final int GENE_INT = 8;
    private static final int MIRNA_INT = 9;
    private static final int EXON_INT = 10;
    
    //feature types for the track viewer
    private static final int PERFECT_COVERAGE_INT = 11;
    private static final int BEST_MATCH_COVERAGE_INT = 12;
    private static final int COMPLETE_COV_INT = 13;
    
    //feature types for the double track viewer
    private static final int COMPLETE_COVERAGE_INT = 14;
    private static final int TRACK1_COVERAGE_INT = 15;
    private static final int TRACK2_COVERAGE_INT = 16;
    
    //feature types for the histogram viewer
    private static final int BASE_A_INT = 17;
    private static final int BASE_C_INT = 18;
    private static final int BASE_G_INT = 19;
    private static final int BASE_T_INT = 20;
    private static final int BASE_N_INT = 21;
    private static final int MATCH_INT = 22;
    private static final int GAP_INT = 23;
    
    //feature types for the alignment viewer
    private static final int PERFECT_MATCH_INT = 24;
    private static final int BEST_MATCH_INT = 25;
    private static final int ORDINARY_MATCH_INT = 26;
    private static final int DIFF_INT = 27;
    private static final int NONUNIQUE_INT = 31;
    
    //feature types for the sequence pair viewer
    private static final int PERFECT_PAIR_INT = 28;
    private static final int DISTORTED_PAIR_INT = 29;
    private static final int SINGLE_MAPPING_INT = 30;
    
    
    
    //feature types supported by the reference viewer
    private static final String ANY_STRING = "any";
    private static final String UNDEFINED_STRING = "unknown";
    private static final String MRNA_STRING = "mRNA";
    private static final String CDS_STRING = "CDS";
    /** ORF string is only used for input. It cannot be returned. */
    private static final String ORF_STRING = "ORF";
    private static final String MISC_RNA_STRING = "misc RNA";
    /** misc RNA string with underscore "misc_RNA". */
    private static final String MISC_RNA_STRING_USCORE = "misc_RNA";
    private static final String RRNA_STRING = "rRNA";
    private static final String REPEAT_UNIT_STRING = "Repeat unit";
    /** Repeat unit string with underscore "repeat_unit". */
    private static final String REPEAT_UNIT_STRING_USCORE = "Repeat_unit";
    private static final String SOURCE_STRING = "Source";
    private static final String TRNA_STRING = "tRNA";
    private static final String GENE_STRING = "Gene";
    private static final String MIRNA_STRING = "miRNA";
    private static final String EXON_STRING = "Exon";
    
    //feature types for the track viewer
    private static final String PERFECT_COVERAGE_STRING = "Perfect match cov.";
    private static final String BEST_MATCH_COVERAGE_STRING = "Best match cov.";
    private static final String COMPLETE_COV_STRING = "Complete cov.";
    
    //feature types for the double track viewer
    private static final String COMPLETE_COVERAGE_STRING = "Difference between both";
    private static final String TRACK1_COVERAGE_STRING = "Track 1 coverage";
    private static final String TRACK2_COVERAGE_STRING = "Track 2 coverage";
    
    //feature types for the histogram viewer
    private static final String BASE_A_STRING = "A";
    private static final String BASE_C_STRING = "C";
    private static final String BASE_G_STRING = "G";
    private static final String BASE_T_STRING = "T";
    private static final String BASE_N_STRING = "N";
    private static final String MATCH_STRING = "Match";
    private static final String GAP_STRING = "Gap in read";
    
    //feature types for the alignment viewer
    private static final String PERFECT_MATCH_STRING = "Perfect Match";
    private static final String BEST_MATCH_STRING = "Best Match";
    private static final String ORDINARY_MATCH_STRING = "Ordinary Match";
    private static final String DIFF_STRING = "Diff.";
    private static final String NONUNIQUE_STRING = "Non unique Match";
    
    //feature types for the sequence pair viewer
    private static final String PERFECT_PAIR_STRING = "Perfect seq. pair";
    private static final String DISTORTED_PAIR_STRING = "Distorted seq. pair";
    private static final String SINGLE_MAPPING_STRING = "Single mapping";
    
    private int typeInt;
    private String typeString;
    
    /**
     * FeatureTypes that are GUI selectable.
     */
    public static final FeatureType[] SELECTABLE_FEATURE_TYPES = {GENE, CDS, EXON, UNDEFINED, MRNA, MISC_RNA, RRNA, REPEAT_UNIT, SOURCE, TRNA, MIRNA};
   
    private FeatureType(int typeInt, String typeString) {
        this.typeInt = typeInt;
        this.typeString = typeString;
    }

    /**
     * @return the string representation of the current feature type.
     */
    public String getTypeString() {
        return this.typeString;
    }
    
    /**
     * @return the integer value of the type of the current feature.
     */
    public int getTypeInt(){
        return this.typeInt;
    }
    
    /**
     * @return the desired FeatureType for a given integer between 0 and 9.
     * @param type the type of FeatureType to return. If the type is larger than 9
     * FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType(int type){
        switch (type) { 
            case ANY_INT:           return ANY;
            case MRNA_INT:          return MRNA;
            case CDS_INT:           return CDS;
            case MISC_RNA_INT:      return MISC_RNA;
            case RRNA_INT:          return RRNA;
            case REPEAT_UNIT_INT:   return REPEAT_UNIT;
            case SOURCE_INT:        return SOURCE;
            case TRNA_INT:          return TRNA;
            case GENE_INT:          return GENE;
            case MIRNA_INT:         return MIRNA;
            case EXON_INT:          return EXON;
            default:                return UNDEFINED;
        }
    }
    
    /**
     * @return the desired FeatureType for a given type string.
     * @param type the type of FeatureType to return. If the type is unknown
     * FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType(String type) {
        FeatureType featType;
        if (type.equalsIgnoreCase(UNDEFINED_STRING)) {
            featType = UNDEFINED;
        } else if (type.equalsIgnoreCase(MRNA_STRING)) {
            featType = MRNA;
        } else if (type.equalsIgnoreCase(CDS_STRING)) {
            featType = CDS;
        } else if (type.equalsIgnoreCase(ORF_STRING)) {
            featType = CDS;
        } else if (type.equalsIgnoreCase(MISC_RNA_STRING) || type.equalsIgnoreCase(MISC_RNA_STRING_USCORE)) {
            featType = MISC_RNA;
        } else if (type.equalsIgnoreCase(RRNA_STRING)) {
            featType = RRNA;
        } else if (type.equalsIgnoreCase(REPEAT_UNIT_STRING) || type.equalsIgnoreCase(REPEAT_UNIT_STRING_USCORE)) {
            featType = REPEAT_UNIT;
        }  else if (type.equalsIgnoreCase(TRNA_STRING)) {
            featType = TRNA;
        } else if (type.equalsIgnoreCase(GENE_STRING)) {
            featType = GENE;
        } else if (type.equalsIgnoreCase(MIRNA_STRING)) {
            featType = MIRNA;
        } else if (type.equalsIgnoreCase(EXON_STRING)) {
            featType = EXON;
        } else if (type.equalsIgnoreCase(SOURCE_STRING)) {
            featType = SOURCE;
        } else if (type.equalsIgnoreCase(ANY_STRING)) {
            featType = ANY;
        } else {
            featType = UNDEFINED;
        }
        
        return featType;
    }
    
    @Override
    public String toString(){
        return this.getTypeString();
    }
    
}
