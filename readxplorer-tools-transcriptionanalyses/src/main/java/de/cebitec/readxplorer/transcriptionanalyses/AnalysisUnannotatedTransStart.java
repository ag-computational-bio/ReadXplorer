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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.DetectedFeatures;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TransStartUnannotated;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.StartCodonFilter;
import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.utils.sequence.Region;
import java.util.List;


/**
 * Starts the TSS analysis including the detection of unannotated transcripts.
 * <p>
 * The original TSS detection does the following:
 * <p>
 * Carries out the logic behind the transcription start site (TSS) anaylsis.
 * When executing the transcription start site detection increaseReadCount is
 * always active and maxInitialReadCount + increaseReadCount2 are optional
 * parameters. They can further constrain the search space (e.g. inc = 100, max
 * = 10, inc2 = 50 means that coverage increases above 50 with an initial read
 * count of 0-10 are detected as transcription start sites, but also all
 * increases of 100 and bigger. When the parameters are switched, e.g. inc = 50,
 * max = 10, inc2 = 100, then all coverage increases above 100 with an initial
 * read count of 0-10 are detected as transcription start sites, but for all
 * positions with an initial read count > 10 an increase of 50 read counts is
 * enough to be detected.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisUnannotatedTransStart extends AnalysisTranscriptionStart {

    private final TrackConnector trackCon;


    /**
     * Analysis for a TSS detection including detection of unannotated
     * transcripts.
     * <p>
     * @param trackConnector the track connector for which the detection is
     *                       carried out
     * @param paramsTSS      the tss detection paramter set
     */
    public AnalysisUnannotatedTransStart( TrackConnector trackConnector, ParameterSetTSS paramsTSS ) {

        super( trackConnector, paramsTSS );
        this.trackCon = trackConnector;
    }


    /**
     * Acutally adds the detected TSS to the list of detected TSSs, but also
     * elongates currently unannotated transcripts (with no up- or downstream
     * features on the same strand) up to the minimum coverage, set in the
     * constructor, is reached.
     * <p>
     * @param tss the transcription start site to add to the list
     */
    @Override
    protected void addDetectStart( TranscriptionStart tss ) {
        DetectedFeatures features = tss.getDetFeatures();
        int currentPos = tss.getPos();
        List<Classification> excludedClasses = this.getParametersTSS().getReadClassParams().getExcludedClasses();

        //only if no feature is available, detect the length of the unannotated new transcript
        if( features.getCorrectStartFeature() == null &&
                 features.getDownstreamFeature() == null &&
                 features.getUpstreamFeature() == null ) {

            int increment = tss.isFwdStrand() ? 1 : -1; //only this getTotalCoverage method returns the array with the fixed leftmost position!
            while( currentCoverage.getTotalCoverage( excludedClasses ).getCoverage( currentPos, tss.isFwdStrand() ) >
                     this.getParametersTSS().getMinTranscriptExtensionCov() ) {
                currentPos += increment;
            }
            currentPos -= increment;

            // instead of an ordinary TranscriptStart we add the TranscriptStart with unannotated transcript information
            detectedStarts.add( new TransStartUnannotated( tss.getPos(), tss.isFwdStrand(), tss.getReadStartsAtPos(), tss.getPercentIncrease(),
                                                           tss.getCoverageIncrease(), tss.getDetFeatures(), currentPos, trackCon.getTrackID(), tss.getChromId() ) );

        } else {
            detectedStarts.add( tss );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        super.finish();
        calcNovelTranscriptDetails();
    }


    /**
     * Calculate all additional novel transcript details.<br/>
     * They consist of the next downstream start codon and the corresponding
     * stop codon in the same reading frame than the start codon.
     */
    private void calcNovelTranscriptDetails() {
        PersistentReference refGenome = trackCon.getRefGenome();
        StartCodonFilter codonFilter = new StartCodonFilter( 1, refGenome.getActiveChromLength(), refGenome );

        for( TranscriptionStart tss : detectedStarts ) {
            if( tss instanceof TransStartUnannotated ) {
                codonFilter.setAllStopCodonsSelected( false );
                codonFilter.setAllStartCodonsSelected( true );
                codonFilter.setIntervalSize( 500 ); //we expect the result closeby
                codonFilter.setMaxNoResults( 1 );
                codonFilter.setRequireSameFrame( false );

                Region startCodon = codonFilter.findNextCodon( tss.getPos(), tss.isFwdStrand() );
                if( startCodon != null ) {
                    tss.setStartCodon( startCodon );

                    codonFilter.resetCodons();
                    codonFilter.setAllStartCodonsSelected( false );
                    codonFilter.setAllStopCodonsSelected( true );
                    codonFilter.setRequireSameFrame( true );
                    Region stopCodon = codonFilter.findNextCodon( startCodon.getStartOnStrand(), tss.isFwdStrand() );
                    if( stopCodon != null ) {
                        tss.setStopCodon( stopCodon );
                    }
                }
            }
        }
    }


}
