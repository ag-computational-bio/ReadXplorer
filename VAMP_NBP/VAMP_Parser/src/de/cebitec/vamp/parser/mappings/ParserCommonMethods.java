package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedClassification;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTag;
import org.openide.util.NbBundle;

/**
 * Contains the methods which can be useful for more than one specific parser.
 * 
 * @author jstraube, Rolf Hilker
 */
public final class ParserCommonMethods {
    
    /*
     * The cigar values are as follows: 0 (M) = alignment match (both, match or
     * mismatch), 1 (I) = insertion, 2 (D) = deletion, 3 (N) = skipped (=
     * ACG...TCG = 3 skipped bases), 4 (S) = soft clipped, 5 (H) = hard clipped
     * (not needed, because these bases are not present in the read sequence!),
     * 6 (P) = padding (a gap in the reference, which is also found in the
     * current record, but there is at least one record, that has an insertion
     * at that position), 7 (=) = sequene match, 8 (X) = sequence mismatch.
     */
    //option H-hard clipped not necessary, because it does not count into the alignment
    public static final String cigarRegex = "[MIDNSPX=]+";
    
    private ParserCommonMethods(){
        
    }
    
    /**
     * Counts the differences to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the
     * cigar, then the read and reference sequence can be null (it is not used
     * in this case). Read and reference sequence are treated case
     * insensitively, so there is no need to transform the case beforehand.
     * @param cigar the cigar string containing the alignment operations
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq the reference sequence area belonging to the cigar and
     * without gaps
     * @param isRevStrand true, if the ref seq has to be reverse complemented,
     * false if the read is on the fwd strand.
     * @return diff and gap result for the read and reference seq pair
     * @throws NumberFormatException
     */
    public static int countDiffsAndGaps(String cigar, String readSeq, String refSeq, boolean isRevStrand) throws NumberFormatException {

        int differences = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;
        String bases; //bases of the read interval under investigation
        int currentCount;
        int refPos = 0;
        int readPos = 0;
        int diffPos;
        if (!refSeq.isEmpty()) {
            readSeq = readSeq.toUpperCase();
            refSeq = refSeq.toUpperCase();
        }

        for (int i = 0; i < charCigar.length; ++i) {
            op = charCigar[i];

            if (op.equals("=")) { //increase position for matches, skipped regions (N) and padded regions (P)
                currentCount = Integer.valueOf(num[i - 1]);
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("N") || op.equals("P")) {
                refPos += Integer.valueOf(num[i - 1]);
                
            } else if (op.equals("X")) { //count and create diffs for mismatches
                currentCount = Integer.valueOf(num[i - 1]);
                differences += currentCount;
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("D")) { // count and add diff gaps for deletions in read
                currentCount = Integer.valueOf(num[i - 1]);
                differences += currentCount;
                refPos += currentCount;

            } else if (op.equals("I")) { // count and add reference gaps for insertions
                currentCount = Integer.valueOf(num[i - 1]);
                differences += currentCount;
                readPos += currentCount;
                // refPos remains the same

            } else if (op.equals("M")) { //check, count and add diffs for deviating Ms
                currentCount = Integer.valueOf(num[i - 1]);
                bases = readSeq.substring(readPos, readPos + currentCount);
                for (int j = 0; j < bases.length(); ++j) {
                    diffPos = refPos + j;
                    if (bases.charAt(j) != refSeq.charAt(diffPos)) {
                        ++differences;
                    }
                }
                refPos += currentCount;
                readPos += currentCount;

            } //H and S = hard and soft clipped bases are not present in the read string and pos in record, so don't inc. absPos
        }

        return differences;
    }
    
    /**
     * Counts the differences to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the
     * cigar, then the read and reference sequence can be null (it is not used
     * in this case). Read and reference sequence are treated case
     * insensitively, so there is no need to transform the case beforehand. All
     * cigar operations need to be uppercase!
     * @param cigar the cigar string containing the alignment operations
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq the reference sequence area belonging to the cigar and
     * without gaps
     * @param isRevStrand true, if the ref seq has to be reverse complemented,
     * false if the read is on the fwd strand.
     * @param start start of the alignment of read and reference in the reference
     * @return diff and gap result for the read and reference seq pair
     * @throws NumberFormatException
     */
    public static DiffAndGapResult createDiffsAndGaps(String cigar, String readSeq, String refSeq, boolean isRevStrand, int start) throws NumberFormatException {

        Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        List<ParsedDiff> diffs = new ArrayList<>();
        List<ParsedReferenceGap> gaps = new ArrayList<>();
        int differences = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;
        String bases; //bases of the read interval under investigation
        int currentCount;
        int refPos = 0;
        int readPos = 0;
        int diffPos;
        char base;
        if (!refSeq.isEmpty()) {
            readSeq = readSeq.toUpperCase();
            refSeq = refSeq.toUpperCase();
        }
        
        for (int i = 1; i < charCigar.length; ++i) {
            op = charCigar[i];
            currentCount = Integer.valueOf(num[i - 1]);
            if (op.equals("M")) { //check, count and add diffs for deviating Ms
                bases = readSeq.substring(readPos, readPos + currentCount);
                for (int j = 0; j < bases.length(); ++j) {
                    diffPos = refPos + j;
                    base = bases.charAt(j);
                    if (base != refSeq.charAt(diffPos)) {
                        ++differences;
                        if (isRevStrand) {
                            base = SequenceUtils.getDnaComplement(base);
                        }
                        diffs.add(new ParsedDiff(diffPos + start, base));
                    }
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("=")) { //only increase position for matches
                refPos += currentCount;
                readPos += currentCount;
               
            } else if (op.equals("X")) { //count and create diffs for mismatches
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    diffPos = readPos + j;
                    base = readSeq.charAt(diffPos);
                    if (isRevStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    diffs.add(new ParsedDiff(diffPos + start, base));
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("D")) { // count and add diff gaps for deletions in read
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    diffs.add(new ParsedDiff(refPos + j + start, '_'));
                }
                refPos += currentCount;
                // readPos remains the same
            
            } else if (op.equals("I")) { // count and add reference gaps for insertions
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    base = readSeq.charAt(readPos + j);
                    if (isRevStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    gaps.add(new ParsedReferenceGap(refPos + start, base, getOrderForGap(refPos + start, gapOrderIndex)));
                }
                //refPos remains the same
                readPos += currentCount;

            } else if (op.equals("N")) {
                refPos += currentCount;
                //readPos remains the same

            } else if (op.equals("S")) {
                //refPos remains the same
                readPos += currentCount;
            } //P, S and H = padding, soft and hard clipping do not contribute to differences
        }

        return new DiffAndGapResult(diffs, gaps, differences);
    }
    
    /**
     * Creates diffs and gaps for a given read and reference sequence. Both are
     * treated case insensitively, so there is no need to transform the case
     * beforehand.
     * @param readSeq read whose diffs and gaps are calculated
     * @param refSeq reference sequence aligned to the read sequence
     * @param start start position on the whole chromosome (absolute position)
     * @param direction direction of the read
     * @return the diff and gap result for the read
     */
    public static DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction) {

        Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        List<ParsedDiff> diffs = new ArrayList<>();
        List<ParsedReferenceGap> gaps = new ArrayList<>();
        int errors = 0;
        char base;
        ParsedReferenceGap gap;
        ParsedDiff diff;
        readSeq = readSeq.toUpperCase();
        refSeq = refSeq.toUpperCase();

        for (int i = 0; i < readSeq.length(); i++) {
            if (readSeq.charAt(i) != refSeq.charAt(i)) {
                ++errors;
                base = readSeq.charAt(i);
                if (direction == SequenceUtils.STRAND_REV) {
                    base = SequenceUtils.getDnaComplement(base);
                }
                if (refSeq.charAt(i) == '_') {
                    // store a lower case char, if this is a gap in genome
                    gap = new ParsedReferenceGap(start, base, getOrderForGap(start, gapOrderIndex));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the char from input file, if this is a modification in the read
                    diff = new ParsedDiff(start, base);
                    diffs.add(diff);
                    ++start;
                }
            } else {
                ++start;
            }
        }

        return new DiffAndGapResult(diffs, gaps, errors);
    }
        
    /**
     * This method calculates the order of the gaps. For a gap we don't include a new 
     * position in the reference genome, but we store the number of gaps for one 
     * position of the ref genome.
     * @param gapPos position of the gap
     * @param gapOrderIndex the gap order index for the current gap (larger the more gaps
     *      in a row
     * @return the new gap order index for the gap
     */
    public static int getOrderForGap(int gapPos, Map<Integer, Integer> gapOrderIndex) {
        if (!gapOrderIndex.containsKey(gapPos)) {
            gapOrderIndex.put(gapPos, 0);
        }
        int order = gapOrderIndex.get(gapPos);

        // increase order for next request
        gapOrderIndex.put(gapPos, order + 1);

        return order;
    }
    

    /**
     * This method tries to convert the cigar string to the mapping again because
     * SAM format has no other mapping information
     * @param cigar contains mapping information of reference and read sequence
     * M can be a Match or Mismatch, D is a deletion on the read, I insertion on the read, S softclipped read
     * @param refSeq
     * @param readSeq
     * @return the refSeq with gaps in fact of insertions in the reads
     * XXXTODO:CHeck this
     */
    public static String[] createMappingOfRefAndRead(String cigar, String refSeq, String readSeq) {
        String newRefSeqwithGaps = null;
        String newreadSeq = null;
    
        int refpos = 0;
        int numberOfInsertions;
        int numberofDeletion;
        int readPos = 0;
        int softclipped = 0;

        
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;
        String numOfBases;
        for (int i = 1; i < charCigar.length; i++) {
            op = charCigar[i];
            numOfBases = num[i - 1];

            if (op.equals("D") || op.equals("N") || op.equals("P")) {
                //deletion of the read
                numberofDeletion = Integer.parseInt(numOfBases);

                refpos += numberofDeletion;

                while (numberofDeletion > 0) {
                    if (readSeq.length() != readPos) {
                        readSeq = readSeq.substring(0, readPos).concat("_") + readSeq.substring(readPos, readSeq.length());
                    } else {
                        readSeq = readSeq.substring(0, readPos).concat("_");
                    }
                    --numberofDeletion;
                    newreadSeq = readSeq;
                    ++readPos;
                    //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq + "cigar" + cigar);
                }

            } else if (op.equals("I")) {
                //insertion of the  read
                numberOfInsertions = Integer.parseInt(numOfBases);

                readPos += numberOfInsertions;
                while (numberOfInsertions > 0) {

                    if (refpos != refSeq.length()) {
                        refSeq = refSeq.substring(0, refpos).concat("_") + refSeq.substring(refpos, refSeq.length());
                    } else {
                        refSeq = refSeq.substring(0, refpos).concat("_");
                    }
                    newRefSeqwithGaps = refSeq;
                    --numberOfInsertions;
                    ++refpos;

                    //   Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq);
                }
                
            } else if (op.equals("M") || op.equals("=") || op.equals("X")) {
                //for match/mismatch thr positions just move forward
                readPos += Integer.parseInt(numOfBases);
                refpos += Integer.parseInt(numOfBases);
                newRefSeqwithGaps = refSeq;
                newreadSeq = readSeq;
                
            } else if (op.equals("S")) {
                if (i > 1) {
                    //soft clipping of the last bases
                    newreadSeq = newreadSeq.substring(0, readSeq.length() - Integer.parseInt(numOfBases));
                } else {
                    //soft clipping of the first bases
                    readPos += Integer.parseInt(numOfBases);
                    softclipped = Integer.parseInt(numOfBases);
                }
            } else {
                Logger.getLogger(ParserCommonMethods.class.getName()).log(Level.WARNING, NbBundle.getMessage(ParserCommonMethods.class, "CommonMethod.CIGAR ", op));
            }
        }
        newreadSeq = newreadSeq.substring(softclipped, newreadSeq.length());
        String[] refAndRead = new String[2];
        refAndRead[0] = newRefSeqwithGaps;
        refAndRead[1] =  newreadSeq;
        return  refAndRead;
    }

    /**
     * Checks a read for common properties: <br>1. Empty or null read sequence
     * <br>2. mapping beyond the reference sequence length or to negative
     * positions <br>3. a start position larger than the stop position
     * @param parent the parent observable to receive messages
     * @param readSeq the read sequence
     * @param refSeqLength the length of the reference sequence
     * @param start the start of the mapping
     * @param stop the stop of the mapping
     * @param filename the file name of which the mapping originates
     * @param lineno the line number in the file
     * @return true, if the read is consistent, false otherwise
     */
    public static boolean checkRead(Observable parent,
            String readSeq,
            int refSeqLength,
            int start,
            int stop,
            String filename,
            int lineno) {
        boolean isConsistent = true;
        if (readSeq == null || readSeq.isEmpty()) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorReadEmpty", filename, lineno, readSeq));
            isConsistent = false;
        }
        if (refSeqLength < start || refSeqLength < stop) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorReadPosition",
                    filename, lineno, start, stop, refSeqLength));
            isConsistent = false;
        }
        if (start >= stop) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorStartStop", filename, lineno, start, stop));
            isConsistent = false;
        }

        return isConsistent;
    }
    
    /**
     * Checks a read for common properties: 
     * <br>1. Empty or null read sequence
     * <br>2. mapping beyond the reference sequence length or to negative
     * positions 
     * <br>3. a start position larger than the stop position
     * <br>4. an error in the cigar string
     * @param parent the parent observable to receive messages
     * @param readSeq the read sequence
     * @param refSeqLength the length of the reference sequence
     * @param cigar the cigar of the mapping
     * @param start the start of the mapping
     * @param stop the stop of the mapping
     * @param filename the file name of which the mapping originates
     * @param lineno the line number in the file
     * @return true, if the read is consistent, false otherwise
     */
    public static boolean checkReadSam(
            Observable parent, 
            String readSeq, 
            int refSeqLength, 
            String cigar,
            int start, 
            int stop, 
            String filename, 
            int lineno) {
        
        boolean isConsistent = ParserCommonMethods.checkRead(parent, readSeq, refSeqLength, start, stop, filename, lineno);
        if (!cigar.matches("[MHISDPXN=\\d]+")) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorCigar", cigar, filename, lineno));
            isConsistent = false;
        }
        
        return isConsistent;
    } 
    
    /**
     * Checks a read for common properties: 
     * <br>1. Empty or null read sequence
     * <br>2. mapping beyond the reference sequence length or to negative
     * positions 
     * <br>3. a start position larger than the stop position
     * <br>4. an empty read name
     * <br>5. an empty refrence sequence
     * <br>6. an unknown mapping orientation
     * @param parent the parent observable to receive messages
     * @param readSeq the read sequence
     * @param readname the name of the read
     * @param refSeq reference sequence beloning to the mapping (not the complete reference genome)
     * @param refSeqLength the length of the reference sequence
     * @param start the start of the mapping
     * @param stop the stop of the mapping
     * @param direction direction of the mapping
     * @param filename the file name of which the mapping originates
     * @param lineno the line number in the file
     * @return true, if the read is consistent, false otherwise
     */
    public static boolean checkReadJok(
            Observable parent,
            String readSeq,
            String readname,
            String refSeq,
            int refSeqLength,
            int start,
            int stop,
            int direction,
            String filename,
            int lineno) {
        
        boolean isConsistent = ParserCommonMethods.checkRead(parent, readSeq, refSeqLength, start, stop, filename, lineno);
        
        if (readname == null || readname.isEmpty()) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorReadname", filename, lineno, readname));
            isConsistent = false;
        }
        if (direction == 0) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorDirectionJok", filename, lineno));
            isConsistent = false;
        }
        if (refSeq == null || refSeq.isEmpty()) {
            parent.notifyObservers(NbBundle.getMessage(ParserCommonMethods.class,
                    "Parser.checkMapping.ErrorRef", filename, lineno, refSeq));
            isConsistent = false;
        }
        
        return isConsistent;
    }

    /**
     * In fact deletions in the read shift the stop position of the
     * ref genome mapping, we need to count the number of deletions to calculate
     * the stop position of the read in the ref genome.
     * @param cigar contains mapping information
     * @param startPosition of the mapping
     * @param readLength the length of the read
     * @return
     */
    public static int countStopPosition(String cigar, Integer startPosition, Integer readLength) {
        int stopPosition;
        int numberofDeletion = 0;
        int numberofInsertion = 0;
        int numberofSoftclipped = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;
        String numOfBases;
        for (int i = 1; i < charCigar.length; i++) {
            op = charCigar[i];
            numOfBases = num[i - 1];
            if (op.contains("D") || op.contains("N") || op.contains("P")) {
                numberofDeletion += Integer.parseInt(numOfBases);
            }
            if (op.contains("I")) {
                numberofInsertion += Integer.parseInt(numOfBases);
            }
            if (op.contains("S")) {
                numberofSoftclipped += Integer.parseInt(numOfBases);
            }
        }
        stopPosition = startPosition + readLength - 1 + numberofDeletion - numberofInsertion - numberofSoftclipped;
        return stopPosition;
    }

    /**
     * Returns the read name without the pair tag.
     * @param readNameFull The read name whose pair tag shall be removed
     * @return The read name without its pair tag
     */
    public static String getReadNameWithoutPairTag(String readNameFull) {
        String readName;
        String[] nameParts = readNameFull.split(" ");
        if (nameParts.length == 2 && (nameParts[1].startsWith("1") || nameParts[1].startsWith("2"))) {
            readName = nameParts[0];
        } else {
            readName = readNameFull.substring(0, readNameFull.length() - 2);
        }

        return readName;
    }
    
    /**
     * Calculates the pair tag for a given sam record. It checks the ending of
     * the read name, the ReadPairedFlag of the sam record and the casava > 1.8
     * format for an appropriate pair flag until it is found. If no paired read
     * tag can be found, the pair tag returns a neutral pairTag for single
     * end mapped reads.
     * @param record the record to check for a pair tag
     * @return Either '1' for first read of pair, '2' for second read of pair
     * or '0' for a single end mapping
     */
    public static char getReadPairTag(SAMRecord record) {
        String readName = record.getReadName();
        char pairTag = '0';
        char lastChar = readName.charAt(readName.length() - 1);
        
        if (lastChar == Properties.EXT_A1 || lastChar == Properties.EXT_B1) {
            pairTag = '1';
        
        } else if (lastChar == Properties.EXT_A2 || lastChar == Properties.EXT_B2) {
            pairTag = '2';
            
        } else if (lastChar != Properties.EXT_A1 && lastChar != Properties.EXT_B1
                && lastChar != Properties.EXT_A2 && lastChar != Properties.EXT_B2) {
            
            //check for casava > 1.8 paired read
            String[] nameParts = readName.split(" ");
            if (nameParts.length == 2) {
                if (nameParts[1].startsWith("1")) {
                    pairTag = '1';
                } else if (nameParts[1].startsWith("2")) {
                    pairTag = '2';
                }
            }
            
            //if tag is not set yet, but paired read flag is set, we can use it
            if (pairTag == 0 && record.getReadPairedFlag()) { 
                pairTag = record.getFirstOfPairFlag() ? '1' : '2';
            }
        }
        return pairTag;
    }
    
    /**
     * Checks if a read name is written in the casava format > 1.8, in which
     * the pair tag appears after a blank in the read name.
     * @param readName read name to check
     * @return true, if the read is in the casava format > 1.8, false otherwise
     */
    public static boolean isCasavaLarger1Dot8Format(String readName) {
        String[] nameParts = readName.split(" ");
        if (nameParts.length == 2 && (nameParts[1].startsWith("1") || nameParts[1].startsWith("2"))) {
            return true;
        }
        return false;
    }
    
    /**
     * Adds a "1" or "2" at the end of the given records read name, if it is a
     * paired read and does not already contain a paired read ending.
     * @param record the record whose read name should be elongated, if it is a
     * paired read
     * @return The elongated read name or the original one, if it already had a
     * paired read ending
     */
    public static String elongatePairedReadName(SAMRecord record) {
        String readName = record.getReadName();
        char pairTag = readName.charAt(readName.length() - 1);
        if (record.getReadPairedFlag() && pairTag != Properties.EXT_A1 && pairTag != Properties.EXT_B1
                                       && pairTag != Properties.EXT_A2 && pairTag != Properties.EXT_B2
                                       && !isCasavaLarger1Dot8Format(readName)) {
            readName = readName.concat(record.getFirstOfPairFlag() ? "1" : "2");
        }
        return readName;
    }

    /**
     * Converts the the decimal number(flag) into binary code and checks if 4 is 1 or 0
     * @param flag
     * @param startPosition of mapping
     * @return
     */
    public static boolean isMappedSequence(int flag, int startPosition) {
        boolean isMapped = true;
        if (flag >= 4) {
            String binaryValue = Integer.toBinaryString(flag);
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring(binaryLength - 3, binaryLength - 2);

            if (b.equals("1") || startPosition == 0) {
                isMapped = false;
            } else {
                isMapped = true;
            }
        }
        return isMapped;
    }
    /**
     * Adds the classification data (type and number of mapped positions) to the
     * sam record. Use this method, if the number of differences is already
     * known. No data is added to reads, which do not occur in the 
     * classification map.
     * @param readName the complete read name
     * @param record the sam record to update
     * @param differences the number of differences the record has to the
     * reference
     * @param classificationMap the classification map from which the 
     * classification data has to be retrieved
     * @throws AssertionError
     */
    public static void addClassificationData(SAMRecord record, int differences,
            Map<String, ParsedClassification> classificationMap) throws AssertionError {
        
        String readName = ParserCommonMethods.elongatePairedReadName(record);
        ParsedClassification classification;
        int nextMappingPos;
        if (classificationMap.get(readName) != null) {
            classification = classificationMap.get(readName);
            int lowestDiffRate = classification.getMinMismatches();

            nextMappingPos = classification.getNextMappingStart(record.getAlignmentStart());

            if (nextMappingPos > 0) {
                record.setAttribute(SAMTag.CP.name(), nextMappingPos);
                record.setAttribute(SAMTag.CC.name(), "=");
            }

            if (differences == 0) { //perfect mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.PERFECT_COVERAGE);

            } else if (differences == lowestDiffRate) { //best match mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.BEST_MATCH_COVERAGE);

            } else if (differences > lowestDiffRate) { //common mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.COMPLETE_COVERAGE);

            } else { //meaning: differences < lowestDiffRate
                throw new AssertionError("Cannot contain less than the lowest diff rate number of errors!");
            }
            record.setAttribute(Properties.TAG_MAP_COUNT, classification.getNumberOccurrences());
        } else {
            //currently no data is added to reads with errors, since they are not contained in the classification map
        }
    }
    
//    /**
//     * converts the the decimal number into binary code and checks if 16 is 1 or 0
//     * @param flag contains information wheater the read is mapped on the rev or fw strand
//     * @return wheater the read is mapped on the rev or fw strand
//     */
//    public static boolean isForwardRead(int flag) {
//        boolean isForward = true;
//        if (flag >= 16) {
//            String binaryValue = Integer.toBinaryString(flag);
//            int binaryLength = binaryValue.length();
//            String b = binaryValue.substring(binaryLength - 5, binaryLength - 4);
//
//            if (b.equals("1")) {
//                isForward = false;
//            } else {
//                isForward = true;
//            }
//        }
//        return isForward;
//    }
    
    
    
    /**
     * TODO: Can be used for homopolymer snp detection, to flag snps in homopolymers. needed?
     * @param genome
     * @param snp
     * @return 
     */
        public static boolean snpHasStretch(String genome, int snp) {
        String beforeSNP = genome.substring(0, 1);

        if (snp == 1) {
            beforeSNP = genome.substring(snp - 1, snp);
        } else 
        if (snp == 2) {
            beforeSNP = genome.substring(snp - 2, snp);
        } else 
        if (snp == 3) {
            beforeSNP = genome.substring(snp - 3, snp);
        } else 
        if (snp >= 4) {
            beforeSNP = genome.substring(snp - 4, snp);
        }
        System.out.println("before" + beforeSNP);
        String afterSNP = genome.substring(snp, snp + 4);
        System.out.println("afterSnp:" + afterSNP);
        boolean hasStretch = false;
        if (beforeSNP.matches("[atgc]{4,8}") || afterSNP.matches("[atgc]{4,8}")) {
            hasStretch = true;
        }
        if (beforeSNP.matches("[atgc]{1,8}") && afterSNP.matches("[atgc]{3,8}")) {
            System.out.println("1-3" + hasStretch);
        }
        if (beforeSNP.matches("[atgc]{3,8}")) {
            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
            System.out.println("charbefore " + charBefore);
            String charAfter = afterSNP.substring(0, 1);
            String regex = charBefore.concat("{1}");
            if (charAfter.matches(regex)) {
                System.out.println("3-1" + hasStretch);
            }
        }
        if (afterSNP.matches("[atgc]{3,8}")) {
            String charBefore = beforeSNP.substring(beforeSNP.length() - 1, beforeSNP.length());
            String regex = afterSNP.substring(0, 1).concat("{1}");
            if (charBefore.matches(regex)) {
                System.out.println("3-1" + hasStretch + " " + regex);
            }
        }
        System.out.println(hasStretch);
        return hasStretch;
    }
}
