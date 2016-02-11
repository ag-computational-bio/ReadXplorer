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

import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * Compares the sequence id stored in mapping files with the sequence ids in a
 * reference.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdChecker {

    private SeqIdCorrectionContainer correctionDataContainer;


    /**
     * Compares the sequence id stored in mapping files with the sequence ids in
     * a reference.
     *
     * @param correctionDataContainer Container bundling all information
     *                                regarding fixing of sequence ids between
     *                                the reference and the mapping files.
     */
    public SeqIdChecker( SeqIdCorrectionContainer correctionDataContainer ) {
        this.correctionDataContainer = correctionDataContainer;
    }


    /**
     * Compares the sequence id stored in the mapping files with the sequence
     * ids in the reference and performs auto correction if possible.
     *
     * @param mappingFile mapping file to check
     * @param ref         the DB reference associated to the mapping files
     *
     * @return <code>true</code> if all mapping file ids could be found in the
     *         reference, <code>false</code> otherwise
     */
    public boolean checkSeqIds( File mappingFile, PersistentReference ref ) {
        //get chrom names from DB
        Collection<PersistentChromosome> chroms = ref.getChromosomes().values();
        List<String> chromNames = new ArrayList<>();
        for( PersistentChromosome chrom : chroms ) {
            chromNames.add( chrom.getName() );
        }
        return compareSeqIds( mappingFile, chromNames );

    }


    /**
     * Compares the sequence ids from a mapping file to the given list of
     * sequence ids and performs auto correction if possible.
     *
     * @param mappingFile mapping file to check
     * @param chromIds    the list of chromosome identifiers from the reference
     *
     * @return <code>true</code> if all mapping file ids could be found in the
     *         reference, <code>false</code> otherwise
     */
    private boolean compareSeqIds( File mappingFile, List<String> chromIds ) {
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        SAMFileHeader fileHeader = samReaderFactory.getFileHeader( mappingFile );
        SAMSequenceDictionary sequenceDictionary = fileHeader.getSequenceDictionary();
        int noMatches = 0;
        correctionDataContainer.setChromNames( chromIds );
        correctionDataContainer.setSequenceDictionary( sequenceDictionary );

        Set<String> chromSet = new HashSet<>( chromIds );
        for( SAMSequenceRecord record : sequenceDictionary.getSequences() ) {
            if( chromSet.contains( record.getSequenceName() ) ) {
                noMatches++;
            }
        }

        boolean foundAllIds = true;
        int foundIds = noMatches;
        if( noMatches < sequenceDictionary.size() ) {
            //Try auto correction when at least one mapping file id is missing
            SeqIdAutoCorrector idCorrector = new SeqIdAutoCorrector( sequenceDictionary, chromSet );
            foundAllIds = idCorrector.isFixed();
            foundIds = sequenceDictionary.size() - idCorrector.getMissingSeqIds().size();
            correctionDataContainer.setMissingSeqIds( idCorrector.getMissingSeqIds() );
        }
        correctionDataContainer.setFoundIds( foundIds );
        correctionDataContainer.setIsSeqIdsValid( foundAllIds );
        return foundAllIds;
    }


    /**
     * @return The container bundling all sequence id fixing related information
     */
    public SeqIdCorrectionContainer getCorrectionDataContainer() {
        return correctionDataContainer;
    }


}
