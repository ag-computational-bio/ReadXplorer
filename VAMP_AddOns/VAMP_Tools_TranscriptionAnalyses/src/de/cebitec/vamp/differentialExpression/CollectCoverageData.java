package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.GetMappingsFromTrack;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kstaderm
 */
public class CollectCoverageData{

    private int trackID;
    private List<PersistantAnnotation> genomeAnnotations;
    private Map<Integer, Integer> countData = new HashMap<Integer, Integer>();
    private List<PersistantMapping> mappings;
    private final static int STARTOFFSET = 30;

    public CollectCoverageData(int trackID, PerformAnalysis perfAnalysis) {
        this.genomeAnnotations = perfAnalysis.getPersAnno();
        this.trackID = trackID;
    }

    public Map<Integer, Integer> startCollecting() {
        GetMappingsFromTrack getMappings = new GetMappingsFromTrack();
        mappings = getMappings.loadReducedMappingsByTrackID(trackID);
        Collections.sort(mappings);
        updateReadCountForAnnotations(mappings);
        return countData;
    }

    /**
     * Updates the read count for the annotations with the given mappings. This
     * only works if the list of mappings and the list of annotations are sorted
     * according to the start position.
     *
     * @param mappings the mappings
     */
    private void updateReadCountForAnnotations(List<PersistantMapping> mappings) {
        int lastMappingIdx = 0;
        PersistantAnnotation annotation;
        boolean fstFittingMapping;

        for (int i = 0; i < this.genomeAnnotations.size(); ++i) {
            annotation = this.genomeAnnotations.get(i);
            int featStart = annotation.getStart()-STARTOFFSET;
            int featStop = annotation.getStop();
            int key = annotation.getId();
            fstFittingMapping = true;
            //If no matching mapping is found, we still need to know that by
            //writing down a count of zero for this annotation.
            countData.put(key, 0);

            for (int j = lastMappingIdx; j < mappings.size(); ++j) {
                PersistantMapping mapping = mappings.get(j);

                //mappings identified within a annotation
                if (mapping.getStop() > featStart && annotation.getStrand() == mapping.getStrand()
                        && mapping.getStart() < featStop) {

                    if (fstFittingMapping == true) {
                        lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                    int value = countData.get(key)+1;                    
                    countData.put(key, value);

                    //still mappings left, but need next annotation
                } else if (mapping.getStart() > featStop) {
                    break;
                }
            }
        }
    }
}
