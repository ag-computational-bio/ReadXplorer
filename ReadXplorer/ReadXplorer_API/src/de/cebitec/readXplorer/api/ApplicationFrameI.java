package de.cebitec.readXplorer.api;

import javax.swing.JPanel;

/**
 *
 * @author ddoppmeier
 */
public interface ApplicationFrameI {
    
    public void showRefGenPanel(JPanel refGenPanel);

    public void removeRefGenPanel(JPanel genomeViewer);

    public void showTrackPanel(JPanel trackPanel);

    public void closeTrackPanel(JPanel trackPanel);

}
