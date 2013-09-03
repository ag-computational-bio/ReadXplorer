package de.cebitec.vamp.plotting;

import java.awt.Graphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.panel.Overlay;

/**
 *
 * @author kstaderm
 */
public class ChartPanelOverlay implements Overlay {

    private MouseActions mouseActions;

    public ChartPanelOverlay(MouseActions mouseActions) {
        this.mouseActions = mouseActions;
    }

    @Override
    public void paintOverlay(Graphics2D gd, ChartPanel pnl) {
        PlotDataItem selectedItem = mouseActions.getSelectedItem();
        if (selectedItem != null) {
        
        }
    }

    @Override
    public void addChangeListener(OverlayChangeListener ol) {
    }

    @Override
    public void removeChangeListener(OverlayChangeListener ol) {
    }
}
