/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.cli.constants;


/**
 * SNP Detection constants.
 * @author Oliver Schwengers
 * <oliver.schwengers@computational.bio.uni-giessen.de>
 */
public final class SNPConstants {

    private SNPConstants() {}

    
    // snp analysis count main bases
    public static final String SNP_COUNT_MAIN_BASES = "snp.count-main-bases";
    // snp analysis feature types
    public static final String SNP_FEATURE_TYPES = "snp.feature-types";
    // snp analysis mapping classes
    public static final String SNP_MAPPING_CLASSES = "snp.mapping-classes";
    // snp analysis min base quality
    public static final String SNP_MIN_MAPPING_QUALITY = "snp.min-mapping-quality";
    // snp analysis min base quality
    public static final String SNP_MIN_BASE_QUALITY = "snp.min-base-quality";
    // snp analysis min amount of mismatch bases
    public static final String SNP_MIN_MISMATCH_BASES = "snp.min-mismatch-bases";
    // snp analysis min percental variation
    public static final String SNP_MIN_VARIATION = "snp.min-variation";
    // snp analysis min average base quality
    public static final String SNP_MIN_AVERAGE_BASE_QUALITY = "snp.min-average-base-quality";
    // snp analysis min average mapping quality
    public static final String SNP_MIN_AVERAGE_MAPPING_QUALITY = "snp.min-average-mapping-quality";

}
