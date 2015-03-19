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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.sf.samtools.SAMRecord;

import static java.util.regex.Pattern.compile;


/**
 * Utility methods related to GASV.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class GASVUtils {

    public static final Pattern COMMA_PATTERN = compile( "," );

    private static final Logger LOG = Logger.getLogger( GASVUtils.class.getName() );


    /**
     * Instantiation not allowed!
     */
    private GASVUtils() {
    }


    /**
     * Edits the partitioning of the GASV result table data for better viewing
     * in ReadXplorer. Editing comprises splitting of break point start and stop
     * positions in separate columns.
     * <p>
     * @param tableData GASV result table data to edit
     * <p>
     * @return The edited result table data
     */
    public static List<List<?>> editGASVResultTable( List<List<?>> tableData ) {
        final String leftBreakPoint = "LeftBreakPoint:";
        final String rightBreakPoint = "RightBreakPoint:";
        final String leftBreakPointStart = "LeftBreakPointStart:";
        final String rightBreakPointStart = "RightBreakPointStart:";
        final String leftBreakPointEnd = "LeftBreakPointEnd:";
        final String rightBreakPointEnd = "RightBreakPointEnd:";

        List<List<?>> editedTableData = new ArrayList<>();

        int leftPosColumn = 2; //this is correct for a default GASV run
        int rightPosColumn = 4; //this is correct for a default GASV run
        if( tableData.size() > 1 ) {
            List<?> headers = tableData.get( 0 );
            List<String> newHeaders = new ArrayList<>();
            for( int i = 0; i < headers.size(); i++ ) {
                if( headers.get( i ) instanceof String ) {
                    if( leftBreakPoint.equals( headers.get( i ) ) ) {
                        leftPosColumn = i;
                        newHeaders.add( leftBreakPointStart );
                        newHeaders.add( leftBreakPointEnd );
                    } else if( rightBreakPoint.equals( headers.get( i ) ) ) {
                        rightPosColumn = i;
                        newHeaders.add( rightBreakPointStart );
                        newHeaders.add( rightBreakPointEnd );
                    } else {
                        newHeaders.add( (String) headers.get( i ) );
                    }
                }
            }

            editedTableData.add( newHeaders );

            for( int i = 1; i < tableData.size(); ++i ) {
                List<?> row = tableData.get( i );
                List<Object> newRow = new ArrayList<>();

                for( int j = 0; j < row.size(); j++ ) {
                    if( j == leftPosColumn || j == rightPosColumn ) {
                        Pair<Integer, Integer> breakPointEnds = splitBreakPoint( row.get( j ) );
                        newRow.add( breakPointEnds.getFirst() );
                        newRow.add( breakPointEnds.getSecond() );
                    } else {
                        newRow.add( row.get( j ) );
                    }
                }

                editedTableData.add( newRow );
            }
        }

        return editedTableData;
    }


    /**
     * Breaks a break point String in two Integer values: The start and the end
     * of the break point.
     * <p>
     * @param breakPoint The break point to split
     * <p>
     * @return The pair of start and end positions of the break point.
     */
    private static Pair<Integer, Integer> splitBreakPoint( Object breakPoint ) throws IllegalArgumentException {
        Pair<Integer, Integer> bpPosPair = new Pair<>( -1, -1 );
        if( breakPoint instanceof String ) {
            String bpString = (String) breakPoint;
            String[] splittedBP = COMMA_PATTERN.split( bpString );
            if( splittedBP.length == 2 ) {
                boolean isValidStart = GeneralUtils.isValidIntegerInput( splittedBP[0] );
                boolean isValidEnd = GeneralUtils.isValidIntegerInput( splittedBP[1] );
                int bpStart = 1;
                int bpEnd = -1;
                if( isValidStart ) {
                    bpStart = Integer.parseInt( splittedBP[0] );
                } else {
                    LOG.info( "Breakpoint has an invalid (negative or non-integer) start position!" );
                }
                if( isValidEnd ) {
                    bpEnd = Integer.parseInt( splittedBP[1] );
                } else {
                    LOG.info( "Breakpoint has an invalid (negative or non-integer) end position!" );
                }
                bpPosPair = new Pair<>( bpStart, bpEnd );
            } else {
                throw new IllegalArgumentException(
                        "Two numbers separated by a comma are expected for a BreakPoint, but encountered a different String: " +
                        bpString );
            }
        } else {
            throw new IllegalArgumentException(
                    "Only Strings are allowed in the BreakPoint columns of GASV tables, but encountered another object: " +
                    breakPoint.getClass() );
        }
        return bpPosPair;
    }


    /**
     * Checks whether the given SamRecord belongs to either the
     * {@link MappingClass.SINGLE_PERFECT_MATCH} or
     * {@link MappingClass.SINGLE_BEST_MATCH} class. These mappings are allowed,
     * all others are not.
     * <p>
     * @param record The record to check
     * <p>
     * @return <code>true</code> if the record does NOT belong to one of the
     *         above mentioned mapping classes, <code>false</code> if it is a
     *         mapping that can be accepted for the analysis
     * <p>
     * @throws NumberFormatException
     */
    public static boolean isForbiddenMapping( SAMRecord record ) throws NumberFormatException {
        Object readClass = record.getAttribute( Properties.TAG_READ_CLASS );
        if( readClass != null ) {
            Byte classification = Byte.valueOf( readClass.toString() );
            MappingClass mappingClass = MappingClass.getFeatureType( classification );
            if( MappingClass.SINGLE_PERFECT_MATCH != mappingClass && MappingClass.SINGLE_BEST_MATCH != mappingClass ) {
                return true;
            }
        }
        return false;
    }


}
