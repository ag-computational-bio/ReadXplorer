package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.databackend.ParameterSetI;
import de.cebitec.vamp.databackend.ParametersFeatureTypes;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.util.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with a SNP and DIP detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ParameterSetSNPs extends ParametersFeatureTypes implements ParameterSetI<ParameterSetSNPs> {
    private int minMismatchBases;
    private int minPercentage;
    private final boolean useMainBase;
    private ParametersReadClasses readClassParams;

    /**
     * Data storage for all parameters associated with a SNP and DIP detection.
     * @param minMismatchBases the minimum number of mismatches at a SNP position 
     * @param minPercentage the minimum percentage of mismatches at a SNP position
     * @param useMainBase <cc>true</cc>, if the minVaryingBases count corresponds to the count of
     * the most frequent base at the current position. <cc>false</cc>, if the 
     * minVaryingBases count corresponds to the overall mismatch count at the
     * current position.
     * @param selFeatureTypes list of seletect feature types to use for the 
     * snp translation.
     * @param readClassParams only include mappings in the analysis, which 
     * belong to the selected mapping classes.
     */
    public ParameterSetSNPs(int minMismatchBases, int minPercentage, boolean useMainBase, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams) {
        super(selFeatureTypes);
        this.minMismatchBases = minMismatchBases;
        this.minPercentage = minPercentage;
        this.useMainBase = useMainBase;
        this.readClassParams = readClassParams;
    }

    /**
     * @return the minimum number of mismatches at a SNP position 
     */
    public int getMinMismatchingBases() {
        return minMismatchBases;
    }

    public void setMinVaryingBases(int minVaryingBases) {
        this.minMismatchBases = minVaryingBases;
    }

    /**
     * @return the minimum percentage of mismatches at a SNP position
     */
    public int getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(int minPercentage) {
        this.minPercentage = minPercentage;
    }

    /**
     * @return only include mappings in the analysis, which 
     * belong to the selected mapping classes.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }

    public void setReadClassParams(ParametersReadClasses readClassParams) {
        this.readClassParams = readClassParams;
    }

    /**
     * @return <cc>true</cc>, if the minVaryingBases count corresponds to the count of
     * the most frequent base at the current position. <cc>false</cc>, if the 
     * minVaryingBases count corresponds to the overall mismatch count at the
     * current position.
     */
    boolean isUseMainBase() {
        return this.useMainBase;
    }
    
}
