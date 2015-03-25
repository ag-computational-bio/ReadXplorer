/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.cli.analyses;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.io.File;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class SNPAnalysisCallable extends AnalysisCallable {

    private static final Logger LOG = Logger.getLogger( SNPAnalysisCallable.class.getName() );

    private final PersistentTrack persistentTrack;


    public SNPAnalysisCallable( boolean verbosity, PersistentTrack persistentTrack ) {

        super( verbosity, "SNP" );

        this.persistentTrack = persistentTrack;

    }


    @Override
    public AnalysisResult call() throws Exception {

        try {

            File trackFile = new File( persistentTrack.getFilePath() );
            final String trackFileName = trackFile.getName();

            LOG.log( FINE, "start SNP analysis for {0}...", trackFileName );
            result.addOutput( "start analysis..." );
            // TODO perform analysis

            LOG.log( FINE, "store SNP results for {0}...", trackFileName );
            result.addOutput( "store results..." );
            // TODO store results to snp-{trackFileName}.xls

            result.setResultFile( null );

        } catch( Exception ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            result.addOutput( "Error: " + ex.getMessage() );
        } catch( OutOfMemoryError ome ) {
            LOG.log( SEVERE, ome.getMessage(), ome );
            CommandException ce = new CommandException( 1, "ran out of memory!" );
            ce.initCause( ome );
            throw ce;
        }

        return result;

    }


}
