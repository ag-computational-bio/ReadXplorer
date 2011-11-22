package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.SQLStatements;
import de.cebitec.vamp.databackend.dataObjects.PersistantExon;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    ReferenceConnector(int refGenID){
        this.refGenID = refGenID;
        con = ProjectConnector.getInstance().getConnection();
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

    public List<PersistantFeature> getFeaturesForRegion(int from, int to){
        List<PersistantFeature> features = new ArrayList<PersistantFeature>();
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_FEATURES_FOR_INTERVAL_FROM_GENOME);
            fetch.setLong(1, refGenID);
            fetch.setInt(2, from);
            fetch.setInt(3, to);

            ResultSet rs = fetch.executeQuery();
            while(rs.next()){
                int id = rs.getInt(FieldNames.FEATURE_ID);
                String ecnum = rs.getString(FieldNames.FEATURE_ECNUM);
                String locus = rs.getString(FieldNames.FEATURE_LOCUS);
                String product = rs.getString(FieldNames.FEATURE_PRODUCT);
                int start = rs.getInt(FieldNames.FEATURE_START);
                int stop = rs.getInt(FieldNames.FEATURE_STOP);
                int strand = rs.getInt(FieldNames.FEATURE_STRAND);
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.FEATURE_TYPE));
                String gene = rs.getString(FieldNames.FEATURE_GENE);

                features.add(new PersistantFeature(id, ecnum, locus, product, start, stop, strand, type, gene));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }
    
    public List<PersistantFeature> getFeaturesForGenome(int left, int right){
        List<PersistantFeature> features = new ArrayList<PersistantFeature>();
        try {
            
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_FEATURES_FOR_CLOSED_INTERVAL_FROM_GENOME);
            
            fetch.setInt(1, refGenID);
            fetch.setInt(2, left);
            fetch.setInt(3, right);
            fetch.setInt(4, left);
            fetch.setInt(5, right);

            ResultSet rs = fetch.executeQuery();
            while(rs.next()){
                int id = rs.getInt(FieldNames.FEATURE_ID);
                String ecnum = rs.getString(FieldNames.FEATURE_ECNUM);
                String locus = rs.getString(FieldNames.FEATURE_LOCUS);
                String product = rs.getString(FieldNames.FEATURE_PRODUCT);
                int start = rs.getInt(FieldNames.FEATURE_START);
                int stop = rs.getInt(FieldNames.FEATURE_STOP);
                int strand = rs.getInt(FieldNames.FEATURE_STRAND);
                FeatureType type = FeatureType.getFeatureType(rs.getInt(FieldNames.FEATURE_TYPE));
                String gene = rs.getString(FieldNames.FEATURE_GENE);

                features.add(new PersistantFeature(id, ecnum, locus, product, start, stop, strand, type, gene));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }
        
    public List<PersistantExon> getSubfeaturesForRegion(int from, int to){
        List<PersistantExon> subfeatures = new ArrayList<PersistantExon>();
        try {
            PreparedStatement fetchExons = con.prepareStatement(SQLStatements.FETCH_SUBFEATURES_FOR_GENOMIC_INTERVAL);
            fetchExons.setInt(1, refGenID);
            fetchExons.setInt(1, from);
            fetchExons.setInt(2, to);

            ResultSet rs = fetchExons.executeQuery();
            while(rs.next()){
                int parentId = rs.getInt(FieldNames.SUBFEATURES_FEATURE_ID);
                int start = rs.getInt(FieldNames.SUBFEATURES_START);
                int stop = rs.getInt(FieldNames.SUBFEATURES_STOP);

                subfeatures.add(new PersistantExon(parentId, start, stop));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return subfeatures;
    }
    
    public List<PersistantExon> getSubfeaturesForGenome(int left, int right){
        List<PersistantExon> subfeatures = new ArrayList<PersistantExon>();
        try {
            
            PreparedStatement fetchExons = con.prepareStatement(SQLStatements.FETCH_SUBFEATURES_FOR_CLOSED_GENOMIC_INTERVAL);
            
            fetchExons.setInt(1, refGenID);
            fetchExons.setInt(2, left);
            fetchExons.setInt(3, right);
            fetchExons.setInt(4, left);
            fetchExons.setInt(5, right);

            ResultSet rs = fetchExons.executeQuery();
            while(rs.next()){
                int parentId = rs.getInt(FieldNames.SUBFEATURES_FEATURE_ID);
                int start = rs.getInt(FieldNames.SUBFEATURES_START);
                int stop = rs.getInt(FieldNames.SUBFEATURES_STOP);

                subfeatures.add(new PersistantExon(parentId, start, stop));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return subfeatures;
    }

    public List<PersistantTrack> getAssociatedTracks() {
        List<PersistantTrack> list = new ArrayList<PersistantTrack>();
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACKS_FOR_GENOME);
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            while(rs.next()){
                int id = rs.getInt(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                int refGenomeID = rs.getInt(FieldNames.TRACK_REFERENCE_ID);
//                Long runID = rs.getLong(FieldNames.TRACK_RUN);
                list.add(new PersistantTrack(id, description, date, refGenomeID));//, runID));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }

}
