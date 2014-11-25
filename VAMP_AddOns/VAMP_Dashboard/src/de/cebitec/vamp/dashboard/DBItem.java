package de.cebitec.vamp.dashboard;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dialogMenus.explorer.StandardItem;
import java.util.Date;


/**
 * This class is used to create an information piece to be displayed in the 
 * dashboard of ReadXplorer. It represents a track or a genome.
 * 
 * @author jeff
 */
public class DBItem extends StandardItem {
    
    private Long id;
    private Child child = Child.GENOME;
    private String title;
    private String description;
    private Date timestamp;
    private Long refID;
    
    /**
     * This class is used to create an information piece to be displayed in the
     * dashboard of ReadXplorer. It represents a track or a genome.
     * @param ref reference genome to store in this object
     */
    public DBItem(PersistantReference ref) {
        this.setChild(DBItem.Child.GENOME);
        this.setID(ref.getId());
        this.setTitleHidden(ref.getName());
        this.setDescriptionHidden(ref.getDescription());
        this.setTimestampHidden(ref.getTimeStamp());
        this.setRefID(null);
    }
    
    /**
     * This class is used to create an information piece to be displayed in the
     * dashboard of ReadXplorer. It represents a track or a genome.
     * @param track track to store in this object
     */
    public DBItem(PersistantTrack track) {
        this.setChild(DBItem.Child.TRACK);
        this.setID(track.getId()); 
        this.setTitleHidden("Track #" + track.getId());
        this.setDescriptionHidden(track.getDescription());
        this.setTimestampHidden(track.getTimestamp());   
        this.setRefID(new Long(track.getRefGenID()));
    }

    /**
     * Creates an empty instance of DBItem.
     */
    public DBItem() {
    }
    
    /**
     * @return the child
     */
    public Child getChild() {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChild(Child child) {
        this.child = child;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescriptionHidden(String description) {
        this.description = description;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestampHidden(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the unique Reference ID
     */
    public Long getRefID() {
        return refID;
    }

    /**
     * @param RefID the unique Reference ID to set
     */
    public void setRefID(Long RefID) {
        this.refID = RefID;
    }
    
    /**
     * @return The unique id of this DBItem
     */
    public Long getID() {
        return id;
    }
     
    /**
     * sets the unique id of this DBItem.
     * @param number The unique id of this DBItem
     */
    public void setID(Integer number) {
        this.id = new Long(number);
    }
    
    /**
     * @return The title of this DBItem.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * The title of this DBItem.
     * @param title The title of this DBItem.
     */
    public void setTitleHidden(String title) {
        this.title = title;
    }
    
    
    
    /**
     * Determines the type of a DBItem: GENOME or TRACK.
     */
    public enum Child {

        GENOME, TRACK
    }
    
}