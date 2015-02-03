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

package de.cebitec.readxplorer.parser.common;


import de.cebitec.readxplorer.parser.reference.filter.FeatureFilter;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Data holder for a parsed reference sequence. Besides that it knows whether
 * the reference features hierarchy is based on the subfeature concept or the
 * parent id concept and offers a method to transform subfeatures to parent ids.
 * By default the subfeature concept is assumed.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class ParsedReference {

    public static String FINISHED = "ParsingFinished";

    private final List<ParsedChromosome> chromosomes;
    private String description;
    private String name;
    private FeatureFilter filter;
    private Timestamp timestamp;
    private int id;
    private File fastaFile;


    /**
     * Data holder for a parsed reference sequence. Besides that it knows
     * whether the reference features hierarchy is based on the subfeature
     * concept or the parent id concept and offers a method to transform
     * subfeatures to parent ids. By default the subfeature concept is assumed.
     * <p>
     * @author ddoppmeier, rhilker
     */
    public ParsedReference() {
        chromosomes = new ArrayList<>();
        filter = new FeatureFilter();
    }


    /**
     * Sets the unique id of this feature, which will be used in the db.
     * <p>
     * @param id The unique id of this feature, which will be used in the db.
     */
    public void setID( int id ) {
        this.id = id;
    }


    /**
     * @return The unique id of this feature, which will be used in the db.
     */
    public int getID() {
        return id;
    }


    /**
     * @return The timestamp at which this reference was created.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }


    /**
     * The timestamp at which this reference was created.
     * <p>
     * @param timestamp The timestamp to set
     */
    public void setTimestamp( Timestamp timestamp ) {
        this.timestamp = timestamp;
    }


    /**
     * @param filter The feature filter of this reference genome. It defines,
     *               which feature types can be added to the reference and which are excluded.
     */
    public void setFeatureFilter( FeatureFilter filter ) {
        this.filter = filter;
    }


    /**
     * @return The feature filter of this reference genome. It defines, which
     *         feature types can be added to the reference and which are excluded.
     */
    public FeatureFilter getFeatureFilter() {
        return filter;
    }


    /**
     * @return The description of this reference genome
     */
    public String getDescription() {
        return description;
    }


    /**
     * @param description The description of this reference genome
     */
    public void setDescription( String description ) {
        this.description = description;
    }


    /**
     * @return The name of this reference genome.
     */
    public String getName() {
        return name;
    }


    /**
     * @param name The name of this reference genome.
     */
    public void setName( String name ) {
        this.name = name;
    }


    /**
     * Adds a chromosome to the list of chromosomes for this reference.
     * <p>
     * @param parsedChrom the chromosome to add
     */
    public void addChromosome( ParsedChromosome parsedChrom ) {
        chromosomes.add( parsedChrom );
    }


    /**
     * @return The list of chromosomes for this reference.
     */
    public List<ParsedChromosome> getChromosomes() {
        return Collections.unmodifiableList( chromosomes );
    }


    /**
     * Sets the reference fasta file containing the sequence.
     * <p>
     * @param fastaFile The fasta file of the reference containing the sequence.
     */
    public void setFastaFile( File fastaFile ) {
        this.fastaFile = fastaFile;
    }


    /**
     * @return The fasta file of the reference containing the sequence.
     */
    public File getFastaFile() {
        return this.fastaFile;
    }


}
