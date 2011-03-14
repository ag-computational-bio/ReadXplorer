package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmei
 */
public class CoverageThread extends Thread {

    private long trackID;
    private long trackID2;
    private Connection con;
    private ConcurrentLinkedQueue<CoverageRequest> requestQueue;
    private PersistantCoverage currentCov;
    private int coveredWidth;
    private CoverageRequest latestRequest;
    private double requestCounter;
    private double skippedCounter;

    public CoverageThread(List<Long> trackIds){
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<CoverageRequest>();
        con = ProjectConnector.getInstance().getConnection();
        coveredWidth = 25000;
        requestCounter = 0;
        skippedCounter = 0;

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
    
     public void setCoveredWidth(int coveredWidth) {
        this.coveredWidth = coveredWidth;
    }

    private int calcCenterLeft(CoverageRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int result = centerMiddle - coveredWidth;
        return result;
    }

    private int calcCenterRight(CoverageRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int result = centerMiddle + coveredWidth;
        return result;
    }

    private int calcCenterMiddle(CoverageRequest request) {
        return (request.getFrom() + request.getTo()) / 2;
    }

    public void addCoverageRequest(CoverageRequest request) {
        latestRequest = request;
        requestQueue.add(request);
    }

    private boolean matchesLatestRequestBounds(CoverageRequest request) {
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);

        // rounding error somewhere....
        if (currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1) {
            return true;
        } else {
            return false;
        }
    }

    private PersistantCoverage loadCoverage(CoverageRequest request) {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

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

                cov.setBmFwMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBmFwNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBmRvMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBmRvNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
                //complete cov
                cov.setnFwMult(pos, rs.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setnFwNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setnRvMult(pos, rs.getInt(FieldNames.COVERAGE_N_RV_MULT));
                cov.setnRvNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));
                //perfect cov
                cov.setzFwMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
                cov.setzFwNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
                cov.setzRvMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
                cov.setzRvNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

            }
            fetch.close();
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return cov;
    }

    private PersistantCoverage loadCoverage2(CoverageRequest request) {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

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
                cov.setNFwMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setNRvMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

            }
            while (rs.next()) {
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);

                //check if cov of track 2 exists at position
                int nFwMultTrack2 = cov.getNFwMultTrack2(pos);
                int nRvMultTrack2 = cov.getNRvMultTrack2(pos);
                int nFwMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
                int nRvMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);

                //we just set coverage of the diff if cov of  track 2 or track 1 exist
                if (nFwMultTrack1 != 0 && nFwMultTrack2 != 0) {
                    cov.setnFwMult(pos, Math.abs(nFwMultTrack1 - nFwMultTrack2));
                }
                if (nRvMultTrack1 != 0 && nRvMultTrack2 != 0) {
                    cov.setnRvMult(pos, Math.abs(nRvMultTrack1 - nRvMultTrack2));
                }

                cov.setNFwMultTrack1(pos, nFwMultTrack1);
                cov.setNRvMultTrack1(pos, nRvMultTrack1);

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

            CoverageRequest r = requestQueue.poll();
            if (r != null) {
                if (!currentCov.coversBounds(r.getFrom(), r.getTo())) {
                    requestCounter++;
                    if (matchesLatestRequestBounds(r)) {
                        if (trackID2 != 0) {
                            currentCov = this.loadCoverage2(r);
                        } else {
                            currentCov = this.loadCoverage(r);
                        }
                    } else {
                        skippedCounter++;
                    }
                }
                if (matchesLatestRequestBounds(r)) {
                    r.getSender().receiveCoverage(currentCov);
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoverageThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
}
