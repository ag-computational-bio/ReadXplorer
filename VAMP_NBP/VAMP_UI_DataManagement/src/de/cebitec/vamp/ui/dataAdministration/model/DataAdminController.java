package de.cebitec.vamp.ui.dataAdministration.model;

import de.cebitec.vamp.ui.dataAdministration.DeletionThread;
import de.cebitec.vamp.ui.dataAdministration.ViewI;

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
//        View v = new View();
//        view = v;

        // register listeners for view and model
        view.addDataAdminViewListenerI(this);
        view.addDataAdminJobManager(m);

        model.addListener((ModelListenerI) view);
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
        DeletionThread t = new DeletionThread(model.getScheduledRefGenJobs(), model.getScheduledTrackJobsRun());

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
