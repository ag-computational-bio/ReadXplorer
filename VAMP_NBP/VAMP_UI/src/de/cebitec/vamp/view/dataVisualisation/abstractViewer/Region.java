package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

/**
 * A region marked by a start and stop position and if it should
 * be read in fwd or reverse direction. Furthermore, it holds the type of the
 * region.
 * 
 * @author ddoppmeier, rhilker
 */
public class Region {

    private int start;
    private int stop;
    private boolean isForwardStrand;
    private int type;

    /**
     * A region marked by a start and stop position and if it should be read in
     * fwd or reverse direction. Furthermore, it holds the type of the region.
     * @param start the start of the region
     * @param stop the stop of the region
     * @param isForwardStrand true, if it is on the fwd strand, false otherwise
     * @param type type of the region. Use Region.START or Region.STOP.
     */
    public Region(int start, int stop, boolean isForwardStrand, int type){
        this.start = start;
        this.stop = stop;
        this.isForwardStrand = isForwardStrand;
        this.type = type;
    }

    public int getStart() {
        return this.start;
    }

    public int getStop() {
        return this.stop;
    }

    public boolean isForwardStrand(){
        return this.isForwardStrand;
    }

    /**
     * @return the type of the region. Either Region.START or Region.STOP.
     */
    public int getType() {
        return this.type;
    }
    
    

}
