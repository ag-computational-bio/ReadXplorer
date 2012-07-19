package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.util.Properties;
import org.openide.util.NbPreferences;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 * Calls Gnu R.
 *
 * @author kstaderm
 */
public class GnuR implements RMainLoopCallbacks {

    private Rengine engine;
    /**
     * The Cran Mirror used to receive additional packages.
     */
    private String cranMirror;

    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    public GnuR() {
        cranMirror = NbPreferences.forModule(Object.class).get(Properties.CRAN_MIRROR, "http://cran.mirrors.hoobly.com/");
    }

    /**
     * Creates a new Gnu R process.
     */
    public void startUp() {
        String[] args = new String[0];
        engine = new Rengine(args, false, this);
    }

    /**
     * Shuts down the Gnu R process.
     */
    public void shutdown() {
        engine.end();
    }

    /**
     * Clears up the memory of an runnig R instance. This should the called
     * every time before a new computation is startet. Doing so you can be sure
     * that no previous result is interfering with the new computation.
     */
    public void clearGnuR() {
        engine.eval("rm(list = ls(all = TRUE))");
    }

    public String getCranMirror() {
        return cranMirror;
    }

    public Rengine getEngine() {
        return engine;
    }

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
