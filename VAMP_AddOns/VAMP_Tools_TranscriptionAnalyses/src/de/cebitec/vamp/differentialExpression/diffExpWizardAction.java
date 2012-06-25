/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category = "...", id = "de.cebitec.vamp.differentialExpression.diffExpWizardAction")
@ActionRegistration(displayName = "Differential expression analysis")
@ActionReference(path = "Menu/Tools")
public final class diffExpWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new diffExpWizardPanel1());
        panels.add(new diffExpWizardPanel1b());
        panels.add(new diffExpWizardPanel2());
        panels.add(new diffExpWizardPanel3());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("Differential expression analysis");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            List<Group> createdGroups = (List<Group>) wiz.getProperty("createdGroups");
            List<PersistantTrack> selectedTraks = (List<PersistantTrack>) wiz.getProperty("tracks");
            Integer genomeID = (Integer) wiz.getProperty("genomeID");
            int[] replicateStructure = (int[]) wiz.getProperty("replicateStructure");
            PerformAnalysis perfAnalysis = new PerformAnalysis(PerformAnalysis.Tool.BaySeq, selectedTraks, createdGroups, genomeID, replicateStructure);
            
            DiffExpResultViewerTopComponent diffExpResultViewerTopComponent = new DiffExpResultViewerTopComponent();
            diffExpResultViewerTopComponent.open();
            diffExpResultViewerTopComponent.requestActive();
            perfAnalysis.registerObserver(diffExpResultViewerTopComponent);
            perfAnalysis.start();
        }
    }
}
