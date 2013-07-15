package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.databackend.ParameterSetI;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.util.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with a SNP and DIP detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ParameterSetSNPs implements ParameterSetI<ParameterSetSNPs> {
    private int minVaryingBases;
    private int minPercentage;
    private Set<FeatureType> selFeatureTypes;
    private ParametersReadClasses readClassParams;

    /**
     * Data storage for all parameters associated with a SNP and DIP detection.
     * @param minVaryingBases
     * @param minPercentage
     * @param selFeatureTypes
     * @param readClassParams 
     */
    public ParameterSetSNPs(int minVaryingBases, int minPercentage, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams) {
        this.minVaryingBases = minVaryingBases;
        this.minPercentage = minPercentage;
        this.selFeatureTypes = selFeatureTypes;
        this.readClassParams = readClassParams;
    }

    public int getMinVaryingBases() {
        return minVaryingBases;
    }

    public void setMinVaryingBases(int minVaryingBases) {
        this.minVaryingBases = minVaryingBases;
    }

    public int getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(int minPercentage) {
        this.minPercentage = minPercentage;
    }

    public Set<FeatureType> getSelFeatureTypes() {
        return selFeatureTypes;
    }

    public void setSelFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }

    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }

    public void setReadClassParams(ParametersReadClasses readClassParams) {
        this.readClassParams = readClassParams;
    }
    
}
