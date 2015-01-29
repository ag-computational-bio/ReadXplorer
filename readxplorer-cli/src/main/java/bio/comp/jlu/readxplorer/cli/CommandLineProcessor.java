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


import bio.comp.jlu.readxplorer.cli.imports.ImportPairedEndCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportPairedEndCallable.ImportPairedEndResults;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable.ImportTrackResults;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.StorageException;
import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.mappings.JokToBamDirectParser;
import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import de.cebitec.readxplorer.parser.mappings.SamBamParser;
import de.cebitec.readxplorer.utils.Properties;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;



public final class CommandLineProcessor implements ArgsProcessor {

    private static final Logger LOG = Logger.getLogger( CommandLineProcessor.class.getName() );
    private static final String DATABASE_NAME = "readxplorer-db";

    static {
        LOG.setLevel( Level.ALL );
    }


    /**
     * Mandatory options
     */
    @Arg( shortName = 'f', longName = "reference" )
    @Description( displayName = "Reference", shortDescription = "Reference genome to import / analysis." )
    public String referenceArg;

    @Arg( shortName = 'r', longName = "reads" )
    @Description( displayName = "Reads Folder", shortDescription = "Reads to import / analysis." )
    public String[] readsArgs;

    @Arg( shortName = 'e', longName = "per" )
    @Description( displayName = "Pair-End Reads Folder", shortDescription = "Paired-end reads to import / analysis." )
    public String[] pairedEndReads;


    /**
     * Optional options
     */
    @Arg( shortName = 'v', longName = "verbose" )
    @Description( displayName = "Verbose", shortDescription = "Print detailed messages to the console." )
    public boolean verboseArg;

    @Arg( shortName = 'p', longName = "pairedend" )
    @Description( displayName = "Paired-End", shortDescription = "Set this flag if reads are paired-end reads." )
    public boolean pairedEndArg;

    @Arg( shortName = 'm', longName = "multithreaded" )
    @Description( displayName = "Multithreaded", shortDescription = "Execute imports and analysis concurrently." )
    public boolean multiThreadingArg;

    @Arg( longName = "db" )
    @Description( displayName = "Database", shortDescription = "Set a database name to persistently store imported data." )
    public String dbFileArg;

    @Arg( longName = "props" )
    @Description( displayName = "Properties", shortDescription = "Specify a path to a property file." )
    public String propsFileArg;


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


    private final java.util.Properties props;
    private final java.util.Properties defaultProps;




    public CommandLineProcessor() throws CommandException {

        try {
            props = loadProperties();
            defaultProps = loadDefaultProperties();
        }
        catch( IOException ex ) {
            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;
        }

    }




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
        final File referenceFile = testReferenceFile();
        if( referenceFile == null ) {
            env.usage();
            return;
        }


        // test track file
        final File[] readsFiles = testReadsFiles( ps );
        if( readsFiles == null ) {
            env.usage();
            return;
        }


        // test paired-end read files
        final File[] pairedEndFiles = testPairedEndReadsFiles( ps );



        // setup ProjectConnector
        final ProjectConnector pc = setupProjectConnector();



        /**
         * Get a thread pool.
         * If multi-threading flag is not set, number of threads is fixed to 1
         * thus multi-threading is not active.
         */
        int noThreads = multiThreadingArg ? Runtime.getRuntime().availableProcessors() : 1;
        final ExecutorService es = Executors.newFixedThreadPool( noThreads, new ReadXplorerCliThreadFactory() );


        // import reference
        final ImportReferenceResult referenceResult = importReference( referenceFile, es, ps );



        // import tracks
        try {
            if( pairedEndArg  ||  pairedEndFiles != null ) {
                // import paired-end reads
                importPairedEndReads( readsFiles, pairedEndFiles, referenceResult, es, ps );
            }
            else {
                // import normal reads
                importReads( readsFiles, referenceResult, es, ps );
            }
        }
        catch( InterruptedException | ExecutionException ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "track import failed!" );
                ce.initCause( ex );
            throw ce;
        }


        // run analyses
        printInfo( ps, "start analyses..." );


        try {
            pc.disconnect();
            printInfo( ps, "disconnected from " + dbFileArg );
        }
        catch( Exception ex ) {
            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;
        }

    }




    private ProjectConnector setupProjectConnector() throws CommandException {

        try {

            if( dbFileArg != null ) {

                if( !dbFileArg.isEmpty() ) {
                    File dbFile = new File( dbFileArg );
                    if( !dbFile.canRead() || !dbFile.canWrite() ) {
                        throw new IOException( "can not access database file!" );
                    }
                }
                else {
                    throw new FileNotFoundException( "path to database file is empty!" );
                }

            }
            else {
                dbFileArg = System.getProperty( "user.dir" ) + FileSystems.getDefault().getSeparator() + DATABASE_NAME;
                File dbFile = new File( dbFileArg );
                int i = 0;
                while( dbFile.exists() ) {
                    dbFile = new File( dbFileArg + '-' + i );
                }
            }

            ProjectConnector pc = ProjectConnector.getInstance();
            pc.connect( Properties.ADAPTER_H2, dbFileArg, null, null, null );
            LOG.log( Level.CONFIG, "connected to {0}", dbFileArg );
            return pc;

        }
        catch( SQLException | IOException ex ) {

            CommandException ce = new CommandException( 1 );
            ce.initCause( ex );
            throw ce;

        }

    }




    private File testReferenceFile() throws CommandException {

        if( referenceArg != null ) {

            File referenceFile = new File( referenceArg );
            if( !referenceFile.canRead() ) {
                throw new CommandException( 1, "Cannot read reference file!" );
            }
            LOG.fine( "reference file to import: " + referenceFile.getName() );

            return referenceFile;

        }
        else
            return null;

    }


    private File[] testReadsFiles( PrintStream ps ) throws CommandException {

        if( readsArgs != null ) {

            printInfo( ps, "track files to import:" );
            File[] trackFiles = new File[readsArgs.length];
            for( int i = 0; i < trackFiles.length; i++ ) {
                String trackPath = readsArgs[i];
                File trackFile = new File( trackPath );
                if( !trackFile.canRead() ) {
                    throw new CommandException( 1, "Cannot read track file " + (i + 1) + "(" + trackPath + ")!" );
                }
                trackFiles[i] = trackFile;
                printInfo( ps, "\tadd " + trackFiles[i].getName() );
            }
            return trackFiles;

        }
        else
            return null;

    }


    private File[] testPairedEndReadsFiles( PrintStream ps ) throws CommandException {

        if( pairedEndReads != null ) {

            if( pairedEndReads.length != readsArgs.length )
                throw new CommandException( 1, "Mate pair track count (" + pairedEndReads.length + ") does not match track count (" + pairedEndReads.length + ")!" );

            printInfo( ps, "mate pair files to import:" );
            File[] pairedEndFiles = new File[pairedEndReads.length];
            for( int i = 0; i < pairedEndFiles.length; i++ ) {
                String peTrackPath = pairedEndReads[i];
                File pairedEndFile = new File( peTrackPath );
                if( !pairedEndFile.canRead() )
                    throw new CommandException( 1, "Cannot read paired-end file " + (i + 1) + "(" + peTrackPath + ")!" );
                printInfo( ps, "\tadd " + pairedEndFile.getName() );
                pairedEndFiles[i] = pairedEndFile;
            }

            return pairedEndFiles;

        }
        else
            return null;

    }




    private ImportReferenceResult importReference( File referenceFile, ExecutorService es, PrintStream ps ) throws CommandException {

        try {

            printInfo( ps, "import reference genome..." );
            Future<ImportReferenceResult> refFuture = es.submit( new ImportReferenceCallable( referenceFile ) );
            ImportReferenceResult referenceResult = refFuture.get();

            // print (concurrent) output sequently
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }

            // stores reference sequence in the db
            LOG.log( Level.FINE, "start storing reference to db..." );
            ProjectConnector.getInstance().addRefGenome( referenceResult.getParsedReference() );
            printInfo( ps, "\tstored reference to db..." );
            LOG.log( Level.FINE, "...stored reference genome source \"{0}\"", referenceResult.getParsedReference().getName() );
            printInfo( ps, "...finished!" );
            return referenceResult;

        }
        catch( InterruptedException | ExecutionException | StorageException ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "reference import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private void importPairedEndReads( File[] trackFiles, File[] pairedEndFiles, ImportReferenceResult referenceResult, ExecutorService es, PrintStream ps ) throws InterruptedException, ExecutionException {

        printInfo( ps, "import paired-end reads..." );

        // submit parse reads jobs for (concurrent) execution
        final ProjectConnector pc = ProjectConnector.getInstance();
        final List<Future<ImportPairedEndResults>> futures = new ArrayList<>( trackFiles.length );
        final int distance = Integer.parseInt( getProperty( Constants.PER_DISTANCE ) );
        final byte orientation = (byte) Integer.parseInt( getProperty( Constants.PER_ORIENTATION ) );
        final short deviation = (short) Integer.parseInt( getProperty( Constants.PER_DEVIATION ) );
        for( int i = 0; i < trackFiles.length; i++ ) {

            File trackFile = trackFiles[i];
            TrackJob trackJob1 = new TrackJob( pc.getLatestTrackId(), trackFile,
                                               trackFile.getName(),
                                               referenceResult.getReferenceJob(),
                                               selectParser( trackFile ),
                                               false,
                                               new Timestamp( System.currentTimeMillis() ) );
            TrackJob trackJob2 = null;
            if( pairedEndFiles != null ) {
                File pairedEndFile = pairedEndFiles[i];
                trackJob2 = new TrackJob( pc.getLatestTrackId(), pairedEndFile,
                                          pairedEndFile.getName(),
                                          referenceResult.getReferenceJob(),
                                          selectParser( pairedEndFile ),
                                          false,
                                          new Timestamp( System.currentTimeMillis() ) );
            }

            ReadPairJobContainer rpjc = new ReadPairJobContainer( trackJob1, trackJob2, distance, deviation, orientation );
            futures.add( es.submit( new ImportPairedEndCallable( referenceResult, rpjc ) ) );

        }

        // store parsed reads sequently to db
        for( Future<ImportPairedEndResults> future : futures ) {
            ImportPairedEndResults result = future.get();
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }

            // store track entry in db
            ParsedTrack pt = result.getParsedTrack();
            pc.storeBamTrack( pt );
            pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );

            // read pair ids have to be set in track entry
            ProjectConnector.getInstance().setReadPairIdsForTrackIds( pt.getID(), -1 );
            printInfo( ps, "\tstored parsed paired-end reads(" + result.getParsedTrack().getTrackName() + ") to db" );
        }
        printInfo( ps, "...finished!" );

    }


    private void importReads( File[] trackFiles, ImportReferenceResult referenceResult, ExecutorService es, PrintStream ps ) throws InterruptedException, ExecutionException {

        printInfo( ps, "import tracks..." );

        final ProjectConnector pc = ProjectConnector.getInstance();
        // submit track parse jobs for (concurrent) execution
        List<Future<ImportTrackResults>> futures = new ArrayList<>( trackFiles.length );
        for( File trackFile : trackFiles ) {

            TrackJob trackJob = new TrackJob( pc.getLatestTrackId(), trackFile,
                                              trackFile.getName(),
                                              referenceResult.getReferenceJob(),
                                              selectParser( trackFile ),
                                              false,
                                              new Timestamp( System.currentTimeMillis() ) );

            futures.add( es.submit( new ImportTrackCallable( referenceResult, trackJob ) ) );

        }

        // store parsed tracks sequently to db
        for( Future<ImportTrackResults> future : futures ) {
            ImportTrackResults result = future.get();
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }
            ParsedTrack pt = result.getParsedTrack();
            pc.storeBamTrack( pt );
            pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );
            printInfo( ps, "\tstored parsed track (" + result.getParsedTrack().getTrackName() + ") to db" );
        }
        printInfo( ps, "...finished!" );

    }




    private void printConfig( PrintStream ps, String msg ) {

        LOG.config( msg );
        if( verboseArg )
            ps.println( msg );

    }


    private void printInfo( PrintStream ps, String msg ) {

        LOG.info( msg );
        if( verboseArg )
            ps.println( msg );

    }


    private void printSevere( PrintStream ps, String msg ) {

        LOG.severe( msg );
        ps.println( msg );

    }




    /**
     * Loads a property value.
     * Uses either a provided file or the default property file.
     *
     * @param key the property key
     * @return the property value
     */
    private String getProperty( String key ) {

        String val = props.getProperty( key );

        if( val == null )
            val = defaultProps.getProperty( key );

        return val;

    }


    /**
     * Loads a <link>java.util.Properties</link> object.
     * Tries to load a property file as specified in the --props argument.
     * If the argument isn't present it will look for it in the current working directory.
     *
     * @return a java.util.Properties object
     * @throws IOException
     */
    private java.util.Properties loadProperties() throws IOException {

        java.util.Properties properties = new java.util.Properties();

        InputStream is = null;
        // check if a prop file is specified
        if( propsFileArg != null ) { // file is specified
            File propsFile = new File( propsFileArg );
            if( !propsFile.canRead() )
                throw new FileNotFoundException( "specified file ("+propsFileArg+") not found!" );
            else
                is = new FileInputStream( propsFile );
        }
        else { // no file specified

            File propsFile = new File( System.getProperty( "user.dir" ), "readxplorer-cli.properties" );
            // check if props file is present in current working directory
            if( propsFile.canRead() )
                is = new FileInputStream( propsFile );

        }

        if( is != null ) {
            try( BufferedInputStream bis = new BufferedInputStream( is ) ) {
                properties.load( bis );
            }
        }

        return properties;

    }


    /**
     * Loads a default <link>java.util.Properties</link> object.
     *
     * @return a java.util.Properties object
     * @throws IOException
     */
    private java.util.Properties loadDefaultProperties() throws IOException {

        java.util.Properties properties = new java.util.Properties();

        try( BufferedInputStream bis = new BufferedInputStream( CommandLineProcessor.class.getResourceAsStream( "/bio/comp/jlu/readxplorer/cli/readxplorer-cli.properties") ) ) {
            properties.load( bis );
        }

        return properties;

    }




    private static MappingParserI selectParser( File trackFile ) {

        switch( trackFile.getName().substring( trackFile.getName().lastIndexOf( '.' ) ).toLowerCase() ) {
            case "out":
            case "jok":
                return new JokToBamDirectParser();
            case "sam":
            case "bam":
            default:
                return new SamBamParser();
        }

    }

}
