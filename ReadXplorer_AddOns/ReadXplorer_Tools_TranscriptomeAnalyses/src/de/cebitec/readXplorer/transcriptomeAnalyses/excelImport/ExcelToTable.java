/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @author jritter
 */
public class ExcelToTable implements ExcelImportDataI {
    private String tableName;
    private List<String> columnNames;
    private Object[][] fileContent;
    
    
    public ExcelToTable(File file) throws IOException {
        this.columnNames = new ArrayList<>();
        setData(file);
    }
    
    private void setData(File file) throws IOException {
        File inputWorkbook = file;
        Workbook w;
        try {
            w = Workbook.getWorkbook(inputWorkbook);
            // Get the first sheet
            Sheet sheet = w.getSheet(0);
            this.fileContent = new Object[sheet.getRows()][sheet.getColumns()];
            // Loop over first 10 column and lines

            for (int i = 0; i < sheet.getColumns(); i++) {
                Cell cell1 = sheet.getCell(i, 0);
                columnNames.add(cell1.getContents());
            }

            // hier kann man noch die Columns auf Richtigkeit testen, wenn man den Typ
            // der Analyse kennt!
            
            for (int j = 0; j < sheet.getRows(); j++) {
                for (int i = 0; i < sheet.getColumns(); i++) {
                    Cell cell = sheet.getCell(i, j);
                    fileContent[j][i] = cell.getContents();
                }
            }
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<String> dataSheetNames() {
        List<String> ret = new ArrayList<>();
        ret.add(tableName);
        return ret;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> ret = new ArrayList<>();
        ret.add(this.columnNames);
        return ret;
    }

    @Override
    public DefaultTableModel dataToDataTableImport() {
        return new DefaultTableModel(fileContent, columnNames.toArray());
    }
    
    
}
