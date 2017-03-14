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


import java.util.List;


/**
 * By implementing this interface, a data structure is able to provide its data
 * in the correct format to be received by a {@link TableExporterI}.
 * <p>
 * @author -Rolf Hilker-
 */
public interface ExportDataI {

    /**
     * @return creates and returns the list of sheet names which can be used for
     *         multiple sheets or files belonging together.
     */
    public List<String> dataSheetNames();


    /**
     * @return creates and returns the list of descriptions for the columns.
     */
    public List<List<String>> dataColumnDescriptions();


    /**
     * @return creates and returns the list of rows belonging to the table.
     */
    public List<List<List<Object>>> dataToExcelExportList();


}
