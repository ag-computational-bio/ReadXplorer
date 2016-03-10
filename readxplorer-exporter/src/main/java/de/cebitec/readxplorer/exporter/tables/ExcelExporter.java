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

package de.cebitec.readxplorer.exporter.tables;


import de.cebitec.readxplorer.utils.UrlWithTitle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * General excel exporter. It supports even multiple sheets in one document.
 *
 * @author -Rolf Hilker-
 */
public class ExcelExporter implements TableExporterI {

    private static final Logger LOG = LoggerFactory.getLogger( ExcelExporter.class.getName() );

    private static final String TABLE_DOUBLE = "DOUBLE";
    private static final String TABLE_FLOAT = "FLOAT";
    private static final String TABLE_INTEGER = "INTEGER";
    private static final String TABLE_LABEL = "LABEL";
    private static final String TABLE_URL_W_TITLE = "LINK";
    private static final String TABLE_STRING = "STRING";
    private static final String UNKNOWN = "UNKNOWN";


    private final ProgressHandle progressHandle;
    private List<String> sheetNames; //contains all sheet names
    private List<List<String>> headers; //contains all headers
    /**
     * Inner list contains data of one row, middle list contains all rows, outer
     * list is the list of sheets.
     */
    private List<List<List<Object>>> exportData;
    private int rowNumberGlobal;


    /**
     * General excel exporter. It supports even multiple sheets in one document.
     * All 3 data fields have to be set in order to start a successful export.
     * <p>
     * @param progressHandle the progress handle which should display the
     *                       progress of the ExcelExporter
     */
    public ExcelExporter( ProgressHandle progressHandle ) {
        this.progressHandle = progressHandle;
        this.rowNumberGlobal = 0;
    }


    /**
     * @param dataSheetNames the sheet name listf for the sheets which should be
     *                       exported to the excel file. Must be set!
     */
    @Override
    public void setSheetNames( List<String> dataSheetNames ) {
        this.sheetNames = dataSheetNames;
    }


    /**
     * @param headers the header list for the tables which should be exported to
     *                the excel file. Must be set!
     */
    @Override
    public void setHeaders( List<List<String>> headers ) {
        this.headers = headers;
    }


    /**
     * @param exportData The list of data which should be exported to excel.
     *                   Must be set!
     */
    @Override
    public void setExportData( List<List<List<Object>>> exportData ) {
        this.exportData = exportData;
    }


    /**
     * Carries out the whole export process to an excel file.
     * <p>
     * @param file the file in which to write the data.
     * <p>
     * @return the file in which the data was written.
     * <p>
     * @throws FileNotFoundException
     * @throws IOException
     * @throws WriteException
     * @throws OutOfMemoryError
     */
    @NbBundle.Messages( { "SuccessMsg=Excel exporter stored data successfully: ",
                          "SuccessHeader=Success" } )
    @Override
    public File writeFile( File file ) throws FileNotFoundException, IOException, WriteException, OutOfMemoryError {

        LOG.info( "Starting to write Excel file...{0}", file.getAbsolutePath() );

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale( new Locale( "en", "EN" ) );

        WritableWorkbook workbook = Workbook.createWorkbook( file, wbSettings );
        WritableSheet sheet = null;
        int totalPage = 0;
        for( int i = 0; i < exportData.size(); ++i ) {
            String sheetName = sheetNames.get( i );
            List<List<Object>> sheetData = exportData.get( i );
            List<String> headerRow = headers.get( i );
            boolean dataLeft = true;
            int currentPage = 0;
            while( dataLeft ) { //only 65536 rows allowed per sheet in xls format
                if( !sheetData.isEmpty() ) {
                    if( currentPage++ > 0 ) {
                        sheetName += "I";
                    }
                    sheet = workbook.createSheet( sheetName, totalPage++ );
                }

                if( sheet != null ) {
                    dataLeft = this.fillSheet( sheet, sheetData, headerRow );
                }
            }
        }
        workbook.write();
        workbook.close();

        NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(), Bundle.SuccessMsg() + sheetNames.get( 0 ), null );

        LOG.info( "Finished writing Excel file!" );

        return file;
    }


    /**
     * This method actually fills the given excel sheet with the data handed
     * over to this ExcelExporter.
     * <p>
     * @param sheet     the sheet to write the data to
     * @param sheetData the data to write in this sheet
     * @param headerRow the header to use for this sheet
     * <p>
     * @return dataLeft: false, if the sheet could store all data, true, if
     *         there is too much data for one sheet
     * <p>
     * @throws OutOfMemoryError
     * @throws WriteException
     */
    public boolean fillSheet( WritableSheet sheet, List<List<Object>> sheetData, List<String> headerRow ) throws OutOfMemoryError, WriteException {

        boolean dataLeft = false;
        int row = 0;
        int column = 0;

        for( String header : headerRow ) {
            ExcelExporter.addColumn( sheet, TABLE_LABEL, header, column++, row );
        }
        row++;
        this.progressHandle.progress( "Storing line", this.rowNumberGlobal++ );

        for( List<Object> exportRow : sheetData ) {

            column = 0;
            for( Object entry : exportRow ) {
                String objectType = getObjectType( entry );
                try {
                    ExcelExporter.addColumn( sheet, objectType, entry, column++, row );
                } catch( RowsExceededException e ) {
                    dataLeft = true;
                    break;
                }
            }
            if( dataLeft ) {
                break;
            }
            if( this.rowNumberGlobal++ % 100 == 0 ) {
                this.progressHandle.progress( "Storing line", this.rowNumberGlobal );
            }
            ++row;
        }

        if( dataLeft ) {
            for( int i = 0; i < row; ++i ) {
                sheetData.remove( 0 );
            }
        }

        return dataLeft;
    }


    /**
     * @param entry the entry whose object type needs to be checked
     * <p>
     * @return The string representing the object type of the entry. Among
     *         {@link #TABLE_STRING} and all other constants defined above.
     */
    private static String getObjectType( Object entry ) {
        if( entry instanceof Integer || entry instanceof Byte || entry instanceof Long ) {
            return TABLE_INTEGER;
        } else if( entry instanceof String || entry instanceof Character || entry instanceof CharSequence ) {
            return TABLE_STRING;
        } else if( entry instanceof Double ) {
            return TABLE_DOUBLE;
        } else if( entry instanceof Float ) {
            return TABLE_FLOAT;
        } else if( entry instanceof UrlWithTitle ) {
            return TABLE_URL_W_TITLE;
        } else {
            return UNKNOWN;
        }
    }


    /**
     * Writes a single column in a given excel sheet.
     * <p>
     * @param sheet     the sheet to write to
     * @param celltype  the celltype of the cell to write
     * @param cellvalue the value to be written in the column
     * @param column    column number
     * @param row       row number
     * <p>
     * @throws WriteException
     * @throws OutOfMemoryError
     */
    public static void addColumn( WritableSheet sheet, String celltype, Object cellvalue, int column, int row ) throws WriteException, OutOfMemoryError {
        WritableFont arialbold = new WritableFont( WritableFont.ARIAL, 10, WritableFont.BOLD );
        WritableFont arial = new WritableFont( WritableFont.ARIAL, 10 );
        if( cellvalue == null ) {
            Label label = new Label( column, row, "n/a" );
            sheet.addCell( label );

        } else if( celltype.equals( TABLE_LABEL ) ) {
            WritableCellFormat header = new WritableCellFormat( arialbold );
            Label label = new Label( column, row, (String) cellvalue, header );
            sheet.addCell( label );

        } else if( celltype.equals( TABLE_STRING ) ) {
            cellvalue = cellvalue instanceof Character ? String.valueOf( cellvalue ) : cellvalue;
            cellvalue = cellvalue instanceof CharSequence ? String.valueOf( cellvalue ) : cellvalue;
            cellvalue = cellvalue instanceof Double ? cellvalue.toString() : cellvalue;
            WritableCellFormat string = new WritableCellFormat( arial );
            Label label = new Label( column, row, (String) cellvalue, string );
            sheet.addCell( label );

        } else if( celltype.equals( TABLE_INTEGER ) ) {
            WritableCellFormat integerFormat = new WritableCellFormat( NumberFormats.INTEGER );
            Integer value = Integer.parseInt( cellvalue.toString() );
            Number number = new Number( column, row, value, integerFormat );
            sheet.addCell( number );

        } else if( celltype.equals( TABLE_DOUBLE ) ) {
            Double value = Double.parseDouble( cellvalue.toString() );
            Number number = new Number( column, row, value );
            sheet.addCell( number );

        } else if( celltype.equals( TABLE_FLOAT ) ) {
            WritableCellFormat integerFormat = new WritableCellFormat( NumberFormats.FLOAT );
            Float value = Float.parseFloat( cellvalue.toString() );
            Number number = new Number( column, row, value, integerFormat );
            sheet.addCell( number );

        } else if( celltype.equals( TABLE_URL_W_TITLE ) ) {
            UrlWithTitle titleUrl = (UrlWithTitle) cellvalue;
            WritableHyperlink link = new WritableHyperlink( column, row, titleUrl.getUrl() );
            link.setDescription( titleUrl.getTitle() );
            sheet.addHyperlink( link );

        } else if( celltype.equals( UNKNOWN ) ) {
            WritableCellFormat string = new WritableCellFormat( arial );
            Label label = new Label( column, row, cellvalue.toString(), string );
            sheet.addCell( label );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readyToExport() {
        return this.exportData != null && !this.exportData.isEmpty() &&
                 this.headers != null && !this.headers.isEmpty() &&
                 this.sheetNames != null && !this.sheetNames.isEmpty();
    }


}
