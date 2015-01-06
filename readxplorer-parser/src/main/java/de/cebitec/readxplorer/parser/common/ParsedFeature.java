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


import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.polytree.Node;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains all available information about a persistent feature.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class ParsedFeature extends Node implements Comparable<ParsedFeature> {

    private Integer start;
    private Integer stop;
    private Integer strand;
    private String locusTag;
    private String product;
    private String ecNumber;
    private String geneName;
    private String identifier;
    private List<ParsedFeature> subFeatures;
    private List<String> parentIds;
    private long id;


    /**
     * Contains all available information about a persistent feature.
     * <p>
     * @param type        FeatureType.CDS, FeatureType.REPEAT_UNIT,
     *                    FeatureType.R_RNA, FeatureType.SOURCE,
     *                    FeatureType.T_RNA, FeatureType.MISC_RNA,
     *                    FeatureType.MI_RNA, FeatureType.GENE,
     *                    FeatureType.M_RNA (mandatory)
     * @param start       start position (mandatory)
     * @param stop        stop position (mandatory)
     * @param strand      SequenceUtils.STRAND_FWD for featues on forward and
     *                    SequenceUtils.STRAND_REV on reverse strand
     * @param locusTag    locus information
     * @param product     description of the protein product
     * @param ecNumber    ec number
     * @param geneName    name of the gene, if it exists (e.g. "dnaA")
     * @param subFeatures the list of sub features belonging to this feature
     * @param parentIds   the ids of the features to which the current feature
     *                    belongs
     */
    public ParsedFeature( FeatureType type, int start, int stop, int strand, String locusTag, String product,
                          String ecNumber, String geneName, List<ParsedFeature> subFeatures, List<String> parentIds ) {
        super( type, null ); // if type is null, 0 is assumed, which is equal to FeatureType.UNDEFINED
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.locusTag = locusTag;
        this.product = product;
        this.ecNumber = ecNumber;
        this.geneName = geneName;
        this.subFeatures = subFeatures != null ? subFeatures : new ArrayList<ParsedFeature>();
        this.parentIds = parentIds != null ? parentIds : new ArrayList<String>();
    }


    /**
     * Contains all available information about a persistent feature.
     * <p>
     * @param type        FeatureType.CDS, FeatureType.REPEAT_UNIT,
     *                    FeatureType.R_RNA,
     *                    FeatureType.SOURCE, FeatureType.T_RNA, FeatureType.MISC_RNA,
     *                    FeatureType.MI_RNA, FeatureType.GENE, FeatureType.M_RNA (mandatory)
     * @param start       start position (mandatory)
     * @param stop        stop position (mandatory)
     * @param strand      SequenceUtils.STRAND_FWD for featues on forward and
     *                    SequenceUtils.STRAND_REV on reverse strand
     * @param locusTag    locus information
     * @param product     description of the protein product
     * @param ecNumber    ec number
     * @param geneName    name of the gene, if it exists (e.g. "dnaA")
     * @param subFeatures the list of sub features belonging to this feature
     * @param parentIds   the ids of the features to which the current feature
     *                    belongs
     * @param identifier  unique identifier of this feature in this data set
     */
    public ParsedFeature( FeatureType type, int start, int stop, int strand, String locusTag, String product,
                          String ecNumber, String geneName, List<ParsedFeature> subFeatures, List<String> parentIds, String identifier ) {
        this( type, start, stop, strand, locusTag, product, ecNumber, geneName, subFeatures, parentIds );
        this.identifier = identifier;
    }


    /**
     * Contains only the unique identifier of this feature and should be used
     * for multifurcated tree handling (e.g. adding children to the feature).
     * <p>
     * @param identifier unique identifier of this feature in this data set
     */
    public ParsedFeature( String identifier ) {
        this( null, 0, 0, 0, null, null, null, null, null, null, identifier );
        this.identifier = identifier;
    }


    public boolean hasEcNumber() {
        return ecNumber != null;
    }


    public String getEcNumber() {
        return ecNumber;
    }


    public boolean hasGeneName() {
        return this.geneName != null;
    }


    public String getGeneName() {
        return this.geneName;
    }


    public boolean hasLocusTag() {
        return locusTag != null;
    }


    public String getLocusTag() {
        return locusTag;
    }


    public boolean hasProduct() {
        return product != null;
    }


    public String getProduct() {
        return product;
    }


    /**
     * @return start of the feature. Always the smaller value among start and
     *         stop.
     */
    public int getStart() {
        return start;
    }


    /**
     * @return stop of the feature. Always the larger value among start and
     *         stop.
     */
    public int getStop() {
        return stop;
    }


    public boolean hasStrand() {
        return strand != null && strand != 0;
    }


    public int getStrand() {
        return strand;
    }


    public FeatureType getType() {
        return this.getNodeType(); //TODO: remove this method, if everything works
    }


    /**
     * @return the list of exons of this feature or an empty list if there are
     *         no exons
     */
    public List<ParsedFeature> getSubFeatures() {
        return subFeatures;
    }


    /**
     * Adds a sub feature to the list of sub features (e.g. an exon to a gene).
     * <p>
     * @param parsedSubFeature the sub feature to add.
     */
    public void addSubFeature( ParsedFeature parsedSubFeature ) {
        this.subFeatures.add( parsedSubFeature );
    }


    /**
     * Sets the parent id list for this feature, which contains all identifiers
     * of the parents of this feature.
     * <p>
     * @param parentIds list of parent ids
     */
    public void setParentIds( List<String> parentIds ) {
        this.parentIds = parentIds;
    }


    /**
     * @return The list of parent identifiers/names of this feature, if it has
     *         parent features. Otherwise the list is empty.
     */
    public List<String> getParentIds() {
        return parentIds;
    }


    /**
     * @return True, if this feature has at least one parent, false otherwise.
     */
    public boolean hasParents() {
        return !parentIds.isEmpty();
    }


    /**
     * @return Concatenates the parent names and returns them.
     */
    public String getParentIdsConcat() {
        StringBuilder builder = new StringBuilder( 20 );
        for( String parentId : parentIds ) {
            builder.append( parentId ).append( ";" );
        }
        return builder.length() > 0 ? builder.substring( 0, builder.length() - 1 ) : Properties.NO_PARENT_STRING;
    }


    /**
     * @return The identifier of this gene. <code>null</code> if the feature has
     *         no identifier.
     */
    public String getIdentifier() {
        return identifier;
    }


    /**
     * @return True, if this feature has an identifier unique for this data set,
     *         false otherwise.
     */
    public boolean hasIdentifier() {
        return this.identifier != null;
    }


    /**
     * Compares the start and stop positions of both objects.
     * <p>
     * @param o feature to compare to this feature
     * <p>
     * @return 1, if this feature is larger than the other feature, -1 if vice
     *         versa
     *         and 0 if both positions are equal
     */
    @Override
    public int compareTo( ParsedFeature o ) {
        if( o != null ) {

            if( this.getStart() > o.getStart() ) {
                return 1;
            }
            else if( this.getStart() < o.getStart() ) {
                return -1;

            }
            else {
                if( this.getStop() > o.getStop() ) {
                    return 1;
                }
                else if( this.getStop() < o.getStop() ) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
        else {
            throw new NullPointerException();
        }
    }


    /**
     * @param id Unique id for this feature, which will be used in the DB as
     *           primary key
     */
    public void setId( long id ) {
        this.id = id;
    }


    /**
     * @return Unique id for this feature, which will be used in the DB as
     *         primary key
     */
    public long getId() {
        return this.id;
    }


}
