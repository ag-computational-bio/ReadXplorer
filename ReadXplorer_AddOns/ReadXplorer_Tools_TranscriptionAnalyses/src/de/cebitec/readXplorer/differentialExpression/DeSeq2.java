package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class DeSeq2 {

    private GnuR gnuR;

    public DeSeq2(int referenceId) {
    }

    public List<ResultDeAnalysis> process(DeSeqAnalysisData analysisData,
            int numberOfFeatures, int numberOfTracks, File saveFile, UUID key)
            throws PackageNotLoadableException, JRILibraryNotInPathException,
            IllegalStateException, UnknownGnuRException {
        gnuR = GnuR.SecureGnuRInitiliser.getGnuRinstance(key);
        gnuR.clearGnuR();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R is processing data.", currentTimestamp);
        gnuR.loadPackage("DESeq2");
        gnuR.loadPackage("Biobase");
        List<ResultDeAnalysis> results = new ArrayList<>();
        //A lot of bad things can happen during the data processing by Gnu R.
        //So we need to prepare for this.
        try {
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
            //Then the big count data matrix is created from the single track data handed over.
            gnuR.eval("inputData <- matrix(" + concatenate.toString() + "," + numberOfFeatures + ")");
            //The colum names are handed over to Gnu R...
            gnuR.assign("columNames", analysisData.getTrackDescriptions());
            //...and assigned to the count data matrix.
            gnuR.eval("colnames(inputData) <- columNames");
            //Now we need to name the rows. First hand over the row names to Gnu R...
            gnuR.assign("rowNames", analysisData.getFeatureNames());
            //...and then assign them to the count data matrix.
            gnuR.eval("rownames(inputData) <- rowNames");
            //Remove all the sides that don't appear under any condition because
            //those rows produce "NA" rows in the results table.
            gnuR.eval("inputData <- inputData[rowSums(inputData) > 0,]");

            //Now we need to hand over the experimental design behind the data.
            concatenate = new StringBuilder();
            //First all sub designs are assigned to an individual variable.
            while (analysisData.hasNextSubDesign()) {
                DeSeqAnalysisData.ReturnTupel subDesign = analysisData.getNextSubDesign();
                gnuR.assign(subDesign.getKey(), subDesign.getValue());
                concatenate.append(subDesign.getKey()).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);

            if (saveFile != null) {
                String path = saveFile.getAbsolutePath();
                path = path.replace("\\", "\\\\");
                gnuR.eval("save.image(\"" + path + "\")");
            }

            if (analysisData.moreThanTwoConditions()) {
                //TODO
            } else {
                //If this is just a two conditons experiment we only create the conds array
                gnuR.eval("conds <- factor(" + concatenate.toString() + ")");
                gnuR.eval("design <- data.frame(row.names = colnames(inputData),conds)");
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval("dds <- DESeqDataSetFromMatrix(countData = inputData, colData = design, design = ~ conds)");
            }

            REXP E = gnuR.eval("dds <- DESeq(dds)");
            E = gnuR.eval("res <- results(dds)");
            E = gnuR.eval("res <- res[order(res$padj),]");

            REXP currentResult1 = gnuR.eval("as.data.frame(res)");
            RVector tableContents1 = currentResult1.asVector();
            REXP colNames1 = gnuR.eval("colnames(res)");
            REXP rowNames1 = gnuR.eval("rownames(res)");
            results.add(new ResultDeAnalysis(tableContents1, colNames1, rowNames1, "Results", analysisData));

            if (saveFile != null) {
                String path = saveFile.getAbsolutePath();
                path = path.replace("\\", "\\\\");
                gnuR.eval("save.image(\"" + path + "\")");
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
    public void shutdown(UUID key) {
        if (gnuR != null) {
            gnuR.releaseGnuRInstance(key);
            gnuR = null;
        }
    }

    public void saveResultsAsCSV(int index, File saveFile) {
        String path = saveFile.getAbsolutePath();
        path = path.replace("\\", "/");
        gnuR.eval("write.csv(res" + index + ",file=\"" + path + "\")");
    }

    public void plotDispEsts(File file) throws IllegalStateException, PackageNotLoadableException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.loadPackage("grDevices");
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("svg(filename=\"" + path + "\")");
        gnuR.eval("plotDispEsts(cD)");
        gnuR.eval("dev.off()");
    }

    public void plotDE(File file) throws IllegalStateException, PackageNotLoadableException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.loadPackage("grDevices");
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("svg(filename=\"" + path + "\")");
        gnuR.eval("plotDE(res)");
        gnuR.eval("dev.off()");
    }

    public void plotHist(File file) throws IllegalStateException, PackageNotLoadableException {
        if (gnuR == null) {
            throw new IllegalStateException("Shutdown was already called!");
        }
        gnuR.loadPackage("grDevices");
        String path = file.getAbsolutePath();
        path = path.replace("\\", "\\\\");
        gnuR.eval("svg(filename=\"" + path + "\")");
        gnuR.eval("hist(res$pval, breaks=100, col=\"skyblue\", border=\"slateblue\", main=\"\")");
        gnuR.eval("dev.off()");
    }
}
