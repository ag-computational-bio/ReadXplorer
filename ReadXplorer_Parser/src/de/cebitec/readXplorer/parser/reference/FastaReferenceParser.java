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


import de.cebitec.common.parser.fasta.FastaIndexEntry;
import de.cebitec.common.parser.fasta.FastaIndexReader;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.common.ParsedChromosome;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.util.FastaUtils;
import de.cebitec.readXplorer.util.Observer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The FastaReferenceParser can parse a reference genome from a fasta file.
 * This means, the sequence dictonary is checked for multiple sequences,
 * corresponding chromosomes are created and the fasta file is indexed, if that
 * is not already the case. Later, the data has to be directly fetched from the
 * now indexed fasta file.
 * Attention: there will be no features in this file just the sequence.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static final String parsername = "Fasta file";
    private static final String[] fileExtension = new String[]{ "fas", "fasta", "fna", "fa" };
    private static final String fileDescription = "Fasta File";
    private final ArrayList<Observer> observers = new ArrayList<>();
    private String errorMsg;


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
     * @param filter       the feature filter to use for this reference. Not
     *                     needed
     *                     for fasta, since it does not have features.
     * <p>
     * @return returns the object parsedReference with the name, description
     *         and chromosomes for the reference genome
     * <p>
     * @throws de.cebitec.readXplorer.parser.common.ParsingException
     */
    @Override
    public ParsedReference parseReference( ReferenceJob referenceJob, FeatureFilter filter ) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        int chromCounter = 0;
        Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "Start reading file  \"{0}\"", referenceJob.getFile() );
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


        }
        catch( IOException ex ) {
            this.notifyObservers( ex.getMessage() );
        }

        Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "Finished reading file  \"{0}" + "\"" + "genome with: {1} chromosomes", new Object[]{ referenceJob.getFile(), chromCounter } );
        return refGenome;

    }


    /**
     * Creates a chromosome for a given name and adds it to the given reference.
     * <p>
     * @param chromName name of the chromosome
     * @param reference reference genome to which the chromosome shall be added
     */
    private void createChromosome( String chromName, long chromLength, ParsedReference reference ) {
        ParsedChromosome chrom = new ParsedChromosome( chromName, chromLength, false );
        reference.addChromosome( chrom );
    }

    /*
     * get the name of the used parser
     */

    @Override
    public String getName() {
        return parsername;
    }


    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }


    @Override
    public String getInputFileDescription() {
        return fileDescription;
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
