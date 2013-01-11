package de.cebitec.vamp.databackend.dataObjects;

/**
 * Data storage for SNP detection result metrics / statistics.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpResultStatistics {
    
    //functional stats
    private int totalNoSnps;
    private int noIntergenicSnps;
    private int noSynonymousSnps;
    private int noMissenseSnps;
    private int noChemicallyNeutralSnps;
    private int noAAInsertions;
    private int noAADeletions ;
    
    //snp type stats
    private int noSubstitutions;
    private int noInsertions;
    private int noDeletions ;

    /**
     * @return the total number of snps 
     */
    public int getTotalNoSnps() {
        return this.totalNoSnps;
    }

    /**
     * set the total number of snps
     * @param totalNoSnps the total number of snps
     */
    public void setTotalNoSnps(int totalNoSnps) {
        this.totalNoSnps = totalNoSnps;
    }

    /**
     * @return the number of intergenic snps
     */
    public int getNoIntergenicSnps() {
        return this.noIntergenicSnps;
    }

    /**
     * set the number of intergenic snps
     * @param noIntergenicSnps the number of intergenic snps
     */
    public void setNoIntergenicSnps(int noIntergenicSnps) {
        this.noIntergenicSnps = noIntergenicSnps;
    }

    /**
     * @return the number of synonymous snps
     */
    public int getNoSynonymousSnps() {
        return this.noSynonymousSnps;
    }

    /**
     * set the number of synonymous snps
     * @param noSynonymousSnps the number of synonymous snps
     */
    public void setNoSynonymousSnps(int noSynonymousSnps) {
        this.noSynonymousSnps = noSynonymousSnps;
    }

    /**
     * @return the number of missense mutation snps
     */
    public int getNoMissenseSnps() {
        return this.noMissenseSnps;
    }

    /**
     * set the number of missense mutation snps
     * @param noMissenseSnps the number of missense mutation snps
     */
    public void setNoMissenseSnps(int noMissenseSnps) {
        this.noMissenseSnps = noMissenseSnps;
    }

    /**
     * @return the number of chemically neutral snps
     */
    public int getNoChemicallyNeutralSnps() {
        return this.noChemicallyNeutralSnps;
    }

    /**
     * set the number of chemically neutral snps
     * @param noChemicallyNeutralSnps the number of chemically neutral snps
     */
    public void setNoChemicallyNeutralSnps(int noChemicallyNeutralSnps) {
        this.noChemicallyNeutralSnps = noChemicallyNeutralSnps;
    }

    /**
     * @return the number of snps causing a frame shift by an insertion
     */
    public int getNoAAInsertions() {
        return this.noAAInsertions;
    }

    /**
     * sets the number of snps causing a frame shift by an insertion
     * @param noAAInsertions the number of snps causing a frame shift by an
     * insertion
     */
    public void setNoAAInsertions(int noAAInsertions) {
        this.noAAInsertions = noAAInsertions;
    }

    /**
     * @return the number of snps causing a frame shift by a deletion
     */
    public int getNoAADeletions() {
        return this.noAADeletions;
    }

    /**
     * sets the number of snps causing a frame shift by a deletion
     * @param noAADeletions the number of snps causing a frame shift by a deletion
     */
    public void setNoAADeletions(int noAADeletions) {
        this.noAADeletions = noAADeletions;
    }

    /**
     * @return the number of snps causing an amino acid substitution
     */
    public int getNoSubstitutions() {
        return this.noSubstitutions;
    }

    /**
     * sets the number of snps causing an amino acid substitution
     * @param noSubstitutions the number of snps causing an amino acid substitution
     */
    public void setNoSubstitutions(int noSubstitutions) {
        this.noSubstitutions = noSubstitutions;
    }
    
    /**
     * @return the number of snps of the type insertion
     */
    public int getNoInsertions() {
        return this.noInsertions;
    }

    /**
     * set the number of snps of the type insertion
     *
     * @param noInsertions the number of snps of the type insertion
     */
    public void setNoInsertions(int noInsertions) {
        this.noInsertions = noInsertions;
    }

    /**
     * @return the number of snps of the type deletion
     */
    public int getNoDeletions() {
        return this.noDeletions;
    }

    /**
     * sets the number of snps of the type deletion
     *
     * @param noDeletions the number of snps of the type deletion
     */
    public void setNoDeletions(int noDeletions) {
        this.noDeletions = noDeletions;
    }
    
    
}
