package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface LinearRegressionI {

    /**
     * This method is called to start the analysis.
     * As a parameter the continuous count data is handed over. The data
     * structure is build as follows:
     * Map<ConditionName, Map<GeneName, List<CountValueForEachSingleNucleotideOfTheGene>>>;
     * 
     * @param countData The count data for each gene.
     */
    public void process(Map<Integer, Map<PersistentFeature, int[]>> countData);

    /**
     * Adds an observer that is notified when the calculation is finished.
     * 
     * @param o The observer that should be added.
     */
    public void addObserver(Observer o);

    /**
     * Removes an observer from the list of observers that are notified when the calculation is finished.
     * 
     * @param o The observer that should be remove.
     */
    public void removeObserver(Observer o);
    
    /**
     * Returns the result of the analysis run.
     * 
     * @return A Map containing gene names as key and the similarity score as value.
     * @throws IllegalStateException If the method is called before the calculation is finished.
     */
    public Map<PersistentFeature, double[]> getResults() throws IllegalStateException;

}
