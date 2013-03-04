package de.cebitec.vamp.databackend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides some methods for basic database queries.
 * 
 * @author Rolf Hilker
 */
public class GenericSQLQueries {
    
    private GenericSQLQueries(){
        //not for instantiation
    }
    
    /**
     * Retrieves an integer value from the database defined by the connection object
     * according to the sqlStatement and trackID passed to this method.
     * @param sqlStatement statement to execute on database
     * @param identifier the identifier of the return value from the database, needed to get desired value from ResultSet
     * @param con connection to the database
     * @param trackID ID of the track 
     * @return the value calculated for the given sqlStatement
     */
    public static int getIntegerFromDB(String sqlStatement, String identifier, Connection con, long trackID){
        int num = -1;
        try (PreparedStatement fetch = con.prepareStatement(sqlStatement)) {
            fetch.setLong(1, trackID);

            ResultSet rs = fetch.executeQuery();
            if (rs.next()) {
                num = rs.getInt(identifier);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(GenericSQLQueries.class.getName()).log(Level.SEVERE, null, ex);
        }

        return num;
    }
    
    /**
     * @param sqlStatement the statement to fetch the latest id of some table and
     * increasing it by one
     * @param con connection to the database
     * @return the latest id of the querried table increased by one
     */
    public static long getLatestIDFromDB(String sqlStatement, Connection con) {
        long id = 0;
        try (PreparedStatement latestID = con.prepareStatement(sqlStatement)) {

            ResultSet rs = latestID.executeQuery();
            if (rs.next()) {
                id = rs.getLong("LATEST_ID");
            }
            rs.close();
            latestID.close();
        } catch (SQLException ex) {
            Logger.getLogger(GenericSQLQueries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ++id;
    }

    
    public static String generateAddColumnString(String table, String column){
        return "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = "
                + table
                + " AND COLUMN_NAME =" + column + ")"
                + " BEGIN "
                + "ALTER TABLE "
                + table
                + " ADD COLUMN "
                + column + " BIGINT UNSIGNED "
                + " END";
    }
    

    /**
     * Adds a new column to the table.
     * @param table table
     * @param column column to add
     * @param type the type of the column
     * @return SQL command
     */
    public static String genAddColumnString(String table, String column, String type) {
        return "ALTER TABLE "
                + table
                + " ADD COLUMN "
                + column + " " + type;
    }
}
