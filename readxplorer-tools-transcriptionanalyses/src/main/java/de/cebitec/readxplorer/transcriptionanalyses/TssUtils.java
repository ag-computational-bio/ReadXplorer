/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses;

import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.utils.SequenceUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains utility methods for the TSS analysis.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class TssUtils {

    /**
     * Do not instantiate.
     */
    private TssUtils() {
    }


    /**
     * Retrieves the upstream chromosome sequence for a TSS on the fwd strand.
     *
     * @param promoterStart The desired promoter start (may be smaller 0 when
     *                      the investigated chromosome is circular)
     * @param ref           Current reference
     * @param tSS           The TSS whose promoter region is needed
     * @param chromLength   Total length in bp of the chromosome on which the
     *                      TSS is located
     *
     * @return The upstream chromosome sequence for the given TSS
     */
    public static String getFwdUpstreamSeq( int promoterStart, PersistentReference ref, TranscriptionStart tSS, int chromLength ) {
        String upstream;
        if( promoterStart > 0 ) {
            upstream = ref.getChromSequence( tSS.getChromId(), promoterStart, tSS.getPos() - 1 );
        } else {
            String firstPart = ref.getChromSequence( tSS.getChromId(), chromLength + promoterStart, chromLength );
            String secondPart = ref.getChromSequence( tSS.getChromId(), 1, tSS.getPos() - 1 );
            upstream = firstPart.concat( secondPart );
        }
        return upstream;
    }


    /**
     * Retrieves the downstream chromosome sequence for a TSS on the fwd strand.
     *
     * @param promoterEnd The desired promoter end (may be larger than the
     *                    <code>chromLength</code> when the investigated
     *                    chromosome is circular)
     * @param ref         Current reference
     * @param tSS         The TSS whose promoter region is needed
     * @param chromLength Total length in bp of the chromosome on which the TSS
     *                    is located
     *
     * @return The downstream chromosome sequence for the given TSS
     */
    public static String getFwdDownstreamSeq( int promoterEnd, PersistentReference ref, TranscriptionStart tSS, int chromLength ) {
        String downstream;
        if( promoterEnd <= chromLength ) {
            downstream = ref.getChromSequence( tSS.getChromId(), tSS.getPos(), promoterEnd );
        } else {
            String firstPart = ref.getChromSequence( tSS.getChromId(), tSS.getPos(), chromLength );
            String secondPart = ref.getChromSequence( tSS.getChromId(), 1, promoterEnd - chromLength );
            downstream = firstPart.concat( secondPart );
        }
        return downstream;
    }


    /**
     * Retrieves the upstream chromosome sequence for a TSS on the rev strand.
     *
     * @param promoterStart The desired promoter start (may be larger than the
     *                      <code>chromLength</code> when the investigated
     *                      chromosome is circular). It is larger than the TSS
     *                      position, because we are on the reverse strand
     * @param ref           Current reference
     * @param tSS           The TSS whose promoter region is needed
     * @param chromLength   Total length in bp of the chromosome on which the
     *                      TSS is located
     *
     * @return The upstream chromosome sequence for the given TSS
     */
    public static String getRevUpstreamSeq( int promoterStart, PersistentReference ref, TranscriptionStart tSS, int chromLength ) {

        String upstream;
        if( promoterStart <= chromLength ) {
            upstream = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), tSS.getPos() + 1, promoterStart ) );
        } else {
            String secondPart = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), tSS.getPos() + 1, chromLength ) );
            String firstPart = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), 1, promoterStart - chromLength ) );
            upstream = firstPart.concat( secondPart );
        }
        return upstream;
    }


    /**
     * Retrieves the downstream chromosome sequence for a TSS on the rev strand.
     *
     * @param promoterEnd The desired promoter end (may be smaller 0 when the
     *                    investigated chromosome is circular)
     * @param ref         Current reference
     * @param tSS         The TSS whose promoter region is needed
     * @param chromLength Total length in bp of the chromosome on which the TSS
     *                    is located
     *
     * @return The downstream chromosome sequence for the given TSS
     */
    public static String getRevDownstreamSeq( int promoterEnd, PersistentReference ref, TranscriptionStart tSS, int chromLength ) {
        String downstream;
        if( promoterEnd > 0 ) {
            downstream = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), promoterEnd, tSS.getPos() ) );
        } else {
            String secondPart = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), 1, tSS.getPos() ) );
            String firstPart = SequenceUtils.getReverseComplement( ref.getChromSequence( tSS.getChromId(), chromLength + promoterEnd, chromLength ) );
            downstream = firstPart.concat( secondPart );
        }
        return downstream;
    }


    /**
     * Prepares the result for output. Any special operations are carried out
     * here. In this case generating the promoter region for each TSS. The
     * promoter regions are stored in the given <code>tssResult</code>.
     *
     * @param tssResult   TSS result whose promoter regions are needed
     * @param refGenomeId Current reference genome id
     *
     * @throws DatabaseException When fetching the reference fails
     */
    public static void processResultForExport( TssDetectionResult tssResult, int refGenomeId ) throws DatabaseException {

        //Generating promoter regions for the TSS
        List<String> promoterRegions = new ArrayList<>();
        List<String> downstreamRegions = new ArrayList<>();
        int promoterUpstreamLength = tssResult.getBpUpstream();
        int promoterDownstreamLength = tssResult.getBpDownstream();
        boolean isCircularChromosomes = tssResult.isCircularChromosomes();

        //get reference sequence for promoter regions
        PersistentReference ref = ProjectConnector.getInstance().getRefGenomeConnector( refGenomeId ).getRefGenome();

        //get the promoter region for each TSS
        int lastChromId = -1;
        int chromLength = -1;
        for( TranscriptionStart tSS : tssResult.getResults() ) {
            if( lastChromId != tSS.getChromId() ) {
                chromLength = ref.getChromosome( tSS.getChromId() ).getLength();
                lastChromId = tSS.getChromId();
            }
            final String upstream;
            final String downstream;
            if( tSS.isFwdStrand() ) {
                int promoterStart = tSS.getPos() - promoterUpstreamLength;
                int promoterEnd = tSS.getPos() + promoterDownstreamLength;
                if( promoterStart < 0 && !isCircularChromosomes ) {
                    promoterStart = 0;
                }
                if( promoterEnd > chromLength && !isCircularChromosomes ) {
                    promoterEnd = chromLength;
                }
                upstream = TssUtils.getFwdUpstreamSeq( promoterStart, ref, tSS, chromLength );
                downstream = TssUtils.getFwdDownstreamSeq( promoterEnd, ref, tSS, chromLength );
            } else {
                int promoterStart = tSS.getPos() + promoterUpstreamLength;
                int promoterEnd = tSS.getPos() - promoterDownstreamLength;
                if( promoterStart > chromLength && !isCircularChromosomes ) {
                    promoterStart = chromLength;
                }
                if( promoterEnd < 0 && !isCircularChromosomes ) {
                    promoterEnd = 0;
                }
                downstream = TssUtils.getRevDownstreamSeq( promoterEnd, ref, tSS, chromLength );
                upstream = TssUtils.getRevUpstreamSeq( promoterStart, ref, tSS, chromLength );
            }
            promoterRegions.add( upstream );
            downstreamRegions.add( downstream );
        }

        tssResult.setPromoterRegions( promoterRegions, downstreamRegions );
    }


}
