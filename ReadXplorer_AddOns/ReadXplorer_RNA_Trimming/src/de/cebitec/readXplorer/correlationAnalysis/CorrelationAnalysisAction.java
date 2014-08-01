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

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
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
 * This action creates an menu item to start the correlation analysis
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
@ActionID(
    category = "Tools",
id = "de.cebitec.readXplorer.correlationAnalysis.CorrelationAnalysisAction")
@ActionRegistration(
    displayName = "#CTL_CorrelationAnalysisAction")
@ActionReference(path = "Menu/Tools", position = 156) 
@Messages("CTL_CorrelationAnalysisAction=Correlation analysis")
public final class CorrelationAnalysisAction implements ActionListener {
    public final static String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public final static String PROP_INTERVALLENGTH = "PROP_INTERVALLENGTH";
    public final static String PROP_MINCORRELATION = "PROP_MINCORRELATION";
    public final static String PROP_MINPEAKCOVERAGE = "PROP_MINPEAKCOVERAGE";
    public final static String PROP_CORRELATIONCOEFFICIENT = "PROP_CORRELATIONCOEFFICIENT";
    
    public enum CorrelationCoefficient {PEARSON, SPEARMAN};
    

    private final static Logger LOG = Logger.getLogger(CorrelationAnalysisAction.class.getName());
    
    
    private final ReferenceViewer context;


    public CorrelationAnalysisAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        TrackListPanel trackPanel = new TrackListPanel(this.context.getReference().getId());
        trackPanel.getComponent().setSelectAmount(2);
        panels.add(trackPanel);
        panels.add(new ParameterSelectionPanel());
        panels.add(new OverviewPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(CorrelationAnalysisAction.class, "TTL_CAAWizardTitle"));
        
        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            new CorrelationAnalysisProcessor(  
                (CorrelationCoefficient) wiz.getProperty(CorrelationAnalysisAction.PROP_CORRELATIONCOEFFICIENT),
                context,
                (List<PersistantTrack>) wiz.getProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_INTERVALLENGTH),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINCORRELATION),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINPEAKCOVERAGE)
                    );
        } else {
            //do nothing
        }
    }
    
}
