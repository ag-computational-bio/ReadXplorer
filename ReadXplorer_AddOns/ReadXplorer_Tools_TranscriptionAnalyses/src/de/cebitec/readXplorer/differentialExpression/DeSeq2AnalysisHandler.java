package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author kstaderm
 */
public class DeSeq2AnalysisHandler extends DeAnalysisHandler {

    private DeSeq2 deSeq;
    private final DeSeqAnalysisData deSeqAnalysisData;
    private final UUID key;

    public static enum Plot {

        DispEsts("Per gene estimates against normalized mean expression"),
        DE("Log2 fold change against base means"),
        HIST("Histogram of p values"),
        MAplot("MA Plot");
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
                return new Plot[]{DispEsts, DE, HIST, MAplot};
            }
        }
    }

    public DeSeq2AnalysisHandler(List<PersistantTrack> selectedTracks, Map<String, String[]> design, boolean moreThanTwoConditions,
            List<String> fittingGroupOne, List<String> fittingGroupTwo, Integer refGenomeID, boolean workingWithoutReplicates,
            File saveFile, Set<FeatureType> selectedFeatures, int startOffset, int stopOffset, ParametersReadClasses readClassParams, boolean regardReadOrientation, UUID key) {
        super(selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, regardReadOrientation);
        deSeq = new DeSeq2(this.getRefGenomeID());
        this.key = key;
        deSeqAnalysisData = new DeSeqAnalysisData(selectedTracks.size(),
                design, moreThanTwoConditions, fittingGroupOne, fittingGroupTwo, 
                workingWithoutReplicates);
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<ResultDeAnalysis> results;
        prepareFeatures(deSeqAnalysisData);
        prepareCountData(deSeqAnalysisData, getAllCountData());
        results = deSeq.process(deSeqAnalysisData, getPersAnno().size(), getSelectedTracks().size(), getSaveFile(), key);
        return results;

    }

    public boolean moreThanTwoCondsForDeSeq() {
        return deSeqAnalysisData.moreThanTwoConditions();
    }

    @Override
    public void endAnalysis() {
        deSeq.shutdown(key);
        deSeq = null;
    }

    public File plot(Plot plot) throws IOException, IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile("ReadXplorer_Plot_", ".svg");
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
