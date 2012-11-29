package de.cebitec.vamp.tools.snp;

/**
 * Data storage for SNP detection result metrics / statistics.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpResultStatistics {
    
    private int totalNoSnps;
    private int noIntergenicSnps;
    private int noSynonymousSnps;
    private int noMissenseSnps;
    private int noChemicallyNeutralSnps;
    private int noInsertions;
    private int noDeletions ;

    /**
     * @return the total number of snps 
     */
    public int getTotalNoSnps() {
        return totalNoSnps;
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
        return noIntergenicSnps;
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
        return noSynonymousSnps;
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
        return noMissenseSnps;
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
        return noChemicallyNeutralSnps;
    }

    /**
     * set the number of chemically neutral snps
     * @param noChemicallyNeutralSnps the number of chemically neutral snps
     */
    public void setNoChemicallyNeutralSnps(int noChemicallyNeutralSnps) {
        this.noChemicallyNeutralSnps = noChemicallyNeutralSnps;
    }

    /**
     * @return the number of insertions
     */
    public int getNoInsertions() {
        return noInsertions;
    }

    /**
     * set the number of insertions
     * @param noInsertions the number of insertions
     */
    public void setNoInsertions(int noInsertions) {
        this.noInsertions = noInsertions;
    }

    /**
     * @return the number of deletions
     */
    public int getNoDeletions() {
        return noDeletions;
    }

    /**
     * set the number of deletions
     * @param noDeletions the number of deletions
     */
    public void setNoDeletions(int noDeletions) {
        this.noDeletions = noDeletions;
    }
}
