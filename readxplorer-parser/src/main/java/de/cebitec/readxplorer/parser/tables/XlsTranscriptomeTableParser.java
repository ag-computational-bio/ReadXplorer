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
import java.util.HashMap;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.openide.util.Exceptions;


/**
 *
 * @author jritter
 */
public class XlsTranscriptomeTableParser extends XlsTableParser {

    private TableType tableType;
    private DefaultTableModel model;
    private HashMap<String, String> secondSheetMap;
    private HashMap<String, String> secondSheetMapThirdCol;


    /**
     * Parser for Xls table files using the jxl package.
     */
    public XlsTranscriptomeTableParser() {
    }


    /**
     * @param tableType
     */
    public void setTableType( TableType tableType ) {
        this.tableType = tableType;
    }


    /**
     * A method for parsing Xls table files using the jxl package. Returned list
     * might be empty. In that case, all data is stored in the structures which
     * can be obtained via the getters.
     * <p>
     * @param fileToRead The file containing the table to read.
     * <p>
     * @return Table in form of a list, which contains the row lists of Objects.
     * <p>
     * @throws ParsingException
     */
    @Override
    public List<List<?>> parseTable( File fileToRead ) throws ParsingException {

        List<List<?>> parseTable = new ArrayList<>();

        if( tableType == TableType.TSS_DETECTION_JR
                || tableType == TableType.OPERON_DETECTION_JR
                || tableType == TableType.RPKM_ANALYSIS_JR
                || tableType == TableType.NOVEL_TRANSCRIPT_DETECTION_JR ) {

            ExcelToTable exlToTable = null;
            try {
                exlToTable = new ExcelToTable( fileToRead );
            } catch( IOException ex ) {
                Exceptions.printStackTrace( ex );
            }

            this.model = exlToTable.dataToDataTableImport();
            this.secondSheetMap = exlToTable.getSecondSheetData();
            this.secondSheetMapThirdCol = exlToTable.getSecondSheetDataThirdColumn();

        } else {
            parseTable = super.parseTable( fileToRead );
        }

        return parseTable;
    }


    public DefaultTableModel getModel() {
        return model;
    }


    public HashMap<String, String> getSecondSheetMap() {
        return secondSheetMap;
    }


    public HashMap<String, String> getSecondSheetMapThirdCol() {
        return secondSheetMapThirdCol;
    }


}
