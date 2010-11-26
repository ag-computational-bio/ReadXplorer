package de.cebitec.vamp.view;

import de.cebitec.vamp.RunningTaskI;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackItem;

/**
 *
 * @author ddoppmeier
 */
public interface ApplicationFrameI {

    public void setViewController(ViewController viewController);

    public void releaseButtons();

    public void setVisible(boolean b);

    public void blockControlsByRunningTask(RunningTaskI runninTask);
    
    public void showRefGenPanel(BasePanel refGenPanel);

    public void removeRefGenPanel(BasePanel genomeViewer);

    public void showTrackPanel(BasePanel trackPanel, TrackItem trackMenuItem);

    public void closeTrackPanel(BasePanel trackPanel, TrackItem trackMenuItem);

}
