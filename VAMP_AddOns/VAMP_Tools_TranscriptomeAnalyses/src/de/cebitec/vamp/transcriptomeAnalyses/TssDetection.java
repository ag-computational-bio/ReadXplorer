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
        this.detectedTSS = new ArrayList<TranscriptionStart>();
    }

    /**
     * Running the transcription start site detection. 
     * 
     * @param refSeqLength Length of the reference genome.
     * @param forwardCDSs CDS information for forward regions in genome.
     * @param reverseCDSs CDS information for reverse regions in genome.
     * @param allRegionsInHash HashMap with all featureIDs and associated features.
     * @param forward Array with startsite count information for forward mapping positions.
     * @param reverse Array with startsite count information for reverse mapping positions.
     * @param ratio User given ratio for minimum increase of start counts from pos to pos + 1.
     * @param mm Mappings per Million.
     * @param bg Background cutoff
     * @param up Number of bases for sequence in upstream direction beginning from TSS.
     * @param down Number of bases for sequence in downstream direction beginning from TSS.
     */
    public void runningTSSDetection(int refSeqLength, HashMap<Integer, List<Integer>> forwardCDSs, 
            HashMap<Integer, List<Integer>> reverseCDSs, HashMap<Integer, 
                    PersistantFeature> allRegionsInHash, int[] forward, int[] reverse,
                    double ratio, double mm, int bg, int up, int down) {

//# now do the actual summations and adjust the gene length 
//# (this is currently done intrinsically, a method to "import"
//# gene starts from a 5'-end enriched set has yet to be implemented)

        int length = refSeqLength;

        for (int i = 0; i < length; i++) {

            if ((forward[i] > bg) || (reverse[i] > bg)) { // background cutoff is passed
                int f_before = forward[i - 1] + 1;
                int r_before = reverse[i + 1] + 1;

                int f_ratio = (forward[i] + 1) / f_before;
                int r_ratio = (reverse[i] + 1) / r_before;

                String fdata;
                String rdata;

                if (f_ratio >= ratio) {
                    int j = 0;
                    int end = 0;

                    // TO DO: Also check the next gene!!!
                    // it can be, that the List on pos i+j-end isnt initiated => null!
                    while (forwardCDSs.get(i + j - end) == null) { // as long as there is no featureID

                        if ((i + j) > length) {
                            end = length;
                        }
                        j++;
                    }

                    // for the 10 Positions before feature-start
                    String before = "";
                    int[] beforeCountsFwd = new int[10];

                    for (int k = 10; k > 0; k--) {
                        int count = forward[i - k];
                        before += count+";";
                        beforeCountsFwd[k] = count;
                    }

                    double rel_count = forward[i] / mm;

                    // TODO: here we have with forwardCDSs.get(i + j - end).get(0) the feature ID, now we have to get the name
                    PersistantFeature feature = allRegionsInHash.get(forwardCDSs.get(i + j - end).get(0));
                    fdata = forward[i] + ";" + rel_count + ";" + before + feature.getFeatureName() + ";" + j;
                    int dist2start = 0;
                    int dist2stop = 0;
                    int l = 0;
                    if (j == 0) {
                        dist2start = i - feature.getStart();
                        dist2stop = feature.getStop() - i;
                        fdata += ";" + dist2start + ";" + dist2stop;
                        
                        if (dist2start != 0) {
                            // here we want to find the next gene because the startsite and Gene start are the same.
                            while (forwardCDSs.get(i + j + l - end) == null && (forwardCDSs.get(i + j - end).get(0) != forwardCDSs.get(i + j + l - end).get(0))) { 
                                
                                if ((i + j + l) > length) {
                                    end = length;
                                }
                                l++;
                            }
                            fdata += ";" + allRegionsInHash.get(forwardCDSs.get(i + j + l - end).get(0)) + ";" + l;
                        }
                    }
                    //this.referenceSequence.substring(i-up+1, up+down)
                    detectedTSS.add(new TranscriptionStart(i+1, true, forward[i+1], rel_count, beforeCountsFwd, feature, j, dist2start, dist2stop, allRegionsInHash.get(forwardCDSs.get(i + j + l - end).get(0)), l, null, 0));
                }

                if (r_ratio >= ratio) {
                    int j = 0;
                    int end = 0;
                    while (reverseCDSs.get(end + i - j) == null) {
                        if ((i - j) == 0) {
                            end = length;
                        }
                        j++;
                    }
                    String before = "";
                    int[] beforeCountRev = new int[10];
                    for (int k = 10; k > 0; k--) {
                        int count = reverse[i + k];
                        before += count + ";";
                        beforeCountRev[k] = count;
                    }
                    double rel_count = reverse[i] / mm;

                    PersistantFeature feature = allRegionsInHash.get(reverseCDSs.get(end + i).get(0));
                    rdata = reverse[i] + ";" + rel_count + ";" + before + feature.getFeatureName() + ";" + j;
                    int dist2start = 0;
                    int dist2stop = 0;
                    int l = 0;
                    if (j == 0) {
                       dist2start = feature.getStart() - i;
                        dist2stop = i - feature.getStop();
                        rdata += ";" + dist2start + ";" + dist2stop;
                        if (dist2start != 0) {
                            
                            // TODO: not clear, if reverseCDSs.get(end + i - j - l).get(0) have to be  == 0 or != 0
                            while (reverseCDSs.get(end + i - j - l) == null && (reverseCDSs.get(end + i - j).get(0) != reverseCDSs.get(end + i - j - l).get(0))) {
                                if ((i - j - l) == 0) {
                                    end = length;
                                }
                                l++;
                            }
                            rdata += ";" + allRegionsInHash.get(reverseCDSs.get(end + i - j - l).get(0)) + ";" + l;
                        }
                    }
                    String seq = this.referenceSequence.substring(i-down, up+down);
                    String reversedSeq = new StringBuffer(seq).reverse().toString();
                    detectedTSS.add(new TranscriptionStart(i+1, false, forward[i+1], rel_count, beforeCountRev, feature, j, dist2start, dist2stop, allRegionsInHash.get(reverseCDSs.get(end + i - j - l).get(0)), l, reversedSeq, 0));
                }
            }
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
