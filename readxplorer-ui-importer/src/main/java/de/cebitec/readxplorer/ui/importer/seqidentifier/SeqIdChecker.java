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
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.SamSeqDictionary;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import de.cebitec.readxplorer.utils.sequence.RefDictionary;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import static de.cebitec.readxplorer.ui.importer.seqidentifier.Bundle.FixRefsWizardTitle;
import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * Compares the sequence id stored in mapping files with the sequence ids in a
 * reference.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdChecker {

    private SAMSequenceDictionary sequenceDictionary;
    private List<String> missingSeqs;
    private List<String> chromNames;


    /**
     * Compares the sequence id stored in mapping files with the sequence ids in
     * a reference.
     */
    public SeqIdChecker() {
        missingSeqs = new ArrayList<>();
    }


    /**
     * Compares the sequence id stored in the mapping files with the sequence
     * ids in the reference.
     *
     * @param mappingFile mapping file to check
     * @param ref         the DB reference associated to the mapping files
     *
     * @return <code>true</code> if at least one id was found,
     *         <code>false</code> otherwise
     */
    public boolean checkSeqIds( File mappingFile, PersistentReference ref ) {
        //get chrom names from DB
        Collection<PersistentChromosome> chroms = ref.getChromosomes().values();
        chromNames = new ArrayList<>();
        for( PersistentChromosome chrom : chroms ) {
            chromNames.add( chrom.getName() );
        }
        return compareSeqIds( mappingFile, chromNames );

    }


    /**
     * Compares the sequence id stored in the mapping files with the sequence
     * ids in the reference.
     *
     * @param mappingFile mapping file to check
     * @param refJob      a new reference to import and associated to the
     *                    mapping files
     */
    public void checkSeqIds( File mappingFile, ReferenceJob refJob ) {
        //TODO: get chrom names from file, depending on parser, or only run this method after reference imports in import thread
    }


    /**
     * Compares the sequence ids from a mapping file to the given list of
     * sequence ids.
     *
     * @param mappingFile
     * @param chromIds
     *
     * @return <code>true</code> if at least one id was found,
     *         <code>false</code> otherwise
     */
    private boolean compareSeqIds( File mappingFile, List<String> chromIds ) {
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        SAMFileHeader fileHeader = samReaderFactory.getFileHeader( mappingFile );
        sequenceDictionary = fileHeader.getSequenceDictionary();
        int noMatches = 0;

        HashSet<String> chromSet = new HashSet<>( chromIds );
        for( SAMSequenceRecord record : sequenceDictionary.getSequences() ) {
            if( chromSet.contains( record.getSequenceName() ) ) {
                noMatches++;
            }
        }

        boolean foundId;
        if( noMatches <= 0 ) {
            //1.: Try auto correction
            SeqIdAutoCorrector idCorrector = new SeqIdAutoCorrector( mappingFile, sequenceDictionary, new HashSet<>( chromIds ) );
            foundId = idCorrector.isFixed();
            missingSeqs = idCorrector.getMissingSeqs();
        } else {
            foundId = true;
        }
        return foundId;
    }


    /**
     * @return All sequence identifiers of the mapping file missing in the
     *         reference file.
     */
    public List<String> getMissingSeqs() {
        return missingSeqs;
    }


    /**
     * @return The sequence dictionary containing automatic corrections to
     *         incorporate in an extended mapping file.
     */
    public RefDictionary getSequenceDictionary() {
        return new SamSeqDictionary( sequenceDictionary );
    }


    /**
     * Triggers manual editing of mapping file sequence ids to match those of
     * the reference.
     *
     * @return The updated sequence dictionary
     *
     * @throws ParsingException When manual editing fails
     */
    @NbBundle.Messages( { "FixRefsWizardTitle=Manually fix reference sequence ids" } )
    public RefDictionary triggerManualEditing() throws ParsingException {

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>( 1 );
        panels.add( new FixRefsWizardPanel( new ArrayList<>( chromNames ), sequenceDictionary ) ); //TODO: implement null check
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( FixRefsWizardTitle() );

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            sequenceDictionary = (SAMSequenceDictionary) wiz.getProperty( FixRefsWizardPanel.PROP_FIXED_DICTIONARY );
        } else {
            throw new ParsingException( "No reference sequence id matches any sequence ids in the mapping file. If you don't use identical names, the import does not succeed." );
        }
        return new SamSeqDictionary( sequenceDictionary );
    }


}
