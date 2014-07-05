/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.LegendAndOptionsProvider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

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
                        "AlignmentOptionsPanel.Centering=Enable centering sequence bar",
                        "AlignmentOptionsPanel.Qualities=Show base qualities", 
                        "AlignmentOptionsPanel_QualityToolTip=Good quality bases = bright hue, bad quality bases = dark hue"})
    private void initOtherComponents() {
        //create header
        this.add(LegendAndOptionsProvider.createHeader(Bundle.AlignmentOptionsPanel_General()));
        //create mapping filter
        LegendAndOptionsProvider.createMappingQualityFilter(alignmentViewer, this);
        this.createCenterSeqBarBox();
        this.createShowBaseQualitiesBox();
        
        this.updateUI();
    }

    /**
     * Creates a check box for centering the sequence bar when selected.
     */
    private void createCenterSeqBarBox() {
        JPanel generalPanel = LegendAndOptionsProvider.createStandardPanel();
        final JCheckBox centerBox = LegendAndOptionsProvider.createStandardCheckBox(Bundle.AlignmentOptionsPanel_Centering());
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
    }

    /**
     * Creates a check box for enabling visualization of base qualities when
     * selected.
     */
    private void createShowBaseQualitiesBox() {
        Preferences pref = NbPreferences.forModule(Object.class);
        JPanel generalPanel = LegendAndOptionsProvider.createStandardPanel();
        final JCheckBox qualitiesBox = LegendAndOptionsProvider.createStandardCheckBox(Bundle.AlignmentOptionsPanel_Qualities());
        qualitiesBox.setSelected(pref.getBoolean(Properties.BASE_QUALITY_OPTION, true));
        qualitiesBox.setToolTipText(Bundle.AlignmentOptionsPanel_QualityToolTip());
        generalPanel.add(qualitiesBox, BorderLayout.WEST);
        
        //update base qualities property, when the box selection is changed
        qualitiesBox.addActionListener(new ActionListener() {

            //
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox scaleBox = (JCheckBox) e.getSource();
                NbPreferences.forModule(Object.class).putBoolean(Properties.BASE_QUALITY_OPTION, scaleBox.isSelected());
            }
        });
        
        this.add(generalPanel);
    }
    
}








