/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import de.cebitec.readXplorer.transcriptomeAnalyses.enums.ElementsOfInterest;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;


public class DataSelectionWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DataSelectionVisualPanel component;
    private final PurposeEnum purpose;


    public DataSelectionWizardPanel( PurposeEnum purpose ) {
        this.purpose = purpose;
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DataSelectionVisualPanel getComponent() {
        if( component == null ) {
            component = new DataSelectionVisualPanel( this.purpose );
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
        wiz.putProperty( ElementsOfInterest.ALL.toString(), component.isAllElements() );
        wiz.putProperty( ElementsOfInterest.ONLY_ANTISENSE_TSS.toString(), component.isOnlyAntisenseElements() );
        wiz.putProperty( ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS.toString(), component.isOnlyLeaderlessElements() );
        wiz.putProperty( ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS.toString(), component.isOnlyRealTSS() );
        wiz.putProperty( ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES.toString(), component.isOnlySelected() );
        wiz.putProperty( PromotorAnalysisWizardIterator.PROP_PROMOTOR_ANALYSIS_LENGTH_ALL_ELEMENTS, component.getLengthRelativeToTss() );
    }


    @Override
    public void validate() throws WizardValidationException {

        if( this.component.isAllElements() == false
            && this.component.isOnlyAntisenseElements() == false
            && this.component.isOnlyLeaderlessElements() == false
            && this.component.isOnlyRealTSS() == false
            && this.component.isOnlySelected() == false ) {
            throw new WizardValidationException( null, "Plese select one of the possible Element types!", null );
        }
    }


}
