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

package de.cebitec.readxplorer.parser;


import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import java.io.File;
import java.sql.Timestamp;


/**
 * A track job is a container for all necessary data for a track to be parsed.
 *
 * @author ddoppmeier, rhilker
 */
public class TrackJob implements Job {

    private File file;
    private final String description;
    private final Timestamp timestamp;
    private final MappingParserI parser;
    private final int trackID;
    private ReferenceJob refGen;
    private final boolean isAlreadyImported;


    /**
     * Creates a new track job along with its data.
     * <p>
     * @param trackID           id of the track to create
     * @param file              the file to be parsed as track
     * @param description       the description of the track
     * @param refGen            the ReferenceJob with all information about the
     *                          reference
     * @param parser            the parser to use for parsing
     * @param isAlreadyImported true, if this track was already imported in
     *                          another readxplorer db.
     * @param timestamp         the timestamp when it was created
     */
    public TrackJob( int trackID, File file, String description, ReferenceJob refGen,
                     MappingParserI parser, boolean isAlreadyImported, Timestamp timestamp ) {
        this.trackID = trackID;
        this.file = file;
        this.description = description;
        this.timestamp = timestamp;
        this.parser = parser;
        this.isAlreadyImported = isAlreadyImported;
        this.refGen = refGen;
    }


    /**
     * @return the parser, which shall be used for parsing this track job.
     */
    public MappingParserI getParser() {
        return parser;
    }


    /**
     * @return the description of this track job.
     */
    @Override
    public String getDescription() {
        return description;
    }


    /**
     * @return the reference genome associated with this track job.
     */
    public ReferenceJob getRefGen() {
        return refGen;
    }


    /**
     * @return the file, which contains the track data and which should be
     *         parsed and stored as track.
     */
    @Override
    public File getFile() {
        return file;
    }


    /**
     * @param file the file, which contains the track data and which should be
     *             parsed and stored as track.
     */
    public void setFile( File file ) {
        this.file = file;
    }


    /**
     * @param refGen the reference genome associated with this track job.
     */
    public void setRefGen( ReferenceJob refGen ) {
        this.refGen = refGen;
    }


    /**
     * @return the timestamp at which this track job was created.
     */
    @Override
    public Timestamp getTimestamp() {
        return this.timestamp;
    }


    /**
     * @return the track id of this track job used in the db later on.
     */
    @Override
    public int getID() {
        return this.trackID;
    }


    /**
     * @return the name of this track job.
     */
    @Override
    public String getName() {
        return this.getDescription();
    }


    /**
     * @return Modified to return the description and the timestamp.
     */
    @Override
    public String toString() {
        return this.description + ":" + this.timestamp;
    }


    /**
     * @return true, if this track was already imported in another readxplorer
     *         db, false otherwise.
     */
    public boolean isAlreadyImported() {
        return this.isAlreadyImported;
    }


}
