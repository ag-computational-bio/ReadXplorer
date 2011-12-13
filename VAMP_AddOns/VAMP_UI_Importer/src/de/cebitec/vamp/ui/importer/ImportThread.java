package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.parser.mappings.ISeqPairClassifier;
import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.externalSort.ExternalSortBAM;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.mappings.TrackParser;
import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import de.cebitec.vamp.parser.reference.Filter.FilterRuleSource;
import de.cebitec.vamp.parser.reference.ReferenceParserI;
import de.cebitec.vamp.util.Observer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.mappings.SamBamStepParser;
import de.cebitec.vamp.parser.mappings.SequencePairParserI;
import org.openide.util.Lookup;

/**
 * @author ddoppmeier, rhilker
 * 
 * Thread handling the import of data.
 */
public class ImportThread extends SwingWorker<Object, Object> implements Observer {

    private InputOutput io;
    private List<ReferenceJob> references;
    private List<TrackJob> tracksJobs;
    private List<SeqPairJobContainer> seqPairJobs;
    private List<TrackJob> posTableJobs;
    private HashMap<TrackJob, Boolean> validTracksRun;
    private ProgressHandle ph;
    private int workunits;
    private CoverageContainer covContainer;

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

        this.validTracksRun = new HashMap<TrackJob, Boolean>();
    }

    private ParsedReference parseRefJob(ReferenceJob refGenJob) throws ParsingException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        ReferenceParserI parser = refGenJob.getParser();
        parser.registerObserver(this);
        FeatureFilter filter = new FeatureFilter();
        filter.addBlacklistRule(new FilterRuleSource());
        ParsedReference refGenome = parser.parseReference(refGenJob, filter);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
        return refGenome;
    }

    private ParsedTrack parseTrack(TrackJob trackJob) throws ParsingException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing track data from source \"{0}trackjobID{1}\"", new Object[]{trackJob.getFile().getAbsolutePath(), trackJob.getID()});

        String sequenceString = null;
        try {
            int id = trackJob.getRefGen().getID();
            sequenceString = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefGen().getSequence();
        } catch (Exception ex) {
            Logger.getLogger(ImportThread.class.getName()).log(Level.WARNING, "Could not get the ref genome\"{0}\"{1}", new Object[]{trackJob.getFile().getAbsolutePath(), ex});
        }

        TrackParser parser = new TrackParser();
//        ParsedTrack track = parser.parseMappings(trackJob, readnameToSeqIDmap, sequenceString, this);
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

    
    private void storeTrack(ParsedTrack track, TrackJob trackJob, boolean seqPairs, boolean onlyPosTable) throws StorageException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());

        int trackID = ProjectConnector.getInstance().addTrack(track, trackJob.getRefGen().getID(), seqPairs, onlyPosTable);
        trackJob.setPersistant(trackID);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing track data from source \"{0}\"", trackJob.getFile().getAbsolutePath());
    }

    
    private void setValidTracksRun(List<TrackJob> trackJobs, boolean valid) {
        for (Iterator<TrackJob> it = trackJobs.iterator(); it.hasNext();) {
            TrackJob t = it.next();
            if (validTracksRun.containsKey(t)) {
                // do not change status of tracks back from false to true
                // once false, always false
                if (validTracksRun.get(t) == true) {
                    validTracksRun.put(t, valid);
                }
            } else {
                // register new track
                validTracksRun.put(t, valid);
            }
        }
    }

    private void processRefGenomeJobs() {
        if (!references.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.ref") + ":");

            for (Iterator<ReferenceJob> it = references.iterator(); it.hasNext();) {
                ReferenceJob r = it.next();
                ph.progress(workunits++);

                try {
                    // parsing
                    ParsedReference refGen = this.parseRefJob(r);
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));

                    // storing
                    try {
                        storeRefGenome(refGen, r);
                        io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored"));
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                        if (r.hasRegisteredTrackswithoutrRunJob()) {
                            setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
                        }
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // validate tracks
                    setValidTracksRun(r.getDependentTrackswithoutRunjob(), true);

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                    if (r.hasRegisteredTrackswithoutrRunJob()) {
                        setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
                    }
                    Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OutOfMemoryError ex) {
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.outOfMemory") + "!");
                }

                it.remove();
            }

            io.getOut().println("");
        }
    }

    /**
     * Processes track jobs (parsing and storing).
     */
    private void processTrackJobs() {
        if (!tracksJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.track") + ":");

            for (Iterator<TrackJob> it = tracksJobs.iterator(); it.hasNext();) {
                TrackJob t = it.next();
                ph.progress(workunits++);

                //parsing track

                if (t.getParser() instanceof SamBamStepParser) {
                    this.parseStepwiseTrack(t, false);
                } else {
                    this.parseSingleTrack(t, false);
                }

                it.remove();
            }
        }
    }

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

    private void processSeqPairJobs() {

        if (!seqPairJobs.isEmpty()) {

            //handle processing of sequence pair track jobs AFTER both belonging tracks are stored!
            //because now all mapping ids are set!

            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.seqPairs") + ":");

            final ISeqPairClassifier seqPairClassifier = Lookup.getDefault().lookup(ISeqPairClassifier.class);
            boolean seqPairImport = seqPairClassifier != null;

            for (Iterator<SeqPairJobContainer> it = seqPairJobs.iterator(); it.hasNext();) {
                SeqPairJobContainer seqPairJobContainer = it.next();
                ph.progress(workunits++);

                int distance = seqPairJobContainer.getDistance();
                if (distance > 0) {
                    //parsing tracks
                    ParsedTrack track1 = this.parseSingleTrack(seqPairJobContainer.getTrackJob1(), false);
                    ParsedTrack track2 = this.parseSingleTrack(seqPairJobContainer.getTrackJob2(), false);

                    if (seqPairImport) {
                        seqPairClassifier.setData(track1, track2, distance, seqPairJobContainer.getDeviation(), seqPairJobContainer.getOrientation());
                        String description = seqPairJobContainer.getTrackJob1().getFile().getName() + " and " + seqPairJobContainer.getTrackJob2().getFile().getName();

                        try { //storing sequence pairs data
                            this.storeSeqPairs(seqPairClassifier.classifySeqPairs(), description);
                            io.getOut().println("\"" + description + " sequence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored"));
                        } catch (StorageException ex) {
                            // something went wrong
                            io.getOut().println("\"" + description + " sequence pair data infos \" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                            Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        ProjectConnector.getInstance().setSeqPairIdsForTrackIds(track1.getID(), track2.getID());

                        track1.clear();
                        track2.clear();
                        System.gc();
                    }
                } else if (distance <= 0) {
                    this.showErrorMsg(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.error"));
                }

                it.remove();

            }
        }
    }

    /**
     * Parses a trackJob.
     * @param trackJob job to parse
     * @return returns the trackJob if everything went fine, otherwise <code>null</code>
     */
    private ParsedTrack parseSingleTrack(TrackJob trackJob, boolean onlyPositionTable) {

        // only import this track if no problems occured with dependencies
        try {

            //parsing track
            ParsedTrack track = this.parseTrack(trackJob);
            track.setID(trackJob.getID()); //needed for onlyPositionTable case
            boolean seqPairs = false;
            if (trackJob.getParser() instanceof SequencePairParserI) {
                track.setReadnameToSeqIdMap(((SequencePairParserI) trackJob.getParser()).getSeqIDToReadNameMap());
                ((SequencePairParserI) trackJob.getParser()).resetSeqIdToReadnameMap();
                seqPairs = true;
            }

            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));


            //storing track
            try {
                this.storeTrack(track, trackJob, seqPairs, onlyPositionTable);
                io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored"));
            } catch (StorageException ex) {
                // something went wrong
                io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            return track;


        } catch (ParsingException ex) {
            // something went wrong
            io.getOut().println(ex.getMessage());
            io.getOut().println("\"" + trackJob.getFile().getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
        }
        return null;
    }

    private void parseStepwiseTrack(TrackJob trackJob, boolean onlyPositionTable) {
        String filename = trackJob.getFile().getName();
        boolean isLastTrack = false;
        //TODO:make stepsize changable for user
        int start = 1;
        int stepsize = trackJob.getStepSize();
        int stop = stepsize;
        
        if(!trackJob.isSorted()){
             ExternalSortBAM ex = new ExternalSortBAM(trackJob.getFile().getPath());
             trackJob.setFile(ex.getSortedFile());
        }
        
        while (!isLastTrack) {
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
            }
            track.setIsFirstTrack(start == 1 ? true : false);
            isLastTrack = track.getParsedMappingContainer().isLastMappingContainer();
            io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsedReads", start, stop));

            //storing
            try {
                this.storeTrack(track, trackJob, false, onlyPositionTable);
                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.storedReads", start, stop));
            } catch (StorageException ex) {
                // something went wrong
                io.getOut().println("\"" + filename + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            stop += stepsize;
            start += stepsize;
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
        validTracksRun.clear();

        return null;
    }

    @Override
    protected void done() {
        super.done();
        ph.progress(workunits);
        io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finished"));
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
    }

    @Override
    public void update(Object errorMsg) {
        if (errorMsg instanceof String) {
            this.showErrorMsg((String) errorMsg);
        }
    }

    /**
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param errorMsg
     */
    private void showErrorMsg(String errorMsg) {
        this.io.getOut().println("\"" + errorMsg);
    }

    private void storeSeqPairs(ParsedSeqPairContainer seqPairContainer, String description) throws StorageException {

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing sequence pair data for track data from source \"{0}\"", description);
        ProjectConnector.getInstance().addSeqPairData(seqPairContainer);
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing sequence pair data for track data from source \"{0}\"", description);

    }
}
