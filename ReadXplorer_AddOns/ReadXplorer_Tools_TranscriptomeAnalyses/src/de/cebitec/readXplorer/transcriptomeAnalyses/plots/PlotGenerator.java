/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.plots;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public PlotGenerator() {
    }

    public List<DataTable> prepareData(ChartType chartType, ElementsOfInterest elements, List<TranscriptionStart> tss, ReferenceViewer refViewer, int length, int bin) {

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
            HashMap<Double, Double[]> map = new HashMap<>();
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

            for (Double relPosToFeatureStart : map.keySet()) {
                Double[] absoluteOccurenceOnPosition = map.get(relPosToFeatureStart);
                dataGA.add(relPosToFeatureStart, absoluteOccurenceOnPosition[0]);
                dataCT.add(relPosToFeatureStart, absoluteOccurenceOnPosition[1]);
            }

            dataList.add(dataCT);
            dataList.add(dataGA);
        }

        if (chartType == ChartType.DISTRIBUTION_OF_ALL_TSS_OFFSETS_LENGTH) {
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

    public InteractivePanel generateYXPlot(DataTable data, String xAxisLabel, String yAxisLabel) {
        XYPlot plot = new XYPlot(data);

        double insetsTop = 20.0,
                insetsLeft = 100.0,
                insetsBottom = 60.0,
                insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
//        plot.setAxisRenderer(yAxisLabel, null);
//        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, xAxisLabel);
//        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, yAxisLabel);
//        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, 0.0);
//        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, 0.0);


        Color color = new Color(0.0f, 0.3f, 1.0f);
//        plot.getPointRenderer(data).setSetting(PointRenderer.COLOR, color);
        return new InteractivePanel(plot);
    }

    public InteractivePanel generateBarPlot(DataTable data, String xAxisLabel, String yAxisLabel) {
        BarPlot plot = new BarPlot(data);
//                plot.setBounds(5000, 5000, 500, 500);
//                LogarithmicRenderer2D rendererX = new LogarithmicRenderer2D();
//                LogarithmicRenderer2D rendererY = new LogarithmicRenderer2D();
//                plot.setAxisRenderer(BarPlot.AXIS_X, rendererX);
//                plot.setAxisRenderer(BarPlot.AXIS_Y, rendererY);
        double insetsTop = 20.0,
                insetsLeft = 100.0,
                insetsBottom = 60.0,
                insetsRight = 40.0;
//        plot.setInsets(new Insets2D.Double(
//                insetsTop, insetsLeft, insetsBottom, insetsRight));
//        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.LABEL, xAxisLabel);
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.LABEL, yAxisLabel);
//        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);


        Color color = new Color(0.0f, 0.3f, 1.0f);
        plot.getPointRenderer(data).setColor(COLOR1);

        return new InteractivePanel(plot);
    }

    public InteractivePanel generateOverlappedBarPlot(DataTable dataCT, DataTable dataGA, String xAxisLabel, String yAxisLabel) {

        BarPlot plot = new BarPlot();

        plot.add(dataCT);
        plot.add(dataGA);

        plot.setInsets(new Insets2D.Double(
                20.0, 40.0, 60.0, 125.0));

//        plot.setSetting(Plot.LEGEND, true);
//        plot.getLegend().setSetting(Legend.ORIENTATION, Orientation.HORIZONTAL);
//        plot.getLegend().setSetting(Legend.ALIGNMENT_X, 1.0);
//        plot.getLegend().setSetting(Legend.ALIGNMENT_Y, 1.0);
//                    plot.getLegend().setSetting(BarPlot.BarPlotLegend.LABEL_FORMAT, );

        //            plot.setSetting(Plot.LEGEND_DISTANCE, 2.0);
        //            plot.setSetting(Plot.LEGEND_LOCATION, Location.SOUTH);
//        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.LABEL, "relative positions to feature start");
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.LABEL, "absolute occurence of bases");
//        plot.getPointRenderer(dataGA).setSetting(PointRenderer.COLOR, new Color(0, 0, 205, 100)); // blue
//        plot.getPointRenderer(dataCT).setSetting(PointRenderer.COLOR, new Color(255, 0, 0, 100)); // red
//        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, 0.0);
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, 0.0);
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(AxisRenderer.TICK_LABELS_OUTSIDE, false);
//        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_DISTANCE, 3);

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
}
