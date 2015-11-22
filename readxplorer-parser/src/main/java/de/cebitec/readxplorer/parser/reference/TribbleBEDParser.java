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


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsedChromosome;
import de.cebitec.readxplorer.parser.common.ParsedFeature;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import de.cebitec.readxplorer.utils.Observer;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.bed.FullBEDFeature.Exon;
import htsjdk.tribble.readers.LineIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;


/**
 * Parses the sequence from a fasta file contained in the ReferenceJob and the
 * BED annotations from the BED file contained in the ReferenceJob.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TribbleBEDParser implements ReferenceParserI {

    private static final Logger LOG = Logger.getLogger( TribbleBEDParser.class.getName() );

    // File extension used by Filechooser to choose files to be parsed by this parser
    private static final String[] FILE_EXTENSION = new String[]{"bed", "BED"};
    // name of this parser for use in ComboBoxes
    private static final String PARSER_NAME = "BED file";
    private static final String FILE_DESCRIPTION = "BED file";
    private final List<Observer> observers = new ArrayList<>();


    /**
     * Parses the sequence from a fasta file contained in the ReferenceJob and
     * the BED annotations from the BED file contained in the ReferenceJob.
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

        FastaReferenceParser fastaParser = new FastaReferenceParser();
        ParsedReference refGenome = fastaParser.parseReference( referenceJob, filter );
        refGenome.setFeatureFilter( filter );
        final Map<String, ParsedChromosome> chromMap = CommonsRefParser.generateStringMap( refGenome.getChromosomes() );
        //at first store all eonxs in one data structure and add them to the ref genome at the end
//        Map<FeatureType, List<ParsedFeature>> featMap = new HashMap<>();

        LOG.log( INFO, "Start reading file  \"{0}\"", referenceJob.getFile() );
        try {

            final BEDCodec bedCodec = new BEDCodec( BEDCodec.StartOffset.ZERO );
            final AbstractFeatureReader<BEDFeature, LineIterator> reader = AbstractFeatureReader.getFeatureReader( referenceJob.getFile().getAbsolutePath(), bedCodec, false );
            if( bedCodec.canDecode( referenceJob.getFile().getAbsolutePath() ) ) {

                Object header = reader.getHeader(); //TODO something to do with the header?

                final Iterator<BEDFeature> featIt = reader.iterator();
                while( reader.hasIndex() ) {

                    final BEDFeature feat = featIt.next();
                    if( chromMap.containsKey( feat.getChr() ) ) {

                        final int start = feat.getStart();
                        final int stop = feat.getEnd();
                        final de.cebitec.readxplorer.api.enums.Strand strand = feat.getStrand().equals( Strand.POSITIVE ) ? de.cebitec.readxplorer.api.enums.Strand.Forward : de.cebitec.readxplorer.api.enums.Strand.Reverse;
                        final String geneName = feat.getName();
                        final String locusTag = feat.getDescription();
                        final String ecNumber = feat.getDescription(); //TODO check this and test it
                        final String product = feat.getDescription();
                        final String parsedType = feat.getType();

                        /*
                         * If the type of the feature is unknown to readxplorer (see below),
                         * an undefined type is used.
                         */
                        final FeatureType type = FeatureType.getFeatureType( parsedType );
                        if( type == FeatureType.UNDEFINED ) {
                            this.notifyObservers( referenceJob.getFile().getName()
                                    + ": Using unknown feature type for " + parsedType );
                        }

                        final List<ParsedFeature> subFeatures = new ArrayList<>();
                        for( Exon exon : feat.getExons() ) {
                            int subStart = exon.getCdStart();
                            int subStop = exon.getCdEnd();
//                            exon.getNumber();

                            subFeatures.add( new ParsedFeature( type, subStart, subStop, strand, locusTag, product, ecNumber, geneName, null, null ) );
                        }

//                        feat.getLink(); could be used in upcoming versions
//                        feat.getScore();
//                        feat.getColor();

                        ParsedFeature currentFeature = new ParsedFeature( type, start, stop, strand, locusTag, product, ecNumber, geneName, subFeatures, null );
                        ParsedChromosome currentChrom = chromMap.get( feat.getChr() );
                        currentChrom.addFeature( currentFeature );

                    }
                }
            }

        } catch( IOException | TribbleException ex ) {
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
    public void registerObserver( Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


}
