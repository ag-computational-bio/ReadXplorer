/* 
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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
package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Holds all the overlapping dataset between all analysis handlers.
 *
 * @author kstaderm
 */
public class DeAnalysisData {

    /**
     * Contains ID of the reference features as keys and corresponding feature as values.
     */
    private Map<String, PersistantFeature> featureData;
    /**
     * Contains the count data for all the tracks. The first Integer array
     * represents the count data for the selected track with the lowest id. The
     * secound Integer array holds the count data for the selected track with
     * the secound lowest id an so on.
     */
    private final Queue<Integer[]> countData;
    /**
     * The tracks selected by the user to perform the analysis on.
     */
    private List<PersistantTrack> selectedTracks;
    /**
     * Track Descriptions. Each description just appears one time.
     */
    private String[] trackDescriptions;
    
    /**
     * Creates a new instance of the DeAnalysisData class.
     *
     * @param capacity Number of selected tracks.
     */
    public DeAnalysisData(int capacity) {
        countData = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Adds count data as an Integer array to a Queue holding all count data
     * necessary for the analysis. The data must be added in an ascending order
     * starting with the count data belonging to the track with the lowest ID.
     *
     * @param data count data
     */
    public void addCountDataForTrack(Integer[] data) {
        countData.add(data);
    }

    /**
     * Return the first count data value on the Queue and removes it. So this
     * method will give you back the cound data added bei the
     *
     * @see addCountDataForTrack() method. The count data added first will also
     * be the first this method returns. This method also converts the count
     * data from an Integer array to an int array so that they can be handed
     * over to Gnu R directly.
     * @return count data as int[]
     */
    public int[] pollFirstCountData() {
        Integer[] cdata = countData.poll();
        int[] ret = new int[cdata.length];
        for (int i = 0; i < cdata.length; i++) {
            ret[i] = cdata[i].intValue();
        }
        return ret;
    }

    /**
     * Checks if there is still count data on the Queue
     *
     * @return true if there is at least on count data on the Queue or false if
     * it is empty.
     */
    public boolean hasCountData() {
        return !countData.isEmpty();
    }

    /**
     * Return the start positions of the reference features.
     *
     * @return Start positions of the reference features.
     */
    public int[] getStart() {
        int[] ret = new int[featureData.size()];
        int i = 0;
        for (Iterator<String> it = featureData.keySet().iterator(); it.hasNext(); i++) {
            String key = it.next();
            ret[i] = featureData.get(key).getStart();
        }
        return ret;
    }

    /**
     * Return the stop positions of the reference features.
     *
     * @return stop positions of the reference features.
     */
    public int[] getStop() {
        int[] ret = new int[featureData.size()];
        int i = 0;
        for (Iterator<String> it = featureData.keySet().iterator(); it.hasNext(); i++) {
            String key = it.next();
            ret[i] = featureData.get(key).getStop();
        }
        return ret;
    }

    /**
     * Return the names of the reference features.
     *
     * @return Names of the reference features as an String Array.
     */
    public String[] getFeatureNames() {
        String[] ret = featureData.keySet().toArray(new String[featureData.keySet().size()]);
        ProcessingLog.getInstance().addProperty("Number of annotations", ret.length);
        return ret;
    }

    /**
     * Returns the reference features.
     * @return Reference features as an Array.
     */
    public PersistantFeature[] getFeatures() {
        PersistantFeature[] features = new PersistantFeature[featureData.keySet().size()];
        int i = 0;
        for (Iterator<String> it = featureData.keySet().iterator(); it.hasNext(); i++) {
            String key = it.next();
            features[i] = featureData.get(key);
        }
        return features;
    }

    /**
     * Returns the tracks selected by the user to perform the analysis on.
     *
     * @return List of PersistantTrack containing the selected tracks.
     */
    public List<PersistantTrack> getSelectedTracks() {
        return selectedTracks;
    }

    public String[] getTrackDescriptions() {
        return trackDescriptions;
    }

    public PersistantFeature getPersistantFeatureByGNURName(String gnuRName) {
        return featureData.get(gnuRName);
    }

    public boolean existsPersistantFeatureForGNURName(String gnuRName) {
        return featureData.containsKey(gnuRName);
    }

    public void setFeatures(List<PersistantFeature> features) {
        featureData = new LinkedHashMap<>();
        int counter = 1;
        for (Iterator<PersistantFeature> it = features.iterator(); it.hasNext();) {
            PersistantFeature persistantFeature = it.next();
            if (featureData.containsKey(persistantFeature.getLocus())) {
                featureData.put(persistantFeature.getLocus() + "_DN_" + counter++, persistantFeature);
            } else {
                featureData.put(persistantFeature.getLocus(), persistantFeature);
            }
        }
    }

    public void setSelectedTracks(List<PersistantTrack> selectedTracks) {
        this.selectedTracks = selectedTracks;
        Set<String> tmpSet = new LinkedHashSet<>();
        int counter = 1;
        for (int i = 0; i < selectedTracks.size(); i++) {
            if (!tmpSet.add(selectedTracks.get(i).getDescription())) {
                tmpSet.add(selectedTracks.get(i).getDescription() + "_DN_" + counter++);
            }
        }
        trackDescriptions = tmpSet.toArray(new String[tmpSet.size()]);
    }
}
