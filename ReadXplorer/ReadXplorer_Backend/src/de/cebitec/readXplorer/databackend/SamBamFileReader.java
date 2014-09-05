/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResult;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.Difference;
import de.cebitec.readXplorer.databackend.dataObjects.Mapping;
import de.cebitec.readXplorer.databackend.dataObjects.DiffAndGapResult;
import de.cebitec.readXplorer.databackend.dataObjects.ReadPairGroup;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.ReferenceGap;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.util.IndexFileNotificationPanel;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.SamAlignmentBlock;
import de.cebitec.readXplorer.util.SamUtils;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import java.awt.Dialog;
import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
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
import org.openide.util.NbPreferences;

/**
 * A SamBamFileReader has different methods to read data from a bam or sam file.
 *
 * @author -Rolf Hilker- <rhilker@cebitec.uni-bielefeld.de>
 */
public class SamBamFileReader implements Observable {

    /**
     * 0 = Default mapping quality value, if it is unknown.
     */
    public static final int UNKNOWN_MAP_QUAL = 0;
    /**
     * 255 = Default mapping quality value according to SAM spec, if it was not
     * calculated.
     */
    public static final int DEFAULT_MAP_QUAL = 255;
    /**
     * -1 = Replacement of 255 from SAM spec. Enables us to use byte.
     */
    public static final int UNKNOWN_CALCULATED_MAP_QUAL = -1;

    public static final String cigarRegex = "[MIDNSPXH=]+";
    private final File dataFile;
    private final int trackId;
    private final PersistentReference reference;
    private SamUtils samUtils;
    private SAMFileReader samFileReader;
    private String header;
    private List<Observer> observers;
    private final Preferences pref = NbPreferences.forModule(Object.class);
    private boolean useExtendedClassification = pref.getBoolean(Properties.VIEWER_CLASSIFICATION, false); //TODO: change this for future version

    /**
     * A SamBamFileReader has different methods to read data from a bam or sam
     * file.
     *
     * @param dataFile the file to read from
     * @param trackId the track id of the track whose data is stored in the
     * given file
     * @param reference reference genome used in the bam file
     * @throws RuntimeIOException
     */
    public SamBamFileReader(File dataFile, int trackId, PersistentReference reference) throws RuntimeIOException {
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
     * Checks if the index of the bam file is present or creates it. If it needs
     * to be created, the gui is blocked by a dialog, which waits for the finish
     * signal of the index creation.
     *
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
    public Collection<Mapping> getMappingsFromBam(IntervalRequest request) {

        Collection<Mapping> mappings = new ArrayList<>();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(
                    request.getChromId()).getName(), request.getTotalFrom(), request.getTotalTo(), false);
            String refSubSeq;
            int id = 0;
            String cigar;
            SAMRecord record;
            int start;
            int stop;
            boolean isFwdStrand;
            Byte classification;
            MappingClass mappingClass;
            Integer numMappingsForRead;
            Mapping mapping;
            int numReplicates = 1;
            int mappingQuality;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    start = record.getAlignmentStart();
                    stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                    isFwdStrand = !record.getReadNegativeStrandFlag();
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    //only add mappings, which are valid according to the read classification parameters
                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {
                        
                        mapping = this.getMappingForValues(mappingClass, numMappingsForRead, numReplicates, id++,
                                start, stop, isFwdStrand, mappingQuality, record.getBaseQualities());
                        mapping.setAlignmentBlocks(samUtils.getAlignmentBlocks(record.getCigar(), start));
                        // We must alway check for Diffs and Gaps even if "classification != MappingClass.PERFECT_MATCH"
                        // because there might still be a split read.
                        if (this.hasNeededDiffs(request, mappingClass)) {

                            //find check alignment via cigar string and add diffs to mapping
                            cigar = record.getCigarString();
                            if (cigar.contains("M")) { //TODO: check if this check is faster or the version in other methods here
                                refSubSeq = reference.getChromSequence(request.getChromId(), start, stop);
                            } else {
                                refSubSeq = null;
                            }
                            this.createDiffsAndGaps(record, numReplicates, refSubSeq, mapping);
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
     * Checks if diffs and gaps are needed and if the mapping contains some.
     * @param request
     * @param type
     * @return true, if diffs and gaps are needed and the mapping contains some,
     * false otherwise
     */
    private boolean hasNeededDiffs(IntervalRequest request, MappingClass type) {
        return request.isDiffsAndGapsNeeded() 
                && type != MappingClass.SINGLE_PERFECT_MATCH
                && type != MappingClass.PERFECT_MATCH;
    }

    /**
     * Retrieves the reduced mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name. Diffs and gaps are never included.
     *
     * @param request the request to carry out
     * @return the reduced mappings for the given interval. Diffs and gaps are
     * never included.
     */
    public Collection<Mapping> getReducedMappingsFromBam(IntervalRequest request) {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();
        Collection<Mapping> mappings = new ArrayList<>();

        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);
            SAMRecord record;
            int start;
            int stop;
            Byte classification;
            Integer numMappingsForRead;
            boolean isFwdStrand;
            Mapping mapping;
            MappingClass mappingClass;
            int mappingQuality;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                if (!record.getReadUnmappedFlag()) {
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    //only add mappings, which are valid according to the read classification paramters
                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {

                        start = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        mapping = new Mapping(start, stop, isFwdStrand, 1);
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
     * Retrieves the read pair mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name.
     *
     * @param request request to carry out
     * @return the coverage for the given interval
     */
    public Collection<ReadPairGroup> getReadPairMappingsFromBam(IntervalRequest request) {
        Map<Long, ReadPairGroup> readPairs = new HashMap<>();
        Collection<ReadPairGroup> readPairGroups = new ArrayList<>();

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

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
            Byte classification;
            MappingClass mappingClass;
            Integer numMappingsForRead;
            Integer pairId;
            Integer pairType;
            long readPairId;
            ReadPairType readPairType;
            int mateStart;
            int mateStop;
            boolean bothVisible;
            Mapping mapping;
            Mapping mate;
            ReadPairGroup newGroup;
            int numReplicates = 1;
            int mappingQuality;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {

                        startPos = record.getAlignmentStart();
                        stop = record.getAlignmentEnd();
          //            start = start < 0 ? 0 : start;
          //            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        pairId = (Integer) record.getAttribute(Properties.TAG_READ_PAIR_ID);
                        pairType = (Integer) record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        mateStart = record.getMateAlignmentStart();
                        bothVisible = mateStart > from && mateStart < to;

                        mapping = this.getMappingForValues(mappingClass, numMappingsForRead, numReplicates, id++,
                                startPos, stop, isFwdStrand, mappingQuality, record.getBaseQualities());
                        if (pairId != null && pairType != null) { //since both data fields are always written together
                            // add new readPair if not exists
                            readPairId = pairId;
                            readPairType = ReadPairType.getReadPairType(pairType);
                            if (!readPairs.containsKey(readPairId)) {
                                newGroup = new ReadPairGroup();
                                newGroup.setReadPairId(pairId);
                                readPairs.put(readPairId, newGroup);
                            } //TODO: check where ids are needed
                            try {
                                mate = this.getMappingForValues(MappingClass.COMMON_MATCH, -1, numReplicates, -1, mateStart, -1, !record.getMateNegativeStrandFlag(), new Byte("0"), new byte[0]);
                            } catch (IllegalStateException e) {
                                mate = this.getMappingForValues(MappingClass.COMMON_MATCH, -1, numReplicates, -1, mateStart, -1, true, new Byte("0"), new byte[0]);
                            } //TODO: get mate data from querried records later
                            readPairs.get(readPairId).addPersistentDirectAccessMapping(mapping, mate, readPairType, bothVisible);
                        }

                        if (this.hasNeededDiffs(request, mappingClass)) {

                            //check alignment via cigar string and add diffs to mapping
                            cigar = record.getCigarString();
                            if (cigar.contains("M")) {
                                refSubSeq = reference.getChromSequence(request.getChromId(), startPos, stop);
                            } else {
                                refSubSeq = null;
                            }
                            this.createDiffsAndGaps(record, numReplicates, refSubSeq, mapping);
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
     *
     * @param classification the classification data
     * @param numReplicates number of replicates of the mapping
     * @param id unique id of the mapping
     * @param startPos start position of the mapping
     * @param stop stop position of the mapping
     * @param isFwdStrand true, if the mapping is on the fwd strand
     * @return A new mapping with classification information, if classification
     * is not null. Otherwise isBestMapping is currently always true.
     */
    private Mapping getMappingForValues(MappingClass classification, Integer numMappingsForRead, int numReplicates, int id,
            int startPos, int stop, boolean isFwdStrand, int mappingQuality, byte[] baseQualities) {
        int mappingsForRead = numMappingsForRead != null ? numMappingsForRead : -1;
        boolean isBestMapping = classification != null && 
                (classification == MappingClass.SINGLE_PERFECT_MATCH || classification == MappingClass.PERFECT_MATCH 
                 || classification == MappingClass.SINGLE_BEST_MATCH || classification == MappingClass.BEST_MATCH);
        return new Mapping(id, startPos, stop, trackId, isFwdStrand, numReplicates, 0, 0,
                isBestMapping, mappingQuality, baseQualities, mappingsForRead);
    }

    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * @param request the request to carry out
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResult getReadStartsFromBam(IntervalRequest request) {

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        CoverageManager coverage = new CoverageManager(from, to);
        coverage.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>(); //both empty for read starts
        List<ReferenceGap> gaps = new ArrayList<>();

        CoverageAndDiffResult result = new CoverageAndDiffResult(coverage, diffs, gaps, request);
        try {
            this.checkIndex();
            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Byte classification;
            MappingClass mappingClass;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            int mappingQuality;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {

                        if (readClassParams.isStrandBothOption()) {
                            isFwdStrand = readClassParams.isStrandBothFwdOption();
                        } else {
                            isFwdStrand = !record.getReadNegativeStrandFlag();
                        }
                        startPos = isFwdStrand ? record.getAlignmentStart() : record.getAlignmentEnd();

                            this.increaseCoverage(readClassParams, mappingClass, isFwdStrand, 
                                    startPos, startPos, coverage);
                        }
                    }
                }
            samRecordIterator.close();
            result = new CoverageAndDiffResult(coverage, diffs, gaps, request);

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
    public CoverageAndDiffResult getCoverageAndReadStartsFromBam(IntervalRequest request) {

        byte trackNeeded = request.getWhichTrackNeeded();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();
        
        CoverageManager coverage = new CoverageManager(from, to);
        CoverageManager readStarts = new CoverageManager(from, to);
        coverage.incArraysToIntervalSize();
        readStarts.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>();
        List<ReferenceGap> gaps = new ArrayList<>();
        DiffAndGapResult diffsAndGaps;

        CoverageAndDiffResult result = new CoverageAndDiffResult(coverage, diffs, gaps, request);
        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Byte classification;
            MappingClass mappingClass;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            int stop;
            int mappingQuality;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {

                        if (readClassParams.isStrandBothOption()) {
                            isFwdStrand = readClassParams.isStrandBothFwdOption();
                        } else {
                            if (readClassParams.isStrandOppositeOption()) {
                                isFwdStrand = record.getReadNegativeStrandFlag();
                            } else {
                                isFwdStrand = !record.getReadNegativeStrandFlag();
                            }
                        }

                        // add read start
                        startPos = isFwdStrand ? record.getAlignmentStart() : record.getAlignmentEnd();
                        if (readClassParams.isStrandBothOption()) {
                            isFwdStrand = readClassParams.isStrandBothFwdOption();
                        } else {
                            isFwdStrand = !record.getReadNegativeStrandFlag();
                        }
                        
                        this.increaseCoverage(readClassParams, mappingClass, isFwdStrand, 
                                startPos, startPos, readStarts);
                        
                        //This enables us to handle split reads correctly.
                        startPos = record.getAlignmentStart();
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        List<SamAlignmentBlock> alignmentBlocks = samUtils.getAlignmentBlocks(record.getCigar(), startPos);
                        for (SamAlignmentBlock block : alignmentBlocks) {
                            this.increaseCoverage(readClassParams, mappingClass, isFwdStrand,
                                    block.getRefStart(), block.getRefStop(), coverage);
                        }

                        if (this.hasNeededDiffs(request, mappingClass)) {
                            stop = record.getAlignmentEnd();
                            diffsAndGaps = this.createDiffsAndGaps(record, 1,
                                    reference.getChromSequence(request.getChromId(), startPos, stop), null);
                            diffs.addAll(diffsAndGaps.getDiffs());
                            gaps.addAll(diffsAndGaps.getGaps());
                        }
                    }
                }
            }
            samRecordIterator.close();
            result = new CoverageAndDiffResult(coverage, diffs, gaps, request);
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
    public CoverageAndDiffResult getCoverageFromBam(IntervalRequest request) {

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        CoverageManager coverage = new CoverageManager(from, to);
        coverage.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>();
        List<ReferenceGap> gaps = new ArrayList<>();
        DiffAndGapResult diffsAndGaps;

        CoverageAndDiffResult result = new CoverageAndDiffResult(coverage, diffs, gaps, request);
        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(reference.getChromosome(request.getChromId()).getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Byte classification;
            MappingClass mappingClass;
            Integer numMappingsForRead;
            int startPos; //in the genome, to get the index: -1
            int stop;
            int mappingQuality;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();

                if (!record.getReadUnmappedFlag()) {
                    classification = Byte.valueOf(record.getAttribute(Properties.TAG_READ_CLASS).toString());
                    mappingClass = this.updateClassification(MappingClass.getFeatureType(classification));
//                    classification = this.updateClassification(classification);
                    numMappingsForRead = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                    mappingQuality = record.getMappingQuality();

                    if (this.isIncludedMapping(mappingClass, numMappingsForRead, mappingQuality, readClassParams)) {
                        isFwdStrand = !record.getReadNegativeStrandFlag();
                        //This enables us to handle split reads correctly.
                        startPos = record.getAlignmentStart();
                        List<SamAlignmentBlock> alignmentBlocks = samUtils.getAlignmentBlocks(record.getCigar(), startPos);
                        for (SamAlignmentBlock block : alignmentBlocks) {
                            this.increaseCoverage(readClassParams, mappingClass, isFwdStrand, 
                                    block.getRefStart(), block.getRefStop(), coverage);
                        }

                        if (this.hasNeededDiffs(request, mappingClass)) {
                            stop = record.getAlignmentEnd();
                            diffsAndGaps = this.createDiffsAndGaps(record, 1,
                                    reference.getChromSequence(request.getChromId(), startPos, stop), null);
                            diffs.addAll(diffsAndGaps.getDiffs());
                            gaps.addAll(diffsAndGaps.getGaps());
                        }
                    }
                }
            }
            samRecordIterator.close();
            result = new CoverageAndDiffResult(coverage, diffs, gaps, request);

        } catch (NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        } catch (BufferUnderflowException e) {
            //do nothing
        }

        return result;
    }

    /**
     * Increases the coverage between the given start and stop position for all
     * mappings, that fulfill the wanted parameters and adds the coverage to the
     * needed arrays (indicated by the "trackNeeded" parameter).
     * @param readClassParams parameters to adhere to
     * @param numMappingsForRead
     * @param mappingClass mapping class of the current mapping
     * @param isFwdStrand true, if the mapping is on the fwd strand, false
     * otherwise
     * @param startPos start position of the mapping
     * @param stopPos stop position of the mapping
     */
    private void increaseCoverage(ParametersReadClasses readClassParams, MappingClass mappingClass,
            boolean isFwdStrand, int startPos, int stopPos, CoverageManager covManager) {

        List<int[]> coverageArrays = new ArrayList<>();

        //only the arrays, which are allowed to be updated are added to the coverage array list
        this.addCoverageArray(readClassParams, MappingClass.COMMON_MATCH, isFwdStrand, coverageArrays, covManager);
        if (mappingClass == MappingClass.SINGLE_COMMON_MATCH) {
            this.addCoverageArray(readClassParams, MappingClass.SINGLE_COMMON_MATCH, isFwdStrand, coverageArrays, covManager);
        
        } else if (mappingClass != MappingClass.COMMON_MATCH) { //so it's one of the higher level classifications
            this.addCoverageArray(readClassParams, MappingClass.BEST_MATCH, isFwdStrand, coverageArrays, covManager);
            
            if (mappingClass == MappingClass.SINGLE_BEST_MATCH) { //three possibilities remain:
                this.addCoverageArray(readClassParams, MappingClass.SINGLE_BEST_MATCH, isFwdStrand, coverageArrays, covManager);

            } else if (mappingClass == MappingClass.PERFECT_MATCH) { 
                this.addCoverageArray(readClassParams, MappingClass.PERFECT_MATCH, isFwdStrand, coverageArrays, covManager);

            } else if (mappingClass == MappingClass.SINGLE_PERFECT_MATCH) { 
                this.addCoverageArray(readClassParams, MappingClass.SINGLE_PERFECT_MATCH, isFwdStrand, coverageArrays, covManager);
                this.addCoverageArray(readClassParams, MappingClass.PERFECT_MATCH, isFwdStrand, coverageArrays, covManager);
                this.addCoverageArray(readClassParams, MappingClass.SINGLE_BEST_MATCH, isFwdStrand, coverageArrays, covManager);

            }
        }

        covManager.increaseCoverage(startPos, stopPos, coverageArrays);
    }
        
    /**
     * Add the appropriate coverage array to the list of arrays which whose 
     * coverage shall be increased by the current mapping.
     * @param readClassParams parameters to adhere to
     * @param mappingClass mapping class whose arrays are considered here
     * @param isFwdStrand true, if the mapping is on the fwd strand, false
     * otherwise
     * @param coverageArrays the list of arrays which whose coverage shall be
     * increased by the current mapping
     * @param covManager the coverage manager containing all coverage arrays
     */
    private void addCoverageArray(ParametersReadClasses readClassParams, MappingClass mappingClass, 
            boolean isFwdStrand, List<int[]> coverageArrays, CoverageManager covManager) {
        
        if (readClassParams.isClassificationAllowed(mappingClass)) {
            if (isFwdStrand) {
                coverageArrays.add(covManager.getCoverage(mappingClass).getFwdCov());
            } else {
                coverageArrays.add(covManager.getCoverage(mappingClass).getRevCov());
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
     * DiffAndGapResult contains all the diff and gap data.
     * @return DiffAndGapResult containing all the diffs and gaps
     */
    private DiffAndGapResult createDiffsAndGaps(SAMRecord record, int nbReplicates,
            String refSeq, Mapping mapping) throws NumberFormatException {

        Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        List<Difference> diffs = new ArrayList<>();
        List<ReferenceGap> gaps = new ArrayList<>();
        int differences = 0;
        String cigar = record.getCigarString();
        String readSeq = record.getReadString();
        boolean isFwdStrand = !record.getReadNegativeStrandFlag();
        int start = record.getAlignmentStart();
        byte[] baseQualities = record.getBaseQualities();
        Byte mappingQuality = (byte) (record.getMappingQuality() >= DEFAULT_MAP_QUAL ? UNKNOWN_CALCULATED_MAP_QUAL : record.getMappingQuality());
        String[] num = cigar.split(cigarRegex);
        String[] charCigar = cigar.split("\\d+");
        String op;//operation
        char base; //currently visited base
        String bases; //bases of the read interval under investigation
        String refBases; //bases of the reference corresponding to the read interval under investigation
        int currentCount;
        int refPos = 0;
        int readPos = 0;
        byte baseQuality;

        for (int i = 1; i < charCigar.length; ++i) {
            op = charCigar[i];
            currentCount = Integer.valueOf(num[i - 1]);
            switch (op) {
                case "M":
                    //check, count and add diffs for deviating Ms
                    bases = readSeq.substring(readPos, readPos + currentCount).toUpperCase();
                    refBases = refSeq.substring(refPos, refPos + currentCount);
                    for (int j = 0; j < bases.length(); ++j) {
                        base = bases.charAt(j);
                        if (base != refBases.charAt(j)) {
                            ++differences;
                            if (!isFwdStrand) {
                                base = SequenceUtils.getDnaComplement(base);
                            }
                            baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                            Difference d = new Difference(refPos + j + start, base, isFwdStrand, nbReplicates, baseQuality, mappingQuality);
                            this.addDiff(mapping, diffs, d);
                        }
                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "=":
                    //only increase position for matches
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "X":
                    //count and create diffs for mismatches
                    differences += currentCount;
                    for (int j = 0; j < currentCount; ++j) {
                        base = Character.toUpperCase(readSeq.charAt(readPos + j));
                        if (!isFwdStrand) {
                            base = SequenceUtils.getDnaComplement(base);
                        }
                        baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        Difference d = new Difference(refPos + j + start, base, isFwdStrand, nbReplicates, baseQuality, mappingQuality);
                        this.addDiff(mapping, diffs, d);

                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "D":
                    // count and add diff gaps for deletions in reference
                    differences += currentCount;
                    for (int j = 0; j < currentCount; ++j) {
                        baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        Difference d = new Difference(refPos + j + start, '-', isFwdStrand, nbReplicates, baseQuality, mappingQuality);
                        this.addDiff(mapping, diffs, d);
                    }
                    refPos += currentCount;
                    // readPos remains the same
                    break;
                case "I":
                    // count and add reference gaps for insertions
                    differences += currentCount;
                    for (int j = 0; j < currentCount; ++j) {
                        base = Character.toUpperCase(readSeq.charAt(readPos + j));
                        if (!isFwdStrand) {
                            base = SequenceUtils.getDnaComplement(base);
                        }
                        baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        ReferenceGap gap = new ReferenceGap(refPos + start, base,
                                CommonsMappingParser.getOrderForGap(refPos + start, gapOrderIndex),
                                isFwdStrand, nbReplicates, baseQuality, mappingQuality);
                        if (mapping != null) {
                            mapping.addGenomeGap(gap);
                        } else {
                            gaps.add(gap);
                        }
                    }   //refPos remains the same
                    readPos += currentCount;
                    break;
                case "N": //fallthrough, treatment is equal to "P"
                case "P":
                    //increase ref position for padded and skipped reference bases
                    refPos += currentCount;
                    //readPos remains the same
                    break;
                case "S":
                    //increase read position for soft clipped bases which are present in the read
                    //refPos remains the same
                    readPos += currentCount;
                    break;
                case "H": //just do nothing since the position is not in the reference and not in the read!
                    break;
            }
        }

        if (mapping != null) {
            mapping.setDifferences(differences);
        }

        return new DiffAndGapResult(diffs, gaps, gapOrderIndex, differences);
    }

    /**
     * Adds a diff either to the mapping, if it is not null, or to the diffs.
     *
     * @param mapping the mapping to which the diff shall be added or
     * <cc>null</cc>.
     * @param diffs the diffs list to which the diff shall be added.
     * @param diff the diff to add
     */
    private void addDiff(Mapping mapping, List<Difference> diffs, Difference diff) {
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
     * @param readClassParams the request whose parameters are used
     * @return true, if the mapping can be included in further steps, false
     * otherwise
     */
    private boolean isIncludedMapping(MappingClass mappingClass, Integer numMappingsForRead, int mappingQuality, ParametersReadClasses readClassParams) {
        //TODO: make sure it knows if extended classification is used. if not, replace single perfect... by perfect...
        boolean isIncludedMapping = (readClassParams.isClassificationAllowed(FeatureType.MULTIPLE_MAPPED_READ)
                || !readClassParams.isClassificationAllowed(FeatureType.MULTIPLE_MAPPED_READ) && numMappingsForRead != null && numMappingsForRead == 1)
                && (mappingQuality == UNKNOWN_MAP_QUAL
                || mappingQuality >= readClassParams.getMinMappingQual());
        if (isIncludedMapping) {
                isIncludedMapping = readClassParams.isClassificationAllowed(mappingClass);
        }
        return isIncludedMapping;
    }

    /**
     * Updates the read mapping class in case the extended classification is not
     * used.
     * @param mappingClass mapping classification to update, if necessary
     * @return the updated read mapping classification or the original one, if
     * the extended classification is used
     */
    private MappingClass updateClassification(MappingClass mappingClass) {
        if (!this.useExtendedClassification) {
            switch (mappingClass) {
                case SINGLE_PERFECT_MATCH : return MappingClass.PERFECT_MATCH;
                case SINGLE_BEST_MATCH :    return MappingClass.BEST_MATCH;
                case SINGLE_COMMON_MATCH :  return MappingClass.COMMON_MATCH;
                default :                   return mappingClass;
            }
        }
        return mappingClass;
    }
}
