package de.cebitec.vamp.plotting;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import org.jfree.data.xy.XYDataItem;

/**
 *
 * @author kstaderm
 */
public class PlotDataItem extends XYDataItem{

    private PersistantFeature feature;
    
    public PlotDataItem(PersistantFeature feature, Number x, Number y) {
        super(x, y);
        this.feature = feature;
    }

    public PlotDataItem(PersistantFeature feature, double x, double y) {
        super(x, y);
        this.feature = feature;
    }

    public PersistantFeature getFeature() {
        return feature;
    }
}
