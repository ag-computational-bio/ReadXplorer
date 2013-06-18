package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.ErrorLimit;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.StatsContainer;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;
import org.openide.util.NbBundle;

/**
 * Parser only for parsing statistics, which could not be calculated during the
 * other import steps.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class StatsParser implements MappingParserI {
    
    private StatsContainer statsContainer = new StatsContainer();
    private List<Observer> observers = new ArrayList<>();

    @Override
    public Object parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        
        String refName = trackJob.getRefGen().getName();
        List<String> readNameList = new ArrayList<>();
        
        try (SAMFileReader sam = new SAMFileReader(trackJob.getFile())) {
            sam.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
            SAMRecordIterator samItor = sam.iterator();

            SAMRecord record;
            String readName;
            ErrorLimit errorLimit = new ErrorLimit();
            int lineno = 0;
            while (samItor.hasNext()) {
                try {
                    ++lineno;
                    record = samItor.next();
                    if (!record.getReadUnmappedFlag() && record.getReferenceName().equals(refName)) {

                        readName = record.getReadName();
                        if (!readNameList.contains(readName)) {
                            readNameList.add(readName);
                        }
                    }
                } catch (SAMFormatException e) {
                    if (errorLimit.allowOutput()) {
                        if (!e.getMessage().contains("MAPQ should be 0")) {
                            this.notifyObservers(NbBundle.getMessage(SamBamDirectParser.class,
                                    "Parser.Parsing.CorruptData", lineno, e.toString()));
                        }
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored  
                }
            }
        } catch (RuntimeEOFException e) {
            this.notifyObservers("Last read in the file is incomplete, ignoring it.");
        }
        this.statsContainer.increaseValue(StatsContainer.NO_READS, readNameList.size());
        return statsContainer;
    }

    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object convert(TrackJob trackJob, String referenceSequence) throws ParsingException, OutOfMemoryError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getFileExtensions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getInputFileDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        this.statsContainer = statsContainer;
    }
    
    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
    
    
}
