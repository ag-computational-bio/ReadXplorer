package vamp.parsing.mappings;


import vamp.importer.TrackJobs;
import vamp.parsing.common.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.parsing.common.ParsingException;

/**
 *
 * @author ddoppmeier
 */
public class JokParser implements MappingParserI{

    private static String name = "Jok Output Parser";
    private static String[] fileExtension = new String []{"out", "Jok", "jok"};
    private static String fileDescription = "Jok Output";
    private HashMap<Integer, Integer> gapOrderIndex;

    public JokParser(){
        gapOrderIndex = new HashMap<Integer, Integer>();
    }


    @Override
    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException{

        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
      //  ParsedRun run = new ParsedRun("");

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \""+trackJob.getFile().getAbsolutePath()+"\"");
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\s", 8);

                // cast tokens
                String readname = tokens[0];
                int start = Integer.parseInt(tokens[1]);
                int stop = Integer.parseInt(tokens[2]);
                start++;
                stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                byte direction = 0;
                if(tokens[3].equals(">>")){
                    direction = 1;
                } else if(tokens[3].equals("<<")){
                    direction = -1;
                }
                String readSeq = tokens[4];
                String refSeq = tokens[5];
                int errors = Integer.parseInt(tokens[6]);

                if(stop>2 && readSeq != null && !readSeq.equals("")){
                // check tokens
                if(readname == null || readname.equals("")){
                    throw new ParsingException("could not read readname in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                            "Found read name: "+readname);
                }
                if(start >= stop){
                    throw new ParsingException("start bigger than stop in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                            "Found start: "+start+", stop: "+stop);
                }
                if(direction == 0){
                    throw new ParsingException("could not parse direction in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". "+
                            "Must be >> oder <<");
                }
                if(readSeq == null || readSeq.equals("")){
                    throw new ParsingException("read sequence could not be parsed in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                            "Found: "+readSeq + tokens.length);
                }
                if(refSeq == null || refSeq.equals("")){
                    throw new ParsingException("reference sequence could not be parsed in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                                "Found: "+refSeq);
                }
                if(readSeq.length() != refSeq.length()){
                    throw new ParsingException("alignment sequences have different length in " +
                            ""+ trackJob.getFile().getAbsolutePath() +" line " + lineno+ "! " +
                            "Found read sequence: "+readSeq+", reference sequence: "+refSeq);
                }
                if(errors < 0 || errors > readSeq.length()){
                    throw new ParsingException("Error number has invalid value "+errors+"" +
                            " in "+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                            "Must be bigger or equal to zero and smaller that alignment length." +
                            "readname");
                }
                if(!readnameToSequenceID.containsKey(readname)){
                    throw new ParsingException("Could not find sequence id mapping for read  "+readname+ "" +
                            " in "+trackJob.getFile().getAbsolutePath()+ "line "+lineno+". " +
                            "Please make sure you are referencing the correct read data set!" );
                }

                DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                List<ParsedDiff> diffs = result.getDiffs();
                List<ParsedReferenceGap> gaps = result.getGaps();

                ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                int seqID = readnameToSequenceID.get(readname);
                mappingContainer.addParsedMapping(mapping, seqID);
            }else{
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mappings without Reads:\""+lineno+"\"");   
            }
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising mapping data from \""+trackJob.getFile().getAbsolutePath()+"\"");

        } catch (IOException ex){
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }


    private int getOrderForGap(int gapPos){
        if(!gapOrderIndex.containsKey(gapPos)){
            gapOrderIndex.put(gapPos, 0);
        }
        int order = gapOrderIndex.get(gapPos);

        // increase order for next request
        gapOrderIndex.put(gapPos, order+1);

        return order;
    }

    private Character getReverseComplement(char base, String readSeq){
        Character rev = ' ';
        if(base == 'A'){
            rev = 'T';
        } else if(base == 'C'){
            rev = 'G';
        } else if(base == 'G'){
            rev = 'C';
        } else if(base == 'T'){
            rev = 'A';
        } else if(base == 'N'){
            rev = 'N';
        } else if(base == '_'){
            rev = '_';
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char "+base+"!Sequence: " + readSeq);
        }

        return rev;
    }

    private DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction){
        List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
        List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();

        int absPos;
        gapOrderIndex.clear();

        for(int i = 0, basecounter =0; i<readSeq.length(); i++){
            if(readSeq.charAt(i) != refSeq.charAt(i)){
                absPos = start+basecounter;
                if(refSeq.charAt(i) == '_'){
                    // store a lower case char, if this is a gap in genome
                    Character base = readSeq.charAt(i);
                    base = Character.toUpperCase(base);
                    if(direction == -1){
                        base = getReverseComplement(base , readSeq);
                    }

                    ParsedReferenceGap gap = new ParsedReferenceGap(absPos, base, this.getOrderForGap(absPos));
                    gaps.add(gap);
                    // note: do not increase position. that means that next base of read is mapped
                    // to the same position as this gap. two subsequent gaps map to the same position!
                } else {
                    // store the upper case char from input file, if this is a modification in the read
                    char c = readSeq.charAt(i);
                    c = Character.toUpperCase(c);
                    if(direction == -1){
                        c = getReverseComplement(c, readSeq);
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
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String getParserName(){
        return name;
    }

    @Override
    public String[] getFileExtensions() {
       return fileExtension;
    }

    @Override
    public ParsedRun parseInputForReadData(TrackJobs trackJob) throws ParsingException {
        
        ParsedRun run = new ParsedRun(fileDescription);
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing read data from mappings from file \""+trackJob.getFile().getAbsolutePath()+"\"");
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));
            int no = 0;
            int lineno = 0;
            String line = null;

            while ((line = br.readLine()) != null) {
                lineno++;
                // tokenize input line
                String[] tokens = line.split("\\t", 8);
                // cast tokens
                String readname = tokens[0];
                String refSeq = tokens[5];
                int stop = Integer.parseInt(tokens[2]);
                String readSeq = tokens[4];
                if(stop>2 && readSeq != null && !readSeq.equals("")&& refSeq != null && !refSeq.equals("")){
                String editReadSeq = readSeq;
                if(editReadSeq.contains("_")){
                    editReadSeq = editReadSeq.replaceAll("_", "");
                }
                editReadSeq = editReadSeq.toLowerCase();
                run.addReadData(editReadSeq, readname);
                if(lineno == 2000000){
                    no += lineno;
                  Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Parsed reads:"+no);
                  lineno = 0;
                }
                }
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parsing read data from mapping data from \""+trackJob.getFile().getAbsolutePath()+"\"");

        } catch (IOException ex){
            throw new ParsingException(ex);
        }
        run.setTimestamp(trackJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "read data successfully parsed");
        return run;
    }

    private class DiffAndGapResult{

        private List<ParsedDiff> diffs;
        private List<ParsedReferenceGap> gaps;

        public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps){
            this.diffs = diffs;
            this.gaps = gaps;
        }

        public List<ParsedDiff> getDiffs() {
            return diffs;
        }

        public List<ParsedReferenceGap> getGaps() {
            return gaps;
        }



    }

}
