package de.cebitec.vamp.databackend;

/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of VAMP's coverage classes and if only uniquely mapped reads shall be used,
 * or all reads.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParametersReadClasses {
    
    private boolean usePerfectMatch; 
    private boolean useBestMatch;
    private boolean useCommonMatch;
    private boolean useOnlyUniqueReads;

    /**
     * Creates a parameter set which contains all parameters concerning the 
     * usage of VAMP's coverage classes and if only uniquely mapped reads shall
     * be used, or all reads.
     * @param usePerfectMatch <cc>true</cc>, if the perfect match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useBestMatch <cc>true</cc>, if the best match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useCommonMatch cc>true</cc>, if the common match class used in
     * this parameter set, <cc>false</cc> otherwise
     * @param useOnlyUniqueReads <cc>true</cc>, if only uniquely mapped reads
     * are used in this parameter set, <cc>false</cc> otherwise
     */
    public ParametersReadClasses(boolean usePerfectMatch, boolean useBestMatch, boolean useCommonMatch, boolean useOnlyUniqueReads) {
        this.usePerfectMatch = usePerfectMatch;
        this.useBestMatch = useBestMatch;
        this.useCommonMatch = useCommonMatch;
        this.useOnlyUniqueReads = useOnlyUniqueReads;
    }

    /**
     * Constructor with standard values. All read classes and all reads are 
     * included here.
     */
    public ParametersReadClasses() {
        this(true, true, true, false);
    }

    public boolean isPerfectMatchUsed() {
        return usePerfectMatch;
    }

    public boolean isBestMatchUsed() {
        return useBestMatch;
    }

    public boolean isCommonMatchUsed() {
        return useCommonMatch;
    }

    public boolean isOnlyUniqueReads() {
        return useOnlyUniqueReads;
    }
}
