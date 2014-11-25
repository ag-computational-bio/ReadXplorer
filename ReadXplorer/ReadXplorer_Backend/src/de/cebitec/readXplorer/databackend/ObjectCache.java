/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Object cache provides the possibility to save any serializable 
 * object in the database and load it at any later moment even
 * if the program will be closed
 * 
 * The objects are saved according to a simple scheme:
 * - any object has a family name and a key name
 *   and can be saved or accessed by providing those
 * - objects of the same family can be deleted all at once 
 * 
 * @author evgeny
 */
public class ObjectCache {
    private static ObjectCache instance;
    
    //change the cache version if the object structure of cached responses changes
    public final static int CACHEVERSION = 3;
    
    /** provides singleton pattern */
    public static synchronized ObjectCache getInstance() {
        if (instance == null) {
            instance = new ObjectCache();
        }
        return instance;
    }
    
    public Object get(String family, String key) {
        
        if (NbPreferences.forModule(Object.class).getBoolean(Properties.OBJECTCACHE_ACTIVE, true)) {
            PreparedStatement fetch;
            try {
                fetch = ProjectConnector.getInstance()
                .getConnection().prepareStatement(SQLStatements.FETCH_OBJECTFROMCACHE);
                fetch.setString(1, family);
                fetch.setString(2, key);

                ResultSet rs = fetch.executeQuery();
                if (rs.next()) {
                    Object id = rs.getBytes(FieldNames.OBJECTCACHE_DATA);
                    ByteArrayInputStream bais;
                    GZIPInputStream gz;
                    ObjectInputStream ins;
                    bais = new ByteArrayInputStream(rs.getBytes(FieldNames.OBJECTCACHE_DATA));
                    try {
                        gz = new GZIPInputStream(bais);
                        ins = new ObjectInputStream(gz);
                        Object data = ins.readObject();
                        return data;
                    } catch (Exception e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                        "Failed to load object " + family + "." + " from cache: "+ e.getMessage());
                    }                 
                }
            } catch (SQLException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                "Failed to load object " + family + "." + " from cache: "+ e.getMessage());
                return null;
            }
        }
           
        
        return null;
        
    }
    
    public void set(String family, String key, Serializable data) {
        
        if (NbPreferences.forModule(Object.class).getBoolean(Properties.OBJECTCACHE_ACTIVE, true)) {
            StopWatch watch = new StopWatch();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            GZIPOutputStream gz;
            ObjectOutputStream    oos; 
            try {
                gz = new GZIPOutputStream(baos);
                oos = new ObjectOutputStream( gz );
                oos.writeObject( data ); 
                oos.close(); 
                byte[] array = baos.toByteArray();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
                "Needed " + watch.getElapsedTimeAsString() + " to create byte representation ("+array.length+" bytes)");

                this.delete(family, key);

                try {
                    PreparedStatement insert = ProjectConnector.getInstance()
                    .getConnection().prepareStatement(SQLStatements.INSERT_OBJECTINTOCACHE);
                    insert.setString(1, family);
                    insert.setString(2, key);
                    insert.setObject(3, array);
                    insert.executeUpdate();
                    insert.close();

                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                }
                catch (NullPointerException ex) { 
                    Exceptions.printStackTrace(ex);
                }



            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public boolean delete(String family, String key) {
        PreparedStatement update;
        try {
            update = ProjectConnector.getInstance()
            .getConnection().prepareStatement(SQLStatements.DELETE_OBJECTFROMCACHE);
            update.setString(1, family);
            update.setString(2, key);
            int affectedRows = update.executeUpdate();
            
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
            "Deleted " + family + "."+ key +" key from ObjectCache: "+affectedRows+" rows affected.");
            return ( affectedRows > 0);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;        
    }
    
    public boolean deleteFamily(String family) {
        PreparedStatement update;
        try {
            update = ProjectConnector.getInstance()
            .getConnection().prepareStatement(SQLStatements.DELETE_OBJECTFAMILYFROMCACHE);
            update.setString(1, family);
            int affectedRows = update.executeUpdate();
            
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
            "Deleted " + family + " family from ObjectCache: "+affectedRows+" rows affected.");
            return ( affectedRows > 0);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;       
    }
    
    /**
     * returns the name of the cache family, that is to be used 
     * to determine that a certain track has been allready cached
     * @return cache family name
     */
    public static String getTrackCacherFieldFamily() {
        return "TrackCacher."+ObjectCache.CACHEVERSION+"."
                + CoverageAndDiffResultPersistant.serialVersionUID 
                + "." + "run";
    }
      
}
