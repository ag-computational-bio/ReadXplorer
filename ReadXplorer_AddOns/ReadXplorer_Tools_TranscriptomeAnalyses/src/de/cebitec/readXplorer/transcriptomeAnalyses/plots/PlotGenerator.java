/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.plots;

import de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 *
 * @author jritter
 */
public class PlotGenerator {

    private AppPanelTopComponent appPanelTopComponent;

    public PlotGenerator() {
    }

    public void generateYXPlot(DataTable data, String xAxisLabel, String yAxisLabel, String name) {
        this.appPanelTopComponent = new AppPanelTopComponent();
        this.appPanelTopComponent.setLayout(new BorderLayout());
        XYPlot plot = new XYPlot(data);
        double insetsTop = 20.0,
                insetsLeft = 100.0,
                insetsBottom = 60.0,
                insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, xAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, yAxisLabel);
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, 0.0);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, 0.0);


        Color color = new Color(0.0f, 0.3f, 1.0f);
        plot.getPointRenderer(data).setSetting(PointRenderer.COLOR, color);
        this.appPanelTopComponent.add(new InteractivePanel(plot), BorderLayout.CENTER);
        this.appPanelTopComponent.open();
        this.appPanelTopComponent.setName(name);
    }

    public void generateBarPlot(DataTable data, String xAxisLabel, String yAxisLabel, String name) {
        this.appPanelTopComponent = new AppPanelTopComponent();
        this.appPanelTopComponent.setLayout(new BorderLayout());
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
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.LABEL, xAxisLabel);
        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.LABEL, yAxisLabel);
        plot.getAxisRenderer(BarPlot.AXIS_X).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);
        plot.getAxisRenderer(BarPlot.AXIS_Y).setSetting(LogarithmicRenderer2D.INTERSECTION, 0.0);


        Color color = new Color(0.0f, 0.3f, 1.0f);
        plot.getPointRenderer(data).setSetting(PointRenderer.COLOR, color);
        this.appPanelTopComponent.add(new InteractivePanel(plot), BorderLayout.CENTER);
        this.appPanelTopComponent.open();
        this.appPanelTopComponent.setName(name);
    }
}
