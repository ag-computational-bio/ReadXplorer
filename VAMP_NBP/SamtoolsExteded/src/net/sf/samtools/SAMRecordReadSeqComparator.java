package net.sf.samtools;

/**
 * Comparator for comparing SAMRecords by read sequence.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SAMRecordReadSeqComparator implements SAMRecordComparator {

    @Override
    public int compare(final SAMRecord samRecord1, final SAMRecord samRecord2) {
        int cmp = fileOrderCompare(samRecord1, samRecord2);
        if (cmp != 0) {
            return cmp;
        }

        final boolean r1Paired = samRecord1.getReadPairedFlag();
        final boolean r2Paired = samRecord2.getReadPairedFlag();

        if (r1Paired || r2Paired) {
            if (!r1Paired) {
                return 1;
            } else if (!r2Paired) {
                return -1;
            } else if (samRecord1.getFirstOfPairFlag() && samRecord2.getSecondOfPairFlag()) {
                return -1;
            } else if (samRecord1.getSecondOfPairFlag() && samRecord2.getFirstOfPairFlag()) {
                return 1;
            }
        }

        if (samRecord1.getReadNegativeStrandFlag() != samRecord2.getReadNegativeStrandFlag()) {
            return (samRecord1.getReadNegativeStrandFlag() ? 1 : -1);
        }
        if (samRecord1.getNotPrimaryAlignmentFlag() != samRecord2.getNotPrimaryAlignmentFlag()) {
            return samRecord2.getNotPrimaryAlignmentFlag() ? -1 : 1;
        }
        final Integer hitIndex1 = samRecord1.getIntegerAttribute(SAMTag.HI.name());
        final Integer hitIndex2 = samRecord2.getIntegerAttribute(SAMTag.HI.name());
        if (hitIndex1 != null) {
            if (hitIndex2 == null) {
                return 1;
            } else {
                cmp = hitIndex1.compareTo(hitIndex2);
                if (cmp != 0) {
                    return cmp;
                }
            }
        } else if (hitIndex2 != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Less stringent compare method than the regular compare. If the two
     * records are equal enough that their ordering in a sorted SAM file would
     * be arbitrary, this method returns 0.
     *
     * @return negative if samRecord1 < samRecord2, 0 if equal, else positive
     */
    @Override
    public int fileOrderCompare(final SAMRecord samRecord1, final SAMRecord samRecord2) {
        return compareReadSequences(samRecord1.getReadString(), samRecord2.getReadString());
    }

    /**
     * Encapsulate algorithm for comparing read sequences in queryname-sorted file.
     */
    public static int compareReadSequences(final String readSeq1, final String readSeq2) {
        return readSeq1.compareTo(readSeq2);
    }
}
