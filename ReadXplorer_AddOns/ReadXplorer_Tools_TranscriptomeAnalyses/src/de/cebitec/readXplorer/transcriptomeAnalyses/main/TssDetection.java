package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
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

    private List<TranscriptionStart> detectedTSS;
    private List<NovelRegion> detectedPutativeNewRegions;
    private List<Antisense> detectedPutativeAntisenseTSS;
    private int trackid;
    private final PersistantReference refGenome;
    private HashMap<Integer, Boolean> fwdTss, revTss;
    private List<Integer[]> fwdOffsets, revOffsets;

    public TssDetection(PersistantReference refGenome, int trackID) {
        this.refGenome = refGenome;
        this.trackid = trackID;
        this.detectedTSS = new ArrayList<>();
        this.detectedPutativeAntisenseTSS = new ArrayList<>();
        this.detectedPutativeNewRegions = new ArrayList<>();
        this.fwdOffsets = new ArrayList<>();
        this.revOffsets = new ArrayList<>();
        this.fwdTss = new HashMap<>();
        this.revTss = new HashMap<>();

    }

    /**
     * Running the transcription start site detection.
     *
     * @param length Length of the reference genome.
     * @param forwardCDSs CDS information for forward regions in genome.
     * @param reverseCDSs CDS information for reverse regions in genome.
     * @param allRegionsInHash HashMap with all featureIDs and associated
     * features.
     * @param ratio User given ratio for minimum increase of start counts from
     * pos to pos + 1.
     * @param up Number of bases for sequence in upstream direction beginning
     * from TSS.
     * @param down Number of bases for sequence in downstream direction
     * beginning from TSS.
     * @param isLeaderlessDetection true for performing leaderless detection.
     * @param leaderlessRestirction Restriction of bases upstream and
     * downstream.
     * @param isExclusionOfInternalTss true for excluding internal TSS.
     * @param distanceForExcludingTss number restricting the distance between
     * TSS and detected gene.
     */
    public void runningTSSDetection(HashMap<Integer, List<Integer>> forwardCDSs, HashMap<Integer, List<Integer>> reverseCDSs,
            HashMap<Integer, PersistantFeature> allRegionsInHash, Statistics stats, ParameterSetFiveEnrichedAnalyses parameters) {

        int ratio = parameters.getRatio();
        int up = parameters.getUpstreamRegion();
        int down = parameters.getDownstreamRegion();
        int leaderlessRestirction = parameters.getLeaderlessLimit();
        /**
         * This boolean tells if all internal Tss, which means it is detected in
         * the region, where a feature is allready annotated. True if all
         * internal Tss have to be excluded from analysis.
         */
        boolean isExclusionOfInternalTss = parameters.isExclusionOfInternalTSS();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();
        int keepingInternalTssDistance = parameters.getKeepingInternalTssDistance();
        int[][] forward = stats.getForward(); // Array with startsite count information for forward mapping positions.
        int[][] reverse = stats.getReverse(); // Array with startsite count information for reverse mapping positions.
        int[][] fwdCov = stats.getFwdCoverage(); // Array with coverage counts of mappings in forward direction.
        int[][] revCov = stats.getRevCoverage(); // Array with coverage counts of mappings in reverse direction.
        double mm = stats.getMm(); // Mappings per Million.
        double bg = stats.getBg(); // Background cutoff

        for (PersistantChromosome chrom : refGenome.getChromosomes().values()) {
            int chromId = chrom.getId();
            int chromNo = chrom.getChromNumber();
            int chromLength = chrom.getLength();
            for (int i = 0; i < chromLength; i++) {

                if ((forward[chromNo - 1][i] > bg) || (reverse[chromNo - 1][i] > bg)) { // background cutoff is passed
                    int f_before = forward[chromNo - 1][i - 1] + 1;
                    int r_before = reverse[chromNo - 1][i + 1] + 1;

                    int f_ratio = (forward[chromNo - 1][i] + 1) / f_before;
                    int r_ratio = (reverse[chromNo - 1][i] + 1) / r_before;

                    if (f_ratio >= ratio) {

                        int offset = 0;
                        int end = 0;

                        // counting the offset as long as there is no featureID
                        while (!forwardCDSs.containsKey(i + offset - end)) {
                            if ((i + offset) > chromLength) {
                                end = chromLength;
                            }
                            offset++;
                        }

                        double rel_count = forward[chromNo - 1][i] / mm;

                        // TODO: here we have with forwardCDSs.get(i + j - end).get(0) the feature ID, now we have to get the name
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
                        int offsetToNextDownstreamFeature = 0;
                        String startCodon;
                        String stopCodon;


                        // We have 4 possible cases here
                        // 1. Offset is 0 which means, that the TSS is a Leaderless (<=7bp downstream from feature start)  TSS 
                        // or 
                        // 2. TSS is an internal (>7bp downstream from feature start) TSS
                        // 3. Offset is < 7bp which is maybe a leaderless upstream
                        // 4. Offset is > 500bp, which maybe is a TSS for unannotated Transcript which have 
                        // to be blasted
                        // 

                        // Offset is = 0 => distance to start is > 0 
                        if (offset == 0) {
                            dist2start = i - feature.getStart();
                            dist2stop = feature.getStop() - i;

                            // check if leaderless (downstream direction)
                            if (dist2start < leaderlessRestirction) {
                                leaderless = true;
                                isInternal = false;

                                startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
//                                
                                // check antisensness

                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                }
                                // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                                // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                                // (dist2start % 3) == 0 checks if in frame
                                if (dist2start > 0 && (dist2start % 3) == 0) {
                                    String startAtTSS = getSubSeq(chrom, isFwd, i - 1, i + 2);
                                    if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                        cdsShift = true;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                        feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                        getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift,
                                        startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal

                                if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                    detectedTSS.add(tss);
                                }
                            } else if (dist2start > leaderlessRestirction && isExclusionOfInternalTss == false) { // internal mapping or TSS for next downstream Feature
                                // here we want to find the next gene because the startsite is inbetween a gene which is not a leaderles gene
                                int currentFeatureID = feature.getId();
                                isInternal = true;
                                int j = 1;
                                PersistantFeature nextDownstreamFeature = allRegionsInHash.get(currentFeatureID + j);
                                while (feature.getLocus().equals(nextDownstreamFeature.getLocus())) {
                                    j++;
                                    nextDownstreamFeature = allRegionsInHash.get(currentFeatureID + j);
                                }
                                offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - i;

                                // check antisensness

                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                }

                                cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, 0.1);

                                if (offsetToNextDownstreamFeature < keepingInternalTssDistance) {
                                    // putative the corresponding gene for TSS
                                    if (nextDownstreamFeature.isFwdStrand()) {
                                        startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                        stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                                null, offset, dist2start, dist2stop, nextDownstreamFeature, offsetToNextDownstreamFeature,
                                                getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift,
                                                startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                        if (!nextDownstreamFeature.getType().equals(FeatureType.RRNA) && !nextDownstreamFeature.getType().equals(FeatureType.TRNA)) {
                                            detectedTSS.add(tss);
                                        }
                                    }
                                } else if (cdsShift) {
                                    if (nextDownstreamFeature.isFwdStrand()) {
                                        startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                        stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                                feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift,
                                                startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                        if (!nextDownstreamFeature.getType().equals(FeatureType.RRNA) && !nextDownstreamFeature.getType().equals(FeatureType.TRNA)) {
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
                                // check for cdsShift, when offset > and offset+1 mod 3 == 0
                                if (offset > 0 && ((offset + 1) % 3) == 0) {
                                    String startAtTSS = getSubSeq(chrom, isFwd, i - 1, i + 2);
                                    if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                        cdsShift = true;
                                    }
                                }
                                leaderless = true;
                                startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count, feature,
                                        offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq), leaderless,
                                        cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);//TODO: check if it is internal
                                if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                    detectedTSS.add(tss);
                                }
                            } else {
                                if (offset < distanceForExcludingTss) {
                                    // check antisensness
                                    if (reverseCDSs.get(i) != null) {
                                        isPutAntisense = true;
                                    }
                                    startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq), leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                    if (!feature.getType().equals(FeatureType.RRNA) && !feature.getType().equals(FeatureType.TRNA)) {
                                        detectedTSS.add(tss);
                                        fwdOffsets.add(new Integer[]{i, i + offset});
                                        fwdTss.put(i, false);
                                    }
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
                                end = chromLength;
                            }
                            offset++;
                        }

                        double rel_count = reverse[chromNo - 1][i] / mm;

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
                                String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                startCodon = Complement(startCodonRev);
                                stopCodon = Complement(stopCodonRev);
                                String reversedSeq = new StringBuffer(getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                String revComplement = Complement(reversedSeq);

                                // check for cdsShift when offset is 0 and distance2Start > 0 and dist2Start mod 3 == 0
                                if (dist2start > 0 && (dist2start % 3) == 0) {
                                    String startAtTSSRev = getSubSeq(chrom, isFwd, i - 3, i);
                                    String complement = Complement(startAtTSSRev);
                                    if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                        cdsShift = true;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
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

                                cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, 0.1);

                                if (nextOffset < keepingInternalTssDistance) {
                                    // puttative nextgene
                                    if (!nextFeature.isFwdStrand()) {
                                        String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                        String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                        startCodon = Complement(startCodonRev);
                                        stopCodon = Complement(stopCodonRev);
                                        String reversedSeq = new StringBuffer(getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                        String revComplement = Complement(reversedSeq);
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                null, offset, dist2start, dist2stop, nextFeature, nextOffset, revComplement, leaderless, cdsShift,
                                                startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                        if (!nextFeature.getType().equals(FeatureType.RRNA) && !nextFeature.getType().equals(FeatureType.TRNA)) {
                                            detectedTSS.add(tss);
                                        }
                                    }
                                } else if (cdsShift) {
                                    if (!nextFeature.isFwdStrand()) {
                                        String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                        String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                        startCodon = Complement(startCodonRev);
                                        stopCodon = Complement(stopCodonRev);
                                        String reversedSeq = new StringBuffer(getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                        String revComplement = Complement(reversedSeq);
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift,
                                                startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
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
                                String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                startCodon = Complement(startCodonRev);
                                stopCodon = Complement(stopCodonRev);
                                String reversedSeq = new StringBuffer(getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                String revComplement = Complement(reversedSeq);

                                // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                                if (offset > 0 && ((offset + 1) % 3) == 0) {
                                    String startAtTSSRev = getSubSeq(chrom, isFwd, i - 3, i);
                                    String complement = Complement(startAtTSSRev);
                                    if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                        cdsShift = true;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
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
                                    String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    startCodon = Complement(startCodonRev);
                                    stopCodon = Complement(stopCodonRev);
                                    String reversedSeq = new StringBuffer(getSubSeq(chrom, isFwd, startSubSeq, stopSubSeq)).reverse().toString();
                                    String revComplement = Complement(reversedSeq);
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                            feature, offset, dist2start, dist2stop, null, nextOffset, revComplement, leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
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
    }

    /**
     *
     * @param chrom PersistantChromosome.
     * @param featureStart Start position of PersistantFeature.
     * @param featureStop Stop position of PersistantFeature.
     * @param tssStart Start position of transcription start site.
     * @param dist2start Distance from transcription start site to start
     * position of PersistantFeature.
     * @param isFwd true if is forward direction.
     * @param relPercentage
     * @return true, if putative CDS-Shift occur.
     */
    private boolean checkCdsShift(PersistantChromosome chrom, int featureStart, int featureStop, int tssStart, int dist2start, boolean isFwd, double relPercentage) {
        boolean ret = false;
        double length = featureStop - featureStart;
        double partOfFeature = dist2start / length;

        if (partOfFeature <= relPercentage) {

            if (isFwd) {
                // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                if ((dist2start % 3) == 0) {
                    String startAtTSS = getSubSeq(chrom, isFwd, tssStart - 1, tssStart + 2);
                    if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                        ret = true;
                    }
                }
            } else {
                // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                if ((dist2start % 3) == 0) {
                    String startAtTSSRev = getSubSeq(chrom, isFwd, tssStart - 3, tssStart);
                    String complement = Complement(startAtTSSRev);
                    if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
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
    private String getSubSeq(PersistantChromosome chrom, boolean isFwd, int start, int stop) {

        String seq = "";
        if (start > 0 && stop < chrom.getLength()) {
            seq = chrom.getSequence(this).substring(start, stop);
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
