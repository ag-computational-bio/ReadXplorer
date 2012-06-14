package de.cebitec.vamp.differentialExpression;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author kstaderm
 */
public class GnuR implements RMainLoopCallbacks {

    private Rengine gnuR;

    public GnuR() {
    }

    public void process(BaySeqAnalysisData bseqData, int numberOfAnnotations, int numberOfTracks) {
        String[] args = new String[0];
        gnuR = new Rengine(args, false, this);
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R is processing data.", currentTimestamp);
        REXP baySeq = gnuR.eval("library(baySeq)");
        if (baySeq == null) {
            gnuR.eval("source(\"http://bioconductor.org/biocLite.R\")");
            gnuR.eval("biocLite(\"baySeq\")");
            gnuR.eval("library(baySeq)");
        }
        REXP snow = gnuR.eval("library(snow)");
        if (snow == null) {
            gnuR.eval("install.packages(\"snow\")");
            gnuR.eval("library(snow)");
        }
        int i=1;
        StringBuilder concatenate = new StringBuilder("c(");
        while (bseqData.hasCountData()) {
            gnuR.assign("inputData"+i, bseqData.pollFirstCountData());
            concatenate.append("inputData").append(i++).append(",");
        }
        concatenate.deleteCharAt(concatenate.length()-1);
        concatenate.append(")");
        System.out.println(concatenate.toString());
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Gnu R running on "+processors+" processors.");
        gnuR.eval("cl <- makeCluster("+processors+", \"SOCK\")");
        gnuR.eval("inputData <- matrix("+concatenate.toString()+","+numberOfAnnotations+")");
        gnuR.assign("inputAnnotationsStart", bseqData.getStart());
        gnuR.assign("inputAnnotationsStop", bseqData.getStop());
        gnuR.eval("annotations <- data.frame(inputAnnotationsStart,inputAnnotationsStop)");
        gnuR.eval("colnames(annotations) <- c(\"start\", \"stop\")");
        gnuR.eval("seglens <- annotations$stop - annotations$start + 1");
        gnuR.eval("cD <- new(\"countData\", data = inputData, seglens = seglens, annotation = annotations)");
        gnuR.eval("cD@libsizes <- getLibsizes(cD, estimationType = \"quantile\")");
        gnuR.assign("replicates", bseqData.getReplicateStructure());
        gnuR.eval("replicates(cD) <- as.factor(c(replicates))");
        concatenate = new StringBuilder();
        i=1;
        while(bseqData.hasGroups()){
            gnuR.assign("group"+i, bseqData.getNextGroup());
            concatenate.append("group").append(i).append("=").append("group").append(i++).append(",");            
        }
        concatenate.deleteCharAt(concatenate.length()-1);
        gnuR.eval("groups(cD) <- list("+concatenate.toString()+")");
        REXP test = gnuR.eval("getwd()");
        System.out.println(test);
        gnuR.eval("save.image(\"testData.RData\")");
    }

    public void shutdown() {
        gnuR.end();
    }

    @Override
    public void rWriteConsole(Rengine rngn, String string, int i) {
        System.out.println(string);
    }

    @Override
    public void rBusy(Rengine rngn, int i) {
        System.out.println("rBusy(" + i + ")");
    }

    @Override
    public String rReadConsole(Rengine rngn, String string, int i) {
        System.out.print(string);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            return (s == null || s.length() == 0) ? s : s + "\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void rShowMessage(Rengine rngn, String string) {
        System.out.println("rShowMessage \"" + string + "\"");
    }

    @Override
    public String rChooseFile(Rengine rngn, int i) {
        FileDialog fd = new FileDialog(new Frame(), (i == 0) ? "Select a file" : "Select a new file", (i == 0) ? FileDialog.LOAD : FileDialog.SAVE);
        fd.show();
        String res = null;
        if (fd.getDirectory() != null) {
            res = fd.getDirectory();
        }
        if (fd.getFile() != null) {
            res = (res == null) ? fd.getFile() : (res + fd.getFile());
        }
        return res;
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
