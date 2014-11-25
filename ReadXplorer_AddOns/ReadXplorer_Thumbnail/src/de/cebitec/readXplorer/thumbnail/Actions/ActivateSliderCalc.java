package de.cebitec.readXplorer.thumbnail.Actions;

import de.cebitec.readXplorer.thumbnail.ThumbnailController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * This ActionClass presents a CheckBoxMenuItem to enable or disable the 
 * automatic calibration of the CoverageZoomSlider on a TrackPanel.
 * For better perfomance this option can be disabled.
 * @author denis
 */
public final class ActivateSliderCalc implements ActionListener, Presenter.Menu {

    private String ITEM_TEXT = "Auto Slider Calculation";

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(ITEM_TEXT, true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThumbnailController thumb = Lookup.getDefault().lookup(ThumbnailController.class);
                if (thumb != null) {
                    thumb.setAutoSlider(item.isSelected());
                }
            }
        });
        return item;
    }
}
