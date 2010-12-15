package de.cebitec.vamp.ui.dataAdministration;

import de.cebitec.vamp.parser.JobManager;
import de.cebitec.vamp.ui.dataAdministration.model.ViewListenerI;

/**
 *
 * @author ddoppmeier
 */
public interface ViewI  {

    public void setVisible(boolean isVisible);

    public void addDataAdminViewListenerI(ViewListenerI listener);

    public void removeDataAdminViewListenerI(ViewListenerI listener);
    
    public void addDataAdminJobManager(JobManager jobmanager);

    public void startingDeletion();

    public void deletionFinished();

    public void updateDeletionStatus(String message);
}
