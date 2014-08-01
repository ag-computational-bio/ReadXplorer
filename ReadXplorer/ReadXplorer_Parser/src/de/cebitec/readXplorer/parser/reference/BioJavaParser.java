/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.parser.reference;

import de.cebitec.common.parser.Converter;
import de.cebitec.common.parser.embl.parsers.EmblSequenceToFastaConverterParser;
import de.cebitec.common.parser.genbank.parsers.GenbankSequenceToFastaConverterParser;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedChromosome;
import de.cebitec.readXplorer.parser.common.ParsedFeature;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.FastaUtils;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Location;
import org.biojavax.Namespace;
import org.biojavax.Note;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.EMBLFormat;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;

/**
 * A biojava parser can be initialized to parse embl or genbank files and parses
 * them into a ParsedReference object.
 *
 * @author ddopmeier, rhilker
 */
public class BioJavaParser implements ReferenceParserI, MessageSenderI {

    /** Use this for initializing an embl parser. */
    public static final int EMBL = 1;
    /** Use this for initializing a genbank parser. */
    public static final int GENBANK = 2;
    // File extension used by Filechooser to choose files to be parsed by this parser
    private static final String[] fileExtensionEmbl = new String[]{"embl", "EMBL"};
    private static final String[] fileExtensionGbk = new String[]{"gbk", "gb", "genbank", "GBK", "GB", "GENBANK"};
    // name of this parser for use in ComboBoxes
    private static final String parserNameEmbl = "EMBL file";
    private static final String parserNameGbk = "GenBank file";
    private static final String fileDescriptionEmbl = "EMBL file";
    private static final String fileDescriptionGbk = "GenBank file";
    private String[] fileExtension;
    private String parserName;
    private String fileDescription;
    private final RichSequenceFormat seqFormat;
    private ArrayList<Observer> observers = new ArrayList<>();
    private ErrorLimit errorLimit;

    /**
     * A biojava parser can be initialized to parse embl or genbank files and
     * parses them into a ParsedReference object.
     *
     * @param type the type of the parser, either BioJavaParser.EMBL or
     * BioJavaParser.GENBANK
     */
    public BioJavaParser(int type) {

        if (type == BioJavaParser.EMBL) {
            this.fileExtension = fileExtensionEmbl;
            this.parserName = parserNameEmbl;
            this.fileDescription = fileDescriptionEmbl;
            this.seqFormat = new EMBLFormat();
            
        } else { //for your info: if (type == BioJavaParser.GENBANK){
            this.fileExtension = fileExtensionGbk;
            this.parserName = parserNameGbk;
            this.fileDescription = fileDescriptionGbk;
            this.seqFormat = new GenbankFormat();
        }
        
        this.errorLimit = new ErrorLimit(100);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParsedReference parseReference(ReferenceJob refGenJob, FeatureFilter filter) throws ParsingException {

        ParsedReference refGenome = new ParsedReference();
        refGenome.setFeatureFilter(filter);
        //at first store all exons in one data structure and add them to the ref genome at the end
        Map<FeatureType, List<ParsedFeature>> featMap = new HashMap<>();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"{0}\"", refGenJob.getFile());
        try (BufferedReader in = new BufferedReader(new FileReader(refGenJob.getFile()))) {

            Namespace ns = RichObjectFactory.getDefaultNamespace();
            SymbolTokenization dna = DNATools.getDNA().getTokenization("token");
            RichSequenceBuilderFactory factory = RichSequenceBuilderFactory.THRESHOLD;

            refGenome.setDescription(refGenJob.getDescription());
            refGenome.setName(refGenJob.getName());
            refGenome.setTimestamp(refGenJob.getTimestamp());

            RichStreamReader seqIter = new RichStreamReader(in, seqFormat, dna, factory, ns);
            RichSequence seq;
            Iterator<Feature> featIt;
            RichFeature feature;
            String parsedType;
            String locusTag;
            String product;
            int start;
            int stop;
            int strand;
            String ecNumber;
            String geneName;
            List<ParsedFeature> subFeatures;
            Location location;
            Iterator<Note> noteIter;
            Note note;
            String name;
            String value;
            Iterator<Location> subFeatureIter;
            Location subLocation;
            int subStart;
            int subStop;
            ParsedFeature currentFeature;
            ParsedChromosome chrom;
            
            //Convert Genbank and EMBL to indexed fasta
            this.notifyObservers("Converting " + refGenJob.getFile() + " into indexed fasta...");
            Path fastaPath = new File(refGenJob.getFile().getAbsolutePath() + ".fasta").toPath();
            Converter seqConverter;
            if (seqFormat instanceof EMBLFormat) {
                seqConverter = EmblSequenceToFastaConverterParser.fileConverter(refGenJob.getFile().toPath(), fastaPath);
            } else {
                seqConverter = GenbankSequenceToFastaConverterParser.fileConverter(refGenJob.getFile().toPath(), fastaPath);
            }
            seqConverter.convert();
            seqConverter.close();
            this.notifyObservers("Creating fasta index " + refGenJob.getFile() + ".fai...");
            FastaUtils fastaUtils = new FastaUtils();
            File file = fastaPath.toFile();
            fastaUtils.indexFasta(file, this.observers);
            refGenome.setFastaFile(file);
            this.notifyObservers("Finished creating fasta index.");
            this.notifyObservers("Finished conversion into indexed fasta.");

            //Store features in DB
            while (seqIter.hasNext()) {
                try {
                    chrom = new ParsedChromosome();
                    seq = seqIter.nextRichSequence();
                    chrom.setName(seq.getName());
                    chrom.setChromLength(seq.length());
                    
                    // iterate through all features
                    featIt = seq.getFeatureSet().iterator();
                    while (featIt.hasNext()) {
                        locusTag = "unknown locus tag";
                        geneName = "";
                        ecNumber = "";
                        product = "";

                        feature = (RichFeature) featIt.next();

                        // attributes of feature that should be stored
                        subFeatures = new ArrayList<>();
                        location = feature.getLocation();

                        parsedType = feature.getType();
                        try {
                            strand = feature.getStrand().equals(StrandedFeature.POSITIVE) ? SequenceUtils.STRAND_FWD : SequenceUtils.STRAND_REV;
                        } catch (IllegalStateException e) {
                            this.sendMsgIfAllowed(e.getMessage());
                            continue;
                        }

                        //Determine feature tags
                        noteIter = feature.getRichAnnotation().getNoteSet().iterator();
                        while (noteIter.hasNext()) {
                            note = noteIter.next();
                            name = note.getTerm().getName();
                            value = note.getValue();

                            if (name.equals("locus_tag")) {
                                locusTag = value;
                            } else if (name.equalsIgnoreCase("locus")) {
                                locusTag = value;
                            } else if (name.equalsIgnoreCase("name") && locusTag.equals("unknown locus tag")) {
                                locusTag = value;
                            } else if (name.equalsIgnoreCase("product")) {
                                product = value;
                            } else if (name.equalsIgnoreCase("EC_number")) {
                                ecNumber = value;
                            } else if (name.equalsIgnoreCase("gene")) {
                                if (value.length() > 20) {
                                    geneName = value.substring(0, 20);
                                    this.sendMsgIfAllowed("Gene name too long, only keeping first 20 characters: " + geneName);
                                } else {
                                    geneName = value;
                                }
                            }
                        }

                        /* 
                         * If the type of the feature is unknown to readXplorer (see below),
                         * an undefined type is used.
                         */
                        FeatureType type = FeatureType.getFeatureType(parsedType);
                        if (type == FeatureType.UNDEFINED) {
                            this.sendMsgIfAllowed(refGenJob.getFile().getName()
                                    + ": Using unknown feature type for " + parsedType);
                        }


                        /*
                         * for eukaryotic organism its important to see the single cds/exons
                         * to exclude introns
                         * if we choose min and max we get the first pos of the first cds/exon
                         * of one gene and the last position of the last cds/exon and we can't
                         * see exon intron structure
                         */
                        //check feature for subfeatures
                        //it seems that features() is never used by biojava parsers
//                        Iterator<RichFeature> subFeatureIt = feature.features();
//                        while (subFeatureIt.hasNext()) {
//
//                            RichFeature subFeature = subFeatureIt.next();
//                            type = FeatureType.getFeatureType(subFeature.getType());
//
//                            subStart = subFeature.getLocation().getMin();
//                            subStop = subFeature.getLocation().getMax();
//                            subFeatures.add(new ParsedFeature(type, subStart, subStop, strand,
//                                    locusTag, product, ecNumber, geneName, new ArrayList<ParsedFeature>(), null));
//                        }
                        start = location.getMin();
                        stop = location.getMax();
                        boolean featAcrossBorder = false;
                        int index = 0;
                        if (location.toString().contains("join")) {
                            subFeatureIter = location.blockIterator();
                            while (subFeatureIter.hasNext()) {

                                subLocation = subFeatureIter.next(); //TODO: check if handling here is correct
                                //array always contains at least 2 entries
                                subStart = subLocation.getMin();
                                subStop = subLocation.getMax();
                                subFeatures.add(new ParsedFeature(type, subStart, subStop, strand,
                                        locusTag, product, ecNumber, geneName, new ArrayList<ParsedFeature>(), null));
                                featAcrossBorder = subStart == 1 && index > 0; //feature across circular chrom start, separate in two features
                                ++index;
                            }
                        }

                        //TODO: filter unknown features, if a known feature exists with same locus! best to do not here
                        if (featAcrossBorder) { //feature across circular chrom start, add each subfeature separately
                            for (ParsedFeature subFeature : subFeatures) {
                                if (!featMap.containsKey(type)) {
                                    featMap.put(type, new ArrayList<ParsedFeature>());
                                }
                                featMap.get(type).add(subFeature);
                            }
                        } else {
                            currentFeature = new ParsedFeature(type, start, stop, strand, locusTag, product, ecNumber, geneName, subFeatures, null);
                            if (!featMap.containsKey(type)) {
                                featMap.put(type, new ArrayList<ParsedFeature>());
                            }
                            featMap.get(type).add(currentFeature);
                        }
                    }
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Sequence successfully read");

                    chrom.addAllFeatures(this.createFeatureHierarchy(featMap));
                    refGenome.addChromosome(chrom);

                } catch (BioException | NoSuchElementException e) {
                    JOptionPane.showMessageDialog(new JPanel(), "One of the imported chromosomes does not contain any sequence data or is in corrupted format!",
                            "Chromosome Parsing Error", JOptionPane.ERROR_MESSAGE);
                    throw new ParsingException(e);
                }
            }
            
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
        
        if (errorLimit.getSkippedCount() > 0) {
            this.notifyObservers("... " + errorLimit.getSkippedCount() + " more errors occurred");
        }

        return refGenome;
    }

    /**
     * Creates the hierarchy of the given features.
     * @param featureMap the map of features, whose hierarchy is to be known
     * @return The list of top level features containing all subfeatures
     */
    public List<ParsedFeature> createFeatureHierarchy(Map<FeatureType, List<ParsedFeature>> featureMap) {
        List<ParsedFeature> featList = new ArrayList<>();
        List<ParsedFeature> rnaList = new ArrayList<>();
        List<ParsedFeature> cdsList = new ArrayList<>();
        List<ParsedFeature> exonList;

        if (featureMap.containsKey(FeatureType.GENE)) {
            featList = featureMap.get(FeatureType.GENE);
            featureMap.remove(FeatureType.GENE);
        }
        //merge rna lists (which are on same hierarchy level)
        if (featureMap.containsKey(FeatureType.MRNA)) {
            rnaList.addAll(featureMap.get(FeatureType.MRNA));
            featureMap.remove(FeatureType.MRNA);
        }
        if (featureMap.containsKey(FeatureType.RRNA)) {
            rnaList.addAll(featureMap.get(FeatureType.RRNA));
            featureMap.remove(FeatureType.RRNA);
        }
        if (featureMap.containsKey(FeatureType.TRNA)) {
            rnaList.addAll(featureMap.get(FeatureType.TRNA));
            featureMap.remove(FeatureType.TRNA);
        }
        if (featureMap.containsKey(FeatureType.NC_RNA)) {
            rnaList.addAll(featureMap.get(FeatureType.NC_RNA));
            featureMap.remove(FeatureType.NC_RNA);
        }

        if (featureMap.containsKey(FeatureType.CDS)) {
            cdsList = featureMap.get(FeatureType.CDS);
            featureMap.remove(FeatureType.CDS);
        }

        //add all cds found within an exon to their exons
        if (featureMap.containsKey(FeatureType.EXON)) {
            exonList = featureMap.get(FeatureType.EXON);
            featureMap.remove(FeatureType.EXON);
            exonList = this.addSubfeatures(cdsList, exonList);
        } else {
            exonList = cdsList;
        }

        rnaList = this.addSubfeatures(exonList, rnaList);
        featList = this.addSubfeatures(rnaList, featList);

        Iterator<FeatureType> typeIt = featureMap.keySet().iterator();
        while (typeIt.hasNext()) {
            featList.addAll(featureMap.get(typeIt.next()));
        }

        return featList;
    }

    /**
     * Add a list of subfeatures to their corresponding parent features. If a
     * feature has no parent, it is added to the return list of features.
     * @param subFeatures The subfeatures to add to their parents
     * @param features The feature list containing the parents
     * @return The feature list with the parents, now knowing their children and
     * all features without a parent
     */
    private List<ParsedFeature> addSubfeatures(List<ParsedFeature> subFeatures, List<ParsedFeature> features) {
        List<ParsedFeature> mergedList = new ArrayList<>();
        int lastIndex = 0;
        boolean added = false;
        ParsedFeature feature;
        Collections.sort(features);
        Collections.sort(subFeatures);
        for (ParsedFeature subFeature : subFeatures) {
            //since the features are sorted in this.features we can do this in linear time
            for (int i = lastIndex; i < features.size(); ++i) {
                feature = features.get(i);
                if (feature.getStrand() == subFeature.getStrand() && feature.getStart() <= subFeature.getStart()
                        && feature.getStop() >= subFeature.getStop()) {
                    feature.addSubFeature(subFeature);
                    added = true;
                    lastIndex = i == 0 ? 0 : --i;
                    break;
                } else if (feature.getStart() > subFeature.getStop()) {
                    break;
                }
            }
            if (!added) { //if there is no parent feature for the sub feature it becomes an ordinary feature
                mergedList.add(subFeature);
            }
        }
        mergedList.addAll(features);
        Collections.sort(mergedList);
        return mergedList;
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
            observer.update(data);
        }
    }    
    
    /**
     * Sends the given msg to all observers, if the error limit is not already
     * reached for this parser.
     * @param msg The message to send
     */
    @Override
    public void sendMsgIfAllowed(String msg) {
        if (this.errorLimit.allowOutput()) {
            this.notifyObservers(msg);
        }
    }
}
