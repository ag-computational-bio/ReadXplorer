package de.cebitec.readXplorer.databackend.connector;

import de.cebitec.readXplorer.databackend.FieldNames;
import de.cebitec.readXplorer.databackend.SQLStatements;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The reference genome connector is responsible for the connection to a
 * reference genome.
 *
 * @author ddoppmeier, rhilker
 */
public class ReferenceConnector {

    private int refGenID;
    private Connection con;
//    private String projectFolder;
//    private boolean isFolderSet = false;
    private List<PersistantTrack> associatedTracks;

    /**
     * The reference genome connector is responsible for the connection to a
     * reference genome.
     * @param refGenID id of the associated reference genome
     */
    ReferenceConnector(int refGenID) {
        this.refGenID = refGenID;
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        this.con = projectConnector.getConnection();
        this.associatedTracks = new ArrayList<>();
//        this.projectFolder = projectConnector.getProjectFolder();
//        this.isFolderSet = !this.projectFolder.isEmpty();
    }

    /**
     * @return Fetches the reference genome of the reference associated with
     * this connector. If it was called once, it is kept in memory and does not
     * need to be fetched from the DB again.
     */
    public PersistantReference getRefGenome() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading reference genome with id  \"{0}\" from database", refGenID);
        PersistantReference reference = null;

        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME)) {
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                Timestamp time = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);

                reference = new PersistantReference(refGenID, name, description, time);
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return reference;
    }

    /**
     * @return All chromosomes of this reference without their sequence.
     */
    public Map<Integer, PersistantChromosome> getChromosomesForGenome() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading chromosomes for reference with id  \"{0}\" from database", refGenID);

        Map<Integer, PersistantChromosome> chromosomes = new HashMap<>();
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_CHROMOSOMES)) {
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.CHROM_ID);
                int chromNumber = rs.getInt(FieldNames.CHROM_NUMBER);
                String name = rs.getString(FieldNames.CHROM_NAME);
                int chromLength = rs.getInt(FieldNames.CHROM_LENGTH);

                chromosomes.put(id, new PersistantChromosome(id, chromNumber, refGenID, name, chromLength));
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return chromosomes;
    }
    
    /**
     * @param chromId the id of the chromosome to fetch
     * @return One chromosome of this reference without its sequence.
     */
    public PersistantChromosome getChromosomeForGenome(int chromId) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading chromosome for reference with id  \"{0}\" from database", refGenID);
        
        PersistantChromosome chrom = null;
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_CHROMOSOME)) {
            fetch.setLong(1, chromId);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int chromNumber = rs.getInt(FieldNames.CHROM_NUMBER);
                String name = rs.getString(FieldNames.CHROM_NAME);
                int chromLength = rs.getInt(FieldNames.CHROM_LENGTH);

                chrom = new PersistantChromosome(chromId, chromNumber, refGenID, name, chromLength);
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return chrom;
    }

    /**
     *
     * @param chromId id of the chromosome to retrieve from this 
     * reference genome.
     * @return Fetches the needed chromosome sequence of the reference.
     */
    public String getChromSequence(int chromId) {
        String sequence = "";
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading chromosome sequence with id  \"{0}\" from database", chromId);
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_CHROM_SEQ)) {
            fetch.setLong(1, chromId);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                sequence = rs.getString(FieldNames.CHROM_SEQUENCE);
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            //dirty fix for a bug in h2 database stating "lob not found"
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.INFO,
                    "Cound not read reference sequence from dabase (this probably a concurrency issue), ignoring ..  ",
                    e);
        }

        return sequence;
    }

    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference.
     * @param from start position of the region of interest
     * @param to end position of the region of interest
     * @param featureType type of features to retrieve from the db. Either
     * FeatureType.ANY or a specified type
     * @param chromId chromosome id of the features of interest
     * @return the list of all features found in the interval of interest
     */
    public List<PersistantFeature> getFeaturesForRegion(int from, int to, FeatureType featureType, int chromId) {
        List<PersistantFeature> features = new ArrayList<>();
        try {
            PreparedStatement fetch;
            if (featureType == FeatureType.ANY) {
                fetch = con.prepareStatement(SQLStatements.FETCH_FEATURES_FOR_CHROM_INTERVAL);
                fetch.setLong(1, chromId);
                fetch.setInt(2, from);
                fetch.setInt(3, to);
            } else {
                fetch = con.prepareStatement(SQLStatements.FETCH_SPECIFIED_FEATURES_FOR_CHROM_INTERVAL);
                fetch.setLong(1, chromId);
                fetch.setInt(2, from);
                fetch.setInt(3, to);
                fetch.setInt(4, featureType.getTypeInt());
            }

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.FEATURE_ID);
                String parentIds = rs.getString(FieldNames.FEATURE_PARENT_IDS);
                parentIds = parentIds.equals("0") ? "" : parentIds;
                String ecnum = rs.getString(FieldNames.FEATURE_EC_NUM);
                String locus = rs.getString(FieldNames.FEATURE_LOCUS_TAG);
                String product = rs.getString(FieldNames.FEATURE_PRODUCT);
                int start = rs.getInt(FieldNames.FEATURE_START);
                int stop = rs.getInt(FieldNames.FEATURE_STOP);
                boolean isFwdStrand = rs.getInt(FieldNames.FEATURE_STRAND) == SequenceUtils.STRAND_FWD;
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.FEATURE_TYPE));
                String gene = rs.getString(FieldNames.FEATURE_GENE);

                features.add(new PersistantFeature(id, chromId, parentIds, ecnum, locus, product, start, stop, isFwdStrand, type, gene));
            }
            rs.close();
            fetch.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }

    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference.
     * @param from start position of the region of interest
     * @param to end position of the region of interest
     * @param usedFeatures list of features used to retrieve from the db.
     * @param chromId chromosome id of the features of interest
     * @return the list of all features found in the interval of interest
     */
    public List<PersistantFeature> getFeaturesForRegion(int from, int to, List<FeatureType> usedFeatures, int chromId) {
        List<PersistantFeature> features = new ArrayList<>();
        for (Iterator<FeatureType> it = usedFeatures.iterator(); it.hasNext();) {
            FeatureType featureType = it.next();
            features.addAll(getFeaturesForRegion(from, to, featureType, chromId));
        }
        return features;
    }

    /**
     * Fetches all features which are completely located within a given region
     * of the reference.
     * @param left start position of the region of interest
     * @param right end position of the region of interest
     * @param chromId chromosome id of the features of interest
     * @return the list of all features found in the interval of interest
     */
    public List<PersistantFeature> getFeaturesForClosedInterval(int left, int right, int chromId) {
        List<PersistantFeature> features = new ArrayList<>();
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_FEATURES_FOR_CLOSED_GENOME_INTERVAL)) {

            fetch.setInt(1, chromId);
            fetch.setInt(2, left);
            fetch.setInt(3, right);
            fetch.setInt(4, left);
            fetch.setInt(5, right);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.FEATURE_ID);
                String parentIds = rs.getString(FieldNames.FEATURE_PARENT_IDS);
                String ecnum = rs.getString(FieldNames.FEATURE_EC_NUM);
                String locus = rs.getString(FieldNames.FEATURE_LOCUS_TAG);
                String product = rs.getString(FieldNames.FEATURE_PRODUCT);
                int start = rs.getInt(FieldNames.FEATURE_START);
                int stop = rs.getInt(FieldNames.FEATURE_STOP);
                boolean isFwdStrand = rs.getInt(FieldNames.FEATURE_STRAND) == SequenceUtils.STRAND_FWD;
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.FEATURE_TYPE));
                String gene = rs.getString(FieldNames.FEATURE_GENE);

                features.add(new PersistantFeature(id, chromId, parentIds, ecnum, locus, product, start, stop, isFwdStrand, type, gene));
            }
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }

    /**
     * @return the tracks associated to this reference connector.
     */
    public List<PersistantTrack> getAssociatedTracks() {
        try (PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACKS_FOR_GENOME)) {
            this.associatedTracks.clear();
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenomeID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
                String filePath = rs.getString(FieldNames.TRACK_PATH);
                int readPairId = rs.getInt(FieldNames.TRACK_READ_PAIR_ID);
                associatedTracks.add(new PersistantTrack(id, filePath, description, date, refGenomeID, readPairId));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return associatedTracks;
    }

    /**
     * Calculates and returns the names of all tracks belonging to this
     * reference hashed to their track id.
     * @return the names of all tracks of this reference hashed to their track
     * id.
     */
    public HashMap<Integer, String> getAssociatedTrackNames() {
        this.getAssociatedTracks(); //ensures the tracks are already in the list

        HashMap<Integer, String> namesList = new HashMap<>();
        for (PersistantTrack track : associatedTracks) {
            namesList.put(track.getId(), track.getDescription());
        }
        return namesList;
    }

    /**
     * Checks if this reference genome has at least one feature of any of the 
     * given types.
     * @param typeList the feature type list to check
     * @return true, if this reference genome has at least one feature of the
     * given type, false otherwise
     */
    public boolean hasFeatures(List<FeatureType> typeList) {
        for (Iterator<FeatureType> it = typeList.iterator(); it.hasNext();) {
            FeatureType featureType = it.next();
            if (hasFeatures(featureType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this reference genome has at least one feature of the given type.
     * @param type the feature type to check
     * @return true, if this reference genome has at least one feature of the
     * given type, false otherwise
     */
    public boolean hasFeatures(FeatureType type) {
        try {
            PreparedStatement fetch;
            if (type == FeatureType.ANY) {//TODO: update check feature method for chromosomes!!
                fetch = con.prepareStatement(SQLStatements.CHECK_IF_FEATURES_EXIST);
                fetch.setLong(1, refGenID);
            } else {
                fetch = con.prepareStatement(SQLStatements.CHECK_IF_FEATURES_OF_TYPE_EXIST);
                fetch.setLong(1, refGenID);
                fetch.setLong(2, type.getTypeInt());
            }

            ResultSet rs = fetch.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
