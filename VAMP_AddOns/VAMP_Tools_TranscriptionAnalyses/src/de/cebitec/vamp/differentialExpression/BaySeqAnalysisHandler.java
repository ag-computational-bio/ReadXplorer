package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.BaySeq.SamplesNotValidException;
import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.vamp.util.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisHandler extends DeAnalysisHandler {

    private List<Group> groups;
    private BaySeq baySeq = new BaySeq();
    private BaySeqAnalysisData baySeqAnalysisData;

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

    public BaySeqAnalysisHandler(List<PersistantTrack> selectedTraks, List<Group> groups,
            Integer refGenomeID, int[] replicateStructure, File saveFile, FeatureType feature, int startOffset, int stopOffset) {
        super(selectedTraks, refGenomeID, saveFile, feature, startOffset, stopOffset);
        baySeqAnalysisData = new BaySeqAnalysisData(getSelectedTracks().size(), groups, replicateStructure);
        this.groups=groups;
    }

    @Override
    public void endAnalysis() {
        baySeq.shutdown();
        baySeq = null;
    }

    @Override
    protected List<Result> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        prepareFeatures(baySeqAnalysisData);
        prepareCountData(baySeqAnalysisData, getAllCountData());
        List<Result> results = baySeq.process(baySeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile());
        return results;
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
