package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.plotting.api.dataset.INamedElementProvider;
import de.cebitec.vamp.util.Pair;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public class ConvertData {

    public static synchronized INamedElementProvider<List<Point2D>, PersistantFeature> mAplotData(Vector<Vector> resultTable, DeAnalysisHandler.Tool usedTool) {
        INamedElementProvider<List<Point2D>, PersistantFeature> ret = null;
        switch (usedTool) {
            case BaySeq:
                ret = convertBaySeqResults(resultTable);
                break;
            case DeSeq:
                ret = convertDESeqResults(resultTable);
                break;
            case SimpleTest:
                ret = convertSimpleTestResults(resultTable);
                break;
        }
        return ret;
    }

    private static INamedElementProvider<List<Point2D>, PersistantFeature> convertBaySeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static INamedElementProvider<List<Point2D>, PersistantFeature> convertDESeqResults(Vector<Vector> resultTable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static INamedElementProvider<List<Point2D>, PersistantFeature> convertSimpleTestResults(Vector<Vector> resultTable) {
        ElementProvider elementProvider = new ElementProvider();
        for (Iterator<Vector> it = resultTable.iterator(); it.hasNext();) {
            Vector row = it.next();
            PersistantFeature feature = (PersistantFeature) row.get(0);
            Double R = (Double) row.get(3);
            Double G = (Double) row.get(5);
            Pair<Double, Double> MA = createMAvalues(G, R);
            elementProvider.addData(new Point2D.Double(R, G), feature);
        }
        return elementProvider;
    }

    private static Pair<Double, Double> createMAvalues(Double G, Double R) {
        Double M = (Math.log(R) / Math.log(2)) - (Math.log(G) / Math.log(2));
        Double A = ((Math.log(R) / Math.log(2)) + (Math.log(G) / Math.log(2))) / 2;
        return new Pair<>(M, A);
    }

    public static class ElementProvider implements INamedElementProvider<List<Point2D>, PersistantFeature> {

        private List<Point2D> source = new ArrayList<>();
        private List<PersistantFeature> target = new ArrayList<>();

        @Override
        public List<Point2D> getSource() {
            return source;
        }

        @Override
        public Comparable<?> getKey() {
            return "MA data set";
        }

        @Override
        public void setKey(Comparable<?> key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int size() {
            return source.size();
        }

        @Override
        public PersistantFeature get(int i) {
            return target.get(i);
        }

        @Override
        public List<PersistantFeature> get(int start, int stop) {
            return target.subList(start, stop);
        }

        @Override
        public void reset() {
            source.clear();
            target.clear();
        }

        public void addData(Point2D point, PersistantFeature feature) {
            source.add(point);
            target.add(feature);
        }
    }
}
