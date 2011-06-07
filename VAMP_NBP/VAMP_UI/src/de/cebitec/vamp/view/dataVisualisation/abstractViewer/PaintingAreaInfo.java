package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.awt.geom.Point2D;

/**
 * Contains useful info about a painting area, such heights and widhts,
 * left and right end positions.
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
        sb.append("left: ").append(phyLeft).append("\n");
        sb.append("right: ").append(phyRight).append("\n");
        sb.append("width: ").append(phyWidth).append("\n");
        sb.append("fwHigh: ").append(forwardHigh).append("\n");
        sb.append("fwLow: ").append(forwardLow).append("\n");
        sb.append("fwHght: ").append(availableForwardHeight).append("\n");
        sb.append("rvLow: ").append(reverseLow).append("\n");
        sb.append("rvHigh: ").append(reverseHigh).append("\n");
        sb.append("rvHght: ").append(availableReverseHeight).append("\n");
        sb.append("compl. Hght: ").append(completeHeight);

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

    public int getPhyWidth() {
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

    public void setReverseHigh(int reverseHigh) {
        this.reverseHigh = reverseHigh;
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
