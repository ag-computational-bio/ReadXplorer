package de.cebitec.readXplorer.plotting;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.controller.ViewController;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
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

    MouseActions(ChartPanelOverlay overlay) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
        if (cme.getEntity() instanceof XYItemEntity) {
            XYItemEntity xyitem = (XYItemEntity) cme.getEntity(); // get clicked entity
            XYSeriesCollection dataset = (XYSeriesCollection) xyitem.getDataset(); // get data set
            int itemIndex = xyitem.getItem();
            int seriesIndex = xyitem.getSeriesIndex();
            PlotDataItem clickedItem = (PlotDataItem) dataset.getSeries(seriesIndex).getDataItem(itemIndex);
            showPosition(clickedItem.getFeature());
            selectedItem = clickedItem;
            selectedPoint = cme.getTrigger().getPoint();
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
    }

    private void showPosition(PersistantFeature feature) {
        if (feature != null) {
            int pos = feature.getStart();
            Collection<ViewController> viewControllers;
            viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll(ViewController.class);
            for (Iterator<ViewController> it = viewControllers.iterator(); it.hasNext();) {
                ViewController tmpVCon = it.next();
                BoundsInfoManager bm = tmpVCon.getBoundsManager();
                if (bm != null && tmpVCon.getCurrentRefGen().getChromosome(feature.getChromId()) != null) {
                    bm.chromosomeChanged(feature.getChromId());
                    bm.navigatorBarUpdated(pos);
                }
            }
            refComp.showFeatureDetails(feature);
        }
    }

    public PlotDataItem getSelectedItem() {
        return selectedItem;
    }

    public Point getSelectedPoint() {
        return selectedPoint;
    }
}
