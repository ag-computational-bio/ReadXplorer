package de.cebitec.vamp.api.objects;

/**
 *
 * @author msmith
 */
public class Snp454 {
    
    private String refBase;
    private int count;
    private int position;
    private String base;
    private int percentage;
    private int positionVariation;

    
    
    public Snp454(int count, int position, String base, int percentage, int positionVariation, String refBase) {
        this.refBase = refBase;
        this.count = count;
        this.position = position;
        this.base = base;
        this.percentage = percentage;
        this.positionVariation = positionVariation;
    }
    
    public String getRefBase() {
        return this.refBase;
    }

    public String getBase() {
        return base;
    }

    public int getCount() {
        return count;
    }

    public int getPercentage() {
        return percentage;
    }

    public int getPosition() {
        return position;
    }

    public int getVariationPercentag() {
        return positionVariation;
    }

    @Override
    public String toString(){
        return "position: "+position+"\tbase: "+base+"\tPercentage: "+percentage+"%\tno.: "+count+"\tvariation at pos.: "+positionVariation+"%";
    }
}
