package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Antisense;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelRegion;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Observer;
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
    private List<NovelRegion> detectedPutativeNewRegions;
    private List<Antisense> detectedPutativeAntisenseTSS;
    private int trackid;
    private List<Integer[]> fwdOffsets, revOffsets;
    private HashMap<Integer, Boolean> fwdTss, revTss;

    public TssDetection(String referenceSequence, int trackID) {
        this.referenceSequence = referenceSequence;
        this.detectedTSS = new ArrayList<>();
        this.detectedPutativeAntisenseTSS = new ArrayList<>();
        this.detectedPutativeNewRegions = new ArrayList<>();
        this.trackid = trackID;
        this.fwdOffsets = new ArrayList<>();
        this.revOffsets = new ArrayList<>();
        this.fwdTss = new HashMap<>();
        this.revTss = new HashMap<>();

    }

    /**
     *
     * @param length
     * @param forwardCDSs
     * @param reverseCDSs
     * @param allRegionsInHash
     * @param stats
     * @param parameters
     */
    public void runningTSSDetection(int length, HashMap<Integer, List<Integer>> forwardCDSs,
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, PersistantFeature> allRegionsInHash, Statistics stats, ParameterSetFiveEnrichedAnalyses parameters) {

        int ratio = parameters.getRatio();
        int up = parameters.getUpstreamRegion();
        int down = parameters.getDownstreamRegion();
        int leaderlessRestirction = parameters.getLeaderlessLimit();
        boolean isExclusionOfInternalTss = parameters.isExclusionOfInternalTSS();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();
        int keepingInternalTssDistance = parameters.getKeepingInternalTssDistance();
        int[] forward = stats.getForward(); // Array with startsite count information for forward mapping positions.
        int[] reverse = stats.getReverse(); // Array with startsite count information for reverse mapping positions.
        int[] fwdCov = stats.getFwdCoverage(); // Array with coverage counts of mappings in forward direction.
        int[] revCov = stats.getRevCoverage(); // Array with coverage counts of mappings in reverse direction.
        double mm = stats.getMm(); // Mappings per Million.
        double bg = stats.getBg(); // Background cutoff

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

                    double rel_count = forward[i] / mm;

                    PersistantFeature feature = allRegionsInHash.get(forwardCDSs.get(i + offset - end).get(0));
                    int dist2start = 0;
                    int dist2stop = 0;
                    boolean leaderless = false;
                    boolean cdsShift = false;
                    boolean isFwd = true;
                    boolean isInternal = false;
                    boolean isPutAntisense = false;
                    int startSubSeq = i - up;
                    int stopSubSeq = i + down;
                    int nextOffset = 0;
                    String startCodon = "";
                    String stopCodon = "";


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
                        if (dist2start < leaderlessRestirction) {
                            leaderless = true;
                            isInternal = false;
                            startCodon = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                            stopCodon = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());

                            // check antisensness

                            if (reverseCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }


                            // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                            // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                            // (dist2start % 3) == 0 checks if in frame
                            if (dist2start > 0 && (dist2start % 3) == 0) {
                                String startAtTSS = getSubSeq(isFwd, i - 1, i + 2);
                                if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                    cdsShift = true;
                                }
                            }

                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);

                            if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                detectedTSS.add(tss);
                            }
                        } else if (dist2start > leaderlessRestirction && isExclusionOfInternalTss == false) { // internal mapping or TSS for nextGene
                            // here we want to find the next gene because the startsite is inbetween a gene which is not a leaderles gene
                            int currentFeatureID = feature.getId();
                            isInternal = true;
                            int j = 1;
                            PersistantFeature nextFeature = allRegionsInHash.get(currentFeatureID + j);
                            while (feature.getLocus().equals(nextFeature.getLocus())) {
                                j++;
                                nextFeature = allRegionsInHash.get(currentFeatureID + j);
                            }
                            nextOffset = nextFeature.getStart() - i;

                            // check antisensness

                            if (reverseCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }

                            if (nextOffset < keepingInternalTssDistance) {
                                // putative the corresponding gene for TSS
                                if (nextFeature.isFwdStrand()) {
                                    startCodon = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    stopCodon = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, null, offset, dist2start, dist2stop, nextFeature, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                                    if (!nextFeature.getType().equals(FeatureType.RRNA) && !nextFeature.getType().equals(FeatureType.TRNA)) {
                                        detectedTSS.add(tss);
                                    }
                                }
                            }
                        }
                    } else {

                        // leaderless in upstream direction
                        if (offset < leaderlessRestirction) {
                            // check antisensness

                            if (reverseCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }

                            // check for cdsShift when offset > 0 and distance2Start == 0 and mod 3 == 0
                            // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                            // (dist2start % 3) == 0 checks if in frame
                            if (offset > 0 && (offset % 3) == 0) {
                                String startAtTSS = getSubSeq(isFwd, i - 1, i + 2);
                                if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                    cdsShift = true;
                                }
                            }
                            leaderless = true;
                            startCodon = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                            stopCodon = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                            if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                detectedTSS.add(tss);
                            }
                        } else {
                            if (offset < distanceForExcludingTss) {
                                // check antisensness

                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                }
                                startCodon = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                stopCodon = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, getSubSeq(isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                                if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                    detectedTSS.add(tss);
                                    fwdOffsets.add(new Integer[]{i, i + offset});
                                    fwdTss.put(i, false);
                                }
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

                    double rel_count = reverse[i] / mm;

                    PersistantFeature feature = allRegionsInHash.get(reverseCDSs.get(end + i - offset).get(0));
                    int dist2start = 0;
                    int dist2stop = 0;
                    boolean leaderless = false;
                    boolean cdsShift = false;
                    boolean isFwd = false;
                    boolean isInternal = false;
                    boolean isPutAntisense = false;
                    int startSubSeq = i - down;
                    int stopSubSeq = i + up;
                    int nextOffset = 0;
                    String startCodon = "";
                    String stopCodon = "";

                    if (offset == 0) {
                        dist2start = feature.getStop() - i;
                        dist2stop = i - feature.getStart();

                        // check if leaderless (downstream)
                        if (dist2start < leaderlessRestirction) {
                            // check antisensness

                            if (forwardCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }
                            leaderless = true;
//                           
                            String startCodonRev = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                            String stopCodonRev = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                            startCodon = Complement(startCodonRev);
                            stopCodon = Complement(stopCodonRev);
                            String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                            String revComplement = Complement(reversedSeq);

                            // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                            // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                            // (dist2start % 3) == 0 checks if in frame
                            if (dist2start > 0 && (dist2start % 3) == 0) {
                                String startAtTSSRev = getSubSeq(isFwd, i - 3, i);
                                String complement = Complement(startAtTSSRev);
                                if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                    cdsShift = true;
                                }
                            }

                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                            if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                detectedTSS.add(tss);
                            }
                        }

                        if (dist2start > leaderlessRestirction && isExclusionOfInternalTss == false) {

                            int currentFeatureID = feature.getId();
                            int j = 1;
                            PersistantFeature nextFeature = allRegionsInHash.get(currentFeatureID - j);
                            while (feature.getLocus().equals(nextFeature.getLocus())) {
                                j++;
                                nextFeature = allRegionsInHash.get(currentFeatureID - j);
                            }
                            nextOffset = i - nextFeature.getStop();

                            // check antisensness
                            if (forwardCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }
                            if (nextOffset < keepingInternalTssDistance) {
                                // puttative nextgene
                                if (!nextFeature.isFwdStrand()) {
                                    String startCodonRev = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                                    String stopCodonRev = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    startCodon = Complement(startCodonRev);
                                    stopCodon = Complement(stopCodonRev);
                                    String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                    String revComplement = Complement(reversedSeq);
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, null, offset, dist2start, dist2stop, nextFeature, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                                    if (!nextFeature.getType().equals(FeatureType.RRNA) && !nextFeature.getType().equals(FeatureType.TRNA)) {
                                        detectedTSS.add(tss);
                                    }
                                }
                            }
                        }

                    } else {
                        if (offset < leaderlessRestirction) {
                            // check antisensness
                            if (forwardCDSs.get(i) != null) {
                                isPutAntisense = true;
                            }
                            // Leaderless TSS upstream
                            leaderless = true;
//                            if (leaderless && (leaderlessInitSeqDown.equals("GTG") || leaderlessInitSeqDown.equals("CTG") || leaderlessInitSeqDown.equals("TTG"))) {
//                                cdsShift = true;
//                            }
                            String startCodonRev = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                            String stopCodonRev = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                            startCodon = Complement(startCodonRev);
                            stopCodon = Complement(stopCodonRev);
                            String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                            String revComplement = Complement(reversedSeq);

                            // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                            // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                            // (dist2start % 3) == 0 checks if in frame
                            if (dist2start > 0 && (dist2start % 3) == 0) {
                                String startAtTSSRev = getSubSeq(isFwd, i - 3, i);
                                String complement = Complement(startAtTSSRev);
                                if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                    cdsShift = true;
                                }
                            }

                            TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                            if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                detectedTSS.add(tss);
                            }
                        } else {
                            // "normal" TSS
                            if (offset < distanceForExcludingTss) {
                                // check antisensness
                                if (forwardCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                }
                                String startCodonRev = getSubSeq(isFwd, feature.getStop() - 3, feature.getStop());
                                String stopCodonRev = getSubSeq(isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                startCodon = Complement(startCodonRev);
                                stopCodon = Complement(stopCodonRev);
                                String reversedSeq = new StringBuffer(getSubSeq(isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                String revComplement = Complement(reversedSeq);
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, this.trackid);
                                if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {

                                    revOffsets.add(new Integer[]{i - offset, i});
                                    revTss.put(i, false);
                                    detectedTSS.add(tss);
                                }
                            }
                        }
                    }

                }
            }
        }

        // running additional Antisense detection
        for (int i = 0; i < fwdOffsets.size(); i++) {
            Integer[] offset = fwdOffsets.get(i);
            int j = offset[0];
            int k = offset[1];
            for (; j < k; j++) {
                if (revTss.containsKey(j)) {
                    revTss.put(j, true);
                }
            }
        }

        for (int i = 0; i < revOffsets.size(); i++) {
            Integer[] offset = revOffsets.get(i);
            int j = offset[0];
            int k = offset[1];
            for (; j < k; j++) {
                if (fwdTss.containsKey(j)) {
                    fwdTss.put(j, true);
                }
            }
        }

        for (TranscriptionStart transcriptionStart : detectedTSS) {
            int start = transcriptionStart.getStartPosition();
            boolean isFwd = transcriptionStart.isFwdStrand();
            if (isFwd) {
                if (fwdTss.containsKey(start)) {
                    if (fwdTss.get(start) == true) {
                        transcriptionStart.setPutativeAntisense(true);
                    }
                }
            } else {
                if (revTss.containsKey(start)) {
                    if (revTss.get(start) == true) {
                        transcriptionStart.setPutativeAntisense(true);
                    }
                }
            }

        }
    }

    /**
     * Gets a DNA String and complement it. A to T, T to A, G to C and C to G.
     *
     * @param seq is DNA String.
     * @return the compliment of seq.
     */
    private String Complement(String seq) {
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
        if (start > 0 && stop < referenceSequence.length()) {
            seq = this.referenceSequence.substring(start, stop);
        }
        if (isFwd) {
            return seq;
        } else {
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

    public List<NovelRegion> getDetectedPutativeNewRegions() {
        return detectedPutativeNewRegions;
    }

    public List<Antisense> getDetectedPutativeAntisenseTSS() {
        return detectedPutativeAntisenseTSS;
    }
}
