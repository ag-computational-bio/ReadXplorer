package de.cebitec.vamp.tools.snp;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.common.sequencetools.GeneticCode;
import de.cebitec.common.sequencetools.GeneticCodeFactory;
import de.cebitec.vamp.databackend.dataObjects.CodonSnp;
import de.cebitec.vamp.databackend.dataObjects.Snp;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
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
 * 
 */
public class SnpTranslator {

    
    private final Preferences pref;
    private final String refSeq;
    private final List<PersistantFeature> genomicFeatures;
    private GeneticCode code;
    private int lastIndex;
    private int lastPos;
    private int pos;

    public SnpTranslator(List<PersistantFeature> genomicFeatures, String refSeq) {
        this.genomicFeatures = genomicFeatures;
        this.refSeq = refSeq;
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
     */
    public List<PersistantFeature> checkCoveredByFeature(String position) {

        //find feature/s which cover current snp position
        List<PersistantFeature> featuresFound = new ArrayList<PersistantFeature>();

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
     * Checks if the position of the handed in snp is covered by features (genes)
     * and stores the feature list in the snp object.
     * @param snp the snp object to check
     */
    public void checkForFeature(Snp snp) {

        //find feature/s which cover current snp position
        List<PersistantFeature> featuresFound = new ArrayList<PersistantFeature>();

        pos = PositionUtils.convertPosition(snp.getPosition());
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

        //amino acid substitution calculations
        List<CodonSnp> codonSnpList = this.calcSnpList(featuresFound, pos, refSeq, snp, code);
        for (CodonSnp codon : codonSnpList) {
            snp.addCodon(codon);
        }
        
        lastPos = pos;
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
    private List<CodonSnp> calcSnpList(List<PersistantFeature> featuresFound, int pos, String refSeq, Snp snp, GeneticCode code) {
        
        boolean border1 = pos > refSeq.length() - 2;
        boolean border2 = pos < 2; //pos is never smaller than 1, 1 is min
        boolean border3 = pos > refSeq.length() - 1;
        boolean border4 = pos < 3; 
                
        //handle feature knowledge:
        //get each strand and triplet for correct reading frame for translation
        List<CodonSnp> codonSnpList = new ArrayList<CodonSnp>();
        for (PersistantFeature feature : featuresFound) {
            int featureStart;
            int mod;
            String tripletRef = "";
            String tripletSnp = "";

            try { //we need to catch, if any of the positions is out of bounds!

                if (feature.getStrand() == SequenceUtils.STRAND_FWD) {

                    featureStart = feature.getStart();
                    mod = (pos - featureStart) % 3;
                    if (mod == 0 && !border1) { //left base of triplet, get pos to pos+2
                        tripletRef = refSeq.substring(pos - 1, pos + 2);
                        tripletSnp = snp.getBase().concat(tripletRef.substring(1));
                    } else if (mod == 1 && !border2 && !border3) { //middle base of triplet, get pos-1, pos and pos+1
                        tripletRef = refSeq.substring(pos - 2, pos + 1);
                        tripletSnp = tripletRef.charAt(0) + snp.getBase() + tripletRef.charAt(2);
                    } else if (mod == 2 && !border4) { //right base of triplet, get pos-2 to pos
                        tripletRef = refSeq.substring(pos - 3, pos);
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase());
                    }
                } else {

                    featureStart = feature.getStop();
                    mod = (featureStart - pos) % 3; // 35-30 = 5%3=2=border -> ATGGGG = correct
                    if (mod == 0 && !border4) { //right base of triplet, get pos-2 to pos
                        tripletRef = refSeq.substring(pos - 3, pos);
                        tripletSnp = tripletRef.substring(0, 2).concat(snp.getBase());
                    } else if (mod == 1 && !border2 && !border3) { //middle base of triplet, get pos-1, pos and pos+1
                        tripletRef = refSeq.substring(pos - 2, pos + 1);
                        tripletSnp = tripletRef.charAt(0) + snp.getBase() + tripletRef.charAt(2);
                    } else if (mod == 2 && !border1) { //left base of triplet, get pos to pos+2
                        tripletRef = refSeq.substring(pos - 1, pos + 2);
                        tripletSnp = snp.getBase().concat(tripletRef.substring(1));
                    }

                    tripletRef = SequenceUtils.getReverseComplement(tripletRef);
                    tripletSnp = SequenceUtils.getReverseComplement(tripletSnp);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }

            //get feature id
            String id = feature.hasGeneName() ? feature.getGeneName() : feature.getLocus();

            //translate string to amino acid and store reference and snp codon
            char aminoRef = code.getTranslation(tripletRef);
            char aminoSnp = code.getTranslation(tripletSnp);
            
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
        }
        return codonSnpList;
    }
}
