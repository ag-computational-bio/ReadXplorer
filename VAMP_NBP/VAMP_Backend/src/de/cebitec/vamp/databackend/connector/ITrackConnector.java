package de.cebitec.vamp.databackend.connector;

//import de.cebitec.vamp.api.objects.Read;
import de.cebitec.vamp.api.objects.Snp;
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

    public Collection<PersistantDiff> getDiffsForInterval(int from, int to);

    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervalOrderedByMappingID(int from, int to);

    public int getNumOfReads();

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
    
    public int getNumOfSeqPairs();
    
    public int getNumOfSeqPairsCalculate();
    
    public int getNumOfPerfectSeqPairs();
    
    public int getNumOfPerfectSeqPairsCalculate();
    
    public int getNumOfUniqueSeqPairs();
    
    public int getNumOfUniqueSeqPairsCalculate();

    public int getNumOfUniquePerfectSeqPairs();
    
    public int getNumOfUniquePerfectSeqPairsCalculate();
    
    public int getNumOfSingleMappings();
    
    public int getNumOfSingleMappingsCalculate();
    
    public void setStatistics(int numMappings, int numUniqueMappings, int numUniqueSeqbmMappings,
                            int numPerfectMappings, int numBestMatchMappings, double coveragePerf,
                            double coverageBM, double coverageComplete, int numReads);
    
    public void addSeqPairStatistics(int numSeqPairs, int numPerfectSeqPairs, 
            int numUniqueSeqPairs, int numUniquePerfectSeqPairs, int numSingleReads);

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
     * @return
     */
    public List<Snp> findSNPs(int percentageThreshold, int absThreshold);

    public double getPercentRefGenPerfectCovered();

    public double getPercentRefGenPerfectCoveredCalculate();

    public double getPercentRefGenBmCovered();

    public double getPercentRefGenBmCoveredCalculate();

    public double getPercentRefGenNErrorCovered();

    public double getPercentRefGenNErrorCoveredCalculate();

    public HashMap<Integer, Integer> getCoverageInfosOfTrack(int from, int to);
}
