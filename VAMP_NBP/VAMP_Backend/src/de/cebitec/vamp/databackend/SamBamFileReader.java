package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.*;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SamUtils;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.File;
import java.util.*;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeIOException;

/**
 * A SamBamFileReader has different methods to read data from a bam or sam file.
 *
 * @author -Rolf Hilker-
 */
public class SamBamFileReader implements Observable {

    public static final String cigarRegex = "[MIDNSPX=]+";
    private final File dataFile;
    private final int trackId;
    private SamUtils samUtils;
    private SAMFileReader samFileReader;
    private String header;
    private List<de.cebitec.vamp.util.Observer> observers;

    /**
     * A SamBamFileReader has different methods to read data from a bam or sam
     * file.
     *
     * @param dataFile the file to read from
     * @param trackId the track id of the track whose data is stored in the
     * given file
     * @throws RuntimeIOException
     */
    public SamBamFileReader(File dataFile, int trackId) throws RuntimeIOException {
        this.observers = new ArrayList<>();
        this.dataFile = dataFile;
        this.trackId = trackId;
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
     * Checks if the index of the bam file is present or creates it.
     */
    private void checkIndex() {
        if (!samFileReader.hasIndex()) {
            samUtils.createIndex(samFileReader, new File(dataFile.getAbsolutePath().concat(Properties.BAM_INDEX_EXT)));
        }
    }

    /**
     * Retrieves the mappings from the given interval from the sam or bam file
     * set for this data reader and the reference sequence with the given name.
     *
     * @param refGenome reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param needDiffs true, if the diffs need to be fetched from the file as
     * well
     * @return the mappings for the given interval
     */
    public Collection<PersistantMapping> getMappingsFromBam(PersistantReference refGenome, int from, int to, boolean needDiffs) {

        Collection<PersistantMapping> mappings = new ArrayList<>();
        
        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);
            String refSeq = refGenome.getSequence().toUpperCase();
            String refSubSeq;
            int id = 0;
            String cigar;
            SAMRecord record;
            int start;
            int stop;
            boolean isFwdStrand;
            Integer classification;
            Integer count;
            boolean classify;
            PersistantMapping mapping;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                start = record.getAlignmentStart();
                stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                isFwdStrand = !record.getReadNegativeStrandFlag();
                classification = (Integer) record.getAttribute("Yc");
                count = (Integer) record.getAttribute("Yt");

                //find check alignment via cigar string and add diffs to mapping
                cigar = record.getCigarString();
                if (cigar.contains("M")) {
                    refSubSeq = refSeq.substring(start - 1, stop);
                } else {
                    refSubSeq = null;
                }

                if (classification != null && count != null) { //since both data fields are always written together
                    classify = classification == (int) Properties.PERFECT_COVERAGE
                            || (classification == (int) Properties.BEST_MATCH_COVERAGE) ? true : false;
                    mapping = new PersistantMapping(id++, start, stop, trackId, isFwdStrand, count, 0, 0, classify);
                } else {
                    count = 1;
                    mapping = new PersistantMapping(id++, start, stop, trackId, isFwdStrand, count, 0, 0, true);
                }

                this.createDiffsAndGaps(record.getCigarString(), start, isFwdStrand, count,
                        record.getReadString(), refSubSeq, mapping);

                mappings.add(mapping);
            }
            samRecordIterator.close();

        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        }
        
        return mappings;
    }

    /**
     * Retrieves the reduced mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name.
     *
     * @param refGenome reference genome used in the bam file
     * @return the reduced mappings for the given interval
     */
    public Collection<PersistantMapping> getReducedMappingsFromBam(PersistantReference refGenome, int from, int to) {
        Collection<PersistantMapping> mappings = new ArrayList<>();

        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);
            SAMRecord record;
            int start;
            int stop;
            boolean isFwdStrand;
            PersistantMapping mapping;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                start = record.getAlignmentStart();
                stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
                isFwdStrand = !record.getReadNegativeStrandFlag();
                mapping = new PersistantMapping(start, stop, isFwdStrand);
                mappings.add(mapping);
            }
            samRecordIterator.close();

        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        }
        
        return mappings;
    }

    /**
     * Retrieves the sequence pair mappings from the given interval from the sam
     * or bam file set for this data reader and the reference sequence with the
     * given name.
     *
     * @param refGenome reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param diffsAndGapsNeeded true, if the diffs and gaps have to be fetched
     * from the file as well
     * @return the coverage for the given interval
     */
    public Collection<PersistantSeqPairGroup> getSeqPairMappingsFromBam(PersistantReference refGenome,
            int from, int to, boolean diffsAndGapsNeeded) {
        HashMap<Long, PersistantSeqPairGroup> seqPairs = new HashMap<>();
        Collection<PersistantSeqPairGroup> seqPairGroups = new ArrayList<>();
        
        try {
            this.checkIndex();

            SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);
            String refSeq = refGenome.getSequence().toUpperCase();
            String refSubSeq;
            int id = 0;
            String cigar;
            SAMRecord record;
            int startPos; //in the genome, to get the index: -1
            int stop;
            boolean isFwdStrand;
            Integer classification;
            Integer count;
            Integer pairId;
            Integer pairType;
            long seqPairId;
            byte seqPairType;
            int mateStart;
            boolean bothVisible;
            boolean isBestMapping;
            PersistantMapping mapping;
            PersistantSeqPairGroup newGroup;

            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                startPos = record.getAlignmentStart();
                stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                isFwdStrand = !record.getReadNegativeStrandFlag();
                classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
                count = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
                pairId = (Integer) record.getAttribute(Properties.TAG_SEQ_PAIR_ID);
                pairType = (Integer) record.getAttribute(Properties.TAG_SEQ_PAIR_TYPE);
                mateStart = record.getMateAlignmentStart(); //TODO: handle somewhere
                bothVisible = mateStart > from && mateStart < to;


                //check alignment via cigar string and add diffs to mapping
                cigar = record.getCigarString();
                if (cigar.contains("M")) {
                    refSubSeq = refSeq.substring(startPos - 1, stop);
                } else {
                    refSubSeq = null;
                }

                if (classification != null && count != null) { //since both data fields are always written together
                    isBestMapping = classification == (int) Properties.PERFECT_COVERAGE
                            || (classification == (int) Properties.BEST_MATCH_COVERAGE) ? true : false;
                    mapping = new PersistantMapping(id++, startPos, stop, trackId, isFwdStrand, count, 0, 0, isBestMapping);
                } else {
                    count = 1;
                    mapping = new PersistantMapping(id++, startPos, stop, trackId, isFwdStrand, count, 0, 0, false);
                }
                if (pairId != null && pairType != null) { //since both data fields are always written together
//                // add new seqPair if not exists
                    seqPairId = (long) pairId;
                    seqPairType = Byte.valueOf(pairType.toString());
                    if (!seqPairs.containsKey(seqPairId)) {
                        newGroup = new PersistantSeqPairGroup();
                        newGroup.setSeqPairId(pairId);
                        seqPairs.put(seqPairId, newGroup);
                    } //TODO: check where ids are needed
                    seqPairs.get(seqPairId).addPersistantMapping(mapping, seqPairType, Long.valueOf(id), -1, 1);
                }

                if (diffsAndGapsNeeded) {
                    this.createDiffsAndGaps(record.getCigarString(), startPos, isFwdStrand, count,
                            record.getReadString(), refSubSeq, mapping);
                }

            }
            samRecordIterator.close();
            seqPairGroups = seqPairs.values();


        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        }

        return seqPairGroups;
    }

    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     *
     * @param refGenome the reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param diffsAndGapsNeeded true, if the diffs and gaps are needed, false
     * otherwise
     * @param trackNeeded value among 0, if it is an ordinary request,
     * PersistantCoverage.TRACK1 and PersistantCoverage.TRACK2 if it is a part
     * of a double track request
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResultPersistant getCoverageFromBam(PersistantReference refGenome, int from, int to,
            boolean diffsAndGapsNeeded, byte trackNeeded) {
        
        int[] perfectCoverageFwd = new int[0];
        int[] perfectCoverageRev = new int[0];
        int[] bestMatchCoverageFwd = new int[0];
        int[] bestMatchCoverageRev = new int[0];
        int[] commonCoverageFwd = new int[0];
        int[] commonCoverageRev = new int[0];
        int[] commonCoverageFwdTrack1 = new int[0];
        int[] commonCoverageRevTrack1 = new int[0];
        int[] commonCoverageFwdTrack2 = new int[0];
        int[] commonCoverageRevTrack2 = new int[0];
        int intervalSize = to - from;

        if (trackNeeded == 0) {
            perfectCoverageFwd = new int[intervalSize];
            perfectCoverageRev = new int[intervalSize];
            bestMatchCoverageFwd = new int[intervalSize];
            bestMatchCoverageRev = new int[intervalSize];
            commonCoverageFwd = new int[intervalSize];
            commonCoverageRev = new int[intervalSize];

        } else if (trackNeeded == PersistantCoverage.TRACK1) {
            commonCoverageFwdTrack1 = new int[intervalSize];
            commonCoverageRevTrack1 = new int[intervalSize];

        } else if (trackNeeded == PersistantCoverage.TRACK2) {
            commonCoverageFwdTrack2 = new int[intervalSize];
            commonCoverageRevTrack2 = new int[intervalSize];
        }

        PersistantCoverage coverage = new PersistantCoverage(from, to);
        List<PersistantDiff> diffs = new ArrayList<>();
        List<PersistantReferenceGap> gaps = new ArrayList<>();
        PersistantDiffAndGapResult diffsAndGaps;
        String refSeq = "";

        CoverageAndDiffResultPersistant result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, true, from, to);
        if (diffsAndGapsNeeded) {
            refSeq = refGenome.getSequence().toUpperCase();
        }
        try {
            this.checkIndex();
            
            SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Integer classification;
            int refPos;
            int indexPos;
            int startPos; //in the genome, to get the index: -1
            int stop;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                isFwdStrand = !record.getReadNegativeStrandFlag();
                classification = (Integer) record.getAttribute("Yc");
                startPos = record.getAlignmentStart();
                stop = record.getAlignmentEnd();
//                start = start < 0 ? 0 : start;
//                stop = stop >= refSeq.length() ? refSeq.length() : stop;
                for (int i = 0; i <= stop - startPos; i++) {
                    refPos = startPos + i; //example: 1000 = from, 999 = start, i = 0 -> refPos = 999, indexPos = -1;
                    if (refPos >= from && refPos < to) {
                        indexPos = refPos - from;
                        if (trackNeeded == 0) {
                            if (classification != null) {
                                if (classification == Properties.PERFECT_COVERAGE) {
                                    if (isFwdStrand) {
                                        ++perfectCoverageFwd[indexPos];
                                        ++bestMatchCoverageFwd[indexPos];
                                        ++commonCoverageFwd[indexPos];
                                    } else {
                                        ++perfectCoverageRev[indexPos];
                                        ++bestMatchCoverageRev[indexPos];
                                        ++commonCoverageRev[indexPos];
                                    }

                                } else if (classification == Properties.BEST_MATCH_COVERAGE) {
                                    if (isFwdStrand) {
                                        ++bestMatchCoverageFwd[indexPos];
                                        ++commonCoverageFwd[indexPos];
                                    } else {
                                        ++bestMatchCoverageRev[indexPos];
                                        ++commonCoverageRev[indexPos];
                                    }

                                } else { //meaning: if (classification == Properties.COMPLETE_COVERAGE) {
                                    if (isFwdStrand) {
                                        ++commonCoverageFwd[indexPos];
                                    } else {
                                        ++commonCoverageRev[indexPos];
                                    }
                                }

                            } else {
                                if (isFwdStrand) {
                                    ++commonCoverageFwd[indexPos];
                                } else {
                                    ++commonCoverageRev[indexPos];
                                }
                            }

                            //part for double track coverage, where we need to store it in map for track 1 or 2
                        } else if (trackNeeded == PersistantCoverage.TRACK1) {
                            if (isFwdStrand) {
                                ++commonCoverageFwdTrack1[indexPos];
                            } else {
                                ++commonCoverageRevTrack1[indexPos];
                            }

                        } else if (trackNeeded == PersistantCoverage.TRACK2) {
                            if (isFwdStrand) {
                                ++commonCoverageFwdTrack2[indexPos];
                            } else {
                                ++commonCoverageRevTrack2[indexPos];
                            }
                        }
                    }
                }

                if (diffsAndGapsNeeded) {
                    diffsAndGaps = this.createDiffsAndGaps(record.getCigarString(),
                            startPos, isFwdStrand, 1, record.getReadString(),
                            refSeq.substring(startPos - 1, stop), null);
                    diffs.addAll(diffsAndGaps.getDiffs());
                    gaps.addAll(diffsAndGaps.getGaps());
                }
            }
            samRecordIterator.close();

            if (trackNeeded == 0) {
                coverage.setPerfectFwdMult(perfectCoverageFwd);
                coverage.setPerfectRevMult(perfectCoverageRev);
                coverage.setBestMatchFwdMult(bestMatchCoverageFwd);
                coverage.setBestMatchRevMult(bestMatchCoverageRev);
                coverage.setCommonFwdMult(commonCoverageFwd);
                coverage.setCommonRevMult(commonCoverageRev);

            } else if (trackNeeded == PersistantCoverage.TRACK1) {
                coverage.setCommonFwdMultTrack1(commonCoverageFwdTrack1);
                coverage.setCommonRevMultTrack1(commonCoverageRevTrack1);

            } else if (trackNeeded == PersistantCoverage.TRACK2) {
                coverage.setCommonFwdMultTrack2(commonCoverageFwdTrack2);
                coverage.setCommonRevMultTrack2(commonCoverageRevTrack2);
            }

            result = new CoverageAndDiffResultPersistant(coverage, diffs, gaps, true, from, to);

        } catch (NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e) {
            this.notifyObservers(e);
        }

        return result;
    }

    /**
     * Counts and returns each difference to the reference sequence for a cigar
     * string and the belonging read sequence. If the operation "M" is not used
     * in the cigar, then the reference sequence can be null (it is not used in
     * this case). If the mapping is also handed over to the method, the diffs
     * and gaps are stored directly in the mapping.
     *
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
        int currentCount;
        int refPos = 0;
        int readPos = 0;
        int diffPos;
        if (refSeq != null && !refSeq.isEmpty()) {
            readSeq = readSeq.toUpperCase();
            refSeq = refSeq.toUpperCase();
        }
        
        for (int i = 1; i < charCigar.length; ++i) {
            op = charCigar[i];
            currentCount = Integer.valueOf(num[i - 1]);
            
            if (op.equals("=")) { //only increase position for matches
                refPos += currentCount;
                readPos += currentCount;
               
            } else if (op.equals("N") || op.equals("P")) {
                refPos += Integer.valueOf(num[i - 1]);

            } else if (op.equals("X")) { //count and create diffs for mismatches
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    diffPos = readPos + j;
                    base = readSeq.charAt(diffPos);
                    if (!isFwdStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    PersistantDiff d = new PersistantDiff(diffPos + start, base, isFwdStrand, nbReplicates);
                    if (mapping != null) {
                        mapping.addDiff(d);
                    } else {
                        diffs.add(d);
                    }
                    
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("D")) { // count and add diff gaps for deletions in read
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    PersistantDiff d = new PersistantDiff(refPos + j + start, '_', isFwdStrand, nbReplicates);
                    if (mapping != null) {
                        mapping.addDiff(d);
                    } else {
                        diffs.add(d);
                    }
                }
                refPos += currentCount;
                // readPos remains the same
            
            } else if (op.equals("I")) { // count and add reference gaps for insertions
                differences += currentCount;
                for (int j = 0; j < currentCount; ++j) {
                    base = readSeq.charAt(readPos + j);
                    if (!isFwdStrand) {
                        base = SequenceUtils.getDnaComplement(base);
                    }
                    gaps.add(new PersistantReferenceGap(refPos + start, base, ParserCommonMethods.getOrderForGap(
                            refPos + start, gapOrderIndex), isFwdStrand, nbReplicates));
                }
                //refPos remains the same
                readPos += currentCount;

            } else if (op.equals("M")) { //check, count and add diffs for deviating Ms
                bases = readSeq.substring(readPos, readPos + currentCount);
                for (int j = 0; j < bases.length(); ++j) {
                    diffPos = refPos + j;
                    base = bases.charAt(j);
                    if (base != refSeq.charAt(diffPos)) {
                        ++differences;
                        if (!isFwdStrand) {
                            base = SequenceUtils.getDnaComplement(base);
                        }
                        PersistantDiff d = new PersistantDiff(diffPos + start, base, isFwdStrand, nbReplicates);
                        if (mapping != null) {
                            mapping.addDiff(d);
                        } else {
                            diffs.add(d);
                        }
                    }
                }
                refPos += currentCount;
                readPos += currentCount;

            } else if (op.equals("S")) {
                //refPos remains the same
                readPos += currentCount;
            }
            
            //P, S and H = padding, soft and hard clipping do not contribute to differences
        }

        if (mapping != null) {
            mapping.setDifferences(differences);
        }

        return new PersistantDiffAndGapResult(diffs, gaps, gapOrderIndex, differences);
    }
    
    /**
     * Closes this reader.
     */
    public void close() {
        samFileReader.close();
    }

    @Override
    public void registerObserver(de.cebitec.vamp.util.Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(de.cebitec.vamp.util.Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (de.cebitec.vamp.util.Observer observer : observers) {
            observer.update(data);
        }
    }
}
