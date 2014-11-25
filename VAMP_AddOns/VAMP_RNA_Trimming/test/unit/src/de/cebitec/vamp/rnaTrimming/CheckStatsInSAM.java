/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import java.io.File;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeff
 */
public class CheckStatsInSAM {
    
    public CheckStatsInSAM() {
        
    }
    
    public void readFileAndCountUnmappedReads(String path) {
        File samfile = new File(path);
        SAMFileReader samBamReader = new SAMFileReader(samfile);
        long mapped = 0;
        long unmapped = 0;
        SAMRecordIterator samItor = samBamReader.iterator();
        while (samItor.hasNext()) {
            try {
                    SAMRecord record = samItor.next();
                    if (record.getReadUnmappedFlag()) {
                        unmapped++;
                    }
                    else {
                        mapped++;
                    }
            } catch(SAMFormatException e) {
                    System.out.println("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
            }
        }
        samItor.close();
        System.out.println(path);
        System.out.println("mapped: "+mapped);
        System.out.println("unmapped: "+unmapped);
        
    }
    
    @Test
    public void test() {
        String dirpath = "/Users/jeff/Masterarbeit/Daten_vom_cebitec/";
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_f_l_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_f_r_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_f_lr_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_v_l_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_v_r_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_v_lr_8.redo_with_originals.sam");
        readFileAndCountUnmappedReads(dirpath+"s_7_index01_fwd_v_r_12.redo_with_originals.sam");
    }
}
