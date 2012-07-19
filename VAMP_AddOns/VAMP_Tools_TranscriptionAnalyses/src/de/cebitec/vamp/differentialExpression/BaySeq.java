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
import org.rosuda.JRI.Rengine;

/**
 *
 * @author kstaderm
 */
public class BaySeq {

    private GnuR gnuR;
    private Rengine engine;
    private String cranMirror;
    
    public BaySeq(){
        gnuR=new GnuR();
        gnuR.startUp();
        engine=gnuR.getEngine();
        cranMirror=gnuR.getCranMirror();
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
    public List<RVector> processWithBaySeq(BaySeqAnalysisData bseqData, int numberOfAnnotations, int numberOfTracks, File saveFile) {
        int numberofGroups;
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R is processing data.", currentTimestamp);
        REXP baySeq = engine.eval("library(baySeq)");
        if (baySeq == null) {
            engine.eval("source(\"http://bioconductor.org/biocLite.R\")");
            engine.eval("{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}");
            engine.eval("biocLite(\"baySeq\")");
            engine.eval("library(baySeq)");
        }
        REXP snow = engine.eval("library(snow)");
        if (snow == null) {
            engine.eval("{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}");
            engine.eval("install.packages(\"snow\")");
            engine.eval("library(snow)");
        }
        //Gnu R is configured to use all your processor cores aside from one. So the
        //computation will speed up a little bit but still leave you one core
        //for your other work.
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors > 1) {
            processors--;
        }
        System.out.println("Gnu R running on " + processors + " cores.");
        engine.eval("cl <- makeCluster(" + processors + ", \"SOCK\")");
        if (!PerformAnalysis.TESTING_MODE) {
            int i = 1;
            StringBuilder concatenate = new StringBuilder("c(");
            while (bseqData.hasCountData()) {
                engine.assign("inputData" + i, bseqData.pollFirstCountData());
                concatenate.append("inputData").append(i++).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            concatenate.append(")");
            engine.eval("inputData <- matrix(" + concatenate.toString() + "," + numberOfAnnotations + ")");
            engine.assign("inputAnnotationsStart", bseqData.getStart());
            engine.assign("inputAnnotationsStop", bseqData.getStop());
            engine.assign("inputAnnotationsID", bseqData.getLoci());
            engine.eval("annotations <- data.frame(inputAnnotationsID,inputAnnotationsStart,inputAnnotationsStop)");
            engine.eval("colnames(annotations) <- c(\"locus\", \"start\", \"stop\")");
            engine.eval("seglens <- annotations$stop - annotations$start + 1");
            engine.eval("cD <- new(\"countData\", data = inputData, seglens = seglens, annotation = annotations)");
            engine.eval("cD@libsizes <- getLibsizes(cD, estimationType = \"quantile\")");
            engine.assign("replicates", bseqData.getReplicateStructure());
            engine.eval("replicates(cD) <- as.factor(c(replicates))");
            concatenate = new StringBuilder();
            numberofGroups = 0;
            while (bseqData.hasGroups()) {
                numberofGroups++;
                engine.assign("group" + numberofGroups, bseqData.getNextGroup());
                concatenate.append("group").append(numberofGroups).append("=").append("group").append(numberofGroups).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            engine.eval("groups(cD) <- list(" + concatenate.toString() + ")");
            //parameter samplesize could be added.
            engine.eval("cD <- getPriors.NB(cD, cl = cl)");
            engine.eval("cD <- getLikelihoods.NB(cD, nullData = TRUE, cl = cl)");
        } else {
            engine.eval("data(testData)");
            numberofGroups = 2;
        }
        List<RVector> results = new ArrayList<>();
        for (int j = 1; j <= numberofGroups; j++) {
            REXP result = engine.eval("topCounts(cD , group = " + j + " , number = " + numberOfAnnotations + ")");
            RVector rvec = result.asVector();
            results.add(rvec);
        }
        for (int j = 1; j <= numberofGroups; j++) {
            REXP result = engine.eval("topCounts(cD , group = " + j + " , number = " + numberOfAnnotations + " , normaliseData=TRUE)");
            RVector rvec = result.asVector();
            results.add(rvec);
        }
        if (saveFile != null) {
            String path = saveFile.getAbsolutePath();
            path = path.replace("\\", "\\\\");
            engine.eval("save.image(\"" + path + "\")");
        }
        return results;
    }

    /**
     * Creates an MACD plot of the current data. The processWithBaySeq(...)
     * method must be called before. If this is not done there will be no data
     * in the Gnu R memory which can be plotted. So this method will also not
     * work after calling clearGnuR() at least not until you have called
     * processWithBaySeq(...) again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param samplesA an int array representing the first sample group that
     * should be plotted.
     * @param samplesB an int array representing the secound sample group that
     * should be plotted. SamplesA and samplesB must not be the same!
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotMACD(File file, int[] samplesA, int[] samplesB) throws SamplesNotValidException {
        if (!validateSamples(samplesA, samplesB)) {
            throw new SamplesNotValidException();
        }
        setUpSvgOutput();
        StringBuilder samplesABuilder = new StringBuilder();
        samplesABuilder.append((samplesA[0] + 1)).append(":").append((samplesA[samplesA.length - 1] + 1));
        StringBuilder samplesBBuilder = new StringBuilder();
        samplesBBuilder.append((samplesB[0] + 1)).append(":").append((samplesB[samplesB.length - 1] + 1));
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        engine.eval("devSVG(file=\"" + path + "\")");
        engine.eval("plotMA.CD(cD, samplesA = " + samplesABuilder.toString() + ", "
                + "samplesB = " + samplesBBuilder.toString() + ")");
        engine.eval("dev.off()");
    }

    /**
     * Plots the posterior values of the current data. The
     * processWithBaySeq(...) method must be called before. If this is not done
     * there will be no data in the Gnu R memory which can be plotted. So this
     * method will also not work after calling clearGnuR() at least not until
     * you have called processWithBaySeq(...) again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param group the underlying group for the plot.
     * @param samplesA an int array representing the first sample group that
     * should be plotted.
     * @param samplesB an int array representing the secound sample group that
     * should be plotted. SamplesA and samplesB must not be the same!
     * @throws SamplesNotValidException if SamplesA and samplesB are the same
     */
    public void plotPosteriors(File file, Group group, int[] samplesA, int[] samplesB) throws SamplesNotValidException {
        if (!validateSamples(samplesA, samplesB)) {
            throw new SamplesNotValidException();
        }
        setUpSvgOutput();
        StringBuilder samplesABuilder = new StringBuilder();
        samplesABuilder.append((samplesA[0] + 1)).append(":").append((samplesA[samplesA.length - 1] + 1));
        StringBuilder samplesBBuilder = new StringBuilder();
        samplesBBuilder.append((samplesB[0] + 1)).append(":").append((samplesB[samplesB.length - 1] + 1));
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        engine.eval("devSVG(file=\"" + path + "\")");
        engine.eval("plotPosteriors(cD, group = " + group.getGnuRID()
                + ", samplesA = " + samplesABuilder.toString()
                + ", samplesB = " + samplesBBuilder.toString()
                + ", col = c(rep(\"blue\", 100), rep(\"black\", 900))"
                + ")");
        engine.eval("dev.off()");
    }

    /**
     * Plots the prior values of the current data. The processWithBaySeq(...)
     * method must be called before. If this is not done there will be no data
     * in the Gnu R memory which can be plotted. So this method will also not
     * work after calling clearGnuR() at least not until you have called
     * processWithBaySeq(...) again.
     *
     * @param file a File the created SVG image should be saved to.
     * @param group the underlying group for the plot.
     */
    public void plotPriors(File file, Group group) {
        setUpSvgOutput();
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        engine.eval("devSVG(file=\"" + path + "\")");
        engine.eval("plotPriors(cD, group = " + group.getGnuRID() + ")");
        engine.eval("dev.off()");
    }

    /**
     * Used by all the plotting methods to set up Gnu R to create SVGs.
     */
    private void setUpSvgOutput() {
        REXP svg = engine.eval("library(RSvgDevice)");
        if (svg == null) {
            engine.eval("{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}");
            engine.eval("install.packages(\"RSvgDevice\")");
            engine.eval("library(RSvgDevice)");
        }
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
     * The SamplesNotValidException is thrown by the plotting methods.
     */
    public class SamplesNotValidException extends Exception {

        public SamplesNotValidException() {
            super();
        }
    }
}
