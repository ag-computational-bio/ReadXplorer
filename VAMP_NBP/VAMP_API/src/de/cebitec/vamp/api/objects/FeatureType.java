package de.cebitec.vamp.api.objects;

import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public enum FeatureType {
    
    
    
    /** getType() returns '0' = To be used for a feature with unknown type. */
    UNDEFINED(FeatureType.UNDEFINED_INT),
    /** getType() returns '1' = To be used for mRNAs. */
    M_RNA(FeatureType.M_RNA_INT),
    /** getType() returns '2' = To be used for coding sequences. */
    CDS(FeatureType.CDS_INT),
    /** getType() returns '3' = To be used for misc rnas. */
    MISC_RNA(FeatureType.MISC_RNA_INT),
    /** getType() returns '4' = To be used for rRNAs. */
    R_RNA(FeatureType.R_RNA_INT),
    /** getType() returns '5' = To be used for repeat units. */
    REPEAT_UNIT(FeatureType.REPEAT_UNIT_INT),
    /** getType() returns '6' = To be used for sources. */
    SOURCE(FeatureType.SOURCE_INT),
    /** getType() returns '7' = To be used for tRNAs. */
    T_RNA(FeatureType.T_RNA_INT),
    /** getType() returns '8' = To be used for genes. */
    GENE(FeatureType.GENE_INT),
    /** getType() returns '9' = To be used for micro RNAs. */
    MI_RNA(FeatureType.MI_RNA_INT), 
    /** getType() returns '10' = To be used for exons. */
    EXON(FeatureType.EXON_INT);

    private static final int UNDEFINED_INT = 0;
    private static final int M_RNA_INT = 1;
    private static final int CDS_INT = 2;
    private static final int MISC_RNA_INT = 3;
    private static final int R_RNA_INT = 4;
    private static final int REPEAT_UNIT_INT = 5;
    private static final int SOURCE_INT = 6;
    private static final int T_RNA_INT = 7;
    private static final int GENE_INT = 8;
    private static final int MI_RNA_INT = 9;
    private static final int EXON_INT = 10;

    private static final HashMap<FeatureType, String> map = new HashMap<FeatureType, String>() {{
          put( UNDEFINED, "unknown" );
          put( M_RNA, "mRNA" );
          put( CDS, "CDS" );
          put( MISC_RNA, "misc RNA" );
          put( R_RNA, "rRNA" );
          put( REPEAT_UNIT, "Repeat unit" );
          put( SOURCE, "Source" );
          put( T_RNA, "tRNA" );
          put( GENE, "Gene" );
          put( MI_RNA, "miRNA");
          put( EXON, "Exon");
        }};
    
    private int type;
    
    private FeatureType(int type) {
        this.type = type;
    }

    /**
     * @param type the feature type whose string representation is needed.
     * @return the string representation of the feature type.
     */
    public static String getTypeString(FeatureType type) {
        return map.get(type);
    }
    
    /**
     * @return the type of the current feature.
     */
    public int getTypeInt(){
        return type;
    }
    
    /**
     * Returns the desired FeatureType for a given integer between 0 and 9.
     * @param type the type of FeatureType to return. If the type is larger than 9
     * FeatureType.UNDEFINED is returned.
     * @return 
     */
    public static FeatureType getFeatureType(int type){
        switch (type) {
            case 0:
                return UNDEFINED;
            case 1:
                return M_RNA;
            case 2:
                return CDS;
            case 3:
                return MISC_RNA;
            case 4:
                return R_RNA;
            case 5:
                return REPEAT_UNIT;
            case 6:
                return SOURCE;
            case 7:
                return T_RNA;
            case 8:
                return GENE;
            case 9:
                return MI_RNA;
            case 10:
                return EXON;
            default:
                return UNDEFINED;
        }
    }
    
    @Override
    public String toString(){
        return getTypeString(this);
    }

}
