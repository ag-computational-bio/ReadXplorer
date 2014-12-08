/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;


public class RbsAnalysisWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RbsAnalysisVisualPanel component;
    private String wizardName;
    private int wholeLengthOfAnalysisRegion;


    public RbsAnalysisWizardPanel( String wizardName ) {
        this.wizardName = wizardName;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public RbsAnalysisVisualPanel getComponent() {
        if( component == null ) {
            component = new RbsAnalysisVisualPanel( this.wizardName );
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
//        this.wholeLengthOfAnalysisRegion = (int) wiz.getProperty(PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS);
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH, component.getExpectedMotifWidth() );
        wiz.putProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_NO_TRYING_BIOPROSPECTOR, component.getNoOfTrying() );
        wiz.putProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER, component.getMinSpacer() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, component.getLengthForAnalysis() );
        storePrefs();
    }


    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_LENGTH_MOTIFWIDTH, component.getExpectedMotifWidth().toString() );
        pref.put( wizardName + RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_MIN_SPACER, component.getMinSpacer().toString() );
        pref.put( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, component.getLengthForAnalysis().toString() );
    }


    @Override
    public void validate() throws WizardValidationException {
        int fstMinSpacer = component.getMinSpacer();
        int rbsMotifWidth = component.getExpectedMotifWidth();

        if( this.component.getLengthForAnalysis() < (fstMinSpacer + rbsMotifWidth) ) {
            throw new WizardValidationException( null, "Please check your Parameters for RBS analysis.", null );
        }
        if( (this.component.getLengthForAnalysis() - fstMinSpacer) < rbsMotifWidth ) {
            throw new WizardValidationException( null, "Please check on expected RBS motif-width for, beacause it is bigger than the region of interest.", null );
        }
    }


}
