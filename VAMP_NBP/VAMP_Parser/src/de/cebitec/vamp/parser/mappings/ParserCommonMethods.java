package de.cebitec.vamp.parser.mappings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojavax.bio.seq.ThinRichSequence;
import org.openide.util.NbBundle;

/**
 *
 * @author jstraube
 */
public final class ParserCommonMethods {
    
    
    //option H-hardclippt not necessary in fact that it does not count into the alignment
    final static String cigarRegex = "[MIDNSPX=]+";
    
    private ParserCommonMethods(){
        
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
    public static String[]  createMappingOfRefAndRead(String cigar, String refSeq, String readSeq) {
        String newRefSeqwithGaps = null;
        String newreadSeq = null;
    
        int refpos = 0;
        int numberOfInsertions = 0;
        int numberofDeletion = 0;
        //int pos = 0;
        int readPos = 0;
        int softclipped = 0;

        
       String[] num=  cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        for (int i=1;i<charCigar.length;i++) {
            String c = charCigar[i];
            String numOfBases = num[i-1];
            if (c.matches(cigarRegex))  {
                
                if (c.equals("D")| c.equals("N") | c.equals("P")) {
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
                } else if (c.equals("M")| c.equals("=")|c.equals("X")){
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
          
        }else{
                Logger.getLogger(ParserCommonMethods.class.getName()).log(Level.WARNING,  NbBundle.getMessage(ParserCommonMethods.class,"CommonMethod.CIGAR ",c));
            }
        }
        newreadSeq = newreadSeq.substring(softclipped, newreadSeq.length());
        String[] refAndRead = new String[2];
        refAndRead[0] =newRefSeqwithGaps;
        refAndRead[1] =  newreadSeq;
        return  refAndRead;
    }

    

    /**
     * In fact that deletions in the read stretches the stop position of the
     * ref genome mapping, we need to count the number of deletions to calculate
     * the stopposition of the read to the ref genome.
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

                if (c.contains("D")|c.contains("N")|c.contains("P")) {
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
     * converts the the dezimal number into binary code and checks if 16 is 1 or 0
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
