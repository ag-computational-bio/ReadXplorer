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
import bio.comp.jlu.readxplorer.cli.analyses.SNPAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.analyses.TSSAnalysisCallable;
import bio.comp.jlu.readxplorer.cli.constants.PairedEndConstants;
import bio.comp.jlu.readxplorer.cli.constants.SNPConstants;
import bio.comp.jlu.readxplorer.cli.constants.TSSConstants;
import bio.comp.jlu.readxplorer.cli.filefilter.AnalysisFileFilter;
import bio.comp.jlu.readxplorer.cli.filefilter.ReadsFileFilter;
import bio.comp.jlu.readxplorer.cli.imports.ImportPairedEndCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportPairedEndCallable.ImportPairedEndResults;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable.ImportTrackResults;
import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.StorageException;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.mappings.JokToBamDirectParser;
import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import de.cebitec.readxplorer.parser.mappings.SamBamParser;
import de.cebitec.readxplorer.tools.snpdetection.ParameterSetSNPs;
import de.cebitec.readxplorer.transcriptionanalyses.ParameterSetTSS;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Blank;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import static bio.comp.jlu.readxplorer.cli.analyses.CLIAnalyses.SNP;
import static bio.comp.jlu.readxplorer.cli.analyses.CLIAnalyses.TSS;
import static java.util.logging.Level.SEVERE;


/**
 * Entry Point of ReadXplorer's CLI Version.
 * The <code>CommandLineProcessor</code> is responsible of:
 * <ol>
 * <li>option and argument handling</li>
 * <li>orchestration of import and analysis threads</li>
 * <li>output to the CLI and persistence of merged analysis files</li>
 * </ol>
 * <p>
 * The following options and arguments are available:
 * <p>
 * Mandatory:
 * <ul>
 * <li>--ref {file}: the reference genome to import</li>
 * <li>--reads {dir}: a directory with read files</li>
 * <li>--per {dir}: a directory with paired-end read files</li>
 * </ul>
 * <p>
 * Optional:
 * <ul>
 * <li>--db {name}: name of newly created H2 database file</li>
 * <li>--props {file}: a customised property file</li>
 * <li>--threads {n}: enables multithreading with {n} worker threads</li>
 * <li>-p / --pairedend: files stored in -r/--reads dir are combined paired-end files</li>
 * <li>-v / --verbose: enables verbose output</li>
 * <li>-h / --help: prints a usage to STD out</li>
 * </ul>
 * <p>
 * Analyses Options:
 * <ul>
 * <li>--tss: perform TSS analses for all imported tracks</li>
 * <li>--snp: perform SNP analses for all imported tracks</li>
 * </ul>
 */
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
    @Arg( longName = "ref" )
    @Description( shortDescription = "Reference genome to import / analysis." )
    public String referenceArg;

    @Arg( longName = "reads" )
    @Description( shortDescription = "Directory with SAM/BAM read files to import / analysis." )
    public String readsDirArg;

    @Arg( longName = "per" )
    @Description( shortDescription = "Directory with SAM/BAM paired-end read files to import / analysis." )
    public String pairedEndReadsDirArg;


    /**
     * Optional options
     */
    @Arg( longName = "threads" )
    @Description( shortDescription = "Specifies the number of available worker threads. Take care on multi user systems!" )
    public String threadAmountArg;

    @Arg( longName = "db" )
    @Description( shortDescription = "Set a database name to persistently store imported data (default: "+DEFAULT_DATABASE_NAME+"). ATTENTION! Existing databases will be deleted!" )
    public String dbFileArg;

    @Arg( longName = "props" )
    @Description( shortDescription = "Sets the path to a custom property file." )
    public String propsFileArg;

    @Arg( shortName = 'p', longName = "pairedend" )
    @Description( shortDescription = "Set this flag if reads are paired-end reads." )
    public boolean pairedEndArg;

    @Arg( shortName = 'v', longName = "verbose" )
    @Description( shortDescription = "Print detailed messages to the console." )
    public boolean verboseArg;

    @Arg( shortName = 'h', longName = "help" )
    @Description( shortDescription = "Print usage information to the console." )
    public boolean helpArg;


    /**
     * Analysis options
     */
    /**
    @Arg( longName = "cvrg" )
    @Description( shortDescription = "Perform coverage analyses on all tracks." )
    public boolean cvrgAnalysis;

    @Arg( longName = "opdt" )
    @Description( shortDescription = "Perform operon detection analyses on all tracks." )
    public boolean opdnAnalysis;

    @Arg( longName = "rpkm" )
    @Description( shortDescription = "Perform reads per kilobase of transcript per million (RPKM) analyses on all tracks." )
    public boolean rpkmAnalysis;
    */

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

        final long startTime = System.currentTimeMillis();

        // check if help/usgae is requested
        if( helpArg ) {
            env.usage();
            return;
        }


        // print optional arguments...
        final PrintStream ps = env.getOutputStream();
        printFine( ps, "\nrun parameters: " );
        printFine( ps, "\tverbosity: " + (verboseArg ? "on" : "off") );
        printFine( ps, "\tpaired end: " + ((pairedEndArg  ||  pairedEndReadsDirArg!=null) ? "yes" : "no") );
        printFine( ps, "\tthreading: " + (threadAmountArg != null ? threadAmountArg : "1") );
        printFine( ps, "\tdb file: " + (dbFileArg != null ? dbFileArg : "default") );
        printFine( ps, "\tproperty file: " + (propsFileArg != null ? propsFileArg : "default") );


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
        if( pairedEndArg || pairedEndReadFiles != null ) {
            // import paired-end reads
            importPairedEndReads( readFiles, pairedEndReadFiles, referenceResult, es, ps );
        } else {
            // import normal reads
            importReads( readFiles, referenceResult, es, ps );
        }


        // run analyses
        int runAnalyses = runAnalyses( es, ps );


        // print information
        ps.println();
        printInfo( ps, "reference genome:" );
        printFine( ps, "\tname: " + pr.getName() );
        String desc = pr.getDescription();
        if( desc != null  &&  !desc.isEmpty() ) {
            printFine( ps, "\tdescription: " + desc );
        }
        printFine( ps, "\tfasta file: " + pr.getFastaFile().getName() );
        printFine( ps, "\t# chromosomes: " + pr.getChromosomes().size() );
        printInfo( ps, "# tracks: " + pc.getTracks().size() );
        printInfo( ps, "# analyses: " + runAnalyses );


        try {
            pc.disconnect();
            printFine( ps, null );
            printFine( ps, "disconnected from " + dbFileArg );
        } catch( Exception ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1 );
            ce.initCause( ex );
            throw ce;
        }

        printRuntime( startTime, ps );

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

        if( pairedEndArg ) {
            return null;
        } else if( pairedEndReadsDirArg != null ) {

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
                printFine( ps, "\t" + msg );
            }

            // stores reference sequence in the db
            int refGenID = ProjectConnector.getInstance().addRefGenome( referenceResult.getParsedReference() );
            referenceResult.getReferenceJob().setPersistent( refGenID );
            referenceResult.getReferenceJob().setFile( referenceResult.getParsedReference().getFastaFile() );
            printInfo( ps, "imported reference " + referenceFile.getName() );
            return referenceResult;

        } catch( InterruptedException | ExecutionException | StorageException ex ) {
            LOG.log( Level.SEVERE, ex.getMessage(), ex );
            printInfo( ps, "import of reference file failed:" );
            printInfo( ps, "reason: "+ ex.getMessage() );
            CommandException ce = new CommandException( 1, "reference import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private void importReads( final File[] trackFiles, final ImportReferenceResult referenceResult, final ExecutorService es, final PrintStream ps ) throws CommandException {

        printFine( ps, null );
        printFine( ps, "submitted jobs to import read files..." );

        final ProjectConnector pc = ProjectConnector.getInstance();
        int latestTrackId = pc.getLatestTrackId();
        // submit track parse jobs for (concurrent) execution
        List<Future<ImportTrackResults>> futures = new ArrayList<>( trackFiles.length );
        for( int i = 0; i < trackFiles.length; i++ ) {

            File trackFile = trackFiles[i];
            TrackJob trackJob = new TrackJob( latestTrackId, trackFile,
                                              trackFile.getName(),
                                              referenceResult.getReferenceJob(),
                                              selectParser( trackFile ),
                                              false,
                                              new Timestamp( System.currentTimeMillis() ) );
            latestTrackId++;

            futures.add( es.submit( new ImportTrackCallable( referenceResult, trackJob ) ) );
            printFine( ps, "\t" + (i+1) + ": " + trackFile );

        }

        // store parsed tracks sequently to db
        printInfo( ps, null );
        printInfo( ps, "imported read files:" );
        try {

            for( int i = 0; i < futures.size(); i++ ) {
                ImportTrackResults result = futures.get( i ).get();
                ParsedTrack pt = result.getParsedTrack();

                // store track entry in db
                pc.storeBamTrack( pt );
                pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );

                // print result information
                if( result.isSuccessful() ) {
                    printInfo( ps, "\t" + (i+1) + " " + result.getFileName() );
                    for( String msg : result.getOutput() ) {
                        printFine( ps, "\t\t" + msg );
                    }
                } else {
                    printInfo( ps, "\t" + (i+1) + " " + result.getFileName() + " crashed!" );
                    for( String msg : result.getOutput() ) {
                        printInfo( ps, "\t\t" + msg );
                    }
                }

            }

        } catch( InterruptedException | ExecutionException ex ) { // something severe happened, stop everything!
            LOG.log( SEVERE, ex.getMessage(), ex );
            printInfo( ps, "import of read file failed:" );
            printInfo( ps, "reason: "+ ex.getMessage() );
            CommandException ce = new CommandException( 1, "track import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private void importPairedEndReads( final File[] trackFiles, final File[] pairedEndFiles, final ImportReferenceResult referenceResult, final ExecutorService es, final PrintStream ps ) throws CommandException {

        printFine( ps, null );
        printFine( ps, "submitted jobs to import paired-end read files..." );

        // submit parse reads jobs for (concurrent) execution
        final int distance = Integer.parseInt( getProperty( PairedEndConstants.PER_DISTANCE ) );
        final byte orientation = (byte) Integer.parseInt( getProperty( PairedEndConstants.PER_ORIENTATION ) );
        final short deviation = (short) Integer.parseInt( getProperty( PairedEndConstants.PER_DEVIATION ) );

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
            if( pairedEndFiles != null ) { // if no pairedEndFiles are available, this is a paired end track within a combined read file!
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
            printFine( ps, "\t" + (i+1) + ": " + trackFile );

        }

        // store parsed reads sequently to db
        printInfo( ps, null );
        printInfo( ps, "imported paired-end read files:" );
        try {

            for( int i = 0; i < futures.size(); i++ ) {

                ImportPairedEndResults result = futures.get( i ).get();

                // store track entry in db
                ParsedTrack pt = result.getParsedTrack();
                pc.storeBamTrack( pt );
                pc.storeTrackStatistics( pt.getStatsContainer(), pt.getID() );

                // read pair ids have to be set in track entry
                pc.setReadPairIdsForTrackIds( pt.getID(), -1 );

                // print result information
                if( result.isSuccessful() ) {
                    printInfo( ps, "\t" + (i+1) + " " + result.getFileName() );
                    for( String msg : result.getOutput() ) {
                        printFine( ps, "\t\t" + msg );
                    }
                } else {
                    printInfo( ps, "\t" + (i+1) + " " + result.getFileName() + " crashed!" );
                    for( String msg : result.getOutput() ) {
                        printInfo( ps, "\t\t" + msg );
                    }
                }

            }

        } catch( InterruptedException | ExecutionException ex ) { // something severe happened, stop everything!
            LOG.log( SEVERE, ex.getMessage(), ex );
            printInfo( ps, "import of paired-end read file failed:" );
            printInfo( ps, "reason: "+ ex.getMessage() );
            CommandException ce = new CommandException( 1, "track import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private int runAnalyses( final ExecutorService es, final PrintStream ps ) throws CommandException {


        printInfo( ps, null );
        printFine( ps, "submitted analyses:" );
        int runAnalyses = 0;
        final ProjectConnector pc = ProjectConnector.getInstance();

        final List<Future<AnalysisResult>> futures = new ArrayList<>();

        /**
        if( cvrgAnalysis ) {
            runAnalyses++;
            printFine( ps, "\t"+ runAnalyses +": coverage analysis" );
            CoverageAnalysisCallable cvrgAnalysisCallable = new CoverageAnalysisCallable( verboseArg );
            futures.add( es.submit( cvrgAnalysisCallable ) );
        }

        if( opdnAnalysis ) {
            runAnalyses++;
            printFine( ps, "\t"+ runAnalyses +": operon detection analysis" );
            OperonDetectionAnalysisCallable opdnAnalysisCallable = new OperonDetectionAnalysisCallable( verboseArg );
            futures.add( es.submit( opdnAnalysisCallable ) );
        }

        if( rpkmAnalysis ) {
            runAnalyses++;
            printFine( ps, "\t"+ runAnalyses +": RPKM analysis" );
            RPKMAnalysisCallable rpkmAnalysisCallable = new RPKMAnalysisCallable( verboseArg );
            futures.add( es.submit( rpkmAnalysisCallable ) );
        }
        */

        if( snpAnalysis ) {
            runAnalyses++;
            printFine( ps, "\t"+ runAnalyses +": SNP analysis" );

            // create necessary parameter objects for all analyses
            boolean useMainBases = Boolean.parseBoolean( getProperty( SNPConstants.SNP_COUNT_MAIN_BASES ) );
            byte minBaseQuality    = Byte.parseByte( getProperty( SNPConstants.SNP_MIN_BASE_QUALITY ) );
            byte minAvrBaseQuality = Byte.parseByte( getProperty( SNPConstants.SNP_MIN_AVERAGE_BASE_QUALITY ) );
            byte minMappingQuality = Byte.parseByte( getProperty( SNPConstants.SNP_MIN_MAPPING_QUALITY ) );
            int minVaryingBases      = Integer.parseInt( getProperty( SNPConstants.SNP_MIN_MISMATCH_BASES ) );
            int minAvrMappingQuality = Integer.parseInt( getProperty( SNPConstants.SNP_MIN_AVERAGE_MAPPING_QUALITY ) );
            double minPercVariation  = Double.parseDouble( getProperty( SNPConstants.SNP_MIN_VARIATION ) );
            Set<FeatureType> selFeatureTypes      = getSelectedFeatureTypes( getProperty( SNPConstants.SNP_FEATURE_TYPES ) );
            ParametersReadClasses readClassParams = getParametersReadClasses( getProperty( SNPConstants.SNP_MAPPING_CLASSES ), minMappingQuality, Strand.Feature );

            final ParameterSetSNPs parameterSet = new ParameterSetSNPs( minVaryingBases, minPercVariation, useMainBases, selFeatureTypes,
                                                    readClassParams, minBaseQuality, minAvrBaseQuality, minAvrMappingQuality );

            for( PersistentTrack persistentTrack : pc.getTracks() ) {
                SNPAnalysisCallable snpAnalysisCallable = new SNPAnalysisCallable( verboseArg, persistentTrack, parameterSet );
                futures.add( es.submit( snpAnalysisCallable ) );
                File trackFile = new File( persistentTrack.getFilePath() );
                printFine( ps, "\t\t" + trackFile.getName() );
            }
        }

        if( tssAnalysis ) {
            runAnalyses++;
            printFine( ps, "\t"+ runAnalyses +": TSS analysis" );

            boolean autoTssParamEstimation     = Boolean.parseBoolean( getProperty( TSSConstants.TSS_PARAMETER_ESTIMATION ) );
            boolean associateTSS               = Boolean.parseBoolean( getProperty( TSSConstants.TSS_ASSOCIATE ) );
            boolean performUnannotatedTransDet = Boolean.parseBoolean( getProperty( TSSConstants.TSS_UNANNOTATED_DETECTION ) );
            byte minMappingQuality             = Byte.parseByte( getProperty( TSSConstants.TSS_MIN_MAPPING_QUALITY ) );
            Strand strandUsage                 = Strand.fromType( Integer.parseInt( getProperty( TSSConstants.TSS_STRAND_USAGE ) ) );
            ParametersReadClasses readClassParams = getParametersReadClasses( getProperty( TSSConstants.TSS_MAPPING_CLASSES ), minMappingQuality, strandUsage );
                readClassParams.setStrandOption( Strand.BothForward );
            int minIncreaseTotal             = Integer.parseInt( getProperty( TSSConstants.TSS_MIN_INCREASE_TOTAL ) );
            int minIncreasePercent           = Integer.parseInt( getProperty( TSSConstants.TSS_MIN_INCREASE_PERCENT ) );
            int maxFeatureDistance           = Integer.parseInt( getProperty( TSSConstants.TSS_MAX_FEATURE_DISTANCE ) );
            int maxLeaderlessFeatureDistance = Integer.parseInt( getProperty( TSSConstants.TSS_MAX_LEADERLESS_FEATURE_DISTANCE ) );
            int associateTssWindow           = Integer.parseInt( getProperty( TSSConstants.TSS_ASSOCIATE_WINDOW ) );
            int maxLowCovInitCount           = Integer.parseInt( getProperty( TSSConstants.TSS_MAX_LOW_COVERAGE_INIT ) );
            int minLowCovIncrease            = Integer.parseInt( getProperty( TSSConstants.TSS_MIN_LOW_COVERAGE_INCREASE ) );
            int minTransExtensionCov         = Integer.parseInt( getProperty( TSSConstants.TSS_MIN_TRANSCRIPT_EXTENSION_COVERAGE ) );

            final ParameterSetTSS parameterSet = new ParameterSetTSS( true, autoTssParamEstimation, performUnannotatedTransDet,
                                                 minIncreaseTotal, minIncreasePercent, maxLowCovInitCount, minLowCovIncrease, minTransExtensionCov,
                                                 maxLeaderlessFeatureDistance, maxFeatureDistance, associateTSS, associateTssWindow, readClassParams );

            for( PersistentTrack persistentTrack : pc.getTracks() ) {
                TSSAnalysisCallable tssAnalysisCallable = new TSSAnalysisCallable( verboseArg, persistentTrack, parameterSet );
                futures.add( es.submit( tssAnalysisCallable ) );
                File trackFile = new File( persistentTrack.getFilePath() );
                printFine( ps, "\t\t" + trackFile.getName() );
            }
        }


        printInfo( ps, null );
        printInfo( ps, "finished analyses:" );
        try {

            for( int i = 0; i < futures.size(); i++ ) {

                Future<AnalysisResult> future = futures.get( i );
                AnalysisResult analysisResult = future.get();
                File resultFile = analysisResult.getResultFile();
                if( resultFile != null ) { // successful run
                    printInfo( ps, "\t"+ (i+1) +" "+ analysisResult.getType() + "\tresult file: " + resultFile );
                    for( String msg : analysisResult.getOutput() ) {
                        printFine( ps, "\t\t"+msg );
                    }
                } else { // something went wrong during this analysis, other runs are not compromised
                    printInfo( ps, "\t"+ (i+1) +" "+ analysisResult.getType() + " crashed:" );
                    for( String msg : analysisResult.getOutput() ) {
                        printInfo( ps, "\t\t"+msg );
                    }
                }

            }

        } catch( InterruptedException | ExecutionException ex ) { // something severe happened, stop everything!
            LOG.log( SEVERE, ex.getMessage(), ex );
            printInfo( ps, "analysis failed:" );
            printInfo( ps, "reason: "+ ex.getMessage() );
            CommandException ce = new CommandException( 1, "analysis failed!" );
            ce.initCause( ex );
            throw ce;
        }

        printInfo( ps, null );
        printInfo( ps, "combined analyses result files:" );
        if( snpAnalysis ) {
            mergeAnlaysisFiles( ps, "snp", new AnalysisFileFilter( SNP ) );
        }
        if( snpAnalysis ) {
            mergeAnlaysisFiles( ps, "tss", new AnalysisFileFilter( TSS ) );
        }

        return runAnalyses;

    }




    private void printRuntime( long startTime, final PrintStream ps ) {

        long endTime = System.currentTimeMillis();
        int runTime = (int) (endTime - startTime);

        int hours = runTime / (60 * 60 * 1000);
        runTime -= hours * 60 * 60 * 1000;

        int mins  = runTime / (60 * 1000);
        runTime -= mins * 60 * 1000;

        int secs  = runTime / 1000;

        printFine( ps, "total run time: " + hours + "h " + mins + "m " + secs + "s" );

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




    private static void mergeAnlaysisFiles( final PrintStream ps, final String analysisType, final FileFilter fileFilter ) throws CommandException {

        try {
            File analysesFile = new File( analysisType + "-analyses.xls" );
            WritableWorkbook wwb = Workbook.createWorkbook( analysesFile );
            int idxSheet = 0;
            for( File analysisFile : (new File(".")).getCanonicalFile().listFiles( fileFilter ) ) {
                Workbook wb = Workbook.getWorkbook( analysisFile );
                String analysisName = analysisFile.getName().replace( analysisType + "-", "" ).replace( ".xls", "" );
                if( idxSheet == 0 ) { // copy analysis statistics
                    copyExcelSheet( wwb, wb.getSheet( 1 ), idxSheet, "statistics" );
                    idxSheet++;
                }

                copyExcelSheet( wwb, wb.getSheet( 0 ), idxSheet, analysisName );
                idxSheet++;
                wb.close();
                analysisFile.delete(); // delete unnecessary analysis file
            }
            wwb.write();
            wwb.close();
            printInfo( ps, "\t" + analysisType + ": " + analysesFile.getName() );

        } catch( IOException | BiffException | IndexOutOfBoundsException | WriteException ex ) {
            LOG.log( SEVERE, "ERROR: merge " + analysisType + " analysis files: " + ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "merge of " + analysisType + " analysis files failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    private static void copyExcelSheet( WritableWorkbook wwb, Sheet sourceSheet, int idxSheet, String sheetName ) throws WriteException {

        final WritableSheet ws = wwb.createSheet( sheetName, idxSheet );
        final int noRows = sourceSheet.getRows();
        final int noCols = sourceSheet.getColumns();
        for( int idxRow = 0; idxRow < noRows; idxRow++ ) {
            for( int idxCol = 0; idxCol < noCols; idxCol++ ) {
                WritableCell wc;
                String cellCntnt = sourceSheet.getCell( idxCol, idxRow ).getContents();
                if( cellCntnt != null && !cellCntnt.isEmpty() ) {
                    try {
                        double val = Double.parseDouble( cellCntnt );
                        wc = new jxl.write.Number( idxCol, idxRow, val );
                    } catch( NumberFormatException nfe ) {
                        wc = new Label( idxCol, idxRow, cellCntnt );
                    }
                } else {
                    wc = new Blank( idxCol, idxRow );
                }
                ws.addCell( wc );
            }
        }

    }


    private static Set<FeatureType> getSelectedFeatureTypes( String property ) {

        Set<FeatureType> featureTypes = new HashSet<>();
        for( String number : property.split( "," ) ) {
            int type = Integer.parseInt( number );
            FeatureType featureType = FeatureType.getFeatureType( type );
            featureTypes.add( featureType );
        }
        return featureTypes;

    }


    private static ParametersReadClasses getParametersReadClasses( String property, byte minMappingQuality, Strand strandUsage ) {

        List<MappingClass> mappingClasses = new ArrayList<>();
        for( String number : property.split( "," ) ) {
            byte type = Byte.parseByte( number );
            MappingClass mc = MappingClass.getFeatureType( type );
            mappingClasses.add( mc );
        }

        // TODO implement UNIQUE option
        List<Classification> excludedFeatureTypes = new ArrayList<>();
        if( !mappingClasses.contains( MappingClass.PERFECT_MATCH ) ) {
            excludedFeatureTypes.add( MappingClass.PERFECT_MATCH );
        }
        if( !mappingClasses.contains( MappingClass.BEST_MATCH ) ) {
            excludedFeatureTypes.add( MappingClass.BEST_MATCH );
        }
        if( !mappingClasses.contains( MappingClass.COMMON_MATCH ) ) {
            excludedFeatureTypes.add( MappingClass.COMMON_MATCH );
        }
        if( !mappingClasses.contains( MappingClass.SINGLE_PERFECT_MATCH ) ) {
            excludedFeatureTypes.add( MappingClass.SINGLE_PERFECT_MATCH );
        }
        if( !mappingClasses.contains( MappingClass.SINGLE_BEST_MATCH ) ) {
            excludedFeatureTypes.add( MappingClass.SINGLE_BEST_MATCH );
        }

        return new ParametersReadClasses( excludedFeatureTypes, minMappingQuality, strandUsage );

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
