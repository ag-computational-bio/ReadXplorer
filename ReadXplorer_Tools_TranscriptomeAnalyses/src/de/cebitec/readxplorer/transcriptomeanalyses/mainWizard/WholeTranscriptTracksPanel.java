
package de.cebitec.readxplorer.transcriptomeanalyses.mainWizard;


import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;


public class WholeTranscriptTracksPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WholeTranscriptTracksVisualPanel component;
    private final String wizardName;


    public WholeTranscriptTracksPanel( String wizardName ) {
        this.wizardName = wizardName;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public WholeTranscriptTracksVisualPanel getComponent() {
        if( component == null ) {
            component = new WholeTranscriptTracksVisualPanel( this.wizardName );
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
        wiz.putProperty(WizardPropertyStrings.PROP_RPKM_ANALYSIS, component.isRPKM());
        wiz.putProperty(WizardPropertyStrings.PROP_NOVEL_ANALYSIS, component.isNewRegions());
        wiz.putProperty(WizardPropertyStrings.PROP_OPERON_ANALYSIS, component.isOperonDetection());
        if( component.isNewRegions() ) {
            wiz.putProperty(WizardPropertyStrings.PROP_Fraction, component.getFractionForNewRegionDetection());
            wiz.putProperty(WizardPropertyStrings.PROP_MIN_LENGTH_OF_NOVEL_TRANSCRIPT, component.getMinBoundaryForNovelRegionDetection());
            wiz.putProperty(WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION, component.isInclusionOfRatioValueSelected());
            wiz.putProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_NR, component.isIncludeBestMatchedReadsNR());
            wiz.putProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE, component.isBgThresholdSetManually_NT());
            wiz.putProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE, component.getBgThresholdSetManually_NT());
            wiz.putProperty(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION, component.getIncreaseRatioValue());
        }
        else if( component.isOperonDetection() ) {
            wiz.putProperty(WizardPropertyStrings.PROP_Fraction, component.getFractionForOperonDetection());
            wiz.putProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_OP, component.isIncludeBestMatchedReadsOP());
            wiz.putProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE, component.isBgThresholdSetManually_OP());
            wiz.putProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE, component.getBgThresholdSetManually_OP());
        }
        wiz.putProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_RPKM, component.isIncludeBestMatchedReadsRpkm());


//        wiz.putProperty(WizardPropertyStrings.PROP_FRACTION_NOVELREGION_DETECTION, (double) component.getFractionForNewRegionDetection());
        File f = component.getRefFile();
        wiz.putProperty( WizardPropertyStrings.PROP_REFERENCE_FILE_RPKM_DETERMINATION, f );
//        if (component.isInclusionOfRatioValueSelected()) {
//            wiz.putProperty(WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION, (int) component.getIncreaseRatioValue());
//        }
        storePrefs();
    }


    /**
     * Stores the entries that are made during use.
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.putBoolean( wizardName + WizardPropertyStrings.PROP_RPKM_ANALYSIS, component.isRPKM() );
        pref.putBoolean( wizardName + WizardPropertyStrings.PROP_NOVEL_ANALYSIS, component.isNewRegions() );
        pref.putBoolean( wizardName + WizardPropertyStrings.PROP_OPERON_ANALYSIS, component.isOperonDetection() );
        if( component.isOperonDetection() ) {
            pref.putDouble( wizardName + WizardPropertyStrings.PROP_Fraction, component.getFractionForOperonDetection() );
        }
        else if( component.isNewRegions() ) {
            pref.putDouble( wizardName + WizardPropertyStrings.PROP_Fraction, component.getFractionForNewRegionDetection() );
        }
        pref.putInt( wizardName + WizardPropertyStrings.PROP_MIN_LENGTH_OF_NOVEL_TRANSCRIPT, component.getMinBoundaryForNovelRegionDetection() );
        pref.putBoolean( wizardName + WizardPropertyStrings.PROP_INCLUDE_RATIO_VALUE_IN_NOVEL_REGION_DETECTION, component.isInclusionOfRatioValueSelected() );
        if( component.isInclusionOfRatioValueSelected() ) {
            pref.putInt( WizardPropertyStrings.PROP_RATIO_NOVELREGION_DETECTION, component.getIncreaseRatioValue() );
        }
    }


    /**
     * Validation of the fraction TextFields whether the Fraction is bigger than
     * 0.0 and smaller than 1 or not.
     *
     * @return true if fraction bigger 0.0 && smaller 1.0, false else.
     */
    private boolean validateFractionInput() {
        boolean validate = false;
        if( component.getFractionForNewRegionDetection() > 0
            && component.getFractionForOperonDetection() > 0
            && component.getFractionForNewRegionDetection() < 1.0
            && component.getFractionForOperonDetection() < 1.0 ) {
            validate = true;
        }
        return validate;
    }


    @Override
    public void validate() throws WizardValidationException {

        try {
            double fractionNT = component.getFractionForNewRegionDetection();
            double fractionOP = component.getFractionForOperonDetection();
            int bGThreshold_NT = component.getBgThresholdSetManually_NT();
            int bGThreshold_OP = component.getBgThresholdSetManually_OP();
            int minLength_NT = component.getMinBoundaryForNovelRegionDetection();
            int ratio_NT = component.getIncreaseRatioValue();
        }
        catch( NumberFormatException e ) {
            throw new WizardValidationException( null, "Please check your textfields regarding string input.", null );
        }
        if( !this.component.isRPKM() && !this.component.isOperonDetection() && !this.component.isNewRegions() ) {
            throw new WizardValidationException( null, "Please selct at least one of the given analysis types.", null );
        }

        if( validateFractionInput() == false ) {
            throw new WizardValidationException( null, "Please give a fraction bigger 0.0 and smaller 1.0", null );
        }
    }


}
