package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.RPKMvalue;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javax.swing.JPanel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Class for displaying a histogram of RPKM values.
 * 
 * @author Martin Tötsches, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ResultHistogramRPKM extends javax.swing.JPanel implements ComponentListener {
    
    private static final long serialVersionUID = 1L;

    private JFXPanel fxPanel;
    private AppPanelTopComponent appPanelTopComponent;
    private JPanel mainPanel;
    private List<RPKMvalue> rpkmValues;
    private BarChart<String, Number> barChart;
    private BorderPane border;
    private Scene scene;

    /**
     * Class for displaying a histogram of RPKM values.
     * @param rpkmValues list of RPKM values to display
     */
    public ResultHistogramRPKM(List<RPKMvalue> rpkmValues) {
        this.rpkmValues = rpkmValues;
        this.appPanelTopComponent = new AppPanelTopComponent();
        this.appPanelTopComponent.setLayout(new BorderLayout());
        initSwingComponents();
        initFxComponents();
        this.appPanelTopComponent.addComponentListener(this);
        this.appPanelTopComponent.open();
        this.appPanelTopComponent.setName("Histogram of RPKM values");
    }

    private void initSwingComponents() {
        mainPanel = new JPanel(new BorderLayout());
        fxPanel = new JFXPanel();
        mainPanel.setSize(300, 300);
        mainPanel.add(fxPanel, BorderLayout.CENTER);
        this.appPanelTopComponent.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes all javafx components.
     */
    private void initFxComponents() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                border = new BorderPane();
                scene = new Scene(border, 1200, 600);
                
                double max = 0;
                double min = Integer.MAX_VALUE;
                for (int j = 0; j < rpkmValues.size(); j++) {
                    if (rpkmValues.get(j).getRPKM() < min) {
                        min = rpkmValues.get(j).getRPKM();
                    }
                    if (rpkmValues.get(j).getRPKM() >= max) {
                        max = rpkmValues.get(j).getRPKM();
                    }
                }
                double shift = max / 20;
                int[] intervals = new int[21]; //intervals of bars that are shown later
                for (int l = 0; l < intervals.length; l++) {
                    intervals[l] = 0;
                }
                for (int k = 0; k < rpkmValues.size(); k++) {
                    double value = rpkmValues.get(k).getRPKM();
                    int index = (int) Math.floor(value / shift);
                    intervals[index]++;
                }
                max = Math.log(rpkmValues.size());
                NumberAxis lineYAxis = new NumberAxis(0, max, 2);
                lineYAxis.setLabel("Number of Features (Log scale)");
                CategoryAxis lineXAxis = new CategoryAxis();
                lineXAxis.setLabel("RPKM Values");
                barChart = new BarChart<>(lineXAxis, lineYAxis);
                XYChart.Series<String, Number> bar = new XYChart.Series<>();
                bar.setName("RPKM Values");
                double start = 0.0;
                /*for (int i = 0; i < rpkmValues.size(); i++) {
                    double rpkm = rpkmValues.get(i).getRPKM();
                    String name = rpkmValues.get(i).getFeature().getFeatureName();
                    //bar.getData().add(getData(rpkm, name));
                    XYChart.Data o = getData(rpkm, name);
                    /*o.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent t) {
                            System.out.println("MouseEvent!!");
                        }
                    
                    });
                    bar.getData().add(o);
                }*/
                
                for (int i = 0; i < intervals.length; i++) {
                    int end = (int) (start + shift);
                    String name = (int) start + " - " + end;
                    double logValue = Math.log(intervals[i]);
                    logValue = logValue == 0 ? 0.1 : logValue;
                    XYChart.Data<String, Number> entry = new XYChart.Data<String, Number>(name, logValue);
                    entry.setExtraValue(intervals[i]);
                    bar.getData().add(entry);
                    start += shift;
                }
                barChart.getData().addAll(bar);
                
                for (XYChart.Series<String, Number> series : barChart.getData()) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        Tooltip.install(data.getNode(), new Tooltip("# features: " + data.getExtraValue().toString()));
//                        this.addLabelToEntry(data, data.getExtraValue().toString());
                    }
                }
                
                border.setCenter(barChart);
                fxPanel.setScene(scene);
                exportPanel(fxPanel);
                Platform.setImplicitExit(false);
            }

//            /**
//             * Adds a label to a data entry of a javafx chart. The label is able
//             * to reposition and resize, depending on change events of their
//             * node.
//             */
//            private void addLabelToEntry(XYChart.Data<String, Number> entry, String value) {
//                final Node node = entry.getNode();
//                final Text dataText = new Text(String.valueOf(value));
//                node.parentProperty().addListener(new ChangeListener<Parent>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
//                        Group parentGroup = (Group) parent;
//                        parentGroup.getChildren().add(dataText);
//                    }
//                });
//
//                node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
//                        dataText.setLayoutX(
//                                Math.round(bounds.getMinX() + bounds.getWidth() / 2 - dataText.prefWidth(-1) / 2));
//                        dataText.setLayoutY(
//                                Math.round(bounds.getMinY() - dataText.prefHeight(-1) * 0.5));
//                    }
//                });
//            }
        });

    }

    /**
     * @return The complete bar chart of this histogram.
     */
    public BarChart<String, Number> getBarChart() {
        return this.barChart;
    }

    public void takeSnapshot() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /*WritableImage image = barChart.snapshot(new SnapshotParameters(), null);
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showSaveDialog(null);
                File file = chooser.getSelectedFile();
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                } catch (IOException e) {
                    // TODO: handle exception here
                }*/
                
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = domImpl.createDocument(svgNS, "svg", null);
                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                appPanelTopComponent.paint(svgGenerator);
                try {
                    OutputStream file = new FileOutputStream("snapshot.svg");
                    Writer out = new OutputStreamWriter(file, "UTF-8");
                    svgGenerator.stream(out, false);
                } catch (UnsupportedEncodingException | SVGGraphics2DIOException | FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private void exportPanel(JFXPanel fxPanel) {
        this.fxPanel = fxPanel;
    }
    
    public void close() {
        this.appPanelTopComponent.close();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.fxPanel.validate();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        
    }

    @Override
    public void componentShown(ComponentEvent e) {
        
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        
    }
}
