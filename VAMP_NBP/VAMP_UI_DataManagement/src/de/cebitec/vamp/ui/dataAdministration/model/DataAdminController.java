package de.cebitec.vamp.ui.dataAdministration.model;

import de.cebitec.vamp.ui.dataAdministration.View;
import de.cebitec.vamp.ui.dataAdministration.ViewI;
import de.cebitec.vamp.ui.dataAdministration.model.DeletionThread;
import de.cebitec.vamp.ui.dataAdministration.model.Model;
import de.cebitec.vamp.ui.dataAdministration.model.ModelInterface;
import de.cebitec.vamp.ui.dataAdministration.model.ViewListenerI;

/**
 *
 * @author ddoppmeier
 */
public class DataAdminController implements ViewListenerI {

    private ViewI view;
    private ModelInterface model;

    public void showDataAdministration() {

        // setup model and view
        Model m = new Model();
        model = m;
        View v = new View();
        view = v;

        // register listeners for view and model
        view.addDataAdminViewListenerI(this);
        view.addDataAdminJobManager(m);

        model.addListener(v);
        model.fetchNecessaryData();
        
        // show the view
        view.setVisible(true);
    }

    @Override
    public void cancelDataAdmin() {
        view.setVisible(false);

        view = null;
        model = null;
    }

    @Override
    public void startDeletion() {
        view.startingDeletion();
        DeletionThread t = new DeletionThread(this, model.getScheduledRefGenJobs(), model.getScheduledTrackJobsRun());

//        ApplicationController.getInstance().addRunningTask(t);
        t.execute();
    }

    public void deletionDone(Object runningJob) {
//        ApplicationController.getInstance().removeRunningTask(runningJob);
        view.deletionFinished();
    }

    public void updateDataAdminStatus(String message) {
        view.updateDeletionStatus(message);
    }

}
