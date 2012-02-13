package de.cebitec.vamp.ui.importer;

/**
 * Interface for all dialogs that create new jobs and need some required info set
 * before they can finish successfully.
 *
 * @author jwinneba
 */
public interface NewJobDialogI {

    public boolean isRequiredInfoSet();

}
