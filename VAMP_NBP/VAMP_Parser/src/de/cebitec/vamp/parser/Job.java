package de.cebitec.vamp.parser;

import java.io.File;
import java.sql.Timestamp;

/**
 * Interface for any kind of jobs, which involve handling a file and can be
 * described by id, name, description and timestamp.
 *
 * @author ddoppmeier
 */
public interface Job {

    /**
     * @return The name of the job
     */
    public String getName();

    /**
     * @return The file belonging to the job
     */
    public File getFile();

    /**
     * @return The description of the job
     */
    public String getDescription();

    /**
     * @return The timestamp of the creation of the job
     */
    public Timestamp getTimestamp();

    /**
     * @return The id of the job
     */
    public int getID();

}
