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
package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.LegendAndOptionsProvider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * Panel showing general options for an alignment viewer.
 * 
 * @author -Rolf Hilker-
 */
public class AlignmentOptionsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final AlignmentViewer alignmentViewer;

    public AlignmentOptionsPanel(AlignmentViewer alignmentViewer) {
        this.alignmentViewer = alignmentViewer;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(ColorProperties.LEGEND_BACKGROUND);
        this.initOtherComponents();
    }

    @NbBundle.Messages({"AlignmentOptionsPanel.General=General:",
                        "AlignmentOptionsPanel.Centering=Enable Centering Sequence Bar"})
    private void initOtherComponents() {
        final JLabel header = new JLabel(Bundle.AlignmentOptionsPanel_General());
        header.setBackground(ColorProperties.LEGEND_BACKGROUND);
        header.setFont(new Font("Arial", Font.BOLD, 11));
        final JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorProperties.LEGEND_BACKGROUND);
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.setPreferredSize(new Dimension(headerPanel.getPreferredSize().width, headerPanel.getPreferredSize().height + 2));
        this.add(headerPanel);
        
        LegendAndOptionsProvider.createMappingQualityFilter(alignmentViewer, this);
        
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








