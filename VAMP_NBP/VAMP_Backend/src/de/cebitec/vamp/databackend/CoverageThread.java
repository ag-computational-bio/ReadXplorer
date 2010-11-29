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
        this.requestQueue = new ConcurrentLinkedQueue<CoverageRequest>();
        con = ProjectConnector.getInstance().getConnection();
        currentCov = new PersistantCoverage(0, 0);
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
            int counter = 0;
            while(rs.next()){
                int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
                counter++;
                cov.setBmFwMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
                cov.setBmFwNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
                cov.setBmRvMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
                cov.setBmRvNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));

                cov.setnFwMult(pos, rs.getInt(FieldNames.COVERAGE_N_FW_MULT));
                cov.setnFwNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
                cov.setnRvMult(pos, rs.getInt(FieldNames.COVERAGE_N_RV_MULT));
                cov.setnRvNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

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

    @Override
    public void run(){

        while(!interrupted()){

            CoverageRequest r = requestQueue.poll();
            if(r != null){
                if(!currentCov.coversBounds(r.getFrom(), r.getTo())){
                    requestCounter++;
                    if(matchesLatestRequestBounds(r)){
                        currentCov = this.loadCoverage(r);
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
