/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.view.dataVisualisation.trackViewer;

import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.Coverage;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.ComparisonClass;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Display the coverage for a sequenced track related to a reference genome
 * @author ddoppmeier
 */
public class DoubleTrackViewer extends TrackViewer {

    private static final long serialVersionUID = 572406471;
    
    private final Preferences pref = NbPreferences.forModule(Object.class);
    private List<Integer> trackIDs ;
    private int id1;
    private int id2 ;
    
    double covFwd1 = 0.0;
    double covFwd2 = 0.0;
    double covRev1 = 0.0;
    double covRev2 = 0.0;
 //   public static final String PROP_TRACK_CLICKED = "track clicked";
  //  public static final String PROP_TRACK_ENTERED = "track entered";

    /**
     * Create a new panel to show coverage information
     * @param boundsManager manager for component bounds
     * @param basePanel 
     * @param refGen reference genome
     * @param trackCon database connection to one track, that is displayed
     */
    public DoubleTrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference refGen, 
            TrackConnector trackCon) {
        super(boundsManager, basePanel, refGen, trackCon, false);
        
        trackIDs = trackCon.getTrackIds();
        id1 = trackIDs.get(0);
        id2 = trackIDs.size() == 2 ? trackIDs.get(1) : -1;
                        
        pref.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                DoubleTrackViewer.this.createColors(pref);
                repaint();
            }
        });         
    }
    
    @Override
    protected List<Classification> createVisibleClasses() {
        List<Classification> newClassList = new ArrayList<>();
        newClassList.add(ComparisonClass.TRACK2_COVERAGE);
        newClassList.add(ComparisonClass.TRACK1_COVERAGE);
        newClassList.add(ComparisonClass.DIFF_COVERAGE);
        
        return newClassList;
    }
    
    /**
     * Updates the colors of the coverage in this viewer.
     * @param pref The preference object containing the new colors
     * @return 
     */
    @Override
    protected Map<Classification, Color> createColors(Preferences pref) {
        Map<Classification, Color> newClassToColorMap = new HashMap<>();
        Color track1Color = this.checkColor(pref.get(ColorProperties.TRACK1_COLOR_STRING, ""), ColorProperties.TRACK1_COLOR);
        Color track2Color = this.checkColor(pref.get(ColorProperties.TRACK2_COLOR_STRING, ""), ColorProperties.TRACK2_COLOR);
        Color diffColor = this.checkColor(pref.get(ColorProperties.COV_DIFF_STRING, ""), ColorProperties.COV_DIFF_COLOR);
        newClassToColorMap.put(ComparisonClass.TRACK1_COVERAGE, track1Color);
        newClassToColorMap.put(ComparisonClass.TRACK2_COVERAGE, track2Color);
        newClassToColorMap.put(ComparisonClass.DIFF_COVERAGE, diffColor);
        
        return newClassToColorMap;
    }

    /**
     * Calculates the (normalized) coverage value for the given strand, coverage type and position.
     * @param isForwardStrand if -1, coverage is drawn from bottom to top, if 1 otherwise
     * @param classType the mapping classification type of the coverage path handled here
     * @param absPos the reference position for which the coverage should be obtained
     * @return the coverage value for the given strand, coverage type and position.
     */
    @Override
    protected double calcCoverageValue(boolean isForwardStrand, Classification classType, int absPos) {
        double value = 0;

        if (this.getCoverageManagers().size() == 2) { //check for correct data structure
            
            List<Classification> excludedClasses = this.getExcludedFeatureTypes(); 

            // Calculate the correct coverage composed of the different mapping classes and depending on the currently excluded classes
            if (!excludedClasses.contains(MappingClass.COMMON_MATCH)) {
                value = this.updateCovValue(MappingClass.COMMON_MATCH, isForwardStrand, classType, absPos);
                return value; //no other data can be added to common match
            
            } else if (!excludedClasses.contains(MappingClass.SINGLE_COMMON_MATCH)) {
                value = this.updateCovValue(MappingClass.SINGLE_COMMON_MATCH, isForwardStrand, classType, absPos);
            }
            
            if (!excludedClasses.contains(MappingClass.BEST_MATCH)) {
                value += this.updateCovValue(MappingClass.BEST_MATCH, isForwardStrand, classType, absPos);
                return value; //no other data can be added to best match, if common match is not included, we have to add single common match (if included) nonetheless
           
            } else if (!excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) {
                value += this.updateCovValue(MappingClass.SINGLE_BEST_MATCH, isForwardStrand, classType, absPos);
            }
            
            if (!excludedClasses.contains(MappingClass.PERFECT_MATCH)) {
                value += this.updateCovValue(MappingClass.PERFECT_MATCH, isForwardStrand, classType, absPos);
                if (this.isUseExtendedClassification() && !excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) {
                    value -= this.updateCovValue(MappingClass.SINGLE_PERFECT_MATCH, isForwardStrand, classType, absPos);
                }
                return value; //no other data can be added to perfect match, since single perfect match is a subset of perfect match
               
            } else if (!excludedClasses.contains(MappingClass.SINGLE_PERFECT_MATCH) && excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) { //only add, 
                value += this.updateCovValue(MappingClass.SINGLE_PERFECT_MATCH, isForwardStrand, classType, absPos);  //single best match is not included (sp is subset)
            }

        } else {
            throw new IllegalArgumentException("The size of the coverage manager list is not equal to 2.");
        }
        
        return value;
    }
    
    private double updateCovValue(Classification mappingClass, boolean isForwardStrand, Classification classType, int absPos) {
        double value = 0;
        Coverage cov1 = this.getCoverageManagers().get(0).getCoverage(mappingClass); //applies, if only the not excluded ones are contained in the result
        Coverage cov2 = this.getCoverageManagers().get(1).getCoverage(mappingClass);
        if (isForwardStrand) {
            if (classType == ComparisonClass.DIFF_COVERAGE) {
                int value1 = (int) this.getNormalizedValue(id1, cov1.getFwdCov(absPos));
                int value2 = (int) this.getNormalizedValue(id2, cov2.getFwdCov(absPos));
                value = Math.abs(value2 - value1);
            } else if (classType == ComparisonClass.TRACK2_COVERAGE) {
                value = this.getNormalizedValue(id2, cov2.getFwdCov(absPos));
            } else if (classType == ComparisonClass.TRACK1_COVERAGE) {
                value = this.getNormalizedValue(id1, cov1.getFwdCov(absPos));
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
            }
        } else {
            if (classType == ComparisonClass.DIFF_COVERAGE) {
                int value1 = (int) this.getNormalizedValue(id1, cov1.getRevCov(absPos));
                int value2 = (int) this.getNormalizedValue(id2, cov2.getRevCov(absPos));
                value = Math.abs(value2 - value1);
            } else if (classType == ComparisonClass.TRACK2_COVERAGE) {
                value = this.getNormalizedValue(id2, cov2.getRevCov(absPos));
            } else if (classType == ComparisonClass.TRACK1_COVERAGE) {
                value = this.getNormalizedValue(id1, cov1.getRevCov(absPos));
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
            }
        }
        return value;
    }
    
//    /**
//     * {@inheritDoc }
//     */
//    @Override
//    public void changeToolTipText(int logPos) {
//
//        List<CoverageManager> coverageManagers = this.getCoverageManagers();
//        if (coverageManagers != null && coverageManagers.size() == 2) {
//            Double[] data = new Double[7];
//            data[0] = (double) logPos;
//            
//            for (MappingClass mappingClass : this.availableMappingClasses) {
//                Coverage cov1 = coverageManagers.get(0).getCoverage(mappingClass);
//                Coverage cov2 = coverageManagers.get(1).getCoverage(mappingClass);
//
//                int covFwd1 = cov1.getFwdCov(logPos);
//                int covFwd2 = cov2.getFwdCov(logPos);
//                int covRev1 = cov1.getRevCov(logPos);
//                int covRev2 = cov2.getRevCov(logPos);
//
//                int diffFwd = Math.abs(covFwd1 - covFwd2);
//                int diffRev = Math.abs(covRev1 - covRev2);
//
//                if (!this.hasNormalizationFactor()) {
//
//                    data[1] = (double) diffFwd;
//                    data[2] = (double) diffRev;
//                    data[3] = (double) covFwd1;
//                    data[4] = (double) covRev1;
//                    data[5] = (double) covFwd2;
//                    data[6] = (double) covRev2;
//
//                } else {
//
//                    int covFwd1Norm = (int) this.getNormalizedValue(id1, covFwd1);
//                    int covFwd2Norm = (int) this.getNormalizedValue(id2, covFwd2);
//                    int covRev1Norm = (int) this.getNormalizedValue(id1, covRev1);
//                    int covRev2Norm = (int) this.getNormalizedValue(id2, covRev2);
//                    
//                    double fwdScale1 = TrackViewer.threeDecAfter(covFwd1Norm);
//                    double revScale1 = TrackViewer.threeDecAfter(covRev1Norm);
//                    double fwdScale2 = TrackViewer.threeDecAfter(covFwd2Norm);
//                    double revScale2 = TrackViewer.threeDecAfter(covRev2Norm);
//
//                    double diffFw = Math.abs(fwdScale2 - fwdScale1);
//                    double diffRv = Math.abs(revScale2 - revScale1);
//                    double diffFwdScale = TrackViewer.threeDecAfter(diffFw);
//                    double diffRevScale = TrackViewer.threeDecAfter(diffRv);
//                    diffFwdScale = diffFwdScale < 0 ? diffFwdScale * -1 : diffFwdScale;
//                    diffRevScale = diffRevScale < 0 ? diffRevScale * -1 : diffRevScale;
//
//                    data = new Double[13];
//                    data[0] = (double) logPos;
//                    data[1] = (double) diffFwd;
//                    data[2] = diffFwdScale;
//                    data[3] = (double) diffRev;
//                    data[4] = diffRevScale;
//                    data[5] = (double) covFwd1;
//                    data[6] = fwdScale1;
//                    data[7] = (double) covRev1;
//                    data[8] = revScale1;
//                    data[9] = (double) covFwd2;
//                    data[10] = fwdScale2;
//                    data[11] = (double) covRev2;
//                    data[12] = revScale2;
//
//                }
//            }
//
//            this.setToolTipText(this.createToolTipText(data, this.hasNormalizationFactor()));
//        }
//    }
    
    private void updateTooltipValue(MappingClass mappingClass, int logPos) {
        Coverage coverage = this.getCoverageManagers().get(0).getCoverage(mappingClass);
        covFwd1 += coverage.getFwdCov(logPos);
        covRev1 += coverage.getRevCov(logPos);
        Coverage coverage2 = this.getCoverageManagers().get(1).getCoverage(mappingClass);
        covFwd2 += coverage2.getFwdCov(logPos);
        covRev2 += coverage2.getRevCov(logPos);
    }
    
    private void subtractCoverage(MappingClass mappingClass, int logPos) {
        Coverage coverage = this.getCoverageManagers().get(0).getCoverage(mappingClass);
        covFwd1 -= coverage.getFwdCov(logPos);
        covRev1 -= coverage.getRevCov(logPos);
        Coverage coverage2 = this.getCoverageManagers().get(1).getCoverage(mappingClass);
        covFwd2 -= coverage2.getFwdCov(logPos);
        covRev2 -= coverage2.getRevCov(logPos);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void changeToolTipText(int logPos) {
        this.covFwd1 = 0.0;
        this.covFwd2 = 0.0;
        this.covRev1 = 0.0;
        this.covRev2 = 0.0;
        
        StringBuilder sb = new StringBuilder(200);
        sb.append("<html>");
        sb.append("<b>Position</b>: ").append(logPos);
        sb.append("<br>");
        sb.append("<table>");
        
        List<CoverageManager> coverageManagers = this.getCoverageManagers();
        if (coverageManagers != null && coverageManagers.size() == 2) {


            List<Classification> excludedClasses = this.getExcludedFeatureTypes();

            // Calculate the correct coverage composed of the different mapping classes and depending on the currently excluded classes
            if (!excludedClasses.contains(MappingClass.COMMON_MATCH)) {
                this.updateTooltipValue(MappingClass.COMMON_MATCH, logPos);
                //no other data can be added to common match

            } else if (!excludedClasses.contains(MappingClass.SINGLE_COMMON_MATCH)) {
                this.updateTooltipValue(MappingClass.SINGLE_COMMON_MATCH, logPos);

                if (!excludedClasses.contains(MappingClass.BEST_MATCH)) {
                    this.updateTooltipValue(MappingClass.BEST_MATCH, logPos);
                    //no other data can be added to best match, if common match is not included, we have to add single common match (if included) nonetheless

                } else if (!excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) {
                    this.updateTooltipValue(MappingClass.SINGLE_BEST_MATCH, logPos);

                    if (!excludedClasses.contains(MappingClass.PERFECT_MATCH)) {
                        this.updateTooltipValue(MappingClass.PERFECT_MATCH, logPos);
                        if (!excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) {
                            this.subtractCoverage(MappingClass.SINGLE_PERFECT_MATCH, logPos);
                        } //no other data can be added to perfect match, since single perfect match is a subset of perfect match

                    } else if (!excludedClasses.contains(MappingClass.SINGLE_PERFECT_MATCH) && excludedClasses.contains(MappingClass.SINGLE_BEST_MATCH)) { 
                        //only add if single best match is not included (sp is subset)
                        this.updateTooltipValue(MappingClass.SINGLE_PERFECT_MATCH, logPos);
                    }
                }
            }

            double covFwd1Norm = 0;
            double covFwd2Norm = 0;
            double covRev1Norm = 0;
            double covRev2Norm = 0;
            double diffFwdScale = 0;
            double diffRevScale = 0;
            
            if (this.hasNormalizationFactor()) {
                covFwd1Norm = TrackViewer.threeDecAfter(this.getNormalizedValue(id1, covFwd1));
                covFwd2Norm = TrackViewer.threeDecAfter(this.getNormalizedValue(id2, covFwd2));
                covRev1Norm = TrackViewer.threeDecAfter(this.getNormalizedValue(id1, covRev1));
                covRev2Norm = TrackViewer.threeDecAfter(this.getNormalizedValue(id2, covRev2));
                diffFwdScale = TrackViewer.threeDecAfter(Math.abs(covFwd1Norm - covFwd2Norm));
                diffRevScale = TrackViewer.threeDecAfter(Math.abs(covRev1Norm - covRev2Norm));
//                diffFwdScale = diffFwdScale < 0 ? diffFwdScale * -1 : diffFwdScale; //TODO: check if needed???
//                diffRevScale = diffRevScale < 0 ? diffRevScale * -1 : diffRevScale; //???
            }
            
            double diffFwd = Math.abs(covFwd1 - covFwd2);
            double diffRev = Math.abs(covRev1 - covRev2);

            this.addToBuilder(sb, ComparisonClass.DIFF_COVERAGE, diffFwd, diffFwdScale, SequenceUtils.STRAND_FWD_STRING);
            this.addToBuilder(sb, ComparisonClass.DIFF_COVERAGE, diffRev, diffRevScale, SequenceUtils.STRAND_REV_STRING);
            this.addToBuilder(sb, ComparisonClass.TRACK1_COVERAGE, covFwd1, covFwd1Norm, SequenceUtils.STRAND_FWD_STRING);
            this.addToBuilder(sb, ComparisonClass.TRACK2_COVERAGE, covFwd2, covFwd2Norm, SequenceUtils.STRAND_FWD_STRING);
            this.addToBuilder(sb, ComparisonClass.TRACK1_COVERAGE, covRev1, covRev1Norm, SequenceUtils.STRAND_REV_STRING);
            this.addToBuilder(sb, ComparisonClass.TRACK2_COVERAGE, covRev2, covRev2Norm, SequenceUtils.STRAND_REV_STRING);
        }
                
        this.setToolTipText(sb.toString());
    }
        
    /**
     * Adds the given coverage value for the given mapping classification index
     * to the given string builder as a nice table row.
     * @param sb The string builder to add to
     * @param classification The current mapping classification
     * @param coverage The coverage value to store in the StringBuilder
     */
    private void addToBuilder(StringBuilder sb, Classification classification, double coverage, double coverageNorm, String strandString) {
        String classType = classification.getTypeString() + " " + strandString;
            if (this.hasNormalizationFactor()) {
                sb.append(createTableRow(classType, coverage, coverageNorm));
            } else {
                sb.append(createTableRow(classType, coverage));
            }
    }
    
//    private String createToolTipText(Double[] data, boolean hasNormFac) {
//        StringBuilder sb = new StringBuilder(200);
//        sb.append("<html>");
//        sb.append("<b>Position</b>: ").append(data[0]);
//        sb.append("<br>");
//        sb.append("<table>");
//        sb.append("<tr><td align=\"left\"><b>Difference(Blue):</b></td></tr>");
//        sb.append(hasNormFac ? createTableRow("Forward cov.", data[1], data[2]) : createTableRow("Forward cov", data[1]));
//        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[3], data[4]) : createTableRow("Reverse cov.", data[2]));
//        sb.append("</table>");
//
//        sb.append("<table>");
//        sb.append("<tr><td align=\"left\"><b>Track 1(Orange):</b></td></tr>");
//
//        sb.append(hasNormFac ? createTableRow("Forward cov.", data[5], data[6]) : createTableRow("Forward cov.", data[3]));
//        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[7], data[8]) : createTableRow("Reverse cov.", data[4]));
//        sb.append("</table>");
//
//        sb.append("<table>");
//        sb.append("<tr><td align=\"left\"><b>Track 2(Cyan):</b></td></tr>");
//
//        sb.append(hasNormFac ? createTableRow("Forward cov.", data[9], data[10]) : createTableRow("Forward cov.", data[5]));
//        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[11], data[12]) : createTableRow("Reverse cov.", data[6]));
//        sb.append("</table>");
//        sb.append("</html>");
//        return sb.toString();
//    }
    
}