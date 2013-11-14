package de.cebitec.readXplorer.view.dataVisualisation.trackViewer;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jstraube
 */
public class NormalizationSettings {

    
    public HashMap<Integer, Double[]> idToValue;

    
    public NormalizationSettings(List<Integer> trackIDs, List<Boolean> isLogNormList, List<Double> factors, List<Boolean> hasNormFactor) {
        idToValue = new HashMap<Integer, Double[]>();
        int j = 0;
        for (int i : trackIDs) {
            Double[] d = new Double[3];
            d[0] = isLogNormList.get(j) ? 1.0 : 0.0;
            d[1] = factors.get(j);
            d[2] = hasNormFactor.get(j) ? 1.0 : 0.0;
            idToValue.put(i, d);
            j++;
        }
    }

    
    public HashMap<Integer, Double[]> getIdToValue() {
        return idToValue;
    }

    
    public void setIdToValue(HashMap<Integer, Double[]> idToValue) {
        this.idToValue = idToValue;
    }

    
    public double getFactors(int trackID) {
        return idToValue.get(trackID)[1];
    }

    
    public void setFactors(double factors, int trackID) {
        Double[] d = idToValue.get(trackID);
        d[1] = factors;
        idToValue.put(trackID, d);
    }

    
    public Boolean getIsLogNorm(int trackID) {
        return idToValue.get(trackID)[0] == 1.0 ? true : false;
    }

    
    public void setIsLogNorm(Boolean isLogNorm, int trackID) {
        Double[] d = idToValue.get(trackID);
        d[0] = (isLogNorm ? 1.0 : 0.0);
        idToValue.put(trackID, d);
    }

    
    public Boolean getHasNormFac(int trackID) {
        return idToValue.get(trackID)[2] == 1.0 ? true : false;
    }

    
    public void setHasNormFac(Boolean hasNorm, int trackID) {
        Double[] d = idToValue.get(trackID);
        d[2] = (hasNorm ? 1.0 : 0.0);
        idToValue.put(trackID, d);
    }
}