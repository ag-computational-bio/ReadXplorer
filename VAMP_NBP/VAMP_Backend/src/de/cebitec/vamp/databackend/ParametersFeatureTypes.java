package de.cebitec.vamp.databackend;

import de.cebitec.vamp.util.FeatureType;
import java.util.Set;

/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's feature types.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersFeatureTypes {
    private Set<FeatureType> selFeatureTypes;

    /**
     * Creates a parameters set which contains all parameters concerning the
     * usage of ReadXplorer's feature types.
     * @param selFeatureTypes the set of selected feature types 
     */
    public ParametersFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }

    /**
     * @return the set of selected feature types 
     */
    public Set<FeatureType> getSelFeatureTypes() {
        return selFeatureTypes;
    }

    /**
     * @param selFeatureTypes the set of selected feature types 
     */
    public void setSelFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }
}
