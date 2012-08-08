package de.cebitec.vamp.externalSort;

import java.io.File;
import net.sf.samtools.SAMRecord;

/**
 * External sort for sorting a sam or bam file by read sequence.
 *
 * @author -Rolf Hilker-
 */
public class ExternalSortBamReadSeq extends ExternalSortBAM {
    
    /**
     * This class sorts Sam or Bam files by read sequence and returns a sorted
     * Bam file. It uses a merge sort which creates temporary files for merging
     * to save memory. Created files will be removed after sorting.
     * @param inputFile the input file to sort
     */
    public ExternalSortBamReadSeq(File inputFile) {
        super(inputFile);
        ExternalSortBAM.CRITERION = "readSeq";
    }
    
    /**
     * Compares the two parameters in terms of their read string.
     * @param record1 first record to compare
     * @param record2 second recrod to compare
     * @return the result of the record comparison by their read string with String.compareTo()
     */
    @Override
    protected int compareTwoEntries(SAMRecord record1, SAMRecord record2) {
         return record1.getReadString().compareTo(record2.getReadString());
    }
    
}
