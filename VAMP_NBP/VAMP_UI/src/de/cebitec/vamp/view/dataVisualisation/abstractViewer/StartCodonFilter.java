package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Pattern atgForward;
    private Pattern atgReverse;
    private Pattern gtgForward;
    private Pattern gtgReverse;
    private Pattern ttgForward;
    private Pattern ttgReverse;
    private boolean atgSelected;
    private boolean ttgSelected;
    private boolean gtgSelected;
    private boolean atgCurrFeatureSel;
    private boolean ttgCurrFeatureSel;
    private boolean gtgCurrFeatureSel;
    private int frameCurrFeature;

    public StartCodonFilter(int absStart, int absStop, PersistantReference refGen){
        this.regions = new ArrayList<Region>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
        this.atgForward = Pattern.compile("atg");
        this.atgReverse = Pattern.compile("cat");
        this.gtgForward = Pattern.compile("gtg");
        this.gtgReverse = Pattern.compile("cac");
        this.ttgForward = Pattern.compile("ttg");
        this.ttgReverse = Pattern.compile("caa");

        this.atgSelected = false;
        this.ttgSelected = false;
        this.gtgSelected = false;
        this.atgCurrFeatureSel = false;
        this.gtgCurrFeatureSel = false;
        this.ttgCurrFeatureSel = false;

        this.frameCurrFeature = StartCodonFilter.INIT; //because this is not a frame value
    }

    /**
     * Searches and identifies start codons and saves their position
     * in this class' region list.
     */
    private void findStartCodons(){
        regions.clear();

        if(atgSelected || ttgSelected || gtgSelected || atgCurrFeatureSel
                || ttgCurrFeatureSel || gtgCurrFeatureSel){
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

            if(this.atgSelected){
               this.matchPattern(sequence, atgForward, true, offset, false);
               this.matchPattern(sequence, atgReverse, false, offset, false);
            }
            if(this.gtgSelected){
               this.matchPattern(sequence, gtgForward, true, offset, false);
               this.matchPattern(sequence, gtgReverse, false, offset, false);
            }
            if(this.ttgSelected){
               this.matchPattern(sequence, ttgForward, true, offset, false);
               this.matchPattern(sequence, ttgReverse, false, offset, false);
            }
            if(this.atgCurrFeatureSel){
               this.matchPattern(sequence, atgForward, true, offset, true);
               this.matchPattern(sequence, atgReverse, false, offset, true);
            }
            if(this.gtgCurrFeatureSel){
               this.matchPattern(sequence, gtgForward, true, offset, true);
               this.matchPattern(sequence, gtgReverse, false, offset, true);
            }
            if(this.ttgCurrFeatureSel){
               this.matchPattern(sequence, ttgForward, true, offset, true);
               this.matchPattern(sequence, ttgReverse, false, offset, true);
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

    public void setAtgSelected(boolean atgSelected) {
        this.atgSelected = atgSelected;
    }

    public void setGtgSelected(boolean gtgSelected) {
        this.gtgSelected = gtgSelected;
    }

    public void setTtgSelected(boolean ttgSelected) {
        this.ttgSelected = ttgSelected;
    }

    public boolean isAtgSelected() {
        return this.atgSelected;
    }

    public boolean isGtgSelected() {
        return this.gtgSelected;
    }

    public boolean isTtgSelected() {
        return this.ttgSelected;
    }

    public void setAtgCurrFeatureSelected(boolean atgCurrFeatureSel) {
        this.atgCurrFeatureSel = atgCurrFeatureSel;
    }

    public void setGtgCurrFeatureSelected(boolean gtgCurrFeatureSel) {
        this.gtgCurrFeatureSel = gtgCurrFeatureSel;
    }

    public void setTtgCurrFeatureSelected(boolean ttgCurrFeatureSel) {
        this.ttgCurrFeatureSel = ttgCurrFeatureSel;
    }

    public boolean isAtgCurrFeatureSelected() {
        return this.atgCurrFeatureSel;
    }

    public boolean isGtgCurrFeatureSelected() {
        return this.gtgCurrFeatureSel;
    }

    public boolean isTtgCurrFeatureSelected() {
        return this.ttgCurrFeatureSel;
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

}
