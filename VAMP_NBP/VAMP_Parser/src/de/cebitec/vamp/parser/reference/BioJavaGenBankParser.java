package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.common.ParsedAnnotation;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.AnnotationFilter;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedSubAnnotation;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * @deprecated use BioJavaParser
 */
public class BioJavaGenBankParser implements ReferenceParserI, Observable {

    private static String[] fileExtension = new String[]{"gbk", "gb", "genbank"};
    private static String parserName = "BioJava GenBank";
    private static String fileDescription = "GenBank file";
    
    private ArrayList<Observer> observers = new ArrayList<Observer>();
    private String errorMsg;

    @Override
    public ParsedReference parseReference(ReferenceJob refGenJob, AnnotationFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        refGenome.setAnnotationFilter(filter);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", refGenJob.getFile());
        try {

            BufferedReader in = new BufferedReader(new FileReader(refGenJob.getFile()));
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            SymbolTokenization dna = DNATools.getDNA().getTokenization("token");
            RichSequenceFormat genbank = new GenbankFormat();
            RichSequenceBuilderFactory factory = RichSequenceBuilderFactory.THRESHOLD;

            RichStreamReader seqIter = new RichStreamReader(in, genbank, dna, factory, ns);

            // take only the first sequence from file, if exists
            if (seqIter.hasNext()){

                RichSequence seq = seqIter.nextRichSequence();

                refGenome.setDescription(refGenJob.getDescription());
                refGenome.setName(refGenJob.getName());
                refGenome.setTimestamp(refGenJob.getTimestamp());
                refGenome.setSequence(seq.seqString());

                // iterate through all annotations
                Iterator<Feature> featIt = seq.getFeatureSet().iterator();
                while (featIt.hasNext()){
                    RichFeature f = (RichFeature) featIt.next();

                    // attributes of annotation that should be stored
                    String parsedType = null;
                    String locusTag = "unknown locus tag";
                    String product = null;
                    int start = 0;
                    int stop = 0;
                    int strand = 0;
                    String ecNumber = null;
                    String geneName = null;
                    List<ParsedSubAnnotation> exons = new ArrayList<ParsedSubAnnotation>();

                    parsedType = f.getType();
                    start = f.getLocation().getMin();
                    stop = f.getLocation().getMax();
                    strand = this.determineStrand(f, refGenJob);
                    
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
                    
                    //check annotation for subannotations (currently only exons)
                    @SuppressWarnings("unchecked")
                    Iterator<RichFeature> subAnnotationIter = f.features();
                    while (subAnnotationIter.hasNext()){
                        
                        RichFeature subAnnotation = subAnnotationIter.next();
                        String type = subAnnotation.getType();
                        
                        if (type.equalsIgnoreCase("exon")){
                            int subStart = subAnnotation.getLocation().getMin();
                            int subStop = subAnnotation.getLocation().getMax();
                            exons.add(new ParsedSubAnnotation(subStart, subStop, FeatureType.EXON));
                        }
                    }
                    

                    /* if the type of the annotation is unknown to vamp (see below),
                     * an undefined type is used
                     */
                    FeatureType type = FeatureType.UNDEFINED;

                    // look for known types
                    if (parsedType.equalsIgnoreCase("CDS")){
                        type = FeatureType.CDS;
                    } else if (parsedType.equalsIgnoreCase("repeat_unit")){
                        type = FeatureType.REPEAT_UNIT;
                    } else if (parsedType.equalsIgnoreCase("rRNA")){
                        type = FeatureType.RRNA;
                    } else if (parsedType.equalsIgnoreCase("source")){
                        type = FeatureType.SOURCE;
                    } else if (parsedType.equalsIgnoreCase("tRNA")){
                        type = FeatureType.TRNA;
                    } else if (parsedType.equalsIgnoreCase("misc_RNA")){
                        type = FeatureType.MISC_RNA;
                    } else if (parsedType.equalsIgnoreCase("miRNA")){
                        type = FeatureType.MIRNA;
                    } else if (parsedType.equalsIgnoreCase("gene")){
                        type = FeatureType.GENE;
                    } else if (parsedType.equalsIgnoreCase("mRNA")){
                        type = FeatureType.MRNA;
                    } else {
                        this.sendErrorMsg(refGenJob.getFile().getAbsolutePath() 
                                + ": Using unknown annotation type for " + parsedType);
                    }

                    //TODO: filter unknown annotations, if a known annotation exists with same locus! best to do not here
                    refGenome.addAnnotation(new ParsedAnnotation(type, start, stop, strand, locusTag, product, ecNumber, geneName, exons));

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
    
    /**
     * Determines the strand of an annotation
     * @param annotation the annotation whose strand is needed
     * @param refGenJob the reference genome job this annotation belongs to
     * @return SequenceUtils.STRAND_REV (-1), SequenceUtils.STRAND_FWD (1) or 0, if the strand cannot be determined
     */
    private int determineStrand(RichFeature annotation, ReferenceJob refGenJob) {
        String strandString = RichLocation.Tools.enrich(annotation.getLocation()).getStrand().toString();
        int strand = 0;
        if (strandString.equals("-")) {
            strand = SequenceUtils.STRAND_REV;
        } else if (strandString.equals("+")) {
            strand = SequenceUtils.STRAND_FWD;
        } else {
            this.sendErrorMsg(refGenJob.getFile().getAbsolutePath() + ": Unknown strand found: " + strandString);
        }
        return strand;
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
