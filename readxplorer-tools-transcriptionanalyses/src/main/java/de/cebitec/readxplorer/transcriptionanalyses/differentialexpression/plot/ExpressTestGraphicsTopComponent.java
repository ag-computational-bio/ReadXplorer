/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot;


import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.plotting.ChartExporter;
import de.cebitec.readxplorer.plotting.CreatePlots;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.ResultDeAnalysis;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.fileChooser.ReadXplorerFileChooser;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.DefaultComboBoxModel;
import org.jfree.chart.ChartPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * TopComponent, which displays all graphics available for the express test.
 */
@ConvertAsProperties(
    dtd = "-//de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot//Plot//EN",
    autostore = false )
@TopComponent.Description(
    preferredID = "PlotTopComponent",
    //iconBase="SET/PATH/TO/ICON/HERE",
    persistenceType = TopComponent.PERSISTENCE_NEVER )
@TopComponent.Registration( mode = "editor", openAtStartup = false )
//@ActionID(category = "Tools", id = "de.cebitec.readxplorer.differentialExpression.plot.PlotTopComponent")
//@ActionReference(path = "Menu/Tools")
@ActionID( category = "Window", id = "de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot.PlotTopComponent" )
@ActionReference( path = "Menu/Window" )
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_PlotAction",
    preferredID = "PlotTopComponent" )
@Messages( {
    "CTL_PlotAction=Plot",
    "CTL_PlotTopComponent=Express Test Graphics",
    "HINT_PlotTopComponent=This is a differential expression plot window"
} )
public final class ExpressTestGraphicsTopComponent extends TopComponentExtended
        implements Observer {

    private static final long serialVersionUID = 1L;

    private DefaultComboBoxModel<PlotTypes> cbmPlotType = new DefaultComboBoxModel<>( PlotTypes.values() );
    private DefaultComboBoxModel<String> cbmDataSet;
    private DeAnalysisHandler analysisHandler;
    private List<ResultDeAnalysis> results;
    private DeAnalysisHandler.Tool usedTool;
    private ChartPanel chartPanel;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating plot" );
    private ProgressHandle svgExportProgressHandle;


    /**
     * TopComponent, which displays all graphics available for the express test.
     */
    public ExpressTestGraphicsTopComponent() {
        cbmDataSet = new DefaultComboBoxModel<>();
        initComponents();
        initAdditionalComponents();
        ChartPanel panel = CreatePlots.createInfPlot( createSamplePoints( 500 ), "X", "Y", new ToolTip() );
        plotPanel.add( panel );
        plotPanel.updateUI();

    }


    /**
     * TopComponent, which displays all graphics available for the express test.
     * <p>
     * @param analysisHandler The analysis handler containing the results
     * @param usedTool        The tool used for the analysis (has to be the
     *                        ExpressTest in this case)
     */
    public ExpressTestGraphicsTopComponent( DeAnalysisHandler analysisHandler, DeAnalysisHandler.Tool usedTool ) {
        this.analysisHandler = analysisHandler;
        this.usedTool = usedTool;
        results = analysisHandler.getResults();
        List<String> descriptions = new ArrayList<>();
        for( ResultDeAnalysis currentResult : results ) {
            descriptions.add( currentResult.getDescription() );
        }
        cbmDataSet = new DefaultComboBoxModel<>( descriptions.toArray( new String[descriptions.size()] ) );
        initComponents();
        initAdditionalComponents();
    }


    private void initAdditionalComponents() {
        setName( Bundle.CTL_PlotTopComponent() );
        setToolTipText( Bundle.HINT_PlotTopComponent() );
        iSymbol.setToolTipText( org.openide.util.NbBundle.getMessage( ExpressTestGraphicsTopComponent.class, "GraphicsTopComponent.iSymbol.toolTipText" ) );
    }


    public Map<PersistentFeature, Pair<Double, Double>> createSamplePoints( int n ) {
        Random r = new Random( System.nanoTime() );
        Map<PersistentFeature, Pair<Double, Double>> points = new HashMap<>();
        for( int i = 0; i < n; i++ ) {
            PersistentFeature dummyFeature = new PersistentFeature( 0, 0, "", "", "", "", 0, 0, true, FeatureType.ANY, "" );
            double random = Math.random();
            if( random > 0.95 ) {
                points.put( dummyFeature, new Pair<>( r.nextDouble() * 256.0d, Double.POSITIVE_INFINITY ) );
                points.put( dummyFeature, new Pair<>( r.nextDouble() * 256.0d, Double.NEGATIVE_INFINITY ) );
            }
            else {
                points.put( dummyFeature, new Pair<>( 2 * i + (r.nextGaussian() - 0.5d), r.nextDouble() * 256.0d ) );
            }
        }
        PersistentFeature dummyFeature = new PersistentFeature( 0, 0, "", "", "", "", 0, 0, true, FeatureType.ANY, "" );
        points.put( dummyFeature, new Pair<>( 200d, 300d ) );
        dummyFeature = new PersistentFeature( 0, 0, "", "", "", "", 0, 0, true, FeatureType.ANY, "" );
        points.put( dummyFeature, new Pair<>( 100d, Double.POSITIVE_INFINITY ) );
        return points;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        dataSetComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        plotTypeComboBox = new javax.swing.JComboBox();
        createPlotButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        iSymbol = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        plotPanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExpressTestGraphicsTopComponent.class, "ExpressTestGraphicsTopComponent.jLabel1.text_1")); // NOI18N

        dataSetComboBox.setModel(cbmDataSet);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ExpressTestGraphicsTopComponent.class, "ExpressTestGraphicsTopComponent.jLabel2.text_1")); // NOI18N

        plotTypeComboBox.setModel(cbmPlotType);

        org.openide.awt.Mnemonics.setLocalizedText(createPlotButton, org.openide.util.NbBundle.getMessage(ExpressTestGraphicsTopComponent.class, "ExpressTestGraphicsTopComponent.createPlotButton.text_1")); // NOI18N
        createPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPlotButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(ExpressTestGraphicsTopComponent.class, "ExpressTestGraphicsTopComponent.saveButton.text_1")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readxplorer/transcriptionanalyses/differentialexpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(ExpressTestGraphicsTopComponent.class, "ExpressTestGraphicsTopComponent.iSymbol.text_1")); // NOI18N

        jScrollPane1.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(240, 240, 240));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(2);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane1.setViewportView(messages);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(iSymbol, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(saveButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createPlotButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(plotTypeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(createPlotButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 241, Short.MAX_VALUE)
                .addComponent(iSymbol)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        plotPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        plotPanel.setPreferredSize(new java.awt.Dimension(100, 200));
        plotPanel.setLayout(new javax.swing.BoxLayout(plotPanel, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane1.setRightComponent(plotPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(1088, Short.MAX_VALUE))
                    .addComponent(dataSetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPlotButtonActionPerformed
        progressHandle.start();
        progressHandle.switchToIndeterminate();
        plotPanel.removeAll();
        messages.setText( "" );
        PlotTypes type = (PlotTypes) cbmPlotType.getSelectedItem();
        int index = dataSetComboBox.getSelectedIndex();
        final ResultDeAnalysis result = results.get( index );
        switch( type ) {
            case MA_Plot:
                chartPanel = CreatePlots.createInfPlot( ConvertData.createMAvalues( result, usedTool, null, null ), "A ((log(baseMeanA)/log(2)) + (log(baseMeanB)/log(2)))/2", "M (log(baseMeanA)/log(2)) - (log(baseMeanB)/log(2))", new ToolTip() );
                plotPanel.add( chartPanel );
                plotPanel.updateUI();
                break;
            case RatioAB_Confidence:
                chartPanel = CreatePlots.createPlot( ConvertData.ratioABagainstConfidence( result ), "ratioAB", "Confidence", new ToolTip() );
                plotPanel.add( chartPanel );
                plotPanel.updateUI();
                break;
            case RatioBA_Confidence:
                chartPanel = CreatePlots.createPlot( ConvertData.ratioBAagainstConfidence( result ), "ratioBA", "Confidence", new ToolTip() );
                plotPanel.add( chartPanel );
                plotPanel.updateUI();
                break;
        }
        saveButton.setEnabled( true );
        progressHandle.switchToDeterminate( 100 );
        progressHandle.finish();
    }//GEN-LAST:event_createPlotButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser( new String[]{ "svg" }, "svg" ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                saveToSVG( fileLocation );
            }


            @Override
            public void open( String fileLocation ) {
            }


        };
        fc.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createPlotButton;
    private javax.swing.JComboBox dataSetComboBox;
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea messages;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JComboBox plotTypeComboBox;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables


    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }


    @Override
    public void componentClosed() {
        if( analysisHandler != null ) {
            analysisHandler.removeObserver( this );
        }
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // TODO store your settings
    }


    void readProperties( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // TODO read your settings according to their version
    }


    private void saveToSVG( String fileLocation ) {
        svgExportProgressHandle = ProgressHandleFactory.createHandle( "Save plot to svg file: " + fileLocation );
        Path to = FileSystems.getDefault().getPath( fileLocation, "" );
        ChartExporter exporter = new ChartExporter( to, chartPanel.getChart() );
        exporter.registerObserver( this );
        new Thread( exporter ).start();

    }


    @Override
    public void update( Object args ) {
        if( args instanceof ChartExporter.ChartExportStatus ) {
            DgeExportUtilities.updateExportStatus( svgExportProgressHandle, (ChartExporter.ChartExportStatus) args, saveButton );
        }
    }


    public static enum PlotTypes {

        MA_Plot( "MA Plot" ), RatioAB_Confidence( "Ratio A/B against Confidence" ),
        RatioBA_Confidence( "Ratio B/A against Confidence" );
        private final String name;


        private PlotTypes( String name ) {
            this.name = name;
        }


        @Override
        public String toString() {
            return name;
        }


    }

}
