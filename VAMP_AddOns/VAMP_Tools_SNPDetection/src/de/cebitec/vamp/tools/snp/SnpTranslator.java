package de.cebitec.vamp.tools.snp;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.common.sequencetools.GeneticCode;
import de.cebitec.common.sequencetools.GeneticCodeFactory;
import de.cebitec.vamp.databackend.dataObjects.CodonSnp;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSubFeature;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.util.PositionUtils;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceComparison;
import de.cebitec.vamp.util.SequenceUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.openide.util.NbPreferences;

/**
 * @author rhilker
 * 
 * Generates all translations possible for a given snp for the given genomic features
 * and a reference sequence. A translation is only generated if one of the following holds:
 * - The current feature has no subfeatures and the position is not at a border while the current
 * triplet violates the border.
 * - The current feature has subfeatures and the snp is located in such a subfeature.
 * - The snp is located in a subfeature at a border, but this is not the last subfeature
 * (depending on the strand) and the triplet can be completed from the neighboring subfeature.
 */
public class SnpTranslator {

    
    private final Preferences pref;
    private final String refSeq;
    private int refLength;
    private final List<PersistantFeature> genomicFeatures;
    private GeneticCode code;
    private int lastIndex;
    private int lastPos;
    private int pos;

    /**
     * Generates all translations possible for a given snp for the given genomic features
     * and a reference sequence. A translation is only generated if one of the following holds:
     * - The current feature has no subfeatures and the position is not at a border while the current
     * triplet violates the border.
     * - The current feature has subfeatures and the snp is located in such a subfeature.
     * - The snp is located in a subfeature at a border, but this is not the last subfeature
     * (depending on the strand) and the triplet can be completed from the neighboring subfeature.
     * @param genomicFeatures all features of the reference genome
     * @param refSeq the reference sequence
     */
    public SnpTranslator(List<PersistantFeature> genomicFeatures, PersistantReference reference) {
        this.genomicFeatures = genomicFeatures;
        this.refSeq = reference.getSequence();
        this.refLength = reference.getRefLength();
        lastIndex = 0;
        lastPos = Integer.MAX_VALUE;
        pos = -1;
        this.pref = NbPreferences.forModule(Object.class);
        try {
            GeneticCodeFactory.initGeneticCodes();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        code = GeneticCodeFactory.getGeneticCodeById(Integer.valueOf( //init for later use
                pref.get(Properties.SEL_GENETIC_CODE, Properties.STANDARD_CODE_INDEX)));
    }
    
    /**
     * @param position check, if this position is covered by at least one feature (gene) 
     * @return the list of features covering the given position
     */
    public List<PersistantFeature> checkCoveredByFeature(String position) {

        //find feature/s which cover current snp position
        List<PersistantFeature> featuresFound = new ArrayList<>();

        pos = PositionUtils.convertPosition(position);
        if (lastPos > pos) {
            lastIndex = 0;
        } //since positions in table cannot be sorted completely, because they are strings

        while (lastIndex < this.genomicFeatures.size()) {

            PersistantFeature feature = this.genomicFeatures.get(lastIndex++);
            if (feature.getStart() <= pos && feature.getStop() >= pos) {
                //found hit, also try next index
                featuresFound.add(feature);
            } else if (feature.getStop() < pos) {
                //do nothing
            } else { //for your information: if (feature.getStop() > pos && feature.getStart() > pos){
                lastIndex -= featuresFound.size() + 1;
                break; //stop
            }
        }
        
        lastPos = pos;
        
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
        List<PersistantFeature> featuresFound = new ArrayList<>();
//        List<PersistantSubfeature> subfeaturesFound = new ArrayList<PersistantSubfeature>();

        this.pos = PositionUtils.convertPosition(snp.getPosition());
        this.lastIndex = this.lastPos > this.pos ? 0 : this.lastIndex;
        //since positions in table are sorted alphabetically, because they are strings

        while (lastIndex < this.genomicFeatures.size()) {

            PersistantFeature feature = this.genomicFeatures.get(this.lastIndex++);
            if (feature.getStart() <= pos && feature.getStop() >= pos) {
                //found hit, also try next index
                featuresFound.add(feature);
            } else if (feature.getStart() < pos) {
                //do nothing
            } else { //for your information: if (feature.getStop() > pos && feature.getStart() > pos){
                this.lastIndex -= (featuresFound.size() + 1);
                break; //stop
            }
        }
        lastIndex -= (featuresFound.size() + 1);
        lastIndex = lastIndex < 0 ? 0 : lastIndex; //to always ensure not to forget about the last visited features

        //amino acid substitution calculations
        List<CodonSnp> codonSnpList = this.calcSnpList(featuresFound, snp);
        for (CodonSnp codon : codonSnpList) {
            snp.addCodon(codon);
        }
        
        lastPos = pos;
    }

    /**
     * Checks if the position of the handed in snp is covered by a subfeature (exon)
     * and returns the subfeature.
     * @param feature the feature whose subfeatures are checked
     * @param snp the snp object to check
     * @return the subfeature covering the current position or null, if no subfeature covers
     *          this position
     */
    private PersistantSubFeature checkForSubfeature(PersistantFeature feature, int pos) {
        for (PersistantSubFeature subfeature : feature.getSubFeatures()){
            if (subfeature.getStart() <= pos && subfeature.getStop() >= pos) {
                //found hit
                return subfeature;
            }
        }
        return null;
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
        
        boolean posDirectAtLeftBorder = this.pos < 2; //pos is never smaller than 1, 1 is min
        boolean posAtLeftBorder = this.pos < 3; 
        boolean posAtRightBorder = this.pos + 2 > this.refLength;
        boolean posDirectAtRightBorder = this.pos + 1 > this.refLength;
                
        //handle feature knowledge:
        //get each strand and triplet for correct reading frame for translation
        List<CodonSnp> codonSnpList = new ArrayList<>();
        for (PersistantFeature feature : featuresFound) {
            int featureStart;
            int mod;
            String tripletRef = "";
            String tripletSnp = "";
            int subPos = 0;
            
            /* 
             * Check for subfeatures and calculate length of spliced mRNA and snp position on this mRNA.
             * Also need to check, if the current position is at a border of its subfeature. Then we need
             * the neighboring subfeatures for the refrence sequence of the translation triplet and also
             * the distance along all subfeatures up to our snp position.
             */
            boolean fwdStrand = feature.isFwdStrand();
            boolean posAtLeftSubBorder = false;
            boolean posAtRightSubBorder = false;
            boolean snpInSubfeature = false; //if not and we have subfeatures, then this snp will not be translated
            PersistantSubFeature subfeatBefore = null;
            PersistantSubFeature borderSubfeat = null; //only set if pos is at borders
            PersistantSubFeature subfeatAfter = null;
            for (PersistantSubFeature subfeature : feature.getSubFeatures()) {
                    
                int featureStartOnStrand = fwdStrand ? subfeature.getStart() : subfeature.getStop();
                featureStart = subfeature.getStart();

                if (subfeature.getStop() >= this.pos && featureStart <= this.pos) {
                    subPos += (Math.abs(this.pos - featureStartOnStrand)+1);
                    //only set subfeatAtPos, if position is at a border of the subfeature
                    posAtLeftSubBorder = this.pos - 2 < featureStart;
                    posAtRightSubBorder = this.pos + 2 > subfeature.getStop();
                    borderSubfeat = !posAtLeftSubBorder && !posAtRightSubBorder ? null : subfeature;
                    snpInSubfeature = true;
                } else if (featureStart < this.pos) {
                    //get distance in feature and left neighbor subfeature of subfeature with position
                    if (fwdStrand) { subPos += (subfeature.getStop() - (featureStart-1)); }
                    if (subfeatBefore == null || subfeatBefore.getStart() < subfeature.getStart()) {
                        subfeatBefore = subfeature;
                    }
                } else if (featureStart > this.pos) {
                    if (!fwdStrand) { subPos += (featureStartOnStrand - (subfeature.getStart()-1)); }
                    //get right neighbor subfeature of subfeature with position
                    if (subfeatAfter == null || subfeatAfter.getStart() > subfeature.getStart()) {
                        subfeatAfter = subfeature;
                    }
                }
            }
            
            if (!feature.getSubFeatures().isEmpty() && !snpInSubfeature){ 
                continue; // we have subfeatures, but the snp is not in them, so we skip it!
            }
           
            try { //we need to catch, if any of the positions is out of bounds!

                if (subPos <= 1 || borderSubfeat == null) { //there are no subfeatures, or pos is not at border in subfeature

                    int featureStartOnStrand = fwdStrand ? feature.getStart() : feature.getStop();
                    
                    if (subPos == 0) {
                        mod = (this.pos - featureStartOnStrand+1) % 3;
                    } else {
                        mod = subPos % 3;
                    }

                    if (!posAtRightBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0)) { //left base of triplet, get pos to pos+2
                        tripletRef = refSeq.substring(this.pos - 1, this.pos + 2);
                        tripletSnp = snp.getBase().concat(tripletRef.substring(1));
                    } else if (mod == 2 && !posDirectAtLeftBorder && !posDirectAtRightBorder) { //middle base of triplet, get pos-1, pos and pos+1
                        tripletRef = refSeq.substring(this.pos - 2, this.pos + 1);
                        tripletSnp = tripletRef.charAt(0) + snp.getBase() + tripletRef.charAt(2);
                    } else if (!posAtLeftBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1)) { //right base of triplet, get pos-2 to pos
                        tripletRef = refSeq.substring(this.pos - 3, this.pos);
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase());
                    }
                } else { //snp is located in a subfeature (exon) and at a border of a subfeature

                    mod = subPos % 3;
                    boolean posDirectAtLeftSubBorder = this.pos - 1 < borderSubfeat.getStart();
                    boolean posDirectAtRightSubBorder = this.pos + 1 > borderSubfeat.getStop();

                    if (!posAtRightBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0)) { //left base of triplet, get pos to pos+2

                        if (posAtRightSubBorder) {
                            if (posDirectAtRightSubBorder) { //get only last base from other subfeature
                                tripletRef = refSeq.substring(pos - 1, pos) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart() + 1);
                            } else { //get last two bases from other feature
                                tripletRef = refSeq.substring(pos - 1, pos + 1) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart());
                            }
                        } else {
                            tripletRef = refSeq.substring(this.pos - 1, this.pos + 2);
                        }
                        tripletSnp = snp.getBase().concat(tripletRef.substring(1));

                    } else if (mod == 2 && !posDirectAtLeftBorder && !posDirectAtRightBorder) { //middle base of triplet, get pos-1, pos and pos+1

                        if (posDirectAtLeftSubBorder) { //get one base from left subfeature and one from right subfeature
                            tripletRef = refSeq.substring(subfeatBefore.getStop() - 1, subfeatBefore.getStop()) + refSeq.substring(pos - 1, pos);
                        } else {
                            tripletRef = refSeq.substring(this.pos - 2, this.pos);
                        }
                        if (posDirectAtRightSubBorder) {
                            tripletRef += refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart());
                        } else {
                            tripletRef += refSeq.substring(this.pos, this.pos + 1);
                        }
                        tripletSnp = tripletRef.charAt(0) + snp.getBase() + tripletRef.charAt(2);

                    } else if (!posAtLeftBorder && (fwdStrand && mod == 0 || !fwdStrand && mod == 1)) { //right base of triplet, get pos-2 to pos

                        if (posAtLeftSubBorder) {
                            if (posDirectAtLeftSubBorder) { //get both left bases from other subfeature
                                tripletRef = refSeq.substring(subfeatBefore.getStop() - 2, subfeatBefore.getStop()) + refSeq.substring(pos - 1, pos);
                            } else { //get last base from other feature
                                tripletRef = refSeq.substring(subfeatBefore.getStop() - 1, subfeatBefore.getStop()) + refSeq.substring(pos - 2, pos);
                            }
                        } else {
                            tripletRef = refSeq.substring(this.pos - 3, this.pos);
                        }
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase());
                    }
                }

                if (!fwdStrand) {
                    tripletRef = SequenceUtils.getReverseComplement(tripletRef);
                    tripletSnp = SequenceUtils.getReverseComplement(tripletSnp);
                }
            } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
                continue;
            }

            //get feature id
            String id = feature.toString();

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

                codonSnpList.add(new CodonSnp(tripletRef, tripletSnp, aminoRef, aminoSnp, type, id));
            } catch (NullPointerException e) {
                continue; //ignore translations with N's or gaps
            }
        }
        return codonSnpList;
    }
}
