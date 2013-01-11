package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedAnnotations;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;

/**
 * Starts the TSS analysis including the detection of unannotated transcripts.
 *
 * The original TSS detection does the following:
 * 
 * Carries out the logic behind the transcription start site (TSS) anaylsis.
 * When executing the transcription start site detection increaseReadCount is always active
 * and maxInitialReadCount + increaseReadCount2 are optional parameters. They can
 * further constrain the search space (e.g. inc = 100, max = 10, inc2 = 50 means 
 * that coverage increases above 50 with an initial read count of 0-10 are detected
 * as transcription start sites, but also all increases of 100 and bigger. When the parameters are
 * switched, e.g. inc = 50, max = 10, inc2 = 100, then all coverage increases above 100 
 * with an initial read count of 0-10 are detected as transcription start sites, but for all positions
 * with an initial read count > 10 an increase of 50 read counts is enough to be detected.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisUnannotatedTransStart extends AnalysisTranscriptionStart {
    
    private int minCoverage;

    public AnalysisUnannotatedTransStart(TrackConnector trackConnector, int increaseReadCount, int increaseReadPercent, 
            int maxInitialReadCount, int increaseReadCount2, boolean tssAutomatic, int minCoverage) {
           
        super(trackConnector, increaseReadCount, increaseReadPercent, maxInitialReadCount, increaseReadCount2, tssAutomatic);
        this.minCoverage = minCoverage;
    }
    
    /**
     * Acutally adds the detected TSS to the list of detected TSSs, but also
     * elongates currently unannotated transcripts (with no up- or downstream 
     * annotations on the same strand) up to the minimum coverage, set in the
     * constructor, is reached.
     * @param tss the transcription start site to add to the list
     */
    @Override
    protected void addDetectStart(TranscriptionStart tss) {
        DetectedAnnotations annos = tss.getDetAnnotations();
        int currentPos = tss.getPos();
        
        //only if no annotation is available, detect the length of the unannotated new transcript
        if (    annos.getCorrectStartAnnotation() == null && 
                annos.getDownstreamAnnotation() == null &&
                annos.getUpstreamAnnotation() == null) {
            
            if (tss.isFwdStrand()) {
                while (currentCoverage.getBestMatchFwdMult(currentPos) > minCoverage) {
                    ++currentPos;
                }
                --currentPos;
            } else {
                while (currentCoverage.getBestMatchRevMult(currentPos) > minCoverage) { // TODO: && currentPos < referenceLength > 0
                    --currentPos;
                }
                ++currentPos;
            }
            
            // instead of an ordinary TranscriptStart we add the TranscriptStart with unannotated transcript information
            detectedStarts.add(new TransStartUnannotated(tss.getPos(), tss.isFwdStrand(), tss.getInitialCoverage(), 
                    tss.getStartCoverage(), tss.getDetAnnotations(), currentPos));
            
        } else {
            detectedStarts.add(tss);
        }
    }
}
