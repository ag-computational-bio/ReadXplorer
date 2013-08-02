package de.cebitec.vamp.plotting;

import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author kstaderm
 */
public class ChartExporter implements Runnable, Observable {

    private Path file;
    private JFreeChart chart;
    private List<Observer> obs = new ArrayList<>();
    
    public enum ChartExportStatus{
        RUNNING, FINISHED, FAILED;
    }

    public ChartExporter(Path file, JFreeChart chart) {
        this.chart = chart;
        this.file = file;
    }

    @Override
    public void run() {
        notifyObservers(ChartExportStatus.RUNNING);
        Rectangle bounds = new Rectangle(1920, 1080);
        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        Document document = dom.createDocument(null, "svg", null);
        SVGGraphics2D generator = new SVGGraphics2D(document);
        chart.draw(generator, bounds);
        try (OutputStream outputStream = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
            Writer out = new OutputStreamWriter(outputStream, "UTF-8");
            generator.stream(out, true);
            outputStream.flush();
            notifyObservers(ChartExportStatus.FINISHED);
        } catch (IOException ex) {
            notifyObservers(ChartExportStatus.FAILED);
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        obs.add(observer);
        
    }

    @Override
    public void removeObserver(Observer observer) {
        obs.remove(observer);
        
    }

    @Override
    public void notifyObservers(Object data) {
        for (Iterator<Observer> it = obs.iterator(); it.hasNext();) {
            Observer observer = it.next();
            observer.update(data);
        }
    }
}
