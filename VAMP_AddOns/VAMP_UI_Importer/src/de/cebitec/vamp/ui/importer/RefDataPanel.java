package de.cebitec.vamp.ui.importer;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.util.VisualisationUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

/**
 * An ordinary JPanel containing the list of references present in the currently
 * opened VAMP DB. It allows to retrieve them as a list.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class RefDataPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 
     * @param jobs list of reference jobs which shall be imported now and thus
     * have to be available for the import of new tracks too.
     * @return Complete list of reference jobs in the db and which are imported now
     */
    public ReferenceJob[] getReferenceJobs(List<ReferenceJob> jobs) {
        List<ReferenceJob> refJobList = this.getRefJobList();

        refJobList.addAll(jobs);

        ReferenceJob[] references = new ReferenceJob[1];
        references = refJobList.toArray(references);
        
        return references;
    }

    /**
     * @return all reference genomes which are stored in the db until now.
     */
    public ReferenceJob[] getRefGenJobs() {
        List<ReferenceJob> refJobList = this.getRefJobList();
        
        ReferenceJob[] references = new ReferenceJob[1];
        references = refJobList.toArray(references);

        return references;
    }
    
    /**
     * @return the list of reference jobs stored in the current db
     */
    private List<ReferenceJob> getRefJobList() {
        List<ReferenceJob> refJobList = new ArrayList<>();

        try {
            List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
            for (Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext();) {
                PersistantReference r = it.next();
                refJobList.add(new ReferenceJob(r.getId(), null, null, r.getDescription(), r.getName(), r.getTimeStamp()));
            }
        } catch (OutOfMemoryError e) {
            VisualisationUtils.displayOutOfMemoryError(this);
        }
        return refJobList;
    }
    
}
