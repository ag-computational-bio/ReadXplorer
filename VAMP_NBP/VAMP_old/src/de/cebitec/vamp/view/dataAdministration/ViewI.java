package de.cebitec.vamp.view.dataAdministration;

import de.cebitec.vamp.dataAdministration.JobManager;
import de.cebitec.vamp.dataAdministration.ViewListenerI;

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
