/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.enums;


/**
 * Properties for filtering a TranscriptionStart List.
 *
 * @author jritter
 */
public enum FilterType {

    READSTARTS,
    SHIFTS_IN_TSS_POS,
    MULTIPLE,
    SINGLE,
    ONLY_LEADERLESS,
    ONLY_ANTISENSE,
    ONLY_INTERGENIC,
    ONLY_INTRAGENIC,
    ONLY_FALSE_POSITIVES,
    FINISHED_TAGGED,
    UPSTREMA_ANALYSIS_TAGGED,
    STABLE_RNA,
    FIVE_PRIME_ANTISENSE,
    THREE_PRIME_ANTISENSE,
    INTRAGENIC_ANTISENSE,
    ONLY_NON_STABLE_RNA;

}
