package de.cebitec.vamp.view.dataVisualisation.histogramViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.CoverageRequest;
import de.cebitec.vamp.databackend.CoverageThreadListener;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantCoverage;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.ZoomLevelExcusePanel;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.SequenceBar;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author ddoppmeier
 */
public class HistogramViewer extends AbstractViewer implements CoverageThreadListener {

    private static final long serialVersionUID = 234765253;
    private List<String> bases;
    private static int height = 500;
    private TrackConnector trackConnector;
    private PersistantReference refGen;
    private GenomeGapManager gapManager;
    private int lowerBound;
    private int upperBound;
    private int width;
    private Collection<PersistantReferenceGap> gaps;
    private Collection<PersistantDiff> diffs;
    private LogoDataManager logoData;
    private PersistantCoverage cov;
    private boolean dataLoaded;
    private boolean isColored = false;
    private ZoomLevelExcusePanel zoomExcuse;
    // maximum coverage found in interval, regarding both strands
    private int maxCoverage;
    private List<Integer> scaleValues;
    private double pxPerCoverageUnit;

    public HistogramViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen, TrackConnector trackConnector) {
        super(boundsInfoManager, basePanel, refGen);
        this.refGen = refGen;
        this.trackConnector = trackConnector;
        this.lowerBound = super.getBoundsInfo().getLogLeft();
        this.upperBound = super.getBoundsInfo().getLogRight();

        scaleValues = new ArrayList<Integer>();
        zoomExcuse = new ZoomLevelExcusePanel();

        logoData = new LogoDataManager(lowerBound, upperBound);
        gapManager = new GenomeGapManager(lowerBound, upperBound);
        gaps = new ArrayList<PersistantReferenceGap>();
        cov = new PersistantCoverage(lowerBound, lowerBound);

        bases = new ArrayList<String>();
        bases.add("match");
        bases.add("a");
        bases.add("c");
        bases.add("g");
        bases.add("t");
        bases.add("n");
        bases.add("readgap");

        this.showSequenceBar(true);
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void changeToolTipText(int logPos) {
        // do not update if this windows is inactive
        if (this.isActive() && dataLoaded) {
            int relPos = logPos;
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<b>Position</b>: ").append(logPos);

            // logo data manager has no information about gaps, so we have to shift positions right here
            if (gapManager != null) {
                relPos += gapManager.getNumOfGapsSmaller(logPos);
                // if there is a gap at logPos, logo data manager would provide us with gap information, which we do not wand
                if (gapManager.hasGapAt(logPos)) {
                    relPos++;
                }
            }

            int complete = cov.getnFwMult(logPos);
            if (complete != 0) {
                appendStatsTable(sb, complete, relPos, true, "Forward strand", false);
            }

            complete = cov.getnRvMult(logPos);
            if (complete != 0) {
                appendStatsTable(sb, complete, relPos, false, "Reverse strand", false);
            }

            if (gapManager != null && gapManager.hasGapAt(logPos)) {
                int tmp = logPos + gapManager.getNumOfGapsSmaller(logPos);
                complete = cov.getnFwMult(logPos);
                appendStatsTable(sb, complete, tmp, true, "Genome gaps forward", true);

                complete = cov.getnFwMult(logPos);
                appendStatsTable(sb, complete, tmp, false, "Genome gaps reverse", true);
            }

            sb.append("</html>");
            this.setToolTipText(sb.toString());
        } else {
            setToolTipText(null);
        }
    }

    private int getPercentage(int all, int value) {
        int percent = (int) (((double) value / all) * 100);

        return percent;
    }

    private void appendStatsTable(StringBuilder sb, int complete, int relPos, boolean isForwardStrand, String title, boolean isGapStats) {
        int matches = logoData.getNumOfMatchesAt(relPos, isForwardStrand);
        int as = logoData.getNumOfAAt(relPos, isForwardStrand);
        int cs = logoData.getNumOfCAt(relPos, isForwardStrand);
        int gs = logoData.getNumOfGAt(relPos, isForwardStrand);
        int ts = logoData.getNumOfTAt(relPos, isForwardStrand);
        int ns = logoData.getNumOfNAt(relPos, isForwardStrand);
        int readgaps = logoData.getNumOfReadGapsAt(relPos, isForwardStrand);

        if (matches != 0 || as != 0 || cs != 0 || gs != 0 || ts != 0 || ns != 0 || readgaps != 0) {
            sb.append("<table>");
            sb.append("<tr><td align=\"left\"><b>").append(title).append("</b></td></tr>");

            // if this is used to show stats about genome gaps, do not print complete coverage again
            if (!isGapStats) {
                sb.append(createTableRow("Compl. cov.", complete));
            }

            if (matches != 0) {
                sb.append(createTableRow("Match cov.", matches, getPercentage(complete, matches)));
            }
            if (as != 0) {
                sb.append(createTableRow("A", as, getPercentage(complete, as)));
            }
            if (cs != 0) {
                sb.append(createTableRow("C", cs, getPercentage(complete, cs)));
            }
            if (gs != 0) {
                sb.append(createTableRow("G", gs, getPercentage(complete, gs)));
            }
            if (ts != 0) {
                sb.append(createTableRow("T", ts, getPercentage(complete, ts)));
            }
            if (ns != 0) {
                sb.append(createTableRow("N", ns, getPercentage(complete, ns)));
            }
            if (readgaps != 0) {
                sb.append(createTableRow("Read gap", readgaps, getPercentage(complete, readgaps)));
            }

            sb.append("</table>");
        }
    }

    private String createTableRow(String label, int value, int percent) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"right\">" + String.valueOf(value) + "</td><td align=\"right\">~" + percent + "%</td></tr>";
    }

    private String createTableRow(String label, int value) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"right\">" + String.valueOf(value) + "</td><td align=\"right\"></td></tr>";
    }

    @Override
    public synchronized void receiveCoverage(final PersistantCoverage coverage) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                cov = coverage;
                setupData();
                repaint();
            }
        });
    }

    /**
     * Sets up whole data of this class.
     */
    private synchronized void setupData() {
        gapManager = new GenomeGapManager(lowerBound, upperBound);

        try {
            gaps = trackConnector.getExtendedReferenceGapsForIntervalOrderedByMappingID(lowerBound, upperBound);
        } catch (Exception ex) {
            System.err.print("trackConnector couldn't initialize gaps" + ex);
            //TOTO: error an nutzer geben
        }
        this.fillGapManager();
        this.getSequenceBar().setGenomeGapManager(gapManager);
        this.adjustAbsStop();

        this.diffs = trackConnector.getDiffsForInterval(lowerBound, upperBound);
        this.setUpLogoData();
        if (logoData.getMaxFoundCoverage() != 0) {
            this.createLogoBlocks();
            this.scaleValues = getCoverageScaleLineValues();
        }
        this.dataLoaded = true;
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void requestData() {
        if (cov != null && cov.coversBounds(lowerBound, upperBound)) {
            this.setupData();
        } else {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            trackConnector.addCoverageRequest(new CoverageRequest(lowerBound, upperBound, this));
        }
    }

    @Override
    public void boundsChangedHook() {
        this.lowerBound = super.getBoundsInfo().getLogLeft();
        this.upperBound = super.getBoundsInfo().getLogRight();
        this.width = upperBound - lowerBound + 1;
        this.dataLoaded = false;
        this.removeAll();

        if (isInMaxZoomLevel()) {
            this.setInDrawingMode(true);

            if (this.hasLegend()) {
                this.add(this.getLegendLabel());
                this.add(this.getLegendPanel());
            }
            if (this.hasSequenceBar()) {
                this.add(this.getSequenceBar());
            }

            this.requestData();

        } else {
            this.setInDrawingMode(false);
            gapManager = null;

            this.placeExcusePanel(zoomExcuse);
        }
    }

    private List<Integer> getCoverageScaleLineValues() {
        int minMargin = 20;
        int step;

        ArrayList<Integer> test = new ArrayList<Integer>();
        test.add(50000);
        test.add(20000);
        test.add(10000);
        test.add(5000);
        test.add(2000);
        test.add(1000);
        test.add(500);
        test.add(200);
        test.add(100);
        test.add(50);
        test.add(20);
        test.add(15);
        test.add(10);
        test.add(5);
        test.add(2);
        test.add(1);

        step = 1;
        for (Integer i : test) {
            if (pxPerCoverageUnit * i > minMargin) {
                step = i;
            }
        }

        return getValues(step);
    }

    private ArrayList<Integer> getValues(int stepsize) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        int tmp = stepsize;
        while (tmp <= maxCoverage) {
            list.add(tmp);
            tmp += stepsize;
        }

        return list;
    }

    private void placeExcusePanel(JPanel p) {
        // has to be checked for null because, this method may be called upon
        // initialization of this object (depending on behaviour of AbstractViewer)
        // BEFORE the panels are initialized!
        if (p != null) {
            int tmpWidth = p.getPreferredSize().width;
            int x = this.getSize().width / 2 - tmpWidth / 2;
            if (x < 0) {
                x = 0;
            }

            int tmpHeight = p.getPreferredSize().height;
            int y = this.getSize().height / 2 - tmpHeight / 2;
            if (y < 0) {
                y = 0;
            }
            p.setBounds(x, y, tmpWidth, tmpHeight);
            this.add(p);
            this.updateUI();
        }
    }

    /**
     * Creates the whole color model of the alignment view and the coloring of
     * the bases in their background.
     */
    private void createLogoBlocks() {
        maxCoverage = logoData.getMaxFoundCoverage();
        PaintingAreaInfo info = this.getPaintingAreaInfo();
        // asuming forward and reverse height are equal
        int availableHeight = info.getAvailableForwardHeight();

        pxPerCoverageUnit = (double) availableHeight / maxCoverage;

        SequenceBar seqBar = this.getSequenceBar();
        if (seqBar != null) {
            seqBar.removeAll();
        }
        for (int i = lowerBound; i <= upperBound; i++) {
            // compute relative position in layout
            int relPos = i + gapManager.getNumOfGapsSmaller(i);
            relPos += gapManager.getNumOfGapsAt(i);

            // get physical x coordinate
            int x = (int) getPhysBoundariesForLogPos(i).getLeftPhysBound();
            x += getPhysBoundariesForLogPos(i).getPhysWidth() * gapManager.getNumOfGapsAt(i);

            this.cycleBases(i, relPos, x, pxPerCoverageUnit, true, isColored);
            this.cycleBases(i, relPos, x, pxPerCoverageUnit, false, isColored);

            if (seqBar != null) {
                seqBar.paintBaseBackgroundColor(i);
            }
            if (gapManager.hasGapAt(i)) {
                for (int j = 0; j < gapManager.getNumOfGapsAt(i); j++) {
                    relPos = i + gapManager.getNumOfGapsSmaller(i);
                    relPos += j;

                    x = (int) getPhysBoundariesForLogPos(i).getLeftPhysBound();
                    x += getPhysBoundariesForLogPos(i).getPhysWidth() * j;

                    this.cycleBases(i, relPos, x, pxPerCoverageUnit, true, isColored);
                    this.cycleBases(i, relPos, x, pxPerCoverageUnit, false, isColored);
                }
            }
        }
    }

    /**
     * Creates the colored histogram bars.
     * @param absPos
     * @param relPos
     * @param x
     * @param heightPerCoverageUnit
     * @param isForwardStrand true, if bars for fwd strand should be painted
     * @param isColored true, if the histogram should be colored
     */
    private void cycleBases(int absPos, int relPos, int x, double heightPerCoverageUnit, boolean isForwardStrand, boolean isColored) {
        double value;
        int featureHeight;
        Color c = null;
        int y = (isForwardStrand ? getPaintingAreaInfo().getForwardLow() : getPaintingAreaInfo().getReverseLow());
        String base = refGen.getSequence().substring(absPos - 1, absPos);
        if (absPos != relPos) {
        }

        for (String type : bases) {
            if (type.equals("match") && isColored) {
                value = logoData.getNumOfMatchesAt(relPos, isForwardStrand);
                if (isForwardStrand) {
                    if (base.equals("a")) {
                        c = ColorProperties.LOGO_A;
                    } else if (base.equals("t")) {
                        c = ColorProperties.LOGO_T;
                    } else if (base.equals("c")) {
                        c = ColorProperties.LOGO_C;
                    } else if (base.equals("g")) {
                        c = ColorProperties.LOGO_G;
                    } else if (base.equals("n")) {
                        c = ColorProperties.LOGO_N;
                    } else if (base.equals("readgap")) {
                        c = ColorProperties.LOGO_READGAP;
                    }
                } else {
                    if (base.equals("t")) {
                        c = ColorProperties.LOGO_A;
                    } else if (base.equals("a")) {
                        c = ColorProperties.LOGO_T;
                    } else if (base.equals("g")) {
                        c = ColorProperties.LOGO_C;
                    } else if (base.equals("c")) {
                        c = ColorProperties.LOGO_G;
                    } else if (base.equals("n")) {
                        c = ColorProperties.LOGO_N;
                    } else if (base.equals("readgap")) {
                        c = ColorProperties.LOGO_READGAP;
                    }
                }
                
             }else if (type.equals("match")&& !isColored){
                c = ColorProperties.LOGO_MATCH;
                 value = logoData.getNumOfMatchesAt(relPos, isForwardStrand);
            } else if (type.equals("a")) {
                value = logoData.getNumOfAAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_A;
            } else if (type.equals("c")) {
                value = logoData.getNumOfCAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_C;
            } else if (type.equals("g")) {
                value = logoData.getNumOfGAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_G;
            } else if (type.equals("t")) {
                value = logoData.getNumOfTAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_T;
            } else if (type.equals("readgap")) {
                value = logoData.getNumOfReadGapsAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_READGAP;
            } else if (type.equals("n")) {
                value = logoData.getNumOfNAt(relPos, isForwardStrand);
                c = ColorProperties.LOGO_N;
            } else {
                value = logoData.getNumOfNAt(relPos, isForwardStrand);
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown base {0}!", type);
                c = ColorProperties.LOGO_BASE_UNDEF;
            }
            featureHeight = (int) (value * heightPerCoverageUnit);

            if (isForwardStrand) {
                y -= featureHeight;
            }

            PhysicalBaseBounds bounds = getPhysBoundariesForLogPos(absPos);
            BarComponent block = new BarComponent(featureHeight, (int) bounds.getPhysWidth(), c);
            if (isForwardStrand) {
                block.setBounds(x, y, (int) bounds.getPhysWidth(), featureHeight);
            } else {
                block.setBounds(x, y + 1, (int) bounds.getPhysWidth(), featureHeight);
            }
            this.add(block);

            if (!isForwardStrand) {
                y += featureHeight;
            }
        }
    }

    private void adjustAbsStop() {
        // count the number of gaps occuring in visible area
        int tmpWidth = upperBound - lowerBound + 1;
        int gapNo = 0; // count the number of gaps
        int widthCount = 0; // count the number of bases
        int i = 0; // count variable till max width
        while (widthCount < tmpWidth) {
            int num = gapManager.getNumOfGapsAt(lowerBound + i); // get the number of gaps at current position
            widthCount++; // current position needs 1 base space in visual alignment
            widthCount += num; // if gaps occured at current position, they need some space, too
            gapNo += num;
            i++;
        }
        upperBound -= gapNo;
        this.getBoundsInfo().correctLogRight(upperBound);
    }

    private void fillGapManager() {
        HashMap<Integer, Integer> positionToNum = new HashMap<Integer, Integer>();
        for (Iterator<PersistantReferenceGap> it = gaps.iterator(); it.hasNext();) {
            PersistantReferenceGap gap = it.next();
            int gapPosition = gap.getPosition();
            int gapOrder = gap.getOrder();
            gapOrder++;

            if (!positionToNum.containsKey(gapPosition)) {
                positionToNum.put(gapPosition, 0);
            }
            int oldValue = positionToNum.get(gapPosition);
            if (gapOrder > oldValue) {
                positionToNum.put(gapPosition, gapOrder);
            }
        }

        for (Iterator<Integer> it = positionToNum.keySet().iterator(); it.hasNext();) {
            int position = it.next();
            int numOfGaps = positionToNum.get(position);
            gapManager.addNumOfGapsAtPosition(position, numOfGaps);
        }
    }

    private void setUpLogoData() {
        logoData = new LogoDataManager(lowerBound, width);

        // store coverage information in logo data
        for (int i = lowerBound; i <= upperBound; i++) {
            int relPos = i + gapManager.getNumOfGapsAt(i);
            relPos += gapManager.getNumOfGapsSmaller(i);
            logoData.setCoverageAt(relPos, cov.getnFwMult(i), true);
            logoData.setCoverageAt(relPos, cov.getnRvMult(i), false);

        }

        // store diff information from the refernce genome in logo data
        for (Iterator<PersistantDiff> it = diffs.iterator(); it.hasNext();) {
            PersistantDiff d = it.next();
            int position = d.getPosition() + gapManager.getNumOfGapsAt(d.getPosition()) + gapManager.getNumOfGapsSmaller(d.getPosition());
            logoData.addExtendedPersistantDiff(d, position);
        }

        // store gap information in logo data
        logoData.addGaps(gaps, gapManager);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        if (isInDrawingMode()) {
            if (!dataLoaded) {
                g.fillRect(0, 0, this.getSize().width - 1, this.getSize().height - 1);
            }
            g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
            drawBaseLines(g);

            PaintingAreaInfo info = getPaintingAreaInfo();
            for (Integer i : scaleValues) {
                String label = String.valueOf(i);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                int fontHeight = g.getFontMetrics().getAscent();

                int y = info.getForwardLow() - (int) (i * pxPerCoverageUnit);
                graphics.drawLine(info.getPhyLeft(), y, info.getPhyRight(), y);
                graphics.drawString(label, info.getPhyLeft() - labelWidth - 4, y + fontHeight / 2);
                graphics.drawString(label, info.getPhyRight() + 4, y + fontHeight / 2);

                y = (int) (i * pxPerCoverageUnit) + info.getReverseLow();
                graphics.drawLine(info.getPhyLeft(), y, info.getPhyRight(), y);
                graphics.drawString(label, info.getPhyLeft() - labelWidth - 4, y + fontHeight / 2);
                graphics.drawString(label, info.getPhyRight() + 4, y + fontHeight / 2);
            }
        }
    }

    private void drawBaseLines(Graphics2D graphics) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine(info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow());
        graphics.drawLine(info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow());
    }

    @Override
    public int transformToLogicalCoord(int physPos) {
        int logPos = super.transformToLogicalCoord(physPos);
        if (isInDrawingMode()) {
            int gapsSmaller = gapManager.getAccumulatedGapsSmallerThan(logPos);
            logPos -= gapsSmaller;
        }
        return logPos;
    }

    @Override
    public double transformToPhysicalCoord(int logPos) {

        // if this viewer is operating in detail view mode, adjust logPos
        if (gapManager != null && isInDrawingMode()) {
            int gapsSmaller = gapManager.getNumOfGapsSmaller(logPos);
            logPos += gapsSmaller;
        }
        return super.transformToPhysicalCoord(logPos);
    }

    @Override
    public int getWidthOfMouseOverlay(int position) {
        PhysicalBaseBounds mouseAreaLeft = getPhysBoundariesForLogPos(position);

        int tmp = (int) mouseAreaLeft.getPhysWidth();
        // if currentPosition is a gap, the following bases to the right marks the same position!
        if (isInDrawingMode() && gapManager.hasGapAt(position)) {
            tmp *= (gapManager.getNumOfGapsAt(position) + 1);
        }
        return tmp;
    }

    public boolean isColored(boolean setColored){
    isColored = setColored;
    return isColored;
    }

}
