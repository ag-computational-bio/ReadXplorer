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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.Properties;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;


/**
 * This mapping thread should be used for analyses, but not for visualizing
 * data. The thread carries out all queries to receive the mappings for a
 * certain interval.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class MappingThreadAnalyses extends MappingThread {

    private static final Logger LOG = getLogger( MappingThreadAnalyses.class.getName() );


    /**
     * Creates a new mapping thread for carrying out mapping request either to a
     * file.
     * <p>
     * @param tracks the list of tracks for which this mapping thread is created
     */
    public MappingThreadAnalyses( List<PersistentTrack> tracks ) {
        super( tracks );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void run() {

        while( !interrupted() ) {

            IntervalRequest request = requestQueue.poll();
            List<Mapping> currentMappings;
            if( request != null ) {
                if( request.getDesiredData() == Properties.REDUCED_MAPPINGS ) {
                    currentMappings = this.loadReducedMappings( request );
                } else {
                    currentMappings = this.loadMappings( request );
                }
                request.getSender().receiveData( new MappingResult( currentMappings, request ) );

            } else {
                try {
                    Thread.sleep( 10 );
                } catch( InterruptedException ex ) {
                    LOG.log( SEVERE, null, ex );
                }
            }

        }
    }


}
