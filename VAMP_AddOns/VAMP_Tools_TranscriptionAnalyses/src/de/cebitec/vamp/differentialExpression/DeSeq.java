package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.wizard.DeSeqAnalysisData;
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
        REXP baySeq = gnuR.eval("library(DESeq)");
        if (baySeq == null) {
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

//        if (!PerformAnalysis.TESTING_MODE) {
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

        //The individual variables are then used to create the design element
        gnuR.eval("design <- data.frame(row.names = colnames(inputData)," + concatenate.toString() + ")");

        //Now everything is set up and the count data object on which the main
        //analysis will be performed can be created
        gnuR.eval("cD <- newCountDataSet(inputData, design)");

        //We estimate the size factor
        gnuR.eval("cD <- estimateSizeFactors(cD)");

        //The dispersion is estimated
        gnuR.eval("cD <- estimateDispersions(cD)");


        //Perform the Test.
        //TODO: Test for multi experiments
        gnuR.eval("res <- nbinomTest( cD, \"1\", \"2\" )");

        List<RVector> results = new ArrayList<>();
        try {

            REXP result = gnuR.eval("res");
            RVector rvec = result.asVector();
            results.add(rvec);
        } catch (Exception e) {
            System.out.println("fehler");
        }

        if (saveFile != null) {
            String path = saveFile.getAbsolutePath();
            path = path.replace("\\", "\\\\");
            gnuR.eval("save.image(\"" + path + "\")");
        }
        return results;
    }
}
