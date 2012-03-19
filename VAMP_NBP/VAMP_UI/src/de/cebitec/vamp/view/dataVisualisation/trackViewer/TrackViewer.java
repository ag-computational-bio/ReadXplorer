package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.GenomeRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
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

    private NormalizationSettings normSetting = null;
    private static final long serialVersionUID = 572406471;
    private TrackConnector trackCon;
    private ArrayList<Integer> trackIDs ;
    private PersistantCoverage cov;
    private boolean covLoaded;
    private boolean twoTracks;
    private int id1;
    private int id2 ;
    private boolean colorChanges;
    public boolean hasNormalizationFactor = false;
    
    private JSlider slider = null;


 
    private static int height = 300;
    private CoverageInfoI trackInfo;
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
     */
    public TrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference refGen, 
            TrackConnector trackCon, boolean combineTracks){
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
       
         
        final Preferences pref = NbPreferences.forModule(Object.class);
        this.setColors(pref);

        pref.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                TrackViewer.this.setColors(pref);
                repaint();
            }
        });
        this.setViewerSize();
    }

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
        Map<Object, Object> hints = new HashMap<Object, Object>();
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

                Color complete = ColorProperties.COMPLETE_COV;
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
                g.setColor(complete);
                g.fill(zFw);
                g.draw(zFw);
                g.fill(zRv);
                g.draw(zRv);

            }

        } else {
            g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
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

    
    private double getCoverageValue(boolean isForwardStrand, int covType, int absPos) {
        double value = 0;
        int currentCoverage = 0;

        if (!this.twoTracks || this.twoTracks && this.combineTracks) {

            if (isForwardStrand) {
                if (covType == PersistantCoverage.PERFECT) {
                    value = this.cov.getPerfectFwdMult(absPos);
                } else if (covType == PersistantCoverage.BM) {
                    value = this.cov.getBestMatchFwdMult(absPos);
                } else if (covType == PersistantCoverage.NERROR) {
                    int ncovFw = this.cov.getCommonFwdMult(absPos);
                    currentCoverage = ncovFw;
                    value = ncovFw;

                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.PERFECT) {
                    value = this.cov.getPerfectRevMult(absPos);
                } else if (covType == PersistantCoverage.BM) {
                    value = this.cov.getBestMatchRevMult(absPos);
                } else if (covType == PersistantCoverage.NERROR) {

                    int ncovRev = this.cov.getCommonRevMult(absPos);
                    currentCoverage = ncovRev;
                    value = ncovRev;
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
                    value = cov.getCommonFwdMult(absPos);
                    if (this.hasNormalizationFactor) {
                        int value2 = cov.getCommonFwdMultTrack2(absPos);
                        int value1 = cov.getCommonFwdMultTrack1(absPos);
                        value = this.getNormalizedValue(id2, value2) - this.getNormalizedValue(id1, value1);
                        value = value < 0 ? value * -1 : value;
                    }
                } else if (covType == PersistantCoverage.TRACK2) {
                    value = cov.getCommonFwdMultTrack2(absPos);
                    currentCoverage = (int) value;
                    value = this.getNormalizedValue(id2, value);
                } else if (covType == PersistantCoverage.TRACK1) {
                    value = cov.getCommonFwdMultTrack1(absPos);
                    currentCoverage = (int) value;
                    value = this.getNormalizedValue(id1, value);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.DIFF) {
                    value = cov.getCommonRevMult(absPos);
                    if (this.hasNormalizationFactor) {
                        int value2 = cov.getCommonRevMultTrack2(absPos);
                        int value1 = cov.getCommonRevMultTrack1(absPos);
                        value = this.getNormalizedValue(id2, value2) - this.getNormalizedValue(id1, value1);
                        value = value < 0 ? value * -1 : value;
                    }
                } else if (covType == PersistantCoverage.TRACK2) {
                    value = cov.getCommonRevMultTrack2(absPos);
                    currentCoverage = (int) value;
                    value = this.getNormalizedValue(id2, value);

                } else if (covType == PersistantCoverage.TRACK1) {
                    value = cov.getCommonRevMultTrack1(absPos);
                    currentCoverage = (int) value;
                    value = this.getNormalizedValue(id1, value);
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
     * Normalizes the value handed over to the method according to the normalization
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
    private GeneralPath getCoveragePath(boolean isForwardStrand, int covType) {
        GeneralPath p = new GeneralPath();
        int orientation = (isForwardStrand ? -1 : 1);

        PaintingAreaInfo info = getPaintingAreaInfo();
        int low = (orientation < 0 ? info.getForwardLow() : info.getReverseLow());
        // paint every physical position
        p.moveTo(info.getPhyLeft(), low);
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

            p.lineTo(d, low + value * orientation);
        }

        p.lineTo(info.getPhyRight(), low);
        p.closePath();

        return p;
    }

    /**
     * Load coverage information for the current bounds.
     */
    private void requestCoverage() {
        covLoaded = false;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (this.combineTracks) {
            trackCon.addCoverageRequest(new GenomeRequest(getBoundsInfo().getLogLeft(),
                    getBoundsInfo().getLogRight(), this, Properties.COMPLETE_COVERAGE));
        } else { //ordinary coverage request or double track in non-combined mode request
            trackCon.addCoverageRequest(new GenomeRequest(getBoundsInfo().getLogLeft(),
                    getBoundsInfo().getLogRight(), this, Properties.COMPLETE_COVERAGE));
        }
    }

    @Override
    public synchronized void receiveData(Object coverageData){
        if (coverageData instanceof PersistantCoverage) {
            this.cov = (PersistantCoverage) coverageData;
            this.cov.setHighestCoverage(0);
            this.trackInfo.setCoverage(this.cov);
                   
            if (this.cov.isTwoTracks() && !this.combineTracks) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            
            this.computeAutomaticScaling();
            this.covLoaded = true;
            this.repaint();
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        
    }

    @Override
    public void boundsChangedHook() {
        if (this.cov == null || 
                !this.cov.coversBounds(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight())) {
            this.requestCoverage();
        } else {
            // coverage already loaded
            
            this.trackInfo.setCoverage(this.cov);
            
            if (cov.isTwoTracks()) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            this.computeAutomaticScaling();
            this.computeScaleStep();
            this.covLoaded = true;
        }

        if (this.hasLegend()) {
            this.add(this.getLegendLabel());
            this.add(this.getLegendPanel());
        }
        
        if (this.hasOptions()) {
            this.add(this.getOptionsLabel());
            this.add(this.getOptionsPanel());
        }
    }
    

    private void createCoveragePaths() {
        this.cov.setHighestCoverage(0);
        
        if (!this.getExcludedFeatureTypes().contains(FeatureType.BEST_MATCH_COVERAGE)) {
            bmFw = this.getCoveragePath(true, PersistantCoverage.BM);
            bmRv = this.getCoveragePath(false, PersistantCoverage.BM);

        } else {
            bmFw.reset();
            bmRv.reset();
        }
        if (!this.getExcludedFeatureTypes().contains(FeatureType.PERFECT_COVERAGE)) {
            zFw = this.getCoveragePath(true, PersistantCoverage.PERFECT);
            zRv = this.getCoveragePath(false, PersistantCoverage.PERFECT);
        } else {
            zFw.reset();
            zRv.reset();
        }
        if (!this.getExcludedFeatureTypes().contains(FeatureType.COMPLETE_COV)) {
            nFw = this.getCoveragePath(true, PersistantCoverage.NERROR);
            nRv = this.getCoveragePath(false, PersistantCoverage.NERROR);
        } else {
            nFw.reset();
            nRv.reset();
        }
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

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void changeToolTipText(int logPos) {
        if (covLoaded && twoTracks && !hasNormalizationFactor && !combineTracks) {

            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdMultTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevMultTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdMultTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevMultTrack2(logPos);

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

            int zFwVal = cov.getPerfectFwdMult(logPos);
            int zRvVal = cov.getPerfectRevMult(logPos);
            int bFw = cov.getBestMatchFwdMult(logPos);
            int bRv = cov.getBestMatchRevMult(logPos);
            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);

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

            int zFwVal = cov.getPerfectFwdMult(logPos);
            int zRvVal = cov.getPerfectRevMult(logPos);
            int bFw = cov.getBestMatchFwdMult(logPos);
            int bRv = cov.getBestMatchRevMult(logPos);
            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
    
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

            this.setToolTipText(toolTipSingle(data,hasNormalizationFactor));
        } else if (covLoaded && twoTracks && hasNormalizationFactor && !combineTracks) {

            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdMultTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevMultTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdMultTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevMultTrack2(logPos);


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
        StringBuilder sb = new StringBuilder();
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
        StringBuilder sb = new StringBuilder();
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

    public void setTrackInfoPanel(CoverageInfoI info) {
        this.trackInfo = info;
    }

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
        trackCon = null;
    }

    /**
     * Method to be called when the vertical zoom level of this track viewer was
     * changed, thus the coverage paths have to be recalculated according to the 
     * new zoom level. 
     * @param value the new vertical zoom slider value
     */
    public void verticalZoomLevelUpdated(int value) {
        this.scaleFactor = Math.round(Math.pow(value, 2) / 10);
        this.scaleFactor = (this.scaleFactor < 1 ? 1 : this.scaleFactor);
 
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


    private void computeScaleStep() {
        int visibleCoverage = (int) (this.getPaintingAreaInfo().getAvailableForwardHeight() * this.scaleFactor);

        if (visibleCoverage <= 10) {
            this.scaleLineStep = 1;
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
        } else {
            this.scaleLineStep = 20000;
        }
    }
    
    /**
     * Automatically detects the most suitable scaling value to fit the coverage
     * to the track viewer.
     * This Method transforms highest coverage to slider value, where the slider 
     * values range from 1-200.
     */
    private void computeAutomaticScaling() {
        if (this.cov != null && this.slider != null) {
            
            this.scaleFactor = Math.round(this.cov.getHighestCoverage() / 140.0) + 1;
            this.scaleFactor = this.scaleFactor < 1 ? 1.0 : this.scaleFactor;
            this.scaleFactor = this.scaleFactor > 140000.0 ? this.slider.getMaximum() : this.scaleFactor;

            //set the inverse of the value set in verticalZoomLevelUpdated
            this.slider.setValue((int) Math.round(Math.sqrt(this.scaleFactor * 10)));
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
     * Sets the initial size of the track viewer.
     */
    private void setViewerSize() {
        this.setPreferredSize(new Dimension(1, 300));
        this.revalidate();
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

    public void setVerticalZoomValue(JSlider vzoom) {
        slider = vzoom;
    }

    /**
     * In case this viewer should receive the ability to combine coverages from a
     * PersistantCoverage array, this method provides this functionality.
     * @param coverages the coverages of different tracks, which should be combined
     * @return 
     */
    private PersistantCoverage combineCoverages(PersistantCoverage[] coverages) {
        
        PersistantCoverage resultCov = new PersistantCoverage(coverages[0].getLeftBound(), coverages[0].getRightBound());
        
        for (int i = coverages[0].getLeftBound(); i < coverages[0].getRightBound(); ++i) {
            for (PersistantCoverage cov : coverages) {
                resultCov.setPerfectFwdMult(i, resultCov.getPerfectFwdMult(i) + cov.getPerfectFwdMult(i));
                resultCov.setPerfectFwdNum(i, resultCov.getPerfectFwdNum(i) + cov.getPerfectFwdNum(i));
                resultCov.setPerfectRevMult(i, resultCov.getPerfectRevMult(i) + cov.getPerfectRevMult(i));
                resultCov.setPerfectRevNum(i, resultCov.getPerfectRevNum(i) + cov.getPerfectRevNum(i));
                
                resultCov.setBestMatchFwdMult(i, resultCov.getBestMatchFwdMult(i) + cov.getBestMatchFwdMult(i));
                resultCov.setBestMatchFwdNum(i, resultCov.getBestMatchFwdNum(i) + cov.getBestMatchFwdNum(i));
                resultCov.setBestMatchRevMult(i, resultCov.getBestMatchRevMult(i) + cov.getBestMatchRevMult(i));
                resultCov.setBestMatchRevNum(i, resultCov.getBestMatchRevNum(i) + cov.getBestMatchRevNum(i));
                
                resultCov.setCommonFwdMult(i, resultCov.getCommonFwdMult(i) + cov.getCommonFwdMult(i));
                resultCov.setCommonFwdNum(i, resultCov.getCommonFwdNum(i) + cov.getCommonFwdNum(i));
                resultCov.setCommonRevMult(i, resultCov.getCommonRevMult(i) + cov.getCommonRevMult(i));
                resultCov.setCommonRevNum(i, resultCov.getCommonRevNum(i) + cov.getCommonRevNum(i));
            }
        }
        
        return resultCov;
    }

    /**
     * @return true, if this is a track viewer for at least two tracks.
     */
    public boolean isTwoTracks() {
        return this.twoTracks;
    }    

}
