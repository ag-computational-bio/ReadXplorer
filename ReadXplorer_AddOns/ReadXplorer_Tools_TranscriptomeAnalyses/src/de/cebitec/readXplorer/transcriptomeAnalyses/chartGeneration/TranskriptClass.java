/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration;

/**
 * This Enum class lists all possible transcript classes.
 *
 * @author jritter
 */
public enum TranskriptClass {
    LEADERLESS, // leaderless transcript
    INTRAGENIC, // intragenic transcript
    ANTISENSE, // antisense transcript
    WITH_UTR, // native transcript with a 5'-UTR of length > 0
}
