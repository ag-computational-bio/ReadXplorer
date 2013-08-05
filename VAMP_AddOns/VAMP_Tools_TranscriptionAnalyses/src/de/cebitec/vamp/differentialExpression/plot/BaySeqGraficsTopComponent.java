package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.BaySeq;
import de.cebitec.vamp.differentialExpression.BaySeqAnalysisHandler;
import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import de.cebitec.vamp.differentialExpression.GnuR;
import de.cebitec.vamp.differentialExpression.Group;
import de.cebitec.vamp.differentialExpression.ResultDeAnalysis;
import de.cebitec.vamp.plotting.CreatePlots;
import de.cebitec.vamp.plotting.ChartExporter;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FAILED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FINISHED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.RUNNING;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
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
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.differentialExpression//DiffExpGrafics//EN",
        autostore = false)
@TopComponent.Description(preferredID = "DiffExpGraficsTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "bottomSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.vamp.differentialExpression.DiffExpGraficsTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(displayName = "#CTL_DiffExpGraficsAction",
        preferredID = "DiffExpGraficsTopComponent")
@Messages({
    "CTL_DiffExpGraficsAction=DiffExpGrafics",
    "CTL_DiffExpGraficsTopComponent=Create graphics",
    "HINT_DiffExpGraficsTopComponent=This is a DiffExpGrafics window"
})
public final class BaySeqGraficsTopComponent extends TopComponent implements Observer, ItemListener {

    private BaySeqAnalysisHandler baySeqAnalysisHandler;
    private JSVGCanvas svgCanvas;
    private ComboBoxModel cbm;
    private DefaultListModel<PersistantTrack> samplesA = new DefaultListModel<>();
    private DefaultListModel<PersistantTrack> samplesB = new DefaultListModel<>();
    private File currentlyDisplayed;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Creating plot");
    private ResultDeAnalysis result;
    private List<Group> groups;
    private ChartPanel chartPanel;
    private boolean SVGCanvasActive;
    private MouseActions mouseAction = new MouseActions();
    private ProgressHandle svgExportProgressHandle;

    public BaySeqGraficsTopComponent() {
    }

    public BaySeqGraficsTopComponent(DeAnalysisHandler handler) {
        baySeqAnalysisHandler = (BaySeqAnalysisHandler) handler;
        List<ResultDeAnalysis> results = handler.getResults();
        this.result = results.get(results.size() - 1);
        groups = baySeqAnalysisHandler.getGroups();
        cbm = new DefaultComboBoxModel(BaySeqAnalysisHandler.Plot.values());
        initComponents();
        iSymbol.setVisible(false);
        setName(Bundle.CTL_DiffExpGraficsTopComponent());
        setToolTipText(Bundle.HINT_DiffExpGraficsTopComponent());
        svgCanvas = new JSVGCanvas();
        jPanel1.add(svgCanvas, BorderLayout.CENTER);
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
        SVGCanvasActive = true;
    }

    private void addResults() {
        List<Group> groups = baySeqAnalysisHandler.getGroups();
        groupComboBox.setModel(new DefaultComboBoxModel(groups.toArray()));
        List<PersistantTrack> tracks = baySeqAnalysisHandler.getSelectedTracks();
        for (Iterator<PersistantTrack> it = tracks.iterator(); it.hasNext();) {
            PersistantTrack persistantTrack = it.next();
            samplesA.addElement(persistantTrack);
            samplesB.addElement(persistantTrack);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        plotTypeComboBox = new javax.swing.JComboBox(cbm);
        jLabel2 = new javax.swing.JLabel();
        groupComboBox = new javax.swing.JComboBox();
        samplesALabel = new javax.swing.JLabel();
        samplesBLabel = new javax.swing.JLabel();
        plotButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        samplesAList = new javax.swing.JList(samplesA);
        jScrollPane2 = new javax.swing.JScrollPane();
        samplesBList = new javax.swing.JList(samplesB);
        saveButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        iSymbol = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.jLabel1.text")); // NOI18N

        plotTypeComboBox.addItemListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(samplesALabel, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.samplesALabel.text")); // NOI18N
        samplesALabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(samplesBLabel, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.samplesBLabel.text")); // NOI18N
        samplesBLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.plotButton.text")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setLayout(new java.awt.BorderLayout());

        samplesAList.setEnabled(false);
        jScrollPane1.setViewportView(samplesAList);

        samplesBList.setEnabled(false);
        jScrollPane2.setViewportView(samplesBList);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.saveButton.text")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(null);

        messages.setEditable(false);
        messages.setBackground(new java.awt.Color(240, 240, 240));
        messages.setColumns(20);
        messages.setLineWrap(true);
        messages.setRows(5);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane3.setViewportView(messages);

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/vamp/differentialExpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(BaySeqGraficsTopComponent.class, "BaySeqGraficsTopComponent.iSymbol.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(plotTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(samplesALabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(samplesBLabel)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(plotButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(saveButton)
                            .addComponent(jScrollPane3)
                            .addComponent(iSymbol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plotTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(samplesBLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(samplesALabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(plotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(iSymbol, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        VampFileChooser fc = new VampFileChooser(new String[]{"svg"}, "svg") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                Path to = FileSystems.getDefault().getPath(fileLocation, "");
                BaySeqAnalysisHandler.Plot selectedPlot = (BaySeqAnalysisHandler.Plot) plotTypeComboBox.getSelectedItem();
                if (selectedPlot == BaySeqAnalysisHandler.Plot.MACD) {
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
                    }
                }
            }

            @Override
            public void open(String fileLocation) {
            }
        };
        fc.openFileChooser(VampFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void plotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotButtonActionPerformed
        BaySeqAnalysisHandler.Plot selectedPlot = (BaySeqAnalysisHandler.Plot) plotTypeComboBox.getSelectedItem();
        messages.setText("");
        int[] samplA = samplesAList.getSelectedIndices();
        int[] samplB = samplesBList.getSelectedIndices();
        if (selectedPlot == BaySeqAnalysisHandler.Plot.MACD) {
            progressHandle.start();
            progressHandle.switchToIndeterminate();
            List<Integer> sampleA = new ArrayList<>();
            List<Integer> sampleB = new ArrayList<>();
            Group selectedGroup = groups.get(groupComboBox.getSelectedIndex());
            Integer[] integerRep = selectedGroup.getIntegerRepresentation();
            Integer integerGroupA = integerRep[0];
            Integer integerGroupB = null;
            sampleA.add(0);
            for (int i = 1; i < integerRep.length; i++) {
                Integer currentInteger = integerRep[i];
                if (currentInteger == integerGroupA) {
                    sampleA.add(i);
                } else {
                    if (integerGroupB == null) {
                        integerGroupB = currentInteger;
                        sampleB.add(i);
                    } else {
                        if (integerGroupB == currentInteger) {
                            sampleB.add(i);
                        } else {
                            messages.setText("Select a model with exactly two groups to create a MA-Plot.");
                            break;
                        }
                    }
                }
            }
            if (sampleB.isEmpty() || (sampleA.size() + sampleB.size()) != integerRep.length) {
                messages.setText("Select a model with exactly two groups to create a MA-Plot.");
            } else {
                chartPanel = CreatePlots.createInfPlot(
                        ConvertData.createMAvalues(result, DeAnalysisHandler.Tool.BaySeq, sampleA.toArray(new Integer[sampleA.size()]),
                        sampleB.toArray(new Integer[sampleB.size()])), "A", "M", new ToolTip());
                chartPanel.addChartMouseListener(mouseAction);
                if (SVGCanvasActive) {
                    jPanel1.remove(svgCanvas);
                    SVGCanvasActive = false;
                }
                jPanel1.add(chartPanel, BorderLayout.CENTER);
                jPanel1.updateUI();
                plotButton.setEnabled(true);
                saveButton.setEnabled(true);
            }
            progressHandle.switchToDeterminate(100);
            progressHandle.finish();
        } else {
            if (!SVGCanvasActive) {
                jPanel1.remove(chartPanel);
                jPanel1.add(svgCanvas, BorderLayout.CENTER);
                jPanel1.updateUI();
                SVGCanvasActive = true;
            }
            try {
                messages.setText("");
                plotButton.setEnabled(false);
                saveButton.setEnabled(false);
                currentlyDisplayed = baySeqAnalysisHandler.plot(selectedPlot, ((Group) groupComboBox.getSelectedItem()), samplA, samplB);
                svgCanvas.setURI(currentlyDisplayed.toURI().toString());
                svgCanvas.setVisible(true);
                svgCanvas.repaint();
            } catch (IOException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
                JOptionPane.showMessageDialog(null, "Can't create the temporary svg file!", "Gnu R Error", JOptionPane.WARNING_MESSAGE);
            } catch (BaySeq.SamplesNotValidException ex) {
                messages.setText("Samples A and B must not be the same!");
                plotButton.setEnabled(true);
            } catch (GnuR.PackageNotLoadableException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_plotButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox groupComboBox;
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
        baySeqAnalysisHandler.removeObserver(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
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
        } else {
            addResults();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        BaySeqAnalysisHandler.Plot item = (BaySeqAnalysisHandler.Plot) e.getItem();
        if (item == BaySeqAnalysisHandler.Plot.MACD) {
            samplesAList.setEnabled(false);
            samplesALabel.setEnabled(false);
            samplesBList.setEnabled(false);
            samplesBLabel.setEnabled(false);
            groupComboBox.setEnabled(true);
            iSymbol.setVisible(true);
        }
        if (item == BaySeqAnalysisHandler.Plot.Posteriors) {
            samplesAList.setEnabled(true);
            samplesALabel.setEnabled(true);
            samplesBList.setEnabled(true);
            samplesBLabel.setEnabled(true);
            groupComboBox.setEnabled(true);
            iSymbol.setVisible(false);
        }
        if (item == BaySeqAnalysisHandler.Plot.Priors) {
            samplesAList.setEnabled(false);
            samplesALabel.setEnabled(false);
            samplesBList.setEnabled(false);
            samplesBLabel.setEnabled(false);
            groupComboBox.setEnabled(true);
            iSymbol.setVisible(false);
        }
    }
}
