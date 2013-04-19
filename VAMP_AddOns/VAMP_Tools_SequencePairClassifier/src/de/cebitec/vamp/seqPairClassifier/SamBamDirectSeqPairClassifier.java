package de.cebitec.vamp.seqPairClassifier;

import de.cebitec.vamp.parser.SeqPairJobContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.parser.mappings.SeqPairClassifierI;
import de.cebitec.vamp.parser.output.SamBamSorter;
import de.cebitec.vamp.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.*;
import org.openide.util.NbBundle;

/**
 * Sam/Bam sequence pair classifier for a direct file access track. This means
 * the classification of the seq pairs has to be carried out. Besides the 
 * classification this class also acts as extender for the given sam/bam file
 * and thus creates an extended copy of the original file after the
 * classification. The reads are be sorted by read name for efficient
 * classification. Note for multichromosomal mappings: The classification works,
 * no matter on which chromosome the reads were mapped!
 *
 * @author Rolf Hilker
 */
public class SamBamDirectSeqPairClassifier implements SeqPairClassifierI, Observer, Observable {

    private ArrayList<Observer> observers;
    private TrackJob trackJob;
    private int dist;
    private int minDist;
    private int maxDist;
    private short orienation; //orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
    private SAMFileWriter samBamFileWriter;
    private Map<String,Pair<Integer,Integer>> classificationMap; 
    private final String refSeq;
    private boolean deleteSortedFile;
    
    private StatsContainer statsContainer;
    private DiscreteCountingDistribution seqPairSizeDistribution;
    
    /**
     * Sam/Bam sequence pair classifier for a direct file access track. This
     * means the classification of the seq pairs has to be carried out. Besides
     * the classification this class also acts as extender for the given sam/bam
     * file and thus creates an extended copy of the original file after the
     * classification. The reads are sorted by read name for efficient
     * classification. Note for multichromosomal mappings: The classification 
     * works, no matter on which chromosome the reads were mapped!
     * @param seqPairJobContainer the sequence pair job container to classify
     * @param refSeq the reference sequence belonging to the sequence pair job
     * @param classificationMap the ordinary classification map of the reads. It
     *      is needed for the extension of the sam/bam file
     */
    public SamBamDirectSeqPairClassifier(SeqPairJobContainer seqPairJobContainer, String refSeq, 
            Map<String,Pair<Integer,Integer>> classificationMap) {
        this.observers = new ArrayList<>();
        this.trackJob = seqPairJobContainer.getTrackJob1();
        this.dist = seqPairJobContainer.getDistance();
        this.calculateMinAndMaxDist(dist, seqPairJobContainer.getDeviation());
        this.orienation = seqPairJobContainer.getOrientation();
        this.classificationMap = classificationMap;
        this.refSeq = refSeq;
        this.seqPairSizeDistribution = new DiscreteCountingDistribution(maxDist * 2);
        seqPairSizeDistribution.setType(Properties.SEQ_PAIR_SIZE_DISTRIBUTION);
    }
    
    /**
     * Sorts the file by read name.
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        SamBamSorter sorter = new SamBamSorter();
        sorter.registerObserver(this);
        boolean success = sorter.sortSamBam(trackJob, SAMFileHeader.SortOrder.queryname, SamUtils.SORT_READNAME_STRING);
        this.deleteSortedFile = success;
        return success;
    }

    /**
     * First preprocesses the track job stored in this classifier by sorting it
     * by read name in this implementation and then classifies the seuqence
     * pairs. The reads have to be sorted by read name for efficient
     * classification.
     * @return an empty seq pair container, because no data needs to be stored
     * @throws ParsingException  
     */
    @Override
    public ParsedSeqPairContainer classifySeqPairs() throws ParsingException, OutOfMemoryError {
        
        this.preprocessData(trackJob);
        File oldWorkFile = trackJob.getFile();
        
        try {
            long start = System.currentTimeMillis();
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Classification.Start"));
            
            List<SAMRecord> currentRecords1 = new ArrayList<>();
            List<SAMRecord> currentRecords2 = new ArrayList<>();       
            
            int lineno = 0;
            SAMFileReader samBamReader = new SAMFileReader(trackJob.getFile());
            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = samBamReader.iterator();
            
            SAMFileHeader header = samBamReader.getFileHeader();
            header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
            
            Pair<SAMFileWriter, File> writerAndFile = SamUtils.createSamBamWriter(
                    trackJob.getFile(), header, false, SamUtils.EXTENDED_STRING);
            
            this.samBamFileWriter = writerAndFile.getFirst();
            String refName = trackJob.getRefGen().getName();
            
            File outputFile = writerAndFile.getSecond();
            
            SAMRecord record;
            String lastReadName = "";
            String readNameFull; //read name with pair tag
            String readName; //read name without pair tag
            char pairTag;
            int seqPairId = 1;
            while (samItor.hasNext()) {
                ++lineno;
                //separate all mappings of same pair by seq pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {
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
                    if (pairTag == Properties.EXT_A1 || pairTag == Properties.EXT_B1 || record.getReadPairedFlag() && record.getFirstOfPairFlag()) { //TODO: add new casava tag
                        record.setReadPairedFlag(true);
                        record.setFirstOfPairFlag(true);
                        currentRecords1.add(record);
                    } else if (pairTag == Properties.EXT_A2 || pairTag == Properties.EXT_B2 || record.getReadPairedFlag() && record.getSecondOfPairFlag()) {
                        record.setReadPairedFlag(true);
                        record.setSecondOfPairFlag(true);
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
            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            File indexFile = new File(outputFile.getAbsolutePath() + ".bai");
            SamUtils utils = new SamUtils();
            utils.createIndex(samBamReader, indexFile);
            samBamReader.close();
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Classification.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));
            
            //delete the sorted/preprocessed file
            if (deleteSortedFile) { GeneralUtils.deleteOldWorkFile(oldWorkFile); }
            
            trackJob.setFile(outputFile);
            
            this.statsContainer.setSeqPairDistribution(this.seqPairSizeDistribution);
            
        } catch (Exception e) {
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "Classifier.Error", e.getMessage()));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
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
      
        if (!currentRecords2.isEmpty()) {
            //both sides of the sequence pair have been mapped
            
            pairSize = currentRecords1.size() == 1 && currentRecords2.size() == 1;

            if (pairSize) { //only one mapping per readname = we can always store a pair object

                record1 = currentRecords1.get(0);
                record2 = currentRecords2.get(0);
                direction = record1.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                start1 = record1.getAlignmentStart();
                stop1 = record1.getAlignmentEnd();

                start2 = record2.getAlignmentStart();
                stop2 = record2.getAlignmentEnd();
                direction2 = record2.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                //ensures direction values only in 1 and -1 and dir1 != dir2 or equal in case ff/rr
                
                //add classification data here, before passing mapping to writer
                this.addClassificationData(record1);
                this.addClassificationData(record2);
                
                case1 = direction == orient1 && start1 < start2;
                if ((case1 || direction == -orient1 && start2 < start1)
                        && direction == dir * direction2) {

                    //determine insert size between both reads
                    if (case1) {
                        currDist = Math.abs(start1 - stop2) + 1; //distance if on different chromosomes??? read 1 + rest chr1 + start chr2 bis read2?
                    } else {
                        currDist = Math.abs(start2 - stop1) + 1;
                    }

                    if (currDist <= this.maxDist && currDist >= this.minDist) {
                        ///////////////////////////// found a perfect pair! /////////////////////////////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_PERFECT_PAIR, currDist));
                    } else if (currDist < this.maxDist) { //both reads of pair mapped, but distance in reference is different
                        ///////////////////////////// imperfect pair, distance too small /////////////////////////////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_DIST_SMALL_PAIR, currDist));
                    } else { //////////////// imperfect pair, distance too large //////////////////////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_DIST_LARGE_PAIR, currDist));
                    }
                } else { //////////////////////////// inversion of one read ////////////////////////////////
                    currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                    ++currDist;

                    if (currDist <= this.maxDist && currDist >= this.minDist) {////distance fits, orientation not ///////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_ORIENT_WRONG_PAIR, currDist));
                    } else if (currDist < this.maxDist) {///// orientation wrong & distance too small //////////////////////////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR, currDist));
                    } else { //////////////// orientation wrong & distance too large //////////////////////////
                        this.addPairedRecord(new DirectSeqPair(record1, record2, seqPairId, Properties.TYPE_OR_DIST_LARGE_PAIR, currDist));
                    }
                }
            } else if (!currentRecords1.isEmpty()) {

                String totalReadName;
                DirectSeqPair seqPair;
                for (SAMRecord recordA : currentRecords1) { //block for one readname, pos and direction can deviate
                    
                    try {

                        direction = recordA.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                        start1 = recordA.getAlignmentStart();
                        stop1 = recordA.getAlignmentEnd();
                        totalReadName = ParserCommonMethods.elongatePairedReadName(recordA);
                        class1 = this.classificationMap.get(totalReadName);
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
                                    direction2 = recordB.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                                    totalReadName = ParserCommonMethods.elongatePairedReadName(recordB);
                                    class2 = this.classificationMap.get(totalReadName);
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
                                            currDist = Math.abs(start1 - stop2) + 1;
                                        } else {
                                            currDist = Math.abs(start2 - stop1) + 1;
                                        }
                                        if (currDist <= this.maxDist && currDist >= this.minDist) { //distance fits
                                            ///////////////////////////// found a perfect pair! /////////////////////////////////
                                            seqPair = new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_PERFECT_PAIR, currDist);
                                            if (diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //only perfect and best match mappings pass here
                                                this.addClassificationData(recordA, diffs1);
                                                this.addClassificationData(recordB, diffs2);
                                                this.addPairedRecord(seqPair);
                                                omitList.add(recordA);
                                                omitList.add(recordB);
                                            } else {//////////////// store potential perfect pair ////////////////////////// for common mappings
                                                potPairList.add(seqPair);
                                            }
                                        } else //////////////// distance too small, potential pair //////////////////////////
                                        if (currDist < this.maxDist) {
                                            seqPair = new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_DIST_SMALL_PAIR, currDist);
                                            if (largestSmallerDist < currDist && diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                largestSmallerDist = currDist;
                                                potSmallPairList.add(seqPair);
                                            } else if (largestPotSmallerDist < currDist) { //at least one common mapping in potential pair
                                                largestPotSmallerDist = currDist; //replace even smaller pair with this one (more likely)
                                                potPotSmallPairList.add(seqPair);
                                            }
                                        } else {//////////////// distance too large //////////////////////////
                                            //TODO: something to do if dist too large??
                                        }
                                    } else { //////////////////////////// inversion of one read ////////////////////////////////
                                        currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                                        ++currDist;

                                        if (currDist <= this.maxDist && currDist >= this.minDist) { ////distance fits, orientation not ///////////
                                            seqPair = new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_ORIENT_WRONG_PAIR, currDist);
                                            if (diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                unorPairList.add(seqPair);
                                            } else {
                                                potUnorPairList.add(seqPair);
                                            }
                                        } else if (currDist < this.maxDist && largestSmallerDist < currDist) {///// orientation wrong & distance too small //////////////////////////////
                                            seqPair = new DirectSeqPair(recordA, recordB, seqPairId, Properties.TYPE_OR_DIST_SMALL_PAIR, currDist);
                                            if (largestUnorSmallerDist < currDist && diffs1 <= class1.getSecond() && diffs2 <= class2.getSecond()) { //best mappings
                                                largestUnorSmallerDist = currDist;
                                                unorSmallPairList.add(seqPair);
                                            } else if (largestPotUnorSmallerDist < currDist) {
                                                largestPotUnorSmallerDist = currDist;
                                                potUnorSmallPairList.add(seqPair);
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
                        this.addSingleRecord(mapping, seqPairId, false);
                    }
                }

                for (SAMRecord mapping : currentRecords2) {
                    if (!omitList.contains(mapping)) {
                        this.addSingleRecord(mapping, seqPairId, false);
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
            } else {
                for (SAMRecord record : currentRecords2) { //pos and direction can deviate
                    this.addSingleRecord(record, seqPairId, true);
                }
            }

        } else { //only one side of the sequence pair could be mapped
            for (SAMRecord record : currentRecords1) { //pos and direction can deviate
                this.addSingleRecord(record, seqPairId, true);
            }
        }
    }
    
    
    /**
     * Adds a new sequence pair mapping object to the list and sets necessary
     * sam flags for both records.
     * @param mapping1 mapping 1 of the read pair to add
     * @param mapping2 mapping 2 of the read pair to add
     * @param seqPairId id of the sequence pair to add
     * @param type type of the pair 0 = perfect, 1 = dist too large, 2 = dist
     * too small, 3 = orient wrong 4 = orient wrong and dist too large, 5 =
     * orient wrong and dist too small
     * @param distance distance of the read pair
     */
    private void addPairedRecord(DirectSeqPair seqPair) {
        SAMRecord mapping1 = seqPair.getRecord1();
        SAMRecord mapping2 = seqPair.getRecord2();
        this.setSeqPairForType(seqPair);
        mapping1.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, seqPair.getType());
        mapping1.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPair.getSeqPairId());
        mapping1.setMateAlignmentStart(mapping2.getAlignmentStart());
        mapping1.setMateNegativeStrandFlag(mapping2.getReadNegativeStrandFlag());
        mapping1.setProperPairFlag(seqPair.getType() == Properties.TYPE_PERFECT_PAIR || seqPair.getType() == Properties.TYPE_PERFECT_UNQ_PAIR);
        
        mapping2.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, seqPair.getType());
        mapping2.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPair.getSeqPairId());
        mapping2.setMateAlignmentStart(mapping1.getAlignmentStart());
        mapping2.setMateNegativeStrandFlag(mapping1.getReadNegativeStrandFlag());
        mapping2.setProperPairFlag(seqPair.getType() == Properties.TYPE_PERFECT_PAIR || seqPair.getType() == Properties.TYPE_PERFECT_UNQ_PAIR);
        
        if (mapping1.getAlignmentStart() < mapping2.getAlignmentStart()) { //different chromosomes means there is no distance here according to sam spec. Still think about a solution for stats
            mapping1.setInferredInsertSize(seqPair.getDistance());
            mapping2.setInferredInsertSize(-seqPair.getDistance());
        } else {
            mapping1.setInferredInsertSize(-seqPair.getDistance());
            mapping2.setInferredInsertSize(seqPair.getDistance());
        }
        
        this.samBamFileWriter.addAlignment(mapping1);
        this.samBamFileWriter.addAlignment(mapping2);
        this.seqPairSizeDistribution.increaseDistribution(seqPair.getDistance());
        this.statsContainer.incSeqPairStats(seqPair.getType(), 1);
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
            this.addPairedRecord(potPair);
            omitList.add(record1);
            omitList.add(record2);
        }
    }

    /**
     * Adds a single mapping (sam record) to the file writer and sets its seq 
     * pair and classification attributes.
     * @param record the unpaired record to write
     * @param seqPairId the sequence pair id of this single record
     * @param mateUnmapped true, if the mate of this mapping is unmapped, false
     * if it is mapped, but does not form a pair with this record
     */
    private void addSingleRecord(SAMRecord record, int seqPairId, boolean mateUnmapped) {
        if (mateUnmapped) {
            record.setMateReferenceName("*");
            record.setMateAlignmentStart(0);
        }
        record.setMateUnmappedFlag(mateUnmapped);
        record.setNotPrimaryAlignmentFlag(!mateUnmapped);
        this.addClassificationData(record);
        record.setAttribute(Properties.TAG_SEQ_PAIR_TYPE, Properties.TYPE_UNPAIRED_PAIR);
        record.setAttribute(Properties.TAG_SEQ_PAIR_ID, seqPairId);
        this.samBamFileWriter.addAlignment(record);
        this.statsContainer.incSeqPairStats(Properties.TYPE_UNPAIRED_PAIR, 1);
    }

    /**
     * Updated the sequence pair for a given sequence pair. If both reads are
     * only mapped once, the corresponding unique sequence pair type is chosen.
     * @param seqPair the sequence pair to update in case it is unique
     */
    private void setSeqPairForType(DirectSeqPair seqPair) {
        Integer mapCount1 = this.getMapCount(seqPair.getRecord1());
        Integer mapCount2 = this.getMapCount(seqPair.getRecord2());
        if (mapCount1 != null && mapCount2 != null && mapCount1 == 1 && mapCount2 == 1) {
            switch (seqPair.getType()) {
                case Properties.TYPE_PERFECT_PAIR : seqPair.setType(Properties.TYPE_PERFECT_UNQ_PAIR); break;
                case Properties.TYPE_DIST_SMALL_PAIR : seqPair.setType(Properties.TYPE_DIST_SMALL_UNQ_PAIR); break;
                case Properties.TYPE_DIST_LARGE_PAIR : seqPair.setType(Properties.TYPE_DIST_LARGE_UNQ_PAIR); break;
                case Properties.TYPE_ORIENT_WRONG_PAIR : seqPair.setType(Properties.TYPE_ORIENT_WRONG_UNQ_PAIR); break;
                case Properties.TYPE_OR_DIST_SMALL_PAIR : seqPair.setType(Properties.TYPE_OR_DIST_SMALL_UNQ_PAIR); break;
                case Properties.TYPE_OR_DIST_LARGE_PAIR : seqPair.setType(Properties.TYPE_OR_DIST_LARGE_UNQ_PAIR); break;
                default: //change nothing
            }
        }
    }

    /**
     * Calculates the mapping count of a sam record. Note, that the count can be
     * null, if no classification was carried out until now.
     *
     * @param record the record whose mapping count is needed
     * @return the mapping count of the given sam record
     */
    private Integer getMapCount(SAMRecord record) {
        Integer mapCount = null;
        String readName = ParserCommonMethods.elongatePairedReadName(record);
        if (this.classificationMap.get(readName) != null) {
            mapCount = this.classificationMap.get(readName).getFirst();
        }
        return mapCount;
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
    protected int calculateMinAndMaxDist(final int dist, final int deviation) {
        int devInBP = dist / 100 * deviation;
        this.minDist = dist - devInBP;
        this.maxDist = dist + devInBP;
        return maxDist;
    }

    /**
     * Adds the classification data (type and number of mapped positions) to the sam record.
     * Use this method, if the number of differences is already known.
     * @param record the sam record to update
     * @param differences the number of differences the record has to the reference
     */
    private void addClassificationData(SAMRecord record,  int differences) {
        String readName = ParserCommonMethods.elongatePairedReadName(record);
        if (this.classificationMap.get(readName) != null) {
            int lowestDiffRate = this.classificationMap.get(readName).getSecond();

            if (differences == 0) { //perfect mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.PERFECT_COVERAGE);

            } else if (differences == lowestDiffRate) { //best match mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.BEST_MATCH_COVERAGE);

            } else if (differences > lowestDiffRate) { //common mapping
                record.setAttribute(Properties.TAG_READ_CLASS, Properties.COMPLETE_COVERAGE);

            } else { //meaning: differences < lowestDiffRate
                this.notifyObservers("Cannot contain less than the lowest diff rate number of errors!");
            }
            record.setAttribute(Properties.TAG_MAP_COUNT, this.classificationMap.get(readName).getFirst());
        } else {
            //currently no data is added to reads with errors, since they are not contained in the classification map
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

    /**
     * Sets the stats container to keep track of statistics for this track.
     * @param statsContainer The stats container to add
     */
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

    /**
     * @return The stats container to keep track of statistics for this track.
     */
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
}
