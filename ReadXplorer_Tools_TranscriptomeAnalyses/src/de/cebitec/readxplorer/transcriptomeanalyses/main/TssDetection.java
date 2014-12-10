
package de.cebitec.readxplorer.transcriptomeanalyses.main;


import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.StartCodon;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides a method for detecting and classification of Transcription start
 * sites.
 *
 * @author jritter
 */
public class TssDetection implements Observer,
                                     AnalysisI<List<TranscriptionStart>> {

    private final List<TranscriptionStart> detectedTSS;
    private final int trackid;
    private final Map<Integer, Boolean> canonicalFwdTss, revTss;
    /**
     * Key: Feature ID, Value: Locus
     */
    private final Map<Integer, String> fwdFeaturesIds, revFeaturesIds;
    private final List<Integer[]> fwdOffsets, revOffsets;


    /**
     * Provides a method for detecting and classification of Transcription start
     * sites.
     *
     * @param refGenome PersistentReference instance.
     * @param trackID   for wich the Transcription start site detection shall
     *                  running.
     */
    public TssDetection( int trackID ) {
        this.trackid = trackID;
        this.detectedTSS = new ArrayList<>();
        this.fwdOffsets = new ArrayList<>();
        this.revOffsets = new ArrayList<>();
        this.canonicalFwdTss = new HashMap<>();
        this.revTss = new HashMap<>();
        this.fwdFeaturesIds = new HashMap<>();
        this.revFeaturesIds = new HashMap<>();
    }


    /**
     *
     * @param ref         PersistentReference
     * @param chromId     the chromosome id
     * @param detectedTss all detected putative transcription start sites
     * @param mm          mappings per million
     * @param chromLength the chromosome length
     * @param fwdFeatures all features in forward direction
     * @param revFeatures all features in reverse direction
     * @param allFeatures all fetures
     * @param parameters  ParameterSetFiveEnrichedAnalyses
     * <p>
     * @return
     */
    public List<TranscriptionStart> postProcessing( PersistentReference ref, int chromId, List<TranscriptionStart> detectedTss, double mm, int chromLength,
                                                    HashMap<Integer, List<Integer>> fwdFeatures, HashMap<Integer, List<Integer>> revFeatures,
                                                    HashMap<Integer, PersistentFeature> allFeatures, ParameterSetFiveEnrichedAnalyses parameters ) {
        List<TranscriptionStart> postProcessedTssList = new ArrayList<>();
        Set<FeatureType> fadeOutFeatureTypes = parameters.getExcludeFeatureTypes();
        // settings for checking CDS-shift
        Map<String, StartCodon> validCodons = parameters.getValidStartCodons();
        double relPercentage = (parameters.getCdsShiftPercentage() / 100.0);

        int leaderlessRange = parameters.getLeaderlessLimit();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();

        boolean isExclusionOfAllIntragenicTss = parameters.isExclusionOfAllIntragenicTSS();
        boolean keepAllIntragenicTss = parameters.isKeepAllIntragenicTss();
        boolean keepOnlyAssignedIntragenicTss = parameters.isKeepOnlyAssignedIntragenicTss();

        int keepingInternalTssDistance = parameters.getKeepIntragenicTssDistanceLimit();

        // for determining first and last feature
        int lastFeatureId = determineBiggestId( allFeatures );
        int fstFeatureId = determineSmallestId( allFeatures );

        for( TranscriptionStart tss : detectedTss ) {
            if( tss.isFwdStrand() ) {
                int pos = tss.getStartPosition();
                int offset = 0;
                int end = 0;
                int dist2start;
                int dist2stop;
                boolean cdsShift = false;
                int offsetToNextDownstreamFeature = 0;

                // determining the offset to next downstream feature
                while( !fwdFeatures.containsKey( pos + offset - end ) ) {
                    if( (pos + offset) > chromLength ) {
                        end = chromLength;
                    }
                    offset++;
                }

                double rel_count = tss.getReadStarts() / mm;
                tss.setRelCount( rel_count );
                PersistentFeature feature = getCorrespondingFeature( fwdFeatures, pos, offset, end, allFeatures, fadeOutFeatureTypes, chromLength );

                if( offset == 0 ) {
                    dist2start = pos - feature.getStart();
                    dist2stop = feature.getStop() - pos;
                    tss.setDist2start( dist2start );
                    tss.setDist2stop( dist2stop );

                    // check if feture is leaderless (downstream direction)
                    if( dist2start <= leaderlessRange ) {
                        tss.setLeaderless( true );

                        // check if cis antisense
                        if( revFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        cdsShift = checkLeaderlessCdsShift( dist2start, ref, chromId, tss.isFwdStrand(), pos, validCodons );

                        if( !cdsShift && dist2start > 0 ) {
                            cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                        }
                        tss.setCdsShift( cdsShift );

                        tss.setDetectedGene( feature );
                        postProcessedTssList.add( tss );
                        if( !cdsShift ) {
                            fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                    }
                    else if( dist2start > leaderlessRange && isExclusionOfAllIntragenicTss ) {
                        // do nothing
                    }
                    else if( dist2start > leaderlessRange && keepAllIntragenicTss ) {
                        int currentFeatureID = feature.getId();
                        // Getting next Feature
                        // PersistentFeature nextDownstreamFeature = getNextDownstreamFeature(fadeOutFeatureTypes, feature, offsetToNextDownstreamFeature, currentFeatureID, lastFeatureId, fstFeatureId, allFeatures, chromLength, pos);
                        PersistentFeature nextDownstreamFeature = null;
                        boolean flag = true;
                        if( flag ) {
                            while( nextDownstreamFeature == null || nextDownstreamFeature.isFwdStrand() == false || fadeOutFeatureTypes.contains( nextDownstreamFeature.getType() ) || feature.getLocus().equals( nextDownstreamFeature.getLocus() ) ) {

                                if( currentFeatureID >= lastFeatureId ) {
                                    currentFeatureID = fstFeatureId - 1;
                                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                    if( nextDownstreamFeature != null ) {
                                        offsetToNextDownstreamFeature = chromLength - pos + nextDownstreamFeature.getStart();
                                    }
                                }
                                else {
                                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                    if( nextDownstreamFeature != null ) {
                                        if( pos > nextDownstreamFeature.getStart() ) {
                                            offsetToNextDownstreamFeature = chromLength + nextDownstreamFeature.getStart() - pos;
                                        }
                                        else {
                                            offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - pos;
                                        }
                                    }
                                }
                            }
                        }
                        // check antisensness
                        if( revFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                            // the putative corresponding gene for TSS
                            tss.setIntragenicTSS( true );
                            if( !cdsShift && dist2start >= 3 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                            }
                            tss.setNextGene( nextDownstreamFeature );
                            tss.setOffsetToNextDownstrFeature( offsetToNextDownstreamFeature );
                            tss.setCdsShift( cdsShift );
                            postProcessedTssList.add( tss );
                            fwdFeaturesIds.put( nextDownstreamFeature.getId(), nextDownstreamFeature.getLocus() );

                        }
                        else {
                            tss.setIntragenicTSS( true );

                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                            }
                            tss.setCdsShift( cdsShift );
                            tss.setDetectedGene( feature );
                            tss.setOffset( offset );
                            tss.setOffsetToNextDownstrFeature( offsetToNextDownstreamFeature );
                            postProcessedTssList.add( tss );
                        }

                    }
                    else if( dist2start > leaderlessRange && keepOnlyAssignedIntragenicTss ) {
                        int currentFeatureID = feature.getId();
                        // Getting next Feature
                        // PersistentFeature nextDownstreamFeature = getNextDownstreamFeature(fadeOutFeatureTypes, feature, offsetToNextDownstreamFeature, currentFeatureID, lastFeatureId, fstFeatureId, allFeatures, chromLength, pos);
                        PersistentFeature nextDownstreamFeature = null;
                        boolean flag = true;
                        if( flag ) {
                            while( nextDownstreamFeature == null || nextDownstreamFeature.isFwdStrand() == false || fadeOutFeatureTypes.contains( nextDownstreamFeature.getType() ) || feature.getLocus().equals( nextDownstreamFeature.getLocus() ) ) {

                                if( currentFeatureID >= lastFeatureId ) {
                                    currentFeatureID = fstFeatureId - 1;
                                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                    if( nextDownstreamFeature != null ) {
                                        offsetToNextDownstreamFeature = chromLength - pos + nextDownstreamFeature.getStart();
                                    }
                                }
                                else {
                                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                    if( nextDownstreamFeature != null ) {
                                        if( pos > nextDownstreamFeature.getStart() ) {
                                            offsetToNextDownstreamFeature = chromLength + nextDownstreamFeature.getStart() - pos;
                                        }
                                        else {
                                            offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - pos;
                                        }
                                    }
                                }
                            }
                        }
                        // check antisensness
                        if( revFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                            // the putative corresponding gene for TSS
                            tss.setIntragenicTSS( true );
                            if( !cdsShift && dist2start >= 3 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                            }
                            tss.setNextGene( nextDownstreamFeature );
                            tss.setOffsetToNextDownstrFeature( offsetToNextDownstreamFeature );
                            tss.setCdsShift( cdsShift );
                            postProcessedTssList.add( tss );
                            fwdFeaturesIds.put( nextDownstreamFeature.getId(), nextDownstreamFeature.getLocus() );
                        }
                    }
                }
                else {
                    // leaderless in upstream direction, offset is != 0 but in leaderless range
                    if( offset <= leaderlessRange ) {
                        tss.setLeaderless( true );
                        // check antisensness
                        if( revFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }
                        // check for cdsShift, when offset > and offset+1 mod 3 == 0
                        if( offset > 0 && ((offset + 1) % 3) == 0 ) {
                            String startAtTSS = getSubSeq( ref, chromId, tss.isFwdStrand(), pos, pos + 2 );
                            if( validCodons.containsKey( startAtTSS ) ) {
                                cdsShift = true;
                            }
                        }
                        tss.setDetectedGene( feature );
                        tss.setOffset( offset );
                        tss.setCdsShift( cdsShift );
                        postProcessedTssList.add( tss );
                        fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                    }
                    else {
                        // checking for "normal" TSS
                        if( offset < distanceForExcludingTss ) {
                            // check antisensness
                            if( revFeatures.get( pos ) != null ) {
                                tss.setPutativeAntisense( true );
                            }

                            tss.setDetectedGene( feature );
                            tss.setOffset( offset );
                            postProcessedTssList.add( tss );
                            fwdOffsets.add( new Integer[]{ pos, pos + offset } );
                            canonicalFwdTss.put( pos, false );
                            fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                        else {
                            // TSS is too far away from next annotated feature
                            // check only for antisense
                            if( revFeatures.get( pos ) != null ) {
                                for( int id : revFeatures.get( pos ) ) {
                                    feature = allFeatures.get( id );
                                    if( !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                        tss.setPutativeAntisense( true );
                                    }
                                }
                            }

                            if( tss.isPutativeAntisense() ) {
                                tss.setDetectedGene( feature );
                                tss.setIntragenicAntisense( true );
                                postProcessedTssList.add( tss );
                            }

                            // check for 3'utr anisenseness
                            if( tss.isPutativeAntisense() == false ) {
                                offset = 0;
                                end = 0;
                                boolean noRevFeatureFlag = true;
                                while( noRevFeatureFlag ) {
                                    if( revFeatures.containsKey( pos + offset - end ) ) {
                                        for( int id : revFeatures.get( pos + offset - end ) ) {
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noRevFeatureFlag = false;
                                                feature = allFeatures.get( id );
                                            }
                                        }

                                    }
                                    if( (pos + offset) > chromLength ) {
                                        end = chromLength;
                                    }
                                    offset++;
                                }
                                if( offset < parameters.getThreeUtrLimitAntisenseDetection() ) {

                                    tss.setPutativeAntisense( true );
                                    tss.setDetectedGene( feature );
                                    tss.setIs3PrimeUtrAntisense( true );
                                    postProcessedTssList.add( tss );
                                }
                            }

                            // determining the offset to next downstream feature
                            // and checking the antisense site for annotated features
                            if( tss.isPutativeAntisense() == false ) {
                                offset = 0;
                                boolean noRevFeatureFlag = true;
                                boolean noFwdFeatureFlag = true;
                                while( noRevFeatureFlag && noFwdFeatureFlag ) {
                                    if( revFeatures.containsKey( pos + offset - end ) ) {
                                        for( int id : revFeatures.get( pos + offset - end ) ) {
//                                                    int id = reverseCDSs.get(i + offset - end).get(0);
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noRevFeatureFlag = false;
                                            }
                                        }
                                    }
                                    if( fwdFeatures.containsKey( pos + offset - end ) ) {
                                        for( Integer id : fwdFeatures.get( pos + offset - end ) ) {
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noFwdFeatureFlag = false;
                                            }
                                        }
                                    }
                                    if( (pos + offset) > chromLength ) {
                                        end = chromLength;
                                    }
                                    offset++;
                                }

                                if( offset > parameters.getThreeUtrLimitAntisenseDetection() ) {
                                    tss.setIntergenicTSS( true );
                                    postProcessedTssList.add( tss );
                                }
                            }
                        }
                    }

                }

            }
            else {
                int pos = tss.getStartPosition();
                int offset = 0;
                int end = 0;
                int dist2start = 0;
                int dist2stop;
                boolean cdsShift = false;
                int offsetToNextDownstreamFeature = 0;

                // determining the offset to feature
                while( !revFeatures.containsKey( end + pos - offset ) ) {
                    if( (pos - offset) == 0 ) {
                        end = chromLength;
                    }
                    offset++;
                }

                double rel_count = tss.getReadStarts() / mm;

                PersistentFeature feature = null;

                boolean flag = true;
//                    // check for overlapping Features
                while( flag ) {
                    if( revFeatures.containsKey( end + pos - offset ) ) {
                        for( int id : revFeatures.get( end + pos - offset ) ) {
                            feature = allFeatures.get( id );
                            if( flag && feature != null && !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                flag = false;
                                break;
                            }
                            else {
                                if( (pos - offset) == 0 ) {
                                    end = chromLength;
                                }
                                offset++;
                            }
                        }
                    }
                    else {
                        if( (pos - offset) == 0 ) {
                            end = chromLength;
                        }
                        offset++;
                    }
                }

                if( offset == 0 ) {
                    dist2start = feature.getStop() - pos;
                    dist2stop = pos - feature.getStart();
                    tss.setDist2start( dist2start );
                    tss.setDist2stop( dist2stop );

                    // check if leaderless (downstream)
                    if( dist2start <= leaderlessRange ) {
                        tss.setLeaderless( true );
                        // check antisensness
                        if( fwdFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        // check for cdsShift when offset is 0 and distance2Start > 0 and dist2Start mod 3 == 0
//                            cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, relPercentage, validCodons);
                        if( dist2start > 0 && (dist2start % 3) == 0 ) {
                            String startAtTSSRev = getSubSeq( ref, chromId, tss.isFwdStrand(), pos - 3, pos );
                            if( validCodons.containsKey( startAtTSSRev ) ) {
                                cdsShift = true;
                            }
                        }

                        if( !cdsShift && dist2start > 0 ) {
                            cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                        }
                        tss.setCdsShift( cdsShift );
                        tss.setDetectedGene( feature );
                        tss.setOffset( offset );
                        postProcessedTssList.add( tss );
                        if( !cdsShift ) {
                            revFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                    }
                    else if( dist2start > leaderlessRange && keepAllIntragenicTss ) {
                        int currentFeatureID = feature.getId();
                        PersistentFeature nextFeature = null;

                        flag = true;
                        if( flag ) {
                            while( nextFeature == null || feature.getLocus().equals( nextFeature.getLocus() ) || fadeOutFeatureTypes.contains( nextFeature.getType() ) || nextFeature.isFwdStrand() ) {

                                if( currentFeatureID <= fstFeatureId ) {
                                    currentFeatureID = lastFeatureId + 1;
                                    nextFeature = allFeatures.get( --currentFeatureID );
                                    if( nextFeature != null ) {
                                        offsetToNextDownstreamFeature = chromLength - nextFeature.getStop() + pos;
                                    }
                                }
                                else {
                                    nextFeature = allFeatures.get( --currentFeatureID );
                                    if( nextFeature != null ) {
                                        if( nextFeature.getStop() > pos ) {
                                            offsetToNextDownstreamFeature = chromLength - nextFeature.getStop() + pos;
                                        }
                                        else {
                                            offsetToNextDownstreamFeature = pos - nextFeature.getStop();
                                        }
                                    }
                                }
                            }
                        }
                        // check antisensness
                        if( fwdFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                            tss.setIntragenicTSS( true );
                            if( !cdsShift && dist2start >= 3 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFalsePositive(), relPercentage, validCodons );
                            }
                            // puttative nextgene
                            tss.setCdsShift( cdsShift );
                            tss.setNextGene( nextFeature );
                            tss.setOffsetToNextDownstrFeature( offsetToNextDownstreamFeature );
                            postProcessedTssList.add( tss );
                            revFeaturesIds.put( nextFeature.getId(), nextFeature.getLocus() );
                        }
                        else {
                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                            }
                            tss.setIntragenicTSS( true );
                            tss.setCdsShift( cdsShift );
                            tss.setDetectedGene( feature );
                            tss.setOffset( offset );
                            postProcessedTssList.add( tss );
                        }
                    }
                    else if( dist2start > leaderlessRange && keepOnlyAssignedIntragenicTss ) {
                        int currentFeatureID = feature.getId();
                        PersistentFeature nextFeature = null;

                        flag = true;
                        if( flag ) {
                            while( nextFeature == null || feature.getLocus().equals( nextFeature.getLocus() ) || fadeOutFeatureTypes.contains( nextFeature.getType() ) || nextFeature.isFwdStrand() ) {

                                if( currentFeatureID <= fstFeatureId ) {
                                    currentFeatureID = lastFeatureId + 1;
                                    nextFeature = allFeatures.get( --currentFeatureID );
                                    if( nextFeature != null ) {
                                        offsetToNextDownstreamFeature = chromLength - nextFeature.getStop() + pos;
                                    }
                                }
                                else {
                                    nextFeature = allFeatures.get( --currentFeatureID );
                                    if( nextFeature != null ) {
                                        if( nextFeature.getStop() > pos ) {
                                            offsetToNextDownstreamFeature = chromLength - nextFeature.getStop() + pos;
                                        }
                                        else {
                                            offsetToNextDownstreamFeature = pos - nextFeature.getStop();
                                        }
                                    }
                                }
                            }
                        }
                        // check antisensness
                        if( fwdFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }

                        if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                            tss.setIntragenicTSS( true );
                            if( !cdsShift && dist2start >= 3 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, pos, dist2start, tss.isFwdStrand(), relPercentage, validCodons );
                            }
                            // puttative nextgene
                            tss.setCdsShift( cdsShift );
                            tss.setNextGene( nextFeature );
                            tss.setOffsetToNextDownstrFeature( offsetToNextDownstreamFeature );
                            postProcessedTssList.add( tss );
                            revFeaturesIds.put( nextFeature.getId(), nextFeature.getLocus() );
                        }
                    }
                }
                else {
                    if( offset <= leaderlessRange ) {

                        // check antisensness
                        if( fwdFeatures.get( pos ) != null ) {
                            tss.setPutativeAntisense( true );
                        }
                        // Leaderless TSS upstream
                        tss.setLeaderless( true );
                        // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                        // cdsShift = checkCdsShift(chrom, feature.getStart(), feature.getStop(), i, dist2start, isFwd, relPercentage, validCodons);
                        if( dist2start > 0 && (dist2start % 3) == 0 ) {
                            String startAtTSSRev = getSubSeq( ref, chromId, tss.isFwdStrand(), pos - 3, pos );
                            if( validCodons.containsKey( startAtTSSRev ) ) {
                                cdsShift = true;
                            }
                        }
                        tss.setCdsShift( cdsShift );
                        tss.setDetectedGene( feature );
                        tss.setOffset( offset );
                        postProcessedTssList.add( tss );
                        revFeaturesIds.put( feature.getId(), feature.getLocus() );
                    }
                    else {
                        // bigger Leaderless restriction
                        // "normal" TSS
                        if( offset < distanceForExcludingTss ) {

                            // check antisensness
                            if( fwdFeatures.get( pos ) != null ) {
                                tss.setPutativeAntisense( true );
                            }
                            tss.setDetectedGene( feature );
                            tss.setOffset( offset );
                            revOffsets.add( new Integer[]{ pos - offset, pos } );
                            revTss.put( pos, false );
                            postProcessedTssList.add( tss );
                            revFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                        else {
                            // check only for antisense
                            if( fwdFeatures.get( pos ) != null ) {
                                for( int id : fwdFeatures.get( pos ) ) {
                                    feature = allFeatures.get( id );
                                    if( !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                        tss.setPutativeAntisense( true );
                                    }
                                }
                                if( tss.isPutativeAntisense() ) {
                                    tss.setDetectedGene( feature );
                                    tss.setIntragenicAntisense( true );
                                    postProcessedTssList.add( tss );
                                }
                            }

                            // check for 3'utr anisenseness
                            if( !tss.isPutativeAntisense() ) {
                                offset = 0;
                                end = 0;
                                boolean noFwdFeatureFlag = true;
                                while( noFwdFeatureFlag ) {
                                    if( fwdFeatures.containsKey( end + pos - offset ) ) {
                                        for( Integer id : fwdFeatures.get( end + pos - offset ) ) {
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noFwdFeatureFlag = false;
                                                feature = allFeatures.get( id );
                                            }
                                        }
                                    }
                                    if( (pos - offset) == 0 ) {
                                        end = chromLength;
                                    }
                                    offset++;
                                }
                                if( offset < parameters.getThreeUtrLimitAntisenseDetection() ) {
                                    tss.setPutativeAntisense( true );
                                    tss.setDetectedGene( feature );
                                    tss.setIs3PrimeUtrAntisense( true );
                                    postProcessedTssList.add( tss );
                                }
                            }
                            // determining the offset to next downstream feature
                            if( !tss.isPutativeAntisense() ) {
                                offset = 0;

                                boolean noRevFeatureFlag = true;
                                boolean noFwdFeatureFlag = true;
                                while( noRevFeatureFlag && noFwdFeatureFlag ) {
                                    if( revFeatures.containsKey( end + pos - offset ) ) {
                                        for( Integer id : revFeatures.get( end + pos - offset ) ) {
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noRevFeatureFlag = false;
                                            }
                                        }
                                    }
                                    if( fwdFeatures.containsKey( end + pos - offset ) ) {
                                        for( Integer id : fwdFeatures.get( end + pos - offset ) ) {
                                            if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                noFwdFeatureFlag = false;
                                            }
                                        }
                                    }
                                    if( (pos - offset) == 0 ) {
                                        end = chromLength;
                                    }
                                    offset++;
                                }
                                if( offset > parameters.getThreeUtrLimitAntisenseDetection() ) {
                                    tss.setIntergenicTSS( true );
                                    postProcessedTssList.add( tss );
                                }
                            }
                        }

                    }

                }
            }

        }

        // running additional 5'-UTR Antisense detection
        for( Integer[] offset : fwdOffsets ) {
            int j = offset[0];
            int k = offset[1];
            for( ; j < k; j++ ) {
                if( revTss.containsKey( j ) ) {
                    revTss.put( j, true );
                }
            }
        }

        for( Integer[] offset : revOffsets ) {
            int j = offset[0];
            int k = offset[1];
            for( ; j < k; j++ ) {
                if( canonicalFwdTss.containsKey( j ) ) {
                    canonicalFwdTss.put( j, true );
                }
            }
        }

        for( TranscriptionStart transcriptionStart : postProcessedTssList ) {
            int start = transcriptionStart.getStartPosition();
            boolean isFwd = transcriptionStart.isFwdStrand();
            if( isFwd ) {
                if( canonicalFwdTss.containsKey( start ) ) {
                    if( canonicalFwdTss.get( start ) == true ) {
                        transcriptionStart.setPutativeAntisense( true );
                        transcriptionStart.setIs5PrimeUtrAntisense( true );
                    }
                }
            }
            else {
                if( revTss.containsKey( start ) ) {
                    if( revTss.get( start ) == true ) {
                        transcriptionStart.setPutativeAntisense( true );
                        transcriptionStart.setIs5PrimeUtrAntisense( true );
                    }
                }
            }

            // Features with assigned stable RNA are flagged as assigned to stable RNA
            if( transcriptionStart.getAssignedFeature() != null ) {
                if( transcriptionStart.getAssignedFeature().getType() == FeatureType.RRNA || transcriptionStart.getAssignedFeature().getType() == FeatureType.TRNA ) {
                    transcriptionStart.setAssignedToStableRNA( true );
                    transcriptionStart.setAssignedFeatureType( transcriptionStart.getAssignedFeature().getType() );
                }
            }

            /**
             * Running postprocessing of cds-shifts. All tss marked as putative
             * cds-shifts are only a valid putative cds-shift, if and only if
             * the feature has not alyready a TSS asigned.
             */
            if( transcriptionStart.isCdsShift() ) {
                int featureID = transcriptionStart.getAssignedFeature().getId();

                if( fwdFeaturesIds.containsKey( featureID ) || revFeaturesIds.containsKey( featureID ) ) {
                    transcriptionStart.setCdsShift( false );
                    transcriptionStart.setIntragenicTSS( true );
                }
            }
        }

        return postProcessedTssList;
    }


    /**
     *
     * @param fadeOutFeatureTypes           features which are excluded from the
     *                                      analysis
     *                                      by the user
     * @param feature                       PersistentFeature
     * @param offsetToNextDownstreamFeature offset to the next downstream
     *                                      located features
     * @param currentFeatureID              the id of current assigned feature
     *                                      to putative
     *                                      tss
     * @param lastFeatureId                 the id of last on the chromosome
     *                                      located feature
     * @param fstFeatureId                  the id of the first on the chomosome
     *                                      located feature
     * @param allFeatures                   all features
     * @param chromLength                   the chromosome length
     * @param pos                           current position during iteraing the
     *                                      chromosome
     * <p>
     * @return
     */
    private PersistentFeature getNextDownstreamFeature( HashSet<FeatureType> fadeOutFeatureTypes, PersistentFeature feature, int offsetToNextDownstreamFeature, int currentFeatureID, int lastFeatureId, int fstFeatureId, HashMap<Integer, PersistentFeature> allFeatures, int chromLength, int pos ) {
        PersistentFeature nextDownstreamFeature = null;
        boolean flag = true;
        if( flag ) {
            while( nextDownstreamFeature == null || nextDownstreamFeature.isFwdStrand() == false || fadeOutFeatureTypes.contains( nextDownstreamFeature.getType() ) || feature.getLocus().equals( nextDownstreamFeature.getLocus() ) ) {

                if( currentFeatureID >= lastFeatureId ) {
                    currentFeatureID = fstFeatureId - 1;
                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                    if( nextDownstreamFeature != null ) {
                        offsetToNextDownstreamFeature = chromLength - pos + nextDownstreamFeature.getStart();
                    }
                }
                else {
                    nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                    if( nextDownstreamFeature != null ) {
                        if( pos > nextDownstreamFeature.getStart() ) {
                            offsetToNextDownstreamFeature = chromLength + nextDownstreamFeature.getStart() - pos;
                        }
                        else {
                            offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - pos;
                        }
                    }
                }
            }
        }

        return nextDownstreamFeature;
    }


    /**
     * This method determines the offset to the current feature.
     *
     * @param fwdFeatures         All persistent features in forward direction.
     * @param pos                 Transcription start position
     * @param offset              length from transcription start site to
     *                            translation start
     *                            site
     * @param end
     * @param allFeatures         All persistent features in both directions
     * @param fadeOutFeatureTypes Feature types which have to be excluded from
     *                            analysis
     * @param chromLength         Chromosome length.
     * <p>
     * @return the current persistent feature
     */
    private PersistentFeature getCorrespondingFeature( Map<Integer, List<Integer>> fwdFeatures, int pos, int offset, int end, Map<Integer, PersistentFeature> allFeatures, Set<FeatureType> fadeOutFeatureTypes, int chromLength ) {
        // getting the PersistentFeature
        PersistentFeature feature = null;

        /**
         * check for overlapping Features and determining the corresponding
         * feature and offset
         */
        boolean flag = true;
        while( flag ) {
            if( fwdFeatures.containsKey( pos + offset - end ) ) {
                for( int id : fwdFeatures.get( pos + offset - end ) ) {
                    feature = allFeatures.get( id );
                    if( feature != null && !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                        flag = false;
                        break;
                    }
                    else {
                        if( (pos + offset) > chromLength ) {
                            end = chromLength;
                        }
                        offset++;
                    }
                }
            }
            else {
                if( (pos + offset) > chromLength ) {
                    end = chromLength;
                }
                offset++;
            }
        }
        return feature;
    }


    /**
     * Determines all putative transcription start sites meet two criteria: the
     * background threshold (bg) and the increation ratio value (ratio).
     *
     * @param statistics      Instance of StatisticsOnMappingData
     * @param ratio           the increase raion value of mapped read starts on
     *                        position i
     *                        to i-1
     * @param chromNo         Chromosome number
     * @param chromId         Chromosome ID
     * @param chromosomLength Chromosome length
     * <p>
     * @return a list of transcription start sites
     */
    public List<TranscriptionStart> tssDetermination( StatisticsOnMappingData statistics, int ratio, int chromNo, int chromId, int chromosomLength ) {

        List<TranscriptionStart> detectedTss = new ArrayList<>(); // List of detected transcription start sites
        int[][] forward = statistics.getForwardReadStarts(); // Array with startsite count information for forward mapping positions.
        int[][] reverse = statistics.getReverseReadStarts(); // Array with startsite count information for reverse mapping positions.
        double bg = statistics.getBgThreshold(); // Background cutoff
        int f_before; // fwd read stack one pos before
        int r_before; // rev read stack one pos before
        boolean isFwd; // direction

        for( int i = 0; i < chromosomLength; i++ ) {

            if( forward[chromNo - 1][i] > bg ) { // background cutoff is passed
                f_before = getReadStartsBeforeTss( true, forward, chromNo, i );

                int f_ratio = (forward[chromNo - 1][i]) / f_before;

                if( f_ratio >= ratio ) {
                    isFwd = true;
                    TranscriptionStart ts = new TranscriptionStart( i, isFwd, chromId, this.trackid );
                    ts.setReadStarts( forward[chromNo - 1][i] );
                    detectedTss.add( ts );
                }
            }

            if( reverse[chromNo - 1][i] > bg ) {
                r_before = getReadStartsBeforeTss( false, reverse, chromNo, i );

                int r_ratio = (reverse[chromNo - 1][i]) / r_before;
                if( r_ratio >= ratio ) {
                    isFwd = false;
                    TranscriptionStart ts = new TranscriptionStart( i, isFwd, chromId, this.trackid );
                    ts.setReadStarts( reverse[chromNo - 1][i] );
                    detectedTss.add( ts );
                }
            }
        }
        return detectedTss;
    }


    /**
     * Returns the number of read starts on the position before the putative
     * tss.
     *
     * @param isFwd      forward direction
     * @param readstarts list of reat starts on each chromosome of the reference
     * @param chromNo    chromosome number
     * @param tss        putative transcription start site
     * <p>
     * @return the number of read starts on the position before the putative tss
     */
    private int getReadStartsBeforeTss( boolean isFwd, int[][] readstarts, int chromNo, int tss ) {

        if( isFwd ) {
            if( readstarts[chromNo - 1][tss - 1] == 0 ) {
                return 1;
            }
            else {
                return readstarts[chromNo - 1][tss - 1];
            }
        }
        else {
            if( readstarts[chromNo - 1][tss + 1] == 0 ) {
                return 1;
            }
            else {
                return readstarts[chromNo - 1][tss + 1];
            }
        }
    }


    /**
     * Running the transcription start site (tss) detection and classification.
     *
     * @param length                   Length of the reference genome.
     * @param fwdFeatures              CDS information for forward regions in
     *                                 genome.
     * @param revFeatures              CDS information for reverse regions in
     *                                 genome.
     * @param allFeatures              HashMap with all featureIDs and
     *                                 associated features.
     * @param ratio                    User given ratio for minimum increase of
     *                                 start counts from
     *                                 pos to pos + 1.
     * @param isLeaderlessDetection    true for performing leaderless detection.
     * @param leaderlessRestirction    Restriction of bases upstream and
     *                                 downstream.
     * @param isExclusionOfInternalTss true for excluding internal TSS.
     * @param distanceForExcludingTss  number restricting the distance between
     *                                 TSS and detected gene.
     */
    public void runningTSSDetection( PersistentReference ref, Map<Integer, List<Integer>> fwdFeatures, Map<Integer, List<Integer>> revFeatures,
                                     Map<Integer, PersistentFeature> allFeatures, StatisticsOnMappingData statistics, int chromId, ParameterSetFiveEnrichedAnalyses parameters ) {

        PersistentChromosome chromosome = ref.getChromosome( chromId );
        int chromosomeLength = chromosome.getLength();
        int chromNo = chromosome.getChromNumber();

        int ratio = parameters.getRatio();
        Set<FeatureType> fadeOutFeatureTypes = parameters.getExcludeFeatureTypes();
        int leaderlessRange = parameters.getLeaderlessLimit();
        Integer distanceForExcludingTss = parameters.getExclusionOfTSSDistance();

        boolean isExclusionOfAllIntragenicTss = parameters.isExclusionOfAllIntragenicTSS();
        boolean keepAllIntragenicTss = parameters.isKeepAllIntragenicTss();
        boolean keepOnlyAssignedIntragenicTss = parameters.isKeepOnlyAssignedIntragenicTss();

        int keepingInternalTssDistance = parameters.getKeepIntragenicTssDistanceLimit();

        int[][] forward = statistics.getForwardReadStarts(); // Array with startsite count information for forward mapping positions.
        int[][] reverse = statistics.getReverseReadStarts(); // Array with startsite count information for reverse mapping positions.

        double mm = statistics.getMappingsPerMillion(); // Mappings per Million.
        double bg = statistics.getBgThreshold(); // Background cutoff

        // for determining first and last feature
        int lastFeatureId = determineBiggestId( allFeatures );
        int fstFeatureId = determineSmallestId( allFeatures );

        // settings for checking CDS-shift
        Map<String, StartCodon> validCodons = parameters.getValidStartCodons();
        double relPercentage = (parameters.getCdsShiftPercentage() / 100.0);

        int f_before;
        int r_before;

        for( int i = 0; i < chromosomeLength; i++ ) {
            if( (forward[chromNo - 1][i] > bg) || (reverse[chromNo - 1][i] > bg) ) { // background cutoff is passed
                int dist2start = 0;
                int dist2stop = 0;
                boolean leaderless = false;
                boolean cdsShift = false;
                boolean isIntragenic = false;
                boolean isPutAntisense = false;
                int offsetToNextDownstreamFeature = 0;

                f_before = getReadStartsBeforeTss( true, forward, chromNo, i );
                r_before = getReadStartsBeforeTss( false, reverse, chromNo, i );

                int f_ratio = (forward[chromNo - 1][i]) / f_before;
                int r_ratio = (reverse[chromNo - 1][i]) / r_before;

                if( f_ratio >= ratio || (forward[chromNo - 1][i - 1] == 0 && f_ratio > bg) ) {
                    boolean isFwd = true;

                    int[] offsetAndArray = determineOffsetInFwdDirection( chromosomeLength, fwdFeatures, i );
                    int offset = offsetAndArray[0];
                    int end = offsetAndArray[1];
                    double rel_count = forward[chromNo - 1][i] / mm;

                    // getting the PersistentFeature
                    PersistentFeature feature = null;
                    boolean flag = true;
                    // check for overlapping Features
                    while( flag ) {
                        if( fwdFeatures.containsKey( i + offset - end ) ) {
                            for( int id : fwdFeatures.get( i + offset - end ) ) {
                                feature = allFeatures.get( id );
                                if( feature != null && !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                    flag = false;
                                    break;
                                }
                                else {
                                    if( (i + offset) > chromosomeLength ) {
                                        end = chromosomeLength;
                                    }
                                    offset++;
                                }
                            }
                        }
                        else {
                            if( (i + offset) > chromosomeLength ) {
                                end = chromosomeLength;
                            }
                            offset++;
                        }
                    }

                    /**
                     * Case 1: offset = 0 => no 5'-UTR => TSS is whether
                     * intragenic, leaderless or represents a CDS-Shift and can
                     * also be located antisense to an annotated feature
                     */
                    if( offset == 0 ) {
                        dist2start = i - feature.getStart();
                        dist2stop = feature.getStop() - i;

                        // check if feture is leaderless (downstream direction)
                        if( dist2start <= leaderlessRange ) {
                            leaderless = true;

                            // check if cis antisense
                            if( revFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }

                            cdsShift = checkLeaderlessCdsShift( dist2start, ref, chromId, isFwd, i, validCodons );

                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                            }

                            TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count,
                                                                             feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                                             leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                            detectedTSS.add( tss );
                            if( !cdsShift ) {
                                fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                            }
                        }
                        else if( dist2start > leaderlessRange && isExclusionOfAllIntragenicTss == false ) {
                            // Case 2: Intragenic TSS if exclusion of intragenic TSS is not set to true
                            // here we want to find the next downstream feature
                            // because the start site is intragenic

                            /**
                             * Getting next Feature
                             */
                            int currentFeatureID = feature.getId();
                            PersistentFeature nextDownstreamFeature = null;
                            flag = true;

                            if( flag ) {
                                while( nextDownstreamFeature == null || nextDownstreamFeature.isFwdStrand() == false || fadeOutFeatureTypes.contains( nextDownstreamFeature.getType() ) || feature.getLocus().equals( nextDownstreamFeature.getLocus() ) ) {

                                    if( currentFeatureID >= lastFeatureId ) {
                                        currentFeatureID = fstFeatureId - 1;
                                        nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                        if( nextDownstreamFeature != null ) {
                                            offsetToNextDownstreamFeature = chromosomeLength - i + nextDownstreamFeature.getStart();
                                        }
                                    }
                                    else {
                                        nextDownstreamFeature = allFeatures.get( ++currentFeatureID );
                                        if( nextDownstreamFeature != null ) {
                                            if( i > nextDownstreamFeature.getStart() ) {
                                                offsetToNextDownstreamFeature = chromosomeLength + nextDownstreamFeature.getStart() - i;
                                            }
                                            else {
                                                offsetToNextDownstreamFeature = nextDownstreamFeature.getStart() - i;
                                            }
                                        }
                                    }
                                }
                            }
                            // check antisensness
                            if( revFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }

                            if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                                // putative the corresponding gene for TSS
                                isIntragenic = true;
                                if( !cdsShift && dist2start >= 3 ) {
                                    cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                                }
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count,
                                                                                 null, offset, dist2start, dist2stop, nextDownstreamFeature, offsetToNextDownstreamFeature,
                                                                                 leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid );
                                detectedTSS.add( tss );
                                fwdFeaturesIds.put( nextDownstreamFeature.getId(), nextDownstreamFeature.getLocus() );
                            }
                            else if( keepAllIntragenicTss ) {
                                isIntragenic = true;
                                if( !cdsShift && dist2start > 0 ) {
                                    cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                                }
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count,
                                                                                 feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                                                 leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid );
                                detectedTSS.add( tss );
                            }
                        }
                        else if( dist2start > leaderlessRange && isExclusionOfAllIntragenicTss == true ) {
                            // at least test whether CDS-Shift occur or not
                            isIntragenic = true;

                            // check antisensness
                            if( revFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }
                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                            }
                            if( cdsShift ) {
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count,
                                                                                 feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                                                 leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid ); //TODO: check if it is internal
                                detectedTSS.add( tss );
                            }
                        }

                    }
                    else {
                        // leaderless in upstream direction, offset is != 0 but in leaderless range
                        if( offset <= leaderlessRange ) {
                            leaderless = true;
                            // check antisensness
                            if( revFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }
                            // check for cdsShift, when offset > and offset+1 mod 3 == 0
                            cdsShift = checkLeaderlessCdsShift( dist2start, ref, chromId, isFwd, i, validCodons );

                            TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count, feature,
                                                                             offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless,
                                                                             cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );//TODO: check if it is internal
                            detectedTSS.add( tss );
                            fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                        else {
                            // checking for "normal" TSS
                            if( offset < distanceForExcludingTss ) {
                                // check antisensness
                                if( revFeatures.get( i ) != null ) {
                                    isPutAntisense = true;
                                }

                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                detectedTSS.add( tss );
                                fwdOffsets.add( new Integer[]{ i, i + offset } );
                                canonicalFwdTss.put( i, false );
                                fwdFeaturesIds.put( feature.getId(), feature.getLocus() );
                            }
                            else {
                                // TSS is too far away from next annotated feature
                                // check only for antisense
                                if( revFeatures.get( i ) != null ) {
                                    for( int id : revFeatures.get( i ) ) {
                                        feature = allFeatures.get( id );
                                        if( !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                            isPutAntisense = true;
                                        }
                                    }
                                }
                                if( isPutAntisense ) {
                                    TranscriptionStart tss = new TranscriptionStart( i, isFwd, forward[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0,
                                                                                     leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                    tss.setIntragenicAntisense( true );
                                    detectedTSS.add( tss );
                                }

                                // check for 3'utr anisenseness
                                if( isPutAntisense == false ) {
                                    offset = 0;
                                    end = 0;
                                    boolean noRevFeatureFlag = true;
                                    while( noRevFeatureFlag ) {
                                        if( revFeatures.containsKey( i + offset - end ) ) {
                                            for( int id : revFeatures.get( i + offset - end ) ) {
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noRevFeatureFlag = false;
                                                    feature = allFeatures.get( id );
                                                }
                                            }

                                        }
                                        if( (i + offset) > chromosomeLength ) {
                                            end = chromosomeLength;
                                        }
                                        offset++;
                                    }
                                    if( offset < parameters.getThreeUtrLimitAntisenseDetection() ) {

                                        isPutAntisense = true;
                                        TranscriptionStart tss = new TranscriptionStart(
                                                i, isFwd, forward[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0,
                                                leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                        tss.setIs3PrimeUtrAntisense( isPutAntisense );
                                        detectedTSS.add( tss );
                                    }
                                }

                                // determining the offset to next downstream feature
                                // and checking the antisense site for annotated features
                                if( isPutAntisense == false ) {
                                    offset = 0;
                                    boolean noRevFeatureFlag = true;
                                    boolean noFwdFeatureFlag = true;
                                    while( noRevFeatureFlag && noFwdFeatureFlag ) {
                                        if( revFeatures.containsKey( i + offset - end ) ) {
                                            for( int id : revFeatures.get( i + offset - end ) ) {
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noRevFeatureFlag = false;
                                                }
                                            }
                                        }
                                        if( fwdFeatures.containsKey( i + offset - end ) ) {
                                            for( Integer id : fwdFeatures.get( i + offset - end ) ) {
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noFwdFeatureFlag = false;
                                                }
                                            }
                                        }
                                        if( (i + offset) > chromosomeLength ) {
                                            end = chromosomeLength;
                                        }
                                        offset++;
                                    }

                                    if( offset > parameters.getThreeUtrLimitAntisenseDetection() ) {
                                        TranscriptionStart tss = new TranscriptionStart(
                                                i, isFwd, forward[chromNo - 1][i], rel_count, null, 0, 0, 0, null, 0,
                                                leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                        tss.setIntergenicTSS( true );
                                        detectedTSS.add( tss );
                                    }
                                }
                            }
                        }
                    }
                }

                if( r_ratio >= ratio || (reverse[chromNo - 1][i - 1] == 0 && r_ratio > bg) ) {
                    boolean isFwd = false;
                    int offset = 0;
                    int end = 0;

                    // determining the offset to feature
                    while( !revFeatures.containsKey( end + i - offset ) ) {
                        if( (i - offset) == 0 ) {
                            end = chromosomeLength;
                        }
                        offset++;
                    }

                    double rel_count = reverse[chromNo - 1][i] / mm;

                    PersistentFeature feature = null;

                    boolean flag = true;
//                    // check for overlapping Features
                    while( flag ) {
                        if( revFeatures.containsKey( end + i - offset ) ) {
                            for( int id : revFeatures.get( end + i - offset ) ) {
                                feature = allFeatures.get( id );
                                if( flag && feature != null && !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                    flag = false;
                                    break;
                                }
                                else {
                                    if( (i - offset) == 0 ) {
                                        end = chromosomeLength;
                                    }
                                    offset++;
                                }
                            }
                        }
                        else {
                            if( (i - offset) == 0 ) {
                                end = chromosomeLength;
                            }
                            offset++;
                        }
                    }

                    if( offset == 0 ) {
                        dist2start = feature.getStop() - i;
                        dist2stop = i - feature.getStart();

                        // check if leaderless (downstream)
                        if( dist2start <= leaderlessRange ) {
                            leaderless = true;
                            // check antisensness
                            if( fwdFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }

                            // check for cdsShift when offset is 0 and distance2Start > 0 and dist2Start mod 3 == 0
                            cdsShift = checkLeaderlessCdsShift( dist2start, ref, chromId, isFwd, i, validCodons );

                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                            }

                            TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                                             leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                            detectedTSS.add( tss );
                            if( !cdsShift ) {
                                revFeaturesIds.put( feature.getId(), feature.getLocus() );
                            }
                        }
                        else if( dist2start > leaderlessRange && isExclusionOfAllIntragenicTss == false ) {
                            int currentFeatureID = feature.getId();
                            PersistentFeature nextFeature = null;

                            flag = true;

                            if( flag ) {
                                while( nextFeature == null || feature.getLocus().equals( nextFeature.getLocus() ) || fadeOutFeatureTypes.contains( nextFeature.getType() ) || nextFeature.isFwdStrand() == true ) {

                                    if( currentFeatureID <= fstFeatureId ) {
                                        currentFeatureID = lastFeatureId + 1;
                                        nextFeature = allFeatures.get( --currentFeatureID );
                                        if( nextFeature != null ) {
                                            offsetToNextDownstreamFeature = chromosomeLength - nextFeature.getStop() + i;
                                        }
                                    }
                                    else {
                                        nextFeature = allFeatures.get( --currentFeatureID );
                                        if( nextFeature != null ) {
                                            if( nextFeature.getStop() > i ) {
                                                offsetToNextDownstreamFeature = chromosomeLength - nextFeature.getStop() + i;
                                            }
                                            else {
                                                offsetToNextDownstreamFeature = i - nextFeature.getStop();
                                            }
                                        }
                                    }
                                }
                            }
                            // check antisensness
                            if( fwdFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }

                            if( offsetToNextDownstreamFeature < keepingInternalTssDistance ) {
                                isIntragenic = true;
                                if( !cdsShift && dist2start >= 3 ) {
                                    cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                                }
                                // puttative nextgene
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                                                 null, offset, dist2start, dist2stop, nextFeature, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid ); //TODO: check if it is internal
                                detectedTSS.add( tss );
                                revFeaturesIds.put( nextFeature.getId(), nextFeature.getLocus() );
                            }
                            else if( keepAllIntragenicTss ) {
                                if( !cdsShift && dist2start > 0 ) {
                                    cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                                }
                                isIntragenic = true;
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                                                 feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid );
                                detectedTSS.add( tss );
                            }
                        }
                        else if( dist2start > leaderlessRange && isExclusionOfAllIntragenicTss == true ) {
                            // at least test if CDS-Shift occur or not
                            isIntragenic = true;
                            // check antisensness
                            if( fwdFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }
                            if( !cdsShift && dist2start > 0 ) {
                                cdsShift = checkCdsShift( ref, chromId, feature, i, dist2start, isFwd, relPercentage, validCodons );
                            }
                            if( cdsShift ) {
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                                                 feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid );
                                detectedTSS.add( tss );
                            }
                        }
                    }
                    else {
                        if( offset <= leaderlessRange ) {

                            // check antisensness
                            if( fwdFeatures.get( i ) != null ) {
                                isPutAntisense = true;
                            }
                            // Leaderless TSS upstream
                            leaderless = true;
                            // check for cdsShift when offset is 0 and distance2Start > 0 and mod 3 == 0
                            cdsShift = checkLeaderlessCdsShift( dist2start, ref, chromId, isFwd, i, validCodons );

                            TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count, feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature,
                                                                             leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                            detectedTSS.add( tss );
                            revFeaturesIds.put( feature.getId(), feature.getLocus() );
                        }
                        else {
                            // bigger Leaderless restriction
                            // "normal" TSS
                            if( offset < distanceForExcludingTss ) {

                                // check antisensness
                                if( fwdFeatures.get( i ) != null ) {
                                    isPutAntisense = true;
                                }
                                TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count,
                                                                                 feature, offset, dist2start, dist2stop, null, offsetToNextDownstreamFeature, leaderless, cdsShift,
                                                                                 isIntragenic, isPutAntisense, chromId, this.trackid );
                                revOffsets.add( new Integer[]{ i - offset, i } );
                                revTss.put( i, false );
                                detectedTSS.add( tss );
                                revFeaturesIds.put( feature.getId(), feature.getLocus() );
                            }
                            else {
                                // check only for antisense
                                if( fwdFeatures.get( i ) != null ) {
                                    for( int id : fwdFeatures.get( i ) ) {
                                        feature = allFeatures.get( id );
                                        if( !fadeOutFeatureTypes.contains( feature.getType() ) ) {
                                            isPutAntisense = true;
                                        }
                                    }
                                    if( isPutAntisense ) {
                                        TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0,
                                                                                         leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                        tss.setIntragenicAntisense( true );
                                        detectedTSS.add( tss );
                                    }
                                }

                                // check for 3'utr anisenseness
                                if( isPutAntisense == false ) {
                                    offset = 0;
                                    end = 0;
                                    boolean noFwdFeatureFlag = true;
                                    while( noFwdFeatureFlag ) {
                                        if( fwdFeatures.containsKey( end + i - offset ) ) {
                                            for( Integer id : fwdFeatures.get( end + i - offset ) ) {
//                                                int id = forwardCDSs.get(end + i - offset).get(0);
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noFwdFeatureFlag = false;
                                                    feature = allFeatures.get( id );
                                                }
                                            }
                                        }
                                        if( (i - offset) == 0 ) {
                                            end = chromosomeLength;
                                        }
                                        offset++;
                                    }
                                    if( offset < parameters.getThreeUtrLimitAntisenseDetection() ) {
                                        isPutAntisense = true;
                                        TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count, feature, 0, 0, 0, null, 0,
                                                                                         leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                        tss.setIs3PrimeUtrAntisense( isPutAntisense );
                                        detectedTSS.add( tss );
                                    }
                                }
                                // determining the offset to next downstream feature
                                if( isPutAntisense == false ) {
                                    offset = 0;

                                    boolean noRevFeatureFlag = true;
                                    boolean noFwdFeatureFlag = true;
                                    while( noRevFeatureFlag && noFwdFeatureFlag ) {
                                        if( revFeatures.containsKey( end + i - offset ) ) {
                                            for( Integer id : revFeatures.get( end + i - offset ) ) {
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noRevFeatureFlag = false;
                                                }
                                            }
                                        }
                                        if( fwdFeatures.containsKey( end + i - offset ) ) {
                                            for( Integer id : fwdFeatures.get( end + i - offset ) ) {
                                                if( !fadeOutFeatureTypes.contains( allFeatures.get( id ).getType() ) ) {
                                                    noFwdFeatureFlag = false;
                                                }
                                            }
                                        }
                                        if( (i - offset) == 0 ) {
                                            end = chromosomeLength;
                                        }
                                        offset++;
                                    }
                                    if( offset > parameters.getThreeUtrLimitAntisenseDetection() ) {
                                        TranscriptionStart tss = new TranscriptionStart( i, isFwd, reverse[chromNo - 1][i], rel_count, null, 0, 0, 0, null, 0,
                                                                                         leaderless, cdsShift, isIntragenic, isPutAntisense, chromId, this.trackid );
                                        tss.setIntergenicTSS( true );
                                        detectedTSS.add( tss );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for( Integer[] offset : fwdOffsets ) {
            int j = offset[0];
            int k = offset[1];
            for( ; j < k; j++ ) {
                if( revTss.containsKey( j ) ) {
                    revTss.put( j, true );
                }
            }
        }

        for( Integer[] offset : revOffsets ) {
            int j = offset[0];
            int k = offset[1];
            for( ; j < k; j++ ) {
                if( canonicalFwdTss.containsKey( j ) ) {
                    canonicalFwdTss.put( j, true );
                }
            }
        }

        for( TranscriptionStart transcriptionStart : detectedTSS ) {
            int start = transcriptionStart.getStartPosition();
            boolean isFwd = transcriptionStart.isFwdStrand();
            if( isFwd ) {
                if( canonicalFwdTss.containsKey( start ) ) {
                    if( canonicalFwdTss.get( start ) == true ) {
                        transcriptionStart.setPutativeAntisense( true );
                        transcriptionStart.setIs5PrimeUtrAntisense( true );
                    }
                }
            }
            else {
                if( revTss.containsKey( start ) ) {
                    if( revTss.get( start ) == true ) {
                        transcriptionStart.setPutativeAntisense( true );
                        transcriptionStart.setIs5PrimeUtrAntisense( true );
                    }
                }
            }

            // Features with assigned stable RNA are flagged as assigned to stable RNA
            if( transcriptionStart.getAssignedFeature() != null ) {
                if( transcriptionStart.getAssignedFeature().getType() == FeatureType.RRNA || transcriptionStart.getAssignedFeature().getType() == FeatureType.TRNA ) {
                    transcriptionStart.setAssignedToStableRNA( true );
                    transcriptionStart.setAssignedFeatureType( transcriptionStart.getAssignedFeature().getType() );
                }
            }

            /**
             * Running postprocessing of cds-shifts. All tss marked as putative
             * cds-shifts are only a valid putative cds-shift, if and only if
             * the feature has not alyready a TSS asigned.
             */
            if( transcriptionStart.isCdsShift() ) {
                int featureID = transcriptionStart.getAssignedFeature().getId();

                if( fwdFeaturesIds.containsKey( featureID ) || revFeaturesIds.containsKey( featureID ) ) {
                    transcriptionStart.setCdsShift( false );
                    transcriptionStart.setIntragenicTSS( true );
                }
            }
        }

    }


    /**
     * Checks for a putative CDS-shift for leaderless transcripts.
     *
     * @param dist2start  if tss is intragenic, the distance between tss and
     *                    start of the overlapping feature is the distance to start
     * @param ref         the PersistentReference
     * @param chromId     the chromosome id
     * @param isFwd       <true> if direction is forward else <false>
     * @param tss         transcription start site
     * @param validCodons all from user selected start codons to evaluate
     * <p>
     * @return <true> if a putative CDS-shift occur else <false>
     */
    private boolean checkLeaderlessCdsShift( int dist2start, PersistentReference ref, int chromId, boolean isFwd, int tss, Map<String, StartCodon> validCodons ) {
        // check for cdsShift, when offset > and offset+1 mod 3 == 0
        boolean cdsShift = false;
        if( dist2start > 0 && (dist2start % 3) == 0 ) {
            String startAtTSS;
            if( isFwd ) {
                startAtTSS = getSubSeq( ref, chromId, isFwd, tss, tss + 2 );
            }
            else {
                startAtTSS = getSubSeq( ref, chromId, isFwd, tss - 2, tss );
            }
            if( validCodons.containsKey( startAtTSS ) ) {
                cdsShift = true;
            }
        }
        return cdsShift;
    }


    /**
     * Method checking for a potential CDS-shift.
     *
     * @param ref              Current processing PersistentChromosome.
     * @param feature          PersistentFeature with information about the
     *                         start anf
     *                         stop
     * @param tssStart         Start position of transcription start site.
     * @param dist2start       Distance from transcription start site to start
     *                         position of PersistentFeature.
     * @param isFwd            true if is forward direction.
     * @param relPercentage    relative region of feature in which the tss
     *                         occur.
     * @param validStartCodons Map of Codons, which has to be validated.
     *
     * @return true, if putative CDS-Shift occur.
     */
    private boolean checkCdsShift( PersistentReference ref, int chromId, PersistentFeature feature, int tssStart, int dist2start, boolean isFwd,
                                   double relPercentage, Map<String, StartCodon> validStartCodons ) {
        double length = feature.getStop() - feature.getStart();
        double partOfFeature = dist2start / length;

        if( partOfFeature <= relPercentage ) {
            //1. is tss in the range of X % of the feature
            if( isFwd ) {
                int startFwd = feature.getStart() + 3;
                int stopFwd = startFwd + 2;
                while( partOfFeature <= relPercentage ) {
                    String startAtTSS = getSubSeq( ref, chromId, isFwd, startFwd, stopFwd );
                    if( validStartCodons.containsKey( startAtTSS ) || startFwd >= tssStart ) {
                        return true;
                    }
                    dist2start = startFwd - feature.getStart();
                    partOfFeature = dist2start / length;
                    startFwd += 3;
                    stopFwd += 3;
                }
            }
            else {
                int startRev = feature.getStop() - 5;
                int stopRev = feature.getStop() - 3;
                while( partOfFeature <= relPercentage ) {
                    String startAtTSSRev = getSubSeq( ref, chromId, isFwd, startRev, stopRev );
                    if( validStartCodons.containsKey( startAtTSSRev ) && startRev <= tssStart ) {
                        return true;
                    }
                    dist2start = feature.getStop() - startRev;
                    partOfFeature = dist2start / length;
                    startRev -= 3;
                    stopRev -= 3;
                }
            }
        }
        return false;
    }


    /**
     * If the direction is reverse, the subsequence will be inverted.
     *
     * @param isFwd direction of sequence.
     * @param start start of subsequence.
     * @param stop  stop of subsequence.
     * <p>
     * @return the subsequence.
     */
    private String getSubSeq( PersistentReference ref, int chromId, boolean isFwd, int start, int stop ) {

        String seq = "";
        if( start > 0 && stop < ref.getChromosome( chromId ).getLength() ) {
            seq = ref.getChromSequence( chromId, start, stop );
        }
        if( isFwd ) {
            return seq;
        }
        else {
            return SequenceUtils.getReverseComplement( seq );
        }
    }


    @Override
    public void update( Object args ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public List<TranscriptionStart> getResults() {
        return detectedTSS;
    }


    /**
     * Determines the biggest Feature id in a map of PersistentFeatures, whereby
     * the Key is the featureID.
     * <p>
     * @param features HashMap: Key => FeatureID, Value => PersistentFeatures
     * <p>
     * @return biggest FeatureID
     */
    private int determineBiggestId( Map<Integer, PersistentFeature> features ) {
        int result = 0;

        for( PersistentFeature persistentFeature : features.values() ) {
            if( persistentFeature.getId() > result ) {
                result = persistentFeature.getId();
            }
        }

        return result;
    }


    /**
     * Determines the smallest Feature ID from a Map with PersistentFeatures.
     *
     * @param features Map: Key => FeatureID, Value => PersistentFeature.
     * <p>
     * @return smallest FeatureID
     */
    private int determineSmallestId( Map<Integer, PersistentFeature> features ) {
        int result = 0;
        for( PersistentFeature persistentFeature : features.values() ) {
            result = persistentFeature.getId();
            break;
        }

        for( PersistentFeature persistentFeature : features.values() ) {
            if( persistentFeature.getId() < result ) {
                result = persistentFeature.getId();
            }
        }

        return result;
    }


    /**
     * Determines the offset in forward direction. Returns an int array with the
     * offset on first and the end value on second position.
     *
     * @param chromLength length of the current chromosome.
     * @param fwdFeatures all forwatd features.
     * @param currentPos  current position in chromosome.
     * <p>
     * @return the offset to next upstream feature.
     */
    private int[] determineOffsetInFwdDirection( int chromLength, Map<Integer, List<Integer>> fwdFeatures, int currentPos ) {
        int offset = 0;
        int end = 0;
        int[] result = new int[2];

        // determining the offset to next downstream feature
        while( !fwdFeatures.containsKey( currentPos + offset - end ) ) {
            if( (currentPos + offset) > chromLength ) {
                end = chromLength;
            }
            offset++;
        }

        result[0] = offset;
        result[1] = end;
        return result;
    }


}
