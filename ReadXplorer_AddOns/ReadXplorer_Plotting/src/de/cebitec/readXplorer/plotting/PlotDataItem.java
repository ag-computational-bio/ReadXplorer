/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.plotting;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import java.awt.Paint;
import org.jfree.data.xy.XYDataItem;

/**
 *
 * @author kstaderm
 */
public class PlotDataItem extends XYDataItem{

    private PersistantFeature feature;
    private Paint paint;
    
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

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
}
