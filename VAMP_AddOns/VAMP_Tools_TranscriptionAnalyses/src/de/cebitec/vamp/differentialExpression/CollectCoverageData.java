package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.util.Observer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collect the coverage data for a given track.
 *
 * @author kstaderm
 */
public class CollectCoverageData implements Observer {

    /**
     * The whole sete of features for the current genome.
     */
    private List<PersistantFeature> genomeFeatures;
    /**
     * The storage holding the collected coverage data, also named count data.
     * The Key value of this HashMap is the ID of the feature. The value value
     * represents the corresponding number of counted coverage data.
     */
    private Map<Integer, Integer> countData = new HashMap<>();
    /**
     * Adjusts how many bases downstream from the start position of an feature a
     * mapping should still be considered a hit. The feature in the database are
     * manly CDS positions. So it is normal that a lot of mappings will start in
     * an are downstream of the start position of the feature.
     */
    private int startOffset;
    private int stopOffset;

    /**
     * Constructor of the class.
     *
     * @param trackID The ID of the track the instance of this class should
     * collect the coverage data for
     * @param perfAnalysis Instance of the calling instance of
     * DeAnalysisHandler.
     */
    public CollectCoverageData(List<PersistantFeature> genomeFeatures, int startOffset, int stopOffset) {
        this.genomeFeatures = genomeFeatures;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
    }

    /**
     * Updates the read count for the features with the given mappings.
     *
     * @param mappings the mappings
     */
    private void updateReadCountForFeatures(List<PersistantMapping> mappings) {
        Collections.sort(mappings);
        Collections.sort(genomeFeatures);
        int lastMappingIdx = 0;
        PersistantFeature feature;
        boolean fstFittingMapping;

        for (int i = 0; i < this.genomeFeatures.size(); ++i) {
            feature = this.genomeFeatures.get(i);
            int featStart = feature.getStart() - startOffset;
            int featStop = feature.getStop() + stopOffset;
            int key = feature.getId();
            fstFittingMapping = true;
            //If no matching mapping is found, we still need to know that by
            //writing down a count of zero for this feature.
            if (!countData.containsKey(key)) {
                countData.put(key, 0);
            }
            for (int j = lastMappingIdx; j < mappings.size(); ++j) {
                PersistantMapping mapping = mappings.get(j);

                //mappings identified within a feature
                if (mapping.getStop() > featStart && feature.isFwdStrand() == mapping.isFwdStrand()
                        && mapping.getStart() < featStop) {

                    if (fstFittingMapping == true) {
                        lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                    int value = countData.get(key) + 1;
                    countData.put(key, value);

                    //still mappings left, but need next feature
                } else if (mapping.getStart() > featStop) {
                    break;
                }
            }
        }
    }

    @Override
    public void update(Object args) {
        if (args instanceof MappingResultPersistant) {
            MappingResultPersistant results = (MappingResultPersistant) args;
            updateReadCountForFeatures(results.getMappings());
        }
    }

    public Map<Integer, Integer> getCountData() {
        return countData;
    }
}
