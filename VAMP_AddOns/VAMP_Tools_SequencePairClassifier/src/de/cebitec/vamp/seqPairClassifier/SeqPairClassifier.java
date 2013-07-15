package de.cebitec.vamp.seqPairClassifier;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.parser.mappings.SeqPairClassifierI;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Main class of the read pair identifier. For two given files (one file
 * with all reads from one side and the other with all reads from the other
 * side) it calculates the different combinations of each read pair mapping
 * and returns which read pairs could be mapped successfully, which do not
 * have a sequence in the correct distance or/and orientation and which cannot
 * be assigned to at least one position. Thus it covers all possible pairing
 * cases.
 *
 * TODO: identify when pair goes across end of genome! but only if circular
 * reference genome!
 *
 * @author Rolf Hilker
 */
@ServiceProvider(service = SeqPairClassifierI.class)
public class SeqPairClassifier implements SeqPairClassifierI, Observer, Observable {
    
    private ArrayList<Observer> observers;
    private ParsedTrack track1;
    private ParsedTrack track2;
    private int dist;
    private int minDist;
    private int maxDist;
    private short orienation; //orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
    private String errorMsg;
    private ParsedSeqPairContainer seqPairContainer;

    /**
     * Empty constructor, because nothing to do yet. But don't forget to set
     * data before calling classifySeqPairs().
     */
    public SeqPairClassifier() {
        //set data later
        this.observers = new ArrayList<>();
    }

    /**
     *
     * @param track1 fst track with read 1
     * @param track2 scnd track with read 2
     * @param dist distance in bases
     * @param deviation deviation in base pairs: 1 = 1 base
     * @param orientation orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
     * @throws ParsingException
     * @throws IOException
     */
    public SeqPairClassifier(ParsedTrack track1, ParsedTrack track2, int dist,
            int deviation, short orientation) throws ParsingException, IOException {
        
        this.observers = new ArrayList<>();
        this.track1 = track1;
        this.track2 = track2;
        this.dist = dist;
        this.orienation = orientation;
        this.calculateMinAndMaxDist(dist, deviation);
    }

    /**
     * Not implemented for this classifier implementation, as currently no
     * preprocessing is needed.
     * @param trackJob
     * @return true
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        return true;
    }
    
    /**
     * Before classification can start the data has to be set.
     * @param track1 fst track with read 1
     * @param track2 scnd track with read 2
     * @param distance insert distance depicting distance of insert between both
     * ADAPTER sequences = whole fragment length
     * @param deviation maximal deviation in % of the distance
     * @param orientation orientation of the pairs: 0 = fr, 1 = rf, 2 = ff/rr
     */
    public void setData(ParsedTrack track1, ParsedTrack track2, int distance, short deviation, byte orientation) {
        this.track1 = track1;
        this.track2 = track2;
        this.dist = distance;
        this.calculateMinAndMaxDist(dist, deviation);
        this.orienation = orientation;
    }

    /**
     * Responsible for all actual calculations of this tool.
     */
    private void execute() throws ParsingException {
//        MappingParserI parser1 = trackJob1.getParser();
//        MappingParserI parser2 = trackJob2.getParser();
//        parser1.registerObserver(this);
//        parser2.registerObserver(this);
//        ParsedMappingContainer mappingContainer1 = parser1.parseInput(this.trackJob1, "");
//        ParsedMappingContainer mappingContainer2 = parser2.parseInput(this.trackJob2, "");
//
//        this.classifySeqPairs(mappingContainer1, mappingContainer2);
    }
    
    @Override
    public ParsedSeqPairContainer classifySeqPairs() {
        
        this.notifyObservers(NbBundle.getMessage(SeqPairClassifier.class, "Classifier.Classification.Start"));
        
        this.seqPairContainer = new ParsedSeqPairContainer();
        this.seqPairContainer.setTrackId1(this.track1.getID());
        this.seqPairContainer.setTrackId2(this.track2.getID());
        HashMap<String, Integer> idToNameMap1 = this.track1.getReadnameToSeqIdMap1();
        HashMap<String, Integer> idToNameMap2 = this.track1.getReadnameToSeqIdMap2();
        idToNameMap1.putAll(track2.getReadnameToSeqIdMap1());
        idToNameMap2.putAll(track2.getReadnameToSeqIdMap2());
        Iterator<String> it1 = idToNameMap1.keySet().iterator();
        Iterator<String> it2 = idToNameMap2.keySet().iterator();
        ParsedMappingContainer mappings1 = this.track1.getParsedMappingContainer();
        ParsedMappingContainer mappings2 = this.track2.getParsedMappingContainer();
        
        String currReadname;
        int seqID1;
        int seqID2;
        List<ParsedMapping> mappingGroup1;
        List<ParsedMapping> mappingGroup2;
        ParsedMapping parsedMapping1;
        ParsedMapping parsedMapping2;
        int direction1;
        int start1;
        int stop1;
        int start2;
        int stop2;
        int currDist;
        boolean pairSize;
        long interimSeqPairId = 1;
        
        int largestSmallerDist = Integer.MIN_VALUE;
        int largestPotSmallerDist = Integer.MIN_VALUE;        
        int largestUnorSmallerDist = Integer.MIN_VALUE;
        int largestPotUnorSmallerDist = Integer.MIN_VALUE;
        
        int orient1 = this.orienation == 1 ? -1 : 1;
        int dir = this.orienation == 2 ? 1 : -1;
        boolean case1;
        
        List<ParsedSeqPairMapping> potPairList = new ArrayList<>(); //also perfect
        List<ParsedSeqPairMapping> potSmallPairList = new ArrayList<>();        
        List<ParsedSeqPairMapping> potPotSmallPairList = new ArrayList<>();        
        List<ParsedSeqPairMapping> unorPairList = new ArrayList<>();
        List<ParsedSeqPairMapping> potUnorPairList = new ArrayList<>();
        List<ParsedSeqPairMapping> unorSmallPairList = new ArrayList<>();
        List<ParsedSeqPairMapping> potUnorSmallPairList = new ArrayList<>();
        
        List<Long> omitIdList = new ArrayList<>(); //(enthält alle und werden step by step rausgelöscht)

        /*
         * 0 = fr -r1(1)-> <-r2(-1)- (stop1<start2) oder -r2(1)-> <-r1(-1)-
         * (stop2 < start1) 1 = rf <-r1(-1)- -r2(1)-> (stop1<start2) oder
         * <-r2(-1)- -r1(1)-> (stop2 < start1) 2 = ff -r1(1)-> -r2(1)->
         * (stop1<start2) oder <-r2(-1)- <-r1(-1)- (stop2 < start1)
         */
        
        while (it1.hasNext()) { //block for one readname
            currReadname = it1.next();
            seqID1 = idToNameMap1.get(currReadname);
            mappingGroup1 = mappings1.getParsedMappingGroupBySeqID(seqID1).getMappings();
            
            if (idToNameMap2.containsKey(currReadname)) {
                //both sides of the read pair have been mapped
                seqID2 = idToNameMap2.get(currReadname);
                mappingGroup2 = mappings2.getParsedMappingGroupBySeqID(seqID2).getMappings();
                
                pairSize = mappingGroup1.size() == 1 && mappingGroup2.size() == 1;
                
                if (pairSize) { //only one mapping per readname = we can always store a pair object

                    parsedMapping1 = mappingGroup1.get(0);
                    parsedMapping2 = mappingGroup2.get(0);
                    direction1 = parsedMapping1.getDirection();
                    start1 = parsedMapping1.getStart();
                    stop1 = parsedMapping1.getStop();
                    
                    start2 = parsedMapping2.getStart();
                    stop2 = parsedMapping2.getStop();
                    //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                    case1 = direction1 == orient1 && start1 < start2;
                    if ((case1 || direction1 == -orient1 && start2 < start1)
                            && direction1 == dir * parsedMapping2.getDirection()) {

                        //determine insert size between both reads
                        if (case1) {
                            currDist = Math.abs(start1 - stop2);
                        } else {
                            currDist = Math.abs(start2 - stop1);
                        }
                        
                        if (currDist <= this.maxDist && currDist >= this.minDist) {

                            // For estimating pair size
                            ///////////////////////////// found a perfect pair! /////////////////////////////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_PERFECT_PAIR, currDist);
                        } else if (currDist < this.maxDist) { //both reads of pair mapped, but distance in reference is different
                            ///////////////////////////// imperfect pair, distance too small /////////////////////////////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_DIST_SMALL_PAIR, currDist);
                        } else { //////////////// imperfect pair, distance too large //////////////////////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_DIST_LARGE_PAIR, currDist);
                        }
                    } else { //////////////////////////// inversion of one read ////////////////////////////////
                        currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                        
                        if (currDist <= this.maxDist && currDist >= this.minDist) {////distance fits, orientation not ///////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_ORIENT_WRONG_PAIR, currDist);
                        } else if (currDist < this.maxDist) {///// orientation wrong & distance too small //////////////////////////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR, currDist);
                        } else { //////////////// orientation wrong & distance too large //////////////////////////
                            this.addPairedMapping(parsedMapping1.getID(), parsedMapping2.getID(), interimSeqPairId, Properties.TYPE_OR_DIST_LARGE_PAIR, currDist);
                        }
                    }
                } else {
                    
                    for (ParsedMapping parsedMappingA : mappingGroup1) { //pos and direction can deviate

                        direction1 = parsedMappingA.getDirection();
                        start1 = parsedMappingA.getStart();
                        stop1 = parsedMappingA.getStop();
                        
                        for (ParsedMapping parsedMappingB : mappingGroup2) {
                            
                            if (!(omitIdList.contains(parsedMappingA.getID()) && omitIdList.contains(parsedMappingB.getID()))) {
                                start2 = parsedMappingB.getStart();
                                stop2 = parsedMappingB.getStop();
                                //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                                case1 = direction1 == orient1 && start1 < start2;
                                if ((case1 || direction1 == -orient1 && start2 < start1)
                                        && direction1 == dir * parsedMappingB.getDirection()) { //direction fits

                                    //determine insert size between both reads
                                    if (case1) {
                                        currDist = Math.abs(start1 - stop2);
                                    } else {
                                        currDist = Math.abs(start2 - stop1);
                                    }
                                    
                                    if (currDist <= this.maxDist && currDist >= this.minDist) { //distance fits
                                        ///////////////////////////// found a perfect pair! /////////////////////////////////
                                        if (parsedMappingA.isBestMapping() && parsedMappingB.isBestMapping()) {
                                            this.addPairedMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_PERFECT_PAIR, currDist);
                                            omitIdList.add(parsedMappingA.getID());
                                            omitIdList.add(parsedMappingB.getID());
                                            seqPairContainer.getStatsContainer().getSeqPairSizeDistribution().increaseDistribution(currDist);
                                        } else {//////////////// store potential perfect pair //////////////////////////
                                            potPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_PERFECT_PAIR, currDist));
                                        }
                                    } else //////////////// distance too small, potential pair //////////////////////////
                                    if (currDist < this.maxDist) {
                                        if (largestSmallerDist < currDist && parsedMappingA.isBestMapping() && parsedMappingB.isBestMapping()) { //best mappings
                                            largestSmallerDist = currDist;
                                            potSmallPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_DIST_SMALL_PAIR, currDist));
                                        } else if (largestPotSmallerDist < currDist) { //at least one common mapping in potential pair
                                            largestPotSmallerDist = currDist;
                                            potPotSmallPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_DIST_SMALL_PAIR, currDist));
                                        }
                                    } else {//////////////// distance too large //////////////////////////
                                        //currently nothing to do
                                    }
                                } else { //////////////////////////// inversion of one read ////////////////////////////////
                                    currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                                    
                                    if (currDist <= this.maxDist && currDist >= this.minDist) { ////distance fits, orientation not ///////////
                                        if (parsedMappingA.isBestMapping() && parsedMappingB.isBestMapping()) { //best mappings
                                            unorPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_ORIENT_WRONG_PAIR, currDist));
                                        } else {
                                            potUnorPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_ORIENT_WRONG_PAIR, currDist));
                                        }
                                    } else if (currDist < this.maxDist && largestSmallerDist < currDist) {///// orientation wrong & distance too small //////////////////////////////
                                        if (largestUnorSmallerDist < currDist && parsedMappingA.isBestMapping() && parsedMappingB.isBestMapping()) { //best mappings
                                            largestUnorSmallerDist = currDist;
                                            unorSmallPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR, currDist));
                                        } else if (largestPotUnorSmallerDist < currDist) {
                                            largestPotUnorSmallerDist = currDist;
                                            potUnorSmallPairList.add(new ParsedSeqPairMapping(parsedMappingA.getID(), parsedMappingB.getID(), interimSeqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR, currDist));
                                        }
                                    } else { //////////////// orientation wrong & distance too large //////////////////////////
                                        //currently nothing to do
                                    }
                                }
                            }
                        }
                        largestSmallerDist = Integer.MIN_VALUE;
                        largestPotSmallerDist = Integer.MIN_VALUE;
                        largestUnorSmallerDist = Integer.MIN_VALUE;
                        largestPotUnorSmallerDist = Integer.MIN_VALUE;
                        
                    }

                    /*
                     * Determines order of insertion of pairs. If one id is
                     * contained in an earlier list, then it is ignored in all
                     * other lists!
                     */
                    for (ParsedSeqPairMapping pairMapping : potSmallPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    for (ParsedSeqPairMapping pairMapping : unorPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    for (ParsedSeqPairMapping pairMapping : unorSmallPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    for (ParsedSeqPairMapping pairMapping : potPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    for (ParsedSeqPairMapping pairMapping : potSmallPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    for (ParsedSeqPairMapping pairMapping : potUnorSmallPairList) {
                        this.addPairedMapping(pairMapping, omitIdList);
                    }
                    
                    long id;
                    for (ParsedMapping mapping : mappingGroup1) {
                        if (!omitIdList.contains(id = mapping.getID())) {
                            this.seqPairContainer.addMappingToPairId(new Pair<>(id, interimSeqPairId));
                        }
                    }
                    
                    for (ParsedMapping mapping : mappingGroup2) {
                        if (!omitIdList.contains(id = mapping.getID())) {
                            this.seqPairContainer.addMappingToPairId(new Pair<>(id, interimSeqPairId));
                        }
                    }

                    //reset data structures
                    potPairList.clear();
                    potSmallPairList.clear();
                    potPotSmallPairList.clear();
                    unorPairList.clear();
                    potUnorPairList.clear();
                    unorSmallPairList.clear();
                    potUnorSmallPairList.clear();
                    omitIdList.clear();
                }
                
            } else { //only one side of the read pair could be mapped
                for (ParsedMapping parsedMapping : mappingGroup1) { //pos and direction can deviate
                    this.seqPairContainer.addMappingToPairId(new Pair<>(parsedMapping.getID(), interimSeqPairId));
                }
            }
            ++interimSeqPairId;
        }

        //also have to iterate over it2, cause there might be single side mapped sequences
        while (it2.hasNext()) { //block for one readname

            currReadname = it2.next();
            seqID2 = idToNameMap2.get(currReadname);
            mappingGroup2 = mappings2.getParsedMappingGroupBySeqID(seqID2).getMappings();
            
            if (!idToNameMap1.containsKey(currReadname)) { //only scnd side of the read pair could be mapped

                for (ParsedMapping parsedMapping : mappingGroup2) { //pos and direction can deviate
                    this.seqPairContainer.addMappingToPairId(new Pair<>(parsedMapping.getID(), interimSeqPairId));
                }
                ++interimSeqPairId;
            }
        }
        this.seqPairContainer.getStatsContainer().incSeqPairStats(Properties.TYPE_UNPAIRED_PAIR, seqPairContainer.getMappingToPairIdList().size());
        
        this.notifyObservers(NbBundle.getMessage(SeqPairClassifier.class, "Classifier.Classification.Finish"));
        
//        //TODO: trying to clean up
//        mappings1 = null;
//        mappings2 = null;
//        mappingGroup1 = null;
//        mappingGroup2 = null;
        
        return seqPairContainer;
        
    }

    /**
     * Either adds a new read pair mapping object to the list or increases
     * the number of replicates if the current read pair mapping contains
     * identical data to an already existing read pair mapping object.
     * @param interimID id for this read pair mapping. Interim, because it
     * has to be shifted when inserting into database to guarantee uniqueness
     * @param interimMatepairID interimMatepairID id for mappings belonging to
     * same read pair. Interim, because it has to be shifted when inserting
     * into database to guarantee uniqueness
     * @param mappingId1
     * @param mappingId2
     * @param type type of the pair 0 = perfect, 1 = dist too large, 2 = dist
     * too small, 3 = orient wrong 4 = orient wrong and dist too large, 5 =
     * orient wrong and dist too small
     * @param the read pair distance
     */
    private void addPairedMapping(long mappingId1, long mappingId2, long seqPairId, byte type, int distance) {
        
        ParsedSeqPairMapping newSeqPair = new ParsedSeqPairMapping(mappingId1, mappingId2, seqPairId, type, distance);
        Pair<Long, Long> mappingIDs = new Pair<>(mappingId1, mappingId2);
        this.seqPairContainer.addParsedSeqPair(mappingIDs, newSeqPair);
    }
    
    private void addPairedMapping(ParsedSeqPairMapping pairMapping, List<Long> omitIdList) {
        long id1 = pairMapping.getMappingId1();
        long id2 = pairMapping.getMappingId2();
        if (!(omitIdList.contains(id1) || omitIdList.contains(id2))) {
            this.seqPairContainer.addParsedSeqPair(new Pair<>(id1, id2), pairMapping);
            omitIdList.add(id1);
            omitIdList.add(id2);
        }
        seqPairContainer.getStatsContainer().getSeqPairSizeDistribution().increaseDistribution(pairMapping.getDistance());
    }
    
    @Override
    public void update(Object args) {
        if (args instanceof String) {
            this.errorMsg = (String) args;
            this.notifyObservers(null);
        }
    }
    
    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(this.errorMsg);
        }
    }

    /**
     * Depending on deviation the min and max values of the distance between a
     * read pair is set.
     * @param dist distance in bases
     * @param deviation deviation in % (1-100)
     */
    private void calculateMinAndMaxDist(final int dist, final int deviation) {
        int devInBP = dist / 100 * deviation;
        this.minDist = dist - devInBP;
        this.maxDist = dist + devInBP;
    }
}
