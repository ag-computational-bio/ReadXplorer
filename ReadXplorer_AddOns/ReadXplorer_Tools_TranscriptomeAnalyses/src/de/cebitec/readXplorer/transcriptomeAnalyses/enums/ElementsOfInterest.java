/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.enums;

/**
 *
 * @author jritter
 */
public enum ElementsOfInterest {
    ONLY_SELECTED_FOR_UPSTREAM_ANALYSES,
    ONLY_LEADERLESS_TRANSCRIPTS,
    ONLY_ANTISENSE_TSS,
    ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS,
    ONLY_TSS_WITH_UTR_INCLUDING_ANTISENSE_LEADERLESS,
    ALL;
}
