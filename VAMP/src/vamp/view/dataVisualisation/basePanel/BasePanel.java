package vamp.view.dataVisualisation.basePanel;

import vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import vamp.view.dataVisualisation.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JSlider;
import vamp.view.dataVisualisation.BoundsInfoManager;
import vamp.view.ViewController;

/**
 *
 * @author ddoppmei
 */
public class BasePanel extends JPanel implements MousePositionListener {

    private static final long serialVersionUID = 246153482;

    private AbstractViewer viewer;
    private AbstractInfoPanel rightPanel;
    private AbstractInfoPanel leftPanel;
    private BoundsInfoManager boundsManager;
    private ViewController viewController;
    private List<MousePositionListener> currentMousePosListeners;
    private JPanel centerPanel;
    private AdjustmentPanel adjustmentPanel;

    public BasePanel(BoundsInfoManager boundsManager, ViewController viewController){
        super();
        this.setLayout(new BorderLayout());
        centerPanel = new JPanel(new BorderLayout());
        this.add(centerPanel, BorderLayout.CENTER);
        this.boundsManager = boundsManager;
        this.viewController = viewController;
        currentMousePosListeners = new ArrayList<MousePositionListener>();
    }

    public void close(){
        this.shutdownViewer();
        this.shutdownInfoPanelAndAdjustmentPanel();
        this.remove(centerPanel);
        centerPanel = null;
        viewController = null;
        this.updateUI();
    }

    private void shutdownViewer(){
        if(viewer != null){
            boundsManager.removeBoundListener(viewer);
            currentMousePosListeners.remove(viewer);
            centerPanel.remove(viewer);
            viewer.close();
            viewer = null;
        }
    }

    private void shutdownInfoPanelAndAdjustmentPanel(){
        if(adjustmentPanel != null){
            centerPanel.remove(adjustmentPanel);
            adjustmentPanel = null;
        }

        if(rightPanel != null){
            rightPanel.close();
            this.remove(rightPanel);
            currentMousePosListeners.remove(rightPanel);
            rightPanel = null;

        }

        if(leftPanel != null){
            leftPanel.close();
            this.remove(leftPanel);
            currentMousePosListeners.remove(leftPanel);
            leftPanel = null;
        }
    }

    public void setViewer(AbstractViewer viewer, JSlider verticalZoom){
        this.viewer = viewer;
        verticalZoom.setOrientation(JSlider.VERTICAL);
        boundsManager.addBoundsListener(viewer);
        currentMousePosListeners.add(viewer);
        centerPanel.add(viewer, BorderLayout.CENTER);
        centerPanel.add(verticalZoom, BorderLayout.WEST);
        this.updateSize();
    }

    public void setViewer(AbstractViewer viewer){
        this.viewer = viewer;
        boundsManager.addBoundsListener(viewer);
        currentMousePosListeners.add(viewer);
        centerPanel.add(viewer, BorderLayout.CENTER);
        this.updateSize();
    }

    public void setAdjustmentPanel(AdjustmentPanel adjustmentPanel){
        this.adjustmentPanel = adjustmentPanel;
        centerPanel.add(adjustmentPanel, BorderLayout.NORTH);
        this.updateSize();
    }

    public void setRightInfoPanel(AbstractInfoPanel infoPanel){
        this.rightPanel = infoPanel;
        this.add(infoPanel, BorderLayout.EAST);
        currentMousePosListeners.add(infoPanel);
        this.updateSize();
    }

    public void setLeftInfoPanel(AbstractInfoPanel infoPanel){
        this.leftPanel = infoPanel;
        this.add(leftPanel, BorderLayout.WEST);
        this.updateSize();
    }

    public void setTitlePanel(JPanel title){
        this.add(title, BorderLayout.NORTH);
        this.updateSize();
    }

    public void reportCurrentMousePos(int currentLogMousePos) {
        viewController.setCurrentMousePosition(currentLogMousePos);
    }

    @Override
    public void setCurrentMousePosition(int logPos) {
        for(MousePositionListener c : currentMousePosListeners){
            c.setCurrentMousePosition(logPos);
        }
    }

    public void reportMouseOverPaintingStatus(boolean b) {
        viewController.setMouseOverPaintingRequested(b);
    }

    @Override
    public void setMouseOverPaintingRequested(boolean requested) {
        for(MousePositionListener c : currentMousePosListeners){
            c.setMouseOverPaintingRequested(requested);
        }
    }

    public AbstractViewer getViewer(){
        return viewer;
    }

    private void updateSize(){
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height));
    }

}
