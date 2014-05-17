/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering occurrences.
 *
 * @author Margarita Steinhauer
 */
class FilterOccurrence<E extends DefaultTableModel> {
    

    private final String selectedButton;
    private final int occurrenceNumber;
    private int sortedTable;
    private E sortedTableModel;
    private final TableRightClickFilter<E> tableRightClickFilter;
    private E tableModel;
    private Class<E> classType;
    private int tableColumnPosition;
    private int tableColumnTrack;

    public FilterOccurrence(String selectedButton, int occurrenceNumber, TableRightClickFilter<E> tableRightClickFilter, 
            int track, int position) {
        this.selectedButton = selectedButton;
        this.occurrenceNumber = occurrenceNumber;
        this.tableRightClickFilter = tableRightClickFilter;
        classType = tableRightClickFilter.getClassType();
        tableModel = (E) tableRightClickFilter.getLastTable().getModel();
        tableColumnPosition = position;
        tableColumnTrack = track;
    }

    public void filterTable() {
        sortedTableModel = sortByPosition();
        compareToTrack(sortedTableModel);
    }

    /**
     * This method sort the given table by the position column (column 0)
     * @return tableModel sorted by the position
     */
    public E sortByPosition() {

        /*
         * Get all rows with data (say, all rows but the one with the headers),
         * sort them based on 'position' column and return the sorted result
         * as table model.
         */
        Vector dataVector = tableModel.getDataVector();
        Collections.sort(dataVector, new Comparator<Vector>() {
            /**
             * Implements the only method needed by the Comparator interface.
             * Ther result is < 0 if o1 is smaller than o2, = 0 if o1 == o2 and
             * > 0 if o1 > o2.
             */
            @Override
            public int compare(Vector o1, Vector o2) {
                return ((Integer) o1.get(tableColumnPosition)).compareTo(((Integer) o2.get(tableColumnPosition)));
            }
        });

        return tableModel;
    }

    /**
     * 
     * @param sortedTableModel
     */
    public void compareToTrack(E sortedTableModel) {
        TableFilterUtils<E> utils = new TableFilterUtils<>(classType);
        E comparedTableModel = utils.prepareNewTableModel(tableModel);
        Vector dataRows = sortedTableModel.getDataVector();

        // count unique tracks per position
        Map<String, Set<Integer>> uniqueTracksPerPosition =
                getUniqueTracksPerPosition(dataRows);

        Set<String> matchingPositions = new HashSet<>();

        for (Entry<String, Set<Integer>> positionInTracks : uniqueTracksPerPosition.entrySet()) {
            if ((selectedButton.equals("max") && positionInTracks.getValue().size() <= occurrenceNumber)
                    || (selectedButton.equals("min") && positionInTracks.getValue().size() >= occurrenceNumber)
                    || (selectedButton.equals("all") && tableRightClickFilter.getTrackMap().size() == 
                    positionInTracks.getValue().size())) {
                matchingPositions.add(positionInTracks.getKey());
            }
        }

        for (Object dataRowObject : dataRows) {
            Vector dataRow = (Vector) dataRowObject;
            if (matchingPositions.contains(String.valueOf(dataRow.get(tableColumnPosition)))) {
                comparedTableModel.addRow(dataRow);
            }

        }

        tableRightClickFilter.setNewTableModel(comparedTableModel);
    }

    /**
     * Creates a map with position entries as keys and matching tracks per
     * position. The tracks are within a Set, so the size of the set you get for
     * a given position is the count of differents tracks the position occurs
     * in.
     *
     * @param dataRows
     * @return
     */
    private Map<String, Set<Integer>> getUniqueTracksPerPosition(Vector<Vector> dataRows) {
        Map<String, Set<Integer>> uniqueTracksPerPosition = new HashMap<>();

        for (Vector currentRow : dataRows) {
            String currentPosition = String.valueOf(currentRow.get(tableColumnPosition));
            if (!uniqueTracksPerPosition.containsKey(currentPosition)) {
                uniqueTracksPerPosition.put(currentPosition, new HashSet<Integer>());
            }
            Integer currentTrack = ((PersistantTrack) currentRow.get(tableColumnTrack)).getId();
            uniqueTracksPerPosition.get(currentPosition).add(currentTrack);
        }

        return uniqueTracksPerPosition;
    }
}
