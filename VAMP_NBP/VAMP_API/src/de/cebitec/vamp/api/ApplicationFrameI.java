package de.cebitec.vamp.api;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author ddoppmeier
 */
public interface ApplicationFrameI {
    
    public void showRefGenPanel(JPanel refGenPanel);

    public void removeRefGenPanel(JPanel genomeViewer);

    public void showTrackPanel(JPanel trackPanel, JMenuItem trackMenuItem);

    public void closeTrackPanel(JPanel trackPanel);

}
