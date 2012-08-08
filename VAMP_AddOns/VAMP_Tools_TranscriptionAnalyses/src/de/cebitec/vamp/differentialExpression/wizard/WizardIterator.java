package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.api.cookies.LoginCookie;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.AnalysisHandler;
import de.cebitec.vamp.differentialExpression.BaySeqAnalysisHandler;
import de.cebitec.vamp.differentialExpression.DiffExpResultViewerTopComponent;
import de.cebitec.vamp.differentialExpression.Group;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(category = "Tools", id = "de.cebitec.vamp.differentialExpression.DiffExpWizardAction")
@ActionRegistration(displayName = "Differential expression analysis")
@ActionReference(path = "Menu/Tools")
public final class WizardIterator implements WizardDescriptor.Iterator<WizardDescriptor>, ActionListener {

    private final LoginCookie context;

    public WizardIterator(LoginCookie context) {
        this.context = context;
    }
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> baySeqPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> deSeqPanels;
    private String[] baySeqIndex;
    private String[] deSeqIndex;
    private WizardDescriptor wiz;

    @Override
    public void actionPerformed(ActionEvent e) {
        initializePanels();
        wiz = new WizardDescriptor(this);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle("Differential expression analysis");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            AnalysisHandler.Tool tool = (AnalysisHandler.Tool) wiz.getProperty("tool");
            List<Group> createdGroups = (List<Group>) wiz.getProperty("createdGroups");
            List<PersistantTrack> selectedTraks = (List<PersistantTrack>) wiz.getProperty("tracks");
            Integer genomeID = (Integer) wiz.getProperty("genomeID");
            int[] replicateStructure = (int[]) wiz.getProperty("replicateStructure");
            File saveFile = (File) wiz.getProperty("saveFile");
            List<String[]> design = (List<String[]>) wiz.getProperty("design");
            AnalysisHandler handler = null;

            if (tool == AnalysisHandler.Tool.BaySeq) {
                handler = new BaySeqAnalysisHandler(selectedTraks, createdGroups, genomeID, replicateStructure, saveFile);
                DiffExpResultViewerTopComponent diffExpResultViewerTopComponent = new DiffExpResultViewerTopComponent(handler);
                diffExpResultViewerTopComponent.open();
                diffExpResultViewerTopComponent.requestActive();
                handler.registerObserver(diffExpResultViewerTopComponent);
            }

            if (tool == AnalysisHandler.Tool.DeSeq) {
                handler = new DeSeqAnalysisHandler(selectedTraks, design, genomeID, saveFile);
            }

            handler.start();
        }
    }

    private void initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
            allPanels.add(new ChooseWizardPanel());
            allPanels.add(new SelectTrackWizardPanel());
            allPanels.add(new BaySeqWizardPanel2());
            allPanels.add(new BaySeqWizardPanel3());
            allPanels.add(new DeSeqWizardPanelDesign());
            allPanels.add(new StartAnalysisWizardPanel());
            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    String[] initialyShownSteps = new String[]{steps[0], "..."};
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, initialyShownSteps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            baySeqPanels = new ArrayList<>();
            baySeqPanels.add(allPanels.get(0));
            baySeqPanels.add(allPanels.get(1));
            baySeqPanels.add(allPanels.get(2));
            baySeqPanels.add(allPanels.get(3));
            baySeqPanels.add(allPanels.get(5));
            baySeqIndex = new String[]{steps[0], steps[1], steps[2], steps[3], steps[5]};

            deSeqPanels = new ArrayList<>();
            deSeqPanels.add(allPanels.get(0));
            deSeqPanels.add(allPanels.get(1));
            deSeqPanels.add(allPanels.get(4));
            deSeqPanels.add(allPanels.get(5));
            deSeqIndex = new String[]{steps[0], steps[1], steps[4], steps[5]};

            currentPanels = baySeqPanels;
        }
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return currentPanels.get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + currentPanels.size();
    }

    @Override
    public boolean hasNext() {
        return index < currentPanels.size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (index == 0) {
            String[] contentData = null;
            AnalysisHandler.Tool tool = (AnalysisHandler.Tool) wiz.getProperty("tool");
            if (tool == AnalysisHandler.Tool.DeSeq) {
                currentPanels = deSeqPanels;
                contentData = deSeqIndex;
            }
            if (tool == AnalysisHandler.Tool.BaySeq) {
                currentPanels = baySeqPanels;
                contentData = baySeqIndex;
            }
            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }
        index++;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between allPanels), e.g.
    // the number of allPanels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed
}
