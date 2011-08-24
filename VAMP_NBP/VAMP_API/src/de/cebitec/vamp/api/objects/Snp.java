package de.cebitec.vamp.api.objects;

/**
 *
 * @author ddoppmeier
 */
public class Snp {

//    private int count;
//    private int position;
//    private String base;
//    private int percentage;
//    private int positionVariation;
//
//    public Snp(int count, int position, String base, int percentage, int positionVariation){
//        this.count = count;
//        this.position = position;
//        this.base = base;
//        this.percentage = percentage;
//        this.positionVariation = positionVariation;
//    }
//
//    public String getBase() {
//        return base;
//    }
//
//    public int getCount() {
//        return count;
//    }
//
//    public int getPercentage() {
//        return percentage;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public int getVariationPercentag() {
//        return positionVariation;
//    }
//
//    @Override
//    public String toString(){
//        return "position: "+position+"\tbase: "+base+"\tPercentage: "+percentage+"%\tno.: "+count+"\tvariation at pos.: "+positionVariation+"%";
//    }
    
    private String position;
    private int track;
    private char base;
    private char refBase;
    private int aRate;
    private int cRate;
    private int gRate;
    private int tRate;
    private int nRate;
    private int gapRate;
    private int coverage;
    private int frequency;
    private char type;

    public Snp(String position,int track, char base, char refBase, int aRate, int cRate, 
                    int gRate, int tRate, int nRate, int gapRate, int coverage,
                    int frequency, char type){
        this.position = position;
        this.track = track;
        this.base = base;
        this.refBase = refBase;
        this.aRate = aRate;
        this.cRate = cRate;
        this.gRate = gRate;
        this.tRate = tRate;
        this.nRate = nRate;
        this.gapRate = gapRate;
        this.coverage = coverage;
        this.frequency = frequency;
        this.type = type;
    }

    
    public String getPosition() {
        return position;
    }
    
    public int getTrack() {
        return track;
    }
    
    public String getBase() {
        return String.valueOf(base);
    }
    
    public String getRefBase() {
        return String.valueOf(refBase);
    }
    
    
    public int getARate() {
        return aRate;
    }
    
    public int getCRate() {
        return cRate;
    }
    
    public int getGRate() {
        return gRate;
    }
    
    public int getTRate() {
        return tRate;
    }
    
    public int getNRate() {
        return nRate;
    }
    
    public int getGapRate() {
        return gapRate;
    }
    
    public int getCoverage() {
        return coverage;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public String getType() {
        return String.valueOf(type);
    }



    @Override
    public String toString(){
        return "position: "+position+"\tbase: "+base+"\trefBase: "+refBase+"\taRate: "+aRate+"\tcRate: "
                +cRate+"\tgRate: "+gRate+ "\ttRate: "+tRate+"\tnRate: "+nRate+"\tgapRate: "
                +gapRate+"\tcoverage: "+coverage+"\tfrequency: "+frequency+"%\ttype: "+type;
    }

    
}
