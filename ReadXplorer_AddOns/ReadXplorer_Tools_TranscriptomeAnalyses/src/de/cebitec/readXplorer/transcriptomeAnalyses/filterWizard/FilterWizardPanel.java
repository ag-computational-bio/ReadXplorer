/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard;


import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;


public class FilterWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor> {

    public static final String PROP_FILTER_ONLY_TAGGED_FOR_UPSTREAM_ANALYSIS = "only tagged for upstream analysis";
    public static final String PROP_FILTER_ONLY_INTRAGENIC = "only intragenic TSS";
    public static final String PROP_FILTER_ONLY_TAGGED_AS_FINISH = "only tagged as finish";
    public static final String PROP_FILTER_ONLY_INTERGENIC = "only intergenic TSS";
    public static final String PROP_FILTER_ONLY_ANTISENSE = "only antisense TSS";
    public static final String PROP_FILTER_ONLY_LEADERLESS = "only leaderless TSS";
    public static final String PROP_FILTER_COMPLEMENT = "filter complement";
    public static final String PROP_FILTER_FALSE_POSITIVE = "only false positives";
    public static final String PROP_FILTER_STABLE_RNA = "only stable rna";
    public static final String PROP_FILTER_FIVE_PRIME_UTR_ANTISENSE = "only 5'-UTR antisense";
    public static final String PROP_FILTER_THREE_PRIME_UTR_ANTISENSE = "only 3'-UTR antisense";
    public static final String PROP_FILTER_INTRAGENIC_ANTISENSE = "only intragenic antisense";
    public static final String PROP_FILTER_ALL_NON_STABLE_RNA = "only elements assigned to non stable RNA features";

    private final String wizardName;
    public static final String PROP_FILTER_FOR_READSTARTS = "filter for read starts";
    public static final String PROP_FILTER_READSTARTS = "read starts";
    public static final String PROP_FILTER_FOR_SINGLE_TSS = "filter for single TSS";
    public static final String PROP_FILTER_FOR_MULTIPLE_TSS_WITH_SHIFTS = "filter for multiple shifts in TSS positions";
    public static final String PROP_FILTER_WITH_MIN_SHIFT = "shift";
    public static final String PROP_FILTER_FOR_MULTIPLE_TSS = "filter for multiple TSS";

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FilterVisualPanel component;


    public FilterWizardPanel( String wizardName ) {
        this.wizardName = wizardName;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public FilterVisualPanel getComponent() {
        if( component == null ) {
            component = new FilterVisualPanel( wizardName );
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
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_FOR_MULTIPLE_TSS, component.isMultipleSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_FOR_READSTARTS, component.isExtractionOfTSSWithAtLeastRSSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_FOR_SINGLE_TSS, component.isSingleSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_READSTARTS, component.getAtLeastReadStarts() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_TAGGED_FOR_UPSTREAM_ANALYSIS, component.isOnlyUpstreamRegions() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_TAGGED_AS_FINISH, component.isOnlyFinished() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_INTRAGENIC, component.isOnlyIntragenic() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_INTERGENIC, component.isOnlyIntergenic() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_ANTISENSE, component.isOnlyPutativeAntisense() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ONLY_LEADERLESS, component.isOnlyLeaderless() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_FALSE_POSITIVE, component.isFalsePositive() );

        wiz.putProperty( FilterWizardPanel.PROP_FILTER_INTRAGENIC_ANTISENSE, component.isIntragenicAntisenseSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_THREE_PRIME_UTR_ANTISENSE, component.isThreePrimeAntisenseSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_FIVE_PRIME_UTR_ANTISENSE, component.isFivePrimeUtrAntisenseSelected() );

        wiz.putProperty( FilterWizardPanel.PROP_FILTER_STABLE_RNA, component.isStableRnaSelected() );
        wiz.putProperty( FilterWizardPanel.PROP_FILTER_ALL_NON_STABLE_RNA, component.isOnlyNonStableRnaSelected() );
        storePrefs();
    }


    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( wizardName + FilterWizardPanel.PROP_FILTER_READSTARTS, component.getAtLeastReadStarts().toString() );
    }


}
