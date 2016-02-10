/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.SamSeqDictionary;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import htsjdk.samtools.SAMSequenceDictionary;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import static de.cebitec.readxplorer.ui.importer.seqidentifier.Bundle.FixRefsWizardTitle;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdManualCorrector {


    /**
     * Triggers manual editing of mapping file sequence ids to match those of
     * the reference.
     *
     * @param identicalDictionaryContainers List of sequence id correction
     *                                      containers, where each container
     *                                      holds all track jobs with the same
     *                                      SAMSequenceDictionary.
     *
     * @throws ParsingException When manual editing fails
     */
    @NbBundle.Messages( { "FixRefsWizardTitle=Manually fix reference sequence ids" } )
    @SuppressWarnings( { "unchecked", "unchecked" } )
    public void triggerManualEditing( List<SeqIdCorrectionContainer> identicalDictionaryContainers ) throws ParsingException {

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>( identicalDictionaryContainers.size() );

        for( SeqIdCorrectionContainer container : identicalDictionaryContainers ) {

            SAMSequenceDictionary dictionary = ((SamSeqDictionary) container.getSequenceDictionary()).getSamDictionary();
            panels.add( new FixRefsWizardPanel( container.getChromNames(), dictionary, container.getMappingFileNames(), container.getId() ) );
            //TODO: implement null check
        }

        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( FixRefsWizardTitle() );

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {

            for( SeqIdCorrectionContainer container : identicalDictionaryContainers ) {
                SAMSequenceDictionary fixedDictionariy = (SAMSequenceDictionary) wiz.getProperty( FixRefsWizardPanel.PROP_FIXED_DICTIONARY + container.getId() );
                if( fixedDictionariy != null ) {

                    SamSeqDictionary wrappedDictionary = new SamSeqDictionary( fixedDictionariy );
                    container.setSequenceDictionary( wrappedDictionary );
                    List<TrackJob> trackJobs = container.getTrackJobs();
                    for( TrackJob trackJob : trackJobs ) { //TODO: decide if both times newDictionary needs to be added or which is better
                        trackJob.setSequenceDictionary( wrappedDictionary );
                    }
                    //means all tracks missing in the map do not get a new dictionary
                } else {
                    container.setManualFixFailed( true );
                    //TODO: make sure this data set is not imported
                }
            }
        } else {
            throw new ParsingException( "No reference sequence id matches any sequence ids in the mapping file. If you don't use identical names, the import does not succeed." );
        }
    }


}
