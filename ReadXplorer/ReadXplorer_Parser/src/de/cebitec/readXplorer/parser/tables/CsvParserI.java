package de.cebitec.readXplorer.parser.tables;

import org.supercsv.prefs.CsvPreference;

/**
 * Interface for CSV table parsers.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public interface CsvParserI extends TableParserI {
    
    /**
     * @param autoDelimiter <cc>true</cc>, if the delimiter shall be detected
     * automatically, <cc>false</cc>, if the delimiter was selected by the user.
     */
    public void setAutoDelimiter(boolean autoDelimiter);

    /**
     * @param csvPref The currently selected CsvPreference.
     */
    public void setCsvPref(CsvPreference csvPref);
    
}
