package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.api.objects.AnalysisI;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.TranscriptionStart;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jritter
 */
public class TssDetection implements Observer, AnalysisI<List<TranscriptionStart>> {

    protected String referenceSequence;
    private List<TranscriptionStart> detectedTSS;

    public TssDetection(String referenceSequence) {
        this.referenceSequence = referenceSequence;
        this.detectedTSS = new ArrayList<>();
    }

    /**
     * Running the transcription start site detection.
     *
     * @param length Length of the reference genome.
     * @param forwardCDSs CDS information for forward regions in genome.
     * @param reverseCDSs CDS information for reverse regions in genome.
     * @param allRegionsInHash HashMap with all featureIDs and associated
     * features.
     * @param forward Array with startsite count information for forward mapping
     * positions.
     * @param reverse Array with startsite count information for reverse mapping
     * positions.
     * @param ratio User given ratio for minimum increase of start counts from
     * pos to pos + 1.
     * @param mm Mappings per Million.
     * @param bg Background cutoff
     * @param up Number of bases for sequence in upstream direction beginning
     * from TSS.
     * @param down Number of bases for sequence in downstream direction
     * beginning from TSS.
     */
    public void runningTSSDetection(int length, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash, int[] forward, int[] reverse, int[] fwdCov, int[] revCov,
            int ratio, double mm, double bg, int up, int down) {

//# now do the actual summations and adjust the gene length 
//# (this is currently done intrinsically, a method to "import"
//# gene starts from a 5'-end enriched set has yet to be implemented)

        for (int i = 0; i < length; i++) {

            if ((forward[i] > bg) || (reverse[i] > bg)) { // background cutoff is passed
                int f_before = forward[i - 1] + 1;
                int r_before = reverse[i + 1] + 1;

                int f_ratio = (forward[i] + 1) / f_before;
                int r_ratio = (reverse[i] + 1) / r_before;


                if (f_ratio >= ratio) {

                    int offset = 0;
                    int end = 0;

                    // counting the offset as long as there is no featureID
                    while (!forwardCDSs.containsKey(i + offset - end)) {
                        if ((i + offset) > length) {
                            end = length;
                        }
                        offset++;
                    }

                    // the 10 count Positions before mapping starts
                    int[] beforeCountsFwd = new int[10];
                    for (int k = 0; k > 9; k++) {
                        int count = fwdCov[i - (k + 1)];
                        beforeCountsFwd[k] = count;
                    }

                    double rel_count = forward[i] / mm;

                    // TODO: here we have with forwardCDSs.get(i + j - end).get(0) the feature ID, now we have to get the name
                    PersistantFeature feature = allRegionsInHash.get(forwardCDSs.get(i + offset - end).get(0));
                    int dist2start = 0;
                    int dist2stop = 0;
                    boolean leaderless = false;
                    boolean cdsShift = false;
                    boolean putativeUnannotated = false;
                    boolean isFwd = true;
                    int startSubSeq = i - up;
                    int stopSubSeq = i + down;
                    int nextOffset = 0;


                    // We have 4 possible cases here
                    // 1. Offset is 0 which means, that the TSS is a Leaderless (<=7bp downstream from feature start)  TSS 
                    // or 
                    // 2. TSS is an internal (>7bp downstream from feature start) TSS
                    // 3. Offset is < 7bp which is maybe a leaderless upstream
                    // 4. Offset is > 500bp, which maybe is a TSS for unannotated Transcript which have 
                    // to be blasted
                    // 
                    if (offset == 0) {
                        dist2start = i - feature.getStart();
                        dist2stop = feature.getStop() - i;

                        // check if leaderless (downstream)
                        if (dist2start < 7) {
                            leaderless = true;
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, beforeCountsFwd, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                        }

                        if (dist2start > 7 && dist2stop < 500) { // internal mapping or TSS for nextGene
                            // here we want to find the next gene because the startsite is inbetween a gene which is not a leaderles gene
                            int currentFeatureID = feature.getId();
                            PersistantFeature nextFeature = allRegionsInHash.get(currentFeatureID + 1);
                            nextOffset = nextFeature.getStart() - i;
                            
                            if (nextOffset < 500) {
                                // putative the corresponding gene for TSS
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, beforeCountsFwd, null, offset, dist2start, dist2stop, nextFeature, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, putativeUnannotated, 1);
                                detectedTSS.add(tss);
                            }
                            // TODO not yet needed!
//                            else {
//                                // TSS for putative unannotated TSS 
//                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, beforeCountsFwd, null, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, true, 1);
//                                detectedTSS.add(tss);
//                            }
                        }
                    } else {

                        // leaderless in upstream direction
                        if (offset < 7) {
                            leaderless = true;
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, beforeCountsFwd, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                        } else {
                            if(offset < 500) {
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, beforeCountsFwd, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                            } else {
                                // TODO maybe unannotated!
                            }
                        }
                    }
                }


                if (r_ratio >= ratio) {

                    int offset = 0;
                    int end = 0;

                    // counting the offset as long as there is no featureID
                    while (!reverseCDSs.containsKey(end + i - offset)) {
                        if ((i - offset) == 0) {
                            end = length;
                        }
                        offset++;
                    }

                    int[] beforeCountRev = new int[10];
                    for (int k = 0; k > 9; k++) {
                        int count = revCov[i + k + 1];
                        beforeCountRev[k] = count;
                    }

                    double rel_count = reverse[i] / mm;

                    PersistantFeature feature = allRegionsInHash.get(reverseCDSs.get(end + i - offset).get(0));
                    int dist2start = 0;
                    int dist2stop = 0;
                    boolean leaderless = false;
                    boolean cdsShift = false;
                    boolean putativeUnannotated = false;
                    boolean isFwd = false;
                    int startSubSeq = i - down;
                    int stopSubSeq = i + up;
                    int nextOffset = 0;

                    if (offset == 0) {
                        dist2start = feature.getStop() - i;
                        dist2stop = i - feature.getStart();

                        // check if leaderless (downstream)
                        if (dist2start < 7) {
                            leaderless = true;
//                            if (leaderless && (leaderlessInitSeqDown.equals("GTG") || leaderlessInitSeqDown.equals("CTG") || leaderlessInitSeqDown.equals("TTG"))) {
//                                cdsShift = true;
//                            }
                            String reversedSeq = new StringBuffer(getSubSeq(false, startSubSeq, stopSubSeq)).reverse().toString();
                            String revComplement = ReverseComplement(reversedSeq);
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, beforeCountRev, feature, offset, dist2start, dist2stop, null, 0, revComplement, leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                        }

                        if (dist2start > 7) {

                            int currentFeatureID = feature.getId();
                            PersistantFeature nextFeature = allRegionsInHash.get(currentFeatureID - 1);
                            nextOffset = i - nextFeature.getStop();

                            if (nextOffset < 500) {
                                // puttative nextgene
                                String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                String revComplement = ReverseComplement(reversedSeq);
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, beforeCountRev, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, putativeUnannotated, 1);
                                detectedTSS.add(tss);
                            } 
                            // TODO not yet needed!
//                            else {
//                                // puttative Unannotated 
//                                String reversedSeq = new StringBuffer(getSubSeq(false, startSubSeq, stopSubSeq)).reverse().toString();
//                                String revComplement = ReverseComplement(reversedSeq);
//                                TranscriptionStart tss = new TranscriptionStart(i, false, reverse[i], rel_count, beforeCountRev, feature, offset, dist2start, dist2stop, null, 0, revComplement, leaderless, cdsShift, false, 1);
//                                detectedTSS.add(tss);
//                            }
                        }

                    } else {
                        if (offset < 7) {
                            // Leaderless TSS upstream
                            leaderless = true;
//                            if (leaderless && (leaderlessInitSeqDown.equals("GTG") || leaderlessInitSeqDown.equals("CTG") || leaderlessInitSeqDown.equals("TTG"))) {
//                                cdsShift = true;
//                            }
                            String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                            String revComplement = ReverseComplement(reversedSeq);
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, beforeCountRev, feature, offset, dist2start, dist2stop, null, 0, revComplement, leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                        } else {
                            // "normal" TSS
                            if(offset < 500) {
                            String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                            String revComplement = ReverseComplement(reversedSeq);
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, beforeCountRev, feature, offset, dist2start, dist2stop, null, 0, revComplement, leaderless, cdsShift, putativeUnannotated, 1);
                            detectedTSS.add(tss);
                            } else {
                                // TODO maybe unannotated!
                            }
                        }
                    }

                }
            }
        }
    }

    private String ReverseComplement(String seq) {
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

    private String getSubSeq(boolean isFwd, int start, int stop) {
        if (isFwd) {
            String seq = "";
            if (start > 0 && stop < referenceSequence.length()) {
                seq = this.referenceSequence.substring(start, stop);
            }
            return seq;
        } else {
            String seq = "";
            if (start > 0 && stop < referenceSequence.length()) {
                seq = this.referenceSequence.substring(start, stop);
            }
            String reversedSeq = new StringBuffer(seq).reverse().toString();
            return reversedSeq;
        }
    }

    @Override
    public void update(Object args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<TranscriptionStart> getResults() {
        return detectedTSS;
    }
}
