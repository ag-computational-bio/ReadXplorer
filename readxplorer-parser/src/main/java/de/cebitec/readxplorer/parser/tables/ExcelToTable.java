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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


/**
 * This class includes a method that extracts the data from the sheets in the
 * passed excel file.
 *
 * @author jritter
 */
public class ExcelToTable implements ExcelImportDataI {

    private static final Logger LOG = Logger.getLogger( ExcelToTable.class.getName() );

    private String tableName;
    private final List<String> columnNames;
    private Object[][] fileContentFirstSheet;
    private HashMap<String, String> fileContentSecondSheet;
    private HashMap<String, String> fileContentSecondSheetThirdColumn;


    /**
     * Constructor for this class, which needs an excel file and a
     * ProgressHandle.
     *
     * @param file excel file.
     * @param handle ProgressHandle
     * <p>
     * @throws IOException
     */
    public ExcelToTable( File file ) throws IOException {
        this.columnNames = new ArrayList<>();
        fetchSheetTableAndParameters( file );
    }


    /**
     * This method extracts the data from the sheets in the given excel file.
     *
     * @param file excel file.
     * @param handle ProgressHandle
     * <p>
     * @throws IOException
     */
    private void fetchSheetTableAndParameters( File file ) throws IOException {
        File inputWorkbook = file;
        Workbook w;
        try {
            w = Workbook.getWorkbook( inputWorkbook );

            // Get the first sheet
            Sheet sheet = w.getSheet( 0 );

            this.fileContentFirstSheet = new Object[sheet.getRows()][sheet.getColumns()];

            // Loop over first 10 column and lines
            for( int i = 0; i < sheet.getColumns(); i++ ) {
                Cell cell1 = sheet.getCell( i, 0 );
                columnNames.add( cell1.getContents() );
            }

            // hier kann man noch die Columns auf Richtigkeit testen, wenn man den Typ
            // der Analyse kennt!
            //handle.progress("Read first sheed with table content ... ", 6);
            for( int j = 0; j < sheet.getRows(); j++ ) {
//                handle.progress("Read lines ... ", j);
                for( int i = 0; i < sheet.getColumns(); i++ ) {
                    Cell cell = sheet.getCell( i, j );
                    fileContentFirstSheet[j][i] = cell.getContents();
                }
            }

            // read second Sheet!
            sheet = w.getSheet( 1 );
            //handle.progress("Read first sheed with table content ... ", 9);
            fileContentSecondSheet = new HashMap<>();
            fileContentSecondSheetThirdColumn = new HashMap<>();
            for( int j = 0; j < sheet.getRows(); j++ ) {
                Cell cellX = sheet.getCell( 0, j );
                Cell cellY = sheet.getCell( 1, j );
                Cell cellZ = sheet.getCell( 2, j );
                fileContentSecondSheet.put( cellX.getContents(), cellY.getContents() );
                fileContentSecondSheetThirdColumn.put( cellX.getContents(), cellZ.getContents() );
            }

            w.close();
        } catch( BiffException e ) {
            LOG.log( Level.WARNING, e.getMessage(), e );
        }

    }


    @Override
    public DefaultTableModel dataToDataTableImport() {
        return new DefaultTableModel( fileContentFirstSheet, columnNames.toArray() );
    }


    /**
     * Return the Information from second Sheed in excel file, which consists of
     * only two sheets. The second sheet consists of two columns.
     *
     * @return HashMap<firstColumn Entry, secondColumn Entry>
     */
    public HashMap<String, String> getSecondSheetData() {
        return this.fileContentSecondSheet;
    }


    public HashMap<String, String> getSecondSheetDataThirdColumn() {
        return this.fileContentSecondSheetThirdColumn;
    }


}
