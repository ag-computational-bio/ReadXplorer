
package de.cebitec.readxplorer.transcriptomeanalyses.chartGeneration;


import de.cebitec.readxplorer.transcriptomeanalyses.enums.ChartType;
import de.cebitec.readxplorer.transcriptomeanalyses.motifSearch.RbsAnalysisWizardIterator;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;


/**
 *
 * @author jritter
 */
public class ChartsGenerationSelectChatTypeWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    public static final String CHARTS_BINING = "Bining all 5'-UTR length";
    public static final String CHARTS_BINING_SIZE = "Bining size of 5'-UTR length";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ChartsGenerationSelectChatTypeVisualPanel component;


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ChartsGenerationSelectChatTypeVisualPanel getComponent() {
        if( component == null ) {
            component = new ChartsGenerationSelectChatTypeVisualPanel();
        }
        return component;
    }


    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }


    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }


    @Override
    public void addChangeListener( ChangeListener l ) {
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        // use wiz.getProperty to retrieve previous panel state
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty( ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString(), component.isAbsoluteFrequency() );
        wiz.putProperty( ChartType.BASE_DISTRIBUTION.toString(), component.isBaseDistribution() );
        wiz.putProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH, component.getRangeOfUpstrSeq() );
        wiz.putProperty( CHARTS_BINING, component.isBiningSelected() );
        wiz.putProperty( CHARTS_BINING_SIZE, component.getBiningSize() );
        wiz.putProperty( ChartType.CHARTS_BASE_DIST_GA_CT.toString(), component.isDistOfGaCtSelected() );
        wiz.putProperty( ChartType.CHARTS_BASE_DIST_GC_AT.toString(), component.isDistOfGcATSelected() );

    }


    @Override
    public void validate() throws WizardValidationException {
        int upstreamLength;
        int binSize;

        if( component.isBaseDistribution() ) {
            if( !component.isDistOfGaCtSelected() && !component.isDistOfGcATSelected() ) {
                throw new WizardValidationException( null, "You've selected the base distribution plot. Please also select one of the given types.", null );
            }
        }

        try {
            upstreamLength = component.getRangeOfUpstrSeq();
            binSize = component.getBiningSize();

        }
        catch( NumberFormatException nfe ) {
            throw new WizardValidationException( null, "Please check your textfields regarding string input.", null );
        }

        if( upstreamLength < 1 ) {
            throw new WizardValidationException( null, "Please choose an upstream sequence length greater or equal 1.", null );
        }
        if( binSize < 1 ) {
            throw new WizardValidationException( null, "Please choose a bin size greater or equal 1.", null );
        }
    }


}
