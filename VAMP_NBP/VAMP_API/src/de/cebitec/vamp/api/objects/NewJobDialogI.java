package de.cebitec.vamp.api.objects;

/**
 * Interface for all dialogs that create new jobs and need some required info set
 * before they can finish successfully.
 *
 * @author jwinneba
 */
public interface NewJobDialogI {

    /**
     * @return true, if all required info for this job dialog is set, false otherwise.
     */
    public boolean isRequiredInfoSet();

}
