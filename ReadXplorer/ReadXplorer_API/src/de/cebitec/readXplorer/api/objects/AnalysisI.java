package de.cebitec.readXplorer.api.objects;

/**
 * @author -Rolf Hilker-
 * 
 * Interface to use for any kind of analysis, which can take advantage of using
 * the given methods.
 */
public interface AnalysisI<T> {    
    
    /**
     * @return Returns the results of the analysis.
     */
    public T getResults();
    
    
    
}
