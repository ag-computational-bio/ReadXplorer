package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import de.cebitec.vamp.differentialExpression.DeSeqAnalysisHandler;
import de.cebitec.vamp.differentialExpression.GnuR;
import de.cebitec.vamp.differentialExpression.ResultDeAnalysis;
import de.cebitec.vamp.plotting.CreatePlots;
import de.cebitec.vamp.plotting.ChartExporter;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FAILED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FINISHED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.RUNNING;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.awt.BorderLayout;
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
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//de.cebitec.vamp.differentialExpression//DeSeqGrafics//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "DeSeqGraficsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "bottomSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.vamp.differentialExpression.DeSeqGraficsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DeSeqGraficsAction",
        preferredID = "DeSeqGraficsTopComponent")
@Messages({
    "CTL_DeSeqGraficsAction=DeSeqGrafics",
    "CTL_DeSeqGraficsTopComponent=Create graphics",
    "HINT_DeSeqGraficsTopComponent=This is a DeSeqGrafics window"
})
public final class DeSeqGraficsTopComponent extends TopComponent implements Observer {

    private DeAnalysisHandler analysisHandler;
    private JSVGCanvas svgCanvas;
    private ChartPanel chartPanel;
    private ComboBoxModel cbm;
    private File currentlyDisplayed;
    private ResultDeAnalysis result;
    private boolean SVGCanvasActive;
    private MouseActions mouseAction = new MouseActions();
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Creating plot");
    private ProgressHandle svgExportProgressHandle;

    public DeSeqGraficsTopComponent() {
    }

    public DeSeqGraficsTopComponent(DeAnalysisHandler handler, boolean moreThanTwoConditions) {
        analysisHandler = handler;
        this.result = handler.getResults().get(0);
        cbm = new DefaultComboBoxModel(DeSeqAnalysisHandler.Plot.getValues(moreThanTwoConditions));
        initComponents();
        setupGrafics();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        plotType = new javax.swing.JComboBox();
        plotButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeSeqGraficsTopComponent.class, "DeSeqGraficsTopComponent.jLabel1.text")); // NOI18N

        plotType.setModel(cbm);

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(DeSeqGraficsTopComponent.class, "DeSeqGraficsTopComponent.plotButton.text")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(DeSeqGraficsTopComponent.class, "DeSeqGraficsTopComponent.saveButton.text")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(240, 240, 240));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(5);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane1.setViewportView(messages);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(plotType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(plotButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plotType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(plotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 307, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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
                chartPanel.addChartMouseListener(mouseAction);
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
        VampFileChooser fc = new VampFileChooser(new String[]{"svg"}, "svg") {
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
        fc.openFileChooser(VampFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
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

    private void setupGrafics() {
        setName(Bundle.CTL_DeSeqGraficsTopComponent());
        setToolTipText(Bundle.HINT_DeSeqGraficsTopComponent());
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
}
