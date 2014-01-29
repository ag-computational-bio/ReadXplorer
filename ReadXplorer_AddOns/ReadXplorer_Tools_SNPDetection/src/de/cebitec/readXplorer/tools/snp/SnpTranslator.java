package de.cebitec.readXplorer.tools.snp;

import de.cebitec.common.sequencetools.geneticcode.AminoAcidProperties;
import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.readXplorer.databackend.dataObjects.ChromosomeObserver;
import de.cebitec.readXplorer.databackend.dataObjects.CodonSnp;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SequenceComparison;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.polyTree.Node;
import de.cebitec.readXplorer.util.polyTree.NodeVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Generates all translations possible for a given snp for the given genomic
 * features and a reference sequence. A translation is only generated if one of
 * the following holds: - The current feature has no subfeatures and the
 * position is not at a border while the current triplet violates the border. -
 * The current feature has subfeatures and the snp is located in such a
 * subfeature. - The snp is located in a subfeature at a border, but this is not
 * the last subfeature (depending on the strand) and the triplet can be
 * completed from the neighboring subfeature.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpTranslator {

    
    private final Preferences pref;
    private final String refSeq;
    private int refLength;
    private List<PersistantFeature> genomicFeatures;
    private GeneticCode code;
    private int index;
    private int subPos; //summed up bases in subfeatures up to the snp position regarding the strand of the feature
    private boolean posDirectAtLeftChromBorder;
    private boolean posAtLeftChromBorder;
    private boolean posAtRightChromBorder;
    private boolean posDirectAtRightChromBorder;
    private boolean posAtLeftSubBorder;
    private boolean posAtRightSubBorder;
    private boolean snpInSubfeature; //if not and we have subfeatures, then this snp will not be translated
    private PersistantFeature borderSubfeat;
    private PersistantFeature subfeatBefore; //only set if pos is at borders
    private PersistantFeature subfeatAfter;
    private boolean subFeatureFound;

    /**
     * Generates all translations possible for a given snp for the given genomic features
     * and a reference sequence. A translation is only generated if one of the following holds:
     * - The current feature has no subfeatures and the position is not at a border while the current
     * triplet violates the border.
     * - The current feature has subfeatures and the snp is located in such a subfeature.
     * - The snp is located in a subfeature at a border, but this is not the last subfeature
     * (depending on the strand) and the triplet can be completed from the neighboring subfeature.
     * @param genomicFeatures all features of the reference genome of the desired feature types
     * @param refSeq the reference sequence
     */
    public SnpTranslator(List<PersistantFeature> genomicFeatures, PersistantChromosome chromosome) {
        this.genomicFeatures = genomicFeatures;
        ChromosomeObserver chromObserver = new ChromosomeObserver();
        this.refSeq = chromosome.getSequence(chromObserver);
        this.refLength = refSeq.length();
        index = 0;
        this.pref = NbPreferences.forModule(Object.class);
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        code = genCodeFactory.getGeneticCodeById(Integer.valueOf(
                pref.get(Properties.SEL_GENETIC_CODE, Properties.STANDARD_CODE_INDEX)));
    }
    
    /**
     * @param position check, if this position is covered by at least one feature (gene) 
     * @return the list of features covering the given position
     */
    public List<PersistantFeature> checkCoveredByFeature(int position) {

        //find feature/s which cover current snp position
        List<PersistantFeature> featuresFound = new ArrayList<>();
        int stopIndex = index;
        boolean fstFoundFeat = true;

        while (index < this.genomicFeatures.size()) {
            
            PersistantFeature feature = this.genomicFeatures.get(index++);
            if (feature.getStart() <= position && feature.getStop() >= position) {
                //found hit, also try next index
                featuresFound.add(feature);
                if (fstFoundFeat) {
                    stopIndex = index - 1;
                    fstFoundFeat = false;
                }
            } else if (feature.getStop() < position) {
                //do nothing
            } else if (feature.getStop() > position && feature.getStart() > position) {
                break; //stop
            }
        }
        index = stopIndex < 0 ? 0 : stopIndex; //to always ensure not to forget about the last visited features
        
        return featuresFound;

    }

    /**
     * Generates all translations possible for a given snp for the given genomic features (genes)
     * and reference sequence (set in the constructor) and stores them in the given Snp object. 
     * A translation is only generated if one of the following holds:
     * - The current feature has no subfeatures and the position is not at a border while the current
     * triplet violates the border.
     * - The current feature has subfeatures and the snp is located in such a subfeature.
     * - The snp is located in a subfeature at a border, but this is not the last subfeature
     * (depending on the strand) and the triplet can be completed from the neighboring subfeature.
     * @param snp the snp object to check
     */
    public void checkForFeature(Snp snp) {

        //find feature/s which cover current snp position
        List<PersistantFeature> featuresFound = this.checkCoveredByFeature(snp.getPosition());

        //amino acid substitution calculations
        List<CodonSnp> codonSnpList = this.calcSnpList(featuresFound, snp);
        for (CodonSnp codon : codonSnpList) {
            snp.addCodon(codon);
        }
    }

    /**
     * Calculates the list of snp codons belonging to a single snp. This list is larger than one
     * if more than one features have been found at the "pos" position in the reference genome.
     * @param featuresFound list of features in the reference genome for current position "pos"
     * @param pos position of the current snp
     * @param refSeq whole reference genome sequence
     * @param snp complete snp object
     * @param code genetic code to retrieve the amino acid translation
     * @return list of CodonSnps for the current snp position "pos"
     */
    private List<CodonSnp> calcSnpList(List<PersistantFeature> featuresFound, Snp snp) {
        
        int pos = snp.getPosition();
        posDirectAtLeftChromBorder = pos < 2; //pos is never smaller than 1, 1 is min
        posAtLeftChromBorder = pos < 3; 
        posAtRightChromBorder = pos + 2 > this.refLength;
        posDirectAtRightChromBorder = pos + 1 > this.refLength;
                
        //handle feature knowledge:
        //get each strand and triplet for correct reading frame for translation
        List<CodonSnp> codonSnpList = new ArrayList<>();
        for (PersistantFeature feature : featuresFound) {
            int mod; //fwdStrand: 1 = left base, 2 = middle base, 0 = right base / revStrand: 1 = left base, 2 = middle base, 0 = right base
            String tripletRef = "";
            String tripletSnp = "";
            subPos = 0;
            
            /* 
             * Check for subfeatures and calculate length of spliced mRNA and snp position on this mRNA.
             * Also need to check, if the current position is at a border of its subfeature. Then we need
             * the neighboring subfeatures for the refrence sequence of the translation triplet and also
             * the distance along all subfeatures up to our snp position.
             */
            boolean fwdStrand = feature.isFwdStrand();
            
            if (       feature.getType() == FeatureType.GENE || feature.getType() == FeatureType.MRNA
                    || feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA) {
                this.calcFeatureData(feature, pos, FeatureType.CDS);
                if (!subFeatureFound) {
                    this.calcFeatureData(feature, pos, FeatureType.EXON);
                }
            }
            if (!subFeatureFound && feature.getType() == FeatureType.GENE) {
                this.calcFeatureData(feature, pos, FeatureType.MRNA);
                if (!subFeatureFound) { //if the gene has a rRNA or tRNA instead of an mRNA, we have to check this, too
                    this.calcFeatureData(feature, pos, FeatureType.RRNA);
                }
                if (!subFeatureFound) {
                    this.calcFeatureData(feature, pos, FeatureType.TRNA);
                }
            }
            
            if (subFeatureFound && !snpInSubfeature) {
                continue; // we have subfeatures, but the snp is not in them, so we do not translate it!
            }

            try { //we need to catch, if any of the positions is out of bounds!

                if (subPos <= 0 || borderSubfeat == null) { //there are no subfeatures, or pos is not at border in subfeature

                    int featureStartOnStrand = fwdStrand ? feature.getStart() : feature.getStop();
                    
                    if (subPos == 0) { //feature without subfeatures
                        mod = (Math.abs(pos - featureStartOnStrand) + 1) % 3;
                    } else { //feature with subfeatures, we just have to get the mod
                        mod = subPos % 3;
                    }

                    if (!posAtRightChromBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0)) { //left base of triplet, get pos to pos+2
                        tripletRef = refSeq.substring(pos - 1, pos + 2);
                        tripletSnp = snp.getBase().toLowerCase().concat(tripletRef.substring(1));
                    } else if (mod == 2 && !posDirectAtLeftChromBorder && !posDirectAtRightChromBorder) { //middle base of triplet, get pos-1, pos and pos+1
                        tripletRef = refSeq.substring(pos - 2, pos + 1);
                        tripletSnp = tripletRef.charAt(0) + snp.getBase().toLowerCase() + tripletRef.charAt(2);
                    } else if (!posAtLeftChromBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1)) { //right base of triplet, get pos-2 to pos
                        tripletRef = refSeq.substring(pos - 3, pos);
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase().toLowerCase());
                    }
                } else { //snp is located in a subfeature (exon) and at a border of a subfeature

                    mod = subPos % 3;
                    boolean posDirectAtLeftSubBorder = pos - 1 < borderSubfeat.getStart();
                    boolean posDirectAtRightSubBorder = pos + 1 > borderSubfeat.getStop();

                    if (!posAtRightChromBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0)) { //left (fwd) or right (rev) base of triplet, get pos to pos+2

                        if (posAtRightSubBorder) {
                            if (posDirectAtRightSubBorder) { //get only last base from current subfeature and two from next subfeature
                                tripletRef = refSeq.substring(pos - 1, pos) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart() + 1);
                            } else { //get last two bases from current subfeature and first base of next subfeature
                                tripletRef = refSeq.substring(pos - 1, pos + 1) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart());
                            }
                        } else {
                            tripletRef = refSeq.substring(pos - 1, pos + 2);
                        }
                        tripletSnp = snp.getBase().toLowerCase().concat(tripletRef.substring(1));

                    } else if (mod == 2 && !posDirectAtLeftChromBorder && !posDirectAtRightChromBorder) { //middle base of triplet, get pos-1, pos and pos+1

                        if (posDirectAtLeftSubBorder) { //get one base from left subfeature and one from right subfeature, adding last base later
                            tripletRef = refSeq.substring(subfeatBefore.getStop() - 1, subfeatBefore.getStop()) + refSeq.substring(pos - 1, pos);
                        } else {
                            tripletRef = refSeq.substring(pos - 2, pos);
                        }
                        if (posDirectAtRightSubBorder) {
                            tripletRef += refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart());
                        } else {
                            tripletRef += refSeq.substring(pos, pos + 1);
                        }
                        tripletSnp = tripletRef.charAt(0) + snp.getBase().toLowerCase() + tripletRef.charAt(2);

                    } else if (!posAtLeftChromBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1)) { //right base of triplet, get pos-2 to pos

                        if (posAtLeftSubBorder) {
                            if (posDirectAtLeftSubBorder) { //get both left bases from other subfeature
                                tripletRef = refSeq.substring(subfeatBefore.getStop() - 2, subfeatBefore.getStop()) + refSeq.substring(pos - 1, pos);
                            } else { //get last base from feature before
                                tripletRef = refSeq.substring(subfeatBefore.getStop() - 1, subfeatBefore.getStop()) + refSeq.substring(pos - 2, pos);
                            }
                        } else {
                            tripletRef = refSeq.substring(pos - 3, pos);
                        }
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase().toLowerCase());
                    }
                }

                if (!fwdStrand) {
                    tripletRef = SequenceUtils.getReverseComplement(tripletRef);
                    tripletSnp = SequenceUtils.getReverseComplement(tripletSnp);
                }
            } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
                continue;
            }

            //translate string to amino acid and store reference and snp codon
            try {
                char aminoRef = this.code.getTranslation(tripletRef);
                char aminoSnp = this.code.getTranslation(tripletSnp);

                //determine effect type of snp on the amino acid sequence
                SequenceComparison type = aminoRef == aminoSnp ? SequenceComparison.MATCH : SequenceComparison.SUBSTITUTION;
                if (type == SequenceComparison.SUBSTITUTION) {
                    if (AminoAcidProperties.getPropertyForAA(aminoRef).equals(AminoAcidProperties.getPropertyForAA(aminoSnp))) {
                        type = SequenceComparison.NEUTRAL;
                    } else {
                        type = SequenceComparison.MISSENSE;
                    }
                }

                codonSnpList.add(new CodonSnp(tripletRef, tripletSnp, aminoRef, aminoSnp, type, feature));
            } catch (NullPointerException e) {
                continue; //ignore translations with N's or gaps
            }
        }
        return codonSnpList;
    }
    
    /**
     * Calculates the feature type length of the given feature type for all
     * features downward in the feature tree hierarchy (top-down fashion).
     * @param feature the feature whose subfeature length and readcount is needed
     * @param type the feature type for which the length sum and total read count
     * is needed
     */
    private void calcFeatureData(final PersistantFeature feature, final int pos, final FeatureType type) {
        //calc length of all exons of the mRNA and number of reads mapped to exon regions
        subfeatAfter = null;
        subfeatBefore = null;
        snpInSubfeature = false;
        subPos = 0;
        subFeatureFound = false;
        feature.topDown(new NodeVisitor() {
            
            @Override
            public void visit(Node node) {
                
                PersistantFeature subFeature = (PersistantFeature) node;
                final boolean fwdStrand = feature.isFwdStrand();
                if (subFeature.getType() == type) {
                    subFeatureFound = true;
                    int featureStartOnStrand = fwdStrand ? subFeature.getStart() : subFeature.getStop();
                    int featureStart = subFeature.getStart();
                    
                    if (subFeature.getStop() >= pos && featureStart <= pos) {
                        subPos += (Math.abs(pos - featureStartOnStrand) + 1);
                        //only set subfeatAtPos, if position is at a border of the subFeature
                        posAtLeftSubBorder = pos - 2 < featureStart;
                        posAtRightSubBorder = pos + 2 > subFeature.getStop();
                        borderSubfeat = !posAtLeftSubBorder && !posAtRightSubBorder ? null : subFeature;
                        snpInSubfeature = true;
                    } else if (featureStart < pos) {
                        //get distance in feature and left neighbor subFeature of subFeature with position
                        if (fwdStrand) { subPos += (subFeature.getStop() - (featureStart - 1)); }
                        if (subfeatBefore == null || subfeatBefore.getStart() < subFeature.getStart()) {
                            subfeatBefore = subFeature;
                        }
                    } else if (featureStart > pos) {
                        if (!fwdStrand) { subPos += (featureStartOnStrand - (subFeature.getStart() - 1)); }
                        //get right neighbor subFeature of subFeature with position
                        if (subfeatAfter == null || subfeatAfter.getStart() > subFeature.getStart()) {
                            subfeatAfter = subFeature;
                        }
                    }
                }
            }
        });
    }
}
