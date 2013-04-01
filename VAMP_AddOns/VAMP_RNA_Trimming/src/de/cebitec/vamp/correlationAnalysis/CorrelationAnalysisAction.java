/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.controller.TrackCacher;
import de.cebitec.vamp.databackend.CoverageAndDiffRequest;
import de.cebitec.vamp.databackend.CoverageThread;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Tools",
id = "de.cebitec.vamp.correlationAnalysis.CorrelationAnalysisAction")
@ActionRegistration(
    displayName = "#CTL_CorrelationAnalysisAction")
@ActionReference(path = "Menu/Tools", position = 3333)
@Messages("CTL_CorrelationAnalysisAction=analyse tracks correlation ")
public final class CorrelationAnalysisAction implements ActionListener {
    public final static String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public final static String PROP_INTERVALLENGTH = "PROP_INTERVALLENGTH";
    public final static String PROP_MINCORRELATION = "PROP_MINCORRELATION";

    

    private final static Logger LOG = Logger.getLogger(CorrelationAnalysisAction.class.getName());
    
    
    private final ReferenceViewer context;


    public CorrelationAnalysisAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        @SuppressWarnings("unchecked")
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        TrackListPanel tlp = new TrackListPanel(this.context.getReference().getId());
        tlp.setSelectAmount(2);
        panels.add(tlp);
        panels.add(new ParameterSelectionPanel());
        panels.add(new OverviewPanel());
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(VisualisationUtils.getWizardPanels(panels)));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(CorrelationAnalysisAction.class, "TTL_CAAWizardTitle"));
        
        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify(wiz) != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            new CorrelationAnalysisProcessor((List<PersistantTrack>) wiz.getProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_INTERVALLENGTH),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINCORRELATION));
        } else {
            //this.snpDetectionTopComp.close();
        }
    }
    
}
