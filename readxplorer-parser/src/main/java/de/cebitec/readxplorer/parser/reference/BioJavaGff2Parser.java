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
import de.cebitec.readxplorer.api.enums.Strand;
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
import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ParserException;


/**
 * A GFF 2 parser for parsing the sequence from a fasta file contained in the
 * ReferenceJob and the GFF2 annotations from the GFF2 file contained in the
 * ReferenceJob.
 *
 * @author marie-theres, @author Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class BioJavaGff2Parser implements ReferenceParserI {

    // File extension used by Filechooser to choose files to be parsed by this parser
    private static final String[] FILE_EXTENSION = new String[]{"gff", "GFF", "gff2", "GFF2", "gtf", "GTF"};
    // name of this parser for use in ComboBoxes
    private static final String PARSER_NAME = "GFF2/GTF file";
    private static final String FILE_DESCRIPTION = PARSER_NAME;
    private static final String UNKNOWN_LOCUS_TAG = "unknown locus tag";

    private final List<Observer> observers = new ArrayList<>();


    /**
     * Parses the sequence from a fasta file contained in the ReferenceJob and
     * the GFF2 annotations from the GFF2 file contained in the ReferenceJob.
     * <p>
     * @param referenceJob the reference job containing the files
     * @param filter the feature filter to use (removes undesired features)
     * <p>
     * @return the parsed reference object with all parsed features
     * <p>
     * @throws ParsingException
     */
    @Override
    public ParsedReference parseReference( final ReferenceJob referenceJob, final FeatureFilter filter ) throws ParsingException {

        final FastaReferenceParser fastaParser = new FastaReferenceParser();
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
            GFFParser gffParser = new GFFParser();
            gffParser.parse( reader, new GFFDocumentHandler() {

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
                    //TODO anything to do here? check for extra information?
                }


                @Override
                @SuppressWarnings("unchecked")
                public void recordLine( final GFFRecord gffr ) {

                    if( chromMap.containsKey( gffr.getSeqName() ) ) {

                        final String parsedType = gffr.getFeature();

                        //phase can be used for translation within incomplete annotated genes. a given phase shows where to start in such a case
//                        int phase = gffr.getPhase(); //0, 1, 2 or -1, if not used
//                        if (phase >= 0 && phase <= 2) {
//                            if (strand == SequenceUtils.STRAND_FWD) {
//                                start += phase;
//                            } else {// rev strand
//                                stop -= phase;
//                            }
//                        } // else ignore phase as it is not used

                        Map<?, ?> attributes = gffr.getGroupAttributes();
                        Iterator<?> attrIt = attributes.keySet().iterator();

                        String geneName = "";
                        String product = "";
                        String ecNumber = "";
                        String identifier = "";
                        String locusTag = UNKNOWN_LOCUS_TAG;
                        while( attrIt.hasNext() ) {
                            Object key = attrIt.next();
                            if( key instanceof String ) {
                                String keyString = ((String) key);
                                Object value = attributes.get( keyString );
                                if( value instanceof List && !((Collection) value).isEmpty() ) {
                                    Object attribute = ((List) value).get( 0 ); //currently only one item per tag is supported, except for parent
                                    if( attribute instanceof String ) {
                                        String attrString = (String) attribute;
                                        if( keyString.equalsIgnoreCase( "ID" ) ) {
                                            identifier = attrString;
                                            if( locusTag.equals( UNKNOWN_LOCUS_TAG ) ) {
                                                locusTag = attrString;
                                            }
                                        } else if( keyString.equalsIgnoreCase( "product" ) ) {
                                            product = attrString;
                                        } else if( attrString.length() < 20
                                                && (keyString.equalsIgnoreCase( "name" )
                                                || keyString.equalsIgnoreCase( "gene" )
                                                || keyString.equalsIgnoreCase( "gene_name" )
                                                || keyString.equalsIgnoreCase( "genename" )) ) {
                                            geneName = attrString;
                                        } else if( (keyString.equalsIgnoreCase( "name" )
                                                || keyString.equalsIgnoreCase( "gene_id" )
                                                || keyString.equalsIgnoreCase( "gene_name" )
                                                || keyString.equalsIgnoreCase( "gene" )) && product.isEmpty() ) {
                                            product = attrString;
                                        } else if( keyString.equalsIgnoreCase( "alias" )
                                                || keyString.equalsIgnoreCase( "locus" )
                                                || keyString.equalsIgnoreCase( "locus_tag" ) ) {
                                            locusTag = attrString;
                                        }
                                        if( keyString.equalsIgnoreCase( parsedType ) ) {
                                            geneName = attrString;
                                        }
                                    }

                                    //parents not available in GFF2, only grouping is possible via the attribute field

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

                        FeatureType type = FeatureType.getFeatureType( parsedType );
                        if( type == FeatureType.UNDEFINED ) {
                            notifyObservers( referenceJob.getFile().getName()
                                    + ": Using unknown feature type for " + parsedType );
                        }

                        int start = gffr.getStart();
                        int stop = gffr.getEnd();
                        Strand strand = gffr.getStrand().equals( StrandedFeature.POSITIVE ) ? Strand.Forward : Strand.Reverse;
                        ParsedFeature currentFeature = new ParsedFeature( type, start, stop, strand,
                                locusTag, product, ecNumber, geneName, null, new ArrayList<>(), identifier );
                        ParsedChromosome currentChrom = chromMap.get( gffr.getSeqName() );
                        currentChrom.addFeature( currentFeature );

                    }

                }


            } );

        } catch( IOException | BioException | ParserException ex ) {
            JOptionPane.showMessageDialog( new JPanel(), ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE );
            throw new ParsingException( ex );
        }

        return refGenome;
    }


    @Override
    public String getName() {
        return PARSER_NAME;
    }


    @Override
    public String getInputFileDescription() {
        return FILE_DESCRIPTION;
    }


    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSION;
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
