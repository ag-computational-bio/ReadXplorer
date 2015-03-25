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


import bio.comp.jlu.readxplorer.cli.analyses.AnalysisCallable.AnalysisResult;
import bio.comp.jlu.readxplorer.cli.analyses.CoverageAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.analyses.OperonDetectionAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.analyses.RPKMAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.analyses.SNPAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.analyses.TSSAnalysisCallable;
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
    @Description( shortDescription = "Directory with SAM/BAM read files to import / analysis." )
    public String readsDirArg;

    @Arg( shortName = 'e', longName = "per" )
    @Description( shortDescription = "Directory with SAM/BAM paired-end read files to import / analysis." )
    public String pairedEndReadsDirArg;


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
    @Arg( longName = "cvrg" )
    @Description( shortDescription = "Perform coverage analyses on all tracks." )
    public boolean cvrgAnalysis;

    @Arg( longName = "opdn" )
    @Description( shortDescription = "Perform operon detection analyses on all tracks." )
    public boolean opdnAnalysis;

    @Arg( longName = "rpkm" )
    @Description( shortDescription = "Perform reads per kilobase of transcript per million (RPKM) analyses on all tracks." )
    public boolean rpkmAnalysis;

    @Arg( longName = "snp" )
    @Description( shortDescription = "Perform single nucleotide polymorphism (SNP) analyses on all tracks." )
    public boolean snpAnalysis;

    @Arg( longName = "tss" )
    @Description( shortDescription = "Perform transcription start site (TSS) analyses on all tracks." )
    public boolean tssAnalysis;


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


        printFine( ps, null );
        // test reference file
        final File referenceFile = getReferenceFile( ps );

        // test track file
        final File[] readFiles = getReadFiles( ps );

        // test paired-end read files
        final File[] pairedEndReadFiles = getPairedEndReadFiles( ps, readFiles );

        // setup ProjectConnector
        final ProjectConnector pc = setupProjectConnector( ps );

        // setup ExecutorService as worker thread pool
        final ExecutorService es = createWorkerThreadPool();


        // import reference
        final ImportReferenceResult referenceResult = importReference( referenceFile, es, ps );
        final ParsedReference pr = referenceResult.getParsedReference();



        // import tracks
        try {
            if( pairedEndArg || pairedEndReadFiles != null ) {
                // import paired-end reads
                importPairedEndReads( readFiles, pairedEndReadFiles, referenceResult, es, ps );
            } else {
                // import normal reads
                importReads( readFiles, referenceResult, es, ps );
            }
        } catch( InterruptedException | ExecutionException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "track import failed!" );
            ce.initCause( ex );
            throw ce;
        }


        // run analyses
        int runAnalyses = runAnalyses( es, ps );


        // print reference info
        ps.println();
        printInfo( ps, "imported reference: " + pr.getName() );
        printInfo( ps, "\tdescription: " + pr.getDescription() );
        printInfo( ps, "\tfasta file: " + pr.getFastaFile().getName() );
        printInfo( ps, "\t# chromosomes: " + pr.getChromosomes().size() );
        // print read info
        printInfo( ps, null );
        printInfo( ps, "# imported tracks: " + pc.getTracks().size() );
        // print analyses info
        printInfo( ps, null );
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




    private File getReferenceFile( final PrintStream ps ) throws CommandException {

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


    private File[] getReadFiles( final PrintStream ps ) throws CommandException {

        if( readsDirArg != null ) {

            File readsDir = new File( readsDirArg );
            readsDir = readsDir.toPath().toAbsolutePath().normalize().toFile();
            if( !readsDir.isDirectory() ) {
                throw new CommandException( 1, readsDirArg + " is not a directory!" );
            }
            if( !readsDir.canRead()) {
                throw new CommandException( 1, "directory " + readsDirArg + " is not readable!" );
            }

            // get all sam/bam files in lexicographic order
            File[] readFiles = readsDir.listFiles( new ReadsFileFilter() );
            Arrays.sort( readFiles );

            printFine( ps, "read files to import: ("+ readFiles.length +")" );
            for( int i = 0; i < readFiles.length; i++ ) {

                File readFile = readFiles[i];
                if( !readFile.canRead() ) { // check file permissions
                    throw new CommandException( 1, "Cannot access read file " + (i+1) + '(' + readFile.getName() + ")!" );
                }
                printFine( ps, "\t- " + readFile.getName() );

            }
            return readFiles;

        } else {
            throw new CommandException( 1, "No read files set!" );
        }

    }


    private File[] getPairedEndReadFiles( final PrintStream ps, File[] readFiles ) throws CommandException {

        if( pairedEndReadsDirArg != null ) {

            File pairedEndReadsDir = new File( pairedEndReadsDirArg );
            pairedEndReadsDir = pairedEndReadsDir.toPath().toAbsolutePath().normalize().toFile();
            if( !pairedEndReadsDir.isDirectory() ) {
                throw new CommandException( 1, pairedEndReadsDirArg + " is not a directory!" );
            }
            if( !pairedEndReadsDir.canRead()) {
                throw new CommandException( 1, "directory " + pairedEndReadsDirArg + " is not readable!" );
            }

            // get all sam/bam files in lexicographic order
            File[] pairedEndReadFiles = pairedEndReadsDir.listFiles( new ReadsFileFilter() );
            Arrays.sort( pairedEndReadFiles );

            if( pairedEndReadFiles.length != readFiles.length ) {
                throw new CommandException( 1, "Number of paired-end files (" + pairedEndReadFiles.length + ") does not match number of read files (" + readFiles.length + ")!" );
            }

            printFine( ps, "paired-end read files to import: ("+ pairedEndReadFiles.length +")" );
            for( int i = 0; i < pairedEndReadFiles.length; i++ ) {

                File pairedEndReadFile = pairedEndReadFiles[i];
                if( !pairedEndReadFile.canRead() ) { // check file permissions
                    throw new CommandException( 1, "Cannot access paired-end file " + (i+1) + "(" + pairedEndReadFile.getName() + ")!" );
                }

                printFine( ps, "\t- " + pairedEndReadFile.getName() );

            }

            return pairedEndReadFiles;

        } else {
            throw new CommandException( 1, "No paired-end read files set!" );
        }

    }


    private ProjectConnector setupProjectConnector( final PrintStream ps ) throws CommandException {

        try {

            printFine( ps, null );

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


    private ExecutorService createWorkerThreadPool() throws CommandException {

        /**
         * Create a thread pool. Number of worker threads are based on the
         * threadAmount argument, default is 1 (no multithreading).
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

        return Executors.newFixedThreadPool( noThreads, new ReadXplorerCliThreadFactory() );

    }




    private ImportReferenceResult importReference( final File referenceFile, final ExecutorService es, final PrintStream ps ) throws CommandException {

        try {

            printFine( ps, "parse and store reference... " );
            Future<ImportReferenceResult> refFuture = es.submit( new ImportReferenceCallable( referenceFile ) );
            ImportReferenceResult referenceResult = refFuture.get();

            // print (concurrent) output sequently
            for( String msg : referenceResult.getOutput() ) {
                printInfo( ps, msg );
            }

            // stores reference sequence in the db
            int refGenID = ProjectConnector.getInstance().addRefGenome( referenceResult.getParsedReference() );
            referenceResult.getReferenceJob().setPersistent( refGenID );
            referenceResult.getReferenceJob().setFile( referenceResult.getParsedReference().getFastaFile() );
            printInfo( ps, "imported reference " + referenceFile.getName() );
            return referenceResult;

        } catch( InterruptedException | ExecutionException | StorageException ex ) {
            LOG.log( Level.SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "reference import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private void importReads( final File[] trackFiles, final ImportReferenceResult referenceResult, final ExecutorService es, final PrintStream ps ) throws InterruptedException, ExecutionException {

        printInfo( ps, null );
        printFine( ps, "submitted jobs to import read files..." );

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
        printFine( ps, null );
        printFine( ps, "imported read files:" );
        for( Future<ImportTrackResults> future : futures ) {
            ImportTrackResults result = future.get();
            for( String msg : result.getOutput() ) {
                printInfo( ps, msg );
            }
            ParsedTrack pt = result.getParsedTrack();
            pc.storeBamTrack( pt );
            pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );
            printInfo( ps, "\t- " + result.getParsedTrack().getTrackName() );
        }

    }


    private void importPairedEndReads( final File[] trackFiles, final File[] pairedEndFiles, final ImportReferenceResult referenceResult, final ExecutorService es, final PrintStream ps ) throws InterruptedException, ExecutionException {

        printInfo( ps, null );
        printFine( ps, "submitted jobs to import paired-end read files..." );

        // submit parse reads jobs for (concurrent) execution
        final int distance = Integer.parseInt( getProperty( Constants.PER_DISTANCE ) );
        final byte orientation = (byte) Integer.parseInt( getProperty( Constants.PER_ORIENTATION ) );
        final short deviation = (short) Integer.parseInt( getProperty( Constants.PER_DEVIATION ) );

        final ProjectConnector pc = ProjectConnector.getInstance();
        int latestTrackId = pc.getLatestTrackId();

        final List<Future<ImportPairedEndResults>> futures = new ArrayList<>( trackFiles.length );
        for( int i = 0; i < trackFiles.length; i++ ) {

            File trackFile = trackFiles[i];
            Timestamp timestamp = new Timestamp( System.currentTimeMillis() );
            TrackJob trackJob1 = new TrackJob( latestTrackId, trackFile,
                                               trackFile.getName(),
                                               referenceResult.getReferenceJob(),
                                               selectParser( trackFile ),
                                               false,
                                               timestamp );
            latestTrackId++;
            TrackJob trackJob2 = null;
            if( pairedEndFiles != null ) {
                File pairedEndFile = pairedEndFiles[i];
                trackJob2 = new TrackJob( latestTrackId, pairedEndFile,
                                          pairedEndFile.getName(),
                                          referenceResult.getReferenceJob(),
                                          selectParser( pairedEndFile ),
                                          false,
                                          timestamp );
                latestTrackId++;
            }

            ReadPairJobContainer rpjc = new ReadPairJobContainer( trackJob1, trackJob2, distance, deviation, orientation );
            futures.add( es.submit( new ImportPairedEndCallable( referenceResult, rpjc ) ) );
            printFine( ps, "\t" + i + ": " + trackFile );

        }

        // store parsed reads sequently to db
        printFine( ps, null );
        printFine( ps, "imported paired-end read files:" );
        for( Future<ImportPairedEndResults> future : futures ) {
            ImportPairedEndResults result = future.get();
            for( String msg : result.getOutput() ) {
                printInfo( ps, msg );
            }

            // store track entry in db
            ParsedTrack pt = result.getParsedTrack();
            pc.storeBamTrack( pt );
            pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );

            // read pair ids have to be set in track entry
            pc.setReadPairIdsForTrackIds( pt.getID(), -1 );
            printFine( ps, "\t- " + result.getParsedTrack().getTrackName() );
        }

    }


    private int runAnalyses( final ExecutorService es, final PrintStream ps ) {


        printInfo( ps, null );
        printFine( ps, "submitted analyses:" );
        int runAnalyses = 0;

        final List<Future<AnalysisResult>> futures = new ArrayList<>();
        if( cvrgAnalysis ) {
            runAnalyses++;
            CoverageAnalysisCallable cvrgAnalysisCallable = new CoverageAnalysisCallable( verboseArg );
            futures.add( es.submit( cvrgAnalysisCallable ) );
            printFine( ps, "\t"+ runAnalyses +": coverage analysis" );
        }

        if( opdnAnalysis ) {
            runAnalyses++;
            OperonDetectionAnalysisCallable opdnAnalysisCallable = new OperonDetectionAnalysisCallable( verboseArg );
            es.submit( opdnAnalysisCallable );
            printFine( ps, "\t"+ runAnalyses +": operon detection analysis" );
        }

        if( rpkmAnalysis ) {
            runAnalyses++;
            RPKMAnalysisCallable rpkmAnalysisCallable = new RPKMAnalysisCallable( verboseArg );
            es.submit( rpkmAnalysisCallable );
            printFine( ps, "\t"+ runAnalyses +": RPKM analysis" );
        }

        if( snpAnalysis ) {
            runAnalyses++;
            SNPAnalysisCallable snpAnalysisCallable = new SNPAnalysisCallable( verboseArg );
            es.submit( snpAnalysisCallable );
            printFine( ps, "\t"+ runAnalyses +": SNP analysis" );
        }

        if( tssAnalysis ) {
            runAnalyses++;
            TSSAnalysisCallable tssAnalysisCallable = new TSSAnalysisCallable( verboseArg );
            es.submit( tssAnalysisCallable );
            printFine( ps, "\t"+ runAnalyses +": TSS analysis" );
        }


        printInfo( ps, null );
        printInfo( ps, "finished analyses:" );
        for( int i=0; i<futures.size(); i++ ) {

            Future<AnalysisResult> future = futures.get( i );
            try {

                AnalysisResult analysisResult = future.get();
                String resultFilePath = analysisResult.getResultFile();
                if( resultFilePath != null ) {
                    printInfo( ps, "\t"+ i +" "+ analysisResult.getType() + "\tresult file: " + analysisResult.getResultFile() );
                }
                else {
                    printInfo( ps, "\t"+ i +" "+ analysisResult.getType() + " crashed:" );
                    for( String msg : analysisResult.getOutput() ) {
                        printInfo( ps, "\t\t"+msg );
                    }
                }

            } catch( InterruptedException | ExecutionException ex ) {
                LOG.log( SEVERE, ex.getMessage(), ex );
                printInfo( ps, dbFileArg );
            }

        }

        return runAnalyses;

    }


    private void printFine( final PrintStream ps, final String msg ) {

        if( msg != null ) {
            LOG.fine( msg );
        }
        if( verboseArg ) {
            ps.println( msg != null ? msg : "" );
        }

    }


    private static void printInfo( final PrintStream ps, final String msg ) {

        if( msg != null ) {
            LOG.info( msg );
            ps.println( msg );
        } else {
            ps.println();
        }

    }




    /**
     * Loads a property value. Uses either a provided file or the default
     * property file.
     * <p>
     * @param key the property key
     * <p>
     * @return the property value
     */
    private String getProperty( final String key ) {

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




    private static MappingParserI selectParser( final File trackFile ) {

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


}
