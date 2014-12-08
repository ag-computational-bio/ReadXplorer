/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author jritter
 */
public class ExportFeature {

    private PersistentFeature feature;
    /**
     * Key: TSS-Start position
     */
    HashMap<Integer, Integer> rbsPosistions;
    HashMap<Integer, Integer> minus10Positions;
    HashMap<Integer, Integer> minus35Positions;
    HashMap<Integer, Boolean> promotorAssignments;
    HashMap<Integer, Boolean> rbsAssignments;
    List<Integer> tssPositions;
    int promotorSequenceLength;
    boolean isPromotrSequenceLenghSet;
    boolean isRbsAnalysisSequenceLengthSet;
    int rbsSequenceLength;
    int minus10MotifWidth;
    int minus35MotifWidth;
    int rbsMotifWidth;
    int geneStart;
    int geneStop;


    /**
     *
     * @param feature
     * @param minus10MotifWidth
     * @param minus35MotifWidth
     * @param minusRbsMotifWidth
     */
    public ExportFeature( PersistentFeature feature, int minus10MotifWidth, int minus35MotifWidth, int minusRbsMotifWidth ) {
        this.minus10MotifWidth = minus10MotifWidth;
        this.minus35MotifWidth = minus35MotifWidth;
        this.rbsMotifWidth = minusRbsMotifWidth;
        tssPositions = new ArrayList<>();
        rbsPosistions = new HashMap<>();
        minus10Positions = new HashMap<>();
        minus35Positions = new HashMap<>();
        rbsAssignments = new HashMap<>();
        promotorAssignments = new HashMap<>();
        this.feature = feature;
    }


    /**
     *
     * @param tssPosition
     * @param rbsPosistion
     * @param minus10Position
     * @param minus35Position
     * @param hasPromotor
     * @param hasRbs
     * @param promotorSequenceLength
     * @param rbsSequenceLength
     */
    public void setValues( int tssPosition, int rbsPosistion, int minus10Position, int minus35Position, boolean hasPromotor, boolean hasRbs, int promotorSequenceLength, int rbsSequenceLength ) {
        tssPositions.add( tssPosition );
        rbsAssignments.put( tssPosition, hasRbs );
        promotorAssignments.put( tssPosition, hasPromotor );
        if( hasRbs ) {
            rbsPosistions.put( tssPosition, rbsPosistion );
        }
        if( hasPromotor ) {
            if( minus10Position > 0 ) {
                minus10Positions.put( tssPosition, minus10Position );
            }
            if( minus35Position > 0 ) {
                minus35Positions.put( tssPosition, minus35Position );
            }
        }

        if( promotorSequenceLength > 0 && isPromotrSequenceLenghSet == false ) {
            this.promotorSequenceLength = promotorSequenceLength;
            isPromotrSequenceLenghSet = true;
        }
        if( rbsSequenceLength > 0 && isRbsAnalysisSequenceLengthSet == false ) {
            this.rbsSequenceLength = rbsSequenceLength;
            isRbsAnalysisSequenceLengthSet = true;
        }
        if( this.feature.isFwdStrand() ) {
            if( geneStart == 0 ) {
                geneStart = tssPosition;
            }
            if( minus35Position != 0 && (tssPosition - promotorSequenceLength + minus35Position) < this.geneStart ) {
                this.geneStart = tssPosition - promotorSequenceLength + minus35Position;
            }
            else if( minus10Position != 0 && (tssPosition - promotorSequenceLength + minus10Position) < this.geneStart ) {
                this.geneStart = tssPosition - promotorSequenceLength + minus10Position;
            }
        }
        else {
            if( geneStart == 0 ) {
                geneStart = tssPosition;
            }
            if( minus35Position != 0 && (tssPosition + promotorSequenceLength - minus35Position) > this.geneStart ) {
                this.geneStart = tssPosition + promotorSequenceLength - minus35Position;
            }
            else if( minus10Position != 0 && (tssPosition - promotorSequenceLength + minus10Position) > this.geneStart ) {
                this.geneStart = tssPosition + promotorSequenceLength - minus10Position;
            }
        }
    }


    public PersistentFeature getFeature() {
        return feature;
    }


    public void setFeature( PersistentFeature feature ) {
        this.feature = feature;
    }


    public HashMap<Integer, Integer> getRbsPosistions() {
        return rbsPosistions;
    }


    public void setRbsPosistions( HashMap<Integer, Integer> rbsPosistions ) {
        this.rbsPosistions = rbsPosistions;
    }


    public HashMap<Integer, Integer> getMinus10Positions() {
        return minus10Positions;
    }


    public void setMinus10Positions( HashMap<Integer, Integer> minus10Positions ) {
        this.minus10Positions = minus10Positions;
    }


    public HashMap<Integer, Integer> getMinus35Positions() {
        return minus35Positions;
    }


    public void setMinus35Positions( HashMap<Integer, Integer> minus35Positions ) {
        this.minus35Positions = minus35Positions;
    }


    public int getMinus10MotifWidth() {
        return minus10MotifWidth;
    }


    public void setMinus10MotifWidth( int minus10MotifWidth ) {
        this.minus10MotifWidth = minus10MotifWidth;
    }


    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }


    public void setMinus35MotifWidth( int minus35MotifWidth ) {
        this.minus35MotifWidth = minus35MotifWidth;
    }


    public int getRbsMotifWidth() {
        return rbsMotifWidth;
    }


    public void setRbsMotifWidth( int minusRbsMotifWidth ) {
        this.rbsMotifWidth = minusRbsMotifWidth;
    }


    public int getGeneStart() {
        return geneStart;
    }


    public List<Integer> getTssPositions() {
        return tssPositions;
    }


    public HashMap<Integer, Boolean> getPromotorAssignments() {
        return promotorAssignments;
    }


    public HashMap<Integer, Boolean> getRbsAssignments() {
        return rbsAssignments;
    }


    public int getPromotorSequenceLength() {
        return promotorSequenceLength;
    }


    public int getRbsSequenceLength() {
        return rbsSequenceLength;
    }


    /**
     *
     * @return
     */
    public int getGeneStop() {
        if( feature != null ) {
            if( feature.isFwdStrand() ) {
                geneStop = feature.getStop();
                return geneStop;
            }
            else {
                geneStop = feature.getStart();
                return geneStop;
            }
        }
        else {
            return 0;
        }
    }


}
