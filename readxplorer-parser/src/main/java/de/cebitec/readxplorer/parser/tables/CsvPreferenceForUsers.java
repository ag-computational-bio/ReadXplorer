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


import org.supercsv.prefs.CsvPreference;


/**
 * Enumeration of CSV preferences combined with a String, which can be displayed
 * to the users.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum CsvPreferenceForUsers {

    /**
     * Excel north europe pref: delimiter ";", line end symbol: "\n".
     */
    EXCEL_NORTH_EUROPE_PREF( CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE, CsvPreferenceForUsers.EXCEL_NORTH_EUROPE_PREF_STRING ),
    /**
     * Excel standard pref: delimiter ",", line end symbol: "\n".
     */
    EXCEL_PREF( CsvPreference.EXCEL_PREFERENCE, CsvPreferenceForUsers.EXCEL_PREF_STRING ),
    /**
     * Standard pref: delimiter ",", line end symbol: "\r\n".
     */
    STANDARD_PREF( CsvPreference.STANDARD_PREFERENCE, CsvPreferenceForUsers.STANDARD_PREF_STRING ),
    /**
     * Tab separated pref: delimiter "\t", line end symbol: "\n".
     */
    TAB_PREF( CsvPreference.TAB_PREFERENCE, CsvPreferenceForUsers.TAB_PREF_STRING );

    private static final String EXCEL_NORTH_EUROPE_PREF_STRING = "Excel north europe (; and \\n)";
    private static final String EXCEL_PREF_STRING = "Excel std (, and \\n)";
    private static final String STANDARD_PREF_STRING = "Standard (, and \\r\\n)";
    private static final String TAB_PREF_STRING = "Tab separated (\\t and \\n)";

    private final CsvPreference csvPref;
    private final String description;


    private CsvPreferenceForUsers( CsvPreference csvPref, String description ) {
        this.csvPref = csvPref;
        this.description = description;
    }


    /**
     * @return The CsvPreference belonging to this pref.
     */
    public CsvPreference getCsvPref() {
        return csvPref;
    }


    /**
     * @return The user-readable String representation of the CsvPreference.
     */
    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        return this.getDescription();
    }


}
