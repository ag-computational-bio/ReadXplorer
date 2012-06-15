package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Sam/Bam parser for the data needed for a direct file access track. This means
 * the classification of the reads has to be carried out.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamDirectParser implements MappingParserI {

    private static String name = "SAM/BAM Direct Access Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam", "bam", "BAM", "Bam"};
    private static String fileDescription = "SAM/BAM Output";
    private List<Observer> observers;

    //TODO: think about statistic inclusion in some kind of ParsedMappingContainer?
    @Override
    public String getParserName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public Map<Integer, Pair<Integer, Integer>> parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {

        String filename = trackJob.getFile().getName();
        this.notifyObservers(NbBundle.getMessage(JokParser.class, "Parser.Parsing.Start", filename));

        int lineno = 0;
        int refSeqLength = sequenceString.length();

        /*
         * id of each read sequence. Since the same file is used in both
         * iterations we don't need to store a mapping between read seq and id.
         * We assign the same id to each read sequence in both iterations. If
         * the file would change inbetween, this would not work!
         */
        String lastReadSeq = "";
        int seqId = -1;
        int lowestDiffRate = Integer.MAX_VALUE; //lowest error rate for this sequence id - defines best match mappings
        int noMatches = -1; //total number of matches for this sequence id
        Map<Integer, Pair<Integer, Integer>> dataMap = new HashMap<Integer, Pair<Integer, Integer>>();

        String refSeq = null;
        int start;
        int stop;
        int differences = 0;
        String readSeq = null;
        String cigar = null;


        SAMFileReader sam = new SAMFileReader(trackJob.getFile());
        SAMRecordIterator samItor = sam.iterator();

        SAMRecord record;
        while (samItor.hasNext()) {
            ++lineno;
            try {

                record = samItor.next();
                if (!record.getReadUnmappedFlag()) {

                    cigar = record.getCigarString();
                    readSeq = record.getReadString().toLowerCase();

                    if (readSeq == null || readSeq.isEmpty()) {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                "Parser.checkMapping.ErrorReadEmpty", filename, lineno, readSeq));
                        continue;
                    }
                    if (!cigar.matches("[MHISDPXN=\\d]+")) {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                "Parser.checkMapping.ErrorCigar", cigar, filename, lineno));
                        continue;
                    }

                    // increase seqId for new read sequence and reset other fields
                    if (!lastReadSeq.equals(readSeq)) {
                        dataMap.put(seqId, new Pair<Integer, Integer>(noMatches, lowestDiffRate));
                        ++seqId;
                        noMatches = 1;
                        lowestDiffRate = Integer.MAX_VALUE;
                    } else { //meaning lastReadSeq.equals(readSeqWithoutGaps)
                        ++noMatches;
                    }
                    lastReadSeq = readSeq;


                    /*
                     * The cigar values are as follows: 0 (M) = alignment match
                     * (both, match or mismatch), 1 (I) = insertion, 2 (D) =
                     * deletion, 3 (N) = skipped, 4 (S) = soft clipped, 5 (H) =
                     * hard clipped, 6 (P) = padding, 7 (=) = sequene match, 8
                     * (X) = sequence mismatch. H not needed, because these
                     * bases are not present in the read sequence!
                     */
                    //count differences to reference
                    if (cigar.contains("M")) {
                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();

                        if (refSeqLength < start || refSeqLength < stop) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                    "Parser.checkMapping.ErrorReadPosition",
                                    filename, lineno, start, stop, refSeqLength));
                            continue;
                        }
                        if (start >= stop) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                    "Parser.checkMapping.ErrorStartStop", filename, lineno, start, stop));
                            continue;
                        }

                        refSeq = sequenceString.substring(start - 1, stop).toLowerCase(); //TODO: test if -1 is correct

                        if (refSeq == null || refSeq.isEmpty()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                    "Parser.checkMapping.ErrorRef", filename, lineno, refSeq));
                            continue;
                        }
                        if (readSeq.length() != refSeq.length()) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                    "Parser.checkMapping.ErrorReadLength", filename, lineno, readSeq, refSeq));
                            continue;
                        }

                        differences = ParserCommonMethods.countDifferencesToRef(cigar, readSeq, refSeq);


                    } else //the convenient case, that no "M"'s are present in the cigar
                    if (cigar.contains("X") || cigar.contains("D") || cigar.contains("I")
                            || cigar.contains("S") || cigar.contains("N") || cigar.contains("P")) {

                        differences = ParserCommonMethods.countDifferencesToRef(cigar);

                    } else {
                        differences = 0;
                    }

                    if (differences < lowestDiffRate) {
                        lowestDiffRate = differences;
                    }

                    //saruman starts genome at 0 other algorithms like bwa start genome at 1

                } else {
                    this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                            "Parser.Parsing.CorruptData", lineno, record.getReadName()));
                }
            } catch (RuntimeEOFException e) {
                continue; //skip current incomplete read
            }
        }


        samItor.close();
        sam.close();
        dataMap.remove(-1); //the first entry with -1 has to be thrown away at the end

        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Finished", filename));
        this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Successfully"));

        return dataMap;
    }

    @Override
    public void processReadname(int seqID, String readName) {
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }
}
