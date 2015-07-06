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
import java.awt.Paint;
import org.jfree.data.xy.XYDataItem;


/**
 *
 * @author kstaderm
 */
public class PlotDataItem extends XYDataItem {

    private final PersistentFeature feature;
    private Paint paint;


    public PlotDataItem( PersistentFeature feature, Number x, Number y ) {
        super( x, y );
        this.feature = feature;
    }


    public PlotDataItem( PersistentFeature feature, double x, double y ) {
        super( x, y );
        this.feature = feature;
    }


    public PersistentFeature getFeature() {
        return feature;
    }


    public Paint getPaint() {
        return paint;
    }


    public void setPaint( Paint paint ) {
        this.paint = paint;
    }


}
