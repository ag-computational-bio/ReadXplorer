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


import de.cebitec.common.parser.fasta.FastaIndexEntry;
import de.cebitec.common.parser.fasta.FastaIndexReader;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsedChromosome;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import de.cebitec.readxplorer.utils.FastaUtils;
import de.cebitec.readxplorer.utils.Observer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;


/**
 * The FastaReferenceParser can parse a reference genome from a fasta file. This
 * means, the sequence dictonary is checked for multiple sequences,
 * corresponding chromosomes are created and the fasta file is indexed, if that
 * is not already the case. Later, the data has to be directly fetched from the
 * now indexed fasta file. Attention: there will be no features in this file
 * just the sequence.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static final Logger LOG = Logger.getLogger( FastaReferenceParser.class.getName() );


    private static final String PARSER_NAME = "Fasta file";
    private static final String[] FILE_EXTENSION = new String[]{"fas", "fasta", "fna", "fa"};
    private static final String FILE_DESCRIPTION = "Fasta File";

    private final List<Observer> observers = new ArrayList<>();
//    private String errorMsg;


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
     * <p>
     * @param referenceJob the reference job, for which the data shall be parsed
     * @param filter the feature filter to use for this reference. Not needed
     * for fasta, since it does not have features.
     * <p>
     * @return returns the object parsedReference with the name, description and
     * chromosomes for the reference genome
     * <p>
     * @throws de.cebitec.readxplorer.parser.common.ParsingException
     */
    @Override
    public ParsedReference parseReference( final ReferenceJob referenceJob, final FeatureFilter filter ) throws ParsingException {

        final ParsedReference refGenome = new ParsedReference();
        int chromCounter = 0;
        LOG.log( INFO, "Start reading file  \"{0}\"", referenceJob.getFile() );
        try {

            refGenome.setDescription( referenceJob.getDescription() );
            refGenome.setName( referenceJob.getName() );
            refGenome.setTimestamp( referenceJob.getTimestamp() );
            refGenome.setFastaFile( referenceJob.getFile() );

            this.notifyObservers( "Creating fasta index " + referenceJob.getFile() + ".fai..." );
            FastaUtils fastaUtils = new FastaUtils();
            fastaUtils.indexFasta( referenceJob.getFile(), this.observers );
            this.notifyObservers( "Finished creating fasta index." );

            FastaIndexReader reader = new FastaIndexReader();
            File indexFile = new File( referenceJob.getFile().toString() + ".fai" );
            List<FastaIndexEntry> entries = reader.read( indexFile.toPath() );
            for( FastaIndexEntry entry : entries ) {
                this.createChromosome( entry.getSequenceId(), entry.getSequenceLength(), refGenome );
            }


        } catch( IOException ex ) {
            this.notifyObservers( ex.getMessage() );
        }

        LOG.log( INFO, "Finished reading file  \"{0}" + "\"" + "genome with: {1} chromosomes", new Object[]{referenceJob.getFile(), chromCounter} );
        return refGenome;

    }


    /**
     * Creates a chromosome for a given name and adds it to the given reference.
     * <p>
     * @param chromName name of the chromosome
     * @param reference reference genome to which the chromosome shall be added
     */
    private void createChromosome( final String chromName, final long chromLength, final ParsedReference reference ) {
        ParsedChromosome chrom = new ParsedChromosome( chromName, chromLength, false );
        reference.addChromosome( chrom );
    }

    /*
     * get the name of the used parser
     */

    @Override
    public String getName() {
        return PARSER_NAME;
    }


    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSION;
    }


    @Override
    public String getInputFileDescription() {
        return FILE_DESCRIPTION;
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
