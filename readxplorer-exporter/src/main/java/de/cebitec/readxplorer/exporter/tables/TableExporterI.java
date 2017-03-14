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

package de.cebitec.readxplorer.exporter.tables;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jxl.write.WriteException;


/**
 * Interface to use for table exporters.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface TableExporterI {

    /**
     * @param headers the header list for the tables which should be exported to
     *                the table file. Must be set!
     */
    public void setHeaders( List<List<String>> headers );


    /**
     * @param dataSheetNames the sheet name listf for the sheets which should be
     *                       exported to the table file. Must be set!
     */
    public void setSheetNames( List<String> dataSheetNames );


    /**
     * @param exportData The list of data which should be exported to a table
     *                   file. Must be set!
     */
    public void setExportData( List<List<List<Object>>> exportData );


    /**
     * Carries out the whole export process to a table file.
     * <p>
     * @param file the file in which to write the data.
     * <p>
     * @return the file in which the data was written.
     * <p>
     * @throws FileNotFoundException
     * @throws IOException
     * @throws OutOfMemoryError
     * @throws WriteException
     */
    public File writeFile( File file ) throws FileNotFoundException, IOException, OutOfMemoryError, WriteException;


    /**
     * @return true, if the complete export process can be started by calling
     *         {@link writeFile()}.
     */
    public boolean readyToExport();


}
