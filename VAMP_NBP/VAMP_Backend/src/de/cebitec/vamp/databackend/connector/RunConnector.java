package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.SQLStatements;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for the connection to the RUN domain of the database.
 *
 * @author ddoppmeier
 * @deprecated Since the RUN domain has been excluded this class is not needed anymore!
 */
@Deprecated
public class RunConnector {

    private long runID;
    private long trackID;
    private Connection con;

    @Deprecated
    RunConnector(long runID, long trackID) {
        this.runID = runID;
        this.trackID = trackID;
        con = ProjectConnector.getInstance().getConnection();
    }

    @Deprecated
    public HashMap<String, Integer> getReadnameToSeqIDMapping() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Creating mapping from readname to sequence id");

//        try {
//
//            PreparedStatement stm = con.prepareStatement(SQLStatements.FETCH_READNAME_SEQUENCEID_MAPPING);
//            stm.setLong(1, runID);
//            ResultSet rs = stm.executeQuery();

        map = new HashMap<String, Integer>();
//            while(rs.next()){
//                map.put(rs.getString("readname"), rs.getInt("seqID"));
//            }
//
//            rs.close();
//            stm.close();
//
//        } catch (SQLException ex) {
//            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
//        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished creating of mapping from readname to sequence id");

        return map;
    }

    @Deprecated
    public int getNumberOfReads() {
        int num = 0;

//        try {
//            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_READS_FOR_TRACK);
//            fetch.setLong(1, this.runID);
//
//            ResultSet rs = fetch.executeQuery();
//            if (rs.next()) {
//                num = rs.getInt("NUM");
//            }
//
//        } catch (SQLException ex) {
//            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return num;
    }

    @Deprecated
    public int getNumberOfReadsCalculate() {
        int num = 0;

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_OF_MAPPINGS_FOR_TRACK_CALCULATE);
            fetch.setLong(1, this.runID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }

        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Deprecated
    public int getNumberOfUniqueSequences() {
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK);
            fetch.setLong(1, this.runID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }


    public int getNumberOfUniqueSequencesCalculate() {
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE);
            fetch.setLong(1, this.runID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RunConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }
}
