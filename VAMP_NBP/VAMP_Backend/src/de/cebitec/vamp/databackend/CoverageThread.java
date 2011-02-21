package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmei
 */
public class CoverageThread extends Thread{

    private long trackID;
    private long trackID2;
    private Connection con;
    private ConcurrentLinkedQueue<CoverageRequest> requestQueue;
    private PersistantCoverage currentCov;
    private int coveredWidth;
    private CoverageRequest latestRequest;

    private double requestCounter;
    private double skippedCounter;

    public CoverageThread(long trackID){
        super();
        this.trackID = trackID;
     //   trackID2 = 0;
        this.requestQueue = new ConcurrentLinkedQueue<CoverageRequest>();
        con = ProjectConnector.getInstance().getConnection();
        currentCov = new PersistantCoverage(0, 0);
        coveredWidth = 25000;
        requestCounter = 0;
        skippedCounter = 0;
    }

       public CoverageThread(long trackID,long trackID2){
        super();
        this.trackID = trackID;
        this.trackID2 = trackID2;
        this.requestQueue = new ConcurrentLinkedQueue<CoverageRequest>();
        con = ProjectConnector.getInstance().getConnection();
        currentCov = new PersistantCoverage(0, 0,true);
        coveredWidth = 25000;

        requestCounter = 0;
        skippedCounter = 0;
    }
    private int calcCenterLeft(CoverageRequest request){
        int centerMiddle = calcCenterMiddle(request);
        int result = centerMiddle - coveredWidth;
        return result;
    }

    private int calcCenterRight(CoverageRequest request){
        int centerMiddle = calcCenterMiddle(request);
        int result = centerMiddle + coveredWidth;
        return result;
    }

    private int calcCenterMiddle(CoverageRequest request){
        return (request.getFrom()+request.getTo()) /2;
    }

    public void addCoverageRequest(CoverageRequest request){
        latestRequest = request;
        requestQueue.add(request);
    }

    private boolean matchesLatestRequestBounds(CoverageRequest request){
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);

        // rounding error somewhere....
        if(currentMiddle -1 <= latestMiddle && latestMiddle <= currentMiddle+1){
            return true;
        } else {
            return false;
        }
    }


    private PersistantCoverage loadCoverage(CoverageRequest request){
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
            fetch.setInt(1, from);
            fetch.setInt(2, to);
            fetch.setLong(3, trackID);

            ResultSet rs = fetch.executeQuery();
         //  int counter = 0;
            while(rs.next()){
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
             //   counter++;
                //best match cov
                cov.setTwoTracks(false);
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

    private PersistantCoverage loadCoverage2(CoverageRequest request){
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to,true);
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
            while(rs2.next()){
                int pos = rs2.getInt(FieldNames.COVERAGE_POSITION);
             //   counter++;
                cov.setTwoTracks(true);
                cov.setnFwMult(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setnFwNum(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setnRvMult(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));
                cov.setnRvNum(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_NUM));
                cov.setNFwMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setNRvMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

            }
            while(rs.next()){
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
             //   counter++;
                int nFWMult= cov.getnFwMult(pos);
                int nFWNum= cov.getnFwNum(pos);
                int nRvMult= cov.getnRvMult(pos);
                int nRvNum= cov.getnRvNum(pos);
                if(nFWMult != 0){
                cov.setnFwMult(pos, Math.abs(rs.getInt(FieldNames.COVERAGE_N_FW_MULT)-nFWMult));
                }
                if(nFWNum!=0){
                cov.setnFwNum(pos, Math.abs(rs.getInt(FieldNames.COVERAGE_N_FW_NUM)-nFWMult));
                }
                if(nRvMult!=0){
                cov.setnRvMult(pos, Math.abs(rs.getInt(FieldNames.COVERAGE_N_RV_MULT)-nRvMult));
                }
                if(nRvNum!=0){
                cov.setnRvNum(pos, Math.abs(rs.getInt(FieldNames.COVERAGE_N_RV_NUM)-nRvNum));
                }
                cov.setNFwMultTrack1(pos, rs.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setNRvMultTrack1(pos, rs.getInt(FieldNames.COVERAGE_N_RV_MULT));

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
    public void run(){

        while(!interrupted()){

            CoverageRequest r = requestQueue.poll();
            if(r != null){
                if(!currentCov.coversBounds(r.getFrom(), r.getTo())){
                    requestCounter++;
                    if(matchesLatestRequestBounds(r)){
                       if(trackID2 != 0){
                        currentCov = this.loadCoverage2(r);
                        if(currentCov == null){
                             Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "CoveradeThread null");
                        }
                      }else{
                     
                        currentCov = this.loadCoverage(r);
                      }
                    } else {
                        skippedCounter++;
                    }
                }
                    if(matchesLatestRequestBounds(r)){
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
