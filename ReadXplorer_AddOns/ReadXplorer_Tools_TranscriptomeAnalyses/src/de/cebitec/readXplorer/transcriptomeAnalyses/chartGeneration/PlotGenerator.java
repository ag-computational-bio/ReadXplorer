/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.areas.LineAreaRenderer2D;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author jritter
 */
public class PlotGenerator implements Observer {

    /**
     * First corporate color used for normal coloring.
     */
    protected static final Color COLOR1 = new Color(55, 170, 200);
    /**
     * Second corporate color used as signal color
     */
    protected static final Color COLOR2 = new Color(200, 80, 75);
    private double smallestColumnValue = 0.0;

    public List<DataTable> prepareData(ChartType chartType, ElementsOfInterest elements, List<TranscriptionStart> tss, ReferenceViewer refViewer, int length) {

        List<DataTable> dataList = new ArrayList<>();
        List<TranscriptionStart> tssForAnalysis = getTssOfInterest(elements, tss);

        if (chartType == ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs) {
            DataTable data = new DataTable(Double.class, Double.class);
            HashMap<Double, Double> tmpMap = new HashMap<>();
            double maxX = 0;
            double minX = 0;
            for (TranscriptionStart tSS : tssForAnalysis) {
                // We want to show the distribution of length between TSS to TLS
                double x = tSS.getOffset();
                if (tSS.isLeaderless() && x == 0) {
                    x = -tSS.getDist2start();
                }

                if (x > maxX) {
                    maxX = x;
                }
                if (x < minX) {
                    minX = x;
                }
                if (tmpMap.containsKey(x)) {
                    tmpMap.put(x, tmpMap.get(x) + 1.0);
                } else {
                    tmpMap.put(x, 1.0);
                }

            }

            for (Double x : tmpMap.keySet()) {
                data.add(x, tmpMap.get(x));
            }

            dataList.add(data);
        }

        if (chartType == ChartType.BASE_DISTRIBUTION) {
            HashMap<Integer, PersistantChromosome> chromosomes = (HashMap<Integer, PersistantChromosome>) refViewer.getReference().getChromosomes();
            StringBuffer buffer;
            List<String> tmpSubstrings = new ArrayList<>();
            String substr;
            for (TranscriptionStart tSS : tssForAnalysis) {

                int chromID = tSS.getChromId();
                int featureStart = tSS.getAssignedFeature().getStart();
                int featureStop = tSS.getAssignedFeature().getStop();
                if (tSS.isFwdStrand()) {
                    substr = chromosomes.get(chromID).getSequence(this).substring(featureStart - 1 - length, featureStart - 1);
                    tmpSubstrings.add(substr);
                } else {
                    substr = chromosomes.get(chromID).getSequence(this).substring(featureStop + 1, featureStop + 1 + length);
                    buffer = new StringBuffer(substr);
                    String reversedSubstr = buffer.reverse().toString();
                    String complement = Complement(reversedSubstr);
                    tmpSubstrings.add(complement);
                }
            }
            DataTable dataGA = new DataTable(Double.class, Double.class);
            DataTable dataCT = new DataTable(Double.class, Double.class);
            TreeMap<Double, Double[]> map = new TreeMap<>();
            for (double i = -1; i > -(length + 1); i--) {
                map.put(i, new Double[]{0.0, 0.0});
            }

            for (String string : tmpSubstrings) {
                double relativePosToFeatureStart = -1; // relative position to feature start
                for (int i = string.length() - 1; i >= 0; i--) {
                    Double[] tmp = map.get(relativePosToFeatureStart);
                    if (string.charAt(i) == 'A' || string.charAt(i) == 'G') {
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
                dataGA.add(relPosToFeatureStart, absoluteOccurenceOnPosition[0] / totalNoOfReads);
                dataCT.add(relPosToFeatureStart, absoluteOccurenceOnPosition[1] / totalNoOfReads);
            }

            dataList.add(dataCT);
            dataList.add(dataGA);
        }

        if (chartType == ChartType.DISTRIBUTION_OF_ALL_TSS_OFFSETS_LENGTH) {
            
            // TODO here we need some BITS!
            DataTable data = new DataTable(Double.class, Double.class);
            // We want to show the distribution of length between TSS to TLS
            for (TranscriptionStart tSS : tssForAnalysis) {
                double x = tSS.getOffset();
                if (tSS.isLeaderless() && x == 0) {
                    x = -tSS.getDist2start();
                }

                double y = tSS.getReadStarts();
                data.add(x, y);
            }

            dataList.add(data);
        }
        return dataList;
    }

    @Override
    public void update(Object args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<TranscriptionStart> getTssOfInterest(ElementsOfInterest elements, List<TranscriptionStart> tss) {
        List<TranscriptionStart> resultList = new ArrayList<>();

        if (elements == ElementsOfInterest.ALL) {
            resultList = tss;
        }
        if (elements == ElementsOfInterest.ONLY_ANTISENSE) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isPutativeAntisense()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_LEADERLESS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isLeaderless()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_NONE_LEADERLESS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (!transcriptionStart.isLeaderless()) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_REAL_TSS) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.getOffset() > 0) {
                    resultList.add(transcriptionStart);
                }
            }
        }
        if (elements == ElementsOfInterest.ONLY_SELECTED) {
            for (TranscriptionStart transcriptionStart : tss) {
                if (transcriptionStart.isSelected()) {
                    resultList.add(transcriptionStart);
                }
            }
        }

        return resultList;
    }

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

    public InteractivePanel generateBarPlot(DataTable data, String xAxisLabel, String yAxisLabel, Double minValue) {
        BarPlot plot = new BarPlot(data);
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
     *
     * @param dataCT
     * @param dataGA
     * @param xAxisLabel
     * @param yAxisLabel
     * @return
     */
    public InteractivePanel generateOverlappedAreaPlot(DataTable dataCT, DataTable dataGA, String xAxisLabel, String yAxisLabel) {

        // Create data series
        DataSeries data1 = new DataSeries("CT", dataCT, 0, 1);
        DataSeries data2 = new DataSeries("GA", dataGA, 0, 1);

        // Create new xy-plot
        XYPlot plot = new XYPlot(data1, data2);
        plot.setLegendVisible(true);
        plot.setInsets(new Insets2D.Double(20.0, 20.0, 70.0, 100.0));
        plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(xAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(yAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelsOutside(false);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(3);
        plot.getAxisRenderer(XYPlot.AXIS_X).setTickSpacing(1);

        // Format data series
        formatFilledArea(plot, data1, COLOR2);
        formatFilledArea(plot, data2, COLOR1);

        return new InteractivePanel(plot);

    }

    /**
     * Gets a DNA String and complement it. A to T, T to A, G to C and C to G.
     *
     * @param seq is DNA String.
     * @return the compliment of seq.
     */
    private String Complement(String seq) {
        char BASE_A = 'A';
        char BASE_C = 'C';
        char BASE_G = 'G';
        char BASE_T = 'T';
        String a = "A";
        String c = "C";
        String g = "G";
        String t = "T";
        String compliment = "";

        for (int i = 0; i < seq.length(); i++) {
            if (BASE_A == seq.charAt(i)) {
                compliment = compliment.concat(t);
            } else if (BASE_C == (seq.charAt(i))) {
                compliment = compliment.concat(g);

            } else if (BASE_G == seq.charAt(i)) {
                compliment = compliment.concat(c);

            } else if (BASE_T == seq.charAt(i)) {
                compliment = compliment.concat(a);
            }
        }

        return compliment;

    }

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

    private static void formatLineArea(XYPlot plot, DataSource data, Color color) {
        PointRenderer point = new DefaultPointRenderer2D();
        point.setColor(color);
        plot.setPointRenderer(data, point);
        plot.setLineRenderer(data, null);
        AreaRenderer area = new LineAreaRenderer2D();
        area.setGap(3.0);
        area.setColor(color);
        plot.setAreaRenderer(data, area);
    }

    public double getSmallestColumnValue() {
        return smallestColumnValue;
    }

    public void setSmallestColumnValue(double smallestColumnValue) {
        this.smallestColumnValue = smallestColumnValue;
    }
}
