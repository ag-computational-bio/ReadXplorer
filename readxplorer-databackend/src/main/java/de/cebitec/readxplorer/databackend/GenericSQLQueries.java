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
import java.sql.Statement;


/**
 * Provides some methods for basic database queries.
 * <p>
 * @author Rolf Hilker
 */
public final class GenericSQLQueries {


    private GenericSQLQueries() {
        //not for instantiation
    }


    /**
     * Retrieves an integer value from the database defined by the connection
     * object according to the sqlStatement and trackId passed to this method.
     * <p>
     * @param sqlStatement statement to execute on database
     * @param identifier   the identifier of the return value from the database,
     *                     needed to get desired value from ResultSet
     * @param con          connection to the database
     * @param trackId      ID of the track
     * <p>
     * @return the value calculated for the given sqlStatement
     *
     * @throws SQLException An SQL exception
     */
    public static int getIntegerFromDB( String sqlStatement, String identifier, Connection con, long trackId ) throws SQLException {

        try( PreparedStatement fetch = con.prepareStatement( sqlStatement ) ) {
            fetch.setLong( 1, trackId );

            try( ResultSet rs = fetch.executeQuery() ) {
                if( rs.next() ) {
                    return rs.getInt( identifier );
                } else {
                    return -1;
                }
            }
        }
    }


    /**
     * @param sqlStmt the statement to fetch the latest id of some table and
     *                increasing it by one
     * @param con     connection to the database
     * <p>
     * @return the latest id of the querried table increased by one
     *
     * @throws SQLException An SQL exception
     */
    public static long getLatestIDFromDB( String sqlStmt, Connection con ) throws SQLException {

        long id = 0;
        try( Statement stmtLatestID = con.createStatement();
             ResultSet rs = stmtLatestID.executeQuery( sqlStmt ) ) {

            if( rs.next() ) {
                id = rs.getLong( "LATEST_ID" );
            }
        }

        return ++id;
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
        return "ALTER TABLE " +
               table +
               " ADD COLUMN IF NOT EXISTS " +
               column + " " + type;
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
        return "ALTER TABLE " +
               table +
               " DROP COLUMN IF EXISTS " +
               column;
    }


}
