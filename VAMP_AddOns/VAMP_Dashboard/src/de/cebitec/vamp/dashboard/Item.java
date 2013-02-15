/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.dashboard;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.Date;


/**
 * 
 * This class is used to create an information piece to be displayed in the 
 * dashboard of vamp. It representats a track or a genome.
 * 
 * @author jeff
 */
public class Item {
    
    private Long ID;
    private String  category;
    private String  title;
    private String description;
    private Date timestamp;
    private Boolean mark = false;
    private Long RefID;
    

    public Item(PersistantReference r) {
        this.setKind(Item.Kind.GENOME);
        this.setID(r.getId());
        this.setTitleHidden(r.getName());
        this.setDescriptionHidden(r.getDescription());
        this.setTimestampHidden(r.getTimeStamp());
        this.setRefID(null);
    }
    
    public Item(PersistantTrack r) {
        this.setKind(Item.Kind.TRACK);
        this.setID(r.getId()); 
        this.setTitleHidden("Track #" + r.getId());
        this.setDescriptionHidden(r.getDescription());
        this.setTimestampHidden(r.getTimestamp());   
        this.setRefID(new Long(r.getRefGenID()));
    }
    
    /**
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(Kind kind) {
        this.kind = kind;
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
     * @return the mark
     */
    public Boolean getMark() {
        return mark;
    }

    /**
     * @param mark the mark to set
     */
    public void setMark(Boolean mark) {
        this.mark = mark;
    }

    /**
     * @return the RefID
     */
    public Long getRefID() {
        return RefID;
    }

    /**
     * @param RefID the RefID to set
     */
    public void setRefID(Long RefID) {
        this.RefID = RefID;
    }
    
    
    public enum Kind {
        GENOME, TRACK 
    }
    
    private Kind kind = Kind.GENOME;

    
    /** Creates a new instance of Instrument */
    public Item() {
    }
    
    public Long getID() {
        return ID;
    }
     
    public void setID(Integer number) {
        this.ID = new Long(number);
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitleHidden(String title) {
        this.title = title;
    }
    
}