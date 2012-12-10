package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisHandler extends AnalysisHandler {

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
                return new Plot[]{DispEsts,DE,HIST};
            }
        }
    }

    public DeSeqAnalysisHandler(List<PersistantTrack> selectedTraks,
            Map<String, String[]> design, boolean moreThanTwoConditions,
            List<String> fittingGroupOne, List<String> fittingGroupTwo,
            Integer refGenomeID, boolean workingWithoutReplicates, File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        deSeqAnalysisData = new DeSeqAnalysisData(selectedTraks.size(),
                design, moreThanTwoConditions, fittingGroupOne, fittingGroupTwo, workingWithoutReplicates);
    }

    @Override
    public void performAnalysis() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<Result> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            prepareAnnotations(deSeqAnalysisData);
            prepareCountData(deSeqAnalysisData, allCountData);
            results = deSeq.process(deSeqAnalysisData, getPersAnno().size(), getSelectedTraks().size(), getSaveFile());
        } else {
            results = deSeq.process(deSeqAnalysisData, 3434, getSelectedTraks().size(), getSaveFile());
        }
        setResults(results);
        notifyObservers(AnalysisStatus.FINISHED);
    }
    
    public boolean moreThanTwoCondsForDeSeq(){
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
