package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisHandler extends AnalysisHandler {

    private List<String[]> design;
    private DeSeq deSeq = new DeSeq();
    private boolean workingWithoutReplicates;

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
    }

    public DeSeqAnalysisHandler(List<PersistantTrack> selectedTraks, List<String[]> design, Integer refGenomeID, boolean workingWithoutReplicates, File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        this.design = design;
        this.workingWithoutReplicates = workingWithoutReplicates;
    }

    @Override
    public void performAnalysis() {
        List<RVector> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            DeSeqAnalysisData deSeqAnalysisData = new DeSeqAnalysisData(getSelectedTraks().size(), this.design, this.workingWithoutReplicates);
            prepareAnnotations(deSeqAnalysisData);
            prepareCountData(deSeqAnalysisData, allCountData);
            results = deSeq.process(deSeqAnalysisData, getPersAnno().size(), getSelectedTraks().size(), getSaveFile());
        } else {
            results = deSeq.process(null, 3232, getSelectedTraks().size(), getSaveFile());
        }

        setResults(convertRresults(results));
        notifyObservers(this);
    }

    private List<Object[][]> convertRresults(List<RVector> results) {
        List<Object[][]> ret = new ArrayList<>();
        for (Iterator<RVector> it = results.iterator(); it.hasNext();) {
            RVector currentRVector = it.next();
            int i = 0;
            Object[][] current = new Object[currentRVector.at(1).asDoubleArray().length][currentRVector.size()];
            String[] currentStringValues = currentRVector.at(i).asStringArray();
            for (int j = 0; j < currentStringValues.length; j++) {
                current[j][i] = currentStringValues[j];
            }
            i++;
            for (; i < currentRVector.size(); i++) {
                double[] currentDoubleValues = currentRVector.at(i).asDoubleArray();
                for (int j = 0; j < currentDoubleValues.length; j++) {
                    current[j][i] = currentDoubleValues[j];
                }
            }
            ret.add(current);
        }
        return ret;
    }

    @Override
    public void endAnalysis() {
        deSeq.shutdown();
        deSeq = null;
    }

    void saveResultsAsCSV(int selectedIndex, String path) {
        File saveFile = new File(path);
        deSeq.saveResultsAsCSV(selectedIndex, saveFile);
    }

    File plot(Plot plot) throws IOException {
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
