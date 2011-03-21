package de.cebitec.vamp.databackend.dataObjects;

/**
 * A persistant feature. Containing background information about a feature, such as id,
 * ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier
 */
public class PersistantFeature {

    private int id;
    private String ecNumber;
    private String locus;
    private String product;
    private int start;
    private int stop;
    private int strand;
    private int type;

    public PersistantFeature(int id, String ecnum, String locus, String product, int start, int stop, int strand, int type) {
        this.id = id;
        this.ecNumber = ecnum;
        this.locus = locus;
        this.product = product;
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.type = type;
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public int getId() {
        return id;
    }

    public String getLocus() {
        return locus;
    }

    public String getProduct() {
        return product;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    /**
     * Returns if the feature is located on the fwd or rev strand.
     * @return
     */
    public int getStrand() {
        return strand;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString(){
        return locus;
    }

}
