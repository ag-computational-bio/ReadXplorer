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
     * @param tracks the list of tracks for which this mapping thread is created
     */
    public MappingThreadAnalyses(List<PersistantTrack> tracks) {
        super(tracks);
    }

    @Override
    public void run() {

        while (!interrupted()) {

            IntervalRequest request = requestQueue.poll();
            List<PersistantMapping> currentMappings;
            if (request != null) {
                if (request.getDesiredData() == Properties.MAPPINGS_DB_BY_ID) {
                    currentMappings = this.loadMappingsById(request);
                } else if (request.getDesiredData() == Properties.REDUCED_MAPPINGS) {
                    currentMappings = this.loadReducedMappings(request);
                } else {
                    currentMappings = this.loadMappings(request);
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
