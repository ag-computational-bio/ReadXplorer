package de.cebitec.readXplorer.parser.reference;

import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.Observer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The FastaReferenceParser can parse the reference genome from a fasta file.
 * Attention: there will be no features in this file just the sequence
 *
 * @author jstraube, rhilker
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static String parsername = "Fasta file";
    private static String[] fileExtension = new String[]{"fas", "fasta", "fna", "fa"};
    private static String fileDescription = "Fasta File";
    private ArrayList<Observer> observers = new ArrayList<>();
    private String errorMsg;

    /*
     * parses the containing sequences to one long sequence
     * @return returns the object parsedReference with the name, describtion
     * and the sequence from the reference genome
     */
    @Override
    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        StringBuilder sBuilder = new StringBuilder();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", referenceJob.getFile());
        try {

            BufferedReader in = new BufferedReader(new FileReader(referenceJob.getFile()));
            refGenome.setDescription(referenceJob.getDescription());
            refGenome.setName(referenceJob.getName());
            refGenome.setTimestamp(referenceJob.getTimestamp());
            String line;
            boolean parseData = false;
            boolean finished = false;

            while ((line = in.readLine()) != null) {
                if (line.startsWith(">" + refGenome.getName())) {
                    parseData = true;
                    finished = false;
                } else if (line.startsWith(">")) {
                    finished = true;
                } else if (parseData && !finished) {
                    sBuilder.append(line);
                }
            }
            in.close();

            refGenome.setSequence(sBuilder.substring(0).toUpperCase());

        } catch (Exception ex) {
            this.sendErrorMsg(ex.getMessage());
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished reading file  \"{0}" + "\"" + "genome length:" + "{1}", new Object[]{referenceJob.getFile(), sBuilder.length()});
        return refGenome;

    }

    /*
     * get the name of the used parser
     */
    @Override
    public String getName() {
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
        this.notifyObservers(null);
    }
}
