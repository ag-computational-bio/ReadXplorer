package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import java.io.File;

/**
 *
 * @author jritter
 */
public class PromotorSearchParameters {

    private int minusTenMotifWidth, minus35MotifWidth;
    private int minSpacer1, minSpacer2, sequenceWidthToAnalyzeMinus10, sequenceWidthToAnalyzeMinus35;
    private int noOfTimesTrying;
    private int lengthOfPromotorRegion;
    private File workingDirectory;

    public PromotorSearchParameters(int minusTenMotifWidth, int minus35MotifWidth, int noOfTimesTryingTF, int minSpacer1, int minSpacer2, int seqWidthMinus10, int seqWidthMinus35, int lengthRelToTss, File workingDir) {
        this.minusTenMotifWidth = minusTenMotifWidth;
        this.minus35MotifWidth = minus35MotifWidth;
        this.noOfTimesTrying = noOfTimesTryingTF;
        this.workingDirectory = workingDir;
        this.minSpacer1 = minSpacer1;
        this.minSpacer2 = minSpacer2;
        this.sequenceWidthToAnalyzeMinus10 = seqWidthMinus10;
        this.sequenceWidthToAnalyzeMinus35 = seqWidthMinus35;
        this.lengthOfPromotorRegion = lengthRelToTss;
    }

    public int getMinusTenMotifWidth() {
        return minusTenMotifWidth;
    }

    public void setMinusTenMotifWidth(int minusTenMotifWidth) {
        this.minusTenMotifWidth = minusTenMotifWidth;
    }

    public int getLengthOfPromotorRegion() {
        return lengthOfPromotorRegion;
    }

    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }

    public void setMinus35MotifWidth(int minus35MotifWidth) {
        this.minus35MotifWidth = minus35MotifWidth;
    }

    public int getNoOfTimesTrying() {
        return noOfTimesTrying;
    }

    public void setNoOfTimesTrying(int noOfTimesTryingTF) {
        this.noOfTimesTrying = noOfTimesTryingTF;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public int getMinSpacer1() {
        return minSpacer1;
    }

    public void setMinSpacer1(int minSpacer1) {
        this.minSpacer1 = minSpacer1;
    }

    public int getMinSpacer2() {
        return minSpacer2;
    }

    public void setMinSpacer2(int spacer2) {
        this.minSpacer2 = spacer2;
    }

    public int getSequenceWidthToAnalyzeMinus10() {
        return sequenceWidthToAnalyzeMinus10;
    }

    public void setSequenceWidthToAnalyzeMinus10(int sequenceWidthToAnalyzeMinus10) {
        this.sequenceWidthToAnalyzeMinus10 = sequenceWidthToAnalyzeMinus10;
    }

    public int getSequenceWidthToAnalyzeMinus35() {
        return sequenceWidthToAnalyzeMinus35;
    }

    public void setSequenceWidthToAnalyzeMinus35(int sequenceWidthToAnalyzeMinus35) {
        this.sequenceWidthToAnalyzeMinus35 = sequenceWidthToAnalyzeMinus35;
    }
}
