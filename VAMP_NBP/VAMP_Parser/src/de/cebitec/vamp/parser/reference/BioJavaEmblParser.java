package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.common.ParsedFeature;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Observer;
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
import org.biojavax.bio.seq.io.EMBLFormat;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;

/**
 *
 * @author ddoppmeier
 * @deprecated use BioJavaParser
 */
public class BioJavaEmblParser implements ReferenceParserI {

    // Fileextension used by Filechooser to choose files to be parsed by this parser
    private static String[] fileExtension = new String[]{"embl"};
    // name of this parser for use in ComboBoxes
    private static String parserName = "BioJava EMBL";
    private static String fileDescription = "EMBL file";
    private ArrayList<Observer> observers = new ArrayList<>();
    private String errorMsg;

    public BioJavaEmblParser() {
    }

    @Override
    public ParsedReference parseReference(ReferenceJob refGenJob, FeatureFilter filter) throws ParsingException {

        ParsedReference refGenome = new ParsedReference();
        refGenome.setFeatureFilter(filter);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", refGenJob.getFile());
        try {

            BufferedReader in = new BufferedReader(new FileReader(refGenJob.getFile()));
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            SymbolTokenization dna = DNATools.getDNA().getTokenization("token");
            RichSequenceFormat embl = new EMBLFormat();
            RichSequenceBuilderFactory factory = RichSequenceBuilderFactory.THRESHOLD;

            RichStreamReader seqIter = new RichStreamReader(in, embl, dna, factory, ns);

            // take only the first sequence from file, if exists
            while (seqIter.hasNext()) {
                RichSequence seq;
                try {
                    try {
                        seq = seqIter.nextRichSequence();
//                        this.sendErrorMsg("rich seq set");
                    } catch (Exception e) {
                        this.sendErrorMsg(e.getMessage());
                        break;
                    }


                    refGenome.setDescription(refGenJob.getDescription());
                    refGenome.setName(refGenJob.getName());
                    refGenome.setTimestamp(refGenJob.getTimestamp());
                    refGenome.setSequence(seq.seqString());

                    FeatureType type;
                    RichFeature f;
                    
                    // attributes of feature that should be stored
                    String parsedType;
                    String locusTag;
                    String product;
                    int start;
                    int stop;
                    int strand;
                    String ecNumber;
                    String geneName;
                    List<ParsedFeature> subFeatures;

                    // iterate through all features
                    Iterator<Feature> featIt = seq.getFeatureSet().iterator();
                    while (featIt.hasNext()) {
                        f = (RichFeature) featIt.next();

                        // attributes of feature that should be stored
                        locusTag = "unknown locus tag";
                        product = null;
                        ecNumber = null;
                        geneName = null;
                        strand = 0;
                        subFeatures = new ArrayList<>();

                        parsedType = f.getType();
                        start = f.getLocation().getMin();
                        stop = f.getLocation().getMax();

                        @SuppressWarnings("unchecked")
                        Iterator<Note> noteIter = f.getRichAnnotation().getNoteSet().iterator();
                        while (noteIter.hasNext()) {
                            Note n = noteIter.next();
                            String name = n.getTerm().getName();
                            String value = n.getValue();

                            if (name.equals("locus_tag")) {
                                locusTag = value;
                            } else if (name.equalsIgnoreCase("locus")) {
                                locusTag = value;
                            } else if (name.equalsIgnoreCase("name")) {
                                locusTag = value;
                            } else if (name.equals("product")) {
                                product = value;
                            } else if (name.equals("EC_number")) {
                                ecNumber = value;
                            } else if (name.equals("gene")) {
                                geneName = value;
                            }
                        }

                        String strandString = RichLocation.Tools.enrich(f.getLocation()).getStrand().toString();
                        if (strandString.equals("-")) {
                            strand = -1;
                        } else if (strandString.equals("+")) {
                            strand = 1;
                        } else {
                            this.sendErrorMsg(refGenJob.getFile().getAbsolutePath() + ": "
                                    + "Unknown strand found: " + strandString);
                        }

                        @SuppressWarnings("unchecked")
                        Iterator<RichFeature> subFeatureIter = f.features();
                        while (subFeatureIter.hasNext()) {

                            RichFeature subFeature = subFeatureIter.next();
                            type = FeatureType.getFeatureType(subFeature.getType());

                            int subStart = subFeature.getLocation().getMin();
                            int subStop = subFeature.getLocation().getMax();
                            subFeatures.add(new ParsedFeature(type, subStart, subStop, strand,
                                    locusTag, product, ecNumber, geneName, new ArrayList<ParsedFeature>(), null));
                        }
                    
                        /* if the type of the feature is unknown to vamp (see below),
                         * an undefined type is used
                         */
                        type = FeatureType.getFeatureType(parsedType);

                        refGenome.addFeature(new ParsedFeature(type, start, stop, strand, locusTag, product, ecNumber, geneName, subFeatures, null));
                    }
                    
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "File successfully read");
                } catch (Exception ex) {
                    this.sendErrorMsg(ex.getMessage());
                }

            }

        } catch (Exception ex) {
            this.sendErrorMsg(ex.getMessage());
        }
        return refGenome;
    }

    @Override
    public String getName() {
        return parserName;
    }

    @Override
    public String getInputFileDescription() {
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
