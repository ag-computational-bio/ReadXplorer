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


import de.cebitec.readxplorer.parser.common.ParsingException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parser for Xls table files using the jxl package.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class XlsTableParser implements TableParserI {

    private static final Logger LOG = LoggerFactory.getLogger( XlsTableParser.class.getName() );
    
    private static final String NAME = "Xls Table Parser";
    private static final String[] FILE_EXTENSIONS = new String[]{"xls", "XLS", "Xls"};
    private static final String FILE_DESCRIPTION = "Xls table";


    /**
     * Parser for Xls table files using the jxl package.
     */
    public XlsTableParser() {
    }


    /**
     * A method for parsing Xls table files using the jxl package.
     * <p>
     * @param fileToRead The file containing the table to read.
     * <p>
     * @return Table in form of a list, which contains the row lists of Objects.
     * <p>
     * @throws ParsingException
     */
    @Override
    public List<List<?>> parseTable( File fileToRead ) throws ParsingException {

        List<List<?>> tableData = new ArrayList<>();

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale( new Locale( "en", "EN" ) );
            Workbook workbook;
            workbook = Workbook.getWorkbook( fileToRead );
            boolean fstSheet = true;
            String headerStartFstSheet = "";
            for( Sheet sheet : workbook.getSheets() ) { //TODO filter stats and parametersheet
                for( int i = 0; i < sheet.getRows(); i++ ) {
                    if( fstSheet ) {
                        headerStartFstSheet = sheet.getCell( 0, 0 ).getContents();
                        fstSheet = false;
                    }
                    //only read all sheets from the beginning, which seem to contain the data = has the same header start than the first sheet
                    if( headerStartFstSheet.equals( sheet.getCell( 0, 0 ).getContents() ) ) {

                        Cell[] cells = sheet.getRow( i );
                        List<Object> rowData = new ArrayList<>();
                        for( Cell cell : cells ) {
                            String content = cell.getContents();
                            rowData.add( content );
//                        CellType type = cell.getType(); other usefule stuff, if needed
//                        CellFeatures feature = cell.getCellFeatures();
//                        CellFormat format = cell.getCellFormat();
                        }
                        tableData.add( rowData );
                    }
                }
            }
        } catch( IOException | BiffException ex ) {
            LOG.error( ex.getMessage(), ex );
        }

        return tableData;

    }


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSIONS;
    }


    @Override
    public String getInputFileDescription() {
        return FILE_DESCRIPTION;
    }


    /**
     * @return The NAME of the parser.
     */
    @Override
    public String toString() {
        return this.getName();
    }


}
