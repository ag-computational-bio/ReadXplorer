/*
 * Copyright (C) 2016 Patrick Blumenkamp<patrick.blumenkamp@computational.bio.uni-giessen.de>
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

import de.cebitec.readxplorer.api.constants.RServe;
import de.cebitec.readxplorer.utils.OsUtils;
import de.cebitec.readxplorer.utils.PasswordStore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Patrick
 * Blumenkamp<patrick.blumenkamp@computational.bio.uni-giessen.de>
 */
public final class GnuRAccess {

    /**
     * The next free port that will be used to start a new RServe instance. Per
     * default RServe starts on port 6311 in order to not interfere with already
     * running instances we start one port above
     */
    private static int nextFreePort = 6312;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Logger LOG = LoggerFactory.getLogger( GnuRAccess.class.getName() );


    private GnuRAccess() {
    }


    public static GnuR startRServe( ProcessingLog processingLog ) throws RserveException, IOException {
        GnuR instance;
        String host;
        int port;
        boolean useStartupScript = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_USE_STARTUP_SCRIPT_SETUP, false );
        boolean remoteSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_USE_REMOTE_SETUP, false );
        boolean useAuthStartupScript = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_STARTUP_SCRIPT_USE_AUTH, false );
        boolean useAuthRemote = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_REMOTE_USE_AUTH, false );
//        File cebitecIndicator = new File( "/vol/readxplorer/R/CeBiTecMode" );
//        if( cebitecIndicator.exists() ) {
//            return accessCebitecRserve( cebitecIndicator, processingLog );
//        } else 
        if( remoteSetup || OsUtils.isMac() ) {
            port = NbPreferences.forModule( Object.class ).getInt( RServe.RSERVE_REMOTE_PORT, 6311 );
            host = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_REMOTE_HOST, "localhost" );
            instance = accessRemoteRserve( host, port, processingLog );
            if( useAuthRemote ) {
                String user = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_REMOTE_USER, "" );
                String password = new String( PasswordStore.read( RServe.RSERVE_REMOTE_PASSWORD ) );
                instance.login( user, password );
            }
        } else {
            instance = accessLocalRserve( useStartupScript, processingLog );
            if( useStartupScript && useAuthStartupScript ) {
                String user = NbPreferences.forModule( Object.class ).get( RServe.RSERVE_STARTUP_SCRIPT_USER, "" );
                String password = new String( PasswordStore.read( RServe.RSERVE_STARTUP_SCRIPT_PASSWORD ) );
                instance.login( user, password );
            }

        }
        return instance;
    }


//    private static GnuR accessCebitecRserve( File cebitecIndicator, ProcessingLog processingLog ) throws FileNotFoundException, IOException, RserveException {
//        String ip;
//        try( FileReader fr = new FileReader( cebitecIndicator ); BufferedReader br = new BufferedReader( fr ) ) {
//            ip = br.readLine();
//        }
//        GnuR instance = new GnuR( ip, 6311, false, processingLog );
//        instance.login( "readxplorer", "DEfq984Fue3Xor81905jft249" );
//        return instance;
//    }
    private static GnuR accessRemoteRserve( String host, int port, ProcessingLog processingLog ) throws FileNotFoundException, RserveException {
        return new GnuR( host, port, false, processingLog );
    }


    private static GnuR accessLocalRserve( boolean useStartupScript, ProcessingLog processingLog ) throws FileNotFoundException, IOException, RserveException {
        ProcessBuilder pb;
        final Process rserveProcess;
        String host = "localhost";

        if( useStartupScript ) {
            int port = NbPreferences.forModule( Object.class ).getInt( RServe.RSERVE_STARTUP_SCRIPT_PORT, 6311 );
            if( !(OsUtils.isLinux() && GnuR.isLocalMachineRunning()) ) {
                if( NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_STARTUP_SCRIPT_USE_DEFAULT_SCRIPT, false ) ) {
                    rserveProcess = launchDefaultStartUpScript( port, processingLog );
                } else {
                    File startUpScript = new File( NbPreferences.forModule( Object.class ).get( RServe.RSERVE_STARTUP_SCRIPT_PATH, "" ) );
                    rserveProcess = launchStartUpScript( startUpScript, port, processingLog );
                }
                if( rserveProcess == null || (rserveProcess.exitValue() != 0) ) {
                    throw new IOException( "Could not start Rserve instance!" );
                } else {
                    GnuR.setLocalMachineRunning( true );
                }
            }

            if( GnuR.isLocalMachineRunning() ) {
                return new GnuR( host, port, true, processingLog );
            } else {
                throw new IOException( "Could not log into running RServe instance!" );
            }
        } else {
            int port = nextFreePort++;
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
            if( rserveProcess == null || !rserveProcess.isAlive() ) {
                return new GnuR( host, port, true, processingLog );
            } else {
                throw new IOException( "Could not start Rserve instance!" );
            }
        }
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
        final Process rserveProcess = pb.start();
        new Thread( () -> {
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
        } ).start();
        new Thread( () -> {
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


    private static Process launchDefaultStartUpScript( int port, ProcessingLog processingLog1 ) throws IOException {
//        ProcessBuilder pb;
//        List<String> commands = new ArrayList<>();
//        commands.add( "which R" );
//        commands.add( "R CMD Rserve --RS-port " + port + " --vanilla" );
//        commands.add( String.valueOf( port ) );
//        commands.add( "--vanilla" );
        ProcessBuilder pb = new ProcessBuilder( "R", "CMD", "Rserve", "--RS-port", String.valueOf(port), "--vanilla" );
        final Process rserveProcess = pb.start();
        new Thread( () -> {
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
        } ).start();
        new Thread( () -> {
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
        boolean startupScriptSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_USE_STARTUP_SCRIPT_SETUP, false );
        boolean remoteSetup = NbPreferences.forModule( Object.class ).getBoolean( RServe.RSERVE_USE_REMOTE_SETUP, false );

        if( !(remoteSetup || startupScriptSetup) ) {
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
