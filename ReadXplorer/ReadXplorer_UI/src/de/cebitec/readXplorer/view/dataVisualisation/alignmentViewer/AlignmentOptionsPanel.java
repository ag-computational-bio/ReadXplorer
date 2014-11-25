package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.util.ColorProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.openide.util.NbBundle;

/**
 * @author -Rolf Hilker-
 * 
 * Panel showing general options for an alignment viewer.
 */
public class AlignmentOptionsPanel extends JPanel {
    
    private final AlignmentViewer alignmentViewer;

    public AlignmentOptionsPanel(AlignmentViewer alignmentViewer) {
        this.alignmentViewer = alignmentViewer;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(ColorProperties.LEGEND_BACKGROUND);
        this.initOtherComponents();
    }

    private void initOtherComponents() {
        final JLabel header = new JLabel(NbBundle.getMessage(AlignmentOptionsPanel.class, "AlignmentOptionsPanel.General"));
        header.setBackground(ColorProperties.LEGEND_BACKGROUND);
        header.setFont(new Font("Arial", Font.BOLD, 11));
        final JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorProperties.LEGEND_BACKGROUND);
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.setPreferredSize(new Dimension(headerPanel.getPreferredSize().width, headerPanel.getPreferredSize().height + 2));
        this.add(headerPanel);
        
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BorderLayout());
        generalPanel.setBackground(ColorProperties.LEGEND_BACKGROUND);
        final JCheckBox centerBox = new JCheckBox(NbBundle.getMessage(AlignmentOptionsPanel.class, "AlignmentOptionsPanel.Centering"));
        centerBox.setBackground(ColorProperties.LEGEND_BACKGROUND);
        centerBox.setSelected(true);
        
        //automatic centering around the sequence bar enabled event
        centerBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox scaleBox = (JCheckBox) e.getSource();
                alignmentViewer.setAutomaticCentering(scaleBox.isSelected());
            }
        });
        generalPanel.add(centerBox, BorderLayout.WEST);
        this.add(generalPanel);
        this.updateUI();
    }
    
}








