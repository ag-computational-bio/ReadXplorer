package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
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

@ActionID(
    category = "Tools",
id = "de.cebitec.vamp.correlationAnalysis.CorrelationAnalysisAction")
@ActionRegistration(
    displayName = "#CTL_CorrelationAnalysisAction")
@ActionReference(path = "Menu/Tools", position = 3333) 
@Messages("CTL_CorrelationAnalysisAction=Analyse tracks correlation ")
public final class CorrelationAnalysisAction implements ActionListener {
    public final static String PROP_SELECTED_TRACKS = "PROP_SELECTED_TRACKS";
    public final static String PROP_INTERVALLENGTH = "PROP_INTERVALLENGTH";
    public final static String PROP_MINCORRELATION = "PROP_MINCORRELATION";
    public final static String PROP_MINPEAKCOVERAGE = "PROP_MINPEAKCOVERAGE";
    

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
            new CorrelationAnalysisProcessor(context,    
                (List<PersistantTrack>) wiz.getProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_INTERVALLENGTH),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINCORRELATION),
                (Integer) wiz.getProperty(CorrelationAnalysisAction.PROP_MINPEAKCOVERAGE)
                    );
        } else {
            //this.snpDetectionTopComp.close();
        }
    }
    
}
