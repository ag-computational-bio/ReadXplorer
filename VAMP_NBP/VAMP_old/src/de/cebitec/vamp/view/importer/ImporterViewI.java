package de.cebitec.vamp.view.importer;

import de.cebitec.vamp.importer.ImporterViewListenerI;
import de.cebitec.vamp.importer.JobManagerI;

/**
 *
 * @author ddoppmeier
 */
public interface ImporterViewI {

    public void addImporterViewListener(ImporterViewListenerI listener);

    public void removeImporterViewListener(ImporterViewListenerI listener);

    public void setJobManager(JobManagerI jobManager);

    public void removeJobManager(JobManagerI jobManager);

    public void importFinished();

    public void startingImport();

    public void setVisible(boolean isVisible);

    public void udateImportStatus(String actualMessage);

}
