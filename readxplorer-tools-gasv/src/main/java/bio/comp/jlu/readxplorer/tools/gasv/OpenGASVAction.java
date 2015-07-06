/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv;

import bio.comp.jlu.readxplorer.tools.gasv.wizard.BamToGASVWizardPanel;
import bio.comp.jlu.readxplorer.tools.gasv.wizard.GASVMainWizardPanel;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.tables.CsvTableParser;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksWizardPanel;
import de.cebitec.readxplorer.ui.tablevisualization.PosTablePanel;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.ui.visualisation.TableVisualizationHelper;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import org.supercsv.prefs.CsvPreference;

import static de.cebitec.readxplorer.parser.tables.TableType.GASV_TABLE;


/**
 * Action for opening a genome rearrangement detection using the integrated
 * version of GASV. It opens a track list containing all tracks of the selected
 * reference and creates a new GASV configuration setup wizard, runs the
 * analysis and opens the result TopComponent.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(
         category = "Tools",
         id = "bio.comp.jlu.readxplorer.tools.gasv.OpenGASVAction"
)
@ActionRegistration(
         iconBase = "bio/comp/jlu/readxplorer/tools/gasv/gasv.png",
         displayName = "#CTL_OpenGASVAction"
)
@ActionReferences( {
    @ActionReference( path = "Menu/Tools", position = 225 ),
    @ActionReference( path = "Toolbars/Tools", position = 150 )
} )
@Messages( "CTL_OpenGASVAction=Run GASV" )
public final class OpenGASVAction implements ActionListener, DataVisualisationI {

    private static final Logger LOG = Logger.getLogger( OpenGASVAction.class.getName() );
    private static final String PROP_WIZARD_NAME = "GASV_Wizard";

    private final ReferenceViewer context;
    private GASVTopComponent gasvTopComp;
    private ProgressHandle progressHandle;

    private final PersistentReference reference;
    private List<PersistentTrack> tracks;
    private Map<Integer, PersistentTrack> trackMap;

    private Map<Integer, GASVCaller> trackToAnalysisMap;
    private WizardDescriptor wiz;
    private OpenTracksWizardPanel openTracksPanel;
    private File trackFile;


    /**
     * Action for opening a genome rearrangement detection using the integrated
     * version of GASV. It opens a track list containing all tracks of the
     * selected reference and creates a new GASV configuration setup wizard,
     * runs the analysis and opens the result TopComponent.
     * <p>
     * @param context The ReferenceViewer for which the analysis is carried out.
     */
    public OpenGASVAction( ReferenceViewer context ) {
        this.context = context;
        reference = context.getReference();
    }


    /**
     * Carries out the calculations for a complete genome rearrangement
     * detection using GASV + opening the corresponding TopComponent.
     * <p>
     * @param ev the event itself, which is not used currently
     */
    @Override
    public void actionPerformed( ActionEvent ev ) {

        trackToAnalysisMap = new HashMap<>();

        boolean cancelled = runWizard();
        if( !cancelled ) {
            runAnalysis();
        }
    }


    /**
     * Initializes the setup wizard for the genome rearrangement detection using
     * GASV.
     * <p>
     * @return <code>true</code>, if the wizard has been finished successfully,
     *         <code>false</code> otherwise
     */
    @Messages( { "TTL_GASVWizardTitle=GASV Genome Rearrangement Parameter Wizard" } )
    private boolean runWizard() {
        //Open track selection & option wizard.
        @SuppressWarnings( "unchecked" )
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        openTracksPanel = new OpenTracksWizardPanel( PROP_WIZARD_NAME, reference.getId() );

        panels.add( openTracksPanel );
        panels.add( new BamToGASVWizardPanel() );
        panels.add( new GASVMainWizardPanel() );

        wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );

        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( Bundle.TTL_GASVWizardTitle() );

        //action to perform after successfully finishing the wizard
        return DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
    }


    /**
     * Starts the genome rearrangement detection using GASV.
     */
    @Messages( { "TITLE_GASVTopComp=GASV Genome Rearrangements Window",
                 "HINT_GASVTopComp=This is a GASV Genome Rearrangements window" } )
    private void runAnalysis() {

        ParametersBamToGASV bamToGASVParams = (ParametersBamToGASV) wiz.getProperty( BamToGASVWizardPanel.PROP_BAM_TO_GASV_PARAMS );
        ParametersGASVMain gasvMainParams = (ParametersGASVMain) wiz.getProperty( GASVMainWizardPanel.PROP_GASV_MAIN_PARAMS );

        List<PersistentTrack> selectedTracks = openTracksPanel.getComponent().getSelectedTracks();
        if( !selectedTracks.isEmpty() ) {
            tracks = selectedTracks;
            trackMap = ProjectConnector.getTrackMap( tracks );

            if( gasvTopComp == null ) {
                gasvTopComp = (GASVTopComponent) WindowManager.getDefault().findTopComponent( "GASVTopComponent" );
            }
            gasvTopComp.open();

            //Afterwards run GASV with:
            for( PersistentTrack track : tracks ) {
                TrackConnector connector;
                try {
                    connector = (new SaveFileFetcherForGUI()).getTrackConnector( track );
                } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                    continue;
                }

                createAnalysis( connector, bamToGASVParams, gasvMainParams );
            }
        }
    }


    /**
     * Creates the actual analysis objects and runs them for a single track.
     * <p>
     * @param connector       The track connector for which the analysis shall
     *                        be run
     * @param bamToGASVParams BamToGASV parameter set to apply
     * @param gasvMainParams  GASVMain parameter set to apply
     */
    @NbBundle.Messages( { "ActionProgressName=Genome Rearrangements are calculated with GASV..." } )
    private void createAnalysis( TrackConnector connector, ParametersBamToGASV bamToGASVParams, ParametersGASVMain gasvMainParams ) {
        progressHandle = ProgressHandleFactory.createHandle( Bundle.ActionProgressName() );
        progressHandle.start();
        GASVCaller gasvCaller = new GASVCaller( reference, connector, bamToGASVParams, gasvMainParams, this, progressHandle );
        Thread thread = new Thread( gasvCaller );
        thread.start();
    }


    /**
     * Visualizes the result from GASV in a table.
     * <p>
     * @param data A message indicating that GASV has finished
     */
    @Override
    public void showData( Object data ) {

        if( data instanceof String && "done".equals( data ) ) {
            File tableFile = new File( trackFile.getAbsolutePath() + ".gasv.in.clusters" );
            if( tableFile.exists() && tableFile.canRead() ) {
                CsvTableParser csvParser = new CsvTableParser();
                csvParser.setAutoDelimiter( false );
                csvParser.setCsvPref( CsvPreference.TAB_PREFERENCE );
                csvParser.setTableModel( GASV_TABLE );
                List<List<?>> tableData;
                try {
                    tableData = csvParser.parseTable( tableFile );
                    if( tableData.size() > 1 ) { //there is more content than just the header
                        tableData = GASVUtils.editGASVResultTable( tableData );
                        final UneditableTableModel tableModel = TableUtils.transformDataToTableModel( tableData );

                        //open table visualization panel with given reference for jumping to the position
                        SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                            @Override
                            public void run() {
                                PosTablePanel tablePanel = new PosTablePanel( tableModel, GASV_TABLE );
                                tablePanel.setReferenceGenome( reference );
                                TableVisualizationHelper.checkAndOpenRefViewer( reference, tablePanel );

                                String panelName = "Imported table from: " + tableFile.getName();
                                gasvTopComp.openAnalysisTab( panelName, tablePanel );
                            }


                        } );
                    } else {
                        JOptionPane.showMessageDialog( gasvTopComp,
                                                       "No rearrangements have been detected by GASV for this data set: " +
                                                       trackFile.getAbsolutePath(), "No rearrangements detected", JOptionPane.INFORMATION_MESSAGE );
                    }
                } catch( ParsingException ex ) {
                    GASVCaller.IO.getOut().println( "An error occurred during parsing of the mapping data:\\n" + ex.getMessage() );
                    LOG.log( Level.SEVERE, "An error occurred during parsing of the mapping data:\\n{0}", ex.getMessage() );
                }

            }
        }
        progressHandle.finish();
    }


}
