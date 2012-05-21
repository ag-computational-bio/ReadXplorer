package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author ddoppmei, rhilker
 */
public class CoverageThread extends Thread implements RequestThreadI {

    private long trackID;
    private long trackID2;
    private List<Integer> trackIds;
    private Connection con;
    private ConcurrentLinkedQueue<GenomeRequest> requestQueue;
    private PersistantCoverage currentCov;
    private int coveredWidth;
    private GenomeRequest latestRequest;
    private double requestCounter;
    private double skippedCounter;

    public CoverageThread(List<Integer> trackIds, boolean combineTracks) {
        super();
        // do general stuff
        this.requestQueue = new ConcurrentLinkedQueue<GenomeRequest>();
        con = ProjectConnector.getInstance().getConnection();
        coveredWidth = 25000;
        requestCounter = 0;
        skippedCounter = 0;

        // do id specific stuff
        if (trackIds.size() == 1) {
            this.singleCoverageThread(trackIds.get(0));
        } else if (trackIds.size() == 2 && !combineTracks) {
            this.doubleCoverageThread(trackIds.get(0), trackIds.get(1));
        } else if (trackIds.size() >= 2) {
            this.multipleCoverageThread(trackIds);
        } else {
            throw new UnsupportedOperationException("At least one track needs to be handed over to the CoverageThread.");
        }
    }

    private void singleCoverageThread(long trackID) {
        this.trackID = trackID;
        trackID2 = 0;
        currentCov = new PersistantCoverage(0, 0);
    }

    private void doubleCoverageThread(long trackID, long trackID2) {
        this.trackID = trackID;
        this.trackID2 = trackID2;
        currentCov = new PersistantCoverage(0, 0, true);
    }

    private void multipleCoverageThread(List<Integer> trackIds) {
        this.trackIds = trackIds;
        this.trackID = 0;
        this.trackID2 = 0;
        currentCov = new PersistantCoverage(0, 0);
    }

    public void setCoveredWidth(int coveredWidth) {
        this.coveredWidth = coveredWidth;
    }

    private int calcCenterLeft(GenomeRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int interval = request.getTo() - request.getFrom();
        coveredWidth = interval > coveredWidth * 2 ? interval / 2 : coveredWidth;
        int result = centerMiddle - coveredWidth;
        return result < 0 ? 0 : result;
    }

    private int calcCenterRight(GenomeRequest request) {
        int centerMiddle = calcCenterMiddle(request);
        int interval = request.getTo() - request.getFrom();
        coveredWidth = interval > coveredWidth * 2 ? interval / 2 : coveredWidth;
        int result = centerMiddle + coveredWidth;
        return result;
    }

    private int calcCenterMiddle(GenomeRequest request) {
        return (request.getFrom() + request.getTo()) / 2;
    }

    @Override
    public void addRequest(GenomeRequest request) {
        latestRequest = request;
        requestQueue.add(request);
    }

    private boolean matchesLatestRequestBounds(GenomeRequest request) {
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);

        // rounding error somewhere....
        if (currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads the fwd and rev coverage combined for one single track. Meaning, it
     * contains the absoulte coverage values for each position of the track included
     * in the request.
     * @param request the request to carry out for one track and a given interval
     * @return the PersistantCoverage for the given interval and the track
     * @throws SQLException 
     */
    private PersistantCoverage loadCoverage(GenomeRequest request) throws SQLException {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.setTwoTracks(false);
        PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK);
        fetch.setInt(1, from);
        fetch.setInt(2, to);
        fetch.setLong(3, trackID);

        ResultSet rs = fetch.executeQuery();
//        int counter = 0;
//        int tmpHighestCov = 0;
        while (rs.next()) {
            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
//            counter++;
            //perfect cov
            cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
            cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
            cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
            cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));
            //best match cov

            cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
            cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
            cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
            cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));
            //complete cov

            int covNFWMult = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
            int covNRevMult = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//            if (pos >= request.getFrom() & pos <= request.getTo()
//                    && (tmpHighestCov < covNFWMult || tmpHighestCov < covNRevMult)) {
//                tmpHighestCov = covNFWMult < covNRevMult ? covNRevMult : covNFWMult;
//                cov.setHighestCoverage(tmpHighestCov);
//            }
            cov.setCommonFwdMult(pos, covNFWMult);
            cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
            cov.setCommonRevMult(pos, covNRevMult);
            cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

        }
        fetch.close();
        rs.close();

        return cov;
    }

    /**
     * Fetches the best match coverage of an interval of a certain track.
     *
     * @param request the coverage request to carry out
     * @return the best match coverage of an interval of a certain track.
     */
    private PersistantCoverage loadCoverageBest(GenomeRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);
        cov.setTwoTracks(false);
        PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_BEST_FOR_INTERVAL);
        fetch.setLong(1, trackID);
        fetch.setInt(2, from);
        fetch.setInt(3, to);

        ResultSet rs = fetch.executeQuery();

        while (rs.next()) {
            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
            //perfect cov
            cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
            cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
            //best match cov
            cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
            cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
        }
        fetch.close();
        rs.close();

        return cov;
    }

    /**
     * Loads the coverage for two tracks, which should not be combined, but
     * viewed as two data sets like in the DoubleTrackViewer. The returned
     * PersistantCoverage will also contain the coverage difference for all
     * positions, which are covered in both tracks.
     *
     * @param request the genome request for two tracks.
     * @return PersistantCoverage of the interval of both tracks, also
     * containing the coverage difference for all positions, which are covered
     * in both tracks.
     * @throws SQLException
     */
    private PersistantCoverage loadCoverage2(GenomeRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to, true);
        cov.setTwoTracks(true);
        PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);
        PreparedStatement fetch2 = con.prepareStatement(SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK2);
        fetch.setInt(1, from);
        fetch.setInt(2, to);
        fetch.setLong(3, trackID);
        fetch2.setInt(1, from);
        fetch2.setInt(2, to);
        fetch2.setLong(3, trackID2);
        ResultSet rs2 = fetch2.executeQuery();
        ResultSet rs = fetch.executeQuery();
//        int counter = 0;
        while (rs2.next()) {
            int pos = rs2.getInt(FieldNames.COVERAGE_POSITION);
            //coverage of Track2
            cov.setCommonFwdMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_FW_MULT));
            cov.setCommonRevMultTrack2(pos, rs2.getInt(FieldNames.COVERAGE_N_RV_MULT));

        }
        while (rs.next()) {
            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);

            //check if cov of track 2 exists at position
            int nFwMultTrack2 = cov.getCommonFwdMultTrack2(pos);
            int nRvMultTrack2 = cov.getCommonRevMultTrack2(pos);
            int nFwMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
            int nRvMultTrack1 = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);

            //we just set coverage of the diff if cov of  track 2 or track 1 exist
            if (nFwMultTrack1 != 0 && nFwMultTrack2 != 0) {
                cov.setCommonFwdMult(pos, Math.abs(nFwMultTrack1 - nFwMultTrack2));
            }
            if (nRvMultTrack1 != 0 && nRvMultTrack2 != 0) {
                cov.setCommonRevMult(pos, Math.abs(nRvMultTrack1 - nRvMultTrack2));
            }

            cov.setCommonFwdMultTrack1(pos, nFwMultTrack1);
            cov.setCommonRevMultTrack1(pos, nRvMultTrack1);
        }

        fetch2.close();
        fetch.close();
        rs.close();
        rs2.close();

        return cov;
    }

    /**
     * Loads the coverage for multiple tracks in one PersistantCoverage object.
     * This is more efficient, than storing it in several PersistantCoverage
     * objects, each for one track.
     *
     * @param request the genome request to carry out
     * @return the coverage of multiple track combined in one PersistantCoverage
     * object.
     */
    private PersistantCoverage loadCoverageMutliple(GenomeRequest request) throws SQLException {
        int from = this.calcCenterLeft(request);
        int to = this.calcCenterRight(request);

        PersistantCoverage cov = new PersistantCoverage(from, to);

        //create the sql statement dynamically according to the number of tracks combined
        String dynamicSqlStatement = SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART1 + " ( ";
        for (int i = 0; i < this.trackIds.size(); ++i) {
            if (i > 0) {
                dynamicSqlStatement += " OR ";
            }
            dynamicSqlStatement += SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART2;

        }
        dynamicSqlStatement += ");";

        PreparedStatement fetch = con.prepareStatement(dynamicSqlStatement);
        fetch.setInt(1, from);
        fetch.setInt(2, to);
        for (int i = 0; i < this.trackIds.size(); ++i) {
            fetch.setInt(3 + i, this.trackIds.get(i));
        }

        ResultSet rs = fetch.executeQuery();
//        int counter = 0;
//        int tmpHighestCov = 0;
        while (rs.next()) {

            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
//            counter++;
            //perfect cov
            cov.setPerfectFwdMult(pos, cov.getPerfectFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
            cov.setPerfectFwdNum(pos, cov.getPerfectFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
            cov.setPerfectRevMult(pos, cov.getPerfectRevMult(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
            cov.setPerfectRevNum(pos, cov.getPerfectRevNum(pos) + rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

            //best match cov
            cov.setBestMatchFwdMult(pos, cov.getBestMatchFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
            cov.setBestMatchFwdNum(pos, cov.getBestMatchFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
            cov.setBestMatchRevMult(pos, cov.getBestMatchRevMult(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
            cov.setBestMatchRevNum(pos, cov.getBestMatchRevNum(pos) + rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));

            //complete cov and highest coverage in interval calculation
            int covCommonFwdMult = cov.getCommonFwdMult(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
            int covCommonRevMult = cov.getCommonRevMult(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//            if (pos >= request.getFrom() && pos <= request.getTo()
//                    && (tmpHighestCov < covCommonFwdMult || tmpHighestCov < covCommonRevMult)) {
//                tmpHighestCov = covCommonFwdMult < covCommonRevMult ? covCommonRevMult : covCommonFwdMult;
//                cov.setHighestCoverage(tmpHighestCov);
//            }
            cov.setCommonFwdMult(pos, covCommonFwdMult);
            cov.setCommonFwdNum(pos, cov.getCommonFwdNum(pos) + rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
            cov.setCommonRevMult(pos, covCommonRevMult);
            cov.setCommonRevNum(pos, cov.getCommonRevNum(pos) + rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

        }
        fetch.close();
        rs.close();

        return cov;
    }

    /**
     * Loads the coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     *
     * @param request the request with more than one track
     * @return The coverage for multiple tracks separated in different
     * PersistantCoverage objects.
     */
    private PersistantCoverage[] loadCoverageMutliple2(GenomeRequest request) throws SQLException {
        int from = calcCenterLeft(request);
        int to = calcCenterRight(request);

        PersistantCoverage[] covArray = new PersistantCoverage[this.trackIds.size()];
        Map<Integer, PersistantCoverage> covMap = new HashMap<Integer, PersistantCoverage>();
        for (int i = 0; i < this.trackIds.size(); ++i) {
            covArray[i] = new PersistantCoverage(from, to);
            covMap.put(this.trackIds.get(i), new PersistantCoverage(from, to));
        }

        //create the sql statement dynamically according to the number of tracks combined
        String dynamicSqlStatement = SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART1 + " ( ";
        for (int i = 0; i < this.trackIds.size(); ++i) {
            if (i > 0) {
                dynamicSqlStatement += " OR ";
            }
            dynamicSqlStatement += SQLStatements.FETCH_COVERAGE_FOR_INTERVAL_OF_TRACK_PART2;
        }
        dynamicSqlStatement += ");";

        PreparedStatement fetch = con.prepareStatement(dynamicSqlStatement);
        fetch.setInt(1, from);
        fetch.setInt(2, to);
        for (int i = 0; i < this.trackIds.size(); ++i) {
            fetch.setInt(3 + i, this.trackIds.get(i));
        }

        ResultSet rs = fetch.executeQuery();
//        int counter = 0;
//        int tmpHighestCov = 0;
        while (rs.next()) {

            PersistantCoverage cov = covMap.get(rs.getInt(FieldNames.COVERAGE_TRACK));

            int pos = rs.getInt(FieldNames.COVERAGE_POSITION);
            //   counter++;
            //perfect cov
            cov.setPerfectFwdMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_MULT));
            cov.setPerfectFwdNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_FW_NUM));
            cov.setPerfectRevMult(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_MULT));
            cov.setPerfectRevNum(pos, rs.getInt(FieldNames.COVERAGE_ZERO_RV_NUM));

            //best match cov
            cov.setBestMatchFwdMult(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_MULT));
            cov.setBestMatchFwdNum(pos, rs.getInt(FieldNames.COVERAGE_BM_FW_NUM));
            cov.setBestMatchRevMult(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_MULT));
            cov.setBestMatchRevNum(pos, rs.getInt(FieldNames.COVERAGE_BM_RV_NUM));

            //complete cov and highest coverage in interval calculation
            int covNFWMult = rs.getInt(FieldNames.COVERAGE_N_FW_MULT);
            int covNRevMult = rs.getInt(FieldNames.COVERAGE_N_RV_MULT);
//            if (pos >= request.getFrom() && pos <= request.getTo()
//                    && (tmpHighestCov < covNFWMult || tmpHighestCov < covNRevMult)) {
//                tmpHighestCov = covNFWMult < covNRevMult ? covNRevMult : covNFWMult;
//                cov.setHighestCoverage(tmpHighestCov);
//            }
            cov.setCommonFwdMult(pos, covNFWMult);
            cov.setCommonFwdNum(pos, rs.getInt(FieldNames.COVERAGE_N_FW_NUM));
            cov.setCommonRevMult(pos, covNRevMult);
            cov.setCommonRevNum(pos, rs.getInt(FieldNames.COVERAGE_N_RV_NUM));

        }
        fetch.close();
        rs.close();

        Iterator<PersistantCoverage> covIt = covMap.values().iterator();
        int count = 0;
        while (covIt.hasNext()) {
            covArray[count++] = covIt.next();
        }
        return covArray;
    }

    @Override
    public void run() {

        try {
            while (!interrupted()) {
                GenomeRequest request = requestQueue.poll();
                if (request != null) {
                    if (!currentCov.coversBounds(request.getFrom(), request.getTo())) {
                        requestCounter++;
                        if (matchesLatestRequestBounds(request)) {
                            if (trackID2 != 0) {
                                currentCov = this.loadCoverage2(request); //at the moment we only need the complete coverage here
                            } else if (this.trackID != 0) {
                                if (request.getDesiredCoverage() == Properties.COMPLETE_COVERAGE) {
                                    currentCov = this.loadCoverage(request);
                                } else if (request.getDesiredCoverage() == Properties.BEST_MATCH_COVERAGE) {
                                    currentCov = this.loadCoverageBest(request);
                                } //else request.getDesiredCoverage() == Properties.PERFECT_COVERAGE does not exist yet, as it is not needed yet
                            } else if (this.trackIds != null && !this.trackIds.isEmpty()) {
                                currentCov = this.loadCoverageMutliple(request);
                            }
                        } else {
                            skippedCounter++;
                        }
                    }
                    if (this.matchesLatestRequestBounds(request)) {
                        request.getSender().receiveData(currentCov);
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CoverageThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            String msg = NbBundle.getMessage(CoverageThread.class, "OOM_Message",
                    "An out of memory error occured during fetching the references. Please restart the software with more memory.");
            String title = NbBundle.getMessage(CoverageThread.class, "OOM_Header", "Restart Software");
            JOptionPane.showMessageDialog(new JPanel(), msg, title, JOptionPane.INFORMATION_MESSAGE);
            this.interrupt();
        } catch (SQLException e) {
            String msg;
            String title;
            if (e.getMessage().contains("Out of memory")) {
                msg = NbBundle.getMessage(CoverageThread.class, "OOM_Message",
                        "An out of memory error occured during fetching the references. Please restart the software with more memory.");
                title = NbBundle.getMessage(CoverageThread.class, "OOM_Header", "Restart Software");
                JOptionPane.showMessageDialog(new JPanel(), msg, title, JOptionPane.INFORMATION_MESSAGE);
                this.interrupt();
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
