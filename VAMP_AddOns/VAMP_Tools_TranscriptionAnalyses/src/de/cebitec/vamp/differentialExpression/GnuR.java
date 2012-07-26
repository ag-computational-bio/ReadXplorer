package de.cebitec.vamp.differentialExpression;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author kstaderm
 */
public class GnuR implements RMainLoopCallbacks {

    private Rengine gnuR;

    public GnuR() {
    }

    public List<RVector> process(BaySeqAnalysisData bseqData, int numberOfAnnotations, int numberOfTracks, File saveFile) {
        String[] args = new String[0];
        int numberofGroups;
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
        //Gnu R is configured to use all your processor cores aside from one. So the
        //computation will speed up a little bit but still leave you one core
        //for your other work.
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors > 1) {
            processors--;
        }
        System.out.println("Gnu R running on " + processors + " cores.");
        gnuR.eval("cl <- makeCluster(" + processors + ", \"SOCK\")");
        if (!PerformAnalysis.TESTING_MODE) {
            int i = 1;
            StringBuilder concatenate = new StringBuilder("c(");
            while (bseqData.hasCountData()) {
                gnuR.assign("inputData" + i, bseqData.pollFirstCountData());
                concatenate.append("inputData").append(i++).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            concatenate.append(")");
            gnuR.eval("inputData <- matrix(" + concatenate.toString() + "," + numberOfAnnotations + ")");
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
            numberofGroups = 0;
            while (bseqData.hasGroups()) {
                numberofGroups++;
                gnuR.assign("group" + numberofGroups, bseqData.getNextGroup());
                concatenate.append("group").append(numberofGroups).append("=").append("group").append(numberofGroups).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            gnuR.eval("groups(cD) <- list(" + concatenate.toString() + ")");
            //parameter samplesize could be added.
            gnuR.eval("cD <- getPriors.NB(cD, cl = cl)");
            gnuR.eval("cD <- getLikelihoods.NB(cD, nullData = TRUE, cl = cl)");
        } else {
            gnuR.eval("data(testData)");
            numberofGroups = 2;
        }
        List<RVector> results = new ArrayList<>();
        for (int j = 1; j <= numberofGroups; j++) {
            REXP result = gnuR.eval("topCounts(cD , group = " + j + " , number = " + numberOfAnnotations + ")");
            RVector rvec = result.asVector();
            results.add(rvec);
        }
        for (int j = 1; j <= numberofGroups; j++) {
            REXP result = gnuR.eval("topCounts(cD , group = " + j + " , number = " + numberOfAnnotations + " , normaliseData=TRUE)");
            RVector rvec = result.asVector();
            results.add(rvec);
        }
        if (saveFile != null) {
            String path = saveFile.getAbsolutePath();
            path = path.replace("\\", "\\\\");
            gnuR.eval("save.image(\"" + path + "\")");
        }
        return results;
    }

    public void shutdown() {
        gnuR.end();
    }

    public void plotMACD(File file, int[] samplesA, int[] samplesB) {
        setUpSvgOutput();
        StringBuilder samplesABuilder = new StringBuilder();
        samplesABuilder.append((samplesA[0] + 1)).append(":").append((samplesA[samplesA.length - 1] + 1));
        StringBuilder samplesBBuilder = new StringBuilder();
        samplesBBuilder.append((samplesB[0] + 1)).append(":").append((samplesB[samplesB.length - 1] + 1));
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("devSVG(file=\"" + path + "\")");
        gnuR.eval("plotMA.CD(cD, samplesA = " + samplesABuilder.toString() + ", "
                + "samplesB = " + samplesBBuilder.toString() + ")");
        gnuR.eval("dev.off()");
    }

    public void plotPosteriors(File file, Group group, int[] samplesA, int[] samplesB) {
        setUpSvgOutput();
        StringBuilder samplesABuilder = new StringBuilder();
        samplesABuilder.append((samplesA[0] + 1)).append(":").append((samplesA[samplesA.length - 1] + 1));
        StringBuilder samplesBBuilder = new StringBuilder();
        samplesBBuilder.append((samplesB[0] + 1)).append(":").append((samplesB[samplesB.length - 1] + 1));
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("devSVG(file=\"" + path + "\")");
        gnuR.eval("plotPosteriors(cD, group = " + group.getGnuRID()
                + ", samplesA = "+ samplesABuilder.toString() 
                + ", samplesB = " + samplesBBuilder.toString() 
                + ", col = c(rep(\"blue\", 100), rep(\"black\", 900))"
                + ")");
        gnuR.eval("dev.off()");
    }

    public void plotPriors(File file, Group group) {
        setUpSvgOutput();
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("devSVG(file=\"" + path + "\")");
        gnuR.eval("plotPriors(cD, group = " + group.getGnuRID() + ")");
        gnuR.eval("dev.off()");
    }

    private void setUpSvgOutput() {
        REXP svg = gnuR.eval("library(RSvgDevice)");
        if (svg == null) {
            gnuR.eval("install.packages(\"RSvgDevice\")");
            gnuR.eval("library(RSvgDevice)");
        }
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
