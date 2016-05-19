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


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.plotting.ChartExporter;
import de.cebitec.readxplorer.plotting.CreatePlots;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.BaySeq;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.BaySeqAnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.GnuR;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.Group;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.ResultDeAnalysis;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.filechooser.ReadXplorerFileChooser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
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
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TopComponent, which displays all graphics available for a baySeq analysis.
 */
@ConvertAsProperties( dtd = "-//de.cebitec.readxplorer.transcriptionanalyses.differentialexpression//BaySeqGraphics//EN",
                      autostore = false )
@TopComponent.Description( preferredID = "BaySeqGraphicsTopComponent",
                           persistenceType = TopComponent.PERSISTENCE_NEVER )
@TopComponent.Registration( mode = "bottomSlidingSide", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.BaySeqGraphicsTopComponent" )
@ActionReference( path = "Menu/Window" )
@TopComponent.OpenActionRegistration( displayName = "#CTL_BaySeqGraphicsAction",
                                      preferredID = "BaySeqGraphicsTopComponent" )
@Messages( {
    "CTL_BaySeqGraphicsAction=BaySeqGraphics",
    "CTL_BaySeqGraphicsTopComponent=BaySeq Graphics",
    "HINT_BaySeqGraphicsTopComponent=This is a baySeq graphics window"
} )
public final class BaySeqGraphicsTopComponent extends TopComponentExtended
        implements Observer, ItemListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger( BaySeqGraphicsTopComponent.class.getName() );

    private BaySeqAnalysisHandler baySeqAnalysisHandler;
    private JSVGCanvas svgCanvas;
    private ComboBoxModel<BaySeqAnalysisHandler.Plot> cbm;
    private final DefaultListModel<PersistentTrack> samplesA = new DefaultListModel<>();
    private final DefaultListModel<PersistentTrack> samplesB = new DefaultListModel<>();
    private File currentlyDisplayed;
    private ProgressHandle progressHandle;
    private ResultDeAnalysis result;
    private List<Group> groups;
    private ChartPanel chartPanel;
    private boolean svgCanvasActive;
    private ProgressHandle svgExportProgressHandle;


    /**
     * TopComponent, which displays all graphics available for a baySeq
     * analysis.
     */
    public BaySeqGraphicsTopComponent() {
    }


    /**
     * TopComponent, which displays all graphics available for a baySeq
     * analysis.
     * <p>
     * @param handler The analysis handler containing the results
     */
    public BaySeqGraphicsTopComponent( DeAnalysisHandler handler ) {
        baySeqAnalysisHandler = (BaySeqAnalysisHandler) handler;
        List<ResultDeAnalysis> results = handler.getResults();
        this.result = results.get( results.size() - 1 );
        groups = baySeqAnalysisHandler.getGroups();
        cbm = new DefaultComboBoxModel<>( BaySeqAnalysisHandler.Plot.getValues() );
        initComponents();
        iSymbol.setVisible( false );
        iSymbol.setToolTipText( org.openide.util.NbBundle.getMessage( BaySeqGraphicsTopComponent.class, "GraphicsTopComponent.iSymbol.toolTipText" ) );
        setName( Bundle.CTL_BaySeqGraphicsTopComponent() );
        setToolTipText( Bundle.HINT_BaySeqGraphicsTopComponent() );
        svgCanvas = new JSVGCanvas();
        jPanel1.add( svgCanvas, BorderLayout.CENTER );
        svgCanvas.addSVGDocumentLoaderListener( new SVGDocumentLoaderListener() {
            @Override
            public void documentLoadingStarted( SVGDocumentLoaderEvent e ) {
            }


            @Override
            public void documentLoadingCompleted( SVGDocumentLoaderEvent e ) {
            }


            @Override
            public void documentLoadingCancelled( SVGDocumentLoaderEvent e ) {
            }


            @Override
            public void documentLoadingFailed( SVGDocumentLoaderEvent e ) {
                messages.setText( "Could not load SVG file. Please try again." );
            }


        } );
        svgCanvasActive = true;
    }


    private void addResults() {
        List<Group> resultGroups = baySeqAnalysisHandler.getGroups();
        groupComboBox.setModel( new DefaultComboBoxModel( resultGroups.toArray() ) );
        List<PersistentTrack> tracks = baySeqAnalysisHandler.getSelectedTracks();
        for( PersistentTrack persistentTrack : tracks ) {
            samplesA.addElement( persistentTrack );
            samplesB.addElement( persistentTrack );
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        groupComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        plotTypeComboBox = new javax.swing.JComboBox(cbm);
        samplesALabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        samplesAList = new javax.swing.JList(samplesA);
        jScrollPane2 = new javax.swing.JScrollPane();
        samplesBList = new javax.swing.JList(samplesB);
        samplesBLabel = new javax.swing.JLabel();
        plotButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        iSymbol = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.jLabel2.text_1")); // NOI18N

        jSplitPane1.setDividerLocation(240);

        jPanel2.setMinimumSize(new java.awt.Dimension(0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.jLabel1.text_1")); // NOI18N

        plotTypeComboBox.addItemListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(samplesALabel, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.samplesALabel.text_1")); // NOI18N
        samplesALabel.setEnabled(false);

        samplesAList.setEnabled(false);
        jScrollPane1.setViewportView(samplesAList);

        samplesBList.setEnabled(false);
        jScrollPane2.setViewportView(samplesBList);

        org.openide.awt.Mnemonics.setLocalizedText(samplesBLabel, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.samplesBLabel.text_1")); // NOI18N
        samplesBLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.plotButton.text_1")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.saveButton.text_1")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(238, 238, 238));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(5);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        messages.setDisabledTextColor(new java.awt.Color(238, 238, 238));
        jScrollPane3.setViewportView(messages);

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readxplorer/transcriptionanalyses/differentialexpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(BaySeqGraphicsTopComponent.class, "BaySeqGraphicsTopComponent.iSymbol.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(plotTypeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(samplesBLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(samplesALabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)))
                        .addGap(0, 25, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(iSymbol)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(samplesALabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(samplesBLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(plotButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(iSymbol)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSplitPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    @NbBundle.Messages( { "BaySeqSuccessMsg=SVG image saved to ",
                          "BaySeqSuccessHeader=Success" } )
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser( new String[]{ "svg" }, "svg" ) {
            private static final long serialVersionUID = 1L;


            @Override
            public void save( String fileLocation ) {
                Path to = FileSystems.getDefault().getPath( fileLocation, "" );
                BaySeqAnalysisHandler.Plot selectedPlot = (BaySeqAnalysisHandler.Plot) plotTypeComboBox.getSelectedItem();
                if( selectedPlot == BaySeqAnalysisHandler.Plot.MACD ) {
                    saveToSVG( fileLocation );
                } else {
                    Path from = currentlyDisplayed.toPath();
                    try {
                        Path outputFile = Files.copy( from, to, StandardCopyOption.REPLACE_EXISTING );
                        NotificationDisplayer.getDefault().notify( Bundle.BaySeqSuccessHeader(), new ImageIcon(), Bundle.BaySeqSuccessMsg() + outputFile.toString(), null );
                    } catch( IOException ex ) {
                        Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                        LOG.error( "{0}: " + ex.getMessage(), currentTimestamp );
                        JOptionPane.showMessageDialog( null, ex.getMessage(), "Could not write to file.", JOptionPane.WARNING_MESSAGE );
                    }
                }
            }


            @Override
            public void open( String fileLocation ) {
            }


        };
        fc.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
    }//GEN-LAST:event_saveButtonActionPerformed

    private void plotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotButtonActionPerformed
        progressHandle = ProgressHandleFactory.createHandle( "Creating plot" );
        progressHandle.start();
        progressHandle.switchToIndeterminate();
        BaySeqAnalysisHandler.Plot selectedPlot = (BaySeqAnalysisHandler.Plot) plotTypeComboBox.getSelectedItem();
        messages.setText( "" );
        int[] samplA = samplesAList.getSelectedIndices();
        int[] samplB = samplesBList.getSelectedIndices();
        if( selectedPlot == BaySeqAnalysisHandler.Plot.MACD ) {
            Group selectedGroup = groups.get( groupComboBox.getSelectedIndex() );
            final int[] integerRep = selectedGroup.getIntegerRepresentation();
            final int integerGroupA = integerRep[0];
            Integer integerGroupB = null;
            final List<Integer> sampleA = new ArrayList<>( integerRep.length + 1 );
            final List<Integer> sampleB = new ArrayList<>( integerRep.length + 1 );
            sampleA.add( 0 );
            for( int i = 1; i < integerRep.length; i++ ) {
                int currentInteger = integerRep[i];
                if( currentInteger == integerGroupA ) {
                    sampleA.add( i );
                } else if( integerGroupB == null ) {
                    integerGroupB = currentInteger;
                    sampleB.add( i );
                } else if( integerGroupB == currentInteger ) {
                    sampleB.add( i );
                } else {
                    messages.setText( "Select a model with exactly two groups to create a MA-Plot." );
                    break;
                }
            }
            if( sampleB.isEmpty() || (sampleA.size() + sampleB.size()) != integerRep.length ) {
                messages.setText( "Select a model with exactly two groups to create a MA-Plot." );
            } else {
                chartPanel = CreatePlots.createInfPlot(
                        ConvertData.createMAvalues( result, DeAnalysisHandler.Tool.BaySeq, sampleA.toArray( new Integer[sampleA.size()] ),
                                                    sampleB.toArray( new Integer[sampleB.size()] ) ), "A ((log(baseMeanA)/log(2)) + (log(baseMeanB)/log(2)))/2", "M (log(baseMeanA)/log(2)) - (log(baseMeanB)/log(2))", new ToolTip() );
                if( svgCanvasActive ) {
                    jPanel1.remove( svgCanvas );
                    svgCanvasActive = false;
                }
                jPanel1.add( chartPanel, BorderLayout.CENTER );
                jPanel1.updateUI();
                plotButton.setEnabled( true );
                saveButton.setEnabled( true );
            }
            progressHandle.switchToDeterminate( 100 );
            progressHandle.finish();
        } else {
            if( !svgCanvasActive ) {
                jPanel1.remove( chartPanel );
                jPanel1.add( svgCanvas, BorderLayout.CENTER );
                jPanel1.updateUI();
                svgCanvasActive = true;
            }
            try {
                messages.setText( "" );
                plotButton.setEnabled( false );
                saveButton.setEnabled( false );
                currentlyDisplayed = baySeqAnalysisHandler.plot( selectedPlot, ((Group) groupComboBox.getSelectedItem()), samplA, samplB );
                svgCanvas.setURI( currentlyDisplayed.toURI().toString() );
                svgCanvas.setVisible( true );
                svgCanvas.repaint();
            } catch( IOException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( "{0}: " + ex.getMessage(), currentTimestamp );
                JOptionPane.showMessageDialog( null, "Can't create the temporary svg file!", "Gnu R Error", JOptionPane.WARNING_MESSAGE );
            } catch( BaySeq.SamplesNotValidException ex ) {
                messages.setText( "Samples A and B must not be the same!" );
            } catch( GnuR.PackageNotLoadableException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( "{0}: " + ex.getMessage(), currentTimestamp );
                JOptionPane.showMessageDialog( null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE );
            } catch( IllegalStateException | REXPMismatchException | REngineException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( "{0}: " + ex.getMessage(), currentTimestamp );
                JOptionPane.showMessageDialog( null, ex.getMessage(), "RServe Error", JOptionPane.WARNING_MESSAGE );
            } finally {
                progressHandle.switchToDeterminate( 100 );
                progressHandle.finish();
                plotButton.setEnabled( true );
            }

        }
    }//GEN-LAST:event_plotButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox groupComboBox;
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea messages;
    private javax.swing.JButton plotButton;
    private javax.swing.JComboBox plotTypeComboBox;
    private javax.swing.JLabel samplesALabel;
    private javax.swing.JList samplesAList;
    private javax.swing.JLabel samplesBLabel;
    private javax.swing.JList samplesBList;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables


    @Override
    public void componentOpened() {
        addResults();
    }


    @Override
    public void componentClosed() {
        baySeqAnalysisHandler.removeObserver( this );
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
        } else {
            addResults();
        }
    }


    @Override
    public void itemStateChanged( ItemEvent e ) {
        BaySeqAnalysisHandler.Plot item = (BaySeqAnalysisHandler.Plot) e.getItem();
        if( item == BaySeqAnalysisHandler.Plot.Priors ||
            item == BaySeqAnalysisHandler.Plot.MACD ) {
            samplesAList.setEnabled( false );
            samplesALabel.setEnabled( false );
            samplesBList.setEnabled( false );
            samplesBLabel.setEnabled( false );
            groupComboBox.setEnabled( true );
        }
        if( item == BaySeqAnalysisHandler.Plot.Posteriors ) {
            samplesAList.setEnabled( true );
            samplesALabel.setEnabled( true );
            samplesBList.setEnabled( true );
            samplesBLabel.setEnabled( true );
            groupComboBox.setEnabled( true );
        }
        iSymbol.setVisible( item == BaySeqAnalysisHandler.Plot.MACD );
    }


}
