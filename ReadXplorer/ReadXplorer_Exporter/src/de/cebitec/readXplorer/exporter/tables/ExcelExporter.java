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
package de.cebitec.readXplorer.exporter.tables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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
import jxl.write.biff.RowsExceededException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;


/**
 * General excel exporter. It supports even multiple sheets in one document.
 *
 * @author -Rolf Hilker-
 */
public class ExcelExporter implements TableExporterI {
    

    private ProgressHandle progressHandle;
    private List<String> sheetNames; //contains all sheet names
    private List<List<String>> headers; //contains all headers
    /** Inner list contains data of one row, middle list contains all rows, 
     * outer list is the list of sheets. */
    private List<List<List<Object>>> exportData; 
    private int rowNumberGlobal;
    
    /**
     * General excel exporter. It supports even multiple sheets in one document.
     * All 3 data fields have to be set in order to start a successful export.
     * @param progressHandle the progress handle which should display the
     * progress of the ExcelExporter
     */
    public ExcelExporter(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.rowNumberGlobal = 0;
    } 
    
    /**
     * @param dataSheetNames the sheet name listf for the sheets which should be
     * exported to the excel file. Must be set!
     */
    @Override
    public void setSheetNames(List<String> dataSheetNames) {
        this.sheetNames = dataSheetNames;
    } 

    /**
     * @param headers the header list for the tables which should be exported to
     * the excel file. Must be set!
     */
    @Override
    public void setHeaders(List<List<String>> headers) {
        this.headers = headers;
    }

    /**
     * @param exportData The list of data which should be exported to excel.
     * Must be set!
     */
    @Override
    public void setExportData(List<List<List<Object>>> exportData) {
        this.exportData = exportData;
    }
    
    /**
     * Carries out the whole export process to an excel file.
     * @param file the file in which to write the data.
     * @return the file in which the data was written.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws WriteException
     * @throws OutOfMemoryError  
     */
    @NbBundle.Messages({"SuccessMsg=Excel exporter stored data successfully: ", 
                        "SuccessHeader=Success"})
    @Override
    public File writeFile(File file) throws FileNotFoundException, IOException, WriteException, OutOfMemoryError {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting to write Excel file...{0}", file.getAbsolutePath());
 
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
        WritableSheet sheet = null;
        int currentPage;
        int totalPage = 0;
        boolean dataLeft;
        String sheetName;
        List<List<Object>> sheetData;
        List<String> headerRow;
        
        for (int i = 0; i < exportData.size(); ++i) {
            sheetName = sheetNames.get(i);
            sheetData = exportData.get(i);
            headerRow = headers.get(i);
            dataLeft = true;
            currentPage = 0;
            while (dataLeft) { //only 65536 rows allowed per sheet in xls format
                if (!sheetData.isEmpty()) {
                    if (currentPage++ > 0) {
                        sheetName += "I";
                    }
                    sheet = workbook.createSheet(sheetName, totalPage++);
                }

                if (sheet != null) {
                    dataLeft = this.fillSheet(sheet, sheetData, headerRow);
                }
            }
        }
        workbook.write();
        workbook.close();

        NotificationDisplayer.getDefault().notify(Bundle.SuccessHeader(), new ImageIcon(), Bundle.SuccessMsg() + sheetNames.get(0), null);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished writing Excel file!");

        return file;
    }
  
    /**
     * This method actually fills the given excel sheet with the data handed over to
     * this ExcelExporter.
     * @param sheet the sheet to write the data to
     * @param sheetData the data to write in this sheet
     * @param headerRow the header to use for this sheet
     * @return dataLeft: false, if the sheet could store all data, true, if there is too
     * much data for one sheet
     * @throws OutOfMemoryError 
     * @throws WriteException 
     */
    public boolean fillSheet(WritableSheet sheet, List<List<Object>> sheetData, List<String> headerRow) throws OutOfMemoryError, WriteException {

        boolean dataLeft = false;
        int row = 0;
        int column = 0;

        for (String header : headerRow) {
            this.addColumn(sheet, "LABEL", header, column++, row);
        }
        ++row;
        this.progressHandle.progress("Storing line", this.rowNumberGlobal++);

        String objectType;
        for (List<Object> exportRow : sheetData) {

            column = 0;
            for (Object entry : exportRow) {
                objectType = this.getObjectType(entry);
                try {
                    this.addColumn(sheet, objectType, entry, column++, row);
                } catch (RowsExceededException e) {
                    dataLeft = true;
                    break;
                }
            }
            if (dataLeft) { break; }
            if (this.rowNumberGlobal++ % 100 == 0) {
                this.progressHandle.progress("Storing line", this.rowNumberGlobal);
            }
            ++row;
        }
        
        if (dataLeft) {
            for (int i = 0; i < row; ++i) {
                sheetData.remove(0);
            }
        }
        
        return dataLeft;
    }

    /**
     * @param entry the entry whose object type needs to be checked
     * @return The string representing the object type of the entry. Currently
     * only "INTEGER", "STRING, or "UNKNOWN".
     */
    private String getObjectType(Object entry) {
        if (entry instanceof Integer || entry instanceof Byte || entry instanceof Long) {
            return "INTEGER";
        } else if (entry instanceof String || entry instanceof Character
                || entry instanceof CharSequence) {
            return "STRING";
        } else if (entry instanceof Double) {
            return "DOUBLE";
        } else if (entry instanceof Float) {
            return "FLOAT";
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
            cellvalue = cellvalue instanceof Double ? cellvalue.toString() : cellvalue;
            WritableCellFormat string = new WritableCellFormat(arial);
            Label label = new Label(column, row, (String) cellvalue, string);
            sheet.addCell(label);
        } else if (celltype.equals("INTEGER")) {
            WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);
            Integer value = Integer.parseInt(cellvalue.toString());
            Number number = new Number(column, row, value, integerFormat);
            sheet.addCell(number);
        } else if (celltype.equals("DOUBLE")) {
            Double value = Double.parseDouble(cellvalue.toString());
            Number number = new Number(column, row, value);
            sheet.addCell(number);
        }else if (celltype.equals("FLOAT")) {
            WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.FLOAT);
            Float value = Float.parseFloat(cellvalue.toString());
            Number number = new Number(column, row, value, integerFormat);
            sheet.addCell(number);
        } else if (celltype.equals("UNKNOWN")) {
            WritableCellFormat string = new WritableCellFormat(arial);
            Label label = new Label(column, row, cellvalue.toString(), string);
            sheet.addCell(label);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readyToExport() {
        return this.exportData != null && !this.exportData.isEmpty() 
                && this.headers != null && !this.headers.isEmpty()
                && this.sheetNames != null && !this.sheetNames.isEmpty();
    }
}
