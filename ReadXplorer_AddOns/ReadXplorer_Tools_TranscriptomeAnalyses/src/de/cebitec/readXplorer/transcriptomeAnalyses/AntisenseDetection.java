/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructure.Antisense;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class AntisenseDetection {

    private int trackID;
    private List<Antisense> antisenseTSS;

    public AntisenseDetection(int trackID) {
        this.trackID = trackID;
        this.antisenseTSS = new ArrayList<>();
    }

    public void runningAntisenseDetection(int length, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash,
            Statistics stats, ParameterSetFiveEnrichedAnalyses parameters) {

        int ratio = parameters.getRatio();
        int leaderlessRestirction = parameters.getLeaderlessLimit();
        boolean isExclusionOfInternalTss = parameters.isExclusionOfInternalTSS();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();
        int keepingInternalTssDistance = parameters.getKeepingInternalTssDistance();
        double bg = stats.getBg(); // Background cutoff
        int[] forward = stats.getForward(); // Array with startsite count information for forward mapping positions.
        int[] reverse = stats.getReverse(); // Array with startsite count information for reverse mapping positions.
        int[] fwdCov = stats.getFwdCoverage(); // Array with coverage counts of mappings in forward direction.
        int[] revCov = stats.getRevCoverage(); // Array with coverage counts of mappings in reverse direction.
        for (int i = 0; i < length; i++) {

            if ((forward[i] > bg) || (reverse[i] > bg)) { // background cutoff is passed
                int f_before = forward[i - 1] + 1;
                int r_before = reverse[i + 1] + 1;

                int f_ratio = (forward[i] + 1) / f_before;
                int r_ratio = (reverse[i] + 1) / r_before;

                if (f_ratio >= ratio) {
                }

                if (r_ratio >= ratio) {
                }
            }
        }


    }
}
