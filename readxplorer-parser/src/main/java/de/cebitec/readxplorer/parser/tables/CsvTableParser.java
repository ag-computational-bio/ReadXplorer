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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.util.Exceptions;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import static java.util.logging.Level.INFO;


/**
 * A parser for parsing CSV files.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CsvTableParser implements CsvParserI {

    private static final Logger LOG = Logger.getLogger( CsvTableParser.class.getName() );

    private static final String NAME = "CSV Table Parser";
    private static final String[] FILE_EXTENSIONS = new String[]{"csv", "CSV"};
    private static final String FILE_DESCRIPTION = "CSV table";

    private boolean autoDelimiter;
    private CsvPreference csvPref;
    //different CellProcessors for different tables
    public static final CellProcessor[] DEFAULT_TABLE_PROCESSOR = new CellProcessor[0];
    public static final CellProcessor[] POS_TABLE_PROCESSOR = new CellProcessor[]{new ParseInt()};

    private CellProcessor[] tableProcessor;
    private TableType tableModel;


    public CsvTableParser() {
        this.autoDelimiter = true;
        this.csvPref = null;
    }


    /**
     * A method for parsing CSV files in any of the four available formats
     * supported by the @see CsvPreference class.
     *
     * @see CsvPreference
     * @param fileToRead The file containing the table to read.
     * <p>
     * @return Table in form of a list, which contains the row lists of Objects.
     */
    @Override
    public List<List<?>> parseTable( File fileToRead ) throws ParsingException {

        List<List<?>> tableData = null;

        if( autoDelimiter ) {

            //try all available csv preferences
            List<CsvPreference> csvPreferences = new ArrayList<>( 5 );
            csvPreferences.add( CsvPreference.STANDARD_PREFERENCE );
            csvPreferences.add( CsvPreference.EXCEL_PREFERENCE );
            csvPreferences.add( CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE );
            csvPreferences.add( CsvPreference.TAB_PREFERENCE );

            for( CsvPreference pref : csvPreferences ) {

                tableData = this.parseTable( fileToRead, pref );
                if( tableData != null ) {
                    LOG.log( INFO, "Entry delimiter used for this table is: {0}", (char) pref.getDelimiterChar() );
                    break;
                }
            }

            if( tableData == null ) {
                throw new ParsingException( "Table is not in a readable format and cannot be imported. Use a valid CSV format!" );
            }

        } else {
            tableData = this.parseTable( fileToRead, csvPref );

            if( tableData == null ) {
                throw new ParsingException( "Table is not in a readable format and cannot be imported.\n"
                        + "Either choose the correct delimiter and line end characters or try autodetection of delimiter and line end character!" );
            }
        }

        return tableData;
    }


    /**
     * Method for parsing a CSV file for a given csv preference.
     *
     * @param fileToRead The file containing the table to read.
     * @param csvPreference The CsvPreference to use for parsing.
     * <p>
     * @return Table in form of a list, which contains the row lists of Objects.
     * @throws de.cebitec.readxplorer.parser.common.ParsingException
     */
    public List<List<?>> parseTable( File fileToRead, CsvPreference csvPreference ) throws ParsingException {

        List<List<?>> tableData = new ArrayList<>();
        try( ICsvListReader listReader = new CsvListReader( new FileReader( fileToRead ), csvPreference ); ) { //Preference could be parsed as option

            final String[] header = listReader.getHeader( true );
            tableData.add( Arrays.asList( header ) );

            CellProcessor[] generalProcessors;
            if( tableModel == TableType.COVERAGE_ANALYSIS
                    || tableModel == TableType.POS_TABLE
                    || tableModel == TableType.SNP_DETECTION
                    || tableModel == TableType.TSS_DETECTION ) {
                generalProcessors = POS_TABLE_PROCESSOR;
            } else if( tableModel == TableType.TSS_DETECTION_JR ) {
                generalProcessors = getTssCellProcessor();
            } else if( tableModel == TableType.OPERON_DETECTION_JR ) {
                generalProcessors = getOperonCellProcessor();
            } else if( tableModel == TableType.RPKM_ANALYSIS_JR ) {
                generalProcessors = getRpkmCellProcessor();
            } else if( tableModel == TableType.NOVEL_TRANSCRIPT_DETECTION_JR ) {
                generalProcessors = getNovelTranscriptCellProcessor();
            } else if( tableModel == TableType.STATS_TABLE ) {
                generalProcessors = this.getStatsProcessor();
            } else {
                generalProcessors = DEFAULT_TABLE_PROCESSOR;
            }

            while( listReader.read() != null ) {
                int length = listReader.length();
                if( length > 0 ) {
                    CellProcessor[] processors = generalProcessors.clone();
                    int numProcessorsToAdd = length - processors.length;
                    if( numProcessorsToAdd >= 0 ) {
                        processors = ArrayUtils.addAll( processors, new CellProcessor[numProcessorsToAdd] );
                        List<Object> rowData = listReader.executeProcessors( processors );
                        tableData.add( rowData );
                    } else {
                        throw new ParsingException( "It seems that the wrong delimiter or table format has been chosen. "
                                + "The number of columns (" + length + ") in a row does not correspond to the expected number of columns (" + processors.length + ")!" );
                    }
                }
            }
        } catch( FileNotFoundException ex ) {
            Exceptions.printStackTrace( ex );
        } catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        } catch( SuperCsvException ex ) {
            tableData = null;
        }

        return tableData;

    }


    public void setTableModel( TableType tableModel ) {
        this.tableModel = tableModel;
    }


    /**
     * @param autoDelimiter <code>true</code>, if the delimiter shall be
     * detected automatically, <code>false</code>, if the delimiter was selected
     * by the user.
     */
    @Override
    public void setAutoDelimiter( boolean autoDelimiter ) {
        this.autoDelimiter = autoDelimiter;
    }


    /**
     * @param csvPref The currently selected CsvPreference.
     */
    @Override
    public void setCsvPref( CsvPreference csvPref ) {
        this.csvPref = csvPref;
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


    @Override
    public void setCellProscessors( CellProcessor[] cellProcessors ) {
        tableProcessor = cellProcessors;
    }


    /**
     * Generates a cellprocessor for the tss analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getTssCellProcessor() {

        return new CellProcessor[]{
            new ParseInt(), // Position
            null, // Strand
            null, // Comment
            new ParseInt(), // Read Starts
            //            new ParseDouble(), // Rel. Count
            null, // Rel. Count
            null, // Feature Name
            null, // Feature Locus
            null, // Offset, may be '-' instead of integer
            null, // Dist. To Start, may be '-' instead of integer
            null, // Dist. To Stop, may be '-' instead of integer
            null, // Sequence
            new ParseBool(), // Leaderless
            new ParseBool(), // Putative TLS-Shift
            new ParseBool(), // Intragenic TSS
            new ParseBool(), // Intergenic TSS
            new ParseBool(), // Putative Antisense
            new ParseBool(), // Putative 5'-UTR Antisense
            new ParseBool(), // Putative 3'-UTR Antisense
            new ParseBool(), // Putative Intragenic Antisense
            new ParseBool(), // Assigned To Stable RNA
            new ParseBool(), // False Positive
            new ParseBool(), // Selected For Upstream Region Analysis
            new ParseBool(), // Finished
            null, // Gene Start, may be '-' instead of integer
            null, // Gene Stop, may be '-' instead of integer
            null, // Gene Length In Bp, may be '-' instead of integer
            null, // Frame, may be '-' instead of integer
            null, // Gene Product
            null, // Start Codon
            null, // Stop Codon
            null, // Chromosome
            new ParseInt(), // Chrom ID
            new ParseInt() //Track ID
        };
    }


    /**
     * Generates a cellprocessor for the operon analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getOperonCellProcessor() {
        return new CellProcessor[]{
            new ParseInt(), // Putative Operon Transcript Begin
            null, // Feature1
            null, // Feature2
            null, // Strand
            null, // Start annotation 1
            null, // Start annotation 2
            new ParseBool(), // false positive
            new ParseBool(), // marked as finish observed
            null, // Spanning reads
            null, // Operon String
            new ParseInt(), // Number of Genes
            null, // Chromosome NAME
            new ParseInt(), // Chromosome id
            null, // Track NAME
            new ParseInt(), // Track id
        };
    }


    /**
     * Generates a cellprocessor for the RPKM analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getRpkmCellProcessor() {
        return new CellProcessor[]{
            null, // Feature
            null, // Feature Type
            new ParseInt(), // Start
            new ParseInt(), // Stop
            new ParseInt(), // Feature length
            null, // Strand
            new ParseInt(), // Longest Detected 5'-UTR Length
            null, // RPKM
            null, // Log-RPKM
            new ParseInt(), // Mapped Total
            null, // Chromosome
            new ParseInt(), // Chromosome id
            null,
            new ParseInt(), // Track id
        };
    }


    /**
     * Generates a cellprocessor for the novel transcript analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getNovelTranscriptCellProcessor() {
        return new CellProcessor[]{
            new ParseInt(), // Putative start position
            null, // Strand
            new ParseBool(), // False positive
            new ParseBool(), // Selected for Blast export
            new ParseBool(), // markd as finished
            null, // Site
            new ParseInt(), // Coverage Dropoff
            new ParseInt(), // Length in BP
            null, // Sequence
            null, // Chromosome NAME
            new ParseInt(), // Chromosome id
            null, // Track NAME
            new ParseInt(), // Track id
        };
    }


    /**
     * Generates a cellprocessor for the novel transcript analysis result table.
     *
     * @return List of CellProcessors.
     */
    private CellProcessor[] getStatsProcessor() {
        return new CellProcessor[]{
            null,
            null,
            null
        };
    }


}
