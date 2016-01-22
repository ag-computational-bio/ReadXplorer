/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdAutoCorrector {

    private final File mappingFile;
    private final SAMSequenceDictionary sequenceDictionary;
    private final Set<String> chromIds;
    private boolean fixed;
    private List<String> missingSeqs;


    /**
     *
     * @param mappingFile
     * @param sequenceDictionary
     * @param chromIds
     */
    public SeqIdAutoCorrector( File mappingFile, SAMSequenceDictionary sequenceDictionary, Set<String> chromIds ) {
        this.mappingFile = mappingFile;
        this.sequenceDictionary = sequenceDictionary;
        this.chromIds = chromIds;
        correctSeqIds();
    }


    private void correctSeqIds() {

        /* replace -,.,' ', by '_'; chr_x by x or x by chr_x, gbk locus found as
         * part of fasta header = only keep locus tag in fasta,.
         *
         */
        List<SAMSequenceRecord> newSeqRecordList = new ArrayList<>();
        missingSeqs = new ArrayList<>();
        for( SAMSequenceRecord refRecord : sequenceDictionary.getSequences() ) {
            String refFixed = refRecord.getSequenceName();
            if( !chromIds.contains( refFixed ) ) {
                refFixed = refRecord.getSequenceName()
                        .replace( ':', '-' )
                        .replace( '/', '-' )
                        .replace( '\\', '-' )
                        .replace( '*', '-' )
                        .replace( '?', '-' )
                        .replace( '|', '-' )
                        .replace( '<', '-' )
                        .replace( '>', '-' )
                        .replace( '"', '_' );

                if( !chromIds.contains( refFixed ) ) {
                    refFixed = refFixed.replaceFirst( "chr\\d+", "\\d+" );
                }
                if( !chromIds.contains( refFixed ) ) {
                    refFixed = refFixed.replaceFirst( "^\\d+", "chr\\d+" );
                }
                
                if( chromIds.contains( refFixed ) ) {
                    SAMSequenceRecord newRecord = new SAMSequenceRecord( refFixed, refRecord.getSequenceLength() );
                    newSeqRecordList.add( newRecord );
                } else {
                    missingSeqs.add( refRecord.getSequenceName() );
                }
            } else {
                newSeqRecordList.add( refRecord );
            }
        }
        if( !newSeqRecordList.isEmpty() ) {
            sequenceDictionary.setSequences( newSeqRecordList );
        }
        fixed = missingSeqs.isEmpty();
    }


    /**
     * @return All sequence identifiers of the mapping file missing in the
     *         reference file.
     */
    public List<String> getMissingSeqs() {
        return missingSeqs;
    }


    /**
     * @return <code>true</code> if at least one reference id from the mapping
     *         file could be associated to a reference from the genome file.
     */
    public boolean isFixed() {
        return fixed;
    }


}
