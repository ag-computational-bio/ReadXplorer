package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.api.objects.Snp454;
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
import de.cebitec.vamp.databackend.connector.ITrackConnector;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
//import de.cebitec.vamp.databackend.connector.RunConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
 * @author ddoppmeier
 */
public class MultiTrackConnector implements ITrackConnector {

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

    public MultiTrackConnector(PersistantTrack track) {
        associatedTrackName = track.getDescription();
        trackID = track.getId();
        con = ProjectConnector.getInstance().getConnection();
        //runID = fetchRunID();
        genomeSize = this.getRefGenLength();

        List<PersistantTrack> tracks = new ArrayList<PersistantTrack>(1);
        tracks.add(track);
        startCoverageThread(tracks);
    }

    public MultiTrackConnector(List<PersistantTrack> tracks) {
        if (tracks.size() > 2) { throw new UnsupportedOperationException("More than two tracks not supported yet."); }
        this.trackID = 9999;
        con = ProjectConnector.getInstance().getConnection();
        //runID = fetchRunID();
        genomeSize = this.getRefGenLength();

        startCoverageThread(tracks);
    }

    private void startCoverageThread(List<PersistantTrack> tracks) {
        List<Long> trackIds = new ArrayList<Long>(tracks.size());
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
        HashMap<Integer, PersistantMapping> mappings = new HashMap<Integer, PersistantMapping>();

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK);
            fetch.setLong(1, trackID);
            fetch.setInt(2, from);
            fetch.setInt(3, to);
            fetch.setInt(4, from);
            fetch.setInt(5, to);

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

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
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


        return diffs;
    }

    @Override
    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervalOrderedByMappingID(int from, int to) {

        Collection<PersistantReferenceGap> gaps = new ArrayList<PersistantReferenceGap>();
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

        return gaps;
    }


    @Override
    public void setStatistics(int numMappings, int numUniqueMappings, int numUniqueSeq, int numPerfectMappings,
            int numBestMatchMappings, double coveragePerf, double coverageBM, double coverageComplete, int numReads,
            int numSeqPairs, int numPerfectSeqPairs, int numUniqueSeqPairs, int numUniquePerfectSeqPairs) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data");
        try {
            PreparedStatement insertStatics = con.prepareStatement(SQLStatements.INSERT_STATISTICS);
            PreparedStatement latestID = con.prepareStatement(SQLStatements.GET_LATEST_STATISTICS_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;
            int covPerf = (int) (coveragePerf / 100 * genomeSize);
            int covBM = (int) (coverageBM / 100 * genomeSize);
            int covComplete = (int) (coverageComplete / 100 * genomeSize);
            // store track in table
            insertStatics.setLong(1, id);
            insertStatics.setLong(2, trackID);
            insertStatics.setInt(3, numMappings);
            insertStatics.setInt(4, numPerfectMappings);
            insertStatics.setInt(5, numBestMatchMappings);
            insertStatics.setInt(6, numUniqueMappings);
            insertStatics.setInt(7, covPerf);
            insertStatics.setInt(8, covBM);
            insertStatics.setInt(9, covComplete);
            insertStatics.setInt(10, numUniqueSeq);
            insertStatics.execute();

            insertStatics.close();
            latestID.close();

        } catch (SQLException ex) {
            ProjectConnector.getInstance().rollbackOnError(this.getClass().getName(), ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "...done storing track data");

    }


//    @Override
//    public long getRunId() {
//        return runID;
//    }
    @Override
    public long getTrackID() {
        return trackID;
    }

    @Override
    public String getAssociatedTrackName() {
        return associatedTrackName;
    }

    private Character revCompl(char base) {
        if (base == 'A') {
            return 'T';
        } else if (base == 'C') {
            return 'G';
        } else if (base == 'G') {
            return 'C';
        } else if (base == 'T') {
            return 'A';
        } else if (base == 'N') {
            return base;
        } else if (base == '_') {
            return base;
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown char {0} for base", base);
            return ' ';
        }
    }

    private void addValues(HashMap<Integer, Integer[]> map, byte direction, int coverage, int count, int position, char base, boolean isGenomeGap) {
        if (!map.containsKey(position)) {
            Integer[] data = new Integer[13];
            Arrays.fill(data, 0);
            map.put(position, data);
        }

        if (direction == -1) {
            base = revCompl(base);
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
        boolean isSnp = false;

        if (data[index] >= threshold) {
            isSnp = true;
        }

        return isSnp;
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
                    if (isSNP(data, i, absThreshold)) {
                        snps.add(createSNP(data, i, position, percentage));
                    }
                }
            }

        }
        return snps;
    }

//    @Override
//    public List<Read> findReads(String read) {
//        ArrayList<Read> reads = new ArrayList<Read>();
//        //TODO: replace by sequence search!!!
//        try {
//            //PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_READ_POSITION_BY_READNAME);
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

    
    /**
     * this methods searches for SNPs in the whole genome
     * to prevent that there is a big join between the table diff and mapping
     * we make some small mapping
     * we take 50 entrys of the diff table and 400 (200 from the left and 200 from the right)
     * from the table mapping we have to do this so that we dont miss any mapping that have a diff in this 50 positions
     */
//    @Override
//    public List<Snp> findSNPs(int percentageThreshold, int absThreshold) {
//        ArrayList<Snp> snps = new ArrayList<Snp>();
//        HashMap<Integer, Integer[]> covData = new HashMap<Integer, Integer[]>();
//        int fromDiff = 1;
//        int toDiff = 50;
//        int fromMapping = 1;
//        int toMapping = 200;
//        try {
//            while (genomeSize > fromDiff) {
//                //    Logger.getLogger(TrackConnector.class.getName()).log(Level.INFO, "find Snps by genomeposition of the diff:"+fromDiff+"-"+toDiff+" mapping position "+fromMapping+"-"+toMapping);
//                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVAL);
//                fetch.setLong(1, trackID);
//                fetch.setLong(2, fromMapping);
//                fetch.setLong(3, toMapping);
//                fetch.setLong(4, fromDiff);
//                fetch.setLong(5, toDiff);
//                fetch.setLong(6, trackID);
//
//                fromDiff += 50;
//                toDiff += 50;
//                if (toDiff > genomeSize) {
//                    toDiff = genomeSize;
//                }
//
//                if (fromDiff > 200) {
//                    fromMapping = fromDiff - 200;
//                }
//                toMapping = toDiff + 200;
//                if (toMapping > genomeSize) {
//                    toMapping = genomeSize;
//                }
//                ResultSet rs = fetch.executeQuery();
//
//                while (rs.next()) {
//                    int position = rs.getInt(FieldNames.DIFF_POSITION);
//                    char base = rs.getString(FieldNames.DIFF_CHAR).charAt(0);
//                    byte direction = rs.getByte(FieldNames.MAPPING_DIRECTION);
//                    int replicates = rs.getInt("mult_count");
//                    int forwardCov = rs.getInt(FieldNames.COVERAGE_BM_FW_MULT);
//                    int reverseCov = rs.getInt(FieldNames.COVERAGE_BM_RV_MULT);
//                    int type = rs.getInt(FieldNames.DIFF_TYPE);
//                    boolean isGenomeGap = false;
//                    if (type == 0) {
//                        isGenomeGap = true;
//                    }
//                    int cov = forwardCov + reverseCov;
//
//                    addValues(covData, direction, cov, replicates, position, base, isGenomeGap);
//
//                }
//            }
//
//            snps.addAll(filterSnps(covData, percentageThreshold, absThreshold));
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//
//        return snps;
//    }

//        @Override
//    public List<Snp> findSNPs(int percentageThreshold, int absThreshold) {
//        ArrayList<Snp> snps = new ArrayList<Snp>();
//        try {
//            PreparedStatement fetchSNP = con.prepareStatement(SQLStatements.FETCH_SNP_IDS_FOR_TRACK);
//            fetchSNP.setInt(1, percentageThreshold);
//            fetchSNP.setInt(2, absThreshold);
//            fetchSNP.setInt(3, absThreshold);
//            fetchSNP.setInt(4, absThreshold);
//            fetchSNP.setInt(5, absThreshold);
//            
//            ResultSet rs = fetchSNP.executeQuery();
//            while (rs.next()) {
//                String position = rs.getString(FieldNames.POSITIONS_POSITION);
//                int track = rs.getInt(FieldNames.POSITIONS_TRACK_ID);
//                char base = rs.getString(FieldNames.POSITIONS_BASE).charAt(0);
//                char refBase = rs.getString(FieldNames.POSITIONS_REF_BASE).charAt(0);
//                int aRate = rs.getInt(FieldNames.POSITIONS_A);
//                int cRate = rs.getInt(FieldNames.POSITIONS_C);
//                int gRate = rs.getInt(FieldNames.POSITIONS_G);
//                int tRate = rs.getInt(FieldNames.POSITIONS_T);
//                int nRate = rs.getInt(FieldNames.POSITIONS_N);
//                int gapRate = rs.getInt(FieldNames.POSITIONS_GAP);
//                int coverage = rs.getInt(FieldNames.POSITIONS_COVERAGE);
//                int frequency = rs.getInt(FieldNames.POSITIONS_FREQUENCY);
//                char type = rs.getString(FieldNames.POSITIONS_TYPE).charAt(0);
//                
//                snps.add(new Snp(position, track, base, refBase, aRate, cRate, gRate,
//                        tRate, nRate, gapRate, coverage, frequency, type));
//                
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return snps;
//    }
        
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
    public HashMap<Integer, Integer> getCoverageInfosOfTrack(int from, int to) {
        PreparedStatement fetch;
        HashMap<Integer, Integer> positionMap = new HashMap<Integer, Integer>();
        int coverage;
        int position;
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
        return positionMap;
    }
    
//    @Override
//    public List<Snp> findSNPs(int percentageThreshold, int absThreshold) {
//        ArrayList<Snp> snps = new ArrayList<Snp>();
//
//        try {
//
//            PreparedStatement fetchSNP = con.prepareStatement(SQLStatements.FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVAL);
//            PreparedStatement fetchDIFF = con.prepareStatement(SQLStatements.FETCH_DIFFS_HAVING_SNP_ID);
//            PreparedStatement getDirection = con.prepareStatement(SQLStatements.GET_DIRECTION_OF_MAPPING);
//            PreparedStatement getGenomeID = con.prepareStatement(SQLStatements.FETCH_GENOMEID_FOR_TRACK);
//            PreparedStatement getRefSeq = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
//            fetchSNP.setLong(1, trackID);
//
//            ResultSet rs = fetchSNP.executeQuery();
//
//            while (rs.next()) {
//                Integer[] diffs = new Integer[11];
//                long ID = rs.getLong(FieldNames.SNP_ID);
//                int coverage = rs.getInt(FieldNames.SNP_COVERAGE);
//                int frequency = rs.getInt(FieldNames.SNP_FREQUENCY);
//                String type = rs.getString(FieldNames.SNP_TYPE);
//                long position = 0;
//                int gap_order;
//                byte diffType = -1;
//
//                if (frequency >= percentageThreshold) {
//                    // get all diffs at one position (one SnpID for one position per track)
//                    fetchDIFF.setLong(1, ID);
//
//                    ResultSet ps = fetchDIFF.executeQuery();
//
//                    while (ps.next()) {
//                        long mappingID = ps.getLong(FieldNames.DIFF_MAPPING_ID);
//                        char base = ps.getString(FieldNames.DIFF_CHAR).charAt(0);
//                        position = ps.getLong(FieldNames.DIFF_POSITION);
//                        diffType = ps.getByte(FieldNames.DIFF_TYPE);
//                        gap_order = ps.getInt(FieldNames.DIFF_ORDER);
//                        byte direction = 0;
//               
//                        getDirection.setLong(1, mappingID);
//                        ResultSet qs = getDirection.executeQuery();
//                        if (qs.next()) {
//                             direction = qs.getByte(FieldNames.MAPPING_DIRECTION);
//                        }
//                        diffs = countDiffs(diffs, base, diffType, direction);
//                    }
//                    // sort out diffs which have minimal occurrence
//                    if (diffs[DIFF_COV] >= absThreshold) {
//                        // get consensus base
//                        getGenomeID.setLong(1, trackID);
//                        long genomeID = 0;
//                        ResultSet qs = getDirection.executeQuery();
//                        if (qs.next()) {
//                            genomeID = qs.getLong(FieldNames.TRACK_REFERENCE_ID);
//                        }
//                        getRefSeq.setLong(1, genomeID);
//                        String refSeq = "";
//                        ResultSet os = getDirection.executeQuery();
//                        if (os.next()) {
//                            refSeq = os.getString(FieldNames.REF_GEN_SEQUENCE);
//                        }
//                        // gucken ob anzahl diffs der gewuenschten entspricht
//                        // array durchgehen und gucken ob eine zahl ueber der gewuenschten?
//                        // ODER array an stelle diff_cov auf 0 setzen und sortieren und erste zahl pruefen
//                        
//                        // base raus suchen, die am meisten vorkommt
////                        int x = 0;
////                        ArrayList max = new ArrayList();
////                        for ( int i = A_COV; i<diffs.length; i++) {
////                            if(x < diffs[i]) {
////                                x = diffs[i];
////                                max = new ArrayList();
////                                max.add(i);
////                            } else if (x == diffs[i]) {
////                                max.add(i);
////                            }
////                        }
//                        
//                                                // diffs
//                        int max = 0;
//                        int baseInt = 0;
//                        for ( int i = A_COV; i<=_COV; i++) {
//                            if(max < diffs[i]) {
//                                max = diffs[i];
//                                baseInt = i;
//                            }   
//                        }
//                        if (max >= absThreshold) {
//                            snps.add(new Snp(position, getBase(baseInt), refSeq.charAt((int) position - 1),
//                                    (int) diffs[A_COV], (int) diffs[C_COV], (int) diffs[G_COV], (int) diffs[T_COV],
//                                    (int) diffs[N_COV], (int) diffs[_COV], coverage, frequency, type.charAt(0)));
//                        }
//                        
//                        // gaps (insertions)
//                        max = 0;
//                        baseInt = 0;
//                        for ( int i = A_GAP; i<=N_GAP; i++) {
//                            if(max < diffs[i]) {
//                                max = diffs[i];
//                                baseInt = i;
//                            }   
//                        }
//                        if (max >= absThreshold) {
//                            snps.add(new Snp(position, getBase(baseInt), refSeq.charAt((int) position - 1),
//                                    (int) diffs[A_GAP], (int) diffs[C_GAP], (int) diffs[G_GAP], (int) diffs[T_GAP],
//                                    (int) diffs[N_GAP], 0 , coverage, frequency, 'I'));
//                        }
//                        
//                        //diffs[DIFF_COV] = 0;
//                        //Arrays.sort(diffs);
//                        //if (diffs[0] >= absThreshold) {
//                        // create snp
//                            
//                            //snps.add(NewSnp(position,base,refSeq.charAt(position-1),diffs[A_COV]));
//                        //}
// 
//                    }
//
//                }
//            }
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        schreiben(snps);
//        return snps;
//    }
    
    private Integer[] countDiffs (Integer[] diffs, char base, byte diffType, byte direction) {
        Integer[] addDiff = diffs;
            
        addDiff[DIFF_COV] += 1;
        if (direction == -1) {
            base = SequenceUtils.complementDNA(base);
        }
        
        if (diffType == 1) {
            if (base == 'A') {
                addDiff[A_COV] += 1;
            } else if (base == 'C') {
                addDiff[C_COV] += 1;
            } else if (base == 'G') {
                addDiff[G_COV] += 1;
            } else if (base == 'T') {
                addDiff[T_COV] += 1;
            } else if (base == 'N') {
                addDiff[N_COV] += 1;
            } else if (base == '_') {
                addDiff[_COV] += 1;
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unkown diff base {0}", base);
            }

        } else {
            if (base == 'A') {
                addDiff[A_GAP] += 1;
            } else if (base == 'C') {
                addDiff[C_GAP] += 1;
            } else if (base == 'G') {
                addDiff[G_GAP] += 1;
            } else if (base == 'T') {
                addDiff[T_GAP] += 1;
            } else if (base == 'N') {
                addDiff[N_GAP] += 1;
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unkown genome gap base {0}", base);
            }
        }
        
        return addDiff;
    }
    
    private char getBase(int index) {
   
        char base = ' ';
        
        if (index == A_COV || index == A_GAP) {
            base = 'A';
        } else if (index == C_COV || index == C_GAP) {
            base = 'C';
        } else if (index == G_COV || index == G_GAP) {
            base = 'G';
        } else if (index == T_COV || index == T_GAP) {
            base = 'T';
        } else if (index == N_COV || index == N_GAP) {
            base = 'N';
        } else if (index == _COV) {
            base = '-';
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown snp type");
        }
        
        return base;
        
    }
    
    File file;
    FileWriter writer;
    
    public void schreiben(ArrayList<Snp> snps) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "schreiben");
        // File anlegen
        file = new File("/homes/jhess/Bachelor/ausgabe.txt");
        try {
            // new FileWriter(file ,true) - falls die Datei bereits existiert
            // werden die Bytes an das Ende der Datei geschrieben

            // new FileWriter(file) - falls die Datei bereits existiert
            // wird diese überschrieben
            writer = new FileWriter(file, true);

            // Text wird in den Stream geschrieben
            for (Snp snp : snps) {
                writer.write(snp.toString());
            }
            // Platformunabhängiger Zeilenumbruch wird in den Stream geschrieben
            writer.write(System.getProperty("line.separator"));


            // Schreibt den Stream in die Datei
            // Sollte immer am Ende ausgeführt werden, sodass der Stream 
            // leer ist und alles in der Datei steht.
            writer.flush();

            // Schließt den Stream
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CoverageThread getThread() {
        return this.thread;
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
    public int getNumOfSeqPairsCalculate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   
    @Override
    public int getNumOfPerfectSeqPairsCalculate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNumOfUniqueSeqPairsCalculate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNumOfUniquePerfectSeqPairsCalculate() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public int getNumOfPerfectUniqueMappings() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public int getNumOfPerfectUniqueMappingsCalculate() {
        return GenericSQLQueries.getIntegerFromDB(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE, SQLStatements.GET_NUM, con, trackID);
    }

    @Override
    public List<Snp454> findSNPs454(int percentageThreshold, int absThreshold) {
       ArrayList<Snp454> snps = new ArrayList<Snp454>();
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
//            if(is454){
              snps.addAll(this.filterSnps454(covData, percentageThreshold, absThreshold));
//            } else {
//                snps.addAll(this.filterSnps(covData, percentageThreshold, absThreshold));
//            }

        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }


        return snps;
    }
    
     private List<Snp454> filterSnps454(Map<Integer, Integer[]> map, int overallPercentage, int absThreshold) {
        ArrayList<Snp454 > snps = new ArrayList<Snp454>();
        Iterator<Integer> positions = map.keySet().iterator();
        while (positions.hasNext()) {
            int position = positions.next();
            Integer[] data = map.get(position);
            double complete = data[COV];
            double diffCov = data[DIFF_COV];
            int percentage = (int) ((diffCov / complete) * 100);
            boolean continuousCoverage = isCoverageContinuous(position, complete);
            //Filterschritt: mindestens 3 reads muessen abweichen (zusaetzlich, falls nur wenige 
            // reads an der Stelle mappen) && continuousCoverage && (diffCov > 3)
            if ((percentage > overallPercentage) && (complete > 3) && continuousCoverage) {
                //pruefen, ob fuer jede basenabweichung der threshold erreicht ist, wenn ja = SNP 
                for (int i = A_COV; i < data.length; i++) {
                    if (this.isSNP(data, i, absThreshold)) {
                        snps.add(this.createSNP454(data, i, position, percentage));
                    }
                }
            }
        }
        return snps;
    }
    
    /*
     * pruefe coverage links und rechts des Diffs -> soll nicht abfallen,
     * stetige Readabdeckung
     */
    private boolean isCoverageContinuous(int position, double coverage){
        boolean isContinous = true;
        try {
            //Die Coverage der naechsten 5 Basen links und rechts des SNPs wird verglichen
            int fromPos = ((position - 5) < 0) ? 0 : (position - 5);
            int toPos = ((position + 5)  > this.genomeSize) ? this.genomeSize : (position + 5);
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
            fetch.setLong(3, trackID);
            fetch.setLong(1, fromPos);
            fetch.setLong(2, toPos);
            
             ResultSet rs = fetch.executeQuery();

             
             while (rs.next()) {
                int covPosition = rs.getInt(FieldNames.COVERAGE_POSITION);
                int forwardCov = rs.getInt(FieldNames.COVERAGE_BM_FW_MULT);
                int reverseCov = rs.getInt(FieldNames.COVERAGE_BM_RV_MULT);
                int neighbourCov = forwardCov + reverseCov;
                double deviation = (Math.abs(neighbourCov - coverage))/coverage;               
                if(deviation >= 0.5 ){
                    isContinous = false;
                } 
             }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return isContinous;
    }
    
    private Snp454 createSNP454(Integer[] data, int index, int position, int positionVariation) {
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
        
        String sequence = this.getRefGenSequence();
        String refBase = String.valueOf(sequence.charAt(position));
        double count = data[index];
        int percentage = (int) (count / ((double) data[COV]) * 100);
        return new Snp454((int) count, position, base, percentage, positionVariation, refBase);

    }
    
    /**
     * Ermittlung der Gensequenz, um Referenzbase bei SNPs zu ermitteln
     * @return Sequenz des Referenzgens
     */
    private String getRefGenSequence() {
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

        String refGenSequence = "";
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
            fetch.setLong(1, refGenID);
            ResultSet rs = fetch.executeQuery();

            if(rs.next()){
                refGenSequence = rs.getString(FieldNames.REF_GEN_SEQUENCE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return refGenSequence;
    }

    
}
