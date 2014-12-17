/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.featuretableexport;


/**
 * Qualifier /ncRNA_class=
 * <p>
 * Definition a structured description of the classification of the
 * non-coding RNA described by the ncRNA parent key Value format "TYPE"
 * Example: /ncRNA_class="miRNA"
 *              /ncRNA_class="siRNA"
 *              /ncRNA_class="scRNA"
 * Comment: TYPE is a term taken from the INSDC controlled
 * vocabulary for ncRNA classes (http://www.insdc.org/rna_vocab.html); on
 * 15-Oct-2013, the following terms were valid:
 *
 * "antisense_RNA"
 * "autocatalytically_spliced_intron"
 * "ribozyme"
 * "hammerhead_ribozyme"
 * "lncRNA"
 * "RNase_P_RNA"
 * "RNase_MRP_RNA"
 * "telomerase_RNA"
 * "guide_RNA"
 * "rasiRNA"
 * "scRNA"
 * "siRNA"
 * "miRNA"
 * "piRNA"
 * "snoRNA"
 * "snRNA"
 * "SRP_RNA"
 * "vault_RNA"
 * "Y_RNA"
 * "other"
 *
 * ncRNA classes not yet in the INSDC /ncRNA_class controlled vocabulary can be
 * annotated by entering '/ncRNA_class="other"' with '/note="[brief explanation
 * of novel ncRNA_class]"';
 *
 * @author jritter
 */
public enum NcRnaClassType {

    ANTISENSE_RNA,
    AUTOCATALYTICALLY_SPLICED_INTRONS,
    RIBOZYME,
    HAMMERHEAD_RIBOZYME,
    LNC_RNA,
    RNASE_P_RNA,
    RNASE_MRP_RNA,
    TELOMERASE_RNA,
    GUIDE_RNA,
    RASI_RNA,
    SC_RNA,
    SI_RNA,
    MI_RNA,
    PI_RNA,
    SNO_RNA,
    SN_RNA,
    SRP_RNA,
    VAULT_RNA,
    Y_RNA,
    OTHER;

}
