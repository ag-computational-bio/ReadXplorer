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

package de.cebitec.readxplorer.ui.importer;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import de.cebitec.readxplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readxplorer.parser.mappings.SamSeqDictionary;
import de.cebitec.readxplorer.parser.output.SamBamCombiner;
import de.cebitec.readxplorer.parser.reference.ReferenceParserI;
import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import de.cebitec.readxplorer.parser.reference.filter.FilterRuleSource;
import de.cebitec.readxplorer.readpairclassifier.SamBamReadPairClassifier;
import de.cebitec.readxplorer.readpairclassifier.SamBamReadPairStatsParser;
import de.cebitec.readxplorer.ui.importer.seqidentifier.SeqIdChecker;
import de.cebitec.readxplorer.ui.importer.seqidentifier.SeqIdCorrectionContainer;
import de.cebitec.readxplorer.ui.importer.seqidentifier.SeqIdManualCorrector;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.StatsContainer;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * THE thread in ReadXplorer for handling the import of data.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class ImportThread extends SwingWorker<Object, Object> implements Observer {

    private static final Logger LOG = LoggerFactory.getLogger( ImportThread.class.getName() );

    private final InputOutput io;
    private final List<ReferenceJob> referenceJobs;
    private final List<TrackJob> tracksJobs;
    private final List<ReadPairJobContainer> readPairJobs;
    private final ProgressHandle ph;
    private int workunits;
    private boolean noErrors = true;
    private Map<String, Integer> chromLengthMap;


    /**
     * THE thread in ReadXplorer for handling the import of data.
     * <p>
     * @param refJobs      reference jobs to import
     * @param trackJobs    track jobs to import
     * @param readPairJobs read pair jobs to import
     */
    public ImportThread( List<ReferenceJob> refJobs, List<TrackJob> trackJobs, List<ReadPairJobContainer> readPairJobs ) {
        super();
        this.io = IOProvider.getDefault().getIO( getBundleString( "ImportThread.output.name" ), false );
        this.tracksJobs = trackJobs;
        this.referenceJobs = refJobs;
        this.readPairJobs = readPairJobs;
        this.ph = ProgressHandleFactory.createHandle( getBundleString( "MSG_ImportThread.progress.name" ) );

        this.workunits = refJobs.size();
        for( TrackJob trackJob : trackJobs ) {
            workunits += trackJob.isAlreadyImported() ? 1 : 2; //one or two steps are needed
        }
        for( ReadPairJobContainer readPairJob : readPairJobs ) {
            workunits += readPairJob.getTrackJob2() != null ? 3 : 2; //two or three steps are needed
        }
    }


    private ParsedReference parseRefJob( ReferenceJob refGenJob ) throws ParsingException, OutOfMemoryError {
        LOG.info( "Start parsing reference genome from file: " + refGenJob.getFile().getAbsolutePath() );

        ReferenceParserI parser = refGenJob.getParser();
        parser.registerObserver( this );
        FeatureFilter filter = new FeatureFilter();
        filter.addBlacklistRule( new FilterRuleSource() );
        ParsedReference refGenome = parser.parseReference( refGenJob, filter );

        LOG.info( "Finished parsing reference genome from file: " + refGenJob.getFile().getAbsolutePath() );
        return refGenome;
    }


    /**
     * Stores a reference sequence in the DB.
     * <p>
     * @param refGenome the reference sequence to store
     * @param refGenJob the corresponding reference job, whose id will be
     *                  updated
     * <p>
     * @throws DatabaseException
     */
    private void storeRefGenome( ParsedReference refGenome, ReferenceJob refGenJob ) throws DatabaseException {
        LOG.info( "Start storing reference genome from file: " + refGenJob.getFile().getAbsolutePath() );

        int refGenID = ProjectConnector.getInstance().addRefGenome( refGenome );
        refGenJob.setPersistent( refGenID );
        refGenJob.setFile( refGenome.getFastaFile() );

        LOG.info( "Finished storing reference genome from file: " + refGenJob.getFile().getAbsolutePath() );
    }


    /**
     * Processes all reference genome jobs of this import process.
     */
    private void processRefGenomeJobs() {
        if( !referenceJobs.isEmpty() ) {
            printAndLog( getBundleString( "MSG_ImportThread.import.start.ref" ) + ":" );

            for( Iterator<ReferenceJob> it = referenceJobs.iterator(); it.hasNext(); ) {
                long start = System.currentTimeMillis();
                ReferenceJob r = it.next();

                try {
                    // parsing
                    ParsedReference refGen = this.parseRefJob( r );
                    printAndLog( "\"" + r.getName() + "\" " + getBundleString( "MSG_ImportThread.import.parsed" ) );

                    // storing
                    try {
                        storeRefGenome( refGen, r );
                        long finish = System.currentTimeMillis();
                        String msg = "\"" + r.getName() + "\" " + getBundleString( "MSG_ImportThread.import.stored" );
                        printAndLog( Benchmark.calculateDuration( start, finish, msg ) );
                    } catch( DatabaseException ex ) {
                        // if something went wrong, mark all dependent track jobs
                        printAndLogError( "\"" + r.getName() + "\" " + getBundleString( "MSG_ImportThread.import.failed" ) + ": " + ex.getMessage() );
                        this.noErrors = false;
                        LOG.error( ex.getMessage(), ex );
                    }

                } catch( ParsingException ex ) {
                    // if something went wrong, mark all dependent track jobs
                    printAndLogError( "\"" + r.getName() + "\" " + getBundleString( "MSG_ImportThread.import.failed" ) + ": " + ex.getMessage() );
                    this.noErrors = false;
                    LOG.error( ex.getMessage(), ex );
                } catch( OutOfMemoryError ex ) {
                    printAndLogError( "\"" + r.getName() + "\" " + getBundleString( "MSG_ImportThread.import.outOfMemory" ) + "!" );
                    LOG.error( ex.getMessage(), ex );
                }

                ph.progress( ++workunits );
            }

            printAndLog( "" );
        }
    }


    /**
     * Reads all chromosome sequences of the reference genome and puts them into
     * the chromSeqMap map and their length in the chromLengthMap map.
     * <p>
     * @param trackJob The track job for which the chromosome sequences and
     *                 lengths are needed.
     *
     * @throws DatabaseException An exception during data queries
     */
    private void setChromLengthMap( TrackJob trackJob ) throws DatabaseException {
        chromLengthMap = new HashMap<>();
        int id = trackJob.getRefGen().getID();
        Map<Integer, PersistentChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector( id ).getRefGenome().getChromosomes();
        for( PersistentChromosome chrom : chromIdMap.values() ) {
            chromLengthMap.put( chrom.getName(), chrom.getLength() );
        }
    }


    /**
     * Checks if the reference sequence ids can be found in the mapping file of
     * the TrackJob. Auto correction is performed and the result presented to
     * the user. If no auto-correction is possible, both dictionaries are shown
     * to the user and can be edited.
     *
     * @param trackJob The track job to check
     */
    private SeqIdCorrectionContainer checkRefIds( TrackJob trackJob ) throws ParsingException {
        SeqIdChecker seqIdChecker = new SeqIdChecker( new SeqIdCorrectionContainer( trackJob, String.valueOf( trackJob.getID() ) ) );
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackJob.getRefGen().getID() );
        try {
            seqIdChecker.checkSeqIds( trackJob.getFile(), refConnector.getRefGenome() );
            trackJob.setSequenceDictionary( new SamSeqDictionary( seqIdChecker.getCorrectionDataContainer().getSequenceDictionary() ) );

        } catch( DatabaseException ex ) {
            LOG.error( ex.getMessage(), ex );
        }
        return seqIdChecker.getCorrectionDataContainer();
    }


    /**
     * First tries to perform auto correction and then in the GUI version also
     * manual correction. The manual correction is invoced already if one
     * mapping file id is missing in the reference. The console version is
     * forgiving and only aborts if none of the mapping file ids can be
     * automatically associated to any reference sequence ids.
     */
    private void correctRefIds( List<SeqIdCorrectionContainer> seqIdCorrectionList ) {

        String guienvProp = System.getProperty( "guienv" );
        List<SeqIdCorrectionContainer> identicalDictionaries = new ArrayList<>();
        boolean manualCorrectionNeeded = false;
        for( SeqIdCorrectionContainer correctionContainer : seqIdCorrectionList ) {
            if( !correctionContainer.isSeqIdsValid() ) {
                if( guienvProp == null || guienvProp.equals( "true" ) ) {
                    //We always trigger manual editing if at least one mapping file id is missing in the reference
                    mergeIdenticalDictionaries( identicalDictionaries, correctionContainer );
                    manualCorrectionNeeded = true;

                } else if( correctionContainer.getFoundIds() == 0 ) {
                    noErrors = false;
                    ParsingException ex = new ParsingException( "No reference sequence id matches any sequence ids in the mapping file. If you don't use identical names, the import does not succeed." );
                    ErrorHelper.getHandler().handle( ex, "Reference ID error" );
                    printAndLogError( ex );
                }
            }
        }

        if( manualCorrectionNeeded ) {
            SeqIdManualCorrector manualCorrector = new SeqIdManualCorrector();
            try {
                manualCorrector.triggerManualEditing( identicalDictionaries );
            } catch( ParsingException ex ) {
                noErrors = false;
                ErrorHelper.getHandler().handle( ex, "Reference ID error" );
                printAndLogError( ex );
            }
        }
    }


    /**
     * One container in the identicalDictionaries list represents multiple
     * identical dictionaries.
     *
     * @param identicalDictionaries List of sequence id correction containers,
     *                              where each container holds all track jobs
     *                              with the same SAMSequenceDictionary.
     * @param correctionContainer   A single sequence id correction container
     *                              which shall either be merged in one of the
     *                              dictionaries in the identicalDictionaries
     *                              list or simply added to the list when the
     *                              sequence dictionary differs to all other
     *                              ones already in the list
     */
    private void mergeIdenticalDictionaries( List<SeqIdCorrectionContainer> identicalDictionaries, SeqIdCorrectionContainer correctionContainer ) {
        if( identicalDictionaries.isEmpty() ) {
            identicalDictionaries.add( correctionContainer );
        } else {
            //compare with containers in list
            boolean isAdded = false;
            for( SeqIdCorrectionContainer container : identicalDictionaries ) {
                try {
                    container.getSequenceDictionary().assertSameDictionary( correctionContainer.getSequenceDictionary() );
                    container.addTrackJob( correctionContainer.getTrackJob() );
                    isAdded = true;
                    break;
                } catch( AssertionError e ) { //they are not equal, we need to add it to the identicalDictionaries list
                    isAdded = false;
                }
            }
            if( !isAdded ) {
                identicalDictionaries.add( correctionContainer );
            }
        }
    }


    /**
     * Processes track jobs (parsing and storing) of the current import.
     */
    private void processTrackJobs() {
        if( !tracksJobs.isEmpty() ) {
            printAndLog( getBundleString( "MSG_ImportThread.import.start.track" ) + ":" );

            List<SeqIdCorrectionContainer> corretionList = new ArrayList<>();
            for( TrackJob trackJob : tracksJobs ) {
                try {
                    corretionList.add( checkRefIds( trackJob ) );
                } catch( ParsingException ex ) {
                    noErrors = false;
                    ErrorHelper.getHandler().handle( ex, "Reference ID error" );
                    printAndLogError( ex );
                }
            }

            correctRefIds( corretionList ); //After check they can be corrected

            for( TrackJob trackJob : tracksJobs ) {

                if( trackJob.isCanBeImported() ) {
                    parseBamTrack( trackJob );
                    ph.progress( ++workunits );
                } else {
                    printAndLog( trackJob.getName() + " cannot be imported, because the wizard to correctly associate the sequence ids has not been finished correctly." );
                }
            }
        }
    }


    /**
     * Processes read pair jobs (parsing and storing) of the current import.
     */
    private void processReadPairJobs() {
        if( !readPairJobs.isEmpty() ) {

            printAndLog( getBundleString( "MSG_ImportThread.import.start.readPairs" ) + ":" );

            List<SeqIdCorrectionContainer> correctionList = new ArrayList<>();
            for( ReadPairJobContainer readPairJobContainer : readPairJobs ) {
                if( readPairJobContainer.getDistance() > 0 ) {
                    try {
                        correctionList.add( checkRefIds( readPairJobContainer.getTrackJob1() ) );
                        TrackJob trackJob2 = readPairJobContainer.getTrackJob2(); //may be NULL!!!
                        if( trackJob2 != null ) {
                            correctionList.add( checkRefIds( trackJob2 ) );
                        }
                    } catch( ParsingException ex ) {
                        ErrorHelper.getHandler().handle( ex, "Reference ID error" );
                        printAndLogError( ex );
                        noErrors = false;
                    }
                }
            }

            correctRefIds( correctionList ); //After check they can be corrected

            for( ReadPairJobContainer readPairJobContainer : readPairJobs ) {
                parseReadPairJob( readPairJobContainer );
            }
        }
    }


    /**
     * Performs the actual parsing of a read pair import.
     *
     * @param readPairJobContainer The container with both track jobs
     */
    private void parseReadPairJob( ReadPairJobContainer readPairJobContainer ) {

        int distance = readPairJobContainer.getDistance();
        if( distance > 0 ) {

            int trackId1;
            int trackId2 = -1;

            /*
             * Algorithm: start file if (PersistentTrack not yet imported) {
             * convert file 1 to sam/bam, if necessary if (isTwoTracks) {
             * convert file 2 to sam/bam, if necessary combine them unsorted
             * (NEW FILE) } sort by readseq (NEW FILE) - if isTwoTracks:
             * deleteOldFile parse mappings sort by read name (NEW FILE) -
             * deleteOldFile read pair classification, extension & sorting by
             * coordinate - deleteOldFile } create position table (advantage: is
             * already sorted by coordinate & classification in file)
             */

            TrackJob trackJob1 = readPairJobContainer.getTrackJob1();
            TrackJob trackJob2 = readPairJobContainer.getTrackJob2();
            if( trackJob1.isCanBeImported() && trackJob2.isCanBeImported() ) {
                try {
                    this.setChromLengthMap( trackJob1 );
                    File inputFile1 = trackJob1.getFile();
                    inputFile1.setReadOnly(); //prevents changes or deletion of original file!
                    StatsContainer statsContainer = new StatsContainer();
                    statsContainer.prepareForTrack();
                    statsContainer.prepareForReadPairTrack();

                    if( !trackJob1.isAlreadyImported() ) {

                        //executes any conversion before other calculations, if the parser supports any
                        trackJob1.getParser().registerObserver( this );
                        Boolean success = trackJob1.getParser().convert( trackJob1, chromLengthMap );
                        trackJob1.getParser().removeObserver( this );
                        if( !success ) {
                            noErrors = false;
                            printAndLogError( "Conversion of " + trackJob1.getName() + " failed!" );
                            workunits += trackJob2 != null ? 3 : 2;
                            ph.progress( workunits );
                            return;
                        }
                        File lastWorkFile = trackJob1.getFile(); //file which was created in the last step of the import process

                        if( trackJob2 != null ) { //only combine, if data is not already combined
                            File inputFile2 = trackJob2.getFile();
                            inputFile2.setReadOnly();
                            trackJob2.getParser().registerObserver( this );
                            success = trackJob2.getParser().convert( trackJob2, chromLengthMap );
                            trackJob2.getParser().removeObserver( this );
                            File lastWorkFile2 = trackJob2.getFile();
                            if( !success ) {
                                noErrors = false;
                                printAndLogError( "Conversion of " + trackJob2.getName() + " failed!" );
                                workunits += 3;
                                ph.progress( workunits );
                                return;
                            }

                            //combine both tracks and continue with trackJob1, they are unsorted now
                            SamBamCombiner combiner = new SamBamCombiner( trackJob1, trackJob2, false );
                            combiner.registerObserver( this );
                            success = combiner.combineData();
                            ph.progress( ++workunits );

                            if( !success ) {
                                noErrors = false;
                                printAndLogError( "Combination of " + trackJob1.getName() + " and " + trackJob2.getName() + " failed!" );
                                return;
                            }
                            GeneralUtils.deleteOldWorkFile( lastWorkFile ); //either were converted or are write protected
                            GeneralUtils.deleteOldWorkFile( lastWorkFile2 );
                            lastWorkFile = trackJob1.getFile(); //the combined file
                            inputFile2.setWritable( true );

                        }

                        //extension for both classification and read pair info
                        SamBamReadPairClassifier samBamDirectReadPairClassifier = new SamBamReadPairClassifier(
                                readPairJobContainer, chromLengthMap );
                        samBamDirectReadPairClassifier.registerObserver( this );
                        samBamDirectReadPairClassifier.setStatsContainer( statsContainer );
                        samBamDirectReadPairClassifier.classifyReadPairs();

                        //delete the combined file, if it was combined, otherwise the orig. file cannot be deleted
                        GeneralUtils.deleteOldWorkFile( lastWorkFile );
                        ph.progress( ++workunits );

                    } else { //else case with 2 already imported tracks is prohibited
                        //we have to calculate the stats
                        SamBamReadPairStatsParser statsParser = new SamBamReadPairStatsParser( readPairJobContainer, chromLengthMap, null );
                        statsParser.setStatsContainer( statsContainer );
                        statsParser.registerObserver( this );
                        statsParser.classifyReadPairs();

                        ph.progress( ++workunits );
                    }

                    //create general track stats
                    SamBamStatsParser statsParser = new SamBamStatsParser();
                    statsParser.setStatsContainer( statsContainer );
                    statsParser.registerObserver( this );
                    ParsedTrack track = statsParser.createTrackStats( trackJob1, chromLengthMap );
                    statsParser.removeObserver( this );

                    this.storeBamTrack( track ); // store track entry in db
                    trackId1 = trackJob1.getID();
                    inputFile1.setWritable( true );

                    //read pair ids have to be set in track entry
                    ProjectConnector.getInstance().setReadPairIdsForTrackIds( trackId1, trackId2 );

                } catch( OutOfMemoryError ex ) {
                    printAndLogError( "Out of memory error during parsing of bam track: " + ex.getMessage() );
                    LOG.error( ex.getMessage(), ex );
                    noErrors = false;

                } catch( DatabaseException | ParsingException | IOException ex ) {
                    printAndLogError( "Error during parsing of bam track: " + ex.getMessage() );
                    LOG.error( ex.getMessage(), ex );
                    noErrors = false;
                }

            } else {
                printAndLog( trackJob1.getName() + " and " + trackJob2.getName() + " cannot be imported, because the wizard to " +
                             "correctly associate the sequence ids has not been finished correctly." );
            }
        } else { //if (distance <= 0)
            printAndLogError( getBundleString( "MSG_ImportThread.import.error" ) );
            noErrors = false;
        }

        ph.progress( ++workunits );
    }


    /**
     * Parses a bam track and calls the method for storing the track relevant
     * data in the db.
     * <p>
     * @param trackJob the trackjob to import as bam track
     */
    private void parseBamTrack( TrackJob trackJob ) {

        /*
         * Algorithm: if (PersistentTrack not yet imported) { convert to
         * sam/bam, if necessary (NEW FILE) parse mappings extend bam file (NEW
         * FILE) - deleteOldFile } create statistics (advantage: is already
         * sorted by coordinate & classification in file)
         */

        try {
            setChromLengthMap( trackJob );
            boolean success;
            StatsContainer statsContainer = new StatsContainer();
            statsContainer.prepareForTrack();

            //only extend, if data is not already stored in it
            if( !trackJob.isAlreadyImported() ) {
                File inputFile = trackJob.getFile();
                MappingParserI mappingParser = trackJob.getParser();
                inputFile.setReadOnly(); //prevents changes or deletion of original file!
                //executes any conversion before other calculations, if the parser supports any
                success = trackJob.getParser().convert( trackJob, chromLengthMap );
                File lastWorkFile = trackJob.getFile();

                //generate classification data in file sorted by read sequence
                mappingParser.registerObserver( this );
                mappingParser.setStatsContainer( statsContainer );
                mappingParser.parseInput( trackJob, chromLengthMap );
                mappingParser.removeObserver( this );
                noErrors = noErrors ? success : noErrors;
                if( success ) {
                    GeneralUtils.deleteOldWorkFile( lastWorkFile );
                } //only when we reach this line without exceptions and conversion was successful
                ph.progress( ++workunits );
                inputFile.setWritable( true );
                mappingParser.removeObserver( this );
            }

            //file needs to be sorted by coordinate for efficient calculation
            SamBamStatsParser statsParser = new SamBamStatsParser();
            statsParser.setStatsContainer( statsContainer );
            statsParser.registerObserver( this );
            ParsedTrack track = statsParser.createTrackStats( trackJob, chromLengthMap );
            statsParser.removeObserver( this );

            this.storeBamTrack( track );

        } catch( OutOfMemoryError ex ) {
            printAndLogError( "Out of memory error during parsing of bam track: " + ex.getMessage() );
            this.noErrors = false;
        } catch( DatabaseException | ParsingException | IOException ex ) {
            printAndLogError( "Error during parsing of bam track: " + ex.getMessage() );
            LOG.error( ex.getMessage(), ex );
            this.noErrors = false;
        }
    }


    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add( this );
        try {
            io.getOut().reset();
        } catch( IOException ex ) {
            LOG.error( ex.getMessage(), ex );
        }
        io.select();

        ph.start( workunits );
        workunits = 0;

        ph.progress( getBundleString( "MSG_ImportThread.progress.ref" ) + "...", workunits );
        this.processRefGenomeJobs();

        // track jobs have to be imported last, because they may depend upon previously imported genomes, runs
        ph.progress( getBundleString( "MSG_ImportThread.progress.track" ) + "...", workunits );

        //get system JVM info:
        Runtime rt = Runtime.getRuntime();

        printAndLog( "Your current JVM config allows up to " + GeneralUtils.formatNumber( rt.maxMemory() / 1000000 ) + " MB of memory to be allocated." );
        printAndLog( "Currently the platform is using " + GeneralUtils.formatNumber( (rt.totalMemory() - rt.freeMemory()) / 1000000 ) + " MB of memory." );
        printAndLog( "Please be aware that you might need to change the -J-d64 and -J-Xmx value of your JVM to process large imports successfully." );
        printAndLog( "The value can be configured in the ../readxplorer/etc/readxplorer.conf file in the application folder." );
        printAndLog( "" );

        processTrackJobs();
        processReadPairJobs();

        return null;
    }


    @Override
    protected void done() {
        super.done();
        ph.progress( workunits );
        if( this.noErrors ) {
            printAndLog( getBundleString( "MSG_ImportThread.import.finished" ) );
        } else {
            printAndLog( getBundleString( "MSG_ImportThread.import.partFailed" ) );
        }
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove( this );
    }


    @Override
    public void update( Object data ) {
        if( data.toString().contains( "processed" ) || data.toString().contains( "converted" ) || data.toString().contains( "indexed" ) ) {
            this.ph.progress( data.toString() );
        } else {
            printAndLog( data.toString() );
            this.ph.progress( "" );
        }
    }


    /**
     * Stores a bam track in the database and gives appropriate status messages.
     * <p>
     * @param trackJob the information about the track to store
     */
    private void storeBamTrack( ParsedTrack track ) {
        try {
            printAndLog( track.getTrackName() + ": " + getBundleString( "MSG_ImportThread.import.start.trackdirect" ) );
            ProjectConnector.getInstance().storeBamTrack( track );
            ProjectConnector.getInstance().storeTrackStatistics( track.getStatsContainer(), track.getID() );
            printAndLog( getBundleString( "MSG_ImportThread.import.success.trackdirect" ) );

        } catch( OutOfMemoryError e ) {
            printAndLogError( getBundleString( "MSG_ImportThread.import.outOfMemory" ) + "!" );
            LOG.error( e.getMessage(), e );
        } catch( DatabaseException e ) {
            printAndLogError( getBundleString( "Database exception occurred" ) + "!" );
            LOG.error( e.getMessage(), e );
        }
    }


    /**
     * @param name the name of the bundle string to return (found in
     *             Bundle.properties)
     * <p>
     * @return the string associated in the Bundle.properties with the given
     *         name.
     */
    private String getBundleString( String name ) {
        return NbBundle.getMessage( ImportThread.class, name );
    }


    /**
     * Prints the given message to the io stream and the logger at info level.
     *
     * @param msg The message to print
     */
    private void printAndLog( String msg ) {
        io.getOut().println( msg );
        LOG.info( msg );
    }


    /**
     * Prints the given message to the io stream and the logger at error level.
     *
     * @param msg The message to print
     */
    private void printAndLogError( String msg ) {
        io.getOut().println( msg );
        LOG.error( msg );
    }


    /**
     * Prints the given message to the io stream and the logger at error level.
     *
     * @param msg The message to print
     */
    private void printAndLogError( Exception e ) {
        io.getOut().println( e.getMessage() );
        LOG.error( e.getMessage(), e );
    }


}
