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

package de.cebitec.readxplorer.ui.login;


/**
 * Class containing the login properties for ReadXplorer.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class LoginProperties {

    /**
     * Do not instantiate.
     */
    private LoginProperties() {
    }


    /**
     * Database location for a mysql DB.
     */
    public static final String LOGIN_DATABASE_MYSQL = "login.database.mysql";
    /**
     * User name to use for the DB.
     */
    public static final String LOGIN_USER = "login.user";
    /**
     * Hostname to use for the DB.
     */
    public static final String LOGIN_HOSTNAME = "login.hostname";
    /**
     * Database location for an H2 DB.
     */
    public static final String LOGIN_DATABASE_H2 = "login.database.h2";

}
