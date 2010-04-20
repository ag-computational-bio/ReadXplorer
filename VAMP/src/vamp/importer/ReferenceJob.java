package vamp.importer;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceJob {

    private Long id;
    private String name;
    private File file;
    private ReferenceParserI parser;
    private String description;
    private Timestamp timestamp;
    private List<TrackJob> tracks;

    public ReferenceJob(Long id, File file, ReferenceParserI parser, String description, String name, Timestamp timestamp){
        this.id = id;
        this.name = name;
        this.file = file;
        this.parser = parser;
        this.description = description;
        this.timestamp = timestamp;
        tracks = new ArrayList<TrackJob>();
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

    public String getName(){
        return name;
    }

    public File getFile() {
        return file;
    }

    public ReferenceParserI getParser(){
        return parser;
    }

    public String getDescription(){
        return description;
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

    public void setPersistant(Long id){
        this.id = id;
    }

    public long getID(){
        return id;
    }

    @Override
    public String toString(){
        if(isPersistant()){
            return " db: "+name+" "+timestamp;
        } else {
            return "new: "+name+" "+timestamp;
        }
    }
    

}
