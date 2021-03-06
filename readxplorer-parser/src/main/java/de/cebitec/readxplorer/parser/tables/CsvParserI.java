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

package de.cebitec.readxplorer.parser.tables;


import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;


/**
 * Interface for CSV table parsers.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface CsvParserI extends TableParserI {

    /**
     * @param autoDelimiter <code>true</code>, if the delimiter shall be
     *                      detected automatically, <code>false</code>, if the
     *                      delimiter was selected by the user.
     */
    void setAutoDelimiter( boolean autoDelimiter );


    /**
     * @param csvPref The currently selected CsvPreference.
     */
    void setCsvPref( CsvPreference csvPref );


    /**
     *
     * @param cellProcessors The currently CellProcessor for selected CSV table
     */
    void setCellProscessors( CellProcessor[] cellProcessors );


}
