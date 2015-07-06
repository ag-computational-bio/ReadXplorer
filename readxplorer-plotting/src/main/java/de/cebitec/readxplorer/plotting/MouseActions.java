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


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import java.awt.Point;
import java.util.Collection;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYSeriesCollection;


/**
 *
 * @author kstaderm
 */
public class MouseActions implements ChartMouseListener {

    private ReferenceFeatureTopComp refComp;
    private PlotDataItem selectedItem;
    private Point selectedPoint;


    public MouseActions() {
        refComp = ReferenceFeatureTopComp.findInstance();
    }


    MouseActions( ChartPanelOverlay overlay ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void chartMouseClicked( ChartMouseEvent cme ) {
        if( cme.getEntity() instanceof XYItemEntity ) {
            XYItemEntity xyitem = (XYItemEntity) cme.getEntity(); // get clicked entity
            XYSeriesCollection dataset = (XYSeriesCollection) xyitem.getDataset(); // get data set
            int itemIndex = xyitem.getItem();
            int seriesIndex = xyitem.getSeriesIndex();
            PlotDataItem clickedItem = (PlotDataItem) dataset.getSeries( seriesIndex ).getDataItem( itemIndex );
            showPosition( clickedItem.getFeature() );
            selectedItem = clickedItem;
            selectedPoint = cme.getTrigger().getPoint();
        }
    }


    @Override
    public void chartMouseMoved( ChartMouseEvent cme ) {
    }


    private void showPosition( PersistentFeature feature ) {
        if( feature != null ) {
            int pos = feature.getStart();
            Collection<ViewController> viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll( ViewController.class );
            for( ViewController tmpVCon : viewControllers ) {
                BoundsInfoManager bm = tmpVCon.getBoundsManager();
                if( bm != null && tmpVCon.getCurrentRefGen().getChromosome( feature.getChromId() ) != null ) {
                    bm.chromosomeChanged( feature.getChromId() );
                    bm.navigatorBarUpdated( pos );
                }
            }
            refComp.showFeatureDetails( feature );
        }
    }


    public PlotDataItem getSelectedItem() {
        return selectedItem;
    }


    public Point getSelectedPoint() {
        return selectedPoint;
    }


}
