package de.cebitec.vamp.seqPairClassifier;

import de.cebitec.vamp.parser.SeqPairJobContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedClassification;
import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.mappings.ParserCommonMethods;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.DiscreteCountingDistribution;
import de.cebitec.vamp.util.Properties;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;

/**
 * A parser only responsible for parsing read pair statistics for a track.
 * This parser is mainly used for track, which have already been imported into
 * another ReadXplorer DB and are now reimported.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamDirectSeqPairStatsParser extends SamBamDirectSeqPairClassifier {

    //TODO: identify when pair goes across end of genome but only if circular reference genome
    private TrackJob trackJob;
    private int dist;
    private DiscreteCountingDistribution seqPairSizeDistribution;

    /**
     * A parser only responsible for parsing read pair statistics for a
     * track. This parser is mainly used for track, which have already been
     * imported into another ReadXplorer DB and are now reimported.
     * @param seqPairJobContainer container with both track jobs of this pair
     * @param refSeq the complete reference sequence
     * @param classificationMap the classification map of the track - not needed
     * in this parser until now
     */
    public SamBamDirectSeqPairStatsParser(SeqPairJobContainer seqPairJobContainer, String refSeq, Map<String, ParsedClassification> classificationMap) {
        super(seqPairJobContainer, refSeq, classificationMap);
        this.trackJob = seqPairJobContainer.getTrackJob1();
        this.dist = seqPairJobContainer.getDistance();
        int maxDist = this.calculateMinAndMaxDist(dist, seqPairJobContainer.getDeviation());
        this.seqPairSizeDistribution = new DiscreteCountingDistribution(maxDist * 3);
        seqPairSizeDistribution.setType(Properties.SEQ_PAIR_SIZE_DISTRIBUTION);
    }

    /**
     * Carries out the statistics parsing for the read pair job.
     * @return an empty container
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    @Override
    public ParsedSeqPairContainer classifySeqPairs() throws ParsingException, OutOfMemoryError {

        try (SAMFileReader samBamReader = new SAMFileReader(trackJob.getFile())) {
            long start = System.currentTimeMillis();
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "SeqPairStatsParser.Start"));

            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = samBamReader.iterator();

            String refName = trackJob.getRefGen().getName();

            SAMRecord record;
            char pairTag;
            Object classobj;
            byte pairClass;
            int insertSize;
            while (samItor.hasNext()) {
                //separate all mappings of same pair by seq pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {
                    pairTag = ParserCommonMethods.getReadPairTag(record);
                    
                    if (pairTag == Properties.EXT_A1) {
                        
                        classobj = record.getAttribute(Properties.TAG_SEQ_PAIR_TYPE);
                        if (classobj != null) {
                            if (classobj instanceof Integer && ((int) classobj) >= -128 && ((int) classobj) <= 128) {
                                pairClass = Byte.valueOf(classobj.toString());
                                this.getStatsContainer().incSeqPairStats(pairClass, 1);
                                insertSize = Math.abs(record.getInferredInsertSize());
                                if (insertSize != 0) { // 0 = unpaired/not available
                                    this.seqPairSizeDistribution.increaseDistribution(insertSize);
                                }
                            }
                            
                        } else {
                            this.getStatsContainer().incSeqPairStats(Properties.TYPE_UNPAIRED_PAIR, 1);
                        }
                        
                    } else if (pairTag == Properties.EXT_A2) {
                        
                        classobj = record.getAttribute(Properties.TAG_SEQ_PAIR_TYPE);
                        if (classobj != null && classobj instanceof Byte) {
                            pairClass = (Byte) classobj;
                            if (pairClass == Properties.TYPE_UNPAIRED_PAIR) {
                                this.getStatsContainer().incSeqPairStats(pairClass, 1);
                            } //else we have already counted read 1 of the pair
                        } else {
                            this.getStatsContainer().incSeqPairStats(Properties.TYPE_UNPAIRED_PAIR, 1);
                        }
                    }
                }
            }

            samItor.close();

            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "SeqPairStatsParser.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));

            this.getStatsContainer().setSeqPairDistribution(this.seqPairSizeDistribution);

        } catch (Exception e) {
            this.notifyObservers(NbBundle.getMessage(SamBamDirectSeqPairClassifier.class, "SeqPairStatsParser.Error", e.getMessage()));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
        }

        return new ParsedSeqPairContainer();
    }

}
