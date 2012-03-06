package de.cebitec.vamp.exporter.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;


/**
 * @author -Rolf Hilker-
 * 
 * General excel exporter.
 */
public class ExcelExporter {
    

    private ProgressHandle progressHandle;
    private String sheetName;
    private List<String> headers; //contains all headers
    private List<List<Object>> exportData; //each object contains the data of one row
    
    /**
     * 
     * @param sheetName the name of the sheet to export to excel.
     * @param progressHandle the progress handle which should display the progress
     *      of the ExcelExporter
     */
    public ExcelExporter(String sheetName, ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.sheetName = sheetName;
    }

    
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }  

    /**
     * @param headers the header list for the table which should be exported to excel.
     */
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    /**
     * @param exportData The list of data which should be exported to excel.
     */
    public void setExportData(List<List<Object>> exportData) {
        this.exportData = exportData;
    }

    
    public String getSheetName() {
        return this.sheetName;
    }
    
    /**
     * @return the header list for the table which should be exported to excel.
     */
    public List<String> getHeaders() {
        return this.headers;
    }  
    
    
    public List<List<Object>> getExportData() {
        return this.exportData;
    }
    
    /**
     * Carries out the whole export process to an excel file.
     * @param file the file in which to write the data.
     * @return the file in which the data was written.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public File writeFile(File file) throws FileNotFoundException, IOException, WriteException, OutOfMemoryError {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting to write Excel file...{0}", file.getAbsolutePath());
 
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

        WritableSheet sheet = null;
        int currentPage = 0;
        if (!exportData.isEmpty()) {
            sheet = workbook.createSheet(this.sheetName, currentPage++);
        }
        
        if (sheet != null) {
            this.fillSheet(sheet);
        }
        workbook.write();
        workbook.close();

        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.SuccessMsg") + this.sheetName,
                NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.SuccessHeader"), JOptionPane.INFORMATION_MESSAGE);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished writing Excel file!");

        return file;
    }
  
    /**
     * This method actually fills the given excel sheet with the data handed over to
     * this ExcelExporter.
     * @param sheet the sheet to write the data to
     * @throws WriteException 
     */
    public void fillSheet(WritableSheet sheet) throws OutOfMemoryError, WriteException {

        int row = 0;
        int column = 0;

        for (String header : headers) {
            this.addColumn(sheet, "LABEL", header, column++, row);
        }
        this.progressHandle.progress("Storing line", row++);

        String objectType;
        for (List<Object> exportRow : exportData) {

            column = 0;
            for (Object entry : exportRow) {
                objectType = this.getObjectType(entry);
                this.addColumn(sheet, objectType, entry, column++, row);
            }
            if (row++ % 10 == 0) {
                this.progressHandle.progress("Storing line", row);
            }
        }
        this.progressHandle.finish();
    }

    /**
     * @param entry the entry whose object type needs to be checked
     * @return The string representing the object type of the entry. Currently
     * only "INTEGER", "STRING, or "UNKNOWN".
     */
    private String getObjectType(Object entry) {
        if (entry instanceof Integer || entry instanceof Double
                 || entry instanceof Byte || entry instanceof Long
                 || entry instanceof Float) {
            return "INTEGER";
        } else if (entry instanceof String || entry instanceof Character
                || entry instanceof CharSequence) {
            return "STRING";
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Writes a single column in a given excel sheet.
     * @param sheet the sheet to write to
     * @param celltype the celltype of the cell to write
     * @param cellvalue the value to be written in the column
     * @param column column number
     * @param row row number
     * @throws WriteException
     * @throws OutOfMemoryError 
     */
    public void addColumn(WritableSheet sheet, String celltype, Object cellvalue, int column, int row) throws WriteException, OutOfMemoryError {
        WritableFont arialbold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
        if (cellvalue == null) {
            Label label = new Label(column, row, "n/a");
            sheet.addCell(label);
        } else if (celltype.equals("LABEL")) {
            WritableCellFormat header = new WritableCellFormat(arialbold);
            Label label = new Label(column, row, (String) cellvalue, header);
            sheet.addCell(label);
        } else if (celltype.equals("STRING")) {
            cellvalue = cellvalue instanceof Character ? String.valueOf(cellvalue) : cellvalue;
            cellvalue = cellvalue instanceof CharSequence ? String.valueOf(cellvalue) : cellvalue;
            WritableCellFormat string = new WritableCellFormat(arial);
            Label label = new Label(column, row, (String) cellvalue, string);
            sheet.addCell(label);
        } else if (celltype.equals("INTEGER")) {
            WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);
            Double value = Double.parseDouble(cellvalue.toString());
            Number number = new Number(column, row, value, integerFormat);
            sheet.addCell(number);
        } else if (celltype.equals("UNKNOWN")) {
            WritableCellFormat string = new WritableCellFormat(arial);
            Label label = new Label(column, row, cellvalue.toString(), string);
            sheet.addCell(label);
        }
    }
    
    /**
     * @return true, if the complete export process can be started by calling
     *      {@link writeFile()}.
     */
    public boolean readyToExport() {
        return !(this.exportData == null) && !this.exportData.isEmpty() 
                && !(this.headers == null) && !this.headers.isEmpty();
    }
}
