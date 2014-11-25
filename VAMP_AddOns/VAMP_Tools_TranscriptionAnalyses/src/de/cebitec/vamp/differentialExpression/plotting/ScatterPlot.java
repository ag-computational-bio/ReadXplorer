package de.cebitec.vamp.differentialExpression.plotting;

import java.awt.RenderingHints;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.ui.ApplicationFrame;
/**
 * A demo of the fast scatter plot.
 *
 */
public class ScatterPlot extends ApplicationFrame {

    /**
     * A constant for the number of items in the sample dataset.
     */
    private static final int COUNT = 500000;
    /**
     * The data.
     */
    private float[][] data = new float[2][COUNT];

    /**
     * Creates a new fast scatter plot demo.
     *
     * @param title the frame title.
     */
    public ScatterPlot(final String title) {

        super(title);
        populateData();
        final NumberAxis domainAxis = new NumberAxis("X");
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setAutoRangeIncludesZero(false);
        final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
        final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        //      panel.setHorizontalZoom(true);
        //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);

        setContentPane(panel);

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    /**
     * Populates the data array with random values.
     */
    private void populateData() {

//        for (int i = 0; i < this.data[0].length; i++) {
//            final float x = (float) i + 100000;
//            this.data[0][i] = x;
//            this.data[1][i] = 100000 + (float) Math.random() * COUNT;
//        }
        this.data[0][0] = 1;
        this.data[1][0] = 1;
        this.data[0][1] = 10;
        this.data[1][1] = 10;
        this.data[0][2] = 100;
        this.data[1][2] = 100;
//        this.data[0][3] = new Float("Infinity");
//        this.data[1][3] = new Float("Infinity");
    }
}


//
//import de.cebitec.vamp.util.Pair;
//import java.awt.Dimension;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.Scene;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.ScatterChart;
//import javafx.scene.chart.XYChart;
//import javafx.scene.chart.XYChart.Series;
//
///**
// *
// * @author kstaderm
// */
//public class ScatterPlot extends JFXPanel {
//
//    private ScatterChart<Number, Number> chart;
//    private NumberAxis xAxis;
//    private NumberAxis yAxis;
//    private Series series = new Series();
//    private Series infSeries = new Series();
//    private Scene scene;
//    private String xAxisName = "";
//    private String yAxisName = "";
//    private Dimension plotDimension;
//
//    public ScatterPlot(Dimension plotDimension) {
//        this.plotDimension = plotDimension;
//    }
//
//    public ScatterPlot(String xAxisName, String yAxisName, Dimension plotDimension) {
//        this.xAxisName = xAxisName;
//        this.yAxisName = yAxisName;
//        this.plotDimension = plotDimension;
//    }
//
//    public void addData() {
//        series.setName("Equities");
//        series.getData().add(new XYChart.Data(new Double("24.3"), new Double("2.93")));
//
//        chart.getData().add(series);
//        scene = new Scene(chart);
//        this.setScene(scene);
//    }
//
//    public void addData(List<Pair<Double, Double>> coordinates, String coordinatesName) {
//        List<Pair<Double, Double>> Infvalues = new ArrayList<>();
//        series.setName(coordinatesName);
//        infSeries.setName("Infinit values");
//        double lowerXbound = 0;
//        double higherXbound = 0;
//        double lowerYbound = 0;
//        double higherYbound = 0;
//        for (Iterator<Pair<Double, Double>> it = coordinates.iterator(); it.hasNext();) {
//            Pair<Double, Double> pair = it.next();
//            Double x = pair.getSecond();
//            Double y = pair.getFirst();
//            if (!x.isInfinite()) {
//                if (x > higherXbound) {
//                    higherXbound = x;
//                }
//                if (x < lowerXbound) {
//                    lowerXbound = x;
//                }
//            } else {
//                Infvalues.add(pair);
//            }
//            if (!y.isInfinite()) {
//                if (y > higherYbound) {
//                    higherYbound = y;
//                }
//                if (y < lowerYbound) {
//                    lowerYbound = y;
//                }
//            } else {
//                Infvalues.add(pair);
//            }
//            if (!x.isInfinite() && !y.isInfinite()) {
//                series.getData().add(new XYChart.Data(x, y));
//            }
//        }
//        for (Iterator<Pair<Double, Double>> it = Infvalues.iterator(); it.hasNext();) {
//            Pair<Double, Double> pair = it.next();
//            Double x = pair.getSecond();
//            Double y = pair.getFirst();
//            if (x.isInfinite() && !y.isInfinite()) {
//                infSeries.getData().add(new XYChart.Data(higherXbound, y));
//            }
//            if (y.isInfinite() && !x.isInfinite()) {
//                infSeries.getData().add(new XYChart.Data(x, higherYbound));
//            }
//            if (x.isInfinite() && y.isInfinite()) {
//                series.getData().add(new XYChart.Data(higherXbound, higherYbound));
//            }
//        }
//        double xStepSize = (Math.abs(lowerXbound) + Math.abs(higherXbound)) / plotDimension.width;
//        double yStepSize = (Math.abs(lowerYbound) + Math.abs(higherYbound)) / plotDimension.height;
//        xAxis = new NumberAxis(xAxisName, lowerXbound, higherXbound, xStepSize);
//        yAxis = new NumberAxis(yAxisName, lowerYbound, higherYbound, yStepSize);
//        chart = new ScatterChart<>(xAxis, yAxis);
//        chart.getData().addAll(series, infSeries);
//        scene = new Scene(chart);
//        this.setScene(scene);
//    }
//}