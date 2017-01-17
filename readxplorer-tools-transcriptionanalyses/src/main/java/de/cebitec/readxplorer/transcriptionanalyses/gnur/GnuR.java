/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.gnur;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Calls Gnu R.
 * <p>
 * @author kstaderm
 */
public final class GnuR extends RConnection {

    private final boolean runningLocal;

    /* Is there already a instance running that we can connect to? This is only
     * interesting for manual setup on Unix hosts.
     */
    private static boolean isConnectableInstanceRunning = false;

    private static int numberOfLocalActiveConnections = 0;

    private static final Logger LOG = LoggerFactory.getLogger( GnuR.class.getName() );

    private final ProcessingLog processingLog;


    public static boolean isLocalMachineRunning() {
        return isConnectableInstanceRunning;
    }
    
    public static void setLocalMachineRunning(boolean isLocalMachineRunning){
        isConnectableInstanceRunning = isLocalMachineRunning;
    }


    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    public GnuR( String host, int port, boolean runningLocal, ProcessingLog processingLog ) throws RserveException {
        super( host, port );
        this.runningLocal = runningLocal;
        if( runningLocal ) {
            numberOfLocalActiveConnections++;
            GnuR.isConnectableInstanceRunning = true;
        }
        this.processingLog = processingLog;
    }


    /**
     * Returns boolean to show if Rserve runs on local machine
     *
     * @return True if Rserve runs local, else false
     */
    public boolean runsLocal() {
        return runningLocal;
    }


    /**
     * Clears up the memory of an running R instance. This should the called
     * every time before a new computation is started. Doing so you can be sure
     * that no previous result is interfering with the new computation.
     */
    public void clearGnuR() throws RserveException {
        this.eval( "rm(list = ls(all = TRUE))" );
    }


    /**
     * Saves the memory of the current R instance to the given file.
     * <p>
     * @param saveFile File the memory image should be saved to
     */
    public void saveDataToFile( File saveFile ) throws RserveException, REXPMismatchException, IOException {

        if( runningLocal ) {
            String path = saveFile.getAbsolutePath();
            path = path.replace( "\\", "/" );
            this.eval( "save.image(\"" + path + "\")" );
        } else {
            byte[] b = new byte[8192];
            this.eval( "tmpFile <- tempfile(pattern =\"ReadXplorer_data_\", tmpdir = tempdir(), fileext =\".rdata\")" );
            this.eval( "save.image(tmpFile)" );
            String tmpFilePath = this.eval( "tmpFile" ).asString();
            RFileInputStream input;
            try( BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream( saveFile ) ) ) {
                input = this.openFile( tmpFilePath );
                int c = input.read( b );
                while( c >= 0 ) {
                    output.write( b, 0, c );
                    c = input.read( b );
                }
            }
            input.close();
            this.eval( "unlink(tmpFile)" );
        }
    }


//    private void setDefaultCranMirror() throws RserveException {
//        cranMirror = NbPreferences.forModule( Object.class ).get( Paths.CRAN_MIRROR, "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/" );
//        this.eval( "{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}" );
//    }
    /**
     * Loads the specified Gnu R package.
     * <p>
     * @throws PackageNotLoadableException Gnu R package not installed or
     *                                     package could not be loaded.
     * @param packageName
     */
    public void loadPackage( String packageName ) throws PackageNotLoadableException {
        String cmd = "";
        try {
            if( checkPackage( packageName ) ) {
                cmd = "library(" + packageName + ")";
                this.eval( cmd );
            } else {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( "{0}: Package {1} is not installed. Please install it manually and try again.", new Object[]{ currentTimestamp, packageName } );
                throw new PackageNotLoadableException( packageName );
            }
        } catch( RserveException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.error( "{0}: Evaluation of \"{1}\" failed.", new Object[]{ currentTimestamp, cmd } );
            throw new PackageNotLoadableException( packageName );
        } catch( REXPMismatchException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.error( "{0}: Internal error with REXP object in GnuR.loadPackage(\"{1}\").", new Object[]{ currentTimestamp, packageName } );
            throw new PackageNotLoadableException( packageName );
        }
    }


    /**
     * Checks the version of a specified Gnu R package.
     * <p>
     * @throws REXPMismatchException
     * @throws RserveException
     * @param packageName
     *
     * @return Version of the installed package
     */
    public Optional<Version> getPackageVersion( String packageName ) throws REXPMismatchException, RserveException {
        REXP packageInstalled = this.eval( "\"" + packageName + "\" %in% rownames(installed.packages())" );
        if( packageInstalled.asInteger() == 1 ) {
            REXP versionREXP = this.eval( "as.character(packageVersion(\"" + packageName + "\"))" );
            return Optional.of( new Version( versionREXP.asString() ) );
        } else {
            return Optional.empty();
        }
    }


    /**
     * Checks if the specified Gnu R package is installed with at least the
     * package version in pkg.
     * <p>
     * @throws REXPMismatchException
     * @throws RserveException
     * @param pkg Package with or without version
     *
     * @return True if pkg is installed and at least the same version as the
     *         installed one, else false
     */
    public boolean checkPackage( RPackageDependency pkg ) throws REXPMismatchException, RserveException {
        Optional<Version> installedVersion = getPackageVersion( pkg.getName() );
        return installedVersion.filter( version -> pkg.getVersion().compareTo( version ) < 0 )
                .isPresent();
    }


    /**
     * Checks if the specified Gnu R package is installed.
     * <p>
     * @throws REXPMismatchException
     * @throws RserveException
     * @param pkg Name of R package
     *
     * @return True if pkg is installed, else false
     */
    public boolean checkPackage( String pkg ) throws REXPMismatchException, RserveException {
        Optional<Version> installedVersion = getPackageVersion( pkg );
        return installedVersion.isPresent();
    }


    @Override
    public synchronized void shutdown() throws RserveException {
        //If we started the RServe instace by our self we should also terminate it.
        //If we are connected to a remote server however we should not do so and 
        //just close the connection.
        if( runningLocal ) {
            numberOfLocalActiveConnections--;
            if( numberOfLocalActiveConnections < 1 ) {
                processingLog.logGNURoutput( "Close connection and shutdown Rserve instance." );
                isConnectableInstanceRunning = false;
                super.shutdown();
                return;
            }
        }
        processingLog.logGNURoutput( "Close connection." );
        super.close();
    }


    @Override
    public synchronized boolean close() {
        if( runningLocal ) {
            numberOfLocalActiveConnections--;
        }
        processingLog.logGNURoutput( "Close connection." );
        return super.close();
    }


    @Override
    public REXP eval( String cmd ) throws RserveException {
        processingLog.logGNURinput( cmd );
        return super.eval( cmd );
    }


    /**
     * Not usable now! RConnector.eval(REXP what, REXP where, boolean resolve)
     * always returns REXPNull.
     *
     * @param what
     * @param where
     * @param resolve
     *
     * @return
     *
     * @throws REngineException
     */
    @Override
    public REXP eval( REXP what, REXP where, boolean resolve ) throws REngineException {
        return super.eval( what, where, resolve );
    }


    @Override
    public void assign( String sym, REXP rexp ) throws RserveException {
        processingLog.logGNURinput( "***Assign to " + sym + ": " + rexp.toDebugString() + "***" );
        super.assign( sym, rexp );
    }


    //Always assigns ct as String!
    @Override
    public void assign( String sym, String ct ) throws RserveException {
        processingLog.logGNURinput( sym + " <- \"" + ct + "\"" );
        super.assign( sym, ct );
    }


    /**
     * Warning! Rserve does not support environments other than .GlobalEnv. Only
     * valid value for env is null.
     *
     * @param symbol
     * @param value
     * @param env    Must be null for not throwing an exception.
     *
     * @throws REngineException
     */
    @Override
    public void assign( String symbol, REXP value, REXP env ) throws REngineException {
        super.assign( symbol, value, env );
    }


    /**
     * Store an SVG file of a given plot using this GnuR instance.
     * <p>
     * @param file           File to store the plot in
     * @param plotIdentifier String identifying the data to plot
     * <p>
     * @throws
     * de.cebitec.readxplorer.differentialExpression.gnur.PackageNotLoadableException
     * @throws IllegalStateException
     */
    public void storePlot( File file, String plotIdentifier ) throws PackageNotLoadableException, IllegalStateException,
                                                                     RserveException, REngineException, REXPMismatchException,
                                                                     FileNotFoundException, IOException {
        this.loadPackage( "grDevices" );

        if( runningLocal ) {
            String path = file.getAbsolutePath();
            path = path.replace( "\\", "\\\\" );
            this.eval( "svg(filename=\"" + path + "\")" );
            this.eval( plotIdentifier );
            this.eval( "dev.off()" );
        } else {
            this.eval( "tmpFile <- tempfile(pattern =\"ReadXplorer_Plot_\", tmpdir = tempdir(), fileext =\".svg\")" );
            this.eval( "svg(filename=tmpFile)" );
            this.eval( plotIdentifier );
            this.eval( "dev.off()" );
            this.eval( "r=readBin(tmpFile,\"raw\",30720*1024)" );
            this.eval( "unlink(tmpFile)" );
            REXP pictureData = this.parseAndEval( "r" );
            byte[] asBytes = pictureData.asBytes();
            try( OutputStream os = new FileOutputStream( file ) ) {
                os.write( asBytes );
            }
        }
    }


}
