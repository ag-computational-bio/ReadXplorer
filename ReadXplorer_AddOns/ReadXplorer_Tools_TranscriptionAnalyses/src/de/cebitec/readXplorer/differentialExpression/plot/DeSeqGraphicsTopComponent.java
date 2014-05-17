/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.differentialExpression.plot;

import de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.DeSeqAnalysisHandler;
import de.cebitec.readXplorer.differentialExpression.GnuR;
import de.cebitec.readXplorer.differentialExpression.ResultDeAnalysis;
import de.cebitec.readXplorer.plotting.ChartExporter;
import static de.cebitec.readXplorer.plotting.ChartExporter.ChartExportStatus.FAILED;
import static de.cebitec.readXplorer.plotting.ChartExporter.ChartExportStatus.FINISHED;
import static de.cebitec.readXplorer.plotting.ChartExporter.ChartExportStatus.RUNNING;
import de.cebitec.readXplorer.plotting.CreatePlots;
import de.cebitec.readXplorer.differentialExpression.plot.Bundle;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import de.cebitec.readXplorer.view.TopComponentExtended;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jfree.chart.ChartPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * TopComponent, which displays all graphics available for a DESeq analysis.
 */
@ConvertAsProperties(
        dtd = "-//de.cebitec.readXplorer.differentialExpression//DeSeqGraphics//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "DeSeqGraphicsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "bottomSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.readXplorer.differentialExpression.DeSeqGraphicsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DeSeqGraphicsAction",
        preferredID = "DeSeqGraphicsTopComponent")
@Messages({
    "CTL_DeSeqGraphicsAction=DeSeqGraphics",
    "CTL_DeSeqGraphicsTopComponent=DESeq Graphics",
    "HINT_DeSeqGraphicsTopComponent=This is a DESeq graphics window"
})
public final class DeSeqGraphicsTopComponent extends TopComponentExtended implements Observer, ItemListener {
    private static final long serialVersionUID = 1L;

    private DeAnalysisHandler analysisHandler;
    private JSVGCanvas svgCanvas;
    private ChartPanel chartPanel;
    private ComboBoxModel cbm;
    private File currentlyDisplayed;
    private ResultDeAnalysis result;
    private boolean SVGCanvasActive;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Creating plot");
    private ProgressHandle svgExportProgressHandle;

    /**
     * TopComponent, which displays all graphics available for a DESeq analysis.
     */
    public DeSeqGraphicsTopComponent() {
    }

    /**
     * TopComponent, which displays all graphics available for a DESeq analysis.
     * @param analysisHandler The analysis handler containing the results
     * @param usedTool The tool used for the analysis (has to be DESeq in this 
     * case)
     */
    public DeSeqGraphicsTopComponent(DeAnalysisHandler handler, boolean moreThanTwoConditions) {
        analysisHandler = handler;
        this.result = handler.getResults().get(0);
        cbm = new DefaultComboBoxModel(DeSeqAnalysisHandler.Plot.getValues(moreThanTwoConditions));
        initComponents();
        setupGraphics();
        iSymbol.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        iSymbol = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        saveButton = new javax.swing.JButton();
        plotButton = new javax.swing.JButton();
        plotType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/readXplorer/differentialExpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(DeSeqGraphicsTopComponent.class, "DeSeqGraphicsTopComponent.iSymbol.text")); // NOI18N

        jScrollPane1.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(240, 240, 240));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(5);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane1.setViewportView(messages);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(DeSeqGraphicsTopComponent.class, "DeSeqGraphicsTopComponent.saveButton.text")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(DeSeqGraphicsTopComponent.class, "DeSeqGraphicsTopComponent.plotButton.text")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        plotType.setModel(cbm);
        plotType.addItemListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeSeqGraphicsTopComponent.class, "DeSeqGraphicsTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(plotType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(iSymbol)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(iSymbol)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(jPanel1);

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
            messages.setText("");
            plotButton.setEnabled(false);
            saveButton.setEnabled(false);
            DeSeqAnalysisHandler.Plot selectedPlot = (DeSeqAnalysisHandler.Plot) plotType.getSelectedItem();
            if (selectedPlot == DeSeqAnalysisHandler.Plot.MAplot) {
                progressHandle.start();
                progressHandle.switchToIndeterminate();
                chartPanel = CreatePlots.createInfPlot(ConvertData.createMAvalues(result, DeAnalysisHandler.Tool.DeSeq, null, null), "A", "M", new ToolTip());
                if (SVGCanvasActive) {
                    jPanel1.remove(svgCanvas);
                    SVGCanvasActive = false;
                }
                jPanel1.add(chartPanel, BorderLayout.CENTER);
                jPanel1.updateUI();
                plotButton.setEnabled(true);
                saveButton.setEnabled(true);
                progressHandle.switchToDeterminate(100);
                progressHandle.finish();
            } else {
                if (!SVGCanvasActive) {
                    jPanel1.remove(chartPanel);
                    jPanel1.add(svgCanvas, BorderLayout.CENTER);
                    jPanel1.updateUI();
                    SVGCanvasActive = true;
                }
                currentlyDisplayed = ((DeSeqAnalysisHandler) analysisHandler).plot(selectedPlot);
                svgCanvas.setURI(currentlyDisplayed.toURI().toString());
                svgCanvas.setVisible(true);
                svgCanvas.repaint();
            }
        } catch (IOException ex) {
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
            JOptionPane.showMessageDialog(null, "Can't create the temporary svg file!", "Gnu R Error", JOptionPane.WARNING_MESSAGE);
        } catch (GnuR.PackageNotLoadableException ex) {
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_plotButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        ReadXplorerFileChooser fc = new ReadXplorerFileChooser(new String[]{"svg"}, "svg") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Save plot to svg file: " + fileLocation);
                Path to = FileSystems.getDefault().getPath(fileLocation, "");
                DeSeqAnalysisHandler.Plot selectedPlot = (DeSeqAnalysisHandler.Plot) plotType.getSelectedItem();
                if (selectedPlot == DeSeqAnalysisHandler.Plot.MAplot) {
                    saveToSVG(fileLocation);
                } else {
                    Path from = currentlyDisplayed.toPath();
                    try {
                        Path outputFile = Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                        messages.setText("SVG image saved to " + outputFile.toString());
                    } catch (IOException ex) {
                        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Could not write to file.", JOptionPane.WARNING_MESSAGE);
                    } finally {
                        progressHandle.switchToDeterminate(100);
                        progressHandle.finish();
                    }
                }
            }

            @Override
            public void open(String fileLocation) {
            }
        };
        fc.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea messages;
    private javax.swing.JButton plotButton;
    private javax.swing.JComboBox plotType;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        analysisHandler.removeObserver(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void setupGraphics() {
        setName(Bundle.CTL_DeSeqGraphicsTopComponent());
        setToolTipText(Bundle.HINT_DeSeqGraphicsTopComponent());
        svgCanvas = new JSVGCanvas();
        jPanel1.add(svgCanvas, BorderLayout.CENTER);
        SVGCanvasActive = true;
        svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderListener() {
            @Override
            public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                progressHandle.start();
                progressHandle.switchToIndeterminate();
            }

            @Override
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                progressHandle.switchToDeterminate(100);
                progressHandle.finish();
                saveButton.setEnabled(true);
                plotButton.setEnabled(true);
            }

            @Override
            public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
            }

            @Override
            public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
                messages.setText("Could not load SVG file. Please try again.");
            }
        });
    }

    private void saveToSVG(String fileLocation) {
        svgExportProgressHandle = ProgressHandleFactory.createHandle("Save plot to svg file: " + fileLocation);
        Path to = FileSystems.getDefault().getPath(fileLocation, "");
        ChartExporter exporter = new ChartExporter(to, chartPanel.getChart());
        exporter.registerObserver(this);
        new Thread(exporter).start();

    }

    @Override
    public void update(Object args) {
        if (args instanceof ChartExporter.ChartExportStatus) {
            final ChartExporter.ChartExportStatus status = (ChartExporter.ChartExportStatus) args;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case RUNNING:
                                saveButton.setEnabled(false);
                                svgExportProgressHandle.start();
                                svgExportProgressHandle.switchToIndeterminate();
                                break;
                            case FAILED:
                                messages.setText("The export of the plot failed.");
                            case FINISHED:
                                messages.setText("SVG image saved.");
                                svgExportProgressHandle.switchToDeterminate(100);
                                svgExportProgressHandle.finish();
                                break;
                        }
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, ex.getMessage(), currentTimestamp);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        DeSeqAnalysisHandler.Plot plotType = (DeSeqAnalysisHandler.Plot) e.getItem();
        if (plotType == DeSeqAnalysisHandler.Plot.MAplot) {
            iSymbol.setVisible(true);
        } else {
            iSymbol.setVisible(false);
        }
    }
}
