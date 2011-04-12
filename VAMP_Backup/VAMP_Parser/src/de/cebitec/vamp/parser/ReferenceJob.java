package de.cebitec.vamp.parser;

import de.cebitec.vamp.parser.reference.ReferenceParserI;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceJob implements Job{

    private Long id;
    private String name;
    private File file;
    private ReferenceParserI parser;
    private String description;
    private Timestamp timestamp;
    private List<TrackJobs> trackswithoutRunjob;

    public ReferenceJob(Long id, File file, ReferenceParserI parser, String description, String name, Timestamp timestamp){
        this.id = id;
        this.name = name;
        this.file = file;
        this.parser = parser;
        this.description = description;
        this.timestamp = timestamp;
   
        trackswithoutRunjob = new ArrayList<TrackJobs>();
    }

    public void registerTrackWithoutRunJob(TrackJobs t){
        trackswithoutRunjob.add(t);
    }

    public void unregisterTrackwithoutRunJob(TrackJobs t) {
        while (trackswithoutRunjob.contains(t)) {
            trackswithoutRunjob.remove(t);
        }
    }

    public boolean hasRegisteredTrackswithoutrRunJob() {
        if (trackswithoutRunjob.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<TrackJobs> getDependentTrackswithoutRunjob() {
        return trackswithoutRunjob;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public File getFile() {
        return file;
    }

    public ReferenceParserI getParser(){
        return parser;
    }

    @Override
    public String getDescription(){
        return description;
    }

    @Override
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

    @Override
    public Long getID(){
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
