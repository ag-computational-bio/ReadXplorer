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


import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.tools.snpdetection.AnalysisSNPs;
import de.cebitec.readxplorer.tools.snpdetection.ParameterSetSNPs;
import de.cebitec.readxplorer.tools.snpdetection.SnpDetectionResult;
import de.cebitec.readxplorer.utils.UrlWithTitle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.netbeans.api.sendopts.CommandException;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class SNPAnalysisCallable extends AnalysisCallable {

    private static final Logger LOG = Logger.getLogger( SNPAnalysisCallable.class.getName() );

    private static final String TABLE_DOUBLE = "DOUBLE";
    private static final String TABLE_FLOAT = "FLOAT";
    private static final String TABLE_INTEGER = "INTEGER";
    private static final String TABLE_LABEL = "LABEL";
    private static final String TABLE_URL_W_TITLE = "LINK";
    private static final String TABLE_STRING = "STRING";
    private static final String UNKNOWN = "UNKNOWN";

    private final PersistentTrack persistentTrack;
    private final ParameterSetSNPs parameterSet;


    public SNPAnalysisCallable( boolean verbosity, PersistentTrack persistentTrack, ParameterSetSNPs parameterSet ) {

        super( verbosity, "SNP" );

        this.persistentTrack = persistentTrack;
        this.parameterSet    = parameterSet;

    }


    @Override
    public AnalysisResult call() throws CommandException {

        try {

            File trackFile = new File( persistentTrack.getFilePath() );
            final String trackFileName = trackFile.getName();

            LOG.log( FINE, "start SNP analysis for {0}...", trackFileName );
            result.addOutput( "start analysis..." );
            TrackConnector trackConnector = ProjectConnector.getInstance().getTrackConnector( persistentTrack );
            AnalysisSNPs analysisSNPs = new AnalysisSNPs( trackConnector, parameterSet );
            ThreadingHelper threadingHelper = new ThreadingHelper();
                threadingHelper.start();
            AnalysesHandler analysisHandler = new AnalysesHandler( trackConnector, threadingHelper, "", parameterSet.getReadClassParams() );
                analysisHandler.registerObserver( analysisSNPs );
                analysisHandler.setCoverageNeeded( true );
                analysisHandler.setDiffsAndGapsNeeded( true );
                analysisHandler.startAnalysis();

            threadingHelper.join(); // blocks until analysisHandler finishes its job
            Map<Integer,PersistentTrack> trackMap = new HashMap<>();
                trackMap.put( persistentTrack.getId(), persistentTrack );
            PersistentReference reference = ProjectConnector.getInstance().getRefGenomeConnector( persistentTrack.getRefGenID() ).getRefGenome();
            final SnpDetectionResult snpDetectionResult = new SnpDetectionResult( analysisSNPs.getResults(),
                                                                          trackMap, reference, false, 2, 0 );
            snpDetectionResult.setParameters( parameterSet );


            LOG.log( FINE, "store SNP results for {0}...", trackFileName );
            result.addOutput( "store results..." );
            File resultFile = new File( "snp-result-" + trackFileName + ".xls" );
            writeFile( resultFile, snpDetectionResult.dataSheetNames(), snpDetectionResult.dataColumnDescriptions(), snpDetectionResult.dataToExcelExportList() );

            result.setResultFile( resultFile );

        } catch( IOException | WriteException | InterruptedException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            result.addOutput( "Error: " + ex.getMessage() );
        } catch( OutOfMemoryError ome ) {
            LOG.log( SEVERE, ome.getMessage(), ome );
            CommandException ce = new CommandException( 1, "ran out of memory!" );
            ce.initCause( ome );
            throw ce;
        }

        return result;

    }



    private void writeFile( File file, List<String> sheetNames, List<List<String>> headers, List<List<List<Object>>> exportData ) throws FileNotFoundException, IOException, WriteException, OutOfMemoryError {

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
                    dataLeft = fillSheet( sheet, sheetData, headerRow );
                }
            }
        }
        workbook.write();
        workbook.close();

    }


    private static boolean fillSheet( WritableSheet sheet, List<List<Object>> sheetData, List<String> headerRow ) throws OutOfMemoryError, WriteException {

        boolean dataLeft = false;
        int row = 0;
        int column = 0;

        for( String header : headerRow ) {
            addColumn( sheet, TABLE_LABEL, header, column++, row );
        }
        row++;

        for( List<Object> exportRow : sheetData ) {

            column = 0;
            for( Object entry : exportRow ) {
                String objectType = getObjectType( entry );
                try {
                    addColumn( sheet, objectType, entry, column++, row );
                } catch( RowsExceededException e ) {
                    dataLeft = true;
                    break;
                }
            }
            if( dataLeft ) {
                break;
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


    private static void addColumn( WritableSheet sheet, String celltype, Object cellvalue, int column, int row ) throws WriteException, OutOfMemoryError {
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
            jxl.write.Number number = new jxl.write.Number( column, row, value, integerFormat );
            sheet.addCell( number );

        } else if( celltype.equals( TABLE_DOUBLE ) ) {
            Double value = Double.parseDouble( cellvalue.toString() );
            jxl.write.Number number = new jxl.write.Number( column, row, value );
            sheet.addCell( number );

        } else if( celltype.equals( TABLE_FLOAT ) ) {
            WritableCellFormat integerFormat = new WritableCellFormat( NumberFormats.FLOAT );
            Float value = Float.parseFloat( cellvalue.toString() );
            jxl.write.Number number = new jxl.write.Number( column, row, value, integerFormat );
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




    private class ThreadingHelper extends Thread implements DataVisualisationI {

        private boolean keepRunning = true;


        ThreadingHelper() {
            setDaemon( true );
        }


        @Override
        public void run() {

            while( keepRunning ) {
                try {
                    sleep( 100 );
                } catch( InterruptedException e ) {
                }
            }

        }


        @Override
        public void showData( Object data ) {
            keepRunning = false;
        }

    }


}
