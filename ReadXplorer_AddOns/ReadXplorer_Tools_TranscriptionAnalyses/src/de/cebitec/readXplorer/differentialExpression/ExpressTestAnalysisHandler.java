/* 
 * Copyright (C) 2014 Kai Bernd Stadermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.readXplorer.differentialExpression.expressTest.ExpressTest;
import de.cebitec.readXplorer.differentialExpression.expressTest.ExpressTestI;
import de.cebitec.readXplorer.differentialExpression.expressTest.ExpressTestObserver;
import de.cebitec.readXplorer.differentialExpression.expressTest.ExpressTestStatus;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.util.Exceptions;

/**
 *
 * @author kstaderm
 */
public class ExpressTestAnalysisHandler extends DeAnalysisHandler implements ExpressTestObserver {

    private ExpressTestAnalysisData expressTestAnalysisData;
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

    public ExpressTestAnalysisHandler(List<PersistantTrack> selectedTracks,
            int[] groupA, int[] groupB, Integer refGenomeID, boolean workingWithoutReplicates,
            File saveFile, Set<FeatureType> selectedFeatures, int startOffset, int stopOffset, 
            ParametersReadClasses readClassParams, boolean regardReadOrientation, List<Integer> normalizationFeatures) {
        super(selectedTracks, refGenomeID, saveFile, selectedFeatures, startOffset, stopOffset, readClassParams, regardReadOrientation);
        expressTestAnalysisData = new ExpressTestAnalysisData(selectedTracks.size(),
                groupA, groupB, workingWithoutReplicates, normalizationFeatures);
        expressTestAnalysisData.setSelectedTracks(selectedTracks);
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException, JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException {
        prepareFeatures(expressTestAnalysisData);
        prepareCountData(expressTestAnalysisData, getAllCountData());

        ExpressTestI st = new ExpressTest();


        st.addObserver(this);
        PersistantFeature[] regionNames = expressTestAnalysisData.getFeatures();
        int[] start = expressTestAnalysisData.getStart();
        int[] stop = expressTestAnalysisData.getStop();
        int[] indexA = expressTestAnalysisData.getGroupA();
        int[] indexB = expressTestAnalysisData.getGroupB();
        int[][] groupA = new int[indexA.length][];
        int[][] groupB = new int[indexB.length][];
        int counterIndex = 1;
        int counterA = 0;
        int counterB = 0;
        while (expressTestAnalysisData.hasCountData()) {
            int[] currentCountData = expressTestAnalysisData.pollFirstCountData();
            if (indexA.length > counterA) {
                if (indexA[counterA] == counterIndex) {
                    groupA[counterA] = currentCountData;
                    counterA++;
                }
            }
            if (indexB.length > counterB) {
                if (indexB[counterB] == counterIndex) {
                    groupB[counterB] = currentCountData;
                    counterB++;
                }
            }
            counterIndex++;
        }
        if(expressTestAnalysisData.getNormalizationFeatures() != null){
            st.setNormalizationFeatures(expressTestAnalysisData.getNormalizationFeatures());
        }
        st.performAnalysis(regionNames, start, stop, groupA, groupB, 30d);

        while (results.isEmpty()) {
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return results;
    }

    @Override
    public void update(ExpressTestI origin, ExpressTestStatus status) {
        if (status == ExpressTestStatus.FINISHED) {
            ArrayList<ResultDeAnalysis> tmpRes = new ArrayList<>();
            tmpRes.add(new ResultDeAnalysis(origin.getResults(), origin.getColumnNames(), origin.getRowNames(), "result"));
            tmpRes.add(new ResultDeAnalysis(origin.getResultsNormalized(), origin.getColumnNames(), origin.getRowNames(), "normalized result"));
            results = new ArrayList<>();
            results.addAll(tmpRes);
        }
    }

    @Override
    public void endAnalysis() {
        expressTestAnalysisData = null;
        results = null;
    }
}
