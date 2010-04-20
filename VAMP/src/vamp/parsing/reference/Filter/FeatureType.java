package vamp.parsing.reference.Filter;

import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public class FeatureType {

    public static final int UNDEFINED = 0;
    public static final int M_RNA = 1;
    public static final int CDS = 2;
    public static final int MISC_RNA = 3;
    public static final int R_RNA = 4;
    public static final int REPEAT_UNIT = 5;
    public static final int SOURCE = 6;
    public static final int T_RNA = 7;
    public static final int GENE = 8;
    public static final int MI_RNA = 9;

    private static final HashMap<Integer, String> map = new HashMap<Integer, String>() {{

          put( 0, "unknown" );
          put( 1, "mRNA" );
          put( 2, "CDS" );
          put( 3, "misc RNA" );
          put( 4, "rRNA" );
          put( 5, "repeat unit" );
          put( 6, "source" );
          put( 7, "tRNA" );
          put( 8, "gene" );
          put( 9, "miRNA");
        }};

    public static String getTypeString(int type){
        return map.get(type);
    }

}
