package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.BaySeq.SamplesNotValidException;
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
public class BaySeqAnalysisHandler extends AnalysisHandler {

    private int[] replicateStructure;
    private List<Group> groups;
    private BaySeq baySeq = new BaySeq();

    public static enum Plot {

        Priors("Priors"),
        MACD("\"MA\"-Plot for the count data"), 
        Posteriors("Posterior likelihoods of differential expression against log-ratio");
        String representation;

        Plot(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }
    }

    public BaySeqAnalysisHandler(List<PersistantTrack> selectedTraks, List<Group> groups, Integer refGenomeID, int[] replicateStructure, File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        this.groups = groups;
        this.replicateStructure = replicateStructure;
    }

    @Override
    public void performAnalysis() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<Result> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            BaySeqAnalysisData baySeqAnalysisData = new BaySeqAnalysisData(getSelectedTraks().size(), this.groups, this.replicateStructure);
            prepareFeatures(baySeqAnalysisData);
            prepareCountData(baySeqAnalysisData, allCountData);
            results = baySeq.process(baySeqAnalysisData, getPersAnno().size(), getSelectedTraks().size(), getSaveFile());
        } else {
            results = baySeq.process(null, 3232, getSelectedTraks().size(), getSaveFile());
        }

        setResults(results);
        notifyObservers(AnalysisStatus.FINISHED);
    }

    @Override
    public void endAnalysis() {
        baySeq.shutdown();
        baySeq = null;
    }

    public File plot(Plot plot, Group group, int[] samplesA, int[] samplesB) throws IOException, SamplesNotValidException, 
                                                                        IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile("VAMP_Plot_", ".svg");
        file.deleteOnExit();
        if (plot == Plot.MACD) {
            baySeq.plotMACD(file, samplesA, samplesB);
        }
        if (plot == Plot.Posteriors) {
            baySeq.plotPosteriors(file, group, samplesA, samplesB);
        }
        if (plot == Plot.Priors) {
            baySeq.plotPriors(file, group);
        }
        return file;
    }

    @Override
    public void saveResultsAsCSV(int selectedIndex, String path) {
        File saveFile = new File(path);
        baySeq.saveResultsAsCSV(selectedIndex, saveFile);
    }

    public List<Group> getGroups() {
        return groups;
    }
}
