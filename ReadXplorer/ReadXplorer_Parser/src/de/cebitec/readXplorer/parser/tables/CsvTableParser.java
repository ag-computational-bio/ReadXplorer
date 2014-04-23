package de.cebitec.readXplorer.parser.tables;

import de.cebitec.readXplorer.parser.common.ParsingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.util.Exceptions;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * A parser for parsing CSV files.
 *  
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class CsvTableParser implements CsvParserI {

    private static String name = "CSV Table Parser";
    private static String[] fileExtension = new String[]{"csv", "CSV"};
    private static String fileDescription = "CSV table";
    
    private boolean autoDelimiter;
    private CsvPreference csvPref;
    //different CellProcessors for different tables
    public static final CellProcessor[] DEFAULT_TABLE_PROCESSOR = new CellProcessor[0];
    public static final CellProcessor[] POS_TABLE_PROCESSOR = new CellProcessor[]{ new ParseInt()};
    private String tableModel;

    public CsvTableParser() {
        this.autoDelimiter = true;
        this.csvPref = null;
    }
    
    /**
     * A method for parsing CSV files in any of the four available formats
     * supported by the @see CsvPreference class. 
     * @see CsvPreference
     * @param fileToRead The file containing the table to read.
     * @return Table in form of a list, which contains the row lists of Objects.
     */
    @Override
    public List<List<?>> parseTable(File fileToRead) throws ParsingException {
        
        List<List<?>> tableData = null;
        
        if (autoDelimiter) {

            //try all available csv preferences
            List<CsvPreference> csvPreferences = new ArrayList<>();
            csvPreferences.add(CsvPreference.STANDARD_PREFERENCE);
            csvPreferences.add(CsvPreference.EXCEL_PREFERENCE);
            csvPreferences.add(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            csvPreferences.add(CsvPreference.TAB_PREFERENCE);

            for (CsvPreference pref : csvPreferences) {
                
                tableData = this.parseTable(fileToRead, pref);
                if (tableData != null) {
                    Logger.getLogger(CsvTableParser.class.getName()).log(Level.INFO, "Entry delimiter used for this table is: {0}", (char) pref.getDelimiterChar());
                    break;
                }
            }

            if (tableData == null) {
                throw new ParsingException("Table is not in a readable format and cannot be imported. Use a valid CSV format!");
            }
        
        } else {
            tableData = this.parseTable(fileToRead, csvPref);
            
            if (tableData == null) {
                throw new ParsingException("Table is not in a readable format and cannot be imported.\n"
                        + "Either choose the correct delimiter and line end characters or try autodetection of delimiter and line end character!");
            }
        }

        return tableData;
    }
    
    /**
     * Method for parsing a CSV file for a given csv preference.
     * @param fileToRead The file containing the table to read.
     * @param csvPreference The CscPreference to use for parsing.
     * @return Table in form of a list, which contains the row lists of Objects.
     */
    public List<List<?>> parseTable(File fileToRead, CsvPreference csvPreference) {
        ICsvListReader listReader = null;
        List<List<?>> tableData = new ArrayList<>();
        try {
            try {
                listReader = new CsvListReader(new FileReader(fileToRead), csvPreference); //Preference could be parsed as option

                final String[] header = listReader.getHeader(true);
                tableData.add(Arrays.asList(header));
                
                CellProcessor[] generalProcessors;
                if (tableModel.equals(TableType.COVERAGE_ANALYSIS.getName())
                        || tableModel.equals(TableType.POS_TABLE.getName())
                        || tableModel.equals(TableType.SNP_DETECTION.getName())
                        || tableModel.equals(TableType.TSS_DETECTION.getName())
                        ) {
                    generalProcessors = POS_TABLE_PROCESSOR;
                } else {
                    generalProcessors = DEFAULT_TABLE_PROCESSOR;
                }
                
                int length;
                List<Object> rowData;
                CellProcessor[] processors;
                while (listReader.read() != null) {
                    if ((length = listReader.length()) > 0) {
                        processors = generalProcessors.clone();
                        processors = ArrayUtils.addAll(processors, new CellProcessor[length - processors.length]);
                        rowData = listReader.executeProcessors(processors);
                        tableData.add(rowData);
                    }
                }

            } finally {
                if (listReader != null) {
                    listReader.close();
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SuperCsvException ex) {
            tableData = null;
        }
        return tableData;
    }
    
    public void setTableModel(String tableModel) {
        this.tableModel = tableModel;
    }
    
    /**
     * @param autoDelimiter <cc>true</cc>, if the delimiter shall be detected
     * automatically, <cc>false</cc>, if the delimiter was selected by the user.
     */
    @Override
    public void setAutoDelimiter(boolean autoDelimiter) {
        this.autoDelimiter = autoDelimiter;
    }

    /**
     * @param csvPref The currently selected CsvPreference.
     */
    @Override
    public void setCsvPref(CsvPreference csvPref) {
        this.csvPref = csvPref;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }
    
    /**
     * @return The name of the parser.
     */
    @Override
    public String toString() {
        return this.getName();
    }
}
