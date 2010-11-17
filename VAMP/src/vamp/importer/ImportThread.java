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
import vamp.parsing.reference.Filter.FeatureFilter;
import vamp.parsing.reference.Filter.FilterRuleSource;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public class ImportThread extends SwingWorker implements RunningTaskI{

    private ImporterController c;
    private List<ReferenceJob> gens;
    private List<TrackJobs> tracksRun;
    private HashMap<TrackJobs, Boolean> validTracksRun;

    public ImportThread(ImporterController c, List<ReferenceJob> gens, List<TrackJobs> tracksRun){
        super();
        this.c = c;
        this.tracksRun = tracksRun;
        this.gens = gens;
        
        validTracksRun = new HashMap<TrackJobs, Boolean>();
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

/*    private ParsedRun parseRun(RunJob runJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");
        RunParserI parser = runJob.getParser();

        ParsedRun run = parser.parseRun(runJob);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising run data from source \""+runJob.getFile().getAbsolutePath()+"\"");
        return run;
    }*/

        private ParsedRun parseRunfromTrack(TrackJobs trackJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing run data from mapping source \""+trackJob.getFile().getAbsolutePath()+"\"");
        TrackParserI parser = new TrackParser();
         Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing parser: \""+parser.toString()+"\"");
        ParsedRun run = parser.parseMappingforReadData(trackJob);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising run data from mapping source \""+trackJob.getFile().getAbsolutePath()+"\"");
        return run;
    }


    private ParsedTrack parseTrack(TrackJobs trackJob) throws ParsingException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing track data from source \""+trackJob.getFile().getAbsolutePath()+"trackjobID" +trackJob.getID()+"\"");

        HashMap<String, Integer> readnameToSeqIDmap = ProjectConnector.getInstance().getRunConnector(trackJob.getID()).getReadnameToSeqIDMapping();
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

   /* private void storeRun(ParsedRun run, RunJob runJob) throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");

        long runID = ProjectConnector.getInstance().addRun(run);

        runJob.setPersistant(runID);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing run data from source \""+runJob.getFile().getAbsolutePath()+"\"");

    }*/

    public void storeRunFromTrackData(ParsedRun run , TrackJobs trackJob)throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing run data from source \""+trackJob.getFile().getAbsolutePath()+"\"");


        long runID = ProjectConnector.getInstance().addRun(run);

         trackJob.setPersistant(runID);


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing run data from source \""+trackJob.getFile().getAbsolutePath()+"runID" +runID+"\"");


    }

    private void storeTrack(ParsedTrack track, TrackJobs trackJob) throws StorageException{
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start storing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");

        Long trackID = ProjectConnector.getInstance().addTrack(track, trackJob.getID(), trackJob.getRefGen().getID());
        trackJob.setPersistant(trackID);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished storing track data from source \""+trackJob.getFile().getAbsolutePath()+"\"");
    }


        private void setValidTracksRun(List<TrackJobs> trackJobs, boolean valid){
        for(Iterator<TrackJobs> it = trackJobs.iterator(); it.hasNext(); ){
            TrackJobs t = it.next();
            if(validTracksRun.containsKey(t)){

                // do not change status of tracks back from false to true
                // once false, always false
                if(validTracksRun.get(t) == true){
                    validTracksRun.put(t, valid);
                }

            } else {
                // register new track
                validTracksRun.put(t, valid);
            }

        }
    }

        private boolean isValidTrackwithoutRun(TrackJobs trackJob){
        if(!validTracksRun.containsKey(trackJob)){
            // track is not dependent on previous run oder refGen, so it is not registered
            return true;
        } else if(validTracksRun.containsKey(trackJob) && validTracksRun.get(trackJob) == true){
            // if it is registered, it must be a valid one
            return true;
        } else {
            return false;
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
                        if(r.hasRegisteredTrackswithoutrRunJob()){
                            setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
                        }
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // validate tracks
                    setValidTracksRun(r.getDependentTrackswithoutRunjob(), true);

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    c.updateImportStatus("\""+r.getName() + "\" failed!");
                    if(r.hasRegisteredTrackswithoutrRunJob()){
                        setValidTracksRun(r.getDependentTrackswithoutRunjob(), false);
                    }
                    Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                it.remove();
            }

            c.updateImportStatus("");
        }
    }


    private void processTrackRUNJobs(){
        if(!tracksRun.isEmpty()){
            c.updateImportStatus("Starting import of reads of tracks:");
            for(Iterator<TrackJobs> it = tracksRun.iterator(); it.hasNext(); ){
                TrackJobs t = it.next();

                // only import this track if no problems occured with dependencies
                if(isValidTrackwithoutRun(t)){
                    try {

                        //parsing
                        ParsedRun run = parseRunfromTrack(t);
                        c.updateImportStatus("\""+t.getFile().getName() + "\" parsed");
                    //returns the reads that couldnt be read by the parser
                    if(!run.getErrorList().isEmpty() || run.getSequences().isEmpty()){
                    c.updateImportStatus("Couldn't load reads: " + run.getErrorList().toString());
                    }
                    //storing
                    try {
                        storeRunFromTrackData(run, t);
                        c.updateImportStatus("\""+t.getFile().getName() + "\" stored");
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        c.updateImportStatus("\""+t.getFile().getName()+"\" failed!");
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }

            } catch (ParsingException ex) {
                        // something went wrong
                        c.updateImportStatus("\""+t.getFile().getName() + "\" failed");
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    c.updateImportStatus("\""+t.getFile().getName()+" has not been processed, bocause of previous errors in data that this file depends on!");
                }
            }
        }
    }



    private void processTrackJobs(){
        if(!tracksRun.isEmpty()){
            c.updateImportStatus("Starting import of tracks:");
            for(Iterator<TrackJobs> it = tracksRun.iterator(); it.hasNext(); ){
                TrackJobs t = it.next();

                // only import this track if no problems occured with dependencies
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

                it.remove();

            }
        }

    }

    @Override
    protected Object doInBackground() {

        
        processRefGenJobs();
       // processRunJobs();
        processTrackRUNJobs();
        // track jobs have to be imported last, because they may depend upon previously imported genomes, runs
        processTrackJobs();
        validTracksRun.clear();

        return null;
    }

    @Override
    protected void done(){
        super.done();
        c.updateImportStatus("Finished");
        c.importDone(this);
    }

}
