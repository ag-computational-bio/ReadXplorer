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
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.LegendLabel;
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
import javax.swing.JPanel;
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
    public boolean twoTracks;
    private int id1;
    private int id2 ;
    private boolean colorChanges;
    public boolean hasNormalizationFactor = false;


 
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

    /**
     * Create a new panel to show coverage information
     * @param boundsManager manager for component bounds
     * @param basePanel 
     * @param refGen reference genome
     * @param trackCon database connection to one track, that is displayed
     */
    public TrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference refGen, TrackConnector trackCon) {
        super(boundsManager, basePanel, refGen);
        this.trackCon = trackCon;
        trackIDs = trackCon.getTrackIds();
        id1 = trackIDs.get(0);
        id2 = trackIDs.size() ==2?trackIDs.get(1):-1;
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

        if (covLoaded || colorChanges) {
            if (!twoTracks) {

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
        this.createLines(scaleLineStep, g);

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
        twoTracks = cov.isTwoTracks();

        if (!twoTracks) {

            if (isForwardStrand) {
                if (covType == PersistantCoverage.PERFECT) {
                value = cov.getPerfectFwdMult(absPos);
                } else if (covType == PersistantCoverage.BM) {
                value = cov.getBestMatchFwdMult(absPos);
                } else if (covType == PersistantCoverage.NERROR) {
                value = cov.getCommonFwdMult(absPos);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.PERFECT) {
                value = cov.getPerfectRevMult(absPos);
                } else if (covType == PersistantCoverage.BM) {
                value = cov.getBestMatchRevMult(absPos);
                } else if (covType == PersistantCoverage.NERROR) {
                value = cov.getCommonRevMult(absPos);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            }
            if (hasNormalizationFactor) {
                value = getNormValue(id1, value);
            }

        } else {
            if (isForwardStrand) {
                if (covType == PersistantCoverage.DIFF) {
                value = cov.getCommonFwdMult(absPos);
                    if (hasNormalizationFactor) {
                        int value2 = cov.getCommonFwdMultTrack1(absPos);
                        int value1 = cov.getCommonFwdMultTrack2(absPos);
                        value = getNormValue(id2, value2) - getNormValue(id1, value1);
                        value = value < 0 ? value * -1 : value;
                    }
                } else if (covType == PersistantCoverage.TRACK2) {
                value = cov.getCommonFwdMultTrack2(absPos);
                    value = getNormValue(id2, value);
                } else if (covType == PersistantCoverage.TRACK1) {
                value = cov.getCommonFwdMultTrack1(absPos);
                    value = getNormValue(id1, value);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                }
            } else {
                if (covType == PersistantCoverage.DIFF) {
                value = cov.getCommonRevMult(absPos);
                    if (hasNormalizationFactor) {
                        int value2 = cov.getCommonRevMultTrack2(absPos);
                        int value1 = cov.getCommonRevMultTrack1(absPos);
                        value = getNormValue(id2, value2) - getNormValue(id1, value1);
                        value = value < 0 ? value * -1 : value;
                    }
                    } else if (covType == PersistantCoverage.TRACK2) {
                value = cov.getCommonRevMultTrack2(absPos);
                            value = getNormValue(id2, value);
                        
                    } else if (covType == PersistantCoverage.TRACK1) {
                value = cov.getCommonRevMultTrack1(absPos);
                            value = getNormValue(id1, value);                       
                    } else {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown coverage type!");
                    }
               
            }
        }
            return value;
        
    }
    
    private double getNormValue(int trackID,double value){
        if(normSetting!=null){
        if(normSetting.getHasNormFac(trackID)){
            return  normSetting.getIsLogNorm(trackID)?log2(value):value * normSetting.getFactors(trackID);
        }else{
        return value;
                }}else{
            return value;
        }
    }

    /**
     * Create a GeneralPath that represents the coverage
     * @param orientation if -1, coverage is drawn from bottom to top, if 1 otherwise
     * @param values the values for the currently displayed range
     * @return GeneralPath representing the coverage
     */
    private GeneralPath getCoveragePath(boolean isForwardStrand, int covType) {
        GeneralPath p = new GeneralPath();
        int orientation = (isForwardStrand ? -1 : 1);

        PaintingAreaInfo info = getPaintingAreaInfo();
        int low = (orientation < 0 ? info.getForwardLow() : info.getReverseLow());
        // paint every physical position
        p.moveTo(info.getPhyLeft(), low);
        for (int d = info.getPhyLeft(); d < info.getPhyRight(); d++) {

            int left = transformToLogicalCoord(d);
            int right = transformToLogicalCoord(d + 1) - 1;

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

            value = getCoverageYValue(value);
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
     * Load coverage information for the current bounds
     */
    private void requestCoverage() {
        covLoaded = false;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        trackCon.addCoverageRequest(new GenomeRequest(getBoundsInfo().getLogLeft(), 
                getBoundsInfo().getLogRight(), this, Properties.COMPLETE_COVERAGE));
    }

    @Override
    public synchronized void receiveData(Object coverageData){
        if (coverageData instanceof PersistantCoverage) {
            PersistantCoverage coverage = (PersistantCoverage) coverageData;
            this.cov = coverage;
            trackInfo.setCoverage(cov);
            if (cov.isTwoTracks()) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            covLoaded = true;
            this.repaint();
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        
    }

    @Override
    public void boundsChangedHook() {
        if (cov == null) {
            requestCoverage();
        } else if (!cov.coversBounds(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight())) {
            requestCoverage();
        } else {
            // coverage already loaded
            trackInfo.setCoverage(cov);
            if (cov.isTwoTracks()) {
                this.createCoveragePathsDiffOfTwoTracks();
            } else {
                this.createCoveragePaths();
            }
            covLoaded = true;
        }

        computeScaleStep();

        if (this.hasLegend()) {
            LegendLabel label = this.getLegendLabel();
            this.add(label);

            JPanel legend = this.getLegendPanel();
            this.add(legend);
        }
    }

    private void createCoveragePaths() {
        if (!this.getExcludedFeatureTypes().contains(FeatureType.BEST_MATCH_COVERAGE)) {
            bmFw = getCoveragePath(true, PersistantCoverage.BM);
            bmRv = getCoveragePath(false, PersistantCoverage.BM);

        } else {
            bmFw.reset();
            bmRv.reset();
        }
        if (!this.getExcludedFeatureTypes().contains(FeatureType.PERFECT_COVERAGE)) {
            zFw = getCoveragePath(true, PersistantCoverage.PERFECT);
            zRv = getCoveragePath(false, PersistantCoverage.PERFECT);
        } else {
            zFw.reset();
            zRv.reset();
        }
        if (!this.getExcludedFeatureTypes().contains(FeatureType.COMPLETE_COV)) {
            nFw = getCoveragePath(true, PersistantCoverage.NERROR);
            nRv = getCoveragePath(false, PersistantCoverage.NERROR);
        } else {
            nFw.reset();
            nRv.reset();
        }
    }

    private void createCoveragePathsDiffOfTwoTracks() {
        nFw = getCoveragePath(true, PersistantCoverage.TRACK1);
        nRv = getCoveragePath(false, PersistantCoverage.TRACK1);
        bmFw = getCoveragePath(true, PersistantCoverage.TRACK2);
        bmRv = getCoveragePath(false, PersistantCoverage.TRACK2);
        zFw = getCoveragePath(true, PersistantCoverage.DIFF);
        zRv = getCoveragePath(false, PersistantCoverage.DIFF);
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void changeToolTipText(int logPos) {
        if (covLoaded) {
            twoTracks = cov.isTwoTracks();
        }
        if (covLoaded && twoTracks && !hasNormalizationFactor) {

            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdMultTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevMultTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdMultTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevMultTrack2(logPos);

            Double [] data = new Double[7];
            data[0] = (double)logPos;
            data[1]=(double) nFwVal;
            data[2] = (double)nRvVal;
            data[3] = (double)nFwValTrack1;
            data[4]= (double)nRvValTrack1;
            data[5]=  (double)nFwValTrack2;
            data[6]=  (double)nRvValTrack2;


            this.setToolTipText(toolTipDouble(data, hasNormalizationFactor));

        } else if (covLoaded && !twoTracks && !hasNormalizationFactor) {

            int zFwVal = cov.getPerfectFwdMult(logPos);
            int zRvVal = cov.getPerfectRevMult(logPos);
            int bFw = cov.getBestMatchFwdMult(logPos);
            int bRv = cov.getBestMatchRevMult(logPos);
            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);

           Double [] data = new Double[7];
            data[0] = (double)logPos;
            data[1]=(double) zFwVal;
            data[2] = (double)zRvVal;
            data[3] = (double)bFw;
            data[4]= (double)bRv;
            data[5]=  (double)nFwVal;
            data[6]=  (double)nRvVal;
            
            this.setToolTipText(toolTipSingle(data, hasNormalizationFactor));
        } else if (covLoaded && !twoTracks && hasNormalizationFactor) {

            int zFwVal = cov.getPerfectFwdMult(logPos);
            int zRvVal = cov.getPerfectRevMult(logPos);
            int bFw = cov.getBestMatchFwdMult(logPos);
            int bRv = cov.getBestMatchRevMult(logPos);
            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
    
            double zFwValScale = threeDecAfter(getNormValue(id1,zFwVal));
            double zRvValScale = threeDecAfter(getNormValue(id1,zRvVal));
            double bFwScale = threeDecAfter(getNormValue(id1,bFw));
            double bRvScale = threeDecAfter(getNormValue(id1,bRv));
            double nFwValScale = threeDecAfter(getNormValue(id1,nFwVal));
            double nRvValScale = threeDecAfter(getNormValue(id1,nRvVal));

           Double [] data = new Double[13];
            data[0] = (double)logPos;
            data[1]=(double) zFwVal;
            data[2] = zFwValScale;
            data[3] = (double)zRvVal;
            data[4] = zRvValScale;
            data[5] = (double)bFw;
            data[6] =bFwScale; 
            data[7]= (double)bRv;
            data[8]= bRvScale;
            data[9]=  (double)nFwVal;
            data[10]=nFwValScale;
            data[11]=  (double)nRvVal;
            data[12]=  nRvValScale;

            this.setToolTipText(toolTipSingle(data,hasNormalizationFactor));
        } else if (covLoaded && twoTracks && hasNormalizationFactor) {

            int nFwVal = cov.getCommonFwdMult(logPos);
            int nRvVal = cov.getCommonRevMult(logPos);
            //track 1 info
            int nFwValTrack1 = cov.getCommonFwdMultTrack1(logPos);
            int nRvValTrack1 = cov.getCommonRevMultTrack1(logPos);
            //track 2 info
            int nFwValTrack2 = cov.getCommonFwdMultTrack2(logPos);
            int nRvValTrack2 = cov.getCommonRevMultTrack2(logPos);


            double nFwScaleTrack1 = threeDecAfter(getNormValue(id1,nFwValTrack1));
            double nRvScaleTrack1 = threeDecAfter(getNormValue(id1,nRvValTrack1));
            double nFwValScaleTrack2 = threeDecAfter(getNormValue(id2,nFwValTrack2));
            double nRvValScaleTrack2 = threeDecAfter(getNormValue(id2,nRvValTrack2));
            
            double diffFw = (nFwValScaleTrack2-nFwScaleTrack1);
             double diffRv = (nRvValScaleTrack2-nRvScaleTrack1);
            double nFwValScale = threeDecAfter(diffFw<0?diffFw*-1:diffFw);
            double nRvValScale = threeDecAfter(diffRv<0?diffRv*-1:diffRv);
            nFwValScale=nFwValScale<0?nFwValScale*-1:nFwValScale;
            nRvValScale = nRvValScale<0?nRvValScale*-1:nRvValScale;
             
            Double [] data = new Double[13];
            data[0] = (double)logPos;
            data[1]=(double) nFwVal;
            data[2]= nFwValScale;
            data[3] = (double)nRvVal;
            data[4]= nRvValScale;
            data[5] = (double)nFwValTrack1;
            data[6]= nFwScaleTrack1;
            data[7]= (double)nRvValTrack1;
            data[8] = nRvScaleTrack1;
            data[9]=  (double)nFwValTrack2;
            data[10]=nFwValScaleTrack2;
            data[11]=  (double)nRvValTrack2;
            data[12]=nRvValScaleTrack2;
            
            

            this.setToolTipText(toolTipDouble(data,hasNormalizationFactor));
        } else {
            this.setToolTipText(null);
        }
    }
    
    private String toolTipDouble(Double [] data,boolean hasNormFac){
                    StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<b>Position</b>: ").append(data[0]);
            sb.append("<br>");
            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>Difference(Blue):</b></td></tr>");
            sb.append(hasNormFac?createTableRow("Forward cov.", data[1],data[2]):createTableRow("Forward cov",data[1]));
            sb.append(hasNormFac?createTableRow("Reverse cov.",data[3],data[4]):createTableRow("Reverse cov.",data[2]));
            sb.append("</table>");

            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>Track 1(Orange):</b></td></tr>");

            sb.append(hasNormFac?createTableRow("Forward cov.", data[5],data[6]):createTableRow("Forward cov.", data[3]));
            sb.append(hasNormFac?createTableRow("Reverse cov.", data[7],data[8]):createTableRow("Reverse cov.", data[4]));
            sb.append("</table>");

            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>Track 2(Cyan):</b></td></tr>");

            sb.append(hasNormFac?createTableRow("Forward cov.", data[9],data[10]):createTableRow("Forward cov.", data[5]));
            sb.append(hasNormFac?createTableRow("Reverse cov.", data[11],data[12]):createTableRow("Reverse cov.", data[6]));
            sb.append("</table>");
            sb.append("</html>");
            return sb.toString();
    }
    
        private String toolTipSingle(Double [] data,boolean hasNormFac){
                StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<b>Position</b>: ").append(data[0]);
            sb.append("<br>");
            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>Forward strand</b></td></tr>");
            sb.append(hasNormFac?createTableRow("Perfect match cov.", data[1],data[2]):createTableRow("Perfect match cov.", data[1]));
            sb.append(hasNormFac?createTableRow("Best match cov.", data[5],data[6]):createTableRow("Best match cov.", data[3]));
            sb.append(hasNormFac?createTableRow("Complete cov.", data[9],data[10] ):createTableRow("Complete cov.", data[5]));
            sb.append("</table>");

            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>Reverse strand</b></td></tr>");
            sb.append(hasNormFac?createTableRow("Perfect match cov.",data[3],data[4] ):createTableRow("Perfect match cov.", data[2]));
            sb.append(hasNormFac?createTableRow("Best match cov.",data[7],data[8]):createTableRow("Best match cov.", data[4]));
            sb.append(hasNormFac?createTableRow("Complete cov.", data[11],data[12]):createTableRow("Complete cov.", data[6]));
            sb.append("</table>");
            sb.append("</html>");
            
            return sb.toString();
        }

    private String createTableRow(String label, double value) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf((int)value) + "</td></tr>";
    }

    private String createTableRow(String label, double value, double scaleFacVal) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf(scaleFacVal) + " (" + String.valueOf((int)value) + ")" + "</td></tr>";
    }

    public void setTrackInfoPanel(CoverageInfoI info) {
        this.trackInfo = info;
    }

    private int getCoverageYValue(double coverage) {
        int value = (int) Math.round(coverage / scaleFactor);
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

    public void verticalZoomLevelUpdated(int value) {
        scaleFactor = Math.round(Math.pow(value, 2) / 10);
        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor);

        this.computeScaleStep();
        createCoveragePaths();
        this.repaint();
    }

    private static double threeDecAfter(double val) {
        int tmp = (int) (val * 1000);
        return tmp / 1000.0;
    }

    private static double log2(double num) {
        num = num==0?1:num;
        return (Math.log(num) / Math.log(2));
    }

    private void computeScaleStep() {
        int visibleCoverage = (int) (this.getPaintingAreaInfo().getAvailableForwardHeight() * scaleFactor);

        if (visibleCoverage <= 10) {
            scaleLineStep = 1;
        } else if (visibleCoverage <= 100) {
            scaleLineStep = 20;
        } else if (visibleCoverage <= 200) {
            scaleLineStep = 50;
        } else if (visibleCoverage <= 500) {
            scaleLineStep = 100;
        } else if (visibleCoverage <= 1000) {
            scaleLineStep = 250;
        } else if (visibleCoverage <= 3000) {
            scaleLineStep = 500;
        } else if (visibleCoverage <= 4000) {
            scaleLineStep = 750;
        } else if (visibleCoverage <= 7500) {
            scaleLineStep = 1000;
        } else if (visibleCoverage <= 15000) {
            scaleLineStep = 2500;
        } else if (visibleCoverage <= 25000) {
            scaleLineStep = 5000;
        } else if (visibleCoverage <= 45000) {
            scaleLineStep = 7500;
        } else if (visibleCoverage <= 65000) {
            scaleLineStep = 10000;
        } else {
            scaleLineStep = 20000;
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

    public void scaleValueChanged() {
    hasNormalizationFactor =normSetting.getIdToValue().keySet().size() ==2 ?(normSetting.getHasNormFac(id1)| normSetting.getHasNormFac(id2)):normSetting.getHasNormFac(id1);
        boundsChangedHook();
   //     Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "scaleValueChanged " + isSelected + " " + factor.size());
        repaint();
    }

    public void colorChanges() {
        colorChanges = true;
        repaint();
    }

    public TrackConnector getTrackCon() {
        return trackCon;
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

    public NormalizationSettings getNormSetting() {
        return normSetting;
    }

    public void setNormSetting(NormalizationSettings normSetting) {
        this.normSetting = normSetting;
    }
    
    
}
