/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.enums;


/**
 * Enum class of all table types used in this module.
 *
 * @author jritter
 */
public enum TableType {

    /**
     * table of transcription start site
     */
    TSS_TABLE,
    /**
     * table of putative novel transcripts
     */
    NOVEL_TRANSCRIPTS_TABLE,
    /**
     * table of read count calculations
     */
    RPKM_TABLE,
    /**
     * table of operons
     */
    OPERON_TABLE;

}
