package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.CoverageRequest;
import de.cebitec.vamp.databackend.CoverageThread;
import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
//import de.cebitec.vamp.api.objects.Read;
import de.cebitec.vamp.api.objects.Snp;
import de.cebitec.vamp.databackend.GenericSQLQueries;
import de.cebitec.vamp.databackend.dataObjects.PersistantSeqPairGroup;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class TrackConnector implements ITrackConnector {

    /* !!!!!!!!!!!!
     * Note that all parts belonging to the RUN domain have been commented out!
     * !!!!!!!!!!!!
     */

    private String associatedTrackName;
    private long trackID;  
    //private long runID;
    private int genomeSize;
    private CoverageThread thread;
    private Connection con;
    private static int COV = 0;
    private static int DIFF_COV = 1;
    private static int A_COV = 2;
    private static int C_COV = 3;
    private static int G_COV = 4;
    private static int T_COV = 5;
    private static int N_COV = 6;
    private static int _COV = 7;
    private static int A_GAP = 8;
    private static int C_GAP = 9;
    private static int G_GAP = 10;
    private static int T_GAP = 11;
    private static int N_GAP = 12;

    TrackConnector(PersistantTrack track) {
        associatedTrackName = track.getDescription();
        trackID = track.getId();
        con = ProjectConnector.getInstance().getConnection();
        //runID = fetchRunID();
        genomeSize = this.getRefGenLength();

        List<PersistantTrack> tracks = new ArrayList<PersistantTrack>(1);
        tracks.add(track);
        startCoverageThread(tracks);
    }

    TrackConnector(long id, List<PersistantTrack> tracks) {
        if (tracks.size() > 2){ throw new UnsupportedOperationException("More than two tracks not supported yet."); }
        this.trackID = id;
        con = ProjectConnector.getInstance().getConnection();
        //runID = fetchRunID();
        genomeSize = this.getRefGenLength();

        startCoverageThread(tracks);
    }

    private void startCoverageThread(List<PersistantTrack> tracks){
        List<Integer> trackIds = new ArrayList<Integer>(tracks.size());
        for (PersistantTrack track : tracks) {
            trackIds.add(track.getId());
        }

        thread = new CoverageThread(trackIds);
        thread.start();
    }

//    private int fetchRunID() {
//        int id = 0;
//        try {
//            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_RUNID_FOR_TRACK);
//            fetch.setLong(1, trackID);
//
//            ResultSet rs = fetch.executeQuery();
//            if (rs.next()) {
//                id = rs.getInt(FieldNames.TRACK_RUN);
//            }
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return id;
//    }

    @Override
    public Collection<PersistantMapping> getMappings(int from, int to) {
        HashMap<Long, PersistantMapping> mappings = new HashMap<Long, PersistantMapping>();

        if (from < to) {
            try {
                
                //determine readlength
                //TODO: ensure this is only calculated when track id or db changed!
                PreparedStatement fetchReadlength = con.prepareStatement(SQLStatements.GET_CURRENT_READLENGTH);
                fetchReadlength.setLong(1, trackID);
                ResultSet rsReadlength = fetchReadlength.executeQuery();

                int readlength = 1000;
                final int spacer = 10;
                if (rsReadlength.next()){
                    int start = rsReadlength.getInt(FieldNames.MAPPING_START);
                    int stop = rsReadlength.getInt(FieldNames.MAPPING_STOP);
                    readlength = stop - start + spacer;
                }
                fetchReadlength.close();
                
                
                //mapping processing
                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK);
                fetch.setLong(1, from - readlength);
                fetch.setLong(2, to);
                fetch.setLong(3, from);
                fetch.setLong(4, to + readlength);
                fetch.setLong(5, trackID);
                fetch.setLong(6, from);
                fetch.setLong(7, to);

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    // mapping data
                    int mappingID = rs.getInt(FieldNames.MAPPING_ID);
                    int sequenceID = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                    int mappingTrack = rs.getInt(FieldNames.MAPPING_TRACK);
                    int start = rs.getInt(FieldNames.MAPPING_START);
                    int stop = rs.getInt(FieldNames.MAPPING_STOP);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    boolean isForwardStrand = (direction == 1 ? true : false);
                    int count = rs.getInt(FieldNames.MAPPING_COUNT);
                    int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    int bestMapping = rs.getInt(FieldNames.MAPPING_BEST_MAPPING);
                    boolean isBestMapping = (bestMapping == 1 ? true : false);
                    PersistantMapping m = new PersistantMapping(mappingID, start, stop, mappingTrack, direction, count, errors, sequenceID, isBestMapping);

                    // add new mapping if not exists
                    if (!mappings.containsKey(m.getId())) {
                        mappings.put(m.getId(), m);
                    }

                    // diff data
                    String baseString = rs.getString(FieldNames.DIFF_CHAR);
                    int position = rs.getInt(FieldNames.DIFF_POSITION);
                    int type = rs.getInt(FieldNames.DIFF_TYPE);
                    int gapOrder = rs.getInt(FieldNames.DIFF_ORDER);

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

                fetch.close();
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mappings.values();
    }

    @Override
    public void addCoverageRequest(CoverageRequest request) {
        thread.addCoverageRequest(request);
    }

    @Override
    public Collection<PersistantDiff> getDiffsForInterval(int from, int to) {
        
        ArrayList<PersistantDiff> diffs = new ArrayList<PersistantDiff>();
        if (from < to) {
            try {

                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_DIFFS_IN_TRACK_FOR_INTERVAL);
                fetch.setInt(1, from);
                fetch.setInt(2, to);
                fetch.setLong(3, trackID);

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt(FieldNames.DIFF_POSITION);
                    char base = rs.getString(FieldNames.DIFF_CHAR).charAt(0);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    boolean isForwardStrand = (direction == 1 ? true : false);
                    int count = rs.getInt(FieldNames.MAPPING_COUNT);

                    diffs.add(new PersistantDiff(position, base, isForwardStrand, count));
                }

            } catch (SQLException ex) {
                Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return diffs;
    }

    @Override
    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervalOrderedByMappingID(int from, int to) {
   
        Collection<PersistantReferenceGap> gaps = new ArrayList<PersistantReferenceGap>();
        if (from < to) {
            try {
                PreparedStatement fetchGaps = con.prepareStatement(SQLStatements.FETCH_GENOME_GAPS_IN_TRACK_FOR_INTERVAL);
                fetchGaps.setInt(1, from);
                fetchGaps.setInt(2, to);
                fetchGaps.setLong(3, trackID);

                ResultSet rs = fetchGaps.executeQuery();
                while (rs.next()) {
                    char base = rs.getString(FieldNames.DIFF_CHAR).charAt(0);
                    int position = rs.getInt(FieldNames.DIFF_POSITION);
                    int order = rs.getInt(FieldNames.DIFF_ORDER);
                    boolean isForwardStrand = (rs.getByte(FieldNames.MAPPING_DIRECTION) == 1 ? true : false);
                    int count = rs.getInt(FieldNames.MAPPING_COUNT);

                    gaps.add(new PersistantReferenceGap(position, base, order, isForwardStrand, count));
                }

            } catch (SQLException ex) {
                Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       

        return gaps;
    }

    @Override
    public int getNumOfReads(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_READS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    

    @Override
    public int getNumOfReadsCalculate() {
        //return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_READS_FOR_TRACK_CALCULATE, con, trackID);
        return 0; //TODO: implement calc number of reads
    }

    @Override
    public int getNumOfUniqueSequences(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfUniqueSequencesCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfUniqueBmMappings() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfUniqueBmMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }


    @Override
    public int getNumOfMappings() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_OF_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfUniqueMappings(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfUniqueMappingsCalculate(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfSeqPairs(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_SEQ_PAIRS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfPerfectSeqPairs(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_SEQ_PAIRS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfUniqueSeqPairs(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_SEQ_PAIRS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfUniquePerfectSeqPairs(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_UNIQUE_PERFECT_SEQ_PAIRS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfSingleMappings(){
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_SINGLE_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }
    
    @Override
    public int getNumOfPerfectUniqueMappings() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }

    
    @Override
    public int getNumOfPerfectUniqueMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }
    
    
    @Override
    public double getPercentRefGenPerfectCovered() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_PERFECT_COVERAGE_OF_GENOME, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;
    }
    
    
    @Override
    public double getPercentRefGenPerfectCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_COVERED_POSITIONS_FOR_TRACK, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;

    }

    
    @Override
    public double getPercentRefGenBmCovered() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_BM_COVERAGE_OF_GENOME, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;
    }

    
    @Override
    public double getPercentRefGenBmCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_BM_COVERED_POSITION_FOR_TRACK, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;
    }

    
    @Override
    public double getPercentRefGenNErrorCovered() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_COMPLETE_COVERAGE_OF_GENOME, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;
    }

    
    @Override
    public double getPercentRefGenNErrorCoveredCalculate() {
        double absValue = GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_COVERED_POSITIONS, SQLStatements.GET_COVERED, con, trackID);
        return absValue / genomeSize * 100;
    }
    
    
    @Override
    public HashMap<Integer, Integer> getCoverageInfosOfTrack(int from, int to) {
        PreparedStatement fetch;
        HashMap<Integer, Integer> positionMap = new HashMap<Integer, Integer>();
        int coverage;
        int position;
        if (from < to) {
            try {

                fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_TRACK);

                fetch.setLong(1, trackID);
                fetch.setLong(2, from);
                fetch.setLong(3, to);
                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {
                    position = rs.getInt(FieldNames.COVERAGE_POSITION);
                    coverage = rs.getInt(FieldNames.COVERAGE_N_MULT);
                    positionMap.put(position, coverage);
                }
            } catch (SQLException ex) {
                Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return positionMap;
    }
    
    
    @Override
    public void setStatistics(int numMappings, int numUniqueMappings, int numUniqueSeq, 
            int numPerfectMappings, int numBestMatchMappings, double coveragePerf, double coverageBM, 
            double coverageComplete, int numReads) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track statistics");
        try {
            PreparedStatement insertStatistics = con.prepareStatement(SQLStatements.INSERT_STATISTICS);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_STATISTICS_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            latestID.close();
            id++;

            int covPerf = (int) (coveragePerf / 100 * genomeSize);
            int covBM = (int) (coverageBM / 100 * genomeSize);
            int covComplete = (int) (coverageComplete / 100 * genomeSize);
            // store track in table
            insertStatistics.setLong(1, id);
            insertStatistics.setLong(2, trackID);
            insertStatistics.setInt(3, numMappings);
            insertStatistics.setInt(4, numPerfectMappings);
            insertStatistics.setInt(5, numBestMatchMappings);
            insertStatistics.setInt(6, numUniqueMappings);
            insertStatistics.setInt(7, covPerf);
            insertStatistics.setInt(8, covBM);
            insertStatistics.setInt(9, covComplete);
            insertStatistics.setInt(10, numUniqueSeq);
            insertStatistics.setInt(11, numReads);
            insertStatistics.execute();
            insertStatistics.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track statistics");

    }
    
    @Override
    public void addSeqPairStatistics(int numSeqPairs, int numPerfectSeqPairs, int numUniqueSeqPairs, 
            int numUniquePerfectSeqPairs, int numSingleMappings) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing sequence pair statistics");
        try {
            PreparedStatement addSeqPairStats = con.prepareStatement(SQLStatements.ADD_SEQPAIR_STATISTICS);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_STATISTICS_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            latestID.close();
            id++;

            addSeqPairStats.setLong(1, id);
            addSeqPairStats.setInt(2, numSeqPairs);
            addSeqPairStats.setInt(3, numPerfectSeqPairs);
            addSeqPairStats.setInt(4, numUniqueSeqPairs);
            addSeqPairStats.setInt(5, numUniquePerfectSeqPairs);
            addSeqPairStats.setInt(6, numSingleMappings);
            addSeqPairStats.execute();
            addSeqPairStats.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing sequence pair statistics");

    }

//    @Override
//    public long getRunId() {
//        return runID;
//    }

    @Override
    public long getTrackID(){
        return trackID;
    }

    @Override
    public String getAssociatedTrackName() {
        return associatedTrackName;
    }
    

    private void addValues(HashMap<Integer, Integer[]> map, byte direction, int coverage, int count, int position, char base, boolean isGenomeGap) {
        if (!map.containsKey(position)) {
            Integer[] data = new Integer[13];
            Arrays.fill(data, 0);
            map.put(position, data);
        }

        if (direction == -1) {
            base = SequenceUtils.complementDNA(base);
        }

        Integer[] values = map.get(position);
        values[COV] = coverage;
        values[DIFF_COV] += count;
        if (!isGenomeGap) {
            if (base == 'A') {
                values[A_COV] += count;
            } else if (base == 'C') {
                values[C_COV] += count;
            } else if (base == 'G') {
                values[G_COV] += count;
            } else if (base == 'T') {
                values[T_COV] += count;
            } else if (base == 'N') {
                values[N_COV] += count;
            } else if (base == '_') {
                values[_COV] += count;
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unkown diff base {0}", base);
            }

        } else {
            if (base == 'A') {
                values[A_GAP] += count;
            } else if (base == 'C') {
                values[C_GAP] += count;
            } else if (base == 'G') {
                values[G_GAP] += count;
            } else if (base == 'T') {
                values[T_GAP] += count;
            } else if (base == 'N') {
                values[N_GAP] += count;
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unkown genome gap base {0}", base);
            }
        }

    }

    private boolean isSNP(Integer[] data, int index, int threshold) {
        if (data[index] >= threshold) {
            return true;
        }
        return false;
    }

    private Snp createSNP(Integer[] data, int index, int position, int positionVariation) {
        String base = "";
        if (index == A_COV || index == A_GAP) {
            base = "A";
        } else if (index == C_COV || index == C_GAP) {
            base = "C";
        } else if (index == G_COV || index == G_GAP) {
            base = "G";
        } else if (index == T_COV || index == T_GAP) {
            base = "T";
        } else if (index == N_COV || index == N_GAP) {
            base = "N";
        } else if (index == _COV) {
            base = "-";
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown snp type");
        }

        double count = data[index];
        int percentage = (int) (count / ((double) data[COV]) * 100);
        return new Snp((int) count, position, base, percentage, positionVariation);

    }

    private List<Snp> filterSnps(Map<Integer, Integer[]> map, int overallPercentage, int absThreshold) {
        ArrayList<Snp> snps = new ArrayList<Snp>();

        Iterator<Integer> positions = map.keySet().iterator();
        while (positions.hasNext()) {
            int position = positions.next();
            Integer[] data = map.get(position);
            double complete = data[COV];
            double diffCov = data[DIFF_COV];
            int percentage = (int) (diffCov / complete * 100);

            if (percentage > overallPercentage) {
                for (int i = A_COV; i < data.length; i++) {
                    if (this.isSNP(data, i, absThreshold)) {
                        snps.add(this.createSNP(data, i, position, percentage));
                    }
                }
            }

        }
        return snps;
    }

//    @Override
//    public List<Read> findReads(String read) {
//        ArrayList<Read> reads = new ArrayList<Read>();
//        try {
//            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_READ_POSITION_BY_READNAME);
//            fetch.setString(1, read);
//            fetch.setLong(2, trackID);
//            ResultSet rs = fetch.executeQuery();
//            while (rs.next()) {
//                String name = rs.getString(FieldNames.READ_NAME);
//                int position = rs.getInt(FieldNames.MAPPING_START);
//                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
//                int isBestMapping = rs.getInt(FieldNames.MAPPING_BEST_MAPPING);
//
//                Read e = new Read(name, position, errors, isBestMapping);
//                reads.add(e);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return reads;
//    }

    /*
     * this methods searches for SNPs in the whole genome
     * to prevent that there is a big join between the table diff and mapping
     * we make some small mapping
     * we take 50 entrys of the diff table and 400 (200 from the left and 200 from the right)
     * from the table mapping we have to do this so that we dont miss any mapping that have a diff in this 50 positions
     */
    @Override
    public List<Snp> findSNPs(int percentageThreshold, int absThreshold) {
        ArrayList<Snp> snps = new ArrayList<Snp>();
        HashMap<Integer, Integer[]> covData = new HashMap<Integer, Integer[]>();
        final int diffIntervalSize = 50;
        final int mappingIntervalSize = 200;
        int fromDiff = 1;
        int toDiff = diffIntervalSize;
        int fromMapping = 1;
        int toMapping = mappingIntervalSize;
        try {
            while (genomeSize > fromDiff) {
            //    Logger.getLogger(TrackConnector.class.getName()).log(Level.INFO, "find Snps by genomeposition of the diff:"+fromDiff+"-"+toDiff+" mapping position "+fromMapping+"-"+toMapping);
                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVAL);
                fetch.setLong(1, trackID);
                fetch.setLong(2, fromMapping);
                fetch.setLong(3, toMapping);
                fetch.setLong(4, fromDiff);
                fetch.setLong(5, toDiff);
                fetch.setLong(6, trackID);

                fromDiff += diffIntervalSize;
                toDiff += diffIntervalSize;
                if (toDiff > genomeSize) {
                    toDiff = genomeSize;
                }

                if(fromDiff > mappingIntervalSize){
                    fromMapping = fromDiff - mappingIntervalSize;
                }
                toMapping = toDiff + mappingIntervalSize;
                 if (toMapping > genomeSize) {
                    toMapping = genomeSize;
                }
                ResultSet rs = fetch.executeQuery();

                while (rs.next()) {
                    int position = rs.getInt(FieldNames.DIFF_POSITION);
                    char base = rs.getString(FieldNames.DIFF_CHAR).charAt(0);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    int replicates = rs.getInt("mult_count");
                    int forwardCov = rs.getInt(FieldNames.COVERAGE_BM_FW_MULT);
                    int reverseCov = rs.getInt(FieldNames.COVERAGE_BM_RV_MULT);
                    int type = rs.getInt(FieldNames.DIFF_TYPE);
                    boolean isGenomeGap = false;
                    if (type == 0) {
                        isGenomeGap = true;
                    }
                    int cov = forwardCov + reverseCov;

                    this.addValues(covData, direction, cov, replicates, position, base, isGenomeGap);

                }
          }

            snps.addAll(this.filterSnps(covData, percentageThreshold, absThreshold));

        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }


        return snps;
    }

    
    private int getRefGenLength() {
        int refGenID = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_GENOMEID_FOR_TRACK);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                refGenID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        int refGenLength = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_GENOME_LENGTH);
            fetch.setInt(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                refGenLength = rs.getInt("LENGTH");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return refGenLength;

    }

    
    @Override
    public CoverageThread getThread() {
        return this.thread;
    }

    @Override
    public int getNumOfSeqPairsCalculate() {
        return -1; //TODO: implement seq pair stats calculate
    }

    @Override
    public int getNumOfPerfectSeqPairsCalculate() {
        return -1;
    }

    @Override
    public int getNumOfUniqueSeqPairsCalculate() {
        return -1;
    }

    @Override
    public int getNumOfUniquePerfectSeqPairsCalculate() {
       return -1;
    }
    
    @Override 
    public int getNumOfSingleMappingsCalculate(){
        return -1;
    }

    public Collection<PersistantSeqPairGroup> getSeqPairMappings(int from, int to, int trackID2) {
        HashMap<Long, PersistantSeqPairGroup> seqPairs = new HashMap<Long, PersistantSeqPairGroup>();
        if (from < to) {

            try {

                //determine readlength
                //TODO: ensure this is only calculated when track id or db changed!
                PreparedStatement fetchReadlength = con.prepareStatement(SQLStatements.GET_CURRENT_READLENGTH);
                fetchReadlength.setLong(1, trackID);
                ResultSet rsReadlength = fetchReadlength.executeQuery();

                int readlength = 1000;
                final int spacer = 10;
                if (rsReadlength.next()) {
                    int start = rsReadlength.getInt(FieldNames.MAPPING_START);
                    int stop = rsReadlength.getInt(FieldNames.MAPPING_STOP);
                    readlength = stop - start + spacer;
                }
                fetchReadlength.close();

                //sequence pair processing
                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SEQ_PAIRS_W_REPLICATES_FOR_INTERVAL);
                fetch.setLong(1, from - readlength);
                fetch.setLong(2, to);
                fetch.setLong(3, from);
                fetch.setLong(4, to + readlength);
                fetch.setLong(5, trackID);
                fetch.setLong(6, trackID2);

                ResultSet rs = fetch.executeQuery();
                while (rs.next()) {

                    // mapping data
                    int mappingID = rs.getInt("MAPPING_ID");
                    int sequenceID = rs.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                    int mappingTrack = rs.getInt(FieldNames.MAPPING_TRACK);
                    int start = rs.getInt(FieldNames.MAPPING_START);
                    int stop = rs.getInt(FieldNames.MAPPING_STOP);
                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                    int count = rs.getInt("MAPPING_REP");
                    int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    int bestMapping = rs.getInt(FieldNames.MAPPING_BEST_MAPPING);
                    boolean isBestMapping = (bestMapping == 1 ? true : false);
                    long seqPairID = rs.getLong("ORIG_PAIR_ID");
                    long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                    long mapping2Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                    byte seqPairType = rs.getByte(FieldNames.SEQ_PAIR_TYPE);
                    int seqPairReplicates = rs.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                    PersistantMapping mapping = new PersistantMapping(mappingID, start, stop, mappingTrack, direction, count, errors, sequenceID, isBestMapping);
                    
                     // add new seqPair if not exists
                    if (!seqPairs.containsKey(seqPairID)) {
                        PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                        newGroup.setSeqPairId(seqPairID);
                        seqPairs.put(seqPairID, newGroup);
                    }
                    seqPairs.get(seqPairID).addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);

                   

                }

                fetch.close();
                
                //single mapping processing
                PreparedStatement fetchSingleReads = con.prepareStatement(SQLStatements.FETCH_SEQ_PAIRS_PIVOT_DATA_FOR_INTERVAL);
                fetchSingleReads.setLong(1, from - readlength);
                fetchSingleReads.setLong(2, to);
                fetchSingleReads.setLong(3, from);
                fetchSingleReads.setLong(4, to + readlength);
                fetchSingleReads.setLong(5, trackID);
                fetchSingleReads.setLong(6, trackID2);

                ResultSet rs2 = fetchSingleReads.executeQuery();
                while (rs2.next()) {

                    // mapping data
                    int mappingID = rs2.getInt(FieldNames.MAPPING_ID);
                    int sequenceID = rs2.getInt(FieldNames.MAPPING_SEQUENCE_ID);
                    int mappingTrack = rs2.getInt(FieldNames.MAPPING_TRACK);
                    int start = rs2.getInt(FieldNames.MAPPING_START);
                    int stop = rs2.getInt(FieldNames.MAPPING_STOP);
                    byte direction = rs2.getByte(FieldNames.MAPPING_DIRECTION);
                    int count = rs2.getInt(FieldNames.MAPPING_COUNT);
                    int errors = rs2.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                    int bestMapping = rs2.getInt(FieldNames.MAPPING_BEST_MAPPING);
                    boolean isBestMapping = (bestMapping == 1 ? true : false);
                    long seqPairID = rs2.getLong(FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID);
                    PersistantMapping mapping = new PersistantMapping(mappingID, start, stop, mappingTrack, direction, count, errors, sequenceID, isBestMapping);
                    
                     // add to seqPair container
                    if (!seqPairs.containsKey(seqPairID)) {
                        PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                        newGroup.setSeqPairId(seqPairID);
                        seqPairs.put(seqPairID, newGroup);
                    }
                    seqPairs.get(seqPairID).addPersistantMapping(mapping, Properties.TYPE_UNPAIRED_PAIR, -1, -1, -1);

                }

                fetchSingleReads.close();
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }



        return seqPairs.values();
    }

    /**
     * @return The sequence pair id belonging to the track connectors track id.
     */
    public int getSeqPairToTrackID() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_SEQ_PAIR_TO_TRACK_ID, SQLStatements.GET_NUM, con, trackID);
    }
    
    /**
     * @param seqPairId the sequence pair id to get the second track id for
     * @return the second track id of a sequence pair beyond this track connectors track id
     */
    public int getTrackIdToSeqPairId(int seqPairId) {
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACK_ID_TO_SEQ_PAIR_ID);
            fetch.setLong(1, seqPairId);
            fetch.setLong(2, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt(FieldNames.TRACK_ID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLStatements.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    /**
     * Returns all mappings belonging to a sequence pair
     * @param seqPairId sequence id to search for
     * @return all data belonging to this sequence pair id (all mappings and pair replicates)
     */
    public PersistantSeqPairGroup getMappingsForSeqPairId(long seqPairId) {

        PersistantSeqPairGroup seqPairData = new PersistantSeqPairGroup();
        seqPairData.setSeqPairId(seqPairId);
        try {

            //sequence pair processing
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FOR_SEQ_PAIR_ID);
            fetch.setLong(1, seqPairId);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
        
                // mapping data
                long mappingId = rs.getLong(FieldNames.MAPPING_ID);
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                int count = rs.getInt("MAPPING_REPLICATES");
                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int bestMapping = rs.getInt(FieldNames.MAPPING_BEST_MAPPING);
                boolean isBestMapping = (bestMapping == 1 ? true : false);
                long seqPairID = rs.getLong(FieldNames.SEQ_PAIR_PAIR_ID);
                long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING1_ID);
                long mapping2Id = rs.getLong(FieldNames.SEQ_PAIR_MAPPING2_ID);
                byte seqPairType = rs.getByte(FieldNames.SEQ_PAIR_TYPE);
                int seqPairReplicates = rs.getInt(FieldNames.SEQ_PAIR_NUM_OF_REPLICATES);
                
                PersistantMapping mapping = new PersistantMapping((int) mappingId, start, stop, -1, direction, count, errors, -1, isBestMapping);
                seqPairData.addPersistantMapping(mapping, seqPairType, mapping1Id, mapping2Id, seqPairReplicates);
                System.out.println("input id: "+seqPairId + ", output id: "+seqPairID);

            }

            fetch.close();

            //single mapping processing
            PreparedStatement fetchSingleReads = con.prepareStatement(SQLStatements.FETCH_SINGLE_MAPPINGS_FOR_SEQ_PAIR_ID);
            fetchSingleReads.setLong(1, seqPairId);

            rs = fetchSingleReads.executeQuery();
            while (rs.next()) {
                        
                // mapping data
                int start = rs.getInt(FieldNames.MAPPING_START);
                int stop = rs.getInt(FieldNames.MAPPING_STOP);
                byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
                int count = rs.getInt(FieldNames.MAPPING_COUNT);
                int errors = rs.getInt(FieldNames.MAPPING_NUM_OF_ERRORS);
                int bestMapping = rs.getInt(FieldNames.MAPPING_BEST_MAPPING);
                boolean isBestMapping = (bestMapping == 1 ? true : false);
                long seqPairID = rs.getLong(FieldNames.SEQ_PAIR_PIVOT_SEQ_PAIR_ID);
                long mapping1Id = rs.getLong(FieldNames.SEQ_PAIR_PIVOT_MAPPING_ID);
                PersistantMapping mapping = new PersistantMapping((int) mapping1Id, start, stop, -1, direction, count, errors, -1, isBestMapping);

                seqPairData.addPersistantMapping(mapping, Properties.TYPE_UNPAIRED_PAIR, -1, -1, -1);
                System.out.println("input id: "+seqPairId + ", output id single: "+seqPairID);


            }

            fetchSingleReads.close();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        return seqPairData;
        
    }

}
