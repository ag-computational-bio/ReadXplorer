package vamp.databackend.dataObjects;

/**
 *
 * @author ddoppmei
 */
public class PersistantDiff {

    private char base;
    private int position;
    private boolean isForwardStrand;
    private int count;

    public PersistantDiff(int position, char base, boolean isForwardStrand, int count) {
        this.position = position;
        this.base = base;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
    }

    public char getBase() {
        return base;
    }

    public int getPosition() {
        return position;
    }

    public int getCount() {
        return count;
    }

    public boolean isIsForwardStrand() {
        return isForwardStrand;
    }
}
