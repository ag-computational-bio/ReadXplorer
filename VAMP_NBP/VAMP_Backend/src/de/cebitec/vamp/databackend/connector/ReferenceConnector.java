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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceConnector {

    private static final int BATCH_SIZE = 100000;
    
    private int refGenID;
    private Connection con;
//    private String projectFolder;
//    private boolean isFolderSet = false;

    ReferenceConnector(int refGenID){
        this.refGenID = refGenID;
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        this.con = projectConnector.getConnection();
//        this.projectFolder = projectConnector.getProjectFolder();
//        this.isFolderSet = !this.projectFolder.isEmpty();
    }

    public PersistantReference getRefGen(){
        PersistantReference gen = null;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading reference genome with id  \"{0}\" from database", refGenID);
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
            fetch.setLong(1, refGenID);
            ResultSet rs = fetch.executeQuery();

            if(rs.next()){
                int id = rs.getInt(FieldNames.REF_GEN_ID);
                String name = rs.getString(FieldNames.REF_GEN_NAME);
                String description = rs.getString(FieldNames.REF_GEN_DESCRIPTION);
                String sequence = rs.getString(FieldNames.REF_GEN_SEQUENCE);
                Timestamp time = rs.getTimestamp(FieldNames.REF_GEN_TIMESTAMP);

                gen = new PersistantReference(id, name, description, sequence, time);
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return gen;
    }

    public List<PersistantAnnotation> getAnnotationsForRegion(int from, int to){
        List<PersistantAnnotation> annotations = new ArrayList<PersistantAnnotation>();
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
    
    public List<PersistantAnnotation> getAnnotationsForClosedInterval(int left, int right){
        List<PersistantAnnotation> annotations = new ArrayList<PersistantAnnotation>();
        try {
            
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_ANNOTATIONS_FOR_CLOSED_GENOME_INTERVAL);
            
            fetch.setInt(1, refGenID);
            fetch.setInt(2, left);
            fetch.setInt(3, right);
            fetch.setInt(4, left);
            fetch.setInt(5, right);

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
        
    
    public List<PersistantSubAnnotation> getSubAnnotationsForRegion(int from, int to){
        List<PersistantSubAnnotation> subAnnotations = new ArrayList<PersistantSubAnnotation>();
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
        List<PersistantSubAnnotation> subAnnotations = new ArrayList<PersistantSubAnnotation>();
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
        List<PersistantTrack> list = new ArrayList<PersistantTrack>();
        
        //fetch tracks from db
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACKS_FOR_GENOME);
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenomeID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
                String filePath = rs.getString(FieldNames.TRACK_PATH);
                int seqPairId = rs.getInt(FieldNames.TRACK_SEQUENCE_PAIR_ID);
                list.add(new PersistantTrack(id, filePath, description, date, refGenomeID, seqPairId));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }

}
