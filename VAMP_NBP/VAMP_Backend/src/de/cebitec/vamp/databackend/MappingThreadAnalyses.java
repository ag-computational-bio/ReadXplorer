package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.util.Pair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
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
    
//    private static int FIXED_INTERVAL_LENGTH = 1000;

    private int trackId;
    private Connection con;
    private ConcurrentLinkedQueue<GenomeRequest> requestQueue;
    private Pair<PersistantFeature, Collection<PersistantMapping>> currentMappings;
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

    private Collection<PersistantMapping> loadMappings(GenomeRequest request) {
        HashMap<Long, PersistantMapping> mappings = new HashMap<Long, PersistantMapping>();
        int from = request.getFrom();
        int to = request.getTo();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_WITHOUT_DIFFS);
            fetch.setLong(1, from);
            fetch.setLong(2, to);
            fetch.setLong(3, from);
            fetch.setLong(4, to);
            fetch.setLong(5, trackId);

            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs.next()) {
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

                // add new mapping if not exists
                if (!mappings.containsKey(mapping.getId())) {
                    mappings.put(mapping.getId(), mapping);
                }

            }
            rs.close();
            fetch.close();

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return mappings.values();
    }

    @Override
    public void run() {
        
        while (!interrupted()) {

            GenomeRequest request = requestQueue.poll();
            if (request != null) {
                this.requestCounter++;
                this.currentMappings = new Pair<PersistantFeature, Collection<PersistantMapping>>(request.getFeature(), this.loadMappings(request));
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
