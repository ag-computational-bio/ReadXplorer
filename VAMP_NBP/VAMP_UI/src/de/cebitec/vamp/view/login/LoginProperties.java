package de.cebitec.vamp.view.login;

/**
 * Class containing the login properties for VAMP.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class LoginProperties {

    /**
     * Do not instantiate.
     */
    private LoginProperties() {
    }
    
    /** Database location for a mysql DB. */
    public static String LOGIN_DATABASE_MYSQL = "login.database.mysql";
    /** User name to use for the DB. */
    public static String LOGIN_USER = "login.user";
    /** Hostname to use for the DB. */
    public static String LOGIN_HOSTNAME = "login.hostname";
    /** Database location for an H2 DB. */
    public static String LOGIN_DATABASE_H2= "login.database.h2";
    
}
