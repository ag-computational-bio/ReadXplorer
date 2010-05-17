package vamp;

import java.awt.Color;

/**
 *
 * @author ddoppmeier
 */
public class ColorProperties{

    // track panel specific colors
    public static Color BEST_MATCH = new Color(244, 225, 49);
    public static Color N_ERROR_COLOR = new Color(180, 41, 36);
    public static Color PERFECT_MATCH = new Color(38, 180, 36);

    // global colors
    public static Color TRACKPANEL_SCALE_LINES = new Color(153, 153, 153);
    public static Color TRACKPANEL_MIDDLE_LINE = new Color(25, 25, 25);
    public static Color MOUSEOVER = new Color(127, 127, 127);
    public static Color CURRENT_POSITION = new Color(127, 127, 127);
    public static Color START_CODON = new Color(255, 0, 0, 80);
    public static Color TITLE_BACKGROUND = Color.gray;
    public static Color LEGEND_BACKGROUND = Color.white;

    // genome viewer specific colors
    public static Color FEATURE_LABEL = new Color(0, 0, 0);
    public static Color SELECTED_FEATURE = new Color(73, 182, 241);
    public static Color CDS = new Color(253, 196, 0);
    public static Color GENE = new Color(253, 196, 0);
    public static Color MRNA = new Color(253, 79, 0);
    public static Color MI_RNA = new Color(253, 79, 0);
    public static Color MISC_RNA = new Color(207, 8, 70);
    public static Color REPEAT_UNIT = new Color(166, 8, 207);
    public static Color RRNA = new Color(50, 8, 207);
    public static Color SOURCE = new Color(8, 109, 207);
    public static Color TRNA = new Color(8, 207, 178);
    public static Color UNDEF_FEATURE = new Color(0, 255, 77);

    // seq logo viewer specific colors
    public static Color LOGO_MATCH = PERFECT_MATCH;
    public static Color LOGO_A = new Color(22, 0, 255);
    public static Color LOGO_C = new Color(176, 4, 0);
    public static Color LOGO_G = new Color(37, 201, 213);
    public static Color LOGO_T = new Color(247, 229, 63);
    public static Color LOGO_N = new Color(155, 42, 170);
    public static Color LOGO_READGAP = new Color(178, 178, 178);
    public static Color LOGO_BASE_UNDEF = UNDEF_FEATURE;

    // alignment viewer specific colors
    public static Color BRICK_LABEL = new Color(0, 0, 0);
    public static Color BLOCK_MATCH = Color.GREEN;
    public static Color BLOCK_BEST_MATCH = Color.YELLOW;
    public static Color BLOCK_N_ERROR = Color.RED;
    public static Color BLOCK_BORDER = new Color(0, 0, 0);
    public static Color ALIGNMENT_A = new Color(255, 255, 255);
    public static Color ALIGNMENT_C = new Color(255, 255, 255);
    public static Color ALIGNMENT_G = new Color(255, 255, 255);
    public static Color ALIGNMENT_T = new Color(255, 255, 255);
    public static Color ALIGNMENT_N = new Color(255, 255, 255);
    public static Color ALIGNMENT_FOREIGN_GENOMEGAP = Color.LIGHT_GRAY;
    public static Color ALIGNMENT_BASE_UNDEF = UNDEF_FEATURE;
    public static Color ALIGNMENT_BASE_READGAP = new Color(255, 255, 255);
 

}
