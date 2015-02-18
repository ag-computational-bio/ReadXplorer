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

import de.cebitec.readxplorer.utils.PasswordStore;
import de.cebitec.readxplorer.utils.Properties;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * Calls Gnu R.
 *
 * @author kstaderm
 */
public class GnuR extends RConnection {

    /**
     * The Cran Mirror used to receive additional packages.
     */
    private String cranMirror;

    public final boolean runningLocal;

    /*  Is there already a instance running that we can connect to?
     *   This is only interesting for manual setup on Unix hosts.
     */
    private static int connectableInstanceRunning = 0;

    private static final SecureRandom random = new SecureRandom();

    private static final Logger LOG = Logger.getLogger( GnuR.class.getName() );

    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    private GnuR(String host, int port, boolean runningLocal) throws RserveException {
        super(host, port);
        this.runningLocal = runningLocal;
    }

    /**
     * Clears up the memory of an runnig R instance. This should the called
     * every time before a new computation is startet. Doing so you can be sure
     * that no previous result is interfering with the new computation.
     */
    public void clearGnuR() throws RserveException {
        this.eval("rm(list = ls(all = TRUE))");
    }

    /**
     * Saves the memory of the current R instance to the given file.
     *
     * @param saveFile File the memory image should be saved to
     */
    public void saveDataToFile(File saveFile) throws RserveException {
        String path = saveFile.getAbsolutePath();
        path = path.replace("\\", "/");
        this.eval("save.image(\"" + path + "\")");
    }

    private void setDefaultCranMirror() throws RserveException {
        cranMirror = NbPreferences.forModule(Object.class).get(Properties.CRAN_MIRROR, "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/");
        this.eval("{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}");
    }

    /**
     * Loads the specified Gnu R package. If not installed the method will try
     * to download and install the package.
     *
     * @param packageName
     */
    public void loadPackage(String packageName) throws PackageNotLoadableException {
        try {
            this.eval("library(\"" + packageName + "\")");
        } catch (RserveException ex) {
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            LOG.log(Level.WARNING, "{0}: Package {1} is not installed.", new Object[]{currentTimestamp, packageName});
            try {
                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                LOG.log(Level.INFO, "{0}: Trying to install package {1}.", new Object[]{currentTimestamp, packageName});
                this.eval("install.packages(\"" + packageName + "\")");
                this.eval("library(" + packageName + ')');
            } catch (RserveException ex1) {
                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                LOG.log(Level.SEVERE, "{0}: Could not install package {1}. Please install it manually and try again.", new Object[]{currentTimestamp, packageName});
                throw new PackageNotLoadableException(packageName);
            }
        }
    }

    public static class PackageNotLoadableException extends Exception {

        public PackageNotLoadableException(String packageName) {
            super("The Gnu R package " + packageName + " can't be loaded automatically. Please install it manually!");
        }

    }

    public static class UnknownGnuRException extends Exception {

        public UnknownGnuRException(Exception e) {
            super("An unknown exception occurred in GNU R while processing your data. "
                    + "This caused an " + e.getClass().getName() + " on the Java side of the programm.", e);
        }

    }

    @Override
    public void shutdown() throws RserveException {
        //If we started the RServe instace by our self we should also terminate it.
        //If we are connected to a remote server however we should not do so.
        if (runningLocal) {
            super.shutdown();
            if (connectableInstanceRunning > 0) {
                connectableInstanceRunning--;
            }
        }
    }

    @Override
    public REXP eval(String cmd) throws RserveException {
        ProcessingLog.getInstance().logGNURinput(cmd);
        return super.eval(cmd);
    }

    @Override
    public REXP eval(REXP what, REXP where, boolean resolve) throws REngineException {

        return super.eval(what, where, resolve);
    }

    @Override
    public void assign(String sym, REXP rexp) throws RserveException {
        super.assign(sym, rexp);
    }

    @Override
    public void assign(String sym, String ct) throws RserveException {
        super.assign(sym, ct);
    }

    @Override
    public void assign(String symbol, REXP value, REXP env) throws REngineException {
        super.assign(symbol, value, env);
    }

    /**
     * Store an SVG file of a given plot using this GnuR instance.
     * <p>
     * @param file File to store the plot in
     * @param plotIdentifier String identifying the data to plot
     * <p>
     * @throws
     * de.cebitec.readxplorer.differentialExpression.GnuR.PackageNotLoadableException
     * @throws IllegalStateException
     */
    public void storePlot(File file, String plotIdentifier) throws PackageNotLoadableException, IllegalStateException,
            RserveException, REngineException, REXPMismatchException,
            FileNotFoundException, IOException {
        this.loadPackage("grDevices");

        if (runningLocal) {
            String path = file.getAbsolutePath();
            path = path.replace("\\", "\\\\");
            this.eval("svg(filename=\"" + path + "\")");
            this.eval(plotIdentifier);
            this.eval("dev.off()");
        } else {
            this.eval("tmpFile <- tempfile(pattern =\"ReadXplorer_Plot_\", tmpdir = tempdir(), fileext =\".svg\")");
            this.eval("svg(filename=tmpFile)");
            this.eval(plotIdentifier);
            this.eval("dev.off()");
            this.eval("r=readBin(tmpFile,\"raw\",30720*1024)");
            this.eval("unlink(tmpFile)");
            REXP pictureData = this.parseAndEval("r");
            byte[] asBytes = pictureData.asBytes();
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(asBytes);
            }
        }
    }

    /**
     * The next free port that will be used to start a new RServe instance. Per
     * default RServe starts on port 6311 in order to not interfear with already
     * running instances we start one port above
     */
    private static int nextFreePort = 6312;

    public static GnuR startRServe() throws RserveException {
        GnuR instance;
        String host;
        int port;
        boolean manualLocalSetup = NbPreferences.forModule(Object.class).getBoolean(Properties.RSERVE_MANUAL_LOCAL_SETUP, false);
        boolean manualRemoteSetup = NbPreferences.forModule(Object.class).getBoolean(Properties.RSERVE_MANUAL_REMOTE_SETUP, false);
        boolean useAuth = NbPreferences.forModule(Object.class).getBoolean(Properties.RSERVE_USE_AUTH, false);

        if (manualRemoteSetup) {
            port = NbPreferences.forModule(Object.class).getInt(Properties.RSERVE_PORT, 6311);
            host = NbPreferences.forModule(Object.class).get(Properties.RSERVE_HOST, "localhost");
            instance = new GnuR(host, port, !manualRemoteSetup);
            if (useAuth) {
                String user = NbPreferences.forModule(Object.class).get(Properties.RSERVE_USER, "");
                String password = new String(PasswordStore.read(Properties.RSERVE_PASSWORD));
                instance.login(user, password);
            }
        } else {
            ProcessBuilder pb;
            host = "localhost";

            if (manualLocalSetup) {
                String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
                port = NbPreferences.forModule(Object.class).getInt(Properties.RSERVE_PORT, 6311);
                if (!((os.contains("linux") || os.contains("mac")) && (connectableInstanceRunning > 0))) {
                    File startUpScript = new File(NbPreferences.forModule(Object.class).get(Properties.RSERVE_STARTUP_SCRIPT, ""));
                    List<String> commands = new ArrayList<>();
                    commands.add("/bin/bash");
                    commands.add(startUpScript.getAbsolutePath());
                    commands.add(String.valueOf(port));
                    pb = new ProcessBuilder(commands);
                    pb.directory(startUpScript.getParentFile());
                    try {
                        final Process start = pb.start();
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    BufferedReader reader
                                            = new BufferedReader(new InputStreamReader(start.getInputStream()));
                                    String line = null;
                                    while ((line = reader.readLine()) != null) {
                                        ProcessingLog.getInstance().logGNURoutput(line);
                                    }
                                } catch (IOException ex) {
                                    Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                                    LOG.log(Level.SEVERE, "{0}: Could not create InputStream reader for RServe process.", currentTimestamp);
                                }
                            }
                        }).start();

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    BufferedReader reader
                                            = new BufferedReader(new InputStreamReader(start.getErrorStream()));
                                    String line = null;
                                    while ((line = reader.readLine()) != null) {
                                        ProcessingLog.getInstance().logGNURoutput(line);
                                    }
                                } catch (IOException ex) {
                                    Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                                    LOG.log(Level.SEVERE, "{0}: Could not create ErrorStream reader for RServe process.", currentTimestamp);
                                }
                            }
                        }).start();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    //Give the Process a moment to start up everything.
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    connectableInstanceRunning++;
                }
                instance = new GnuR(host, port, !manualRemoteSetup);
                if (useAuth) {
                    String user = NbPreferences.forModule(Object.class).get(Properties.RSERVE_USER, "");
                    String password = new String(PasswordStore.read(Properties.RSERVE_PASSWORD));
                    instance.login(user, password);
                }
            } else {
                port = nextFreePort++;
                String bit = System.getProperty("sun.arch.data.model");
                String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
                File user_dir = Places.getUserDirectory();
                File r_dir = new File(user_dir.getAbsolutePath() + File.separator + "R");
                String password = nextSessionId();
                String user = "readxplorer";
                writePasswordFile(user, password, r_dir);
                if (os.contains("windows")) {
                    String startupBat = r_dir.getAbsolutePath() + File.separator + "bin" + File.separator + "startup.bat";
                    File workdir = new File(r_dir.getAbsolutePath() + File.separator + "bin");
                    String arch = "";
                    if (bit.equals("32")) {
                        arch = "i386";
                    }
                    if (bit.equals("64")) {
                        arch = "x64";
                    }
                    List<String> commands = new ArrayList<>();
                    commands.add(startupBat);
                    commands.add(r_dir.getAbsolutePath());
                    commands.add(arch);
                    commands.add(String.valueOf(port));
                    pb = new ProcessBuilder(commands);
                    pb.directory(workdir);
                    try {
                        pb.start();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                //Give the Process a moment to start up everything.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                instance = new GnuR(host, port, !manualRemoteSetup);
                instance.login(user, password);
                instance.setDefaultCranMirror();
            }
        }
        return instance;
    }

    public static boolean gnuRSetupCorrect() {
        boolean manualLocalSetup = NbPreferences.forModule(Object.class).getBoolean(Properties.RSERVE_MANUAL_LOCAL_SETUP, false);
        boolean manualRemoteSetup = NbPreferences.forModule(Object.class).getBoolean(Properties.RSERVE_MANUAL_REMOTE_SETUP, false);

        if (!(manualLocalSetup || manualRemoteSetup)) {
            File user_dir = Places.getUserDirectory();
            File r_dir = new File(user_dir.getAbsolutePath() + File.separator + "R");
            String startupBat = r_dir.getAbsolutePath() + File.separator + "bin" + File.separator + "startup.bat";
            File batFile = new File(startupBat);
            return (batFile.exists() && batFile.canExecute());
        } else {
            return true;
        }
    }

    private static void writePasswordFile(String user, String password, File r_dir) {

        File out = new File(r_dir.getAbsolutePath() + File.separator + "bin" + File.separator + "passwd");
        out.deleteOnExit();
        try (BufferedWriter writer = Files.newBufferedWriter(out.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(user);
            writer.write(" ");
            writer.write(password);
            writer.write("\n");
            writer.close();
        } catch (IOException ex) {
        }
    }

    private static String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
}
