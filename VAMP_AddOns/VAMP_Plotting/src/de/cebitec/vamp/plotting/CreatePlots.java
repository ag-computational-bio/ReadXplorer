package de.cebitec.vamp.plotting;

import de.cebitec.vamp.util.Pair;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author kstaderm
 */
public class CreatePlots {
    
    private final static Shape cross = ShapeUtilities.createDiagonalCross(3, 1);

    public synchronized static ChartPanel createPlot(List<Pair<Double, Double>> data, String xName, String yName) {
        XYSeriesCollection normal = new XYSeriesCollection();
        XYSeries nor = new XYSeries("Normal");
        for (Iterator<Pair<Double, Double>> it = data.iterator(); it.hasNext();) {
            Pair<Double, Double> pair = it.next();
            Double X = pair.getFirst();
            Double Y = pair.getSecond();
                nor.add(X, Y);
        }
        normal.addSeries(nor);
        // create subplot 1...
        final XYItemRenderer renderer1 = new XYShapeRenderer();
        renderer1.setSeriesShape(0, cross);
        final NumberAxis domainAxis1 = new NumberAxis(xName);
        final NumberAxis rangeAxis1 = new NumberAxis(yName);
        final XYPlot subplot1 = new XYPlot(normal, domainAxis1, rangeAxis1, renderer1);
        JFreeChart chart = new JFreeChart(subplot1);
        chart.removeLegend();
        final ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
        return panel;
    }

    public synchronized static ChartPanel createInfPlot(List<Pair<Double, Double>> data, String xName, String yName) {
        XYSeriesCollection normal = new XYSeriesCollection();
        XYSeriesCollection posInf = new XYSeriesCollection();
        XYSeriesCollection negInf = new XYSeriesCollection();
        XYSeries nor = new XYSeries("Normal");
        XYSeries pos = new XYSeries("Positiv Infinit");
        XYSeries neg = new XYSeries("Negativ Infinit");
        for (Iterator<Pair<Double, Double>> it = data.iterator(); it.hasNext();) {
            Pair<Double, Double> pair = it.next();
            Double X = pair.getFirst();
            Double Y = pair.getSecond();

            if (Y == Double.POSITIVE_INFINITY) {
                Y = 0d;
                pos.add(X, Y);
            }
            if (Y == Double.NEGATIVE_INFINITY) {
                Y = 0d;
                neg.add(X, Y);
            }
            if (!Y.isInfinite() && !X.isInfinite()) {
                nor.add(X, Y);
            }
        }
        normal.addSeries(nor);
        posInf.addSeries(pos);
        negInf.addSeries(neg);
        JFreeChart chart = createCombinedChart(normal, posInf, negInf, xName, yName);
        chart.removeLegend();
        final ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
        return panel;
    }

    private synchronized static JFreeChart createCombinedChart(XYSeriesCollection normal,
            XYSeriesCollection posInf, XYSeriesCollection negInf, String xName, String yName) {       

        // create subplot 1...
        final XYDataset data1 = normal;
        final XYItemRenderer renderer1 = new XYShapeRenderer();
        renderer1.setSeriesShape(0, cross);
        final NumberAxis rangeAxis1 = new NumberAxis(yName);
        final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        // create subplot 2...
        final XYDataset data2 = negInf;
        final XYItemRenderer renderer2 = new XYShapeRenderer();
        renderer2.setSeriesShape(0, cross);
        final NumberAxis rangeAxis2 = new NumberAxis() {
            @Override
            public List refreshTicks(Graphics2D g2, AxisState state,
                    Rectangle2D dataArea, RectangleEdge edge) {
                List myTicks = new ArrayList();
                myTicks.add(new NumberTick(0, "-Inf", TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
                return myTicks;
            }
        };
        rangeAxis2.setAutoRangeIncludesZero(false);
//        rangeAxis2.setRange(0,0);
        final XYPlot subplot2 = new XYPlot(data2, null, rangeAxis2, renderer2);
        subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        // create subplot 3...
        final XYDataset data3 = posInf;
        final XYItemRenderer renderer3 = new XYShapeRenderer();
        renderer3.setSeriesShape(0, cross);
        final NumberAxis rangeAxis3 = new NumberAxis() {
            @Override
            public List refreshTicks(Graphics2D g2, AxisState state,
                    Rectangle2D dataArea, RectangleEdge edge) {
                List myTicks = new ArrayList();
                myTicks.add(new NumberTick(0, "Inf", TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
                return myTicks;
            }
        };
        rangeAxis3.setAutoRangeIncludesZero(false);
//        rangeAxis3.setRange(0,0);
        final XYPlot subplot3 = new XYPlot(data3, null, rangeAxis3, renderer3);
        subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        // parent plot...
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis(xName));
        plot.setGap(0);

        // add the subplots...
        plot.add(subplot3, 1);
        plot.add(subplot1, 10);
        plot.add(subplot2, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);

        // return a new chart containing the overlaid plot...
        return new JFreeChart(plot);

    }
}
