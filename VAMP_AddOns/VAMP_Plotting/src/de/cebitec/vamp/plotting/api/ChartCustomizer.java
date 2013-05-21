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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.util.List;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author Nils Hoffmann
 */
public class ChartCustomizer {

//    public static final Color[] baseColors = new Color[]{
//        new Color(166, 206, 227), 
//        new Color(31, 120, 180),
//        new Color(178, 223, 138),
//        new Color(51, 160, 44),
//        new Color(251, 154, 153),
//        new Color(227, 26, 28),
//        new Color(253, 191, 111),
//        new Color(255, 127, 0),
//        new Color(202, 178, 214),
//        new Color(106, 61, 154)};
    public static final Color[] baseColors = new Color[]{
        new Color(166, 206, 227),
        new Color(178, 223, 138),
        new Color(251, 154, 153),
        new Color(253, 191, 111),
        new Color(202, 178, 214),
        new Color(31, 120, 180),
        new Color(51, 160, 44),
        new Color(227, 26, 28),
        new Color(255, 127, 0),
        new Color(106, 61, 154),};
    public static final Color[] plotColors = new Color[baseColors.length * 2];

    static {
        int cnt = 0;
        for (Color c : baseColors) {
            plotColors[cnt] = c.darker();
            plotColors[baseColors.length + cnt] = c;
            cnt++;
        }
    }

    public static void setSeriesColors(XYPlot plot, float alpha) {
        XYItemRenderer renderer = plot.getRenderer();
        int series = plot.getSeriesCount();
        for (int i = 0; i < series; i++) {
            renderer.setSeriesPaint(i,
                    withAlpha(plotColors[i % plotColors.length], alpha));
        }
    }

    public static Color withAlpha(Color color, float alpha) {
        Color ca = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int) (alpha * 255.0f));
        return ca;
    }

    public static void setSeriesStrokes(XYPlot plot, float width) {
        XYItemRenderer renderer = plot.getRenderer();
        int series = plot.getSeriesCount();
        for (int i = 0; i < series; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(width,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }
    
    public static void setSeriesShapes(XYPlot plot, Shape s) {
        XYItemRenderer renderer = plot.getRenderer();
        int series = plot.getSeriesCount();
        for (int i = 0; i < series; i++) {
            renderer.setSeriesShape(i, s);
        }
    }

    public static void setSeriesColors(CategoryPlot plot, float alpha) {

        int datasets = plot.getDatasetCount();
        for (int i = 0; i < datasets; i++) {
            CategoryDataset ds = plot.getDataset(i);
            CategoryItemRenderer renderer = plot.getRenderer(i);
            System.out.println("Dataset has " + ds.getRowCount() + " rows");
            System.out.println("Dataset has " + ds.getColumnCount() + " columns");
            for (int j = 0; j < ds.getRowCount(); j++) {
                renderer.setSeriesPaint(j,
                        withAlpha(plotColors[j % plotColors.length], alpha));
            }
        }
    }

    public static void setSeriesColors(CategoryPlot plot, float alpha, List<Color> colors) {
        int datasets = plot.getDatasetCount();
        for (int i = 0; i < datasets; i++) {
            CategoryDataset ds = plot.getDataset(i);
            CategoryItemRenderer renderer = plot.getRenderer(i);
            System.out.println("Dataset has " + ds.getRowCount() + " rows");
            System.out.println("Dataset has " + ds.getColumnCount() + " columns");
            for (int j = 0; j < ds.getRowCount(); j++) {
                renderer.setSeriesPaint(j,
                        withAlpha(colors.get(j), alpha));
            }
        }
    }
}
