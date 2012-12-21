package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSubAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.SequenceUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The reference genome connector is responsible for the connection to a 
 * reference genome.
 *
 * @author ddoppmeier
 */
public class ReferenceConnector {

    private static final int BATCH_SIZE = 100000;
    
    private int refGenID;
    private Connection con;
//    private String projectFolder;
//    private boolean isFolderSet = false;
    private PersistantReference reference;
    private List<PersistantTrack> associatedTracks;
    

    /**
     * The reference genome connector is responsible for the connection to a 
     * reference genome.
     * @param refGenID id of the associated reference genome
     */
    ReferenceConnector(int refGenID){
        this.refGenID = refGenID;
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        this.con = projectConnector.getConnection();
        this.associatedTracks = new ArrayList<>();
//        this.projectFolder = projectConnector.getProjectFolder();
//        this.isFolderSet = !this.projectFolder.isEmpty();
    }

    /**
     * @return fetches the reference genome of the reference associated with this
     * connector. If it was called once, it is kept in memory and does not need
     * to be fetched from the DB again.
     */
    public PersistantReference getRefGenome() {
        if (this.reference == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading reference genome with id  \"{0}\" from database", refGenID);
            try {
                PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
                fetch.setLong(1, refGenID);
                ResultSet rs = fetch.executeQuery();

                if (rs.next()) {
                    String name = rs.getString(FieldNames.REF_GEN_NAME);
                    String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                    String sequence = rs.getString(FieldNames.REF_GEN_SEQUENCE);
                    Timestamp time = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);

                    this.reference = new PersistantReference(refGenID, name, description, sequence, time);
                }

            } catch (SQLException ex) {
                Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return this.reference;
    }

    /**
     * Fetches all annotations which at least partly overlap a given region 
     * of the reference.
     * @param from start position of the region of interest
     * @param to end position of the region of interest
     * @return the list of all annotations found in the interval of interest
     */
    public List<PersistantAnnotation> getAnnotationsForRegion(int from, int to){
        List<PersistantAnnotation> annotations = new ArrayList<>();
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_ANNOTATIONS_FOR_GENOME_INTERVAL);
            fetch.setLong(1, refGenID);
            fetch.setInt(2, from);
            fetch.setInt(3, to);

            ResultSet rs = fetch.executeQuery();
            while(rs.next()){
                int id = rs.getInt(FieldNames.ANNOTATION_ID);
                String ecnum = rs.getString(FieldNames.ANNOTATION_EC_NUM);
                String locus = rs.getString(FieldNames.ANNOTATION_LOCUS_TAG);
                String product = rs.getString(FieldNames.ANNOTATION_PRODUCT);
                int start = rs.getInt(FieldNames.ANNOTATION_START);
                int stop = rs.getInt(FieldNames.ANNOTATION_STOP);
                boolean isFwdStrand = rs.getInt(FieldNames.ANNOTATION_STRAND) == SequenceUtils.STRAND_FWD;
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.ANNOTATION_TYPE));
                String gene = rs.getString(FieldNames.ANNOTATION_GENE);

                annotations.add(new PersistantAnnotation(id, ecnum, locus, product, start, stop, isFwdStrand, type, gene));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return annotations;
    }
    
    /**
     * Fetches all annotations which are completely located within a given 
     * region of the reference.
     * @param left start position of the region of interest
     * @param right end position of the region of interest
     * @return the list of all annotations found in the interval of interest
     */
    public List<PersistantAnnotation> getAnnotationsForClosedInterval(int left, int right){
        List<PersistantAnnotation> annotations = new ArrayList<>();
        try {
            
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_ANNOTATIONS_FOR_CLOSED_GENOME_INTERVAL);
            
            fetch.setInt(1, refGenID);
            fetch.setInt(2, left);
            fetch.setInt(3, right);
            fetch.setInt(4, left);
            fetch.setInt(5, right);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.ANNOTATION_ID);
                String ecnum = rs.getString(FieldNames.ANNOTATION_EC_NUM);
                String locus = rs.getString(FieldNames.ANNOTATION_LOCUS_TAG);
                String product = rs.getString(FieldNames.ANNOTATION_PRODUCT);
                int start = rs.getInt(FieldNames.ANNOTATION_START);
                int stop = rs.getInt(FieldNames.ANNOTATION_STOP);
                boolean isFwdStrand = rs.getInt(FieldNames.ANNOTATION_STRAND) == SequenceUtils.STRAND_FWD;
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.ANNOTATION_TYPE));
                String gene = rs.getString(FieldNames.ANNOTATION_GENE);

                annotations.add(new PersistantAnnotation(id, ecnum, locus, product, start, stop, isFwdStrand, type, gene));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return annotations;
    }
        
    
    public List<PersistantSubAnnotation> getSubAnnotationsForRegion(int from, int to){
        List<PersistantSubAnnotation> subAnnotations = new ArrayList<>();
        try {
            PreparedStatement fetchSubAnnotations = con.prepareStatement(SQLStatements.FETCH_SUBANNOTATIONS_FOR_GENOME_INTERVAL);
            fetchSubAnnotations.setInt(1, refGenID);
            fetchSubAnnotations.setInt(2, from);
            fetchSubAnnotations.setInt(3, to);

            ResultSet rs = fetchSubAnnotations.executeQuery();
            while(rs.next()){
                int parentId = rs.getInt(FieldNames.SUBANNOTATION_PARENT_ID);
                int start = rs.getInt(FieldNames.SUBANNOTATION_START);
                int stop = rs.getInt(FieldNames.SUBANNOTATION_STOP);
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.SUBANNOTATION_TYPE));

                subAnnotations.add(new PersistantSubAnnotation(parentId, start, stop, type));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return subAnnotations;
    }
    
    public List<PersistantSubAnnotation> getSubAnnotationsForClosedInterval(int left, int right){
        List<PersistantSubAnnotation> subAnnotations = new ArrayList<>();
        try {
            
            PreparedStatement fetchExons = con.prepareStatement(SQLStatements.FETCH_SUBANNOTATIONS_FOR_CLOSED_GENOME_INTERVAL);
            
            fetchExons.setInt(1, refGenID);
            fetchExons.setInt(2, left);
            fetchExons.setInt(3, right);
            fetchExons.setInt(4, left);
            fetchExons.setInt(5, right);

            ResultSet rs = fetchExons.executeQuery();
            while(rs.next()){
                int parentId = rs.getInt(FieldNames.SUBANNOTATION_PARENT_ID);
                int start = rs.getInt(FieldNames.SUBANNOTATION_START);
                int stop = rs.getInt(FieldNames.SUBANNOTATION_STOP);
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.SUBANNOTATION_TYPE));

                subAnnotations.add(new PersistantSubAnnotation(parentId, start, stop, type));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return subAnnotations;
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
                int seqPairId = rs.getInt(FieldNames.TRACK_SEQUENCE_PAIR_ID);
                associatedTracks.add(new PersistantTrack(id, filePath, description, date, refGenomeID, seqPairId));
            }
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

}
