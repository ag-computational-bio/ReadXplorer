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
public class SimpleTestAnalysisHandler extends AnalysisHandler {

    private SimpleTest simpleTest = new SimpleTest();
    private SimpleTestAnalysisData simpleTestAnalysisData;

    public static enum Plot {

        ABvsConf("Ratio A/B against confidence"),
        BAvsConf("Ratio B/A against confidence");
        String representation;

        Plot(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }
    }

    public SimpleTestAnalysisHandler(List<PersistantTrack> selectedTraks,
            int[] groupA, int[] groupB, Integer refGenomeID, boolean workingWithoutReplicates,File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        simpleTestAnalysisData = new SimpleTestAnalysisData(selectedTraks.size(), 
                                        groupA, groupB, workingWithoutReplicates);
        simpleTestAnalysisData.setSelectedTraks(selectedTraks);
    }

    @Override
    public void performAnalysis() throws JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        List<Result> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            prepareFeatures(simpleTestAnalysisData);
            prepareCountData(simpleTestAnalysisData, allCountData);
            results = simpleTest.process(simpleTestAnalysisData, getPersAnno().size(), getSaveFile());
        } else {
            results = simpleTest.process(simpleTestAnalysisData, 3434, getSaveFile());
        }
        setResults(results);
        notifyObservers(AnalysisStatus.FINISHED);
    }

    @Override
    public void endAnalysis() {
        simpleTest.shutdown();
    }

    @Override
    public void saveResultsAsCSV(int selectedIndex, String path) {
        File saveFile = new File(path);
        simpleTest.saveResultsAsCSV(selectedIndex, saveFile);
    }

    public File plot(SimpleTestAnalysisHandler.Plot plot) throws IOException, 
                            IllegalStateException, PackageNotLoadableException {
        File file = File.createTempFile("VAMP_Plot_", ".svg");
        file.deleteOnExit();
        if (plot == SimpleTestAnalysisHandler.Plot.ABvsConf) {
            simpleTest.plotAB(file);
        }
        if (plot == SimpleTestAnalysisHandler.Plot.BAvsConf) {
            simpleTest.plotBA(file);
        }
        return file;
    }
}
