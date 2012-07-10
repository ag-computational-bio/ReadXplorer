package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple helper class to receive all the Mappings belonging to a track
 * @author kstaderm
 */
public class GetMappingsFromTrack {

    /**
     * Receives all the mappings belonging to the given trackID.
     * @param trackID the ID of the track the received mappings should be from
     * @return list of mappings
     */
    public List<PersistantMapping> loadMappingsByTrackID(final int trackID) {

        Connection con = ProjectConnector.getInstance().getConnection();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);

        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.LOAD_MAPPINGS_BY_TRACK_ID);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.MAPPING_ID);
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int seqId = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);


                PersistantMapping mapping = new PersistantMapping(id, start, stop, trackID,
                        direction, count, errors, seqId, isBestMapping);
                mappings.add(mapping);
            }
            rs.close();
            fetch.close();

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from database...", currentTimestamp);

        return mappings;
    }
    
    /**
     * Receives all the mappings belonging to the given trackID. In order to
     * save space only Start, Stop and Direction are received by this method.
     * If you need the full set of information use the loadMappingsByTrackID(final int trackID)
     * method.
     * @param trackID the ID of the track the received mappings should be from
     * @return list of mappings
     */
    public List<PersistantMapping> loadReducedMappingsByTrackID(final int trackID) {

        Connection con = ProjectConnector.getInstance().getConnection();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);

        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.LOAD_MAPPINGS_BY_TRACK_ID);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);


                PersistantMapping mapping = new PersistantMapping(start, stop, direction);
                mappings.add(mapping);
            }
            rs.close();
            fetch.close();

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from database...", currentTimestamp);

        return mappings;
    }
}

