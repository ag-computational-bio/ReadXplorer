package de.cebitec.readXplorer.readPairClassifier;

import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsedReadPairContainer;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.DiscreteCountingDistribution;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.ReadPairType;
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
public class SamBamDirectReadPairStatsParser extends SamBamDirectReadPairClassifier {

    //TODO: identify when pair goes across end of genome but only if circular reference genome
    private TrackJob trackJob;
    private int dist;
    private DiscreteCountingDistribution readPairSizeDistribution;

    /**
     * A parser only responsible for parsing read pair statistics for a
     * track. This parser is mainly used for track, which have already been
     * imported into another ReadXplorer DB and are now reimported.
     * @param readPairJobContainer container with both track jobs of this pair
     * @param chromSeqMap mapping of chromosome names to their sequence
     * @param classificationMap the classification map of the track - not needed
     * in this parser until now
     */
    public SamBamDirectReadPairStatsParser(ReadPairJobContainer readPairJobContainer, Map<String,String> chromSeqMap, Map<String, ParsedClassification> classificationMap) {
        super(readPairJobContainer, chromSeqMap, classificationMap);
        this.trackJob = readPairJobContainer.getTrackJob1();
        this.dist = readPairJobContainer.getDistance();
        int maxDist = this.calculateMinAndMaxDist(dist, readPairJobContainer.getDeviation());
        this.readPairSizeDistribution = new DiscreteCountingDistribution(maxDist * 3);
        readPairSizeDistribution.setType(Properties.READ_PAIR_SIZE_DISTRIBUTION);
    }

    /**
     * Carries out the statistics parsing for the read pair job.
     * @return an empty container
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    @Override
    public ParsedReadPairContainer classifyReadPairs() throws ParsingException, OutOfMemoryError {

        try (SAMFileReader samBamReader = new SAMFileReader(trackJob.getFile())) {
            long start = System.currentTimeMillis();
            this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "ReadPairStatsParser.Start"));

            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = samBamReader.iterator();

            String refName = trackJob.getRefGen().getName();

            SAMRecord record;
            char pairTag;
            Object classobj;
            ReadPairType pairClass;
            int insertSize;
            while (samItor.hasNext()) {
                //separate all mappings of same pair by read pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {
                    pairTag = CommonsMappingParser.getReadPairTag(record);
                    
                    if (pairTag == Properties.EXT_A1) {
                        
                        classobj = record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        if (classobj != null) {
                            if (classobj instanceof Integer && ((int) classobj) >= -128 && ((int) classobj) <= 128) {
                                pairClass = ReadPairType.getReadPairType(Integer.valueOf(classobj.toString()));
                                this.getStatsContainer().incReadPairStats(pairClass, 1);
                                insertSize = Math.abs(record.getInferredInsertSize());
                                if (insertSize != 0) { // 0 = unpaired/not available
                                    this.readPairSizeDistribution.increaseDistribution(insertSize);
                                }
                            }
                            
                        } else {
                            this.getStatsContainer().incReadPairStats(ReadPairType.UNPAIRED_PAIR, 1);
                        }
                        
                    } else if (pairTag == Properties.EXT_A2) {
                        
                        classobj = record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        if (classobj != null && classobj instanceof Integer) {
                            pairClass = ReadPairType.getReadPairType(Integer.valueOf(classobj.toString()));
                            if (pairClass == ReadPairType.UNPAIRED_PAIR) {
                                this.getStatsContainer().incReadPairStats(pairClass, 1);
                            } //else we have already counted read 1 of the pair
                        } else {
                            this.getStatsContainer().incReadPairStats(ReadPairType.UNPAIRED_PAIR, 1);
                        }
                    }
                }
            }

            samItor.close();

            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "ReadPairStatsParser.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));

            this.getStatsContainer().setReadPairDistribution(this.readPairSizeDistribution);

        } catch (Exception e) {
            this.notifyObservers(NbBundle.getMessage(SamBamDirectReadPairClassifier.class, "ReadPairStatsParser.Error", e.getMessage()));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
        }

        return new ParsedReadPairContainer();
    }

}
