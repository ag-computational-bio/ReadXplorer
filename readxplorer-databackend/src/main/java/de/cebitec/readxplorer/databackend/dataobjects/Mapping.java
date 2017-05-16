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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import de.cebitec.readxplorer.utils.sequence.GenomicRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * Data structure for storing a mapping on a reference genome.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class Mapping implements ObjectWithId, GenomicRange {

    private int id;
    private final int start;
    private int trackId;
    private final int stop;
    private final boolean isFwdStrand;
    private Map<Integer, Difference> diffs;
    private Map<Integer, Set<ReferenceGap>> gaps;
    private int differences;
    private int sequenceID;
    private MappingClass mappingClass;
    private int mappingQuality;
    private byte[] baseQualities;
    private int numMappingsForRead;
    private String originalSequence = null;
    private List<SamAlignmentBlock> alignmentBlocks;


    /**
     * Data structure for storing a mapping on a reference genome.
     * <p>
     * @param id
     * @param start
     * @param stop
     * @param trackId
     * @param isFwdStrand
     * @param mismatches
     * @param sequenceID
     * @param mappingClass       classification of this mapping
     * @param mappingQuality     phred mapping quality (if available it is > 0
     *                           or -1 for 255), otherwise 0
     * @param baseQualities      phred score array of base qualities, (if
     *                           available it is > 0 or -1 for 255), otherwise 0
     * @param numMappingsForRead number of mappings for the read of this mapping
     */
    public Mapping( int id, int start, int stop, int trackId, boolean isFwdStrand, int mismatches,
                    int sequenceID, MappingClass mappingClass, int mappingQuality, byte[] baseQualities, int numMappingsForRead ) {
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.trackId = trackId;
        this.isFwdStrand = isFwdStrand;
        this.diffs = new HashMap<>();
        this.gaps = new TreeMap<>();
        this.differences = mismatches;
        this.sequenceID = sequenceID;
        this.mappingClass = mappingClass;
        this.mappingQuality = mappingQuality;
        this.baseQualities = baseQualities;
        this.numMappingsForRead = numMappingsForRead;
        alignmentBlocks = new ArrayList<>(); //initialize as emtpy list
    }


    /**
     * A minimal version of a mapping. It is used to collect the count data.
     * Here, only start, stop and direction are needed. Everything else can be
     * omitted in order to save memory.
     *
     * @param start       Start position in reference bp of the mapping
     * @param stop        Stop position in reference bp of the mapping
     * @param isFwdStrand <code>true</code> if the mapping is located on the
     *                    forward strand, <code>false</code> otherwise.
     */
    public Mapping( int start, int stop, boolean isFwdStrand ) {
        this.start = start;
        this.stop = stop;
        this.isFwdStrand = isFwdStrand;
        alignmentBlocks = new ArrayList<>(); //initialize as emtpy list
    }


    /**
     * @return The complete Map of differences to the reference for this mapping
     */
    public Map<Integer, Difference> getDiffs() {
        return Collections.unmodifiableMap( diffs );
    }


    /**
     * @return The complete TreeMap of genome gaps for this mapping
     */
    public Map<Integer, Set<ReferenceGap>> getGenomeGaps() {
        return Collections.unmodifiableMap( gaps );
    }


    /**
     * @return The unique id of this mapping.
     */
    @Override
    public long getId() {
        return id;
    }


    /**
     * Returns if the mapping is located on the fwd or rev strand.
     * <p>
     * @return <code>true</code> for mappings on forward and <code>false</code>
     *         for mappings on the reverse strand
     */
    @Override
    public boolean isFwdStrand() {
        return isFwdStrand;
    }


    /**
     * @return The track id of this mapping.
     */
    public int getTrackId() {
        return trackId;
    }


    /**
     * @return the absolute start position in genome coordinates. Always the
     *         smaller value among start and stop.
     */
    @Override
    public int getStart() {
        return start;
    }


    /**
     * @return the absolute stop position in genome coordinates. Always the
     *         larger value among start and stop.
     */
    @Override
    public int getStop() {
        return stop;
    }


    /**
     * @return The length of the feature in base pairs.
     */
    public int getLength() {
        return GenomicRange.Utils.getLength( this );
    }


    /**
     * @return The number of differences of this mapping to the reference.
     */
    public int getDifferences() {
        return differences;
    }


    /**
     * Sets the number of differences of this mapping to the reference.
     * <p>
     * @param differences the number of differences of this mapping to the
     *                    reference
     */
    public void setDifferences( int differences ) {
        this.differences = differences;
    }


    /**
     * @return The ID for this mapping sequence, not for the mapping itself!
     */
    public int getSequenceID() {
        return sequenceID;
    }


    /**
     * @return The <code>MappingClass</code> = classification of this mapping.
     */
    public MappingClass getMappingClass() {
        return mappingClass;
    }


    /**
     * @param position the position which should be checked for reference gaps
     * <p>
     * @return <code>true</code>, if reference gaps are stored for the given
     *         position, <code>false</code> otherwise
     */
    public boolean hasGenomeGapAtPosition( int position ) {
        return gaps.containsKey( position );
    }


    /**
     * @param position the position which should be checked for reference gaps
     * <p>
     * @return A TreeSet containing the reference gaps for the given position.
     *         If no gaps are stored for the position <code>null</code> is
     *         returned
     */
    public Set<ReferenceGap> getGenomeGapsAtPosition( int position ) {
        return gaps.get( position );
    }


    /**
     * @param position the position which should be checked for a difference to
     *                 the reference
     * <p>
     * @return <code>true</code>, if a difference is stored for the given
     *         position, <code>false</code> otherwise
     */
    public boolean hasDiffAtPosition( int position ) {
        return diffs.containsKey( position );
    }


    /**
     * @param position the position whose difference to the reference should be
     *                 returned.
     * <p>
     * @return The character deviating from the reference at the given position
     *         Returns <code>null</code>, if no diff is stored for the given
     *         position
     */
    public Character getDiffAtPosition( int position ) {
        return diffs.get( position ).getBase();
    }


    /**
     * Adds a genome gap for a position of this mapping.
     * <p>
     * @param gap the gap to add to this mapping
     */
    public void addGenomeGap( ReferenceGap gap ) {
        if( !gaps.containsKey( gap.getPosition() ) ) {
            gaps.put( gap.getPosition(), new TreeSet<>() );
        }
        gaps.get( gap.getPosition() ).add( gap );
    }


    /**
     * Adds a difference to the reference for a position of this mapping.
     * <p>
     * @param diff the difference to add to this mapping
     */
    public void addDiff( Difference diff ) {
        diffs.put( diff.getPosition(), diff );
    }


    /**
     * Compares two genomic ranges (e.g. two Mappings) based on their start
     * position. '0' is returned for equal start positions, 1, if the start
     * position of the other is larger and -1, if the start position of this
     * Mapping is larger.
     * <p>
     * @param genomicRange genomic range to compare to this Mapping
     * <p>
     * @return '0' for equal start positions, 1, if the start position of the
     *         other is larger and -1, if the start position of this Mapping is
     *         larger.
     */
    @Override
    public int compareTo( GenomicRange genomicRange ) {
        return GenomicRange.Utils.compareTo( this, genomicRange );
    }


    /**
     * is the mapping unique?
     * <p>
     * @return true if the mapping is unique
     */
    public boolean isUnique() {
        return this.numMappingsForRead == 1;
    }


    /**
     * @return the original sequence of the read this info is used only if the
     *         rna trim module has been used and the corresponding custom tag is
     *         contained in the sam/bam file
     */
    public String getOriginalSequence() {
        return originalSequence;
    }


    /**
     * @param originalSequence the originalSequence to set
     */
    public void setOriginalSequence( String originalSequence ) {
        this.originalSequence = originalSequence;
    }


    private int trimmedFromLeft = 0;
    private int trimmedFromRight = 0;


    /**
     * @return the trimmedFromLeft
     */
    public int getTrimmedFromLeft() {
        return trimmedFromLeft;
    }


    /**
     * @param trimmedFromLeft the trimmedFromLeft to set
     */
    public void setTrimmedFromLeft( int trimmedFromLeft ) {
        this.trimmedFromLeft = trimmedFromLeft;
    }


    /**
     * @return the trimmedFromRight
     */
    public int getTrimmedFromRight() {
        return trimmedFromRight;
    }


    /**
     * @param trimmedFromRight the trimmedFromRight to set
     */
    public void setTrimmedFromRight( int trimmedFromRight ) {
        this.trimmedFromRight = trimmedFromRight;
    }


    /**
     * @return The number of mappings for the read of this mapping. -1 Means
     *         that this information is not available.
     */
    public int getNumMappingsForRead() {
        return this.numMappingsForRead;
    }


    /**
     * @return phred score array of base qualities, (if available it is > 0 or
     *         -1 for 255), otherwise 0
     */
    public byte[] getBaseQualities() {
        return baseQualities;
    }


    /**
     * @return phred mapping quality (if available it is > 0 or -1 for 255),
     *         otherwise 0
     */
    public int getMappingQuality() {
        return mappingQuality;
    }


    /**
     * Sets the alignment blocks formed by this mapping, if there are multiple
     * alignment blocks. For mappings with a a single block representing the
     * whole mapping it is not stored.Several blocks exist for split read
     * mappings with {@link CigarOperator.N} bases. The N blocks have to be
     * omitted in the list. Thus, the list only represents mapped regions.
     * <p>
     * @param alignmentBlocks The blocks to set for this mapping
     */
    public void setAlignmentBlocks( List<SamAlignmentBlock> alignmentBlocks ) {
        if( alignmentBlocks.size() > 1 ) {
            this.alignmentBlocks = alignmentBlocks;
        }
    }


    /**
     * @return The alignment blocks formed by this mapping, if there are
     *         multiple alignment blocks. For mappings with a a single block
     *         representing the whole mapping it is not stored.Several blocks
     *         exist for split read mappings with {@link CigarOperator.N} bases.
     *         The N blocks have to be omitted in the list. Thus, the list only
     *         represents mapped regions.
     */
    public List<SamAlignmentBlock> getAlignmentBlocks() {
        return Collections.unmodifiableList( alignmentBlocks );
    }


    /**
     * Calculate the sum of all alignment block lengths.
     *
     * @return The length of the actually aligned parts of the mapping
     */
    public int getAlignmentBlockLength() {
        int length = 0;
        if( alignmentBlocks.isEmpty() ) {
            length = getLength();
        } else {
            for( SamAlignmentBlock alignmentBlock : alignmentBlocks ) {
                length += Math.abs( alignmentBlock.getRefStop() - alignmentBlock.getRefStart() ) + 1;
            }
        }
        return length;
    }


}
