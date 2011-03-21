package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

/**
 * Storage for the physical bounds of the base window.
 * Contains the left and right bounds, width and middle position.
 *
 * @author ddoppmeier
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

    /**
     * Returns the left physical bound.
     * @return
     */
    public double getLeftPhysBound() {
        return leftPhysBound;
    }

    /**
     * Returns the right physical bound.
     * @return
     */
    public double getRightPhysBound() {
        return rightPhysBound;
    }

    /**
     * Returns the physical width.
     * @return
     */
    public double getPhysWidth() {
        return physWidth;
    }

    /**
     * Returns the physical center value.
     * @return
     */
    public double getPhyMiddle() {
        return phyMiddle;
    }
    

}
