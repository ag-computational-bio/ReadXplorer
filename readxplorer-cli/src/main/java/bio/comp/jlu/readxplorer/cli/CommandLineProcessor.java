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
import de.cebitec.readxplorer.parser.common.ParsedReference;
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
import java.util.Arrays;
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

import static java.util.logging.Level.SEVERE;


public final class CommandLineProcessor implements ArgsProcessor {

    private static final Logger LOG = Logger.getLogger( CommandLineProcessor.class.getName() );
    private static final String DEFAULT_DATABASE_NAME = "readxplorer-db";
    private static final String H2_FILE_SUFFIX = ".h2.db";


    static {
        LOG.setLevel( Level.ALL );
    }


    private final java.util.Properties props;
    private final java.util.Properties defaultProps;


    /**
     * Mandatory options
     */
    @Arg( shortName = 'f', longName = "reference" )
    @Description( shortDescription = "Reference genome to import / analysis." )
    public String referenceArg;

    @Arg( shortName = 'r', longName = "reads" )
    @Description( shortDescription = "SAM/BAM read files to import / analysis." )
    public String[] readsArgs;

    @Arg( shortName = 'e', longName = "per" )
    @Description( shortDescription = "SAM/BAM paired-end read files to import / analysis." )
    public String[] pairedEndReadsArgs;


    /**
     * Optional options
     */
    @Arg( shortName = 'h', longName = "help" )
    @Description( shortDescription = "Print usage information to the console." )
    public boolean helpArg;

    @Arg( shortName = 'v', longName = "verbose" )
    @Description( shortDescription = "Print detailed messages to the console." )
    public boolean verboseArg;

    @Arg( shortName = 'p', longName = "pairedend" )
    @Description( shortDescription = "Set this flag if reads are paired-end reads." )
    public boolean pairedEndArg;

    @Arg( shortName = 't', longName = "threading" )
    @Description( shortDescription = "Specifies the number of available worker threads. Take care on multi user systems!" )
    public String threadAmountArg;

    @Arg( longName = "db" )
    @Description( shortDescription = "Set a database name to persistently store imported data (default: "+DEFAULT_DATABASE_NAME+"). ATTENTION! Existing databases will be deleted!" )
    public String dbFileArg;

    @Arg( longName = "props" )
    @Description( shortDescription = "Sets the path to a custom property file." )
    public String propsFileArg;


    /**
     * Analysis options
     */
    @Arg( longName = "snp" )
    @Description( shortDescription = "Perform Single Nucleotide Polymorphism analyses on all tracks." )
    public boolean snpAnalysis;

    @Arg( longName = "tss" )
    @Description( shortDescription = "Perform Transcription Start Site analyses on all tracks." )
    public boolean tssAnalysis;

    @Arg( longName = "rpkm" )
    @Description( shortDescription = "Perform RPKM analyses on all tracks." )
    public boolean rpkmAnalysis;


    public CommandLineProcessor() throws CommandException {

        try {
            props = loadProperties();
            defaultProps = loadDefaultProperties();
        } catch( IOException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1 );
            ce.initCause( ex );
            throw ce;
        }

    }


    @Override
    public void process( final Env env ) throws CommandException {

        LOG.log( Level.FINE, "triggered command line processor" );


        // check if help/usgae is requested
        if( helpArg ) {
            env.usage();
            return;
        }


        // print optional arguments...
        final PrintStream ps = env.getOutputStream();
        printFine( ps, "verbosity: " + (verboseArg ? "on" : "off") );
        printFine( ps, "paired end: " + (pairedEndArg ? "yes" : "no") );
        printFine( ps, "threading: " + (threadAmountArg != null ? threadAmountArg : "1") );
        printFine( ps, "db file: " + (dbFileArg != null ? dbFileArg : "default") );
        printFine( ps, "property file: " + (propsFileArg != null ? propsFileArg : "default") );


        // test reference file
        final File referenceFile = getReferenceFile( ps );

        // test track file
        final File[] readsFiles = getReadsFiles( ps );

        // test paired-end read files
        final File[] pairedEndFiles = getPairedEndReadsFiles( ps );

        // setup ProjectConnector
        final ProjectConnector pc = setupProjectConnector( ps );


        /**
         * Create a thread pool.
         * Number of worker threads are based on the threadAmount argument,
         * default is 1 (no multithreading).
         */
        final int noThreads;
        if( threadAmountArg != null ) {
            try {
                noThreads = Integer.parseInt( threadAmountArg );
            } catch( NumberFormatException nfe ) {
                LOG.log( SEVERE, nfe.getMessage(), nfe );
                CommandException ce = new CommandException( 1, "Threads argument not parsable as integer \"" + threadAmountArg + "\"!" );
                ce.initCause( nfe );
                throw ce;
            }
        } else {
            noThreads = 1;
        }
        final ExecutorService es = Executors.newFixedThreadPool( noThreads, new ReadXplorerCliThreadFactory() );


        // import reference
        final ImportReferenceResult referenceResult = importReference( referenceFile, es, ps );
        ParsedReference pr = referenceResult.getParsedReference();



        // import tracks
        try {
            if( pairedEndArg || pairedEndFiles != null ) {
                // import paired-end reads
                importPairedEndReads( readsFiles, pairedEndFiles, referenceResult, es, ps );
            } else {
                // import normal reads
                importReads( readsFiles, referenceResult, es, ps );
            }
        } catch( InterruptedException | ExecutionException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "track import failed!" );
            ce.initCause( ex );
            throw ce;
        }


        // run analyses
        printInfo( ps, "start analyses..." );
        int runAnalyses = 0;



        // print reference info
        ps.println();
        printInfo( ps, "imported reference: " + pr.getName() );
        printInfo( ps, "\tdescription: " + pr.getDescription() );
        printInfo( ps, "\tfasta file: " + pr.getFastaFile().getName() );
        printInfo( ps, "\t# chromosomes: " + pr.getChromosomes().size() );
        // print read info
        printInfo( ps, "" );
        printInfo( ps, "# imported tracks: " + pc.getTracks().size() );
        // print analyses info
        printInfo( ps, "" );
        printInfo( ps, "# run analyses: " + runAnalyses );


        try {
            pc.disconnect();
            printInfo( ps, "disconnected from " + dbFileArg );
        } catch( Exception ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1 );
            ce.initCause( ex );
            throw ce;
        }

    }




    private File getReferenceFile( PrintStream ps ) throws CommandException {

        if( referenceArg != null ) {

            File referenceFile = new File( referenceArg );
            if( !referenceFile.canRead() ) {
                throw new CommandException( 1, "Cannot access reference file ("+ referenceArg +")!" );
            }
            printFine( ps, "reference file to import: " + referenceFile.getName() );

            return referenceFile;

        } else {
            throw new CommandException( 1, "No reference file set!" );
        }

    }


    private File[] getReadsFiles( PrintStream ps ) throws CommandException {

        if( readsArgs != null ) {

            Arrays.sort( readsArgs ); // sort read files lexicographically

            printFine( ps, "read files to import: ("+ readsArgs.length +")" );
            File[] trackFiles = new File[readsArgs.length];
            for( int i = 0; i < readsArgs.length; i++ ) {

                String trackPath = readsArgs[i];

                String fileType = trackPath.substring( trackPath.lastIndexOf( '.' ) + 1 ).toLowerCase();
                if( !checkFileType( fileType ) ) { // check file type
                    throw new CommandException( 1, "Wrong reads file type \"" + fileType + "\"!" );
                }

                File trackFile = new File( trackPath );
                if( !trackFile.canRead() ) { // check file permissions
                    throw new CommandException( 1, "Cannot access read file " + (i+1) + "(" + trackPath + ")!" );
                }

                trackFiles[i] = trackFile;
                printFine( ps, "\tadd " + trackFile.getName() );

            }
            return trackFiles;

        } else {
            throw new CommandException( 1, "No reads files set!" );
        }

    }


    private File[] getPairedEndReadsFiles( PrintStream ps ) throws CommandException {

        if( pairedEndReadsArgs != null ) {

            if( pairedEndReadsArgs.length != readsArgs.length ) {
                throw new CommandException( 1, "Number of paired-end files (" + pairedEndReadsArgs.length + ") does not match number of read files (" + pairedEndReadsArgs.length + ")!" );
            }

            Arrays.sort( pairedEndReadsArgs ); // sort paired-end read files lexicographically

            printFine( ps, "paired-end files to import:" );
            File[] pairedEndFiles = new File[pairedEndReadsArgs.length];
            for( int i = 0; i < pairedEndFiles.length; i++ ) {

                String peTrackPath = pairedEndReadsArgs[i];

                String fileType = peTrackPath.substring( peTrackPath.lastIndexOf( '.' ) + 1 ).toLowerCase();
                if( !checkFileType( fileType ) ) { // check file type
                    throw new CommandException( 1, "Wrong paired-end file type \"" + fileType + "\"!" );
                }

                File pairedEndFile = new File( peTrackPath );
                if( !pairedEndFile.canRead() ) { // check file permissions
                    throw new CommandException( 1, "Cannot access paired-end file " + (i+1) + "(" + peTrackPath + ")!" );
                }

                printFine( ps, "\tadd " + pairedEndFile.getName() );
                pairedEndFiles[i] = pairedEndFile;

            }

            return pairedEndFiles;

        } else {
            return null;
        }

    }




    private ProjectConnector setupProjectConnector( PrintStream ps ) throws CommandException {

        try {

            if( dbFileArg == null ) {
                dbFileArg = DEFAULT_DATABASE_NAME;
            }

            File dbFile = new File( System.getProperty( "user.dir" ) + FileSystems.getDefault().getSeparator() + dbFileArg + H2_FILE_SUFFIX );
            if( dbFile.exists() ) { // delete old copy of the specified file
                dbFile.delete();
            }
            ProjectConnector pc = ProjectConnector.getInstance();
            pc.connect( Properties.ADAPTER_H2, dbFileArg, null, null, null );
            printFine( ps, "connected to " + dbFileArg );
            return pc;

        } catch( SQLException | SecurityException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1 );
            ce.initCause( ex );
            throw ce;
        }

    }


    private ImportReferenceResult importReference( File referenceFile, ExecutorService es, PrintStream ps ) throws CommandException {

        try {

            printFine( ps, "parse reference genome..." );
            Future<ImportReferenceResult> refFuture = es.submit( new ImportReferenceCallable( referenceFile ) );
            ImportReferenceResult referenceResult = refFuture.get();

            // print (concurrent) output sequently
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }
            printInfo( ps, "...done!" );

            // stores reference sequence in the db
            printFine( ps, "store reference to db..." );
            int refGenID = ProjectConnector.getInstance().addRefGenome( referenceResult.getParsedReference() );
            referenceResult.getReferenceJob().setPersistent( refGenID );
            referenceResult.getReferenceJob().setFile( referenceResult.getParsedReference().getFastaFile() );
            printFine( ps, "...done!" );
            printInfo( ps, "imported reference genome source " + referenceResult.getParsedReference().getName() );
            return referenceResult;

        } catch( InterruptedException | ExecutionException | StorageException ex ) {
            LOG.log( Level.SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "reference import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private void importReads( File[] trackFiles, ImportReferenceResult referenceResult, ExecutorService es, PrintStream ps ) throws InterruptedException, ExecutionException {

        printFine( ps, "submit jobs to import read files..." );

        final ProjectConnector pc = ProjectConnector.getInstance();
        // submit track parse jobs for (concurrent) execution
        List<Future<ImportTrackResults>> futures = new ArrayList<>( trackFiles.length );
        for( int i = 0; i < trackFiles.length; i++ ) {

            File trackFile = trackFiles[i];
            TrackJob trackJob = new TrackJob( pc.getLatestTrackId(), trackFile,
                                              trackFile.getName(),
                                              referenceResult.getReferenceJob(),
                                              selectParser( trackFile ),
                                              false,
                                              new Timestamp( System.currentTimeMillis() ) );

            futures.add( es.submit( new ImportTrackCallable( referenceResult, trackJob ) ) );
            printFine( ps, "\t" + i + ": " + trackFile );

        }

        // store parsed tracks sequently to db
        printFine( ps, "import read files..." );
        for( Future<ImportTrackResults> future : futures ) {
            ImportTrackResults result = future.get();
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }
            ParsedTrack pt = result.getParsedTrack();
            pc.storeBamTrack( pt );
            pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );
            printInfo( ps, "\t..." + result.getParsedTrack().getTrackName() );
        }
        printInfo( ps, "...done!" );

    }


    private void importPairedEndReads( File[] trackFiles, File[] pairedEndFiles, ImportReferenceResult referenceResult, ExecutorService es, PrintStream ps ) throws InterruptedException, ExecutionException {

        printFine( ps, "submit jobs to import paired-end read files..." );

        // submit parse reads jobs for (concurrent) execution
        final int distance = Integer.parseInt( getProperty( Constants.PER_DISTANCE ) );
        final byte orientation = (byte) Integer.parseInt( getProperty( Constants.PER_ORIENTATION ) );
        final short deviation = (short) Integer.parseInt( getProperty( Constants.PER_DEVIATION ) );
        final ProjectConnector pc = ProjectConnector.getInstance();
        final List<Future<ImportPairedEndResults>> futures = new ArrayList<>( trackFiles.length );
        for( int i = 0; i < trackFiles.length; i++ ) {

            File trackFile = trackFiles[i];
            Timestamp timestamp = new Timestamp( System.currentTimeMillis() );
            TrackJob trackJob1 = new TrackJob( pc.getLatestTrackId(), trackFile,
                                               trackFile.getName(),
                                               referenceResult.getReferenceJob(),
                                               selectParser( trackFile ),
                                               false,
                                               timestamp );
            TrackJob trackJob2 = null;
            if( pairedEndFiles != null ) {
                File pairedEndFile = pairedEndFiles[i];
                trackJob2 = new TrackJob( pc.getLatestTrackId(), pairedEndFile,
                                          pairedEndFile.getName(),
                                          referenceResult.getReferenceJob(),
                                          selectParser( pairedEndFile ),
                                          false,
                                          timestamp );
            }

            ReadPairJobContainer rpjc = new ReadPairJobContainer( trackJob1, trackJob2, distance, deviation, orientation );
            futures.add( es.submit( new ImportPairedEndCallable( referenceResult, rpjc ) ) );
            printFine( ps, "\t" + i + ": " + trackFile );

        }

        // store parsed reads sequently to db
        printFine( ps, "import paired-end read files..." );
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
            pc.setReadPairIdsForTrackIds( pt.getID(), -1 );
            printFine( ps, "\t..." + result.getParsedTrack().getTrackName() );
        }
        printFine( ps, "...done!" );

    }




    private void printFine( PrintStream ps, String msg ) {

        LOG.fine( msg );
        if( verboseArg ) {
            ps.println( msg );
        }

    }


    private static void printInfo( PrintStream ps, String msg ) {

        LOG.info( msg );
        ps.println( msg );

    }




    /**
     * Loads a property value. Uses either a provided file or the default
     * property file.
     * <p>
     * @param key the property key
     * <p>
     * @return the property value
     */
    private String getProperty( String key ) {

        String val = props.getProperty( key );

        if( val == null ) {
            val = defaultProps.getProperty( key );
        }

        return val;

    }


    /**
     * Loads a <link>java.util.Properties</link> object. Tries to load a
     * property file as specified in the --props argument. If the argument isn't
     * present it will look for it in the current working directory.
     * <p>
     * @return a java.util.Properties object
     * <p>
     * @throws IOException
     */
    private java.util.Properties loadProperties() throws IOException {

        java.util.Properties properties = new java.util.Properties();

        InputStream is = null;
        // check if a prop file is specified
        if( propsFileArg != null ) { // file is specified
            File propsFile = new File( propsFileArg );
            if( !propsFile.canRead() ) {
                throw new FileNotFoundException( "specified file (" + propsFileArg + ") not found!" );
            } else {
                is = new FileInputStream( propsFile );
            }
        } else { // no file specified

            File propsFile = new File( System.getProperty( "user.dir" ), "readxplorer-cli.properties" );
            // check if props file is present in current working directory
            if( propsFile.canRead() ) {
                is = new FileInputStream( propsFile );
            }

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
     * <p>
     * @return a java.util.Properties object
     * <p>
     * @throws IOException
     */
    private static java.util.Properties loadDefaultProperties() throws IOException {

        java.util.Properties properties = new java.util.Properties();

        try( BufferedInputStream bis = new BufferedInputStream( CommandLineProcessor.class.getResourceAsStream( "/bio/comp/jlu/readxplorer/cli/readxplorer-cli.properties" ) ) ) {
            properties.load( bis );
        }

        return properties;

    }


    private static MappingParserI selectParser( File trackFile ) {

        switch( trackFile.getName().substring( trackFile.getName().lastIndexOf( '.' ) + 1 ).toLowerCase() ) {
            case "out":
            case "jok":
                return new JokToBamDirectParser();
            case "sam":
            case "bam":
            default:
                return new SamBamParser();
        }

    }


    private static boolean checkFileType( String fileType ) {

        return "sam".equals( fileType ) ||
               "bam".equals( fileType ) ||
               "out".equals( fileType ) ||
               "jok".equals( fileType );

    }


}
