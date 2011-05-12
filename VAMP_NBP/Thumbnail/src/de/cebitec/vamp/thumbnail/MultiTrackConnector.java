package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.databackend.CoverageRequest;
import de.cebitec.vamp.databackend.CoverageThread;
import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.H2SQLStatements;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
//import de.cebitec.vamp.api.objects.Read;
import de.cebitec.vamp.api.objects.Snp;
import de.cebitec.vamp.databackend.connector.ITrackConnector;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
//import de.cebitec.vamp.databackend.connector.RunConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
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

    private void startCoverageThread(List<PersistantTrack> tracks){
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
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_MAPPINGS_FROM_INTERVAL_FOR_TRACK);
            fetch.setLong(1, trackID);
            fetch.setInt(2, from);
            fetch.setInt(3, to);
            fetch.setInt(4, from);
            fetch.setInt(5, to);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                // mapping data
                int mappingID = rs.getInt(FieldNames.MAPPING_ID);
                int sequenceID = rs.getInt(FieldNames.MAPPING_SEQUENCE);
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
    public Collection<PersistantDiff> getDiffsForIntervall(int from, int to) {

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
    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervallOrderedByMappingID(int from, int to) {

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

//    @Override
//    public int getNumOfMappedSequences() {
//        int num = 0;
//        PreparedStatement fetch;
//        try {
//            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
//                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_MAPPED_SEQUENCES_FOR_TRACK);
//            } else {
//                fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_MAPPED_SEQUENCES_FOR_TRACK);
//            }
//            fetch.setLong(1, trackID);
//
//            ResultSet rs = fetch.executeQuery();
//            if (rs.next()) {
//                num = rs.getInt("NUM");
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(MultiTrackConnector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return num;
//    }

    @Override
        public int getNumOfUniqueSequencesCalculate() {
        int num = 0;
        PreparedStatement fetch;
        try {

                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK_CALCULATE);

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MultiTrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
    public int getNumOfUniqueBmMappings() {

        int numOfBmMappings = 0;
        PreparedStatement fetch;
        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {

                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK);
            }
            fetch.setLong(1, trackID);


            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                numOfBmMappings = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MultiTrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return numOfBmMappings;
    }

    @Override
        public int getNumOfUniqueBmMappingsCalculate() {

        int numOfBmMappings = 0;
        PreparedStatement fetch;
        try {

                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_BM_MAPPINGS_FOR_TRACK_CALCULATE);

            fetch.setLong(1, trackID);


            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                numOfBmMappings = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MultiTrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return numOfBmMappings;
    }


    @Override
    public int getNumOfMappings() {
        int num = 0;
        PreparedStatement fetch;
        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK);
            }
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
        public int getNumOfMappingsCalculate() {
        int num = 0;
        PreparedStatement fetch;
        try {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_MAPPINGS_FOR_TRACK_CALCULATE);
                fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

   @Override
    public void setStatics(int numMappings, int numUniqueMappings, int numUniqueSeq, int numPerfectMappings, int numBestMatchMappings, double coveragePerf, double coverageBM, double coverageComplete) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "start storing track data");
        try {
            PreparedStatement insertStatics = con.prepareStatement(H2SQLStatements.INSERT_STATICS);
            PreparedStatement latestID = con.prepareStatement(H2SQLStatements.GET_LATEST_STATICS_ID);

            // get latest id for track
            long id = 0;
            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            id++;
            int covPerf = (int) (coveragePerf / 100 * genomeSize);
             int covBM = (int) (coverageBM / 100 * genomeSize);
              int covComplete = (int) (coverageComplete/ 100 * genomeSize);
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

    @Override
    public int getNumOfPerfectUniqueMappings() {
        int num = 0;
        PreparedStatement fetch;

        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK);
            }
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
        public int getNumOfPerfectUniqueMappingsCalculate() {
        int num = 0;
        PreparedStatement fetch;

        try {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_PERFECT_MAPPINGS_FOR_TRACK_CALCULATE);

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
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
        int fromDiff = 1;
        int toDiff = 50;
        int fromMapping = 1;
        int toMapping = 200;
        try {
            while (genomeSize > fromDiff) {
            //    Logger.getLogger(TrackConnector.class.getName()).log(Level.INFO, "find Snps by genomeposition of the diff:"+fromDiff+"-"+toDiff+" mapping position "+fromMapping+"-"+toMapping);
                PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_SNP_DATA_FOR_TRACK_FOR_INTERVALL);
                fetch.setLong(1, trackID);
                 fetch.setLong(2, fromMapping);
                fetch.setLong(3, toMapping);
                fetch.setLong(4, fromDiff);
                fetch.setLong(5, toDiff);
               fetch.setLong(6, trackID);

                fromDiff += 50;
                toDiff += 50;
                if (toDiff > genomeSize) {
                    toDiff = genomeSize;
                }

                if(fromDiff >200){
                    fromMapping = fromDiff -200;
                }
                toMapping = toDiff + 200;
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

                    addValues(covData, direction, cov, replicates, position, base, isGenomeGap);

                }
          }

            snps.addAll(filterSnps(covData, percentageThreshold, absThreshold));

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
                refGenID = rs.getInt(FieldNames.TRACK_REFGEN);
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
    public double getPercentRefGenPerfectCovered() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
                fetch = con.prepareStatement(SQLStatements.FETCH_PERFECT_COVERAGE_OF_GENOME);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_PERFECT_COVERAGE_OF_GENOME);
            }

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;

    }
    @Override
 public double getPercentRefGenPerfectCoveredCalculate() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_PERFECT_COVERED_POSITIONS_FOR_TRACK);

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;

    }


    @Override
    public double getPercentRefGenBmCovered() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
                fetch = con.prepareStatement(SQLStatements.FETCH_BM_COVERAGE_OF_GENOME);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_BM_COVERAGE_OF_GENOME);
            }
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;
    }

    @Override
        public double getPercentRefGenBmCoveredCalculate() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {
                fetch = con.prepareStatement(SQLStatements.FETCH_NUM_BM_COVERED_POSITION_FOR_TRACK);

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;
    }

    @Override
    public double getPercentRefGenNErrorCovered() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {
            if (con.getMetaData().getDatabaseProductName().contains("MySQL")) {
                fetch = con.prepareStatement(SQLStatements.FETCH_PERFECT_COVERAGE_OF_GENOME);
            } else {
                fetch = con.prepareStatement(H2SQLStatements.FETCH_COMPLETE_COVERAGE_OF_GENOME);
            }
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;
    }

    @Override
        public double getPercentRefGenNErrorCoveredCalculate() {
        double percentage = 0;
        double absValue = 0;
        PreparedStatement fetch;
        try {

          fetch = con.prepareStatement(SQLStatements.FETCH_NUM_COVERED_POSITIONS);

            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                absValue = rs.getInt("COVERED");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        percentage = absValue / genomeSize * 100;
        return percentage;
    }

    @Override
        public HashMap<Integer,Integer> getCoverageInfosofTrack(int from , int to){
            PreparedStatement fetch;
            HashMap<Integer,Integer> positionMap = new HashMap<Integer,Integer>();
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
                positionMap.put(position,coverage);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return positionMap;
        }

    @Override
    public CoverageThread getThread() {
        return this.thread;
    }

@Override
    public int getNumOfReads(){
        int num = 0;

        try {
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }

        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
        public int getNumOfReadsCalculate(){
        int num = 0;

        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_NUM_OF_READS_FOR_TRACK_CALCULATE);
            fetch.setLong(1, this.trackID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }

        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
    public int getNumOfUniqueSequences(){
        int num = 0;
        try {
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.FETCH_NUM_UNIQUE_SEQUENCES_FOR_TRACK);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if(rs.next()){
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }


    /**
     * Updates the values for number of reads and number of unique sequences
     * in the statics table of the database.
     * @param numOfReads calculated total number of reads
     * @param numOfUniqueSeq calculated total number of unique sequences
     */
    @Override
    public void updateTableStatics(int numOfReads, int numOfUniqueSeq){
      try {
            con.setAutoCommit(false);
            PreparedStatement fetch = con.prepareStatement(H2SQLStatements.UPDATE_STATIC_VALUES);
            fetch.setInt(1, numOfReads);
            fetch.setInt(2, numOfUniqueSeq);
            fetch.setLong(3, trackID);
            fetch.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public int getNumOfUniqueMappingsCalculate(){
        int num = 0;
        PreparedStatement fetch;
        try {
            fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK_CALCULATE);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

    @Override
    public int getNumOfUniqueMappings(){
        int num = 0;
        PreparedStatement fetch;
        try {
            fetch = con.prepareStatement(SQLStatements.FETCH_NUM_UNIQUE_MAPPINGS_FOR_TRACK);
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt("NUM");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }

}
