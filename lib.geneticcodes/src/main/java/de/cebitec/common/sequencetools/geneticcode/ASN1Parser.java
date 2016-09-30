package de.cebitec.common.sequencetools.geneticcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rhilker
 *
 * Parses ASN1 data into a List of ParsedASN1Entries. Since the parser is used
 * for genetic code parsing from NCBI, it additionally creates entries for comments
 * starting with "-- Base".
 */
class ASN1Parser {

    /**
     * Parses ASN1 data into a List of ParsedASN1Entries. Since the parser is used
     * for genetic code parsing from NCBI, it additionally creates entries for comments
     * starting with "-- Base".
     * @param inputFile input ASN1 file
     * @return list of parsed entries from the ASN1 file
     */
    public ParsedASN1Table parseData(InputStream inputFile) throws IOException {

        ParsedASN1Table parsedTable = new ParsedASN1Table();
        List<ParsedASN1Entry> entryList = new ArrayList<ParsedASN1Entry>();
        InputStreamReader inReader = new InputStreamReader(inputFile);
        BufferedReader in = new BufferedReader(inReader);
        String line;
        int level = 0;
        HashMap<String, List<String>> entries = new HashMap<String, List<String>>();
        String key;
        String value;
        boolean closed = false;

        while ((line = in.readLine()) != null) {
            line = line.trim();

            if (!line.isEmpty() && (!line.startsWith("--") || line.startsWith("-- Base"))) {

                if (level == 0) { //new table entry
                    parsedTable.setTableHeader(line.substring(0, line.indexOf("::=")).trim());
                    ++level;
                } else if (level == 1) { //entry level in table

                    if (line.startsWith("}")) { //store last entry if exists
                        if (closed) { //check if a second table starts
                            --level;
                            continue;
                        }
                        entryList.add(new ParsedASN1Entry((HashMap<String, List<String>>) entries.clone()));
                        closed = true;
                    } else if (line.startsWith("{")) { //empty data structures
                        entries.clear();
                        closed = false;
                    } else {

                        //compile pattern and match it
                        Pattern pattern = Pattern.compile("(\\w+)\\s+\"?([^\",]+)\"?,?");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            key = matcher.group(1);
                            value = matcher.group(2).trim();

                            //store entry if one was found
                            if (entries.containsKey(key)) { //add to list
                                entries.get(key).add(value);
                            } else { //add new entry
                                List<String> list = new ArrayList<String>();
                                list.add(value);
                                entries.put(key, list);
                            }
                        }
                    }

                }
            }
        }

        parsedTable.setData(entryList);

        return parsedTable;
    }
}
