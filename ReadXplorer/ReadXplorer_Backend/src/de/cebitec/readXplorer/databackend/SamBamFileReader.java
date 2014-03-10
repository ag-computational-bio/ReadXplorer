package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantDiff;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantDiffAndGapResult;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReadPairGroup;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.util.IndexFileNotificationPanel;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.SamUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.awt.Dialog;
import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeIOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * A SamBamFileReader has different methods to read data from a bam or sam file.
 *
 * @author -Rolf Hilker- <rhilker@cebitec.uni-bielefeld.de>
 */
public class SamBamFileReader implements Observable {

    public static final String cigarRegex = "[MIDNSPX=]+";
    private final File dataFile;
    private final int trackId;
    private final PersistantReference reference;
    private SamUtils samUtils;
    private SAMFileReader samFileReader;
    private String header;
    private List<Observer> observers;

    /**
     * A SamBamFileReader has different methods to read data from a bam or sam
     * file.
     * @param dataFile the file to read from
     * @param trackId the track id of the track whose data is stored in the
     * given file
     * @param reference reference genome used in the bam file
     * @throws RuntimeIOException
     */
    public SamBamFileReader(File dataFile, int trackId, PersistantReference reference) throws RuntimeIOException {
        this.observers = new ArrayList<>();
        this.dataFile = dataFile;
        this.trackId = trackId;
        this.reference = reference;
        this.samUtils = new SamUtils();
        
        this.initializeReader();
    }
    
    /**
     * Initializes or re-initializes the bam file reader.
     */
    private void initializeReader() {
        samFileReader = new SAMFileReader(this.dataFile);
        samFileReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
        header = samFileReader.getFileHeader().getTextHeader();
        this.checkIndex();
    }
    
    /**
     * Checks if the index of the bam file is present or creates it. If it
     * needs to be created, the gui is blocked by a dialog, which waits for
     * the finish signal of the index creation.
     * @return true, if the index already exists, false otherwise
     */
    private void checkIndex() {
        File indexFile = new File(dataFile.getAbsolutePath() + ".bai");
        if (!samFileReader.hasIndex() && !indexFile.exists()) { //first time after index creation the hasIndex method does not recognize the new index file
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("BAM index missing, recreating it...");
            progressHandle.start();
            
            final IndexFileNotificationPanel indexPanel = new IndexFileNotificationPanel();
            final JButton okButton = new JButton("OK");
            DialogDescriptor dialogDescriptor = new DialogDescriptor(indexPanel, "BAM index missing!", true, new JButton[]{okButton}, okButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
            Thread indexThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    samUtils.createIndex(samFileReader, new File(dataFile.getAbsolutePath().concat(Properties.BAM_INDEX_EXT)));
                    progressHandle.finish();
                    okButton.setEnabled(true);
                }
            });
            indexThread.start();
            Dialog indexDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            okButton.setEnabled(false);
            indexDialog.setVisible(true); 
        }
    }

    /**
     * Retrieves the mappings from the given interval from the sam or bam file
     * set for this data reader and the reference sequence with the given name.
     * @param request the request to carry out
     * @return the mappings for the given interval
     */
    public Collection<PersistantMapping> getMappingsFromBam(IntervalRequest request) {

        Collection<PersistantMapping> mappings = new ArrayList<>();
        
        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), request.getTotalFrom(), request.getTotalTo(), false);
            String refSubSeq;
            int id = 0;
            String cigar;
            SAMRecord record;
            int start;
            int stop;
            boolean isFwdStrand;
            Integer classification;
            Integer numMappingsForRead;
            PersistantMapping mapping;
            int numReplicates = 1;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                
                if (!record.getReadUnmappedFlag()) {
                    start = record.getAlignmentStart();
                    stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                    isFwdStrand = !record.getReadNegativeStrandFlag();
                    classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    //only add mappings, which are valid according to the read classification parameters
                    if (this.isIncludedMapping(classification, numMappingsForRead, request)) {

                        mapping = this.getMappingForValues(classification, numMappingsForRead, numReplicates, id++, start, stop, isFwdStrand);

                        if (request.isDiffsAndGapsNeeded() && classification != Properties.PERFECT_COVERAGE) {

                            //find check alignment via cigar string and add diffs to mapping
                            cigar = record.getCigarString();
                            if (cigar.contains("M")) { //TODO: check if this check is faster or the version in other methods here
                                refSubSeq = reference.getChromSequence(request.getChromId(), start, stop);
                            } else {
                                refSubSeq = null;
                            }
                            this.createDiffsAndGaps(record.getCigarString(), start, isFwdStrand, numReplicates,
                                    record.getReadString(), refSubSeq, mapping);
                        }
                        
                        //stuff for trimmed reads
                        Object originalSequence = record.getAttribute("os");
                        if ((originalSequence != null) && (originalSequence instanceof String)) {
                            String ors = (String) originalSequence;
                            ors = ors.replace("@", record.getReadString());
                            mapping.setOriginalSequence(ors);
                        }
                        Object trimmedFromLeft = record.getIntegerAttribute("tl");
                        if ((trimmedFromLeft != null) && (trimmedFromLeft instanceof Integer)) {
                            mapping.setTrimmedFromLeft((Integer) trimmedFromLeft);
                        }
                        Object trimmedFromRight = record.getIntegerAttribute("tr");
                        if ((trimmedFromRight != null) && (trimmedFromRight instanceof Integer)) {
                            mapping.setTrimmedFromRight((Integer) trimmedFromRight);
                        }

                        mappings.add(mapping);
                    }
                }

            }
            samRecordIterator.close();

        } catch (NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }
        
        return mappings;
    }

    /**
     * Retrieves the reduced mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name. Diffs and gaps are never included.
     * @param request the request to carry out
     * @return the reduced mappings for the given interval.  Diffs and gaps are 
     * never included.
     */
    public Collection<PersistantMapping> getReducedMappingsFromBam(IntervalRequest request) {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        Collection<PersistantMapping> mappings = new ArrayList<>();

        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);
            SAMRecord record;
            int start;
            int stop;
            Integer classification;
            Integer numMappingsForRead;
            boolean isFwdStrand;
            PersistantMapping mapping;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                if (!record.getReadUnmappedFlag()) {
                    classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    //only add mappings, which are valid according to the read classification paramters
                    if (this.isIncludedMapping(classification, numMappingsForRead, request)) {

                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        mapping = new PersistantMapping(start, stop, isFwdStrand, 1);
                        mappings.add(mapping);
                    }
                }
            }
            samRecordIterator.close();

        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }
        
        return mappings;
    }

    /**
     * Retrieves the read pair mappings from the given interval from the sam
     * or bam file set for this data reader and the reference sequence with the
     * given name.
     * @param request request to carry out
     * @return the coverage for the given interval
     */
    public Collection<PersistantReadPairGroup> getReadPairMappingsFromBam(IntervalRequest request) {
        Map<Long, PersistantReadPairGroup> readPairs = new HashMap<>();
        Collection<PersistantReadPairGroup> readPairGroups = new ArrayList<>();
        
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        
        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);
            String refSubSeq;
            int id = 0;
            String cigar;
            SAMRecord record;
            int startPos; //in the genome, to get the index: -1
            int stop;
            boolean isFwdStrand;
            Integer classification;
            Integer numMappingsForRead;
            Integer pairId;
            Integer pairType;
            long readPairId;
            ReadPairType readPairType;
            int mateStart;
            int mateStop;
            boolean bothVisible;
            PersistantMapping mapping;
            PersistantMapping mate;
            PersistantReadPairGroup newGroup;
            int numReplicates = 1;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                
                if (!record.getReadUnmappedFlag()) {
                    classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    if (this.isIncludedMapping(classification, numMappingsForRead, request)) {

                        startPos = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        pairId = (Integer) record.getAttribute(Properties.TAG_READ_PAIR_ID);
                        pairType = (Integer) record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        mateStart = record.getMateAlignmentStart();
                        bothVisible = mateStart > from && mateStart < to;

                        mapping = this.getMappingForValues(classification, numMappingsForRead, numReplicates, id++, startPos, stop, isFwdStrand);
                        if (pairId != null && pairType != null) { //since both data fields are always written together
//                // add new readPair if not exists
                            readPairId = (long) pairId;
                            readPairType = ReadPairType.getReadPairType(pairType);
                            if (!readPairs.containsKey(readPairId)) {
                                newGroup = new PersistantReadPairGroup();
                                newGroup.setReadPairId(pairId);
                                readPairs.put(readPairId, newGroup);
                            } //TODO: check where ids are needed
                            try {
                                mate = this.getMappingForValues(-1, -1, numReplicates, -1, mateStart, -1, !record.getMateNegativeStrandFlag());
                            } catch (IllegalStateException e) {
                                mate = this.getMappingForValues(-1, -1, numReplicates, -1, mateStart, -1, true);
                            } //TODO: get mate data from querried records later
                            readPairs.get(readPairId).addPersistantDirectAccessMapping(mapping, mate, readPairType, bothVisible);
                        }

                        if (request.isDiffsAndGapsNeeded() && classification != Properties.PERFECT_COVERAGE) {

                            //check alignment via cigar string and add diffs to mapping
                            cigar = record.getCigarString();
                            if (cigar.contains("M")) {
                                refSubSeq = reference.getChromSequence(request.getChromId(), startPos, stop);
                            } else {
                                refSubSeq = null;
                            }
                            this.createDiffsAndGaps(record.getCigarString(), startPos, isFwdStrand, numReplicates,
                                    record.getReadString(), refSubSeq, mapping);
                        }
                    }
                }
            }
            samRecordIterator.close();
            readPairGroups = readPairs.values();


        } catch (NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }

        return readPairGroups;
    }
    
    /**
     * Creates a mapping for the given classification and mapping data.
     * @param classification the classification data
     * @param numReplicates number of replicates of the mapping
     * @param id unique id of the mapping
     * @param startPos start position of the mapping
     * @param stop stop position of the mapping
     * @param isFwdStrand true, if the mapping is on the fwd strand
     * @return A new mapping with classification information, if classification is 
     * not null. Otherwise isBestMapping is currently always true.
     */
    private PersistantMapping getMappingForValues(Integer classification, Integer numMappingsForRead, int numReplicates, int id, 
            int startPos, int stop, boolean isFwdStrand) {
        int mappingsForRead = numMappingsForRead != null ? numMappingsForRead : -1;
        boolean isBestMapping = classification != null && (classification == (int) Properties.PERFECT_COVERAGE
                                || (classification == (int) Properties.BEST_MATCH_COVERAGE));
        return new PersistantMapping(id, startPos, stop, trackId, isFwdStrand, numReplicates, 0, 0, isBestMapping, mappingsForRead);
    }
    
    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * @param request the request to carry out
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResultPersistant getReadStartsFromBam(IntervalRequest request) {

        byte trackNeeded = request.getWhichTrackNeeded();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();

        PersistantCoverage coverage = new PersistantCoverage(from, to);
        if (trackNeeded == 0) {
            coverage.incArraysToIntervalSize();
        } else if (trackNeeded == PersistantCoverage.TRACK1 || trackNeeded == PersistantCoverage.TRACK2) {
            coverage.incDoubleTrackArraysToIntervalSize();
        }

        List<PersistantDiff> diffs = new ArrayList<>(); //both empty for read starts
        List<PersistantReferenceGap> gaps = new ArrayList<>();

        CoverageAndDiffResultPersistant result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);
        try {
            this.checkIndex();
            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Integer classification;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    if (!request.getReadClassParams().isOnlyUniqueReads()
                            || request.getReadClassParams().isOnlyUniqueReads() && numMappingsForRead != null && numMappingsForRead == 1) {
                        
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        startPos = isFwdStrand ? record.getAlignmentStart() : record.getAlignmentEnd();
                        
                        this.increaseCoverage(request, classification, 
                                trackNeeded, isFwdStrand, startPos, startPos, coverage);
                    }
                }
            }
            samRecordIterator.close();
            result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);

        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }

        return result;
    }
    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * @param request the request to carry out
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResultPersistant getCoverageAndReadStartsFromBam(IntervalRequest request) {

        byte trackNeeded = request.getWhichTrackNeeded();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();

        PersistantCoverage coverage = new PersistantCoverage(from, to);
        PersistantCoverage readStarts = new PersistantCoverage(from, to);
        if (trackNeeded == 0) {
            coverage.incArraysToIntervalSize();
            readStarts.incArraysToIntervalSize();
        } else if (trackNeeded == PersistantCoverage.TRACK1 || trackNeeded == PersistantCoverage.TRACK2) {
            coverage.incDoubleTrackArraysToIntervalSize();
            readStarts.incDoubleTrackArraysToIntervalSize();
        }

        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        PersistantDiffAndGapResult diffsAndGaps;

        CoverageAndDiffResultPersistant result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);
        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Integer classification;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            int stop;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    if (!request.getReadClassParams().isOnlyUniqueReads()
                            || request.getReadClassParams().isOnlyUniqueReads() && numMappingsForRead != null && numMappingsForRead == 1) {

                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        startPos = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        
                        this.increaseCoverage(request, classification, 
                                trackNeeded, isFwdStrand, startPos, stop, coverage);
                        if (isFwdStrand) {
                            this.increaseCoverage(request, classification, 
                                    trackNeeded, isFwdStrand, startPos, startPos, readStarts);
                        } else {
                            this.increaseCoverage(request, classification,
                                    trackNeeded, isFwdStrand, stop, stop, readStarts);
                        }

                        if (request.isDiffsAndGapsNeeded() && classification != Properties.PERFECT_COVERAGE
                                && request.getReadClassParams().isClassificationAllowed(classification)) {
                            diffsAndGaps = this.createDiffsAndGaps(record.getCigarString(),
                                    startPos, isFwdStrand, 1, record.getReadString(),
                                    reference.getChromSequence(request.getChromId(), startPos, stop), null);
                            diffs.addAll(diffsAndGaps.getDiffs());
                            gaps.addAll(diffsAndGaps.getGaps());
                        }
                    }
                }
            }
            samRecordIterator.close();
            result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);
            result.setReadStarts(readStarts);

        } catch (NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }

        return result;
    }

    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * @param request the request to carry out
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResultPersistant getCoverageFromBam(IntervalRequest request) {
        
        byte trackNeeded = request.getWhichTrackNeeded();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        
        PersistantCoverage coverage = new PersistantCoverage(from, to);
        if (trackNeeded == 0) {
            coverage.incArraysToIntervalSize();
        } else if (trackNeeded == PersistantCoverage.TRACK1 || trackNeeded == PersistantCoverage.TRACK2) {
            coverage.incDoubleTrackArraysToIntervalSize();
        }

        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        PersistantDiffAndGapResult diffsAndGaps;

        CoverageAndDiffResultPersistant result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);
        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Integer classification;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            int stop;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                
                if (!record.getReadUnmappedFlag()) {
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);

                    if (!request.getReadClassParams().isOnlyUniqueReads()
                            || request.getReadClassParams().isOnlyUniqueReads() && numMappingsForRead != null && numMappingsForRead == 1) {

                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                        startPos = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        
                        this.increaseCoverage(request, classification, trackNeeded, 
                                isFwdStrand, startPos, stop, coverage);

                        if (request.isDiffsAndGapsNeeded() && classification != Properties.PERFECT_COVERAGE && 
                                request.getReadClassParams().isClassificationAllowed(classification)) {
                            diffsAndGaps = this.createDiffsAndGaps(record.getCigarString(),
                                    startPos, isFwdStrand, 1, record.getReadString(),
                                    reference.getChromSequence(request.getChromId(), startPos, stop), null);
                            diffs.addAll(diffsAndGaps.getDiffs());
                            gaps.addAll(diffsAndGaps.getGaps());
                        }
                    }
                }
            }
            samRecordIterator.close();
            result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, request);

        } catch (NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }

        return result;
    }
    
    /**
     * Increases the coverage between the given start and stop position for all
     * mappings, that fulfill the wanted parameters and adds the coverage to
     * the needed arrays (indicated by the "trackNeeded" parameter).
     * @param request
     * @param numMappingsForRead
     * @param classification
     * @param trackNeeded
     * @param isFwdStrand
     * @param startPos
     * @param stopPos 
     */
    private void increaseCoverage(IntervalRequest request, Integer classification, 
            byte trackNeeded, boolean isFwdStrand, int startPos, int stopPos, PersistantCoverage coverage) {
        
        List<int[]> coverageArrays = new ArrayList<>();
        
        if (trackNeeded == 0) {
            //only the arrays, which are allowed to be updated are added to the coverage array list

            if (classification != null) {
                if (classification == Properties.PERFECT_COVERAGE) {

                    if (isFwdStrand) {
                        if (request.getReadClassParams().isPerfectMatchUsed()) {
                            coverageArrays.add(coverage.getPerfectFwdMult());
                        }
                        if (request.getReadClassParams().isBestMatchUsed()) {
                            coverageArrays.add(coverage.getBestMatchFwdMult());
                        }
                    } else {
                        if (request.getReadClassParams().isPerfectMatchUsed()) {
                            coverageArrays.add(coverage.getPerfectRevMult());
                        }
                        if (request.getReadClassParams().isBestMatchUsed()) {
                            coverageArrays.add(coverage.getBestMatchRevMult());
                        }
                    }
                }

                if ((classification == Properties.BEST_MATCH_COVERAGE)
                        && request.getReadClassParams().isBestMatchUsed()) {

                    if (isFwdStrand) {
                        coverageArrays.add(coverage.getBestMatchFwdMult());
                    } else {
                        coverageArrays.add(coverage.getBestMatchRevMult());
                    }
                }
            }

            if (request.getReadClassParams().isCommonMatchUsed()) {
                if (isFwdStrand) {
                    coverageArrays.add(coverage.getCommonFwdMult());
                } else {
                    coverageArrays.add(coverage.getCommonRevMult());
                }
            }

        } else if (trackNeeded == PersistantCoverage.TRACK1) {
            if (isFwdStrand) {
                coverageArrays.add(coverage.getCommonFwdMultCovTrack1());
            } else {
                coverageArrays.add(coverage.getCommonRevMultCovTrack1());
            }
        } else if (trackNeeded == PersistantCoverage.TRACK2) {
            if (isFwdStrand) {
                coverageArrays.add(coverage.getCommonFwdMultCovTrack2());
            } else {
                coverageArrays.add(coverage.getCommonRevMultCovTrack2());
            }
        }
        this.increaseCoverage(startPos, stopPos, request.getTotalFrom(), request.getTotalTo(), coverageArrays);
    }
    
    /**
     * Increases the coverage of the coverage arrays in the given list by one. 
     * In these arrays 0 is included.
     * @param startPos the start pos of the current read, inclusive
     * @param stop the stop pos of the current read, inclusive
     * @param from the start of the currently needed genome interval
     * @param to the stop of the currently needed genome interval
     * @param coverageArrays the coverage arrays whose positions should be 
     * updated
     */
    private void increaseCoverage(int startPos, int stop, int from, int to, List<int[]> coverageArrays) {
        int refPos;
        int indexPos;
//        start = start < 0 ? 0 : start;
//        stop = stop >= refSeq.length() ? refSeq.length() : stop;
        for (int i = 0; i <= stop - startPos; i++) {
            refPos = startPos + i; //example: 1000 = from, 999 = start, i = 0 -> refPos = 999, indexPos = -1;
            if (refPos >= from && refPos <= to) {
                indexPos = refPos - from;
                for (int[] covArray : coverageArrays) {
                    ++covArray[indexPos];
                }
            }
        }
    }

    /**
     * Counts and returns each difference to the reference sequence for a cigar
     * string and the belonging read sequence. If the operation "M" is not used
     * in the cigar, then the reference sequence can be null (it is not used in
     * this case). If the mapping is also handed over to the method, the diffs
     * and gaps are stored directly in the mapping.
     * @param cigar the cigar string containing the alignment operations
     * @param start the start position of the alignment on the chromosome
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq the reference sequence belonging to the cigar and without
     * gaps in upper case characters
     * @param mapping if a mapping is handed over to the method it adds the
     * diffs and gaps directly to the mapping and updates it's number of
     * differences to the reference. If null is passed, only the
     * PersistantDiffAndGapResult contains all the diff and gap data.
     * @return PersistantDiffAndGapResult containing all the diffs and gaps
     */
    private PersistantDiffAndGapResult createDiffsAndGaps(String cigar, int start, boolean isFwdStrand, int nbReplicates,
            String readSeq, String refSeq, PersistantMapping mapping) throws NumberFormatException {

        Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        int differences = 0;
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;//operation
        char base; //currently visited base
        String bases; //bases of the read interval under investigation
        String refBases; //bases of the reference corresponding to the read interval under investigation
        int currentCount;
        int refPos = 0;
        int readPos = 0;
        
        for (int i = 1; i < charCigar.length; ++i) {
            op = charCigar[i];
            currentCount = Integer.valueOf(num[i - 1]);
            
            if (op.equals("M")) { //check, count and add diffs for deviating Ms
                bases = readSeq.substring(readPos, readPos + currentCount).toUpperCase();
                refBases = refSeq.substring(refPos, refPos + currentCount);
                for (int j = 0; j < bases.length(); ++j) {
                    base = bases.charAt(j);
                    if (base != refBases.charAt(j)) {
                        ++differences;
                        if (!isFwdStrand) {
                            base = SequenceUtils.getDnaComplement(base);
                        }
                        PersistantDiff d = new PersistantDiff(refPos + j + start, base, isFwdStrand, nbReplicates);
                        this.addDiff(mapping, diffs, d);
                    }
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("=")) { //only increase position for matches
                refPos += currentCount;
                readPos += currentCount;
               
            } else if (op.equals("X")) { //count and create diffs for mismatches
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    base = Character.toUpperCase(readSeq.charAt(readPos + j));
                    if (!isFwdStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    PersistantDiff d = new PersistantDiff(refPos + j + start, base, isFwdStrand, nbReplicates);
                    this.addDiff(mapping, diffs, d);
                    
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("D")) { // count and add diff gaps for deletions in reference
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    PersistantDiff d = new PersistantDiff(refPos + j + start, '_', isFwdStrand, nbReplicates);
                    this.addDiff(mapping, diffs, d);
                }
                refPos += currentCount;
                // readPos remains the same
            
            } else if (op.equals("I")) { // count and add reference gaps for insertions
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    base = Character.toUpperCase(readSeq.charAt(readPos + j));
                    if (!isFwdStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    PersistantReferenceGap gap = new PersistantReferenceGap(refPos + start, base, 
                            CommonsMappingParser.getOrderForGap(refPos + start, gapOrderIndex), isFwdStrand, nbReplicates);
                    if (mapping != null) {
                        mapping.addGenomeGap(gap);
                    } else {
                        gaps.add(gap);
        }
                }
                //refPos remains the same
                readPos += currentCount;

            } else if (op.equals("N") || op.equals("P")) { //increase ref position for padded and skipped reference bases
                refPos += currentCount;
                //readPos remains the same

            } else if (op.equals("S")) { //increase read position for soft clipped bases which are present in the read
                //refPos remains the same
                readPos += currentCount;
            }
            //H = hard clipping does not contribute to differences
        }

        if (mapping != null) {
            mapping.setDifferences(differences);
        }

        return new PersistantDiffAndGapResult(diffs, gaps, gapOrderIndex, differences);
    }

    /**
     * Adds a diff either to the mapping, if it is not null, or to the diffs.
     * @param mapping the mapping to which the diff shall be added or <cc>null</cc>.
     * @param diffs the diffs list to which the diff shall be added.
     * @param diff the diff to add
     */
    private void addDiff(PersistantMapping mapping, List<PersistantDiff> diffs, PersistantDiff diff) {
        if (mapping != null) {
            mapping.addDiff(diff);
        } else {
            diffs.add(diff);
        }
    }
    
    /**
     * Closes this reader.
     */
    public void close() {
        this.samFileReader.close();
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
        for (Observer observer : observers) {
            observer.update(data);
        }
    }

    /**
     * Checks if the classification is valid according to the read class 
     * parameters contained in the interval request.
     * @param classification the classification to check
     * @param request the request whose parameters are used
     * @return true, if the mapping can be included in further steps, false
     * otherwise
     */
    private boolean isIncludedMapping(Integer classification, Integer numMappingsForRead, IntervalRequest request) {
        return (!request.getReadClassParams().isOnlyUniqueReads()
              || request.getReadClassParams().isOnlyUniqueReads() && numMappingsForRead != null && numMappingsForRead == 1)
                && 
                ((classification != null
                    && ((classification == Properties.PERFECT_COVERAGE
                        && request.getReadClassParams().isPerfectMatchUsed())
                    || ((classification == Properties.PERFECT_COVERAGE || classification == Properties.BEST_MATCH_COVERAGE)
                        && request.getReadClassParams().isBestMatchUsed())
                    || (classification == Properties.COMPLETE_COVERAGE
                        && request.getReadClassParams().isCommonMatchUsed())))
                || classification == null && request.getReadClassParams().isCommonMatchUsed());
    }
}
