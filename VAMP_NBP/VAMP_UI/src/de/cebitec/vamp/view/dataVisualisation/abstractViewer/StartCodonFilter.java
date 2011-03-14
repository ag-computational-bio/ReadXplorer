package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.GeneticCodesStore;
import de.cebitec.vamp.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbPreferences;

/**
 * Filters for startcodons in two ways: First for all
 * available startcodons in a specified region and second
 * for all startcodons of a given frame for a specified region.
 *
 * @author ddoppmeier, rhilker
 */
public class StartCodonFilter implements RegionFilterI {

    public static final int INIT = 10;

    private List<Region> regions;
    private int absStart;
    private int absStop;
    private PersistantReference refGen;
    private String sequence;
    private ArrayList<Boolean> selectedCodons;
    private Pattern[] startCodons;
    private int frameCurrFeature;

    public StartCodonFilter(int absStart, int absStop, PersistantReference refGen){
        this.regions = new ArrayList<Region>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;

        this.resetStartCodons();

        this.frameCurrFeature = StartCodonFilter.INIT; //because this is not a frame value
    }

    /**
     * Searches and identifies start codons and saves their position
     * in this class' region list.
     */
    private void findStartCodons(){
        regions.clear();

        if(this.atLeastOneCodonSelected()){
            // extends intervall to search to the left and right,
            // to find start/stop codons that overlap this interalls boundaries
            int offset = 3;
            int start = absStart - offset;
            int stop = absStop+2;

            if(start < 0 ){
                offset -= Math.abs(start);
                start = 0;
            }
            if(stop > refGen.getSequence().length()){
                stop = refGen.getSequence().length();
            }

            sequence = refGen.getSequence().substring(start, stop);
            boolean isFeatureSelected = this.frameCurrFeature != INIT;

            int index = 0;
            for (int i=0; i<this.selectedCodons.size(); ++i){
                if (this.selectedCodons.get(i)){
                    this.matchPattern(sequence, this.startCodons[index++], true, offset, isFeatureSelected);
                    this.matchPattern(sequence, this.startCodons[index++], false, offset, isFeatureSelected);
                } else {
                    index +=2;
                }
            }
        }

    }

    /**
     * Identifies pattern "p" in the given "sequence" and stores positive results
     * in this class' region list.
     * @param sequence the sequence to analyse
     * @param p pattern to search for
     * @param isForwardStrand if pattern is fwd or rev
     * @param offset offset needed for storing the correct region positions
     * @param restricted determining if the visualization should be restricted to a certain frame
     */
    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand, 
            int offset, boolean restricted){
        // match forward
        Matcher m = p.matcher(sequence);
        while(m.find()){
            int from = m.start();
            int to = m.end()-1;
            if(restricted){
                final int start = absStart-offset+from+1;
                final boolean codonFwdStrand = this.frameCurrFeature > 0 ? true : false;
                if ((start % 3)+1 == Math.abs(this.frameCurrFeature) && codonFwdStrand == isForwardStrand){
                    regions.add(new Region(start, absStart-offset+to+1, isForwardStrand));
                }
            } else {
                regions.add(new Region(absStart-offset+from+1, absStart-offset+to+1, isForwardStrand));
            }
        }
    }

    @Override
    public List<Region> findRegions() {
        this.findStartCodons();
        return this.regions;
    }

    @Override
    public void setIntervall(int start, int stop) {
        this.absStart = start;
        this.absStop = stop;
    }

    /**
     * Sets if the start codon with the index i is currently selected.
     * @param i the index of the current start codon
     * @param isSelected true, if the start codon is selected, false otherwise
     */
    public void setCodonSelected(final int i, final boolean isSelected){
        this.selectedCodons.set(i, isSelected);
    }

    /**
     * Returns if the codon with index i is currently selected.
     * @param i index of the start codon
     * @return true if the start codon with index i is currently selected
     */
    public boolean isCodonSelected(final int i){
        return this.selectedCodons.get(i);
    }

    public int getFrameCurrFeature() {
        return this.frameCurrFeature;
    }

    /**
     * Sets the data needed for the current feature. Currently only the frame is
     * necessary. This always has to be set first in case the action should only
     * show start codons of the correct frame.
     * @param frameCurrFeature the frame of the currently selected feature
     */
    public void setCurrFeatureData(int frameCurrFeature) {
        this.frameCurrFeature = frameCurrFeature;
    }

    /**
     * Checks if at least one codon is currently selected.
     * @return true if at least one codon is currently selected
     */
    private boolean atLeastOneCodonSelected() {
        for (int i=0; i<this.selectedCodons.size(); ++i){
            if (this.selectedCodons.get(i)){
                return true;
            }
        }
        return false;
    }

    /**
     * Resets the set of start codons according to the currently selected genetic code.
     */
    public final void resetStartCodons() {
        // TODO: hier ersetzen
        String[] startCodonsNew = GeneticCodesStore.getGeneticCode(NbPreferences.forModule(Object.class).get("selectedGeneticCode", ""))[0];
        this.startCodons = new Pattern[startCodonsNew.length*2];
        this.selectedCodons = new ArrayList<Boolean>();
        int index = 0;
        String codon;
        for (int i=0; i<startCodonsNew.length; ++i){
            codon = startCodonsNew[i].toLowerCase();
            this.startCodons[index++] = Pattern.compile(codon);
            this.startCodons[index++] = Pattern.compile(Utils.complementDNA(Utils.reverseString(codon)));
            this.selectedCodons.add(false);
        }
    }
    
    

}
