package vamp.view.dataVisualisation.abstractViewer;

/**
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
