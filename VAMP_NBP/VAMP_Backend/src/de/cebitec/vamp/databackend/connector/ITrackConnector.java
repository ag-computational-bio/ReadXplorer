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

    public void addCoverageRequest(CoverageRequest request) ;

    public Collection<PersistantDiff> getDiffsForIntervall(int from, int to) ;

    public Collection<PersistantReferenceGap> getExtendedReferenceGapsForIntervallOrderedByMappingID(int from, int to);

    public void updateTableStatics(int numOfReads, int numOfUniqueSeq);

    public int getNumOfReads();

    public int getNumOfReadsCalculate();

    public int getNumOfUniqueSequences();

    public int getNumOfMappedSequences();

    public int getNumOfMappedSequencesCalculate();

    public int getNumOfUniqueBmMappings();

    public int getNumOfUniqueBmMappingsCalculate();

    public int getNumOfUniqueMappings() ;

    public int getNumOfUniqueMappingsCalculate();

    public void setStatics(int mappings, int perfectMappings, int bmMappings,int mappedSeq, double coveragePerf, double coverageBM, double coverageComplete,int numOfReads, int numOfUniqueSeq);

     public int getNumOfPerfectUniqueMappings();

     public int getNumOfPerfectUniqueMappingsCalculate();

     //public long getRunId();

     public long getTrackID();

     public String getAssociatedTrackName() ;

     //public List<Read> findReads(String read) ;

     /**
      * Returns all unique reads containing the given sequence.
      * Sequence cannot be longer than the readlength.
      * TODO: should also return all positions of the reads
      * @param sequence the sequence to search for
      * @return
      */
//     public List<Read> findSequence(String sequence) ;

     public List<Snp> findSNPs(int percentageThreshold, int absThreshold) ;

     public double getPercentRefGenPerfectCovered();

     public double getPercentRefGenPerfectCoveredCalculate() ;

     public double getPercentRefGenBmCovered() ;

     public double getPercentRefGenBmCoveredCalculate();

     public double getPercentRefGenNErrorCovered() ;

     public double getPercentRefGenNErrorCoveredCalculate() ;

     public HashMap<Integer,Integer> getCoverageInfosofTrack(int from , int to);





}
