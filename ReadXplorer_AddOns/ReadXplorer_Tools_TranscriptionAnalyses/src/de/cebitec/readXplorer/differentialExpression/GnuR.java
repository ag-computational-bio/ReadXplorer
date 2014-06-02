/* 
 * Copyright (C) 2014 Kai Bernd Stadermann
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
package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.util.Properties;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbPreferences;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 * Calls Gnu R.
 *
 * @author kstaderm
 */
public class GnuR extends Rengine {

    /**
     * The current instance of Gnu R. There can only be one instance.
     */
    private static GnuR instance = null;
    /**
     * The Cran Mirror used to receive additional packages.
     */
    private String cranMirror;
    /**
     * Keeps track over the one and only allowed instance of this class. There
     * can only be one instance that should only be used by one other class at a
     * time. If not you might get strange results because two classes are
     * working with one Gnu R instance an the same time sharing memory and all
     * variables. Side effects are likely to accure in this case. If more than
     * one Gnu R instance is created, the Java VM will crash. This key is null
     * if the class is free.
     */
    private static UUID KEY = null;

    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    private GnuR(String[] args) {
        super(args, false, null);
        super.addMainLoopCallbacks(new Callback());
        setDefaultCranMirror();
    }

    private static synchronized GnuR getInstance(UUID key) throws IllegalStateException {
        if (key == KEY) {
            if (instance == null) {
                String[] args = new String[]{"--vanilla", "--slave"};
                instance = new GnuR(args);
            }
            return instance;
        } else {
            throw new IllegalStateException("The instance of Gnu R is currently reserved by another instance.");
        }
    }

    private static synchronized UUID reserveInstance() {
        if (KEY == null) {
            KEY = UUID.randomUUID();
            return KEY;
        } else {
            throw new IllegalStateException("The instance of Gnu R is currently used.");
        }
    }

    public void releaseGnuRInstance(UUID key) {
        if (key == KEY) {
            this.clearGnuR();
            KEY = null;
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Current Gnu R instace was released.", currentTimestamp);
        } else {
            throw new IllegalStateException("The instance of Gnu R is currently reserved by another instance.");
        }
    }

    /**
     * Clears up the memory of an runnig R instance. This should the called
     * every time before a new computation is startet. Doing so you can be sure
     * that no previous result is interfering with the new computation.
     */
    public void clearGnuR() {
        this.eval("rm(list = ls(all = TRUE))");
    }

    /**
     * Saves the memory of the current R instance to the given file.
     *
     * @param saveFile File the memory image should be saved to
     */
    public void saveDataToFile(File saveFile) {
        String path = saveFile.getAbsolutePath();
        path = path.replace("\\", "/");
        this.eval("save.image(\"" + path + "\")");
    }

    private void setDefaultCranMirror() {
        cranMirror = NbPreferences.forModule(Object.class).get(Properties.CRAN_MIRROR, "ftp://ftp.cebitec.uni-bielefeld.de/pub/readXplorer_repo/R/");
        this.eval("{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}");
    }

    /**
     * Loads the specified Gnu R package. If not installed the method will try
     * to download and install the package.
     *
     * @param packageName
     */
    public void loadPackage(String packageName) throws PackageNotLoadableException {
        REXP result = this.eval("library(" + packageName + ")");
        if (result == null) {
            this.eval("install.packages(\"" + packageName + "\")");
            result = this.eval("library(" + packageName + ")");
            if (result == null) {
                throw new PackageNotLoadableException(packageName);
            }
        }
    }

    public static class PackageNotLoadableException extends Exception {

        public PackageNotLoadableException(String packageName) {
            super("The Gnu R package " + packageName + " can't be loaded automatically. Please install it manually!");
        }
    }

    public static class JRILibraryNotInPathException extends Exception {

        public JRILibraryNotInPathException() {
            super("JRI native library can't be found in the PATH. Please add it to the PATH and try again.");
        }
    }

    public static class UnknownGnuRException extends Exception {

        public UnknownGnuRException(Exception e) {
            super("An unknown exception occurred in GNU R while processing your data. "
                    + "This caused an " + e.getClass().getName() + " on the Java side of the programm.", e);
        }
    }

    @Override
    public synchronized REXP eval(String string) {
        return eval(string, true);
    }

    @Override
    public synchronized REXP eval(String string, boolean bln) {
        ProcessingLog.getInstance().logGNURoutput("> " + string + "\n");
        return super.eval(string, bln);
    }

    @Override
    public boolean assign(String string, String string1) {
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + string1 + "\" to variable \"" + string + "\"\n");
        return super.assign(string, string1);
    }

    @Override
    public boolean assign(String string, REXP rexp) {
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + rexp.asString() + "\" to variable \"" + string + "\"\n");
        return super.assign(string, rexp);
    }

    @Override
    public boolean assign(String string, double[] doubles) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < doubles.length; i++) {
            sb.append(doubles[i]).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n");
        return super.assign(string, doubles);
    }

    @Override
    public boolean assign(String string, int[] ints) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ints.length; i++) {
            sb.append(ints[i]).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n");
        return super.assign(string, ints);
    }

    @Override
    public boolean assign(String string, boolean[] blns) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < blns.length; i++) {
            sb.append(blns[i]).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n");
        return super.assign(string, blns);
    }

    @Override
    public boolean assign(String string, String[] strings) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        ProcessingLog.getInstance().logGNURoutput("> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n");
        return super.assign(string, strings);
    }

    private static class Callback implements RMainLoopCallbacks {

        @Override
        public void rWriteConsole(Rengine rngn, String string, int i) {
            ProcessingLog.getInstance().logGNURoutput(string);
        }

        @Override
        public void rBusy(Rengine rngn, int i) {
        }

        @Override
        public String rReadConsole(Rengine rngn, String string, int i) {
            return "";
        }

        @Override
        public void rShowMessage(Rengine rngn, String string) {
            ProcessingLog.getInstance().logGNURoutput(string);
        }

        @Override
        public String rChooseFile(Rengine rngn, int i) {
            return "";
        }

        @Override
        public void rFlushConsole(Rengine rngn) {
        }

        @Override
        public void rSaveHistory(Rengine rngn, String string) {
        }

        @Override
        public void rLoadHistory(Rengine rngn, String string) {
        }
    }

    public static class SecureGnuRInitiliser {

        /**
         * Reserves the GNU R for later usage.
         *
         * @return The key needed to get the actual instance.
         * @throws IllegalStateException if GNU R is already used.
         */
        public static synchronized UUID reserveGnuRinstance() throws IllegalStateException {
            UUID ret = reserveInstance();
            return ret;
        }

        /**
         * Returns the one and only instance of GNU R. reserveGnuRinstance() has
         * to be called first in order to acquire the key.
         *
         * @param key The UUID acquired by calling reserveGnuRinstance()
         * @return The one and only instance of GNU R.
         * @throws IllegalStateException if the key is not correct.
         */
        public static synchronized GnuR getGnuRinstance(UUID key) throws IllegalStateException {
            GnuR ret = getInstance(key);
            return ret;
        }

        /**
         * Checks if R is installed.
         *
         * @return true if the needed libraries are included, else false.
         */
        public static boolean isGnuRSetUpCorrect() {
            System.setProperty("jri.ignore.ule", "yes");
            isGnuRInstanceFree();
            return Rengine.jriLoaded;
        }

        /**
         * Checks if the GNU R instance is available. This is just an informal
         * check that will not reserve the instance if it is available. You have
         * to call reserveGnuRinstance() later if you want to do so. Note that
         * it might be possible that even if you call reserveGnuRinstance()
         * directly after calling this method the GNU R instance is reserved in
         * between by another thread.
         *
         * @return true if the GNU R instance is available, else false.
         */
        public static boolean isGnuRInstanceFree() {
            if (KEY == null) {
                return true;
            } else {
                return false;
            }
        }
    }
}
