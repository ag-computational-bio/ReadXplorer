
package de.cebitec.readxplorer.transcriptomeanalyses.controller;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.transcriptomeanalyses.chartGeneration.ChartsGenerationSelectChatTypeWizardPanel;
import de.cebitec.readxplorer.transcriptomeanalyses.chartGeneration.PlotGenerator;
import de.cebitec.readxplorer.transcriptomeanalyses.chartGeneration.SouthPanel;
import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.TranscriptionStart;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.ChartType;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.ElementsOfInterest;
import de.cebitec.readxplorer.transcriptomeanalyses.enums.PurposeEnum;
import de.cebitec.readxplorer.transcriptomeanalyses.main.ParameterSetFiveEnrichedAnalyses;
import de.cebitec.readxplorer.transcriptomeanalyses.main.TSSDetectionResults;
import de.cebitec.readxplorer.transcriptomeanalyses.motifSearch.MultiPurposeTopComponent;
import de.cebitec.readxplorer.transcriptomeanalyses.motifSearch.RbsAnalysisWizardIterator;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.WizardDescriptor;


/**
 * Controller class for all visualizations in this module.
 *
 * @author jritter
 */
public class VisualizationListener implements ActionListener {

    WizardDescriptor wiz;
    List<TranscriptionStart> currentTss;
    private ElementsOfInterest elements = null;
    boolean isAbsoluteFrequencyPlot, isBaseDistributionPlot, isPieChart;
    boolean isBining;
    int lengthRelToTls, biningSize;
    boolean isGaToCtSelected, isGcToAtSelected;
    private final PersistentReference persistentReference;
    private MultiPurposeTopComponent topComponent;
    private TSSDetectionResults tssResult;
    ParameterSetFiveEnrichedAnalyses params;


    /**
     * Constructor.
     *
     * @param reference  PersistentReference
     * @param wiz        WizardDescriptor
     * @param currentTss list of current transcription start sites
     * @param tssResult  instance of TSSDetectionResults
     */
    public VisualizationListener( PersistentReference reference, WizardDescriptor wiz, List<TranscriptionStart> currentTss, TSSDetectionResults tssResult ) {
        this.persistentReference = reference;
        this.wiz = wiz;
        this.currentTss = currentTss;
        this.params = (ParameterSetFiveEnrichedAnalyses) tssResult.getParameters();
    }


    @Override
    public void actionPerformed( ActionEvent e ) {
        String command = e.getActionCommand();
        if( command.equals( ChartType.WIZARD.toString() ) ) {

            boolean takeAllElements = (boolean) wiz.getProperty( ElementsOfInterest.ALL.toString() );
            boolean takeOnlyLeaderless = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS.toString() );
            boolean takeOnlyAntisense = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_ANTISENSE_TSS.toString() );
            boolean takeOnlyRealTss = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS.toString() );
            boolean takeOnlySelectedElements = (boolean) wiz.getProperty( ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES.toString() );

            isAbsoluteFrequencyPlot = (boolean) wiz.getProperty( ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString() );
            isBaseDistributionPlot = (boolean) wiz.getProperty( ChartType.BASE_DISTRIBUTION.toString() );
            isBining = (boolean) wiz.getProperty( ChartsGenerationSelectChatTypeWizardPanel.CHARTS_BINING );
            isGaToCtSelected = (boolean) wiz.getProperty( ChartType.CHARTS_BASE_DIST_GA_CT.toString() );
            isGcToAtSelected = (boolean) wiz.getProperty( ChartType.CHARTS_BASE_DIST_GC_AT.toString() );
            biningSize = (int) wiz.getProperty( ChartsGenerationSelectChatTypeWizardPanel.CHARTS_BINING_SIZE );
            lengthRelToTls = (int) wiz.getProperty( RbsAnalysisWizardIterator.PROP_RBS_ANALYSIS_REGION_LENGTH );

            if( takeAllElements ) {
                elements = ElementsOfInterest.ALL;
            }
            else if( takeOnlyLeaderless ) {
                elements = ElementsOfInterest.ONLY_LEADERLESS_TRANSCRIPTS;
            }
            else if( takeOnlyAntisense ) {
                elements = ElementsOfInterest.ONLY_ANTISENSE_TSS;
            }
            else if( takeOnlyRealTss ) {
                elements = ElementsOfInterest.ONLY_TSS_WITH_UTR_EXCEPT_AS_LEADERLESS;
            }
            else if( takeOnlySelectedElements ) {
                elements = ElementsOfInterest.ONLY_SELECTED_FOR_UPSTREAM_ANALYSES;
            }
        }
        if( command.equals( ChartType.ABSOLUTE_FREQUENCY_OF_5_PRIME_UTRs.toString() ) ) {
            topComponent = new MultiPurposeTopComponent( PurposeEnum.CHARTS );
            topComponent.setLayout( new BorderLayout() );
            topComponent.open();
            topComponent.setName( "Distribution of 5′-UTR lengths" );
            Thread plotGeneration = new Thread( new Runnable() {
                @Override
                public void run() {
                    PlotGenerator gen = new PlotGenerator( persistentReference );
                    List<DataTable> dataList = gen.prepareDataForUtrDistr( elements, currentTss, params, isBining, biningSize );
                    InteractivePanel panel = gen.generateBarPlot( dataList.get( 0 ), "5′-UTR lenght (distance between TSS and TLS)", "Absolute frequency" );
                    topComponent.add( panel, BorderLayout.CENTER );
                    topComponent.add( new SouthPanel( gen.getAbsFreqOf5PrimeUtrsInCSV() ), BorderLayout.SOUTH );
                }


            } );
            plotGeneration.start();
        }
        else if( command.equals( ChartType.BASE_DISTRIBUTION.toString() ) ) {

            if( isGaToCtSelected ) {
                final MultiPurposeTopComponent topComponentGA = new MultiPurposeTopComponent( PurposeEnum.CHARTS );
                topComponentGA.setLayout( new BorderLayout() );
                topComponentGA.open();
                topComponentGA.setName( "GA content distribution" );
                Thread plotGeneration = new Thread( new Runnable() {
                    @Override
                    public void run() {
                        PlotGenerator gen = new PlotGenerator( persistentReference );
                        List<DataTable> dataList = gen.prepareData( ChartType.BASE_DISTRIBUTION, ChartType.CHARTS_BASE_DIST_GA_CT,
                                                                    elements, currentTss, params, lengthRelToTls );
                        InteractivePanel panel = gen.generateOverlappedAreaPlot( ChartType.CHARTS_BASE_DIST_GA_CT, dataList.get( 0 ), dataList.get( 1 ), "upstream position relative to start codon (nt)", "purine/pyrimidine distribution (relative abundance)" );
                        topComponentGA.add( panel, BorderLayout.CENTER );
//                        topComponent.add(new SouthPanel(null), BorderLayout.SOUTH);
                    }


                } );
                plotGeneration.start();
            }

            if( isGcToAtSelected ) {
                final MultiPurposeTopComponent topComponentGC = new MultiPurposeTopComponent( PurposeEnum.CHARTS );
                topComponentGC.setLayout( new BorderLayout() );
                topComponentGC.open();
                topComponentGC.setName( "GC content distribution" );
                Thread plotGeneration = new Thread( new Runnable() {
                    @Override
                    public void run() {
                        PlotGenerator gen = new PlotGenerator( persistentReference );
                        List<DataTable> dataList = gen.prepareData( ChartType.BASE_DISTRIBUTION, ChartType.CHARTS_BASE_DIST_GC_AT,
                                                                    elements, currentTss, params, lengthRelToTls );
                        InteractivePanel panel = gen.generateOverlappedAreaPlot( ChartType.CHARTS_BASE_DIST_GC_AT, dataList.get( 0 ), dataList.get( 1 ), "upstream position relative to start codon (nt)", "purine/pyrimidine distribution (relative abundance)" );
                        topComponentGC.add( panel, BorderLayout.CENTER );
//                        topComponent.add(new SouthPanel(null), BorderLayout.SOUTH);
                    }


                } );
                plotGeneration.start();
            }
        }
    }


    /**
     *
     * @return <true> if absolute frequency plot is selected else <false>
     */
    public boolean isAbsoluteFrequencyPlotSelected() {
        return isAbsoluteFrequencyPlot;
    }


    /**
     * @return <true> if base distribution plot is selected else <false>
     *
     */
    public boolean isBaseDistributionPlotSelected() {
        return isBaseDistributionPlot;
    }


    /**
     *
     * @return length for region of interest
     */
    public int getLengthRelToTls() {
        return lengthRelToTls;
    }


    /**
     *
     * @return <true> if pie chart representation is selected else <false>
     */
    public boolean isPeiChartSelected() {
        return isPieChart;
    }


}
