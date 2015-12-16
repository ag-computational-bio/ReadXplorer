/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.utils.errorhandling;

import de.cebitec.readxplorer.api.ErrorHandler;
import java.io.PrintStream;
import org.openide.LifecycleManager;


/**
 * Designed for handling exceptions and logging on the console.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ConsoleErrorHandler implements ErrorHandler {

    private PrintStream ps = null;


    /**
     * Designed for handling exceptions and logging on the console.
     */
    public ConsoleErrorHandler() {
    }


    /**
     * @param ps The PrintStream to print all exception messages to
     */
    public void setPrintStream( PrintStream ps ) {
        this.ps = ps;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void handle( Throwable t ) {
        if( t != null ) {
            handle( t, t.getClass().getName() );
        } else {
            handle( t, "" );
        }
    }


    /**
     * Appropriately handles a throwable. This includes showing the Throwable
     * message to the user. The header is useful to display additional
     * information in the line above the Throwable message. By default, the
     * Throwable type is printed in that line.<br>
     * This implementation also closes the Netbeans platform, because the
     * command line version cannot continue or recover from severe errors.
     * Therefore, the method should only be used for severe errors. The only
     * exception is to use this method with a <code>null</code> throwable. In
     * this case, the software is allowed to continue.
     *
     * @param t      Throwable to handle
     * @param header Header for displaying different information in the line
     *               above the Throwable message than the default.
     */
    @Override
    public void handle( Throwable t, String header ) {
        String msg = t != null && t.getMessage() != null ? t.getMessage() : "";
        if( ps != null ) {
            ps.println( header );
            ps.println( msg );
        } else {
            System.out.println( header );
            System.out.println( msg );
        }
        if( t != null ) {
            //exit the whole netbeans platform - the command line version cannot continue or recover from severe errors
            LifecycleManager.getDefault().exit( 1 );
        }
    }


}
