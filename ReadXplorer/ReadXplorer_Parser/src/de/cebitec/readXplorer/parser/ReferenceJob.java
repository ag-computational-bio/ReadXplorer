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
package de.cebitec.readXplorer.parser;

import de.cebitec.readXplorer.parser.reference.ReferenceParserI;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * A reference job containing all information about the import of a reference sequence.
 * 
 * @author ddoppmeier, rhilker
 */
public class ReferenceJob implements Job {

    private Integer id;
    private String name;
    private File file;
    private File gffFile;
    private ReferenceParserI parser;
    private String description;
    private Timestamp timestamp;
    private List<TrackJob> trackswithoutRunjob;

    /**
     * A reference job containing all information about the import of a reference sequence.
     * @param id reference id, if already available
     * @param file the file in which the reference is stored, if needed
     * @param parser the parser for parsing the reference, if needed
     * @param description the description of the reference
     * @param name the name of the reference
     * @param timestamp the timestamp of the import
     */
    public ReferenceJob(Integer id, File file, ReferenceParserI parser, String description, String name, Timestamp timestamp){
        this.id = id;
        this.name = name;
        this.file = file;
        this.parser = parser;
        this.description = description;
        this.timestamp = timestamp;
   
        trackswithoutRunjob = new ArrayList<>();
    }
    
    /**
     * A reference job containing all information about the import of a
     * reference sequence. Use this constructor for importing GFF references,
     * which require an additional sequence file in fasta format.
     * @param id reference id, if already available
     * @param fastaFile the file in which the reference sequence is stored, if needed
     * @param gffFile the file in which the reference features are stored, if needed
     * @param parser the parser for parsing the reference, if needed
     * @param description the description of the reference
     * @param name the name of the reference
     * @param timestamp the timestamp of the import
     */
    public ReferenceJob(Integer id, File fastaFile, File gffFile, ReferenceParserI parser, String description, String name, Timestamp timestamp) {
        this(id, fastaFile, parser, description, name, timestamp);
        this.gffFile = gffFile;
    }

    public void registerTrackWithoutRunJob(TrackJob t){
        trackswithoutRunjob.add(t);
    }

    public void unregisterTrackwithoutRunJob(TrackJob t) {
        while (trackswithoutRunjob.contains(t)) {
            trackswithoutRunjob.remove(t);
        }
    }

    public boolean hasRegisteredTrackswithoutrRunJob() {
        return trackswithoutRunjob.size() > 0;
    }

    public List<TrackJob> getDependentTrackswithoutRunjob() {
        return trackswithoutRunjob;
    }

    /**
     * @return the name of the reference
     */
    @Override
    public String getName(){
        return name;
    }

    /**
     * @return the file, in which the reference is stored
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * @return the file in which the reference features are stored, if this is an
     * import of a GFF reference.
     */
    public File getGffFile() {
        return gffFile;
    }

    public ReferenceParserI getParser(){
        return parser;
    }

    @Override
    public String getDescription(){
        return description;
    }

    /**
     * @return the timestamp of the import
     */
    @Override
    public Timestamp getTimestamp(){
        return timestamp;
    }

    public boolean isPersistant() {
        return id != null;
    }

    /**
     * @param id Set this reference job persistant by setting its unique db id.
     */
    public void setPersistant(int id){
        this.id = id;
    }

    /**
     * @return the unique reference id from the db, if already available
     */
    @Override
    public int getID(){
        return id;
    }

    @Override
    public String toString() {
        if (isPersistant()) {
            return " db: " + name + " " + timestamp;
        } else {
            return "new: " + name + " " + timestamp;
        }
    }

}
