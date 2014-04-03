package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public class ExportOnlyAnalysisHandler extends DeAnalysisHandler {

    private DeAnalysisData data;
    private List<ResultDeAnalysis> results;

    public ExportOnlyAnalysisHandler(List<PersistantTrack> selectedTracks, int refGenomeID, File saveFile, Set<FeatureType> selectedFeatureTypes, int startOffset, int stopOffset, ParametersReadClasses readClassParams, boolean regardReadOrientation) {
        super(selectedTracks, refGenomeID, saveFile, selectedFeatureTypes, startOffset, stopOffset, readClassParams, regardReadOrientation);
        data = new DeAnalysisData(selectedTracks.size());
        data.setSelectedTracks(selectedTracks);
    }

    @Override
    protected List<ResultDeAnalysis> processWithTool() throws GnuR.PackageNotLoadableException, GnuR.JRILibraryNotInPathException, IllegalStateException, GnuR.UnknownGnuRException {
        prepareFeatures(data);
        prepareCountData(data, getAllCountData());
        results = new ArrayList<>();
        String[] featureNames = data.getFeatureNames();
        String[] trackDescriptions = data.getTrackDescriptions();
        int[][] countData = new int[data.getSelectedTracks().size()][];
        List<String> regionNamesList = new ArrayList<>();
        int i = 0;
        while (data.hasCountData()) {
            countData[i++] = data.pollFirstCountData();
        }
        Vector<Vector> tableContents = new Vector<>();
        for (i = 0; i < data.getFeatures().length; i++) {
            boolean allZero = true;
            Integer[] tmp = new Integer[data.getSelectedTracks().size()];
            for (int j = 0; j < data.getSelectedTracks().size(); j++) {
                int value = countData[j][i];
                if (value != 0) {
                    allZero = false;
                }
                tmp[j] = value;
            }
            if (!allZero) {
                tableContents.add(new Vector(Arrays.asList(tmp)));
                regionNamesList.add(featureNames[i]);
            }
        }
        Vector colNames = new Vector(Arrays.asList(trackDescriptions));
        Vector rowNames = new Vector(regionNamesList);

        results.add(new ResultDeAnalysis(tableContents, colNames, rowNames, "Count Data Table"));
        return results;
    }

    @Override
    public void endAnalysis() {
        data = null;
        results = null;
    }

}
