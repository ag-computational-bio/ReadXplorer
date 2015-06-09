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

package de.cebitec.readxplorer.tools.snpdetection;


import de.cebitec.common.sequencetools.geneticcode.AminoAcidProperties;
import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.SequenceComparison;
import de.cebitec.readxplorer.databackend.dataobjects.CodonSnp;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.Snp;
import de.cebitec.readxplorer.utils.CodonUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.polytree.Node;
import de.cebitec.readxplorer.utils.polytree.NodeVisitor;
import java.util.ArrayList;
import java.util.List;


/**
 * Generates all translations possible for a given snp for the given genomic
 * features and a reference sequence. A translation is only generated if one of
 * the following holds: - The current feature has no subfeatures and the
 * position is not at a border while the current triplet violates the border. -
 * The current feature has subfeatures and the snp is located in such a
 * subfeature. - The snp is located in a subfeature at a border, but this is not
 * the last subfeature (depending on the strand) and the triplet can be
 * completed from the neighboring subfeature.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpTranslator {

    private final PersistentChromosome chromosome;
    private final long refLength;
    private final List<PersistentFeature> genomicFeatures;
    private final GeneticCode code;
    private int featIdx;
    private int subPos; //summed up bases in subfeatures up to the snp position regarding the strand of the feature
    private boolean posDirectAtLeftChromBorder;
    private boolean posAtLeftChromBorder;
    private boolean posAtRightChromBorder;
    private boolean posDirectAtRightChromBorder;
    private boolean posAtLeftSubBorder;
    private boolean posAtRightSubBorder;
    private boolean snpInSubfeature; //if not and we have subfeatures, then this snp will not be translated
    private PersistentFeature borderSubfeat;
    private PersistentFeature subfeatBefore; //only set if pos is at borders
    private PersistentFeature subfeatAfter;
    private boolean subFeatureFound;
    private final PersistentReference reference;


    /**
     * Generates all translations possible for a given snp for the given genomic
     * features and a reference sequence. A translation is only generated if one
     * of the following holds: - The current feature has no subfeatures and the
     * position is not at a border while the current triplet violates the
     * border. - The current feature has subfeatures and the snp is located in
     * such a subfeature. - The snp is located in a subfeature at a border, but
     * this is not the last subfeature (depending on the strand) and the triplet
     * can be completed from the neighboring subfeature.
     * <p>
     * @param genomicFeatures all features of the reference genome of the
     *                        desired feature types
     * @param chromosome      the chromosome for the current analysis
     * @param reference       the reference sequence for the current analysis
     */
    public SnpTranslator( List<PersistentFeature> genomicFeatures, PersistentChromosome chromosome, PersistentReference reference ) {
        this.genomicFeatures = genomicFeatures;
        this.chromosome = chromosome;
        this.reference = reference;
        this.refLength = chromosome.getLength();
        featIdx = 0;
        code = CodonUtils.getGeneticCode();
    }


    /**
     * @param position check, if this position is covered by at least one
     *                 feature (gene)
     * <p>
     * @return the list of features covering the given position
     */
    public List<PersistentFeature> checkCoveredByFeature( int position ) {

        //find feature/s which cover current snp position
        List<PersistentFeature> featuresFound = new ArrayList<>();
        int stopIndex = featIdx;
        boolean fstFoundFeat = true;

        while( featIdx < this.genomicFeatures.size() ) {

            PersistentFeature feature = this.genomicFeatures.get( featIdx++ );
            if( feature.getStart() <= position && feature.getStop() >= position ) {
                //found hit, also try next index
                featuresFound.add( feature );
                if( fstFoundFeat ) {
                    stopIndex = featIdx - 1;
                    fstFoundFeat = false;
                }
//            } else if( feature.getStop() < position ) {
//                //Comment just for info purpose: do nothing in this case
            } else if( feature.getStop() > position && feature.getStart() > position ) {
                break; //stop
            }
        }
        featIdx = stopIndex < 0 ? 0 : stopIndex; //to always ensure not to forget about the last visited features

        return featuresFound;

    }


    /**
     * Generates all translations possible for a given snp for the given genomic
     * features (genes) and reference sequence (set in the constructor) and
     * stores them in the given Snp object. A translation is only generated if
     * one of the following holds: - The current feature has no subfeatures and
     * the position is not at a border while the current triplet violates the
     * border. - The current feature has subfeatures and the snp is located in
     * such a subfeature. - The snp is located in a subfeature at a border, but
     * this is not the last subfeature (depending on the strand) and the triplet
     * can be completed from the neighboring subfeature.
     * <p>
     * @param snp the snp object to check
     */
    public void checkForFeature( Snp snp ) {

        //find feature/s which cover current snp position
        List<PersistentFeature> featuresFound = this.checkCoveredByFeature( snp.getPosition() );

        //amino acid substitution calculations
        List<CodonSnp> codonSnpList = this.calcSnpList( featuresFound, snp );
        for( CodonSnp codon : codonSnpList ) {
            snp.addCodon( codon );
        }
    }


    /**
     * Calculates the list of snp codons belonging to a single snp. This list is
     * larger than one if more than one features have been found at the "pos"
     * position in the reference genome.
     * <p>
     * @param featuresFound list of features in the reference genome for current
     *                      position "pos"
     * @param snp           complete snp object
     * <p>
     * @return list of CodonSnps for the current snp position "pos"
     */
    private List<CodonSnp> calcSnpList( List<PersistentFeature> featuresFound, Snp snp ) {

        int pos = snp.getPosition();
        posDirectAtLeftChromBorder = pos < 2; //pos is never smaller than 1, 1 is min
        posAtLeftChromBorder = pos < 3;
        posAtRightChromBorder = pos + 2 > this.refLength;
        posDirectAtRightChromBorder = pos + 1 > this.refLength;

        //handle feature knowledge:
        //get each strand and triplet for correct reading frame for translation
        List<CodonSnp> codonSnpList = new ArrayList<>();
        for( PersistentFeature feature : featuresFound ) {
            int mod; //fwdStrand: 1 = left base, 2 = middle base, 0 = right base / revStrand: 1 = left base, 2 = middle base, 0 = right base
            String tripletRef = "";
            String tripletSnp = "";
            subPos = 0;

            /*
             * Check for subfeatures and calculate length of spliced mRNA and
             * snp position on this mRNA. Also need to check, if the current
             * position is at a border of its subfeature. Then we need the
             * neighboring subfeatures for the refrence sequence of the
             * translation triplet and also the distance along all subfeatures
             * up to our snp position.
             */
            boolean fwdStrand = feature.isFwdStrand();

            if( feature.getType() == FeatureType.GENE || feature.getType() == FeatureType.MRNA ||
                feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA ) {
                this.calcFeatureData( feature, pos, FeatureType.CDS );
                if( !subFeatureFound ) {
                    this.calcFeatureData( feature, pos, FeatureType.EXON );
                }
            }
            if( !subFeatureFound && feature.getType() == FeatureType.GENE ) {
                this.calcFeatureData( feature, pos, FeatureType.MRNA );
                if( !subFeatureFound ) { //if the gene has a rRNA or tRNA instead of an mRNA, we have to check this, too
                    this.calcFeatureData( feature, pos, FeatureType.RRNA );
                }
                if( !subFeatureFound ) {
                    this.calcFeatureData( feature, pos, FeatureType.TRNA );
                }
            }

            if( subFeatureFound && !snpInSubfeature ) {
                continue; // we have subfeatures, but the snp is not in them, so we do not translate it!
            }

            try { //we need to catch, if any of the positions is out of bounds!

                if( subPos <= 0 || borderSubfeat == null ) { //there are no subfeatures, or pos is not at border in subfeature

                    int featureStartOnStrand = fwdStrand ? feature.getStart() : feature.getStop();

                    if( subPos == 0 ) { //feature without subfeatures
                        mod = (Math.abs( pos - featureStartOnStrand ) + 1) % 3;
                    } else { //feature with subfeatures, we just have to get the mod
                        mod = subPos % 3;
                    }

                    if( !posAtRightChromBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0) ) { //left base of triplet, get pos to pos+2
                        tripletRef = reference.getChromSequence( chromosome.getId(), pos, pos + 2 );
                        tripletSnp = snp.getBase().toLowerCase().concat( tripletRef.substring( 1 ) );
                    } else if( mod == 2 && !posDirectAtLeftChromBorder && !posDirectAtRightChromBorder ) { //middle base of triplet, get pos-1, pos and pos+1
                        tripletRef = reference.getChromSequence( chromosome.getId(), pos - 1, pos + 1 );
                        tripletSnp = tripletRef.charAt( 0 ) + snp.getBase().toLowerCase() + tripletRef.charAt( 2 );
                    } else if( !posAtLeftChromBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1) ) { //right base of triplet, get pos-2 to pos
                        tripletRef = reference.getChromSequence( chromosome.getId(), pos - 2, pos );
                        tripletSnp = tripletRef.substring( 0, 2 ).concat( snp.getBase().toLowerCase() );
                    }
                } else { //snp is located in a subfeature (exon) and at a border of a subfeature

                    mod = subPos % 3;
                    boolean posDirectAtLeftSubBorder = pos - 1 < borderSubfeat.getStart();
                    boolean posDirectAtRightSubBorder = pos + 1 > borderSubfeat.getStop();

                    if( !posAtRightChromBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0) ) { //left (fwd) or right (rev) base of triplet, get pos to pos+2

                        if( posAtRightSubBorder ) {
                            if( posDirectAtRightSubBorder ) { //get only last base from current subfeature and two from next subfeature
                                tripletRef = reference.getChromSequence( chromosome.getId(), pos, pos ) + reference.getChromSequence( chromosome.getId(), subfeatAfter.getStart(), subfeatAfter.getStart() + 1 );
                            } else { //get last two bases from current subfeature and first base of next subfeature
                                tripletRef = reference.getChromSequence( chromosome.getId(), pos, pos + 1 ) + reference.getChromSequence( chromosome.getId(), subfeatAfter.getStart(), subfeatAfter.getStart() );
                            }
                        } else {
                            tripletRef = reference.getChromSequence( chromosome.getId(), pos, pos + 2 );
                        }
                        tripletSnp = snp.getBase().toLowerCase().concat( tripletRef.substring( 1 ) );

                    } else if( mod == 2 && !posDirectAtLeftChromBorder && !posDirectAtRightChromBorder ) { //middle base of triplet, get pos-1, pos and pos+1

                        if( posDirectAtLeftSubBorder ) { //get one base from left subfeature and one from right subfeature, adding last base later
                            tripletRef = reference.getChromSequence( chromosome.getId(), subfeatBefore.getStop(), subfeatBefore.getStop() ) + reference.getChromSequence( chromosome.getId(), pos, pos );
                        } else {
                            tripletRef = reference.getChromSequence( chromosome.getId(), pos - 1, pos );
                        }
                        if( posDirectAtRightSubBorder ) {
                            tripletRef += reference.getChromSequence( chromosome.getId(), subfeatAfter.getStart(), subfeatAfter.getStart() );
                        } else {
                            tripletRef += reference.getChromSequence( chromosome.getId(), pos + 1, pos + 1 );
                        }
                        tripletSnp = tripletRef.charAt( 0 ) + snp.getBase().toLowerCase() + tripletRef.charAt( 2 );

                    } else if( !posAtLeftChromBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1) ) { //right base of triplet, get pos-2 to pos

                        if( posAtLeftSubBorder ) {
                            if( posDirectAtLeftSubBorder ) { //get both left bases from other subfeature
                                tripletRef = reference.getChromSequence( chromosome.getId(), subfeatBefore.getStop() - 1, subfeatBefore.getStop() ) + reference.getChromSequence( chromosome.getId(), pos, pos );
                            } else { //get last base from feature before
                                tripletRef = reference.getChromSequence( chromosome.getId(), subfeatBefore.getStop(), subfeatBefore.getStop() ) + reference.getChromSequence( chromosome.getId(), pos - 1, pos );
                            }
                        } else {
                            tripletRef = reference.getChromSequence( chromosome.getId(), pos - 2, pos );
                        }
                        tripletSnp = tripletRef.substring( 0, 2 ).concat( snp.getBase().toLowerCase() );
                    }
                }

                if( !fwdStrand ) {
                    tripletRef = SequenceUtils.getReverseComplement( tripletRef );
                    tripletSnp = SequenceUtils.getReverseComplement( tripletSnp );
                }
            } catch( ArrayIndexOutOfBoundsException | NullPointerException e ) {
                continue;
            }

            //translate string to amino acid and store reference and snp codon
            try {
                char aminoRef = this.code.getTranslation( tripletRef );
                char aminoSnp = this.code.getTranslation( tripletSnp );

                //determine effect type of snp on the amino acid sequence
                SequenceComparison type = aminoRef == aminoSnp ? SequenceComparison.MATCH : SequenceComparison.SUBSTITUTION;
                if( type == SequenceComparison.SUBSTITUTION ) {
                    if( AminoAcidProperties.getPropertyForAA( aminoRef ).equals( AminoAcidProperties.getPropertyForAA( aminoSnp ) ) ) {
                        type = SequenceComparison.NEUTRAL;
                    } else {
                        type = SequenceComparison.MISSENSE;
                    }
                }

                codonSnpList.add( new CodonSnp( tripletRef, tripletSnp, aminoRef, aminoSnp, type, feature ) );
            } catch( NullPointerException | AssertionError e ) {
                codonSnpList.add( new CodonSnp( tripletRef, tripletSnp, '-', '-', SequenceComparison.UNKNOWN, feature ) );
                //nothing to do, ignore translations with N's or gaps
            }
        }
        return codonSnpList;
    }


    /**
     * Calculates the feature type length of the given feature type for all
     * features downward in the feature tree hierarchy (top-down fashion).
     * <p>
     * @param feature the feature whose subfeature length and readcount is
     *                needed
     * @param pos     SNP position
     * @param type    the feature type for which the length sum and total read
     *                count is needed
     */
    private void calcFeatureData( final PersistentFeature feature, final int pos, final FeatureType type ) {
        //calc length of all exons of the mRNA and number of reads mapped to exon regions
        subfeatAfter = null;
        subfeatBefore = null;
        snpInSubfeature = false;
        subPos = 0;
        subFeatureFound = false;
        feature.topDown( new NodeVisitor() {

            @Override
            public void visit( Node node ) {

                PersistentFeature subFeature = (PersistentFeature) node;
                final boolean fwdStrand = feature.isFwdStrand();
                if( subFeature.getType() == type ) {
                    subFeatureFound = true;
                    int featureStartOnStrand = fwdStrand ? subFeature.getStart() : subFeature.getStop();
                    int featureStart = subFeature.getStart();

                    if( subFeature.getStop() >= pos && featureStart <= pos ) {
                        subPos += (Math.abs( pos - featureStartOnStrand ) + 1);
                        //only set subfeatAtPos, if position is at a border of the subFeature
                        posAtLeftSubBorder = pos - 2 < featureStart;
                        posAtRightSubBorder = pos + 2 > subFeature.getStop();
                        borderSubfeat = !posAtLeftSubBorder && !posAtRightSubBorder ? null : subFeature;
                        snpInSubfeature = true;
                    } else if( featureStart < pos ) {
                        //get distance in feature and left neighbor subFeature of subFeature with position
                        if( fwdStrand ) {
                            subPos += (subFeature.getStop() - (featureStart - 1));
                        }
                        if( subfeatBefore == null || subfeatBefore.getStart() < subFeature.getStart() ) {
                            subfeatBefore = subFeature;
                        }
                    } else if( featureStart > pos ) {
                        if( !fwdStrand ) {
                            subPos += (featureStartOnStrand - (subFeature.getStart() - 1));
                        }
                        //get right neighbor subFeature of subFeature with position
                        if( subfeatAfter == null || subfeatAfter.getStart() > subFeature.getStart() ) {
                            subfeatAfter = subFeature;
                        }
                    }
                }
            }


        } );
    }


}
