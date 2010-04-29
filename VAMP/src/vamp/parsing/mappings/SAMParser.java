/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *   This file is part of ProSE.
 *   Copyright (C) 2007-2010 CeBiTec, Bielefeld University
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package vamp.parsing.mappings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.databackend.dataObjects.PersistantReference;
import vamp.importer.TrackJob;
import vamp.parsing.common.ParsedDiff;
import vamp.parsing.common.ParsedMapping;
import vamp.parsing.common.ParsedMappingContainer;
import vamp.parsing.common.ParsedReferenceGap;
import vamp.parsing.common.ParsingException;
import vamp.databackend.dataObjects.PersistantReference;
import vamp.databackend.connector.ProjectConnector;

/**
 *
 * @author jstraube
 */
public class SAMParser implements MappingParserI {

    private static String name = "SAM Parser";
    private static String[] fileExtension = new String[]{"out"};
    private static String fileDescription = "SAM Output";
    private HashMap<Integer, Integer> gapOrderIndex;
    private PersistantReference refGen;

    public SAMParser() {
        gapOrderIndex = new HashMap<Integer, Integer>();
    }

    @Override
    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException {
        String readname = null;
        String position = null;
        String refName = null;
        int flag = 0;
        String readSeq = null;
        String optional = null;
        String cigar = null;
        int errors = 0;
        String refSeq = null;
        ParsedMappingContainer mappingContainer = new ParsedMappingContainer();
        List<PersistantReference> genoms = ProjectConnector.getInstance().getGenomes();

        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start parsing mappings from file \"" + trackJob.getFile().getAbsolutePath() + "\"");
            BufferedReader br = new BufferedReader(new FileReader(trackJob.getFile()));

            int lineno = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                lineno++;
                //parsing the SAM format in following parts
                if (line.startsWith("read")) {
                    String[] readSeqLine = line.split(" ");
                    readname = readSeqLine[0];
                    flag = Integer.parseInt(readSeqLine[1]);
                    refName = readSeqLine[2];
                    position = readSeqLine[3];
                    //  String mappingQuality = readSeqLine[4];
                    cigar = readSeqLine[5];
                    //   String inferredInsertSize = readSeqLine[7];
                    readSeq = readSeqLine[9];
                    optional = readSeqLine[11];

                }
                if (optional.startsWith("NM")) {
                    errors = Integer.parseInt(optional.split(":")[2]);
                }

                if (refSeq == null) {
                    for (PersistantReference genome : genoms) {
                        if (genome.getName().equals(refName)) {
                            refSeq = genome.getSequence();
                        }
                    }

                    int start = Integer.parseInt(position);
                    int stop = countStopPosition(cigar, start, readSeq.length());
                    start++;
                    stop++; // some people (no names here...) start counting at 0, I count genome position starting with 1
                    byte direction = 0;

                    if (isForwardRead(flag)) {
                        //is fw or rev?????
                        direction = 1;
                    } else  {
                        direction = -1;
                    }




                    // check tokens
                    if (readname == null || readname.equals("")) {
                        throw new ParsingException("could not read readname in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found read name: " + readname);
                    }
                    // split the readname into name and counting information
                    String[] parts = readname.split("#");
                    int count = Integer.parseInt(parts[parts.length - 1]);

                    if (start >= stop) {
                        throw new ParsingException("start bigger than stop in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found start: " + start + ", stop: " + stop);
                    }
                    if (direction == 0) {
                        throw new ParsingException("could not parse direction in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be >> oder <<");
                    }
                    if (readSeq == null || readSeq.equals("")) {
                        throw new ParsingException("read sequence could not be parsed in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + readSeq);
                    }
                    if (refSeq == null || refSeq.equals("")) {
                        throw new ParsingException("reference sequence could not be parsed in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Found: " + refSeq);
                    }
                    if (readSeq.length() != refSeq.length()) {
                        throw new ParsingException("alignment sequences have different length in "
                                + "" + trackJob.getFile().getAbsolutePath() + " line " + lineno + "! "
                                + "Found read sequence: " + readSeq + ", reference sequence: " + refSeq);
                    }
                    if (errors < 0 || errors > readSeq.length()) {
                        throw new ParsingException("Error number has invalid value " + errors + ""
                                + " in " + trackJob.getFile().getAbsolutePath() + " line " + lineno + ". "
                                + "Must be bigger or equal to zero and smaller that alignment length."
                                + "readname");
                    }
                    if (!readnameToSequenceID.containsKey(readname)) {
                        throw new ParsingException("Could not find sequence id mapping for read  " + readname + ""
                                + " in " + trackJob.getFile().getAbsolutePath() + "line " + lineno + ". "
                                + "Please make sure you are referencing the correct read data set!");
                    }

                    DiffAndGapResult result = this.createDiffsAndGaps(readSeq, refSeq, start, direction);
                    List<ParsedDiff> diffs = result.getDiffs();
                    List<ParsedReferenceGap> gaps = result.getGaps();

                    ParsedMapping mapping = new ParsedMapping(start, stop, direction, diffs, gaps, errors);
                    mapping.setCount(count);

                    int seqID = readnameToSequenceID.get(readname);
                    mappingContainer.addParsedMapping(mapping, seqID);
                }
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished parising mapping data from \"" + trackJob.getFile().getAbsolutePath() + "\"");

        } catch (IOException ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Mapping data successfully parsed");
        return mappingContainer;
    }

    private int getOrderForGap(int gapPos) {
        if (!gapOrderIndex.containsKey(gapPos)) {
            gapOrderIndex.put(gapPos, 0);
        }
        int order = gapOrderIndex.get(gapPos);

        // increase order for next request
        gapOrderIndex.put(gapPos, order + 1);

        return order;
    }

    private Character getReverseComplement(char base) {
        Character rev = ' ';
        if (base == 'A') {
            rev = 'T';
        } else if (base == 'C') {
            rev = 'G';
        } else if (base == 'G') {
            rev = 'C';
        } else if (base == 'T') {
            rev = 'A';
        } else if (base == 'N') {
            rev = 'N';
        } else if (base == '_') {
            rev = '_';
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown char " + base + "!");
        }

        return rev;
    }

    private DiffAndGapResult createDiffsAndGaps(String readSeq, String refSeq, int start, byte direction) {
        List<ParsedDiff> diffs = new ArrayList<ParsedDiff>();
        List<ParsedReferenceGap> gaps = new ArrayList<ParsedReferenceGap>();

        int absPos;
        gapOrderIndex.clear();

        for (int i = 0, basecounter = 0; i < readSeq.length(); i++) {
            if (readSeq.charAt(i) != refSeq.charAt(i)) {
                absPos = start + basecounter;
                if (refSeq.charAt(i) == '_') {
                    // store a lower case char, if this is a gap in genome
                    Character base = readSeq.charAt(i);
                    base = Character.toUpperCase(base);
                    if (direction == -1) {
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
                    if (direction == -1) {
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

    public int countStopPosition(String cigar, Integer startPosition, Integer readLength) {
        int stopPosition;
        int numberOfInsertions = 0;
        int numberofDeletion = 0;
        List<Integer> countInsertions = new ArrayList<Integer>();
        countInsertions.add(cigar.indexOf("I"));

        stopPosition = startPosition + readLength - numberOfInsertions + numberofDeletion;
        return stopPosition;
    }


    public boolean isForwardRead(Integer flag) {
        boolean isForward;
        String binaryValue = Integer.toBinaryString(flag);
        int binaryLength = binaryValue.length();
        String b = binaryValue.substring(binaryLength-6,binaryLength-5);

        if (b.equals("1")) {
            isForward = false;
        } else {
            isForward = true;
        }
        System.out.println(isForward);
        System.out.println(binaryValue);
        return isForward;
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

    private class DiffAndGapResult {

        private List<ParsedDiff> diffs;
        private List<ParsedReferenceGap> gaps;

        public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps) {
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
