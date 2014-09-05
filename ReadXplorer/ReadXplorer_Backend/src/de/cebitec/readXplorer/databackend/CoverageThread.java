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
import de.cebitec.readXplorer.databackend.dataObjects.Coverage;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistent;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.Difference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReferenceGap;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.util.classification.Classification;
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
    private List<PersistentTrack> tracks;
    ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private CoverageAndDiffResultPersistent currentCov;
//    private double requestCounter = 0;
//    private double skippedCounter = 0;
    private PersistentReference referenceGenome;

    /**
     * Thread for carrying out the requests for receiving coverage from a bam
     * file.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their
     * coverage should be combined in the results
     */
    public CoverageThread(List<PersistentTrack> tracks, boolean combineTracks) {
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
        this.currentCov = new CoverageAndDiffResultPersistent(new CoverageManager(0, 0), null, null, null);
    }

    private void doubleCoverageThread(long trackID, long trackID2) {
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new CoverageAndDiffResultPersistent(new CoverageManager(0, 0, true), null, null, null);
    }

    private void multipleCoverageThread() {
        this.trackID = 0;
        this.trackID2 = 0;
        currentCov = new CoverageAndDiffResultPersistent(new CoverageManager(0, 0), null, null, null);
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
    CoverageAndDiffResultPersistent getCoverageAndDiffsFromFile(IntervalRequest request, PersistentTrack track) {
        File file = new File(track.getFilePath());
        if (this.referenceGenome == null) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
            this.referenceGenome = refConnector.getRefGenome();
        }
        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId(), referenceGenome);
        CoverageAndDiffResultPersistent result = externalDataReader.getCoverageFromBam(request);
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
    CoverageAndDiffResultPersistent getCoverageAndReadStartsFromFile(IntervalRequest request, PersistentTrack track) {
        CoverageAndDiffResultPersistent result;
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
    CoverageAndDiffResultPersistent loadReadStartsAndCoverageMultiple(IntervalRequest request) {

        CoverageAndDiffResultPersistent result;
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        CoverageManager cov = new CoverageManager(from, to);
        CoverageManager readStarts = new CoverageManager(from, to);
        cov.incArraysToIntervalSize();
        readStarts.incArraysToIntervalSize();

        if (tracks.size() > 1) {
            for (int i = 0; i < this.tracks.size(); ++i) {
                CoverageAndDiffResultPersistent intermedRes = this.getCoverageAndReadStartsFromFile(request, tracks.get(i));
                cov = this.mergeMultCoverages(cov, intermedRes.getCovManager());
                readStarts = this.mergeMultCoverages(readStarts, intermedRes.getReadStarts());
            }
            result = new CoverageAndDiffResultPersistent(cov, null, null, request);
            result.setReadStarts(readStarts);
        } else {
            result = this.getCoverageAndReadStartsFromFile(request, tracks.get(0));
        }
            
        return result;
    }

    /**
     * Loads the coverage for two tracks, which should not be combined, but
     * viewed as two data sets like in the DoubleTrackViewer. The returned
 CoverageManager will also contain the coverage difference for all
 positions, which are covered in both tracks.
     * @param request the genome request for two tracks.
     * @return CoverageManager of the interval of both tracks, also
 containing the coverage difference for all positions, which are covered
 in both tracks.
     * @throws SQLException
     */
    CoverageAndDiffResultPersistent loadCoverageDouble(IntervalRequest request) {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        CoverageManager cov1 = new CoverageManager(from, to);
        CoverageManager cov2 = new CoverageManager(from, to);
        cov1.incArraysToIntervalSize();
        cov2.incArraysToIntervalSize();
//        cov.setTwoTracks(true); //TODO: check if not needed

        IntervalRequest newRequest = new IntervalRequest(
                request.getFrom(),
                request.getTo(),
                request.getTotalFrom(),
                request.getTotalTo(),
                request.getChromId(),
                request.getSender(),
                request.isDiffsAndGapsNeeded(),
                Properties.NORMAL, 
                CoverageManager.TRACK2,
                request.getReadClassParams());
        CoverageAndDiffResultPersistent result = this.getCoverageAndDiffsFromFile(newRequest, tracks.get(1));

        newRequest = new IntervalRequest(
                request.getFrom(),
                request.getTo(),
                request.getTotalFrom(),
                request.getTotalTo(),
                request.getChromId(),
                request.getSender(),
                request.isDiffsAndGapsNeeded(),
                Properties.NORMAL, 
                CoverageManager.TRACK1,
                request.getReadClassParams());
        result.addCoverageManager(this.getCoverageAndDiffsFromFile(newRequest, tracks.get(0)).getCovManager());

        return result;
    }

    /**
     * Loads the coverage for multiple tracks in one CoverageManager object.
     * This is more efficient, than storing it in several CoverageManager
 objects, each for one track.
     * @param request the genome request to carry out
     * @return the coverage of multiple track combined in one CoverageManager
 object.
     */
    CoverageAndDiffResultPersistent loadCoverageMultiple(IntervalRequest request) {
        int from = request.getTotalFrom(); // calcCenterLeft(request);
        int to = request.getTotalTo(); // calcCenterRight(request);

        CoverageManager covManager = new CoverageManager(from, to);
        List<Difference> diffs = new ArrayList<>();
        List<PersistentReferenceGap> gaps = new ArrayList<>();
        covManager.incArraysToIntervalSize();

        //get coverage of all direct tracks
        if (tracks.size() > 1) {
            for (PersistentTrack track : tracks) {
                CoverageAndDiffResultPersistent intermedRes = this.getCoverageAndDiffsFromFile(request, track);
                covManager = this.mergeMultCoverages(covManager, intermedRes.getCovManager());
                diffs.addAll(intermedRes.getDiffs());
                gaps.addAll(intermedRes.getGaps());
            }
        } else {
            CoverageAndDiffResultPersistent result = this.getCoverageAndDiffsFromFile(request, tracks.get(0));
            covManager = result.getCovManager();
            diffs = result.getDiffs();
            gaps = result.getGaps();
        }

        return new CoverageAndDiffResultPersistent(covManager, diffs, gaps, request);
    }

    @Override
    public void run() {

        try {
            while (!interrupted()) {
                IntervalRequest request = requestQueue.poll();
                if (request != null) {
                    if (!currentCov.getCovManager().coversBounds(request.getFrom(), request.getTo())
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
     * Method for merging two separate persistent coverage objects.
     * @param cov1
     * @param cov2
     * @return The merged coverage object
     */
    private CoverageManager mergeMultCoverages(CoverageManager covManager1, CoverageManager covManager2) {
        List<Classification> includedClasses = covManager1.getIncludedClassifications();
        for (Classification classification : includedClasses) {
            Coverage cov1 = covManager1.getCoverage(classification);
            Coverage cov2 = covManager2.getCoverage(classification);
            for (int i = cov1.getLeftBound(); i <= cov1.getRightBound(); ++i) {
                this.mergeFwdCoverage(i, cov1, cov2);
                this.mergeRevCoverage(i, cov1, cov2);
            }
        }
        return covManager1;
    }

    /**
     * Merges the fwd coverage values from the given index position of both
     * given <code>Coverage</code> objects.
     * @param i index position within the arrays
     * @param cov1 first coverage object. This one is updated with the sum of
     * both values.
     * @param cov2 second coverage object
     * @throws ArrayIndexOutOfBoundsException 
     */
    private void mergeFwdCoverage(int i, Coverage cov1, Coverage cov2) throws ArrayIndexOutOfBoundsException {
        cov1.setFwdCoverage(i, cov1.getFwdCov(i) + cov2.getFwdCov(i));
    }
    
    /**
     * Merges the rev coverage values from the given index position of both
     * given <code>Coverage</code> objects.
     * @param i index position within the arrays
     * @param cov1 first coverage object. This one is updated with the sum of
     * both values.
     * @param cov2 second coverage object
     * @throws ArrayIndexOutOfBoundsException
     */
    private void mergeRevCoverage(int i, Coverage cov1, Coverage cov2) throws ArrayIndexOutOfBoundsException {
        cov1.setRevCoverage(i, cov1.getRevCov(i) + cov2.getRevCov(i));
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
