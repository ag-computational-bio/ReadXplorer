/*
 * Copyright (C) 2015 Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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
package de.cebitec.readxplorer.databackend;

import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.exporter.tables.ExportDataI;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Generates a complete set of track statistics for all tracks in a DB in a
 * fashion that can be used for table exporter output.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TrackStatisticsGenerator implements ExportDataI {

    private final ProjectConnector projectConnector;
    private final List<String> trackStatsIds;

    /**
     * Generates a complete set of track statistics for all tracks in a DB in a
     * fashion that can be used for table exporter output.
     */
    public TrackStatisticsGenerator() {
        projectConnector = ProjectConnector.getInstance();
        trackStatsIds = StatsContainer.getListOfTrackStatistics();
        trackStatsIds.addAll(StatsContainer.getListOfReadPairStatistics());
    }

    /**
     * @return The single sheet name of the track statistics table.
     */
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Track Statistics Table");
        return sheetNames;
    }

    /**
     * @return The headers of the track statistics table.
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptionsList = new ArrayList<>();

        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Reference");
        dataColumnDescriptions.addAll(trackStatsIds);

        dataColumnDescriptionsList.add(dataColumnDescriptions);

        return dataColumnDescriptionsList;
    }

    /**
     * Fetches the track statistics of all tracks and creates the data structure
     * to store them in a table. The statistics include both, the single end
     * and read pair statistics. If a statistics value is lacking, it is set to
     * "-1".
     * @return The statistics table data
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> allData = new ArrayList<>();
        List<List<Object>> statsData = new ArrayList<>();

        List<PersistentTrack> tracks = projectConnector.getTracks();
        for (PersistentTrack track : tracks) {
            SaveFileFetcherForGUI fileFetcher = new SaveFileFetcherForGUI();
            try {

                TrackConnector trackConnector = fileFetcher.getTrackConnector(track);
                StatsContainer trackStats = trackConnector.getTrackStats();
                statsData.add(statsToList(track, trackStats));

            } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog( null, "You did not complete the track path selection. The track statistics cannot be stored for this track.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE );
                //skipping track
            }
        }

        allData.add(statsData);

        return allData;
    }

    /**
     * Creates a statistics list from the given track and track statistics
     * data. The order of the elements is always the same as declared in the
     * StatsContainer for all statistics. Both, track statistics and read pair
     * track statistics are stored. All missing statistics fields are filled
     * with "-1".
     * @param track The track for which the data should be processed
     * @param trackStats The StatsContainer with all track statistics
     * @return The ready-to-use statistics list
     */
    private List<Object> statsToList(PersistentTrack track, StatsContainer trackStats) {

        List<Object> trackStatsList = new ArrayList<>();

        trackStatsList.add(track);
        trackStatsList.add(projectConnector.getRefGenomeConnector(track.getRefGenID()).getRefGenome());

        Map<String, Integer> statsMap = trackStats.getStatsMap();
        for (String trackStatsId : trackStatsIds) {
            if (statsMap.containsKey(trackStatsId)) {
                trackStatsList.add(statsMap.get(trackStatsId));
            } else {
                trackStatsList.add(-1);
            }
        }

        return trackStatsList;
    }
}
