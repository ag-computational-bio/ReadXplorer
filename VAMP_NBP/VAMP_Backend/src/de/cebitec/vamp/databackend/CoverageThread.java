package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.VisualisationUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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
public class CoverageThread extends RequestThread {

    private long trackID;
    private long trackID2;
    private List<PersistantTrack> tracks;
    private Connection con;
    ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private CoverageAndDiffResultPersistant currentCov;
//    private double requestCounter = 0;
//    private double skippedCounter = 0;
    private boolean isDbUsed = false;
    private PersistantReference referenceGenome;

    /**
     * Thread for retrieving the coverage for a list of tracks either from the
     * db or directly from their mapping files.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their coverage
     * should be combined in the results
     */
    public CoverageThread(List<PersistantTrack> tracks, boolean combineTracks) {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        con = ProjectConnector.getInstance().getConnection();

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
        this.currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, false);
        this.isDbUsed = this.tracks.get(0).isDbUsed();
    }

    private void doubleCoverageThread(long trackID, long trackID2) {
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0, true), null, null, false);
        this.isDbUsed = this.tracks.get(0).isDbUsed() || this.tracks.get(1).isDbUsed();
    }

    private void multipleCoverageThread() {
        this.trackID = 0;
        this.trackID2 = 0;
        currentCov = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, false);
        for (PersistantTrack track : this.tracks) {
            if (track.isDbUsed()) {
                this.isDbUsed = true;
                break;
            }
        }
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
        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId());
        CoverageAndDiffResultPersistant result = externalDataReader.getCoverageFromBam(
                this.referenceGenome, request);
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

        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId());
        result = externalDataReader.getCoverageAndReadStartsFromBam(referenceGenome, request, false);
        externalDataReader.close();
        return result;
    }
    
    /**
     * Fetches the read starts and the coverage either from the DB or a bam file
     * for multiple tracks and combines all the data.
     * @param request the request for which the read starts are needed
     * @return the coverage and diff result containing the read starts, the
     * coverage and no diffs and gaps
     * @param request
     * @return 
     */
    CoverageAndDiffResultPersistant loadReadStartsAndCoverageMultiple(IntervalRequest request) throws SQLException {
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

        CoverageAndDiffResultPersistant result;
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        PersistantCoverage cov = new PersistantCoverage(from, to);
        PersistantCoverage readStarts = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();
        readStarts.incArraysToIntervalSize();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        if (this.isDbUsed) { //Note: uniqueness of mappings cannot be querried from db!!!

            result = this.loadCoverageMultiple(request);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);
            try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_READ_STARTS_BY_TRACK_ID_AND_REF_INTERVAL)) {
                fetch.setLong(1, trackID);
                fetch.setLong(2, from);
                fetch.setLong(3, to);

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {

                    int mismatches = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);

                    //only add desired mappings to mappings
                    if (readClassParams.isPerfectMatchUsed() && mismatches == 0
                            || readClassParams.isBestMatchUsed() && isBestMapping
                            || readClassParams.isCommonMatchUsed() && !isBestMapping) {

                        int start = rs.getInt(FieldNames.MAPPING_START);
                        int stop = rs.getInt(FieldNames.MAPPING_STOP);
                        boolean isFwdStrand = rs.getByte(FieldNames.MAPPING_DIRECTION) == SequenceUtils.STRAND_FWD;
                        int numReplicates = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);

                        if (request.getReadClassParams().isPerfectMatchUsed()) {//perfect match readStarts
                            if (isFwdStrand) {
                                readStarts.setPerfectFwdMult(start, readStarts.getPerfectFwdMult(start) + numReplicates);
                            } else if (stop <= readStarts.getRightBound()) {
                                readStarts.setPerfectRevMult(stop, readStarts.getPerfectRevMult(stop) + numReplicates);
                            }
                        }
                        if (request.getReadClassParams().isBestMatchUsed()) {//best match readStarts
                            if (isFwdStrand) {
                                readStarts.setBestMatchFwdMult(start, readStarts.getBestMatchFwdMult(start) + numReplicates);
                            } else if (stop <= readStarts.getRightBound()) {
                                readStarts.setBestMatchRevMult(stop, readStarts.getBestMatchRevMult(stop) + numReplicates);
                            }
                        }
                        if (request.getReadClassParams().isCommonMatchUsed()) {//complete readStarts
                            if (isFwdStrand) {
                                readStarts.setCommonFwdMult(start, readStarts.getCommonFwdMult(start) + numReplicates);
                            } else if (stop <= readStarts.getRightBound()) {
                                readStarts.setCommonFwdMult(stop, readStarts.getCommonRevMult(stop) + numReplicates);
                            }
                        }
                    }
                }
                rs.close();

                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from database...", currentTimestamp);

            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }

            result.setReadStarts(readStarts);

        } else { //handle retrieving of data from other source than a DB
            if (tracks.size() > 1) {
                for (int i = 0; i < this.tracks.size(); ++i) {
                    if (!this.tracks.get(i).isDbUsed()) {
                        CoverageAndDiffResultPersistant intermedRes = this.getCoverageAndReadStartsFromFile(request, tracks.get(i));
                        cov = this.mergeMultCoverages(cov, intermedRes.getCoverage());
                        readStarts = this.mergeMultCoverages(readStarts, intermedRes.getReadStarts());
                    }
                }
                result = new CoverageAndDiffResultPersistant(cov, null, null, false);
                result.setReadStarts(readStarts);
            } else {
                result = this.getCoverageAndReadStartsFromFile(request, tracks.get(0));
            }

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
    CoverageAndDiffResultPersistant loadCoverageDouble(IntervalRequest request) throws SQLException {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
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
            IntervalRequest newRequest = new IntervalRequest(
                    request.getFrom(), 
                    request.getTo(), 
                    request.getTotalFrom(), 
                    request.getTotalTo(), 
                    request.getSender(), 
                    request.isDiffsAndGapsNeeded(),
                    Properties.NORMAL, PersistantCoverage.TRACK2);
            cov = this.getCoverageAndDiffsFromFile(newRequest, tracks.get(1)).getCoverage();
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
            IntervalRequest newRequest = new IntervalRequest(
                    request.getFrom(),
                    request.getTo(),
                    request.getTotalFrom(),
                    request.getTotalTo(), 
                    request.getSender(),
                    request.isDiffsAndGapsNeeded(),
                    Properties.NORMAL, PersistantCoverage.TRACK1);
            PersistantCoverage intermedCov = this.getCoverageAndDiffsFromFile(newRequest, tracks.get(0)).getCoverage();
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
        
        return new CoverageAndDiffResultPersistant(cov, null, null, false);
    }

    /**
     * Loads the coverage for multiple tracks in one PersistantCoverage object.
     * This is more efficient, than storing it in several PersistantCoverage
     * objects, each for one track.
     * @param request the genome request to carry out
     * @return the coverage of multiple track combined in one PersistantCoverage
     * object.
     */
    CoverageAndDiffResultPersistant loadCoverageMultiple(IntervalRequest request) throws SQLException {
        int from = request.getTotalFrom(); // calcCenterLeft(request);
        int to = request.getTotalTo(); // calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
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
//        int tmpHighestCov = 0;
            while (rs.next()) {

                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
                //perfect cov
                if (request.getReadClassParams().isPerfectMatchUsed()) {
                    cov.setPerfectFwdMult(pos, cov.getPerfectFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                    cov.setPerfectFwdNum(pos, cov.getPerfectFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                    cov.setPerfectRevMult(pos, cov.getPerfectRevMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                    cov.setPerfectRevNum(pos, cov.getPerfectRevNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));
                }
                //best match cov
                if (request.getReadClassParams().isBestMatchUsed()) {
                    cov.setBestMatchFwdMult(pos, cov.getBestMatchFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                    cov.setBestMatchFwdNum(pos, cov.getBestMatchFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                    cov.setBestMatchRevMult(pos, cov.getBestMatchRevMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                    cov.setBestMatchRevNum(pos, cov.getBestMatchRevNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
                }
                //complete cov and highest coverage in interval calculation
                if (request.getReadClassParams().isCommonMatchUsed()) {
                    int covCommonFwdMult = cov.getCommonFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                    int covCommonRevMult = cov.getCommonRevMult(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//                    if (pos >= request.getFrom() && pos <= request.getTo()
//                            && (tmpHighestCov < covCommonFwdMult || tmpHighestCov < covCommonRevMult)) {
//                        tmpHighestCov = covCommonFwdMult < covCommonRevMult ? covCommonRevMult : covCommonFwdMult;
//                        cov.setHighestCoverage(tmpHighestCov);
//                    }
                    cov.setCommonFwdMult(pos, covCommonFwdMult);
                    cov.setCommonFwdNum(pos, cov.getCommonFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                    cov.setCommonRevMult(pos, covCommonRevMult);
                    cov.setCommonRevNum(pos, cov.getCommonRevNum(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_NUM));
                }

            }
            fetch.close();
            rs.close();
        }
        
        //get coverage of all direct tracks
        if (tracks.size() > 1) {
            for (int i = 0; i < this.tracks.size(); ++i) {
                if (!this.tracks.get(i).isDbUsed()) {
                    CoverageAndDiffResultPersistant intermedRes = this.getCoverageAndDiffsFromFile(request, tracks.get(i));
                    cov = this.mergeMultCoverages(cov, intermedRes.getCoverage());
                    diffs.addAll(intermedRes.getDiffs());
                    gaps.addAll(intermedRes.getGaps());
                }
            }
        } else {
            if (!this.tracks.get(0).isDbUsed()) {
                CoverageAndDiffResultPersistant result = this.getCoverageAndDiffsFromFile(request, tracks.get(0));
                cov = result.getCoverage();
                diffs = result.getDiffs();
                gaps = result.getGaps();
            }
        }

        return new CoverageAndDiffResultPersistant(cov, diffs, gaps, request.isDiffsAndGapsNeeded());
    }

    /**
     * Loads the coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     * @param request the request with more than one track
     * @return The coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     */
    //TODO: loadCoverageMutliple2 currently only works for DB tracks
    PersistantCoverage[] loadCoverageMutliple2(IntervalRequest request) throws SQLException {
        int from = request.getTotalFrom(); // calcCenterLeft(request);
        int to = request.getTotalTo(); // calcCenterRight(request);

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
                //perfect cov
                if (request.getReadClassParams().isPerfectMatchUsed()) {
                    cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                    cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                    cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                    cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));
                }
                //best match cov
                if (request.getReadClassParams().isBestMatchUsed()) {
                    cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                    cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                    cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                    cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
                }
                //complete cov and highest coverage in interval calculation
                if (request.getReadClassParams().isCommonMatchUsed()) {
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
    
    /**
     * Returns the diffs for a given interval in the current track from the
     * database.
     * @param request the genome request containing the requested genome
     * interval
     * @return the collection of diffs for this interval
     */
    CoverageAndDiffResultPersistant loadDiffsAndGaps(IntervalRequest request) {

        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        if (from < to) {
            try {
                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_DIFFS_AND_GAPS_FOR_INTERVAL);
                fetch.setInt(1, from);
                fetch.setInt(2, to);
                fetch.setLong(3, trackID);

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt(FieldNames.DIFF_POSITION);
                    char base = rs.getString(FieldNames.DIFF_BASE).charAt(0);
                    int type = rs.getInt(FieldNames.DIFF_TYPE);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    boolean isForwardStrand = (direction == SequenceUtils.STRAND_FWD);
                    int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);

                    if (type == 1) { //1 = diffs
                        diffs.add(new PersistantDiff(position, base, isForwardStrand, count));
                    } else { //0 = gaps
                        int order = rs.getInt(FieldNames.DIFF_GAP_ORDER);
                        gaps.add(new PersistantReferenceGap(position, base, order, isForwardStrand, count));
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, ex.getMessage());
                Logger.getLogger(TrackConnector.class.getName()).log(Level.INFO, null, ex);
            }
        }

        return new CoverageAndDiffResultPersistant(new PersistantCoverage(from, to), diffs, gaps, true);
    }

    @Override
    public void run() {

        try {
            while (!interrupted()) {
                IntervalRequest request = requestQueue.poll();
                if (request != null) {
                    if (request.getDesiredData() == Properties.DIFFS) {
                        //if only diffs are required from the db load them
                        currentCov = this.loadDiffsAndGaps(request);

                    //otherwise load the appropriate coverage (and diffs)
                    } else if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo()) 
                            || (!currentCov.isDiffsAndGapsUsed() && request.isDiffsAndGapsNeeded())) {
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
                        request.getSender().receiveData(currentCov);
                    }
                    else {
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
        } catch (SQLException e) {
            if (e.getMessage().contains("Out of memory")) {
                VisualisationUtils.displayOutOfMemoryError(JOptionPane.getRootFrame());
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
