/* 
 * Copyright (C) 2014 Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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
package de.cebitec.readXplorer.util;

import java.awt.Color;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ColorProperties {
    
    /**
     * Private constructor so this utility class can not be instantiated.
     */
    private ColorProperties() {
    }

    // track panel specific colors
    public static final Color BEST_MATCH = new Color(244, 225, 49);
    public static final Color COMMON_MATCH = new Color(180, 41, 36);
    public static final Color PERFECT_MATCH = new Color(38, 180, 36);
    // double track panel specific colors
    public static final Color COV_DIFF_COLOR = Color.BLUE;
    public static final Color TRACK1_COLOR = new Color(255,117,48);
    public static final Color TRACK2_COLOR = Color.cyan;

    // global colors
    public static final Color TRACKPANEL_SCALE_LINES = new Color(153, 153, 153);
    public static final Color TRACKPANEL_MIDDLE_LINE = new Color(25, 25, 25);
    public static final Color MOUSEOVER = new Color(127, 127, 127);
    public static final Color CURRENT_POSITION = new Color(127, 127, 127);
    public static final Color START_CODON = new Color(39, 116, 116, 80);
    public static final Color STOP_CODON = new Color(255, 0, 0, 80);
    public static final Color PATTERN = new Color(12, 37, 211, 80);
    public static final Color TITLE_BACKGROUND = Color.gray;
    public static final Color LEGEND_BACKGROUND = Color.white;
    public static final Color HIGHLIGHT_FILL = new Color(168, 202, 236, 75);
    public static final Color HIGHLIGHT_BORDER = new Color(51, 153, 255);
    // genome viewer specific colors
    public static final Color FEATURE_LABEL = new Color(0, 0, 0);
    public static final Color SELECTED_FEATURE = new Color(73, 182, 241);
    public static final Color CDS = new Color(253, 196, 0);
    public static final Color GENE = new Color(253, 196, 0);
    public static final Color MRNA = new Color(253, 79, 0);
    public static final Color MI_RNA = new Color(253, 79, 0);
    public static final Color MISC_RNA = new Color(207, 8, 70);
    public static final Color REPEAT_UNIT = new Color(166, 8, 207);
    public static final Color RRNA = new Color(50, 8, 207);
    public static final Color SOURCE = new Color(8, 109, 207);
    public static final Color TRNA = new Color(8, 207, 178);
    public static final Color EXON = new Color(38, 145, 44);
    public static final Color EXON_BORDER = new Color(45, 45, 45);
    public static final Color UNDEF_FEATURE = new Color(0, 255, 77);
    // histogram viewer specific colors
    public static final Color LOGO_MATCH = Color.GREEN;
    public static final Color LOGO_A = new Color(22, 0, 255);
    public static final Color LOGO_C = new Color(176, 4, 0);
    public static final Color LOGO_G = new Color(37, 201, 213);
    public static final Color LOGO_T = new Color(247, 229, 63);
    public static final Color LOGO_N = new Color(155, 42, 170);
    public static final Color LOGO_READGAP = new Color(178, 178, 178);
    public static final Color LOGO_BASE_UNDEF = UNDEF_FEATURE;
    //histogram viewer specific colors for base background
    public static final Color BACKGROUND_A = new Color(22, 0, 255, 100);
    public static final Color BACKGROUND_C = new Color(176, 4, 0, 100);
    public static final Color BACKGROUND_G = new Color(37, 201, 213, 100);
    public static final Color BACKGROUND_T = new Color(247, 229, 63, 100);
    public static final Color BACKGROUND_N = new Color(155, 42, 170, 100);
    public static final Color BACKGROUND_READGAP = new Color(178, 178, 178, 100);
    public static final Color BACKGROUND_BASE_UNDEF = UNDEF_FEATURE;
    // alignment viewer specific colors
    public static final Color BRICK_LABEL = new Color(0, 0, 0);
    public static final Color BLOCK_MATCH = Color.GREEN;
    public static final Color BLOCK_BEST_MATCH = Color.YELLOW;
    public static final Color BLOCK_N_ERROR = Color.RED;
    public static final Color BLOCK_BORDER = new Color(0, 0, 0);
    public static final Color ALIGNMENT_A = new Color(255, 255, 255);
    public static final Color ALIGNMENT_C = new Color(255, 255, 255);
    public static final Color ALIGNMENT_G = new Color(255, 255, 255);
    public static final Color ALIGNMENT_T = new Color(255, 255, 255);
    public static final Color ALIGNMENT_N = new Color(255, 255, 255);
    public static final Color ALIGNMENT_FOREIGN_GENOMEGAP = Color.LIGHT_GRAY;
    public static final Color ALIGNMENT_BASE_READGAP = new Color(255, 255, 255);
    public static final Color ALIGNMENT_BASE_UNDEF = new Color(255, 255, 255);
    public static final Color SKIPPED = new Color(255, 255, 255);
    public static final Color TRIMMED = Color.GRAY;

    // read pair specific colors
    public static final Color BLOCK_PERFECT = Color.GREEN;
    public static final Color BLOCK_DIST_LARGE = Color.YELLOW;
    public static final Color BLOCK_DIST_SMALL = Color.YELLOW;
    public static final Color BLOCK_ORIENT_WRONG = Color.YELLOW;
    public static final Color BLOCK_OR_DIST_LARGE = Color.YELLOW;
    public static final Color BLOCK_OR_DIST_SMALL = Color.YELLOW;
    public static final Color BLOCK_UNPAIRED = Color.RED;
    /** Background color for alignment blocks = Dark gray.*/
    public static final Color BLOCK_BACKGROUND = new Color(200, 200, 200);

}
