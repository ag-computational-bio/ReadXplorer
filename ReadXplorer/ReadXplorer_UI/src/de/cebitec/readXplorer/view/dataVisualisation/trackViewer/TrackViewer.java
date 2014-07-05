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

import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JSlider;
import org.openide.util.NbPreferences;

/**
 * Display the coverage for a sequenced track related to a reference genome
 * @author ddoppmeier
 */
public class TrackViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 572406471;
    private static final int MININTERVALLENGTH = 25000;
    
    private final Preferences pref = NbPreferences.forModule(Object.class);
    private NormalizationSettings normSetting = null;
    private TrackConnector trackCon;
    private List<Integer> trackIDs ;
    private PersistantCoverage cov;
    private boolean covLoaded;
    private boolean twoTracks;
    private int id1;
    private int id2 ;
    private boolean colorChanges;
    private boolean hasNormalizationFactor = false;
    private boolean automaticScaling = pref.getBoolean(Properties.VIEWER_AUTO_SCALING, false);
    private boolean useMinimalIntervalLength = true;
    
    private JSlider verticalSlider = null;
 
//    private CoverageInfoI trackInfo;
    private double scaleFactor;
    private int scaleLineStep;
    private int labelMargin;
    // create pathes for the coverages
    private GeneralPath bmFw;
    private GeneralPath bmRv;
    private GeneralPath zFw;
    private GeneralPath zRv;
    private GeneralPath nFw;
    private GeneralPath nRv;
    // colors for the pathes
    private static Color bmC = ColorProperties.BEST_MATCH;
    private static Color zC = ColorProperties.PERFECT_MATCH;
    private static Color nC = ColorProperties.COMMON_MATCH;
 //   public static final String PROP_TRACK_CLICKED = "track clicked";
  //  public static final String PROP_TRACK_ENTERED = "track entered";
    private boolean combineTracks;

    /**
     * Create a new panel to show coverage information
     * @param boundsManager manager for component bounds
     * @param basePanel 
     * @param refGen reference genome
     * @param trackCon database connection to one track, that is displayed
     * @param combineTracks true, if the coverage of the tracks contained in the
     *      track connector should be combined.
     */
    public TrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference refGen, 
            TrackConnector trackCon, boolean combineTracks) {
        super(boundsManager, basePanel, refGen);
        
        this.trackCon = trackCon;
        this.twoTracks = this.trackCon.getAssociatedTrackNames().size() > 1; 
        this.combineTracks = combineTracks;
        trackIDs = trackCon.getTrackIds();
        id1 = trackIDs.get(0);
        id2 = trackIDs.size() == 2 ? trackIDs.get(1) : -1;
        labelMargin = 3;
        scaleFactor = 1;
        covLoaded = false;
        bmFw = new GeneralPath();
        bmRv = new GeneralPath();
        zFw = new GeneralPath();
        zRv = new GeneralPath();
        nFw = new GeneralPath();
        nRv = new GeneralPath();
       
         
        this.setColors(pref);

        pref.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                TrackViewer.this.setColors(pref);
                repaint();
            }
        });
        
        this.setSizes();          
    }

    /**
     * Updates the colors of the coverage in this viewer.
     * @param pref The preference object containing the new colors
     */
    private void setColors(Preferences pref) {
        boolean uniformColouration = pref.getBoolean("uniformDesired", false);
        if (uniformColouration) {
            String colourRGB = pref.get("uniformColour", "");
            if (!colourRGB.isEmpty()) {
                bmC = new Color(Integer.parseInt(colourRGB));
                nC = new Color(Integer.parseInt(colourRGB));
                zC = new Color(Integer.parseInt(colourRGB));
            }
        } else {
            String bestColour = pref.get("bestMatchColour", "");
            String perfectColour = pref.get("perfectMatchColour", "");
            String commonColour = pref.get("commonMatchColour", "");

            if (!bestColour.isEmpty()) {
                bmC = new Color(Integer.parseInt(bestColour));
            }
            if (!perfectColour.isEmpty()) {
                zC = new Color(Integer.parseInt(perfectColour));
            }
            if (!commonColour.isEmpty()) {
                nC = new Color(Integer.parseInt(commonColour));
            }
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        // set rendering hints
        Map<Object, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHints(hints);

        if (this.covLoaded || this.colorChanges) {
            if (!this.twoTracks || this.twoTracks && this.combineTracks) {
                               
                // fill and draw all coverage pathes

                // n error mappings
                g.setColor(nC);
                g.fill(nFw);
                g.draw(nFw);
                g.fill(nRv);
                g.draw(nRv);

                // best match mappings
                g.setColor(bmC);
                g.fill(bmFw);
                g.draw(bmFw);
                g.fill(bmRv);
                g.draw(bmRv);

                // zero error mappings
                g.setColor(zC);
                g.fill(zFw);
                g.draw(zFw);
                g.fill(zRv);
                g.draw(zRv);
            } else {
                // fill and draw all coverage pathes

                Color difference = ColorProperties.COV_DIFF_COLOR;
                Color track1 = ColorProperties.TRACK1_COLOR;
                Color track2 = ColorProperties.TRACK2_COLOR;

                // track 1 n cov
                g.setColor(track1);
                g.fill(nFw);
                g.draw(nFw);
                g.fill(nRv);
                g.draw(nRv);

                // track2 n cov
                g.setColor(track2);
                g.fill(bmFw);
                g.draw(bmFw);
                g.fill(bmRv);
                g.draw(bmRv);

                // diff n cov
                g.setColor(difference);
                g.fill(zFw);
                g.draw(zFw);
                g.fill(zRv);
                g.draw(zRv);

            }

        } else {
            Color fillcolor = ColorProperties.TITLE_BACKGROUND;
            g.setColor(fillcolor);
            BufferedImage loadingIndicator = this.getLoadingIndicator();
            if (loadingIndicator != null) {
                g.drawImage(loadingIndicator, this.getWidth() - 60 - loadingIndicator.getWidth(), 5, loadingIndicator.getWidth(), loadingIndicator.getHeight(), this);
            }
            //g.fillRect(0, 0, this.getHeight()/4, this.getHeight()/4); //this.getWidth(), this.getHeight()/3);
        }

        // draw scales
        g.setColor(ColorProperties.TRACKPANEL_SCALE_LINES);
        this.createLines(this.scaleLineStep, g);

        // draw black middle line
        g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
        drawBaseLines(g);
    }

    private void drawBaseLines(Graphics2D graphics) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine(info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow());
        graphics.drawLine(info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow());
    }

    /**
     * Returns the coverage value for the given strand, coverage type and position.
     * @param isForwardStrand if -1, coverage is drawn from bottom to top, if 1 otherwise
     * @param covType the mapping classification type of the coverage path handled here
     * @param absPos the reference position for which the coverage should be obtained
     * @return the coverage value for the given strand, coverage type and position.
     */
    private double getCoverageValue(boolean isForwardStrand, byte covType, int absPos) {
        double value = 0;

        if (!this.twoTracks || this.twoTracks && this.combineTracks) {

            if (isForwardStrand) {
                if (covType == PersistantCoverage.PERFECT) {
                    value = this.cov.getPerfectFwd(absPos);
                } else if (covType == PersistantCoverage.BM) {
                    value = this.cov.getBestMatchFwd(absPos);
                } else if (covType == PersistantCoverage.NERROR) {
                    value = this.cov.getCommonFwd(absPos);

                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.PERFECT) {
                    value = this.cov.getPerfectRev(absPos);
                } else if (covType == PersistantCoverage.BM) {
                    value = this.cov.getBestMatchRev(absPos);
                } else if (covType == PersistantCoverage.NERROR) {
                    value = this.cov.getCommonRev(absPos);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            }
            
            if (this.hasNormalizationFactor) {
                value = this.getNormalizedValue(this.id1, value);
            }

        } else {
            if (isForwardStrand) {
                if (covType == PersistantCoverage.DIFF) {
                    int value1 = cov.getCommonFwdTrack1(absPos);
                    int value2 = cov.getCommonFwdTrack2(absPos);
                    if (this.hasNormalizationFactor) {
                        value1 = (int) this.getNormalizedValue(id1, value1);
                        value2 = (int) this.getNormalizedValue(id2, value2);
                    }
                    value = Math.abs(value2 - value1);
                } else if (covType == PersistantCoverage.TRACK2) {
                    value = cov.getCommonFwdTrack2(absPos);
                    if (this.hasNormalizationFactor) {
                        value = this.getNormalizedValue(id2, value);
                    }
                } else if (covType == PersistantCoverage.TRACK1) {
                    value = cov.getCommonFwdTrack1(absPos);
                    if (this.hasNormalizationFactor) {
                        value = this.getNormalizedValue(id1, value);
                    }
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.DIFF) {
                    int value1 = cov.getCommonRevTrack1(absPos);
                    int value2 = cov.getCommonRevTrack2(absPos);
                    if (this.hasNormalizationFactor) {
                        value1 = (int) this.getNormalizedValue(id1, value1);
                        value2 = (int) this.getNormalizedValue(id2, value2);
                    }
                    value = Math.abs(value2 - value1);
                } else if (covType == PersistantCoverage.TRACK2) {
                    value = cov.getCommonRevTrack2(absPos);
                    if (this.hasNormalizationFactor) {
                        value = this.getNormalizedValue(id2, value);
                    }
                } else if (covType == PersistantCoverage.TRACK1) {
                    value = cov.getCommonRevTrack1(absPos);
                    if (this.hasNormalizationFactor) {
                        value = this.getNormalizedValue(id1, value);
                    }
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            }
        }
        
        if (value > this.cov.getHighestCoverage()) {
            this.cov.setHighestCoverage((int) Math.ceil(value));
        }
        
        return value;

    }
    
    /**
     * Normalizes the value handed over to the method acodeording to the normalization
     * method choosen for the given track.
     * @param trackID the track id this value belongs to
     * @param value the value that should be normalized
     * @return the normalized value.
     */
    private double getNormalizedValue(int trackID, double value) {
        if (this.normSetting != null && this.normSetting.getHasNormFac(trackID)) {
            return this.normSetting.getIsLogNorm(trackID) ? TrackViewer.log2(value) : value * this.normSetting.getFactors(trackID);
        } else {
            return value;
        }
    }

    /**
     * Create a GeneralPath that represents the coverage of a certain type.
     * @param isForwardStrand if -1, coverage is drawn from bottom to top, if 1 otherwise
     * @param covType the type of the coverage path handled here
     * @return GeneralPath representing the coverage of a certain type
     */
    private GeneralPath getCoveragePath(boolean isForwardStrand, byte covType) {
        GeneralPath covPath = new GeneralPath();
        int orientation = (isForwardStrand ? SequenceUtils.STRAND_REV : SequenceUtils.STRAND_FWD);

        PaintingAreaInfo info = getPaintingAreaInfo();
        int low = (orientation < 0 ? info.getForwardLow() : info.getReverseLow());
        // paint every physical position
        covPath.moveTo(info.getPhyLeft(), low);
        for (int d = info.getPhyLeft(); d < info.getPhyRight(); d++) {

            int left = this.transformToLogicalCoord(d);
            int right = this.transformToLogicalCoord(d + 1) - 1;

            // physical coordinate d and d+1 may cover the same base, depending on zoomlevel,
            // if not compute max of range of values represented at position d
            double value;
            if (right > left) {

                double max = 0;
                for (int i = left; i <= right; i++) {
                    if (this.getCoverageValue(isForwardStrand, covType, i) > max) {
                        max = this.getCoverageValue(isForwardStrand, covType, i);
                    }
                }
                value = max;

            } else {
                value = this.getCoverageValue(isForwardStrand, covType, left);
            }

            value = this.getCoverageYValue(value);
            if (orientation < 0) {
                // forward
                if (!this.getPaintingAreaInfo().fitsIntoAvailableForwardSpace(value)) {
                    value = getPaintingAreaInfo().getAvailableForwardHeight();
                }
            } else {
                // reverse
                if (!this.getPaintingAreaInfo().fitsIntoAvailableReverseSpace(value)) {
                    value = getPaintingAreaInfo().getAvailableReverseHeight();
                }
            }

            covPath.lineTo(d, low + value * orientation);
        }

        covPath.lineTo(info.getPhyRight(), low);
        covPath.closePath();

        return covPath;
    }

    /**
     * Load coverage information for the current bounds.
     */
    private void requestCoverage() {
        covLoaded = false;
        this.setNewDataRequestNeeded(false);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int totalFrom = getBoundsInfo().getLogLeft();
        int totalTo = getBoundsInfo().getLogRight();
        if (this.useMinimalIntervalLength) {
            totalFrom -= MININTERVALLENGTH;
            totalTo += MININTERVALLENGTH;
        }
        trackCon.addCoverageRequest(new IntervalRequest(
                getBoundsInfo().getLogLeft(), 
                getBoundsInfo().getLogRight(), 
                totalFrom ,
                totalTo , 
                this.getReference().getActiveChromId(), this, false, this.getReadClassParams()));
    }

    @Override
    public synchronized void receiveData(Object coverageData) {
        if (coverageData instanceof CoverageAndDiffResultPersistant) {
            CoverageAndDiffResultPersistant covResult = (CoverageAndDiffResultPersistant) coverageData;
            this.cov = covResult.getCoverage();
            this.cov.setHighestCoverage(0);
//            this.trackInfo.setCoverage(this.cov);

            if (this.cov.isTwoTracks() && !this.combineTracks) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            
            this.computeAutomaticScaling();
            this.computeScaleStep();
            this.covLoaded = true;
            this.repaint();
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void boundsChangedHook() {
        if (this.cov == null || this.isNewDataRequestNeeded() ||
                !this.cov.coversBounds(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight())) {
            this.requestCoverage();
        } else {
            // coverage already loaded
            
//            this.trackInfo.setCoverage(this.cov);
            
            if (cov.isTwoTracks()) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            this.computeAutomaticScaling();
            this.covLoaded = true;
        }
        
        if (this.hasOptions()) {
            this.add(this.getOptionsLabel());
            this.add(this.getOptionsPanel());
        }
        
        if (this.hasLegend()) {
            this.add(this.getLegendLabel());
            this.add(this.getLegendPanel());
        }
    }
    
    /**
     * Creates the coverage paths, which are later painted in the viewer.
     */
    private void createCoveragePaths() {
        this.cov.setHighestCoverage(0);
        
//        if (!this.getExcludedFeatureTypes().contains(FeatureType.PERFECT_COVERAGE)) {
            zFw = this.getCoveragePath(true, PersistantCoverage.PERFECT);
            zRv = this.getCoveragePath(false, PersistantCoverage.PERFECT);
//        } else {
//            zFw.reset();
//            zRv.reset();
//        }
//        if (!this.getExcludedFeatureTypes().contains(FeatureType.BEST_MATCH_COVERAGE)) {
            bmFw = this.getCoveragePath(true, PersistantCoverage.BM);
            bmRv = this.getCoveragePath(false, PersistantCoverage.BM);
//
//        } else {
//            bmFw.reset();
//            bmRv.reset();
//        }
//        if (!this.getExcludedFeatureTypes().contains(FeatureType.COMMON_COVERAGE)) {
            nFw = this.getCoveragePath(true, PersistantCoverage.NERROR);
            nRv = this.getCoveragePath(false, PersistantCoverage.NERROR);
//        } else {
//            nFw.reset();
//            nRv.reset();
//        }
    }

    
    private void createCoveragePathsDiffOfTwoTracks() {
        this.cov.setHighestCoverage(0);
        
        nFw = this.getCoveragePath(true, PersistantCoverage.TRACK1);
        nRv = this.getCoveragePath(false, PersistantCoverage.TRACK1);
        bmFw = this.getCoveragePath(true, PersistantCoverage.TRACK2);
        bmRv = this.getCoveragePath(false, PersistantCoverage.TRACK2);
        zFw = this.getCoveragePath(true, PersistantCoverage.DIFF);
        zRv = this.getCoveragePath(false, PersistantCoverage.DIFF);
    }

    /**
     * @return The maximal height for this viewer.
     */
    @Override
    public int getMaximalHeight() {
        return pref.getInt(Properties.VIEWER_HEIGHT, Properties.DEFAULT_HEIGHT);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void changeToolTipText(int logPos) {
        if (covLoaded && twoTracks && !hasNormalizationFactor && !combineTracks) {

            int nFwVal = cov.getCommonFwd(logPos);
            int nRvVal = cov.getCommonRev(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevTrack2(logPos);

            Double[] data = new Double[7];
            data[0] = (double) logPos;
            data[1] = (double) nFwVal;
            data[2] = (double) nRvVal;
            data[3] = (double) nFwValTrack1;
            data[4] = (double) nRvValTrack1;
            data[5] = (double) nFwValTrack2;
            data[6] = (double) nRvValTrack2;


            this.setToolTipText(toolTipDouble(data, hasNormalizationFactor));

        } else if (covLoaded && (!twoTracks || this.combineTracks) && !hasNormalizationFactor) {

            int zFwVal = cov.getPerfectFwd(logPos);
            int zRvVal = cov.getPerfectRev(logPos);
            int bFw = cov.getBestMatchFwd(logPos);
            int bRv = cov.getBestMatchRev(logPos);
            int nFwVal = cov.getCommonFwd(logPos);
            int nRvVal = cov.getCommonRev(logPos);

            Double[] data = new Double[7];
            data[0] = (double) logPos;
            data[1] = (double) zFwVal;
            data[2] = (double) zRvVal;
            data[3] = (double) bFw;
            data[4] = (double) bRv;
            data[5] = (double) nFwVal;
            data[6] = (double) nRvVal;
            
            this.setToolTipText(toolTipSingle(data, hasNormalizationFactor));
            
        } else if (covLoaded && (!twoTracks || this.combineTracks) && hasNormalizationFactor) {

            int zFwVal = cov.getPerfectFwd(logPos);
            int zRvVal = cov.getPerfectRev(logPos);
            int bFw = cov.getBestMatchFwd(logPos);
            int bRv = cov.getBestMatchRev(logPos);
            int nFwVal = cov.getCommonFwd(logPos);
            int nRvVal = cov.getCommonRev(logPos);
    
            double zFwValScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,zFwVal));
            double zRvValScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,zRvVal));
            double bFwScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,bFw));
            double bRvScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,bRv));
            double nFwValScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,nFwVal));
            double nRvValScale = TrackViewer.threeDecAfter(getNormalizedValue(id1,nRvVal));

            Double[] data = new Double[13];
            data[0] = (double) logPos;
            data[1] = (double) zFwVal;
            data[2] = zFwValScale;
            data[3] = (double) zRvVal;
            data[4] = zRvValScale;
            data[5] = (double) bFw;
            data[6] = bFwScale; 
            data[7] = (double) bRv;
            data[8] = bRvScale;
            data[9] = (double) nFwVal;
            data[10] = nFwValScale;
            data[11] = (double) nRvVal;
            data[12] = nRvValScale;

            this.setToolTipText(toolTipSingle(data, hasNormalizationFactor));
            
        } else if (covLoaded && twoTracks && hasNormalizationFactor && !combineTracks) {

            int nFwVal = cov.getCommonFwd(logPos);
            int nRvVal = cov.getCommonRev(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevTrack2(logPos);


            double nFwScaleTrack1 = TrackViewer.threeDecAfter(getNormalizedValue(id1,nFwValTrack1));
            double nRvScaleTrack1 = TrackViewer.threeDecAfter(getNormalizedValue(id1,nRvValTrack1));
            double nFwValScaleTrack2 = TrackViewer.threeDecAfter(getNormalizedValue(id2,nFwValTrack2));
            double nRvValScaleTrack2 = TrackViewer.threeDecAfter(getNormalizedValue(id2,nRvValTrack2));
            
            double diffFw = (nFwValScaleTrack2 - nFwScaleTrack1);
            double diffRv = (nRvValScaleTrack2 - nRvScaleTrack1);
            double nFwValScale = TrackViewer.threeDecAfter(diffFw < 0 ? diffFw *-1 : diffFw);
            double nRvValScale = TrackViewer.threeDecAfter(diffRv < 0 ? diffRv *-1 : diffRv);
            nFwValScale = nFwValScale < 0 ? nFwValScale *-1 : nFwValScale;
            nRvValScale = nRvValScale < 0 ? nRvValScale *-1 : nRvValScale;
             
            Double[] data = new Double[13];
            data[0] = (double) logPos;
            data[1] = (double) nFwVal;
            data[2] = nFwValScale;
            data[3] = (double) nRvVal;
            data[4] = nRvValScale;
            data[5] = (double) nFwValTrack1;
            data[6] = nFwScaleTrack1;
            data[7] = (double) nRvValTrack1;
            data[8] = nRvScaleTrack1;
            data[9] =  (double) nFwValTrack2;
            data[10] = nFwValScaleTrack2;
            data[11] = (double) nRvValTrack2;
            data[12] = nRvValScaleTrack2;
            
            this.setToolTipText(toolTipDouble(data,hasNormalizationFactor));
            
        } else {
            this.setToolTipText(null);
        }
    }

    private String toolTipDouble(Double[] data, boolean hasNormFac) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("<html>");
        sb.append("<b>Position</b>: ").append(data[0]);
        sb.append("<br>");
        sb.append("<table>");
        sb.append("<tr><td align=\"left\"><b>Difference(Blue):</b></td></tr>");
        sb.append(hasNormFac ? createTableRow("Forward cov.", data[1], data[2]) : createTableRow("Forward cov", data[1]));
        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[3], data[4]) : createTableRow("Reverse cov.", data[2]));
        sb.append("</table>");

        sb.append("<table>");
        sb.append("<tr><td align=\"left\"><b>Track 1(Orange):</b></td></tr>");

        sb.append(hasNormFac ? createTableRow("Forward cov.", data[5], data[6]) : createTableRow("Forward cov.", data[3]));
        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[7], data[8]) : createTableRow("Reverse cov.", data[4]));
        sb.append("</table>");

        sb.append("<table>");
        sb.append("<tr><td align=\"left\"><b>Track 2(Cyan):</b></td></tr>");

        sb.append(hasNormFac ? createTableRow("Forward cov.", data[9], data[10]) : createTableRow("Forward cov.", data[5]));
        sb.append(hasNormFac ? createTableRow("Reverse cov.", data[11], data[12]) : createTableRow("Reverse cov.", data[6]));
        sb.append("</table>");
        sb.append("</html>");
        return sb.toString();
    }

    private String toolTipSingle(Double[] data, boolean hasNormFac) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("<html>");
        sb.append("<b>Position</b>: ").append(data[0]);
        sb.append("<br>");
        sb.append("<table>");
        sb.append("<tr><td align=\"left\"><b>Forward strand</b></td></tr>");
        sb.append(hasNormFac ? createTableRow("Perfect match cov.", data[1], data[2]) : createTableRow("Perfect match cov.", data[1]));
        sb.append(hasNormFac ? createTableRow("Best match cov.", data[5], data[6]) : createTableRow("Best match cov.", data[3]));
        sb.append(hasNormFac ? createTableRow("Complete cov.", data[9], data[10]) : createTableRow("Complete cov.", data[5]));
        sb.append("</table>");

        sb.append("<table>");
        sb.append("<tr><td align=\"left\"><b>Reverse strand</b></td></tr>");
        sb.append(hasNormFac ? createTableRow("Perfect match cov.", data[3], data[4]) : createTableRow("Perfect match cov.", data[2]));
        sb.append(hasNormFac ? createTableRow("Best match cov.", data[7], data[8]) : createTableRow("Best match cov.", data[4]));
        sb.append(hasNormFac ? createTableRow("Complete cov.", data[11], data[12]) : createTableRow("Complete cov.", data[6]));
        sb.append("</table>");
        sb.append("</html>");

        return sb.toString();
    }

    private String createTableRow(String label, double value) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf((int) value) + "</td></tr>";
    }

    private String createTableRow(String label, double value, double scaleFacVal) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf(scaleFacVal) + " (" + String.valueOf((int) value) + ")" + "</td></tr>";
    }

//    public void setTrackInfoPanel(CoverageInfoI info) {
//        this.trackInfo = info;
//    }

    /**
     * @param coverage the coverage for a certain position
     * @return The current y value of the given coverage path. This represents
     * the absoulte position on the screen (pixel) up to which the coverage path
     * should reach.
     */
    private int getCoverageYValue(double coverage) {
        int value = (int) Math.round(coverage / this.scaleFactor);
        if (coverage > 0) {
            value = (value > 0 ? value : 1);
        }

        return value;
    }

    @Override
    public void close() {
        super.close();
        ProjectConnector.getInstance().removeTrackConnector(trackCon.getTrackID());
        trackCon = null; //TODO: close does not work correctly always!
    }

    /**
     * Method to be called when the vertical zoom level of this track viewer was
     * changed, thus the coverage paths have to be recalculated acodeording to the 
     * new zoom level. A scaleFactor of 1 means a 1:1 translation of coverage to 
     * pixels. A value smaller than 1 is adjusted to 1.
     * @param value the new vertical zoom slider value
     */
    public void verticalZoomLevelUpdated(int value) {
        this.scaleFactor = value < 1 ? 1 : Math.pow(value, 2);
 
        if (this.cov != null) {
            if (this.cov.isTwoTracks() && !this.combineTracks) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
        }
        
        this.computeScaleStep();
        this.repaint();
    }

    private static double threeDecAfter(double val) {
        int tmp = (int) (val * 1000);
        return tmp / 1000.0;
    }

    private static double log2(double num) {
        num = num == 0 ? 1 : num;
        return (Math.log(num) / Math.log(2));
    }

    /**
     * Computes the scale line step value, which should be used for the current
     * coverage values.
     */
    private void computeScaleStep() {
        //A scaleFactor of 1 means a 1:1 translation of coverage to pixels.
        int visibleCoverage = (int) (this.getPaintingAreaInfo().getAvailableForwardHeight() * this.scaleFactor);

        if (visibleCoverage <= 10) {
            this.scaleLineStep = 1;
        } else if (visibleCoverage <= 50) {
            this.scaleLineStep = 10;
        } else if (visibleCoverage <= 100) {
            this.scaleLineStep = 20;
        } else if (visibleCoverage <= 200) {
            this.scaleLineStep = 50;
        } else if (visibleCoverage <= 500) {
            this.scaleLineStep = 100;
        } else if (visibleCoverage <= 1000) {
            this.scaleLineStep = 250;
        } else if (visibleCoverage <= 3000) {
            this.scaleLineStep = 500;
        } else if (visibleCoverage <= 4000) {
            this.scaleLineStep = 750;
        } else if (visibleCoverage <= 7500) {
            this.scaleLineStep = 1000;
        } else if (visibleCoverage <= 15000) {
            this.scaleLineStep = 2500;
        } else if (visibleCoverage <= 25000) {
            this.scaleLineStep = 5000;
        } else if (visibleCoverage <= 45000) {
            this.scaleLineStep = 7500;
        } else if (visibleCoverage <= 65000) {
            this.scaleLineStep = 10000;
        } else if (visibleCoverage <= 200000) {
            this.scaleLineStep = 20000;
        } else if (visibleCoverage <= 500000) {
            this.scaleLineStep = 50000;
        } else if (visibleCoverage <= 1000000) {
            this.scaleLineStep = 100000;
        } else {
            this.scaleLineStep = 300000;
        }
    }
    
    /**
     * Automatically detects the most suitable scaling value to fit the coverage
     * to the track viewer.
     * This Method transforms highest coverage to slider value, where the slider 
     * values range from 1-200. A scaleFactor of 1 means a 1:1 translation of 
     * coverage to pixels. A larger scaleFactor means, that the coverage is
     * shrinked to fit the available painting area.
     */
    private void computeAutomaticScaling() {
        if (this.automaticScaling && this.cov != null && this.verticalSlider != null) {
            double oldScaleFactor = this.scaleFactor;
            double availablePixels = this.getPaintingAreaInfo().getAvailableForwardHeight();
            this.scaleFactor = Math.ceil(this.cov.getHighestCoverage() / availablePixels);
            this.scaleFactor = this.scaleFactor < 1 ? 1.0 : this.scaleFactor;

            //set the inverse of the value set in verticalZoomLevelUpdated
            this.verticalSlider.setValue((int) (Math.ceil(Math.sqrt(this.scaleFactor))));
            if (oldScaleFactor != this.scaleFactor) {
                this.createCoveragePaths();
                this.repaint();
            }
        }
    }

    private void createLines(int step, Graphics2D g) {
        PaintingAreaInfo info = this.getPaintingAreaInfo();

        int tmp = step;
        int physY = getCoverageYValue(step);

        while (physY <= info.getAvailableForwardHeight()) {

            int forwardY = info.getForwardLow() - physY;
            int reverseY = info.getReverseLow() + physY;

            int lineLeft = info.getPhyLeft();
            int lineRight = info.getPhyRight();

            g.draw(new Line2D.Double(lineLeft, reverseY, lineRight, reverseY));
            g.draw(new Line2D.Double(lineLeft, forwardY, lineRight, forwardY));

            int labelHeight = g.getFontMetrics().getMaxAscent();
            String label = getLabel(tmp, step);


            tmp += step;
            physY = getCoverageYValue(tmp);

            int labelLeft = lineLeft - labelMargin - g.getFontMetrics().stringWidth(label);
            int labelRight = lineRight + labelMargin;

            g.drawString(label, labelLeft, reverseY + labelHeight / 2);
            g.drawString(label, labelLeft, forwardY + labelHeight / 2);
            // right labels
            g.drawString(label, labelRight, reverseY + labelHeight / 2);
            g.drawString(label, labelRight, forwardY + labelHeight / 2);
        }
    }

    private String getLabel(int logPos, int step) {
        String label = null;
        if (logPos >= 1000 && step >= 1000) {
            if (logPos % 1000 == 0) {
                label = String.valueOf(logPos / 1000);
            } else if (logPos % 500 == 0) {
                label = String.valueOf(logPos / 1000);
                label += ".5";
            }
            label += "K";

        } else {
            label = String.valueOf(logPos);
        }

        return label;
    }

    /**
     * Method for updating this track viewer, when the normalization value was changed.
     */
    public void normalizationValueChanged() {
        this.hasNormalizationFactor = this.normSetting.getIdToValue().keySet().size() == 2 ? 
            (normSetting.getHasNormFac(id1) || normSetting.getHasNormFac(id2)) : normSetting.getHasNormFac(id1);
        this.boundsChangedHook();
        this.repaint();
    }

    public void colorChanges() {
        this.colorChanges = true;
        this.repaint();
    }

    public TrackConnector getTrackCon() {
        return this.trackCon;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return true, if this is a double track viewer, which combines the selected
     * tracks into a single coverage wave.
     */
    public boolean isCombineTracks() {
        return this.combineTracks;
    }
    
    public NormalizationSettings getNormalizationSettings() {
        return normSetting;
    }

    public void setNormalizationSettings(NormalizationSettings normSetting) {
        this.normSetting = normSetting;
    }

    public void setVerticalZoomSlider(JSlider verticalSlider) {
        this.verticalSlider = verticalSlider;
    }

//    /**
//     * In case this viewer should receive the ability to combine coverages from a
//     * PersistantCoverage array, this method provides this functionality.
//     * @param coverages the coverages of different tracks, which should be combined
//     * @return 
//     */
//    private PersistantCoverage combineCoverages(PersistantCoverage[] coverages) {
//        
//        PersistantCoverage resultCov = new PersistantCoverage(coverages[0].getLeftBound(), coverages[0].getRightBound());
//        
//        for (int i = coverages[0].getLeftBound(); i < coverages[0].getRightBound(); ++i) {
//            for (PersistantCoverage cove : coverages) {
//                resultCov.setPerfectFwd(i, resultCov.getPerfectFwd(i) + cove.getPerfectFwd(i));
//                resultCov.setPerfectFwdNum(i, resultCov.getPerfectFwdNum(i) + cove.getPerfectFwdNum(i));
//                resultCov.setPerfectRev(i, resultCov.getPerfectRev(i) + cove.getPerfectRev(i));
//                resultCov.setPerfectRevNum(i, resultCov.getPerfectRevNum(i) + cove.getPerfectRevNum(i));
//                
//                resultCov.setBestMatchFwd(i, resultCov.getBestMatchFwd(i) + cove.getBestMatchFwd(i));
//                resultCov.setBestMatchFwdNum(i, resultCov.getBestMatchFwdNum(i) + cove.getBestMatchFwdNum(i));
//                resultCov.setBestMatchRev(i, resultCov.getBestMatchRev(i) + cove.getBestMatchRev(i));
//                resultCov.setBestMatchRevNum(i, resultCov.getBestMatchRevNum(i) + cove.getBestMatchRevNum(i));
//                
//                resultCov.setCommonFwd(i, resultCov.getCommonFwd(i) + cove.getCommonFwd(i));
//                resultCov.setCommonFwdNum(i, resultCov.getCommonFwdNum(i) + cove.getCommonFwdNum(i));
//                resultCov.setCommonRev(i, resultCov.getCommonRev(i) + cove.getCommonRev(i));
//                resultCov.setCommonRevNum(i, resultCov.getCommonRevNum(i) + cove.getCommonRevNum(i));
//            }
//        }
//        
//        return resultCov;
//    }

    /**
     * @return true, if this is a track viewer for at least two tracks.
     */
    public boolean isTwoTracks() {
        return this.twoTracks;
    }

    /**
     * @param automaticScaling Set <code>true</code>, if the coverage slider
     * should automatically adapt to the coverage shown (the complete coverage
     * in the interval always is visible). <code>false</code>, if the slider
     * value should only be changed manually by the user.
     */
    public void setAutomaticScaling(boolean automaticScaling) {
        this.automaticScaling = automaticScaling;
        this.computeAutomaticScaling();
    }

    @Override
    public void notifySkipped() {
        //do nothing
    }

    /**
     * @return <code>true</code> if the queried interval length should be extended
     * to the <code>MININTERVALLENGTH</code>, <code>false</code> if the original 
     * bounds should be used for the coverage queries.
     */
    public boolean isUseMinimalIntervalLength() {
        return useMinimalIntervalLength;
    }

    /**
     * @param useMinimalIntervalLength <code>true</code> if the queried interval
     * length should be extended to the <code>MININTERVALLENGTH</code>,
     * <code>false</code> if the original bounds should be used for the coverage 
     * queries.
     */
    public void setUseMinimalIntervalLength(boolean useMinimalIntervalLength) {
        this.useMinimalIntervalLength = useMinimalIntervalLength;
    }
    
}