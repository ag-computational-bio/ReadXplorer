package de.cebitec.vamp.databackend.dataObjects;

/**
 *
 * @author msmith
 */
public class Snp454 implements SnpI {
    
    private String refBase;
    private int coverage;
    private int position;
    private String base;
    private int percentage;
    private int positionVariation;

    
    
    public Snp454(int coverage, int position, String base, int percentage, int positionVariation, String refBase) {
        this.refBase = refBase;
        this.coverage = coverage;
        this.position = position;
        this.base = base;
        this.percentage = percentage;
        this.positionVariation = positionVariation;
    }
    
    public String getRefBase() {
        return this.refBase;
    }

    @Override
    public String getBase() {
        return base;
    }

    @Override
    public int getCoverage() {
        return coverage;
    }

    public int getPercentage() {
        return percentage;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public int getVariationPercentag() {
        return positionVariation;
    }

    @Override
    public String toString(){
        return "position: "+position+"\tbase: "+base+"\tPercentage: "+percentage+"%\tno.: "+coverage+"\tvariation at pos.: "+positionVariation+"%";
    }
}
