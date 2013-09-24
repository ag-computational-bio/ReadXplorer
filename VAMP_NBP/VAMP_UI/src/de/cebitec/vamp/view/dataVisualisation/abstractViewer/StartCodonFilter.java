package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.CodonUtilities;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbPreferences;

/**
 * Filters for start and stop codons in two ways: First for all
 * available start and stop codons in a specified region and second
 * for all start and stop codons of a given frame for a specified region.
 *
 * @author ddoppmeier, rhilker
 */
public class StartCodonFilter implements RegionFilterI {

    public static final int INIT = 10;
    private Preferences pref;
    
    private List<Region> regions;
    private int absStart;
    private int absStop;
    private PersistantReference refGen;
    private String sequence;
    private ArrayList<Boolean> selectedStarts;
    private ArrayList<Boolean> selectedStops;
    private Pattern[] startCodons;
    private Pattern[] stopCodons;
    private int frameCurrFeature;
    private int nbGeneticCodes;
    private GeneticCodeFactory genCodeFactory;

    /**
     * Filters for start and stop codons in two ways: First for all available start 
     * codons in a specified region and second for all start codons of a given frame 
     * for a specified region.
     * @param absStart start of the region to search in
     * @param absStop end of the region to search in
     * @param refGen the reference in which to search
     */
    public StartCodonFilter(int absStart, int absStop, PersistantReference refGen){
        this.pref = NbPreferences.forModule(Object.class);
        this.regions = new ArrayList<>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
        this.genCodeFactory = GeneticCodeFactory.getDefault();
        this.nbGeneticCodes = genCodeFactory.getGeneticCodes().size();

        this.resetCodons();

        this.frameCurrFeature = StartCodonFilter.INIT; //because this is not a frame value
    }

    /**
     * Searches and identifies start and stop codons and saves their position
     * in this class' region list.
     */
    private void findSelectedCodons() {
        regions.clear();

        if (this.atLeastOneCodonSelected()) {
            // extends interval to search to the left and right,
            // to find start/stop codons that overlap current interval boundaries
            int offset = 3;
            int start = absStart - offset;
            int stop = absStop + 2;
            int genomeLength = refGen.getRefLength();

            if (stop > 0) {
                if (start < 0) {
                    offset -= Math.abs(start);
                    start = 0;
                }
                if (stop > genomeLength) {
                    stop = genomeLength;
                }

                sequence = refGen.getSequence().substring(start, stop);
                boolean isFeatureSelected = this.frameCurrFeature != INIT;

                int index = 0;
                for (int i = 0; i < this.selectedStarts.size(); ++i) {
                    if (this.selectedStarts.get(i)) {
                        this.matchPattern(sequence, this.startCodons[index++], true, offset, isFeatureSelected, Properties.START);
                        this.matchPattern(sequence, this.startCodons[index++], false, offset, isFeatureSelected, Properties.START);
                    } else {
                        index += 2;
                    }
                }
                index = 0;
                for (int i = 0; i < this.selectedStops.size(); ++i) {
                    if (this.selectedStops.get(i)) {
                        this.matchPattern(sequence, this.stopCodons[index++], true, offset, isFeatureSelected, Properties.STOP);
                        this.matchPattern(sequence, this.stopCodons[index++], false, offset, isFeatureSelected, Properties.STOP);
                    } else {
                        index += 2;
                    }
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
     * @param genomeLength length of the genome, needed for checking frame on the reverse strand
     * @param type The type of the regions to create. Either Region.START or Region.STOP.
     */
    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand, int offset, boolean restricted,
            int type){
        // match forward
        final boolean codonFwdStrand = this.frameCurrFeature > 0 ? true : false;
        if (!restricted || restricted && codonFwdStrand == isForwardStrand){
            Matcher m = p.matcher(sequence);
            while (m.find()) {
                int from = m.start();
                int to = m.end() - 1;
                final int start = absStart - offset + from + 1; // +1 because in matcher each pos is 
                final int stop = absStart - offset + to + 1; //shifted by -1 (index starts with 0)
                if (restricted) {
                    /*
                     * Works because e.g. for positions 1-3 & 6-4: 
                     * +1 = (pos 1 - 1) % 3 = 0 -> 0 + 1 = frame +1
                     * +2 = (pos 2 - 1) % 3 = 1 -> 1 + 1 = frame +2
                     * +3 = (pos 3 - 1) % 3 = 2 -> 2 + 1 = frame +3
                     * -1 = (pos 6 - 1) % 3 = 2 -> 2 - 3 = frame -1
                     * -2 = (pos 5 - 1) % 3 = 1 -> 1 - 3 = frame -2
                     * -3 = (pos 4 - 1) % 3 = 0 -> 0 - 3 = frame -3
                     */
                    if ((start - 1) % 3 + 1 == this.frameCurrFeature ||
                         (stop - 1) % 3 - 3 == this.frameCurrFeature) {
                        regions.add(new Region(start, stop, isForwardStrand, type));
                    }
                } else {
                    regions.add(new Region(start, stop, isForwardStrand, type));
                }
            }
        }
    }

    @Override
    public List<Region> findRegions() {
        this.findSelectedCodons();
        return this.regions;
    }

    @Override
    public void setInterval(int start, int stop) {
        this.absStart = start;
        this.absStop = stop;
    }

    /**
     * Sets if the start codon with the index i is currently selected.
     * @param i the index of the current start codon
     * @param isSelected true, if the start codon is selected, false otherwise
     */
    public void setStartCodonSelected(final int i, final boolean isSelected){
        this.selectedStarts.set(i, isSelected);
    }
    
    /**
     * Sets if the stop codon with the index i is currently selected.
     * @param i the index of the current stop codon
     * @param isSelected true, if the stop codon is selected, false otherwise
     */
    public void setStopCodonSelected(final int i, final boolean isSelected){
        this.selectedStops.set(i, isSelected);
    }

    /**
     * Returns if the start codon with index i is currently selected.
     * @param i index of the start codon
     * @return true if the start codon with index i is currently selected
     */
    public boolean isStartCodonSelected(final int i){
        return this.selectedStarts.get(i);
    }
    
    /**
     * Returns if the stop codon with index i is currently selected.
     * @param i index of the stop codon
     * @return true if the stop codon with index i is currently selected
     */
    public boolean isStopCodonSelected(final int i){
        return this.selectedStops.get(i);
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
        for (int i=0; i<this.selectedStarts.size(); ++i){
            if (this.selectedStarts.get(i)){
                return true;
            }
        }
        for (int i = 0; i < this.selectedStops.size(); ++i) {
            if (this.selectedStops.get(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resets the set of start and stop codons according to the currently selected genetic code.
     */
    public final void resetCodons() {
        String[] startCodonsNew = new String[0];
        String[] stopCodonsNew = new String[0];
        int codeIndex = Integer.valueOf(pref.get(Properties.GENETIC_CODE_INDEX, "0"));
        if (codeIndex < nbGeneticCodes) {
            GeneticCode code = genCodeFactory.getGeneticCodeById(Integer.valueOf(pref.get(Properties.SEL_GENETIC_CODE, "1")));
            startCodonsNew = code.getStartCodons().toArray(startCodonsNew);
            stopCodonsNew = code.getStopCodons().toArray(stopCodonsNew);
        } else {
            startCodonsNew = CodonUtilities.parseCustomCodons(codeIndex, pref.get(Properties.CUSTOM_GENETIC_CODES, "1"));
        }
        this.startCodons = new Pattern[startCodonsNew.length*2];
        this.stopCodons = new Pattern[stopCodonsNew.length*2];
        this.selectedStarts = new ArrayList<>();
        this.selectedStops = new ArrayList<>();
        int index = 0;
        String codon;
        for (int i=0; i<startCodonsNew.length; ++i){
            codon = startCodonsNew[i];
            this.startCodons[index++] = Pattern.compile(codon);
            this.startCodons[index++] = Pattern.compile(SequenceUtils.complementDNA(SequenceUtils.reverseString(codon)));
            this.selectedStarts.add(false);
        }
        index = 0;
        for (int i = 0; i < stopCodonsNew.length; ++i) {
            codon = stopCodonsNew[i];
            this.stopCodons[index++] = Pattern.compile(codon);
            this.stopCodons[index++] = Pattern.compile(SequenceUtils.complementDNA(SequenceUtils.reverseString(codon)));
            this.selectedStops.add(false);
        }
    }   
}
