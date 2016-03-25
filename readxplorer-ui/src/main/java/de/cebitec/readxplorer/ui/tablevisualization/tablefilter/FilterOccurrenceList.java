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

package de.cebitec.readxplorer.ui.tablevisualization.tablefilter;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.ListTableModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Class for filtering occurrences.
 * <p>
 * @author Margarita Steinhauer
 */
class FilterOccurrenceList<E extends ListTableModel> {


    private final String selectedButton;
    private final int occurrenceCount;
    private E sortedTableModel;
    private final TableRightClickFilterList<E> tableRightClickFilter;
    private final E tableModel;
    private final Class<E> classType;
    private final int posColumn;
    private final int trackColumn;


    FilterOccurrenceList( String selectedButton, int occurrenceCount, TableRightClickFilterList<E> tableRightClickFilter,
                          int trackColumn, int posColumn ) {
        this.selectedButton = selectedButton;
        this.occurrenceCount = occurrenceCount;
        this.tableRightClickFilter = tableRightClickFilter;
        classType = tableRightClickFilter.getClassType();
        tableModel = (E) tableRightClickFilter.getLastTable().getModel();
        this.posColumn = posColumn;
        this.trackColumn = trackColumn;
    }


    public void filterTable() {
        sortedTableModel = sortByPosition();
        compareToTrack( sortedTableModel );
    }


    /**
     * This method sort the given table by the position column (column 0)
     * <p>
     * @return tableModel sorted by the position
     */
    public E sortByPosition() {

        /*
         * Get all rows with data (say, all rows but the one with the headers),
         * sort them based on 'position' column and return the sorted result as
         * table model.
         */
        List<List<Object>> dataVector = tableModel.getDataList();
        Collections.sort(dataVector, (List<Object> o1, List<Object> o2) -> {
            Integer pos1 = (Integer) o1.get( posColumn );
            Integer pos2 = (Integer) o2.get( posColumn );
            return pos1.compareTo( pos2 );
        });

        return tableModel;
    }


    /**
     *
     * @param sortedTableModel
     */
    public void compareToTrack( E sortedTableModel ) {
        TableFilterUtilsList<E> utils = new TableFilterUtilsList<>( classType );
        E comparedTableModel = utils.prepareNewTableModel( tableModel );
        List<List<Object>> dataRows = sortedTableModel.getDataList();

        // count unique tracks per position
        Map<String, Set<Integer>> uniqueTracksPerPosition
                = getUniqueTracksPerPosition( dataRows );

        Set<String> matchingPositions = new HashSet<>();

        for( Entry<String, Set<Integer>> positionInTracks : uniqueTracksPerPosition.entrySet() ) {
            if( (selectedButton.equals( "max" ) && positionInTracks.getValue().size() <= occurrenceCount) ||
                (selectedButton.equals( "min" ) && positionInTracks.getValue().size() >= occurrenceCount) ||
                (selectedButton.equals( "all" ) && tableRightClickFilter.getTrackMap().size() ==
                                                   positionInTracks.getValue().size()) ) {
                matchingPositions.add( positionInTracks.getKey() );
            }
        }

        for( List<Object> dataRow : dataRows ) {
            if( matchingPositions.contains( String.valueOf( dataRow.get( posColumn ) ) ) ) {
                comparedTableModel.addRow( dataRow );
            }

        }

        tableRightClickFilter.setNewTableModel( comparedTableModel );
    }


    /**
     * Creates a map with position entries as keys and matching tracks per
     * position. The tracks are within a Set, so the size of the set you get for
     * a given position is the count of different tracks the position occurs
     * in.
     * <p>
     * @param dataRows <p>
     * @return
     */
    private Map<String, Set<Integer>> getUniqueTracksPerPosition( List<List<Object>> dataRows ) {
        Map<String, Set<Integer>> uniqueTracksPerPosition = new HashMap<>();

        for( List<Object> currentRow : dataRows ) {
            String currentPosition = String.valueOf( currentRow.get( posColumn ) );
            if( !uniqueTracksPerPosition.containsKey( currentPosition ) ) {
                uniqueTracksPerPosition.put( currentPosition, new HashSet<>() );
            }
            Integer currentTrack = ((PersistentTrack) currentRow.get( trackColumn )).getId();
            uniqueTracksPerPosition.get( currentPosition ).add( currentTrack );
        }

        return uniqueTracksPerPosition;
    }


}
