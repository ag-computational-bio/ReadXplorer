package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.IntervalRequest;
import de.cebitec.vamp.databackend.MappingThreadAnalyses;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collect the coverage data for a given track.
 *
 * @author kstaderm
 */
public class CollectCoverageData implements ThreadListener {

    /**
     * The ID of the track the coverage data should be collected for.
     */
    private PersistantTrack track;
    /**
     * The whole sete of annotations for the current genome.
     */
    private List<PersistantAnnotation> genomeAnnotations;
    /**
     * The storage holding the collected coverage data, also named count data.
     * The Key value of this HashMap is the ID of the annotation. The value
     * value represents the corresponding number of counted coverage data.
     */
    private Map<Integer, Integer> countData = new HashMap<>();
    /**
     * The whole mappings for the track with the given track ID.
     */
    private List<PersistantMapping> mappings = null;
    /**
     * Adjusts how many bases downstream from the start position of an
     * annotation a mapping should still be considered a hit. The annotation in
     * the database are manly CDS positions. So it is normal that a lot of
     * mappings will start in an are downstream of the start position of the
     * annotation.
     */
    private final static int STARTOFFSET = 30;

    /**
     * Constructor of the class.
     *
     * @param trackID The ID of the track the instance of this class should
     * collect the coverage data for
     * @param perfAnalysis Instance of the calling instance of AnalysisHandler.
     */
    public CollectCoverageData(PersistantTrack track, AnalysisHandler perfAnalysis) {
        this.genomeAnnotations = perfAnalysis.getPersAnno();
        this.track = track;
    }

    /**
     * Starts collecting the coverage Data.
     *
     * @return a Map containig the coverage data for the track provided to the
     * instance of this class at creation time.
     */
    public Map<Integer, Integer> startCollecting() {
        MappingThreadAnalyses analyses = new MappingThreadAnalyses(track);
        analyses.start();
        analyses.addRequest(new IntervalRequest(0, 0, this, Properties.REDUCED_MAPPINGS));
        //TODO:Remove this ugly hot fix and use recalls instead.
        while(mappings==null){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }
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
            int featStart = annotation.getStart() - STARTOFFSET;
            int featStop = annotation.getStop();
            int key = annotation.getId();
            fstFittingMapping = true;
            //If no matching mapping is found, we still need to know that by
            //writing down a count of zero for this annotation.
            countData.put(key, 0);

            for (int j = lastMappingIdx; j < mappings.size(); ++j) {
                PersistantMapping mapping = mappings.get(j);

                //mappings identified within a annotation
                if (mapping.getStop() > featStart && annotation.isFwdStrand() == mapping.isFwdStrand()
                        && mapping.getStart() < featStop) {

                    if (fstFittingMapping == true) {
                        lastMappingIdx = j;
                        fstFittingMapping = false;
                    }
                    int value = countData.get(key) + 1;
                    countData.put(key, value);

                    //still mappings left, but need next annotation
                } else if (mapping.getStart() > featStop) {
                    break;
                }
            }
        }
    }

    @Override
    public void receiveData(Object data) {
        MappingResultPersistant results = (MappingResultPersistant) data;
        mappings = results.getMappings();
    }
}
