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
    String getName();


    /**
     * @return The file belonging to the job
     */
    File getFile();


    /**
     * @return The description of the job
     */
    String getDescription();


    /**
     * @return The timestamp of the creation of the job
     */
    Timestamp getTimestamp();


    /**
     * @return The id of the job
     */
    int getID();


}
