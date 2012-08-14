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
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class BaySeq {

    private GnuR gnuR;
    private int numberOfAnnotations;

    public BaySeq() {
        gnuR = GnuR.getInstance();
    }

    /**
     * Processes data from a differential expression analysis experiment using
     * the baySeq package.
     *
     * @param bseqData The prepared experiment data set.
     * @param numberOfAnnotations The number of underlying annotations.
     * @param numberOfTracks The number of underlying tracks.
     * @param saveFile The Gnu R dataset will be saved to this file. If no
     * saving should be done just pass null here.
     * @return a List of RVector. Each RVector represents the results for one
     * Group. The number of RVectors is always two times the number of committed
     * groups because there is always one normalised and one not normalised
     * result. Example: If you commited two groups. You will get four RVectors
     * as an result. The first RVector will represent the not normalised result
     * for the first committed group. The secound will represent the not
     * normalised result for the secound group. The third result will then
     * represent the normalised result for group one and the fourth result will
     * represent the normalised result for group two. So you will first get all
     * not normalised results and then all the normalised ones.
     */
    public List<RVector> process(BaySeqAnalysisData bseqData, int numberOfAnnotations, int numberOfTracks, File saveFile) throws IllegalStateException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        this.numberOfAnnotations = numberOfAnnotations;
        int numberofGroups;
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
        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Gnu R running on " + processors + " cores.", currentTimestamp);
        gnuR.eval("cl <- makeCluster(" + processors + ", \"SOCK\")");
        if (!AnalysisHandler.TESTING_MODE) {
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
            gnuR.assign("inputAnnotationsID", bseqData.getLoci());
            gnuR.eval("annotations <- data.frame(inputAnnotationsID,inputAnnotationsStart,inputAnnotationsStop)");
            gnuR.eval("colnames(annotations) <- c(\"locus\", \"start\", \"stop\")");
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
            gnuR.saveDataToFile(saveFile);
        }
        return results;
    }

    /**
     * Creates an MACD plot of the current data. The process(...) method must be
     * called before. If this is not done there will be no data in the Gnu R
     * memory which can be plotted. So this method will also not work after
     * calling clearGnuR() at least not until you have called process(...)
     * again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param samplesA an int array representing the first sample group that
     * should be plotted.
     * @param samplesB an int array representing the secound sample group that
     * should be plotted. SamplesA and samplesB must not be the same!
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotMACD(File file, int[] samplesA, int[] samplesB) throws SamplesNotValidException, IllegalStateException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        if (!validateSamples(samplesA, samplesB)) {
            throw new SamplesNotValidException();
        }
        gnuR.setUpSvgOutput();
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

    /**
     * Plots the posterior values of the current data. The process(...) method
     * must be called before. If this is not done there will be no data in the
     * Gnu R memory which can be plotted. So this method will also not work
     * after calling clearGnuR() at least not until you have called process(...)
     * again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param group the underlying group for the plot.
     * @param samplesA an int array representing the first sample group that
     * should be plotted.
     * @param samplesB an int array representing the secound sample group that
     * should be plotted. SamplesA and samplesB must not be the same!
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotPosteriors(File file, Group group, int[] samplesA, int[] samplesB) throws SamplesNotValidException, IllegalStateException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        if (!validateSamples(samplesA, samplesB)) {
            throw new SamplesNotValidException();
        }
        gnuR.setUpSvgOutput();
        StringBuilder samplesABuilder = new StringBuilder();
        samplesABuilder.append((samplesA[0] + 1)).append(":").append((samplesA[samplesA.length - 1] + 1));
        StringBuilder samplesBBuilder = new StringBuilder();
        samplesBBuilder.append((samplesB[0] + 1)).append(":").append((samplesB[samplesB.length - 1] + 1));
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("devSVG(file=\"" + path + "\")");
        gnuR.eval("plotPosteriors(cD, group = " + group.getGnuRID()
                + ", samplesA = " + samplesABuilder.toString()
                + ", samplesB = " + samplesBBuilder.toString()
                + ", col = c(rep(\"blue\", 100), rep(\"black\", 900))"
                + ")");
        gnuR.eval("dev.off()");
    }

    /**
     * Plots the prior values of the current data. The process(...) method must
     * be called before. If this is not done there will be no data in the Gnu R
     * memory which can be plotted. So this method will also not work after
     * calling clearGnuR() at least not until you have called process(...)
     * again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param group the underlying group for the plot.
     */
    public void plotPriors(File file, Group group) throws IllegalStateException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.setUpSvgOutput();
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("devSVG(file=\"" + path + "\")");
        gnuR.eval("plotPriors(cD, group = " + group.getGnuRID() + ")");
        gnuR.eval("dev.off()");
    }

    public void saveResultsAsCSV(Group group, File saveFile, Boolean normalized) {
        String path = saveFile.getAbsolutePath();
        path = path.replace("\\", "/");
        gnuR.eval("write.csv(topCounts(cD, group=" + group.getGnuRID()
                + ",number = " + numberOfAnnotations + ", normaliseData="
                + normalized.toString().toUpperCase() + "),file=\"" + path + "\")");
    }

    /**
     * Validates if the samples A and B are not the same. For the MACD and
     * Posteriors plot samplesA and samplesB must not be the same.
     *
     * @param samplA int array representing samplesA.
     * @param samplB int array representing samplesB.
     * @return true if samplA and samplB are not the same or else false
     */
    private boolean validateSamples(int[] samplA, int[] samplB) {
        boolean inputValid = false;
        if (samplA.length != samplB.length) {
            inputValid = true;
        } else {
            for (int i = 0; i < samplA.length; i++) {
                if (samplA[i] != samplB[i]) {
                    inputValid = true;
                    break;
                }
            }
        }
        return inputValid;
    }

    /**
     * Releases the Gnu R instance and removes the reference to it.
     */
    public void shutdown() {
        gnuR.releaseGnuRInstance();
        gnuR = null;
    }

    /**
     * The SamplesNotValidException is thrown by the plotting methods.
     */
    public static class SamplesNotValidException extends Exception {

        public SamplesNotValidException() {
            super();
        }
    }
}
