package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
public class SimpleTest {

    private GnuR gnuR;

    public SimpleTest() {
    }

    public List<DeAnalysisHandler.Result> process(SimpleTestAnalysisData analysisData, int numberOfFeatures, File saveFile)
            throws JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        gnuR = GnuR.SecureGnuRInitiliser.getGnuRinstance();
        gnuR.clearGnuR();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R is processing data.", currentTimestamp);
        List<DeAnalysisHandler.Result> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
            //Load an R image containing the calculation functions
            try {
                InputStream jarPath = DeSeq.class.getResourceAsStream("/de/cebitec/vamp/differentialExpression/simpleTest.rdata");
                File to = File.createTempFile("VAMP_", ".rdata");
                to.deleteOnExit();
                Files.copy(jarPath, to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String tmpPath = to.getAbsolutePath();
                tmpPath = tmpPath.replace("\\", "\\\\");
                gnuR.eval("load(file=\"" + tmpPath + "\")");
                jarPath.close();
            } catch (IOException ex) {
                currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: Unable to load calculation functions. The Simple Test will not work!", currentTimestamp);
                return (null);
            }

            //Handing over the count data to Gnu R.
            int i = 1;
            StringBuilder concatenate = new StringBuilder("c(");
            //First the count data for each track is handed over seperatly.
            while (analysisData.hasCountData()) {
                gnuR.assign("inputData" + i, analysisData.pollFirstCountData());
                concatenate.append("inputData").append(i++).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            concatenate.append(")");
            //Then the big count data matrix is created out of the single track data handed over.
            gnuR.eval("inputData <- matrix(" + concatenate.toString() + "," + numberOfFeatures + ")");
            //The colum names are handed over to Gnu R... 
            gnuR.assign("columNames", analysisData.getTrackDescriptions());
            //...and assigned to the count data matrix.
            gnuR.eval("colnames(inputData) <- columNames");
            //Now we need to name the rows. First hand over the row names to Gnu R...
            gnuR.assign("rowNames", analysisData.getLoci());
            //...and then assign them to the count data matrix.
            gnuR.eval("rownames(inputData) <- rowNames");

            //The two Integer[] defining the groups must be handed over now
            gnuR.assign("groupA", analysisData.getGroupA());
            gnuR.eval("groupA <- c(groupA)");
            gnuR.assign("groupB", analysisData.getGroupB());
            gnuR.eval("groupB <- c(groupB)");

            if (saveFile != null) {
                gnuR.saveDataToFile(saveFile);
            }

            //We need to distinguish between an experiment with no replicates and an
            //experiement with such. In der first case results will be unreliable.
            if (analysisData.isWorkingWithoutReplicates()) {
                //For just to replicates no means can be computed. So this step
                //is just a rewriting the table to a fitting format for the
                //following function. The Means notet are just the absolute value
                //from each condition and the variance is just set to the string "-".
                //However, if just one condition has no replicate the mean value of
                //the condition having replicates will be used.
                gnuR.eval("means <- createMeansTableTwoConds(inputData,groupA,groupB)");
            } else {
                //First we build the means for each row coresponding to the given groups
                gnuR.eval("means <- createMeansTable(inputData,groupA,groupB)");
            }
            //Now we remove all rows with both values < cutof. This cut of value can
            //be specified as the secound argument of the function. If left unspecified
            //it will be set to 30.
            gnuR.eval("filtered <- filterLowValues(means)");
            //The ratios A/B and B/A are calculated
            gnuR.eval("ratios <- calculateRatios(filtered)");
            if (analysisData.isWorkingWithoutReplicates()) {
                gnuR.eval("res <- calculateConfidenceTwoConds(ratios)");
            } else {
                //Finally the confidence values are beeing computed
                gnuR.eval("res <- calculateConfidence(ratios)");
            }

            //Ordered by expression in group A.
            gnuR.eval("res0 <- head(res[rev(order(res$ratioAB)),],100)");
            REXP result = gnuR.eval("res0");
            RVector rvec = result.asVector();
            REXP colNames = gnuR.eval("colnames(res0)");
            REXP rowNames = gnuR.eval("rownames(res0)");
            results.add(new DeAnalysisHandler.Result(rvec, colNames, rowNames, "Ordered by expression in group A"));

            //Ordered by expression in group B.
            gnuR.eval("res1 <- head(res[rev(order(res$ratioBA)),],100)");
            result = gnuR.eval("res1");
            rvec = result.asVector();
            colNames = gnuR.eval("colnames(res1)");
            rowNames = gnuR.eval("rownames(res1)");
            results.add(new DeAnalysisHandler.Result(rvec, colNames, rowNames, "Ordered by expression in group B"));

            //Ordered by confidence.
            gnuR.eval("res2 <- head(res[rev(order(res$confidence)),],100)");
            result = gnuR.eval("res2");
            rvec = result.asVector();
            colNames = gnuR.eval("colnames(res2)");
            rowNames = gnuR.eval("rownames(res2)");
            results.add(new DeAnalysisHandler.Result(rvec, colNames, rowNames, "Ordered by confidence"));

            if (saveFile != null) {
                gnuR.saveDataToFile(saveFile);
            }
        } //We don't know what errors Gnu R might cause, so we have to catch all.
        //The new generated exception can than be caught an handelt by the DeAnalysisHandler
        catch (Exception e) {
            throw new UnknownGnuRException(e);
        }
        currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R finished processing data.", currentTimestamp);
        return results;
    }

    /**
     * Releases the Gnu R instance and removes the reference to it.
     */
    public void shutdown() {
        gnuR.releaseGnuRInstance();
        gnuR = null;
    }

    public void saveResultsAsCSV(int index, File saveFile) {
        String path = saveFile.getAbsolutePath();
        path = path.replace("\\", "/");
        gnuR.eval("write.csv(res" + index + ",file=\"" + path + "\")");
    }

    public void plotAB(File file) throws IllegalStateException, PackageNotLoadableException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.loadPackage("grDevices");
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("svg(filename=\"" + path + "\")");
        gnuR.eval("plot(res[,5],res[,7],xlab=\"ratio A to B\",ylab=\"confidence\")");
        gnuR.eval("dev.off()");
    }

    public void plotBA(File file) throws IllegalStateException, PackageNotLoadableException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.loadPackage("grDevices");
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("svg(filename=\"" + path + "\")");
        gnuR.eval("plot(res[,6],res[,7],xlab=\"ratio B to A\",ylab=\"confidence\")");
        gnuR.eval("dev.off()");
    }
}
