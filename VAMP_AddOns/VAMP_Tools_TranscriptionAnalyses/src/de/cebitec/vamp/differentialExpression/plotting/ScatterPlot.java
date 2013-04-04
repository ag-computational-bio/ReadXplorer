package de.cebitec.vamp.differentialExpression.plotting;

import de.cebitec.vamp.util.Pair;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

/**
 *
 * @author kstaderm
 */
public class ScatterPlot extends JFXPanel {

    private ScatterChart<Number, Number> chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Series series = new Series();
    private Scene scene;
    private String xAxisName = "";
    private String yAxisName = "";
    private Dimension plotDimension;

    public ScatterPlot(Dimension plotDimension) {
        this.plotDimension = plotDimension;
    }

    public ScatterPlot(String xAxisName, String yAxisName, Dimension plotDimension) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        this.plotDimension = plotDimension;
    }

    public void addData() {
        series.setName("Equities");
        series.getData().add(new XYChart.Data(new Double("24.3"), new Double("2.93")));

        chart.getData().add(series);
        scene = new Scene(chart);
        this.setScene(scene);
    }

    public void addData(List<Pair<Double, Double>> coordinates, String coordinatesName) {
        series.setName(coordinatesName);
        double lowerXbound = 0;
        double higherXbound = 0;
        double lowerYbound = 0;
        double higherYbound = 0;
        for (Iterator<Pair<Double, Double>> it = coordinates.iterator(); it.hasNext();) {
            Pair<Double, Double> pair = it.next();
            Double x = pair.getSecond();
            Double y = pair.getFirst();
            series.getData().add(new XYChart.Data(x, y));
            if (!x.isInfinite()) {
                if (x > higherXbound) {
                    higherXbound = x;
                }
                if (x < lowerXbound) {
                    lowerXbound = x;
                }
            }
            if (!y.isInfinite()) {
                if (y > higherYbound) {
                    higherYbound = y;
                }
                if (y < lowerYbound) {
                    lowerYbound = y;
                }
            }

        }
        double xStepSize = (Math.abs(lowerXbound)+Math.abs(higherXbound))/plotDimension.width;
        double yStepSize = (Math.abs(lowerYbound)+Math.abs(higherYbound))/plotDimension.height;
        xAxis = new NumberAxis(xAxisName, lowerXbound, higherXbound, xStepSize);
        yAxis = new NumberAxis(yAxisName, lowerYbound, higherYbound, yStepSize);
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.getData().add(series);
        scene = new Scene(chart);
        this.setScene(scene);
    }
}