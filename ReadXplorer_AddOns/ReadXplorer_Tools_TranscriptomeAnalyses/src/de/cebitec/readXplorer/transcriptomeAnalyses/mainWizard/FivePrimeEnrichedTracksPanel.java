package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class FivePrimeEnrichedTracksPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FivePrimeEnrichedTracksVisualPanel component;
    private final String wizardName;

    public FivePrimeEnrichedTracksPanel(String wizardName) {
        this.wizardName = wizardName;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FivePrimeEnrichedTracksVisualPanel getComponent() {
        if (component == null) {
            component = new FivePrimeEnrichedTracksVisualPanel(wizardName);
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
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state

        wiz.putProperty(WizardPropertyStrings.PROP_SET_MANAULLY_MIN_STACK_SIZE, component.isMinStackSizeSetManually());
        wiz.putProperty(WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE, component.getMinStackSizeSetManually());
        wiz.putProperty(WizardPropertyStrings.PROP_Fraction, component.getFraction());
        wiz.putProperty(WizardPropertyStrings.PROP_RATIO, component.getRatio());
        wiz.putProperty(WizardPropertyStrings.PROP_EXCLUDE_INTERNAL_TSS, component.isExclusionOfAllIntragenicTss());
        wiz.putProperty(WizardPropertyStrings.PROP_UTR_LIMIT, component.getUtrLimitationDistance());
        wiz.putProperty(WizardPropertyStrings.PROP_KEEP_ALL_INTRAGENIC_TSS, component.isKeepAllIntragenicTss());
        wiz.putProperty(WizardPropertyStrings.PROP_KEEP_ONLY_ITRAGENIC_TSS_ASSIGNED_TO_FEATURE, component.isKeepOnlyIntragenicTssAssignedToFeature());
        if (component.isKeepAllIntragenicTss()) {
            wiz.putProperty(WizardPropertyStrings.PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT, component.getKeepingAllInragenicTssDistance());
        } else if (component.isKeepOnlyIntragenicTssAssignedToFeature()) {
            wiz.putProperty(WizardPropertyStrings.PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT, component.getKeepingInternalTssDistance());
        } else {
            wiz.putProperty(WizardPropertyStrings.PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT, 0);
        }
        wiz.putProperty(WizardPropertyStrings.PROP_LEADERLESS_LIMIT, component.getLeaderlessDistance());
        wiz.putProperty(WizardPropertyStrings.PROP_PERCENTAGE_FOR_CDS_ANALYSIS, component.getPercentageForCdsShiftAnalysis());

        wiz.putProperty(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS, component.isIncludingBestMathcedReads());
        wiz.putProperty(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION, component.getMaxDistFor3PrimeUtrAntisenseDetection());
        wiz.putProperty(WizardPropertyStrings.PROP_VALID_START_CODONS, component.getValidStartCodonSet());
        wiz.putProperty(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT, component.getExcludedFeatreTypes());
        storePrefs();
    }

    private void storePrefs() {
        Preferences pref = NbPreferences.forModule(Object.class);
        pref.put(wizardName + WizardPropertyStrings.PROP_MANAULLY_MIN_STACK_SIZE, component.getMinStackSizeSetManually().toString());
        pref.put(wizardName + WizardPropertyStrings.PROP_Fraction, component.getFraction().toString());
        pref.put(wizardName + WizardPropertyStrings.PROP_RATIO, component.getRatio().toString());
        pref.putBoolean(wizardName + WizardPropertyStrings.PROP_EXCLUDE_INTERNAL_TSS, component.isExclusionOfAllIntragenicTss());
        pref.putBoolean(wizardName + WizardPropertyStrings.PROP_KEEP_ALL_INTRAGENIC_TSS, component.isKeepAllIntragenicTss());
        pref.put(wizardName + WizardPropertyStrings.PROP_UTR_LIMIT, component.getUtrLimitationDistance().toString());
        pref.put(wizardName + WizardPropertyStrings.PROP_KEEP_ITRAGENIC_DISTANCE_LIMIT, component.getKeepingInternalTssDistance().toString());
        pref.put(wizardName + WizardPropertyStrings.PROP_LEADERLESS_LIMIT, component.getLeaderlessDistance().toString());
        pref.put(wizardName + WizardPropertyStrings.PROP_PERCENTAGE_FOR_CDS_ANALYSIS, component.getPercentageForCdsShiftAnalysis().toString());
        pref.putBoolean(wizardName + WizardPropertyStrings.PROP_PERCENTAGE_FOR_CDS_ANALYSIS, component.isIncludingBestMathcedReads());
        pref.put(wizardName + WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION, component.getMaxDistFor3PrimeUtrAntisenseDetection().toString());
    }

    @Override
    public void validate() throws WizardValidationException {
        double fraction;
        int ratio;
        int intragencLimitDistance;
        int percentageForCdsShiftDetection;
        int threePrimeUtrAsDetection;
        int leaderlessDistance;
        try {
            fraction = component.getFraction();
            ratio = component.getRatio();
            intragencLimitDistance = component.getKeepingInternalTssDistance();
            percentageForCdsShiftDetection = component.getPercentageForCdsShiftAnalysis();
            threePrimeUtrAsDetection = component.getMaxDistFor3PrimeUtrAntisenseDetection();
            leaderlessDistance = component.getLeaderlessDistance();
        } catch (NumberFormatException nfe) {
            throw new WizardValidationException(null, "Please check your textfields regarding string input.", null);
        }

        if (fraction < 0.0 || fraction > 1.0) {
            throw new WizardValidationException(null, "Please choose a fraction 0.0 < fraction < 1.0.", null);
        } else if (ratio < 0) {
            throw new WizardValidationException(null, "Please choose a ratio > 0.", null);
        } else if (intragencLimitDistance <= 0) {
            throw new WizardValidationException(null, "Please choose a keeping distance rel. to TLS > 0.", null);
        } else if (percentageForCdsShiftDetection < 0 || percentageForCdsShiftDetection > 100) {
            throw new WizardValidationException(null, "Please check your CDS-shift parameter.", null);
        } else if (!component.isExclusionOfAllIntragenicTss() && !component.isKeepOnlyIntragenicTssAssignedToFeature() && !component.isKeepAllIntragenicTss()) {
            throw new WizardValidationException(null, "Please check your settings regarding dealing with intragenic tss.", null);
        } else if (percentageForCdsShiftDetection < 0 || leaderlessDistance < 0
                || component.getUtrLimitationDistance() < 0 || component.getKeepingAllInragenicTssDistance() < 0 || component.getKeepingInternalTssDistance() < 0
                || ratio < 0 || fraction < 0.0 || threePrimeUtrAsDetection < 0) {
            throw new WizardValidationException(null, "Please check your settings regarding negative input.", null);
        }

    }
}
