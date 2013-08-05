package de.cebitec.vamp.differentialExpression.plot;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import de.cebitec.vamp.differentialExpression.ResultDeAnalysis;
import de.cebitec.vamp.plotting.CreatePlots;
import de.cebitec.vamp.plotting.ChartExporter;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FAILED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.FINISHED;
import static de.cebitec.vamp.plotting.ChartExporter.ChartExportStatus.RUNNING;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.List;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//de.cebitec.vamp.differentialExpression.plot//Plot//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "PlotTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
//@ActionID(category = "Tools", id = "de.cebitec.vamp.differentialExpression.plot.PlotTopComponent")
//@ActionReference(path = "Menu/Tools")
@ActionID(category = "Window", id = "de.cebitec.vamp.differentialExpression.plot.PlotTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PlotAction",
        preferredID = "PlotTopComponent")
@Messages({
    "CTL_PlotAction=Plot",
    "CTL_PlotTopComponent=Create graphics",
    "HINT_PlotTopComponent=This is a Plot window"
})
public final class SimpleTestGraficsTopComponent extends TopComponent implements Observer {

    private DefaultComboBoxModel<PlotTypes> cbmPlotType = new DefaultComboBoxModel<>(PlotTypes.values());
    private DefaultComboBoxModel<String> cbmDataSet;
    private DeAnalysisHandler analysisHandler;
    private List<ResultDeAnalysis> results;
    private DeAnalysisHandler.Tool usedTool;
    private MouseActions mouseAction = new MouseActions();
    private ChartPanel chartPanel;
    private ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Creating plot");
    private ProgressHandle svgExportProgressHandle;

    public SimpleTestGraficsTopComponent() {
        cbmDataSet = new DefaultComboBoxModel<>();
        initComponents();
        setName(Bundle.CTL_PlotTopComponent());
        setToolTipText(Bundle.HINT_PlotTopComponent());
        ChartPanel panel = CreatePlots.createInfPlot(createSamplePoints(500), "X", "Y", new ToolTip());
        panel.addChartMouseListener(mouseAction);
        plotPanel.add(panel);
        plotPanel.updateUI();

    }

    public SimpleTestGraficsTopComponent(DeAnalysisHandler analysisHandler, DeAnalysisHandler.Tool usedTool) {
        this.analysisHandler = analysisHandler;
        this.usedTool = usedTool;
        results = analysisHandler.getResults();
        List<String> descriptions = new ArrayList<>();
        for (Iterator<ResultDeAnalysis> it = results.iterator(); it.hasNext();) {
            ResultDeAnalysis currentResult = it.next();
            descriptions.add(currentResult.getDescription());
        }
        cbmDataSet = new DefaultComboBoxModel<>(descriptions.toArray(new String[descriptions.size()]));
        initComponents();
        setName(Bundle.CTL_PlotTopComponent());
        setToolTipText(Bundle.HINT_PlotTopComponent());
    }

    public Map<PersistantFeature, Pair<Double, Double>> createSamplePoints(int n) {
        Random r = new Random(System.nanoTime());
        Map<PersistantFeature, Pair<Double, Double>> points = new HashMap<>();
        for (int i = 0; i < n; i++) {
            PersistantFeature dummyFeature = new PersistantFeature(0, "", "", "", "", 0, 0, true, FeatureType.ANY, "");
            double random = Math.random();
            if (random > 0.95) {
                points.put(dummyFeature, new Pair<>(r.nextDouble() * 256.0d, Double.POSITIVE_INFINITY));
                points.put(dummyFeature, new Pair<>(r.nextDouble() * 256.0d, Double.NEGATIVE_INFINITY));
            } else {
                points.put(dummyFeature, new Pair<>(2 * i + (r.nextGaussian() - 0.5d), r.nextDouble() * 256.0d));
            }
        }
        PersistantFeature dummyFeature = new PersistantFeature(0, "", "", "", "", 0, 0, true, FeatureType.ANY, "");
        points.put(dummyFeature, new Pair<>(200d, 300d));
        dummyFeature = new PersistantFeature(0, "", "", "", "", 0, 0, true, FeatureType.ANY, "");
        points.put(dummyFeature, new Pair<>(100d, Double.POSITIVE_INFINITY));
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
        jLabel2 = new javax.swing.JLabel();
        dataSetComboBox = new javax.swing.JComboBox();
        plotTypeComboBox = new javax.swing.JComboBox();
        createPlotButton = new javax.swing.JButton();
        plotPanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        iSymbol = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimpleTestGraficsTopComponent.class, "SimpleTestGraficsTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimpleTestGraficsTopComponent.class, "SimpleTestGraficsTopComponent.jLabel2.text")); // NOI18N

        dataSetComboBox.setModel(cbmDataSet);

        plotTypeComboBox.setModel(cbmPlotType);

        org.openide.awt.Mnemonics.setLocalizedText(createPlotButton, org.openide.util.NbBundle.getMessage(SimpleTestGraficsTopComponent.class, "SimpleTestGraficsTopComponent.createPlotButton.text")); // NOI18N
        createPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPlotButtonActionPerformed(evt);
            }
        });

        plotPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        plotPanel.setLayout(new javax.swing.BoxLayout(plotPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(SimpleTestGraficsTopComponent.class, "SimpleTestGraficsTopComponent.saveButton.text")); // NOI18N
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
        messages.setRows(2);
        messages.setWrapStyleWord(true);
        messages.setBorder(null);
        jScrollPane1.setViewportView(messages);

        iSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cebitec/vamp/differentialExpression/plot/i.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iSymbol, org.openide.util.NbBundle.getMessage(SimpleTestGraficsTopComponent.class, "SimpleTestGraficsTopComponent.iSymbol.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataSetComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .addComponent(jLabel2)
                            .addComponent(plotTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(createPlotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(iSymbol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plotTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(createPlotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 240, Short.MAX_VALUE)
                        .addComponent(iSymbol))
                    .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPlotButtonActionPerformed
        progressHandle.start();
        progressHandle.switchToIndeterminate();
        plotPanel.removeAll();
        messages.setText("");
        PlotTypes type = (PlotTypes) cbmPlotType.getSelectedItem();
        int index = dataSetComboBox.getSelectedIndex();
        final ResultDeAnalysis result = results.get(index);
        switch (type) {
            case MA_Plot:
                chartPanel = CreatePlots.createInfPlot(ConvertData.createMAvalues(result, usedTool, null, null), "A", "M", new ToolTip());
                chartPanel.addChartMouseListener(mouseAction);
                plotPanel.add(chartPanel);
                plotPanel.updateUI();
                break;
            case RatioAB_Confidence:
                chartPanel = CreatePlots.createPlot(ConvertData.ratioABagainstConfidence(result), "ratioAB", "Confidence", new ToolTip());
                chartPanel.addChartMouseListener(mouseAction);
                plotPanel.add(chartPanel);
                plotPanel.updateUI();
                break;
            case RatioBA_Confidence:
                chartPanel = CreatePlots.createPlot(ConvertData.ratioBAagainstConfidence(result), "ratioBA", "Confidence", new ToolTip());
                chartPanel.addChartMouseListener(mouseAction);
                plotPanel.add(chartPanel);
                plotPanel.updateUI();
                break;
        }
        saveButton.setEnabled(true);
        progressHandle.switchToDeterminate(100);
        progressHandle.finish();
    }//GEN-LAST:event_createPlotButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        VampFileChooser fc = new VampFileChooser(new String[]{"svg"}, "svg") {
            private static final long serialVersionUID = 1L;

            @Override
            public void save(String fileLocation) {
                saveToSVG(fileLocation);
            }

            @Override
            public void open(String fileLocation) {
            }
        };
        fc.openFileChooser(VampFileChooser.SAVE_DIALOG);
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createPlotButton;
    private javax.swing.JComboBox dataSetComboBox;
    private javax.swing.JLabel iSymbol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
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
        if (analysisHandler != null) {
            analysisHandler.removeObserver(this);
        }
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

    public static enum PlotTypes {

        MA_Plot("MA Plot"), RatioAB_Confidence("Ratio A/B against Confidence"),
        RatioBA_Confidence("Ratio B/A against Confidence");
        private String name;

        private PlotTypes(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
