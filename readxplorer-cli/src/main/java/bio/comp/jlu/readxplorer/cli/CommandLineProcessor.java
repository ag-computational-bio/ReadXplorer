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


import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.StorageException;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.utils.Properties;
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;


public final class CommandLineProcessor implements ArgsProcessor, ThreadFactory {

    private static final Logger LOG = Logger.getLogger( CommandLineProcessor.class.getName() );

    static {
        LOG.setLevel( Level.ALL );
    }


    /**
     * Mandatory options
     */
    @Arg( shortName = 'r', longName = "reference" )
    @Description( displayName = "Reference", shortDescription = "Reference genome to import / analysis." )
    public String referenceArg;

    @Arg( shortName = 't', longName = "tracks" )
    @Description( displayName = "Tracks", shortDescription = "Tracks to import / analysis." )
    public String[] tracksArgs;

    @Arg( shortName = 'p', longName = "matepairtracks" )
    @Description( displayName = "Mate Pair Tracks", shortDescription = "Mate Pair Tracks to import / analysis." )
    public String[] matePairTracksArgs;


    /**
     * Optional options
     */
    @Arg( shortName = 'v', longName = "verbose" )
    @Description( displayName = "Verbose", shortDescription = "Increase verbosity." )
    public boolean verboseArg;

    @Arg( shortName = 'm', longName = "multithreading" )
    @Description( displayName = "Verbose", shortDescription = "Execute imports and analysis concurrently." )
    public boolean multiThreadingArg;

    @Arg( longName = "db" )
    @Description( displayName = "Database", shortDescription = "H2 database to store imported data." )
    public String dbFileArg;


    /**
     * Analysis options
     */
    @Arg( longName = "snp" )
    @Description( displayName = "SNP", shortDescription = "Single Nucleotide Polymorphism analysis." )
    public boolean snpAnalysis;

    @Arg( longName = "tss" )
    @Description( displayName = "TSS", shortDescription = "Transcription Start Site analysis." )
    public boolean tssAnalysis;

    @Arg( longName = "rpkm" )
    @Description( displayName = "RPKM", shortDescription = "XXX analysis." )
    public boolean rpkmAnalysis;


    @Override
    public void process( final Env env ) throws CommandException {

        LOG.log( Level.FINE, "triggered command line processor" );

        final PrintStream ps = env.getOutputStream();
            ps.println( "trigger " + getClass().getName() );


        // print optional arguments...
        printConfig( ps, "verbosity=" + (verboseArg ? "on" : "off") );
        printConfig( ps, "multi-threading=" + (multiThreadingArg ? "on" : "off") );
        printConfig( ps, "db file=" + dbFileArg );


        // test reference file
        final File referenceFile;
        if( referenceArg != null ) {

            referenceFile = new File( referenceArg );
            if( !referenceFile.canRead() ) {
                throw new CommandException( 1, "Cannot read reference file!" );
            }
            LOG.fine( "reference file to import: " + referenceFile.getName() );

        }
        else {
            env.usage();
            return;
        }


        // test track file
        final File[] trackFiles;
        if( tracksArgs != null ) {

            printInfo( ps, "track files to import:" );
            trackFiles = new File[ tracksArgs.length ];
            for( int i=0; i<trackFiles.length; i++ ) {
                String trackPath = tracksArgs[i];
                File trackFile = new File( trackPath );
                if( !trackFile.canRead() ) {
                    throw new CommandException( 1, "Cannot read track file " + (i+1) + "("+trackPath+")!" );
                }
                trackFiles[i] = trackFile;
                printInfo( ps, "\tadd " + trackFiles[i].getName() );
            }

        }
        else {
            env.usage();
            return;
        }


        // test mate pair track files
        File[] matePairTrackFiles;
        final boolean importMatePairReads;
        if( matePairTracksArgs != null ) {

            if( matePairTracksArgs.length != tracksArgs.length )
                throw new CommandException( 1, "Mate pair track count ("+matePairTracksArgs.length+") does not match track count (" +matePairTracksArgs.length+")!" );

            printInfo( ps, "mate pair files to import:" );
            matePairTrackFiles = new File[ matePairTracksArgs.length ];
            for( int i=0; i<matePairTrackFiles.length; i++ ) {
                String peTrackPath = matePairTracksArgs[i];
                matePairTrackFiles[i] = new File( peTrackPath );
                if( !referenceFile.canRead() )
                    throw new CommandException( 1, "Cannot read mate pair track file " + (i+1) + "("+peTrackPath+")!" );
                printInfo( ps, "\tadd " + trackFiles[i].getName() );
            }
            importMatePairReads = true;

        }
        else {
            importMatePairReads = false;
        }


        // setup ProjectConnector
        final ProjectConnector pc = ProjectConnector.getInstance();
        try {
            if( dbFileArg == null  ||  dbFileArg.isEmpty() )
                dbFileArg = System.getProperty( "user.dir") + "/tmp-db";
            pc.connect( Properties.ADAPTER_H2, dbFileArg, null, null, null );
            LOG.log( Level.CONFIG, "connected to {0}", dbFileArg );
        }
        catch( SQLException ex ) {

            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;

        }


        /**
         * Get a thread pool.
         * If multi-threading flag is not set, number of threads is fixed to 1
         * thus multithreading is not active.
         */
        final ExecutorService es = Executors.newFixedThreadPool( multiThreadingArg ? Runtime.getRuntime().availableProcessors() : 1, this );


        // import reference
        final ImportReferenceResult referenceResult;
        try {
            printInfo( ps, "import reference genome..." );
            Future<ImportReferenceResult> refFuture = es.submit( new ImportReferenceCallable( referenceFile ) );
            referenceResult = refFuture.get();

            // print (concurrent) output sequently
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }
            
            // stores reference sequence in the db
            LOG.log( Level.FINE, "start storing reference to db..." );
            pc.addRefGenome( referenceResult.getParsedReference() );
            printInfo( ps, "\tstored reference to db..." );
            LOG.log( Level.FINE, "...stored reference genome source \"{0}\"", referenceResult.getParsedReference().getName() );
            printInfo( ps, "...finished!" );
        }
        catch( InterruptedException | ExecutionException | StorageException ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "reference import failed!" );
                ce.initCause( ex );
            throw ce;
        }


        // import tracks
        if( !importMatePairReads ) {

            try {
                printInfo( ps, "import tracks..." );
                // submit track parse jobs for (concurrent) execution
                List<Future<ImportTrackCallable.ImportTrackResults>> futures = new ArrayList<>( trackFiles.length );
                for( File trackFile : trackFiles ) {
                    Future<ImportTrackCallable.ImportTrackResults> future = es.submit( new ImportTrackCallable( referenceResult, trackFile ) );
                    futures.add( future );
                }

                // store parsed tracks sequently to db
                for( Future<ImportTrackCallable.ImportTrackResults> future : futures ) {
                    ImportTrackCallable.ImportTrackResults result = future.get();
                    for( String msg : referenceResult.getOutput() ) {
                        printInfo( ps, msg );
                    }
                    ParsedTrack pt = result.getParsedTrack();
                    pc.storeBamTrack( pt );
                    pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );
                    printInfo( ps, "\tstored parsed track ("+result.getParsedTrack().getTrackName()+") to db" );
                }
                printInfo( ps, "...finished!" );
            }
            catch( InterruptedException | ExecutionException ex ) {
                LOG.log( Level.SEVERE, null, ex );
                CommandException ce = new CommandException( 1, "track import failed!" );
                    ce.initCause( ex );
                throw ce;
            }

        }
        else {
            // TODO implement mate pair import
        }


        // run analyses
        printInfo( ps, "start analyses..." );


        try {
            pc.disconnect();
            if( verboseArg )
                ps.println( "disconnected from " + dbFileArg );
            LOG.log( Level.FINE, "disconnected from {0}", dbFileArg );
        }
        catch( Exception ex ) {

            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;

        }

    }


    private void printInfo( PrintStream ps, String msg ) {

        LOG.info( msg );
        if( verboseArg )
            ps.println( msg );

    }


    private void printConfig( PrintStream ps, String msg ) {

        LOG.config( msg );
        if( verboseArg )
            ps.println( msg );

    }


    @Override
    public Thread newThread( Runnable r ) {

        Thread t = new Thread( r, "readxplorer-cli-worker" );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
        return t;

    }

}
