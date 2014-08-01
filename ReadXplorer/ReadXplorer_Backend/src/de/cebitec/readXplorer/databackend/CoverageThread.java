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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantDiff;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.VisualisationUtils;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Thread for carrying out the requests for receiving coverage from a bam file.
 *
 * @author ddoppmei, rhilker
 */
public class CoverageThread extends RequestThread {

    private long trackID;
    private long trackID2;
    private List<PersistantTrack> tracks;
    ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private CoverageAndDiffResultPersistant currentCov;
//    private double requestCounter = 0;
//    private double skippedCounter = 0;
    private PersistantReference referenceGenome;

    /**
     * Thread for carrying out the requests for receiving coverage from a bam
     * file.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their
     * coverage should be combined in the results
     */
    public CoverageThread(List<PersistantTrack> tracks, boolean combineTracks) {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();

        // do id specific stuff
        this.tracks = tracks;
        if (tracks.size() == 1) {
            this.singleCoverageThread(tracks.get(0).getId());
        } else if (tracks.size() == 2 && !combineTracks) {
            this.doubleCoverageThread(tracks.get(0).getId(), tracks.get(1).getId());
        } else if (tracks.size() >= 2) {
            this.multipleCoverageThread();
        } else {
            throw new UnsupportedOperationException("At least one track needs to be handed over to the CoverageThread.");
        }
    }

    private void singleCoverageThread(long trackID) {
        this.trackID = trackID;
        this.trackID2 = 0;
        this.currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, null);
    }

    private void doubleCoverageThread(long trackID, long trackID2) {
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0, true), null, null, null);
    }

    private void multipleCoverageThread() {
        this.trackID = 0;
        this.trackID2 = 0;
        currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, null);
    }

    @Override
    public void addRequest(IntervalRequest request) {
        this.setLatestRequest(request);
        requestQueue.add(request);
    }
    
    /**
     * Fetches the coverage and (if desired) also the diffs and gaps for a given
     * interval.
     * @param request the request to carry out
     * @param track the track for which the coverage is requested
     * @return the container with the desired data
     */
    CoverageAndDiffResultPersistant getCoverageAndDiffsFromFile(IntervalRequest request, PersistantTrack track) {
        File file = new File(track.getFilePath());
        if (this.referenceGenome == null) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
            this.referenceGenome = refConnector.getRefGenome();
        }
        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId(), referenceGenome);
        CoverageAndDiffResultPersistant result = externalDataReader.getCoverageFromBam(request);
        externalDataReader.close();
        return result;

    }
    
    /**
     * Fetches the read starts and the coverage from a bam file of the first
     * track.
     * @param request the request for which the read starts are needed
     * @return the coverage and diff result containing the read starts, the
     * coverage and no diffs and gaps
     */
    CoverageAndDiffResultPersistant getCoverageAndReadStartsFromFile(IntervalRequest request, PersistantTrack track) {
        CoverageAndDiffResultPersistant result;
        File file = new File(track.getFilePath());
        if (this.referenceGenome == null) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
            this.referenceGenome = refConnector.getRefGenome();
        }

        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId(), referenceGenome);
        result = externalDataReader.getCoverageAndReadStartsFromBam(request);
        externalDataReader.close();
        return result;
    }
    
    /**
     * Fetches the read starts and the coverage from a bam file
     * for multiple tracks and combines all the data.
     * @param request the request for which the read starts are needed
     * @return the coverage and diff result containing the read starts, the
     * coverage and no diffs and gaps
     * @param request
     * @return 
     */
    CoverageAndDiffResultPersistant loadReadStartsAndCoverageMultiple(IntervalRequest request) {

        CoverageAndDiffResultPersistant result;
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        PersistantCoverage cov = new PersistantCoverage(from, to);
        PersistantCoverage readStarts = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();
        readStarts.incArraysToIntervalSize();

        if (tracks.size() > 1) {
            for (int i = 0; i < this.tracks.size(); ++i) {
                CoverageAndDiffResultPersistant intermedRes = this.getCoverageAndReadStartsFromFile(request, tracks.get(i));
                cov = this.mergeMultCoverages(cov, intermedRes.getCoverage());
                readStarts = this.mergeMultCoverages(readStarts, intermedRes.getReadStarts());
            }
            result = new CoverageAndDiffResultPersistant(cov, null, null, request);
            result.setReadStarts(readStarts);
        } else {
            result = this.getCoverageAndReadStartsFromFile(request, tracks.get(0));
        }
            
        return result;
    }

    /**
     * Loads the coverage for two tracks, which should not be combined, but
     * viewed as two data sets like in the DoubleTrackViewer. The returned
     * PersistantCoverage will also contain the coverage difference for all
     * positions, which are covered in both tracks.
     * @param request the genome request for two tracks.
     * @return PersistantCoverage of the interval of both tracks, also
     * containing the coverage difference for all positions, which are covered
     * in both tracks.
     * @throws SQLException
     */
    CoverageAndDiffResultPersistant loadCoverageDouble(IntervalRequest request) {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        PersistantCoverage cov = new PersistantCoverage(from, to, true);
        cov.incDoubleTrackArraysToIntervalSize();
        cov.setTwoTracks(true);

        IntervalRequest newRequest = new IntervalRequest(
                request.getFrom(),
                request.getTo(),
                request.getTotalFrom(),
                request.getTotalTo(),
                request.getChromId(),
                request.getSender(),
                request.isDiffsAndGapsNeeded(),
                Properties.NORMAL, PersistantCoverage.TRACK2);
        cov = this.getCoverageAndDiffsFromFile(newRequest, tracks.get(1)).getCoverage();

        newRequest = new IntervalRequest(
                request.getFrom(),
                request.getTo(),
                request.getTotalFrom(),
                request.getTotalTo(),
                request.getChromId(),
                request.getSender(),
                request.isDiffsAndGapsNeeded(),
                Properties.NORMAL, PersistantCoverage.TRACK1);
        PersistantCoverage intermedCov = this.getCoverageAndDiffsFromFile(newRequest, tracks.get(0)).getCoverage();
        cov.setCommonFwd(new int[cov.getCommonFwdCovTrack2().length]);
        cov.setCommonRev(new int[cov.getCommonRevCovTrack2().length]);
        for (int i = cov.getLeftBound(); i <= cov.getRightBound(); ++i) {
            //check if cov of track 2 exists at position
            int commonFwdTrack2 = cov.getCommonFwdTrack2(i);
            int commonRevTrack2 = cov.getCommonRevTrack2(i);
            int commonFwdTrack1 = intermedCov.getCommonFwdTrack1(i);
            int commonRevTrack1 = intermedCov.getCommonRevTrack1(i);

            //we just set coverage of the diff if cov of  track 2 or track 1 exist
            if (commonFwdTrack1 != 0 && commonFwdTrack2 != 0) {
                cov.setCommonFwd(i, Math.abs(commonFwdTrack1 - commonFwdTrack2));
            }
            if (commonRevTrack1 != 0 && commonRevTrack2 != 0) {
                cov.setCommonRev(i, Math.abs(commonRevTrack1 - commonRevTrack2));
            }
        }
        cov.setCommonFwdTrack1(intermedCov.getCommonFwdCovTrack1());
        cov.setCommonRevTrack1(intermedCov.getCommonRevCovTrack1());

        return new CoverageAndDiffResultPersistant(cov, null, null, request);
    }

    /**
     * Loads the coverage for multiple tracks in one PersistantCoverage object.
     * This is more efficient, than storing it in several PersistantCoverage
     * objects, each for one track.
     * @param request the genome request to carry out
     * @return the coverage of multiple track combined in one PersistantCoverage
     * object.
     */
    CoverageAndDiffResultPersistant loadCoverageMultiple(IntervalRequest request) {
        int from = request.getTotalFrom(); // calcCenterLeft(request);
        int to = request.getTotalTo(); // calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        cov.incArraysToIntervalSize();

        //get coverage of all direct tracks
        if (tracks.size() > 1) {
            for (PersistantTrack track : tracks) {
                CoverageAndDiffResultPersistant intermedRes = this.getCoverageAndDiffsFromFile(request, track);
                cov = this.mergeMultCoverages(cov, intermedRes.getCoverage());
                diffs.addAll(intermedRes.getDiffs());
                gaps.addAll(intermedRes.getGaps());
            }
        } else {
            CoverageAndDiffResultPersistant result = this.getCoverageAndDiffsFromFile(request, tracks.get(0));
            cov = result.getCoverage();
            diffs = result.getDiffs();
            gaps = result.getGaps();
        }

        return new CoverageAndDiffResultPersistant(cov, diffs, gaps, request);
    }

    @Override
    public void run() {

        try {
            while (!interrupted()) {
                IntervalRequest request = requestQueue.poll();
                if (request != null) {
                    if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo())
                            || currentCov.getRequest().getChromId() != request.getChromId()
                            || (!currentCov.getRequest().isDiffsAndGapsNeeded() && request.isDiffsAndGapsNeeded())
                            || !this.readClassParamsFulfilled(request)) {
//                        requestCounter++;
                        if (doesNotMatchLatestRequestBounds(request)) {
                            if (trackID2 != 0) {
                                currentCov = this.loadCoverageDouble(request); //at the moment we only need the complete coverage here
                            } else if (this.trackID != 0 || this.canQueryCoverage()) {
                                currentCov = this.loadCoverageMultiple(request);
                            }
                        } /*else {
                         skippedCounter++;
                         request.getSender().notifySkipped();
                         }*/
                    }

                    if (this.doesNotMatchLatestRequestBounds(request)) {
                        this.setLastRequest(request);
                        request.getSender().receiveData(currentCov);
                    
                    } else {
                        request.getSender().notifySkipped(); 
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CoverageThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            VisualisationUtils.displayOutOfMemoryError(JOptionPane.getRootFrame());
            this.interrupt();
        }
    }

    /**
     * Method for merging two separate persistant coverage objects.
     * @param cov1
     * @param cov2
     * @return The merged coverage object
     */
    private PersistantCoverage mergeMultCoverages(PersistantCoverage cov1, PersistantCoverage cov2) {
        for (int i = cov1.getLeftBound(); i <= cov1.getRightBound(); ++i) {
            cov1.setPerfectFwd(i, cov1.getPerfectFwd(i) + cov2.getPerfectFwd(i));
            cov1.setPerfectRev(i, cov1.getPerfectRev(i) + cov2.getPerfectRev(i));
            cov1.setBestMatchFwd(i, cov1.getBestMatchFwd(i) + cov2.getBestMatchFwd(i));
            cov1.setBestMatchRev(i, cov1.getBestMatchRev(i) + cov2.getBestMatchRev(i));
            cov1.setCommonFwd(i, cov1.getCommonFwd(i) + cov2.getCommonFwd(i));
            cov1.setCommonRev(i, cov1.getCommonRev(i) + cov2.getCommonRev(i));
        }
        return cov1;
    }
    
    protected long getTrackId() {
        return this.trackID;
    }
    
    protected long getTrackId2() {
        return this.trackID2;
    }
    
    /**
     * @return true, if the tracklist is not null or empty, false otherwise
     */
    protected boolean canQueryCoverage() {
        return this.tracks != null && !this.tracks.isEmpty();
    }
}
