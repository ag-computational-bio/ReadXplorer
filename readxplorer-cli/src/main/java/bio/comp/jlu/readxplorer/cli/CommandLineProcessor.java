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

package bio.comp.jlu.readxplorer.cli;


import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.utils.Properties;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;


public final class CommandLineProcessor implements ArgsProcessor {

    private static final Logger LOG = Logger.getLogger( CommandLineProcessor.class.getName() );

    static {
        LOG.setLevel( Level.ALL );
    }


    @Arg( shortName = 'v', longName = "verbose" )
    @Description( displayName = "Verbose", shortDescription = "Increase verbosity." )
    public boolean verboseArg;

    @Arg( shortName = 'd', longName = "db" )
    @Description( displayName = "Database", shortDescription = "The H2 database to store imported data." )
    public String dbFileArg;

    @Arg( shortName = 't', longName = "test" )
    @Description( displayName = "Test", shortDescription = "This is a test argument." )
    public String testArg;


    @Override
    public void process( final Env env ) throws CommandException {
        LOG.log( Level.INFO, "trigger Test processor" );

        final PrintStream ps = env.getOutputStream();
            ps.println( "trigger " + getClass().getName() );


        LOG.config( "verbose=" + Boolean.toString( verboseArg ) );
        LOG.config( "test=" + testArg );

        LOG.config( "db file=" + dbFileArg );

        try {

            ProjectConnector pc = ProjectConnector.getInstance();
                pc.connect( Properties.ADAPTER_H2, dbFileArg, null, null, null );
            LOG.log( Level.FINE, "connected to {0}", dbFileArg );
            ps.println( "connected to " + dbFileArg );


            LOG.fine( "read reference genomes..." );
            List<PersistentReference> refGenomes = pc.getGenomes();
            LOG.fine( "# reference genomes: " + refGenomes.size() );
            for( PersistentReference refGenome : refGenomes) {

                ps.println( "ref genome: " + refGenome.getId() );
                LOG.log( Level.FINE, "ref genome: {0}", refGenome.getId() );
                if( verboseArg ) {
                    ps.println( "\tname: " + refGenome.getName() );
                    ps.println( "\tdesc: " + refGenome.getDescription() );
                    ps.println( "\t# chrom: " + refGenome.getNoChromosomes() );
                    ps.println( "\tlength: " + refGenome.getGenomeLength() );
                    ps.println( "\tdate: " + refGenome.getTimeStamp() );
                    ps.println();
                }

            }


            LOG.fine( "read tracks..." );
            List<PersistentTrack> tracks = pc.getTracks();
            LOG.fine( "# tracks: " + tracks.size() );
            for( PersistentTrack track : tracks ) {
                ps.println( "track: " + track.getId() );
                LOG.log( Level.FINE, "track: {0}", track.getId() );
                if( verboseArg ) {
                    ps.println( "\tdesc: " + track.getDescription() );
                    ps.println( "\t# chrom: " + track.getActiveChromId() );
                    ps.println( "\tfile: " + track.getFilePath() );
                    ps.println( "\tref genome id: " + track.getRefGenID() );
                    ps.println( "\tdate: " + track.getTimestamp() );
                }
                ps.println();

            }

            pc.disconnect();
            ps.println( "disconnected from " + dbFileArg );
            LOG.log( Level.FINE, "disconnected from {0}", dbFileArg );

        }
        catch( SQLException ex ) {

            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;

        }

    }

}
