package de.cebitec.vamp.seqPairClassifier;

import de.cebitec.vamp.parser.SeqPairJobContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.parser.mappings.SeqPairClassifierI;
import de.cebitec.vamp.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.samtools.*;
import org.openide.util.NbBundle;

/**
 * Sam/Bam sequence pair classifier for a direct file access track. This means
 * the classification of the seq pairs has to be carried out. Besides the 
 * classification this class also acts as extender for the given sam/bam file
 * and thus creates an extended copy of the original file after the 
 * classification.
 *
 * @author Rolf Hilker
 */
public class SamBamDirectSeqPairClassifier implements SeqPairClassifierI, Observer, Observable {

    private ArrayList<Observer> observers;
    private TrackJob trackJob1;
    private int dist;
    private int minDist;
    private int maxDist;
    private short orienation; //orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
    private SAMFileWriter samBamFileWriter;
    private Map<String,Pair<Integer,Integer>> classificationMap; 
    private final String refSeq;
   
    private int average_Seq_Pair_Length = 0; //TODO: calculate average size etc. for statistics
    private int add_Seq_Pair_length = 0;
    private int count_Seq_Pair = 0;
    
    /**
     * Sam/Bam sequence pair classifier for a direct file access track. This
     * means the classification of the seq pairs has to be carried out. Besides
     * the classification this class also acts as extender for the given sam/bam
     * file and thus creates an extended copy of the original file after the
     * classification.
     * @param seqPairJobContainer the sequence pair job container to classify
     * @param refSeq the reference sequence belonging to the sequence pair job
     * @param classificationMap the ordinary classification map of the reads. It
     *      is needed for the extension of the sam/bam file
     */
    public SamBamDirectSeqPairClassifier(SeqPairJobContainer seqPairJobContainer, String refSeq, 
            Map<String,Pair<Integer,Integer>> classificationMap) {
        this.observers = new ArrayList<>();
        this.trackJob1 = seqPairJobContainer.getTrackJob1();
        this.dist = seqPairJobContainer.getDistance();
        this.calculateMinAndMaxDist(dist, seqPairJobContainer.getDeviation());
        this.orienation = seqPairJobContainer.getOrientation();
        this.classificationMap = classificationMap;
        this.refSeq = refSeq;
    }

    /**
     * Classifies the seuqence pairs.
     * @return an empty seq pair container, because no data needs to be stored
     */
    @Override
    public ParsedSeqPairContainer classifySeqPairs() {
        
        try {
            
            long start = System.currentTimeMillis();
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Classification.Start"));
            
            List<SAMRecord> currentRecords1 = new ArrayList<>();
            List<SAMRecord> currentRecords2 = new ArrayList<>();            
            
            int lineno = 0;
            SAMFileReader samBamReader = new SAMFileReader(trackJob1.getFile());
            SAMRecordIterator samItor = samBamReader.iterator();
            
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
            
            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(trackJob1.getFile(), header, false, "_extended");
            this.samBamFileWriter = writerAndFile.getFirst();
            
            File outputFile = writerAndFile.getSecond();
            trackJob1.setFile(outputFile);
            
            SAMRecord record;
            String lastReadName = "";
            String readNameFull; //read name with pair tag
            String readName; //read name without pair tag
            char pairTag;
            int seqPairId = 1;
            while (samItor.hasNext()) {
                ++lineno;
                if (lineno == 371) {
                System.out.println(lineno);
                }
                //separate all mappings of same pair by seq pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag()) {
                    readNameFull = record.getReadName();
                    pairTag = readNameFull.charAt(readNameFull.length() - 1);
                    readName = readNameFull.substring(0, readNameFull.length() - 2);
                    
                    if (!readName.equals(lastReadName)) { //meaning: next pair, because sorted by read name
                        // classify sequence pair, because all mappings for this pair are currently stored in list
                        this.performClassification(currentRecords1, currentRecords2, seqPairId);
                        currentRecords1.clear();
                        currentRecords2.clear();
                        ++seqPairId;
                        
                    }
                    if (pairTag == Properties.EXT_A1 || pairTag == Properties.EXT_B1 || record.getFirstOfPairFlag()) { //TODO: remove pairTag
                        currentRecords1.add(record);
                    } else if (pairTag == Properties.EXT_A2 || pairTag == Properties.EXT_B2 || record.getSecondOfPairFlag()) {
                        currentRecords2.add(record);
                    }
                    lastReadName = readName;
                }
            }
            
            if (!currentRecords1.isEmpty() || !currentRecords2.isEmpty()) {
                this.performClassification(currentRecords1, currentRecords2, seqPairId);
            }
            
            samItor.close();
            this.samBamFileWriter.close();
            samBamReader.close();
            
            samBamReader = new SAMFileReader(outputFile);
            File indexFile = new File(outputFile.getAbsolutePath() + ".bai");
            SamUtils utils = new SamUtils();
            utils.createIndex(samBamReader, indexFile);
            samBamReader.close();
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Classification.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));
            
        } catch (Exception e) {
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Error", e.toString()));
            e.printStackTrace();
        }
        
        return new ParsedSeqPairContainer();
    }

    /**
     * Actually performs the classification of the sequence pairs.
     * @param currentRecords1 all records of the same read pair
     * @return 
     */
    private void performClassification(List<SAMRecord> currentRecords1, List<SAMRecord> currentRecords2, int seqPairId) {

        SAMRecord record1;
        SAMRecord record2;
        byte direction;
        byte direction2;
        int start1;
        int stop1;
        int start2;
        int stop2;
        int currDist;
        boolean pairSize;
        Pair<Integer, Integer> class1;
        Pair<Integer, Integer> class2;
        int diffs1;
        int diffs2;

        int largestSmallerDist = Integer.MIN_VALUE;
        int largestPotSmallerDist = Integer.MIN_VALUE;
        int largestUnorSmallerDist = Integer.MIN_VALUE;
        int largestPotUnorSmallerDist = Integer.MIN_VALUE;

        int orient1 = this.orienation == 1 ? -1 : 1;
        int dir = this.orienation == 2 ? 1 : -1;
        boolean case1;
        
        List<DirectSeqPair> potPairList = new ArrayList<>(); //also perfect
        List<DirectSeqPair> potSmallPairList = new ArrayList<>();        
        List<DirectSeqPair> potPotSmallPairList = new ArrayList<>();        
        List<DirectSeqPair> unorPairList = new ArrayList<>();
        List<DirectSeqPair> potUnorPairList = new ArrayList<>();
        List<DirectSeqPair> unorSmallPairList = new ArrayList<>();
        List<DirectSeqPair> potUnorSmallPairList = new ArrayList<>();
        
        List<SAMRecord> omitList = new ArrayList<>(); //(enthält alle und werden step by step rausgelöscht)

        /*
         * 0 = fr -r1(1)-> <-r2(-1)- (stop1<start2) oder -r2(1)-> <-r1(-1)-
         * (stop2 < start1) 1 = rf <-r1(-1)- -r2(1)-> (stop1<start2) oder
         * <-r2(-1)- -r1(1)-> (stop2 < start1) 2 = ff -r1(1)-> -r2(1)->
         * (stop1<start2) oder <-r2(-1)- <-r1(-1)- (stop2 < start1)
         */

        // For estimating the pair size
//        int count = 0;
//        int overall = 0;
      
        if (!currentRecords2.isEmpty()) {
            //both sides of the sequence pair have been mapped
            
            pairSize = currentRecords1.size() == 1 && currentRecords2.size() == 1;

            if (pairSize) { //only one mapping per readname = we can always store a pair object

                record1 = currentRecords1.get(0);
                record2 = currentRecords2.get(0);
                direction = record1.getReadNegativeStrandFlag() ? (byte) -1 : 1;
                start1 = record1.getAlignmentStart();
                stop1 = record1.getAlignmentEnd();

                start2 = record2.getAlignmentStart();
                stop2 = record2.getAlignmentEnd();
                direction2 = record2.getReadNegativeStrandFlag() ? (byte) -1 : 1;
                //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                
                //add classification data here, before passing mapping to writer
                this.addClassificationData(record1);
                this.addClassificationData(record2);
                
                case1 = direction == orient1 && start1 < start2;
                if ((case1 || direction == -orient1 && start2 < start1)
                        && direction == dir * direction2) {

                    //determine insert size between both reads
                    if (case1) {
                        currDist = Math.abs(start1 - stop2);
                    } else {
                        currDist = Math.abs(start2 - stop1);
                    }

                    if (currDist <= this.maxDist && currDist >= this.minDist) {

                        // For estimating pair size
//                            ++count;
//                            overall += currDist;
                        ///////////////////////////// found a perfect pair! /////////////////////////////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_PERFECT_PAIR);
                    } else if (currDist < this.maxDist) { //both reads of pair mapped, but distance in reference is different
                        ///////////////////////////// imperfect pair, distance too small /////////////////////////////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_DIST_SMALL_PAIR);
                    } else { //////////////// imperfect pair, distance too large //////////////////////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_DIST_LARGE_PAIR);
                    }
                } else { //////////////////////////// inversion of one read ////////////////////////////////
                    currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;

                    if (currDist <= this.maxDist && currDist >= this.minDist) {////distance fits, orientation not ///////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_ORIENT_WRONG_PAIR);
                    } else if (currDist < this.maxDist) {///// orientation wrong & distance too small //////////////////////////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR);
                    } else { //////////////// orientation wrong & distance too large //////////////////////////
                        this.addPairedRecord(record1, record2, seqPairId, Properties.TYPE_OR_DIST_LARGE_PAIR);
                    }
                }
            } else {

                for (SAMRecord recordA : currentRecords1) { //block for one readname, pos and direction can deviate
                    
                    try {

                        direction = recordA.getReadNegativeStrandFlag() ? (byte) -1 : 1;
                        start1 = recordA.getAlignmentStart();
                        stop1 = recordA.getAlignmentEnd();
                        class1 = this.classificationMap.get(recordA.getReadName());
                        diffs1 = ParserCommonMethods.countDiffsAndGaps(
                                recordA.getCigarString(),
                                recordA.getReadString(),
                                this.refSeq.substring(start1 - 1, stop1),
                                recordA.getReadNegativeStrandFlag());

                        for (SAMRecord recordB : currentRecords2) {

                            try {

                                if (!(omitList.contains(recordA) && omitList.contains(recordB))) {
                                    start2 = recordB.getAlignmentStart();
                                    stop2 = recordB.getAlignmentEnd();
                                    direction2 = recordB.getReadNegativeStrandFlag() ? (byte) -1 : 1;

                                    class2 = this.classificationMap.get(recordB.getReadName());
                                    diffs2 = ParserCommonMethods.countDiffsAndGaps(
                                            recordB.getCigarString(),
                                            recordB.getReadString(),
                                            this.refSeq.substring(start2 - 1, stop2),
                                            recordB.getReadNegativeStrandFlag());


                                    //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                                    case1 = direction == orient1 && start1 < start2;
                                    if ((case1 || direction == -orient1 && start2 < start1)
                                            && direction == dir * direction2) { //direction fits

                                        //determine insert size between both reads
                                        if (case1) {
                                            currDist = Math.abs(start1 - stop2);
                                        } else {
                                            currDist = Math.abs(start2 - stop1);
                                        }
                                        if (currDist <= this.maxDist && currDist >= this.minDist) { //distance fits
                                            ///////////////////////////// found a perfect pair! /////////////////////////////////
                                            if (diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) {
                                                this.addClassificationData(recordA, diffs1);
                                                this.addClassificationData(recordB, diffs2);
                                                this.addPairedRecord(recordA, recordB, seqPairId, Properties.TYPE_PERFECT_PAIR);
                                                omitList.add(recordA);
                                                omitList.add(recordB);
                                                add_Seq_Pair_length += currDist;
                                                count_Seq_Pair++;
                                            } else {//////////////// store potential perfect pair //////////////////////////
                                                potPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_PERFECT_PAIR));
                                            }
                                        } else //////////////// distance too small, potential pair //////////////////////////
                                        if (currDist < this.maxDist) {
                                            if (largestSmallerDist < currDist && diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                largestSmallerDist = currDist;
                                                potSmallPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_DIST_SMALL_PAIR));
                                            } else if (largestPotSmallerDist < currDist) { //at least one common mapping in potential pair
                                                largestPotSmallerDist = currDist; //replace even smaller pair with this one (more likely)
                                                potPotSmallPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_DIST_SMALL_PAIR));
                                            }
                                        } else {//////////////// distance too large //////////////////////////
                                            //TODO: something to do if dist too large??
                                        }
                                    } else { //////////////////////////// inversion of one read ////////////////////////////////
                                        currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;

                                        if (currDist <= this.maxDist && currDist >= this.minDist) { ////distance fits, orientation not ///////////
                                            if (diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                unorPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_ORIENT_WRONG_PAIR));
                                            } else {
                                                potUnorPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_ORIENT_WRONG_PAIR));
                                            }
                                        } else if (currDist < this.maxDist && largestSmallerDist < currDist) {///// orientation wrong & distance too small //////////////////////////////
                                            if (largestUnorSmallerDist < currDist && diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                largestUnorSmallerDist = currDist;
                                                unorSmallPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR));
                                            } else if (largestPotUnorSmallerDist < currDist) {
                                                largestPotUnorSmallerDist = currDist;
                                                potUnorSmallPairList.add(new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR));
                                            }
                                        } else { //////////////// orientation wrong & distance too large //////////////////////////
                                            //TODO: something to do??
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.UnclassifiedRead", recordA.getReadName()));
                            }
                        }
                        largestSmallerDist = Integer.MIN_VALUE;
                        largestPotSmallerDist = Integer.MIN_VALUE;
                        largestUnorSmallerDist = Integer.MIN_VALUE;
                        largestPotUnorSmallerDist = Integer.MIN_VALUE;
                    
                    } catch (NullPointerException e) {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.UnclassifiedRead", recordA.getReadName()));
                    }
                }

                /*
                 * Determines order of insertion of pairs. If one id is
                 * contained in an earlier list, then it is ignored in all other
                 * lists!
                 */
                for (DirectSeqPair pairMapping : potSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectSeqPair pairMapping : unorPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectSeqPair pairMapping : unorSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectSeqPair pairMapping : potPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectSeqPair pairMapping : potPotSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectSeqPair pairMapping : potUnorSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (SAMRecord mapping : currentRecords1) {
                    if (!omitList.contains(mapping)) {
                        this.addSingleRecord(mapping, seqPairId);
                    }
                }

                for (SAMRecord mapping : currentRecords2) {
                    if (!omitList.contains(mapping)) {
                        this.addSingleRecord(mapping, seqPairId);
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
                omitList.clear();
            }

        } else { //only one side of the sequence pair could be mapped
            for (SAMRecord record : currentRecords1) { //pos and direction can deviate
                this.addSingleRecord(record, seqPairId);
            }
        }

        //delete? this has already been done in the upper part by iterating over currentRecords2
//        //also have to iterate over list2, cause there might be single side mapped sequences
//        for (SAMRecord record2 : currentRecords2) { //block for one readname
//            
//            readName2 = record2.getReadName();
//            int seqID2 = readNameToSeqIdMap2.get(readName2);
//            
//            readName2 = readName2.substring(0, readName2.length() - 1);
//            if (    !readNameToSeqIdMap1.containsKey(
//                        readName2.concat(
//                            String.valueOf(Properties.EXT_A1))) && 
//                    !readNameToSeqIdMap1.containsKey(
//                        readName2.concat(
//                            String.valueOf(Properties.EXT_B1)))) { //only scnd side of the sequence pair could be mapped
//                //pos and direction can deviate
//                this.seqPairContainer.addMappingToPairId(new Pair<Long, Long>((long) seqID2, seqPairId));
//            }
//        }
//        this.seqPairContainer.setNumOfSingleMappings(this.seqPairContainer.getMappingToPairIdList().size());
//        average_Seq_Pair_Length = add_Seq_Pair_length / count_Seq_Pair;
//        this.seqPairContainer.setAverage_Seq_Pair_length(average_Seq_Pair_Length);
    }
    
    
    /**
     * Adds a new sequence pair mapping object to the list.
     * @param interimID id for this sequence pair mapping. Interim, because it
     * has to be shifted when inserting into database to guarantee uniqueness
     * @param interimMatepairID interimMatepairID id for mappings belonging to
     * same sequence pair. Interim, because it has to be shifted when inserting
     * into database to guarantee uniqueness
     * @param mappingId1
     * @param mappingId2
     * @param type type of the pair 0 = perfect, 1 = dist too large, 2 = dist
     * too small, 3 = orient wrong 4 = orient wrong and dist too large, 5 =
     * orient wrong and dist too small
     */
    private void addPairedRecord(SAMRecord mapping1, SAMRecord mapping2, int seqPairId, byte type) {
        
        mapping1.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, type);
        mapping1.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPairId);
        mapping1.setMateAlignmentStart(mapping2.getAlignmentStart());
        mapping2.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, type);
        mapping2.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPairId);
        mapping2.setMateAlignmentStart(mapping1.getAlignmentStart());
        this.samBamFileWriter.addAlignment(mapping1);
        this.samBamFileWriter.addAlignment(mapping2);
    }
    
    /**
     * Adds a new sequence pair mapping to the writer, if one of the records
     * is not already contained in the omit list. Also takes care that the
     * classification and sequence pair tags are set into the contained sam 
     * records. Note that the ordinary classification data is not set here!
     * @param potPair the potential pair to add to the sam/bam writer
     * @param omitList the omit list containing records, which where already added
     */
    private void addPairedRecord(DirectSeqPair potPair, List<SAMRecord> omitList) {
        SAMRecord record1 = potPair.getRecord1();
        SAMRecord record2 = potPair.getRecord2();
        if (!(omitList.contains(record1) || omitList.contains(record2))) {
            this.addClassificationData(record1);
            this.addClassificationData(record2);
            this.addPairedRecord(record1, record2, potPair.getSeqPairId(), potPair.getType());
            omitList.add(record1);
            omitList.add(record2);
            //TODO: replicate filtering in fetching the seq pairs!
        }
    }

    /**
     * Adds a single mapping (sam record) to the file writer and sets its seq 
     * pair and classification attributes.
     * @param record the unpaired record to write
     * @param seqPairId the sequence pair id of this single record
     */
    private void addSingleRecord(SAMRecord record, int seqPairId) {
        this.addClassificationData(record);
        record.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, Properties.TYPE_UNPAIRED_PAIR);
        record.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPairId);
        this.samBamFileWriter.addAlignment(record);
    }

    @Override
    public void update(Object args) {
        if (args instanceof String) {
            this.notifyObservers((String) args);
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
            observer.update(data);
        }
    }
    
    /**
     * Depending on deviation the min and max values of the distance between a
     * sequence pair is set.
     * @param dist distance in bases
     * @param deviation deviation in % (1-100)
     */
    private void calculateMinAndMaxDist(final int dist, final int deviation) {
        int devInBP = dist / 100 * deviation;
        this.minDist = dist - devInBP;
        this.maxDist = dist + devInBP;
    }

    /**
     * Adds the classification data (type and number of mapped positions) to the sam record.
     * Use this method, if the number of differences is already known.
     * @param record the sam record to update
     * @param differences the number of differences the record has to the reference
     */
    private void addClassificationData(SAMRecord record,  int differences) {
        if (this.classificationMap.get(record.getReadName()) != null) {
            int lowestDiffRate = this.classificationMap.get(record.getReadName()).getSecond();

            if (differences == 0) { //perfect mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.PERFECT_COVERAGE);

            } else if (differences == lowestDiffRate) { //best match mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.BEST_MATCH_COVERAGE);

            } else if (differences > lowestDiffRate) { //common mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.COMPLETE_COVERAGE);

            } else { //meaning: differences < lowestDiffRate
                this.notifyObservers("Cannot contain less than the lowest diff rate number of errors!");
            }
            record.setAttribute(Properties.TAG_MAP_COUNT, this.classificationMap.get(record.getReadName()).getFirst());
        } else {
            //TODO: currently no data is added to reads with errors, since they are not contained in the classification map
        }
    }
    
    /**
     * Adds the classification data (type and number of mapped positions) to the sam record.
     * Use this method, if the number of differences is not already known.
     * @param record the sam record to update
     */
    private void addClassificationData(SAMRecord record) {
        int differences = ParserCommonMethods.countDiffsAndGaps(
                record.getCigarString(), 
                record.getReadString(), 
                this.refSeq.substring(record.getAlignmentStart() - 1, record.getAlignmentEnd()),
                record.getReadNegativeStrandFlag());
        this.addClassificationData(record, differences);
    }

}
