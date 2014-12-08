
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import de.cebitec.readXplorer.databackend.ParameterSetI;


/**
 *
 * @author jritter
 */
public class PromotorSearchParameters implements ParameterSetI<Object> {

    private int minusTenMotifWidth, minus35MotifWidth;
    private int minSpacer1, minSpacer2, alternativeSpacer, sequenceWidthToAnalyzeMinus10, sequenceWidthToAnalyzeMinus35;
    private int noOfTimesTrying;
    private final int lengthOfPromotorRegion;


    public PromotorSearchParameters( int minusTenMotifWidth, int minus35MotifWidth, int noOfTimesTryingTF, int minSpacer1, int minSpacer2, int alternativeSpacer, int seqWidthMinus10, int seqWidthMinus35, int lengthRelToTss ) {
        this.minusTenMotifWidth = minusTenMotifWidth;
        this.minus35MotifWidth = minus35MotifWidth;
        this.noOfTimesTrying = noOfTimesTryingTF;
        this.minSpacer1 = minSpacer1;
        this.minSpacer2 = minSpacer2;
        this.alternativeSpacer = alternativeSpacer;
        this.sequenceWidthToAnalyzeMinus10 = seqWidthMinus10;
        this.sequenceWidthToAnalyzeMinus35 = seqWidthMinus35;
        this.lengthOfPromotorRegion = lengthRelToTss;
    }


    public int getMinusTenMotifWidth() {
        return minusTenMotifWidth;
    }


    public void setMinusTenMotifWidth( int minusTenMotifWidth ) {
        this.minusTenMotifWidth = minusTenMotifWidth;
    }


    public int getLengthOfPromotorRegion() {
        return lengthOfPromotorRegion;
    }


    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }


    public int getAlternativeSpacer() {
        return alternativeSpacer;
    }


    public void setAlternativeSpacer( int alternativeSpacer ) {
        this.alternativeSpacer = alternativeSpacer;
    }


    public void setMinus35MotifWidth( int minus35MotifWidth ) {
        this.minus35MotifWidth = minus35MotifWidth;
    }


    public int getNoOfTimesTrying() {
        return noOfTimesTrying;
    }


    public void setNoOfTimesTrying( int noOfTimesTryingTF ) {
        this.noOfTimesTrying = noOfTimesTryingTF;
    }


    public int getMinSpacer1() {
        return minSpacer1;
    }


    public void setMinSpacer1( int minSpacer1 ) {
        this.minSpacer1 = minSpacer1;
    }


    public int getMinSpacer2() {
        return minSpacer2;
    }


    public void setMinSpacer2( int spacer2 ) {
        this.minSpacer2 = spacer2;
    }


    public int getSequenceWidthToAnalyzeMinus10() {
        return sequenceWidthToAnalyzeMinus10;
    }


    public void setSequenceWidthToAnalyzeMinus10( int sequenceWidthToAnalyzeMinus10 ) {
        this.sequenceWidthToAnalyzeMinus10 = sequenceWidthToAnalyzeMinus10;
    }


    public int getSequenceWidthToAnalyzeMinus35() {
        return sequenceWidthToAnalyzeMinus35;
    }


    public void setSequenceWidthToAnalyzeMinus35( int sequenceWidthToAnalyzeMinus35 ) {
        this.sequenceWidthToAnalyzeMinus35 = sequenceWidthToAnalyzeMinus35;
    }


}
