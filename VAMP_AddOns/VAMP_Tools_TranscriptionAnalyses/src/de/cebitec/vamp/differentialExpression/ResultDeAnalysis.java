package de.cebitec.vamp.differentialExpression;

import java.util.Vector;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class ResultDeAnalysis {

    private String description;
    private RVector rawTableContents;
    private Vector<Vector> tableContents = null;
    private REXP rawColNames;
    private Vector colNames = null;
    private REXP rawRowNames;
    private Vector rowNames = null;
    private DeAnalysisData dEAdata;

    public ResultDeAnalysis(RVector tableContents, REXP colnames, REXP rownames, String description, DeAnalysisData dEAdata) {
        rawTableContents = tableContents;
        rawColNames = colnames;
        rawRowNames = rownames;
        this.description = description;
        this.dEAdata = dEAdata;
    }

    public Vector<Vector> getTableContentsContainingRowNames() {
        Vector rnames = getRownames();
        Vector<Vector> data = getTableContents();
        for (int i = 0; i < rnames.size(); i++) {
            data.get(i).add(0, rnames.get(i));
        }
        return data;
    }

    public Vector<Vector> getTableContents() {
        if (tableContents == null) {
            tableContents = convertRresults(rawTableContents);
        }
        return tableContents;
    }

    public Vector getColnames() {
        if (colNames == null) {
            colNames = convertNames(rawColNames);
        }
        return colNames;
    }

    public Vector getRownames() {
        if (rowNames == null) {
            rowNames = convertNames(rawRowNames);
        }
        return rowNames;
    }

    public String getDescription() {
        return description;
    }

    /*
     * The manual array copy used in this method several times is intended!
     * This way the primitive data types are automatically converted to their
     * corresponding Object presentation.
     */
    private Vector convertNames(REXP currentValues) {
        int currentType = currentValues.getType();
        Vector current = new Vector();
        switch (currentType) {
            case REXP.XT_ARRAY_DOUBLE:
                double[] currentDoubleValues = currentValues.asDoubleArray();
                for (int j = 0; j < currentDoubleValues.length; j++) {
                    current.add(currentDoubleValues[j]);
                }
                break;
            case REXP.XT_ARRAY_INT:
                int[] currentIntValues = currentValues.asIntArray();
                for (int j = 0; j < currentIntValues.length; j++) {
                    current.add(currentIntValues[j]);
                }
                break;
            case REXP.XT_ARRAY_STR:
                String[] currentStringValues = currentValues.asStringArray();
                for (int j = 0; j < currentStringValues.length; j++) {
                    String name = currentStringValues[j];
                    if (dEAdata.existsPersistantFeatureForGNURName(name)) {
                        current.add(dEAdata.getPersistantFeatureByGNURName(name));
                    } else {
                        current.add(name);
                    }
                }
                break;
            case REXP.XT_ARRAY_BOOL_INT:
                int[] currentBoolValues = currentValues.asIntArray();
                for (int j = 0; j < currentBoolValues.length; j++) {
                    current.add(currentBoolValues[j] == 1);
                }
                break;
            case REXP.XT_FACTOR:
                RFactor factor = currentValues.asFactor();
                for (int j = 0; j < factor.size(); j++) {
                    current.add(factor.at(j));
                }
                break;
        }
        return current;
    }

    private Vector<Vector> convertRresults(RVector currentRVector) {
        Vector<Vector> current = new Vector<>();
        for (int i = 0; i < currentRVector.size(); i++) {
            REXP currentValues = currentRVector.at(i);
            Vector converted = convertNames(currentValues);
            for (int j = 0; j < converted.size(); j++) {
                try {
                    current.get(j);
                } catch (ArrayIndexOutOfBoundsException e) {
                    current.add(new Vector());
                }
                current.get(j).add(converted.get(j));
            }
        }
        return current;
    }
}
