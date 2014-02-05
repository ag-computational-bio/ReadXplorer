package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
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

    private final List<TranscriptionStart> detectedTSS;
    private final List<NovelTranscript> detectedPutativeNewRegions;
    private final int trackid;
    private final PersistantReference refGenome;
    private final HashMap<Integer, Boolean> fwdTss, revTss;
    private final List<Integer[]> fwdOffsets, revOffsets;
    private int totalCntOfTss,
            cntOfCisAntisenseWithAssignedDownstreamFeature,
            cntOfAntisenseWithoutAssignedDownstreamFeature,
            cntOfReverseTss, cntOfFwdTss, cntOfIntrageneticTssWithAssignedFeatures,
            cntOfLeaderlessTss, cntOfIntrageneticWithoutAssignedFeauters,
            cntOfCdsShiftTSS, cntOfNormalTSS, cntPutativeIntergenicTSSWithNoAssignment;

    /**
     *
     * @param refGenome
     * @param trackID
     */
    public TssDetection(PersistantReference refGenome, int trackID) {
        this.refGenome = refGenome;
        this.trackid = trackID;
        this.detectedTSS = new ArrayList<>();
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
     * @param isLeaderlessDetection true for performing leaderless detection.
     * @param leaderlessRestirction Restriction of bases upstream and
     * downstream.
     * @param isExclusionOfInternalTss true for excluding internal TSS.
     * @param distanceForExcludingTss number restricting the distance between
     * TSS and detected gene.
     */
    public void runningTSSDetection(HashMap<Integer, List<Integer>> forwardCDSs, HashMap<Integer, List<Integer>> reverseCDSs,
            HashMap<Integer, PersistantFeature> allRegionsInHash, StatisticsOnMappingData stats, ParameterSetFiveEnrichedAnalyses parameters) {

        int ratio = parameters.getRatio();
        int leaderlessRestirction = parameters.getLeaderlessLimit();
        /**
         * This boolean tells if all internal Tss (intragenic) schould be
         * ecluded. True if all internal Tss have to be excluded from analysis.
         */
        boolean isExclusionOfInternalTss = parameters.isExclusionOfInternalTSS();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();
        int keepingInternalTssDistance = parameters.getKeepingInternalTssDistance();
        int[][] forward = stats.getForwardReadStarts(); // Array with startsite count information for forward mapping positions.
        int[][] reverse = stats.getReverseReadStarts(); // Array with startsite count information for reverse mapping positions.
        double mm = stats.getMm(); // Mappings per Million.
        double bg = stats.getBgThreshold(); // Background cutoff

        for (PersistantChromosome chrom : refGenome.getChromosomes().values()) {
            int chromId = chrom.getId();
            int chromNo = chrom.getChromNumber();
            int chromLength = chrom.getLength();
            int f_before;
            int r_before;

            for (int i = 0; i < chromLength; i++) {

                if ((forward[chromNo - 1][i] > bg) || (reverse[chromNo - 1][i] > bg)) { // background cutoff is passed

                    int dist2start = 0;
                    int dist2stop = 0;
                    boolean leaderless = false;
                    boolean cdsShift = false;
                    boolean isInternal = false;
                    boolean isPutAntisense = false;
                    int offsetToNextDownstreamFeature = 0;
                    String startCodon;
                    String stopCodon;

                    if (forward[chromNo - 1][i - 1] == 0) {
                        f_before = 1;
                    } else {
                        f_before = forward[chromNo - 1][i - 1];
                    }

                    if (reverse[chromNo - 1][i + 1] == 0) {
                        r_before = 1;
                    } else {
                        r_before = reverse[chromNo - 1][i + 1];
                    }

                    int f_ratio = (forward[chromNo - 1][i]) / f_before;
                    int r_ratio = (reverse[chromNo - 1][i]) / r_before;

                    if (f_ratio >= ratio) {
                        boolean isFwd = true;

                        // increase total count of putative TSS
                        this.totalCntOfTss++;
                        this.cntOfFwdTss++;

                        int offset = 0;
                        int end = 0;

                        // determining the offset to next downstream feature
                        while (!forwardCDSs.containsKey(i + offset - end)) {
                            if ((i + offset) > chromLength) {
                                end = chromLength;
                            }
                            offset++;
                        }

                        double rel_count = forward[chromNo - 1][i] / mm;

                        // getting the PersistantFeature (only CDS)!
                        int featureID = forwardCDSs.get(i + offset - end).get(0);
                        PersistantFeature feature = allRegionsInHash.get(featureID);

                        while (feature.getType() != FeatureType.CDS || feature.isFwdStrand() == false) {
                            feature = allRegionsInHash.get(++featureID);
                        }

                        // Case 1: offset is 0, TSS is whether intragenic or leaderless
                        if (offset == 0) {

                            dist2start = i - feature.getStart();
                            dist2stop = feature.getStop() - i;

                            // check if feture is leaderless (downstream direction)
                            if (dist2start < leaderlessRestirction) {
                                leaderless = true;
                                this.cntOfLeaderlessTss++;
                                isInternal = false;

                                startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
//                                
                                // check if cis antisense
                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                }

                                // Check for putative cds shift. When offset is 0 and distance2Start > 0 
                                // Check if TSS is in frame  => dist2Start mod 3 == 0
                                // && !startCodon.equals("TTG") && !startCodon.equals("CTG") && !startCodon.equals("ATG") && !(startCodon.equals("GTG"))
                                if (dist2start > 0 && (dist2start % 3) == 0) {
                                    String startAtTSS = getSubSeq(chrom, isFwd, i - 1, i + 2);
                                    if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                        cdsShift = true;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                        feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                        leaderless, cdsShift,
                                        startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal

                                detectedTSS.add(tss);
                            } else if (dist2start > leaderlessRestirction && isExclusionOfInternalTss == false) {
                                // Case 2: Intragenic TSS if not exclusion on
                                // here we want to find the next feature because the startsite is intragenic
                                isInternal = true;

                                int currentFeatureID = feature.getId();
                                currentFeatureID++;
                                PersistantFeature nextDownstreamFeature = allRegionsInHash.get(currentFeatureID);

                                while (feature.getLocus().equals(nextDownstreamFeature.getLocus()) || nextDownstreamFeature.getType() != FeatureType.CDS || nextDownstreamFeature.isFwdStrand() == false) {
                                    currentFeatureID++;
                                    nextDownstreamFeature = allRegionsInHash.get(currentFeatureID);
                                }

                                offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - i;

                                // check antisensness
                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
//                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                }

                                cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, (parameters.getCdsShiftPercentage() / 100.0));

                                if (offsetToNextDownstreamFeature < keepingInternalTssDistance) {
                                    // putative the corresponding gene for TSS
                                    startCodon = getSubSeq(chrom, isFwd, nextDownstreamFeature.getStart() - 1, nextDownstreamFeature.getStart() + 2);
                                    stopCodon = getSubSeq(chrom, isFwd, nextDownstreamFeature.getStop() - 3, nextDownstreamFeature.getStop());
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                            null, offset, dist2start, dist2stop, nextDownstreamFeature, offsetToNextDownstreamFeature,
                                            leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                    detectedTSS.add(tss);
                                    this.cntOfIntrageneticTssWithAssignedFeatures++;
                                    if (isPutAntisense) {
                                        this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                    }
                                } else if (cdsShift) {
                                    startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count,
                                            feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                            leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                    detectedTSS.add(tss);
                                    this.cntOfCdsShiftTSS++;
                                } else {
                                    // No CDS shift, intragenetic but no Feature in limited distance range and not 
                                    this.cntOfIntrageneticWithoutAssignedFeauters++;
                                }
                            }
                        } else {
                            // leaderless in upstream direction, offset is != 0 but in leaderless range
                            if (offset < leaderlessRestirction) {
                                leaderless = true;
                                this.cntOfLeaderlessTss++;
                                // check antisensness
                                if (reverseCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                }
                                // check for cdsShift, when offset > and offset+1 mod 3 == 0
                                if (offset > 0 && ((offset + 1) % 3) == 0) {
                                    String startAtTSS = getSubSeq(chrom, isFwd, i - 1, i + 2);
                                    if (startAtTSS.equals("TTG") || startAtTSS.equals("CTG") || startAtTSS.equals("ATG") || startAtTSS.equals("GTG")) {
                                        cdsShift = true;
                                        this.cntOfCdsShiftTSS++;
                                    }
                                }

                                startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count, feature,
                                        offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless,
                                        cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);//TODO: check if it is internal
                                detectedTSS.add(tss);
                            } else {

                                // TSS is too far away from annotated feature
                                if (offset < distanceForExcludingTss) {
                                    // check antisensness
                                    if (reverseCDSs.get(i) != null) {
                                        isPutAntisense = true;
                                        this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                    }
                                    startCodon = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    stopCodon = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                    detectedTSS.add(tss);
                                    fwdOffsets.add(new Integer[]{i, i + offset});
                                    fwdTss.put(i, false);

                                    this.cntOfNormalTSS++;
                                } else {
                                    // check only for antisense
                                    if (reverseCDSs.get(i) != null) {
                                        isPutAntisense = true;
                                        this.cntOfAntisenseWithoutAssignedDownstreamFeature++;
                                        feature = allRegionsInHash.get(reverseCDSs.get(i).get(0));
                                        String startCodonRev = getSubSeq(chrom, false, feature.getStop() - 3, feature.getStop());
                                        String stopCodonRev = getSubSeq(chrom, false, feature.getStart() - 1, feature.getStart() + 2);
                                        startCodon = complement(startCodonRev);
                                        stopCodon = complement(stopCodonRev);
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, forward[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                        tss.setIntergenicAntisense(true);
                                        detectedTSS.add(tss);
                                    } else {
                                        this.cntPutativeIntergenicTSSWithNoAssignment++;
                                    }

                                }
                            }
                        }
                    }

                    if (r_ratio >= ratio) {
                        boolean isFwd = false;
                        this.totalCntOfTss++;
                        this.cntOfReverseTss++;

                        int offset = 0;
                        int end = 0;

                        // determining the offset to feature
                        while (!reverseCDSs.containsKey(end + i - offset)) {
                            if ((i - offset) == 0) {
                                end = chromLength;
                            }
                            offset++;
                        }

                        double rel_count = reverse[chromNo - 1][i] / mm;

                        int featureID = reverseCDSs.get(end + i - offset).get(0);
                        PersistantFeature feature = allRegionsInHash.get(featureID);

                        while (feature.getType() != FeatureType.CDS || feature.isFwdStrand() == true) {
                            feature = allRegionsInHash.get(--featureID);
                        }

                        if (offset == 0) {
                            dist2start = feature.getStop() - i;
                            dist2stop = i - feature.getStart();

                            // check if leaderless (downstream)
                            if (dist2start < leaderlessRestirction) {
                                // check antisensness
                                if (forwardCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                }
                                leaderless = true;
                                this.cntOfLeaderlessTss++;

                                String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                startCodon = complement(startCodonRev);
                                stopCodon = complement(stopCodonRev);

                                // check for cdsShift when offset is 0 and distance2Start > 0 and dist2Start mod 3 == 0
                                if (dist2start > 0 && (dist2start % 3) == 0) {
                                    String startAtTSSRev = getSubSeq(chrom, isFwd, i - 3, i);
                                    String complement = complement(startAtTSSRev);
                                    if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                        cdsShift = true;
                                        this.cntOfCdsShiftTSS++;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                detectedTSS.add(tss);
                            }

                            if (dist2start > leaderlessRestirction && isExclusionOfInternalTss == false) {

                                int currentFeatureID = feature.getId();
                                currentFeatureID--;
                                PersistantFeature nextFeature = allRegionsInHash.get(currentFeatureID);
                                while (feature.getLocus().equals(nextFeature.getLocus()) || nextFeature.getType() != FeatureType.CDS || nextFeature.isFwdStrand() == true) {
                                    nextFeature = allRegionsInHash.get(--currentFeatureID);
                                }
                                offsetToNextDownstreamFeature = i - nextFeature.getStop();

                                // check antisensness
                                if (forwardCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                }

                                cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, (parameters.getCdsShiftPercentage() / 100.0));

                                if (offsetToNextDownstreamFeature < keepingInternalTssDistance) {
                                    // puttative nextgene
                                    String startCodonRev = getSubSeq(chrom, isFwd, nextFeature.getStop() - 3, nextFeature.getStop());
                                    String stopCodonRev = getSubSeq(chrom, isFwd, nextFeature.getStart() - 1, nextFeature.getStart() + 2);
                                    startCodon = complement(startCodonRev);
                                    stopCodon = complement(stopCodonRev);
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                            null, offset, dist2start, dist2stop, nextFeature, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid); //TODO: check if it is internal
                                    detectedTSS.add(tss);
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                } else if (cdsShift) {
                                    String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    startCodon = complement(startCodonRev);
                                    stopCodon = complement(stopCodonRev);
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                            feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                    detectedTSS.add(tss);
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                    this.cntOfCdsShiftTSS++;
                                } else {
                                    // No CDS shift, intragenetic but no Feature in limited distance range and not 
                                    this.cntOfIntrageneticWithoutAssignedFeauters++;
                                }
                            }

                        } else {
                            if (offset < leaderlessRestirction) {
                                // check antisensness
                                if (forwardCDSs.get(i) != null) {
                                    isPutAntisense = true;
                                    this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                }
                                // Leaderless TSS upstream
                                leaderless = true;
                                this.cntOfLeaderlessTss++;
                                String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                startCodon = complement(startCodonRev);
                                stopCodon = complement(stopCodonRev);
                                // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                                if (offset > 0 && ((offset + 1) % 3) == 0) {
                                    String startAtTSSRev = getSubSeq(chrom, isFwd, i - 3, i);
                                    String complement = complement(startAtTSSRev);
                                    if (complement.equals("TTG") || complement.equals("CTG") || complement.equals("ATG") || complement.equals("GTG")) {
                                        cdsShift = true;
                                        this.cntOfCdsShiftTSS++;
                                    }
                                }

                                TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                        leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                detectedTSS.add(tss);
                            } else {
                                // "normal" TSS
                                if (offset < distanceForExcludingTss) {
                                    // check antisensness
                                    if (forwardCDSs.get(i) != null) {
                                        isPutAntisense = true;
                                        this.cntOfCisAntisenseWithAssignedDownstreamFeature++;
                                    }
                                    String startCodonRev = getSubSeq(chrom, isFwd, feature.getStop() - 3, feature.getStop());
                                    String stopCodonRev = getSubSeq(chrom, isFwd, feature.getStart() - 1, feature.getStart() + 2);
                                    startCodon = complement(startCodonRev);
                                    stopCodon = complement(stopCodonRev);
                                    TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count,
                                            feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                            startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                    revOffsets.add(new Integer[]{i - offset, i});
                                    revTss.put(i, false);
                                    detectedTSS.add(tss);
                                    this.cntOfNormalTSS++;
                                } else {
                                    // check only for antisense
                                    if (forwardCDSs.get(i) != null) {
                                        isPutAntisense = true;
                                        this.cntOfAntisenseWithoutAssignedDownstreamFeature++;
                                        feature = allRegionsInHash.get(forwardCDSs.get(i).get(0));
                                        startCodon = getSubSeq(chrom, true, feature.getStart() - 1, feature.getStart() + 2);
                                        stopCodon = getSubSeq(chrom, true, feature.getStop() - 3, feature.getStop());
                                        TranscriptionStart tss = new TranscriptionStart(i, isFwd, reverse[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0, leaderless, cdsShift, startCodon, stopCodon, isInternal, isPutAntisense, chromId, this.trackid);
                                        tss.setIntergenicAntisense(true);
                                        detectedTSS.add(tss);
                                    } else {
                                        this.cntPutativeIntergenicTSSWithNoAssignment++;
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
        System.out.println(getStatisticsOnTssDetection());
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
                    String complement = complement(startAtTSSRev);
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
    private String complement(String seq) {
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

    public List<NovelTranscript> getDetectedPutativeNewRegions() {
        return detectedPutativeNewRegions;
    }

    private String getStatisticsOnTssDetection() {
        String result = null;

        result = "Gesamtanzahl von TSS:\t" + this.totalCntOfTss
                + "\nAnzahl der reversen TSS:\t" + this.cntOfReverseTss
                + "\nAnzahl der forward TSS:\t" + this.cntOfFwdTss
                + "\nAnzahl der normalen TSS:\t" + this.cntOfNormalTSS
                + "\nAnzahl der Cis-Antisense mit zugewiesenen Feature\t" + this.cntOfCisAntisenseWithAssignedDownstreamFeature
                + "\nAnzahl der Cis-Antisense ohne zugewiesenen Feature\t" + this.cntOfAntisenseWithoutAssignedDownstreamFeature
                + "\nAnzahl der intragenen TSS mit zugewiesenen Feature:\t" + this.cntOfIntrageneticTssWithAssignedFeatures
                + "\nAnzahl der intragenen TSS ohne zugewiesenen Feature:\t" + this.cntOfIntrageneticWithoutAssignedFeauters
                + "\nAnzahl der intergenetischen TSS ohne zugewiesenes Feature:\t" + this.cntPutativeIntergenicTSSWithNoAssignment
                + "\nAnzahl der TSS mit möglichem CDS Shift:\t" + this.cntOfCdsShiftTSS
                + "\nAnzahl der leaderless TSS:\t" + this.cntOfLeaderlessTss;

        return result;
    }
}
