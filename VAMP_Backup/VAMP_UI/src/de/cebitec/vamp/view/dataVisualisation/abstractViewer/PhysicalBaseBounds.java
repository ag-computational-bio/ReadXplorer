package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

/**
 *
 * @author ddoppmeier
 *
 * Storage for the physical bounds of the base window.
 * Contains the left and right bounds, width and middle position.
 */
public class PhysicalBaseBounds {

    private double leftPhysBound;
    private double rightPhysBound;
    private double physWidth;
    private double phyMiddle;

    public PhysicalBaseBounds(double leftPhysBound, double rightPhysBound){
        this.leftPhysBound = leftPhysBound;
        this.rightPhysBound = rightPhysBound;
        this.physWidth = rightPhysBound - leftPhysBound + 1;
        phyMiddle = (leftPhysBound + rightPhysBound) / 2;
    }

    public double getLeftPhysBound() {
        return leftPhysBound;
    }

    public double getRightPhysBound() {
        return rightPhysBound;
    }

    public double getPhysWidth() {
        return physWidth;
    }

    public double getPhyMiddle() {
        return phyMiddle;
    }
    

}
