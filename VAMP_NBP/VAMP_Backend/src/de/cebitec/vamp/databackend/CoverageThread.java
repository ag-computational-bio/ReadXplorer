package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/*
 * TODO: finish coverage thread for direct bam support. already done: loadCoverage
 * loadCoverageDouble, loadCoverageMutliple
 */

/**
 * Thread for carrying out the requests for receiving coverage either from a database
 * or a bam file.
 *
 * @author ddoppmei, rhilker
 */
public class CoverageThread extends Thread implements RequestThreadI {

    private long trackID;
    private long trackID2;
    private List<PersistantTrack> tracks;
    private Connection con;
    private ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private CoverageAndDiffResultPersistant currentCov;
    private int coveredWidth;
    private IntervalRequest latestRequest;
    private double requestCounter;
    private double skippedCounter;
    private boolean isDbUsed = false;
    private PersistantReference referenceGenome;

    public CoverageThread(List<PersistantTrack> tracks, boolean combineTracks) {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        con = ProjectConnector.getInstance().getConnection();
        coveredWidth = 25000;
        requestCounter = 0;
        skippedCounter = 0;

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
        trackID2 = 0;
        currentCov = new CoverageAndDiffResultPersistant(null, null, null, false, 0, 0);
        this.isDbUsed = this.tracks.get(0).isDbUsed();
    }

    private void doubleCoverageThread(long trackID, long trackID2) {
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0, true), null, null, false, 0, 0);
        this.isDbUsed = this.tracks.get(0).isDbUsed() || this.tracks.get(1).isDbUsed();
    }

    private void multipleCoverageThread() {
        this.trackID = 0;
        this.trackID2 = 0;
        currentCov = new CoverageAndDiffResultPersistant(null, null, null, false, 0, 0);
        for (PersistantTrack track : this.tracks) {
            this.isDbUsed = track.isDbUsed() ? true : this.isDbUsed;
        }
    }

    @Override
    public void addRequest(IntervalRequest request) {
        latestRequest = request;
        requestQueue.add(request);
    }

    public void setCoveredWidth(int coveredWidth) {
        this.coveredWidth = coveredWidth;
    }

    private int calcCenterLeft(IntervalRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int interval = request.getTo() - request.getFrom();
        coveredWidth = interval > coveredWidth * 2 ? interval / 2 : coveredWidth;
        int result = centerMiddle - coveredWidth;
        return result < 0 ? 0 : result;
    }

    private int calcCenterRight(IntervalRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int interval = request.getTo() - request.getFrom();
        coveredWidth = interval > coveredWidth * 2 ? interval / 2 : coveredWidth;
        int result = centerMiddle + coveredWidth;
        return result;
    }

    private int calcCenterMiddle(IntervalRequest request) {
        return (request.getFrom() + request.getTo()) / 2;
    }

    private boolean matchesLatestRequestBounds(IntervalRequest request) {
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);

        // rounding error somewhere....
        if (currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Fetches the coverage and (if desired) also the diffs and gaps for a given
     * interval.
     * @param request the request to carry out
     * @param from exact left bound of the interval
     * @param to exact right bound of the interval
     * @return the container with the desired data
     */
    private CoverageAndDiffResultPersistant getCoverageAndDiffsFromFile(IntervalRequest request, int from, int to, PersistantTrack track) {
        boolean diffsAndGapsNeeded = request instanceof CoverageAndDiffRequest;
        File file = new File(track.getFilePath());
        if (this.referenceGenome == null) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
            this.referenceGenome = refConnector.getRefGen();
        }
        SamBamFileReader externalDataReader;
        externalDataReader = new SamBamFileReader(file, track.getId());
        return externalDataReader.getCoverageFromBam(this.referenceGenome, from, to, diffsAndGapsNeeded, request.getDesiredData());

    }

    /**
     * Loads the fwd and rev coverage combined for one single track. Meaning, it
     * contains the absoulte coverage values for each position of the track included
     * in the request.
     * @param request the request to carry out for one track and a given interval
     * @return the PersistantCoverage for the given interval and the track
     * @throws SQLException 
     */
    private CoverageAndDiffResultPersistant loadCoverage(IntervalRequest request) throws SQLException {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);
        
        CoverageAndDiffResultPersistant result;
        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();

        if (this.isDbUsed) {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            fetch.setLong(3, trackID);

            ResultSet rs = fetch.executeQuery();
//            int counter = 0;
//            int tmpHighestCov = 0;
            while (rs.next()) {
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
//                counter++;
                //perfect cov
                cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));
                //best match cov
                cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
                //complete cov
                int covNFWMult = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                int covNRevMult = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//                if (pos >= request.getFrom() & pos <= request.getTo()
//                        && (tmpHighestCov < covNFWMult || tmpHighestCov < covNRevMult)) {
//                    tmpHighestCov = covNFWMult < covNRevMult ? covNRevMult : covNFWMult;
//                    cov.setHighestCoverage(tmpHighestCov);
//                }
                cov.setCommonFwdMult(pos, covNFWMult);
                cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setCommonRevMult(pos, covNRevMult);
                cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

            }
            fetch.close();
            rs.close();
            result = new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
            
        } else {
            result = this.getCoverageAndDiffsFromFile(request, from, to, tracks.get(0));
        }

        return result;
    }

    /**
     * Fetches the best match coverage of an interval of a certain track.
     *
     * @param request the coverage request to carry out
     * @return the best match coverage of an interval of a certain track.
     */
    private CoverageAndDiffResultPersistant loadCoverageBest(IntervalRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();
        PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_BEST_FOR_INTERVAL);
        fetch.setLong(1, trackID);
        fetch.setInt(2, from);
        fetch.setInt(3, to);

        ResultSet rs = fetch.executeQuery();

        while (rs.next()) {
            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
            //perfect cov
            cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
            cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
            //best match cov
            cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
            cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
        }
        fetch.close();
        rs.close();

        return new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
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
    private CoverageAndDiffResultPersistant loadCoverageDouble(IntervalRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);
        PersistantCoverage cov = new PersistantCoverage(from, to, true);
        cov.incDoubleTrackArraysToIntervalSize();
        cov.setTwoTracks(true);

        if (tracks.get(1).isDbUsed()) {
            PreparedStatement fetch2 = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);

            fetch2.setInt(1, from);
            fetch2.setInt(2, to);
            fetch2.setLong(3, trackID2);
            ResultSet rs2 = fetch2.executeQuery();
            while (rs2.next()) {
                int pos = rs2.getInt(FieldNames.COVERAGE_POSITION);
                //coverage of Track2
                cov.setCommonFwdMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setCommonRevMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

            }
            fetch2.close();
            rs2.close();
        
        } else {
            IntervalRequest newRequest = new IntervalRequest(request.getFrom(), request.getTo(), request.getSender(), PersistantCoverage.TRACK2);
            cov = this.getCoverageAndDiffsFromFile(newRequest, from, to, tracks.get(1)).getCoverage();
            //TODO: maybe optimize request to only store common coverage information
        }
        
        if (tracks.get(0).isDbUsed()) {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            fetch.setLong(3, trackID);
            ResultSet rs = fetch.executeQuery();
       
            while (rs.next()) {
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);

                //check if cov of track 2 exists at position
                int nFwMultTrack2 = cov.getCommonFwdMultTrack2(pos);
                int nRvMultTrack2 = cov.getCommonRevMultTrack2(pos);
                int nFwMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                int nRvMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);

                //we just set coverage of the diff if cov of  track 2 or track 1 exist
                if (nFwMultTrack1 != 0 && nFwMultTrack2 != 0) {
                    cov.setCommonFwdMult(pos, Math.abs(nFwMultTrack1 - nFwMultTrack2));
                }
                if (nRvMultTrack1 != 0 && nRvMultTrack2 != 0) {
                    cov.setCommonRevMult(pos, Math.abs(nRvMultTrack1 - nRvMultTrack2));
                }

                cov.setCommonFwdMultTrack1(pos, nFwMultTrack1);
                cov.setCommonRevMultTrack1(pos, nRvMultTrack1);
            }
            fetch.close();
            rs.close();
        
        } else {
            IntervalRequest newRequest = new IntervalRequest(request.getFrom(), request.getTo(), request.getSender(), PersistantCoverage.TRACK1);
            PersistantCoverage intermedCov = this.getCoverageAndDiffsFromFile(newRequest, from, to, tracks.get(0)).getCoverage();
            cov.setCommonFwdMult(new int[cov.getCommonFwdMultCovTrack2().length]);
            cov.setCommonRevMult(new int[cov.getCommonRevMultCovTrack2().length]);
            for (int i = cov.getLeftBound(); i <= cov.getRightBound(); ++i) {
                //check if cov of track 2 exists at position
                int nFwMultTrack2 = cov.getCommonFwdMultTrack2(i);
                int nRvMultTrack2 = cov.getCommonRevMultTrack2(i);
                int nFwMultTrack1 = intermedCov.getCommonFwdMultTrack1(i);
                int nRvMultTrack1 = intermedCov.getCommonRevMultTrack1(i);

                //we just set coverage of the diff if cov of  track 2 or track 1 exist
                if (nFwMultTrack1 != 0 && nFwMultTrack2 != 0) {
                    cov.setCommonFwdMult(i, Math.abs(nFwMultTrack1 - nFwMultTrack2));
                }
                if (nRvMultTrack1 != 0 && nRvMultTrack2 != 0) {
                    cov.setCommonRevMult(i, Math.abs(nRvMultTrack1 - nRvMultTrack2));
                }
            }
            cov.setCommonFwdMultTrack1(intermedCov.getCommonFwdMultCovTrack1());
            cov.setCommonRevMultTrack1(intermedCov.getCommonRevMultCovTrack1());
            
        }
        
        return new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
    }

    /**
     * Loads the coverage for multiple tracks in one PersistantCoverage object.
     * This is more efficient, than storing it in several PersistantCoverage
     * objects, each for one track.
     *
     * @param request the genome request to carry out
     * @return the coverage of multiple track combined in one PersistantCoverage
     * object.
     */
    private CoverageAndDiffResultPersistant loadCoverageMutliple(IntervalRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();

        if (this.isDbUsed) {
            //create the sql statement dynamically according to the number of tracks combined
            String dynamicSqlStatement = SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART1 + " ( ";
            for (int i = 0; i < this.tracks.size(); ++i) {
                if (i > 0 && this.tracks.get(i).isDbUsed()) {
                    dynamicSqlStatement += " OR ";
                }
                dynamicSqlStatement += SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART2;

            }
            dynamicSqlStatement += ");";

            PreparedStatement fetch = con.prepareStatement(dynamicSqlStatement);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            for (int i = 0; i < this.tracks.size(); ++i) {
                if (this.tracks.get(i).isDbUsed()) {
                    fetch.setInt(3 + i, this.tracks.get(i).getId());
                }
            }

            ResultSet rs = fetch.executeQuery();
//        int counter = 0;
//        int tmpHighestCov = 0;
            while (rs.next()) {

                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
//            counter++;
                //perfect cov
                cov.setPerfectFwdMult(pos, cov.getPerfectFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                cov.setPerfectFwdNum(pos, cov.getPerfectFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                cov.setPerfectRevMult(pos, cov.getPerfectRevMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                cov.setPerfectRevNum(pos, cov.getPerfectRevNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

                //best match cov
                cov.setBestMatchFwdMult(pos, cov.getBestMatchFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBestMatchFwdNum(pos, cov.getBestMatchFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBestMatchRevMult(pos, cov.getBestMatchRevMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBestMatchRevNum(pos, cov.getBestMatchRevNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));

                //complete cov and highest coverage in interval calculation
                int covCommonFwdMult = cov.getCommonFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                int covCommonRevMult = cov.getCommonRevMult(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//            if (pos >= request.getFrom() && pos <= request.getTo()
//                    && (tmpHighestCov < covCommonFwdMult || tmpHighestCov < covCommonRevMult)) {
//                tmpHighestCov = covCommonFwdMult < covCommonRevMult ? covCommonRevMult : covCommonFwdMult;
//                cov.setHighestCoverage(tmpHighestCov);
//            }
                cov.setCommonFwdMult(pos, covCommonFwdMult);
                cov.setCommonFwdNum(pos, cov.getCommonFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setCommonRevMult(pos, covCommonRevMult);
                cov.setCommonRevNum(pos, cov.getCommonRevNum(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

            }
            fetch.close();
            rs.close();
        }
        
        //get coverage of all direct tracks
        cov.incArraysToIntervalSize();
        for (int i = 0; i < this.tracks.size(); ++i) {
            if (!this.tracks.get(i).isDbUsed()) {
                CoverageAndDiffResultPersistant intermedRes = this.getCoverageAndDiffsFromFile(request, from, to, tracks.get(i));
                cov = this.mergeMultCoverages(cov, intermedRes.getCoverage());
            }
        }

        return new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
    }

    /**
     * Loads the coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     *
     * @param request the request with more than one track
     * @return The coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     */
    private PersistantCoverage[] loadCoverageMutliple2(IntervalRequest request) throws SQLException {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

        PersistantCoverage[] covArray = new PersistantCoverage[this.tracks.size()];
        Map<Integer, PersistantCoverage> covMap = new HashMap<>();
        for (int i = 0; i < this.tracks.size(); ++i) {
            PersistantCoverage coverage = new PersistantCoverage(from, to);
            coverage.incArraysToIntervalSize();
            covArray[i] = coverage;
            covMap.put(this.tracks.get(i).getId(), new PersistantCoverage(from, to));
        }

        //create the sql statement dynamically according to the number of tracks combined
        String dynamicSqlStatement = SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART1 + " ( ";
        for (int i = 0; i < this.tracks.size(); ++i) {
            if (i > 0) {
                dynamicSqlStatement += " OR ";
            }
            dynamicSqlStatement += SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART2;
        }
        dynamicSqlStatement += ");";
        try (PreparedStatement fetch = con.prepareStatement(dynamicSqlStatement)) {
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            for (int i = 0; i < this.tracks.size(); ++i) {
                fetch.setInt(3 + i, this.tracks.get(i).getId());
            }
            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {

                PersistantCoverage cov = covMap.get(rs.getInt(FieldNames.COVERAGE_TRACK));

                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
                //   counter++;
                //perfect cov
                cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

                //best match cov
                cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));

                //complete cov and highest coverage in interval calculation
                int covNFWMult = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                int covNRevMult = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
    //            if (pos >= request.getFrom() && pos <= request.getTo()
    //                    && (tmpHighestCov < covNFWMult || tmpHighestCov < covNRevMult)) {
    //                tmpHighestCov = covNFWMult < covNRevMult ? covNRevMult : covNFWMult;
    //                cov.setHighestCoverage(tmpHighestCov);
    //            }
                cov.setCommonFwdMult(pos, covNFWMult);
                cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setCommonRevMult(pos, covNRevMult);
                cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

            }
            rs.close();
        }

        Iterator<PersistantCoverage> covIt = covMap.values().iterator();
        int count = 0;
        while (covIt.hasNext()) {
            covArray[count++] = covIt.next();
        }
        return covArray;
    }

    @Override
    public void run() {

        try {
            while (!interrupted()) {
                IntervalRequest request = requestQueue.poll();
                if (request != null) {
                    if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo()) 
                            || (currentCov.isDiffsAndGapsUsed() && request instanceof CoverageAndDiffRequest)) {
                        requestCounter++;
                        if (matchesLatestRequestBounds(request)) {
                            if (trackID2 != 0) {
                                currentCov = this.loadCoverageDouble(request); //at the moment we only need the complete coverage here
                            } else if (this.trackID != 0) {
                                if (request.getDesiredData() == Properties.BEST_MATCH_COVERAGE) {
                                    currentCov = this.loadCoverageBest(request);
                                } else { 
                                    currentCov = this.loadCoverage(request);
                                } //else request.getDesiredData() == Properties.PERFECT_COVERAGE does not exist yet, as it is not needed yet
                            } else if (this.tracks != null && !this.tracks.isEmpty()) {
                                currentCov = this.loadCoverageMutliple(request);
                            }
                        } else {
                            skippedCounter++;
                        }
                    }
                    if (this.matchesLatestRequestBounds(request)) {
                        request.getSender().receiveData(currentCov);
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
            String msg = NbBundle.getMessage(CoverageThread.class, "OOM_Message",
                    "An out of memory error occured during fetching the references. Please restart the software with more memory.");
            String title = NbBundle.getMessage(CoverageThread.class, "OOM_Header", "Restart Software");
            JOptionPane.showMessageDialog(new JPanel(), msg, title, JOptionPane.INFORMATION_MESSAGE);
            this.interrupt();
        } catch (SQLException e) {
            String msg;
            String title;
            if (e.getMessage().contains("Out of memory")) {
                msg = NbBundle.getMessage(CoverageThread.class, "OOM_Message",
                        "An out of memory error occured during fetching the references. Please restart the software with more memory.");
                title = NbBundle.getMessage(CoverageThread.class, "OOM_Header", "Restart Software");
                JOptionPane.showMessageDialog(new JPanel(), msg, title, JOptionPane.INFORMATION_MESSAGE);
                this.interrupt();
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Method for merging two separate persistant coverage objects.
     * @param cov1
     * @param cov2
     * @return 
     */
    private PersistantCoverage mergeMultCoverages(PersistantCoverage cov1, PersistantCoverage cov2) {
        for (int i = cov1.getLeftBound(); i <= cov1.getRightBound(); ++i) {
            cov1.setPerfectFwdMult(i, cov1.getPerfectFwdMult(i) + cov2.getPerfectFwdMult(i));
            cov1.setPerfectRevMult(i, cov1.getPerfectRevMult(i) + cov2.getPerfectRevMult(i));
            cov1.setBestMatchFwdMult(i, cov1.getBestMatchFwdMult(i) + cov2.getBestMatchFwdMult(i));
            cov1.setBestMatchRevMult(i, cov1.getBestMatchRevMult(i) + cov2.getBestMatchRevMult(i));
            cov1.setCommonFwdMult(i, cov1.getCommonFwdMult(i) + cov2.getCommonFwdMult(i));
            cov1.setCommonRevMult(i, cov1.getCommonRevMult(i) + cov2.getCommonRevMult(i));
        }
        return cov1;
    }
}
