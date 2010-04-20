package vamp.parsing.mappings;

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
import vamp.importer.TrackJob;

/**
 * This class is a slightly different version of the JokParser. In contrast to
 * the JokParser, this parser expects filtered mapping results, such that each
 * duplicate read was filtered BEFORE the mapping and the abundance of this read
 * is encoded in the readname by adding #x to the readname, with x being the
 * number of equal sequences found during the filtering step.
 * @author ddoppmeier
 */
public class UniqueJokParser implements MappingParserI{

    private static String name = "Unique Reads Jok Output Parser";
    private static String fileExtension = "out";
    private static String fileDescription = "Jok Output";
    private HashMap<Integer, Integer> gapOrderIndex;

    public UniqueJokParser(){
        gapOrderIndex = new HashMap<Integer, Integer>();
    }


    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException{

        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \""+trackJob.getFile().getAbsolutePath()+"\"");
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                lineno++;

                // tokenize input line
                String[] tokens = line.split("\\t", 8);

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

                // check tokens
                if(readname == null || readname.equals("")){
                    throw new ParsingException("could not read readname in " +
                            ""+trackJob.getFile().getAbsolutePath()+" line "+lineno+". " +
                            "Found read name: "+readname);
                }
                // split the readname into name and counting information
                String[] parts = readname.split("#");
                int count = Integer.parseInt(parts[parts.length-1]);

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
                            "Found: "+readSeq);
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
                mapping.setCount(count);

                int seqID = readnameToSequenceID.get(readname);
                mappingContainer.addParsedMapping(mapping, seqID);
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

    private Character getReverseComplement(char base){
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char "+base+"!");
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
                        base = getReverseComplement(base);
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
                        c = getReverseComplement(c);
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
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String getParserName(){
        return name;
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
