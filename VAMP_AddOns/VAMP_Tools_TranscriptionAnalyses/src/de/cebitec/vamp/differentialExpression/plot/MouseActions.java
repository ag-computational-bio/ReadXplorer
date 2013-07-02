package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.plotting.PlotDataItem;
import de.cebitec.vamp.ui.visualisation.reference.ReferenceFeatureTopComp;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
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
    
    public MouseActions() {
        refComp = ReferenceFeatureTopComp.findInstance();
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
                if (bm != null) {
                    bm.navigatorBarUpdated(pos);
                }
            }
            refComp.showFeatureDetails(feature);
        }
    }
}
