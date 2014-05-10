package de.cebitec.readXplorer.view.dataVisualisation.basePanel;

import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbBundle;

/**
 * A provider for panels containing generally available options.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class LegendAndOptionsProvider {
    
    /**
     * A provider for panels containing generally available options.
     */
    private LegendAndOptionsProvider() {
    }
    
    /**
     * Creates a mapping quality (phred scale) filter.
     * @param viewer The viewer for to which the filter shall be added
     * @param parentPanel The panel in which the filter shall be embedded
     */
    @NbBundle.Messages("MinMappingQualityText=Filter by min. mapping quality")
    public static void createMappingQualityFilter(final AbstractViewer viewer, JPanel parentPanel) {
        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
        optionPanel.setBackground(ColorProperties.LEGEND_BACKGROUND);
        final JTextField minMappingQualityField = new JTextField("0");
        minMappingQualityField.setMinimumSize(new Dimension(50, 20));
        minMappingQualityField.setPreferredSize(new Dimension(50, 20));
        minMappingQualityField.setMaximumSize(new Dimension(50, 20));
        final JButton minMappingQualityButton = new JButton(Bundle.MinMappingQualityText());

        //automatic centering around the sequence bar enabled event
        minMappingQualityButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateMinMappingQuality(viewer, minMappingQualityField);
            }
        });
        optionPanel.add(minMappingQualityField);
        optionPanel.add(createPlaceholder());
        optionPanel.add(minMappingQualityButton);
        parentPanel.add(optionPanel, BorderLayout.WEST);
    }
    
    /**
     * Updates the minimum mapping quality value of the given viewer if the 
     * input is valid. Otherwise an error message is displayed.
     * @param viewer The viewer to update
     * @param minMappingQualityField The minimum mapping quality to set
     */
    private static void updateMinMappingQuality(AbstractViewer viewer, JTextField minMappingQualityField) {
        if (GeneralUtils.isValidByteInput(minMappingQualityField.getText())) {
            byte minMappingQual = Byte.parseByte(minMappingQualityField.getText().trim());
            viewer.setMinMappingQuality(minMappingQual);
            viewer.setNewDataRequestNeeded(true);
            viewer.boundsChangedHook();
        } else if (!minMappingQualityField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(minMappingQualityField, "Please enter a valid mapping quality value! (1-127)", "Invalid Position", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * @return A placeholder with width 3 and height 20.
     */
    public static JPanel createPlaceholder() {
        JPanel placeholder = new JPanel();
        placeholder.setBackground(ColorProperties.LEGEND_BACKGROUND);
        placeholder.setMinimumSize(new Dimension(3, 20));
        placeholder.setPreferredSize(new Dimension(3, 20));
        return placeholder;
    }
}
