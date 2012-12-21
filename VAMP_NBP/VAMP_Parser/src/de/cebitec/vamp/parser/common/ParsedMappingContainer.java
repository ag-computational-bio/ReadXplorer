package de.cebitec.vamp.parser.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Container for all mappings belonging to one track. Contains statistics as well
 * as a all mappings.
 *
 * @author ddoppmeier
 */
public class ParsedMappingContainer {
    private int numberOfMappings = 0; //the number of created mappings by the mapper
    private int numberOfBM = 0;
    private int numberOfPerfect = 0;
    private int numOfMappings = 0;
    private int numUniqueSeq = 0;
    private int numUniqueMappings = 0;
    private int numReads = 0;
    private HashMap<Integer, ParsedMappingGroup> mappings;
    private boolean lastMappingContainer = false;
    private boolean firstMappingContainer = false;
    private int sumReadLength;
    private boolean mappingInfosCalculated = false;
    private Map<Integer, Integer> mappingInfos;
    
    /**
     * Creates an empty mapping container.
     */
    public ParsedMappingContainer(){
        this.mappings = new HashMap<>();
        this.mappingInfos = new HashMap<>();
        this.sumReadLength = 0;
    }

    public void addParsedMapping(ParsedMapping mapping, int sequenceID) {
        ++this.numOfMappings;
        if (!this.mappings.containsKey(sequenceID)) {
            ParsedMappingGroup mappingGroup = new ParsedMappingGroup();
            mappings.put(sequenceID, mappingGroup);
            ++this.numUniqueSeq;
        }
        this.mappings.get(sequenceID).addParsedMapping(mapping);
        if (this.mappings.get(sequenceID).getMappings().size() == 2) {
            --this.numUniqueSeq;
        }
    }

    public Collection<Integer> getMappedSequenceIDs(){
        return this.mappings.keySet();
    }

    public ParsedMappingGroup getParsedMappingGroupBySeqID(int sequenceID){
        return this.mappings.get(sequenceID);
    }

    /**
     * Set the mapping informations, if calculated somewhere else already.
     * @param mappingInfos the mapping infos to set:
     * <br>(1, numberOfMappings);
     * <br>(2, numberOfPerfect); 
     * <br>(3, numberOfBM); 
     * <br>(4, numUniqueMappings); 
     * <br>(5, numUniqueSeq); 
     * <br>(6, numReads); 
     * <br>(7, sumReadLength);
     */
    public void setMappingInfos(Map<Integer, Integer> mappingInfos) {
        this.mappingInfos = mappingInfos;
        this.mappingInfosCalculated = true;
    }

    /**
     * @return Hashmap with following entries: <br>(1, numberOfMappings);
     * <br>(2, numberOfPerfect); <br>(3, numberOfBM); <br>(4,
     * numUniqueMappings); <br>(5, numUniqueSeq); <br>(6, numReads); <br>(7,
     * sumReadLength);
     */
    public Map<Integer, Integer> getMappingInfos() {
        if (!mappingInfosCalculated) {
            this.calcMappingInformations();
        }
        return this.mappingInfos;
    }    

    /**
     * Calculates the mapping informations by analyzing the mapping container 
     * data.
     */
    private void calcMappingInformations() {

            Collection<ParsedMappingGroup> groups = mappings.values();
            Iterator<ParsedMappingGroup> groupsIt = groups.iterator();

            while (groupsIt.hasNext()) {
                ParsedMappingGroup mappingGroup = groupsIt.next();
                List<ParsedMapping> mappingList = mappingGroup.getMappings();
                Iterator<ParsedMapping> mappingIt = mappingList.iterator();
                while (mappingIt.hasNext()) {
                    ParsedMapping mapping = mappingIt.next();
                    if (mapping.isBestMapping() == true) {
                        ++numberOfBM;
                        if (mapping.getErrors() == 0) {
                            ++numberOfPerfect;
                        }
                    }
                    numberOfMappings += mapping.getNumReplicates();
                }

                //calculate number of unique mappings (map only to one position, but have replicates)
                if (mappingList.size() == 1) {
                    this.numUniqueMappings += mappingList.get(0).getNumReplicates();
                }
                this.numReads += mappingList.get(0).getNumReplicates();
            }
        
//        int averageReadLength = 0;
//        averageReadLength = this.numOfMappings != 0 ? this.sumReadLength / this.numOfMappings : 0;

        mappingInfos.put(1, this.numberOfMappings);
        mappingInfos.put(2, this.numberOfPerfect);
        mappingInfos.put(3, this.numberOfBM);
        mappingInfos.put(4, this.numUniqueMappings);
        mappingInfos.put(5, this.numUniqueSeq);
        mappingInfos.put(6, this.numReads);
        mappingInfos.put(7, this.sumReadLength);
        
        this.mappingInfosCalculated = true;
    }

    public void clear(){
        mappings.clear();
    }

    public boolean isLastMappingContainer() {
        return lastMappingContainer;
    }

    public void setLastMappingContainer(boolean lastMappingContainer) {
        this.lastMappingContainer = lastMappingContainer;
    }

    public boolean isFirstMappingContainer() {
        return firstMappingContainer;
    }

    public void setFirstMappingContainer(boolean firstMappingContainer) {
        this.firstMappingContainer = firstMappingContainer;
    }

    /**
     * @return the averageReadLength
     */
    public int getAverageReadLength() {
        return this.numOfMappings != 0 ? this.sumReadLength / this.numOfMappings : 0;
    }

    /**
     * @param sumReadLength the sumReadLength to set
     */
    public void setSumReadLength(int sumReadLength) {
        this.sumReadLength = sumReadLength;
    }

    
    public int getSumReadLength() {
        return this.sumReadLength;
    }
    
}
