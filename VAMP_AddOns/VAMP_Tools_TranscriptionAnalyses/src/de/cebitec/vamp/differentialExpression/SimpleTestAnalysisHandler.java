package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.vamp.differentialExpression.simpleTest.SimpleTestI;
import de.cebitec.vamp.differentialExpression.simpleTest.SimpleTestObserver;
import de.cebitec.vamp.differentialExpression.simpleTest.SimpleTestStatus;
import de.cebitec.vamp.util.FeatureType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kstaderm
 */
public class SimpleTestAnalysisHandler extends DeAnalysisHandler implements SimpleTestObserver {

    private SimpleTest simpleTest = new SimpleTest();
    private SimpleTestAnalysisData simpleTestAnalysisData;
    private List<ResultDeAnalysis> results;

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
            int[] groupA, int[] groupB, Integer refGenomeID, boolean workingWithoutReplicates,
            File saveFile, List<FeatureType> selectedFeatures, int startOffset, int stopOffset, 
            ParametersReadClasses readClassParams, boolean regardReadOrientation) {
        super(selectedTraks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, regardReadOrientation);
        simpleTestAnalysisData = new SimpleTestAnalysisData(selectedTraks.size(),
                groupA, groupB, workingWithoutReplicates);
        simpleTestAnalysisData.setSelectedTraks(selectedTraks);
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        prepareFeatures(simpleTestAnalysisData);
        prepareCountData(simpleTestAnalysisData, getAllCountData());
        results = simpleTest.process(simpleTestAnalysisData, getPersAnno().size(), getSaveFile());
//
//        SimpleTestI st = new de.cebitec.vamp.differentialExpression.simpleTest.SimpleTest();
//
//
//        st.addObserver(this);
//        PersistantFeature[] regionNames = simpleTestAnalysisData.getFeatures();
//        int[] start = simpleTestAnalysisData.getStart();
//        int[] stop = simpleTestAnalysisData.getStop();
//        int[] indexA = simpleTestAnalysisData.getGroupA();
//        int[] indexB = simpleTestAnalysisData.getGroupB();
//        int[][] groupA = new int[indexA.length][];
//        int[][] groupB = new int[indexB.length][];
//        int counterIndex = 1;
//        int counterA = 0;
//        int counterB = 0;
//        while (simpleTestAnalysisData.hasCountData()) {
//            int[] currentCountData = simpleTestAnalysisData.pollFirstCountData();
//            if (indexA.length > counterA) {
//                if (indexA[counterA] == counterIndex) {
//                    groupA[counterA] = currentCountData;
//                    counterA++;
//                }
//            }
//            if (indexB.length > counterB) {
//                if (indexB[counterB] == counterIndex) {
//                    groupB[counterB] = currentCountData;
//                    counterB++;
//                }
//            }
//            counterIndex++;
//        }
//        st.performAnalysis(regionNames, start, stop, groupA, groupB, 30d);
//
//        while (results.isEmpty()) {
//            try {
//                sleep(500);
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

        return results;
    }

    @Override
    public void update(SimpleTestI origin, SimpleTestStatus status) {
        if (status == SimpleTestStatus.FINISHED) {
            ArrayList<ResultDeAnalysis> tmpRes = new ArrayList<>();
            tmpRes.add(new ResultDeAnalysis(origin.getResults(), origin.getColumnNames(), origin.getRowNames(), "result"));
            tmpRes.add(new ResultDeAnalysis(origin.getResultsNormalized(), origin.getColumnNames(), origin.getRowNames(), "normalized result"));
            results = new ArrayList<>();
            results.addAll(tmpRes);
        }
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
