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


import de.cebitec.readxplorer.api.enums.IntervalRequestData;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageAndDiffResult;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This coverage thread should be used for analyses, but not for visualizing
 * data. The thread carries out the queries to receive coverage for a certain
 * interval.
 *
 * @author -Rolf Hilker-
 */
public class CoverageThreadAnalyses extends CoverageThread {

    private static final Logger LOG = LoggerFactory.getLogger( CoverageThreadAnalyses.class.getName() );


    /**
     * Thread for retrieving the coverage for a list of tracks from their
     * mapping files.
     * <p>
     * @param tracks          the tracks handled here
     * @param referenceGenome The reference genome
     * @param combineTracks   true, if more than one track is added and their
     *                        coverage should be combined in the results
     */
    public CoverageThreadAnalyses( List<PersistentTrack> tracks, PersistentReference referenceGenome, boolean combineTracks ) {
        super( tracks, referenceGenome, combineTracks );
    }


    @Override
    public void run() {

        while( !interrupted() ) {

            IntervalRequest request = requestQueue.poll();
            CoverageAndDiffResult currentCov = new CoverageAndDiffResult( new CoverageManager( 0, 0 ), null, null, request );
            if( request != null ) {
                if( request.getDesiredData() == IntervalRequestData.ReadStarts ) {
                    currentCov = this.loadReadStartsAndCoverageMultiple( request );
                } else if( !currentCov.getCovManager().coversBounds( request.getFrom(), request.getTo() ) ) {
                    if( this.getTrackId2() != 0 ) {
                        currentCov = this.loadCoverageDouble( request ); //at the moment we only need the complete coverage here
                    } else if( this.getTrackId() != 0 || this.canQueryCoverage() ) {
                        currentCov = this.loadCoverageMultiple( request );
                    }
                }
                request.getSender().receiveData( currentCov );
            } else {
                try {
                    Thread.sleep( 10 );
                } catch( InterruptedException ex ) {
                    LOG.error( null, ex );
                }
            }

        }
    }


}
