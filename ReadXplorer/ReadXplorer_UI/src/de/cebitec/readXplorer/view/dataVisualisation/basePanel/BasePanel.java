/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.dataVisualisation.basePanel;

import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.MousePositionListener;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

/**
 * A BasePanel serves as basis for other visual components.
 *
 * @author ddoppmei, rhilker
 */
public class BasePanel extends JPanel implements MousePositionListener {

    private static final long serialVersionUID = 246153482;

    private AbstractViewer viewer;
    private AbstractInfoPanel rightPanel;
    private AbstractInfoPanel leftPanel;
    private BoundsInfoManager boundsManager;
    private MousePositionListener viewController;
    private List<MousePositionListener> currentMousePosListeners;
    private JPanel centerPanel;
    private AdjustmentPanel adjustmentPanelHorizontal;
    private Component topPanel;
    private JScrollPane centerScrollpane;

    public BasePanel(BoundsInfoManager boundsManager, MousePositionListener viewController){
        super();
        this.setLayout(new BorderLayout());
        this.centerPanel = new JPanel(new BorderLayout());
        this.add(centerPanel, BorderLayout.CENTER);
        this.boundsManager = boundsManager;
        this.viewController = viewController;
        this.currentMousePosListeners = new ArrayList<>();
    }

    public void close(){
        this.shutdownViewer();
        this.shutdownInfoPanelAndAdjustmentPanel();
        this.remove(centerPanel);
        this.centerPanel = null;
        this.viewController = null;
        this.updateUI();
    }

    private void shutdownViewer(){
        if(this.viewer != null){
            this.boundsManager.removeBoundListener(viewer);
            this.currentMousePosListeners.remove(viewer);
            this.centerPanel.remove(viewer);
            this.viewer.close();
            this.viewer = null;
        }
    }

    private void shutdownInfoPanelAndAdjustmentPanel(){
        if(adjustmentPanelHorizontal != null){
            centerPanel.remove(adjustmentPanelHorizontal);
            adjustmentPanelHorizontal = null;
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
        this.boundsManager.addBoundsListener(viewer);
        currentMousePosListeners.add(viewer);
        if (viewer instanceof TrackViewer) {
            TrackViewer tv = (TrackViewer) viewer;
            tv.setVerticalZoomSlider(verticalZoom);
        }
        centerPanel.add(viewer, BorderLayout.CENTER);
        centerPanel.add(verticalZoom, BorderLayout.WEST);

        this.updateSize();
    }

    public void setViewer(AbstractViewer viewer){
        this.viewer = viewer;
        this.boundsManager.addBoundsListener(viewer);
        currentMousePosListeners.add(viewer);
        centerPanel.add(viewer, BorderLayout.CENTER);
        
        this.addPlaceholder();
        this.updateSize();
    }

    public void setHorizontalAdjustmentPanel(AdjustmentPanel adjustmentPanel){
        this.adjustmentPanelHorizontal = adjustmentPanel;
        centerPanel.add(adjustmentPanel, BorderLayout.NORTH);
        this.updateSize();
    }
    
    /**
     * Adds a viewer in a scrollpane allowing for vertical scrolling.
     * Horizontal scrolling is only available by "setHorizontalAdjustmentPanel".
     * @param viewer viewer to set
     */
    public void setViewerInScrollpane(AbstractViewer viewer){
        this.viewer = viewer;
        this.boundsManager.addBoundsListener(viewer);
      
        this.currentMousePosListeners.add(viewer);
        this.centerScrollpane = new JScrollPane(this.viewer);
        this.centerScrollpane.setPreferredSize(new Dimension(490, 200));
        this.centerScrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        this.centerPanel.add(this.centerScrollpane, BorderLayout.CENTER);
        this.centerScrollpane.setVisible(true);
        this.viewer.setVisible(true);
        this.viewer.setScrollBar(this.centerScrollpane.getVerticalScrollBar());
        
        this.addPlaceholder();
        this.updateSize();
        
    }
    
    /**
     * Adds a viewer in a scrollpane allowing for vertical scrolling and vertical zooming.
     * Horizontal scrolling is only available by "setHorizontalAdjustmentPanel".
     * @param viewer viewer to set
     * @param verticalZoom vertical zoom slider
     */
    public void setViewerInScrollpane(AbstractViewer viewer, JSlider verticalZoom){
        this.viewer = viewer;
        verticalZoom.setOrientation(JSlider.VERTICAL);
        this.boundsManager.addBoundsListener(viewer);
        this.currentMousePosListeners.add(viewer);
        this.centerScrollpane = new JScrollPane(this.viewer);
        this.centerScrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.centerPanel.add(this.centerScrollpane, BorderLayout.CENTER);
        this.centerPanel.add(verticalZoom, BorderLayout.WEST);
        this.viewer.setScrollBar(this.centerScrollpane.getVerticalScrollBar());
        
        this.addPlaceholder();
        this.updateSize();
    }
    
    /**
     * Adds a placeholder in case this viewer is a ReferenceViewer
     */
    private void addPlaceholder() {
        if (viewer instanceof ReferenceViewer) {
            JPanel p = new JPanel();
            p.add(new JLabel(" "));
            p.setLayout(new FlowLayout(FlowLayout.LEFT));
            centerPanel.add(p, BorderLayout.WEST);
        }
    }

    public void setTopInfoPanel(MousePositionListener infoPanel){
        this.topPanel = (Component) infoPanel;
        centerPanel.add(topPanel, BorderLayout.NORTH);
        currentMousePosListeners.add(infoPanel);
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
