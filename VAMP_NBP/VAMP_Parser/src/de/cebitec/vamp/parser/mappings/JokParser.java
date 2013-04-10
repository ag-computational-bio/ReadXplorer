package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Parser for parsing jok data files for vamp.
 *
 * @author ddoppmeier
 */
public class JokParser implements MappingParserI {

    private static String name = "Jok Output Parser";
    private static String[] fileExtension = new String[]{"out", "Jok", "jok", "JOK"};
    private static String fileDescription = "Jok Read Mappings";
    private HashMap<Integer, Integer> gapOrderIndex;
    private HashMap<String, Integer> seqToIDMap;
    private SeqPairProcessorI seqPairProcessor;

    private ArrayList<Observer> observers;
    private String msg;

    /**
     * Parser for parsing jok data files for vamp.
     */
    public JokParser() {
        this.gapOrderIndex = new HashMap<>();
        this.observers = new ArrayList<>();
        this.seqPairProcessor = new SeqPairProcessorDummy();
    }
    
    /**
     * Parser for parsing jok data files for vamp. Use this constructor for
     * parsing sequence pair data along with the ordinary track data.
     * @param seqPairProcessor the specific sequence pair processor for handling
     *      sequence pair data
     */
    public JokParser(SeqPairProcessorI seqPairProcessor) {
        this();
        this.seqPairProcessor = seqPairProcessor;
    }

    /**
     * Does nothing, as the jok parser currently does not need any conversions.
     * @param trackJob
     * @param referenceSequence
     * @return true
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    @Override
    public Object convert(TrackJob trackJob, String referenceSequence) throws ParsingException, OutOfMemoryError {
        return true;
    }

    /**
     * Not implemented for this parser implementation, as currently no
     * preprocessing is needed.
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        return true;
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        
        this.preprocessData(trackJob);
        
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        this.seqToIDMap = new HashMap<>();
        String filepath = trackJob.getFile().getAbsolutePath();
        
        try (BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()))) {

            this.sendMsg(NbBundle.getMessage(JokParser.class, "Parser.Parsing.Start", filepath));
            int lineno = 0;
            String line;
            int seqID;
            int sumReadLength = 0;
            int start;
            int stop;
            byte direction;
            String readSeq;
            String refSeq;
            int differences;
            DiffAndGapResult result;
            String readwithoutGaps;
            List<ParsedDiff> diffs;
            List<ParsedReferenceGap> gaps;
            ParsedMapping mapping;
            StringBuilder sBuilder;
            String[] read;
            while ((line = br.readLine()) != null) {
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\t+", 8);
                if (tokens.length == 7) { // if the length is not correct the read is not parsed
                    // cast tokens
                    String readName = tokens[0];
                    try {
                        start = Integer.parseInt(tokens[1]);
                        stop = Integer.parseInt(tokens[2]);
                        start++;
                        stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                    } catch (NumberFormatException e) { //
                        if (!tokens[1].equals("*")) {
                            this.sendMsg("Value for current start position in "
                                    + filepath + " line " + lineno + " is not a number or *. "
                                    + "Found start: " + tokens[1]);
                        }
                        if (!tokens[2].equals("*")) {
                            this.sendMsg("Value for current stop position in "
                                    + filepath + " line " + lineno + " is not a number or *. "
                                    + "Found stop: " + tokens[2]);
                        }
                        continue; //*'s are ignored = unmapped read
                    }

                    direction = 0;
                    if (tokens[3].equals(">>")) {
                        direction = 1;
                    } else if (tokens[3].equals("<<")) {
                        direction = -1;
                    }
                    readSeq = tokens[4];
                    refSeq = tokens[5];
                    try {
                        differences = Integer.parseInt(tokens[6]);
                    } catch (NumberFormatException e) {
                        this.sendMsg("Value for current errors in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number. "
                                + "Found error: " + tokens[2]);
                        continue;
                    }
                    //*'s are ignored = unmapped read
                    if (!ParserCommonMethods.checkReadJok(this, readSeq, readName, refSeq, sequenceString.length(), start, stop, direction, filepath, lineno)) {
                        continue; //continue, and ignore read, if it contains inconsistent information
                    }
                    
                    //                if (readSeq.length() != refSeq.length()) {
                    //                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                    //                            "Parser.checkMapping.ErrorReadLength",
                    //                            filepath, lineno, readSeq, refSeq));
                    //                    continue;
                    //                }
                    //                    if (!readnameToSequenceID.containsKey(readname)) {
                    //                        this.sendErrorMsg("Could not find sequence id mapping for read " + readname
                    //                                + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
                    //                                + "Please make sure you are referencing the correct read data set!");
                    //                        continue;
                    //                    }


                    // Reads with an error already skip this part because of "continue" statements
                    //++noReads; //would be the count mappings
                    // parse read
                    result = ParserCommonMethods.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    diffs = result.getDiffs();
                    gaps = result.getGaps();
                    if (differences != result.getDifferences()) {
                        this.sendMsg("Value for current differences in "
                                + trackJob.getFile().getName() + " line " + lineno + " is differing from newly calculated number of differences."
                                + "Found differences: " + differences + " versus: " + result.getDifferences());
                        continue;
                    }
                    //dont ask me why but we have to do it
                    if (!gaps.isEmpty() || !diffs.isEmpty()) {
                        stop -= 1;
                    }

                    mapping = new ParsedMapping(start, stop, direction, diffs, gaps, differences);

                    if (readSeq.contains("_")) {
                        sBuilder = new StringBuilder();
                        read = readSeq.split("_+");
                        for (int i = 0; i < read.length; i++) {
                            sBuilder.append(read[i]);
                        }
                        readwithoutGaps = sBuilder.substring(0);

                    } else {
                        readwithoutGaps = readSeq;
                    }
                    //Saruman reverse-complements the read string and aligns it to the fwd strand of the reference!
                    readwithoutGaps = (direction == -1 ? SequenceUtils.getReverseComplement(readwithoutGaps) : readwithoutGaps);
                    if (this.seqToIDMap.containsKey(readwithoutGaps)) {
                        seqID = this.seqToIDMap.get(readwithoutGaps);
                    } else {
                        seqID = this.seqToIDMap.size();
                        this.seqToIDMap.put(readwithoutGaps, seqID);
                    }
                    mappingContainer.addParsedMapping(mapping, seqID);
                    sumReadLength += (stop - start);
                    this.seqPairProcessor.processReadname(seqID, readName);
                } else {
                    this.sendMsg(NbBundle.getMessage(JokParser.class, "Parser.Parsing.MissingData", lineno, line));
                }
            }
            //            Iterator<Integer> it = readnameToSequenceID.values().iterator();
            //            HashSet<Integer> s = new HashSet<Integer>();
            //            while (it.hasNext()) { //!!! was used before RUN domain was kicked!!!
            //                int i = it.next();
            //                s.add(i);
            //            }
            //            // it.remove();
            //            int numberMappings = mappingContainer.getMappingInformations().get(1);
            //            numberMappings = numberMappings == 0 ? 1 : numberMappings;
            mappingContainer.setSumReadLength(sumReadLength);

            this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Finished", filepath));
            
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
        if (mappingContainer.getMappedSequenceIDs().isEmpty()){ //if track does not contain any reads
            throw new ParsingException(NbBundle.getMessage(JokParser.class, "Parser.Empty.Track.Error"));
        }

        this.seqToIDMap = null; //release resources        

        return mappingContainer;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
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
        for (Observer observer : this.observers){
            observer.update(this.msg);
        }
    }

    /**
     * Method setting and sending the msg to all observers.
     * @param msg the msg to send (can be an error or any other message).
     */
    private void sendMsg(final String msg){
        this.msg = msg;
        this.notifyObservers(null);
    }

    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        return this.seqPairProcessor;
    }  
}
