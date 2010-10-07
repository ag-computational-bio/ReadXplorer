package vamp.parsing.reads;

import vamp.parsing.common.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.parsing.common.ParsingException;
import vamp.importer.RunJob;

/**
 *
 * @author ddoppmeier
 */
public class FastaParser implements RunParserI {

    private static String name = "Fasta Parser";
    private static String[] fileExtension = new String[]{"fas", "fasta","mfn"};
    private static String fileDescription = "Fasta File";

    public FastaParser(){

    }

    @Override
    public ParsedRun parseRun(RunJob runJob) throws ParsingException {
        File file = runJob.getFile();
        String description = runJob.getDescription();

        ParsedRun run = new ParsedRun(description);
        run.setTimestamp(runJob.getTimestamp());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lesen der Read Datei " + file);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            String readname = null;
            String sequence = null;

            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (line.length() >= 100) {
                        readname = line.substring(1, 100);
                    } else{
                        readname = line.substring(1);
                    }
                    
                } else {
                    sequence = line;
                    run.addReadData(sequence, readname);
                }
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lesen der Datei "+file+" beendet");

        } catch (Exception ex) {
            throw new ParsingException(ex);
        }

        return run;

    }
    
    @Override
    public String getParserName(){
        return name;
    }

    @Override
    public String getInputFileDescription(){
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

}

