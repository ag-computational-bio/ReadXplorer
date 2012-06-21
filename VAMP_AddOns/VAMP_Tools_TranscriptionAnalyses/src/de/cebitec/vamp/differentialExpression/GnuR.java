package de.cebitec.vamp.differentialExpression;

import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author kstaderm
 */
public class GnuR {

    private Rengine gnuR;
    diffExpVisualPanel3 monitor;

    public GnuR(diffExpVisualPanel3 monitor) {
        this.monitor = monitor;
    }

    public void process(BaySeqAnalysisData bseqData, int numberOfAnnotations, int numberOfTracks) {
        String[] args = new String[0];
        int numberofGroups;
        gnuR = new Rengine(args, false, monitor);
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
        //Gnu R is configured to use all your processor cores but one. So the
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
            numberofGroups = 1;
            while (bseqData.hasGroups()) {
                gnuR.assign("group" + numberofGroups, bseqData.getNextGroup());
                concatenate.append("group").append(numberofGroups).append("=").append("group").append(numberofGroups++).append(",");
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
        List<Vector> results = new ArrayList<Vector>();
        for (int j = 1; j <= numberofGroups; j++) {
            REXP result = gnuR.eval("topCounts(cD, group = " + j + ")");
            RVector rvec = result.asVector();
            results.add(rvec);
        }
        monitor.addResult(results);
        monitor.writeLineToConsole("Found " + results.size() + " results.");
    }

    public void shutdown() {
        gnuR.end();
    }
}
