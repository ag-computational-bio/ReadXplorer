package de.cebitec.vamp.differentialExpression.plot;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;

/**
 *
 * @author kstaderm
 */
public class MouseActions implements ChartMouseListener {

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
        ChartEntity ent = cme.getEntity();
        if (cme.getEntity() instanceof XYItemEntity) {
            XYItemEntity itemEntity = ((XYItemEntity) cme.getEntity());
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
    }
}
