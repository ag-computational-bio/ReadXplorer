/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.databackend;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Provides some methods for basic database queries.
 * <p>
 * @author Rolf Hilker
 */
public class GenericSQLQueries {

    private GenericSQLQueries() {
        //not for instantiation
    }


    /**
     * Retrieves an integer value from the database defined by the connection
     * object
     * according to the sqlStatement and trackID passed to this method.
     * <p>
     * @param sqlStatement statement to execute on database
     * @param identifier   the identifier of the return value from the database,
     *                     needed to get desired value from ResultSet
     * @param con          connection to the database
     * @param trackID      ID of the track
     * <p>
     * @return the value calculated for the given sqlStatement
     */
    public static int getIntegerFromDB( String sqlStatement, String identifier, Connection con, long trackID ) {
        int num = -1;
        try( PreparedStatement fetch = con.prepareStatement( sqlStatement ) ) {
            fetch.setLong( 1, trackID );

            ResultSet rs = fetch.executeQuery();
            if( rs.next() ) {
                num = rs.getInt( identifier );
            }
            rs.close();
        }
        catch( SQLException ex ) {
            Logger.getLogger( GenericSQLQueries.class.getName() ).log( Level.SEVERE, null, ex );
        }

        return num;
    }


    /**
     * @param sqlStatement the statement to fetch the latest id of some table
     *                     and
     *                     increasing it by one
     * @param con          connection to the database
     * <p>
     * @return the latest id of the querried table increased by one
     */
    public static long getLatestIDFromDB( String sqlStatement, Connection con ) {
        long id = 0;
        try( PreparedStatement latestID = con.prepareStatement( sqlStatement ) ) {

            ResultSet rs = latestID.executeQuery();
            if( rs.next() ) {
                id = rs.getLong( "LATEST_ID" );
            }
            rs.close();
        }
        catch( SQLException ex ) {
            Logger.getLogger( GenericSQLQueries.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return ++id;
    }


    public static String generateAddColumnString( String table, String column ) {
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
     * <p>
     * @param table  table
     * @param column column to add
     * @param type   the type of the column
     * <p>
     * @return SQL command
     */
    public static String genAddColumnString( String table, String column, String type ) {
        return "ALTER TABLE "
               + table
               + " ADD COLUMN IF NOT EXISTS "
               + column + " " + type;
    }


    /**
     * Adds a new column to the table.
     * <p>
     * @param table  table
     * @param column column to add
     * <p>
     * @return SQL command
     */
    public static String genRemoveColumnString( String table, String column ) {
        return "ALTER TABLE "
               + table
               + " DROP COLUMN IF EXISTS "
               + column;
    }


}