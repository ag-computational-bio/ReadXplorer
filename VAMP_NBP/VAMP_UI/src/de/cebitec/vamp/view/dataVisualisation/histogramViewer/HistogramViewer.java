package de.cebitec.vamp.view.dataVisualisation.histogramViewer;

import de.cebitec.vamp.databackend.CoverageAndDiffRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.*;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.SequenceBar;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * The histogram viewer. Showing the match an deviating coverage for each position
 * in a reference genome as a histogram.
 *
 * @author ddoppmeier
 */
public class HistogramViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 234765253;
    private InputOutput io;
    private static int height = 500;
    private TrackConnector trackConnector;
    private PersistantReference refGen;
    private GenomeGapManager gapManager;
    private int lowerBound;
    private int upperBound;
    private int width;
    private List<PersistantReferenceGap> gaps;
    private List<PersistantDiff> diffs;
    private LogoDataManager logoData;
    private PersistantCoverage cov;
    private boolean dataLoaded;
    private boolean isColored = false;
    private boolean diffsLoaded;
    private boolean coverageLoaded;
//    private boolean isUpperBoundCorrected;
//    private ZoomLevelExcusePanel zoomExcuse;
    // maximum coverage found in interval, regarding both strands
    private int maxCoverage;
    private List<Integer> scaleValues;
    private double pxPerCoverageUnit;

    @Override
    public void notifySkipped() {
        //do nothing
    }

    private enum Bases {
        m, a, c, t, g, n, _,
    }
    public HistogramViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen, TrackConnector trackConnector) {
        super(boundsInfoManager, basePanel, refGen);
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(HistogramViewer.class, "HistogramViewer.output.name"), false);
        this.refGen = refGen;
        this.trackConnector = trackConnector;
        this.setInDrawingMode(false);
        this.lowerBound = super.getBoundsInfo().getLogLeft();
        this.upperBound = super.getBoundsInfo().getLogRight();
        this.scaleValues = new ArrayList<>();
//        zoomExcuse = new ZoomLevelExcusePanel();

        logoData = new LogoDataManager(lowerBound, upperBound);
        gapManager = new GenomeGapManager(lowerBound, upperBound);
        gaps = new ArrayList<>();
        diffs = new ArrayList<>();
        cov = new PersistantCoverage(lowerBound, lowerBound);
        this.showSequenceBar(true, true);
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

            int complete = cov.getCommonFwdMult(logPos);
            if (complete != 0) {
                appendStatsTable(sb, complete, relPos, true, "Forward strand", false);
            }

            complete = cov.getCommonRevMult(logPos);
            if (complete != 0) {
                appendStatsTable(sb, complete, relPos, false, "Reverse strand", false);
            }

            if (gapManager != null && gapManager.hasGapAt(logPos)) {
                int tmp = logPos + gapManager.getNumOfGapsSmaller(logPos);
                complete = cov.getCommonFwdMult(logPos);
                appendStatsTable(sb, complete, tmp, true, "Genome gaps forward", true);

                complete = cov.getCommonFwdMult(logPos);
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

    /**
     * Requests the data to show from the DB.
     */
    private void requestData() {
        if (cov != null && cov.coversBounds(lowerBound, upperBound)) {
            this.coverageLoaded = true;
            this.diffsLoaded = trackConnector.addDiffRequest(new CoverageAndDiffRequest(lowerBound, upperBound, this));
            if (this.diffsLoaded) {
                this.setupData();
            }
        } else {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            this.coverageLoaded = false;
            this.diffsLoaded = false;
            trackConnector.addCoverageRequest(new CoverageAndDiffRequest(lowerBound, upperBound, this));
            trackConnector.addDiffRequest(new CoverageAndDiffRequest(lowerBound, upperBound, this));
        }
    }

    @Override
    public synchronized void receiveData(Object data) {
        if (data instanceof CoverageAndDiffResultPersistant) {
            final CoverageAndDiffResultPersistant result = (CoverageAndDiffResultPersistant) data;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (!coverageLoaded && result.getCoverage().getRightBound() != 0 && result.getLowerBound() <= lowerBound && result.getUpperBound() >= upperBound) {
                        cov = result.getCoverage();
                        coverageLoaded = true;
                    }
                    if (result.isDiffsAndGapsUsed() && !diffsLoaded && result.getLowerBound() <= lowerBound && result.getUpperBound() >= upperBound) {
                        diffs = result.getDiffs();
                        gaps = result.getGaps();
                        Collections.sort(diffs);
                        Collections.sort(gaps);
                        diffsLoaded = true;
                    }
                    if (coverageLoaded && diffsLoaded) {
                        setupData();
                        repaint();
                    }
                }
            });
        }
    }

    /**
     * Sets up whole data of this class.
     */
    private synchronized void setupData() {
        gapManager = new GenomeGapManager(lowerBound, upperBound);

        this.fillGapManager();
        this.getSequenceBar().setGenomeGapManager(gapManager);
        this.adjustAbsStop();

        this.setUpLogoData();
        if (logoData.getMaxFoundCoverage() != 0) {
            this.createLogoBlocks();
            this.scaleValues = getCoverageScaleLineValues();
        }
        this.dataLoaded = true;
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void boundsChangedHook() {
        if (super.getBoundsInfo().getLogLeft() != lowerBound || super.getBoundsInfo().getLogRight() != upperBound) {
            this.lowerBound = super.getBoundsInfo().getLogLeft();
            this.upperBound = super.getBoundsInfo().getLogRight();
            this.width = upperBound - lowerBound + 1;
            this.dataLoaded = false;
            this.removeAll();

            if (!this.isInMaxZoomLevel()) {
                this.getBoundsInformationManager().zoomLevelUpdated(1);
            }

            this.setInDrawingMode(true);

            if (this.hasLegend()) {
                this.add(this.getLegendLabel());
                this.add(this.getLegendPanel());
            }
            if (this.hasSequenceBar()) {
                this.add(this.getSequenceBar());
            }

            this.requestData();
        }
    }

    private List<Integer> getCoverageScaleLineValues() {
        int minMargin = 20;
        int step;

        ArrayList<Integer> test = new ArrayList<>();
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
        ArrayList<Integer> list = new ArrayList<>();

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
    @SuppressWarnings("fallthrough")
    private void cycleBases(int absPos, int relPos, int x, double heightPerCoverageUnit, boolean isForwardStrand, boolean isColored) {
        double value;
        int featureHeight;
        Color c;
        int y = (isForwardStrand ? getPaintingAreaInfo().getForwardLow() : getPaintingAreaInfo().getReverseLow());
        char base= refGen.getSequence().charAt(absPos-1);

        for (Bases type : Bases.values()) {
                switch (type) {
                    case m:
                        value = logoData.getNumOfMatchesAt(relPos, isForwardStrand);
                        if (!isColored) {
                            c = ColorProperties.LOGO_MATCH;
                        } else {
                            switch (base) {
                                case 'a':
                                    c = isForwardStrand ? ColorProperties.LOGO_A : ColorProperties.LOGO_T;
                                    break;
                                case 't':
                                    c = isForwardStrand ? ColorProperties.LOGO_T : ColorProperties.LOGO_A;
                                    break;
                                case 'c':
                                    c = isForwardStrand ? ColorProperties.LOGO_C : ColorProperties.LOGO_G;
                                    break;
                                case 'g':
                                    c = isForwardStrand ? ColorProperties.LOGO_G : ColorProperties.LOGO_C;
                                    break;
                                case 'n':
                                    c = ColorProperties.LOGO_N;
                                    break;
                                case '-':
                                    c = ColorProperties.LOGO_READGAP;
                                    break;
                                default:
                                    c = ColorProperties.LOGO_BASE_UNDEF;
                                    break;
                            }
                        }
                        break;
                    case a:
                        value = logoData.getNumOfAAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_A;
                        break;
                    case c:
                        value = logoData.getNumOfCAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_C;
                        break;
                    case g:
                        value = logoData.getNumOfGAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_G;
                        break;
                    case t:
                        value = logoData.getNumOfTAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_T;
                        break;
                    case _:
                        value = logoData.getNumOfReadGapsAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_READGAP;
                        break;
                    case n:
                        value = logoData.getNumOfNAt(relPos, isForwardStrand);
                        c = ColorProperties.LOGO_N;
                        break;
                    default:
                        c = ColorProperties.LOGO_BASE_UNDEF;
                        value = logoData.getNumOfNAt(relPos, isForwardStrand);
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown base {0}!", type);
                        break;
                }

            featureHeight = (int) (value * heightPerCoverageUnit);

            PhysicalBaseBounds bounds = getPhysBoundariesForLogPos(absPos);
            BarComponent block = new BarComponent(featureHeight, (int) bounds.getPhysWidth(), c);
            if (isForwardStrand) {
                    y -= featureHeight;
                block.setBounds(x, y, (int) bounds.getPhysWidth(), featureHeight);
            } else {
                block.setBounds(x, y + 1, (int) bounds.getPhysWidth(), featureHeight);
                   y += featureHeight;
            }
            this.add(block);
        }
    }

    /**
     * Adjusts the absolute stop position, if gaps are involved.
     */
    private void adjustAbsStop() {
        // count the number of gaps occuring in visible area
        int tmpWidth = upperBound - lowerBound + 1;
        int gapNo = 0; // count the number of gaps
        int widthCount = 0; // count the number of bases
        int i = 0; // count variable till max width
        int num;
        while (widthCount < tmpWidth) {
            num = gapManager.getNumOfGapsAt(lowerBound + i); // get the number of gaps at current position
            widthCount++; // current position needs 1 base space in visual alignment
            widthCount += num; // if gaps occured at current position, they need some space, too
            gapNo += num;
            i++;
        }
        upperBound -= gapNo;
        this.getBoundsInfo().correctLogRight(upperBound);
    }

    private void fillGapManager() {
        HashMap<Integer, Integer> positionToNum = new HashMap<>();
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
            logoData.setCoverageAt(relPos, cov.getCommonFwdMult(i), true);
            logoData.setCoverageAt(relPos, cov.getCommonRevMult(i), false);

        }

        // store diff information from the reference genome in logo data
        for (Iterator<PersistantDiff> it = diffs.iterator(); it.hasNext();) {
            PersistantDiff d = it.next();
            int position = d.getPosition() + gapManager.getNumOfGapsAt(d.getPosition()) + gapManager.getNumOfGapsSmaller(d.getPosition());
            if (position > lowerBound && position < upperBound) {
                logoData.addExtendedPersistantDiff(d, position);
            } else if (position > upperBound) {
                break; //TODO: store last index to speed up clac
            }
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

            // draw coverage values and lines for the coverage values depending
            // on the maximum coverage in the given interval
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

    /**
     * Draw the base lines for fwd and rev strand in the middle of the viewer.
     * @param graphics 
     */
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
