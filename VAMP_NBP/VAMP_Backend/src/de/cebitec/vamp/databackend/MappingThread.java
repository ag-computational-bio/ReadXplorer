package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This mapping thread should be used for analyses, but not for visualizing
 * data. The thread carries out the database querries to receive the mappings
 * for a certain interval.
 *
 * @author -Rolf Hilker-
 */
public class MappingThread extends RequestThread {

    public static int FIXED_INTERVAL_LENGTH = 1000;
    private int trackId;
    private Connection con;
    ConcurrentLinkedQueue<IntervalRequest> requestQueue;
    private List<PersistantMapping> currentMappings;
    private boolean isDbUsed;
    private final PersistantReference refGenome;
    private SamBamFileReader externalDataReader;

    /**
     * Creates a new mapping thread for carrying out mapping request either to a
     * database or a file.
     *
     * @param track the track for which this mapping thread is created
     */
    public MappingThread(PersistantTrack track) {
        super();
        // do general stuff
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector(track.getRefGenID());
        this.refGenome = refConnector.getRefGenome();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.con = ProjectConnector.getInstance().getConnection();
        this.trackId = track.getId();
        this.isDbUsed = track.isDbUsed();
        if (!this.isDbUsed) {
            this.externalDataReader = new SamBamFileReader(new File(track.getFilePath()), this.trackId);
        }
    }

    @Override
    public void addRequest(IntervalRequest request) {
        this.setLatestRequest(request);
        this.requestQueue.add(request);
    }

    /**
     * Loads all mappings (without diffs) from the DB with start positions
     * within the given interval of the reference genome.
     * @param request the genome request containing the requested genome
     * interval
     * @return the list of mappings belonging to the given interval
     */
    List<PersistantMapping> loadMappingsWithoutDiffs(IntervalRequest request) {

        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

        List<PersistantMapping> mappings = new ArrayList<>();
        int from = request.getFrom();
        int to = request.getTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        if (this.isDbUsed) { //Note: uniqueness of mappings cannot be querried from db!!!
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);
            try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_WITHOUT_DIFFS)) {
                fetch.setLong(1, from);
                fetch.setLong(2, to);
                fetch.setLong(3, trackId);

                ResultSet rs = fetch.executeQuery();
                //  int counter = 0;
                while (rs.next()) {
                    int currentTrackId = rs.getInt(FieldNames.MAPPING_TRACK);
                    if (currentTrackId == this.trackId) {
                        
                        int mismatches = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                        boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);
                        
                        //only add desired mappings to mappings
                        if (    readClassParams.isPerfectMatchUsed() && mismatches == 0 ||
                                readClassParams.isBestMatchUsed() && isBestMapping ||
                                readClassParams.isCommonMatchUsed() && !isBestMapping) {
                                
                            int id = rs.getInt(FieldNames.MAPPING_ID);
                            int start = rs.getInt(FieldNames.MAPPING_START);
                            int stop = rs.getInt(FieldNames.MAPPING_STOP);
                            boolean isFwdStrand = rs.getByte(FieldNames.MAPPING_DIRECTION) == SequenceUtils.STRAND_FWD;
                            int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                            int seqId = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);

                            PersistantMapping mapping = new PersistantMapping(id, start, stop, this.trackId,
                                    isFwdStrand, count, mismatches, seqId, isBestMapping);
                            mappings.add(mapping);
                        }
                    }
                }
                rs.close();
                
                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from database...", currentTimestamp);

            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } else { //handle retrieving of data from other source than a DB
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from file...", currentTimestamp);
            mappings = new ArrayList<>(externalDataReader.getMappingsFromBam(this.refGenome, request, false));
            currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from file...", currentTimestamp);
        }

        

        return mappings;
    }

    /**
     * Collects all mappings of the associated track for the interval described
     * by the request parameters. Mappings can only be obtained for one track
     * currently.
     * @param request the interval request containing the requested reference
     * interval
     * @return the collection of mappings for the given interval
     */
    List<PersistantMapping> loadMappingsWithDiffs(IntervalRequest request) {
        HashMap<Long, PersistantMapping> mappings = new HashMap<>();

        int from = request.getFrom();
        int to = request.getTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        if (from < to && from > 0 && to > 0) {
            if (this.isDbUsed) {
                try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK)) {

                    //determine readlength
                    //TODO: ensure this is only calculated when track id or db changed!
                    //TODO: fetch largest read length from db
//                    PreparedStatement fetchReadlength = con.prepareStatement(SQLStatements.GET_CURRENT_READLENGTH);
//                    fetchReadlength.setLong(1, trackID);
//                    ResultSet rsReadlength = fetchReadlength.executeQuery();
//
//                    int readlength = 1000;
//                    final int spacer = 10;
//                    if (rsReadlength.next()) {
//                        int start = rsReadlength.getInt(FieldNames.MAPPING_START);
//                        int stop = rsReadlength.getInt(FieldNames.MAPPING_STOP);
//                        readlength = stop - start + spacer;
//                    }
//                    fetchReadlength.close();


                    //mapping processing
                    
                    fetch.setLong(1, from - FIXED_INTERVAL_LENGTH);
                    fetch.setLong(2, to);
                    fetch.setLong(3, from);
                    fetch.setLong(4, to + FIXED_INTERVAL_LENGTH);
                    fetch.setLong(5, trackId);
                    fetch.setLong(6, from);
                    fetch.setLong(7, to);

                    ResultSet rs = fetch.executeQuery();
                    while (rs.next()) {
                        // mapping data
                        int mismatches = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                        boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);
                        
                        //only add desired mappings to mappings
                        if (       readClassParams.isPerfectMatchUsed() && mismatches == 0
                                || readClassParams.isBestMatchUsed() && isBestMapping
                                || readClassParams.isCommonMatchUsed() && !isBestMapping) {
                        
                            int mappingID = rs.getInt(FieldNames.MAPPING_ID);
                            int sequenceID = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                            int mappingTrack = rs.getInt(FieldNames.MAPPING_TRACK);
                            int start = rs.getInt(FieldNames.MAPPING_START);
                            int stop = rs.getInt(FieldNames.MAPPING_STOP);
                            byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                            boolean isForwardStrand = (direction == SequenceUtils.STRAND_FWD);
                            int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                            PersistantMapping m = new PersistantMapping(mappingID, start, stop, mappingTrack, isForwardStrand, count, mismatches, sequenceID, isBestMapping);

                            // add new mapping if not exists
                            if (!mappings.containsKey(m.getId())) {
                                mappings.put(m.getId(), m);
                            }

                            // diff data
                            String baseString = rs.getString(FieldNames.DIFF_BASE);
                            int position = rs.getInt(FieldNames.DIFF_POSITION);
                            int type = rs.getInt(FieldNames.DIFF_TYPE);
                            int gapOrder = rs.getInt(FieldNames.DIFF_GAP_ORDER);

                            // diff data may be null, if mapping has no diffs
                            if (baseString != null) {
                                char base = baseString.charAt(0);
                                if (type == 1) {
                                    PersistantDiff d = new PersistantDiff(position, base, isForwardStrand, count);
                                    mappings.get(m.getId()).addDiff(d);
                                } else if (type == 0) {
                                    PersistantReferenceGap g = new PersistantReferenceGap(position, base, gapOrder, isForwardStrand, count);
                                    mappings.get(m.getId()).addGenomeGap(g);
                                } else {
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown type for diff in database {0}", type);
                                }
                            }
                        }
                    }
                    rs.close();

                } catch (SQLException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex.getStackTrace());
                }
            } else { //handle retrieving of data from other source than a DB

                Collection<PersistantMapping> mappingList = externalDataReader.getMappingsFromBam(this.refGenome, request, true);
                Iterator<PersistantMapping> it = mappingList.iterator();
                while (it.hasNext()) {
                    PersistantMapping next = it.next();
                    mappings.put(next.getId(), next); //TODO: optimize, remove while loop
                }
            }
        }
        return new ArrayList<>(mappings.values());
    }

    /**
     * Loads all mappings (without diffs) from the DB with ids within the given
     * interval of the reference genome.
     * @param request the genome request containing the requested mapping id
     * interval
     * @return the list of mappings belonging to the given mapping id interval
     * sorted by mapping start
     */
    List<PersistantMapping> loadMappingsById(IntervalRequest request) {

        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);

        List<PersistantMapping> mappings = new ArrayList<>();
        int from = request.getFrom();
        int to = request.getTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_BY_ID_WITHOUT_DIFFS)) {
            fetch.setLong(1, from);
            fetch.setLong(2, to);

            ResultSet rs = fetch.executeQuery();
            //  int counter = 0;
            while (rs.next()) {
                int currentTrackId = rs.getInt(FieldNames.MAPPING_TRACK);
                if (currentTrackId == this.trackId) {

                    int mismatches = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);
                    
                    //only add desired mappings to mappings
                    if (readClassParams.isPerfectMatchUsed() && mismatches == 0
                            || readClassParams.isBestMatchUsed() && isBestMapping
                            || readClassParams.isCommonMatchUsed() && !isBestMapping) {
                        
                        int id = rs.getInt(FieldNames.MAPPING_ID);
                        int start = rs.getInt(FieldNames.MAPPING_START);
                        int stop = rs.getInt(FieldNames.MAPPING_STOP);
                        boolean isFwdStrand = rs.getByte(FieldNames.MAPPING_DIRECTION) == SequenceUtils.STRAND_FWD;
                        int count = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);
                        int seqId = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);

                        PersistantMapping mapping = new PersistantMapping(id, start, stop, this.trackId,
                                isFwdStrand, count, mismatches, seqId, isBestMapping);
                        mappings.add(mapping);
                    }
                }


            }
            rs.close();

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
     * @param request the request to carry out
     * @return list of mappings
     */
    public List<PersistantMapping> loadReducedMappings(IntervalRequest request) {

        Connection connection = ProjectConnector.getInstance().getConnection();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

        List<PersistantMapping> mappings = new ArrayList<>();
        ParametersReadClasses readClassParams = request.getReadClassParams();
        if (this.isDbUsed) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from database...", currentTimestamp);
            try (PreparedStatement fetch = connection.prepareStatement(SQLStatements.LOAD_REDUCED_MAPPINGS_BY_TRACK_ID_AND_INTERVAL)) {
                fetch.setLong(1, trackId);
                fetch.setLong(2, request.getFrom());
                fetch.setLong(3, request.getTo());

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    
                    int mismatches = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    boolean isBestMapping = rs.getBoolean(FieldNames.MAPPING_IS_BEST_MAPPING);

                    //only add desired mappings to mappings
                    if (readClassParams.isPerfectMatchUsed() && mismatches == 0
                            || readClassParams.isBestMatchUsed() && isBestMapping
                            || readClassParams.isCommonMatchUsed() && !isBestMapping) {
                        
                        int start = rs.getInt(FieldNames.MAPPING_START);
                        int stop = rs.getInt(FieldNames.MAPPING_STOP);
                        boolean isFwdStrand = rs.getByte(FieldNames.MAPPING_DIRECTION) == SequenceUtils.STRAND_FWD;
                        int numReplicates = rs.getInt(FieldNames.MAPPING_NUM_OF_REPLICATES);

                        PersistantMapping mapping = new PersistantMapping(start, stop, isFwdStrand, numReplicates);
                        mappings.add(mapping);
                    }
                }
                rs.close();
                
                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from database...", currentTimestamp);

            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } else { //handle retrieving of data from other source than a DB
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Reading mapping data from file...", currentTimestamp);
            mappings = new ArrayList<>(externalDataReader.getReducedMappingsFromBam(this.refGenome, request));
            currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Done reading mapping data from file...", currentTimestamp);
        }

        return mappings;
    }

    @Override
    public void run() {

        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            if (request != null) {
                if (doesNotMatchLatestRequestBounds(request)) {
                    if (request.getDesiredData() == Properties.MAPPINGS_W_DIFFS) {
                        this.currentMappings = this.loadMappingsWithDiffs(request);
                    } else if (request.getDesiredData() == Properties.MAPPINGS_WO_DIFFS) {
                        this.currentMappings = this.loadMappingsWithoutDiffs(request);
                    } else {
                        this.currentMappings = this.loadMappingsById(request);
                    }
                    request.getSender().receiveData(new MappingResultPersistant(currentMappings, request.getFrom(), request.getTo()));
                }

            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoverageThreadAnalyses.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public int getTrackId() {
        return trackId;
    }

    /**
     * Releases unneeded resources.
     */
    public void close() {
        if (externalDataReader != null) {
            this.externalDataReader.close();
        }
    }    
}
