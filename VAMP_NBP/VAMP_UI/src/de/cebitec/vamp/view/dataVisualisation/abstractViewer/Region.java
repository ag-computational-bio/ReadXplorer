package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

/**
 * A region marked by a start and stop position and if it should
 * be read in fwd or reverse direction.
 * 
 * @author ddoppmeier
 */
public class Region {

    private int start;
    private int stop;
    private boolean isForwardStrand;

    public Region(int start, int stop, boolean isForwardStrand){
        this.start = start;
        this.stop = stop;
        this.isForwardStrand = isForwardStrand;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public boolean isForwardStrand(){
        return isForwardStrand;
    }

}
