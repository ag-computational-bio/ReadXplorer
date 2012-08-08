package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.common.ParsedAnnotation;
import de.cebitec.vamp.parser.common.ParsedReference;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.reference.Filter.AnnotationFilter;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedSubAnnotation;
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
    private ArrayList<Observer> observers = new ArrayList<Observer>();
    private String errorMsg;

    public BioJavaEmblParser() {
    }

    @Override
    public ParsedReference parseReference(ReferenceJob refGenJob, AnnotationFilter filter) throws ParsingException {

        ParsedReference refGenome = new ParsedReference();
        refGenome.setAnnotationFilter(filter);

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
                RichSequence seq = null;
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

                    // iterate through all annotations
                    Iterator<Feature> featIt = seq.getFeatureSet().iterator();
                    while (featIt.hasNext()) {
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
                        Iterator subAnnotationIter = f.getLocation().blockIterator();

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

                        /* if the type of the annotation is unknown to vamp (see below),
                         * an undefined type is used
                         */
                        FeatureType type = FeatureType.UNDEFINED;

                        // look for known types
                        if (parsedType.equalsIgnoreCase("CDS")) {
                            type = FeatureType.CDS;
                        } else if (parsedType.equalsIgnoreCase("repeat_unit")) {
                            type = FeatureType.REPEAT_UNIT;
                        } else if (parsedType.equalsIgnoreCase("rRNA")) {
                            type = FeatureType.RRNA;
                        } else if (parsedType.equalsIgnoreCase("source")) {
                            type = FeatureType.SOURCE;
                        } else if (parsedType.equalsIgnoreCase("tRNA")) {
                            type = FeatureType.TRNA;
                        } else if (parsedType.equalsIgnoreCase("misc_RNA")) {
                            type = FeatureType.MISC_RNA;
                        } else if (parsedType.equalsIgnoreCase("miRNA")) {
                            type = FeatureType.MIRNA;
                        } else if (parsedType.equalsIgnoreCase("gene")) {
                            type = FeatureType.GENE;
                        } else if (parsedType.equalsIgnoreCase("mRNA")) {
                            type = FeatureType.MRNA;
                        } else {
                            this.sendErrorMsg(refGenJob.getFile().getAbsolutePath()
                                    + ": Using unknown annotation type for " + parsedType);
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

                        while (subAnnotationIter.hasNext()) {

                            String pos = subAnnotationIter.next().toString();
                            /*for eukaryotic organism its important to see the single cds
                            for looking for introns
                            if we choose min and max we get the first pos of the first cds
                            of one gen and the last position of the last cds and we cant
                            see exon intron structure*/
                            if (pos.contains("..")) {
                                String[] p = pos.split("\\..");
                                start = Integer.parseInt(p[0]);
                                stop = Integer.parseInt(p[1]);

                            }
                            
                            refGenome.addAnnotation(new ParsedAnnotation(type, start, stop, strand, locusTag, product, ecNumber, geneName, exons));

                        }
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
