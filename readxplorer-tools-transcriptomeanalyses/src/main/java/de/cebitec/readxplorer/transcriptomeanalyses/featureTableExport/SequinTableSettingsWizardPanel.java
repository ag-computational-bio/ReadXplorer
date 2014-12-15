/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.featureTableExport;


import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;


public class SequinTableSettingsWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor> {

    public static final String SEQUIN_EXPORT_FEATURE_NAME = "feature name for sequin table export";
    public static final String SEQUIN_EXPORT_SEPARATOR = "separator";
    public static final String SEQUIN_EXPORT_STRAIN_LENGTH = "length of prefix strain";
    public static final String SEQUIN_EXPORT_PARSING_LOCUS_TAG = "Parsing of the locus tag";


    public SequinTableSettingsWizardPanel( String wizardName ) {
        this.wizardName = wizardName;
    }


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SequinTableSettingsVisualPanel component;
    private final String wizardName;


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SequinTableSettingsVisualPanel getComponent() {
        if( component == null ) {
            component = new SequinTableSettingsVisualPanel( wizardName );
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
        boolean separatorCorrect = false;
        boolean strainAndSuffixLengthOk = false;
        boolean featureNameIsNotEmpty = false;
        // Check if Separator 1is a String.

        // Check if strain and suffix length is an Integer value
        // Check if Featurename is not empty string
        if( separatorCorrect && strainAndSuffixLengthOk && featureNameIsNotEmpty ) {
            return true;
        }
        else {
            return true;
        }
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
        wiz.putProperty( SEQUIN_EXPORT_FEATURE_NAME, this.component.getFeatureName() );
        wiz.putProperty( SEQUIN_EXPORT_PARSING_LOCUS_TAG, this.component.isLocusTagParsingSelected() );

        if( component.isLocusTagParsingSelected() ) {
            if( this.component.isSeparatorChoosen() ) {
                wiz.putProperty( SEQUIN_EXPORT_SEPARATOR, component.getSeparator() );
            }
            else {
                wiz.putProperty( SEQUIN_EXPORT_SEPARATOR, "" );
            }
            wiz.putProperty( SEQUIN_EXPORT_STRAIN_LENGTH, this.component.getStrainLength() );
//        wiz.putProperty(SEQUIN_EXPORT_SUFFIX_LABEL_LENGTH, this.component.getSuffixLabelLength());
        }
        storePrefs();
    }


    /**
     * Stores the selected read classes for this specific wizard for later use,
     * also after restarting the software.
     *
     * @param readClassParams The parameters to store
     */
    private void storePrefs() {
        Preferences pref = NbPreferences.forModule( Object.class );
        pref.put( wizardName + SEQUIN_EXPORT_FEATURE_NAME, component.getFeatureName() );
    }


}
