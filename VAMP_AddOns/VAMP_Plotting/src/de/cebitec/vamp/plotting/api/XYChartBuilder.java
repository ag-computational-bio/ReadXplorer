/* 
 * Maui, Maltcms User Interface. 
 * Copyright (C) 2008-2012, The authors of Maui. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maui may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maui, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maui is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package de.cebitec.vamp.plotting.api;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 *
 * @author Nils Hoffmann
 */
public class XYChartBuilder {
    
    private XYDataset dataset = new DefaultXYDataset();
    
    private ValueAxis domainAxis = new NumberAxis();
    
    private ValueAxis rangeAxis = new NumberAxis();
    
    private XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
    
    private XYToolTipGenerator tooltipGenerator = new StandardXYToolTipGenerator();
    
    private Map<Comparable<?>,Color> datasetSeriesColorMap = null;
    
    private Plot plot;
    
    private JFreeChart chart;
    
    private String chartTitle = "";
    
    private boolean chartPanelBuffer = true;
    
    private boolean chartPanelProperties = true;
    
    private boolean chartPanelSave = true;
    
    private boolean chartPanelPrint = true;
    
    private boolean chartPanelZoom = true;
    
    private boolean chartPanelToolTips = true;
    
    private Font chartTitleFont = JFreeChart.DEFAULT_TITLE_FONT;
    
    private boolean chartCreateLegend = true;
    
    public XYChartBuilder() {
        plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        chart = new JFreeChart(plot);
    }
    
    private void notNull(Object o) {
        if(o==null) {
            throw new NullPointerException("Argument must not be null!");
        }
    }
    
    public XYChartBuilder xy(XYDataset dataset) {
        notNull(dataset);
        this.dataset = dataset;
        return this;
    }
    
    public XYChartBuilder tooltips(XYToolTipGenerator tooltipGenerator) {
        notNull(tooltipGenerator);
        this.tooltipGenerator = tooltipGenerator;
        return this;
    }
    
    public XYChartBuilder xyz(XYZDataset dataset) {
        notNull(dataset);
        this.dataset = dataset;
        return this;
    }
    
    public XYChartBuilder colors(Map<Comparable<?>,Color> datasetSeriesColorMap) {
        notNull(datasetSeriesColorMap);
        this.datasetSeriesColorMap = datasetSeriesColorMap;
        return this;
    }
    
    public XYChartBuilder renderer(XYItemRenderer renderer) {
        notNull(renderer);
        this.renderer = renderer;
        return this;
    }
    
    public XYChartBuilder tooltips(boolean b) {
        this.chartPanelToolTips = b;
        return this;
    }
    
    public XYChartBuilder titleFont(Font font) {
        notNull(font);
        this.chartTitleFont = font;
        return this;
    }
    
    public XYChartBuilder createLegend(boolean b) {
        this.chartCreateLegend = b;
        return this;
    }
    
    public XYChartBuilder chart(String title) {
        notNull(title);
        this.chart = new JFreeChart(title, chartTitleFont, plot, chartCreateLegend);
        return this;
    }
	
	public XYChartBuilder domainAxis(ValueAxis axis) {
		notNull(axis);
		this.domainAxis = axis;
		return this;
	}
	
	public XYChartBuilder rangeAxis(ValueAxis axis) {
		notNull(axis);
		this.rangeAxis = axis;
		return this;
	}
    
    public XYChartBuilder plot() {
        this.plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        renderer.setBaseToolTipGenerator(tooltipGenerator);
        setDatasetSeriesColorMap(renderer, datasetSeriesColorMap);
        return this;
    }
    
    protected void setDatasetSeriesColorMap(XYItemRenderer renderer, Map<Comparable<?>,Color> datasetSeriesColorMap) {
        if(datasetSeriesColorMap!=null) {
            if(datasetSeriesColorMap.keySet().size()!=dataset.getSeriesCount()) {
                throw new IllegalArgumentException("Mismatch in series colors and series count!");
            }
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                Comparable<?> key = dataset.getSeriesKey(i);
                renderer.setSeriesPaint(i, datasetSeriesColorMap.get(key));
            }
        }
    }
    
    public ChartFrame buildFrame(boolean scrollPane) {
        ChartFrame chartFrame = new ChartFrame(chartTitle, chart, scrollPane);
        return chartFrame;
    }
    
    public ChartPanel buildPanel() {
        ChartPanel chartPanel = new ChartPanel(chart, chartPanelProperties, chartPanelSave, chartPanelPrint, chartPanelZoom, chartPanelToolTips);
        chartPanel.setMouseWheelEnabled(true);
        return chartPanel;
    }
    
}
