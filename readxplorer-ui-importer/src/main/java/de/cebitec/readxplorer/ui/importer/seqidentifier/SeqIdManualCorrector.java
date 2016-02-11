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
 * Handle manual correction of seuqence ids to match those of the reference.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdManualCorrector {

    /**
     * Handle manual correction of seuqence ids to match those of the reference.
     */
    public SeqIdManualCorrector() {
    }


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

            panels.add( new FixRefsWizardPanel( container ) );
        }

        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( FixRefsWizardTitle() );

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {

            for( SeqIdCorrectionContainer container : identicalDictionaryContainers ) {
                SAMSequenceDictionary fixedDictionary = (SAMSequenceDictionary) wiz.getProperty( FixRefsWizardPanel.PROP_FIXED_DICTIONARY + container.getId() );
                boolean isFixed = (boolean) wiz.getProperty( FixRefsWizardPanel.PROP_FIXED + container.getId() );
                if( fixedDictionary != null && isFixed ) {

                    container.setSequenceDictionary( fixedDictionary );
                    List<TrackJob> trackJobs = container.getTrackJobs();
                    for( TrackJob trackJob : trackJobs ) {
                        trackJob.setSequenceDictionary( new SamSeqDictionary( fixedDictionary ) );
                    }
                    //means all tracks missing in the map do not get a new dictionary
                } else {
                    //make sure this data set is not imported
                    for( TrackJob trackJob : container.getTrackJobs() ) {
                        trackJob.setCanBeImported( false );
                    }
                }
            }
        } else { //make sure these data sets are not imported
            boolean atLeastOneFailed = false;
            for( SeqIdCorrectionContainer container : identicalDictionaryContainers ) {
                if( container.getFoundIds() <= 0 ) { //make sure these data sets are not imported
                    atLeastOneFailed = true;
                    for( TrackJob trackJob : container.getTrackJobs() ) {
                        trackJob.setCanBeImported( false );
                    }
                }
            }
            if( atLeastOneFailed ) {
                throw new ParsingException( "No reference sequence id matches any sequence ids in the mapping file. If you don't use identical names, the import does not succeed." );
            }
        }
    }


}
