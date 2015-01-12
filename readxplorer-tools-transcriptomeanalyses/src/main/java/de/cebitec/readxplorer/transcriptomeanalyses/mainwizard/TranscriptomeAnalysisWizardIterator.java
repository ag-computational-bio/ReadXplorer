
package de.cebitec.readxplorer.transcriptomeanalyses.mainwizard;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;


/**
 *
 * @author jritter
 */
public final class TranscriptomeAnalysisWizardIterator implements
        WizardDescriptor.Iterator<WizardDescriptor> {

    // Wizard descriptors
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> initPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> fivePrimeDatasetAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> wholeTranscriptDatasetAnalyses;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private String[] initPanelsIndex;
    private String[] fivePrimeIndex;
    private String[] wholeGenomeIndex;
    private WizardDescriptor wiz;
    private int index;
    private OpenTracksWizardPanel openTracksPanel;
    private final int referenceId;


    /**
     *
     * @param referenceId
     */
    public TranscriptomeAnalysisWizardIterator( int referenceId ) {
        this.referenceId = referenceId;
        this.initializePanels();
    }


    /**
     * Initializes all Wizard Panels.
     */
    private void initializePanels() {
        if( allPanels == null ) {
            allPanels = new ArrayList<>();
            allPanels.add( new RnaSeqDataTypeSelectionWizardPanel() );// 0
            allPanels.add( new WholeTranscriptTracksPanel( WizardPropertyStrings.PROP_WIZARD_NAME ) ); // 1
            allPanels.add( new FivePrimeEnrichedTracksPanel( WizardPropertyStrings.PROP_WIZARD_NAME ) ); // 2
            openTracksPanel = new OpenTracksWizardPanel( WizardPropertyStrings.PROP_WIZARD_NAME, referenceId ); // 3
            allPanels.add( openTracksPanel );

            String[] steps = new String[allPanels.size()];
            for( int i = 0; i < allPanels.size(); i++ ) {
                Component c = allPanels.get( i ).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if( c instanceof JComponent ) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i );
                    jc.putClientProperty( WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true );
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DISPLAYED, true );
                    jc.putClientProperty( WizardDescriptor.PROP_CONTENT_NUMBERED, true );
                }
            }

            initPanels = new ArrayList<>();
            initPanels.add( this.allPanels.get( 3 ) );
            initPanels.add( this.allPanels.get( 0 ) );
            initPanels.add( this.allPanels.get( 1 ) );
            initPanelsIndex = new String[]{ steps[3], steps[0], "..." };

            fivePrimeDatasetAnalyses = new ArrayList<>();
            fivePrimeDatasetAnalyses.add( this.allPanels.get( 3 ) );
            fivePrimeDatasetAnalyses.add( this.allPanels.get( 0 ) );
            fivePrimeDatasetAnalyses.add( this.allPanels.get( 2 ) );
            fivePrimeIndex = new String[]{ steps[3], steps[0], steps[2] };

            wholeTranscriptDatasetAnalyses = new ArrayList<>();
            wholeTranscriptDatasetAnalyses.add( this.allPanels.get( 3 ) );
            wholeTranscriptDatasetAnalyses.add( this.allPanels.get( 0 ) );
            wholeTranscriptDatasetAnalyses.add( this.allPanels.get( 1 ) );
            wholeGenomeIndex = new String[]{ steps[3], steps[0], steps[1] };

            this.currentPanels = initPanels;
            Component c = allPanels.get( 3 ).getComponent();
            if( c instanceof JComponent ) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex );
            }
        }
    }


    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return this.currentPanels.get( index );
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
        if( !hasNext() ) {
            throw new NoSuchElementException();
        }

        if( index == 1 ) { //whole genome dataset
            String[] contentData = initPanelsIndex;

            if( (boolean) wiz.getProperty( WizardPropertyStrings.PROP_FIVEPRIME_DATASET ) ) {
                this.currentPanels = this.fivePrimeDatasetAnalyses;
                contentData = this.fivePrimeIndex;
            }

            if( (boolean) wiz.getProperty( WizardPropertyStrings.PROP_WHOLEGENOME_DATASET ) ) {
                this.currentPanels = this.wholeTranscriptDatasetAnalyses;
                contentData = this.wholeGenomeIndex;
            }

            if( contentData != null ) {
                wiz.putProperty( WizardDescriptor.PROP_CONTENT_DATA, contentData );
            }
        }

        index++;
        wiz.putProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index );
    }


    @Override
    public void previousPanel() {
        if( !hasPrevious() ) {
            throw new NoSuchElementException();
        }
        if( index == 1 ) {
            currentPanels = initPanels;
            wiz.putProperty( WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex );
        }
        if( index == 2 ) {
            currentPanels = initPanels;
            wiz.putProperty( WizardDescriptor.PROP_CONTENT_DATA, initPanelsIndex );
        }
        if( index == 3 ) {
            currentPanels = initPanels;
            wiz.putProperty( WizardDescriptor.PROP_CONTENT_DATA, fivePrimeIndex );
        }
        index--;
        wiz.putProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index );
    }


    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener( ChangeListener l ) {
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
    }


    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     *            is not set, no properties can be stored, thus it always has to be set.
     */
    public void setWiz( WizardDescriptor wiz ) {
        this.wiz = wiz;
    }


    /**
     * @return the wizard, in which this wizard iterator is contained.
     */
    public WizardDescriptor getWiz() {
        return wiz;
    }


    /**
     * @return The list of track selected in this wizard.
     */
    public List<PersistentTrack> getSelectedTracks() {
        return this.openTracksPanel.getComponent().getSelectedTracks();
    }


}
