package de.cebitec.vamp.ui.importer;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.SeqPairJobContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.parser.mappings.SamBamDirectParser;
import de.cebitec.vamp.parser.mappings.SamBamPosTableCreator;
import de.cebitec.vamp.parser.mappings.SamBamStepParser;
import de.cebitec.vamp.parser.mappings.SeqPairProcessorI;
import de.cebitec.vamp.parser.mappings.TrackParser;
import de.cebitec.vamp.parser.output.SamBamCombiner;
import de.cebitec.vamp.parser.output.SamBamExtender;
import de.cebitec.vamp.parser.reference.Filter.AnnotationFilter;
import de.cebitec.vamp.parser.reference.Filter.FilterRuleSource;
import de.cebitec.vamp.parser.reference.ReferenceParserI;
import de.cebitec.vamp.seqPairClassifier.SamBamDirectSeqPairClassifier;
import de.cebitec.vamp.seqPairClassifier.SeqPairClassifier;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.SamUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMRecordIterator;
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
    private List<ReferenceJob> references;
    private List<TrackJob> tracksJobs;
    private List<SeqPairJobContainer> seqPairJobs;
    private List<TrackJob> posTableJobs;
//    private HashMap<TrackJob, Boolean> validTracksRun;
    private ProgressHandle ph;
    private int workunits;
    private CoverageContainer covContainer;
    private boolean noErrors = true;

    /**
     * THE thread in VAMP for handling the import of data.
     * @param refJobs reference jobs to import
     * @param trackJobs track jobs to import
     * @param seqPairJobs sequence pair jobs to import
     * @param posTableJobs position table jobs to import
     */
    public ImportThread(List<ReferenceJob> refJobs, List<TrackJob> trackJobs, List<SeqPairJobContainer> seqPairJobs,
            List<TrackJob> posTableJobs) {
        super();
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ImportThread.class, "ImportThread.output.name"), false);
        this.tracksJobs = trackJobs;
        this.references = refJobs;
        this.seqPairJobs = seqPairJobs;
        this.posTableJobs = posTableJobs;
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.name"));
        this.workunits = refJobs.size() + 2 * trackJobs.size() + 2 * seqPairJobs.size() + 2 * posTableJobs.size();

//        this.validTracksRun = new HashMap<TrackJob, Boolean>();
    }

    private ParsedReference parseRefJob(ReferenceJob refGenJob) throws ParsingException, OutOfMemoryError {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        ReferenceParserI parser = refGenJob.getParser();
        parser.registerObserver(this);
        AnnotationFilter filter = new AnnotationFilter();
        filter.addBlacklistRule(new FilterRuleSource());
        ParsedReference refGenome = parser.parseReference(refGenJob, filter);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
        return refGenome;
    }

    
    private ParsedTrack parseTrack(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing track data from source \"{0} with track job ID {1}\"", new Object[]{trackJob.getFile().getAbsolutePath(), trackJob.getID()});
        
        String sequenceString = this.getReference(trackJob);
        TrackParser parser = new TrackParser();
        ParsedTrack track = parser.parseMappings(trackJob, sequenceString, this, covContainer);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());
        return track;
    }

    
    private void storeRefGenome(ParsedReference refGenome, ReferenceJob refGenJob) throws StorageException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        int refGenID = ProjectConnector.getInstance().addRefGenome(refGenome);
        refGenJob.setPersistant(refGenID);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
    }

    
    /**
     * Calls the appropriate method for storing a track in the db.
     * @param track the track data to store
     * @param trackJob the track job belonging to the track
     * @param seqPairs true, if this is a sequence pair track, false otherwise
     * @param onlyPosTable true, if only the position table should be stored, but not the track
     * @throws StorageException 
     */
    private void storeTrack(ParsedTrack track, TrackJob trackJob, boolean seqPairs, boolean onlyPosTable) throws StorageException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());

        ProjectConnector.getInstance().addTrack(track, seqPairs, onlyPosTable);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());
    }

    //TODO: Delete setValidTracksRun? Seems to be unneeded
//    private void setValidTracksRun(List<TrackJob> trackJobs, boolean valid) {
//        for (Iterator<TrackJob> it = trackJobs.iterator(); it.hasNext();) {
//            TrackJob t = it.next();
//            if (validTracksRun.containsKey(t)) {
//                // do not change status of tracks back from false to true
//                // once false, always false
//                if (validTracksRun.get(t) == true) {
//                    validTracksRun.put(t, valid);
//                }
//            } else {
//                // register new track
//                validTracksRun.put(t, valid);
//            }
//        }
//    }

    /**
     * Processes all reference genome jobs of this import process.
     */
    private void processRefGenomeJobs() {
        if (!references.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.ref") + ":");
            long start;
            long finish;
            String msg;
            
            for (Iterator<ReferenceJob> it = references.iterator(); it.hasNext();) {
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
//                        if (r.hasRegisteredTrackswithoutrRunJob()) {
//                            setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
//                        }
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

//                    // validate tracks
//                    setValidTracksRun(r.getDependentTrackswithoutRunjob(), true);

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                    this.noErrors = false;
//                    if (r.hasRegisteredTrackswithoutrRunJob()) {
//                        setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
//                    }
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
     * @param trackJob the track job whose reference genome is needed
     * @return the reference genome string
     */
    private String getReference(TrackJob trackJob) { 
        String referenceSeq = null;
        try {
            int id = trackJob.getRefGen().getID();
            referenceSeq = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefGenome().getSequence();
        } catch (Exception ex) {
            Logger.getLogger(ImportThread.class.getName()).log(Level.WARNING, "Could not get the ref genome\"{0}\"{1}", new Object[]{trackJob.getFile().getName(), ex});
        }
        return referenceSeq;
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
                                
                //parsing track
                if (!trackJob.isDbUsed() && trackJob.getParser() instanceof SamBamDirectParser) {
                    this.parseDirectAccessTrack(trackJob);

                } else if (trackJob.getParser() instanceof SamBamStepParser) {
                    this.parseStepwiseTrack(trackJob, false);
                
                } else if (trackJob.isDbUsed()) {
                    this.parseSingleTrack(trackJob, false);
                }

                it.remove();
            }
        }
    }

    /**
     * Processes all position table jobs for the current import.
     */
    private void processPosTableJobs() {
        if (!this.posTableJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.posTable") + ":");

            for (Iterator<TrackJob> it = this.posTableJobs.iterator(); it.hasNext();) {
                TrackJob t = it.next();
                ph.progress(workunits++);

                //parsing position table

                if (t.getParser() instanceof SamBamStepParser) {
                    this.parseStepwiseTrack(t, true);
                } else {
                    this.parseSingleTrack(t, true);
                }

                it.remove();
            }
        }
    }

    /**
     * Processes all sequence pair jobs for the current import.
     */
    private void processSeqPairJobs() {

        if (!seqPairJobs.isEmpty()) {

            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.seqPairs") + ":");

            long start;
            long finish;
            String msg;
            
            for (Iterator<SeqPairJobContainer> it = seqPairJobs.iterator(); it.hasNext();) {
                start = System.currentTimeMillis();
                SeqPairJobContainer seqPairJobContainer = it.next();
                ph.progress(workunits++);

                int distance = seqPairJobContainer.getDistance();
                if (distance > 0) {
                    
                    int trackId1;
                    int trackId2 = -1; //TODO: change import of track 2 for db import
                    
                    ///////////////////////////////////////////////////////////////////////
                    //////////// Treatment of db tracks! //////////////////////////////////
                    ///////////////////////////////////////////////////////////////////////
                    if (seqPairJobContainer.getTrackJob1().isDbUsed()) {
                        //parsing tracks
                        ParsedTrack track1 = this.parseSingleTrack(seqPairJobContainer.getTrackJob1(), false);
                        ParsedTrack track2 = this.parseSingleTrack(seqPairJobContainer.getTrackJob2(), false);
                        trackId1 = track1.getID();
                        trackId2 = track2.getID();
                        //TODO: handle import of a single file, too!
                        SeqPairClassifier seqPairClassifier = new SeqPairClassifier();
                        seqPairClassifier.setData(track1, track2, distance, seqPairJobContainer.getDeviation(), seqPairJobContainer.getOrientation());
                        String description = seqPairJobContainer.getTrackJob1().getFile().getName() + " and " + seqPairJobContainer.getTrackJob2().getFile().getName();

                        try { //storing sequence pairs data
                            this.storeSeqPairs(seqPairClassifier.classifySeqPairs(), description);
                            finish = System.currentTimeMillis();
                            msg = "\"" + description + " sequence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
                            io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
                        } catch (StorageException ex) {
                            Exceptions.printStackTrace(ex);
                            io.getOut().println("\"" + description + " sequence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                            Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                            this.noErrors = false;
                        }

                        track1.clear();
                        track2.clear();
                        System.gc();
                        
                    ///////////////////////////////////////////////////////////////////////
                    //////////// Special treatment of direct access tracks here: //////////
                    ///////////////////////////////////////////////////////////////////////
                    } else {
                        
                        /*
                         * Algorithm:
                         * start file
                         * if (track not yet imported) {
                         *      if (isTwoTracks) { combine them unsorted (NEW FILE) }
                         *      sort by readseq (NEW FILE) - if isTwoTracks: deleteOldFile
                         *      parse mappings 
                         *      sort by read name (NEW FILE) - deleteOldFile
                         *      seq pair classification, extension & sorting by coordinate - deleteOldFile
                         * }
                         * create position table (advantage: is already sorted by coordinate & classification in file)
                         */
                        
                        TrackJob trackJob1 = seqPairJobContainer.getTrackJob1();
                        TrackJob trackJob2 = seqPairJobContainer.getTrackJob2();
                        Map<String, Pair<Integer, Integer>> classificationMap = new HashMap<>();
                        String referenceSeq = this.getReference(trackJob1);
                        File inputFile1 = trackJob1.getFile();
                        File inputFile2 = trackJob2.getFile();
                        inputFile1.setReadOnly(); //prevents changes or deletion of original file!
                        
                        File lastWorkFile = trackJob1.getFile(); //file which was created in the last step of the classification

                        if (!trackJob1.isAlreadyImported()) {
                            
                            boolean isTwoTracks = trackJob2.getFile() != null;
                            if (isTwoTracks) { //only combine, if data is not already combined
                                inputFile2.setReadOnly();

                                //combine both tracks and continue with trackJob1
                                SamBamCombiner combiner = new SamBamCombiner(trackJob1, trackJob2, false);
                                combiner.registerObserver(this);
                                combiner.combineTracks();
                                lastWorkFile = trackJob1.getFile();
                                inputFile2.setWritable(true);
                            }

                            //sort file by read sequence for efficient classification
                            this.sortSamBam(trackJob1, SAMFileHeader.SortOrder.readseq, "readSequence");
                            if (isTwoTracks) { //only if a new file was created before, we want to delete the obsolete one
                                this.deleteOldWorkFile(lastWorkFile);
                            }
                            lastWorkFile = trackJob1.getFile();

                            //generate classification data in sorted file
                            SamBamDirectParser samBamDirectParser = (SamBamDirectParser) trackJob1.getParser();
                            samBamDirectParser.registerObserver(this);
                            try {
                                DirectAccessDataContainer dataContainer = samBamDirectParser.parseInput(trackJob1, referenceSeq);
                                classificationMap = dataContainer.getClassificationMap();
                            } catch (OutOfMemoryError ex) {
                                this.showMsg("Out of Memory error during parsing of direct access track: " + ex.toString());
                                this.noErrors = false;
                            } catch (Exception ex) {
                                this.showMsg("Error during parsing of direct access track: " + ex.toString());
                                this.noErrors = false;
                            }

                            //sort by read name for efficient seq pair classification
                            this.sortSamBam(trackJob1, SAMFileHeader.SortOrder.queryname, "readName");
                            this.deleteOldWorkFile(lastWorkFile);
                            lastWorkFile = trackJob1.getFile();

                            //extension for both classification and seq pair info
                            SamBamDirectSeqPairClassifier samBamDirectSeqPairClassifier = new SamBamDirectSeqPairClassifier(
                                    seqPairJobContainer, referenceSeq, classificationMap);
                            samBamDirectSeqPairClassifier.registerObserver(this);
                            samBamDirectSeqPairClassifier.classifySeqPairs();
                            this.deleteOldWorkFile(lastWorkFile);
                        } //else case with 2 already imported tracks is prohibited

                        //create position table
                        SamBamPosTableCreator posTableCreator = new SamBamPosTableCreator();
                        posTableCreator.registerObserver(this);
                        ParsedTrack track = posTableCreator.createPosTable(trackJob1, referenceSeq);
//                            posTableCreator.getStatistics();

                        this.storeDirectAccessTrack(track); // store track entry in db
                        trackId1 = trackJob1.getID();
                        inputFile1.setWritable(true);
                    }

                    //seq pair ids have to be set in track entry
                    ProjectConnector.getInstance().setSeqPairIdsForTrackIds(trackId1, trackId2);

                } else { //if (distance <= 0)
                    this.showMsg(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.error"));
                    this.noErrors = false;
                }

                it.remove();
            }
        }
    }

    /**
     * Parses and stores a single trackJob.
     * @param trackJob job to parse
     * @return returns the trackJob if everything went fine, otherwise <code>null</code>
     */
    private ParsedTrack parseSingleTrack(TrackJob trackJob, boolean onlyPositionTable) {

        // only import this track if no problems occured with dependencies
        try {
            
            long start = System.currentTimeMillis();

            //parsing track
            ParsedTrack track = this.parseTrack(trackJob);
            //needed for onlyPositionTable case
            boolean seqPairs = false;
            if (trackJob.getParser() instanceof SeqPairProcessorI) {
                track.setReadnameToSeqIdMap1(((SeqPairProcessorI) trackJob.getParser()).getReadNameToSeqIDMap1());
                ((SeqPairProcessorI) trackJob.getParser()).resetSeqIdToReadnameMaps();
                seqPairs = true;
            }

            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));


            //storing track
            try {
                this.storeTrack(track, trackJob, seqPairs, onlyPositionTable);
                long finish = System.currentTimeMillis();
                String msg = "\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
                io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
            } catch (StorageException ex) {
                // something went wrong
                io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                this.noErrors = false;
            }
            return track;


        } catch (ParsingException | OutOfMemoryError ex) {
            // something went wrong
            io.getOut().println(ex.getMessage());
            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
            this.noErrors = false;
        }
        return null;
    }

    /**
     * Parses a track in chunks and stores all it's data in the db afterwards.
     * @param trackJob the trackjob to import
     * @param onlyPositionTable true, if only the position table should be 
     *      imported for the track
     */
    private void parseStepwiseTrack(TrackJob trackJob, boolean onlyPositionTable) {
        String filename = trackJob.getFile().getName();
        boolean isLastTrack = false;
        int start = 1;
        int stepsize = trackJob.getStepSize();
        int stop = stepsize;
        
        long startTime;
        long finish;
        String msg;
        
        if (!trackJob.isSorted()) { this.sortSamBam(trackJob, SAMFileHeader.SortOrder.readseq, "readSequence"); }
        
        while (!isLastTrack) {
            
            startTime = System.currentTimeMillis();
            
            //parsing
            trackJob.setStart(start);
            trackJob.setStop(stop);
            trackJob.setIsFirstJob(start == 1 ? true : false);
            ParsedTrack track = null;
            try {
                track = this.parseTrack(trackJob);

                covContainer = track.getCoverageContainer();
            } catch (ParsingException ex) {
                // something went wrong
                io.getOut().println(ex.getMessage());
                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                this.noErrors = false;
            }
            track.setIsFirstTrack(start == 1 ? true : false);
            isLastTrack = track.getParsedMappingContainer().isLastMappingContainer();
            io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsedReads", start, stop));

            //storing
            try {
                this.storeTrack(track, trackJob, false, onlyPositionTable);
                finish = System.currentTimeMillis();
                msg = "\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.storedReads", start, stop);
                io.getOut().println(Benchmark.calculateDuration(startTime, finish, msg));
            } catch (StorageException ex) {
                // something went wrong
                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                this.noErrors = false;
            }

            stop += stepsize;
            start += stepsize;
        }

    }
    
    /**
     * Parses a direct access track and calls the method for storing the
     * track relevant data in the db.
     * @param trackJob the trackjob to import as direct access track
     */
    private void parseDirectAccessTrack(TrackJob trackJob) {
        
        String referenceSeq = this.getReference(trackJob);
        
        /*
         * Algorithm:
         * if (track not yet imported) {
         *      sort by readseq (NEW FILE)
         *      parse mappings 
         *      extend bam file (NEW FILE) - deleteOldFile
         * }
         * create position table (advantage: is already sorted by coordinate & classification in file)
         */   
        
        //only extend, if data is not already stored in it
        if (!trackJob.isAlreadyImported()) {
            File inputFile = trackJob.getFile();
            inputFile.setReadOnly(); //prevents changes or deletion of original file!
            //sort file by read sequence for efficient classification
            this.sortSamBam(trackJob, SAMFileHeader.SortOrder.readseq, "readSequence");
            File lastWorkFile = trackJob.getFile();

            //generate classification data in sorted file
            SamBamDirectParser samBamDirectParser = (SamBamDirectParser) trackJob.getParser();
            samBamDirectParser.registerObserver(this);
            try {
                DirectAccessDataContainer dataContainer = samBamDirectParser.parseInput(trackJob, referenceSeq);
                Map<String, Pair<Integer, Integer>> classificationMap = dataContainer.getClassificationMap();
                
                //write new file with classification information
                this.extendSamBamFile(classificationMap, trackJob, referenceSeq);
                this.deleteOldWorkFile(lastWorkFile);

            } catch (OutOfMemoryError ex) {
                this.showMsg("Out of memory error during parsing of direct access track: " + ex.toString());
            } catch (Exception ex) {
                this.showMsg("Error during parsing of direct access track: " + ex.toString());
                Exceptions.printStackTrace(ex); //TODO: remove this error handling
            }
            inputFile.setWritable(true);
        }

        //generate position table data from track
        //file needs to be sorted by coordinate for efficient calculation
        SamBamPosTableCreator posTableCreator = new SamBamPosTableCreator();
        posTableCreator.registerObserver(this);
        ParsedTrack track = posTableCreator.createPosTable(trackJob, referenceSeq);

        this.storeDirectAccessTrack(track);
    }
    
    /**
     * Deletes the given file.
     * @param lastWorkFile the file to delete
     */
    private void deleteOldWorkFile(File lastWorkFile) {
        try {
            Files.delete(lastWorkFile.toPath());
        } catch (IOException ex) {
            this.showMsg(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.FileDeletionError", lastWorkFile.getAbsolutePath()));
        }
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
        this.processTrackJobs();
        this.processSeqPairJobs();
        this.processPosTableJobs();
//        validTracksRun.clear();

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
        if (data instanceof String) {
            this.showMsg((String) data);
        
        } else if (data instanceof ParsedTrack) {
            //if we have a coverage container, it means that we want to store data in the position table
            ParsedTrack track = (ParsedTrack) data;
            if (track.getCoverageContainer() != null) {
                ProjectConnector.getInstance().storePositionTable(track);
            }
        } else {
            this.showMsg(data.toString());
        }
            
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

    /**
     * Stores the sequence pairs either in the db or directly in the file.
     * @param seqPairContainer the sequence pair data to store
     * @param description the data set description
     * @throws StorageException 
     */
    private void storeSeqPairs(ParsedSeqPairContainer seqPairContainer, String description) throws StorageException {
        
        this.io.getOut().println("Start storing sequence pair data for track data from source \""+ description +"\"");
        ProjectConnector.getInstance().addSeqPairData(seqPairContainer);
        this.io.getOut().println("Finished storing sequence pair data for track data from source \""+ description +"\"");
    }

    /**
     * Stores a direct access track in the database and gives appropriate status messages.
     * @param trackJob the information about the track to store
     */
    private void storeDirectAccessTrack(ParsedTrack track) {
        try {
            io.getOut().println(track.getTrackName() + ": " + this.getBundleString("MSG_ImportThread.import.start.trackdirect"));
            ProjectConnector.getInstance().storeDirectAccessTrack(track);
            ProjectConnector.getInstance().storeTrackStatistics(track);
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.success.trackdirect"));
            
        } catch(OutOfMemoryError e) {
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.outOfMemory") + "!");
        }
    }
    
    /**
     * Sorts a sam or bam file according to the specified
     * SamFileHeader.SortOrder and sets the new sorted file as the file in the
     * trackJob.
     * @param trackJob track job containing the file to sort
     * @param sortOrder the sort order to use
     * @param sortOrderMsg the string representation of the sort order for 
     * status messages
     */
    private void sortSamBam(TrackJob trackJob, SAMFileHeader.SortOrder sortOrder, String sortOrderMsg) {
        io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.sort.Start", sortOrderMsg));
        long start = System.currentTimeMillis();
        
        try (SAMFileReader samBamReader = new SAMFileReader(trackJob.getFile())) {
            SAMRecordIterator samItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(sortOrder);
            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(trackJob.getFile(), header, false, ".sort_" + sortOrderMsg);
            SAMFileWriter writer = writerAndFile.getFirst();
            while (samItor.hasNext()) {
                writer.addAlignment(samItor.next());
            }
            samItor.close();
            writer.close();
            
            trackJob.setFile(writerAndFile.getSecond());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            trackJob.setFile(new File(trackJob.getFile() + ".sort_" + sortOrderMsg));
        }

        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.sort.Finish", sortOrderMsg);
        io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
    }
    
    /**
     * @param name the name of the bundle string to return (found in Bundle.properties)
     * @return the string associated in the Bundle.properties with the given name.
     */
    private String getBundleString(String name) {
        return NbBundle.getMessage(ImportThread.class, name);
    }

    private void extendSamBamFile(Map<String, Pair<Integer, Integer>> classificationMap, TrackJob trackJob, String sequenceString) {
        try {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.extension", trackJob.getFile().getName()));
            long start = System.currentTimeMillis();
            
            //sort file again by genome coordinate (position) & store classification data
            SamBamExtender bamExtender = new SamBamExtender(classificationMap);
            bamExtender.setDataToConvert(trackJob, sequenceString);
            bamExtender.registerObserver(this);
            bamExtender.convert();
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finish.extension", trackJob.getFile().getName());
            io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
            
        } catch (ParsingException ex) {
            this.showMsg(ex.toString());
        }
    }
}
