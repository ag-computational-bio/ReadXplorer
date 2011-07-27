package de.cebitec.vamp.databackend.connector;

//import de.cebitec.vamp.api.objects.Read;
import de.cebitec.vamp.api.objects.Snp;
import de.cebitec.vamp.api.objects.Snp454;
import de.cebitec.vamp.databackend.CoverageRequest;
import de.cebitec.vamp.databackend.CoverageThread;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for TrackConnector and MultiTrackConnector.
 * @author dkramer
 */
public interface ITrackConnector {

    public CoverageThread getThread();

    public Collection<PersistantMapping> getMappings(int from, int to);

    public void addCoverageRequest(CoverageRequest request);

    public Collection<PersistantDiff> getDiffsForIntervall(int from, int to);

    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervallOrderedByMappingID(int from, int to);

    public void updateTableStatics(int numOfReads, int numOfUniqueSeq);

    /**
     * @return
     * @deprecated Since the RUN domain has been excluded from vamp
     */
    @Deprecated
    public int getNumOfReads();

    /**
     * @return 
     * @deprecated Since the RUN domain has been excluded from vamp
     */
    @Deprecated
    public int getNumOfReadsCalculate();

    public int getNumOfMappings();

    public int getNumOfMappingsCalculate();

    public int getNumOfUniqueMappings();

    public int getNumOfUniqueMappingsCalculate();

    public int getNumOfUniqueSequences();

    public int getNumOfUniqueSequencesCalculate();

    public int getNumOfPerfectUniqueMappings();

    public int getNumOfPerfectUniqueMappingsCalculate();

    public int getNumOfUniqueBmMappings();

    public int getNumOfUniqueBmMappingsCalculate();

    public void setStatics( int numMappings, int numUniqueMappings, int numUniqueSeqbmMappings,
                            int numPerfectMappings, int numBestMatchMappings, double coveragePerf,
                            double coverageBM, double coverageComplete);

    //public long getRunId();
    public long getTrackID();

    public String getAssociatedTrackName();

        /**
     * Returns all unique reads containing the given sequence.
     * Sequence cannot be longer than the readlength.
     * TODO: should also return all positions of the reads
     * @param sequence the sequence to search for
     * @return
     */
//     public List<Read> findSequence(String sequence) ;

    //public List<Read> findReads(String read) ;

    /**
     * identifies SNPs.
     * @param percentageThreshold
     * @param absThreshold
     * @param is454 
     * @return
     */
    public List<Snp> findSNPs(int percentageThreshold, int absThreshold);
    
    public List<Snp454> findSNPs454(int percentageThreshold, int absThreshold);

    public double getPercentRefGenPerfectCovered();

    public double getPercentRefGenPerfectCoveredCalculate();

    public double getPercentRefGenBmCovered();

    public double getPercentRefGenBmCoveredCalculate();

    public double getPercentRefGenNErrorCovered();

    public double getPercentRefGenNErrorCoveredCalculate();

    public HashMap<Integer, Integer> getCoverageInfosofTrack(int from, int to);
}
