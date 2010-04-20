package vamp.importer;

import java.util.HashMap;
import vamp.parsing.common.ParsingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import vamp.RunningTaskI;
import vamp.databackend.connector.StorageException;
import vamp.databackend.connector.ProjectConnector;
import vamp.parsing.common.ParsedReference;
import vamp.parsing.common.ParsedRun;
import vamp.parsing.common.ParsedTrack;
import vamp.parsing.mappings.TrackParser;
import vamp.parsing.mappings.TrackParserI;
import vamp.parsing.reads.FastaParser;
import vamp.parsing.reads.RunParserI;
import vamp.parsing.reference.Filter.FeatureFilter;
import vamp.parsing.reference.Filter.FilterRuleSource;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public class ImportThread extends SwingWorker implements RunningTaskI{

    private ImporterController c;
    private List<RunJob> runs;
    private List<ReferenceJob> gens;
    private List<TrackJob> tracks;
    private HashMap<TrackJob, Boolean> validTracks;

    public ImportThread(ImporterController c, List<RunJob> runs, List<ReferenceJob> gens, List<TrackJob> tracks){
        super();
        this.c = c;
        this.runs = runs;
        this.gens = gens;
        this.tracks = tracks;
        validTracks = new HashMap<TrackJob, Boolean>();
    }

    private ParsedReference parseRefGen(ReferenceJob refGenJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing reference genome from source \""+refGenJob.getFile().getAbsolutePath()+"\"");

        ReferenceParserI parser = refGenJob.getParser();
        FeatureFilter filter = new FeatureFilter();
        filter.addBlacklistRule(new FilterRuleSource());
        ParsedReference refGenome =  parser.parseReference(refGenJob, filter);
        
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing reference genome from source \""+refGenJob.getFile().getAbsolutePath()+"\"");
        return refGenome;

    }

    private ParsedRun parseRun(RunJob runJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");

        RunParserI parser = new FastaParser();
        ParsedRun run = parser.parseRun(runJob);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising run data from source \""+runJob.getFile().getAbsolutePath()+"\"");
        return run;
    }

    private ParsedTrack parseTrack(TrackJob trackJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");

        HashMap<String, Integer> readnameToSeqIDmap = ProjectConnector.getInstance().getRunConnector(trackJob.getRunJob().getID()).getReadnameToSeqIDMapping();
        TrackParserI parser = new TrackParser();
        ParsedTrack track = parser.parseMappings(trackJob, readnameToSeqIDmap);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");
        return track;

    }

    private void storeRefGen(ParsedReference refGenome, ReferenceJob refGenJob) throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing reference genome from source \""+refGenJob.getFile().getAbsolutePath()+"\"");

        long refGenID = ProjectConnector.getInstance().addRefGenome(refGenome);
        refGenJob.setPersistant(refGenID);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing reference genome from source \""+refGenJob.getFile().getAbsolutePath()+"\"");
    }

    private void storeRun(ParsedRun run, RunJob runJob) throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");

        long runID = ProjectConnector.getInstance().addRun(run);

        runJob.setPersistant(runID);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");

    }

    private void storeTrack(ParsedTrack track, TrackJob trackJob) throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");

        Long trackID = ProjectConnector.getInstance().addTrack(track, trackJob.getRunJob().getID(), trackJob.getRefGen().getID());
        trackJob.setPersistant(trackID);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");
    }

    private void setValidTracks(List<TrackJob> trackJobs, boolean valid){
        for(Iterator<TrackJob> it = trackJobs.iterator(); it.hasNext(); ){
            TrackJob t = it.next();
            if(validTracks.containsKey(t)){

                // do not change status of tracks back from false to true
                // once false, always false
                if(validTracks.get(t) == true){
                    validTracks.put(t, valid);
                }

            } else {
                // register new track
                validTracks.put(t, valid);
            }
            
        }
    }
    
    private boolean isValidTrack(TrackJob trackJob){
        if(!validTracks.containsKey(trackJob)){
            // track is not dependent on previous run oder refGen, so it is not registered
            return true;
        } else if(validTracks.containsKey(trackJob) && validTracks.get(trackJob) == true){
            // if it is registered, it must be a valid one
            return true;
        } else {
            return false;
        }
    }

    private void processRunJobs(){

        if(!runs.isEmpty()){
            c.updateImportStatus("Starting import of runs:");

            for(Iterator<RunJob> it = runs.iterator(); it.hasNext(); ){
                RunJob r = it.next();

                try {
                    //parsing
                    ParsedRun run = parseRun(r);
                    c.updateImportStatus("\""+r.getFile().getName() + "\" parsed");

                    //storing
                    try {
                        storeRun(run, r);
                        c.updateImportStatus("\""+r.getFile().getName() + "\" stored");
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        c.updateImportStatus("\""+r.getFile().getName()+"\" failed!");
                        if(r.hasRegisteredTracks()){
                            setValidTracks(r.getDependentTracks(), false);
                        }
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }

                    // validate dependent tracks
                    setValidTracks(r.getDependentTracks(), true);

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    c.updateImportStatus("\""+r.getFile().getName()+"\" failed!");
                    if(r.hasRegisteredTracks()){
                        setValidTracks(r.getDependentTracks(), false);
                    }
                    Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                it.remove();

            }

            c.updateImportStatus("");
        }

    }

    private void processRefGenJobs(){
        if(!gens.isEmpty()){
            c.updateImportStatus("Starting import of references:");

            for(Iterator<ReferenceJob> it = gens.iterator(); it.hasNext(); ){
                ReferenceJob r = it.next();

                try {
                    // parsing
                    ParsedReference refGen = parseRefGen(r);
                    c.updateImportStatus("\""+r.getName() + "\" parsed");

                    // storing
                    try {
                        storeRefGen(refGen, r);
                        c.updateImportStatus("\""+r.getName() + "\" stored");
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        c.updateImportStatus("\""+r.getName() + "\" failed!");
                        if(r.hasRegisteredTracks()){
                            setValidTracks(r.getDependentTracks(), false);
                        }
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // validate tracks
                    setValidTracks(r.getDependentTracks(), true);

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    c.updateImportStatus("\""+r.getName() + "\" failed!");
                    if(r.hasRegisteredTracks()){
                        setValidTracks(r.getDependentTracks(), false);
                    }
                    Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                it.remove();
            }

            c.updateImportStatus("");
        }
    }

    private void processTrackJobs(){
        if(!tracks.isEmpty()){
            c.updateImportStatus("Starting import of tracks:");
            for(Iterator<TrackJob> it = tracks.iterator(); it.hasNext(); ){
                TrackJob t = it.next();

                // only import this track if no problems occured with dependencies
                if(isValidTrack(t)){
                    try {

                        //parsing
                        ParsedTrack track = parseTrack(t);
                        c.updateImportStatus("\""+t.getFile().getName() + "\" parsed");

                        //storing
                        try {
                            storeTrack(track, t);
                            c.updateImportStatus("\""+t.getFile().getName() + "\" stored");
                        } catch (StorageException ex) {
                        // something went wrong
                            c.updateImportStatus("\""+t.getFile().getName() + "\" failed");
                            Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (ParsingException ex) {
                        // something went wrong
                        c.updateImportStatus("\""+t.getFile().getName() + "\" failed");
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    c.updateImportStatus("\""+t.getFile().getName()+" has not been processed, bocause of previous errors in data that this file depends on!");
                }

                it.remove();

            }
        }

    }

    @Override
    protected Object doInBackground() {

        processRunJobs();
        processRefGenJobs();
        // track jobs have to be imported last, because they may depend upon previously imported genomes, runs
        processTrackJobs();

        validTracks.clear();

        return null;
    }

    @Override
    protected void done(){
        super.done();
        c.updateImportStatus("Finished");
        c.importDone(this);
    }

}
