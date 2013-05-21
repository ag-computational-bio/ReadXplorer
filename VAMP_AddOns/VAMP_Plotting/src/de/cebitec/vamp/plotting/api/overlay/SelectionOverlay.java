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
package de.cebitec.vamp.plotting.api.overlay;

import de.cebitec.vamp.plotting.api.selection.SelectionChangeEvent;
import de.cebitec.vamp.plotting.api.selection.XYSelection;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.data.xy.XYDataset;
import org.openide.util.WeakListeners;

/**
 *
 * @author Nils Hoffmann
 */
public class SelectionOverlay extends AbstractChartOverlay implements ChartOverlay, PropertyChangeListener {

    private XYSelection mouseHoverSelection;
    private final Set<XYSelection> mouseClickSelection = new LinkedHashSet<XYSelection>();
    private Color selectionFillColor = new Color(255, 64, 64);
    private Color hoverFillColor = new Color(64, 64, 255);
    private float hoverScaleX = 2.5f;
    private float hoverScaleY = 2.5f;
    private float fillAlpha = 0.5f;
    private final Crosshair domainCrosshair;
    private final Crosshair rangeCrosshair;
    private final CrosshairOverlay crosshairOverlay;
    public static final String PROP_SELECTION_FILL_COLOR = "selectionFillColor";
    public static final String PROP_HOVER_FILL_COLOR = "hoverFillColor";
    public static final String PROP_HOVER_SCALE_X = "hoverScaleX";
    public static final String PROP_HOVER_SCALE_Y = "hoverScaleY";
    public static final String PROP_FILL_ALPHA = "fillAlpha";
    public static final String PROP_SELECTION = "selection";
    public static final String PROP_HOVER_SELECTION = "hoverSelection";

    public SelectionOverlay(Color selectionFillColor, Color hoverFillColor, float hoverScaleX, float hoverScaleY, float fillAlpha) {
        super("Selection", "Selection", "Overlay for chart item entity selection", true);
        this.selectionFillColor = selectionFillColor;
        this.hoverFillColor = hoverFillColor;
        this.hoverScaleX = hoverScaleX;
        this.hoverScaleY = hoverScaleY;
        this.fillAlpha = fillAlpha;
        BasicStroke dashed = new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[]{6.0f, 6.0f}, 0.0f);
        domainCrosshair = new Crosshair(1.5d, new Color(0, 0, 0, 128), dashed);
        domainCrosshair.setVisible(true);
        rangeCrosshair = new Crosshair(1.5d, new Color(0, 0, 0, 128), dashed);
        rangeCrosshair.setVisible(true);
        crosshairOverlay = new CrosshairOverlay();
        crosshairOverlay.addDomainCrosshair(domainCrosshair);
        crosshairOverlay.addRangeCrosshair(rangeCrosshair);
        setLayerPosition(0);
    }

    public void clear() {
        XYSelection oldHover = mouseHoverSelection;
        mouseHoverSelection = null;
        firePropertyChange(PROP_HOVER_SELECTION, oldHover, mouseHoverSelection);
        mouseClickSelection.clear();
        firePropertyChange(PROP_SELECTION, null, mouseClickSelection);
        fireOverlayChanged();
    }

    public XYSelection getMouseHoverSelection() {
        return mouseHoverSelection;
    }

    public Set<XYSelection> getMouseClickSelection() {
        synchronized (this.mouseClickSelection) {
            return Collections.unmodifiableSet(new LinkedHashSet<XYSelection>(this.mouseClickSelection));
        }
    }

    public Color getSelectionFillColor() {
        return selectionFillColor;
    }

    public void setSelectionFillColor(Color selectionFillColor) {
        Color old = this.selectionFillColor;
        this.selectionFillColor = selectionFillColor;
        firePropertyChange(PROP_SELECTION_FILL_COLOR, old, this.selectionFillColor);
        fireOverlayChanged();
    }

    public Color getHoverFillColor() {
        return hoverFillColor;
    }

    public void setHoverFillColor(Color hoverFillColor) {
        Color old = this.hoverFillColor;
        this.hoverFillColor = hoverFillColor;
        firePropertyChange(PROP_HOVER_FILL_COLOR, old, this.hoverFillColor);
        fireOverlayChanged();
    }

    public float getHoverScaleX() {
        return hoverScaleX;
    }

    public void setHoverScaleX(float hoverScaleX) {
        float old = this.hoverScaleX;
        this.hoverScaleX = hoverScaleX;
        firePropertyChange(PROP_HOVER_SCALE_X, old, this.hoverScaleX);
        fireOverlayChanged();
    }

    public float getHoverScaleY() {
        return hoverScaleY;
    }

    public void setHoverScaleY(float hoverScaleY) {
        float old = this.hoverScaleY;
        this.hoverScaleY = hoverScaleY;
        firePropertyChange(PROP_HOVER_SCALE_Y, old, this.hoverScaleY);
        fireOverlayChanged();
    }

    public float getFillAlpha() {
        return fillAlpha;
    }

    public void setFillAlpha(float fillAlpha) {
        float old = this.fillAlpha;
        this.fillAlpha = fillAlpha;
        firePropertyChange(PROP_FILL_ALPHA, old, this.fillAlpha);
        fireOverlayChanged();
    }

    @Override
    public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
        if (isVisible()) {
            for (XYSelection selection : mouseClickSelection) {
                if (selection.isVisible()) {
                    Shape selectedEntity = chartPanel.getChart().getXYPlot().getRenderer().getItemShape(selection.getSeriesIndex(), selection.getItemIndex());
                    if (selectedEntity == null) {
                        selectedEntity = generate(selection.getDataset(), selection.getSeriesIndex(), selection.getItemIndex());
                    }
                    updateCrosshairs(selection.getDataset(), selection.getSeriesIndex(), selection.getItemIndex());
                    Shape transformed = toView(selectedEntity, chartPanel, selection.getDataset(), selection.getSeriesIndex(), selection.getItemIndex());
                    drawEntity(transformed, g2, selectionFillColor, chartPanel, false);
                }
            }
            if (this.mouseHoverSelection != null && this.mouseHoverSelection.isVisible()) {
                Shape entity = chartPanel.getChart().getXYPlot().getRenderer().getItemShape(mouseHoverSelection.getSeriesIndex(), mouseHoverSelection.getItemIndex());
                if (entity == null) {
                    entity = generate(mouseHoverSelection.getDataset(), mouseHoverSelection.getSeriesIndex(), mouseHoverSelection.getItemIndex());
                }
                Shape transformed = toView(entity, chartPanel, mouseHoverSelection.getDataset(), mouseHoverSelection.getSeriesIndex(), mouseHoverSelection.getItemIndex());
                drawEntity(transformed, g2, hoverFillColor, chartPanel, true);
            }
        }
        crosshairOverlay.paintOverlay(g2, chartPanel);
    }

    private void updateCrosshairs(XYDataset ds, int seriesIndex, int itemIndex) {
        double x = ds.getXValue(seriesIndex, itemIndex);
        double y = ds.getYValue(seriesIndex, itemIndex);
        domainCrosshair.setValue(x);
        rangeCrosshair.setValue(y);
    }

    private Shape generate(XYDataset ds, int seriesIndex, int itemIndex) {
        double width = 10.0d;
        double height = 10.0d;
        double x = ds.getXValue(seriesIndex, itemIndex) - (width / 2.0d);
        double y = ds.getYValue(seriesIndex, itemIndex);
        Ellipse2D.Double e = new Ellipse2D.Double(x, y, width, height);
        return e;
    }

    private void drawEntity(Shape entity, Graphics2D g2, Color fill, ChartPanel chartPanel, boolean scale) {
        if (entity != null) {
            Shape savedClip = g2.getClip();
            Rectangle2D dataArea = chartPanel.getScreenDataArea();
            Color c = g2.getColor();
            Composite comp = g2.getComposite();
            g2.clip(dataArea);
            g2.setColor(fill);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform originalTransform = g2.getTransform();
            Shape transformed = entity;
            if (scale) {
                transformed = scaleAtOrigin(entity, hoverScaleX, hoverScaleY).createTransformedShape(entity);
            }
            transformed = AffineTransform.getTranslateInstance(entity.getBounds2D().getCenterX() - transformed.getBounds2D().getCenterX(), entity.getBounds2D().getCenterY() - transformed.getBounds2D().getCenterY()).createTransformedShape(transformed);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fillAlpha));
            g2.fill(transformed);
            g2.setColor(Color.DARK_GRAY);
            g2.draw(transformed);
            g2.setComposite(comp);
            g2.setColor(c);
            g2.setClip(savedClip);
        }
    }

    @Override
    public void selectionStateChanged(SelectionChangeEvent ce) {
        XYSelection selection = ce.getSelection();

        if (selection == null) {
            if (mouseHoverSelection != null) {
                mouseHoverSelection.removePropertyChangeListener(XYSelection.PROP_VISIBLE, this);
                firePropertyChange(PROP_HOVER_SELECTION, mouseHoverSelection, null);
            }
            mouseHoverSelection = null;
        } else {
            if (ce.getSelection().getType() == XYSelection.Type.CLICK) {
                if (mouseClickSelection.contains(selection)) {
                    mouseClickSelection.remove(selection);
                    selection.removePropertyChangeListener(XYSelection.PROP_VISIBLE, this);
                } else {
                    mouseClickSelection.add(selection);
                    selection.addPropertyChangeListener(XYSelection.PROP_VISIBLE, WeakListeners.propertyChange(this, selection));
                }
                firePropertyChange(PROP_SELECTION, null, mouseClickSelection);
            } else if (ce.getSelection().getType() == XYSelection.Type.HOVER) {
                if (mouseHoverSelection != null) {
                    mouseHoverSelection.removePropertyChangeListener(XYSelection.PROP_VISIBLE, this);
                }
                mouseHoverSelection = selection;
                mouseHoverSelection.addPropertyChangeListener(XYSelection.PROP_VISIBLE, WeakListeners.propertyChange(this, mouseHoverSelection));
                firePropertyChange(PROP_HOVER_SELECTION, null, mouseHoverSelection);
            }
        }
        fireOverlayChanged();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        fireOverlayChanged();
    }
}
