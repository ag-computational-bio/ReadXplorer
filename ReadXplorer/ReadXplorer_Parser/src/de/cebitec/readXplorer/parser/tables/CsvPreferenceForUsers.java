package de.cebitec.readXplorer.parser.tables;

import org.supercsv.prefs.CsvPreference;

/**
 * Enumeration of CSV preferences combined with a String, which can be displayed
 * to the users.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public enum CsvPreferenceForUsers {

    /** Excel north europe pref: delimiter ";", line end symbol: "\n". */
    EXCEL_NORTH_EUROPE_PREF(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE, CsvPreferenceForUsers.EXCEL_NORTH_EUROPE_PREF_STRING),
    /** Excel standard pref: delimiter ",", line end symbol: "\n". */
    EXCEL_PREF(CsvPreference.EXCEL_PREFERENCE, CsvPreferenceForUsers.EXCEL_PREF_STRING),
    /** Standard pref: delimiter ",", line end symbol: "\r\n". */
    STANDARD_PREF(CsvPreference.STANDARD_PREFERENCE, CsvPreferenceForUsers.STANDARD_PREF_STRING),
    /** Tab separated pref: delimiter "\t", line end symbol: "\n". */
    TAB_PREF(CsvPreference.TAB_PREFERENCE, CsvPreferenceForUsers.TAB_PREF_STRING);

    private static final String EXCEL_NORTH_EUROPE_PREF_STRING = "Excel north europe (; and \\n)";
    private static final String EXCEL_PREF_STRING = "Excel std (, and \\n)";
    private static final String STANDARD_PREF_STRING = "Standard (, and \\r\\n)";
    private static final String TAB_PREF_STRING = "Tab separated (\\t and \\n)";

    private final CsvPreference csvPref;
    private final String description;

    private CsvPreferenceForUsers(CsvPreference csvPref, String description) {
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
