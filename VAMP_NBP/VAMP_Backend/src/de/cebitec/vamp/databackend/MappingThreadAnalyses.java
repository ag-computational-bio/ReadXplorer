package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author -Rolf Hilker-
 * 
 * This mapping thread should be used for analyses, but not for visualizing data.
 * The thread carries out the database querries to receive the mappings for a certain interval.
 */
public class MappingThreadAnalyses extends Thread implements RequestThreadI {

    private int trackId;
    private Connection con;
    private ConcurrentLinkedQueue<GenomeRequest> requestQueue;
    private List<PersistantMapping> currentMappings;
    private int nbRequests;
    private double requestCounter;

    public MappingThreadAnalyses(int trackId, int nbRequests) {
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<GenomeRequest>();
        this.con = ProjectConnector.getInstance().getConnection();
        this.requestCounter = 0;
        this.trackId = trackId;
        this.nbRequests = nbRequests;
    }

    @Override
    public void addRequest(GenomeRequest request) {
        requestQueue.add(request);
    }

    /**
     * Loads all mappings (without diffs) from the DB with start positions within 
     * the given interval of the reference genome.
     * @param request the genome request containing the requested genome interval
     * @return the list of mappings belonging to the given interval
     */
    private List<PersistantMapping> loadMappings(GenomeRequest request) {
        
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);
        
        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();
        int from = request.getFrom();
        int to = request.getTo();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_WITHOUT_DIFFS);
            fetch.setLong(1, from);
            fetch.setLong(2, to);
//            fetch.setLong(3, from);
//            fetch.setLong(4, to);
            fetch.setLong(3, trackId);

            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs.next()) {
                int currentTrackId = rs.getInt(FieldNames.MAPPING_TRACK);
                if (currentTrackId == this.trackId) {

                    int id = rs.getInt(FieldNames.MAPPING_ID);
                    int start = rs.getInt(FieldNames.MAPPING_START);
                    int stop = rs.getInt(FieldNames.MAPPING_STOP);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                    int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    int seqId = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                    boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);


                    PersistantMapping mapping = new PersistantMapping(id, start, stop, this.trackId,
                            direction, count, errors, seqId, isBestMapping);
                    mappings.add(mapping);
                }


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
     * Loads all mappings (without diffs) from the DB with ids within 
     * the given interval of the reference genome.
     * @param request the genome request containing the requested mapping id interval
     * @return the list of mappings belonging to the given mapping id interval sorted by mapping start
     */
    private List<PersistantMapping> loadMappingsById(GenomeRequest request) {
        
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);
        
        List<PersistantMapping> mappings = new ArrayList<PersistantMapping>();
        int from = request.getFrom();
        int to = request.getTo();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_WITHOUT_DIFFS2);
            fetch.setLong(1, from);
            fetch.setLong(2, to);
//            fetch.setLong(3, from);
//            fetch.setLong(4, to);
//            fetch.setLong(3, trackId);

            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs.next()) {
                int currentTrackId = rs.getInt(FieldNames.MAPPING_TRACK);
                if (currentTrackId == this.trackId) {

                    int id = rs.getInt(FieldNames.MAPPING_ID);
                    int start = rs.getInt(FieldNames.MAPPING_START);
                    int stop = rs.getInt(FieldNames.MAPPING_STOP);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                    int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    int seqId = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                    boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);


                    PersistantMapping mapping = new PersistantMapping(id, start, stop, this.trackId,
                            direction, count, errors, seqId, isBestMapping);
                    mappings.add(mapping);
                }


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

    
    @Override
    public void run() {
        
        while (!interrupted()) {

            GenomeRequest request = requestQueue.poll();
            if (request != null) {
                this.requestCounter++;
                this.currentMappings = this.loadMappingsById(request);
                request.getSender().receiveData(currentMappings);
                
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
