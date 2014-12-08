/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;


public final class RbsAnalysisWizardIterator implements
        WizardDescriptor.Iterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor wiz;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] currentPanelsIndex;
    public static final String PROP_WIZARD_NAME = "RBS analysis";
    public static final String PROP_WORKING_DIR = "workingDir";
    public static final String PROP_RBS_ANALYSIS_ANALYSIS_LENGTH_ALL_ELEMENTS = "length for promotor analysis of all elements";
    public static final String PROP_RBS_ANALYSIS_REGION_LENGTH = "region upstream from TLS to analyze";
    public static final String PROP_RBS_ANALYSIS_LENGTH_FOR_CHARTANALYSIS = "region rel. to TLS for chart analysis";
    public static final String PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH = "expected motifWidth for bioProspector";
    public static final String PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR = "number of cycles bioPorspector should do";
    public static final String PROP_RBS_ANALYSIS_MIN_SPACER = "minimum Spacer from TLS to seqOfInterest";


    public RbsAnalysisWizardIterator() {
        initializePanels();
    }


    private void initializePanels() {
        if( allPanels == null ) {
            allPanels = new ArrayList<>();
//            allPanels.add(new DataSelectionWizardPanel(PurposeEnum.RBS_ANALYSIS));
            allPanels.add( new RbsAnalysisWizardPanel( PROP_WIZARD_NAME ) );
            String[] steps = new String[allPanels.size()];
            for( int i = 0; i < allPanels.size(); i++ ) {
                Component c = allPanels.get( i ).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if( c instanceof JComponent ) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i );
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, steps );
                    jc.putClientProperty( WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true );
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DISPLAYED, true );
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_NUMBERED, true );
                }
            }
            this.currentPanels = new ArrayList<>();
            currentPanels.add( this.allPanels.get( 0 ) );
//            currentPanels.add(this.allPanels.get(1));
//            currentPanelsIndex = new String[]{steps[0], steps[1]};
            currentPanelsIndex = new String[]{ steps[0] };

            Component c = allPanels.get( 0 ).getComponent();
            if( c instanceof JComponent ) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, currentPanelsIndex );
            }
        }
    }


    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     *            is not set, no properties can be stored, thus it always has to be set.
     */
    public void setWiz( WizardDescriptor wiz ) {
        this.wiz = wiz;
    }


    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return allPanels.get( index );
    }


    @Override
    public String name() {
        return index + 1 + ". from " + allPanels.size();
    }


    @Override
    public boolean hasNext() {
        return index < allPanels.size() - 1;
    }


    @Override
    public boolean hasPrevious() {
        return index > 0;
    }


    @Override
    public void nextPanel() {
        if( !hasNext() ) {
            throw new NoSuchElementException();
        }
        index++;
    }


    @Override
    public void previousPanel() {
        if( !hasPrevious() ) {
            throw new NoSuchElementException();
        }
        index--;
    }


    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener( ChangeListener l ) {
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed


}
