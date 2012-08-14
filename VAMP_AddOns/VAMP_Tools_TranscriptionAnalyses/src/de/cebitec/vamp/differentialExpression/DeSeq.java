package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class DeSeq {

    private GnuR gnuR;

    public DeSeq() {
        gnuR = GnuR.getInstance();
    }

    public List<RVector> process(DeSeqAnalysisData analysisData, int numberOfAnnotations, int numberOfTracks, File saveFile) {
        int numberOfSubDesigns;
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: GNU R is processing data.", currentTimestamp);
        REXP deseq = gnuR.eval("library(DESeq)");
        if (deseq == null) {
            gnuR.eval("source(\"http://bioconductor.org/biocLite.R\")");
            gnuR.eval("biocLite(\"DESeq\")");
            gnuR.eval("library(DESeq)");
        }
        //Defining the plot functions:
        gnuR.eval("plotDispEsts <- function( cds ){plot(rowMeans( "
                + "counts( cds, normalized=TRUE ) ), fitInfo(cds)$perGeneDispEsts, "
                + "pch = '.', log=\"xy\" ) xg <- 10^seq( -.5, 5, length.out=300 ) "
                + "lines( xg, fitInfo(cds)$dispFun( xg ), col=\"red\")}");

        gnuR.eval("plotDE <- function( res ) plot(res$baseMean, res$log2FoldChange, "
                + "log=\"x\", pch=20, cex=.3, col = ifelse( res$padj < .1, \"red\", \"black\"))");

        if (!AnalysisHandler.TESTING_MODE) {
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
            gnuR.eval("inputData <- matrix(" + concatenate.toString() + "," + numberOfAnnotations + ")");
            //The colum names are created...
            concatenate = new StringBuilder("c(");
            List<PersistantTrack> tracks = analysisData.getSelectedTraks();
            for (Iterator<PersistantTrack> it = tracks.iterator(); it.hasNext();) {
                PersistantTrack persistantTrack = it.next();
                concatenate.append("\"").append(persistantTrack.getDescription()).append("\",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);
            concatenate.append(")");
            //...handed over to Gnu R...
            gnuR.eval("columNames <- " + concatenate.toString());
            //...and assigned to the count data matrix.
            gnuR.eval("colnames(inputData) <- columNames");
            //Now we need to name the rows. First hand over the row names to Gnu R...
            gnuR.assign("rowNames", analysisData.getLoci());
            //...and then assign them to the count data matrix.
            gnuR.eval("rownames(inputData) <- rowNames");

            //Now we need to hand over the experimental design behind the data.
            concatenate = new StringBuilder();
            numberOfSubDesigns = 0;
            //First all sub designs are assigned to an individual variable.
            while (analysisData.hasNextSubDesign()) {
                numberOfSubDesigns++;
                gnuR.assign("subDesign" + numberOfSubDesigns, analysisData.getNextSubDesign());
                concatenate.append("subDesign").append(numberOfSubDesigns).append(",");
            }
            concatenate.deleteCharAt(concatenate.length() - 1);

            if (numberOfSubDesigns > 1) {
                //The individual variables are then used to create the design element
                gnuR.eval("design <- data.frame(row.names = colnames(inputData)," + concatenate.toString() + ")");
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval("cD <- newCountDataSet(inputData, design)");
            } else {
                //If this is just a two conditons experiment we only create the conds array
                gnuR.eval("conds <- factor(subDesign1)");
                //Now everything is set up and the count data object on which the main
                //analysis will be performed can be created
                gnuR.eval("cD <- newCountDataSet(inputData, conds)");
            }

            //We estimate the size factor
            gnuR.eval("cD <- estimateSizeFactors(cD)");

            if (analysisData.isWorkingWithoutReplicates()) {
                // If there are no replicates for each condition we need to tell
                // the function to ignore this fact.
                gnuR.eval("cD <- estimateDispersions(cD, method=\"blind\", sharingMode=\"fit-only\")");
            } else {
                //The dispersion is estimated
                gnuR.eval("cD <- estimateDispersions(cD)");
            }


            if (numberOfSubDesigns > 1) {
                //TODO: Test for multi experiments
            } else {
                //Perform the normal test.
                gnuR.eval("res <- nbinomTest( cD, \"ONE\", \"TWO\" )");
                //Filter for significant genes, given a threshold for the FDR.
                //TODO: Make threshold user adjustable.
                gnuR.eval("resSig <- res[res$padj < 0.1, ]");
            }
        } else {
            gnuR.eval("data(testData)");
        }
        List<RVector> results = new ArrayList<>();
            //Significant results sorted by the most significantly differentially expressed genes
            gnuR.eval("res0 <- resSig[order(resSig$pval), ]");
            REXP result = gnuR.eval("res0");
            RVector rvec = result.asVector();
            results.add(rvec);

            //Significant results sorted by the most strongly down regulated genes
            gnuR.eval("res1 <- resSig[order(resSig$foldChange, -resSig$baseMean), ]");
            result = gnuR.eval("res1");
            rvec = result.asVector();
            results.add(rvec);

            //Significant results sorted by the most strongly up regulated genes
            gnuR.eval("res2 <- resSig[order(-resSig$foldChange, -resSig$baseMean), ]");
            result = gnuR.eval("res2");
            rvec = result.asVector();
            results.add(rvec);
        if (saveFile != null) {
            String path = saveFile.getAbsolutePath();
            path = path.replace("\\", "\\\\");
            gnuR.eval("save.image(\"" + path + "\")");
        }
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
        gnuR.eval("write.csv(res"+index+",file=\"" + path + "\")");
    }
}
