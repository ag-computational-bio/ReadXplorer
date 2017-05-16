/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.api.constants;


import java.awt.Color;


/**
 * Class containing all general colors of ReadXplorer.
 * <p>
 * @author ddoppmeier, rhilker
 */
public final class Colors {

    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private Colors() {
    }


    /**
     * 0.3f = General minimum saturation and brightness for colored elements in
     * ReadXplorer.
     */
    public static final float MIN_SATURATION_AND_BRIGTHNESS = 0.4f;
    /**
     * 0.9f = General maximum saturation and brightness for colored elements in
     * ReadXplorer.
     */
    public static final float MAX_SATURATION_AND_BRIGTHNESS = 1f;
    /**
     * The span between general max and min saturation and brightness for
     * colored elements in ReadXplorer.
     */
    public static final float SPAN_SATURATION_AND_BRIGTHNESS = MAX_SATURATION_AND_BRIGTHNESS - MIN_SATURATION_AND_BRIGTHNESS;

    // read mapping class specific colors
    /**
     * Standard color for common match reads = red.
     */
    public static final Color COMMON_MATCH = new Color( 180, 41, 36 );
    /**
     * Standard color for best match reads = yellow.
     */
    public static final Color BEST_MATCH = new Color( 255, 250, 28 );
    /**
     * Standard color for single best match reads = light yellow.
     */
    public static final Color BEST_MATCH_SINGLE = new Color( 238, 216, 13 );
    /**
     * Standard color for perfect match reads = green.
     */
    public static final Color PERFECT_MATCH = new Color( 115, 226, 112 );
    /**
     * Standard color for single perfect match reads = light green.
     */
    public static final Color PERFECT_MATCH_SINGLE = new Color( 38, 180, 36 );
    // double track panel specific colors
    /**
     * Standard color for coverage difference of two tracks = blue.
     */
    public static final Color COV_DIFF_COLOR = Color.BLUE;
    /**
     * Standard color for the first track in the double track viewer = cyan.
     */
    public static final Color TRACK1_COLOR = Color.cyan;
    /**
     * Standard color for the second track in the double track viewer = orange.
     */
    public static final Color TRACK2_COLOR = new Color( 255, 117, 48 );

    /** Property string for ReadXplorer's background color. */
    public static final String BACKGROUND_COLOR_STRING = "backgroundColor";
    /** Property string for the perfect match color. */
    public static final String PERFECT_MATCH_STRING = "perfectMatchColor";
    /** Property string for the best match color. */
    public static final String BEST_MATCH_STRING = "bestMatchColor";
    /** Property string for the common match color. */
    public static final String COMMON_MATCH_STRING = "commonMatchColor";
    /** Property string for the single perfect match color. */
    public static final String SINGLE_PERFECT_MATCH_STRING = "singlePerfectMatchColor";
    /** Property string for the single best match color. */
    public static final String SINGLE_BEST_MATCH_STRING = "singleBestMatchColor";
    /** Property string for uniform color. */
    public static final String UNIFORM_COLOR_STRING = "uniformColor";
    /** Property string to address if uniform coloring is desired. */
    public static final String UNIFORM_DESIRED = "uniformDesired";
    /** Property string for the coverage difference color of two tracks. */
    public static final String COV_DIFF_STRING = "covDiffColor";
    /** Property string for the first track color in the double track viewer. */
    public static final String TRACK1_COLOR_STRING = "track1Color";
    /** Property string for the first track color in the double track viewer. */
    public static final String TRACK2_COLOR_STRING = "track2Color";


    // global colors
    /** Global background color of ReadXplorer. */
    public static final Color BACKGROUND_COLOR = new Color( 240, 240, 240 ); //to prevent wrong color on mac
    public static final Color TRACKPANEL_SCALE_LINES = new Color( 153, 153, 153 );
    public static final Color TRACKPANEL_MIDDLE_LINE = new Color( 25, 25, 25 );
    public static final Color MOUSEOVER = new Color( 127, 127, 127 );
    public static final Color CURRENT_POSITION = new Color( 127, 127, 127 );
    public static final Color START_CODON = new Color( 39, 116, 116, 80 );
    public static final Color STOP_CODON = new Color( 255, 0, 0, 80 );
    public static final Color PATTERN = new Color( 12, 37, 211, 80 );
    public static final Color TITLE_BACKGROUND = Color.gray;
    public static final Color LEGEND_BACKGROUND = Color.white;
    public static final Color HIGHLIGHT_FILL = new Color( 168, 202, 236, 75 );
    public static final Color HIGHLIGHT_BORDER = new Color( 51, 153, 255 );
    // genome viewer specific colors
    public static final Color FEATURE_LABEL = Color.BLACK;
    public static final Color SELECTED_FEATURE = new Color( 73, 182, 241 ); //light blue
    public static final Color CDS = new Color( 253, 196, 0 ); //orange
    public static final Color GENE = CDS;
    public static final Color MRNA = new Color( 253, 79, 0 ); //light red
    public static final Color MI_RNA = MRNA;
    public static final Color MISC_RNA = new Color( 207, 8, 70 ); //red/violet
    public static final Color REPEAT_UNIT = new Color( 166, 8, 207 ); //purple
    public static final Color RRNA = new Color( 50, 8, 207 ); //blue
    public static final Color SOURCE = new Color( 8, 109, 207 ); //light blue 2
    public static final Color TRNA = new Color( 8, 207, 178 ); //cyan-green
    public static final Color EXON = new Color( 38, 145, 44 ); //green
    public static final Color EXON_BORDER = new Color( 45, 45, 45 ); //dark grey
    public static final Color NC_RNA = MI_RNA;
    public static final Color FIVE_UTR = new Color( 64, 122, 122 ); //dark cyan
    public static final Color THREE_UTR = FIVE_UTR;
    public static final Color RBS = new Color( 185, 122, 87 ); //brown
    public static final Color MINUS_THIRTYFIVE = new Color( 0, 98, 49 ); //dark green
    public static final Color MINUS_TEN = MINUS_THIRTYFIVE;
    public static final Color TRANSCRIPT = new Color(128, 128, 64); //dirty green
    public static final Color UNDEF_FEATURE = Color.GREEN;
    // histogram viewer specific colors
    public static final Color LOGO_MATCH = Color.GREEN;
    public static final Color LOGO_A = new Color( 22, 0, 255 );
    public static final Color LOGO_C = new Color( 176, 4, 0 );
    public static final Color LOGO_G = new Color( 37, 201, 213 );
    public static final Color LOGO_T = new Color( 247, 229, 63 );
    public static final Color LOGO_N = new Color( 155, 42, 170 );
    public static final Color LOGO_READGAP = new Color( 178, 178, 178 );
    public static final Color LOGO_BASE_UNDEF = UNDEF_FEATURE;
    //histogram viewer specific colors for base background
    public static final Color BACKGROUND_A = new Color( 22, 0, 255, 100 );
    public static final Color BACKGROUND_C = new Color( 176, 4, 0, 100 );
    public static final Color BACKGROUND_G = new Color( 37, 201, 213, 100 );
    public static final Color BACKGROUND_T = new Color( 247, 229, 63, 100 );
    public static final Color BACKGROUND_N = new Color( 155, 42, 170, 100 );
    public static final Color BACKGROUND_READGAP = new Color( 178, 178, 178, 100 );
    public static final Color BACKGROUND_BASE_UNDEF = UNDEF_FEATURE;
    // alignment viewer specific colors
    public static final Color BRICK_LABEL = new Color( 0, 0, 0 );
    public static final Color BLOCK_MATCH = Color.GREEN;
    public static final Color BLOCK_BEST_MATCH = Color.YELLOW;
    public static final Color BLOCK_N_ERROR = Color.RED;
    public static final Color BLOCK_BORDER = new Color( 0, 0, 0 );
    public static final Color MISMATCH_BACKGROUND = Color.white;
    public static final Color ALIGNMENT_FOREIGN_GENOMEGAP = Color.LIGHT_GRAY;
    public static final Color TRIMMED = Color.GRAY;
    public static final Color SKIPPED = Color.GRAY;

    // read pair specific colors
    public static final Color BLOCK_PERFECT = Color.GREEN;
    public static final Color BLOCK_DIST_LARGE = Color.YELLOW;
    public static final Color BLOCK_DIST_SMALL = Color.YELLOW;
    public static final Color BLOCK_ORIENT_WRONG = Color.YELLOW;
    public static final Color BLOCK_OR_DIST_LARGE = Color.YELLOW;
    public static final Color BLOCK_OR_DIST_SMALL = Color.YELLOW;
    public static final Color BLOCK_UNPAIRED = Color.RED;
    /**
     * Background color for alignment blocks = Dark gray.
     */
    public static final Color BLOCK_BACKGROUND = new Color( 200, 200, 200 );

}
