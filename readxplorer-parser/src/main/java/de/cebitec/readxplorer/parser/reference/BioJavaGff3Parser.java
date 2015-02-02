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

package de.cebitec.readxplorer.parser.reference;


import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsedChromosome;
import de.cebitec.readxplorer.parser.common.ParsedFeature;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class BioJavaGff3Parser implements ReferenceParserI {

    // File extension used by Filechooser to choose files to be parsed by this parser
    private static final String[] fileExtension = new String[]{ "gff", "gff3" };
    // name of this parser for use in ComboBoxes
    private static final String parserName = "GFF3 file";
    private static final String fileDescription = "GFF3 file";
    private static final String UNKNOWN_LOCUS_TAG = "unknown locus tag";
    private final ArrayList<Observer> observers = new ArrayList<>();


    /**
     * Parses the sequence from a fasta file contained in the ReferenceJob and
     * the GFF3 annotations from the GFF3 file contained in the ReferenceJob.
     * <p>
     * @param referenceJob the reference job containing the files
     * @param filter       the feature filter to use (removes undesired
     *                     features)
     * <p>
     * @return the parsed reference object with all parsed features
     * <p>
     * @throws ParsingException
     */
    @Override
    public ParsedReference parseReference( final ReferenceJob referenceJob, final FeatureFilter filter ) throws ParsingException {

        FastaReferenceParser fastaParser = new FastaReferenceParser();
        for( Observer observer : this.observers ) {
            fastaParser.registerObserver( observer );
        }
        final ParsedReference refGenome = fastaParser.parseReference( referenceJob, filter );
        for( Observer observer : this.observers ) {
            fastaParser.removeObserver( observer );
        }

        refGenome.setFeatureFilter( filter );
        final Map<String, ParsedChromosome> chromMap = CommonsRefParser.generateStringMap( refGenome.getChromosomes() );

        try( BufferedReader reader = new BufferedReader( new FileReader( referenceJob.getGffFile() ) ) ) {
            GFF3Parser gff3Parser = new GFF3Parser();

            gff3Parser.parse( reader, new GFF3DocumentHandler() {

                @Override
                public void startDocument( String string ) {
//                    registerObserver(refGenome);
                }


                @Override
                public void endDocument() {
//                    notifyObservers(ParsedReference.FINISHED);
//                    removeObserver(refGenome);
                }


                @Override
                public void commentLine( String string ) {
                    //TODO: anything to do here? check for extra information?
                }


                @Override
                @SuppressWarnings( "unchecked" )
                public void recordLine( final GFF3Record gffr ) {


                    String locusTag = UNKNOWN_LOCUS_TAG;
                    String geneName = "";
                    String product = "";
                    String ecNumber = "";
                    String identifier = "";
                    List<String> parentIds = new ArrayList<>();

                    if( chromMap.containsKey( gffr.getSequenceID() ) ) {

                        final ParsedChromosome currentChrom = chromMap.get( gffr.getSequenceID() );
                        //phase can be used for translation within incomplete annotated genes. a given phase shows where to start in such a case
//                        int phase = gffr.getPhase(); //0, 1, 2 or -1, if not used
//                        if (phase >= 0 && phase <= 2) {
//                            if (strand == SequenceUtils.STRAND_FWD) {
//                                start += phase;
//                            } else {// rev strand
//                                stop -= phase;
//                            }
//                        } // else ignore phase as it is not used

                        final Map<?,?> attributes = gffr.getAnnotation().asMap();
                        final Iterator<?> attrIt = attributes.keySet().iterator();

                        while( attrIt.hasNext() ) {
                            final Object key = attrIt.next();
                            if( key instanceof Term ) {
                                final String keyString = ((Term) key).getName();
                                final Object value = attributes.get( key );
                                if( value instanceof List && !((Collection) value).isEmpty() ) {
                                    final Object attribute = ((List) value).get( 0 ); //currently only one item per tag is supported, except for parent
                                    if( attribute instanceof String ) { //TODO: think about some way to keep all provided data - write it to product field?
                                        final String attrString = (String) attribute;
                                        if( keyString.equalsIgnoreCase( "ID" ) ) {
                                            identifier = attrString;
                                            if( locusTag.equals( UNKNOWN_LOCUS_TAG ) ) {
                                                locusTag = attrString;
                                            }
                                        }
                                        else if( keyString.equalsIgnoreCase( "product" ) ) {
                                            product = attrString;
                                        }
                                        else if( attrString.length() < 20
                                                 && (keyString.equalsIgnoreCase( "name" )
                                                     || keyString.equalsIgnoreCase( "gene" )
                                                     || keyString.equalsIgnoreCase( "gene_name" )
                                                     || keyString.equalsIgnoreCase( "genename" )) ) {
                                            geneName = attrString;
                                        }
                                        else if( (keyString.equalsIgnoreCase( "name" )
                                                  || keyString.equalsIgnoreCase( "gene_id" )
                                                  || keyString.equalsIgnoreCase( "gene_name" )
                                                  || keyString.equalsIgnoreCase( "gene" )) && product.isEmpty() ) {
                                            product = attrString;
                                        }
                                        else if( keyString.equalsIgnoreCase( "alias" )
                                                 || keyString.equalsIgnoreCase( "locus" )
                                                 || keyString.equalsIgnoreCase( "locus_tag" ) ) {
                                            locusTag = attrString;
                                        }
//                                    case "Target": break; //other available fields according to gff3 definition
//                                    case "Gap": break;
//                                    case "Derives_from": break;
//                                    case "Note": break;
//                                    case "Dbxref":  break;
//                                    case "Ontology_term": break;
//                                    case "Is_circular": break;
//                                    case "Alias": break;
                                    }
                                    //process tags with multiple entries in a block
                                    switch( keyString ) {
                                        case "Parent":
                                            parentIds = (List<String>) value;
                                            break;
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

                        String parsedType = gffr.getType().getName();
                        FeatureType type = FeatureType.getFeatureType( parsedType );
                        if( type == FeatureType.UNDEFINED ) {
                            notifyObservers( referenceJob.getFile().getName()
                                             + ": Using unknown feature type for " + parsedType );
                        }

                        int start = gffr.getStart();
                        int stop = gffr.getEnd();
                        int strand = gffr.getStrand().equals( StrandedFeature.POSITIVE ) ? SequenceUtils.STRAND_FWD : SequenceUtils.STRAND_REV;
                        ParsedFeature currentFeature = new ParsedFeature( type, start, stop, strand,
                                                                          locusTag, product, ecNumber, geneName, null, parentIds, identifier );
                        currentChrom.addFeature( currentFeature );
                    }
                }


            }, new Ontology.Impl( "Ontologyname", "name of ontology" ) );

        }
        catch( IOException | BioException | ParserException ex ) {
            JOptionPane.showMessageDialog( new JPanel(), ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE );
            throw new ParsingException( ex );
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
    public void registerObserver( final Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( final Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


}
