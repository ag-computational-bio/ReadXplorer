/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.plotting;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.utils.Pair;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.labels.XYToolTipGenerator;
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


/**
 *
 * @author kstaderm
 */
public final class CreatePlots {

    private CreatePlots() {
    }


    public static synchronized ChartPanel createPlot( Map<PersistentFeature, Pair<Double, Double>> data, String xName, String yName, XYToolTipGenerator toolTip ) {
        XYSeriesCollection normal = new XYSeriesCollection();
        XYSeries nor = new XYSeries( "Normal" );
        for( PersistentFeature key : data.keySet() ) {
            Pair<Double, Double> pair = data.get( key );
            Double x = pair.getFirst();
            Double y = pair.getSecond();
            nor.add( new PlotDataItem( key, x, y ) );
        }
        normal.addSeries( nor );
        // create subplot 1...
        final XYItemRenderer renderer1 = new XYShapeRenderer();
        renderer1.setBaseToolTipGenerator( toolTip );
        final NumberAxis domainAxis1 = new NumberAxis( xName );
        final NumberAxis rangeAxis1 = new NumberAxis( yName );
        final XYPlot subplot1 = new XYPlot( normal, domainAxis1, rangeAxis1, renderer1 );
        JFreeChart chart = new JFreeChart( subplot1 );
        chart.removeLegend();
        ChartPanel panel = new ChartPanel( chart, true, false, true, true, true );
        panel.setInitialDelay( 0 );
        panel.setMaximumDrawHeight( 1080 );
        panel.setMaximumDrawWidth( 1920 );
        panel.setMouseWheelEnabled( true );
        panel.setMouseZoomable( true );
        MouseActions mouseAction = new MouseActions();
        panel.addChartMouseListener( mouseAction );
        ChartPanelOverlay overlay = new ChartPanelOverlay( mouseAction );
        panel.addOverlay( overlay );
        return panel;
    }


    public static synchronized ChartPanel createInfPlot( Map<PersistentFeature, Pair<Double, Double>> data, String xName, String yName, XYToolTipGenerator toolTip ) {
        XYSeriesCollection normal = new XYSeriesCollection();
        XYSeriesCollection posInf = new XYSeriesCollection();
        XYSeriesCollection negInf = new XYSeriesCollection();
        XYSeries nor = new XYSeries( "Normal" );
        XYSeries pos = new XYSeries( "Positive Infinite" );
        XYSeries neg = new XYSeries( "Negative Infinite" );
        for( PersistentFeature key : data.keySet() ) {
            Pair<Double, Double> pair = data.get( key );
            Double x = pair.getFirst();
            Double y = pair.getSecond();

            if( y == Double.POSITIVE_INFINITY ) {
                y = 0d;
                pos.add( new PlotDataItem( key, x, y ) );
            }
            if( y == Double.NEGATIVE_INFINITY ) {
                y = 0d;
                neg.add( new PlotDataItem( key, x, y ) );
            }
            if( !y.isInfinite() && !x.isInfinite() ) {
                nor.add( new PlotDataItem( key, x, y ) );
            }
        }
        normal.addSeries( nor );
        posInf.addSeries( pos );
        negInf.addSeries( neg );
        JFreeChart chart = createCombinedChart( normal, posInf, negInf, xName, yName, toolTip );
        chart.removeLegend();
        ChartPanel panel = new ChartPanel( chart, true, false, true, true, true );
        panel.setInitialDelay( 0 );
        panel.setMaximumDrawHeight( 1080 );
        panel.setMaximumDrawWidth( 1920 );
        panel.setMouseWheelEnabled( true );
        panel.setMouseZoomable( true );
        MouseActions mouseAction = new MouseActions();
        panel.addChartMouseListener( mouseAction );
        ChartPanelOverlay overlay = new ChartPanelOverlay( mouseAction );
        panel.addOverlay( overlay );
        return panel;
    }


    private static synchronized JFreeChart createCombinedChart( XYSeriesCollection normal,
                                                                XYSeriesCollection posInf, XYSeriesCollection negInf, String xName, String yName, XYToolTipGenerator toolTip ) {

        final NumberAxis domainAxis = new NumberAxis( xName );

        // create subplot 1...
        final XYDataset data1 = normal;
        final XYItemRenderer renderer1 = new XYShapeRenderer();
        renderer1.setBaseToolTipGenerator( toolTip );
        final NumberAxis rangeAxis1 = new NumberAxis( yName );
        final XYPlot subplot1 = new XYPlot( data1, domainAxis, rangeAxis1, renderer1 );
        subplot1.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );

        // create subplot 2...
        final XYDataset data2 = negInf;
        final XYItemRenderer renderer2 = new XYShapeRenderer();
        renderer2.setBaseToolTipGenerator( toolTip );
        final NumberAxis rangeAxis2 = new NumberAxis() {
            @Override
            public List<NumberTick> refreshTicks( Graphics2D g2, AxisState state,
                                      Rectangle2D dataArea, RectangleEdge edge ) {
                return Collections.singletonList( new NumberTick( 0, "-Inf", TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0 ) );
            }


        };
        rangeAxis2.setAutoRangeIncludesZero( false );
        final XYPlot subplot2 = new XYPlot( data2, domainAxis, rangeAxis2, renderer2 );
        subplot2.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );

        // create subplot 3...
        final XYDataset data3 = posInf;
        final XYItemRenderer renderer3 = new XYShapeRenderer();
        renderer3.setBaseToolTipGenerator( toolTip );
        final NumberAxis rangeAxis3 = new NumberAxis() {
            @Override
            public List<NumberTick> refreshTicks( Graphics2D g2, AxisState state,
                                      Rectangle2D dataArea, RectangleEdge edge ) {
                return Collections.singletonList( new NumberTick( 0, "Inf", TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0 ) );
            }


        };
        rangeAxis3.setAutoRangeIncludesZero( false );
        final XYPlot subplot3 = new XYPlot( data3, domainAxis, rangeAxis3, renderer3 );
        subplot2.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );

        // parent plot...
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot( domainAxis );
        plot.setGap( 0 );

        // add the subplots...
        plot.add( subplot3, 1 );
        plot.add( subplot1, 10 );
        plot.add( subplot2, 1 );
        plot.setOrientation( PlotOrientation.VERTICAL );

        // return a new chart containing the overlaid plot...
        return new JFreeChart( plot );

    }


}
