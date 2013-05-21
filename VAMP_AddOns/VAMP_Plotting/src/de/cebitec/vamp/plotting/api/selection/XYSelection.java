/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.plotting.api.selection;

import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Nils Hoffmann
 */
public class XYSelection implements ISelection {

    public enum Type {

        CLEAR, KEYBOARD, HOVER, CLICK
    };
    public static final String PROP_DATASET = "dataset";
    private XYDataset dataset;
    private final int seriesIndex;
    private final int itemIndex;
    private final Type type;
    private final Object target, source;
    private final Shape selectionShape;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean visible = true;
    private String name;
    private String displayName;
    private String shortDescription;
    private static XYSelection clearSelection = null;

    public static XYSelection clearSelection() {
        if (clearSelection == null) {
            clearSelection = new XYSelection(null, -1, -1, Type.CLEAR, null, null, null);
            clearSelection.setName("CLEAR");
            clearSelection.setDisplayName("CLEAR");
            clearSelection.setShortDescription("THE EMPTY SELECTION SIGNALING A CLEAR");
        }
        return clearSelection;
    }

    public XYSelection(XYDataset dataset, int seriesIndex, int itemIndex, Type type, Object source, Object target, Shape selectionShape) {
        this.dataset = dataset;
        this.seriesIndex = seriesIndex;
        this.itemIndex = itemIndex;
        this.type = type;
        this.source = source;
        this.target = target;
        this.selectionShape = selectionShape;
        if (target != null) {
            this.name = target.toString();
            this.displayName = target.toString();
            if (source != null) {
                this.shortDescription = target.toString() + " with source: " + source.toString();
            }
        }
    }

    public XYDataset getDataset() {
        return dataset;
    }

    public void setDataset(XYDataset dataset) {
        XYDataset old = dataset;
        this.dataset = dataset;
        pcs.firePropertyChange(PROP_DATASET, old, this.dataset);
    }

    public int getSeriesIndex() {
        return seriesIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public Shape getSelectionShape() {
        return selectionShape;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean b) {
        boolean old = this.visible;
        this.visible = b;
        pcs.firePropertyChange(PROP_VISIBLE, old, this.visible);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(property, listener);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.dataset != null ? this.dataset.getSeriesKey(this.seriesIndex).hashCode() : 0);
        hash = 79 * hash + this.seriesIndex;
        hash = 79 * hash + this.itemIndex;
        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 79 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 79 * hash + (this.source != null ? this.source.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XYSelection other = (XYSelection) obj;
        if (this.dataset.getSeriesKey(this.seriesIndex) != other.getDataset().getSeriesKey(this.seriesIndex) && (this.dataset.getSeriesKey(this.seriesIndex) == null || !this.dataset.getSeriesKey(this.seriesIndex).equals(other.getDataset().getSeriesKey(this.seriesIndex)))) {
            return false;
        }
        if (this.seriesIndex != other.seriesIndex) {
            return false;
        }
        if (this.itemIndex != other.itemIndex) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "XYSelection{" + "dataset=" + dataset + ", seriesIndex=" + seriesIndex + ", itemIndex=" + itemIndex + ", type=" + type + ", target=" + target + ", source=" + source + ", selectionShape=" + selectionShape + '}';
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        pcs.firePropertyChange(PROP_NAME, old, this.name);
    }

    @Override
    public void setDisplayName(String displayName) {
        String old = this.displayName;
        this.displayName = displayName;
        pcs.firePropertyChange(PROP_DISPLAY_NAME, old, this.displayName);
    }

    @Override
    public void setShortDescription(String shortDescription) {
        String old = this.shortDescription;
        this.shortDescription = shortDescription;
        pcs.firePropertyChange(PROP_SHORT_DESCRIPTION, old, this.shortDescription);
    }
}
