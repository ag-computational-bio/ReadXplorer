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
 * TSS Detection constants.
 * @author Oliver Schwengers
 * <oliver.schwengers@computational.bio.uni-giessen.de>
 */
public final class TSSConstants {

    private TSSConstants() {}

    
    // tss analysis mapping classes
    public static final String TSS_MAPPING_CLASSES = "tss.mapping-classes";
    // tss strand usage
    public static final String TSS_STRAND_USAGE = "tss.strand-usage";
    // tss analysis min base quality
    public static final String TSS_MIN_MAPPING_QUALITY = "tss.min-mapping-quality";
    // tss automatic parameter estimation
    public static final String TSS_PARAMETER_ESTIMATION = "tss.parameter-estimation";
    // tss min total read increase
    public static final String TSS_MIN_INCREASE_TOTAL = "tss.min-increase-total";
    // tss min percent read increase
    public static final String TSS_MIN_INCREASE_PERCENT = "tss.min-increase-percent";
    // tss max distance between TSS and feature
    public static final String TSS_MAX_FEATURE_DISTANCE = "tss.max-feature-distance";
    // tss max distance between TSS and leaderless feature
    public static final String TSS_MAX_LEADERLESS_FEATURE_DISTANCE = "tss.max-leaderless-feature-distance";
    // tss associate tss to most significant tss
    public static final String TSS_ASSOCIATE = "tss.associate-tss";
    // tss base window for tss association
    public static final String TSS_ASSOCIATE_WINDOW = "tss.associate-tss-windows";
    // tss perform detection of not annotated transcripts
    public static final String TSS_UNANNOTATED_DETECTION = "tss.unannotated-detection";
    // tss max low coverage init
    public static final String TSS_MAX_LOW_COVERAGE_INIT = "tss.max-low-coverage-init";
    // tss min low coverage increase
    public static final String TSS_MIN_LOW_COVERAGE_INCREASE = "tss.min-low-coverage-increase";
    // tss min transcript extension coverage
    public static final String TSS_MIN_TRANSCRIPT_EXTENSION_COVERAGE = "tss.min-transcript-extension-coverage";

}
