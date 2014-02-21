package de.cebitec.readXplorer.parser.reference;

import de.cebitec.common.parser.fasta.FastaIndexEntry;
import de.cebitec.common.parser.fasta.FastaIndexReader;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedChromosome;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.FastaUtils;
import de.cebitec.readXplorer.util.Observer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The FastaReferenceParser can parse a reference genome from a fasta file.
 * This means, the sequence dictonary is checked for multiple sequences,
 * corresponding chromosomes are created and the fasta file is indexed, if that
 * is not already the case. Later, the data has to be directly fetched from the
 * now indexed fasta file.
 * Attention: there will be no features in this file just the sequence.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static String parsername = "Fasta file";
    private static String[] fileExtension = new String[]{"fas", "fasta", "fna", "fa"};
    private static String fileDescription = "Fasta File";
    private ArrayList<Observer> observers = new ArrayList<>();
    private String errorMsg;

    /**
     * The FastaReferenceParser can parse a reference genome from a fasta file.
     * This means, the sequence dictonary is checked for multiple sequences,
     * corresponding chromosomes are created and the fasta file is indexed, if
     * that is not already the case. Later, the data has to be directly fetched
     * from the now indexed fasta file. Attention: there will be no features in
     * this file just the sequence.
     */
    public FastaReferenceParser() {
    }

    
    /**
     * Parses a reference genome from a fasta file.\n This means, the sequence
     * dictonary is checked for multiple sequences, corresponding chromosomes
     * are created and the fasta file is indexed, if that is not already the
     * case. Later, the data has to be directly fetched from the now indexed
     * fasta file. Attention: there will be no features in this file just the
     * sequence.
     * @param referenceJob the reference job, for which the data shall be parsed
     * @param filter the feature filter to use for this reference. Not needed
     * for fasta, since it does not have features.
     * @return returns the object parsedReference with the name, description
     * and chromosomes for the reference genome
     * @throws de.cebitec.readXplorer.parser.common.ParsingException
     */
    @Override
    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        int chromCounter = 0;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", referenceJob.getFile());
        try {

            refGenome.setDescription(referenceJob.getDescription());
            refGenome.setName(referenceJob.getName());
            refGenome.setTimestamp(referenceJob.getTimestamp());
            refGenome.setFastaFile(referenceJob.getFile());
            
            FastaUtils fastaUtils = new FastaUtils();
            fastaUtils.indexFasta(referenceJob.getFile(), this.observers);
            
            FastaIndexReader reader = new FastaIndexReader();
            File indexFile = new File(referenceJob.getFile().toString() + ".fai");
            List<FastaIndexEntry> entries = reader.read(indexFile.toPath());
            for (FastaIndexEntry entry : entries) {
                this.createChromosome(entry.getSequenceId(), entry.getSequenceLength(), refGenome);
            }


        } catch (IOException ex) {
            this.sendErrorMsg(ex.getMessage());
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished reading file  \"{0}" + "\"" + "genome with: {1} chromosomes", new Object[]{referenceJob.getFile(), chromCounter});
        return refGenome;

    }

    /**
     * Creates a chromosome for a given name and adds it to the given reference.
     * @param chromName name of the chromosome
     * @param reference reference genome to which the chromosome shall be added
     */
    private void createChromosome(String chromName, long chromLength, ParsedReference reference) {
        ParsedChromosome chrom = new ParsedChromosome(chromName, chromLength, false);
        reference.addChromosome(chrom);
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
