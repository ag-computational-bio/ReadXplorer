package de.cebitec.readXplorer.databackend;

/**
 *
 * @author ddoppmeier, rhilker
 */
public interface ThreadListener {

    public void receiveData(Object data);
    
    public void notifySkipped();
}
