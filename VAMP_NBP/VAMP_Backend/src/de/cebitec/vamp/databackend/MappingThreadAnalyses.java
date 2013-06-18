package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Properties;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This mapping thread should be used for analyses, but not for visualizing
 * data. The thread carries out all database querries or file access to receive
 * the mappings for a certain interval.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class MappingThreadAnalyses extends MappingThread {

    /**
     * Creates a new mapping thread for carrying out mapping request either to a
     * database or a file.
     *
     * @param track the track for which this mapping thread is created
     */
    public MappingThreadAnalyses(PersistantTrack track) {
        super(track);
    }

    @Override
    public void run() {

        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            List<PersistantMapping> currentMappings;
            if (request != null) {
                if (request.getDesiredData() == Properties.MAPPINGS_W_DIFFS) {
                    currentMappings = this.loadMappingsWithDiffs(request);
                } else if (request.getDesiredData() == Properties.MAPPINGS_WO_DIFFS) {
                    currentMappings = this.loadMappingsWithoutDiffs(request);
                } else if (request.getDesiredData() == Properties.REDUCED_MAPPINGS) {
                    currentMappings = this.loadReducedMappings(request);
                } else {
                    currentMappings = this.loadMappingsById(request);
                }
                request.getSender().receiveData(new MappingResultPersistant(currentMappings, request.getFrom(), request.getTo()));

            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoverageThreadAnalyses.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
