package de.cebitec.vamp.parser.reference.fasta;

import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.reference.ReferenceParserI;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstraube
 */

/*
 * The FastaReferenceParser can parse the reference genome from a fasta file
 * attention there are no features in this file just the sequence
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static String parsername = "Fasta Reference Parser";
    private static String[] fileExtension = new String[]{"fas", "fasta"};
    private static String fileDescription = "Fasta File";

    /*
     * parses the containing sequences to one long sequence
     * but its much faster to delete all linebreaks first!
     * @return returns the object parsedReference with the name, describtion
        and the sequence from the reference genome
     */
    @Override
    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        String sequence = "";
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", referenceJob.getFile());
        try {

            BufferedReader in = new BufferedReader(new FileReader(referenceJob.getFile()));
            
            refGenome.setDescription(referenceJob.getDescription());
            refGenome.setName(referenceJob.getName());
            refGenome.setTimestamp(referenceJob.getTimestamp());
            String line = null;
           
            while ((line = in.readLine()) != null) {
                if (!line.startsWith(">")) {
                    sequence = sequence + line;
                }
            }

            refGenome.setSequence(sequence.toLowerCase());

        } catch (Exception ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished reading file  \"{0}" + "\"" + "genome length:" + "{1}", new Object[]{referenceJob.getFile(), sequence.length()});
        return refGenome;

    }

    /*
     * get the name of the used parser
     */
    @Override
    public String getParserName() {
        return parsername;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }
}
