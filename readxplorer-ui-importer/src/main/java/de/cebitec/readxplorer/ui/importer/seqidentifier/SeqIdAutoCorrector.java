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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Attempts to automatically correct improper reference sequence identifiers
 * between mapping and reference files by replacing the mapping file header.
 * Note that the reference file header cannot be modified, as this would
 * compromise other mapping data sets.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdAutoCorrector {

    private final SAMSequenceDictionary sequenceDictionary;
    private final Set<String> chromIds;
    private boolean fixed;
    private List<Integer> missingSeqIds;


    /**
     * Attempts to automatically correct improper reference sequence identifiers
     * between mapping and reference files by replacing the mapping file header.
     * Note that the reference file header cannot be modified, as this would
     * compromise other mapping data sets.
     *
     * @param sequenceDictionary The mapping file sequence dictionary
     * @param chromIds           The reference sequence identifiers
     */
    public SeqIdAutoCorrector( SAMSequenceDictionary sequenceDictionary, Set<String> chromIds ) {
        this.sequenceDictionary = sequenceDictionary;
        this.chromIds = chromIds;
        correctSeqIds();
    }


    /**
     * Attempts to automatically correct improper reference sequence identifiers
     * between mapping and reference files by replacing the mapping file header.
     * Note that the reference file header cannot be modified, as this would
     * compromise other mapping data sets.
     */
    private void correctSeqIds() {

        /* replace -,.,' ', by '_'; chr_x by x or x by chr_x, gbk locus found as
         * part of fasta header = only keep locus tag in fasta,.
         */
        List<SAMSequenceRecord> newSeqRecordList = new ArrayList<>();
        missingSeqIds = new ArrayList<>();
        if( chromIds.size() == 1 && sequenceDictionary.size() == 1 ) {
            String chromId = chromIds.iterator().next();
            SAMSequenceRecord mappingRef = sequenceDictionary.getSequences().get( 0 );
            if( !chromId.equals( mappingRef.getSequenceName() ) ) {
                SAMSequenceRecord newRecord = new SAMSequenceRecord( chromId, mappingRef.getSequenceLength() );
                newSeqRecordList.add( newRecord );
            } //otherwise both are already equal
        } else {
            for( int i = 0; i < sequenceDictionary.size(); i++ ) {
                SAMSequenceRecord refRecord = sequenceDictionary.getSequence( i );
                String refFixed = refRecord.getSequenceName();
                String addToRef = "";
                //Try replacing different characters and prefixes
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
                        refFixed = refFixed.replaceFirst( "chr_?(\\d+)", "$1" );
                    }
                    if( !chromIds.contains( refFixed ) ) {
                        refFixed = refFixed.replaceFirst( "chromosome_?(\\d+)", "$1" );
                    }

                    //Try adding chromosome prefixes
                    if( !chromIds.contains( refFixed ) ) {
                        addToRef = refFixed.replaceFirst( "(^\\d+)", "chr$1" );
                    }
                    if( !chromIds.contains( addToRef ) ) {
                        addToRef = refFixed.replaceFirst( "(^\\d+)", "chr_$1" );
                    }
                    if( !chromIds.contains( addToRef ) ) {
                        addToRef = refFixed.replaceFirst( "(^\\d+)", "chromosome$1" );
                    }
                    if( !chromIds.contains( addToRef ) ) {
                        addToRef = refFixed.replaceFirst( "(^\\d+)", "chromosome_$1" );
                    }
                    if( chromIds.contains( addToRef ) ) {
                        refFixed = addToRef;
                    }

                    if( chromIds.contains( refFixed ) ) {
                        SAMSequenceRecord newRecord = new SAMSequenceRecord( refFixed, refRecord.getSequenceLength() );
                        newSeqRecordList.add( newRecord );
                    } else {
                        newSeqRecordList.add( refRecord );
                        missingSeqIds.add( i );
                    }
                } else {
                    newSeqRecordList.add( refRecord );
                }
            }
        }
        sequenceDictionary.setSequences( newSeqRecordList );
        fixed = missingSeqIds.isEmpty();
    }


    /**
     * @return All sequence record ids of the mapping file missing in the
     *         reference file.
     */
    public List<Integer> getMissingSeqIds() {
        return missingSeqIds;
    }


    /**
     * @return <code>true</code> if all reference ids from the mapping file
     *         could be associated to a reference from the genome file,
     *         <code>false</code> otherwise.
     */
    public boolean isFixed() {
        return fixed;
    }


}
