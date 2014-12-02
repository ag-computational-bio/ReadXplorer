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


public class FivePrimeUTRPromotorSettingsWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    private final String wizardName;
    private int wholeLengthOfAnalysisRegion;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FivePrimeUTRPromotorSettingsVisualPanel component;


    public FivePrimeUTRPromotorSettingsWizardPanel( String wizardName ) {
        this.wizardName = wizardName;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FivePrimeUTRPromotorSettingsVisualPanel getComponent() {
        if( component == null ) {
            component = new FivePrimeUTRPromotorSettingsVisualPanel( this.wizardName );
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
        this.wholeLengthOfAnalysisRegion = (int) wiz.getProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH, component.getSpacer1Length() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH, component.getSpacer2Length() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION, component.getPutativeMinusTenLength() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION, component.getPutativeMinusThirtyFiveLength() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH, component.getMinus10MotifWidth() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH, component.getMinus35MotifWidth() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING, component.getNoTrying() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER, component.getAlternativeSpacer() );
        storePrefs();
    }


    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER1_LENGTH, component.getSpacer1Length() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_SPACER2_LENGTH, component.getSpacer2Length() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_10_REGION, component.getPutativeMinusTenLength() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_PUTATIVE_35_REGION, component.getPutativeMinusThirtyFiveLength() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS10_MOTIF_LENGTH, component.getMinus10MotifWidth() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_MINUS35_MOTIF_LENGTH, component.getMinus35MotifWidth() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_NUMBER_OF_TRYING, component.getNoTrying() );
        pref.putInt( wizardName + PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_ALTERNATIVE_SPACER, component.getAlternativeSpacer() );
    }


    @Override
    public void validate() throws WizardValidationException {
        int fstMinSpacer = component.getSpacer1Length();
        int sndMinSpacer = component.getSpacer2Length();
        int lengtOfMinus10Region = component.getPutativeMinusTenLength();
        int lengthOfMinus35Region = component.getPutativeMinusThirtyFiveLength();
        int minus10MotifWidth = component.getMinus10MotifWidth();
        int minus35MotifWidth = component.getMinus35MotifWidth();
        int alternativeSpacer = component.getAlternativeSpacer();

        if( this.wholeLengthOfAnalysisRegion < (fstMinSpacer + sndMinSpacer + lengtOfMinus10Region + lengthOfMinus35Region) ) {
            throw new WizardValidationException( null, "Please check your Parameters for promotor analysis.", null );
        }

        if( lengtOfMinus10Region < minus10MotifWidth ) {
            throw new WizardValidationException( null, "Please check on motif-width for -10 region, beacause it is bigger than the region of interest.", null );
        }

        if( lengthOfMinus35Region < minus35MotifWidth ) {
            throw new WizardValidationException( null, "Please check on motif-width for -35 region, because it is bigger than the region of interest.", null );
        }

        if( alternativeSpacer < 0 ) {
            throw new WizardValidationException( null, "Alternative Spacer should be bigger than 0.", null );
        }
    }


}
