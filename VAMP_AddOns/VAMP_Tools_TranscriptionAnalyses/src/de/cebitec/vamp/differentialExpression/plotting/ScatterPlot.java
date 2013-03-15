package de.cebitec.vamp.differentialExpression.plotting;

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

    public ScatterPlot() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        chart = new ScatterChart<>(xAxis, yAxis);
    }

    public ScatterPlot(String xAxisName, String yAxisName) {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel(xAxisName);
        yAxis.setLabel(yAxisName);
        chart = new ScatterChart<>(xAxis, yAxis);
    }

    public void addData() {
        series.setName("Equities");
        series.getData().add(new XYChart.Data(4.2, 193.2));
        series.getData().add(new XYChart.Data(2.8, 33.6));
        series.getData().add(new XYChart.Data(6.2, 24.8));
        series.getData().add(new XYChart.Data(1, 14));
        series.getData().add(new XYChart.Data(1.2, 26.4));
        series.getData().add(new XYChart.Data(4.4, 114.4));
        series.getData().add(new XYChart.Data(8.5, 323));
        series.getData().add(new XYChart.Data(6.9, 289.8));
        series.getData().add(new XYChart.Data(9.9, 287.1));
        series.getData().add(new XYChart.Data(0.9, -9));
        series.getData().add(new XYChart.Data(3.2, 150.8));
        series.getData().add(new XYChart.Data(4.8, 20.8));
        series.getData().add(new XYChart.Data(7.3, -42.3));
        series.getData().add(new XYChart.Data(1.8, 81.4));
        series.getData().add(new XYChart.Data(7.3, 110.3));
        series.getData().add(new XYChart.Data(2.7, 41.2));
        chart.getData().add(series);
        scene = new Scene(chart);
        this.setScene(scene);
    }
}