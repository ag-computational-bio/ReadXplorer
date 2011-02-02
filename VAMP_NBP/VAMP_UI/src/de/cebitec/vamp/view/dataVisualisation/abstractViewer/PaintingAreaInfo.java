package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.awt.geom.Point2D;

/**
 *
 * @author ddoppmeier
 */
public class PaintingAreaInfo {

    private int phyLeft;
    private int phyRight;
    private int phyWidth;

    private int forwardLow;
    private int forwardHigh;
    private int availableForwardHeight;

    private int reverseLow;
    private int reverseHigh;
    private int availableReverseHeight;

    private int completeHeight;


    public PaintingAreaInfo(){
        
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("left: "+phyLeft+"\n");
        sb.append("right: "+phyRight+"\n");
        sb.append("width: "+phyWidth+"\n");
        sb.append("fwHigh: "+forwardHigh+"\n");
        sb.append("fwLow: "+forwardLow+"\n");
        sb.append("fwHght: "+availableForwardHeight+"\n");
        sb.append("rvLow: "+reverseLow+"\n");
        sb.append("rvHigh: "+reverseHigh+"\n");
        sb.append("rvHght: "+availableReverseHeight+"\n");
        sb.append("compl. Hght: "+completeHeight);

        return sb.toString();
    }

    public int getForwardHigh() {
        return forwardHigh;
    }

    public void setForwardHigh(int forwardHigh) {
        this.forwardHigh = forwardHigh;
        updateAvailableForwardHeight();
        updateCompleteHeight();
    }

    private void updateCompleteHeight(){
        completeHeight = reverseHigh - forwardHigh +1;
    }

    public int getForwardLow() {
        return forwardLow;
    }

    public void setForwardLow(int forwardLow) {
        this.forwardLow = forwardLow;
        updateAvailableForwardHeight();
    }

    public int getPhyLeft() {
        return phyLeft;
    }

    public void setPhyLeft(int phyLeft) {
        this.phyLeft = phyLeft;
        recalcWidth();
    }

    public int getPhyRight() {
        return phyRight;
    }

    public void setPhyRight(int phyRight) {
        this.phyRight = phyRight;
        recalcWidth();
    }

    public int getPhyWidt() {
        return phyWidth;
    }

    public int getReverseLow() {
        return reverseLow;
    }

    public void setReverseLow(int reverseLow) {
        this.reverseLow = reverseLow;
        updateAvailableReverseHeight();
    }

    public int getReverseHigh() {
        return reverseHigh;
    }

    public void setReverseHigh(int revserHigh) {
        this.reverseHigh = revserHigh;
        updateAvailableReverseHeight();
        updateCompleteHeight();
    }

    private void recalcWidth(){
        this.phyWidth = phyRight - phyLeft +1;
    }

    private void updateAvailableForwardHeight(){
        availableForwardHeight = forwardLow - forwardHigh +1;
    }
    
    private void updateAvailableReverseHeight(){
        availableReverseHeight = reverseHigh - reverseLow +1;
    }

    public int getAvailableForwardHeight() {
        return availableForwardHeight;
    }

    public int getAvailableReverseHeight() {
        return availableReverseHeight;
    }

    public int getCompleteHeight() {
        return completeHeight;
    }

    public boolean fitsIntoArea(Point2D p){
        boolean fitsX = false;
        if(p.getX() >= phyLeft && p.getX() <= phyRight){
            fitsX = true;
        }
        boolean fitsY = false;
        if(p.getY() >= forwardHigh && p.getY() <= reverseHigh){
            fitsY = true;
        }

        if(fitsX && fitsY){
            return true;
        } else {
            return false;
        }
    }

    public boolean fitsIntoAreaY(int yValue){
        if(yValue >= forwardHigh && yValue <= reverseHigh){
            return true;
        } else {
            return false;
        }
    }

    public boolean fitsIntoAvailableForwardSpace(int yValue){
        if(yValue <= availableForwardHeight){
            return true;
        } else {
            return false;
        }
    }

    public boolean fitsIntoAvailableReverseSpace(int yValue){
        if(yValue <= availableReverseHeight){
            return true;
        } else {
            return false;
        }
    }
}
