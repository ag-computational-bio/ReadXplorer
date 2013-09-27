/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses.wizard;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dialogMenus.SelectFeatureTypeWizardPanel;
import de.cebitec.vamp.view.dialogMenus.SelectReadClassWizardPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

// Example of invoking this wizard:
//@ActionID(category = "Tools", id = "de.cebitec.vamp.transcriptomeAnalyses.TranscriptomeAnalysesWizardIterator")
//@ActionRegistration(displayName = "Open Transcriptome Analyses")
//@ActionReference(path = "Menu/Tools")
public final class TranscriptomeAnalysisWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    
    private static final String PROP_WIZARD_NAME = "TransAnalyses";
    
    private List<WizardDescriptor.Panel<WizardDescriptor>> initPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> fifePrimeAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> wholegenomeAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> fifePrimeSelectedAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> wholeGenomeSelectedAnayses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] initPanelsIndex;
    private String[] fifePrimeIndex;
    private String[] wholeGenomeIndex;
    WizardDescriptor wiz;
    private Object dataset;
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private SelectReadClassWizardPanel readClassPanel;
    private SelectFeatureTypeWizardPanel featTypePanel;
    private ChangeSupport changeSupport;

    public TranscriptomeAnalysisWizardIterator() {
        this.changeSupport = new ChangeSupport(this);
        this.initializePanels();
//        wiz = new WizardDescriptor(this);
//        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
//        // {1} will be replaced by WizardDescriptor.Iterator.name()
//        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
//        wiz.setTitle("Settings");
//        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
//            List<PersistantTrack> selectedTracks = (List<PersistantTrack>) wiz.getProperty("tracks");
//            Integer genomeID = (Integer) wiz.getProperty("genomeID");
//
////            FifeEnrichedDataAnalysesHandler handler = new FifeEnrichedDataAnalysesHandler(selectedTracks.get(0), genomeID, parametersetTSS);
////             Hier beginnt die SOUCE wenn auf FINISCH gecklickt wird!
//            // 1. parse Feature information
////            handler.start();
//            // 2. parse Mapping information
//            // . Simmulation der Daten!
////            Simmulations simmulations = new Simmulations(index);
//        }
    }

    private void initializePanels() {
        if (allPanels == null) {
            allPanels = new ArrayList<>();
            allPanels.add(new DataSetChoicePanel());// 0
            allPanels.add(new WholeGenomeTracksPanel()); // 1
            allPanels.add(new FivePrimeEnrichedTracksPanel()); // 2
            allPanels.add(new TssDetectionParamsPanel()); // 3
            allPanels.add(new RbsDetectionParamsPanel()); // 4
            allPanels.add(new PromotorDetectionParamPanel()); // 5
            allPanels.add(new LeaderlessDetectionPanel()); // 6
            allPanels.add(new AntisenseDetectionParamsPanel()); // 7
            allPanels.add(new NewRegionDetectionParamsPanel()); // 8
            allPanels.add(new OperonsDetectionParamsPanel()); // 9
            
            readClassPanel = new SelectReadClassWizardPanel(PROP_WIZARD_NAME);
            allPanels.add(readClassPanel); // 10
            featTypePanel = new SelectFeatureTypeWizardPanel(PROP_WIZARD_NAME);
            allPanels.add(featTypePanel); // 11
            
            String[] steps = new String[allPanels.size()];
            for (int i = 0; i < allPanels.size(); i++) {
                Component c = allPanels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
//                    initPanelsIndex = new String[]{steps[0], allPanels.get(1).getComponent().getName(), allPanels.get(2).getComponent().getName(), "..."};
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }



            initPanels = new ArrayList<>();
            initPanels.add(this.allPanels.get(0));
            initPanels.add(this.allPanels.get(1));
            initPanels.add(this.allPanels.get(2));

//            initPanelsIndex = new String[]{steps[1], steps[2], "..."};


            fifePrimeAnalyses = new ArrayList<>();
            fifePrimeAnalyses.add(this.allPanels.get(0));
            fifePrimeAnalyses.add(this.allPanels.get(2));
            fifePrimeAnalyses.add(this.allPanels.get(3));


            fifePrimeIndex = new String[]{steps[0], steps[2], "..."};

            wholegenomeAnalyses = new ArrayList<>();
            wholegenomeAnalyses.add(this.allPanels.get(0));
            wholegenomeAnalyses.add(this.allPanels.get(1));
            wholegenomeAnalyses.add(this.allPanels.get(8));


            wholeGenomeIndex = new String[]{steps[0], steps[1], "..."};

            fifePrimeSelectedAnalyses = new ArrayList<>();
            fifePrimeSelectedAnalyses.add(this.allPanels.get(0));
            fifePrimeSelectedAnalyses.add(this.allPanels.get(2));


            wholeGenomeSelectedAnayses = new ArrayList<>();
            wholeGenomeSelectedAnayses.add(this.allPanels.get(0));
            wholeGenomeSelectedAnayses.add(this.allPanels.get(1));

            this.currentPanels = initPanels;
        }



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

        if (index == 0) { //whole genome dataset
            String[] contentData = null;
            this.dataset = wiz.getProperty("dataSet");

            System.out.println(this.dataset);
            if (dataset.equals("fifeprime")) {
                this.currentPanels = this.fifePrimeAnalyses;
                contentData = this.fifePrimeIndex;
            }

            if (dataset.equals("wholegenome")) {
                this.currentPanels = this.wholegenomeAnalyses;
                contentData = this.wholeGenomeIndex;
            }

            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }


        if (index == 1 && dataset.equals("fifeprime")) { // we are in fifeprime analyses   
            List<String> contentData = new ArrayList<String>();
            contentData.add(allPanels.get(0).getComponent().getName());
            contentData.add(allPanels.get(2).getComponent().getName());
            if (wiz.getProperty("TSSanalysis") != null) {
                fifePrimeSelectedAnalyses.add(this.allPanels.get(3));
                contentData.add(allPanels.get(3).getComponent().getName());
            }
            if (wiz.getProperty("RBSanalysis") != null) {
                fifePrimeSelectedAnalyses.add(this.allPanels.get(4));
                contentData.add(allPanels.get(4).getComponent().getName());
            }
            if (wiz.getProperty("PROMOTORAnalysis") != null) {
                fifePrimeSelectedAnalyses.add(this.allPanels.get(5));
                contentData.add(allPanels.get(5).getComponent().getName());
            }
            if (wiz.getProperty("LEADERLESSAnalysis") != null) {
                fifePrimeSelectedAnalyses.add(this.allPanels.get(6));
                contentData.add(allPanels.get(6).getComponent().getName());
            }
            if (wiz.getProperty("ANTAnalysis") != null) {
                fifePrimeSelectedAnalyses.add(this.allPanels.get(7));
                contentData.add(allPanels.get(7).getComponent().getName());
            }

            this.currentPanels = this.fifePrimeSelectedAnalyses;
            if (!contentData.isEmpty()) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
        }

        if (index == 1 && dataset.equals("wholegenome")) { // we are in wholegenome analyses
            List<String> contentData = new ArrayList<String>();
            contentData.add(allPanels.get(0).getComponent().getName());
            contentData.add(allPanels.get(1).getComponent().getName());
            if (wiz.getProperty("novel") == null) {
                wholeGenomeSelectedAnayses.add(this.allPanels.get(8));
                contentData.add(allPanels.get(8).getComponent().getName());
            }
            if (wiz.getProperty("operon") != null) {
                wholeGenomeSelectedAnayses.add(this.allPanels.get(9));
                contentData.add(allPanels.get(9).getComponent().getName());
            }

//            wholeGenomeSelectedAnayses.add(allPanels.get(10));
//            contentData.add(allPanels.get(10).getComponent().getName());

            this.currentPanels = this.wholeGenomeSelectedAnayses;
            if (!contentData.isEmpty()) {
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
        String[] contentData = null;
        if (index == 1) {
            currentPanels = initPanels;
            contentData = initPanelsIndex;
            if (contentData != null) {
                wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, contentData);
            }
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

    /**
     * @param usingADBTrack true, if the wizard is running on a track stored
     * completely in the DB, false otherwise.
     */
    public void setUsingDBTrack(boolean containsDBTrack) {
        this.readClassPanel.getComponent().setUsingDBTrack(containsDBTrack);
    }

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
    
    /**
     * @return The dynamically generated property name for the read class 
     * selection for this wizard. Can be used to obtain the corresponding
     * read class parameters.
     */
    public String getReadClassPropForWiz() {
        return this.readClassPanel.getPropReadClassParams();
    }
    
    /**
     * @return The property string for the selected feature type list for the
     * corresponding wizard.
     */
    public String getPropSelectedFeatTypes() {
        return this.featTypePanel.getPropSelectedFeatTypes();
    }
}
