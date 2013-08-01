package de.cebitec.vamp.plotting;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author kstaderm
 */
public class PlottingUtils {
       
    public static synchronized void exportChartAsSVG(Path file, JFreeChart chart) throws IOException {
        Rectangle bounds = new Rectangle(1920, 1080);
        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        Document document = dom.createDocument(null, "svg", null);
        SVGGraphics2D generator = new SVGGraphics2D(document);
        chart.draw(generator, bounds);
        try (OutputStream outputStream = Files.newOutputStream(file, StandardOpenOption.TRUNCATE_EXISTING)) {
            Writer out = new OutputStreamWriter(outputStream, "UTF-8");
            generator.stream(out, true);
            outputStream.flush();
        }
    }
}
