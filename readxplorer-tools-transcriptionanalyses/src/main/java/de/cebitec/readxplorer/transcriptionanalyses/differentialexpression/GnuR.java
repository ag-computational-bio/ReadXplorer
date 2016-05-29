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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


import de.cebitec.readxplorer.api.constants.RServe;
import de.cebitec.readxplorer.utils.OsUtils;
import de.cebitec.readxplorer.utils.PasswordStore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Calls Gnu R.
 * <p>
 * @author kstaderm
 */
public final class GnuR extends RConnection {

    final boolean runningLocal;

    /* Is there already a instance running that we can connect to? This is only
     * interesting for manual setup on Unix hosts.
     */
    private static boolean isConnectableInstanceRunning = false;
    
    private static int numberOfActiveConnections = 0; 

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Logger LOG = LoggerFactory.getLogger( GnuR.class.getName() );

    private final ProcessingLog processingLog;


    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    private GnuR( String host, int port, boolean runningLocal, ProcessingLog processingLog ) throws RserveException {
        super( host, port );
        this.runningLocal = runningLocal;
        this.processingLog = processingLog;
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
    public void saveDataToFile( File saveFile ) throws RserveException {
        String path = saveFile.getAbsolutePath();
        path = path.replace( "\\", "/" );
        this.eval( "save.image(\"" + path + "\")" );
    }


//    private void setDefaultCranMirror() throws RserveException {
//        cranMirror = NbPreferences.forModule( Object.class ).get( Paths.CRAN_MIRROR, "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/" );
//        this.eval( "{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}" );
//    }
    /**
     * Loads the specified Gnu R package. If not installed the method will try
     * to download and install the package.
     * <p>
     * @param packageName
     */
    public void loadPackage( String packageName ) throws PackageNotLoadableException {
        try {
            this.eval( "library(\"" + packageName + "\")" );
        } catch( RserveException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.warn( "{0}: Package {1} is not installed. Please install it manually and try again.", new Object[]{ currentTimestamp, packageName } );
        }
    }


    public static class PackageNotLoadableException extends Exception {

        private static final long serialVersionUID = 1L;


        public PackageNotLoadableException( String packageName ) {
            super( "The Gnu R package " + packageName + " can't be loaded automatically. Please install it manually!" );
        }


    }


    public static class UnknownGnuRException extends Exception {

        private static final long serialVersionUID = 1L;


        public UnknownGnuRException( Exception e ) {
            super( "An unknown exception occurred in GNU R while processing your data. " +
                   "This caused an " + e.getClass().getName() + " on the Java side of the programm.", e );
        }


    }


    @Override
    public synchronized void shutdown() throws RserveException {
        //If we started the RServe instace by our self we should also terminate it.
        //If we are connected to a remote server however we should not do so.
        if( runningLocal ) {
            numberOfActiveConnections--;
            if(numberOfActiveConnections == 0){
                isConnectableInstanceRunning = false;
                super.shutdown();
            }
        }
    }


    @Override
    public REXP eval( String cmd ) throws RserveException {
        processingLog.logGNURinput( cmd );
        return super.eval( cmd );
    }


    @Override
    public REXP eval( REXP what, REXP where, boolean resolve ) throws REngineException {

        return super.eval( what, where, resolve );
    }


    @Override
    public void assign( String sym, REXP rexp ) throws RserveException {
        super.assign( sym, rexp );
    }


    @Override
    public void assign( String sym, String ct ) throws RserveException {
        super.assign( sym, ct );
    }


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
     * de.cebitec.readxplorer.differentialExpression.GnuR.PackageNotLoadableException
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


    /**
     * The next free port that will be used to start a new RServe instance. Per
     * default RServe starts on port 6311 in order to not interfere with already
     * running instances we start one port above
     */
    private static int nextFreePort = 6312;


    public static GnuR startRServe( ProcessingLog processingLog ) throws RserveException, IOException {
        GnuR instance;
        String host;
        int port;
        boolean useStartupScript = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_MANUAL_LOCAL_SETUP, false );
        boolean remoteSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_MANUAL_REMOTE_SETUP, false );
        boolean useAuth = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_USE_AUTH, false );
        File cebitecIndicator = new File( "/vol/readxplorer/R/CeBiTecMode" );
        if( cebitecIndicator.exists() ) {
            String ip;
            try( FileReader fr = new FileReader( cebitecIndicator ); BufferedReader br = new BufferedReader( fr ) ) {
                ip = br.readLine();
            }
            instance = new GnuR( ip, 6311, false, processingLog );
            instance.login( "readxplorer", "DEfq984Fue3Xor81905jft249" );
        } else if( remoteSetup || OsUtils.isMac() ) {
            port = NbPreferences.forModule( Object.class ).getInt( RServe.RSERVE_PORT, 6311 );
            host = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_HOST, "localhost" );
            instance = new GnuR( host, port, false, processingLog );
            if( useAuth ) {
                String user = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_USER, "" );
                String password = new String( PasswordStore.read( RServe.RSERVE_PASSWORD ) );
                instance.login( user, password );
            }
        } else {
            ProcessBuilder pb;
            final Process rserveProcess;
            host = "localhost";

            if( useStartupScript ) {
                port = NbPreferences.forModule( Object.class ).getInt( RServe.RSERVE_PORT, 6311 );
                if( !(OsUtils.isLinux() && isConnectableInstanceRunning ) ) {
                    File startUpScript = new File( NbPreferences.forModule( Object.class ).get( RServe.RSERVE_STARTUP_SCRIPT, "" ) );
                    rserveProcess = launchStartUpScript( startUpScript, port, processingLog );
                    if( rserveProcess != null && (rserveProcess.exitValue() == 0) ) {
                        isConnectableInstanceRunning = true;
                    } else {
                        throw new IOException( "Could not start Rserve instance!" );
                    }
                }

                if( isConnectableInstanceRunning ) {
                    instance = new GnuR( host, port, !remoteSetup, processingLog );
                    if( useAuth ) {
                        String user = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_USER, "" );
                        String password = new String( PasswordStore.read( RServe.RSERVE_PASSWORD ) );
                        instance.login( user, password );
                    }
                    numberOfActiveConnections++;
                } else {
                    throw new IOException( "Could not log into running RServe instance!" );
                }
            } else {
                port = nextFreePort++;
                String bit = System.getProperty( "sun.arch.data.model" );
                File userDir = Places.getUserDirectory();
                File rDir = new File( userDir.getAbsolutePath() + File.separator + "R" );
                String password = nextSessionId();
                String user = "readxplorer";
                writePasswordFile( user, password, rDir );
                if( OsUtils.isWindows() ) {
                    String startupBat = rDir.getAbsolutePath() + File.separator + "bin" + File.separator + "startup.bat";
                    File workdir = new File( rDir.getAbsolutePath() + File.separator + "bin" );
                    String arch = "";
                    if( bit.equals( "32" ) ) {
                        arch = "i386";
                    }
                    if( bit.equals( "64" ) ) {
                        arch = "x64";
                    }
                    List<String> commands = new ArrayList<>();
                    commands.add( startupBat );
                    commands.add( rDir.getAbsolutePath() );
                    commands.add( arch );
                    commands.add( String.valueOf( port ) );
                    pb = new ProcessBuilder( commands );
                    pb.directory( workdir );
                    rserveProcess = pb.start();

                    //Give the Process a moment to start up everything.
                    try {
                        Thread.sleep( 2000 );
                    } catch( InterruptedException ex ) {
                        Exceptions.printStackTrace( ex );
                    }
                } else {
                    rserveProcess = null;
                }
                if( rserveProcess != null && rserveProcess.isAlive() ) {
                    instance = new GnuR( host, port, !remoteSetup, processingLog );
                    instance.login( user, password );
//                        instance.setDefaultCranMirror();
                } else {
                    throw new IOException( "Could not start Rserve instance!" );
                }
            }
        }
        return instance;
    }


    private static Process launchStartUpScript( File startUpScript, int port, ProcessingLog processingLog1 ) throws IOException {
        ProcessBuilder pb;
        List<String> commands = new ArrayList<>();
        commands.add( "/bin/bash" );
        commands.add( startUpScript.getAbsolutePath() );
        commands.add( String.valueOf( port ) );
        pb = new ProcessBuilder( commands );
        System.out.println( startUpScript.getAbsoluteFile() );
        pb.directory( startUpScript.getAbsoluteFile().getParentFile() );
        Process rserveProcess = pb.start();
        new Thread( new Runnable() {

            @Override
            public void run() {
                try {
                    BufferedReader reader
                            = new BufferedReader( new InputStreamReader( rserveProcess.getInputStream() ) );
                    String line;
                    while( (line = reader.readLine()) != null ) {
                        processingLog1.logGNURoutput( line );
                    }
                } catch( IOException ex ) {
                    Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                    LOG.error( "{0}: Could not create InputStream reader for RServe process.", currentTimestamp );
                }
            }


        } ).start();
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader
                            = new BufferedReader( new InputStreamReader( rserveProcess.getErrorStream() ) );
                    String line;
                    while( (line = reader.readLine()) != null ) {
                        processingLog1.logGNURoutput( line );
                    }
                } catch( IOException ex ) {
                    Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                    LOG.error( "{0}: Could not create ErrorStream reader for RServe process.", currentTimestamp );
                }
            }


        } ).start();
        //Give the Process a moment to start up everything.
        try {
            rserveProcess.waitFor();
            Thread.sleep( 1000 );
        } catch( InterruptedException ex ) {
            Exceptions.printStackTrace( ex );
        }
        return rserveProcess;
    }


    public static boolean gnuRSetupCorrect() {
        File cebitecIndicator = new File( "/vol/readxplorer/R/CeBiTecMode" );
        if( cebitecIndicator.exists() ) {
            return true;
        }
        boolean manualLocalSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_MANUAL_LOCAL_SETUP, false );
        boolean manualRemoteSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_MANUAL_REMOTE_SETUP, false );

        if( !(manualLocalSetup || manualRemoteSetup) ) {
            File userDir = Places.getUserDirectory();
            File rDir = new File( userDir.getAbsolutePath() + File.separator + "R" );
            File versionIndicator = new File( rDir.getAbsolutePath() + File.separator + "rx_minimal_version_2_1" );
            return versionIndicator.exists();
        } else {
            return true;
        }
    }


    private static void writePasswordFile( String user, String password, File rDir ) {

        File out = new File( rDir.getAbsolutePath() + File.separator + "bin" + File.separator + "passwd" );
        out.deleteOnExit();
        try( BufferedWriter writer = Files.newBufferedWriter( out.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.APPEND ) ) {
            writer.write( user );
            writer.write( " " );
            writer.write( password );
            writer.write( "\n" );
            writer.close();
        } catch( IOException ex ) {
            LOG.error( "Could not write GnuR password file." );
        }
    }


    private static String nextSessionId() {
        return new BigInteger( 130, RANDOM ).toString( 32 );
    }


}
