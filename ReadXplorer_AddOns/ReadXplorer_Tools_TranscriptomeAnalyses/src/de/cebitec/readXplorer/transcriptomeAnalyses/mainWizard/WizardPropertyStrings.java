/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

/**
 * This class only contains static property strings.
 *
 * @author jritter
 */
public class WizardPropertyStrings {

    public static final String PROP_WIZARD_NAME = "Transcriptome Analyses";
    // DATA type selection strings
    public static final String PROP_FIVEPRIME_DATASET = "5'-enriched data set";
    public static final String PROP_WHOLEGENOME_DATASET = "whole length transcripts data set";
    // 5'-enriched data set analysis
    public static final String PROP_TSS_ANALYSIS = "Transcriptions start site analysis";

    // Whole genome data set analyses
    public static final String PROP_NOVEL_ANALYSIS = "Novel transcript analysis";
    public static final String PROP_OPERON_ANALYSIS = "Operon analysis";
    public static final String PROP_RPKM_ANALYSIS = "RPKM analysis";

    // Novel transcript detection
    public static final String PROP_MIN_LENGTH_OF_NOVEL_TRANSCRIPT = "Minimum length of new transcript in nucleotides (nt)";

    // Transcription start site detection 
    public static final String PROP_Fraction = "Fraction (used for background calculation, #FP)";
    public static final String PROP_MANAULLY_MIN_STACK_SIZE = "Minimum number of required mapped reads.";
    public static final String PROP_SET_MANAULLY_MIN_STACK_SIZE = "Manually set stack size of mapped reads.";
    public static final String PROP_RATIO = "Ratio";

    public static final String PROP_EXCLUDE_INTERNAL_TSS = "excludeInternalTss";
    public static final String PROP_UTR_LIMIT = "excludeTssDistance";
    public static final String PROP_KEEP_ALL_INTRAGENIC_TSS = "keeping intragenic TSS with or whitout assigning to next downstream feature";
    public static final String PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT = "keepingInternalDistance";
    public static final String PROP_KEEP_ONLY_ITRAGENIC_TSS_ASSIGNED_TO_FEATURE = "keeping only intragenic TSS which can be assigned to the next downstream feature";

    public static final String PROP_INCLUDE_BEST_MATCHED_READS_TSS = "Include best matched reads into analysis";
    public static final String PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION = "as3'-UTR detection with maximal distance to feature stop of";
    public static final String PROP_LEADERLESS_LIMIT = "leaderlessLimit";
    public static final String PROP_LEADERLESS_CDSSHIFT = "cdsShiftChoosen";
    public static final String PROP_PERCENTAGE_FOR_CDS_ANALYSIS = "percentage for cds-shift analysis";
    public static final String PROP_VALID_START_CODONS = "Include these start codons in analysis";

    // Operon detectino
    public static final String PROP_INCLUDE_BEST_MATCHED_READS_OP = "Include best matched reads into operon detection";

    // Novel transcript detection
    public static final String PROP_INCLUDE_BEST_MATCHED_READS_NR = "Include best matched reads into novel region analysis";
    public static final String PROP_RATIO_NOVELREGION_DETECTION = "Increase ratio value as additional threshold";
    public static final String PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION = "Inclusion of ratio value for novel region detection";

    // RPKM detection
    public static final String PROP_INCLUDE_BEST_MATCHED_READS_RPKM = "Include best matched reads into RPKM analysis";
    public static final String PROP_NORMAL_RPKM_ANALYSIS = "normalRPKMs";
    public static final String PROP_REFERENCE_FILE_RPKM_DETERMINATION = "Reference file with static start and stop positions";
}
