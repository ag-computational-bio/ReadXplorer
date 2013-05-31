package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.plotting.api.XYChartBuilder;
import de.cebitec.vamp.plotting.api.dataset.DatasetUtils;
import de.cebitec.vamp.plotting.api.dataset.INamedElementProvider;
import de.cebitec.vamp.plotting.api.dataset.Numeric1DDataset;
import de.cebitec.vamp.plotting.ui.XYChartTopComponent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.jfree.chart.axis.InfNumberAxis;
import org.jfree.chart.axis.renderer.xy.InfXYLineAndShapeRenderer;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class InitializePlotting {

    public static void open(final INamedElementProvider<? extends List<Point2D>, ? extends PersistantFeature> elements) {
        Task t = RequestProcessor.getDefault().create(new Runnable() {
            @Override
            public void run() {
//                final Numeric1DDataset<Point2D> dataset = DatasetUtils.createDataset();
                final List<INamedElementProvider<? extends List<Point2D>, ? extends PersistantFeature>> l = new ArrayList<>();
                l.add(elements);
                final Numeric1DDataset<PersistantFeature> dataset = new Numeric1DDataset<>(l);
                final XYChartBuilder builder = new XYChartBuilder();
                InfXYLineAndShapeRenderer renderer = new InfXYLineAndShapeRenderer(false, true);
                renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
                renderer.setBaseItemLabelsVisible(true);
                InfNumberAxis range = new InfNumberAxis("y-axis");
                range.setLowerBound(-1000);
                range.setUpperBound(1000);
                range.setAutoRange(false);
                builder.xy(dataset).renderer(renderer).rangeAxis(range).plot().chart("Sample plot").createLegend(true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TopComponent tc = WindowManager.getDefault().findTopComponent("navigatorTC");
                        if (tc != null) {
                            tc.open();
                        }
                    }
                });
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        XYChartTopComponent<PersistantFeature> xytc = new XYChartTopComponent<>(PersistantFeature.class, dataset, builder);
                        xytc.open();
                        xytc.requestActive();
                    }
                });

            }
        });
        RequestProcessor.getDefault().post(t);
    }
}
