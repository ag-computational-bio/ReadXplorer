package de.cebitec.readXplorer.ui.importer;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.StorageException;
import de.cebitec.readXplorer.databackend.dataObjects.ChromosomeObserver;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.DirectAccessDataContainer;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsedTrack;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.mappings.MappingParserI;
import de.cebitec.readXplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readXplorer.parser.output.SamBamCombiner;
import de.cebitec.readXplorer.parser.output.SamBamExtender;
import de.cebitec.readXplorer.parser.reference.CommonsRefParser;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.parser.reference.Filter.FilterRuleSource;
import de.cebitec.readXplorer.parser.reference.ReferenceParserI;
import de.cebitec.readXplorer.readPairClassifier.SamBamDirectReadPairClassifier;
import de.cebitec.readXplorer.readPairClassifier.SamBamDirectReadPairStatsParser;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Thread handling the import of data.
 * 
 * @author ddoppmeier, rhilker
 */
public class ImportThread extends SwingWorker<Object, Object> implements Observer {

    private InputOutput io;
    private List<ReferenceJob> referenceJobs;
    private List<TrackJob> tracksJobs;
    private List<ReadPairJobContainer> readPairJobs;
//    private List<TrackJob> posTableJobs;
    private ProgressHandle ph;
    private int workunits;
//    private CoverageContainer covContainer;
    private boolean noErrors = true;
    private Map<String, Integer> chromLengthMap;
    private Map<String, String> chromSeqMap;

    /**
     * THE thread in ReadXplorer for handling the import of data.
     * @param refJobs reference jobs to import
     * @param trackJobs track jobs to import
     * @param readPairJobs read pair jobs to import
     */
    public ImportThread(List<ReferenceJob> refJobs, List<TrackJob> trackJobs, List<ReadPairJobContainer> readPairJobs) {
        super();
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ImportThread.class, "ImportThread.output.name"), false);
        this.tracksJobs = trackJobs;
        this.referenceJobs = refJobs;
        this.readPairJobs = readPairJobs;
//        this.posTableJobs = posTableJobs;
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.name"));
        this.workunits = refJobs.size() + 2 * trackJobs.size() + 3 * readPairJobs.size();
    }

    private ParsedReference parseRefJob(ReferenceJob refGenJob) throws ParsingException, OutOfMemoryError {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        ReferenceParserI parser = refGenJob.getParser();
        parser.registerObserver(this);
        FeatureFilter filter = new FeatureFilter();
        filter.addBlacklistRule(new FilterRuleSource());
        ParsedReference refGenome = parser.parseReference(refGenJob, filter);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
        return refGenome;
    }

    
//    /**
//     * Parses tracks to store completely in the DB.
//     * @param trackJob trackjob to parse
//     * @return the parsed track to store in the DB
//     * @throws ParsingException
//     * @throws OutOfMemoryError 
//     */
//    private ParsedTrack parseTrack(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
//        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing track data from source \"{0} with track job ID {1}\"", new Object[]{trackJob.getFile().getAbsolutePath(), trackJob.getID()});
//        
//        String sequenceString = this.getReferenceSeq(trackJob);
//        TrackParser parser = new TrackParser();
//        ParsedTrack track = parser.parseMappings(trackJob, sequenceString, this, covContainer);
//
//        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());
//        return track;
//    }

    /**
     * Stores a reference sequence in the DB.
     * @param refGenome the reference sequence to store
     * @param refGenJob the corresponding reference job, whose id will be updated
     * @throws StorageException 
     */
    private void storeRefGenome(ParsedReference refGenome, ReferenceJob refGenJob) throws StorageException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        int refGenID = ProjectConnector.getInstance().addRefGenome(refGenome);
        refGenJob.setPersistant(refGenID);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
    }

    /**
     * Processes all reference genome jobs of this import process.
     */
    private void processRefGenomeJobs() {
        if (!referenceJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.ref") + ":");
            long start;
            long finish;
            String msg;
            
            for (Iterator<ReferenceJob> it = referenceJobs.iterator(); it.hasNext();) {
                start = System.currentTimeMillis();
                ReferenceJob r = it.next();
                ph.progress(workunits++);

                try {
                    // parsing
                    ParsedReference refGen = this.parseRefJob(r);
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));

                    // storing
                    try {
                        storeRefGenome(refGen, r);
                        finish = System.currentTimeMillis();
                        msg = "\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
                        io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                        this.noErrors = false;
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                    this.noErrors = false;
                    Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, null, ex);
                } catch (OutOfMemoryError ex) {
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.outOfMemory") + "!");
                }

                it.remove();
            }

            io.getOut().println("");
        }
    }

    /**
     * Reads all chromosome sequences of the reference genome and puts them
     * into the chromSeqMap map and their length in the chromLengthMap map. 
     * @param trackJob The track job for which the chromosome sequences and
     * lengths are needed.
     */
    private void setChromMaps(TrackJob trackJob) {
        chromSeqMap = new HashMap<>();
        chromLengthMap = new HashMap<>();
        int id = trackJob.getRefGen().getID();
        Map<Integer, PersistantChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefGenome().getChromosomes();
        ChromosomeObserver chromObserver = new ChromosomeObserver();
        for (PersistantChromosome chrom : chromIdMap.values()) {
            String seq = chrom.getSequence(chromObserver);
            chromLengthMap.put(chrom.getName(), seq.length());
            chromSeqMap.put(chrom.getName(), seq);
            chrom.removeObserver(chromObserver); //tells the chromosome, that the sequence is not needed anymore
        }
    }
    
    /**
     * Processes track jobs (parsing and storing) of the current import.
     */
    private void processTrackJobs() {
        if (!tracksJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.track") + ":");
            
            for (Iterator<TrackJob> it = tracksJobs.iterator(); it.hasNext();) {
                TrackJob trackJob = it.next();
                ph.progress(workunits++);
                                
                    this.parseDirectAccessTrack(trackJob);

                it.remove();
            }
        }
    }

//    /**
//     * Processes all position table jobs for the current import.
//     */
//    private void processPosTableJobs() {
//        if (!this.posTableJobs.isEmpty()) {
//            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.posTable") + ":");
//
//            for (Iterator<TrackJob> it = this.posTableJobs.iterator(); it.hasNext();) {
//                TrackJob t = it.next();
//                ph.progress(workunits++);
//
//                //parsing position table
//
//                if (t.getParser() instanceof SamBamStepParser) {
//                    this.parseStepwiseTrack(t, true);
//                } else {
//                    this.parseSingleTrack(t, true);
//                }
//
//                it.remove();
//            }
//        }
//    }
    
    private void processReadPairJobs() {
        if (!readPairJobs.isEmpty()) {

            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.readPairs") + ":");

            long start;
            long finish;
            String msg;

            for (Iterator<ReadPairJobContainer> it = readPairJobs.iterator(); it.hasNext();) {
                start = System.currentTimeMillis();
                ReadPairJobContainer readPairJobContainer = it.next();
                ph.progress(workunits++);

                int distance = readPairJobContainer.getDistance();
                if (distance > 0) {

                    int trackId1;
                    int trackId2 = -1;

//                    ///////////////////////////////////////////////////////////////////////
//                    //////////// Treatment of db tracks! //////////////////////////////////
//                    ///////////////////////////////////////////////////////////////////////
//                    if (readPairJobContainer.getTrackJob1().isDbUsed()) {
//                        //parsing tracks
//                        ParsedTrack track1 = this.parseSingleTrack(readPairJobContainer.getTrackJob1(), false);
//                        ParsedTrack track2 = this.parseSingleTrack(readPairJobContainer.getTrackJob2(), false);
//                        trackId1 = track1.getID();
//                        trackId2 = track2.getID();
//                        //TODO: handle import of a single file, too!
//                        SeqPairClassifier readPairClassifier = new SeqPairClassifier();
//                        readPairClassifier.setData(track1, track2, distance, readPairJobContainer.getDeviation(), readPairJobContainer.getOrientation());
//                        String description = readPairJobContainer.getTrackJob1().getFile().getName() + " and " + readPairJobContainer.getTrackJob2().getFile().getName();
//
//                        try { //storing readuence pairs data
//                            this.storeSeqPairs(readPairClassifier.classifySeqPairs(), description);
//                            finish = System.currentTimeMillis();
//                            msg = "\"" + description + " readuence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
//                            io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
//                        } catch (StorageException ex) {
//                            Exceptions.printStackTrace(ex);
//                            io.getOut().println("\"" + description + " readuence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
//                            Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
//                            this.noErrors = false;
//                        }
//
//                        track1.clear();
//                        track2.clear();
//                        System.gc();
//
//                        ///////////////////////////////////////////////////////////////////////
//                        //////////// Special treatment of direct access tracks here: //////////
//                        ///////////////////////////////////////////////////////////////////////
//                    } else {

                        /*
                         * Algorithm:
                         * start file
                         * if (track not yet imported) {
                         *      convert file 1 to sam/bam, if necessary
                         *      if (isTwoTracks) { 
                         *          convert file 2 to sam/bam, if necessary
                         *          combine them unsorted (NEW FILE) 
                         *      }
                         *      sort by readseq (NEW FILE) - if isTwoTracks: deleteOldFile
                         *      parse mappings 
                         *      sort by read name (NEW FILE) - deleteOldFile
                         *      read pair classification, extension & sorting by coordinate - deleteOldFile
                         * }
                         * create position table (advantage: is already sorted by coordinate & classification in file)
                         */

                        TrackJob trackJob1 = readPairJobContainer.getTrackJob1();
                        TrackJob trackJob2 = readPairJobContainer.getTrackJob2();
                        Map<String, ParsedClassification> classificationMap;
                        this.setChromMaps(trackJob1);
                        File inputFile1 = trackJob1.getFile();
                        inputFile1.setReadOnly(); //prevents changes or deletion of original file!
                        boolean success;
                        StatsContainer statsContainer = new StatsContainer();
                        statsContainer.prepareForTrack();
                        statsContainer.prepareForSeqPairTrack();

                        if (!trackJob1.isAlreadyImported()) {

                            try {
                                //executes any conversion before other calculations, if the parser supports any
                                trackJob1.getParser().registerObserver(this);
                                success = (boolean) trackJob1.getParser().convert(trackJob1, chromLengthMap);
                                trackJob1.getParser().removeObserver(this);
                                if (!success) {
                                    this.noErrors = false;
                                    this.showMsg("Conversion of " + trackJob1.getName() + " failed!");
                                    continue;
                                }
                                File lastWorkFile = trackJob1.getFile(); //file which was created in the last step of the import process
                                
                                boolean isTwoTracks = trackJob2 != null;
                                if (isTwoTracks) { //only combine, if data is not already combined
                                    File inputFile2 = trackJob2.getFile();
                                    inputFile2.setReadOnly();
                                    trackJob2.getParser().registerObserver(this);
                                    success = (boolean) trackJob2.getParser().convert(trackJob2, chromLengthMap);
                                    trackJob2.getParser().removeObserver(this);
                                    File lastWorkFile2 = trackJob2.getFile();
                                    if (!success) {
                                        this.noErrors = false;
                                        this.showMsg("Conversion of " + trackJob2.getName() + " failed!");
                                        continue;
                                    }

                                    //combine both tracks and continue with trackJob1, they are unsorted now
                                    SamBamCombiner combiner = new SamBamCombiner(trackJob1, trackJob2, false);
                                    combiner.registerObserver(this);
                                    success = combiner.combineData();
                                    if (!success) {
                                        this.noErrors = false;
                                        this.showMsg("Combination of " + trackJob1.getName() + " and " + trackJob2.getName() + " failed!");
                                        continue;
                                    }
                                    GeneralUtils.deleteOldWorkFile(lastWorkFile); //either were converted or are write protected
                                    GeneralUtils.deleteOldWorkFile(lastWorkFile2);
                                    lastWorkFile = trackJob1.getFile(); //the combined file
                                    inputFile2.setWritable(true);
                                }

                                //generate classification data in sorted file
                                MappingParserI mappingParser = trackJob1.getParser();
                                mappingParser.registerObserver(this);
                                //parser also deletes combined file or other writable input file
                                mappingParser.setStatsContainer(statsContainer);
                                Object parsingResult = mappingParser.parseInput(trackJob1, chromSeqMap);
                                mappingParser.removeObserver(this);
                                if (lastWorkFile != trackJob1.getFile()) { //either combined or write protected orig file
                                    GeneralUtils.deleteOldWorkFile(lastWorkFile); //only delete, if file was changed during parsing
                                    lastWorkFile = trackJob1.getFile();
                                }
                                ph.progress(workunits++);
                                if (parsingResult instanceof DirectAccessDataContainer) {
                                    DirectAccessDataContainer dataContainer = (DirectAccessDataContainer) parsingResult;
                                    classificationMap = dataContainer.getClassificationMap();
                                } else {
                                    this.showMsg("Parsing of " + trackJob1.getName() + "failed! The parsing result was of an unexpected type: " + parsingResult.getClass());
                                    this.noErrors = false;
                                    continue;
                                }

                                //extension for both classification and read pair info
                                SamBamDirectReadPairClassifier samBamDirectReadPairClassifier = new SamBamDirectReadPairClassifier(
                                        readPairJobContainer, chromSeqMap, classificationMap);
                                samBamDirectReadPairClassifier.registerObserver(this);
                                samBamDirectReadPairClassifier.setStatsContainer(statsContainer);
                                samBamDirectReadPairClassifier.classifySeqPairs();
                                
                                //delete the combined file, if it was combined, otherwise the orig. file cannot be deleted
                                GeneralUtils.deleteOldWorkFile(lastWorkFile);

                            } catch (OutOfMemoryError ex) {
                                this.showMsg("Out of Memory error during parsing of direct access track: " + ex.getMessage());
                                this.noErrors = false;
                                continue;
                            } catch (Exception ex) {
                                this.showMsg("Error during parsing of direct access track: " + ex.getMessage());
                                Exceptions.printStackTrace(ex);
                                this.noErrors = false;
                                continue;
                            }
                        } else { //else case with 2 already imported tracks is prohibited
                            //we have to calculate the stats
                            ph.progress(workunits++);
                            SamBamDirectReadPairStatsParser statsParser = new SamBamDirectReadPairStatsParser(readPairJobContainer, chromSeqMap, null);
                            statsParser.setStatsContainer(statsContainer);
                            try {
                                statsParser.registerObserver(this);
                                statsParser.classifySeqPairs();
                            } catch (OutOfMemoryError ex) {
                                this.showMsg("Out of Memory error during parsing of direct access track: " + ex.getMessage());
                                this.noErrors = false;
                                continue;
                            } catch (Exception ex) {
                                this.showMsg("Error during parsing of direct access track: " + ex.getMessage());
                                Exceptions.printStackTrace(ex);
                                this.noErrors = false;
                                continue;
                            }
                        }

                        ph.progress(workunits++);
                        //create position table
                        SamBamStatsParser statsParser = new SamBamStatsParser();
                        statsParser.setStatsContainer(statsContainer);
                        statsParser.registerObserver(this);
                        ParsedTrack track = statsParser.createTrackStats(trackJob1, chromLengthMap);
                        statsParser.removeObserver(this);

                        this.storeDirectAccessTrack(track, true); // store track entry in db
                        trackId1 = trackJob1.getID();
                        inputFile1.setWritable(true);
//                    }

                    //read pair ids have to be set in track entry
                    ProjectConnector.getInstance().setSeqPairIdsForTrackIds(trackId1, trackId2);

                } else { //if (distance <= 0)
                    this.showMsg(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.error"));
                    this.noErrors = false;
                }

                it.remove();
            }
        }
    }

//    /**
//     * Parses and stores a single trackJob.
//     * @param trackJob job to parse
//     * @return returns the trackJob if everything went fine, otherwise <code>null</code>
//     */
//    private ParsedTrack parseSingleTrack(TrackJob trackJob, boolean onlyPositionTable) {
//
//        // only import this track if no problems occured with dependencies
//        try {
//            
//            long start = System.currentTimeMillis();
//
//            //parsing track
//            ParsedTrack track = this.parseTrack(trackJob);
//            //needed for onlyPositionTable case
//            boolean readPairs = false;
//            if (trackJob.getParser() instanceof SeqPairProcessorI) {
//                track.setReadnameToSeqIdMap1(((SeqPairProcessorI) trackJob.getParser()).getReadNameToSeqIDMap1());
//                ((SeqPairProcessorI) trackJob.getParser()).resetSeqIdToReadnameMaps();
//                readPairs = true;
//            }
//
//            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));
//
//
//            //storing track
//            try {
//                this.storeTrack(track, trackJob, readPairs, onlyPositionTable);
//                long finish = System.currentTimeMillis();
//                String msg = "\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
//                io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
//            } catch (StorageException ex) {
//                // something went wrong
//                io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
//                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
//                this.noErrors = false;
//            }
//            return track;
//
//
//        } catch (ParsingException | OutOfMemoryError ex) {
//            // something went wrong
//            io.getOut().println(ex.getMessage());
//            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
//            this.noErrors = false;
//        }
//        return null;
//    }

//    /**
//     * Parses a track in chunks and stores all it's data in the db afterwards.
//     * @param trackJob the trackjob to import
//     * @param onlyPositionTable true, if only the position table should be 
//     *      imported for the track
//     */
//    private void parseStepwiseTrack(TrackJob trackJob, boolean onlyPositionTable) {
//        String filename = trackJob.getFile().getName();
//        boolean isLastTrack = false;
//        int start = 1;
//        int stepsize = trackJob.getStepSize();
//        int stop = stepsize;
//        
//        long startTime;
//        long finish;
//        String msg;
//        
//        while (!isLastTrack) {
//            
//            startTime = System.currentTimeMillis();
//            
//            //parsing
//            trackJob.setStart(start);
//            trackJob.setStop(stop);
//            trackJob.setIsFirstJob(start == 1);
//            ParsedTrack track = null;
//            try {
//                track = this.parseTrack(trackJob);
//
//                covContainer = track.getCoverageContainer();
//            } catch (ParsingException ex) {
//                // something went wrong
//                io.getOut().println(ex.getMessage());
//                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
//                this.noErrors = false;
//            }
//            track.setIsFirstTrack(start == 1 ? true : false);
//            isLastTrack = track.getParsedMappingContainer().isLastMappingContainer();
//            io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsedReads", start, stop));
//
//            //storing
//            try {
//                this.storeTrack(track, trackJob, false, onlyPositionTable);
//                finish = System.currentTimeMillis();
//                msg = "\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.storedReads", start, stop);
//                io.getOut().println(Benchmark.calculateDuration(startTime, finish, msg));
//            } catch (StorageException ex) {
//                // something went wrong
//                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
//                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
//                this.noErrors = false;
//            }
//
//            stop += stepsize;
//            start += stepsize;
//        }
//
//    }
    
    /**
     * Parses a direct access track and calls the method for storing the
     * track relevant data in the db.
     * @param trackJob the trackjob to import as direct access track
     */
    private void parseDirectAccessTrack(TrackJob trackJob) {
        
        /*
         * Algorithm:
         * if (track not yet imported) {
         *      convert to sam/bam, if necessary (NEW FILE)
         *      parse mappings 
         *      extend bam file (NEW FILE) - deleteOldFile
         * }
         * create statistics (advantage: is already sorted by coordinate & classification in file)
         */   

        this.setChromMaps(trackJob);
        boolean success;
        
        //only extend, if data is not already stored in it
        if (!trackJob.isAlreadyImported()) {
            File inputFile = trackJob.getFile();
            MappingParserI mappingParser = trackJob.getParser();
            inputFile.setReadOnly(); //prevents changes or deletion of original file!
            try {
                //executes any conversion before other calculations, if the parser supports any
                success = (boolean) trackJob.getParser().convert(trackJob, chromLengthMap);
                File lastWorkFile = trackJob.getFile();

                //generate classification data in file sorted by read sequence
                mappingParser.registerObserver(this);
                Object parsingResult = mappingParser.parseInput(trackJob, chromSeqMap);
                mappingParser.removeObserver(this);
                ph.progress(workunits++);
                if (parsingResult instanceof DirectAccessDataContainer) {
                    DirectAccessDataContainer dataContainer = (DirectAccessDataContainer) parsingResult;
                    Map<String, ParsedClassification> classificationMap = dataContainer.getClassificationMap(); 

                    //write new file with classification information
                    success = success ? this.extendSamBamFile(classificationMap, trackJob, chromSeqMap) : success;
                    noErrors = noErrors ? success : noErrors;
                    if (success) { GeneralUtils.deleteOldWorkFile(lastWorkFile); }
                } else {
                    this.showMsg("Parsing of " + trackJob.getName() + "failed! The parsing result was of an unexpected type: " + parsingResult.getClass());
                }

            } catch (OutOfMemoryError ex) {
                this.showMsg("Out of memory error during parsing of direct access track: " + ex.getMessage());
                this.noErrors = false;
                return;
            } catch (Exception ex) {
                this.showMsg("Error during parsing of direct access track: " + ex.getMessage());
                Exceptions.printStackTrace(ex); //TODO: remove this error handling
                this.noErrors = false;
                return;
            }
            inputFile.setWritable(true);
            mappingParser.removeObserver(this);
        }

        //generate position table and statistics data for track
        //file needs to be sorted by coordinate for efficient calculation
        SamBamStatsParser statsParser = new SamBamStatsParser();
        StatsContainer statsContainer = new StatsContainer();
        statsContainer.prepareForTrack();
        statsParser.setStatsContainer(statsContainer);
        statsParser.registerObserver(this);
        ParsedTrack track = statsParser.createTrackStats(trackJob, chromLengthMap);
        statsParser.removeObserver(this);

        this.storeDirectAccessTrack(track, false);
    }

    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();

        ph.start(workunits);
        workunits = 0;

        ph.progress(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.ref") + "...", workunits);
        this.processRefGenomeJobs();

        // track jobs have to be imported last, because they may depend upon previously imported genomes, runs
        ph.progress(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.track") + "...", workunits);
        
        //get system JVM info:
        Runtime rt = Runtime.getRuntime();
         
        this.showMsg("Your current JVM config allows up to "+GeneralUtils.formatNumber(rt.maxMemory())+" bytes of memory to be allocated.");
        this.showMsg("Currently the plattform is using "+GeneralUtils.formatNumber(rt.totalMemory() - rt.freeMemory())+" bytes of memory.");
        this.showMsg("Please be aware of that you might need to change the -J-Xmx value of your JVM to process large imports successfully.");
        this.showMsg("The value can be configured in the ../readXplorer/etc/readXplorer.conf file of this application."); 
        this.showMsg("");
        
        this.processTrackJobs();
        this.processReadPairJobs();

        return null;
    }

    @Override
    protected void done() {
        super.done();
        ph.progress(workunits);
        if (this.noErrors) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finished"));
        } else {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.partFailed"));
        }
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
    }

    @Override
    public void update(Object data) {
//        if (data instanceof String) {
//            this.showMsg((String) data);
//        
//        } else if (data instanceof ParsedTrack) {
//            //if we have a coverage container, it means that we want to store data in the position table
//            ParsedTrack track = (ParsedTrack) data;
//            if (track.getCoverageContainer() != null) {
//                ProjectConnector.getInstance().storePositionTable(track);
//            }
//        } else {
         this.showMsg(data.toString());
//        }
            
    }

    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
        this.io.getOut().println("\"" + msg);
    }

//    /**
//     * Stores the readuence pairs either in the db or directly in the file.
//     * @param readPairContainer the readuence pair data to store
//     * @param description the data set description
//     * @throws StorageException 
//     */
//    private void storeSeqPairs(ParsedReadPairContainer readPairContainer, String description) throws StorageException {
//        
//        this.io.getOut().println("Start storing readuence pair data for track data from source \""+ description +"\"");
//        ProjectConnector.getInstance().addSeqPairData(readPairContainer);
//        this.io.getOut().println("Finished storing readuence pair data for track data from source \""+ description +"\"");
//    }

    /**
     * Stores a direct access track in the database and gives appropriate status messages.
     * @param trackJob the information about the track to store
     * @param readPairs true, if this is a readuence pair import, false otherwise
     */
    private void storeDirectAccessTrack(ParsedTrack track, boolean readPairs) {
        try {
            io.getOut().println(track.getTrackName() + ": " + this.getBundleString("MSG_ImportThread.import.start.trackdirect"));
            ProjectConnector.getInstance().storeDirectAccessTrack(track);
            ProjectConnector.getInstance().storeTrackStatistics(track);
            if (readPairs) {
                ProjectConnector.getInstance().storeSeqPairTrackStatistics(track.getStatsContainer(), track.getID());
            }
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.success.trackdirect"));
            
        } catch(OutOfMemoryError e) {
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.outOfMemory") + "!");
        }
    }
    
    /**
     * @param name the name of the bundle string to return (found in Bundle.properties)
     * @return the string associated in the Bundle.properties with the given name.
     */
    private String getBundleString(String name) {
        return NbBundle.getMessage(ImportThread.class, name);
    }

    /**
     * Extends a sam or bam file with ReadXplorers classification data.
     * @param classificationMap the classification map of classification data
     * @param trackJob the track job containing the file to extend
     * @param chromSeqMap the mapping of chromosome names to chromosome sequences
     * @return true, if the extension was successful, false otherwise
     */
    private boolean extendSamBamFile(Map<String, ParsedClassification> classificationMap, TrackJob trackJob, Map<String, String> chromSeqMap) {
        boolean success;
        try {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.extension", trackJob.getFile().getName()));
            long start = System.currentTimeMillis();
            
            //sorts file again by genome coordinate (position) & stores classification data
            SamBamExtender bamExtender = new SamBamExtender(classificationMap);
            bamExtender.setDataToConvert(trackJob, chromSeqMap);
            bamExtender.registerObserver(this);
            success = bamExtender.convert();
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finish.extension", trackJob.getFile().getName());
            io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
            
        } catch (ParsingException ex) {
            this.showMsg(ex.toString());
            success = false;
        }
        
        return success;
    }
}
