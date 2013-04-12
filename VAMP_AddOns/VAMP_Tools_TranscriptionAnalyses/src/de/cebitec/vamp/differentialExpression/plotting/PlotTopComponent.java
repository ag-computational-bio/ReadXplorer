/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.differentialExpression.plotting;

import de.cebitec.vamp.differentialExpression.ConvertData;
import de.cebitec.vamp.differentialExpression.DeAnalysisHandler;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Pair;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//de.cebitec.vamp.differentialExpression.plotting//Plot//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "PlotTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "de.cebitec.vamp.differentialExpression.plotting.PlotTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PlotAction",
        preferredID = "PlotTopComponent")
@Messages({
    "CTL_PlotAction=Plot",
    "CTL_PlotTopComponent=Plot Window",
    "HINT_PlotTopComponent=This is a Plot window"
})
public final class PlotTopComponent extends TopComponent implements Observer {

    private DeAnalysisHandler analysisHandler;

    public PlotTopComponent() {
    }

    public PlotTopComponent(DeAnalysisHandler analysisHandler) {
        initComponents();
        setName(Bundle.CTL_PlotTopComponent());
        setToolTipText(Bundle.HINT_PlotTopComponent());
        this.analysisHandler = analysisHandler;
    }

    public void addData(List<Pair<Double, Double>> coordinates) {
        XYSeries series = new XYSeries("DE data");
        double lowerXbound = 0;
        double higherXbound = 0;
        double lowerYbound = 0;
        double higherYbound = 0;
        for (int i = 0; i < coordinates.size(); i++) {
            Pair<Double, Double> pair = coordinates.get(i);

            Double x = pair.getSecond();
            Double y = pair.getFirst();

            if (!x.isInfinite()) {
                if (x > higherXbound) {
                    higherXbound = x;
                }
                if (x < lowerXbound) {
                    lowerXbound = x;
                }
            }
            if (!y.isInfinite()) {
                if (y > higherYbound) {
                    higherYbound = y;
                }
                if (y < lowerYbound) {
                    lowerYbound = y;
                }
            }
            if (!x.isInfinite() && !y.isInfinite()) {
                series.add(x, y);
            }
        }
        TestAxis domainAxis = new TestAxis("A");
        domainAxis.setAutoRangeIncludesZero(true);
        TestAxis rangeAxis = new TestAxis("M");
        rangeAxis.setAutoRangeIncludesZero(true);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(series);
        XYPlot plot = new XYPlot(xySeriesCollection, domainAxis, rangeAxis, renderer);
        plot.setDomainGridlineStroke(new BasicStroke(0f));
        plot.setRangeGridlineStroke(new BasicStroke(0f));
        final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(680, 455));
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);
        jPanel1.setLayout(new BorderLayout());
        jPanel1.add(panel, BorderLayout.CENTER);
        jPanel1.updateUI();
        this.updateUI();
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
        typeOfPlotComboBox = new javax.swing.JComboBox();
        plotButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 680, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 455, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PlotTopComponent.class, "PlotTopComponent.jLabel1.text")); // NOI18N

        typeOfPlotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(plotButton, org.openide.util.NbBundle.getMessage(PlotTopComponent.class, "PlotTopComponent.plotButton.text")); // NOI18N
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PlotTopComponent.class, "PlotTopComponent.jButton1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(typeOfPlotComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(plotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeOfPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(plotButton)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void plotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotButtonActionPerformed
        addData(ConvertData.mAplotData(analysisHandler.getResults().get(0).getTableContents(), DeAnalysisHandler.Tool.DeSeq));
    }//GEN-LAST:event_plotButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton plotButton;
    private javax.swing.JComboBox typeOfPlotComboBox;
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

    @Override
    public void update(Object args) {
    }

    public class TestAxis extends NumberAxis {

        public TestAxis(String label) {
            super(label);
        }

        @Override
        public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
                Rectangle2D dataArea, RectangleEdge edge,
                PlotRenderingInfo plotState) {
            
            double x = dataArea.getX();
            double y = dataArea.getY()+25;
            double height = dataArea.getHeight()-50;
            double width = dataArea.getWidth();
            dataArea.setFrame(x, y, width, height);
            AxisState state = null;
            // if the axis is not visible, don't draw it...
            if (!isVisible()) {
                state = new AxisState(cursor);
                // even though the axis is not visible, we need ticks for the
                // gridlines...
                List ticks = refreshTicks(g2, state, dataArea, edge);
                state.setTicks(ticks);
                return state;
            }

            // draw the tick marks and labels...
            state = drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);

            // draw the axis label...
            state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
            createAndAddEntity(cursor, state, dataArea, edge, plotState);
            return state;

        }
    }
}
