package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class NewRegionDetection {

    private List<NovelRegion> novelRegions;
    private String refSeq;

    public NewRegionDetection(String refSeq) {
        this.novelRegions = new ArrayList<>();
        this.refSeq = refSeq;
    }

    public void runningNewRegionsDetection(int length, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash,
            int[] fwdCoverage, int[] revCoverage, int[] forward, int[] reverse, double bg, int minLengthBoundary, int trackID) {


        // Key is flag and Value the count of this flag
        HashMap<Integer, Integer> dropdownsFwd = new HashMap<>();
        HashMap<Integer, Integer> dropdownsRev = new HashMap<>();
        NovelRegion newRegion = null;

        for (int i = 0; i < length; i++) {

            int fwd_readstarts = forward[i];
            if (fwd_readstarts > bg) { // got through possible forward hits first
                int j = 0;
                int end = 0;

                // check if the hits can be attributed to a region (up to 700bp downstream)
                while (!forwardCDSs.containsKey(i + j - end)) {
                    if (j > 700) {
                        break;
                    }
                    if ((i + j) > length) {
                        end = length;
                    }
                    j++;
                }
                if (!forwardCDSs.containsKey(i + j - end)) {
//	    # if the count crosses the threshold far from a gene
                    int k = 0;
//		# search for the drop off
                    while (fwdCoverage[i + k - end] > bg) {
                        k++;
                    }
                    int start = i;
                    int flag = i + k - end;
                    if (dropdownsFwd.containsKey(flag)) {
                        dropdownsFwd.put(flag, dropdownsFwd.get(flag) + 1);
                    } else {
                        dropdownsFwd.put(flag, 1);
                    }
//                        newRegion = new NovelRegion(true, flag, j, trackID);
//		new_regs{fwd}{flag}++;
                    int possibleStop = flag + 1;
//		# and report the likely transcript
                    String site = "intergenic";
                    if (reverseCDSs.containsKey(start) || reverseCDSs.containsKey(possibleStop)) {
                        site = "cis-antisense";
                    }

                    int lengthOfNewRegion = flag - i;
                    if (dropdownsFwd.get(flag) == 1 && lengthOfNewRegion >= minLengthBoundary) {
//                            push(@{$new_regs{out}{$start}}, "$pos\t+\t$fwd\t$site") unless ($new_regs{fwd}{$flag} > 1);
                        newRegion = new NovelRegion(true, start, possibleStop, site, (possibleStop - start), getSubSeq(true, start, possibleStop), false, false, trackID);
                        System.out.println(newRegion.toString());
                        novelRegions.add(newRegion);
                    }
                }
            }
// #############################################################################

            int rev_readstarts = reverse[i];
            if (rev_readstarts > bg) {
                int j = 0;
                int end = 0;
                while (!reverseCDSs.containsKey(end + i - j)) {
                    if (j > 700) {
                        break;
                    }
                    if ((i - j) == 0) {
                        end = length;
                    }
                    j++;
                }
                if (!reverseCDSs.containsKey(end + i - j)) {
                    if (rev_readstarts > bg) {
                        int k = 0;
                        while (revCoverage[end + i - k] > bg) {
                            k++;
                        }
                        int start = i + 1;
                        int flag = end + i - k;
//                      $new_regs{rev}{flag}++;
                        if (dropdownsRev.containsKey(flag)) {
                            dropdownsRev.put(flag, dropdownsRev.get(flag) + 1);
                        } else {
                            dropdownsRev.put(flag, 1);
                        }
                        int possibleStop = flag + 1;
                        String site = "intergenic";
                        if (forwardCDSs.containsKey(start) || forwardCDSs.containsKey(possibleStop)) {
                            site = "cis-antisense";
                        }
                        int lengthOfNewRegion = i - flag;
                        if (dropdownsRev.get(flag) == 1 && lengthOfNewRegion >= minLengthBoundary) { // unless ($new_regs{rev}{$flag} > 1) {
//                          push(@{$new_regs{out}{$start}}, "$pos\t-\t$rev\t$site");
                            String reversedSeq = new StringBuffer(getSubSeq(false, possibleStop, start)).reverse().toString();
                            String revComplement = getComplement(reversedSeq);
                            newRegion = new NovelRegion(false, start, possibleStop, site, (start - possibleStop), revComplement, false, false, trackID);
                            novelRegions.add(newRegion);
                        }
                    }
                }
            }
        }
        System.out.println(
                "The genome is running out of length!");
    }

    public List<NovelRegion> getNovelRegions() {
        return novelRegions;
    }

    public void setNovelRegions(List<NovelRegion> novelRegions) {
        this.novelRegions = novelRegions;
    }

    /**
     * If the direction is reverse, the subsequence will be inverted.
     *
     * @param isFwd direction of sequence.
     * @param start start of subsequence.
     * @param stop stop of subsequence.
     * @return the subsequence.
     */
    private String getSubSeq(boolean isFwd, int start, int stop) {

        String seq = "";
        if (start > 0 && stop < this.refSeq.length()) {
            seq = this.refSeq.substring(start, stop);
        }
        if (isFwd) {
            return seq;
        } else {
            String reversedSeq = new StringBuffer(seq).reverse().toString();
            return reversedSeq;
        }
    }

    /**
     * Gets a DNA String and complement it. A to T, T to A, G to C and C to G.
     *
     * @param seq is DNA String.
     * @return the compliment of seq.
     */
    private String getComplement(String seq) {
        char BASE_A = 'A';
        char BASE_C = 'C';
        char BASE_G = 'G';
        char BASE_T = 'T';
        String a = "A";
        String c = "C";
        String g = "G";
        String t = "T";
        String compliment = "";

        for (int i = 0; i < seq.length(); i++) {
            if (BASE_A == seq.charAt(i)) {
                compliment = compliment.concat(t);
            } else if (BASE_C == (seq.charAt(i))) {
                compliment = compliment.concat(g);

            } else if (BASE_G == seq.charAt(i)) {
                compliment = compliment.concat(c);

            } else if (BASE_T == seq.charAt(i)) {
                compliment = compliment.concat(a);
            }
        }

        return compliment;

    }
}