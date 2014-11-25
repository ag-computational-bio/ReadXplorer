package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SequenceUtils;
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
    private static final int INTERVAL_SIZE = 3000000;
    private List<Region> matchedPatterns;
    private int absStart;
    private int absStop;
    private PersistantReference refGen;
    private String sequence;
    private Pattern pattern;
//    private Pattern patternRev;

    public PatternFilter(int absStart, int absStop, PersistantReference refGen) {
        this.matchedPatterns = new ArrayList<>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
    }

    /**
     * Identifies the currently set pattern in a given interval of the reference
     * sequence stored in this object.
     * @return A list of the positions where the pattern matched along the interval.
     */
    private List<Region> findPatternInInterval() {

        if (this.pattern != null && !this.pattern.toString().isEmpty()) {

            int offset = this.pattern.toString().length(); //shift by pattern length to left
            int start = this.absStart - offset;
            int stop = this.absStop + offset - 1;

            if (start <= 0 ) {
                offset -= Math.abs(start);
                start = 1;
            }
            if (stop > this.refGen.getActiveChromLength()) {
                stop = this.refGen.getActiveChromLength();
            }

            for (int i = start; i <= stop; i++) {
                if (i + INTERVAL_SIZE <= stop) {
                    this.sequence = refGen.getActiveChromSequence(i, i + INTERVAL_SIZE);
                } else {
                    this.sequence = refGen.getActiveChromSequence(i, stop);
                }
                this.matchPattern(this.sequence, this.pattern, true, offset);
                this.sequence = SequenceUtils.getReverseComplement(this.sequence);
                this.matchPattern(this.sequence, this.pattern, false, offset);
                i += INTERVAL_SIZE;
            }
        }
        return this.matchedPatterns;

    }

    /**
     * Identifies next (closest) occurrence from either forward or reverse strand of a pattern 
     * in the current reference genome.
     * @return the position of the next occurrence of the pattern
     */
    public int findNextOccurrence() {
        
        int refLength = this.refGen.getActiveChromosome().getLength();
        int from = -1;
        int from2;
        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {

            int start = this.absStop;
            
            if (this.absStart <= 0) {
                this.absStart = 1;
            }
            if (start > refLength) {
                start = 1;
            }

            String seq;
            for (int i = start; i <= refLength; i++) {
                if (i + INTERVAL_SIZE <= refLength) {
                    seq = refGen.getActiveChromSequence(i, i + INTERVAL_SIZE);
                } else {
                    seq = refGen.getActiveChromSequence(i, refLength);
                }
                i += INTERVAL_SIZE;
                String seqRev = SequenceUtils.getReverseComplement(seq);

                //at first search from current position till end of sequence on both frames
                from = this.matchNextOccurrence(seq, this.pattern);
                from2 = this.matchNextOccurrence(seqRev, this.pattern);

                //then search from 0 to current position on both frames
                if (from == -1 && from2 == -1 && start > 0) {
                    for (int j = 1; j <= start; i++) {
                        if (from == -1 && from2 == -1 && start > 0) {
                            if (j + INTERVAL_SIZE <= start) {
                                seq = refGen.getActiveChromSequence(j, j + INTERVAL_SIZE);
                            } else {
                                seq = refGen.getActiveChromSequence(j, start);
                            }
                            j += INTERVAL_SIZE;
                            seqRev = SequenceUtils.getReverseComplement(seq);

                            from = this.matchNextOccurrence(seq, this.pattern);
                            from2 = this.matchNextOccurrence(seqRev, this.pattern);
                        } else {
                            start = 1;
                            break;
                        }
                    }
                }

                if (from < from2 && from != -1 || from2 == -1 && from > from2) {
                    return from + start;
                } else if (from2 != -1) {
                    return seq.length() - from2 + start - 1;
                } else { /* both are -1*/ }
            }
        }
        return from;
    }
    
    /**
     * Identifies next (closest) occurrence from either forward or reverse strand of a pattern 
     * in the current reference genome.
     * @param isFwdStrand true, if the next occurrence on the fwd strand is needed
     * @return the position of the next occurrence of the pattern
     */
    public int findNextOccurrenceOnStrand(boolean isFwdStrand) {

        int refLength = this.refGen.getActiveChromosome().getLength();
        int from = -1;
        int start = this.absStart;
        if (!(this.pattern == null) && !this.pattern.toString().isEmpty()) {
            String seq;
            
            boolean isCorrectFrame = false;
            //at first search from current position till end of sequence on selected strand
            if (isFwdStrand) { //start with the stop pos of current codon
                while (++start < refLength && !isCorrectFrame) { //++, because otherwise we start at last start pos
                    for (int i = start; i <= refLength; i++) {
                        if (i + INTERVAL_SIZE <= refLength) {
                            seq = refGen.getActiveChromSequence(i, i + INTERVAL_SIZE);
                        } else {
                            seq = refGen.getActiveChromSequence(i, refLength);
                        }
                        i += INTERVAL_SIZE;
                        from = this.matchNextOccurrence(seq, this.pattern); // because we don't want index, but pos in genome
                        start += from;
                        isCorrectFrame = ((start) % 3 == this.absStart % 3);
                    }
                }
                ++start; //because we had fst pos of stop, then +2 and when exiting while loop -1. by +1 we move to last pos of stop
            } else { //reverse complement dna and start with the stop pos of current codon
                for (int i = 1; i <= absStart; i++) {
                    if (i + INTERVAL_SIZE <= refLength) { //sequence we start with
                        seq = refGen.getActiveChromSequence(i, i + INTERVAL_SIZE);
                    } else {
                        seq = refGen.getActiveChromSequence(i, absStart);
                    }
                    i += INTERVAL_SIZE;
                    String seqRev = SequenceUtils.getReverseComplement(seq);
                    int nextStart = 0;
                    int fromRev = 0;
                    while (nextStart < this.absStart && fromRev != -1 && !isCorrectFrame) {
                        seqRev = seqRev.substring(nextStart, seqRev.length());
                        fromRev = this.matchNextOccurrence(seqRev, this.pattern);
                        //reverse the position again to determine the pos in the total genome seq
                        from = seqRev.length() - fromRev;
                        isCorrectFrame = (from - this.absStart) % 3 == 0;
                        nextStart = fromRev + 1;
                    }
                    start = from - 2;
                }
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
    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand, int offset) {

        Matcher m = p.matcher(sequence);
        int from;
        int to;
        int end;
        while (m.find()) { //If you also want to find the second AAA in AAAA, then use m.find(lastFrom);
            from = m.start();
            to = m.end() - 1;
            if (isForwardStrand) {
                from = absStart - offset + from ;
                to = absStart - offset + to;
            } else {
                end = from;
                from = absStart - offset + sequence.length() - to - 1;
                to = absStart - offset + sequence.length() - end - 1;
            }
            this.matchedPatterns.add(new Region(from, to, isForwardStrand, Properties.PATTERN));
        }
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
//        this.patternRev = Pattern.compile(SequenceUtils.complementDNA(SequenceUtils.reverseString(pattern)));
    }
}
