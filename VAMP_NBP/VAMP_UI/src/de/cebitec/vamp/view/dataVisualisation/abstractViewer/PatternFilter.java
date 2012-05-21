package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters for a given pattern in two ways: First for all
 * available occurrences of the pattern in a given interval of a given DNA sequence 
 * and second for the next occurrence of the pattern along a DNA sequence, such
 * as a whole genome sequence.
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
    //private int frameCurrAnnotation;
    private Pattern pattern;
    private Pattern patternRev;

    public PatternFilter(int absStart, int absStop, PersistantReference refGen) {
        this.matchedPatterns = new ArrayList<Region>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;

        //this.frameCurrAnnotation = StartCodonFilter.INIT; //because this is not a frame value
    }

    /**
     * Identifies the currently set pattern in a given interval of the reference
     * sequence stored in this objet.
     * @return A list of the positions where the pattern matched along the interval.
     */
    private List<Region> findPatternInInterval() {

        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {

            int offset = this.pattern.toString().length();
            int start = this.absStart - offset;
            int stop = this.absStop + this.pattern.toString().length()-1;

            if (start < 0 ) {
                offset -= Math.abs(start);
                start = 0;
            }
            if (stop > this.refGen.getSequence().length()) {
                stop = this.refGen.getSequence().length();
            }

            this.sequence = this.refGen.getSequence().substring(start, stop);
//            boolean isAnnotationSelected = this.frameCurrAnnotation != INIT;
            this.matchPattern(this.sequence, this.pattern, true, offset);//, isAnnotationSelected);
            this.matchPattern(this.sequence, this.patternRev, false, offset);//, isAnnotationSelected);
        }
        return this.matchedPatterns;

    }

    /**
     * Identifies next (closest) occurrence from either forward or reverse strand of a pattern 
     * in the current reference genome.
     * @return the position of the next occurrence of the pattern
     */
    public int findNextOccurrence() {

        int refLength = refGen.getSequence().length();
        int from = -1;
        int from2 = -1;
        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {

            int start = this.absStop;
            
            if (this.absStart < 0) {
                this.absStart = 0;
            }
            if (start > refLength) {
                start = 0;
            }

            String seq = refGen.getSequence().substring(start, refLength);
            
            //at first search from current position till end of sequence on both frames
            from = this.matchNextOccurrence(seq, this.pattern);
            from2 = this.matchNextOccurrence(seq, this.patternRev);
            
            //then search from 0 to current position on both frames
            if (from == -1 && from2 == -1 && start > 0){
                seq = refGen.getSequence().substring(0, start);
                start = 0;
                
                from = this.matchNextOccurrence(seq, this.pattern);
                from2 = this.matchNextOccurrence(seq, this.patternRev);
            }
            
            if (from < from2 && from != -1 || from2 == -1 && from > from2) {
                return from + start; //2.631.133
            } else if (from2 != -1) {
                return from2 + start;
            } else { /* both are -1*/ }
        }
        return from;
    }
    
    /**
     * Identifies next (closest) occurrence from either forward or reverse strand of a pattern 
     * in the current reference genome.
     * @return the position of the next occurrence of the pattern
     */
    public int findNextOccurrenceOnStrand(boolean isFwdStrand) {

        String genomeSeq = refGen.getSequence();
        int refLength = genomeSeq.length();
        int from = -1;
        int start = this.absStart;
        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {
            
            boolean isCorrectFrame = false;
            //at first search from current position till end of sequence on selected strand
            if (isFwdStrand) { //start with the stop pos of current codon
                while (++start < refLength && !isCorrectFrame) { //++, because otherwise we start at last start pos
                    String seq = genomeSeq.substring(start, refLength);
                    from = this.matchNextOccurrence(seq, this.pattern) + 1; // because we don't want index, but pos in genome
                    start += from;
                    isCorrectFrame = ((start) % 3 == this.absStart % 3) ? true : false;
                }
                ++start; //because we had fst pos of stop, then +2 and when exiting while loop -1. by +1 we move to last pos of stop
            } else { //reverse complement dna and start with the stop pos of current codon
                String seq = genomeSeq.substring(0, this.absStart); //sequence we start with
                String seqRev = SequenceUtils.getReverseComplement(seq).toLowerCase();
                int nextStart = 0;
                int fromRev = 0;
                while (nextStart < this.absStart && fromRev != -1 && !isCorrectFrame) {
                    seqRev = seqRev.substring(nextStart, seqRev.length());
                    fromRev = this.matchNextOccurrence(seqRev, this.pattern);
                    //reverse the position again to determine the pos in the total genome seq
                    from = seqRev.length() - fromRev;
                    isCorrectFrame = (from - this.absStart) % 3 == 0 ? true : false;
                    nextStart = fromRev + 1;
                }
                start = from - 2;
            }
            return start; 
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
     */
     //* @param restricted determining if the visualization should be restricted to a certain frame
    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand,
            int offset) { //, boolean restricted){
        // match forward
//        final boolean codonFwdStrand = this.frameCurrAnnotation > 0 ? true : false;
//        if (!restricted || restricted && codonFwdStrand == isForwardStrand){
        Matcher m = p.matcher(sequence);
        while (m.find()) {
            int from = m.start();
            int to = m.end() - 1;
//                if (restricted) {
//                    final int start = absStart - offset + from + 1;
//                    if (((start % 3) + 1 == this.frameCurrAnnotation || -(start % 3) == (-this.frameCurrAnnotation) - 3)) {
//                        regions.add(new Region(start, absStart - offset + to + 1, isForwardStrand));
//                    }
//                } else {
            this.matchedPatterns.add(new Region(absStart - offset + from + 1, absStart - offset + to + 1, isForwardStrand, Properties.PATTERN));
//                }
        }
//        }
    }
    
    /**
     * Identifies the next occurrence of pattern "p" in the given "sequence" and 
     * returns its position.
     * @param sequence the sequence to analyse
     * @param p pattern to search for
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
}
