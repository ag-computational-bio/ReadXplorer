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


import de.cebitec.readxplorer.plotting.ChartExporter;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeSeq2AnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.ResultDeAnalysis;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.fileChooser.ReadXplorerFileChooser;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jfree.chart.ChartPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * TopComponent, which displays all graphics available for a DESeq analysis.
 */
@ConvertAsProperties(
    dtd = "-//de.cebitec.readxplorer.transcriptionanalyses.differentialexpression//DeSeq2Graphics//EN",
    autostore = false )
@TopComponent.Description(
    preferredID = "DeSeq2GraphicsTopComponent",
    //iconBase="SET/PATH/TO/ICON/HERE",
    persistenceType = TopComponent.PERSISTENCE_NEVER )
@TopComponent.Registration( mode = "bottomSlidingSide", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeSeq2GraphicsTopComponent" )
@ActionReference( path = "Menu/Window" /*, position = 333 */ )
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_DeSeq2GraphicsAction",
    preferredID = "DeSeq2GraphicsTopComponent" )
@Messages( {
    "CTL_DeSeq2GraphicsAction=DeSeq2Graphics",
    "CTL_DeSeq2GraphicsTopComponent=DESeq2 Graphics",
    "HINT_DeSeq2GraphicsTopComponent=This is a DESeq2 graphics window"
} )
public final class DeSeq2GraphicsTopComponent extends TopComponentExtended
        implements Observer, ItemListener {

    private static final long serialVersionUID = 1L;

    private DeAnalysisHandler analysisHandler;
    private JSVGCanvas svgCanvas;
    private ChartPanel chartPanel;
    private ComboBoxModel cbm;
    private File currentlyDisplayed;
    private ResultDeAnalysis result;
    private boolean svgCanvasActive;
    private final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Creating plot" );
    private ProgressHandle svgExportProgressHandle;


    /**
     * TopComponent, which displays all graphics available for a DESeq analysis.
     */
    public DeSeq2GraphicsTopComponent() {
    }


    /**
     * TopComponent, which displays all graphics available for a DESeq analysis.
     *
     * @param handler The analysis handler containing the results
     */
    public DeSeq2GraphicsTopComponent( DeAnalysisHandler handler ) {
        analysisHandler = handler;
        this.result = handler.getResults().get( 0 );
        cbm = new DefaultComboBoxModel( DeSeq2AnalysisHandler.Plot.getValues() );
        initComponents();
        setupGraphics();
        iSymbol.setVisible( false );
        iSymbol.setToolTipText( org.openide.util.NbBundle.getMessage( DeSeq2GraphicsTopComponent.class, "GraphicsTopComponent.iSymbol.toolTipText" ) );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        optionsPanel = new javax.swing.JPanel();
        iSymbol = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        saveButton = new javax.swing.JButton();
        plotButton = new javax.swing.JButton();
        plotType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        plotPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        plotDescriptionArea = new javax.swing.JTextArea();

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readxplorer/transcriptionanalyses/differentialexpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(DeSeq2GraphicsTopComponent.class, "DeSeq2GraphicsTopComponent.iSymbol.text")); // NOI18N

        jScrollPane1.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(240, 240, 240));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(5);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane1.setViewportView(messages);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(DeSeq2GraphicsTopComponent.class, "DeSeq2GraphicsTopComponent.saveButton.text")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(DeSeq2GraphicsTopComponent.class, "DeSeq2GraphicsTopComponent.plotButton.text")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        plotType.setModel(cbm);
        plotType.addItemListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeSeq2GraphicsTopComponent.class, "DeSeq2GraphicsTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(plotType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(iSymbol)
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(plotButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 261, Short.MAX_VALUE)
                .addComponent(iSymbol)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(optionsPanel);

        plotPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        plotPanel.setLayout(new java.awt.BorderLayout());

        plotDescriptionArea.setEditable(false);
        plotDescriptionArea.setBackground(new java.awt.Color(240, 240, 240));
        plotDescriptionArea.setColumns(3);
        plotDescriptionArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        plotDescriptionArea.setLineWrap(true);
        plotDescriptionArea.setRows(3);
        plotDescriptionArea.setWrapStyleWord(true);
        plotDescriptionArea.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        plotDescriptionArea.setMinimumSize(new java.awt.Dimension(164, 42));
        plotDescriptionArea.setPreferredSize(new java.awt.Dimension(164, 42));
        jScrollPane2.setViewportView(plotDescriptionArea);

        plotPanel.add(jScrollPane2, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setRightComponent(plotPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void plotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotButtonActionPerformed
        try {
            messages.setText( "" );
            plotButton.setEnabled( false );
            saveButton.setEnabled( false );
            DeSeq2AnalysisHandler.Plot selectedPlot = (DeSeq2AnalysisHandler.Plot) plotType.getSelectedItem();
            plotDescriptionArea.setVisible( true );
            if( !svgCanvasActive ) {
                plotPanel.remove( chartPanel );
                plotPanel.add( svgCanvas, BorderLayout.CENTER );
                plotPanel.updateUI();
                svgCanvasActive = true;
            }
            currentlyDisplayed = ((DeSeq2AnalysisHandler) analysisHandler).plot( selectedPlot );
            svgCanvas.setURI( currentlyDisplayed.toURI().toString() );
            svgCanvas.setVisible( true );
            svgCanvas.repaint();

            plotDescriptionArea.repaint();
        }
        catch( IOException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp );
            JOptionPane.showMessageDialog( null, "Can't create the temporary svg file!", "Gnu R Error", JOptionPane.WARNING_MESSAGE );
        }
        catch( GnuR.PackageNotLoadableException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp );
            JOptionPane.showMessageDialog( null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE );
        }
    }//GEN-LAST:event_plotButtonActionPerformed


    @NbBundle.Messages( { "DeSeq2SuccessMsg=SVG image saved to ",
                          "DeSeq2SuccessHeader=Success" } )
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser( new String[]{ "svg" }, "svg" ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                ProgressHandle plotProgressHandle = ProgressHandleFactory.createHandle( "Save plot to svg file: " + fileLocation );
                Path to = FileSystems.getDefault().getPath( fileLocation, "" );
                DeSeq2AnalysisHandler.Plot selectedPlot = (DeSeq2AnalysisHandler.Plot) plotType.getSelectedItem();
                Path from = currentlyDisplayed.toPath();
                try {
                    Path outputFile = Files.copy( from, to, StandardCopyOption.REPLACE_EXISTING );
                    NotificationDisplayer.getDefault().notify( Bundle.DeSeq2SuccessHeader(), new ImageIcon(), Bundle.DeSeq2SuccessMsg() + outputFile.toString(), null );
                }
                catch( IOException ex ) {
                    Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                    Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp );
                    JOptionPane.showMessageDialog( null, ex.getMessage(), "Could not write to file.", JOptionPane.WARNING_MESSAGE );
                }
                finally {
                    plotProgressHandle.switchToDeterminate( 100 );
                    plotProgressHandle.finish();
                }

            }


            @Override
            public void open( String fileLocation ) {
            }


        };
        fc.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea messages;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton plotButton;
    private javax.swing.JTextArea plotDescriptionArea;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JComboBox plotType;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables


    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }


    @Override
    public void componentClosed() {
        analysisHandler.removeObserver( this );
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // TODO store your settings
    }


    void readProperties( java.util.Properties p ) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }


    private void setupGraphics() {
        setName( Bundle.CTL_DeSeqGraphicsTopComponent() );
        setToolTipText( Bundle.HINT_DeSeqGraphicsTopComponent() );
        svgCanvas = new JSVGCanvas();
        plotPanel.add( svgCanvas, BorderLayout.CENTER );
        svgCanvasActive = true;
        svgCanvas.addSVGDocumentLoaderListener( new SVGDocumentLoaderListener() {
            @Override
            public void documentLoadingStarted( SVGDocumentLoaderEvent e ) {
                progressHandle.start();
                progressHandle.switchToIndeterminate();
            }


            @Override
            public void documentLoadingCompleted( SVGDocumentLoaderEvent e ) {
                progressHandle.switchToDeterminate( 100 );
                progressHandle.finish();
                saveButton.setEnabled( true );
                plotButton.setEnabled( true );
                String description = "";
                switch( (DeSeq2AnalysisHandler.Plot) plotType.getSelectedItem() ) {
                    case DispEsts:
                        description = "DESeq's dispersion estimates plot: Empirical (black dots) per gene and fitted (red line) dispersion values (Y) plotted against mean expression strength (X) (doubly logarithmic)";
                        break;
                    case HIST:
                        description = "p-value histogram: Probability of genes not to be differentially expressed against its frequency in the experiment";
                        break;
                    default:
                        description = "";
                }
                plotDescriptionArea.setText( description );
                plotDescriptionArea.repaint();
            }


            @Override
            public void documentLoadingCancelled( SVGDocumentLoaderEvent e ) {
            }


            @Override
            public void documentLoadingFailed( SVGDocumentLoaderEvent e ) {
                messages.setText( "Could not load SVG file. Please try again." );
            }


        } );
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


    @Override
    public void itemStateChanged( ItemEvent e ) {
//          If an interactive Plot should be integrated in the future this function is needed
//        DeSeq2AnalysisHandler.Plot plotType = (DeSeq2AnalysisHandler.Plot) e.getItem();
//        if (plotType == DeSeq2AnalysisHandler.Plot.MAplot) {
//            iSymbol.setVisible(true);
//        } else {
//            iSymbol.setVisible(false);
//        }
    }


}
