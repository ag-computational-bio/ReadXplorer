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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import jxl.write.WriteException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;


/**
 * General csv exporter. Since csv does not support multiple sheets, data for
 * multiple sheets is written to multiple files appended with a file number and
 * the sheet name.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CsvExporter implements TableExporterI {

    private static final Logger LOG = LoggerFactory.getLogger( CsvExporter.class.getName() );

    private List<List<List<Object>>> exportData;
    private List<List<String>> headers;
    private int fileCount = 0;
    private List<String> sheetNames;
    private final ProgressHandle progressHandle;
    private int rowNumberGlobal;


    /**
     * General csv exporter. Since csv does not support multiple sheets, data
     * for multiple sheets is written to multiple files appended with a file
     * number and the sheet name. All 3 data fields have to be set in order to
     * start a successful export.
     * <p>
     * @param progressHandle the progress handle which should display the
     *                       progress of the CsvExporter
     */
    public CsvExporter( ProgressHandle progressHandle ) {
        this.progressHandle = progressHandle;
        this.rowNumberGlobal = 0;
    }


    /**
     * @param headers the header list for the tables which should be exported to
     *                the csv file. Must be set!
     */
    @Override
    public void setHeaders( List<List<String>> headers ) {
        this.headers = headers;
    }


    /**
     * @param dataSheetNames the sheet name listf for the sheets which should be
     *                       exported to the csv file. Must be set!
     */
    @Override
    public void setSheetNames( List<String> dataSheetNames ) {
        this.sheetNames = dataSheetNames;
    }


    /**
     * @param exportData The list of data which should be exported to the csv
     *                   file. Must be set!
     */
    @Override
    public void setExportData( List<List<List<Object>>> exportData ) {
        this.exportData = exportData;
    }


    /**
     * Carries out the whole export process to a csv file.
     * <p>
     * @param file the file in which to write the data.
     * <p>
     * @return the base file to which the data was written.
     * <p>
     * @throws FileNotFoundException
     * @throws IOException
     * @throws WriteException
     * @throws OutOfMemoryError
     */
    @NbBundle.Messages( { "CsvExporterSuccessMsg=Csv exporter stored data successfully: ",
                          "CsvExporterSuccessHeader=Information Message" } )
    @Override
    public File writeFile( File file ) throws FileNotFoundException, IOException, OutOfMemoryError, WriteException {

        LOG.info( "Starting to write csv file...{0}", file.getAbsolutePath() );

        String[] header = new String[0];

        for( int i = 0; i < exportData.size(); ++i ) {
            try( CsvListWriter csvWriter = new CsvListWriter( new FileWriter( this.createOutputFile( file, sheetNames.get( i ) ) ), CsvPreference.TAB_PREFERENCE ) ) {
                this.progressHandle.progress( "Storing line", this.rowNumberGlobal++ );
                csvWriter.writeHeader( headers.get( i ).toArray( header ) );
                List<List<Object>> sheetData = exportData.get( i );

                for( List<Object> exportRow : sheetData ) {
                    csvWriter.write( exportRow );
                    if( this.rowNumberGlobal++ % 100 == 0 ) {
                        this.progressHandle.progress( "Storing line", this.rowNumberGlobal );
                    }
                }
            }
        }

        NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(), Bundle.SuccessMsg() + sheetNames.get( 0 ), null );

        LOG.info( "Finished writing csv file!" );

//        int currentPage = 0;
//        int totalPage = 0;
//        boolean dataLeft;
//        String sheetName;
//        List<List<Object>> sheetData;
//        List<String> headerRow;
//
//        for (int i = 0; i < exportData.size(); ++i) {
//            sheetName = sheetNames.get(i);
//            sheetData = exportData.get(i);
//            dataLeft = true;
//            currentPage = 0;
//            while (dataLeft) { //only 65536 rows allowed per sheet in xls format
//                if (!sheetData.isEmpty()) {
//                    if (currentPage++ > 0) {
//                        sheetName += "I";
//                    }
//                    sheet = workbook.createSheet(sheetName, totalPage++);
//                }
//
//                if (sheet != null) {
//                    dataLeft = this.fillSheet(sheet, sheetData, headerRow);
//                }
//            }
//        }

        return file;
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


    /**
     * If more than one file are written, this method appends
     * "_fileCount_dataSheetName" to the end of the file name.
     * <p>
     * @param file          the original file to store
     * @param dataSheetName The name of the data sheet, will also be added to
     *                      the file name
     * <p>
     * @return The new file including the file number, if more than one file are
     *         stored
     */
    private File createOutputFile( File file, String dataSheetName ) {
        File newFile = file;
        if( fileCount++ > 0 ) {
            String newPath = file.getAbsolutePath().substring( 0, file.getAbsolutePath().length() - 4 );
            newPath += "_" + fileCount + "_" + dataSheetName + ".csv";
            newFile = new File( newPath );
        }
        return newFile;
    }


}
