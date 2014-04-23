package de.cebitec.readXplorer.parser.tables;

import de.cebitec.readXplorer.parser.common.ParsingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import org.openide.util.Exceptions;

/**
 * Parser for Xls table files using the jxl package.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class XlsTableParser implements TableParserI {

    private static String name = "Xls Table Parser";
    private static String[] fileExtension = new String[]{"xls", "XLS", "Xls"};
    private static String fileDescription = "Xls table";

    /**
     * Parser for Xls table files using the jxl package.
     */
    public XlsTableParser() {
    }
    
    /**
     * A method for parsing Xls table files using the jxl package.
     * @param fileToRead The file containing the table to read.
     * @return Table in form of a list, which contains the row lists of Objects.
     * @throws ParsingException
     */
    @Override
    public List<List<?>> parseTable(File fileToRead) throws ParsingException {
        
        List<List<?>> tableData = new ArrayList<>();
        
        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            Workbook workbook;
            workbook = Workbook.getWorkbook(fileToRead);
            boolean fstSheet = true;
            String headerStartFstSheet = "";
            for (Sheet sheet : workbook.getSheets()) { //TODO: filter stats and parametersheet
                for (int i = 0; i < sheet.getRows(); i++) {
                    if (fstSheet) {
                        headerStartFstSheet = sheet.getCell(0, 0).getContents();
                        fstSheet = false;
                    }
                    //only read all sheets from the beginning, which seem to contain the data = has the same header start than the first sheet
                    if (headerStartFstSheet.equals(sheet.getCell(0, 0).getContents())) {

                        Cell[] cells = sheet.getRow(i);
                        List<Object> rowData = new ArrayList<>();
                        for (Cell cell : cells) {
                            String content = cell.getContents();
                            rowData.add(content);
//                        CellType type = cell.getType(); other usefule stuff, if needed
//                        CellFeatures feature = cell.getCellFeatures();
//                        CellFormat format = cell.getCellFormat();
                        }
                        tableData.add(rowData);
                    }
                }
            }
        } catch (IOException | BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return tableData;

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
