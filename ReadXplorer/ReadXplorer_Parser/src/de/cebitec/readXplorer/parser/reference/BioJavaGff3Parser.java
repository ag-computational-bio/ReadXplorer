/* 
 * Copyright (C) 2014 Rolf Hilker
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

import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedChromosome;
import de.cebitec.readXplorer.parser.common.ParsedFeature;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff3.GFF3DocumentHandler;
import org.biojava.bio.program.gff3.GFF3Parser;
import org.biojava.bio.program.gff3.GFF3Record;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.utils.ParserException;

/**
 * A GFF 3 parser for parsing the sequence from a fasta file contained in the
 * ReferenceJob and the GFF3 annotations from the GFF3 file contained in the
 * ReferenceJob.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class BioJavaGff3Parser implements ReferenceParserI {
    
    // File extension used by Filechooser to choose files to be parsed by this parser
    private static final String[] fileExtension = new String[]{"gff", "gff3"};
    // name of this parser for use in ComboBoxes
    private static final String parserName = "GFF3 file";
    private static final String fileDescription = "GFF3 file";
    private ArrayList<Observer> observers = new ArrayList<>();

    /**
     * Parses the sequence from a fasta file contained in the ReferenceJob and
     * the GFF3 annotations from the GFF3 file contained in the ReferenceJob.
     * @param referenceJob the reference job containing the files
     * @param filter the feature filter to use (removes undesired features)
     * @return the parsed reference object with all parsed features
     * @throws ParsingException 
     */
    @Override
    public ParsedReference parseReference(final ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException {
        
        FastaReferenceParser fastaParser = new FastaReferenceParser();
        for (Observer observer : this.observers) {
            fastaParser.registerObserver(observer);
        }
        final ParsedReference refGenome = fastaParser.parseReference(referenceJob, filter);
        for (Observer observer : this.observers) {
            fastaParser.removeObserver(observer);
        }
        
        refGenome.setFeatureFilter(filter);
        final Map<String, ParsedChromosome> chromMap = CommonsRefParser.generateStringMap(refGenome.getChromosomes());

        try (BufferedReader reader = new BufferedReader(new FileReader(referenceJob.getGffFile()))) {
            GFF3Parser gff3Parser = new GFF3Parser();
            
            gff3Parser.parse(reader, new GFF3DocumentHandler() {

                @Override
                public void startDocument(String string) {
//                    registerObserver(refGenome);
                }

                @Override
                public void endDocument() {
//                    notifyObservers(ParsedReference.FINISHED);
//                    removeObserver(refGenome);
                }

                @Override
                public void commentLine(String string) {
                    //TODO: anything to do here? check for extra information?
                }

                @Override
                @SuppressWarnings("unchecked")
                public void recordLine(GFF3Record gffr) {
                    
                    String parsedType;
                    String locusTag = "unknown locus tag";
                    String geneName = "";
                    String product = "";
                    String ecNumber = "";
                    String identifier = "";
                    int start;
                    int stop;
                    int strand;
                    List<String> parentIds = new ArrayList<>();
                    ParsedChromosome currentChrom;
                    
                    if (chromMap.containsKey(gffr.getSequenceID())) {
                        currentChrom = chromMap.get(gffr.getSequenceID());

                        parsedType = gffr.getType().getName();
                        start = gffr.getStart();
                        stop = gffr.getEnd();
                        strand = gffr.getStrand().equals(StrandedFeature.POSITIVE) ? SequenceUtils.STRAND_FWD : SequenceUtils.STRAND_REV;
                        
                        //phase can be used for translation within incomplete annotated genes. a given phase shows where to start in such a case
//                        int phase = gffr.getPhase(); //0, 1, 2 or -1, if not used
//                        if (phase >= 0 && phase <= 2) {
//                            if (strand == SequenceUtils.STRAND_FWD) {
//                                start += phase;
//                            } else {// rev strand
//                                stop -= phase;
//                            }
//                        } // else ignore phase as it is not used
                        
                        Map attributes = gffr.getAnnotation().asMap();
                        Iterator attrIt = attributes.keySet().iterator();
                        Object key;
                        String keyString;
                        Object value;
                        Object attribute;
                        String attrString;
                        
                        while (attrIt.hasNext()) {
                            key = attrIt.next();
                            if (key instanceof Term) {
                                keyString = ((Term) key).getName();
                                value = attributes.get((Term) key);
                                if (value instanceof List && !((List) value).isEmpty()) {
                                    attribute = ((List) value).get(0); //currently only one item per tag is supported, except for parent
                                    if (attribute instanceof String) {
                                        attrString = (String) attribute;
                                        switch (keyString) {
                                            case "ID":   locusTag = attrString; 
                                                         identifier = attrString; break;
                                            case "Name": geneName = attrString; break;
//                                    case "Target": break; //other available fields according to gff3 definition
//                                    case "Gap": break;
//                                    case "Derives_from": break;
//                                    case "Note": break;
//                                    case "Dbxref":  break;
//                                    case "Ontology_term": break;
//                                    case "Is_circular": break;
                                            default: ;
                                        }
                                    }
                                    //process tags with multiple entries in a block
                                    switch (keyString) {
                                        case "Parent": parentIds = (List<String>) value; break;
                                    }
                                    
//                                    //process tags with multiple entries here
//                                    for (Object attr : (List) value) {
//                                        if (attr instanceof String) {
//                                            attrString = (String) attr;
//                                            switch (keyString) {
//                                                //separate multiple attributes here
//                                            }
//                                        }
//                                    }
                                }
                            }
                        }
                        
                        final String geneNameString = "geneName";
                        if (attributes.containsKey(geneNameString)) {
                            geneName = (String) attributes.get(geneNameString);
                        }
                        final String aliasString = "Alias";
                        if (attributes.containsKey(aliasString)) {
                            locusTag = (String) attributes.get(aliasString);
                        }
                        
                        FeatureType type = FeatureType.getFeatureType(parsedType);
                        if (type == FeatureType.UNDEFINED) {
                            notifyObservers(referenceJob.getFile().getName()
                                    + ": Using unknown feature type for " + parsedType);
                        }
                        
                        ParsedFeature currentFeature = new ParsedFeature(type, start, stop, strand, 
                                locusTag, product, ecNumber, geneName, null, parentIds, identifier);
                        currentChrom.addFeature(currentFeature);
                    }
                }
            }, new Ontology.Impl("Ontologyname", "name of ontology"));
            
        } catch (IOException | BioException | ParserException ex) {
            JOptionPane.showMessageDialog(new JPanel(), ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
            throw new ParsingException(ex);
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
            observer.update(data);
        }
    }
}