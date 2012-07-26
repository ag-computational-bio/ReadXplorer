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
    private static String fileDescription = "Jok Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private HashMap<String, Integer> seqToIDMap;
    private SeqPairProcessorI seqPairProcessor;

    private ArrayList<Observer> observers;
    private String msg;

    /**
     * Parser for parsing jok data files for vamp.
     */
    public JokParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.observers = new ArrayList<Observer>();
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

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        this.seqToIDMap = new HashMap<String, Integer>();
        String filepath = trackJob.getFile().getAbsolutePath();
        try {

            this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Start", filepath));

            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line;
            int seqID;
            int sumReadLength = 0;
            int start;
            int stop;
            byte direction;
            String readSeq;
            String refSeq;
            int errors;
            DiffAndGapResult result;
            String readwithoutGaps;
            while ((line = br.readLine()) != null) {
                lineno++;
                
                // tokenize input line
                String[] tokens = line.split("\\t+", 8);
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
                            this.sendMsg("Value for current start position in "
                                    + filepath + " line " + lineno + " is not a number or *. "
                                    + "Found start: " + tokens[1]);
                        }
                        if (!tokens[2].equals("*")) {
                            this.sendMsg("Value for current stop position in "
                                    + filepath  + " line " + lineno + " is not a number or *. "
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
                        errors = Integer.parseInt(tokens[6]); //TODO: check why errors set twice
                    } catch (NumberFormatException e) {
                        this.sendMsg("Value for current errors in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number. "
                                + "Found error: " + tokens[2]);
                        continue;
                    }
                    // check tokens
                    // report empty mappings saruman should not be producing anymore
                if (readname == null || readname.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                            "Parser.checkMapping.ErrorReadname",
                            filepath, lineno, readname));
                    continue;
                }

                if (start >= stop) {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                            "Parser.checkMapping.ErrorStartStop",
                            filepath, lineno, start, stop));
                    continue;
                }
                if (direction == 0) {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                            "Parser.checkMapping.ErrorDirectionJok", filepath, lineno));
                    continue;
                }
                if (readSeq == null || readSeq.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                            "Parser.checkMapping.ErrorReadEmpty",
                            filepath, lineno, readSeq));
                    continue;
                }
                if (refSeq == null || refSeq.isEmpty()) {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,
                            "Parser.checkMapping.ErrorRef",
                            filepath, lineno, refSeq));
                    continue;
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
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();
                    errors = result.getErrors();
                    //dont ask me why but we have to do it
                    if (!gaps.isEmpty() || !diffs.isEmpty()) {
                        stop -= 1;
                    }
             
                    if (errors < 0 || errors > readSeq.length()) {
                        this.sendMsg(NbBundle.getMessage(JokParser.class,
                                "Parser.checkMapping.ErrorRead",
                                errors, filepath, lineno));
                        continue;
                    }

                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    
                    if (readSeq.contains("_")) {
                        StringBuilder sBuilder = new StringBuilder();
                        String[] read = readSeq.split("_+");
                        for (int i = 0; i < read.length; i++) {
                            sBuilder.append(read[i]);
                        }
                        readwithoutGaps = sBuilder.substring(0);

                    } else {
                        readwithoutGaps = readSeq;
                    }
                    //Saruman flips only the read string by mapping so we can get the native read direction
                    readwithoutGaps = (direction == -1 ? SequenceUtils.getReverseComplement(readwithoutGaps) : readwithoutGaps);
                    if (this.seqToIDMap.containsKey(readwithoutGaps)) {
                        seqID = this.seqToIDMap.get(readwithoutGaps);
                    } else {
                        seqID = this.seqToIDMap.size();
                        this.seqToIDMap.put(readwithoutGaps, seqID);
                    }
                    mappingContainer.addParsedMapping(mapping, seqID);
                    sumReadLength += (stop - start);
                    this.seqPairProcessor.processReadname(seqID, readname);
                } else {
                    this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.MissingData", lineno, line));
                }
            }
//            Iterator<Integer> it = readnameToSequenceID.values().iterator();
//            HashSet<Integer> s = new HashSet<Integer>();
//            while (it.hasNext()) { //!!! was used before RUN domain was kicked!!!
//                int i = it.next();
//                s.add(i);
//            }
//            // it.remove();
            int numberMappings = mappingContainer.getMappingInformations().get(1);
            numberMappings = numberMappings == 0 ? 1 : numberMappings;
            mappingContainer.setSumReadLength(sumReadLength);
            
//            s.clear();
            br.close();

            this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Finished", filepath));
            
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
        if (mappingContainer.getMappedSequenceIDs().isEmpty()){ //if track does not contain any reads
            throw new ParsingException(NbBundle.getMessage(JokParser.class, "Parser.Empty.Track.Error"));
        }

        this.seqToIDMap = null; //release resources        
        this.sendMsg(NbBundle.getMessage(JokParser.class,"Parser.Parsing.Successfully"));

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

    /**
     * Dummy method.
     * @return null, because it is not needed here.
     */
    @Override
    public Object getAdditionalData() {
        return null;
    }
    
}
