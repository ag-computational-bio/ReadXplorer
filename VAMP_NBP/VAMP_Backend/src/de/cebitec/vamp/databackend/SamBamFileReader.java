package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.*;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.File;
import java.util.*;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeIOException;
import org.openide.util.Exceptions;

/**
 * A SamBamFileReader has different methods to read data from a bam or sam file.
 * 
 * @author -Rolf Hilker-
 */
public class SamBamFileReader { //TODO: add observer
    
    public static final String cigarRegex = "[MIDNSPX=]+";
    private final File dataFile;
    private final int trackId;
    
    private SAMFileReader samFileReader;
    private String header;
    private boolean hasIndex;
    private List<de.cebitec.vamp.util.Observer> observer;
    
    /**
     * A SamBamFileReader has different methods to read data from a bam or sam file.
     * @param dataFile the file to read from
     * @param trackId the track id of the track whose data is stored in the given file
     * @throws RuntimeIOException  
     */
    public SamBamFileReader(File dataFile, int trackId) throws RuntimeIOException {
        this.observer = new ArrayList<>();
        this.dataFile = dataFile;
        this.trackId = trackId;
        
        samFileReader = new SAMFileReader(this.dataFile);
        samFileReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
        header = samFileReader.getFileHeader().getTextHeader();
        hasIndex = samFileReader.hasIndex();
        
    }
    
    /**
     * Retrieves the mappings from the given interval from the sam or bam file set 
     * for this data reader and the reference sequence with the given name.
     * @param refGenome reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param needDiffs true, if the diffs need to be fetched from the file as well
     * @return the mappings for the given interval
     */
    public Collection<PersistantMapping> getMappingsFromBam(PersistantReference refGenome, int from, int to, boolean needDiffs) {
        
        List<PersistantMapping> mappings = new ArrayList<>();
        SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);
        String refSeq = refGenome.getSequence();
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
            start = record.getUnclippedStart();
            stop = record.getUnclippedEnd();
            isFwdStrand = !record.getReadNegativeStrandFlag();
            classification = (Integer) record.getAttribute("Yc");
            count = (Integer) record.getAttribute("Yt");            
            
            //find check alignment via cigar string and add diffs to mapping
            cigar = record.getCigarString();
            if (cigar.contains("M")) {
                refSubSeq = refSeq.substring(start, stop);
            } else {
                refSubSeq = null;
            }
                       
            if (classification != null && count != null) { //since both data fields are always written together
                classify = classification == (int) Properties.PERFECT_COVERAGE || 
                                  (classification == (int) Properties.BEST_MATCH_COVERAGE) ? true : false;
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
        return mappings;
    }
    
    /**
     * Retrieves the sequence pair mappings from the given interval from the sam
     * or bam file set for this data reader and the reference sequence with the
     * given name.
     * @param refGenome reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param diffsAndGapsNeeded true, if the diffs and gaps have to be fetched
     *      from the file as well
     * @return the coverage for the given interval
     */
    public Collection<PersistantSeqPairGroup> getSeqPairMappingsFromBam(PersistantReference refGenome, 
            int from, int to, boolean diffsAndGapsNeeded) {
        HashMap<Long, PersistantSeqPairGroup> seqPairs = new HashMap<>();
        
        SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);
        String refSeq = refGenome.getSequence();
        String refSubSeq;
        int id = 0;
        String cigar;
        SAMRecord record;
        int start;
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

        while (samRecordIterator.hasNext()) {
            record = samRecordIterator.next();
            start = record.getUnclippedStart();
            stop = record.getUnclippedEnd();
            isFwdStrand = !record.getReadNegativeStrandFlag();
            classification = (Integer) record.getAttribute(Properties.TAG_READ_CLASS);
            count = (Integer) record.getAttribute(Properties.TAG_MAP_COUNT);
            pairId = (Integer) record.getAttribute(Properties.TAG_SEQ_PAIR_ID);
            pairType = (Integer) record.getAttribute(Properties.TAG_SEQ_PAIR_TYPE);
            mateStart = record.getMateAlignmentStart(); //TODO: handle somewhere
            bothVisible = mateStart > from && mateStart < to ? true : false;
            

            //check alignment via cigar string and add diffs to mapping
            cigar = record.getCigarString();
            if (cigar.contains("M")) {
                refSubSeq = refSeq.substring(start, stop);
            } else {
                refSubSeq = null;
            }

            if (classification != null && count != null) { //since both data fields are always written together
                isBestMapping = classification == (int) Properties.PERFECT_COVERAGE
                        || (classification == (int) Properties.BEST_MATCH_COVERAGE) ? true : false;
                mapping = new PersistantMapping(id++, start, stop, trackId, isFwdStrand, count, 0, 0, isBestMapping);
            } else {
                count = 1;
                mapping = new PersistantMapping(id++, start, stop, trackId, isFwdStrand, count, 0, 0, false);
            }
            if (pairId != null && pairType != null) { //since both data fields are always written together
//                // add new seqPair if not exists
                seqPairId = (long) pairId;
                seqPairType = Byte.valueOf(pairType.toString());
                if (!seqPairs.containsKey(seqPairId)) {
                    PersistantSeqPairGroup newGroup = new PersistantSeqPairGroup();
                    newGroup.setSeqPairId(pairId);
                    seqPairs.put(seqPairId, newGroup);
                } //TODO: check where ids are needed
                seqPairs.get(seqPairId).addPersistantMapping(mapping, seqPairType, Long.valueOf(id), -1, 1);
            }

            if (diffsAndGapsNeeded) {
                this.createDiffsAndGaps(record.getCigarString(), start, isFwdStrand, count,
                    record.getReadString(), refSubSeq, mapping);
            }
            
        }
        samRecordIterator.close();
        
        return seqPairs.values();
    }
    
    /**
     * Retrieves the coverage for the given interval from the bam file set for 
     * this data reader and the reference sequence with the given name.
     * If reads become longer than 1000bp the offset in this method has to be enlarged!
     * @param refGenome the reference genome used in the bam file
     * @param from start of the interval
     * @param to end of the interval
     * @param diffsAndGapsNeeded true, if the diffs and gaps are needed, false otherwise
     * @param trackNeeded value among 0, if it is an ordinary request, 
     *          PersistantCoverage.TRACK1 and PersistantCoverage.TRACK2 if it is a
     *          part of a double track request
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
        if (diffsAndGapsNeeded) {
            refSeq = refGenome.getSequence();
        }
        try {
            SAMRecordIterator samRecordIterator = samFileReader.query(refGenome.getName(), from, to, false);

            SAMRecord record;
            boolean isFwdStrand;
            Integer classification;
            int refPos;
            int indexPos;
            int start;
            int stop;
            while (samRecordIterator.hasNext()) {
                record = samRecordIterator.next();
                isFwdStrand = !record.getReadNegativeStrandFlag();
                classification = (Integer) record.getAttribute("Yc");
                start = record.getAlignmentStart();
                stop = record.getAlignmentEnd();
                for (int i = 0; i <= stop - start; i++) {
                    refPos = start + i; //example: 1000 = from, 999 = start, i = 0 -> refPos = 999, indexPos = -1;
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
                            record.getUnclippedStart(), isFwdStrand, 1, record.getReadString(), 
                            refSeq.substring(record.getUnclippedStart() - 1, record.getUnclippedEnd()), null);
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
            
        } catch (NullPointerException | IllegalArgumentException | SAMFormatException | ArrayIndexOutOfBoundsException e) {
            Exceptions.printStackTrace(e); //TODO: replace by notify observer
        }
        return new CoverageAndDiffResultPersistant(coverage, diffs, gaps, true, from, to);
    }
     
    /**
     * Counts and returns each difference to the reference sequence for a cigar string and
     * the belonging read sequence. If the operation "M" is not used in the cigar,
     * then the reference sequence can be null (it is not used in this case). If the 
     * mapping is also handed over to the method, the diffs and gaps are stored
     * directly in the mapping.
     * @param cigar the cigar string containing the alignment operations
     * @param start the start position of the alignment on the chromosome
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq the reference sequence belonging to the cigar and without gaps
     * @param mapping if a mapping is handed over to the method it adds the diffs and
     *      gaps directly to the mapping and updates it's number of differences to the
     *      reference. If null is passed, only the PersistantDiffAndGapResult contains 
     *      all the diff and gap data.
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
        String op; //operation
        char base; //currently visited base
        int baseNo = 0; //number of first base of consecutive operation types
        int count; //number of consecutive bases with same operation type
        int pos; //baseNo + current position in list of consecutive bases
        int dels = 0; //number of deletions in read until current base
//        int ins = 0; //number of insertions in read until current base
        for (int i = 0; i < charCigar.length; ++i) {
            op = charCigar[i];
            if (op.matches(cigarRegex)) {
                try {
                    count = Integer.valueOf(num[i - 1]);
                    
                    if (op.equals("=")) { //match, the most common case
                        baseNo += count;
                        
                    } else if (op.equals("X") || op.equals("S")) { //mismatch or soft clipped, both treated as mismatch
                        for (int j = 0; j < count; ++j) {
                            pos = baseNo + j;
                            base = readSeq.charAt(pos); //55 means we get base 56, because of 0 shift
                            base = isFwdStrand ? base : SequenceUtils.getDnaComplement(base);
                            PersistantDiff d = new PersistantDiff(start + pos + dels, base, isFwdStrand , nbReplicates);
                            if (mapping != null) {
                                mapping.addDiff(d);
                            } else {
                                diffs.add(d);
                            }
                        }
                        differences += count;
                        baseNo += count;
                        
                    } else if (op.equals("D")) { //deletions             
                        for (int j = 0; j < count; ++j) {
                            PersistantDiff d = new PersistantDiff(start + dels + baseNo + j, '_', isFwdStrand, nbReplicates);
                            if (mapping != null) {
                                mapping.addDiff(d);
                            } else {
                                diffs.add(d);
                            }
                        }
                        differences += count;
                        dels += count;
                        
                    } else if (op.equals("I")) { //insertions
                        for (int j = 0; j < count; ++j) {
                            pos = baseNo + j;
                            base = readSeq.charAt(pos); //55 means we get base 56, because of 0 shift
                            base = isFwdStrand ? base : SequenceUtils.getDnaComplement(base);
                            PersistantReferenceGap gap = new PersistantReferenceGap(start + pos + dels, 
                                    base, ParserCommonMethods.getOrderForGap(pos, gapOrderIndex), 
                                    isFwdStrand, nbReplicates);
                            if (mapping != null) {
                                mapping.addGenomeGap(gap);
                            } else {
                                gaps.add(gap);
                            }
                        }
                        differences += count;
//                        baseNo += count;
//                        ins += count;
                        
                    } else if (op.equals("N")) { //skipped bases of ref
                        for (int j = 0; j < count; ++j) {
                            PersistantDiff d = new PersistantDiff(start + dels + baseNo + j, '.', isFwdStrand, nbReplicates);
                            if (mapping != null) {
                                mapping.addDiff(d);
                            } else {
                                diffs.add(d);
                            }
                        }
                    
                    } else if (op.equals("M")) { //mismatch or match, we don't know yet
                        for (int j = 0; j < count; ++j) {
                            pos = baseNo + j;
                            if (readSeq.charAt(pos) != refSeq.charAt(pos)) {
                                PersistantDiff d = new PersistantDiff(start + pos + dels, readSeq.charAt(pos), isFwdStrand, nbReplicates);
                                if (mapping != null) {
                                    mapping.addDiff(d);
                                } else {
                                    diffs.add(d);
                                }
                                ++differences;
                            }
                        }
                        baseNo += count; 
                    } //P and H = padding and hard clipping do not contribute to differences
                } catch (NumberFormatException e) {
                    //error in the cigar, we currently skip this entry and treat it as match...
                    //TODO: return msg to user about cigar error
                }
            } else {
                //do nothing, we pretend, this is a match
            }
        }
        
        if (mapping != null) {
            mapping.setDifferences(differences);
        }
        
        return new PersistantDiffAndGapResult(diffs, gaps, gapOrderIndex, differences);
    }


}
