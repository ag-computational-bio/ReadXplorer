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
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This coverage thread should be used for analyses, but not for visualizing data.
 * The thread carries out the database querries to receive coverage for a certain interval.
 *
 * @author -Rolf Hilker-
 */
public class CoverageThreadAnalyses extends RequestThread {

    private PersistantTrack track;
    private PersistantTrack track2;
    private List<PersistantTrack> tracks;
    private Connection con;
    private ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private CoverageAndDiffResultPersistant currentCov;
    private PersistantReference referenceGenome;
    private boolean isDbUsed;

    /**
     * Thread for retrieving the coverage for a list of tracks either from the
     * db or directly from their mapping files.
     * @param tracks the tracks handled here
     * @param combineTracks true, if more than one track is added and their
     * coverage should be combined in the results
     */
    public CoverageThreadAnalyses(List<PersistantTrack> tracks, boolean combineTracks) {
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.con = ProjectConnector.getInstance().getConnection();
        this.currentCov = new CoverageAndDiffResultPersistant(null, null, null, false, 0, 0);

        // do id specific stuff
        switch (tracks.size()){
            case 1: singleCoverageThread(tracks.get(0)); break;
            case 2: doubleCoverageThread(tracks.get(0), tracks.get(1)); break;
            default: multipleCoverageThread(tracks);
        }
    }

    private void singleCoverageThread(PersistantTrack track){
        this.track = track;
        this.track2 = null;
        this.isDbUsed = track.isDbUsed();
    }

    private void doubleCoverageThread(PersistantTrack track, PersistantTrack track2){
        this.track = track;
        this.track2 = track2;
        this.isDbUsed = track.isDbUsed() || track2.isDbUsed();
    }
    
    private void multipleCoverageThread(List<PersistantTrack> multipleTracks) {
        tracks = multipleTracks;
        for (PersistantTrack t : tracks) {
            if (t.isDbUsed()) {
                this.isDbUsed = true;
            }
        }
    }

    @Override
    public void addRequest(IntervalRequest request) {
        requestQueue.add(request);
    }
    
    /**
     * Fetches the coverage and (if desired) also the diffs and gaps for a given
     * interval.
     * @param request the request to carry out
     * @param track the track for which the request should be carried out
     * @return the container with the desired data
     */
    private CoverageAndDiffResultPersistant getCoverageAndDiffsFromFile(IntervalRequest request, PersistantTrack track) {
        boolean diffsAndGapsNeeded = request instanceof CoverageAndDiffRequest;
        File file = new File(track.getFilePath());
        if (this.referenceGenome == null) {
            ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
            this.referenceGenome = refConnector.getRefGenome();
        }
        SamBamFileReader externalDataReader = new SamBamFileReader(file, track.getId());
        CoverageAndDiffResultPersistant result = externalDataReader.getCoverageFromBam(
                this.referenceGenome, request, diffsAndGapsNeeded);
        externalDataReader.close();
        return result;

    }

    /**
     * Loads the fwd and rev coverage combined for one single track. Meaning, it
     * contains the absoulte coverage values for each position of the track
     * included in the request.
     * @param request the request to carry out for one track and a given interval
     * @return the PersistantCoverage for the given interval and the track
     * @throws SQLException
     */
    private CoverageAndDiffResultPersistant loadCoverage(IntervalRequest request) {
        int from = request.getFrom();
        int to = request.getTo();

        CoverageAndDiffResultPersistant result;
        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.incArraysToIntervalSize();
        
        if (this.isDbUsed) {
            try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK)) {
                fetch.setInt(1, this.track.getId());
                fetch.setInt(2, from);
                fetch.setInt(3, to);
                ResultSet rs = fetch.executeQuery();
                int tmpHighestCov = 0;
                while (rs.next()) {
                    int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
                    //   counter++;
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

                        if (tmpHighestCov < covNFWMult || tmpHighestCov < covNRevMult) {
                            tmpHighestCov = covNFWMult < covNRevMult ? covNRevMult : covNFWMult;
                            cov.setHighestCoverage(tmpHighestCov);
                        }

                        cov.setCommonFwdMult(pos, covNFWMult);
                        cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                        cov.setCommonRevMult(pos, covNRevMult);
                        cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));
                    }

                }
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            result = new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
        } else {
            result = this.getCoverageAndDiffsFromFile(request, this.track);
        }
        return result;
    }
    
//    /**
//     * Fetches the best match coverage of an interval of a certain track.
//     * @param request the coverage request to carry out
//     * @return the best match coverage of an interval of a certain track.
//     */
//    private CoverageAndDiffResultPersistant loadCoverageBest(IntervalRequest request) {
//        int from = request.getFrom();
//        int to = request.getTo();
//
//        CoverageAndDiffResultPersistant result;
//        PersistantCoverage cov = new PersistantCoverage(from, to);
//        cov.incArraysToIntervalSize();
//        
//        if (this.isDbUsed) {
//            try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_BEST_FOR_INTERVAL)) {
//                fetch.setLong(1, track.getId());
//                fetch.setInt(2, from); 
//                fetch.setInt(3, to);
//
//                ResultSet rs = fetch.executeQuery();
//
//                while (rs.next()) {
//                    int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
//                    //perfect cov
//                    cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
//                    cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
//                    //best match cov
//                    cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
//                    cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
//                }
//                fetch.close();
//                rs.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//            }
//            result = new CoverageAndDiffResultPersistant(cov, null, null, false, from, to);
//        } else {
//            result = this.getCoverageAndDiffsFromFile(request, track);
//        }
//        return result;
//    }
    
    /**
     * Loads the coverage for a double track viewer, not combining the coverage.
     * @param request the request to carry out
     * @return 
     */
    private CoverageAndDiffResultPersistant loadCoverageDouble(IntervalRequest request) {
        int from = request.getFrom();
        int to = request.getTo();

        PersistantCoverage cov = new PersistantCoverage(from, to, true);
        cov.incDoubleTrackArraysToIntervalSize();
        cov.setTwoTracks(true);
        
        if (track2.isDbUsed()) {
            try (PreparedStatement fetch2 = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2)) {

                fetch2.setInt(1, from);
                fetch2.setInt(2, to);
                fetch2.setInt(3, track2.getId());
                ResultSet rs2 = fetch2.executeQuery();
                while (rs2.next()) {
                    int pos = rs2.getInt(FieldNames.COVERAGE_POSITION);
                    //coverage of Track2
                    cov.setCommonFwdMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                    cov.setCommonRevMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

                }
                rs2.close();
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            IntervalRequest newRequest = new IntervalRequest(request.getFrom(), request.getTo(), 
                    request.getSender(), PersistantCoverage.TRACK2);
            cov = this.getCoverageAndDiffsFromFile(newRequest, track2).getCoverage();
        }

        if (track.isDbUsed()) {
            try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2)) {
                fetch.setInt(1, from);
                fetch.setInt(2, to);
                fetch.setInt(3, track.getId());
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
                rs.close();

            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            IntervalRequest newRequest = new IntervalRequest(request.getFrom(), request.getTo(), 
                    request.getSender(), PersistantCoverage.TRACK1);
            PersistantCoverage intermedCov = this.getCoverageAndDiffsFromFile(newRequest, track).getCoverage();
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

    @Override
    public void run() {
        
        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            if (request != null) {
                if (!currentCov.getCoverage().coversBounds(request.getFrom(), request.getTo())) {
                    if (track2 != null) {
                        currentCov = this.loadCoverageDouble(request); //at the moment we only need the complete coverage here
                    } else {
                        if (request.getDesiredData() == Properties.NORMAL) {
                            currentCov = this.loadCoverage(request);
//                        } else if (request.getDesiredData() == Properties.BEST_MATCH_COVERAGE) {
//                            currentCov = this.loadCoverageBest(request);
                        } //else request.getDesiredData() == Properties.PERFECT_COVERAGE does not exist yet, as it is not needed yet
                    }
                }
                request.getSender().receiveData(currentCov);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoverageThreadAnalyses.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
}
