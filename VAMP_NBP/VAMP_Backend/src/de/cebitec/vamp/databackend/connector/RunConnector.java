package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.H2SQLStatements;
import de.cebitec.vamp.databackend.SQLStatements;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class RunConnector {

    private long runID;
    private Connection con;

    RunConnector(long runID){
        this.runID = runID;
        con = ProjectConnector.getInstance().getConnection();
    }

    public HashMap<String, Integer> getReadnameToSeqIDMapping(){
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Creating mapping from readname to sequence id");

        try {

            PreparedStatement stm = con.prepareStatement(SQLStatements.FETCH_READNAME_SEQUENCEID_MAPPING);
            stm.setLong(1, runID);
            ResultSet rs = stm.executeQuery();

            map = new HashMap<String, Integer>();
            while(rs.next()){
                map.put(rs.getString("readname"), rs.getInt("seqID"));
            }

            rs.close();
            stm.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating of mapping from readname to sequence id");

        return map;
    }

    public int getNumberOfReads(){
        int num = 0;

        try {
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_OF_READS_FOR_RUN);
            fetch.setLong(1, runID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }

        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

        public int getNumberOfReadsCalculate(){
        int num = 0;

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_OF_READS_FOR_RUN);
            fetch.setLong(1, runID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }

        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    public int getNumberOfUniqueSequences(){
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_RUN);
            fetch.setLong(1, runID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }


    public int getNumberOfUniqueSequencesCalculate(){
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_RUN);
            fetch.setLong(1, runID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    public void updateTableRun(int numOfReads, int numOfUniqueSeq){
      try {
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.UPDATE_RUN_VALUES);

            fetch.setInt(1, numOfReads);
            fetch.setInt(1, numOfUniqueSeq);
            fetch.setLong(3, runID);
            
        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
