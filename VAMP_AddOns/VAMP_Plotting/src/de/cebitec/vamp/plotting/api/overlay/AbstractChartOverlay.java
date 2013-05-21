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

import de.cebitec.vamp.plotting.api.selection.ISelectionChangeListener;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author Nils Hoffmann
 */
public abstract class AbstractChartOverlay extends AbstractOverlay implements ChartOverlay, AxisChangeListener, ISelectionChangeListener {

	private boolean visible = true;
	private boolean visibilityChangeable = true;
	private int layerPosition = Integer.MAX_VALUE;
	private String displayName = "Chart Overlay";
	private String name = AbstractChartOverlay.class.getSimpleName();
	private String shortDescription = "Displays the currently active chart dataset entity selection.";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public AbstractChartOverlay(String name, String displayName, String shortDescription, boolean visibilityChangeable) {
		this.name = name;
		this.displayName = displayName;
		this.shortDescription = shortDescription;
		this.visibilityChangeable = visibilityChangeable;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getShortDescription() {
		return shortDescription;
	}

	public void setDisplayName(String displayName) {
		String old = this.displayName;
		this.displayName = displayName;
		firePropertyChange(PROP_DISPLAY_NAME, old, this.displayName);
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROP_NAME, old, this.name);
	}

	public void setShortDescription(String shortDescription) {
		String old = this.shortDescription;
		this.shortDescription = shortDescription;
		firePropertyChange(PROP_SHORT_DESCRIPTION, old, this.shortDescription);
	}

	@Override
	public void setVisible(boolean b) {
		boolean old = this.visible;
		this.visible = b;
		firePropertyChange(PROP_VISIBLE, old, this.visible);
		fireOverlayChanged();
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public boolean isVisibilityChangeable() {
		return this.visibilityChangeable;
	}

	@Override
	public int getLayerPosition() {
		return layerPosition;
	}

	@Override
	public void setLayerPosition(int pos) {
		int old = this.layerPosition;
		this.layerPosition = pos;
		firePropertyChange(PROP_LAYER_POSITION, old, this.layerPosition);
		fireOverlayChanged();
	}

	@Override
	public void axisChanged(AxisChangeEvent ace) {
		fireOverlayChanged();
	}

	public static Point2D toView(Point2D entity, ChartPanel chartPanel) {
		AffineTransform toPosition = getModelToViewTransform(chartPanel, entity.getX(), entity.getY());
		toPosition.concatenate(AffineTransform.getTranslateInstance(-entity.getX(), -entity.getY()));
		return toPosition.transform(entity,null);
	}
	
	public static Shape toView(Shape entity, ChartPanel chartPanel) {
		return toView(entity, chartPanel, new Point2D.Double(entity.getBounds2D().getCenterX(), entity.getBounds2D().getCenterY()));
	}

	public static Shape toView(Shape entity, ChartPanel chartPanel, Point2D center) {
		AffineTransform toPosition = getModelToViewTransform(chartPanel, center.getX(), center.getY());
		toPosition.concatenate(AffineTransform.getTranslateInstance(-center.getX(), -center.getY()));
		return toPosition.createTransformedShape(entity);
	}

	public static Shape toView(Shape entity, ChartPanel chartPanel, XYDataset dataset, int seriesIndex, int itemIndex) {
		double x1 = dataset.getXValue(seriesIndex, itemIndex);
		double y1 = dataset.getYValue(seriesIndex, itemIndex);
		AffineTransform toPosition = getModelToViewTransform(chartPanel, x1, y1);
		toPosition.concatenate(AffineTransform.getTranslateInstance(-entity.getBounds2D().getCenterX(), -entity.getBounds2D().getCenterY()));
		return toPosition.createTransformedShape(entity);
	}

	public static AffineTransform getModelToViewTransform(ChartPanel chartPanel, double x1, double y1) {
		double zoomX = chartPanel.getScaleX();
		double zoomY = chartPanel.getScaleY();
		Insets insets = chartPanel.getInsets();
		AffineTransform at = AffineTransform.getTranslateInstance(insets.left,
				insets.top);
		at.concatenate(AffineTransform.getScaleInstance(zoomX, zoomY));
		RectangleEdge xAxisLocation = chartPanel.getChart().getXYPlot().getDomainAxisEdge();
		RectangleEdge yAxisLocation = chartPanel.getChart().getXYPlot().getRangeAxisEdge();
		PlotOrientation orientation = chartPanel.getChart().getXYPlot().getOrientation();
		Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
		double transX = chartPanel.getChart().getXYPlot().getDomainAxis().valueToJava2D(x1, dataArea, xAxisLocation);
		double transY = chartPanel.getChart().getXYPlot().getRangeAxis().valueToJava2D(y1, dataArea, yAxisLocation);
		if (orientation == PlotOrientation.HORIZONTAL) {
			double tmp = transX;
			transX = transY;
			transY = tmp;
		}
		at.concatenate(AffineTransform.getTranslateInstance(transX,
				transY));
		return at;
	}

	public static AffineTransform scaleAtOrigin(Shape s, double baseX, double baseY, float scalex, float scaley) {
		Point2D center = new Point2D.Double(baseX, baseY);
		//Affine transforms are applied from right to left
		AffineTransform at = AffineTransform.getTranslateInstance(center.getX(), center.getY());
		at.concatenate(AffineTransform.getScaleInstance(scalex, scaley));
		return at;
	}

	public static AffineTransform scaleAtOrigin(Shape s, float scalex, float scaley) {
		return scaleAtOrigin(s, s.getBounds2D().getCenterX(), s.getBounds2D().getCenterY(), scalex, scaley);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(property, listener);
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}
}
