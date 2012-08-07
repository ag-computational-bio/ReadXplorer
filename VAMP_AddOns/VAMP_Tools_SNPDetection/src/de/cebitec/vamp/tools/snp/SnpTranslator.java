package de.cebitec.vamp.tools.snp;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.common.sequencetools.GeneticCode;
import de.cebitec.common.sequencetools.GeneticCodeFactory;
import de.cebitec.vamp.databackend.dataObjects.CodonSnp;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSubAnnotation;
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
 * Generates all translations possible for a given snp for the given genomic annotations
 * and a reference sequence. A translation is only generated if one of the following holds:
 * - The current annotation has no subannotations and the position is not at a border while the current
 * triplet violates the border.
 * - The current annotation has subannotations and the snp is located in such a subannotation.
 * - The snp is located in a subannotation at a border, but this is not the last subannotation
 * (depending on the strand) and the triplet can be completed from the neighboring subannotation.
 */
public class SnpTranslator {

    
    private final Preferences pref;
    private final String refSeq;
    private int refLength;
    private final List<PersistantAnnotation> genomicAnnotations;
    private GeneticCode code;
    private int lastIndex;
    private int lastPos;
    private int pos;

    /**
     * Generates all translations possible for a given snp for the given genomic annotations
     * and a reference sequence. A translation is only generated if one of the following holds:
     * - The current annotation has no subannotations and the position is not at a border while the current
     * triplet violates the border.
     * - The current annotation has subannotations and the snp is located in such a subannotation.
     * - The snp is located in a subannotation at a border, but this is not the last subannotation
     * (depending on the strand) and the triplet can be completed from the neighboring subannotation.
     * @param genomicAnnotations all annotations of the reference genome
     * @param refSeq the reference sequence
     */
    public SnpTranslator(List<PersistantAnnotation> genomicAnnotations, PersistantReference reference) {
        this.genomicAnnotations = genomicAnnotations;
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
     * @param position check, if this position is covered by at least one annotation (gene) 
     * @return the list of annotations covering the given position
     */
    public List<PersistantAnnotation> checkCoveredByAnnotation(String position) {

        //find annotation/s which cover current snp position
        List<PersistantAnnotation> annotationsFound = new ArrayList<PersistantAnnotation>();

        pos = PositionUtils.convertPosition(position);
        if (lastPos > pos) {
            lastIndex = 0;
        } //since positions in table cannot be sorted completely, because they are strings

        while (lastIndex < this.genomicAnnotations.size()) {

            PersistantAnnotation annotation = this.genomicAnnotations.get(lastIndex++);
            if (annotation.getStart() <= pos && annotation.getStop() >= pos) {
                //found hit, also try next index
                annotationsFound.add(annotation);
            } else if (annotation.getStop() < pos) {
                //do nothing
            } else { //for your information: if (annotation.getStop() > pos && annotation.getStart() > pos){
                lastIndex -= annotationsFound.size() + 1;
                break; //stop
            }
        }
        
        lastPos = pos;
        
        return annotationsFound;

    }

    /**
     * Generates all translations possible for a given snp for the given genomic annotations (genes)
     * and reference sequence (set in the constructor) and stores them in the given Snp object. 
     * A translation is only generated if one of the following holds:
     * - The current annotation has no subannotations and the position is not at a border while the current
     * triplet violates the border.
     * - The current annotation has subannotations and the snp is located in such a subannotation.
     * - The snp is located in a subannotation at a border, but this is not the last subannotation
     * (depending on the strand) and the triplet can be completed from the neighboring subannotation.
     * @param snp the snp object to check
     */
    public void checkForAnnotation(Snp snp) {

        //find annotation/s which cover current snp position
        List<PersistantAnnotation> annotationsFound = new ArrayList<PersistantAnnotation>();
//        List<PersistantSubannotation> subannotationsFound = new ArrayList<PersistantSubannotation>();

        this.pos = PositionUtils.convertPosition(snp.getPosition());
        this.lastIndex = this.lastPos > this.pos ? 0 : this.lastIndex;
        //since positions in table are sorted alphabetically, because they are strings

        while (lastIndex < this.genomicAnnotations.size()) {

            PersistantAnnotation annotation = this.genomicAnnotations.get(this.lastIndex++);
            if (annotation.getStart() <= pos && annotation.getStop() >= pos) {
                //found hit, also try next index
                annotationsFound.add(annotation);
            } else if (annotation.getStart() < pos) {
                //do nothing
            } else { //for your information: if (annotation.getStop() > pos && annotation.getStart() > pos){
                this.lastIndex -= (annotationsFound.size() + 1);
                break; //stop
            }
        }
        lastIndex -= (annotationsFound.size() + 1);
        lastIndex = lastIndex < 0 ? 0 : lastIndex; //to always ensure not to forget about the last visited annotations

        //amino acid substitution calculations
        List<CodonSnp> codonSnpList = this.calcSnpList(annotationsFound, snp);
        for (CodonSnp codon : codonSnpList) {
            snp.addCodon(codon);
        }
        
        lastPos = pos;
    }

    /**
     * Checks if the position of the handed in snp is covered by a subannotation (exon)
     * and returns the subannotation.
     * @param annotation the annotation whose subannotations are checked
     * @param snp the snp object to check
     * @return the subannotation covering the current position or null, if no subannotation covers
     *          this position
     */
    private PersistantSubAnnotation checkForSubannotation(PersistantAnnotation annotation, int pos) {
        for (PersistantSubAnnotation subannotation : annotation.getSubAnnotations()){
            if (subannotation.getStart() <= pos && subannotation.getStop() >= pos) {
                //found hit
                return subannotation;
            }
        }
        return null;
    }

    /**
     * Calculates the list of snp codons belonging to a single snp. This list is larger than one
     * if more than one annotations have been found at the "pos" position in the reference genome.
     * @param annotationsFound list of annotations in the reference genome for current position "pos"
     * @param pos position of the current snp
     * @param refSeq whole reference genome sequence
     * @param snp complete snp object
     * @param code genetic code to retrieve the amino acid translation
     * @return list of CodonSnps for the current snp position "pos"
     */
    private List<CodonSnp> calcSnpList(List<PersistantAnnotation> annotationsFound, Snp snp) {
        
        boolean posDirectAtLeftBorder = this.pos < 2; //pos is never smaller than 1, 1 is min
        boolean posAtLeftBorder = this.pos < 3; 
        boolean posAtRightBorder = this.pos + 2 > this.refLength;
        boolean posDirectAtRightBorder = this.pos + 1 > this.refLength;
                
        //handle annotation knowledge:
        //get each strand and triplet for correct reading frame for translation
        List<CodonSnp> codonSnpList = new ArrayList<CodonSnp>();
        for (PersistantAnnotation annotation : annotationsFound) {
            int annotationStart;
            int mod;
            String tripletRef = "";
            String tripletSnp = "";
            int subPos = 0;
            
            /* 
             * Check for subannotations and calculate length of spliced mRNA and snp position on this mRNA.
             * Also need to check, if the current position is at a border of its subannotation. Then we need
             * the neighboring subannotations for the refrence sequence of the translation triplet and also
             * the distance along all subannotations up to our snp position.
             */
            boolean fwdStrand = annotation.isFwdStrand();
            boolean posAtLeftSubBorder = false;
            boolean posAtRightSubBorder = false;
            boolean snpInSubannotation = false; //if not and we have subannotations, then this snp will not be translated
            PersistantSubAnnotation subfeatBefore = null;
            PersistantSubAnnotation borderSubfeat = null; //only set if pos is at borders
            PersistantSubAnnotation subfeatAfter = null;
            for (PersistantSubAnnotation subannotation : annotation.getSubAnnotations()) {
                    
                int annotationStartOnStrand = fwdStrand ? subannotation.getStart() : subannotation.getStop();
                annotationStart = subannotation.getStart();

                if (subannotation.getStop() >= this.pos && annotationStart <= this.pos) {
                    subPos += (Math.abs(this.pos - annotationStartOnStrand)+1);
                    //only set subfeatAtPos, if position is at a border of the subannotation
                    posAtLeftSubBorder = this.pos - 2 < annotationStart;
                    posAtRightSubBorder = this.pos + 2 > subannotation.getStop();
                    borderSubfeat = !posAtLeftSubBorder && !posAtRightSubBorder ? null : subannotation;
                    snpInSubannotation = true;
                } else if (annotationStart < this.pos) {
                    //get distance in annotation and left neighbor subannotation of subannotation with position
                    if (fwdStrand) { subPos += (subannotation.getStop() - (annotationStart-1)); }
                    if (subfeatBefore == null || subfeatBefore.getStart() < subannotation.getStart()) {
                        subfeatBefore = subannotation;
                    }
                } else if (annotationStart > this.pos) {
                    if (!fwdStrand) { subPos += (annotationStartOnStrand - (subannotation.getStart()-1)); }
                    //get right neighbor subannotation of subannotation with position
                    if (subfeatAfter == null || subfeatAfter.getStart() > subannotation.getStart()) {
                        subfeatAfter = subannotation;
                    }
                }
            }
            
            if (!annotation.getSubAnnotations().isEmpty() && !snpInSubannotation){ 
                continue; // we have subannotations, but the snp is not in them, so we skip it!
            }
           
            try { //we need to catch, if any of the positions is out of bounds!

                if (subPos <= 1 || borderSubfeat == null) { //there are no subannotations, or pos is not at border in subannotation

                    int annotationStartOnStrand = fwdStrand ? annotation.getStart() : annotation.getStop();
                    
                    if (subPos == 0) {
                        mod = (this.pos - annotationStartOnStrand+1) % 3;
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
                } else { //snp is located in a subannotation (exon) and at a border of a subannotation

                    mod = subPos % 3;
                    boolean posDirectAtLeftSubBorder = this.pos - 1 < borderSubfeat.getStart();
                    boolean posDirectAtRightSubBorder = this.pos + 1 > borderSubfeat.getStop();

                    if (!posAtRightBorder && (fwdStrand && mod == 1 || !fwdStrand && mod == 0)) { //left base of triplet, get pos to pos+2

                        if (posAtRightSubBorder) {
                            if (posDirectAtRightSubBorder) { //get only last base from other subannotation
                                tripletRef = refSeq.substring(pos - 1, pos) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart() + 1);
                            } else { //get last two bases from other annotation
                                tripletRef = refSeq.substring(pos - 1, pos + 1) + refSeq.substring(subfeatAfter.getStart() - 1, subfeatAfter.getStart());
                            }
                        } else {
                            tripletRef = refSeq.substring(this.pos - 1, this.pos + 2);
                        }
                        tripletSnp = snp.getBase().concat(tripletRef.substring(1));

                    } else if (mod == 2 && !posDirectAtLeftBorder && !posDirectAtRightBorder) { //middle base of triplet, get pos-1, pos and pos+1

                        if (posDirectAtLeftSubBorder) { //get one base from left subannotation and one from right subannotation
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
                            if (posDirectAtLeftSubBorder) { //get both left bases from other subannotation
                                tripletRef = refSeq.substring(subfeatBefore.getStop() - 2, subfeatBefore.getStop()) + refSeq.substring(pos - 1, pos);
                            } else { //get last base from other annotation
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
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            } catch (NullPointerException e){
                continue;
            }

            //get annotation id
            String id = annotation.hasGeneName() ? annotation.getGeneName() : annotation.getLocus();

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
