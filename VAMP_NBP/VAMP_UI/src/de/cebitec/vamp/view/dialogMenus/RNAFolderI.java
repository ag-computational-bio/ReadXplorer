package de.cebitec.vamp.view.dialogMenus;

/**
 * Interface designed for classes showing an RNAFolder view.
 *
 * @author Rolf Hilker
 */
public interface RNAFolderI {

    /**
     * Creates a new RNAFolder view.
     * @param sequenceToFold the DNA or RNA sequence to fold
     * @param refName the name of the sequence/reference the sequence originates from
     * @param start start position of the sequence to fold
     * @param stop stop position of the sequence to fold
     * @param fwdStrand true if the sequence originates from the fwd strand, false otherwise
     */
    public void showRNAFolderView(String sequenceToFold, String header);
}
