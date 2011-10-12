package de.cebitec.vamp.parser.mappings;

import java.util.ArrayList;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.DiffAndGapResult;
import de.cebitec.vamp.parser.common.ParsedDiff;
import de.cebitec.vamp.parser.common.ParsedMapping;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedReferenceGap;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.openide.util.NbBundle;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 *
 * @author jstraube
 */
public class SamBamStepParser implements MappingParserI ,Observer{

    private static String name = "SAM/BAM Stepwise Parser";
    private static String[] fileExtension = new String[]{"sam", "SAM", "Sam","bam", "BAM", "Bam"};
    private static String fileDescription = "SAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private int errors = 0;
    private HashMap<String, Integer> seqToIDMap;
    private int noUniqueMappings;
    private ArrayList<Observer> observers;
    private String errorMsg;
    private int noUniqueSeq = 0;
    //private BufferedReader brall=null;
    private SAMRecordIterator itorAll = null;
    private int lineno = 0;
    private int shift=0;
    private SAMRecord record = null;
    
    public SamBamStepParser() {
        this.gapOrderIndex = new HashMap<Integer, Integer>();
        this.seqToIDMap = new HashMap<String, Integer>();
       this.observers = new ArrayList<Observer>();
        
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException {
        this.seqToIDMap = new HashMap<String, Integer>();
        String readname = null;
//        String refName = null;
        String refSeq = null;
        String readSeq = null;
      //  int flag = 0;
        String readSeqwithoutGaps = null;
        String cigar = null;
        String filepath = trackJob.getFile().getAbsolutePath() ;
        this.noUniqueMappings = 0;
        int start;
        int stop;

   //     String refSeqfulllength = null;
        String refSeqwithoutgaps = null;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        //TODO check why if there is too much output we get a java heap space exception 
       // mappingContainer.registerObserver(this);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, NbBundle.getMessage(SamBamStepParser.class,
                "Parser.Parsing.Start", filepath));


        
            if(itorAll ==null){
            SAMFileReader sam = new SAMFileReader(trackJob.getFile());
            SAMRecordIterator itor = sam.iterator();
            itorAll = itor;
            }
             SAMRecord first = null;
            int end = trackJob.getStop();
            end = end+shift;
            
            while ( lineno <end) {
                 lineno++;
                 
                 
                 first =( record==null )? (itorAll.hasNext()? itorAll.next():null) :  record;
                //no more mappings
                if(first==null){
                    mappingContainer.setLastMappingContainer(true);
                    break;
                }
                record=null;            
                
                if (!first.getReadUnmappedFlag()) {
                    
                    readname = first.getReadName();
                    start = first.getAlignmentStart();
                    cigar = first.getCigarString();
                    readSeqwithoutGaps = first.getReadString();
                    
                            stop = 0;
                            errors = 0;

                            int length = sequenceString.length();
                            
                            if (cigar.contains("D") || cigar.contains("I")|| cigar.contains("S")) {
                                stop = ParserCommonMethods.countStopPosition(cigar, start, readSeqwithoutGaps.length());
                                refSeqwithoutgaps = sequenceString.substring(start - 1, stop);
                                String []refAndRead =  ParserCommonMethods.createMappingOfRefAndRead(cigar, refSeqwithoutgaps, readSeqwithoutGaps);
                                refSeq = refAndRead[0];
                                readSeq = refAndRead[1];
                            } else {
                                 stop = start + readSeqwithoutGaps.length() - 1;
                                refSeqwithoutgaps = sequenceString.substring(start - 1, stop);
                                refSeq = refSeqwithoutgaps;
                                readSeq = readSeqwithoutGaps;
                            }
                            
                            byte direction = 0;
                            // 1 = fwd, -1 = rev
                            direction =first.getReadNegativeStrandFlag() ? (byte) -1 : 1;

                            //check parameters
                            if(length<start || length<stop){
                                this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class,
                                        "Parser.checkMapping.ErrorReadPosition",
                                        filepath, lineno,start,stop,length));
                                continue;
                            }
                         if (readname == null || readname.isEmpty()) {
                                this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class,
                            "Parser.checkMapping.ErrorReadname", 
                            filepath, lineno,readname));
                    continue;
                }

                            if (start >= stop) {
                                this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class,
                                        "Parser.checkMapping.ErrorStartStop",filepath, lineno,start, stop));
                                continue;
                            }
                 if (direction == 0) {
                             this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                             "Parser.checkMapping.ErrorDirection", filepath,lineno ));
                           continue;
                       }
                if (readSeq == null || readSeq.isEmpty()) {
                    this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                            "Parser.checkMapping.ErrorReadEmpty", 
                            filepath,lineno,readSeq));
                    continue;
                }
                if (refSeq == null || refSeq.isEmpty()) {
                    this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                            "Parser.checkMapping.ErrorRef", 
                           filepath, lineno, refSeq));
                    continue;
                }
                if (readSeq.length() != refSeq.length()) {
                    this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                            "Parser.checkMapping.ErrorReadLength",
                            filepath,lineno, readSeq,refSeq));
                    continue;
                }
                if (errors < 0 || errors > readSeq.length()) {
                    this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                            "Parser.checkMapping.ErrorRead", 
                            errors,filepath, lineno ));
                    continue;
                }
                if (!cigar.matches("[MHISD\\d]+")) {
                    this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class, 
                            "Parser.checkMapping.ErrorCigar", cigar,filepath,lineno));
                    continue;
                }
                            //saruman starts genome at 0 other algorithms like bwa start genome at 1
                            DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                            List<ParsedDiff> diffs = result.getDiffs();
                            List<ParsedReferenceGap> gaps = result.getGaps();
                           
                            //saruman starts genome at 0 other algorithms like bwa start genome at 1
                            ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                            
                        

                            
                            int seqID;
                            if (this.seqToIDMap.containsKey(readSeqwithoutGaps)) {
                                seqID = this.seqToIDMap.get(readSeqwithoutGaps);
                            } else {
                                seqID = ++noUniqueSeq;
                                this.seqToIDMap.put(readSeqwithoutGaps, seqID);
                            } //int seqID = readnameToSequenceID.get(readname);
                            
                            mappingContainer.addParsedMapping(mapping, seqID);
                            mappingContainer.setNumberOfUniqueMappings(noUniqueMappings);
                            
                            
                            if(lineno==end){
                                       shift++;
                                         record =itorAll.hasNext()? itorAll.next():null;
                                         if(record !=null){
                                         String read =record.getReadString();
                                           if (this.seqToIDMap.containsKey(read)) {
                                            end=end+1;
                                           }
                                         }else{
                                               mappingContainer.setLastMappingContainer(true);
                                              break;
                                           }
                                }
                            
                        } else {
                          this.sendErrorMsg(NbBundle.getMessage(SamBamStepParser.class,
                                  "Parser.Parsing.CorruptData", lineno,first.getReadName()));
                        }
                //    }
                
            }

            
            this.seqToIDMap = null; //release resources
          //  brall.close();
            
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, NbBundle.getMessage(SamBamStepParser.class,
                "Parser.Parsing.Finished",filepath));
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,NbBundle.getMessage(SamBamStepParser.class,
                "Parser.Parsing.Successfully"));
        return mappingContainer;

    }
    /*
     * This method calculates the order of the gap infact that for a gap we dont include a new position to reference genome
     *  but we notice the number of gaps to one position of the ref genome
     */
    private int getOrderForGap(int gapPos) {
        if (!gapOrderIndex.containsKey(gapPos)) {
            gapOrderIndex.put(gapPos, 0);
        }
        int order = gapOrderIndex.get(gapPos);

        // increase order for next request
        gapOrderIndex.put(gapPos, order + 1);

        return order;
    }
    
    private DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction) {

        List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
        List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();
        errors = 0;
        int absPos;
        gapOrderIndex.clear();

        for (int i = 0, basecounter = 0; i < readSeq.length(); i++) {
            if (readSeq.toLowerCase().charAt(i) != refSeq.toLowerCase().charAt(i)) {
                errors++;
                absPos = start + basecounter;
                if (refSeq.charAt(i) == '_') {
                    // store a lower case char, if this is a gap in genome
                    Character base = readSeq.charAt(i);
                    base = Character.toUpperCase(base);
                    if (direction == -1) {
                        base = SequenceUtils.getComplement(base, readSeq);
                    }

                    ParsedReferenceGap gap = new ParsedReferenceGap(absPos, base, this.getOrderForGap(absPos));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the upper case char from input file, if this is a modification in the read
                    char c = readSeq.charAt(i);
                    c = Character.toUpperCase(c);
                    if (direction == -1) {
                        c = SequenceUtils.getComplement(c, readSeq);
                    }
                    ParsedDiff d = new ParsedDiff(absPos, c);
                    diffs.add(d);
                    basecounter++;
                }
            } else {
                basecounter++;
            }
        }

        return new DiffAndGapResult(diffs, gaps);
    }





    @Override
    public String getParserName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public ParsedRun parseInputForReadData(TrackJob trackJob) throws ParsingException {
        String readname = null;
        int position = 0;
        int flag = 0;

        String readSeqwithoutGaps = null;
        ParsedRun run = new ParsedRun(fileDescription);
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing run from file \"{0}\"", trackJob.getFile().getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            boolean readErroneous = false;
            while ((line = br.readLine()) != null) {

                lineno++;
                //parsing the SAM format in following parts
                if (!line.startsWith("@")) {

                    String[] readSeqLine = line.split("\\s+");
                    readname = readSeqLine[0];
                    try {
                        flag = Integer.parseInt(readSeqLine[1]);
                        position = Integer.parseInt(readSeqLine[3]);
                    } catch (NumberFormatException e) {
                        this.sendErrorMsg("Value for current flag or start position in "
                                + trackJob.getFile().getAbsolutePath() + " line " + lineno + " is not a number. "
                                + "Found flag: " + readSeqLine[1] + " and start position: " + readSeqLine[3]);
                        readErroneous = true;
                    }
                    readSeqwithoutGaps = readSeqLine[9].toLowerCase();
                    if (ParserCommonMethods.isMappedSequence(flag, position) && !readErroneous) {
                        run.addReadData(readSeqwithoutGaps, readname);
                    } else {
                        readErroneous = false;
                    }
                }
            }
        } catch (IOException ex) {
            this.sendErrorMsg("A parsing error occured during parsing process in "
                    + trackJob.getFile().getAbsolutePath());
            this.sendErrorMsg(NbBundle.getMessage(SAMFileReader.class, "MSG_ImportThread.import.parsedReads"));
            return run; // or better return something else?
        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Read data successfully parsed");
        return run;

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
    public void notifyObservers() {
        for (Observer observer : this.observers) {
            observer.update(this.errorMsg);
        }
    }

    /**
     * Method setting and sending the error msg to all observers.
     * @param errorMsg the error msg to send
     */
    private void sendErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
    //  this.notifyObservers();
    }

    @Override
    public void update(Object args) {
        if (args instanceof Boolean && (Boolean) args == true) {
            ++this.noUniqueMappings;
        }
    }

}
