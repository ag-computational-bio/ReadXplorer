package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ddoppmeier
 */
public class StartCodonFilter implements RegionFilterI {

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

    public StartCodonFilter(int absStart, int absStop, PersistantReference refGen){
        regions = new ArrayList<Region>();
        this.absStart = absStart;
        this.absStop = absStop;
        this.refGen = refGen;
        atgForward = Pattern.compile("atg");
        atgReverse = Pattern.compile("cat");
        gtgForward = Pattern.compile("gtg");
        gtgReverse = Pattern.compile("cac");
        ttgForward = Pattern.compile("ttg");
        ttgReverse = Pattern.compile("caa");

        atgSelected = false;
        ttgSelected = false;
        gtgSelected = false;

    }

    private void findStartCodons(){
        regions.clear();

        if(atgSelected || ttgSelected || gtgSelected){
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

            if(atgSelected){
                matchPattern(sequence, atgForward, true, offset);
                matchPattern(sequence, atgReverse, false, offset);
            }
            if(gtgSelected){
                matchPattern(sequence, gtgForward, true, offset);
                matchPattern(sequence, gtgReverse, false, offset);
            }
            if(ttgSelected){
                matchPattern(sequence, ttgForward, true, offset);
                matchPattern(sequence, ttgReverse, false, offset);
            }
        }

    }

    private void matchPattern(String sequence, Pattern p, boolean isForwardStrand, int offset){
        // match forward
        Matcher m = p.matcher(sequence);
        while(m.find()){
            int from = m.start();
            int to = m.end()-1;
            regions.add(new Region(absStart-offset+from+1, absStart-offset+to+1, isForwardStrand));
        }
    }

    @Override
    public List<Region> findRegions() {

        findStartCodons();
        return regions;
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

}
