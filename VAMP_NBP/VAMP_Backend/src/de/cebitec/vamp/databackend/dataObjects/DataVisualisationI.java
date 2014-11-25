package de.cebitec.vamp.databackend.dataObjects;

/**
 * Interface for methods commonly used for data visualization.
 * 
 * @author -Rolf Hilker-
 */
public interface DataVisualisationI {
    
    /**
     * Visualizes the data handed over to this method as defined by the implementation.
     * @param data the data object to visualize.
     */
    public void showData(Object data);
    
}
