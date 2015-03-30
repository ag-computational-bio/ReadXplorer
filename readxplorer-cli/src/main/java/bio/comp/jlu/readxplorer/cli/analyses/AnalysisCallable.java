/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.cli.analyses;


import bio.comp.jlu.readxplorer.cli.analyses.AnalysisCallable.AnalysisResult;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.utils.UrlWithTitle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static java.lang.Thread.sleep;
import static java.util.logging.Level.SEVERE;


/**
 * Base Class for all Analysis Implementations.
 * Provides common objects and logic for all CLI analysis implementations.
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public abstract class AnalysisCallable implements Callable<AnalysisResult> {

    private static final Logger LOG = Logger.getLogger( AnalysisCallable.class.getName() );

    private static final String TABLE_DOUBLE = "DOUBLE";
    private static final String TABLE_FLOAT = "FLOAT";
    private static final String TABLE_INTEGER = "INTEGER";
    private static final String TABLE_LABEL = "LABEL";
    private static final String TABLE_URL_W_TITLE = "LINK";
    private static final String TABLE_STRING = "STRING";
    private static final String UNKNOWN = "UNKNOWN";

    protected final boolean verbosity;

    protected final AnalysisResult result;


    /**
     * Super Constructor for all Analysis Implementations.
     *
     * @param verbosity is verbosity required?
     * @param anaylsis type of analysis
     */
    protected AnalysisCallable( boolean verbosity, String anaylsis ) {

        this.verbosity = verbosity;
        this.result = new AnalysisResult( anaylsis );

    }


    public final class AnalysisResult {

        private final String analysisType;
        private final List<String> output;
        private File resultFile;


        private AnalysisResult( String analysisType ) {
            this.analysisType = analysisType;
            this.output = new ArrayList<>( 5 );
        }


        public String getType() {
            return analysisType;
        }


        protected void addOutput( String msg ) {
            output.add( msg );
        }

        public List<String> getOutput() {
            return output;
        }


        public File getResultFile() {
            return resultFile;
        }


        protected void setResultFile( File resultFile ) {
            this.resultFile = resultFile;
        }

    }


    protected final class ThreadingHelper extends Thread implements DataVisualisationI {

        private boolean keepRunning = true;


        protected ThreadingHelper() {
            setDaemon( true );
        }


        @Override
        public void run() {

            while( keepRunning ) {
                try {
                    sleep( 100 );
                } catch( InterruptedException ie ) {
                    LOG.log( SEVERE, ie.getMessage(), ie );
                }
            }

        }


        @Override
        public void showData( Object data ) {
            keepRunning = false;
        }


    }





    protected static final void writeFile( File file, List<String> sheetNames, List<List<String>> headers, List<List<List<Object>>> exportData ) throws FileNotFoundException, IOException, WriteException, OutOfMemoryError {

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale( new Locale( "en", "EN" ) );

        WritableWorkbook workbook = Workbook.createWorkbook( file, wbSettings );
        WritableSheet sheet = null;
        int totalPage = 0;
        for( int i = 0; i < exportData.size(); i++ ) {
            String sheetName = sheetNames.get( i );
            List<List<Object>> sheetData = exportData.get( i );
            List<String> headerRow = headers.get( i );
            boolean dataLeft = true;
            int currentPage = 0;
            while( dataLeft ) { //only 65536 rows allowed per sheet in xls format
                if( !sheetData.isEmpty() ) {
                    if( currentPage > 0 ) {
                        sheetName += "I";
                        currentPage++;
                    }
                    sheet = workbook.createSheet( sheetName, totalPage );
                    totalPage++;
                }

                if( sheet != null ) {
                    dataLeft = fillSheet( sheet, sheetData, headerRow );
                }
            }
        }
        workbook.write();
        workbook.close();

    }


    private static final boolean fillSheet( WritableSheet sheet, List<List<Object>> sheetData, List<String> headerRow ) throws OutOfMemoryError, WriteException {

        boolean dataLeft = false;
        int row = 0;
        int col = 0;

        for( String header : headerRow ) {
            addColumn( sheet, TABLE_LABEL, header, col, row );
            col++;
        }
        row++;

        for( List<Object> exportRow : sheetData ) {
            col = 0;
            for( Object entry : exportRow ) {
                String objectType = getObjectType( entry );
                try {
                    addColumn( sheet, objectType, entry, col, row );
                    col++;
                } catch( RowsExceededException e ) {
                    dataLeft = true;
                    break;
                }
            }
            if( dataLeft ) {
                break;
            }
            row++;
        }

        if( dataLeft ) {
            for( int i = 0; i < row; i++ ) {
                sheetData.remove( 0 );
            }
        }

        return dataLeft;

    }


    private static final String getObjectType( Object entry ) {

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


    private static final void addColumn( WritableSheet sheet, String celltype, Object cellvalue, int column, int row ) throws WriteException, OutOfMemoryError {

        if( cellvalue == null ) {
            sheet.addCell( new Label( column, row, "n/a" ) );
        } else {
            switch( celltype ) {
                case TABLE_LABEL:
                    WritableCellFormat header = new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 10, WritableFont.BOLD ) );
                    Label label = new Label( column, row, (String) cellvalue, header );
                    sheet.addCell( label );
                    break;
                case TABLE_STRING:
                    cellvalue = cellvalue instanceof Character ? String.valueOf( cellvalue ) : cellvalue;
                    cellvalue = cellvalue instanceof CharSequence ? String.valueOf( cellvalue ) : cellvalue;
                    cellvalue = cellvalue instanceof Double ? cellvalue.toString() : cellvalue;
                    sheet.addCell( new Label( column, row, (String) cellvalue, new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 10 ) ) ) );
                    break;
                case TABLE_INTEGER:
                    sheet.addCell( new jxl.write.Number( column, row, Integer.parseInt( cellvalue.toString() ), new WritableCellFormat( NumberFormats.INTEGER ) ) );
                    break;
                case TABLE_DOUBLE:
                    sheet.addCell( new jxl.write.Number( column, row, Double.parseDouble( cellvalue.toString() ) ) );
                    break;
                case TABLE_FLOAT:
                    sheet.addCell( new jxl.write.Number( column, row, Float.parseFloat( cellvalue.toString() ), new WritableCellFormat( NumberFormats.FLOAT ) ) );
                    break;
                case TABLE_URL_W_TITLE:
                    UrlWithTitle titleUrl = (UrlWithTitle) cellvalue;
                    WritableHyperlink link = new WritableHyperlink( column, row, titleUrl.getUrl() );
                    link.setDescription( titleUrl.getTitle() );
                    sheet.addHyperlink( link );
                    break;
                case UNKNOWN:
                    sheet.addCell( new Label( column, row, cellvalue.toString(), new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 10 ) ) ) );
                    break;
                default:
                    break;
            }
        }

    }


}
