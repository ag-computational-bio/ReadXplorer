/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.dashboard;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.dialogmenus.explorer.StandardItem;
import java.util.Date;


/**
 * This class is used to create an information piece to be displayed in the
 * dashboard of ReadXplorer. It represents a track or a genome.
 * <p>
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
     * <p>
     * @param ref reference genome to store in this object
     */
    public DBItem( PersistentReference ref ) {
        this.setChild( DBItem.Child.GENOME );
        this.setID( ref.getId() );
        this.setTitleHidden( ref.getName() );
        this.setDescriptionHidden( ref.getDescription() );
        this.setTimestampHidden( ref.getTimeStamp() );
        this.setRefID( null );
    }


    /**
     * This class is used to create an information piece to be displayed in the
     * dashboard of ReadXplorer. It represents a track or a genome.
     * <p>
     * @param track track to store in this object
     */
    public DBItem( PersistentTrack track ) {
        this.setChild( DBItem.Child.TRACK );
        this.setID( track.getId() );
        this.setTitleHidden( "Track #" + track.getId() );
        this.setDescriptionHidden( track.getDescription() );
        this.setTimestampHidden( track.getTimestamp() );
        this.setRefID( new Long( track.getRefGenID() ) );
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
    public void setChild( Child child ) {
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
    public void setDescriptionHidden( String description ) {
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
    public void setTimestampHidden( Date timestamp ) {
        this.timestamp = timestamp;
    }


    /**
     * @return the unique Reference ID
     */
    public Long getRefID() {
        return refID;
    }


    /**
     * @param refID the unique Reference ID to set
     */
    public void setRefID( Long refID ) {
        this.refID = refID;
    }


    /**
     * @return The unique id of this DBItem
     */
    public Long getID() {
        return id;
    }


    /**
     * sets the unique id of this DBItem.
     * <p>
     * @param number The unique id of this DBItem
     */
    public void setID( Integer number ) {
        this.id = new Long( number );
    }


    /**
     * @return The title of this DBItem.
     */
    public String getTitle() {
        return title;
    }


    /**
     * The title of this DBItem.
     * <p>
     * @param title The title of this DBItem.
     */
    public void setTitleHidden( String title ) {
        this.title = title;
    }


    /**
     * Determines the type of a DBItem: GENOME or TRACK.
     */
    public enum Child {

        GENOME, TRACK

    }

}
