/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.chartGeneration;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ChartType;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.transcriptomeAnalyses.rbsAnalysis.DataSelectionWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

public final class VisualizationWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    // Example of invoking this wizard:
    // @ActionID(category="...", id="...")
    // @ActionRegistration(displayName="...")
    // @ActionReference(path="Menu/...")
    // public static ActionListener run() {
    //     return new ActionListener() {
    //         @Override public void actionPerformed(ActionEvent e) {
    //             WizardDescriptor wiz = new WizardDescriptor(new VisualizationWizardIterator());
    //             // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    //             // {1} will be replaced by WizardDescriptor.Iterator.name()
    //             wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
    //             wiz.setTitle("...dialog title...");
    //             if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
    //                 ...do something...
    //             }
    //         }
    //     };
    // }
    private int index;

    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> initPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> otherDataSelectionPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> elementsSelectionPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] initPanelsIndex;
    private String[] otherDataSelectionPanelsIndex;
    private String[] elementsSelectionPanelsIndex;
    private WizardDescriptor wiz;

    private void initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
            allPanels.add(new ChartsGenerationSelectChatTypeWizardPanel());
            allPanels.add(new DataSelectionWizardPanel(PurposeEnum.CHARTS));
            allPanels.add(new ElementsSelectionWizardPanel());
            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
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

            this.initPanels = new ArrayList<>();
            this.initPanels.add(allPanels.get(0));
            this.initPanels.add(allPanels.get(1));
            initPanelsIndex = new String[]{steps[0], "..."};

            this.otherDataSelectionPanels = new ArrayList<>();
            this.otherDataSelectionPanels.add(allPanels.get(0));
            this.otherDataSelectionPanels.add(allPanels.get(1));
            this.otherDataSelectionPanelsIndex = new String[]{steps[0], steps[1]};

            this.elementsSelectionPanels = new ArrayList<>();
            this.elementsSelectionPanels.add(allPanels.get(0));
            this.elementsSelectionPanels.add(allPanels.get(2));
            this.elementsSelectionPanelsIndex = new String[]{steps[0], steps[2]};

            this.currentPanels = initPanels;
            Component c = allPanels.get(0).getComponent();
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex);
            }
        }
    }

    /**
     *
     * @param referenceId
     */
    public VisualizationWizardIterator() {
        this.initializePanels();
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return this.currentPanels.get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + this.currentPanels.size();
    }

    @Override
    public boolean hasNext() {
        return index < this.currentPanels.size() - 1;
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
        if (index == 0) { //whole genome dataset
            String[] contentData = initPanelsIndex;

            if ((boolean) wiz.getProperty(ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString()) || (boolean) wiz.getProperty(ChartType.BASE_DISTRIBUTION.toString())) {
                this.currentPanels = this.otherDataSelectionPanels;
                contentData = this.otherDataSelectionPanelsIndex;
            }

            if ((boolean) wiz.getProperty(ChartType.PIE_CHART.toString())) {
                this.currentPanels = this.elementsSelectionPanels;
                contentData = this.elementsSelectionPanelsIndex;
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

        if (index == 1) {
            currentPanels = initPanels;
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex);
        }
        if (index == 2) {
            currentPanels = initPanels;
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex);
        }
        index--;
        wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
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

    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     * is not set, no properties can be stored, thus it always has to be set.
     */
    public void setWiz(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    /**
     * @return the wizard, in which this wizard iterator is contained.
     */
    public WizardDescriptor getWiz() {
        return wiz;
    }

}
