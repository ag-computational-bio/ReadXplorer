package de.cebitec.vamp.view.dataVisualisation;

import javax.swing.JPanel;

/**
 * @author -Rolf Hilker-
 * 
 * Interface for classes showing a transcription analyses panel. 
 */
public interface TranscriptionAnalysesFrameI {

    /**
     * Adds the transcriptionAnalysesTopPanel to the view.
     * @param transcriptionAnalysesTopPanel 
     */
    public void showTranscriptionAnalysesTopPanel(JPanel transcriptionAnalysesTopPanel);

    /**
     * Handles the closing of the transcriptionAnalysesTopPanel
     * @param transcriptionAnalysesPanel 
     */
    public void closeTranscriptionAnalysesTopPanel(JPanel transcriptionAnalysesPanel);

    /**
     * @return true, if this component already contains a TranscriptionAnalysesTopPanel,
     * false otherwise.
     */
    public boolean hasTranscriptionAnalysesTopPanel();

    /**
     * @return the TranscriptionAnalysesTopPanel of this component, if there is one.
     * If not it returns null.
     */
    public JPanel getTranscriptionAnalysesTopPanel();
}
