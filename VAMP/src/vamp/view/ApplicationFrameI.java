package vamp.view;

import vamp.RunningTaskI;
import vamp.view.dataVisualisation.basePanel.BasePanel;
import vamp.view.dataVisualisation.trackViewer.TrackItem;

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
