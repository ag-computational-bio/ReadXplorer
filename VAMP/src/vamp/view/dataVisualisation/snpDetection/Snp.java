package vamp.view.dataVisualisation.snpDetection;

/**
 *
 * @author ddoppmeier
 */
public class Snp {

    private int count;
    private int position;
    private String base;
    private int percentage;
    private int positionVariation;

    public Snp(int count, int position, String base, int percentage, int positionVariation){
        this.count = count;
        this.position = position;
        this.base = base;
        this.percentage = percentage;
        this.positionVariation = positionVariation;
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
