package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * Counts the differences to the reference sequence for a cigar string. 
     * This only works, if no "M" operation is used in the cigar! But it is more
     * efficient, than the other version of this method including read and reference
     * sequences.
     * @param cigar the cigar string containing the alignment operations
     */
    public static int countDifferencesToRef(String cigar) throws NumberFormatException {
        
        int differences = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String c;
        for (int i = 0; i < charCigar.length; ++i) {
            c = charCigar[i];
            if (c.matches(cigarRegex)) {
                if (c.equals("X") || c.equals("D") || c.equals("N") || c.equals("P") || c.equals("S")) {
                    differences += Integer.valueOf(num[i - 1]);
                } //P and H = padding and hard clipping do not contribute to differences
            }
        }
        
        return differences;
    }
    
    /**
     * Counts the differences to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the cigar,
     * then the read and reference sequence can be null (it is not used in this case).
     * @param cigar the cigar string containing the alignment operations
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq the reference sequence belonging to the cigar and without gaps
     */
    public static int countDifferencesToRef(String cigar, String readSeq, String refSeq) throws NumberFormatException {
        
        int differences = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;
        String bases; //bases of the read interval under investigation
        int baseNo = 0;
        int count = 0;
        for (int i = 0; i < charCigar.length; ++i) {
            op = charCigar[i];
            if (op.matches(cigarRegex)) {
                count = Integer.valueOf(num[i - 1]);
                if (op.equals("X") || op.equals("D") || op.equals("I") 
                                   || op.equals("N") || op.equals("S")) {
                    differences += count;
                } else 
                if (op.equals("M")) {
                    bases = readSeq.substring(baseNo, baseNo + count);
                    for (int j = 0; j < bases.length(); ++j) {
                        if (bases.charAt(j) != refSeq.charAt(baseNo + j)) {
                            ++differences;
                        }
                    }
                    
                } //P and H = padding and hard clipping do not contribute to differences
                baseNo += count;
            }
        }
        
        return differences;
    }
    
    /**
     * Creaters diffs and gaps for a given read and reference sequence.
     * @param readSeq read whose diffs and gaps are calculated
     * @param refSeq reference sequence aligned to the read sequence
     * @param start start position on the whole chromosome (absolute position)
     * @param direction direction of the read
     * @return the diff and gap result for the read
     */
    public static DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction) {

        Map<Integer, Integer> gapOrderIndex = new HashMap<Integer, Integer>();
        List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
        List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();
        int errors = 0;
        int absPos;

        for (int i = 0, basecounter = 0; i < readSeq.length(); i++) {
            if (readSeq.toLowerCase().charAt(i) != refSeq.toLowerCase().charAt(i)) {
                ++errors;
                absPos = start + basecounter;
                if (refSeq.charAt(i) == '_') {
                    // store a lower case char, if this is a gap in genome
                    Character base = readSeq.charAt(i);
                    base = Character.toUpperCase(base);
                    if (direction == -1) {
                        base = SequenceUtils.getDnaComplement(base, readSeq);
                    }

                    ParsedReferenceGap gap = new ParsedReferenceGap(absPos, base, getOrderForGap(absPos, gapOrderIndex));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the upper case char from input file, if this is a modification in the read
                    char c = readSeq.charAt(i);
                    c = Character.toUpperCase(c);
                    if (direction == -1) {
                        c = SequenceUtils.getDnaComplement(c, readSeq);
                    }
                    ParsedDiff d = new ParsedDiff(absPos, c);
                    diffs.add(d);
                    basecounter++;
                }
            } else {
                basecounter++;
            }
        }

        return new DiffAndGapResult(diffs, gaps, gapOrderIndex, errors);
    }
        
    /**
     * This method calculates the order of the gaps. For a gap we don't include a new 
     * position in the reference genome, but we store the number of gaps for one 
     * position of the ref genome.
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
        int numberOfInsertions = 0;
        int numberofDeletion = 0;
        //int pos = 0;
        int readPos = 0;
        int softclipped = 0;

        
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        for (int i = 1; i < charCigar.length; i++) {
            String c = charCigar[i];
            String numOfBases = num[i - 1];
            if (c.matches(cigarRegex)) {

                if (c.equals("D") || c.equals("N") || c.equals("P")) {
                    //deletion of the read
                    numberofDeletion = Integer.parseInt(numOfBases);

                    refpos = refpos + numberofDeletion;

                    while (numberofDeletion > 0) {
                        if (readSeq.length() != readPos) {
                            readSeq = readSeq.substring(0, readPos).concat("_") + readSeq.substring(readPos, readSeq.length());
                        } else {
                            readSeq = readSeq.substring(0, readPos).concat("_");
                        }
                        numberofDeletion--;
                        newreadSeq = readSeq;
                        readPos  += 1;
                        //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq + "cigar" + cigar);
                    }

                } else if (c.equals("I") ) {
                    //insertion of the  read
                    numberOfInsertions = Integer.parseInt(numOfBases);

                    readPos = readPos + numberOfInsertions;
                    while (numberOfInsertions > 0) {

                        if (refpos != refSeq.length()) {
                            refSeq = refSeq.substring(0, refpos).concat("_") + refSeq.substring(refpos, refSeq.length());
                        } else {
                            refSeq = refSeq.substring(0, refpos).concat("_");
                        }
                        newRefSeqwithGaps = refSeq;
                        numberOfInsertions--;
                        refpos = refpos + 1;

                        //   Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read "+newreadSeq+" refseq "+ refSeq);
                    }
                } else if (c.equals("M") || c.equals("=") || c.equals("X")) {
                    //for match/mismatch thr positions just move forward
                    readPos += Integer.parseInt(numOfBases);
                    refpos +=Integer.parseInt(numOfBases);
                    newRefSeqwithGaps = refSeq;
                    newreadSeq = readSeq;
                } else if (c.equals("S") ) {
                    if (i > 1) {
                        //soft clipping of the last bases
                        newreadSeq = newreadSeq.substring(0, readSeq.length() - Integer.parseInt(numOfBases));
                    } else {
                        //soft clipping of the first bases
                        readPos += Integer.parseInt(numOfBases);
                        softclipped = Integer.parseInt(numOfBases);
                    }
                }
            } else {
                Logger.getLogger(ParserCommonMethods.class.getName()).log(Level.WARNING, NbBundle.getMessage(ParserCommonMethods.class, "CommonMethod.CIGAR ", c));
            }
        }
        newreadSeq = newreadSeq.substring(softclipped, newreadSeq.length());
        String[] refAndRead = new String[2];
        refAndRead[0] =newRefSeqwithGaps;
        refAndRead[1] =  newreadSeq;
        return  refAndRead;
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
               String[] num=  cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        for (int i=1;i<charCigar.length;i++) {
            String c = charCigar[i];
            String numOfBases = num[i-1];

                if (c.contains("D") || c.contains("N") || c.contains("P")) {
                    numberofDeletion += Integer.parseInt(numOfBases);
                } if(c.contains("I")) {
                    numberofInsertion += Integer.parseInt(numOfBases);
                }if(c.contains("S")){
                    numberofSoftclipped += Integer.parseInt(numOfBases);
                }
        }
        stopPosition = startPosition + readLength-1 + numberofDeletion - numberofInsertion-numberofSoftclipped;
        return stopPosition;
    }

        /**
     *  converts the the decimal number(flag) into binary code and checks if 4 is 1 or 0
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
     * converts the the decimal number into binary code and checks if 16 is 1 or 0
     * @param flag contains information wheater the read is mapped on the rev or fw stream
     * @return wheater the read is mapped on the rev or fw stream
     */
    public static boolean isForwardRead(int flag) {
        boolean isForward = true;
        if (flag >= 16) {
            String binaryValue = Integer.toBinaryString(flag);
            int binaryLength = binaryValue.length();
            String b = binaryValue.substring(binaryLength - 5, binaryLength - 4);

            if (b.equals("1")) {
                isForward = false;
            } else {
                isForward = true;
            }
        }
        return isForward;
    }
    
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
        }
        if (snp == 2) {
            beforeSNP = genome.substring(snp - 2, snp);
        }
        if (snp == 3) {
            beforeSNP = genome.substring(snp - 3, snp);
        }
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
