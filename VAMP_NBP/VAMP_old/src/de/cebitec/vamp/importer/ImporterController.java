package de.cebitec.vamp.importer;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.ApplicationController;
import de.cebitec.vamp.RunningTaskI;
import de.cebitec.vamp.importer.ImporterDataModelI;
import de.cebitec.vamp.importer.ImporterDataModelI;
import de.cebitec.vamp.importer.ImporterDataModelI;
import de.cebitec.vamp.importer.ImporterJobManager;
import de.cebitec.vamp.importer.ImporterJobManager;
import de.cebitec.vamp.importer.ImporterJobManager;
import de.cebitec.vamp.importer.ImporterViewListenerI;
import de.cebitec.vamp.importer.ImporterViewListenerI;
import de.cebitec.vamp.importer.ImporterViewListenerI;
import de.cebitec.vamp.view.importer.ImporterViewFrame;
import de.cebitec.vamp.view.importer.ImporterViewI;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ImporterController implements ImporterViewListenerI {

    ImporterViewI view;
    ImporterDataModelI job;

    public ImporterController(){

    }

    public void setupNewImport(){
        // concrete type of objects has to be created first,
        // because it is used and interpreted as two differen interfaces
        ImporterViewFrame v = new ImporterViewFrame();
        view = v;
        ImporterJobManager d = new ImporterJobManager();
        job = d;

        view.addImporterViewListener(this);
        view.setJobManager(d);
        view.setVisible(true);

        job.addTaskListener(v);
    }

    @Override
    public void startImport() {

        view.startingImport();

       //List<RunJob> runs = job.getRunJobList();
        List<ReferenceJob> refgens = job.getRefGenJobList();
        List<TrackJobs> tracksRun = job.getTrackJobListRun();
        ImportThread i = new ImportThread(this, refgens, tracksRun);
        ApplicationController.getInstance().addRunningTask(i);

        i.execute();
    }

    @Override
    public void cancelImport(){
        view.setVisible(false);
        view = null;
        job = null;
    }

    public void importDone(RunningTaskI runningTask) {
        view.importFinished();
        ApplicationController.getInstance().removeRunningTask(runningTask);
    }

    public void updateImportStatus(String string) {
        view.udateImportStatus(string);
    }

}
