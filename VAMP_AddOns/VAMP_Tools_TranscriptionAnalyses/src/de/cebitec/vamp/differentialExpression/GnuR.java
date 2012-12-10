package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.util.Properties;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;
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
     * Keeps track over the requested number of instances and allows only one.
     * There can only be one instance which should only be used by one other
     * class at a time. If not you might get strange results because two classes
     * are working with one Gnu R instance an the same time sharing memory and
     * all variables. Side effects are likely to accure in this case.
     */
    private static Semaphore sem = new Semaphore(1, true);

    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    private GnuR(String[] args) {
        super(args, false, null);
        super.addMainLoopCallbacks(new Callback());
        setDefaultCranMirror();
    }

    private static synchronized GnuR getInstance() throws IllegalStateException {
        if (sem.tryAcquire()) {
            if (instance == null) {
                String[] args = new String[]{"--vanilla", "--quite", "--slave"};
                instance = new GnuR(args);
            }
            return instance;
        } else {
            throw new IllegalStateException("The instance of Gnu R is currently used");
        }
    }

    public void releaseGnuRInstance() {
        this.clearGnuR();
        sem.release();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Current Gnu R instace was released.", currentTimestamp);
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
     * Used by all the plotting methods to set up Gnu R to create SVGs.
     */
    public void setUpSvgOutput() {
        REXP svg = this.eval("library(RSvgDevice)");
        if (svg == null) {
            this.eval("install.packages(\"RSvgDevice\")");
            this.eval("library(RSvgDevice)");
        }
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
        cranMirror = NbPreferences.forModule(Object.class).get(Properties.CRAN_MIRROR, "http://cran.mirrors.hoobly.com/");
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
            this.eval("source(\"http://bioconductor.org/biocLite.R\")");
            this.eval("biocLite(pkgs=\"" + packageName + "\",ask=\"graphics\")");
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
        public UnknownGnuRException(Exception e){
            super("An unknown exception occurred in GNU R while processing your data. "
                    + "This caused an "+e.getClass().getName()+" on the Java side of the programm.", e);
        }
    }

    private static class Callback implements RMainLoopCallbacks {

        @Override
        public void rWriteConsole(Rengine rngn, String string, int i) {
            System.out.println(string);
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

        public static GnuR getGnuRinstance() throws JRILibraryNotInPathException, IllegalStateException {
            if (!isGnuRSetUpCorrect()) {
                throw new JRILibraryNotInPathException();
            }
            GnuR ret = getInstance();
            return ret;
        }

        public static boolean isGnuRSetUpCorrect() {
            String libraryPath = System.getProperty("java.library.path");
            return libraryPath.contains("jri");
        }
    }
}
