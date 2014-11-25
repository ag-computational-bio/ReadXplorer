package de.cebitec.readXplorer.view.dialogMenus;

/**
 * Interface designed for classes showing an RNAFolder view.
 *
 * @author Rolf Hilker
 */
public interface RNAFolderI {

    /**
     * Creates a new RNAFolder view.
     * @param sequenceToFold the DNA or RNA sequence to fold
     * @param header the description of the folded sequence to use as header
     */
    public void showRNAFolderView(String sequenceToFold, String header);
}
