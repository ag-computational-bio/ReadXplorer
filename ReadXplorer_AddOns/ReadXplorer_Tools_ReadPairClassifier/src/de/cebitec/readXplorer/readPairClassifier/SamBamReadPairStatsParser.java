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
import de.cebitec.readXplorer.util.StatsContainer;
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
public class SamBamReadPairStatsParser extends SamBamReadPairClassifier {

    //TODO: identify when pair goes across end of genome but only if circular reference genome
    private TrackJob trackJob;
    private int dist;
    private DiscreteCountingDistribution readPairSizeDistribution;

    /**
     * A parser only responsible for parsing read pair statistics for a
     * track. This parser is mainly used for track, which have already been
     * imported into another ReadXplorer DB and are now reimported.
     * @param readPairJobContainer container with both track jobs of this pair
     * @param chromLengthMap mapping of chromosome names to their length
     * @param classificationMap the classification map of the track - not needed
     * in this parser until now
     */
    public SamBamReadPairStatsParser(ReadPairJobContainer readPairJobContainer, Map<String, Integer> chromLengthMap, Map<String, ParsedClassification> classificationMap) {
        super(readPairJobContainer, chromLengthMap);
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
            long finish;
            int lineNo = 0;
            this.notifyObservers(NbBundle.getMessage(SamBamReadPairClassifier.class, "ReadPairStatsParser.Start"));

            samBamReader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = samBamReader.iterator();

            String refName = trackJob.getRefGen().getName();

            SAMRecord record;
            char pairTag;
            Object classobj;
            ReadPairType pairClass;
            int insertSize;
            while (samItor.hasNext()) {
                ++lineNo;
                //separate all mappings of same pair by read pair tag and hand it over to classification then
                record = samItor.next();
                if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {
                    pairTag = CommonsMappingParser.getReadPairTag(record);
                    
                    if (pairTag == Properties.EXT_A1) {
                        
                        classobj = record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        if (classobj != null) {
                            if (classobj instanceof Integer && ((int) classobj) >= -128 && ((int) classobj) <= 128) {
                                pairClass = ReadPairType.getReadPairType(Integer.valueOf(classobj.toString()));
                                this.statsContainer.incReadPairStats(pairClass, 1);
                                insertSize = Math.abs(record.getInferredInsertSize());
                                if (insertSize != 0) { // 0 = unpaired/not available
                                    this.readPairSizeDistribution.increaseDistribution(insertSize);
                                }
                            }
                            
                        } else {
                            this.statsContainer.incReadPairStats(ReadPairType.UNPAIRED_PAIR, 1);
                        }
                        
                    } else if (pairTag == Properties.EXT_A2) {
                        
                        classobj = record.getAttribute(Properties.TAG_READ_PAIR_TYPE);
                        if (classobj != null && classobj instanceof Integer) {
                            pairClass = ReadPairType.getReadPairType(Integer.valueOf(classobj.toString()));
                            if (pairClass == ReadPairType.UNPAIRED_PAIR) {
                                this.statsContainer.incReadPairStats(pairClass, 1);
                            } //else we have already counted read 1 of the pair
                        } else {
                            this.statsContainer.incReadPairStats(ReadPairType.UNPAIRED_PAIR, 1);
                        }
                    }
                }
                
                if (lineNo % 500000 == 0) {
                    finish = System.currentTimeMillis();
                    this.notifyObservers(Benchmark.calculateDuration(start, finish, lineNo + " mappings processed in "));
                }
            }

            samItor.close();

            finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(SamBamReadPairClassifier.class, "ReadPairStatsParser.Finish");
            this.notifyObservers(Benchmark.calculateDuration(start, finish, msg));

            this.statsContainer.setReadPairDistribution(this.readPairSizeDistribution);

        } catch (Exception e) {
            this.notifyObservers(NbBundle.getMessage(SamBamReadPairClassifier.class, "ReadPairStatsParser.Error", e.getMessage()));
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
        }

        return new ParsedReadPairContainer();
    }
    
    /**
     * Sets the stats container to keep track of statistics for this track.
     * @param statsContainer The stats container to add
     */
    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }

}
