/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;

/**
 *
 * @author jritter
 */
public enum ParsingPrefisSuffix {
//    Legende
//Tag_xXXXXXy
//x: g = gene
//    o = operon
//    c = CDS
//    m = mRNA
//    t = tRNA
//    r = rRNA
//    n = ncRNA
//    p = promotor
//    s = RBS
//    a = attenuator
//    i = terminator
//    u = UTR

    GENE,
    OPERON,
    CDS,
    mRNA,
    tRNA,
    rRNA,
    ncRNA,
    PROMOTER,
    RBS,
    ATTENUATOR,
    TERMINATOR,
    UTR,
    NONE;
}
