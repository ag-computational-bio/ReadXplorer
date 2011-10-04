package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.common.ParsedFeature;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.Note;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;

/**
 *
 * @author ddoppmei
 */
public class BioJavaGenBankParser implements ReferenceParserI, Observable {

    private static String[] fileExtension = new String[]{"gbk", "gb", "genbank"};
    private static String parserName = "BioJava GenBank";
    private static String fileDescription = "GenBank file";
    
    private ArrayList<Observer> observers = new ArrayList<Observer>();
    private String errorMsg;

    @Override
    public ParsedReference parseReference(ReferenceJob refGenJob, FeatureFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        refGenome.setFeatureFilter(filter);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", refGenJob.getFile());
        try {

            BufferedReader in = new BufferedReader(new FileReader(refGenJob.getFile()));
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            SymbolTokenization dna = DNATools.getDNA().getTokenization("token");
            RichSequenceFormat embl = new GenbankFormat();
            RichSequenceBuilderFactory factory = RichSequenceBuilderFactory.THRESHOLD;

            RichStreamReader it = new RichStreamReader(in,embl,dna,factory,ns);

            // take only the first sequence from file, if exists
            if (it.hasNext()){

                RichSequence s = it.nextRichSequence();

                refGenome.setDescription(refGenJob.getDescription());
                refGenome.setName(refGenJob.getName());
                refGenome.setTimestamp(refGenJob.getTimestamp());
                refGenome.setSequence(s.seqString());

                // iterate through all features
                Iterator<Feature> featIt = s.getFeatureSet().iterator();
                while (featIt.hasNext()){
                    RichFeature f = (RichFeature) featIt.next();

                    // attributes of feature that should be stored
                    String parsedType = null;
                    String locusTag = "unknown locus tag";
                    String product = null;
                    int start = 0;
                    int stop = 0;
                    int strand = 0;
                    String ecNumber = null;
                    String geneName = null;

                    parsedType = f.getType();
                    start = f.getLocation().getMin();
                    stop = f.getLocation().getMax();

                    String strandString = RichLocation.Tools.enrich(f.getLocation()).getStrand().toString();
                    if (strandString.equals("-")){
                        strand = -1;
                    } else if (strandString.equals("+")){
                        strand = 1;
                    } else {
                        this.sendErrorMsg(refGenJob.getFile().getAbsolutePath() + ": "
                                + "Unknown strand found: " + strandString);
                    }

                    @SuppressWarnings("unchecked")
                    Iterator<Note> iter = f.getRichAnnotation().getNoteSet().iterator();
                    while (iter.hasNext()){
                        Note n = iter.next();
                        String name = n.getTerm().getName();
                        String value = n.getValue();

                        if (name.equals("locus_tag")){
                            locusTag = value;
                        } else if (name.equalsIgnoreCase("locus")){
                          locusTag = value;
                        } else if (name.equals("product")){
                            product = value;
                        } else if (name.equals("EC_number")){
                            ecNumber = value;
                        } else if (name.equals("gene")){
                            geneName = value;
                        }

                    }

                    /* if the type of the feature is unknown to vamp (see below),
                     * an undefined type is used
                     */
                    Integer type = FeatureType.UNDEFINED;

                    // look for known types
                    if (parsedType.equalsIgnoreCase("CDS")){
                        type = FeatureType.CDS;
                    } else if (parsedType.equalsIgnoreCase("repeat_unit")){
                        type = FeatureType.REPEAT_UNIT;
                    } else if (parsedType.equalsIgnoreCase("rRNA")){
                        type = FeatureType.R_RNA;
                    } else if (parsedType.equalsIgnoreCase("source")){
                        type = FeatureType.SOURCE;
                    } else if (parsedType.equalsIgnoreCase("tRNA")){
                        type = FeatureType.T_RNA;
                    } else if (parsedType.equalsIgnoreCase("misc_RNA")){
                        type = FeatureType.MISC_RNA;
                    } else if (parsedType.equalsIgnoreCase("miRNA")){
                        type = FeatureType.MI_RNA;
                    } else if (parsedType.equalsIgnoreCase("gene")){
                        type = FeatureType.GENE;
                    } else if (parsedType.equalsIgnoreCase("mRNA")){
                        type = FeatureType.M_RNA;
                    } else {
                        this.sendErrorMsg(refGenJob.getFile().getAbsolutePath() + ": "
                                + "Found unknown feature " + parsedType);
                    }

                    refGenome.addFeature(new ParsedFeature(type, start, stop, strand, locusTag, product, ecNumber, geneName));

                }
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "File successfully read");
            } else {
                this.sendErrorMsg("No sequence found in file "+refGenJob.getFile().getAbsolutePath());
            }

        } catch (Exception ex) {
            this.sendErrorMsg(ex.getMessage());
        }

        return refGenome;
    }

    @Override
    public String getParserName(){
        return parserName;
    }

    @Override
    public String getInputFileDescription(){
        return fileDescription;
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
    public void notifyObservers() {
        for (Observer observer : this.observers){
            observer.update(this.errorMsg);
        }
    }

    /**
     * Method setting and sending the error msg to all observers.
     * @param errorMsg the error msg to send
     */
    private void sendErrorMsg(final String errorMsg){
        this.errorMsg = errorMsg;
        this.notifyObservers();
    }

}
