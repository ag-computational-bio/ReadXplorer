package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author -Rolf Hilker-
 * 
 * This coverage thread should be used for analyses, but not for visualizing data.
 * The thread carries out the database querries to receive coverage for a certain interval.
 */
public class CoverageThreadAnalyses extends Thread implements RequestThreadI {

    private long trackID;
    private long trackID2;
    private Connection con;
    private ConcurrentLinkedQueue<GenomeRequest> requestQueue;
    private PersistantCoverage currentCov;
    private double requestCounter;

    public CoverageThreadAnalyses(List<Integer> trackIds){
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<GenomeRequest>();
        con = ProjectConnector.getInstance().getConnection();
        requestCounter = 0;

        // do id specific stuff
        switch (trackIds.size()){
            case 1: singleCoverageThread(trackIds.get(0)); break;
            case 2: doubleCoverageThread(trackIds.get(0), trackIds.get(1)); break;
            default: throw new UnsupportedOperationException("More than two tracks not supported yet.");
        }
    }

    private void singleCoverageThread(long trackID){
        this.trackID = trackID;
        trackID2 = 0;
        currentCov = new PersistantCoverage(0, 0);
    }

    private void doubleCoverageThread(long trackID,long trackID2){
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new PersistantCoverage(0, 0,true);
    }

    @Override
    public void addRequest(GenomeRequest request) {
        requestQueue.add(request);
    }

    private PersistantCoverage loadCoverage(GenomeRequest request) {
        int from = request.getFrom();
        int to = request.getTo();

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.setTwoTracks(false);
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            fetch.setLong(3, trackID);

            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs.next()) {
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
                //   counter++;
                //best match cov
                cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
                //complete cov
                cov.setCommonFwdMult(pos, rs.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setCommonRevMult(pos, rs.getInt(FieldNames.COVERAGE_N_RV_MULT));
                cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));
                //perfect cov
                cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

            }
            fetch.close();
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return cov;
    }
    
    /**
     * Fetches the best match coverage of an interval of a certain track.
     * @param request the coverage request to carry out
     * @return the best match coverage of an interval of a certain track.
     */
    private PersistantCoverage loadCoverageBest(GenomeRequest request) {
        int from = request.getFrom();
        int to = request.getTo();

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.setTwoTracks(false);
        try {
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
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return cov;
    }
    
    
    private PersistantCoverage loadCoverage2(GenomeRequest request) {
        int from = request.getFrom();
        int to = request.getTo();

        PersistantCoverage cov = new PersistantCoverage(from, to, true);
        cov.setTwoTracks(true);
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);
            PreparedStatement fetch2 = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            fetch.setLong(3, trackID);
            fetch2.setInt(1, from);
            fetch2.setInt(2, to);
            fetch2.setLong(3, trackID2);
            ResultSet rs2 = fetch2.executeQuery();
            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs2.next()) {
                int pos = rs2.getInt(FieldNames.COVERAGE_POSITION);
                //coverage of Track2
                cov.setCommonFwdMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setCommonRevMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

            }
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
            fetch2.close();
            fetch.close();
            rs.close();
            rs2.close();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return cov;
    }

    @Override
    public void run() {
        
        while (!interrupted()) {

            GenomeRequest request = requestQueue.poll();
            if (request != null) {
                if (!currentCov.coversBounds(request.getFrom(), request.getTo())) {
                    requestCounter++;
                    if (trackID2 != 0) {
                        currentCov = this.loadCoverage2(request); //at the moment we only need the complete coverage here
                    } else {
                        if (request.getDesiredCoverage() == Properties.COMPLETE_COVERAGE) {
                            currentCov = this.loadCoverage(request);
                        } else if (request.getDesiredCoverage() == Properties.BEST_MATCH_COVERAGE) {
                            currentCov = this.loadCoverageBest(request);
                        } //else request.getDesiredCoverage() == Properties.PERFECT_COVERAGE does not exist yet, as it is not needed yet
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
