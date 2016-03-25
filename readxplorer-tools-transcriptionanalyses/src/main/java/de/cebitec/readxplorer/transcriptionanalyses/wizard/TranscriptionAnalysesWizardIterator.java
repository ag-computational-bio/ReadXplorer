/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.transcriptionanalyses.wizard;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksWizardPanel;
import de.cebitec.readxplorer.ui.dialogmenus.SelectFeatureTypeWizardPanel;
import de.cebitec.readxplorer.ui.dialogmenus.SelectReadClassWizardPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;


/**
 * Wizard page iterator for a transcription analyses wizard.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class TranscriptionAnalysesWizardIterator implements
        WizardDescriptor.Iterator<WizardDescriptor> {

    public static final String PROP_TSS_ANALYSIS = "tssAnalysis";
    public static final String PROP_OPERON_ANALYSIS = "Operon";
    public static final String PROP_NORM_ANALYSIS = "Normalization";
    public static final String PROP_UNANNOTATED_TRANSCRIPT_DET = "unannotatedTranscriptDetection";
    public static final String PROP_AUTO_TSS_PARAMS = "automaticsTSSParameterEstimation";
    public static final String PROP_AUTO_OPERON_PARAMS = "automaticOperonParameterEstimation";
    public static final String PROP_MIN_TOTAL_INCREASE = "minTotalIncrease";
    public static final String PROP_MIN_PERCENT_INCREASE = "minTotalPercentIncrease";
    public static final String PROP_MAX_LOW_COV_INIT_COUNT = "maxLowCovInitialCount";
    public static final String PROP_MIN_LOW_COV_INC = "minLowCovIncrease";
    public static final String PROP_MIN_TRANSCRIPT_EXTENSION_COV = "minTranscriptExtensionCov";
    public static final String PROP_MAX_LEADERLESS_DISTANCE = "maxLeaderlessDistance";
    public static final String PROP_MAX_FEATURE_DISTANCE = "maxFeatureDistance";
    public static final String PROP_MIN_NUMBER_READS = "minNumberReads";
    public static final String PROP_MAX_NUMBER_READS = "maxNumberReads";
    public static final String PROP_USE_EFFECTIVE_LENGTH = "useEffectiveLength";
    public static final String PROP_MIN_SPANNING_READS = "minNumberSpanningReads";
    public static final String PROP_ANALYSIS_DIRECTION = "analysisDirection";
    public static final String PROP_ASSOCIATE_TSS_WINDOW = "associateTssWindow";
    public static final String PROP_IS_ASSOCIATE_TSS = "isAssociateTss";

    static final String PROP_WIZARD_NAME = "TransAnalyses";
    private static final String FINISH_MSG = "Press 'Finish' to start";

    private int index;
    private final ChangeSupport changeSupport;
    private WizardDescriptor wiz;
    private String[] steps;
    private final int referenceId;

    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;

    private TransAnalysesSelectionWizardPanel selectionPanel = new TransAnalysesSelectionWizardPanel();
    private TransAnalysesTSSWizardPanel tSSPanel = new TransAnalysesTSSWizardPanel();
    private TransAnalysesOperonWizardPanel operonPanel = new TransAnalysesOperonWizardPanel();
    private TransAnalysesNormWizardPanel normalizationPanel = new TransAnalysesNormWizardPanel();
    private OpenTracksWizardPanel openTracksPanel;
    private SelectReadClassWizardPanel readClassPanel;
    private SelectFeatureTypeWizardPanel featTypeNormPanel;
    private SelectFeatureTypeWizardPanel featTypeOperonPanel;

    private final Map<WizardDescriptor.Panel<WizardDescriptor>, Integer> panelToStepMap = new HashMap<>();


    /**
     * Wizard page iterator for a transcription analyses wizard. In order to use
     * it correctly, the wizard, in which this iterator is used has to be set.
     * @param referenceId The id of the current reference
     */
    public TranscriptionAnalysesWizardIterator( int referenceId ) {
        this.referenceId = referenceId;
        this.changeSupport = new ChangeSupport( this );
        this.initializePanels();
    }


    /**
     * @return the sequence of all wizard panels of this wizard
     */
    private List<WizardDescriptor.Panel<WizardDescriptor>> initializePanels() {
        if( allPanels == null ) {
            allPanels = new ArrayList<>();

            openTracksPanel = new OpenTracksWizardPanel( PROP_WIZARD_NAME, referenceId );
            selectionPanel = new TransAnalysesSelectionWizardPanel();
            readClassPanel = new SelectReadClassWizardPanel( PROP_WIZARD_NAME, true );
            tSSPanel = new TransAnalysesTSSWizardPanel();
            operonPanel = new TransAnalysesOperonWizardPanel();
            normalizationPanel = new TransAnalysesNormWizardPanel();
            featTypeNormPanel = new SelectFeatureTypeWizardPanel( PROP_NORM_ANALYSIS, true );
            featTypeOperonPanel = new SelectFeatureTypeWizardPanel( PROP_OPERON_ANALYSIS, false );
            featTypeOperonPanel.getComponent().showDisplayName( true );
            featTypeNormPanel.getComponent().showDisplayName( true );

            allPanels.add( openTracksPanel );
            allPanels.add( selectionPanel );
            allPanels.add( readClassPanel );
            allPanels.add( tSSPanel );
            allPanels.add( operonPanel );
            allPanels.add( featTypeOperonPanel );
            allPanels.add( normalizationPanel );
            allPanels.add( featTypeNormPanel );

            this.panelToStepMap.put( openTracksPanel, 0 );
            this.panelToStepMap.put( selectionPanel, 1 );
            this.panelToStepMap.put( readClassPanel, 2 );
            this.panelToStepMap.put( tSSPanel, 3 );
            this.panelToStepMap.put( operonPanel, 4 );
            this.panelToStepMap.put( featTypeOperonPanel, 5 );
            this.panelToStepMap.put( normalizationPanel, 6 );
            this.panelToStepMap.put( featTypeNormPanel, 7 );

            this.steps = new String[allPanels.size() + 1];
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
            steps[steps.length - 1] = FINISH_MSG;

            String[] initiallyShownSteps = new String[]{ steps[0], steps[1], "...", steps[steps.length - 1] };
            openTracksPanel.getComponent().putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, initiallyShownSteps );

            currentPanels = new ArrayList<>();
            currentPanels.add( openTracksPanel );
            currentPanels.add( selectionPanel );
            currentPanels.add( readClassPanel );
        }
        return allPanels;
    }


    /**
     * @return the current wizard panel
     */
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return currentPanels.get( index );
    }


    @Override
    public String name() {
        return index + 1 + ". from " + currentPanels.size();
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
        if( index == 1 ) {
            this.updatePanelList( selectionPanel.getComponent().isTSSAnalysisSelected(), tSSPanel );
            this.updatePanelList( selectionPanel.getComponent().isOperonAnalysisSelected(), operonPanel );
            this.updatePanelList( selectionPanel.getComponent().isOperonAnalysisSelected(), featTypeOperonPanel );
            this.updatePanelList( selectionPanel.getComponent().isNormAnalysisSelected(), normalizationPanel );
            this.updatePanelList( selectionPanel.getComponent().isNormAnalysisSelected(), featTypeNormPanel );

            String[] newStepArray = new String[0];
            List<String> newSteps = new ArrayList<>();
            for( Panel<WizardDescriptor> panel : currentPanels ) {
                newSteps.add( this.steps[this.panelToStepMap.get( panel )] );
            }
            newSteps.add( FINISH_MSG );
            wiz.putProperty( WizardDescriptor.PROP_CONTENT_DATA, newSteps.toArray( newStepArray ) );
        }

        if( !hasNext() ) {
            throw new NoSuchElementException();
        }
        ++index;
        wiz.putProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index );
    }


    @Override
    public void previousPanel() {
        if( !hasPrevious() ) {
            throw new NoSuchElementException();
        }
        --index;
        wiz.putProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index );
    }


    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener( ChangeListener l ) {
        changeSupport.addChangeListener( l );
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
        changeSupport.removeChangeListener( l );
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    /**
     * @param wiz the wizard, in which this wizard iterator is contained. If it
     *            is not set, no properties can be stored, thus it always has to
     *            be set.
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
     * Updates the wizard's panel list with the given analysis panel and it's
     * steps with the newStep string, if the given analysis is selected and the
     * analysis panel is not already contained in the wizard panel list.
     * <p>
     * @param analysisSelected true, if the analysis is selected, false
     *                         otherwise
     * @param analysisPanel    the analysis panel to add to the list of panels
     */
    private void updatePanelList( boolean analysisSelected, Panel<WizardDescriptor> analysisPanel ) {
        if( analysisSelected ) {
            if( !currentPanels.contains( analysisPanel ) ) {
                currentPanels.add( analysisPanel );
            }
        } else if( currentPanels.contains( analysisPanel ) ) {
            currentPanels.remove( analysisPanel );
        }
    }


    /**
     * @return The dynamically generated property name for the read class
     *         selection for this wizard. Can be used to obtain the
     *         corresponding read class parameters.
     */
    public String getReadClassPropForWiz() {
        return readClassPanel.getPropReadClassParams();
    }


    /**
     * @return The property string for the selected feature type list for the
     *         corresponding read count normalization analysis.
     */
    public String getPropSelectedNormFeatTypes() {
        return featTypeNormPanel.getPropSelectedFeatTypes();
    }


    /**
     * @return The property string for the feature start offset configured for
     *         the read count normalization analysis.
     */
    public String getPropNormFeatureStartOffset() {
        return featTypeNormPanel.getPropFeatureStartOffset();
    }


    /**
     * @return The property string for the feature stop offset configured for
     *         the read count normalization analysis.
     */
    public String getPropNormFeatureStopOffset() {
        return featTypeNormPanel.getPropFeatureStopOffset();
    }


    /**
     * @return The property string for the selected feature type list for the
     *         corresponding operon detection.
     */
    public String getPropSelectedOperonFeatTypes() {
        return featTypeOperonPanel.getPropSelectedFeatTypes();
    }


    /**
     * @return The dynamically generated property name for the combine tracks
     *         selection for this wizard. Can be used to obtain the
     *         corresponding boolean if the tracks shall be combined.
     */
    public String getCombineTracksPropForWiz() {
        return openTracksPanel.getPropCombineTracks();
    }


    /**
     * @return The list of track selected in this wizard.
     */
    public List<PersistentTrack> getSelectedTracks() {
        return openTracksPanel.getComponent().getSelectedTracks();
    }


}
