package de.cebitec.readXplorer.readPairClassifier;

import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsedReadPairContainer;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.common.RefSeqFetcher;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.parser.mappings.ReadPairClassifierI;
import de.cebitec.readXplorer.parser.output.SamBamSorter;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.SamUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Sam/Bam read pair classifier for a direct file access track. This means
 * the classification of the read pairs has to be carried out. Besides the 
 * classification this class also acts as extender for the given sam/bam file
 * and thus creates an extended copy of the original file after the
 * classification. The reads are be sorted by read name for efficient
 * classification. Note for multichromosomal mappings: The classification works,
 * no matter on which chromosome the reads were mapped!
 *
 * @author Rolf Hilker
 */
@ServiceProvider(service = ReadPairClassifierI.class)
public class SamBamDirectReadPairClassifier implements ReadPairClassifierI, Observer, Observable {

    private ArrayList<Observer> observers;
    private TrackJob trackJob;
    private int dist;
    private int minDist;
    private int maxDist;
    private short orienation; //orientation of the reads: 0 = fr, 1 = rf, 2 = ff/rr
    private SAMFileWriter samBamFileWriter;
    private Map<String,ParsedClassification> classificationMap; 
    private final Map<String, Integer> chromLengthMap;
    private boolean deleteSortedFile;
    
    private StatsContainer statsContainer;
    private DiscreteCountingDistribution readPairSizeDistribution;
    private RefSeqFetcher refSeqFetcher;
    
    /**
     * Empty constructor, because nothing to do yet. But don't forget to set
     * data before calling classifyReadPairs().
     */
    public SamBamDirectReadPairClassifier() {
        //set data later
        this.observers = new ArrayList<>();
        this.chromLengthMap = new HashMap<>();
    }
    
    /**
     * Sam/Bam read pair classifier for a direct file access track. This
     * means the classification of the read pairs has to be carried out. Besides
     * the classification this class also acts as extender for the given sam/bam
     * file and thus creates an extended copy of the original file after the
     * classification. The reads are sorted by read name for efficient
     * classification. Note for multichromosomal mappings: The classification 
     * works, no matter on which chromosome the reads were mapped!
     * @param readPairJobContainer the read pair job container to classify
     * @param chromLengthMap the mapping of chromosome name to chromosome sequence
     * @param classificationMap the ordinary classification map of the reads. It
     *      is needed for the extension of the sam/bam file
     */
    public SamBamDirectReadPairClassifier(ReadPairJobContainer readPairJobContainer, Map<String, Integer> chromLengthMap, 
            Map<String,ParsedClassification> classificationMap) {
        this.observers = new ArrayList<>();
        this.trackJob = readPairJobContainer.getTrackJob1();
        this.dist = readPairJobContainer.getDistance();
        this.calculateMinAndMaxDist(dist, readPairJobContainer.getDeviation());
        this.orienation = readPairJobContainer.getOrientation();
        this.classificationMap = classificationMap;
        this.chromLengthMap = chromLengthMap;
        this.readPairSizeDistribution = new DiscreteCountingDistribution(maxDist * 3);
        readPairSizeDistribution.setType(Properties.READ_PAIR_SIZE_DISTRIBUTION);
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
     * @return an empty read pair container, because no data needs to be stored
     * @throws ParsingException  
     */
    @Override
    public ParsedReadPairContainer classifyReadPairs() throws ParsingException, OutOfMemoryError {

        this.refSeqFetcher = new RefSeqFetcher(trackJob.getRefGen().getFile(), this);
        this.preprocessData(trackJob);
        File oldWorkFile = trackJob.getFile();

        try {
            long start = System.currentTimeMillis();
            this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "Classifier.Classification.Start"));

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

            File outputFile = writerAndFile.getSecond();

            SAMRecord record;
            String lastReadName = "";
            String readNameFull; //read name with pair tag
            String readName; //read name without pair tag
            char pairTag;
            int readPairId = 1;
            while (samItor.hasNext()) {
                ++lineno;
                //separate all mappings of same pair by read pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag() && chromLengthMap.containsKey(record.getReferenceName())) {
                    readNameFull = record.getReadName();
                    pairTag = CommonsMappingParser.getReadPairTag(record);
                    readName = CommonsMappingParser.getReadNameWithoutPairTag(readNameFull);

                    if (!readName.equals(lastReadName)) { //meaning: next pair, because sorted by read name
                        // classify read pair, because all mappings for this pair are currently stored in list
                        this.performClassification(currentRecords1, currentRecords2, readPairId);
                        currentRecords1.clear();
                        currentRecords2.clear();
                        ++readPairId;

                    }
                    if (pairTag == Properties.EXT_A1) {
                        record.setReadPairedFlag(true);
                        record.setFirstOfPairFlag(true);
                        currentRecords1.add(record);
                    } else if (pairTag == Properties.EXT_A2) {
                        record.setReadPairedFlag(true);
                        record.setSecondOfPairFlag(true);
                        currentRecords2.add(record);
                    } else {
                        this.addSingleRecord(record, readPairId, 0, "*");
                    }
                    lastReadName = readName;
                }
            }

            if (!currentRecords1.isEmpty() || !currentRecords2.isEmpty()) {
                this.performClassification(currentRecords1, currentRecords2, readPairId);
            }

            samItor.close();
            this.samBamFileWriter.close();
            samBamReader.close();

            samBamReader = new SAMFileReader(outputFile);
            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            File indexFile = new File(outputFile.getAbsolutePath() + Properties.BAM_INDEX_EXT);
            SamUtils utils = new SamUtils();
            utils.createIndex(samBamReader, indexFile);
            samBamReader.close();

            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "Classifier.Classification.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));

            //delete the sorted/preprocessed file
            if (deleteSortedFile) {
                GeneralUtils.deleteOldWorkFile(oldWorkFile);
            }

            trackJob.setFile(outputFile);

            this.statsContainer.setReadPairDistribution(this.readPairSizeDistribution);

        } catch (MissingResourceException | IOException e) {
            this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "Classifier.Error", e.getMessage()));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
        }

        return new ParsedReadPairContainer();
    }

    /**
     * Actually performs the classification of the read pairs.
     * @param currentRecords1 all records of the same read pair
     * @return 
     */
    private void performClassification(List<SAMRecord> currentRecords1, List<SAMRecord> currentRecords2, int readPairId) {

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
        ParsedClassification class1;
        ParsedClassification class2;
        int diffs1;
        int diffs2;
        String refSubSeq1;
        String refSubSeq2;

        int largestSmallerDist = Integer.MIN_VALUE;
        int largestPotSmallerDist = Integer.MIN_VALUE;
        int largestUnorSmallerDist = Integer.MIN_VALUE;
        int largestPotUnorSmallerDist = Integer.MIN_VALUE;

        int orient1 = this.orienation == 1 ? -1 : 1;
        int dir = this.orienation == 2 ? 1 : -1;
        boolean case1;
        
        List<DirectReadPair> potPairList = new ArrayList<>(); //also perfect
        List<DirectReadPair> potSmallPairList = new ArrayList<>();        
        List<DirectReadPair> potPotSmallPairList = new ArrayList<>();        
        List<DirectReadPair> unorPairList = new ArrayList<>();
        List<DirectReadPair> potUnorPairList = new ArrayList<>();
        List<DirectReadPair> unorSmallPairList = new ArrayList<>();
        List<DirectReadPair> potUnorSmallPairList = new ArrayList<>();
        
        List<SAMRecord> omitList = new ArrayList<>(); //(enthält alle und werden step by step rausgelöscht)

        /*
         * 0 = fr -r1(1)-> <-r2(-1)- (stop1<start2) oder -r2(1)-> <-r1(-1)-
         * (stop2 < start1) 1 = rf <-r1(-1)- -r2(1)-> (stop1<start2) oder
         * <-r2(-1)- -r1(1)-> (stop2 < start1) 2 = ff -r1(1)-> -r2(1)->
         * (stop1<start2) oder <-r2(-1)- <-r1(-1)- (stop2 < start1)
         */
      
        if (!currentRecords2.isEmpty()) {
            //both sides of the read pair have been mapped
            
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
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.PERFECT_PAIR, currDist));
                    } else if (currDist < this.maxDist) { //both reads of pair mapped, but distance in reference is different
                        ///////////////////////////// imperfect pair, distance too small /////////////////////////////////
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.DIST_SMALL_PAIR, currDist));
                    } else { //////////////// imperfect pair, distance too large //////////////////////////
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.DIST_LARGE_PAIR, currDist));
                    }
                } else { //////////////////////////// inversion of one read ////////////////////////////////
                    currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                    ++currDist;

                    if (currDist <= this.maxDist && currDist >= this.minDist) {////distance fits, orientation not ///////////
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.ORIENT_WRONG_PAIR, currDist));
                    } else if (currDist < this.maxDist) {///// orientation wrong & distance too small //////////////////////////////
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.OR_DIST_SMALL_PAIR, currDist));
                    } else { //////////////// orientation wrong & distance too large //////////////////////////
                        this.addPairedRecord(new DirectReadPair(record1, record2, readPairId, ReadPairType.OR_DIST_LARGE_PAIR, currDist));
                    }
                }
            } else if (!currentRecords1.isEmpty()) {

                String totalReadName;
                DirectReadPair readPair;
                for (SAMRecord recordA : currentRecords1) { //block for one readname, pos and direction can deviate
                    
                    try {

                        direction = recordA.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                        start1 = recordA.getAlignmentStart();
                        stop1 = recordA.getAlignmentEnd();
                        totalReadName = CommonsMappingParser.elongatePairedReadName(recordA);
                        class1 = this.classificationMap.get(totalReadName);
                        refSubSeq1 = refSeqFetcher.getSubSequence(recordA.getReferenceName(), start1, stop1);
                        diffs1 = CommonsMappingParser.countDiffsAndGaps(
                                recordA.getCigarString(),
                                recordA.getReadString(),
                                refSubSeq1,
                                recordA.getReadNegativeStrandFlag());

                        for (SAMRecord recordB : currentRecords2) {

                            try {

                                if (!(omitList.contains(recordA) && omitList.contains(recordB))) {
                                    start2 = recordB.getAlignmentStart();
                                    stop2 = recordB.getAlignmentEnd();
                                    direction2 = recordB.getReadNegativeStrandFlag() ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD;
                                    totalReadName = CommonsMappingParser.elongatePairedReadName(recordB);
                                    class2 = this.classificationMap.get(totalReadName);
                                    refSubSeq2 = refSeqFetcher.getSubSequence(recordB.getReferenceName(), start2, stop2);
                                    diffs2 = CommonsMappingParser.countDiffsAndGaps(
                                            recordB.getCigarString(),
                                            recordB.getReadString(),
                                            refSubSeq2,
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
                                            readPair = new DirectReadPair(recordA, recordB, readPairId, ReadPairType.PERFECT_PAIR, currDist);
                                            if (diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches()) { //only perfect and best match mappings pass here
                                                CommonsMappingParser.addClassificationData(recordA, diffs1, classificationMap);
                                                CommonsMappingParser.addClassificationData(recordB, diffs2, classificationMap);
                                                this.addPairedRecord(readPair);
                                                omitList.add(recordA);
                                                omitList.add(recordB);
                                            } else {//////////////// store potential perfect pair ////////////////////////// for common mappings
                                                potPairList.add(readPair);
                                            }
                                        } else //////////////// distance too small, potential pair //////////////////////////
                                        if (currDist < this.maxDist) {
                                            readPair = new DirectReadPair(recordA, recordB, readPairId, ReadPairType.DIST_SMALL_PAIR, currDist);
                                            if (largestSmallerDist < currDist && diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches()) { //best mappings
                                                largestSmallerDist = currDist;
                                                potSmallPairList.add(readPair);
                                            } else if (largestPotSmallerDist < currDist) { //at least one common mapping in potential pair
                                                largestPotSmallerDist = currDist; //replace even smaller pair with this one (more likely)
                                                potPotSmallPairList.add(readPair);
                                            }
                                        } else {//////////////// distance too large //////////////////////////
                                            //currently nothing to do if dist too large
                                        }
                                    } else { //////////////////////////// inversion of one read ////////////////////////////////
                                        currDist = start1 < start2 ? stop2 - start1 : stop1 - start2;
                                        ++currDist;

                                        if (currDist <= this.maxDist && currDist >= this.minDist) { ////distance fits, orientation not ///////////
                                            readPair = new DirectReadPair(recordA, recordB, readPairId, ReadPairType.ORIENT_WRONG_PAIR, currDist);
                                            if (diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches()) { //best mappings
                                                unorPairList.add(readPair);
                                            } else {
                                                potUnorPairList.add(readPair);
                                            }
                                        } else if (currDist < this.maxDist && largestSmallerDist < currDist) {///// orientation wrong & distance too small //////////////////////////////
                                            readPair = new DirectReadPair(recordA, recordB, readPairId, ReadPairType.OR_DIST_SMALL_PAIR, currDist);
                                            if (largestUnorSmallerDist < currDist && diffs1 <= class1.getMinMismatches() && diffs2 <= class2.getMinMismatches()) { //best mappings
                                                largestUnorSmallerDist = currDist;
                                                unorSmallPairList.add(readPair);
                                            } else if (largestPotUnorSmallerDist < currDist) {
                                                largestPotUnorSmallerDist = currDist;
                                                potUnorSmallPairList.add(readPair);
                                            }
                                        } else { //////////////// orientation wrong & distance too large //////////////////////////
                                            //currently nothing to do
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "Classifier.UnclassifiedRead", recordA.getReadName()));
                            }
                        }
                        largestSmallerDist = Integer.MIN_VALUE;
                        largestPotSmallerDist = Integer.MIN_VALUE;
                        largestUnorSmallerDist = Integer.MIN_VALUE;
                        largestPotUnorSmallerDist = Integer.MIN_VALUE;
                    
                    } catch (NullPointerException e) {
                        this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "Classifier.UnclassifiedRead", recordA.getReadName()));
                    }
                }

                /*
                 * Determines order of insertion of pairs. If one id is
                 * contained in an earlier list, then it is ignored in all other
                 * lists!
                 */
                for (DirectReadPair pairMapping : potSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectReadPair pairMapping : unorPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectReadPair pairMapping : unorSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectReadPair pairMapping : potPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectReadPair pairMapping : potPotSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

                for (DirectReadPair pairMapping : potUnorSmallPairList) {
                    this.addPairedRecord(pairMapping, omitList);
                }

               SAMRecord mateRecord;
                for (SAMRecord record : currentRecords1) {
                    if (!omitList.contains(record)) { //so single mappings link to the first mapping of their partner read
                        mateRecord = currentRecords2.get(0);
                        this.addSingleRecord(record, readPairId, mateRecord.getAlignmentStart(), mateRecord.getReferenceName());
                    }
                }

                for (SAMRecord record : currentRecords2) {
                    if (!omitList.contains(record)) { //so single mappings link to the first mapping of their partner read
                        mateRecord = currentRecords1.get(0);
                        this.addSingleRecord(record, readPairId, mateRecord.getAlignmentStart(), mateRecord.getReferenceName());
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
                    this.addSingleRecord(record, readPairId, 0, "*");
                }
            }

        } else { //only one side of the read pair could be mapped
            for (SAMRecord record : currentRecords1) { //pos and direction can deviate
                this.addSingleRecord(record, readPairId, 0, "*");
            }
        }
    }
    
    
    /**
     * Adds a new read pair mapping object to the list and sets necessary
     * sam flags for both records.
     * @param readPair the read pair to add
     * 
     */
    private void addPairedRecord(DirectReadPair readPair) {
        SAMRecord mapping1 = readPair.getRecord1();
        SAMRecord mapping2 = readPair.getRecord2();
        this.setReadPairForType(readPair);
        mapping1.setAttribute(Properties.TAG_READ_PAIR_TYPE, readPair.getType());
        mapping1.setAttribute(Properties.TAG_READ_PAIR_ID, readPair.getReadPairId());
        mapping1.setMateReferenceName(mapping2.getReferenceName());
        mapping1.setMateAlignmentStart(mapping2.getAlignmentStart());
        mapping1.setMateNegativeStrandFlag(mapping2.getReadNegativeStrandFlag());
        mapping1.setProperPairFlag(readPair.getType() == ReadPairType.PERFECT_PAIR || readPair.getType() == ReadPairType.PERFECT_UNQ_PAIR);
        
        mapping2.setAttribute(Properties.TAG_READ_PAIR_TYPE, readPair.getType());
        mapping2.setAttribute(Properties.TAG_READ_PAIR_ID, readPair.getReadPairId());
        mapping2.setMateReferenceName(mapping1.getReferenceName());
        mapping2.setMateAlignmentStart(mapping1.getAlignmentStart());
        mapping2.setMateNegativeStrandFlag(mapping1.getReadNegativeStrandFlag());
        mapping2.setProperPairFlag(readPair.getType() == ReadPairType.PERFECT_PAIR || readPair.getType() == ReadPairType.PERFECT_UNQ_PAIR);
        
        if (mapping1.getAlignmentStart() < mapping2.getAlignmentStart()) { //different chromosomes means there is no distance here according to sam spec. Still think about a solution for stats
            mapping1.setInferredInsertSize(readPair.getDistance());
            mapping2.setInferredInsertSize(-readPair.getDistance());
        } else {
            mapping1.setInferredInsertSize(-readPair.getDistance());
            mapping2.setInferredInsertSize(readPair.getDistance());
        }
        
        this.samBamFileWriter.addAlignment(mapping1);
        this.samBamFileWriter.addAlignment(mapping2);
        this.readPairSizeDistribution.increaseDistribution(readPair.getDistance());
        this.statsContainer.incReadPairStats(readPair.getType(), 1);
    }
    
    /**
     * Adds a new read pair mapping to the writer, if one of the records
     * is not already contained in the omit list. Also takes care that the
     * classification and read pair tags are set into the contained sam 
     * records. Note that the ordinary classification data is not set here!
     * @param potPair the potential pair to add to the sam/bam writer
     * @param omitList the omit list containing records, which where already added
     */
    private void addPairedRecord(DirectReadPair potPair, List<SAMRecord> omitList) {
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
     * Adds a single mapping (sam record) to the file writer and sets its read 
     * pair and classification attributes.
     * @param record the unpaired record to write
     * @param readPairId the read pair id of this single record
     * @param mateUnmapped true, if the mate of this mapping is unmapped, false
     * if it is mapped, but does not form a pair with this record
     */
    private void addSingleRecord(SAMRecord record, int readPairId, int mateStart, String mateRef) {
        record.setMateReferenceName(mateRef);
        record.setMateAlignmentStart(mateStart);
        record.setMateUnmappedFlag(mateStart == 0);
        record.setNotPrimaryAlignmentFlag(mateStart != 0);
        this.addClassificationData(record);
        record.setAttribute(Properties.TAG_READ_PAIR_TYPE, ReadPairType.UNPAIRED_PAIR);
        record.setAttribute(Properties.TAG_READ_PAIR_ID, readPairId);
        this.samBamFileWriter.addAlignment(record);
        this.statsContainer.incReadPairStats(ReadPairType.UNPAIRED_PAIR, 1);
    }

    /**
     * Updated the read pair for a given read pair. If both reads are
     * only mapped once, the corresponding unique read pair type is chosen.
     * @param readPair the read pair to update in case it is unique
     */
    private void setReadPairForType(DirectReadPair readPair) {
        Integer mapCount1 = this.getMapCount(readPair.getRecord1());
        Integer mapCount2 = this.getMapCount(readPair.getRecord2());
        if (mapCount1 != null && mapCount2 != null && mapCount1 == 1 && mapCount2 == 1) {
            switch (readPair.getType()) {
                case PERFECT_PAIR : readPair.setType(ReadPairType.PERFECT_UNQ_PAIR); break;
                case DIST_SMALL_PAIR : readPair.setType(ReadPairType.DIST_SMALL_UNQ_PAIR); break;
                case DIST_LARGE_PAIR : readPair.setType(ReadPairType.DIST_LARGE_UNQ_PAIR); break;
                case ORIENT_WRONG_PAIR : readPair.setType(ReadPairType.ORIENT_WRONG_UNQ_PAIR); break;
                case OR_DIST_SMALL_PAIR : readPair.setType(ReadPairType.OR_DIST_SMALL_UNQ_PAIR); break;
                case OR_DIST_LARGE_PAIR : readPair.setType(ReadPairType.OR_DIST_LARGE_UNQ_PAIR); break;
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
        String readName = CommonsMappingParser.elongatePairedReadName(record);
        if (this.classificationMap.get(readName) != null) {
            mapCount = this.classificationMap.get(readName).getNumberOccurrences();
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
     * read pair is set.
     * @param dist distance in bases
     * @param deviation deviation in % (1-100)
     * @return the maximum distance of a mapping pair, which is accepted as valid 
     */
    protected int calculateMinAndMaxDist(final int dist, final int deviation) {
        int devInBP = dist / 100 * deviation;
        this.minDist = dist - devInBP;
        this.maxDist = dist + devInBP;
        return maxDist;
    }
    
    /**
     * Adds the classification data (type and number of mapped positions) to the sam record.
     * Use this method, if the number of differences is not already known.
     * @param record the sam record to update
     */
    private void addClassificationData(SAMRecord record) { //TODO: avoid recalculating diffs and gaps!!!
        String refSubSeq = refSeqFetcher.getSubSequence(record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd());
        int differences = CommonsMappingParser.countDiffsAndGaps(
                record.getCigarString(), 
                record.getReadString(), 
                refSubSeq,
                record.getReadNegativeStrandFlag());
        CommonsMappingParser.addClassificationData(record, differences, classificationMap);
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
