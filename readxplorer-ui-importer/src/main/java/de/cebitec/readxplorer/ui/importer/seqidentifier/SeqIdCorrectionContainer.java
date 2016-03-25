/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import de.cebitec.readxplorer.parser.TrackJob;
import htsjdk.samtools.SAMSequenceDictionary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Container for bundling all information regarding fixing of sequence ids
 * between the reference and the mapping files.
 *
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SeqIdCorrectionContainer {


    private String id;
    private final List<TrackJob> trackJobs;
    private List<String> chromNames;
    private SAMSequenceDictionary sequenceDictionary;
    private List<Integer> missingSeqIds;
    private int foundIds;
    private boolean isSeqIdsValid;
    private boolean manualFixFailed;


    /**
     * Container for bundling all information regarding fixing of sequence ids
     * between the reference and the mapping files.
     *
     * @param trackJob The track job to update
     * @param id       The id of this container (is required to be unique among
     *                 all containers in one virtual machine)
     */
    public SeqIdCorrectionContainer( TrackJob trackJob, String id ) {
        this.id = id;
        trackJobs = new ArrayList<>();
        trackJobs.add( trackJob );
        init();
    }


    /**
     * Container for bundling all information regarding fixing of sequence ids
     * between the reference and the mapping files.
     *
     * @param trackJobs The list of track jobs to update
     * @param id        The id of this container (is required to be unique among
     *                  all containers in one virtual machine)
     */
    public SeqIdCorrectionContainer( List<TrackJob> trackJobs, String id ) {
        this.trackJobs = trackJobs;
        this.id = id;
        init();
    }


    private void init() {
        chromNames = new ArrayList<>();
        sequenceDictionary = null;
        missingSeqIds = new ArrayList<>();
        foundIds = 0;
    }


    /**
     * @param foundIds Number of mapping file sequence ids found in the
     *                 reference.
     */
    public void setFoundIds( int foundIds ) {
        this.foundIds = foundIds;
    }


    /**
     * @return Number of mapping file sequence ids found in the reference.
     */
    public int getFoundIds() {
        return foundIds;
    }


    /**
     * @param chromNames List of all chromosome names from the reference.
     */
    public void setChromNames( List<String> chromNames ) {
        this.chromNames = chromNames;
    }


    /**
     * @return List of all chromosome names from the reference.
     */
    public List<String> getChromNames() {
        return Collections.unmodifiableList( chromNames );
    }


    /**
     * @return The id of this container (is required to be unique among all
     *         containers in one virtual machine)
     */
    public String getId() {
        return id;
    }


    /**
     * @param missingSeqs All sequence record ids of the mapping file missing in
     *                    the reference file.
     */
    public void setMissingSeqIds( List<Integer> missingSeqs ) {
        this.missingSeqIds = missingSeqs;
    }


    /**
     * @return All sequence record ids of the mapping file missing in the
     *         reference file.
     */
    public List<Integer> getMissingSeqIds() {
        return Collections.unmodifiableList( missingSeqIds );
    }


    /**
     * @param sequenceDictionary The sequence dictionary containing automatic
     *                           corrections to incorporate in an extended
     *                           mapping file.
     */
    public void setSequenceDictionary( SAMSequenceDictionary sequenceDictionary ) {
        this.sequenceDictionary = sequenceDictionary;
    }


    /**
     * @return The sequence dictionary containing automatic corrections to
     *         incorporate in an extended mapping file.
     */
    public SAMSequenceDictionary getSequenceDictionary() {
        return sequenceDictionary;
    }


    /**
     * @return <code>true</code> if manual correction of the mapping file
     *         sequence ids failed, <code>false</code> otherwise.
     */
    public boolean isManualFixFailed() {
        return manualFixFailed;
    }


    /**
     * @param manualFixFailed <code>true</code> if manual correction of the
     *                        mapping file sequence ids failed,
     *                        <code>false</code> otherwise.
     */
    public void setManualFixFailed( boolean manualFixFailed ) {
        this.manualFixFailed = manualFixFailed;
    }


    /**
     * @return <code>true</code> if the sequence ids are valid = all mapping
     *         file ids can be associated to the chosen reference,
     *         <code>false</code> otherwise.
     */
    public boolean isSeqIdsValid() {
        return isSeqIdsValid;
    }


    /**
     * @param isSeqIdsValid <code>true</code> if the sequence ids are valid =
     *                      all mapping file ids can be associated to the chosen
     *                      reference, <code>false</code> otherwise.
     */
    public void setIsSeqIdsValid( boolean isSeqIdsValid ) {
        this.isSeqIdsValid = isSeqIdsValid;
    }


    /**
     * @param trackJob TrackJob to add to this container.
     */
    public void addTrackJob( TrackJob trackJob ) {
        trackJobs.add( trackJob );
    }


    /**
     * @return The track job to update
     */
    public TrackJob getTrackJob() {
        return trackJobs.get( 0 );
    }


    /**
     * @return The list of track jobs to update
     */
    public List<TrackJob> getTrackJobs() {
        return Collections.unmodifiableList( trackJobs );
    }


    /**
     * @return Creates and returns the mapping files names concatenated by a
     *         comma.
     */
    public String getMappingFileNames() {
        String fileNames = "";
        for( TrackJob trackJob : trackJobs ) {
            fileNames = fileNames.concat( trackJob.getFile().getName() ).concat( ", " );
        }
        fileNames = fileNames.substring( 0, fileNames.length() - 2 );
        return fileNames;
    }


}
