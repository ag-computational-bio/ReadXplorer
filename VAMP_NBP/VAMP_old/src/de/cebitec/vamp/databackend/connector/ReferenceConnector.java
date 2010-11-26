package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.FieldNames;
import de.cebitec.vamp.databackend.SQLStatements;
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

    private long refGenID;
    private Connection con;

    ReferenceConnector(long refGenID){
        this.refGenID = refGenID;
        con = ProjectConnector.getInstance().getConnection();
    }

    public PersistantReference getRefGen(){
        PersistantReference gen = null;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Loading reference genome with id  \""+refGenID+"\" from database");
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_SINGLE_GENOME);
            fetch.setLong(1, refGenID);
            ResultSet rs = fetch.executeQuery();

            if(rs.next()){
                Long id = rs.getLong(FieldNames.REF_GEN_ID);
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
                int type = rs.getInt(FieldNames.FEATURE_TYPE);

                features.add(new PersistantFeature(id, ecnum, locus, product, start, stop, strand, type));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return features;
    }

    public List<PersistantTrack> getAssociatedTracks() {
        List<PersistantTrack> list = new ArrayList<PersistantTrack>();
        try {
            PreparedStatement fetch = con.prepareStatement(SQLStatements.FETCH_TRACKS_FOR_GENOME);
            fetch.setLong(1, refGenID);

            ResultSet rs = fetch.executeQuery();
            while(rs.next()){
                Long id = rs.getLong(FieldNames.TRACK_ID);
                String description = rs.getString(FieldNames.TRACK_DESCRIPTION);
                Timestamp date = rs.getTimestamp(FieldNames.TRACK_TIMESTAMP);
                Long refGenomeID = rs.getLong(FieldNames.TRACK_REFGEN);
                Long runID = rs.getLong(FieldNames.TRACK_RUN);
                list.add(new PersistantTrack(id, description, date, refGenomeID, runID));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReferenceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }


}
