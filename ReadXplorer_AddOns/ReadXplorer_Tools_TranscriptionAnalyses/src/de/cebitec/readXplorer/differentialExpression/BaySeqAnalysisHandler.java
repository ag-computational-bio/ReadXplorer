package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.differentialExpression.BaySeq.SamplesNotValidException;
import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisHandler extends DeAnalysisHandler {

    private List<Group> groups;
    private BaySeq baySeq;
    private BaySeqAnalysisData baySeqAnalysisData;
    private UUID key;

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

    public BaySeqAnalysisHandler(List<PersistantTrack> selectedTracks, List<Group> groups, Integer refGenomeID, int[] replicateStructure,
            File saveFile, List<FeatureType> selectedFeatures, int startOffset, int stopOffset, ParametersReadClasses readClassParams, boolean regardReadOrientation, UUID key) {
        super(selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, regardReadOrientation);
        baySeq = new BaySeq(this.getRefGenomeID());
        baySeqAnalysisData = new BaySeqAnalysisData(getSelectedTracks().size(), groups, replicateStructure);
        this.groups=groups;
        this.key = key;
    }

    @Override
    public void endAnalysis() {
        baySeq.shutdown(key);
        baySeq = null;
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        prepareFeatures(baySeqAnalysisData);
        prepareCountData(baySeqAnalysisData, getAllCountData());
        List<ResultDeAnalysis> results = baySeq.process(baySeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile(), key);
        return results;
    }

    public File plot(Plot plot, Group group, int[] samplesA, int[] samplesB) throws IOException, SamplesNotValidException,
            IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile("ReadXplorer_Plot_", ".svg");
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

    public List<Group> getGroups() {
        return groups;
    }
}
