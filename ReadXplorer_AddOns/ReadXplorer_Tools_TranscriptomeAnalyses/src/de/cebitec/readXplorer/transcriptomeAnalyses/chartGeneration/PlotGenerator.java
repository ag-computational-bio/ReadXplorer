package de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ParameterSetFiveEnrichedAnalyses;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.PiePlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.colors.LinearGradient;
import de.erichseifert.gral.plots.legends.ValueLegend;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.Exceptions;

/**
 * This class contains the logic for plots/charts generation.
 *
 * @author jritter
 */
public class PlotGenerator {

    /**
     * First corporate color used for normal coloring.
     */
    protected static final Color COLOR1 = new Color(55, 170, 200);
    /**
     * Second corporate color used as signal color
     */
    protected static final Color COLOR2 = new Color(200, 80, 75);
    private File absFreq5PrimeUtrsInCsv;
    private final ReferenceViewer referenceViewer;

    /**
     * Constructor for this class.
     *
     * @param referenceViewer ReferenceViewer
     */
    public PlotGenerator(ReferenceViewer referenceViewer) {
        this.referenceViewer = referenceViewer;
    }

    /**
     * Prepare the DataTable for 5'-UTR length distribution plot. For that, all
     * tss with an offset to next feature > 0 and leaderless transcripts with a
     * tss that starts inbetween a feature are taken.
     *
     * @param elements ElementsOfInterest
     * @param tss List of all tss
     * @param params ParameterSetFiveEnrichedAnalyses
     * @param isBining <true> if bining of 5'-UTR length was selected, else
     * <false>
     * @param binsize the bining size of 5'-UTR lengt
     * @return a List of DataTable.
     */
    public List<DataTable> prepareDataForUtrDistr(ElementsOfInterest elements, List<TranscriptionStart> tss, ParameterSetFiveEnrichedAnalyses params, boolean isBining, int binsize) {
        List<DataTable> dataList = new ArrayList<>();
        List<TranscriptionStart> tssForAnalysis = getTssOfInterest(elements, tss);

        // all tss assigned to annotated genome feature
        Map<String, List<TranscriptionStart>> tssToLocus = new TreeMap<>();

        Map<Integer, Integer> notBinedTable = new HashMap<>();
        int leaderlessRange = params.getLeaderlessLimit();
        // 5'-UTR length | Count
        DataTable data = new DataTable(Integer.class, Integer.class);
        String locus;

        if (isBining) {
            int[] collectionArrayDownstream = new int[leaderlessRange + 1];
            int[] collectionArrayUpstream = new int[params.getExclusionOfTSSDistance() + 1];

            // Preparation of DataTable
            for (TranscriptionStart tSS : tssForAnalysis) {
                if (tSS.getAssignedFeature() != null) {
                    int x;
                    if (tSS.getDist2start() > 0 && tSS.getDist2start() <= leaderlessRange) {
                        x = tSS.getDist2start();
                        collectionArrayDownstream[x] = collectionArrayDownstream[x] + 1;
                    } else {
                        x = tSS.getOffset();
                        collectionArrayUpstream[x] = collectionArrayUpstream[x] + 1;
                    }

                    locus = tSS.getAssignedFeature().getLocus();
                    if (tssToLocus.containsKey(locus)) {
                        tssToLocus.get(locus).add(tSS);
                    } else {
                        tssToLocus.put(locus, new ArrayList<TranscriptionStart>());
                        tssToLocus.get(locus).add(tSS);
                    }
                }
            }

            int binValue = 0;
            if (binsize > leaderlessRange) {
                for (int i = 1; i < collectionArrayDownstream.length;) {
                    binValue += collectionArrayDownstream[i];
                }
                if (binValue > 0) {
                    data.add(-1, binValue);
                }
            } else {
                int cnt = -1;
                for (int i = leaderlessRange; i > 0; i--) {
                    binValue = 0;
                    if ((i - binsize + 1) > 0) {
                        for (int j = i; j > i - binsize; j--) {
                            binValue += collectionArrayDownstream[j];
                        }
                        data.add(cnt, binValue);
                        cnt--;
                        i -= binsize - 1;
                    } else {
                        for (int j = i; j > 0; j--) {
                            binValue += collectionArrayDownstream[j];
                        }

                        data.add(cnt, binValue);
                        cnt--;
                    }
                }
            }
            int cnt = 1;
            data.add(0, collectionArrayUpstream[0]);
            for (int i = 1; i < collectionArrayUpstream.length;) {
                binValue = 0;
                if ((i + binsize - 1) < collectionArrayUpstream.length) {
                    for (int j = i; j < i + binsize; j++) {
                        binValue += collectionArrayUpstream[j];
                    }
                    data.add(cnt, binValue);
                    cnt++;
                    i += binsize;
                } else {
                    for (int j = i; j < collectionArrayUpstream.length; j++) {
                        binValue += collectionArrayUpstream[j];
                    }

                    data.add(cnt, binValue);
                    cnt++;
                    i += binsize;
                }
            }
        } else {
            for (TranscriptionStart tSS : tssForAnalysis) {
                if (tSS.getAssignedFeature() != null) {
                    int x;
                    if (tSS.getDist2start() > 0 && tSS.getDist2start() <= leaderlessRange) {
                        x = -tSS.getDist2start();
                        if (notBinedTable.containsKey(x)) {
                            notBinedTable.put(x, notBinedTable.get(x) + 1);
                        } else {
                            notBinedTable.put(x, 1);
                        }
                    } else {
                        x = tSS.getOffset();
                        if (notBinedTable.containsKey(x)) {
                            notBinedTable.put(x, notBinedTable.get(x) + 1);
                        } else {
                            notBinedTable.put(x, 1);
                        }
                    }
                    locus = tSS.getAssignedFeature().getLocus();
                    if (tssToLocus.containsKey(locus)) {
                        tssToLocus.get(locus).add(tSS);
                    } else {
                        tssToLocus.put(locus, new ArrayList<TranscriptionStart>());
                        tssToLocus.get(locus).add(tSS);
                    }
                }
            }

            for (Integer key : notBinedTable.keySet()) {
                data.add(key, notBinedTable.get(key));
            }
        }

        prepareCsvFileForExport(tssToLocus);
        dataList.add(data);
        return dataList;
    }

    /**
     * Prepare the CSV tmp file for putative export of the DataTable and
     * additional information like strand, start and stop of assigned feature,
     * etc.
     *
     * @param tssToLocus List<TranscriptionStart>>
     */
    private void prepareCsvFileForExport(Map<String, List<TranscriptionStart>> tssToLocus) {
        // get reference sequence for promotor regions
        PersistantReference ref = referenceViewer.getReference();

        try {
            this.absFreq5PrimeUtrsInCsv = File.createTempFile("absoluteFrrequencyOf5PrimeUTRs", ".csv");

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(this.absFreq5PrimeUtrsInCsv.getAbsolutePath()), "utf-8"))) {
                writer.write("Gene;Strand;TSS;Gene Start;Start Codon;5'-UTR Length;5-UTR Sequence\n");
                for (List<TranscriptionStart> list : tssToLocus.values()) {
                    for (TranscriptionStart ts : list) {
                        String direction = ts.isFwdStrand() ? "+" : "-";
                        int offset;
                        if (ts.getDetectedGene() != null) {
                            offset = ts.getOffset();
                        } else {
                            offset = ts.getOffsetToNextDownstrFeature();
                        }
                        int geneStart = ts.isFwdStrand() ? ts.getAssignedFeature().getStart() : ts.getAssignedFeature().getStop();
                        writer.write(ts.getAssignedFeature().getLocus() + ";" + direction + ";" + ts.getStartPosition() + ";"
                                + geneStart + ";" + ts.getDetectedFeatStart() + ";" + offset + ";" + get5PrimeUtrSequence(ref, ts, offset) + "\n");
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Prepares the data tables for chart generation.
     *
     * @param chartType ChartType
     * @param elements ElementsOfInterest
     * @param tss List of TranscriptionStartSite instances
     * @param params ParameterSetFiveEnrichedAnalyses
     * @param length for base distribution plot of 5'-UTR sequences
     * @return List<DataTable>
     */
    public List<DataTable> prepareData(ChartType chartType, ChartType baseDistType, ElementsOfInterest elements, List<TranscriptionStart> tss, ParameterSetFiveEnrichedAnalyses params, int length) {

        List<DataTable> dataList = new ArrayList<>();
        List<TranscriptionStart> tssForAnalysis = getTssOfInterest(elements, tss);

        if (chartType == ChartType.BASE_DISTRIBUTION) {
            PersistantReference ref = referenceViewer.getReference();
            Map<String, List<TranscriptionStart>> locusToTSSs = new TreeMap<>();
            for (TranscriptionStart ts : tssForAnalysis) {
                if (ts.getAssignedFeature() != null) {
                    String locus = ts.getAssignedFeature().getLocus();
                    if (locusToTSSs.containsKey(locus)) {
                        locusToTSSs.get(locus).add(ts);
                    } else {
                        List<TranscriptionStart> starts = new ArrayList<>();
                        starts.add(ts);
                        locusToTSSs.put(locus, starts);
                    }
                }
            }
            List<String> tmpSubstrings = new ArrayList<>();
            String substr;
            for (List<TranscriptionStart> list : locusToTSSs.values()) {
                TranscriptionStart tSS = list.get(0);
                int chromID = tSS.getChromId();
                int chromLength = ref.getChromosome(chromID).getLength();
                int featureStart = tSS.getAssignedFeature().getStart();
                int featureStop = tSS.getAssignedFeature().getStop();

                if (tSS.getAssignedFeature().isFwdStrand()) {
                    if (featureStart - 1 - length >= 0) {
                        substr = ref.getChromSequence(chromID, featureStart - length, featureStart + 2);
                        tmpSubstrings.add(substr);
                    } else {
                        int a = length - featureStart - 1;
                        String substr1 = ref.getChromSequence(chromID, ref.getChromosome(chromID).getLength() - a, chromLength);
                        String substr2 = ref.getChromSequence(chromID, 0, featureStart + 2);
                        substr = substr1 + substr2;
                        tmpSubstrings.add(substr);
                    }
                } else {
                    if (featureStop + length >= chromLength) {
                        String substr1 = ref.getChromSequence(chromID, featureStop - 2, chromLength);
                        String substr2 = ref.getChromSequence(chromID, 0, length - (chromLength - featureStop));
                        substr = substr1 + substr2;
                        tmpSubstrings.add(substr);
                    } else {
                        substr = SequenceUtils.getReverseComplement(ref.getChromSequence(chromID, featureStop - 2, featureStop + length));
                        tmpSubstrings.add(substr);
                    }
                }
            }
            DataTable data1 = new DataTable(Double.class, Double.class);
            DataTable data2 = new DataTable(Double.class, Double.class);
            TreeMap<Double, Double[]> map = new TreeMap<>();
            for (double i = 2; i > -(length + 1); i--) {
                map.put(i, new Double[]{0.0, 0.0});
            }

            String[] bases = new String[2];
            if (baseDistType == ChartType.CHARTS_BASE_DIST_GA_CT) {
                bases[0] = "A";
                bases[1] = "G";
            } else {
                bases[0] = "C";
                bases[1] = "G";
            }
            for (String string : tmpSubstrings) {
                double relativePosToFeatureStart = 2; // relative position to feature start
                for (int i = string.length() - 1; i >= 0; i--) {
                    Double[] tmp = map.get(relativePosToFeatureStart);
                    if (string.charAt(i) == bases[0].charAt(0) || string.charAt(i) == bases[1].charAt(0)) {
                        tmp[0]++;
                    } else {
                        tmp[1]++;
                    }
                    map.put(relativePosToFeatureStart, tmp);
                    relativePosToFeatureStart--;
                }
            }

            int totalNoOfReads = tmpSubstrings.size();
            for (Double relPosToFeatureStart : map.keySet()) {
                Double[] absoluteOccurenceOnPosition = map.get(relPosToFeatureStart);
                data1.add(relPosToFeatureStart, absoluteOccurenceOnPosition[0] / totalNoOfReads);
                data2.add(relPosToFeatureStart, absoluteOccurenceOnPosition[1] / totalNoOfReads);
            }

            dataList.add(data2);
            dataList.add(data1);
        }

        if (chartType == ChartType.PIE_CHART) {
            DataTable table = new DataTable(Integer.class, String.class);
            int leaderlessCnt = 0; // 1
            int intragenicCnt = 0; // 2 
            int antisenseCnt = 0; // 3
            int normalTransCnt = 0; // 4
            for (TranscriptionStart ts : tssForAnalysis) {
                if (ts.isLeaderless()) {
                    leaderlessCnt++;
                } else if (!ts.isLeaderless() && (ts.getOffset() > 0 || ts.getOffsetToNextDownstrFeature() > 0)) {
                    normalTransCnt++;
                } else if (ts.isIntragenicTSS()) {
                    intragenicCnt++;
                } else if (ts.isIntragenicAntisense() || ts.isIs3PrimeUtrAntisense() || ts.isIs5PrimeUtrAntisense()) {
                    antisenseCnt++;
                }
            }
            table.add(leaderlessCnt, "Leaderless: " + leaderlessCnt);
            table.add(intragenicCnt, "Intragenic: " + intragenicCnt);
            table.add(antisenseCnt, "Antisense: " + antisenseCnt);
            table.add(normalTransCnt, "Canonical mRNAs (with 5'-UTR): " + normalTransCnt);
            dataList.add(table);
        }

        return dataList;
    }

    /**
     * Returns only the TranscriptionStartSite instances of desired type.
     *
     * @param elements ElementsOfInterest
     * @param tss instance of a TranscriptionsStartSite
     * @return List<TranscriptionStart>
     */
    private List<TranscriptionStart> getTssOfInterest(ElementsOfInterest elements, List<TranscriptionStart> tss) {
        List<TranscriptionStart> resultList = new ArrayList<>();

        if (elements == ElementsOfInterest.ALL) {
            resultList = tss;
        }
        if (elements == ElementsOfInterest.ONLY_ANTISENSE_TSS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isPutativeAntisense()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isLeaderless()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.getOffsetToAssignedFeature() > 0 && !transcriptionStart.isLeaderless()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isSelected()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_TSS_WITH_UTR_INCLUDING_ANTISENSE_LEADERLESS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.getOffsetToAssignedFeature() > 0 || transcriptionStart.isLeaderless() || (transcriptionStart.isPutativeAntisense() && transcriptionStart.getOffsetToAssignedFeature() > 0)) {
                    resultList.add(transcriptionStart);
                }
            }
        }

        return resultList;
    }

    /**
     * Generates a YX-plot for a given DataTable.
     *
     * @param data DataTable
     * @param xAxisLabel Label for x-axis
     * @param yAxisLabel Label for y-axis
     * @param minValue
     * @return
     */
    public InteractivePanel generateYXPlot(DataTable data, String xAxisLabel, String yAxisLabel, Double minValue) {
        XYPlot plot = new XYPlot(data);

        double insetsTop = 20.0,
                insetsLeft = 80.0,
                insetsBottom = 60.0,
                insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(xAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(yAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(2);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setIntersection(minValue);

        plot.getPointRenderer(data).setColor(COLOR1);
        return new InteractivePanel(plot);
    }

    /**
     * Generates a pie plot for a given DataTabel.
     *
     * @param data DataTable
     * @return InteractivePanel
     */
    public InteractivePanel generatePieChart(DataTable data) {

        // Create new pie plot
        PiePlot plot = new PiePlot(data);
        ((ValueLegend) plot.getLegend()).setLabelColumn(1);
        // Format plot
        plot.getTitle().setText("");
        // Change relative size of pie
        plot.setRadius(0.7);
        // Display a legend

        plot.setLegendVisible(true);

        // Add some margin to the plot area
        plot.setInsets(new Insets2D.Double(20.0, 40.0, 40.0, 40.0));

        PiePlot.PieSliceRenderer pointRenderer
                = (PiePlot.PieSliceRenderer) plot.getPointRenderer(data);
        // Change relative size of inner region
        pointRenderer.setInnerRadius(0.4);
        // Change the width of gaps between segments
        pointRenderer.setGap(0.2);
        // Change the colors
        LinearGradient colors = new LinearGradient(COLOR1, COLOR2);
        pointRenderer.setColor(colors);
        // Show labels
        pointRenderer.setValueVisible(true);
        pointRenderer.setValueColor(Color.WHITE);
        pointRenderer.setValueFont(Font.decode(null).deriveFont(Font.BOLD));

        // Add plot to Swing component
        return new InteractivePanel(plot);
    }

    /**
     * Generates a bar plort for a given 2 dim. DataTable.
     *
     * @param data DataTable
     * @param xAxisLabel Label of x-axis
     * @param yAxisLabel Label of y-axis
     * @return InteractivePanel
     */
    public InteractivePanel generateBarPlot(DataTable data, String xAxisLabel, String yAxisLabel) {
        BarPlot plot = new BarPlot(data);
        double insetsTop = 20.0,
                insetsLeft = 80.0,
                insetsBottom = 60.0,
                insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(xAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(yAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(1);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setIntersection(0);
        plot.getAxisRenderer(BarPlot.AXIS_X).setMinorTicksVisible(false);
        plot.setBarWidth(0.8);

        // Format bars
        BarPlot.BarRenderer pointRenderer = (BarPlot.BarRenderer) plot.getPointRenderer(data);
        pointRenderer.setColor(
                new LinearGradientPaint(0f, 0f, 0f, 1f,
                        new float[]{0.0f, 1.0f},
                        new Color[]{COLOR1, GraphicsUtils.deriveBrighter(COLOR1)}));
        pointRenderer.setBorderStroke(new BasicStroke(3f));
        pointRenderer.setBorderColor(
                new LinearGradientPaint(0f, 0f, 0f, 1f,
                        new float[]{0.0f, 1.0f},
                        new Color[]{GraphicsUtils.deriveBrighter(COLOR1), COLOR1}));

        pointRenderer.setValueColor(GraphicsUtils.deriveDarker(COLOR1));
        pointRenderer.setValueFont(Font.decode(null).deriveFont(Font.BOLD));

        return new InteractivePanel(plot);
    }

    /**
     * Generates an overlapped area plot. The input data table is either GC:AT
     * or GA:CT.
     *
     * @param dataCT DataTable of C:T distribution
     * @param dataGA DataTable of G:A distributino
     * @param xAxisLabel Label of x-axis
     * @param yAxisLabel Label of y-axis
     * @return InteractivePanel
     */
    public InteractivePanel generateOverlappedAreaPlot(ChartType baseDistType, DataTable dataA, DataTable dataB, String xAxisLabel, String yAxisLabel) {

        // Create data series
        DataSeries data1;
        DataSeries data2;
        if (baseDistType == ChartType.CHARTS_BASE_DIST_GA_CT) {
            data1 = new DataSeries("CT", dataA, 0, 1);
            data2 = new DataSeries("GA", dataB, 0, 1);
        } else {
            data1 = new DataSeries("AT", dataA, 0, 1);
            data2 = new DataSeries("GC", dataB, 0, 1);
        }

        // Create new xy-plot
        XYPlot plot = new XYPlot(data1, data2);
        plot.setLegendVisible(true);
        plot.setInsets(new Insets2D.Double(20.0, 20.0, 70.0, 100.0));
        plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(xAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(yAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelsOutside(false);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setIntersection(2);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(3);
        plot.getAxisRenderer(XYPlot.AXIS_X).setTickSpacing(1);

        // Format data series
        formatFilledArea(plot, data1, COLOR2);
        formatFilledArea(plot, data2, COLOR1);

        return new InteractivePanel(plot);
    }

    /**
     *
     * @param plot XYPlot
     * @param data DataSource
     * @param color Color
     */
    private static void formatFilledArea(XYPlot plot, DataSource data, Color color) {
        PointRenderer point = new DefaultPointRenderer2D();
        point.setColor(color);
        plot.setPointRenderer(data, point);
        LineRenderer line = new DefaultLineRenderer2D();
        line.setColor(color);
        line.setGap(3.0);
        line.setGapRounded(true);
        plot.setLineRenderer(data, line);
        AreaRenderer area = new DefaultAreaRenderer2D();
        area.setColor(GraphicsUtils.deriveWithAlpha(color, 64));
        plot.setAreaRenderer(data, area);
    }

    /**
     * Returns the csv formattetd file. It consists of 2 column: 1. 5'-UTR
     * length. 2. absolute frequency.
     *
     * @return csv formatted file.
     */
    public File getAbsFreqOf5PrimeUtrsInCSV() {
        return absFreq5PrimeUtrsInCsv;
    }

    /**
     * Prepares the result for output. Any special operations are carried out
     * here. In this case generating the promotor region for each TSS.
     *
     * @return 5'-UTR sequence as a string
     */
    private String get5PrimeUtrSequence(PersistantReference ref, TranscriptionStart tSS, int length) {
        //Generating promotor regions for the TSS
        String utrRegion = "";

        //get the promotor region for each TSS
        int utrStart;
        int chromLength = ref.getChromosome(tSS.getChromId()).getLength();
        if (length != 0) {
            if (tSS.isFwdStrand()) {
                utrStart = tSS.getAssignedFeature().getStart() - length - 1;
                utrStart = utrStart < 0 ? 0 : utrStart;
                utrRegion = ref.getChromSequence(tSS.getChromId(), utrStart, tSS.getAssignedFeature().getStart());
            } else {
                utrStart = tSS.getAssignedFeature().getStop() + length;
                utrStart = utrStart > chromLength ? chromLength : utrStart;
                utrRegion = SequenceUtils.getReverseComplement(ref.getChromSequence(tSS.getChromId(), tSS.getAssignedFeature().getStop(), utrStart));
            }
        }

        return utrRegion;
    }
}
