package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters for startcodons in two ways: First for all
 * available startcodons in a specified region and second
 * for all startcodons of a given frame for a specified region.
 *
 * @author ddoppmeier, rhilker
 */
public class PatternFilter implements RegionFilterI {

    public static final int INIT = 10;
    private List<Region> matchedPatterns;
    private int absStart;
    private int absStop;
    private PersistantReference refGen;
    private String sequence;
    //private int frameCurrFeature;
    private Pattern pattern;
    private Pattern patternRev;

    public PatternFilter(int absStart, int absStop, PersistantReference refGen) {
        this.matchedPatterns = new ArrayList<Region>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;

        //this.frameCurrFeature = StartCodonFilter.INIT; //because this is not a frame value
    }

    private List<Region> findPatternInInterval() {

        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {

            int offset = 3;
            int start = absStart - offset;
            int stop = absStop+2;

            if(start < 0 ){
                offset -= Math.abs(start);
                start = 0;
            }
            if(stop > refGen.getSequence().length()){
                stop = refGen.getSequence().length();
            }

            this.sequence = refGen.getSequence().substring(start, stop);
//            boolean isFeatureSelected = this.frameCurrFeature != INIT;
            this.matchPattern(sequence, this.pattern, true, offset);//, isFeatureSelected);
            this.matchPattern(sequence, this.patternRev, false, offset);//, isFeatureSelected);
        }
        return this.matchedPatterns;

    }

    /**
     * Identifies all positions of the pattern in the whole reference genome sequence.
     * @return list of identified hit regions
     */
    public int findNextOccurrence() {

        int from = -1;
        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {

            int start = this.absStop;
            
            if (this.absStart < 0) {
                this.absStart = 0;
            }
            if (start > refGen.getSequence().length()) {
                start = 0;
            }

            String seq = refGen.getSequence().substring(start, refGen.getSequence().length());
            
            //at first search from current position till end of sequence on both frames
            from = this.matchNextOccurrence(seq, this.pattern);
            if (from == -1){ //try to find occurrence on rev strand
                from = this.matchNextOccurrence(seq, this.patternRev);
            }
            
            //then search from 0 to current position on both frames
            if (from == -1 && start > 0){
                seq = refGen.getSequence().substring(0, start);
                start = 0;
                
                from = this.matchNextOccurrence(seq, this.pattern);
                if (from == -1){ //try to find occurrence on rev strand
                    from = this.matchNextOccurrence(seq, this.patternRev);
                } else {
                from += start;
                }
            } else {
                from += start;
            }
        }
        return from;
    }

    /**
     * Identifies pattern "p" in the given "sequence" and stores positive results
     * in this class' region list.
     * @param sequence the sequence to analyse
     * @param p pattern to search for
     * @param isForwardStrand if pattern is fwd or rev
     * @param offset offset needed for storing the correct region positions
     * @param restricted determining if the visualization should be restricted to a certain frame
     */
    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand,
            int offset) { //, boolean restricted){
        // match forward
//        final boolean codonFwdStrand = this.frameCurrFeature > 0 ? true : false;
//        if (!restricted || restricted && codonFwdStrand == isForwardStrand){
        Matcher m = p.matcher(sequence);
        while (m.find()) {
            int from = m.start();
            int to = m.end() - 1;
//                if (restricted) {
//                    final int start = absStart - offset + from + 1;
//                    if (((start % 3) + 1 == this.frameCurrFeature || -(start % 3) == (-this.frameCurrFeature) - 3)) {
//                        regions.add(new Region(start, absStart - offset + to + 1, isForwardStrand));
//                    }
//                } else {
            this.matchedPatterns.add(new Region(absStart - offset + from + 1, absStart - offset + to + 1, isForwardStrand));
//                }
        }
//        }
    }
    
        /**
     * Identifies pattern "p" in the given "sequence" and stores positive results
     * in this class' region list.
     * @param sequence the sequence to analyse
     * @param p pattern to search for
     * @param isForwardStrand if pattern is fwd or rev
     * @param offset offset needed for storing the correct region positions
     * @param restricted determining if the visualization should be restricted to a certain frame
     */
    private int matchNextOccurrence(String sequence, Pattern p) {

        Matcher m = p.matcher(sequence);
        if (m.find()) {
            return m.start();
        }
        return -1;
    }

    @Override
    public List<Region> findRegions() {
        this.matchedPatterns.clear();
        this.findPatternInInterval();
        return this.matchedPatterns;
    }

    @Override
    public void setInterval(int start, int stop) {
        this.absStart = start;
        this.absStop = stop;
    }

    /**
     * @param pattern Pattern to search for
     */
    public final void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
        this.patternRev = Pattern.compile(SequenceUtils.complementDNA(SequenceUtils.reverseString(pattern)));
    }
//    public int getFrameCurrFeature() {
//        return this.frameCurrFeature;
//    }
//    /**
//     * Sets the data needed for the current feature. Currently only the frame is
//     * necessary. This always has to be set first in case the action should only
//     * show start codons of the correct frame.
//     * @param frameCurrFeature the frame of the currently selected feature
//     */
//    public void setCurrFeatureData(int frameCurrFeature) {
//        this.frameCurrFeature = frameCurrFeature;
//    }
}
