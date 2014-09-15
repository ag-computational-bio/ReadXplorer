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
package de.cebitec.readXplorer.correlationAnalysis;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * This action creates an menu item to start the correlation analysis.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>, rhilker
 */
@ActionID(
    category = "Tools",
id = "de.cebitec.readXplorer.correlationAnalysis.CorrelationAnalysisAction")
@ActionRegistration(
    displayName = "#CTL_CorrelationAnalysisAction")
@ActionReference(path = "Menu/Tools", position = 156) 
@Messages("CTL_CorrelationAnalysisAction=Correlation analysis")
public final class CorrelationAnalysisAction implements ActionListener {
    
    public static final String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public static final String PROP_INTERVALLENGTH = "PROP_INTERVALLENGTH";
    public static final String PROP_MINCORRELATION = "PROP_MINCORRELATION";
    public static final String PROP_MINPEAKCOVERAGE = "PROP_MINPEAKCOVERAGE";
    public static final String PROP_CORRELATIONCOEFFICIENT = "PROP_CORRELATIONCOEFFICIENT";
    private static final String PROP_WIZARD_NAME = "CORRELATION_WIZARD";
    
    
    public enum CorrelationCoefficient {PEARSON, SPEARMAN};
    

    private SelectReadClassWizardPanel readClassWizPanel;
    private final static Logger LOG = Logger.getLogger(CorrelationAnalysisAction.class.getName());
    private final ReferenceViewer context;

    /**
     * This action creates an menu item to start the correlation analysis.
     * @param context 
     */
    public CorrelationAnalysisAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        TrackListPanel trackPanel = new TrackListPanel(this.context.getReference().getId());
        this.readClassWizPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME, false);
        trackPanel.getComponent().setSelectAmount(2);
        panels.add(trackPanel);
        panels.add(new ParameterSelectionPanel());
        panels.add(this.readClassWizPanel);
        panels.add(new OverviewPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(CorrelationAnalysisAction.class, "TTL_CAAWizardTitle"));
        
        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            ParametersReadClasses readClassesParams = (ParametersReadClasses) wiz.getProperty(readClassWizPanel.getPropReadClassParams());
            CorrelationCoefficient coefficient = (CorrelationCoefficient) wiz.getProperty(CorrelationAnalysisAction.PROP_CORRELATIONCOEFFICIENT);
            List<PersistentTrack> tracks = (List<PersistentTrack>) wiz.getProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS);
            int intervalLength = (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_INTERVALLENGTH);
            int minCorrelation = (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINCORRELATION);
            int minPeakCoverage = (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINPEAKCOVERAGE);
            
            ParameterSetCorrelationAnalysis parametersCorrelationAnalysis = new ParameterSetCorrelationAnalysis(
                    readClassesParams, coefficient, intervalLength, minCorrelation, minPeakCoverage, tracks);
            
            new CorrelationAnalysisProcessor(context, parametersCorrelationAnalysis);
        } else {
            //do nothing
        }
    }
    
}
