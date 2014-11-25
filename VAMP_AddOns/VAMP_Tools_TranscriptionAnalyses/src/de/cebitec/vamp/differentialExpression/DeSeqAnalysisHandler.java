package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisHandler extends DeAnalysisHandler {

    private DeSeq deSeq = new DeSeq();
    private DeSeqAnalysisData deSeqAnalysisData;

    public static enum Plot {

        DispEsts("Per gene estimates against normalized mean expression"),
        DE("Log2 fold change against base means"),
        HIST("Histogram of p values");
        String representation;

        Plot(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }

        public static Plot[] getValues(boolean moreThanTwoConditions) {
            if (moreThanTwoConditions) {
                return new Plot[]{DispEsts};
            } else {
                return new Plot[]{DispEsts, DE, HIST};
            }
        }
    }

    public DeSeqAnalysisHandler(List<PersistantTrack> selectedTraks, Map<String, String[]> design, boolean moreThanTwoConditions,
            List<String> fittingGroupOne, List<String> fittingGroupTwo, Integer refGenomeID, boolean workingWithoutReplicates,
            File saveFile, List<FeatureType> selectedFeatures, int startOffset, int stopOffset) {
        super(selectedTraks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset);
        deSeqAnalysisData = new DeSeqAnalysisData(selectedTraks.size(),
                design, moreThanTwoConditions, fittingGroupOne, fittingGroupTwo, 
                workingWithoutReplicates);
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<ResultDeAnalysis> results;
        prepareFeatures(deSeqAnalysisData);
        prepareCountData(deSeqAnalysisData, getAllCountData());
        results = deSeq.process(deSeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile());
        return results;

    }

    public boolean moreThanTwoCondsForDeSeq() {
        return deSeqAnalysisData.moreThanTwoConditions();
    }

    @Override
    public void endAnalysis() {
        deSeq.shutdown();
        deSeq = null;
    }

    @Override
    public void saveResultsAsCSV(int selectedIndex, String path) {
        File saveFile = new File(path);
        deSeq.saveResultsAsCSV(selectedIndex, saveFile);
    }

    public File plot(Plot plot) throws IOException, IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile("VAMP_Plot_", ".svg");
        file.deleteOnExit();
        if (plot == Plot.DE) {
            deSeq.plotDE(file);
        }
        if (plot == Plot.DispEsts) {
            deSeq.plotDispEsts(file);
        }
        if (plot == Plot.HIST) {
            deSeq.plotHist(file);
        }
        return file;
    }
}
