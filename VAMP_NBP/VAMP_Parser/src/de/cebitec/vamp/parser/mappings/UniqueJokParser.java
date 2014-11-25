package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.util.StatsContainer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.*;
import de.cebitec.vamp.util.Observer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a slightly different version of the JokParser. In contrast to
 * the JokParser, this parser expects filtered mapping results, such that each
 * duplicate read was filtered BEFORE the mapping and the abundance of this read
 * is encoded in the readname by adding #x to the readname, with x being the
 * number of equal sequences found during the filtering step.
 * @author ddoppmeier
 */
public class UniqueJokParser implements MappingParserI {

    private static String name = "Unique Reads Jok Output Parser";
    private static String[] fileExtension = new String[]{"out"};
    private static String fileDescription = "Jok Output";
    
    private SeqPairProcessorI seqPairProcessor;
    private ArrayList<Observer> observers;
    private String errorMsg;
    private int noUniqueMappings;
    private HashMap<String, Integer> seqToIDMap;

    public UniqueJokParser() {
        this.observers = new ArrayList<>();
        this.seqPairProcessor = new SeqPairProcessorDummy();
    }
    
    /**
     * Parser for parsing jok data files for vamp in a slightly different
     * version of the JokParser. In contrast to the JokParser, this parser
     * expects filtered mapping results, such that each duplicate read was
     * filtered BEFORE the mapping and the abundance of this read is encoded in
     * the readname by adding #x to the readname, with x being the number of
     * equal sequences found during the filtering step.. Use this constructor
     * for parsing sequence pair data along with the ordinary track data.
     * @param seqPairProcessor the specific sequence pair processor for handling
     *      sequence pair data
     */
    public UniqueJokParser(SeqPairProcessorI seqPairProcessor) {
        this();
        this.seqPairProcessor = seqPairProcessor;
    }
    
    /**
     * Does nothing, as the unique jok parser currently does not need any
     * conversions.
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
//    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        this.seqToIDMap = new HashMap<>();

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));
            int lineno = 0;
            int start;
            int stop;
            int differences;
            this.noUniqueMappings = 0;
            int noUniqueSeq = 0;
            String line;
            while ((line = br.readLine()) != null) { //reads the input file line per line
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\s", 8);
                if (tokens.length == 7) { // if the length is not correct the read is not parsed

                    // cast tokens
                    String readname = tokens[0];
                    try {
                        start = Integer.parseInt(tokens[1]);
                        stop = Integer.parseInt(tokens[2]);
                        start++;
                        stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                    } catch (NumberFormatException e) { //
                        if (!tokens[1].equals("*")) {
                            this.sendErrorMsg("Value for current start position in "
                                    + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number or *. "
                                    + "Found start: " + tokens[1]);
                        }
                        if (!tokens[2].equals("*")) {
                            this.sendErrorMsg("Value for current stop position in "
                                    + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number or *. "
                                    + "Found stop: " + tokens[2]);
                        }
                        continue; //*'s are ignored = unmapped read
                    }
                    byte direction = 0;
                    if (tokens[3].equals(">>")) {
                        direction = 1;
                    } else if (tokens[3].equals("<<")) {
                        direction = -1;
                    }
                    String readSeq = tokens[4];
                    String refSeq = tokens[5];
                    try {
                        differences = Integer.parseInt(tokens[6]);
                    } catch (NumberFormatException e){
                        this.sendErrorMsg("Value for errors in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number. "
                                + "Found error count: " + tokens[6]);
                        continue;
                    }

                    // check tokens
                    if (readname == null || readname.isEmpty()) {
                        this.sendErrorMsg("Could not read readname in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found read name: " + readname);
                        continue;
                    }
                    // split the readname into name and counting information
                    String[] parts = readname.split("#");
                    int count = Integer.parseInt(parts[parts.length - 1]); //check for uniqueness & catch format exception

                    if (start >= stop) {
                        this.sendErrorMsg("Start bigger than stop in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found start: " + start + ", stop: " + stop);
                        continue;
                    }
                    if (direction == 0) {
                        this.sendErrorMsg("Could not parse direction in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be >> oder <<");
                        continue;
                    }
                    if (readSeq == null || readSeq.isEmpty()) {
                        this.sendErrorMsg("Read sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + readSeq + tokens.length);
                        continue;
                    }
                    if (refSeq == null || refSeq.isEmpty()) {
                        this.sendErrorMsg("Reference sequence could not be parsed in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + refSeq);
                        continue;
                    }
                    if (readSeq.length() != refSeq.length() && !refSeq.equals(("*"))) {
                        this.sendErrorMsg("Alignment sequences have different length in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + "! "
                                + "Found read sequence: " + readSeq + ", reference sequence: " + refSeq);
                        continue;
                    }
//                    if (!readnameToSequenceID.containsKey(readname)) {
//                        this.sendErrorMsg("Could not find sequence id mapping for read " + readname
//                                + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
//                                + "Please make sure you are referencing the correct read data set!");
//                        readErroneous = true;
//                    }

                    // Reads with an error already skip this part because of "continue" statements
                    DiffAndGapResult result = ParserCommonMethods.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();
                    if (differences != result.getDifferences()) {
                        this.sendErrorMsg("Value for current differences in "
                                + trackJob.getFile().getName() + " line " + lineno + " is differing from newly calculated number of differences."
                                + "Found differences: " + differences + " versus: " + result.getDifferences());
                        continue;
                    }
                    
                    if (differences < 0 || differences > readSeq.length()) {
                        this.sendErrorMsg("Error number has invalid value " + differences
                                + " in " + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be bigger or equal to zero and smaller than alignment length.");
                        continue;
                    }

                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, differences);
                    mapping.setCount(count);

                    int seqID;
                    if (this.seqToIDMap.containsKey(readSeq)) {
                        seqID = this.seqToIDMap.get(readSeq);
                    } else {
                        seqID = ++noUniqueSeq;
                        this.seqToIDMap.put(readSeq, seqID);
                    }
                    mappingContainer.addParsedMapping(mapping, seqID);
                    this.seqPairProcessor.processReadname(seqID, readname);
                } else {
                    this.sendErrorMsg("The current read in line " + lineno + "is missing some data: ".concat(line));
                }
            }

            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising mapping data from \"{0}\"", trackJob.getFile().getAbsolutePath());

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
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
            observer.update(this.errorMsg);
        }
    }

    /**
     * Method setting and sending the error msg to all observers.
     * @param errorMsg the error msg to send
     */
    private void sendErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
        this.notifyObservers(null);
    }

    @Override
    public SeqPairProcessorI getSeqPairProcessor() {
        return this.seqPairProcessor;
    }

    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        //do nothing right now
    }
}
