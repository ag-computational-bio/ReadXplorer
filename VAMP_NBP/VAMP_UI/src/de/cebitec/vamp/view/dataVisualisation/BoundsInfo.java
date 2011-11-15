package de.cebitec.vamp.view.dataVisualisation;

/**
 * This class stores information about a region that should be displayed for
 * example by an AbstractViewer. It stores left and right borders, current position,
 * current width and zoom level and also maximal left and right values.
 * @author ddoppmeier
 */
public class BoundsInfo {

    private int logLeft;
    private int logRight;
    private int logWidth;
    private int currentLogPos;
    private int zoomValue;
    private int maxLogRight;
    private int maxLogLeft;

    public BoundsInfo(int maxLogLeft, int maxLogRight, int currentLogPos, int zoomValue){
        this.maxLogLeft = maxLogLeft;
        this.maxLogRight = maxLogRight;
        this.currentLogPos = currentLogPos;
        this.zoomValue = zoomValue;
        logWidth = 200;
        updateLeftAndRight();
    }

    public BoundsInfo(int maxLogLeft, int maxLogRight, int currentLogPos, int zoomValue, int width){
        this.maxLogLeft = maxLogLeft;
        this.maxLogRight = maxLogRight;
        this.currentLogPos = currentLogPos;
        this.zoomValue = zoomValue;
        logWidth = width;
        updateLeftAndRight();
    }

     /*
     * @return the left most position of the genome to be shown
     */
    public int getMaxLogLeft() {
        return maxLogLeft;
    }


    /**
     *
     * @return the maximal position of the genome that can be displayed.
     * Normally equals the size of the genome
     */
    public int getMaxLogRight() {
        return maxLogRight;
    }



    /**
     *
     * @return the current position that should be displayed
     */
    public int getCurrentLogPos() {
        return currentLogPos;
    }

    /**
     *
     * @param currentLogPos the current position that should be displayed
     */
    public void setCurrentLogPos(int currentLogPos) {
        this.currentLogPos = currentLogPos;
        updateLeftAndRight();
    }

    /**
     *
     * @return the left most position of the area currently visible
     */
    public int getLogLeft() {
        return logLeft;
    }

    /**
     *
     * @return the right most position of the area currently visible
     */
    public int getLogRight() {
        return logRight;
    }

    /**
     *
     * @return the width of the intervall from the genome that is currently visible
     */
    public int getLogWidth() {
        return logWidth;
    }

    /**
     *
     * @param logWidth the width of the intervall from the genome that is currently visible
     */
    public void setLogWidth(int logWidth){
        this.logWidth = logWidth;
        updateLeftAndRight();
    }

    public void correctLogRight(int newLogRight){
        this.logRight = newLogRight;
        this.logWidth = this.logRight - logLeft +1;
    }

    /**
     * Udate the left and right logical bounds depending on the current position.
     * Restricts bounds, so that they do not reach out of the maximal bounds
     */
    private void updateLeftAndRight() {
        logLeft = currentLogPos - logWidth/2;
        if(logWidth % 2 == 0 ){
            logRight = currentLogPos + logWidth/2 -1;
        } else {
            logRight = currentLogPos + logWidth/2;
        }

        // out of left boundary
        if(logLeft < maxLogLeft){
            int shift = logLeft * -1 +1;
            logLeft += shift;
            logRight += shift;
        }

        // out of right boundary
        if(logRight > maxLogRight){
            int shift = logRight - maxLogRight;
            logRight -= shift;
            logLeft -= shift;
        }
    }


    public int getZoomValue() {
        return zoomValue;
    }


}
