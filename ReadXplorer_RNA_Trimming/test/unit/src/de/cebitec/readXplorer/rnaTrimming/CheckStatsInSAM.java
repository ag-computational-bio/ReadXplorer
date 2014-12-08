/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.rnaTrimming;


import java.io.File;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.junit.Test;


/**
 *
 * @author jeff
 */
public class CheckStatsInSAM {

    public CheckStatsInSAM() {

    }


    public void readFileAndCountUnmappedReads( String path ) {
        File samfile = new File( path );
        SAMFileReader samBamReader = new SAMFileReader( samfile );
        long mapped = 0;
        long unmapped = 0;
        SAMRecordIterator samItor = samBamReader.iterator();
        while( samItor.hasNext() ) {
            try {
                SAMRecord record = samItor.next();
                if( record.getReadUnmappedFlag() ) {
                    unmapped++;
                }
                else {
                    mapped++;
                }
            }
            catch( SAMFormatException e ) {
                System.out.println( "Cought SAMFormatException for a record in your SAM file: " + e.getMessage() );
            }
        }
        samItor.close();
        System.out.println( path );
        System.out.println( "mapped: " + mapped );
        System.out.println( "unmapped: " + unmapped );

    }


    @Test
    public void test() {
        String dirpath = "/Users/jeff/Masterarbeit/Daten_vom_cebitec/";
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_f_l_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_f_r_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_f_lr_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_v_l_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_v_r_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_v_lr_8.redo_with_originals.sam" );
        readFileAndCountUnmappedReads( dirpath + "s_7_index01_fwd_v_r_12.redo_with_originals.sam" );
    }


}
