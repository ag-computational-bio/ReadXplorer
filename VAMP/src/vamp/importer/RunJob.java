package vamp.importer;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vamp.parsing.reads.RunParserI;

/**
 *
 * @author ddoppmeier
 */
public class RunJob {

    private Long id;
    private File file;
    private String description;
    private Timestamp timestamp;
    private List<TrackJob> tracks;
    private RunParserI parser;

    public RunJob(Long id, File file, String description, RunParserI parser, Timestamp timestamp){
        this.id = id;
        this.file = file;
        this.description = description;
        this.timestamp = timestamp;
        tracks = new ArrayList<TrackJob>();
        this.parser = parser;
    }

    public RunParserI getParser(){
        return parser;
    }

    public long getID(){
        return id;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public Timestamp getTimestamp(){
        return timestamp;
    }

    public boolean isPersistant(){
        if(id == null){
            return false;
        } else {
            return true;
        }
    }

    public void setPersistant(long id){
        this.id = id;
    }

    public void registerTrack(TrackJob t){
        tracks.add(t);
    }

    public void unregisterTrack(TrackJob t){
        while(tracks.contains(t)){
            tracks.remove(t);
        }
    }

    public boolean hasRegisteredTracks(){
        if(tracks.size() > 0 ){
            return true;
        } else {
            return false;
        }
    }

    public List<TrackJob> getDependentTracks(){
        return tracks;
    }

    @Override
    public String toString(){
        if(this.isPersistant()){
            return " db: "+description+" "+timestamp;
        } else {
            return "new: "+description+" "+timestamp;
        }
    }

}
